/*********************************************************************************
  Copyright 2010-2013 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import groovy.sql.Sql
import net.hedtech.banner.general.system.EntriesForSql
import net.hedtech.banner.general.system.EntriesForSqlProcesss
import net.hedtech.banner.testing.BaseIntegrationTestCase
import net.hedtech.banner.exceptions.ApplicationException
import org.junit.After
import org.junit.Before
import org.junit.Test

class SqlProcessCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    def sqlProcessCompositeService


    @Before
    void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    @After
    void tearDown() {
        super.tearDown()
    }


    @Test
    void testSqlProcessCompositeServiceTestGetSqlProcessResultsNoBinds() {
        def params = [sqlCode: 'FACULTY', sqlProcessCode: 'INTCOMP']
        Sql db
        try {
            def results = sqlProcessCompositeService.getSqlProcessResults(params)
            db = new Sql(new Sql(sessionFactory.getCurrentSession().connection()))
            //assertEquals results, "testing"
            assertEquals results.size(),
                    db.rows("SELECT DISTINCT a.sibinst_pidm" +
                            "  FROM sibinst a," +
                            "       stvfcst" +
                            " WHERE a.sibinst_term_code_eff IN" +
                            "       (SELECT MAX(b.sibinst_term_code_eff)" +
                            "          FROM sibinst b," +
                            "               sobterm c" +
                            "         WHERE b.sibinst_pidm = a.sibinst_pidm" +
                            "           AND b.sibinst_term_code_eff" +
                            "                 <= c.sobterm_term_code" +
                            "           AND icgokcom.f_calc_valid_ldi_term(c.sobterm_term_code) = 'Y'" +
                            "         GROUP BY c.sobterm_term_code)" +
                            "   AND a.sibinst_fcst_code = stvfcst_code" +
                            "   AND stvfcst_active_ind = 'A'").size()
        }
        finally {
            db?.close()
        }
    }


    @Test
    void testSqlProcessCompositeServiceTestGetSqlProcessResultsBinds() {
        def params = [sqlCode: 'IAM_GOBEACC_RULE', sqlProcessCode: 'IAM', pidm: 32473, garbage: "randomGarbage"]
        Sql db
        try {
            def results = sqlProcessCompositeService.getSqlProcessResults(params)
            db = new Sql(new Sql(sessionFactory.getCurrentSession().connection()))
            //assertEquals results, "testing"
            assertEquals results.size(),
                    db.rows("SELECT GOBTPAC_EXTERNAL_USER PRINCIPAL, GOBTPAC_PIN CREDENTIAL, GOBTPAC_PIDM PIDM FROM GOBTPAC WHERE GOBTPAC.GOBTPAC_PIDM = :pidm", [pidm: 32473]).size()
        }
        finally {
            db?.close()
        }
    }


    @Test
    void testSqlProcessCompositeServiceTestGetSqlProcessResultsTerms() {
        def params = [sqlCode: 'DERIVE_TERM', sqlProcessCode: 'LDM', input_date: new java.sql.Date(new Date().time)]
        Sql db
        try {
            def results = sqlProcessCompositeService.getSqlProcessResultsFromHierarchy(params)
            db = new Sql(new Sql(sessionFactory.getCurrentSession().connection()))
            assertEquals results[0][0], "201410"

        }
        finally {
            db?.close()
        }
    }
    // TODO: Test bad cases.


    @Test
    void testGetProcessResultsHierarchPidm() {

        Sql db
        def testData
        try {
            db = new Sql(new Sql(sessionFactory.getCurrentSession().connection()))
            //assertEquals results, "testing"
            testData = db.rows("SELECT SPRIDEN_PIDM, SPRIDEN_ID FROM SPRIDEN where SPRIDEN_ID in ('STUAFR251', 'STUAFR252', 'STUAFR253')")
        }
        finally {
            db?.close()
        }

        // Create three identical processes that act on a pidm. It will return the spriden_id of the pereson with the pidm
        def entriesForSql = getEntriesForSql()
        def entriesForSqlProcess = getEntriesForSqlProcess()
        def resultMap = [:]
        int seqNo = 1
        testData.each{ it ->
            def validProcess = newValidForCreateSqlProcess(seqNo, it.SPRIDEN_ID, entriesForSql, entriesForSqlProcess, new Date()-1, true)
            validProcess.save(failOnError: true, flush: true)
            def key = "" + seqNo
            resultMap[key] = it.SPRIDEN_PIDM
            seqNo++
        }

        createSqlProcessParameter("INTEGRATION_TEST", "PIDM")

        for (def i=0;i<testData.size();i++) {
            def pidm = resultMap["" + (i+1)]

            def params = [sqlCode: "INTEGRATION_TEST", sqlProcessCode: "INTEGRATION_TEST", PIDM: pidm]

            shouldFail(ApplicationException) {
                sqlProcessCompositeService.getSqlProcessResultsFromHierarchy(params)
            }

        }
   }


    @Test
    void testGetProcessResultsHierarchTerm() {

        Sql db
        def testData = ["213013", "213014", "213015"]

        // Create three identical processes that act on a pidm. It will return the spriden_id of the pereson with the pidm
        def entriesForSql = getEntriesForSql()
        def entriesForSqlProcess = getEntriesForSqlProcess()
        int seqNo = 1
        testData.each{ it ->
            def validProcess = newValidTermForCreateSqlProcess(seqNo, it, entriesForSql, entriesForSqlProcess, new Date()-1, true)
            validProcess.save(failOnError: true, flush: true)
            seqNo++
        }

        createSqlProcessParameter("INTEGRATION_TEST", "TERM")

        for (def i=0;i<testData.size();i++) {
            def params = [sqlCode: "INTEGRATION_TEST", sqlProcessCode: "INTEGRATION_TEST", TERM: testData[i]]
            def results = sqlProcessCompositeService.getSqlProcessResultsFromHierarchy(params)
            assertEquals testData[i], results.code
        }
    }


    @Test
    void testGetProcessResultsHierarchPidmOneExpired() {
        def testData = ["213013", "213014", "213015"]

        // Create three identical processes that act on a pidm. It will return the spriden_id of the pereson with the pidm
        def entriesForSql = getEntriesForSql()
        def entriesForSqlProcess = getEntriesForSqlProcess()

        def validProcess = newValidTermForCreateSqlProcess(1, testData[0], entriesForSql, entriesForSqlProcess, new Date()-1, true)
        validProcess.save(failOnError: true, flush: true)

        validProcess = newValidTermForCreateSqlProcess(2, testData[1], entriesForSql, entriesForSqlProcess, new Date()-5, true)
        validProcess.save(failOnError: true, flush: true)

        validProcess = newValidTermForCreateSqlProcess(3, testData[2], entriesForSql, entriesForSqlProcess, new Date()-1, true)
        validProcess.save(failOnError: true, flush: true)

        createSqlProcessParameter("INTEGRATION_TEST", "TERM")

        def params = [sqlCode: "INTEGRATION_TEST", sqlProcessCode: "INTEGRATION_TEST", TERM: testData[0]]
        def results = sqlProcessCompositeService.getSqlProcessResultsFromHierarchy(params)
        assertEquals testData[0], results.code

        params = [sqlCode: "INTEGRATION_TEST", sqlProcessCode: "INTEGRATION_TEST", TERM: testData[1]]
        results = sqlProcessCompositeService.getSqlProcessResultsFromHierarchy(params)
        assertNull results

        params = [sqlCode: "INTEGRATION_TEST", sqlProcessCode: "INTEGRATION_TEST", TERM: testData[2]]
        results = sqlProcessCompositeService.getSqlProcessResultsFromHierarchy(params)
        assertEquals testData[2], results.code
    }


    @Test
    void testGetProcessResultsHierarchPidmOneInactive() {
        def testData = ["213013", "213014", "213015"]

        // Create three identical processes that act on a pidm. It will return the spriden_id of the pereson with the pidm
        def entriesForSql = getEntriesForSql()
        def entriesForSqlProcess = getEntriesForSqlProcess()

        def validProcess = newValidTermForCreateSqlProcess(1, testData[0], entriesForSql, entriesForSqlProcess, new Date()-1, true)
        validProcess.save(failOnError: true, flush: true)



        validProcess = newValidTermForCreateSqlProcess(2, testData[1], entriesForSql, entriesForSqlProcess, new Date()-1, false)
        validProcess.save(failOnError: true, flush: true)


        validProcess = newValidTermForCreateSqlProcess(3, testData[2], entriesForSql, entriesForSqlProcess, new Date()-1, true)
        validProcess.save(failOnError: true, flush: true)


        createSqlProcessParameter("INTEGRATION_TEST", "TERM")


        def params = [sqlCode: "INTEGRATION_TEST", sqlProcessCode: "INTEGRATION_TEST", TERM: testData[0]]
        def results = sqlProcessCompositeService.getSqlProcessResultsFromHierarchy(params)
        assertEquals testData[0], results.code


        params = [sqlCode: "INTEGRATION_TEST", sqlProcessCode: "INTEGRATION_TEST", TERM: testData[1]]
        results = sqlProcessCompositeService.getSqlProcessResultsFromHierarchy(params)
        assertNull results


        params = [sqlCode: "INTEGRATION_TEST", sqlProcessCode: "INTEGRATION_TEST", TERM: testData[2]]
        results = sqlProcessCompositeService.getSqlProcessResultsFromHierarchy(params)
        assertEquals testData[2], results.code
    }



    private def newValidForCreateSqlProcess(def sequenceNumber, def bannerId, def entriesForSql, def entriesForSqlProcess, def startDate, def active) {
        def sqlString = "select " + sequenceNumber + " from SPRIDEN where SPRIDEN_PIDM=:PIDM and SPRIDEN_ID='" + bannerId + "'"
        def sqlProcess = new SqlProcess(
                sequenceNumber: sequenceNumber,
                activeIndicator: active,
                validatedIndicator: true,
                startDate: startDate,
                selectFrom: "FROM",
                selectValue: "xxxx",
                whereClause: "WHERE",
                endDate: startDate + 2,
                parsedSql: sqlString ,
                systemRequiredIndicator: true,
                entriesForSqlProcess: entriesForSqlProcess,
                entriesForSql: entriesForSql,
        )
        return sqlProcess
    }


    private def newValidTermForCreateSqlProcess(def sequenceNumber, def termCode, def entriesForSql, def entriesForSqlProcess, def startDate, def active) {
        def sqlString = "select STVTERM_CODE from STVTERM where STVTERM_CODE=:TERM and STVTERM_CODE='" + termCode + "'"
        def sqlProcess = new SqlProcess(
                sequenceNumber: sequenceNumber,
                activeIndicator: active,
                validatedIndicator: true,
                startDate: startDate,
                selectFrom: "FROM",
                selectValue: "xxxx",
                whereClause: "WHERE",
                endDate: startDate + 2,
                parsedSql: sqlString ,
                systemRequiredIndicator: true,
                entriesForSqlProcess: entriesForSqlProcess,
                entriesForSql: entriesForSql,
        )
        return sqlProcess
    }


    private def getEntriesForSql() {
        def entriesForSql = new EntriesForSql(code: 'INTEGRATION_TEST', description: 'INTEGRATION_TEST', startDate: new Date(), endDate: new Date() + 1, systemRequiredIndicator: false)
        entriesForSql.save(failOnError: true, flush: true)

        return entriesForSql
    }


    private def getEntriesForSqlProcess() {
        def entriesForSqlProcesss = new EntriesForSqlProcesss(code: 'INTEGRATION_TEST', description: 'INTEGRATION_TEST', startDate: new Date(), endDate: new Date() + 1, systemRequiredIndicator: false)
        entriesForSqlProcesss.save(failOnError: true, flush: true)
        return entriesForSqlProcesss
    }


    void createSqlProcessParameter(def process, def param) {
        def parameter = new SqlProcessParameterByProcess(systemRequiredIndicator:false,
                lastModified:new Date(),
                lastModifiedBy:"GRAILS_USER",
                dataOrigin:"Banner",
                entriesForSqlProcess:process,
                parameterForSqlProcess:param)
        parameter.save(failOnError: true, flush: true)
    }

}
