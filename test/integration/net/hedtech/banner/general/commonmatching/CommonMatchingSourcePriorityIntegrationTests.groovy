/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.commonmatching

import grails.validation.ValidationException
import groovy.sql.Sql
import net.hedtech.banner.general.system.CommonMatchingSource
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException

class CommonMatchingSourcePriorityIntegrationTests extends BaseIntegrationTestCase {

    //Test data for creating new domain instance
    //Valid test data (For success tests)
    def i_success_commonMatchingSource = 'TT'

    def i_success_priorityNumber = 1
    def i_success_description = "TTTTT"
    def i_success_longDescription = "TTTTT"

    def i_success_onlineMatchIndicator = true
    def i_success_entity = "P"
    def i_success_transposeDateIndicator = true
    def i_success_transposeNameIndicator = true
    def i_success_aliasWildcardIndicator = true
    def i_success_lengthOverrideIndicator = true
    def i_success_apiFailureIndicator = true
    //Invalid test data (For failure tests)
    def i_failure_commonMatchingSource

    def i_failure_priorityNumber = 1
    def i_failure_description = "TTTTT"
    def i_failure_longDescription = "TTTTT"

    //Test data for creating updating domain instance
    //Valid test data (For success tests)
    def u_success_commonMatchingSource

    def u_success_priorityNumber = 1
    def u_success_description = "UUUU"
    def u_success_longDescription = "TTTTT"
    //Valid test data (For failure tests)
    def u_failure_commonMatchingSource

    def u_failure_priorityNumber = 1
    def u_failure_description = "TTTTT"
    def u_failure_longDescription = "TTTTT"


    void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    void tearDown() {
        super.tearDown()
    }


    void testCreateValidCommonMatchingSourcePriority() {
        def commonMatchingSourcePriority = newValidForCreateCommonMatchingSourcePriority()
        commonMatchingSourcePriority.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull commonMatchingSourcePriority.id
    }


    void testCreateInvalidCommonMatchingSourcePriority() {
        def commonMatchingSourcePriority = newInvalidForCreateCommonMatchingSourcePriority()
        shouldFail(ValidationException) {
            commonMatchingSourcePriority.save(failOnError: true, flush: true)
        }
    }


    void testUpdateValidCommonMatchingSourcePriority() {
        def commonMatchingSourcePriority = newValidForCreateCommonMatchingSourcePriority()
        commonMatchingSourcePriority.save(failOnError: true, flush: true)
        assertNotNull commonMatchingSourcePriority.id
        assertEquals 0L, commonMatchingSourcePriority.version
        assertEquals i_success_priorityNumber, commonMatchingSourcePriority.priorityNumber
        assertEquals i_success_description, commonMatchingSourcePriority.description
        assertEquals i_success_longDescription, commonMatchingSourcePriority.longDescription

        //Update the entity
        commonMatchingSourcePriority.description = u_success_description
        commonMatchingSourcePriority.longDescription = u_success_longDescription

        commonMatchingSourcePriority.save(failOnError: true, flush: true)
        //Assert for sucessful update
        commonMatchingSourcePriority = CommonMatchingSourcePriority.get(commonMatchingSourcePriority.id)
        assertEquals 1L, commonMatchingSourcePriority?.version
        assertEquals u_success_description, commonMatchingSourcePriority.description
        assertEquals u_success_longDescription, commonMatchingSourcePriority.longDescription

    }


    void testUpdateInvalidCommonMatchingSourcePriority() {
        def commonMatchingSourcePriority = newValidForCreateCommonMatchingSourcePriority()
        commonMatchingSourcePriority.save(failOnError: true, flush: true)
        assertNotNull commonMatchingSourcePriority.id
        assertEquals 0L, commonMatchingSourcePriority.version
        assertEquals i_success_priorityNumber, commonMatchingSourcePriority.priorityNumber
        assertEquals i_success_description, commonMatchingSourcePriority.description
        assertEquals i_success_longDescription, commonMatchingSourcePriority.longDescription

        //Update the entity with invalid values
        commonMatchingSourcePriority.priorityNumber = null

        shouldFail(ValidationException) {
            commonMatchingSourcePriority.save(failOnError: true, flush: true)
        }
    }


    void testOptimisticLock() {
        def commonMatchingSourcePriority = newValidForCreateCommonMatchingSourcePriority()
        commonMatchingSourcePriority.save(failOnError: true, flush: true)

        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("update GV_GORCMSP set GORCMSP_VERSION = 999 where GORCMSP_SURROGATE_ID = ?", [commonMatchingSourcePriority.id])
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
        //Try to update the entity
        //Update the entity
        commonMatchingSourcePriority.description = u_success_description
        commonMatchingSourcePriority.longDescription = u_success_longDescription
        shouldFail(HibernateOptimisticLockingFailureException) {
            commonMatchingSourcePriority.save(failOnError: true, flush: true)
        }
    }


    void testDeleteCommonMatchingSourcePriority() {
        def commonMatchingSourcePriority = newValidForCreateCommonMatchingSourcePriority()
        commonMatchingSourcePriority.save(failOnError: true, flush: true)
        def id = commonMatchingSourcePriority.id
        assertNotNull id
        commonMatchingSourcePriority.delete()
        assertNull CommonMatchingSourcePriority.get(id)
    }


    void testValidation() {
        def commonMatchingSourcePriority = newInvalidForCreateCommonMatchingSourcePriority()
        assertFalse "CommonMatchingSourcePriority could not be validated as expected due to ${commonMatchingSourcePriority.errors}", commonMatchingSourcePriority.validate()
    }


    void testNullValidationFailure() {
        def commonMatchingSourcePriority = new CommonMatchingSourcePriority()
        assertFalse "CommonMatchingSourcePriority should have failed validation", commonMatchingSourcePriority.validate()
        assertErrorsFor commonMatchingSourcePriority, 'nullable',
                [
                        'priorityNumber',
                        'description',
                        'commonMatchingSource'
                ]
        assertNoErrorsFor commonMatchingSourcePriority,
                [
                        'longDescription'
                ]
    }


    void testMaxSizeValidationFailures() {
        def commonMatchingSourcePriority = new CommonMatchingSourcePriority(
                longDescription: 'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX')
        assertFalse "CommonMatchingSourcePriority should have failed validation", commonMatchingSourcePriority.validate()
        assertErrorsFor commonMatchingSourcePriority, 'maxSize', ['longDescription']
    }


    private def newValidForCreateCommonMatchingSourcePriority() {
        def commonMatchingSource = new CommonMatchingSource(code: 'SS', description: 'test')
        commonMatchingSource.save(failOnError: true, flush: true)
        def commonMatchingRule = createCMSourceRule(commonMatchingSource)
        commonMatchingRule.save(failOnError: true, flush: true)
        def commonMatchingSourcePriority = new CommonMatchingSourcePriority(
                priorityNumber: i_success_priorityNumber,
                description: i_success_description,
                longDescription: i_success_longDescription,
                commonMatchingSource: commonMatchingSource,
        )
        return commonMatchingSourcePriority
    }


    private def newInvalidForCreateCommonMatchingSourcePriority() {
        def commonMatchingSourcePriority = new CommonMatchingSourcePriority(
                priorityNumber: i_failure_priorityNumber,
                description: null,
                longDescription: i_failure_longDescription,
                commonMatchingSource: i_failure_commonMatchingSource,
        )
        return commonMatchingSourcePriority
    }


    private def createCMSourceRule(commonMatchingSource) {

        def commonMatchingSourceRule = new CommonMatchingSourceRule(
                onlineMatchIndicator: i_success_onlineMatchIndicator,
                entity: i_success_entity,
                transposeDateIndicator: i_success_transposeDateIndicator,
                transposeNameIndicator: i_success_transposeNameIndicator,
                aliasWildcardIndicator: i_success_aliasWildcardIndicator,
                lengthOverrideIndicator: i_success_lengthOverrideIndicator,
                apiFailureIndicator: i_success_apiFailureIndicator,
                commonMatchingSource: commonMatchingSource,
                addressType: null,
                telephoneType: null,
                emailType: null,
        )
        return commonMatchingSourceRule
    }

}
