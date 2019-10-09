/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.common

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.BusinessLogicValidationException
import net.hedtech.banner.i18n.MessageHelper
import oracle.jdbc.OracleCallableStatement
import oracle.jdbc.OracleConnection
import org.grails.web.converters.exceptions.ConverterException
import org.springframework.security.core.context.SecurityContextHolder

import java.sql.Clob
import java.sql.DatabaseMetaData
import java.sql.SQLException
import java.sql.Types

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
@Transactional
@Slf4j
class GeneralSqlJsonService {
    def sessionFactory
    private static final String PACKAGE_NAME = 'SS_ACC'
    private static final String CONTEXT_NAME = 'LOG_ID'

    /**
     * @param procedureName Name of the procedure
     * @param inputParamsList List of maps with these information - paramValue, paramType, paramName
     * Valid paramType values are - 'string', 'int', 'ident_arr', 'vc_arr'
     * @return Data as JSON
     */
    def executeProcedure(String procedureName, def inputParamsList = null) {
        JSON json_data
        OracleCallableStatement callableStatement = getCallableStatement(procedureName, inputParamsList)
        int size = inputParamsList ? inputParamsList.size() : 0
        try{
            callableStatement?.executeQuery()
            Clob json_clob = callableStatement?.getClob(size + 1)
            String json_string = json_clob?.characterStream?.text
            json_data = new JsonSlurper().parseText(json_string)
        }catch(SQLException | ConverterException e){
            log.error 'Exception in GeneralSqlJsonService.executeProcedure ${e}'
            String message = MessageHelper.message('default.unknown.banner.api.exception')
            throw new ApplicationException(GeneralSqlJsonService, new BusinessLogicValidationException(message, []))
        }
        json_data
    }

    private def getCallableStatement(String procedureName, def inputParamsList) {
        def connection = sessionFactory.currentSession.connection()
        DatabaseMetaData metadata = connection.getMetaData()
        def oraconnection = metadata.getConnection().unwrap(OracleConnection.class)
        def loggedInUser = SecurityContextHolder?.context?.authentication?.principal?.pidm
        String procedureStmt = getProcedureStatement(procedureName, inputParamsList)
        String plSqlBlock = getJsonSqlString(procedureStmt, loggedInUser)
        OracleCallableStatement callableStatement = bindParameters(oraconnection, plSqlBlock, inputParamsList)
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

    private def getJsonSqlString(procedure, pidm) {
        String sql = """
                DECLARE
                    lv_json_out clob;
                BEGIN
                    gb_common.p_set_context('${PACKAGE_NAME}', '${CONTEXT_NAME}', ${pidm}, 'N');
                    gokjson.initialize_clob_output;
                    gokjson.open_object;
                    ${procedure};
                    gokjson.close_object;
                    lv_json_out := gokjson.get_clob_output;
                    gokjson.free_output;
                    gb_common.p_set_context('${PACKAGE_NAME}', '${CONTEXT_NAME}', '');
                    ? := lv_json_out;
                END;"""
        sql
    }

    private def bindParameters(def oraConnection, def plSqlBlock, def inputParamsList) {
        OracleCallableStatement callableStatement = (OracleCallableStatement) oraConnection.prepareCall(plSqlBlock)
        int size = inputParamsList ? inputParamsList.size() : 0
        for (int i = 1; i <= size; i++) {
            def inputParam = inputParamsList[i - 1]
            switch (inputParam?.paramType?.toLowerCase()) {
                case 'string':
                    (inputParam.paramValue) ?
                            callableStatement.setString(i, inputParam.paramValue) :
                            callableStatement.setNull(i, Types.VARCHAR)
                    break
                case 'int':
                    (inputParam.paramValue) ?
                            callableStatement.setInt(i, inputParam.paramValue as int) :
                            callableStatement.setNull(i, Types.INTEGER)
                    break
                case 'ident_arr':
                    String[] identArray = inputParam.paramValue ? inputParam.paramValue.toArray(new String[0]) : new String[0]
                    callableStatement.setPlsqlIndexTable(i, identArray, identArray?.length, identArray?.length, PlsqlDataType.IDENT_ARR.sqlType, PlsqlDataType.IDENT_ARR.maxLen)
                    break
                case 'vc_arr':
                    String[] vcArray = inputParam.paramValue ? inputParam.paramValue.toArray(new String[0]) : new String[0]
                    callableStatement.setPlsqlIndexTable(i, vcArray, vcArray?.length, vcArray?.length, PlsqlDataType.VC_ARR.sqlType, PlsqlDataType.VC_ARR.maxLen)
                    break
                default:
                    log.error("Unsupported Type")
            }
        }
        callableStatement.registerOutParameter(size + 1, java.sql.Types.CLOB)
        callableStatement
    }

}
