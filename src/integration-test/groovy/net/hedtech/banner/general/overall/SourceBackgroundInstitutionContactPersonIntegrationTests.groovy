/*********************************************************************************
  Copyright 2010-2013 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.junit.Before
import org.junit.Test
import org.junit.After
import static groovy.test.GroovyAssert.*
import grails.validation.ValidationException
import groovy.sql.Sql
import net.hedtech.banner.general.system.PersonType
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.springframework.orm.hibernate5.HibernateOptimisticLockingFailureException

import java.text.SimpleDateFormat

@Integration
@Rollback
class SourceBackgroundInstitutionContactPersonIntegrationTests extends BaseIntegrationTestCase {

    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testCreateValidSourceBackgroundInstitutionContactPerson() {
        def sourceBackgroundInstitutionContactPerson = newValidForCreateSourceBackgroundInstitutionContactPerson()
        sourceBackgroundInstitutionContactPerson.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull sourceBackgroundInstitutionContactPerson.id
    }


    @Test
    void testCreateInvalidSourceBackgroundInstitutionContactPerson() {
        def sourceBackgroundInstitutionContactPerson = newInvalidForCreateSourceBackgroundInstitutionContactPerson()
        shouldFail(ValidationException) {
            sourceBackgroundInstitutionContactPerson.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testUpdateValidSourceBackgroundInstitutionContactPerson() {
        def sourceBackgroundInstitutionContactPerson = newValidForCreateSourceBackgroundInstitutionContactPerson()
        sourceBackgroundInstitutionContactPerson.save(failOnError: true, flush: true)
        assertNotNull sourceBackgroundInstitutionContactPerson.id
        assertEquals 0L, sourceBackgroundInstitutionContactPerson.version
        assertEquals "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890",
                sourceBackgroundInstitutionContactPerson.personName
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

        //Assert for successful update
        sourceBackgroundInstitutionContactPerson = SourceBackgroundInstitutionContactPerson.get(sourceBackgroundInstitutionContactPerson.id)
        assertEquals 2L, sourceBackgroundInstitutionContactPerson?.version
        assertEquals "UPDATE", sourceBackgroundInstitutionContactPerson.phoneArea
        assertEquals "UPDATE789012", sourceBackgroundInstitutionContactPerson.phoneNumber
        assertEquals "UPDATE7890", sourceBackgroundInstitutionContactPerson.phoneExtension
        assertEquals "UPDT", sourceBackgroundInstitutionContactPerson.countryPhone
        assertEquals "UPDT", sourceBackgroundInstitutionContactPerson.personType.code
    }


    @Test
    void testUpdateInvalidSourceBackgroundInstitutionContactPerson() {
        def sourceBackgroundInstitutionContactPerson = newValidForCreateSourceBackgroundInstitutionContactPerson()
        sourceBackgroundInstitutionContactPerson.save(failOnError: true, flush: true)
        assertNotNull sourceBackgroundInstitutionContactPerson.id
        assertEquals 0L, sourceBackgroundInstitutionContactPerson.version
        assertEquals "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890",
                sourceBackgroundInstitutionContactPerson.personName
        assertEquals "123456", sourceBackgroundInstitutionContactPerson.phoneArea
        assertEquals "123456789012", sourceBackgroundInstitutionContactPerson.phoneNumber
        assertEquals "1234567890", sourceBackgroundInstitutionContactPerson.phoneExtension
        assertEquals "1234", sourceBackgroundInstitutionContactPerson.countryPhone

        //Update the entity with invalid values
        sourceBackgroundInstitutionContactPerson.personName = null
        shouldFail(ValidationException) {
            sourceBackgroundInstitutionContactPerson.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testDates() {
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


    @Test
    void testOptimisticLock() {
        def sourceBackgroundInstitutionContactPerson = newValidForCreateSourceBackgroundInstitutionContactPerson()
        sourceBackgroundInstitutionContactPerson.save(failOnError: true, flush: true)

        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("update SORBCNT set SORBCNT_VERSION = 999 where SORBCNT_SURROGATE_ID = ?", [sourceBackgroundInstitutionContactPerson.id])
        } finally {
//            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
        //Try to update the entity
        //Update the entity
        sourceBackgroundInstitutionContactPerson.phoneArea = "UPDATE"
        shouldFail(HibernateOptimisticLockingFailureException) {
            sourceBackgroundInstitutionContactPerson.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testDeleteSourceBackgroundInstitutionContactPerson() {
        def sourceBackgroundInstitutionContactPerson = newValidForCreateSourceBackgroundInstitutionContactPerson()
        sourceBackgroundInstitutionContactPerson.save(failOnError: true, flush: true)
        def id = sourceBackgroundInstitutionContactPerson.id
        assertNotNull id
        sourceBackgroundInstitutionContactPerson.delete()
        assertNull SourceBackgroundInstitutionContactPerson.get(id)
    }


    @Test
    void testValidation() {
        def sourceBackgroundInstitutionContactPerson = newInvalidForCreateSourceBackgroundInstitutionContactPerson()
        assertFalse "SourceBackgroundInstitutionContactPerson could not be validated as expected due to ${sourceBackgroundInstitutionContactPerson.errors}", sourceBackgroundInstitutionContactPerson.validate()
    }


    @Test
    void testNullValidationFailure() {
        def sourceBackgroundInstitutionContactPerson = new SourceBackgroundInstitutionContactPerson()
        assertFalse "SourceBackgroundInstitutionContactPerson should have failed validation", sourceBackgroundInstitutionContactPerson.validate()
        assertErrorsFor sourceBackgroundInstitutionContactPerson, 'nullable',
                [
                        'personName',
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


    @Test
    void testMaxSizeValidationFailures() {
        def sourceBackgroundInstitutionContactPerson = new SourceBackgroundInstitutionContactPerson(
                phoneArea: 'XXXXXXXX',
                phoneNumber: 'XXXXXXXXXXXXXX',
                phoneExtension: 'XXXXXXXXXXXX',
                countryPhone: 'XXXXXX')
        assertFalse "SourceBackgroundInstitutionContactPerson should have failed validation", sourceBackgroundInstitutionContactPerson.validate()
        assertErrorsFor sourceBackgroundInstitutionContactPerson, 'maxSize', ['phoneArea', 'phoneNumber', 'phoneExtension', 'countryPhone']
    }


    @Test
    void testFetchSearch() {
        // Create 3 contacts
        def sourceBackgroundInstitutionContactPerson = newValidForCreateSourceBackgroundInstitutionContactPerson()
        sourceBackgroundInstitutionContactPerson.save(failOnError: true, flush: true)
        assertNotNull sourceBackgroundInstitutionContactPerson.id
        assertEquals 0L, sourceBackgroundInstitutionContactPerson.version

        def sourceAndBackgroundInstitution = sourceBackgroundInstitutionContactPerson.sourceAndBackgroundInstitution

        sourceBackgroundInstitutionContactPerson = new SourceBackgroundInstitutionContactPerson(
                personName: "Don Johnson",
                sourceAndBackgroundInstitution: sourceAndBackgroundInstitution,
        )
        sourceBackgroundInstitutionContactPerson.save(failOnError: true, flush: true)

        sourceBackgroundInstitutionContactPerson = new SourceBackgroundInstitutionContactPerson(
                personName: "Larry Johnson",
                sourceAndBackgroundInstitution: sourceAndBackgroundInstitution,
        )
        sourceBackgroundInstitutionContactPerson.save(failOnError: true, flush: true)

        def pagingAndSortParams = [sortColumn: "personName", sortDirection: "asc", max: 5, offset: 0]
        Map paramsMap = [sourceAndBackgroundInstitutionCode: sourceAndBackgroundInstitution.code, personName: "%Johnson%"]
        def criteriaMap = [[key: "personName", binding: "personName", operator: "contains"]]
        def filterData = [params: paramsMap, criteria: criteriaMap]

        def records = SourceBackgroundInstitutionContactPerson.fetchSearch(filterData, pagingAndSortParams)
        assertTrue records.size() == 2
        records.each { record ->
            assertTrue record.personName.indexOf("Johnson") >= 0 // -1 is a failed search
        }
    }


    private def newValidForCreateSourceBackgroundInstitutionContactPerson() {
        def personType = newValidPersonType("TTTT", "TTTT")
        personType.save(failOnError: true, flush: true)

        def sourceBackgroundInstitutionContactPerson = new SourceBackgroundInstitutionContactPerson(
                personName: "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890",
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
