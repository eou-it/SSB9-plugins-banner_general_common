/*********************************************************************************
  Copyright 2010-2013 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import grails.validation.ValidationException
import groovy.sql.Sql
import net.hedtech.banner.general.system.County
import net.hedtech.banner.general.system.Nation
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.general.system.State
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException

import java.text.SimpleDateFormat

class SourceBackgroundInstitutionBaseIntegrationTests extends BaseIntegrationTestCase {

    protected void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    protected void tearDown() {
        super.tearDown()
    }


    void testCreateValidSourceBackgroundInstitutionBase() {
        def sourceBackgroundInstitutionBase = newValidForCreateSourceBackgroundInstitutionBase()
        sourceBackgroundInstitutionBase.save(failOnError: true, flush: true)
        assertEquals 0L, sourceBackgroundInstitutionBase.version
        assertEquals "123456789012345678901234567890123456789012345678901234567890123456789012345", sourceBackgroundInstitutionBase.streetLine1
        assertEquals "123456789012345678901234567890123456789012345678901234567890123456789012345", sourceBackgroundInstitutionBase.streetLine2
        assertEquals "123456789012345678901234567890123456789012345678901234567890123456789012345", sourceBackgroundInstitutionBase.streetLine3
        assertEquals "12345678901234567890123456789012345678901234567890", sourceBackgroundInstitutionBase.city
        assertEquals "123456789012345678901234567890", sourceBackgroundInstitutionBase.zip
        assertEquals "1234567890", sourceBackgroundInstitutionBase.houseNumber
        assertEquals "123456789012345678901234567890123456789012345678901234567890123456789012345", sourceBackgroundInstitutionBase.streetLine4

        //Test if the generated entity now has an id assigned
        assertNotNull sourceBackgroundInstitutionBase.id
    }


    void testCreateInvalidSourceBackgroundInstitutionBase() {
        def sourceBackgroundInstitutionBase = newInvalidForCreateSourceBackgroundInstitutionBase()
        shouldFail(ValidationException) {
            sourceBackgroundInstitutionBase.save(failOnError: true, flush: true)
        }
    }


    private def doUpdate(sourceBackgroundInstitutionBase) {
        //Update the entity
        sourceBackgroundInstitutionBase.streetLine1 = "1234567890UPDATE1"
        sourceBackgroundInstitutionBase.streetLine2 = "1234567890UPDATE2"
        sourceBackgroundInstitutionBase.streetLine3 = "1234567890UPDATE3"
        sourceBackgroundInstitutionBase.city = "CITYNEW"
        sourceBackgroundInstitutionBase.zip = "ZIPNEW"
        sourceBackgroundInstitutionBase.houseNumber = "NUMNEW"
        sourceBackgroundInstitutionBase.streetLine4 = "1234567890UPDATE4"
        sourceBackgroundInstitutionBase.state = State.findWhere(code: "NJ")
        sourceBackgroundInstitutionBase.county = County.findWhere(code: "002")
        sourceBackgroundInstitutionBase.nation = Nation.findWhere(code: "2")
    }


    void testUpdateValidSourceBackgroundInstitutionBase() {
        def sourceBackgroundInstitutionBase = newValidForCreateSourceBackgroundInstitutionBase()
        sourceBackgroundInstitutionBase.save(failOnError: true, flush: true)
        assertEquals 0L, sourceBackgroundInstitutionBase.version
        assertNotNull sourceBackgroundInstitutionBase.id

        //Update the entity
        doUpdate(sourceBackgroundInstitutionBase)
        sourceBackgroundInstitutionBase.save(failOnError: true, flush: true)

        //Assert for sucessful update
        sourceBackgroundInstitutionBase = SourceBackgroundInstitutionBase.get(sourceBackgroundInstitutionBase.id)
        assertEquals 1L, sourceBackgroundInstitutionBase?.version
        assertEquals "1234567890UPDATE1", sourceBackgroundInstitutionBase.streetLine1
        assertEquals "1234567890UPDATE2", sourceBackgroundInstitutionBase.streetLine2
        assertEquals "1234567890UPDATE3", sourceBackgroundInstitutionBase.streetLine3
        assertEquals "CITYNEW", sourceBackgroundInstitutionBase.city
        assertEquals "ZIPNEW", sourceBackgroundInstitutionBase.zip
        assertEquals "NUMNEW", sourceBackgroundInstitutionBase.houseNumber
        assertEquals "1234567890UPDATE4", sourceBackgroundInstitutionBase.streetLine4
        assertEquals "NJ", sourceBackgroundInstitutionBase.state.code
        assertEquals "002", sourceBackgroundInstitutionBase.county.code
        assertEquals "2", sourceBackgroundInstitutionBase.nation.code
    }


    void testUpdateInvalidSourceBackgroundInstitutionBase() {
        def sourceBackgroundInstitutionBase = newValidForCreateSourceBackgroundInstitutionBase()
        sourceBackgroundInstitutionBase.save(failOnError: true, flush: true)
        assertNotNull sourceBackgroundInstitutionBase.id
        assertEquals 0L, sourceBackgroundInstitutionBase.version

        //Update the entity with invalid values
        sourceBackgroundInstitutionBase.city = null
        shouldFail(ValidationException) {
            sourceBackgroundInstitutionBase.save(failOnError: true, flush: true)
        }
    }


    void testDates() {
        def time = new SimpleDateFormat('HHmmss')
        def hour = new SimpleDateFormat('HH')
        def date = new SimpleDateFormat('yyyy-M-d')
        def today = new Date()

        def sourceBackgroundInstitutionBase = newValidForCreateSourceBackgroundInstitutionBase()
        sourceBackgroundInstitutionBase.save(flush: true, failOnError: true)
        sourceBackgroundInstitutionBase.refresh()
        assertNotNull "SourceBackgroundInstitutionBase should have been saved", sourceBackgroundInstitutionBase.id

        // test date values -
        assertEquals date.format(today), date.format(sourceBackgroundInstitutionBase.lastModified)
        assertEquals hour.format(today), hour.format(sourceBackgroundInstitutionBase.lastModified)
    }


    void testOptimisticLock() {
        def sourceBackgroundInstitutionBase = newValidForCreateSourceBackgroundInstitutionBase()
        sourceBackgroundInstitutionBase.save(failOnError: true, flush: true)

        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("update SOBSBGI set SOBSBGI_VERSION = 999 where SOBSBGI_SURROGATE_ID = ?", [sourceBackgroundInstitutionBase.id])
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
        //Try to update the entity
        //Update the entity
        doUpdate(sourceBackgroundInstitutionBase)
        shouldFail(HibernateOptimisticLockingFailureException) {
            sourceBackgroundInstitutionBase.save(failOnError: true, flush: true)
        }
    }


    void testDeleteSourceBackgroundInstitutionBase() {
        def sourceBackgroundInstitutionBase = newValidForCreateSourceBackgroundInstitutionBase()
        sourceBackgroundInstitutionBase.save(failOnError: true, flush: true)
        def id = sourceBackgroundInstitutionBase.id
        assertNotNull id
        sourceBackgroundInstitutionBase.delete()
        assertNull SourceBackgroundInstitutionBase.get(id)
    }


    void testValidation() {
        def sourceBackgroundInstitutionBase = newInvalidForCreateSourceBackgroundInstitutionBase()
        assertFalse "SourceBackgroundInstitutionBase could not be validated as expected due to ${sourceBackgroundInstitutionBase.errors}", sourceBackgroundInstitutionBase.validate()
    }


    void testNullValidationFailure() {
        def sourceBackgroundInstitutionBase = new SourceBackgroundInstitutionBase()
        assertFalse "SourceBackgroundInstitutionBase should have failed validation", sourceBackgroundInstitutionBase.validate()
        assertErrorsFor sourceBackgroundInstitutionBase, 'nullable',
                ['city', 'sourceAndBackgroundInstitution']
        assertNoErrorsFor sourceBackgroundInstitutionBase,
                [
                        'streetLine1',
                        'streetLine2',
                        'streetLine3',
                        'zip',
                        'houseNumber',
                        'streetLine4',
                        'state',
                        'county',
                        'nation'
                ]
    }


    void testMaxSizeValidationFailures() {
        def sourceBackgroundInstitutionBase = new SourceBackgroundInstitutionBase(
                streetLine1: '123456789012345678901234567890123456789012345678901234567890123456789012345X',
                streetLine2: '123456789012345678901234567890123456789012345678901234567890123456789012345X',
                streetLine3: '123456789012345678901234567890123456789012345678901234567890123456789012345X',
                zip: '123456789012345678901234567890X',
                houseNumber: '1234567890X',
                streetLine4: '123456789012345678901234567890123456789012345678901234567890123456789012345X')
        assertFalse "SourceBackgroundInstitutionBase should have failed validation", sourceBackgroundInstitutionBase.validate()
        assertErrorsFor sourceBackgroundInstitutionBase, 'maxSize',
                ['streetLine1', 'streetLine2', 'streetLine3', 'zip', 'houseNumber', 'streetLine4']
    }


    void testFetchBySourceAndBackgroundInstitution() {
        // Create 2 bases
        def sourceBackgroundInstitutionBase = newValidForCreateSourceBackgroundInstitutionBase()
        sourceBackgroundInstitutionBase.save(failOnError: true, flush: true)
        assertNotNull sourceBackgroundInstitutionBase.id
        assertEquals 0L, sourceBackgroundInstitutionBase.version

        def sourceAndBackgroundInstitution = sourceBackgroundInstitutionBase.sourceAndBackgroundInstitution

        sourceBackgroundInstitutionBase = new SourceBackgroundInstitutionBase(
                city: "CITY2",
                sourceAndBackgroundInstitution: SourceAndBackgroundInstitution.findWhere(code: "999998"),
        )
        sourceBackgroundInstitutionBase.save(failOnError: true, flush: true)

        def record = SourceBackgroundInstitutionBase.fetchBySourceAndBackgroundInstitution(sourceAndBackgroundInstitution)
        assertNotNull record
        assertEquals sourceAndBackgroundInstitution.code,  record.sourceAndBackgroundInstitution.code
    }


    private def newValidForCreateSourceBackgroundInstitutionBase() {
        def sourceBackgroundInstitutionBase = new SourceBackgroundInstitutionBase(
                streetLine1: "123456789012345678901234567890123456789012345678901234567890123456789012345",
                streetLine2: "123456789012345678901234567890123456789012345678901234567890123456789012345",
                streetLine3: "123456789012345678901234567890123456789012345678901234567890123456789012345",
                city: "12345678901234567890123456789012345678901234567890",
                zip: "123456789012345678901234567890",
                houseNumber: "1234567890",
                streetLine4: "123456789012345678901234567890123456789012345678901234567890123456789012345",
                sourceAndBackgroundInstitution: SourceAndBackgroundInstitution.findWhere(code: "999999"),
                state: State.findWhere(code: "PA"),
                county: County.findWhere(code: "001"),
                nation: Nation.findWhere(code: "1"),
        )
        return sourceBackgroundInstitutionBase
    }


    private def newInvalidForCreateSourceBackgroundInstitutionBase() {
        def sourceBackgroundInstitutionBase = new SourceBackgroundInstitutionBase(
                city: "12345678901234567890123456789012345678901234567890FAIL",
                sourceAndBackgroundInstitution: SourceAndBackgroundInstitution.findWhere(code: "999999"),
        )
        return sourceBackgroundInstitutionBase
    }
}
