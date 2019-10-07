/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.common

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import oracle.jdbc.OracleCallableStatement
import oracle.jdbc.OracleConnection
import org.springframework.security.core.context.SecurityContextHolder

import java.sql.Clob
import java.sql.DatabaseMetaData
import java.sql.Types

/**
 * Service to get Json Data from SQl.
 * Executes the wrapper function by putting PIDM into the context and returns the CLOB.
 * Clob is converted to JSON and returns it.
 */
@Transactional
@Slf4j
class GeneralSqlJsonService {
    def sessionFactory
    private static final String OUT_PARAMETER = 'json_out'
    private static final String PACKAGE_NAME = 'SS_ACC'
    private static final String CONTEXT_NAME = 'LOG_ID'

    /**
     *
     * @param procedureName Name of the procedure
     * @param inputParamsList List of maps with these information - paramValue, paramType, paramName
     * Valid paramType values are - 'string', 'int', 'ident_arr', 'vc_arr'
     * @return
     */
    def executeProcedure(String procedureName, def inputParamsList = null) {
        OracleCallableStatement callableStatement = getCallableStatement(procedureName, inputParamsList)
        callableStatement?.executeQuery()
        int size = inputParamsList ? inputParamsList.size() : 0
        Clob json_clob = callableStatement?.getClob(size + 1)
        String json_string = json_clob?.characterStream?.text
        JSON json_data = new JsonSlurper().parseText(json_string)
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

    private def getProcedureStatement(procedureName, def inputParamsList) {
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

    private def bindParameters(def oraconnection, def plSqlBlock, def inputParamsList) {
        OracleCallableStatement callableStatement = (OracleCallableStatement) oraconnection.prepareCall(plSqlBlock)
        int size = inputParamsList ? inputParamsList.size() : 0
        for (int i = 0; i < size; i++) {
            def inputParam = inputParamsList[i]
            switch (inputParam?.paramType?.toLowerCase()) {
                case 'string':
                    (inputParam.paramValue) ?
                            callableStatement.setString(i + 1, inputParam.paramValue) :
                            callableStatement.setNull(i + 1, Types.VARCHAR)
                    break
                case 'int':
                    (inputParam.paramValue) ?
                            callableStatement.setInt(i + 1, inputParam.paramValue as int) :
                            callableStatement.setNull(i + 1, Types.INTEGER)
                    break
                case 'ident_arr':
                    String[] identArray = inputParam.paramValue ? inputParam.paramValue.toArray(new String[0]) : new String[0]
                    callableStatement.setPlsqlIndexTable(i + 1, identArray, identArray?.length, identArray?.length, PlsqlDataType.IDENT_ARR.sqlType, PlsqlDataType.IDENT_ARR.maxLen)
                    break
                case 'vc_arr':
                    String[] vcArray = inputParam.paramValue ? inputParam.paramValue.toArray(new String[0]) : new String[0]
                    callableStatement.setPlsqlIndexTable(i + 1, vcArray, vcArray?.length, vcArray?.length, PlsqlDataType.VC_ARR.sqlType, PlsqlDataType.VC_ARR.maxLen)
                    break
                default:
                    log.error("Unsupported Type")
            }
        }
        callableStatement.registerOutParameter(size + 1, java.sql.Types.CLOB)
        callableStatement
    }

}
