/*********************************************************************************
  Copyright 2010-2013 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall
import org.junit.Before
import org.junit.Test
import org.junit.After

import net.hedtech.banner.testing.BaseIntegrationTestCase
import groovy.sql.Sql
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException
import grails.validation.ValidationException


class SequenceNumberBaseIntegrationTests extends BaseIntegrationTestCase {

    //Test data for creating new domain instance
    //Valid test data (For success tests)

    def i_success_function = "TTTTT"
    def i_success_sequenceNumberPrefix = "#"
    def i_success_maximumSequenceNumber = 1
    //Invalid test data (For failure tests)

    def i_failure_function = "TTTTT"
    def i_failure_sequenceNumberPrefix = "##"
    def i_failure_maximumSequenceNumber = 1

    //Test data for creating updating domain instance
    //Valid test data (For success tests)

    def u_success_function = "UUUUU"
    def u_success_sequenceNumberPrefix = "A"
    def u_success_maximumSequenceNumber = 1
    //Valid test data (For failure tests)

    def u_failure_function = "TTTTT"
    def u_failure_sequenceNumberPrefix = "##"
    def u_failure_maximumSequenceNumber = 1


    @Before
    public void setUp() {
        formContext = ['SCACRSE'] // Since we are not testing a controller, we need to explicitly set this
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testCreateValidSequenceNumberBase() {
        def sequenceNumberBase = newValidForCreateSequenceNumberBase()
        sequenceNumberBase.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull sequenceNumberBase.id
        assertNotNull sequenceNumberBase.lastModified
        assertNotNull sequenceNumberBase.lastModifiedBy
        assertNotNull sequenceNumberBase.dataOrigin
    }


    @Test
    void testCreateInvalidSequenceNumberBase() {
        def sequenceNumberBase = newInvalidForCreateSequenceNumberBase()
        shouldFail(ValidationException) {
            sequenceNumberBase.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testUpdateValidSequenceNumberBase() {
        def sequenceNumberBase = newValidForCreateSequenceNumberBase()
        sequenceNumberBase.save(failOnError: true, flush: true)
        assertNotNull sequenceNumberBase.id
        assertEquals 0L, sequenceNumberBase.version
        assertEquals i_success_function, sequenceNumberBase.function
        assertEquals i_success_sequenceNumberPrefix, sequenceNumberBase.sequenceNumberPrefix
        assertEquals i_success_maximumSequenceNumber, sequenceNumberBase.maximumSequenceNumber

        //Update the entity
        sequenceNumberBase.sequenceNumberPrefix = u_success_sequenceNumberPrefix
        sequenceNumberBase.maximumSequenceNumber = u_success_maximumSequenceNumber
        sequenceNumberBase.save(failOnError: true, flush: true)
        //Assert for sucessful update
        sequenceNumberBase = SequenceNumberBase.get(sequenceNumberBase.id)
        assertEquals 1L, sequenceNumberBase?.version
        assertEquals u_success_sequenceNumberPrefix, sequenceNumberBase.sequenceNumberPrefix
        assertEquals u_success_maximumSequenceNumber, sequenceNumberBase.maximumSequenceNumber
    }


    @Test
    void testUpdateInvalidSequenceNumberBase() {
        def sequenceNumberBase = newValidForCreateSequenceNumberBase()
        sequenceNumberBase.save(failOnError: true, flush: true)
        assertNotNull sequenceNumberBase.id
        assertEquals 0L, sequenceNumberBase.version
        assertEquals i_success_function, sequenceNumberBase.function
        assertEquals i_success_sequenceNumberPrefix, sequenceNumberBase.sequenceNumberPrefix
        assertEquals i_success_maximumSequenceNumber, sequenceNumberBase.maximumSequenceNumber

        //Update the entity with invalid values
        sequenceNumberBase.sequenceNumberPrefix = u_failure_sequenceNumberPrefix
        sequenceNumberBase.maximumSequenceNumber = u_failure_maximumSequenceNumber
        shouldFail(ValidationException) {
            sequenceNumberBase.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testOptimisticLock() {
        def sequenceNumberBase = newValidForCreateSequenceNumberBase()
        sequenceNumberBase.save(failOnError: true, flush: true)

        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("update SOBSEQN set SOBSEQN_VERSION = 999 where SOBSEQN_SURROGATE_ID = ?", [sequenceNumberBase.id])
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
        //Try to update the entity
        //Update the entity
        sequenceNumberBase.sequenceNumberPrefix = u_success_sequenceNumberPrefix
        sequenceNumberBase.maximumSequenceNumber = u_success_maximumSequenceNumber
        shouldFail(HibernateOptimisticLockingFailureException) {
            sequenceNumberBase.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testDeleteSequenceNumberBase() {
        def sequenceNumberBase = newValidForCreateSequenceNumberBase()
        sequenceNumberBase.save(failOnError: true, flush: true)
        def id = sequenceNumberBase.id
        assertNotNull id
        sequenceNumberBase.delete()
        assertNull SequenceNumberBase.get(id)
    }


    @Test
    void testValidation() {
        def sequenceNumberBase = newInvalidForCreateSequenceNumberBase()
        assertFalse "SequenceNumberBase could not be validated as expected due to ${sequenceNumberBase.errors}", sequenceNumberBase.validate()
    }


    @Test
    void testNullValidationFailure() {
        def sequenceNumberBase = new SequenceNumberBase()
        assertFalse "SequenceNumberBase should have failed validation", sequenceNumberBase.validate()
        assertErrorsFor sequenceNumberBase, 'nullable',
                        [
                        'function',
                        'maximumSequenceNumber'
                        ]
        assertNoErrorsFor sequenceNumberBase,
                          [
                          'sequenceNumberPrefix'
                          ]
    }


    @Test
    void testMaxSizeValidationFailures() {
        def sequenceNumberBase = new SequenceNumberBase(
                sequenceNumberPrefix: 'XXX',
                function: 'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX')
        assertFalse "SequenceNumberBase should have failed validation", sequenceNumberBase.validate()
        assertErrorsFor sequenceNumberBase, 'maxSize', ['sequenceNumberPrefix', 'function']
    }


    @Test
    void testMaxValidationFailures() {
        def sequenceNumberBase = new SequenceNumberBase(
                maximumSequenceNumber: 999999991)
        assertFalse "SequenceNumberBase should have failed validation", sequenceNumberBase.validate()
        assertErrorsFor sequenceNumberBase, 'max', ['maximumSequenceNumber']
    }


    @Test
    void testMinValidationFailures() {
        def sequenceNumberBase = new SequenceNumberBase(
                maximumSequenceNumber: -999999991)
        assertFalse "SequenceNumberBase should have failed validation", sequenceNumberBase.validate()
        assertErrorsFor sequenceNumberBase, 'min', ['maximumSequenceNumber']
    }


    private def newValidForCreateSequenceNumberBase() {
        def sequenceNumberBase = new SequenceNumberBase(
                function: i_success_function,
                sequenceNumberPrefix: i_success_sequenceNumberPrefix,
                maximumSequenceNumber: i_success_maximumSequenceNumber,
        )
        return sequenceNumberBase
    }


    private def newInvalidForCreateSequenceNumberBase() {
        def sequenceNumberBase = new SequenceNumberBase(
                function: i_failure_function,
                sequenceNumberPrefix: i_failure_sequenceNumberPrefix,
                maximumSequenceNumber: i_failure_maximumSequenceNumber,
        )
        return sequenceNumberBase
    }

}
