/*******************************************************************************
 Copyright 2020 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.common

import grails.gorm.transactions.Transactional
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.BusinessLogicValidationException
import net.hedtech.banner.i18n.MessageHelper
import oracle.jdbc.OracleCallableStatement
import oracle.jdbc.OracleConnection
import oracle.sql.NUMBER
import org.grails.web.converters.exceptions.ConverterException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.transaction.annotation.Propagation

import java.sql.Clob
import java.sql.DatabaseMetaData
import java.sql.SQLException
import java.sql.Types
import java.text.MessageFormat

/**
 * This is a common service to execute any 8x procedure that returns data as JSON.
 * This service -
 * creates a pl/sql anonymous block to execute procedure,
 * creates OracleCallableStatement and binds parameters of types - VARCHAR, INTEGER, OWA_UTIL.IDENT_ARR & OWA_UTIL.VC_ARR
 * executes the query and converts the clob output to JSON
 *
 * Note: Some 8x procedures accepts PL/SQL index-by table parameters (OWA_UTIL.IDENT_ARR & OWA_UTIL.VC_ARR). To bind such parameters,
 * setPlsqlIndexTable() method defined in the OraclePreparedStatement and OracleCallableStatement classes is used.
 * JDBC native CallableStatement or Connection classes do not support binding parameters of PL/SQL data types. *
 * These APIs are specific to Oracle and needs to be modified when the database is migrated to anything else.
 */
@Transactional(readOnly = false, propagation = Propagation.REQUIRED )
@Slf4j
class GeneralSqlJsonService {
    def sessionFactory
    def springSecurityService
    private static final String PACKAGE_NAME = 'SS_ACC'
    private static final String CONTEXT_NAME = 'LOG_ID'
    private static final String STATELESS_INDICATOR = 'N'
    private static final String MESSAGE_TAGS = 'message_info'
    private static final String MESSAGE_TYPE = 'message_type'
    private static final String MESSAGES = 'messages'
    private static final String MESSAGE = 'message'
    private static final String MESSAGE_KEY = 'message_key'
    private static final String MESSAGE_ARGS = 'message_args'
    private static final String ARGUMENT = 'argument'
    private static final int OUTPUT_CLOB_INDEX_WITH_AUTH = 5
    private static final int OUTPUT_CLOB_INDEX_WITHOUT_AUTH = 1

    /**
     * @param procedureName Name of the procedure
     * @param inputParamsList List of maps with these information - paramValue, paramType, paramName
     * Valid paramType values are - 'string', 'int', 'ident_arr', 'vc_arr'
     * @return Data as JSON
     */
    def executeProcedure(String procedureName, def inputParamsList = null, boolean withAuthentication = true, boolean populateMessages = true) {
        def json_data, messages_data
        Clob json_clob
        String json_string
        OracleCallableStatement callableStatement = getCallableStatement(procedureName, inputParamsList, withAuthentication)
        int size = inputParamsList ? inputParamsList.size() : 0
        try {
            callableStatement?.executeQuery()
            int outputIndex = withAuthentication ? OUTPUT_CLOB_INDEX_WITH_AUTH : OUTPUT_CLOB_INDEX_WITHOUT_AUTH
            json_clob = callableStatement?.getClob(size + outputIndex)
            json_string = json_clob?.characterStream?.text
            json_data = new JsonSlurper().parseText(json_string)
            if(populateMessages) {
                messages_data = populateMessagesFromJson(json_data)
                if (messages_data.size() > 0) {
                    json_data << [messages: messages_data]
                }
            }
        } catch (SQLException | ConverterException e) {
            log.error "Exception in GeneralSqlJsonService.executeProcedure ${e}"
            String message = MessageHelper.message('default.unknown.banner.api.exception')
            throw new ApplicationException(GeneralSqlJsonService, new BusinessLogicValidationException(message, []))
        } finally {
            json_clob?.truncate(json_string?.length())
            json_clob?.free()
            json_clob = null
            callableStatement?.close()
        }
        json_data
    }

    private def getCallableStatement(String procedureName, def inputParamsList, boolean withAuthentication = true) {
        def connection = sessionFactory.currentSession.connection()
        def loggedInUser
        DatabaseMetaData metadata = connection.getMetaData()
        def oraConnection = metadata.getConnection().unwrap(OracleConnection.class)
        if (withAuthentication) {
            loggedInUser = SecurityContextHolder?.context?.authentication?.principal?.pidm
        }
        String procedureStmt = getProcedureStatement(procedureName, inputParamsList)
        String plSqlBlock = withAuthentication ? getJsonSqlString(procedureStmt) : getJsonSqlStringWithoutLogin(procedureStmt)
        OracleCallableStatement callableStatement = bindParameters(oraConnection, plSqlBlock, inputParamsList, loggedInUser, withAuthentication)
        callableStatement
    }

    private String getProcedureStatement(procedureName, def inputParamsList) {
        int numberOfParams = inputParamsList ? inputParamsList.size() : 0
        String procedureStmt = "${procedureName}("
        for (int i = 0; i < numberOfParams; i++) {
            procedureStmt = "${procedureStmt}${inputParamsList[i].paramName}=>?"
            if (i < numberOfParams - 1) {
                procedureStmt = "${procedureStmt},"
            }
        }
        procedureStmt = "${procedureStmt})"
        procedureStmt
    }

    private def getJsonSqlString(procedure) {
        String sql = """
                DECLARE
                    lv_json_out clob;
                BEGIN
                    gb_common.p_set_context(?,?,?,?);
                    gokjson.initialize_clob_output;
                    BEGIN
                      gokjson.open_object(with_exception => true);
                      ${procedure};
                      gokjson.close_object;
                    EXCEPTION
                      WHEN OTHERS THEN
                        gokjson.close_for_exception;
                        gokjson.put_exception_info(sqlcode, SQLERRM);
                        gokjson.close_object;
                    END;
                    BEGIN
                      lv_json_out := gokjson.get_clob_output;
                    EXCEPTION
                       WHEN OTHERS THEN
                         gokjson.free_output;
                         goksels.p_clear_user_context;
                    END;
                    gokjson.free_output;
                    goksels.p_clear_user_context;
                    ? := lv_json_out;
                END;"""
        sql
    }

    private def getJsonSqlStringWithoutLogin(procedure) {
        String sql = """
                DECLARE
                    lv_json_out clob;
                BEGIN                    
                    gokjson.initialize_clob_output;
                    BEGIN
                      gokjson.open_object(with_exception => true);
                      ${procedure};
                      gokjson.close_object;
                    EXCEPTION
                      WHEN OTHERS THEN
                        gokjson.close_for_exception;
                        gokjson.put_exception_info(sqlcode, SQLERRM);
                        gokjson.close_object;
                    END;
                    BEGIN
                    lv_json_out := gokjson.get_clob_output;
                    EXCEPTION
                       WHEN OTHERS THEN
                        gokjson.free_output;
                    END;    
                    gokjson.free_output;                   
                    ? := lv_json_out;
                END;"""
        sql
    }

    private def bindParameters(def oraConnection, def plSqlBlock, def inputParamsList, def pidm, boolean withAuth = true) {
        OracleCallableStatement callableStatement = (OracleCallableStatement) oraConnection.prepareCall(plSqlBlock)
        int size = inputParamsList ? inputParamsList.size() : 0
        //For DB Context Params
        int contextParamsIndex = 0
        if (withAuth) {
            callableStatement.setString(1, PACKAGE_NAME)
            callableStatement.setString(2, CONTEXT_NAME)
            callableStatement.setInt(3, pidm)
            callableStatement.setString(4, STATELESS_INDICATOR)
            contextParamsIndex = 4
        }

        for (int i = 1; i <= size; i++) {
            def inputParam = inputParamsList[i - 1]
            int inputParamsIndex = i + contextParamsIndex

            switch (inputParam?.paramType?.toLowerCase()) {
                case 'string':
                    (inputParam.paramValue != null) ?
                            callableStatement.setString(inputParamsIndex, inputParam.paramValue) :
                            callableStatement.setNull(inputParamsIndex, Types.VARCHAR)
                    break
                case 'int':
                    (inputParam.paramValue != null) ?
                            callableStatement.setInt(inputParamsIndex, inputParam.paramValue as int) :
                            callableStatement.setNull(inputParamsIndex, Types.INTEGER)
                    break
                case 'number':
                    (inputParam.paramValue != null) ?
                            callableStatement.setNUMBER(inputParamsIndex, new NUMBER(inputParam.paramValue)) :
                            callableStatement.setNull(inputParamsIndex, Types.INTEGER)
                    break
                case 'ident_arr':
                    String[] identArray = inputParam.paramValue ? inputParam.paramValue.toArray(new String[0]) : new String[0]
                    callableStatement.setPlsqlIndexTable(inputParamsIndex, identArray, identArray?.length, identArray?.length, PlsqlDataType.IDENT_ARR.sqlType, PlsqlDataType.IDENT_ARR.maxLen)
                    break
                case 'vc_arr':
                    String[] vcArray = inputParam.paramValue ? inputParam.paramValue.toArray(new String[0]) : new String[0]
                    callableStatement.setPlsqlIndexTable(inputParamsIndex, vcArray, vcArray?.length, vcArray?.length, PlsqlDataType.VC_ARR.sqlType, PlsqlDataType.VC_ARR.maxLen)
                    break
                case 'vc_tab_type':
                    String[] vcTabType = inputParam.paramValue ? inputParam.paramValue.toArray(new String[0]) : new String[0]
                    callableStatement.setPlsqlIndexTable(inputParamsIndex, vcTabType, vcTabType?.length, vcTabType?.length, PlsqlDataType.TAB_TYPE.sqlType, PlsqlDataType.TAB_TYPE.maxLen)
                    break
                case 'char_arr':
                    String[] char_arr = inputParam.paramValue ? inputParam.paramValue.toArray(new String[0]) : new String[0]
                    callableStatement.setPlsqlIndexTable(inputParamsIndex, char_arr, char_arr?.length, char_arr?.length, PlsqlDataType.CHAR_ARR.sqlType, PlsqlDataType.CHAR_ARR.maxLen)
                    break
                case 'vc_table_type':
                    String[] vcTableType = inputParam.paramValue ? inputParam.paramValue.toArray(new String[0]) : new String[0]
                    callableStatement.setPlsqlIndexTable(inputParamsIndex, vcTableType, vcTableType?.length, vcTableType?.length, PlsqlDataType.TABLE_TYPE.sqlType, PlsqlDataType.TABLE_TYPE.maxLen)
                    break
                default:
                    log.error("Unsupported Type")
            }
        }
        int outputIndex = withAuth ? OUTPUT_CLOB_INDEX_WITH_AUTH : OUTPUT_CLOB_INDEX_WITHOUT_AUTH
        callableStatement.registerOutParameter(size + outputIndex, java.sql.Types.CLOB)
        callableStatement
    }

    // Filters the messages from the given Json
    private def populateMessagesFromJson(tree, errors = [:]) {
        switch (tree) {
            case Map:
                tree.each { k, v ->
                    def key = k.toUpperCase()
                    if (key?.toLowerCase().startsWith(MESSAGE_TAGS)) {
                        def messageInfoObj = v
                        String messageType = messageInfoObj?."${MESSAGE_TYPE}"?.toLowerCase()
                        def messages = messageInfoObj?."${MESSAGES}"
                        messages?.each { messageObj ->
                            String message_key = messageObj?."${MESSAGE_KEY}", message = messageObj?."${MESSAGE}", default_message = message
                            def message_args = []
                            for (def argObj : messageObj?."${MESSAGE_ARGS}") {
                                message_args << MessageHelper.message(argObj?."${ARGUMENT}" as String)
                            }

                            if (message_key) {
                                message = MessageHelper.message(message_key, message_args as Object[])
                            }

                            if ((message == message_key || !message_key) && message_args.size()) {
                                message = MessageFormat.format(default_message, message_args as Object[])
                            }

                            if (errors.containsKey(messageType)) {
                                errors.get(messageType).add(message)
                            } else {
                                errors.put(messageType, [message])
                            }
                        }
                    }
                    populateMessagesFromJson(v, errors)
                }
                return errors
            case Collection:
                tree.each { e -> populateMessagesFromJson(e, errors) }
                return errors
            default:
                return errors
        }
    }

}
