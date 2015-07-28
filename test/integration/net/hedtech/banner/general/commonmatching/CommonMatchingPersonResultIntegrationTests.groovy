/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.commonmatching

import groovy.sql.Sql
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.general.system.CommonMatchingSource
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.dao.InvalidDataAccessResourceUsageException

import java.sql.CallableStatement

class CommonMatchingPersonResultIntegrationTests extends BaseIntegrationTestCase {

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testCreateValidCommonMatchingPersonResult() {
        def CommonMatchingPersonResult = newValidCommonMatchingPersonResult()
        shouldFail(InvalidDataAccessResourceUsageException) {
            CommonMatchingPersonResult.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testUpdateResult() {
        newGeneratedResult()
        def personList = CommonMatchingPersonResult.findAll()
        assertEquals 1, personList.size()
        personList[0].message = "test"
        shouldFail(InvalidDataAccessResourceUsageException) {
            personList[0].save(failOnError: true, flush: true)
        }
    }


    @Test
    void testDeleteResult() {
        newGeneratedResult()
        def personList = CommonMatchingPersonResult.findAll()
        assertEquals 1, personList.size()
        assertNotNull personList[0].id
        shouldFail(InvalidDataAccessResourceUsageException) {
            personList[0].delete(failOnError: true, flush: true)
        }
    }


    @Test
    void testFetchResults() {
        newGeneratedResult()
        def personList = CommonMatchingPersonResult.findAll()
        assertEquals 1, personList.size()
        assertEquals "M", personList[0].resultIndicator

        def personMatchedList = CommonMatchingPersonResult.fetchAllMatchResults()
        assertEquals 1, personMatchedList.size()

        assertEquals "HOS00001", personMatchedList[0].bannerId
        assertEquals "M", personMatchedList[0].resultIndicator
    }


    @Test
    void testFetchResultsWithPagination() {
        newGeneratedLotsOfResults()
        def personList = CommonMatchingPersonResult.findAllByResultIndicator("M")
        assertTrue  personList.size() > 10
        assertEquals "M", personList[0].resultIndicator

        def personMatchedList = CommonMatchingPersonResult.fetchAllMatchResults([max: 10, offset: 0])
        assertEquals 10, personMatchedList.size()

        def personMatchedList2 = CommonMatchingPersonResult.fetchAllMatchResults([max: 10, offset: 10])
        assertEquals 10, personMatchedList2.size()
    }


    private def newValidCommonMatchingPersonResult() {
        def result = new CommonMatchingPersonResult(
                id: "1",
                pidm: 11111,
                resultType: "M",
                message: "Testing",
                name: "Emily Jamison",
                bannerId: "HOS000001"
        )
        return result
    }


    private def newGeneratedResult() {
        def sourceCode = CommonMatchingSource.findByCode("HEDM_PERSON_MATCH")
        assertNotNull sourceCode
        def sources = CommonMatchingSourceRule.findAllByCommonMatchingSource(sourceCode)
        assertTrue sources.size() > 0

        def person = PersonUtility.getPerson("HOS00001")
        assertNotNull person
        assertEquals "Emily", person.firstName
        assertEquals "Jamison", person.lastName

        CallableStatement sqlCall
        try {
            def connection = sessionFactory.currentSession.connection()
            String matchPersonQuery = "{ call spkcmth.p_common_mtch(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) }"
            sqlCall = connection.prepareCall(matchPersonQuery)

            sqlCall.setString(1, 'HEDM_PERSON_MATCH')
            sqlCall.setString(2, "Emily")
            sqlCall.setString(3, "Jamison")
            sqlCall.setString(4, null)
            sqlCall.setString(5, null)
            sqlCall.setString(6, "F")
            sqlCall.setString(7, null)
            sqlCall.setString(8, null)
            sqlCall.setString(9, null)
            sqlCall.setString(10, null)
            sqlCall.setString(11, null)
            sqlCall.setString(12, null)
            sqlCall.setString(13, null)
            sqlCall.setString(14, null)
            sqlCall.setString(15, null)
            sqlCall.setString(16, null)

            sqlCall.registerOutParameter(17, java.sql.Types.VARCHAR)
            sqlCall.executeQuery()

            String errorCode = sqlCall.getString(17)
        }
        finally {
            sqlCall?.close()
        }

    }

    private def newGeneratedLotsOfResults() {
        def sourceCode = CommonMatchingSource.findByCode("HEDM_LASTNAME_MATCH")
        assertNotNull sourceCode
        def sources = CommonMatchingSourceRule.findAllByCommonMatchingSource(sourceCode)
        assertTrue sources.size() > 0
        // find list of spriden last names with lots of records we can use for pagination
        def sql =   new Sql(sessionFactory.currentSession.connection())
        def lists = sql.rows("select count(*) cnt, spriden_last_name from spriden group by spriden_last_name having count(*) > 1 order by count(*) desc")
        def list1 = lists[0]
        assertNotNull list1
        assertTrue list1.cnt > 20
        assertNotNull list1.spriden_last_name
        String errorCode
        CallableStatement sqlCall
        try {
            def connection = sessionFactory.currentSession.connection()
            String matchPersonQuery = "{ call spkcmth.p_common_mtch(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) }"
            sqlCall = connection.prepareCall(matchPersonQuery)

            sqlCall.setString(1, 'HEDM_LASTNAME_MATCH')
            sqlCall.setString(2, null)
            sqlCall.setString(3, list1.spriden_last_name )
            sqlCall.setString(4, null)
            sqlCall.setString(5, null)
            sqlCall.setString(6, null)
            sqlCall.setString(7, null)
            sqlCall.setString(8, null)
            sqlCall.setString(9, null)
            sqlCall.setString(10, null)
            sqlCall.setString(11, null)
            sqlCall.setString(12, null)
            sqlCall.setString(13, null)
            sqlCall.setString(14, null)
            sqlCall.setString(15, null)
            sqlCall.setString(16, null)

            sqlCall.registerOutParameter(17, java.sql.Types.VARCHAR)
            sqlCall.executeQuery()

            errorCode = sqlCall.getString(17)

        }
        finally {
            sqlCall?.close()
        }
        assertNull errorCode
    }
}
