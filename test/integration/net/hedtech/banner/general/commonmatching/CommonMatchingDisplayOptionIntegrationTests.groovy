/*******************************************************************************
 Copyright 2013-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.commonmatching
import org.junit.Before
import org.junit.Test
import org.junit.After

import grails.validation.ValidationException
import groovy.sql.Sql
import net.hedtech.banner.general.system.CommonMatchingSource
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException

class CommonMatchingDisplayOptionIntegrationTests extends BaseIntegrationTestCase {

    //Test data for creating new domain instance
    //Valid test data (For success tests)
    def i_success_commonMatchingSource = "TT"
    def i_success_objectName = "SPAIDEN"
    def i_success_sequenceNumber = 1
    //Invalid test data (For failure tests)
    def i_failure_commonMatchingSource
    def i_failure_objectName = "TTTTT"
    def i_failure_sequenceNumber = 1

    //Test data for creating updating domain instance
    //Valid test data (For success tests)
    def u_success_commonMatchingSource
    def u_success_objectName = "TTTTT"
    def u_success_sequenceNumber = 2
    //Valid test data (For failure tests)
    def u_failure_commonMatchingSource
    def u_failure_objectName = "TTTTT"
    def u_failure_sequenceNumber = 1


    @Before
    public void setUp() {
        formContext = ['GUAGMNU'] // Since we are not testing a controller, we need to explicitly set this
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testCreateValidCommonMatchingDisplayOption() {
        def commonMatchingDisplayOption = newValidForCreateCommonMatchingDisplayOption()
        commonMatchingDisplayOption.save(failOnError: true, flush: true)
        assertNotNull commonMatchingDisplayOption.id
    }


    @Test
    void testCreateInvalidCommonMatchingDisplayOption() {
        def commonMatchingDisplayOption = newInvalidForCreateCommonMatchingDisplayOption()
        shouldFail(ValidationException) {
            commonMatchingDisplayOption.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testUpdateValidCommonMatchingDisplayOption() {
        def commonMatchingDisplayOption = newValidForCreateCommonMatchingDisplayOption()
        commonMatchingDisplayOption.save(failOnError: true, flush: true)
        assertNotNull commonMatchingDisplayOption.id
        assertEquals 0L, commonMatchingDisplayOption.version
        assertEquals i_success_objectName, commonMatchingDisplayOption.objectName
        assertEquals i_success_sequenceNumber, commonMatchingDisplayOption.sequenceNumber

        //Update the entity
        commonMatchingDisplayOption.sequenceNumber = u_success_sequenceNumber

        commonMatchingDisplayOption.save(failOnError: true, flush: true)
        //Assert for sucessful update
        commonMatchingDisplayOption = CommonMatchingDisplayOption.get(commonMatchingDisplayOption.id)
        assertEquals 1L, commonMatchingDisplayOption?.version
        assertEquals u_success_sequenceNumber, commonMatchingDisplayOption.sequenceNumber

    }


    @Test
    void testUpdateInvalidCommonMatchingDisplayOption() {
        def commonMatchingDisplayOption = newValidForCreateCommonMatchingDisplayOption()
        commonMatchingDisplayOption.save(failOnError: true, flush: true)
        assertNotNull commonMatchingDisplayOption.id
        assertEquals 0L, commonMatchingDisplayOption.version
        assertEquals i_success_objectName, commonMatchingDisplayOption.objectName
        assertEquals i_success_sequenceNumber, commonMatchingDisplayOption.sequenceNumber

        //Update the entity with invalid values
        commonMatchingDisplayOption.commonMatchingSource = null

        shouldFail(ValidationException) {
            commonMatchingDisplayOption.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testOptimisticLock() {
        def commonMatchingDisplayOption = newValidForCreateCommonMatchingDisplayOption()
        commonMatchingDisplayOption.save(failOnError: true, flush: true)

        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("update GV_GORCMDO set GORCMDO_VERSION = 999 where GORCMDO_SURROGATE_ID = ?", [commonMatchingDisplayOption.id])
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
        //Try to update the entity
        //Update the entity
        commonMatchingDisplayOption.sequenceNumber = u_success_sequenceNumber
        shouldFail(HibernateOptimisticLockingFailureException) {
            commonMatchingDisplayOption.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testDeleteCommonMatchingDisplayOption() {
        def commonMatchingDisplayOption = newValidForCreateCommonMatchingDisplayOption()
        commonMatchingDisplayOption.save(failOnError: true, flush: true)
        def id = commonMatchingDisplayOption.id
        assertNotNull id
        commonMatchingDisplayOption.delete()
        assertNull CommonMatchingDisplayOption.get(id)
    }


    @Test
    void testValidation() {
        def commonMatchingDisplayOption = newInvalidForCreateCommonMatchingDisplayOption()
        assertFalse "CommonMatchingDisplayOption could not be validated as expected due to ${commonMatchingDisplayOption.errors}", commonMatchingDisplayOption.validate()
    }


    @Test
    void testNullValidationFailure() {
        def commonMatchingDisplayOption = new CommonMatchingDisplayOption()
        assertFalse "CommonMatchingDisplayOption should have failed validation", commonMatchingDisplayOption.validate()
        assertErrorsFor commonMatchingDisplayOption, 'nullable',
                [
                        'objectName',
                        'sequenceNumber',
                        'commonMatchingSource'
                ]
    }


    private def newValidForCreateCommonMatchingDisplayOption() {
        def commonMatchingSource = new CommonMatchingSource(code: i_success_commonMatchingSource, description: 'test')
        commonMatchingSource.save(failOnError: true, flush: true)
        def commonMatchingDisplayOption = new CommonMatchingDisplayOption(
                objectName: i_success_objectName,
                sequenceNumber: i_success_sequenceNumber,
                commonMatchingSource: commonMatchingSource,
        )
        return commonMatchingDisplayOption
    }


    private def newInvalidForCreateCommonMatchingDisplayOption() {
        def commonMatchingSource = null
        def commonMatchingDisplayOption = new CommonMatchingDisplayOption(
                objectName: i_failure_objectName,
                sequenceNumber: i_failure_sequenceNumber,
                commonMatchingSource: commonMatchingSource,
        )
        return commonMatchingDisplayOption
    }

}
