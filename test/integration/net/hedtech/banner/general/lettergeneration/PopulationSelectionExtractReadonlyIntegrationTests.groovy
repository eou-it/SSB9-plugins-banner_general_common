/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.lettergeneration

import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.dao.InvalidDataAccessResourceUsageException

class PopulationSelectionExtractReadonlyIntegrationTests extends BaseIntegrationTestCase {

    //Test data for creating new domain instance
    //Valid test data (For success tests)

    def i_success_application = "TTTTT"
    def i_success_selection = "TTTTT"
    def i_success_creatorId = "TTTTT"
    def i_success_key = "TTTTT"
    def i_success_systemIndicator = "M"
    def i_success_selectIndicator = "Y"
    //Invalid test data (For failure tests)

    def i_failure_application = "TTTTT"
    def i_failure_selection = "TTTTT"
    def i_failure_creatorId = "TTTTT"
    def i_failure_key = "TTTTT"
    def i_failure_systemIndicator = "MT"
    def i_failure_selectIndicator = "Y"

    //Test data for creating updating domain instance
    //Valid test data (For success tests)
    def u_success_systemIndicator = "S"
    def u_success_selectIndicator = "Y"


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
    void testCreateValidLetterGenerationPopulationSelectionExtractReadonly() {
        def letterGenerationPopulationSelectionExtractReadonly = newValidForCreateLetterGenerationPopulationSelectionExtractReadonly()
        shouldFail(InvalidDataAccessResourceUsageException) {
            letterGenerationPopulationSelectionExtractReadonly.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testUpdateLetterGenerationPopulationSelectionExtractReadonly() {
        def letterGenerationPopulationSelectionExtractReadonly = PopulationSelectionExtractReadonly.findAllByApplicationAndSelection("STUDENT", 'HEDM')[0]

        assertNotNull letterGenerationPopulationSelectionExtractReadonly.id
        assertEquals "M", letterGenerationPopulationSelectionExtractReadonly.systemIndicator

        //Update the entity
        letterGenerationPopulationSelectionExtractReadonly.systemIndicator = u_success_systemIndicator
        shouldFail(InvalidDataAccessResourceUsageException) {
            letterGenerationPopulationSelectionExtractReadonly.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testDeleteLetterGenerationPopulationSelectionExtractReadonly() {
        def letterGenerationPopulationSelectionExtractReadonly = PopulationSelectionExtractReadonly.findAllByApplicationAndSelection("STUDENT", 'HEDM')[0]

        assertNotNull letterGenerationPopulationSelectionExtractReadonly.id
        shouldFail(InvalidDataAccessResourceUsageException) {
            letterGenerationPopulationSelectionExtractReadonly.delete(failOnError: true, flush: true)
        }
    }


    @Test
    void testFetchAllPidmsByApplicationSelectionCreatorIdLastModifiedBy() {
        /*
        <GLBEXTR_APPLICATION>STUDENT</GLBEXTR_APPLICATION>
        <GLBEXTR_SELECTION>HEDM</GLBEXTR_SELECTION>
        <GLBEXTR_CREATOR_ID>BANNER</GLBEXTR_CREATOR_ID>
        <GLBEXTR_USER_ID>GRAILS</GLBEXTR_USER_ID>
         */
        def popsel = PopulationSelectionExtractReadonly.findAllByApplicationAndSelection("STUDENT", 'HEDM')
        assertEquals 7, popsel.size()
        assertEquals 7, popsel.findAll { it.creatorId == "BANNER" }?.size()
        assertEquals 7, popsel.findAll { it.lastModifiedBy == "GRAILS" }?.size()
        popsel.each {
            assertNotNull it.numericKey
            assertEquals it.numericKey,  it.key.toLong()
        }

        def popselFetched =
                PopulationSelectionExtractReadonly.fetchAllPidmsByApplicationSelectionCreatorIdLastModifiedBy("STUDENT", "HEDM", "BANNER", "GRAILS")

        assertEquals 7, popselFetched.size()
        popselFetched.each {
            assertTrue it.numericKey instanceof Long
            assertTrue it.numericKey > 0
        }

    }


    @Test
    void testFetchAllPidmsByApplicationSelectionCreatorIdLastModifiedByWithPagination() {

        def popsel = PopulationSelectionExtractReadonly.findAllByApplicationAndSelection("STUDENT", 'HEDM')
        assertEquals 7, popsel.size()
        assertEquals 7, popsel.findAll { it.creatorId == "BANNER" }?.size()
        assertEquals 7, popsel.findAll { it.lastModifiedBy == "GRAILS" }?.size()

        def popselFetched =
                PopulationSelectionExtractReadonly.fetchAllPidmsByApplicationSelectionCreatorIdLastModifiedBy("STUDENT", "HEDM", "BANNER", "GRAILS",
                        [max: 3, offset: 0])

        assertEquals 3, popselFetched.size()
        popselFetched =
                PopulationSelectionExtractReadonly.fetchAllPidmsByApplicationSelectionCreatorIdLastModifiedBy("STUDENT", "HEDM", "BANNER", "GRAILS",
                        [max: 3, offset: 3])
        assertEquals 3, popselFetched.size()
        popselFetched =
                PopulationSelectionExtractReadonly.fetchAllPidmsByApplicationSelectionCreatorIdLastModifiedBy("STUDENT", "HEDM", "BANNER", "GRAILS",
                        [max: 3, offset: 6])
        assertEquals 1, popselFetched.size()
    }


    @Test
    void testFetchAllPidmsByApplicationSelectionCreatorIdLastModifiedByWithAlphaPagination() {

        def popsel = PopulationSelectionExtractReadonly.findAllByApplicationAndSelection("STUDENT", 'HEDM')
        assertEquals 7, popsel.size()
        assertEquals 7, popsel.findAll { it.creatorId == "BANNER" }?.size()
        assertEquals 7, popsel.findAll { it.lastModifiedBy == "GRAILS" }?.size()

        def popselFetched =
                PopulationSelectionExtractReadonly.fetchAllPidmsByApplicationSelectionCreatorIdLastModifiedBy("STUDENT", "HEDM", "BANNER", "GRAILS",
                        [max: '3', offset: '0'])

        assertEquals 3, popselFetched.size()

    }

    @Test
    void testFetchAllPidmsByApplicationSelectionCreatorIdLastModifiedByWithAlphaPaginationAndSort() {

        def popsel = PopulationSelectionExtractReadonly.findAllByApplicationAndSelection("STUDENT", 'HEDM')
        assertEquals 7, popsel.size()
        assertEquals 7, popsel.findAll { it.creatorId == "BANNER" }?.size()
        assertEquals 7, popsel.findAll { it.lastModifiedBy == "GRAILS" }?.size()

        def popselFetched =
                PopulationSelectionExtractReadonly.fetchAllPidmsByApplicationSelectionCreatorIdLastModifiedBy("STUDENT", "HEDM", "BANNER", "GRAILS",
                        [max: '10', offset: '0', sort: 'key'])

        assertEquals 7, popselFetched.size()
        assertTrue popselFetched[0].key < popselFetched[1].key
        assertTrue popselFetched[1].key < popselFetched[2].key
        assertTrue popselFetched[2].key < popselFetched[3].key
        assertTrue popselFetched[3].key < popselFetched[4].key
        assertTrue popselFetched[4].key < popselFetched[5].key
        assertTrue popselFetched[5].key < popselFetched[6].key


    }

    @Test
    void testFetchAllPidmsByApplicationSelectionCreatorIdLastModifiedByWithAlphaPaginationAndNoSort() {

        def popsel = PopulationSelectionExtractReadonly.findAllByApplicationAndSelection("STUDENT", 'HEDM')
        assertEquals 7, popsel.size()
        assertEquals 7, popsel.findAll { it.creatorId == "BANNER" }?.size()
        assertEquals 7, popsel.findAll { it.lastModifiedBy == "GRAILS" }?.size()

        def popselFetched =
                PopulationSelectionExtractReadonly.fetchAllPidmsByApplicationSelectionCreatorIdLastModifiedBy("STUDENT", "HEDM", "BANNER", "GRAILS",
                        [max: '10', offset: '0'])

        assertEquals 7, popselFetched.size()
        assertTrue popselFetched[0].lastName < popselFetched[1].lastName
        assertTrue popselFetched[1].lastName < popselFetched[2].lastName
        assertTrue popselFetched[2].lastName < popselFetched[3].lastName
        assertTrue popselFetched[3].lastName < popselFetched[4].lastName
        assertTrue popselFetched[4].lastName < popselFetched[5].lastName
        assertTrue popselFetched[5].lastName < popselFetched[6].lastName

    }

    @Test
    void testPaginationForPerformanceUniqueLists() {
        // verify our test case has 7 records
        def popsel = PopulationSelectionExtract.findAllByApplicationAndSelection("STUDENT", 'HEDMPERFORM')
        if ( popsel.size() > 0 ){
            popsel.each {
                it.delete(flush:true, failOnError:true)
            }
        }
        // create big list
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        String idSql = """INSERT INTO GLBEXTR
                  (glbextr_key,
                   glbextr_application,
                   glbextr_selection,
                   glbextr_creator_id,
                   glbextr_user_id,
                   glbextr_sys_ind,
                   glbextr_activity_date)
                select
                   to_char(spriden_pidm),
                   'STUDENT',
                   'HEDMPERFORM',
                   'BANNER',
                   'GRAILS',
                   'S',
                   SYSDATE
                from spriden
                where spriden_CHANGE_ind is null
                and exists ( select 1 from sfrstcr where sfrstcr_pidm = spriden_pidm)
                and not exists ( select 'x' from glbextr old
                  where old.glbextr_key = to_char(spriden_pidm)
                  and old.glbextr_application = 'STUDENT'
                  and old.glbextr_selection = 'HEDMPERFORM') """
        def insertCount = sql.executeUpdate(idSql)
        assertTrue insertCount > 500
        def persextract = PopulationSelectionExtractReadonly.fetchAllPidmsByApplicationSelectionCreatorIdLastModifiedBy("STUDENT", "HEDMPERFORM", "BANNER", "GRAILS")
        assertEquals insertCount, persextract.size()
        def pidmlist = persextract.groupBy {it.pidm }
        // verify the list of pidms is unique
        pidmlist.each {
            assertEquals 1, it.value.size()
        }
        // get page 1
        def person1 =
                PopulationSelectionExtractReadonly.fetchAllPidmsByApplicationSelectionCreatorIdLastModifiedBy("STUDENT", "HEDMPERFORM", "BANNER", "GRAILS",
                        [max: '250', offset: '0'])
        assertEquals 250, person1.size()
        def person2 =
                PopulationSelectionExtractReadonly.fetchAllPidmsByApplicationSelectionCreatorIdLastModifiedBy("STUDENT", "HEDMPERFORM", "BANNER", "GRAILS",
                        [max: '250', offset: '250'])
        assertEquals 250, person1.size()
        // no overlap on first person
        assertTrue  person1[249].pidm != person2[0].pidm
        // verfiy people are not the same
        def cnt = 0
        def match = 0
        person1.each { pers ->
            cnt += 1
            person2.find {
                if ( it.pidm == pers.pidm) {
                    println ("${cnt} pidms match ${it.pidm} ${pers.pidm}")
                    match += 1
                } }
        }
        assertEquals 0, match
    }


    private def newValidForCreateLetterGenerationPopulationSelectionExtractReadonly() {
        def letterGenerationPopulationSelectionExtractReadonly = new PopulationSelectionExtractReadonly(
                application: i_success_application,
                selection: i_success_selection,
                creatorId: i_success_creatorId,
                key: i_success_key,
                systemIndicator: i_success_systemIndicator,
                selectIndicator: i_success_selectIndicator,
        )
        return letterGenerationPopulationSelectionExtractReadonly
    }


    private def newInvalidForCreateLetterGenerationPopulationSelectionExtractReadonly() {
        def letterGenerationPopulationSelectionExtractReadonly = new PopulationSelectionExtractReadonly(
                application: i_failure_application,
                selection: i_failure_selection,
                creatorId: i_failure_creatorId,
                key: i_failure_key,
                systemIndicator: i_failure_systemIndicator,
                selectIndicator: i_failure_selectIndicator,
        )
        return letterGenerationPopulationSelectionExtractReadonly
    }


}
