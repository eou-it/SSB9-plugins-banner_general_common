/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.junit.Before
import org.junit.Test
import org.junit.After

import grails.validation.ValidationException
import groovy.sql.Sql
import net.hedtech.banner.general.system.*
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.springframework.orm.hibernate5.HibernateOptimisticLockingFailureException

@Integration
@Rollback
class CommonMatchingMatchEntryGlobalTemporaryIntegrationTests extends BaseIntegrationTestCase {

    //Test data for creating new domain instance
    //Valid test data (For success tests)
    def i_success_addressType
    def i_success_telephoneType
    def i_success_emailType
    def i_success_addressSource
    def i_success_state
    def i_success_nation
    def i_success_county

    def i_success_lastName = "TTTTT"
    def i_success_entity = "#"
    def i_success_firstName = "TTTTT"
    def i_success_middleInitial = "TTTTT"
    def i_success_commonMatchingMatchEntryGlobalTemporaryId = "TTTTT"
    def i_success_streetLine1 = "TTTTT"
    def i_success_streetLine2 = "TTTTT"
    def i_success_streetLine3 = "TTTTT"
    def i_success_city = "TTTTT"
    def i_success_zip = "TTTTT"
    def i_success_phoneArea = "TTTTT"
    def i_success_phoneNumber = "TTTTT"
    def i_success_phoneExtension = "TTTTT"
    def i_success_ssn = "TTTTT"
    def i_success_birthDay = "TT"
    def i_success_birthMonday = "TT"
    def i_success_birthYear = "TTTT"
    def i_success_sex = "#"
    def i_success_emailAddress = "TTTTT"
    def i_success_countryPhone = "TTTT"
    def i_success_houseNumber = "TTTTT"
    def i_success_streetLine4 = "TTTTT"
    def i_success_surnamePrefix = "TTTTT"
    //Invalid test data (For failure tests)
    def i_failure_addressType
    def i_failure_telephoneType
    def i_failure_emailType
    def i_failure_addressSource
    def i_failure_state
    def i_failure_nation
    def i_failure_county

    def i_failure_lastName = "TTTTT"
    def i_failure_entity = "#"
    def i_failure_firstName = "TTTTT"
    def i_failure_middleInitial = "TTTTT"
    def i_failure_commonMatchingMatchEntryGlobalTemporaryId = "TTTTT"
    def i_failure_streetLine1 = "TTTTT"
    def i_failure_streetLine2 = "TTTTT"
    def i_failure_streetLine3 = "TTTTT"
    def i_failure_city = "TTTTT"
    def i_failure_zip = "TTTTT"
    def i_failure_phoneArea = "TTTTT"
    def i_failure_phoneNumber = "TTTTT"
    def i_failure_phoneExtension = "TTTTT"
    def i_failure_ssn = "TTTTT"
    def i_failure_birthDay = "TT"
    def i_failure_birthMonday = "TT"
    def i_failure_birthYear = "TTTT"
    def i_failure_sex = "#"
    def i_failure_emailAddress = "TTTTT"
    def i_failure_countryPhone = "TTTT"
    def i_failure_houseNumber = "TTTTT"
    def i_failure_streetLine4 = "TTTTT"
    def i_failure_surnamePrefix = "TTTTT"

    //Test data for creating updating domain instance
    //Valid test data (For success tests)
    def u_success_addressType
    def u_success_telephoneType
    def u_success_emailType
    def u_success_addressSource
    def u_success_state
    def u_success_nation
    def u_success_county

    def u_success_lastName = "TTTTT"
    def u_success_entity = "#"
    def u_success_firstName = "TTTTT"
    def u_success_middleInitial = "TTTTT"
    def u_success_commonMatchingMatchEntryGlobalTemporaryId = "TTTTT"
    def u_success_streetLine1 = "TTTTT"
    def u_success_streetLine2 = "TTTTT"
    def u_success_streetLine3 = "TTTTT"
    def u_success_city = "TTTTT"
    def u_success_zip = "TTTTT"
    def u_success_phoneArea = "TTTTT"
    def u_success_phoneNumber = "TTTTT"
    def u_success_phoneExtension = "TTTTT"
    def u_success_ssn = "TTTTT"
    def u_success_birthDay = "TT"
    def u_success_birthMonday = "TT"
    def u_success_birthYear = "TTTT"
    def u_success_sex = "#"
    def u_success_emailAddress = "TTTTT"
    def u_success_countryPhone = "TTTT"
    def u_success_houseNumber = "TTTTT"
    def u_success_streetLine4 = "TTTTT"
    def u_success_surnamePrefix = "TTTTT"
    //Valid test data (For failure tests)
    def u_failure_addressType
    def u_failure_telephoneType
    def u_failure_emailType
    def u_failure_addressSource
    def u_failure_state
    def u_failure_nation
    def u_failure_county

    def u_failure_lastName = "TTTTT"
    def u_failure_entity = "#"
    def u_failure_firstName = "TTTTT"
    def u_failure_middleInitial = "TTTTT"
    def u_failure_commonMatchingMatchEntryGlobalTemporaryId = "TTTTT"
    def u_failure_streetLine1 = "TTTTT"
    def u_failure_streetLine2 = "TTTTT"
    def u_failure_streetLine3 = "TTTTT"
    def u_failure_city = "TTTTT"
    def u_failure_zip = "TTTTT"
    def u_failure_phoneArea = "TTTTT"
    def u_failure_phoneNumber = "TTTTT"
    def u_failure_phoneExtension = "TTTTT"
    def u_failure_ssn = "TTTTT"
    def u_failure_birthDay = "TT"
    def u_failure_birthMonday = "TT"
    def u_failure_birthYear = "TTTT"
    def u_failure_sex = "#"
    def u_failure_emailAddress = "TTTTT"
    def u_failure_countryPhone = "TTTT"
    def u_failure_houseNumber = "TTTTT"
    def u_failure_streetLine4 = "TTTTT"
    def u_failure_surnamePrefix = "TTTTT"


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
        i_success_addressType = AddressType.findByCode("MA")
        i_success_telephoneType = TelephoneType.findByCode("MA")
        i_success_emailType = EmailType.findByCode("HOME")
        i_success_addressSource = AddressSource.findByCode("POST")
        i_success_state = State.findByCode("PA")
        i_success_nation = Nation.findByCode("157")
        i_success_county = County.findByCode("001")

    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testCreateValidCommonMatchingMatchEntryGlobalTemporary() {
        def commonMatchingMatchEntryGlobalTemporary = newValidForCreateCommonMatchingMatchEntryGlobalTemporary()
        commonMatchingMatchEntryGlobalTemporary.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull commonMatchingMatchEntryGlobalTemporary.id
    }


    @Test
    void testCreateInvalidCommonMatchingMatchEntryGlobalTemporary() {
        def commonMatchingMatchEntryGlobalTemporary = newInvalidForCreateCommonMatchingMatchEntryGlobalTemporary()
        shouldFail(ValidationException) {
            commonMatchingMatchEntryGlobalTemporary.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testUpdateValidCommonMatchingMatchEntryGlobalTemporary() {
        def commonMatchingMatchEntryGlobalTemporary = newValidForCreateCommonMatchingMatchEntryGlobalTemporary()
        commonMatchingMatchEntryGlobalTemporary.save(failOnError: true, flush: true)
        assertNotNull commonMatchingMatchEntryGlobalTemporary.id
        assertEquals 0L, commonMatchingMatchEntryGlobalTemporary.version
        assertEquals i_success_lastName, commonMatchingMatchEntryGlobalTemporary.lastName
        assertEquals i_success_entity, commonMatchingMatchEntryGlobalTemporary.entity
        assertEquals i_success_firstName, commonMatchingMatchEntryGlobalTemporary.firstName
        assertEquals i_success_middleInitial, commonMatchingMatchEntryGlobalTemporary.middleInitial
        assertEquals i_success_commonMatchingMatchEntryGlobalTemporaryId, commonMatchingMatchEntryGlobalTemporary.commonMatchingMatchEntryGlobalTemporaryId
        assertEquals i_success_streetLine1, commonMatchingMatchEntryGlobalTemporary.streetLine1
        assertEquals i_success_streetLine2, commonMatchingMatchEntryGlobalTemporary.streetLine2
        assertEquals i_success_streetLine3, commonMatchingMatchEntryGlobalTemporary.streetLine3
        assertEquals i_success_city, commonMatchingMatchEntryGlobalTemporary.city
        assertEquals i_success_zip, commonMatchingMatchEntryGlobalTemporary.zip
        assertEquals i_success_phoneArea, commonMatchingMatchEntryGlobalTemporary.phoneArea
        assertEquals i_success_phoneNumber, commonMatchingMatchEntryGlobalTemporary.phoneNumber
        assertEquals i_success_phoneExtension, commonMatchingMatchEntryGlobalTemporary.phoneExtension
        assertEquals i_success_ssn, commonMatchingMatchEntryGlobalTemporary.ssn
        assertEquals i_success_birthDay, commonMatchingMatchEntryGlobalTemporary.birthDay
        assertEquals i_success_birthMonday, commonMatchingMatchEntryGlobalTemporary.birthMonday
        assertEquals i_success_birthYear, commonMatchingMatchEntryGlobalTemporary.birthYear
        assertEquals i_success_sex, commonMatchingMatchEntryGlobalTemporary.sex
        assertEquals i_success_emailAddress, commonMatchingMatchEntryGlobalTemporary.emailAddress
        assertEquals i_success_countryPhone, commonMatchingMatchEntryGlobalTemporary.countryPhone
        assertEquals i_success_houseNumber, commonMatchingMatchEntryGlobalTemporary.houseNumber
        assertEquals i_success_streetLine4, commonMatchingMatchEntryGlobalTemporary.streetLine4
        assertEquals i_success_surnamePrefix, commonMatchingMatchEntryGlobalTemporary.surnamePrefix

        //Update the entity
        commonMatchingMatchEntryGlobalTemporary.lastName = "UUUU"

        commonMatchingMatchEntryGlobalTemporary.save(failOnError: true, flush: true)
        //Assert for sucessful update
        commonMatchingMatchEntryGlobalTemporary = CommonMatchingMatchEntryGlobalTemporary.get(commonMatchingMatchEntryGlobalTemporary.id)
        assertEquals 1L, commonMatchingMatchEntryGlobalTemporary?.version
        assertEquals commonMatchingMatchEntryGlobalTemporary.lastName, "UUUU"
    }


    @Test
    void testUpdateInvalidCommonMatchingMatchEntryGlobalTemporary() {
        def commonMatchingMatchEntryGlobalTemporary = newValidForCreateCommonMatchingMatchEntryGlobalTemporary()
        commonMatchingMatchEntryGlobalTemporary.save(failOnError: true, flush: true)
        assertNotNull commonMatchingMatchEntryGlobalTemporary.id
        assertEquals 0L, commonMatchingMatchEntryGlobalTemporary.version
        assertEquals i_success_entity, commonMatchingMatchEntryGlobalTemporary.entity
        assertEquals i_success_firstName, commonMatchingMatchEntryGlobalTemporary.firstName
        assertEquals i_success_middleInitial, commonMatchingMatchEntryGlobalTemporary.middleInitial
        assertEquals i_success_commonMatchingMatchEntryGlobalTemporaryId, commonMatchingMatchEntryGlobalTemporary.commonMatchingMatchEntryGlobalTemporaryId
        assertEquals i_success_streetLine1, commonMatchingMatchEntryGlobalTemporary.streetLine1
        assertEquals i_success_streetLine2, commonMatchingMatchEntryGlobalTemporary.streetLine2
        assertEquals i_success_streetLine3, commonMatchingMatchEntryGlobalTemporary.streetLine3
        assertEquals i_success_city, commonMatchingMatchEntryGlobalTemporary.city
        assertEquals i_success_zip, commonMatchingMatchEntryGlobalTemporary.zip
        assertEquals i_success_phoneArea, commonMatchingMatchEntryGlobalTemporary.phoneArea
        assertEquals i_success_phoneNumber, commonMatchingMatchEntryGlobalTemporary.phoneNumber
        assertEquals i_success_phoneExtension, commonMatchingMatchEntryGlobalTemporary.phoneExtension
        assertEquals i_success_ssn, commonMatchingMatchEntryGlobalTemporary.ssn
        assertEquals i_success_birthDay, commonMatchingMatchEntryGlobalTemporary.birthDay
        assertEquals i_success_birthMonday, commonMatchingMatchEntryGlobalTemporary.birthMonday
        assertEquals i_success_birthYear, commonMatchingMatchEntryGlobalTemporary.birthYear
        assertEquals i_success_sex, commonMatchingMatchEntryGlobalTemporary.sex
        assertEquals i_success_emailAddress, commonMatchingMatchEntryGlobalTemporary.emailAddress
        assertEquals i_success_countryPhone, commonMatchingMatchEntryGlobalTemporary.countryPhone
        assertEquals i_success_houseNumber, commonMatchingMatchEntryGlobalTemporary.houseNumber
        assertEquals i_success_streetLine4, commonMatchingMatchEntryGlobalTemporary.streetLine4
        assertEquals i_success_surnamePrefix, commonMatchingMatchEntryGlobalTemporary.surnamePrefix

        //Update the entity with invalid values
        commonMatchingMatchEntryGlobalTemporary.entity = null
        shouldFail(ValidationException) {
            commonMatchingMatchEntryGlobalTemporary.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testOptimisticLock() {
        def commonMatchingMatchEntryGlobalTemporary = newValidForCreateCommonMatchingMatchEntryGlobalTemporary()
        commonMatchingMatchEntryGlobalTemporary.save(failOnError: true, flush: true)

        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("update GOTCMME set GOTCMME_VERSION = 999 where GOTCMME_SURROGATE_ID = ?", [commonMatchingMatchEntryGlobalTemporary.id])
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
        //Try to update the entity
        //Update the entity
        commonMatchingMatchEntryGlobalTemporary.lastName = "UUUU"

        shouldFail(HibernateOptimisticLockingFailureException) {
            commonMatchingMatchEntryGlobalTemporary.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testDeleteCommonMatchingMatchEntryGlobalTemporary() {
        def commonMatchingMatchEntryGlobalTemporary = newValidForCreateCommonMatchingMatchEntryGlobalTemporary()
        commonMatchingMatchEntryGlobalTemporary.save(failOnError: true, flush: true)
        def id = commonMatchingMatchEntryGlobalTemporary.id
        assertNotNull id
        commonMatchingMatchEntryGlobalTemporary.delete()
        assertNull CommonMatchingMatchEntryGlobalTemporary.get(id)
    }


    @Test
    void testValidation() {
        def commonMatchingMatchEntryGlobalTemporary = newInvalidForCreateCommonMatchingMatchEntryGlobalTemporary()
        assertFalse "CommonMatchingMatchEntryGlobalTemporary could not be validated as expected due to ${commonMatchingMatchEntryGlobalTemporary.errors}", commonMatchingMatchEntryGlobalTemporary.validate()
    }


    @Test
    void testNullValidationFailure() {
        def commonMatchingMatchEntryGlobalTemporary = new CommonMatchingMatchEntryGlobalTemporary()
        assertFalse "CommonMatchingMatchEntryGlobalTemporary should have failed validation", commonMatchingMatchEntryGlobalTemporary.validate()
        assertErrorsFor commonMatchingMatchEntryGlobalTemporary, 'nullable',
                [
                        'entity'
                ]
        assertNoErrorsFor commonMatchingMatchEntryGlobalTemporary,
                [
                        'lastName',
                        'firstName',
                        'middleInitial',
                        'commonMatchingMatchEntryGlobalTemporaryId',
                        'streetLine1',
                        'streetLine2',
                        'streetLine3',
                        'city',
                        'zip',
                        'phoneArea',
                        'phoneNumber',
                        'phoneExtension',
                        'ssn',
                        'birthDay',
                        'birthMonday',
                        'birthYear',
                        'sex',
                        'emailAddress',
                        'countryPhone',
                        'houseNumber',
                        'streetLine4',
                        'surnamePrefix',
                        'addressType',
                        'telephoneType',
                        'emailType',
                        'addressSource',
                        'state',
                        'nation',
                        'county'
                ]
    }


    @Test
    void testMaxSizeValidationFailures() {
        def commonMatchingMatchEntryGlobalTemporary = new CommonMatchingMatchEntryGlobalTemporary(
                lastName: 'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
                firstName: 'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
                middleInitial: 'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
                commonMatchingMatchEntryGlobalTemporaryId: 'XXXXXXXXXXX',
                streetLine1: 'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
                streetLine2: 'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
                streetLine3: 'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
                city: 'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
                zip: 'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
                phoneArea: 'XXXXXXXX',
                phoneNumber: 'XXXXXXXXXXXXXX',
                phoneExtension: 'XXXXXXXXXXXX',
                ssn: 'XXXXXXXXXXXXXXXXX',
                birthDay: 'XXXX',
                birthMonday: 'XXXX',
                birthYear: 'XXXXXX',
                sex: 'XXX',
                emailAddress: 'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
                countryPhone: 'XXXXXX',
                houseNumber: 'XXXXXXXXXXXX',
                streetLine4: 'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
                surnamePrefix: 'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX')
        assertFalse "CommonMatchingMatchEntryGlobalTemporary should have failed validation", commonMatchingMatchEntryGlobalTemporary.validate()
        assertErrorsFor commonMatchingMatchEntryGlobalTemporary, 'maxSize', ['lastName', 'firstName', 'middleInitial', 'commonMatchingMatchEntryGlobalTemporaryId', 'streetLine1', 'streetLine2', 'streetLine3', 'city', 'zip', 'phoneArea', 'phoneNumber', 'phoneExtension', 'ssn', 'birthDay', 'birthMonday', 'birthYear', 'sex', 'emailAddress', 'countryPhone', 'houseNumber', 'streetLine4', 'surnamePrefix']
    }


    private def newValidForCreateCommonMatchingMatchEntryGlobalTemporary() {
        def commonMatchingMatchEntryGlobalTemporary = new CommonMatchingMatchEntryGlobalTemporary(
                lastName: i_success_lastName,
                entity: i_success_entity,
                firstName: i_success_firstName,
                middleInitial: i_success_middleInitial,
                commonMatchingMatchEntryGlobalTemporaryId: i_success_commonMatchingMatchEntryGlobalTemporaryId,
                streetLine1: i_success_streetLine1,
                streetLine2: i_success_streetLine2,
                streetLine3: i_success_streetLine3,
                city: i_success_city,
                zip: i_success_zip,
                phoneArea: i_success_phoneArea,
                phoneNumber: i_success_phoneNumber,
                phoneExtension: i_success_phoneExtension,
                ssn: i_success_ssn,
                birthDay: i_success_birthDay,
                birthMonday: i_success_birthMonday,
                birthYear: i_success_birthYear,
                sex: i_success_sex,
                emailAddress: i_success_emailAddress,
                countryPhone: i_success_countryPhone,
                houseNumber: i_success_houseNumber,
                streetLine4: i_success_streetLine4,
                surnamePrefix: i_success_surnamePrefix,
                addressType: i_success_addressType,
                telephoneType: i_success_telephoneType,
                emailType: i_success_emailType,
                addressSource: i_success_addressSource,
                state: i_success_state,
                nation: i_success_nation,
                county: i_success_county,
        )
        return commonMatchingMatchEntryGlobalTemporary
    }


    private def newInvalidForCreateCommonMatchingMatchEntryGlobalTemporary() {
        //creating with null entity
        def commonMatchingMatchEntryGlobalTemporary = new CommonMatchingMatchEntryGlobalTemporary(
                lastName: i_success_lastName,
                entity: null,
                firstName: i_success_firstName,
                middleInitial: i_success_middleInitial,
                commonMatchingMatchEntryGlobalTemporaryId: i_success_commonMatchingMatchEntryGlobalTemporaryId,
                streetLine1: i_success_streetLine1,
                streetLine2: i_success_streetLine2,
                streetLine3: i_success_streetLine3,
                city: i_success_city,
                zip: i_success_zip,
                phoneArea: i_success_phoneArea,
                phoneNumber: i_success_phoneNumber,
                phoneExtension: i_success_phoneExtension,
                ssn: i_success_ssn,
                birthDay: i_success_birthDay,
                birthMonday: i_success_birthMonday,
                birthYear: i_success_birthYear,
                sex: i_success_sex,
                emailAddress: i_success_emailAddress,
                countryPhone: i_success_countryPhone,
                houseNumber: i_success_houseNumber,
                streetLine4: i_success_streetLine4,
                surnamePrefix: i_success_surnamePrefix,
                addressType: i_success_addressType,
                telephoneType: i_success_telephoneType,
                emailType: i_success_emailType,
                addressSource: i_success_addressSource,
                state: i_success_state,
                nation: i_success_nation,
                county: i_success_county,
        )
        return commonMatchingMatchEntryGlobalTemporary
    }

}
