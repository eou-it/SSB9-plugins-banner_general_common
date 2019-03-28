/*******************************************************************************
 Copyright 2015-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.lettergeneration

import grails.validation.ValidationException
import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.orm.hibernate5.HibernateOptimisticLockingFailureException

import java.text.SimpleDateFormat

class PopulationSelectionExtractIntegrationTests extends BaseIntegrationTestCase {

    //Test data for creating new domain instance
    //Valid test data (For success tests)

    def i_success_application = "GENERAL"
    def i_success_selection = "ALL"
    def i_success_creatorId = "BANNER"
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

    def u_success_application = "TTTTT"
    def u_success_selection = "TTTTT"
    def u_success_creatorId = "TTTTT"
    def u_success_key = "TTTTT"
    def u_success_systemIndicator = "S"
    def u_success_selectIndicator = "Y"
    //Valid test data (For failure tests)

    def u_failure_application = "TTTTT"
    def u_failure_selection = "TTTTT"
    def u_failure_creatorId = "TTTTT"
    def u_failure_key = "TTTTT"
    def u_failure_systemIndicator = "MT"
    def u_failure_selectIndicator = "Y"


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        initializeTestDataForReferences()
    }

    //This method is used to initialize test data for references.
    //A method is required to execute database calls as it requires a active transaction
    void initializeTestDataForReferences() {
        //Valid test data (For success tests)

        //Invalid test data (For failure tests)

        //Valid test data (For success tests)

        //Valid test data (For failure tests)

        //Test data for references for custom tests

    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testCreateValidLetterGenerationPopulationSelectionExtract() {
        def letterGenerationPopulationSelectionExtract = newValidForCreateLetterGenerationPopulationSelectionExtract()
        letterGenerationPopulationSelectionExtract.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull letterGenerationPopulationSelectionExtract.id

    }


    @Test
    void testCreateInvalidLetterGenerationPopulationSelectionExtract() {
        def letterGenerationPopulationSelectionExtract = newInvalidForCreateLetterGenerationPopulationSelectionExtract()
        shouldFail(ValidationException) {
            letterGenerationPopulationSelectionExtract.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testUpdateValidLetterGenerationPopulationSelectionExtract() {
        def letterGenerationPopulationSelectionExtract = newValidForCreateLetterGenerationPopulationSelectionExtract()
        letterGenerationPopulationSelectionExtract.save(failOnError: true, flush: true)
        assertNotNull letterGenerationPopulationSelectionExtract.id
        assertEquals 0L, letterGenerationPopulationSelectionExtract.version
        assertEquals i_success_application, letterGenerationPopulationSelectionExtract.application
        assertEquals i_success_selection, letterGenerationPopulationSelectionExtract.selection
        assertEquals i_success_creatorId, letterGenerationPopulationSelectionExtract.creatorId
        assertEquals i_success_key, letterGenerationPopulationSelectionExtract.key
        assertEquals i_success_systemIndicator, letterGenerationPopulationSelectionExtract.systemIndicator
        assertEquals i_success_selectIndicator, letterGenerationPopulationSelectionExtract.selectIndicator

        //Update the entity
        letterGenerationPopulationSelectionExtract.systemIndicator = u_success_systemIndicator
        letterGenerationPopulationSelectionExtract.selectIndicator = u_success_selectIndicator
        //letterGenerationPopulationSelectionExtract.save( failOnError: true, flush: true )
        save letterGenerationPopulationSelectionExtract
        //Assert for sucessful update
        letterGenerationPopulationSelectionExtract = PopulationSelectionExtract.get(letterGenerationPopulationSelectionExtract.id)
        assertEquals 1L, letterGenerationPopulationSelectionExtract?.version
        assertEquals u_success_systemIndicator, letterGenerationPopulationSelectionExtract.systemIndicator
        assertEquals u_success_selectIndicator, letterGenerationPopulationSelectionExtract.selectIndicator
    }


    @Test
    void testUpdateInvalidLetterGenerationPopulationSelectionExtract() {
        def letterGenerationPopulationSelectionExtract = newValidForCreateLetterGenerationPopulationSelectionExtract()
        letterGenerationPopulationSelectionExtract.save(failOnError: true, flush: true)
        assertNotNull letterGenerationPopulationSelectionExtract.id
        assertEquals 0L, letterGenerationPopulationSelectionExtract.version
        assertEquals i_success_application, letterGenerationPopulationSelectionExtract.application
        assertEquals i_success_selection, letterGenerationPopulationSelectionExtract.selection
        assertEquals i_success_creatorId, letterGenerationPopulationSelectionExtract.creatorId
        assertEquals i_success_key, letterGenerationPopulationSelectionExtract.key
        assertEquals i_success_systemIndicator, letterGenerationPopulationSelectionExtract.systemIndicator
        assertEquals i_success_selectIndicator, letterGenerationPopulationSelectionExtract.selectIndicator

        //Update the entity with invalid values
        letterGenerationPopulationSelectionExtract.systemIndicator = u_failure_systemIndicator
        letterGenerationPopulationSelectionExtract.selectIndicator = u_failure_selectIndicator
        shouldFail(ValidationException) {
            letterGenerationPopulationSelectionExtract.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testDates() {
        def time = new SimpleDateFormat('HHmmss')
        def hour = new SimpleDateFormat('HH')
        def date = new SimpleDateFormat('yyyy-M-d')
        def today = new Date()

        def letterGenerationPopulationSelectionExtract = newValidForCreateLetterGenerationPopulationSelectionExtract()

        letterGenerationPopulationSelectionExtract.save(flush: true, failOnError: true)
        letterGenerationPopulationSelectionExtract.refresh()
        assertNotNull "LetterGenerationPopulationSelectionExtract should have been saved", letterGenerationPopulationSelectionExtract.id

        // test date values -
        assertEquals date.format(today), date.format(letterGenerationPopulationSelectionExtract.lastModified)
        assertEquals hour.format(today), hour.format(letterGenerationPopulationSelectionExtract.lastModified)


    }


    @Test
    void testOptimisticLock() {
        def letterGenerationPopulationSelectionExtract = newValidForCreateLetterGenerationPopulationSelectionExtract()
        letterGenerationPopulationSelectionExtract.save(failOnError: true, flush: true)

        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("update GLBEXTR set GLBEXTR_VERSION = 999 where GLBEXTR_SURROGATE_ID = ?", [letterGenerationPopulationSelectionExtract.id])
        }
        finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
        //Try to update the entity
        //Update the entity
        letterGenerationPopulationSelectionExtract.systemIndicator = u_success_systemIndicator
        letterGenerationPopulationSelectionExtract.selectIndicator = u_success_selectIndicator
        shouldFail(HibernateOptimisticLockingFailureException) {
            letterGenerationPopulationSelectionExtract.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testDeleteLetterGenerationPopulationSelectionExtract() {
        def letterGenerationPopulationSelectionExtract = newValidForCreateLetterGenerationPopulationSelectionExtract()
        letterGenerationPopulationSelectionExtract.save(failOnError: true, flush: true)
        def id = letterGenerationPopulationSelectionExtract.id
        assertNotNull id
        letterGenerationPopulationSelectionExtract.delete()
        assertNull PopulationSelectionExtract.get(id)
    }


    @Test
    void testValidation() {
        def letterGenerationPopulationSelectionExtract = newInvalidForCreateLetterGenerationPopulationSelectionExtract()
        assertFalse "LetterGenerationPopulationSelectionExtract could not be validated as expected due to ${letterGenerationPopulationSelectionExtract.errors}", letterGenerationPopulationSelectionExtract.validate()
    }


    @Test
    void testNullValidationFailure() {
        def letterGenerationPopulationSelectionExtract = new PopulationSelectionExtract()
        assertFalse "LetterGenerationPopulationSelectionExtract should have failed validation", letterGenerationPopulationSelectionExtract.validate()
        assertErrorsFor letterGenerationPopulationSelectionExtract, 'nullable',
                [
                        'application',
                        'selection',
                        'creatorId',
                        'key',
                        'systemIndicator'
                ]
        assertNoErrorsFor letterGenerationPopulationSelectionExtract,
                [
                        'selectIndicator'
                ]
    }


    @Test
    void testMaxSizeValidationFailures() {
        def letterGenerationPopulationSelectionExtract = new PopulationSelectionExtract(
                selectIndicator: 'XXX')
        assertFalse "LetterGenerationPopulationSelectionExtract should have failed validation", letterGenerationPopulationSelectionExtract.validate()
        assertErrorsFor letterGenerationPopulationSelectionExtract, 'maxSize', ['selectIndicator']
    }


    @Test
    void testFetchAllPidmsByApplicationSelectionCreatorIdLastModifiedBy() {
        /*
        <GLBEXTR_APPLICATION>STUDENT</GLBEXTR_APPLICATION>
        <GLBEXTR_SELECTION>HEDM</GLBEXTR_SELECTION>
        <GLBEXTR_CREATOR_ID>BANNER</GLBEXTR_CREATOR_ID>
        <GLBEXTR_USER_ID>GRAILS</GLBEXTR_USER_ID>
         */
        def popsel = PopulationSelectionExtract.findAllByApplicationAndSelection("STUDENT", 'HEDM')
        assertEquals 7, popsel.size()
        assertEquals 7, popsel.findAll { it.creatorId == "BANNER" }?.size()
        assertEquals 7, popsel.findAll { it.lastModifiedBy == "GRAILS" }?.size()

        def popselFetched =
                PopulationSelectionExtract.fetchByApplicationSelectionCreatorIdLastModifiedBy("STUDENT", "HEDM", "BANNER", "GRAILS")

        assertEquals 7, popselFetched.size()

    }


    @Test
    void testFetchAllPidmsByApplicationSelectionCreatorIdLastModifiedByWithPagination() {

        def popsel = PopulationSelectionExtract.findAllByApplicationAndSelection("STUDENT", 'HEDM')
        assertEquals 7, popsel.size()
        assertEquals 7, popsel.findAll { it.creatorId == "BANNER" }?.size()
        assertEquals 7, popsel.findAll { it.lastModifiedBy == "GRAILS" }?.size()

        def popselFetched =
                PopulationSelectionExtract.fetchByApplicationSelectionCreatorIdLastModifiedBy("STUDENT", "HEDM", "BANNER", "GRAILS",
                        [max: 3, offset: 0])

        assertEquals 3, popselFetched.size()
        popselFetched =
                PopulationSelectionExtract.fetchByApplicationSelectionCreatorIdLastModifiedBy("STUDENT", "HEDM", "BANNER", "GRAILS",
                        [max: 3, offset: 3])
        assertEquals 3, popselFetched.size()
        popselFetched =
                PopulationSelectionExtract.fetchByApplicationSelectionCreatorIdLastModifiedBy("STUDENT", "HEDM", "BANNER", "GRAILS",
                        [max: 3, offset: 6])
        assertEquals 1, popselFetched.size()
    }


    @Test
    void testFetchAllPidmsByApplicationSelectionCreatorIdLastModifiedByWithAlphaPagination() {

        def popsel = PopulationSelectionExtract.findAllByApplicationAndSelection("STUDENT", 'HEDM')
        assertEquals 7, popsel.size()
        assertEquals 7, popsel.findAll { it.creatorId == "BANNER" }?.size()
        assertEquals 7, popsel.findAll { it.lastModifiedBy == "GRAILS" }?.size()

        def popselFetched =
                PopulationSelectionExtract.fetchByApplicationSelectionCreatorIdLastModifiedBy("STUDENT", "HEDM", "BANNER", "GRAILS",
                        [max: '3', offset: '0'])

        assertEquals 3, popselFetched.size()

    }


    private def newValidForCreateLetterGenerationPopulationSelectionExtract() {
        def letterGenerationPopulationSelectionExtract = new PopulationSelectionExtract(
                application: i_success_application,
                selection: i_success_selection,
                creatorId: i_success_creatorId,
                key: i_success_key,
                systemIndicator: i_success_systemIndicator,
                selectIndicator: i_success_selectIndicator,
        )
        return letterGenerationPopulationSelectionExtract
    }


    private def newInvalidForCreateLetterGenerationPopulationSelectionExtract() {
        def letterGenerationPopulationSelectionExtract = new PopulationSelectionExtract(
                application: i_failure_application,
                selection: i_failure_selection,
                creatorId: i_failure_creatorId,
                key: i_failure_key,
                systemIndicator: i_failure_systemIndicator,
                selectIndicator: i_failure_selectIndicator,
        )
        return letterGenerationPopulationSelectionExtract
    }


}
