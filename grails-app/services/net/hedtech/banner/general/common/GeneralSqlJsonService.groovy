/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.common

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.json.JsonSlurper
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.BusinessLogicValidationException
import net.hedtech.banner.i18n.MessageHelper
import oracle.jdbc.OracleCallableStatement
import oracle.jdbc.OracleConnection
import oracle.jdbc.OracleTypes
import org.grails.web.converters.exceptions.ConverterException
import org.hibernate.SessionFactory
import org.springframework.security.core.context.SecurityContextHolder

import java.sql.CallableStatement
import java.sql.Clob
import java.sql.DatabaseMetaData
import java.sql.SQLException

/**
 * Service to get Json Data from SQl.
 * Executes the wrapper function by putting PIDM into the context and returns the CLOB.
 * Clob is converted to JSON and returns it.
 */
@Transactional
@Slf4j
class GeneralSqlJsonService {
    def sessionFactory

    /**
     *
     * @param procedureName Name of the procedure
     * @param inputParamsList List of maps with these information - paramValue, paramType, paramIndex
     * Valid paramType values are - 'string', 'int', 'ident_arr', 'vc_arr'
     * @return
     */
    def executeProcedure(String procedureName, def inputParamsList = null) {
        OracleCallableStatement callableStatement = getCallableStatement(procedureName, inputParamsList)
        callableStatement?.executeQuery()
        Clob json_clob = callableStatement?.getClob(inputParamsList?.size() + 1)
        String json_string = json_clob?.characterStream?.text
        JSON json_data = new JsonSlurper().parseText(json_string)
        json_data
    }

    private def getCallableStatement(String procedureName, def inputParamsList) {
        def connection = sessionFactory.currentSession.connection()
        DatabaseMetaData metadata = connection.getMetaData()
        def oraconnection = metadata.getConnection().unwrap(OracleConnection.class)
        def loggedInUser = SecurityContextHolder?.context?.authentication?.principal?.pidm
        int numberOfParams = inputParamsList ? inputParamsList.size() : 0
        String procedureStmt = getProcedureStatement(procedureName, numberOfParams)
        String plSqlBlock = getJsonSqlString(procedureStmt, loggedInUser)
        OracleCallableStatement callableStatement = bindParameters(oraconnection, plSqlBlock, inputParamsList)
        callableStatement
    }

    private def getProcedureStatement(procedureName, int numberOfParams) {
        String procedureStmt = "${procedureName}("
        for (int i = 0; i < numberOfParams; i++) {
            procedureStmt = "${procedureStmt}?,"
        }
        procedureStmt = procedureStmt.replaceLast(',', '')
        procedureStmt = "${procedureStmt})"
        procedureStmt
    }

    private def getJsonSqlString(procedure, pidm) {
        String sql = """
                DECLARE
                    lv_json_out clob;
                BEGIN
                    gb_common.p_set_context('SS_ACC', 'LOG_ID', ${pidm}, 'N');
                    APEX_JSON.initialize_clob_output;
                    APEX_JSON.open_object;
                    ${procedure};
                    APEX_JSON.close_object;
                    lv_json_out := APEX_JSON.get_clob_output();
                    APEX_JSON.free_output;
                    gb_common.p_set_context('SS_ACC', 'LOG_ID', '');
                    ? := lv_json_out;
                END;"""
        sql
    }

    private def bindParameters(def oraconnection, def plSqlBlock, def inputParamsList) {
        OracleCallableStatement callableStatement = (OracleCallableStatement) oraconnection.prepareCall(plSqlBlock)
        int indexCounter = 1
        inputParamsList?.each { inputParam ->

            switch (inputParam?.paramType?.toLowerCase()) {
                case 'string':
                    callableStatement.setString(inputParam.paramIndex, inputParam.paramValue)
                    break
                case 'int':
                    callableStatement.setInt(inputParam.paramIndex, inputParam.paramValue)
                    break
                case 'ident_arr':
                    String[] identArray = inputParam.paramValue?.toArray(new String[0])
                    callableStatement.setPlsqlIndexTable(inputParam.paramIndex, identArray, identArray?.length, identArray?.length, OracleTypes.VARCHAR, 30)
                    break
                case 'vc_arr':
                    String[] vcArray = inputParam.paramValue?.toArray(new String[0])
                    callableStatement.setPlsqlIndexTable(inputParam.paramIndex, vcArray, vcArray?.length, vcArray?.length, OracleTypes.VARCHAR, 4000)
                    break
                default :
                    callableStatement.setString(inputParam.paramIndex, inputParam.paramValue)
                    break
            }
            indexCounter++
        }
        callableStatement.registerOutParameter(indexCounter, java.sql.Types.CLOB)
        callableStatement
    }

}
