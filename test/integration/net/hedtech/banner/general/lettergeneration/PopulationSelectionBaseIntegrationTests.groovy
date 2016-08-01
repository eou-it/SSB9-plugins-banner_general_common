/*******************************************************************************
 Copyright 2015-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.lettergeneration

import grails.validation.ValidationException
import groovy.sql.Sql
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException
import net.hedtech.banner.testing.BaseIntegrationTestCase

import java.text.SimpleDateFormat

class PopulationSelectionBaseIntegrationTests extends BaseIntegrationTestCase {

    //Test data for creating new domain instance
    //Valid test data (For success tests)

    def i_success_application = "GENERAL"
    def i_success_selection = "TTTTT"
    def i_success_creatorId = "TTTTT"
    def i_success_description = "TTTTT"
    def i_success_lockIndicator = true
    def i_success_typeIndicator = "M"

    //Invalid test data (For failure tests)
    def i_failure_application = "TTTTT"
    def i_failure_selection = "TTTTT"
    def i_failure_creatorId = "TTTTT"
    def i_failure_description = "TTTTT"
    def i_failure_lockIndicator = true
    def i_failure_typeIndicator = "MM"

    //Test data for creating updating domain instance
    //Valid test data (For success tests)

    def u_success_application = "TTTTT"
    def u_success_selection = "TTTTT"
    def u_success_creatorId = "TTTTT"
    def u_success_description = "TTTTTYYYYY"
    def u_success_lockIndicator = true
    def u_success_typeIndicator = "M"

    //Valid test data (For failure tests)
    def u_failure_application = "TTTTT"
    def u_failure_selection = "TTTTT"
    def u_failure_creatorId = "TTTTT"
    def u_failure_description = "TTTTT"
    def u_failure_lockIndicator = true
    def u_failure_typeIndicator = "MM"


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
    void testCreateValidLetterGenerationPopulationSelectionBase() {
        def letterGenerationPopulationSelectionBase = newValidForCreateLetterGenerationPopulationSelectionBase()
        letterGenerationPopulationSelectionBase.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull letterGenerationPopulationSelectionBase.id
    }


    @Test
    void testCreateInvalidLetterGenerationPopulationSelectionBase() {
        def letterGenerationPopulationSelectionBase = newInvalidForCreateLetterGenerationPopulationSelectionBase()
        shouldFail(ValidationException) {
            letterGenerationPopulationSelectionBase.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testUpdateValidLetterGenerationPopulationSelectionBase() {
        def letterGenerationPopulationSelectionBase = newValidForCreateLetterGenerationPopulationSelectionBase()
        letterGenerationPopulationSelectionBase.save(failOnError: true, flush: true)
        assertNotNull letterGenerationPopulationSelectionBase.id
        assertEquals 0L, letterGenerationPopulationSelectionBase.version
        assertEquals i_success_application, letterGenerationPopulationSelectionBase.application
        assertEquals i_success_selection, letterGenerationPopulationSelectionBase.selection
        assertEquals i_success_creatorId, letterGenerationPopulationSelectionBase.creatorId
        assertEquals i_success_description, letterGenerationPopulationSelectionBase.description
        assertEquals i_success_lockIndicator, letterGenerationPopulationSelectionBase.lockIndicator
        assertEquals i_success_typeIndicator, letterGenerationPopulationSelectionBase.typeIndicator

        //Update the entity
        letterGenerationPopulationSelectionBase.description = u_success_description
        letterGenerationPopulationSelectionBase.lockIndicator = u_success_lockIndicator
        letterGenerationPopulationSelectionBase.typeIndicator = u_success_typeIndicator
        //letterGenerationPopulationSelectionBase.save( failOnError: true, flush: true )
        save letterGenerationPopulationSelectionBase
        //Assert for sucessful update
        letterGenerationPopulationSelectionBase = PopulationSelectionBase.get(letterGenerationPopulationSelectionBase.id)
        assertEquals 1L, letterGenerationPopulationSelectionBase?.version
        assertEquals u_success_description, letterGenerationPopulationSelectionBase.description
        assertEquals u_success_lockIndicator, letterGenerationPopulationSelectionBase.lockIndicator
        assertEquals u_success_typeIndicator, letterGenerationPopulationSelectionBase.typeIndicator
    }


    @Test
    void testUpdateInvalidLetterGenerationPopulationSelectionBase() {
        def letterGenerationPopulationSelectionBase = newValidForCreateLetterGenerationPopulationSelectionBase()
        letterGenerationPopulationSelectionBase.save(failOnError: true, flush: true)
        assertNotNull letterGenerationPopulationSelectionBase.id
        assertEquals 0L, letterGenerationPopulationSelectionBase.version
        assertEquals i_success_application, letterGenerationPopulationSelectionBase.application
        assertEquals i_success_selection, letterGenerationPopulationSelectionBase.selection
        assertEquals i_success_creatorId, letterGenerationPopulationSelectionBase.creatorId
        assertEquals i_success_description, letterGenerationPopulationSelectionBase.description
        assertEquals i_success_lockIndicator, letterGenerationPopulationSelectionBase.lockIndicator
        assertEquals i_success_typeIndicator, letterGenerationPopulationSelectionBase.typeIndicator

        //Update the entity with invalid values
        letterGenerationPopulationSelectionBase.description = u_failure_description
        letterGenerationPopulationSelectionBase.lockIndicator = u_failure_lockIndicator
        letterGenerationPopulationSelectionBase.typeIndicator = u_failure_typeIndicator
        shouldFail(ValidationException) {
            letterGenerationPopulationSelectionBase.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testDates() {
        def hour = new SimpleDateFormat('HH')
        def date = new SimpleDateFormat('yyyy-M-d')
        def today = new Date()

        def letterGenerationPopulationSelectionBase = newValidForCreateLetterGenerationPopulationSelectionBase()
        letterGenerationPopulationSelectionBase.save(flush: true, failOnError: true)
        letterGenerationPopulationSelectionBase.refresh()
        assertNotNull "LetterGenerationPopulationSelectionBase should have been saved", letterGenerationPopulationSelectionBase.id

        // test date values -
        assertEquals date.format(today), date.format(letterGenerationPopulationSelectionBase.lastModified)
        assertEquals hour.format(today), hour.format(letterGenerationPopulationSelectionBase.lastModified)
    }


    @Test
    void testOptimisticLock() {
        def letterGenerationPopulationSelectionBase = newValidForCreateLetterGenerationPopulationSelectionBase()
        letterGenerationPopulationSelectionBase.save(failOnError: true, flush: true)

        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("update GLBSLCT set GLBSLCT_VERSION = 999 where GLBSLCT_SURROGATE_ID = ?", [letterGenerationPopulationSelectionBase.id])
        }
        finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
        //Try to update the entity
        //Update the entity
        letterGenerationPopulationSelectionBase.description = u_success_description
        letterGenerationPopulationSelectionBase.lockIndicator = u_success_lockIndicator
        letterGenerationPopulationSelectionBase.typeIndicator = u_success_typeIndicator
        shouldFail(HibernateOptimisticLockingFailureException) {
            letterGenerationPopulationSelectionBase.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testDeleteLetterGenerationPopulationSelectionBase() {
        def letterGenerationPopulationSelectionBase = newValidForCreateLetterGenerationPopulationSelectionBase()
        letterGenerationPopulationSelectionBase.save(failOnError: true, flush: true)
        def id = letterGenerationPopulationSelectionBase.id
        assertNotNull id
        letterGenerationPopulationSelectionBase.delete()
        assertNull PopulationSelectionBase.get(id)
    }


    @Test
    void testValidation() {
        def populationSelectionBaseNoValue = new PopulationSelectionBase()
        //should not pass validation since none of the required values are provided
        assertFalse populationSelectionBaseNoValue.validate()

        def populationSelectionBase = newValidForCreateLetterGenerationPopulationSelectionBase()
        assertTrue populationSelectionBase.validate()
    }


    @Test
    void testNullValidationFailure() {
        def letterGenerationPopulationSelectionBase = new PopulationSelectionBase()
        assertFalse "PopulationSelectionBase should have failed validation", letterGenerationPopulationSelectionBase.validate()
        assertErrorsFor letterGenerationPopulationSelectionBase, 'nullable',
                [
                        'application',
                        'selection',
                        'creatorId',
                        'description',
                        'lockIndicator'
                ]
        assertNoErrorsFor letterGenerationPopulationSelectionBase,
                [
                        'typeIndicator'
                ]
    }


    @Test
    void testMaxSizeValidationFailures() {
        def letterGenerationPopulationSelectionBase = new PopulationSelectionBase(
                typeIndicator: 'XXX')
        assertFalse "PopulationSelectionBase should have failed validation", letterGenerationPopulationSelectionBase.validate()
        assertErrorsFor letterGenerationPopulationSelectionBase, 'maxSize', ['typeIndicator']
    }


    private def newValidForCreateLetterGenerationPopulationSelectionBase() {
        def letterGenerationPopulationSelectionBase = new PopulationSelectionBase(
                application: i_success_application,
                selection: i_success_selection,
                creatorId: i_success_creatorId,
                description: i_success_description,
                lockIndicator: i_success_lockIndicator,
                typeIndicator: i_success_typeIndicator,
                lastModified: new Date(),
                lastModifiedBy: "test",
                dataOrigin: "Banner"
        )
        return letterGenerationPopulationSelectionBase
    }


    private def newInvalidForCreateLetterGenerationPopulationSelectionBase() {
        def letterGenerationPopulationSelectionBase = new PopulationSelectionBase(
                application: i_failure_application,
                selection: i_failure_selection,
                creatorId: i_failure_creatorId,
                description: i_failure_description,
                lockIndicator: i_failure_lockIndicator,
                typeIndicator: i_failure_typeIndicator,
        )
        return letterGenerationPopulationSelectionBase
    }

}
