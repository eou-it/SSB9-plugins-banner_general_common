/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import grails.validation.ValidationException
import groovy.sql.Sql
import net.hedtech.banner.general.system.PersonType
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException

import java.text.SimpleDateFormat

class SourceBackgroundInstitutionContactPersonIntegrationTests extends BaseIntegrationTestCase {

    protected void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    protected void tearDown() {
        super.tearDown()
    }


    void testCreateValidSourceBackgroundInstitutionContactPerson() {
        def sourceBackgroundInstitutionContactPerson = newValidForCreateSourceBackgroundInstitutionContactPerson()
        sourceBackgroundInstitutionContactPerson.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull sourceBackgroundInstitutionContactPerson.id
    }


    void testCreateInvalidSourceBackgroundInstitutionContactPerson() {
        def sourceBackgroundInstitutionContactPerson = newInvalidForCreateSourceBackgroundInstitutionContactPerson()
        shouldFail(ValidationException) {
            sourceBackgroundInstitutionContactPerson.save(failOnError: true, flush: true)
        }
    }


    void testUpdateValidSourceBackgroundInstitutionContactPerson() {
        def sourceBackgroundInstitutionContactPerson = newValidForCreateSourceBackgroundInstitutionContactPerson()
        sourceBackgroundInstitutionContactPerson.save(failOnError: true, flush: true)
        assertNotNull sourceBackgroundInstitutionContactPerson.id
        assertEquals 0L, sourceBackgroundInstitutionContactPerson.version
        assertEquals "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890",
                sourceBackgroundInstitutionContactPerson.name
        assertEquals "123456", sourceBackgroundInstitutionContactPerson.phoneArea
        assertEquals "123456789012", sourceBackgroundInstitutionContactPerson.phoneNumber
        assertEquals "1234567890", sourceBackgroundInstitutionContactPerson.phoneExtension
        assertEquals "1234", sourceBackgroundInstitutionContactPerson.countryPhone

        //Update the entity
        def personType = newValidPersonType("UPDT", "UPDTDESC")
        personType.save(failOnError: true, flush: true)

        sourceBackgroundInstitutionContactPerson.phoneArea = "UPDATE"
        sourceBackgroundInstitutionContactPerson.phoneNumber = "UPDATE789012"
        sourceBackgroundInstitutionContactPerson.phoneExtension = "UPDATE7890"
        sourceBackgroundInstitutionContactPerson.countryPhone = "UPDT"
        sourceBackgroundInstitutionContactPerson.personType = newValidPersonType("UPDT", "UPDTDESC")
        sourceBackgroundInstitutionContactPerson.save(failOnError: true, flush: true)

        //Assert for sucessful update
        sourceBackgroundInstitutionContactPerson = SourceBackgroundInstitutionContactPerson.get(sourceBackgroundInstitutionContactPerson.id)
        assertEquals 1L, sourceBackgroundInstitutionContactPerson?.version
        assertEquals "UPDATE", sourceBackgroundInstitutionContactPerson.phoneArea
        assertEquals "UPDATE789012", sourceBackgroundInstitutionContactPerson.phoneNumber
        assertEquals "UPDATE7890", sourceBackgroundInstitutionContactPerson.phoneExtension
        assertEquals "UPDT", sourceBackgroundInstitutionContactPerson.countryPhone
        assertEquals "UPDT", sourceBackgroundInstitutionContactPerson.personType.code
    }


    void testUpdateInvalidSourceBackgroundInstitutionContactPerson() {
        def sourceBackgroundInstitutionContactPerson = newValidForCreateSourceBackgroundInstitutionContactPerson()
        sourceBackgroundInstitutionContactPerson.save(failOnError: true, flush: true)
        assertNotNull sourceBackgroundInstitutionContactPerson.id
        assertEquals 0L, sourceBackgroundInstitutionContactPerson.version
        assertEquals "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890",
                sourceBackgroundInstitutionContactPerson.name
        assertEquals "123456", sourceBackgroundInstitutionContactPerson.phoneArea
        assertEquals "123456789012", sourceBackgroundInstitutionContactPerson.phoneNumber
        assertEquals "1234567890", sourceBackgroundInstitutionContactPerson.phoneExtension
        assertEquals "1234", sourceBackgroundInstitutionContactPerson.countryPhone

        //Update the entity with invalid values
        sourceBackgroundInstitutionContactPerson.name = null
        shouldFail(ValidationException) {
            sourceBackgroundInstitutionContactPerson.save(failOnError: true, flush: true)
        }
    }


    void testDates() {
        def time = new SimpleDateFormat('HHmmss')
        def hour = new SimpleDateFormat('HH')
        def date = new SimpleDateFormat('yyyy-M-d')
        def today = new Date()

        def sourceBackgroundInstitutionContactPerson = newValidForCreateSourceBackgroundInstitutionContactPerson()
        sourceBackgroundInstitutionContactPerson.save(flush: true, failOnError: true)
        sourceBackgroundInstitutionContactPerson.refresh()
        assertNotNull "SourceBackgroundInstitutionContactPerson should have been saved", sourceBackgroundInstitutionContactPerson.id

        // test date values -
        assertEquals date.format(today), date.format(sourceBackgroundInstitutionContactPerson.lastModified)
        assertEquals hour.format(today), hour.format(sourceBackgroundInstitutionContactPerson.lastModified)
    }


    void testOptimisticLock() {
        def sourceBackgroundInstitutionContactPerson = newValidForCreateSourceBackgroundInstitutionContactPerson()
        sourceBackgroundInstitutionContactPerson.save(failOnError: true, flush: true)

        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("update SORBCNT set SORBCNT_VERSION = 999 where SORBCNT_SURROGATE_ID = ?", [sourceBackgroundInstitutionContactPerson.id])
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
        //Try to update the entity
        //Update the entity
        sourceBackgroundInstitutionContactPerson.phoneArea = "UPDATE"
        shouldFail(HibernateOptimisticLockingFailureException) {
            sourceBackgroundInstitutionContactPerson.save(failOnError: true, flush: true)
        }
    }


    void testDeleteSourceBackgroundInstitutionContactPerson() {
        def sourceBackgroundInstitutionContactPerson = newValidForCreateSourceBackgroundInstitutionContactPerson()
        sourceBackgroundInstitutionContactPerson.save(failOnError: true, flush: true)
        def id = sourceBackgroundInstitutionContactPerson.id
        assertNotNull id
        sourceBackgroundInstitutionContactPerson.delete()
        assertNull SourceBackgroundInstitutionContactPerson.get(id)
    }


    void testValidation() {
        def sourceBackgroundInstitutionContactPerson = newInvalidForCreateSourceBackgroundInstitutionContactPerson()
        assertFalse "SourceBackgroundInstitutionContactPerson could not be validated as expected due to ${sourceBackgroundInstitutionContactPerson.errors}", sourceBackgroundInstitutionContactPerson.validate()
    }


    void testNullValidationFailure() {
        def sourceBackgroundInstitutionContactPerson = new SourceBackgroundInstitutionContactPerson()
        assertFalse "SourceBackgroundInstitutionContactPerson should have failed validation", sourceBackgroundInstitutionContactPerson.validate()
        assertErrorsFor sourceBackgroundInstitutionContactPerson, 'nullable',
                [
                        'name',
                        'sourceAndBackgroundInstitution'
                ]
        assertNoErrorsFor sourceBackgroundInstitutionContactPerson,
                [
                        'phoneArea',
                        'phoneNumber',
                        'phoneExtension',
                        'countryPhone',
                        'personType'
                ]
    }


    void testMaxSizeValidationFailures() {
        def sourceBackgroundInstitutionContactPerson = new SourceBackgroundInstitutionContactPerson(
                phoneArea: 'XXXXXXXX',
                phoneNumber: 'XXXXXXXXXXXXXX',
                phoneExtension: 'XXXXXXXXXXXX',
                countryPhone: 'XXXXXX')
        assertFalse "SourceBackgroundInstitutionContactPerson should have failed validation", sourceBackgroundInstitutionContactPerson.validate()
        assertErrorsFor sourceBackgroundInstitutionContactPerson, 'maxSize', ['phoneArea', 'phoneNumber', 'phoneExtension', 'countryPhone']
    }


    private def newValidForCreateSourceBackgroundInstitutionContactPerson() {
        def personType = newValidPersonType("TTTT", "TTTT")
        personType.save(failOnError: true, flush: true)

        def sourceBackgroundInstitutionContactPerson = new SourceBackgroundInstitutionContactPerson(
                name: "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890",
                phoneArea: "123456",
                phoneNumber: "123456789012",
                phoneExtension: "1234567890",
                countryPhone: "1234",
                sourceAndBackgroundInstitution: SourceAndBackgroundInstitution.findWhere(code: "999999"),
                personType: personType,
        )
        return sourceBackgroundInstitutionContactPerson
    }


    private def newInvalidForCreateSourceBackgroundInstitutionContactPerson() {
        def sourceBackgroundInstitutionContactPerson = new SourceBackgroundInstitutionContactPerson(
                phoneArea: "123456FAIL",
                sourceAndBackgroundInstitution: null,
                personType: null,
        )
        return sourceBackgroundInstitutionContactPerson
    }


    private def newValidPersonType(code, description) {
        def personType = new PersonType(code: code, description: description)
        return personType
    }
}
