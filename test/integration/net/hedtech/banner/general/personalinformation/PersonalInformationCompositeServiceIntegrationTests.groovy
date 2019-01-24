/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.personalinformation

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.overall.SqlProcess
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.general.system.EntriesForSql
import net.hedtech.banner.general.system.EntriesForSqlProcesss
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

class PersonalInformationCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    def personalInformationCompositeService

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
    void testGetPersonValidationObjects() {
        def roles = ['EMPLOYEE']
        def person = [:]
        person.addressType = [code: 'PR', description: 'Permanent']
        person.nation = [code: '155', description: 'United Arab Emirates']
        person.relationship = [code: 'P', description: 'Spouse']

        def result = personalInformationCompositeService.getPersonValidationObjects(person, roles)

        assertNotNull result.addressType.id
        assertNotNull result.nation.id
        assertNotNull result.relationship.id
    }

    @Test
    void testGetPersonValidationObjectsBadAtyp() {
        def roles = ['EMPLOYEE']
        def address = [:]
        address.addressType = [code: 'W2', description: 'W2 Address']
        address.nation = [code: '155', description: 'United Arab Emirates']

        try{
            def result = personalInformationCompositeService.getPersonValidationObjects(address, roles)
            fail("I should have received an error but it passed; @@r1:invalidAddressType@@ ")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "invalidAddressType"
        }
    }

    @Test
    void testGetPersonValidationObjectsBadCounty() {
        def roles = ['EMPLOYEE']
        def address = [:]
        address.addressType = [code: 'PR', description: 'Permanent']
        address.county = [code: 'Z989', description: 'fail']

        try{
            def result = personalInformationCompositeService.getPersonValidationObjects(address, roles)
            fail("I should have received an error but it passed; @@r1:invalidCounty@@ ")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "invalidCounty"
        }
    }

    @Test
    void testGetPersonValidationObjectsBadState() {
        def roles = ['EMPLOYEE']
        def address = [:]
        address.addressType = [code: 'PR', description: 'Permanent']
        address.state = [code: 'XJ', description: 'fail']
        address.county = [code: '251', description: 'Los Angeles County']

        try{
            def result = personalInformationCompositeService.getPersonValidationObjects(address, roles)
            fail("I should have received an error but it passed; @@r1:invalidState@@ ")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "invalidState"
        }
    }

    @Test
    void testGetPersonValidationObjectsBadNation() {
        def roles = ['EMPLOYEE']
        def address = [:]
        address.addressType = [code: 'PR', description: 'Permanent']
        address.nation = [code: '8080', description: 'fail']
        address.state = [code: 'TX', description: 'Texas']
        address.county = [code: '251', description: 'Los Angeles County']

        try{
            def result = personalInformationCompositeService.getPersonValidationObjects(address, roles)
            fail("I should have received an error but it passed; @@r1:invalidNation@@ ")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "invalidNation"
        }
    }

    @Test
    void testGetPersonValidationObjectsBadRelationship() {
        def roles = ['EMPLOYEE']
        def person = [:]
        person.addressType = [code: 'PR', description: 'Permanent']
        person.state = [code: 'TX', description: 'Texas']
        person.county = [code: '251', description: 'Los Angeles County']
        person.relationship = [code: 'Z', description: 'fail']

        try{
            def result = personalInformationCompositeService.getPersonValidationObjects(person, roles)
            fail("I should have received an error but it passed; @@r1:invalidRelationship@@ ")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "invalidRelationship"
        }
    }

    @Test
    void testValidateTelephoneTypeRule() {
        setupTelephoneRule()

        def roles = ['EMPLOYEE', 'STUDENT']
        def pidm = PersonUtility.getPerson("GDP000005").pidm
        def phoneType = [code: 'T2']
        personalInformationCompositeService.validateTelephoneTypeRule(phoneType, pidm, roles)
    }

    @Test
    void testValidateTelephoneTypeRuleInvalid() {
        setupTelephoneRule()

        def roles = ['EMPLOYEE']
        def pidm = PersonUtility.getPerson("GDP000005").pidm
        def phoneType = [code: 'T2']
        try {
            personalInformationCompositeService.validateTelephoneTypeRule(phoneType, pidm, roles)
            fail("I should have received an error but it passed; @@r1:invalidTelephoneTypeUpdate@@ ")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "invalidTelephoneTypeUpdate"
        }
    }

    @Test
    void testPopulateTelephoneUpdateableStatus() {
        setupTelephoneRule()

        def roles = ['EMPLOYEE', 'STUDENT']
        def pidm = PersonUtility.getPerson("GDP000005").pidm
        def telephones = [
                [
                    telephoneType: [code: 'T2'],
                    pidm: pidm
                ]
        ]

        personalInformationCompositeService.populateTelephoneUpdateableStatus(telephones, roles)

        assertTrue telephones[0].isUpdateable
    }

    @Test
    void testPopulateTelephoneUpdateableStatusWhereNotUpdateable() {
        setupTelephoneRule()

        def roles = ['EMPLOYEE', 'STUDENT']
        def pidm = PersonUtility.getPerson("GDP000005").pidm
        def telephones = [
                [
                    telephoneType: [code: 'T4'],
                    pidm: pidm
                ]
        ]

        personalInformationCompositeService.populateTelephoneUpdateableStatus(telephones, roles)

        assertFalse telephones[0].isUpdateable
    }

    @Test
    void testFetchUpdateableTelephoneTypeList() {
        setupTelephoneRule()

        def roles = ['EMPLOYEE', 'STUDENT']
        def pidm = PersonUtility.getPerson("GDP000005").pidm
        def phoneTypeList = personalInformationCompositeService.fetchUpdateableTelephoneTypeList(pidm, roles)

        assertLength 2, phoneTypeList
        assertTrue phoneTypeList.code.contains('T2')
        assertTrue phoneTypeList.code.contains('T3')
    }

    @Test
    void testFetchUpdateableTelephoneTypeListSearch() {
        setupTelephoneRule()

        def roles = ['EMPLOYEE', 'STUDENT']
        def pidm = PersonUtility.getPerson("GDP000005").pidm
        def phoneTypeList = personalInformationCompositeService.fetchUpdateableTelephoneTypeList(pidm, roles, 10, 0, 's')

        assertLength 0, phoneTypeList
    }

    @Test
    void testValidateEmailTypeRule() {
        setupEmailRules()

        def roles = ['EMPLOYEE', 'STUDENT']
        def pidm = PersonUtility.getPerson("GDP000005").pidm
        def emailType = [code: 'PERS']
        personalInformationCompositeService.validateEmailTypeRule(emailType, pidm, roles)
    }

    @Test
    void testValidateEmailTypeRuleInvalid() {
        setupEmailRules()

        def roles = ['EMPLOYEE']
        def pidm = PersonUtility.getPerson("GDP000005").pidm
        def emailType = [code: 'HOME']
        try {
            personalInformationCompositeService.validateEmailTypeRule(emailType, pidm, roles)
            fail("I should have received an error but it passed; @@r1:invalidEmailTypeUpdate@@ ")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "invalidEmailTypeUpdate"
        }
    }

    @Test
    void testPopulateEmailUpdateableStatus() {
        setupEmailRules()

        def roles = ['EMPLOYEE', 'STUDENT']
        def pidm = PersonUtility.getPerson("GDP000005").pidm
        def emails = [
                [
                        emailType: [code: 'DORM'],
                        pidm: pidm
                ]
        ]

        personalInformationCompositeService.populateEmailUpdateableStatus(emails, roles)

        assertTrue emails[0].isUpdateable
    }

    @Test
    void testPopulateEmailUpdateableStatusWhereNotUpdateable() {
        setupEmailRules()

        def roles = ['EMPLOYEE', 'STUDENT']
        def pidm = PersonUtility.getPerson("GDP000005").pidm
        def emails = [
                [
                        emailType: [code: 'HOME'],
                        pidm: pidm
                ]
        ]

        personalInformationCompositeService.populateEmailUpdateableStatus(emails, roles)

        assertFalse emails[0].isUpdateable
    }

    @Test
    void testFetchUpdateableEmailTypeList() {
        setupEmailRules()

        def roles = ['EMPLOYEE', 'STUDENT']
        def pidm = PersonUtility.getPerson("GDP000005").pidm
        def phoneTypeList = personalInformationCompositeService.fetchUpdateableEmailTypeList(pidm, roles)

        assertLength 10, phoneTypeList
        assertFalse phoneTypeList.code.contains('BI')
    }

    @Test
    void testFetchUpdateableEmailTypeListSearch() {
        setupEmailRules()

        def roles = ['EMPLOYEE', 'STUDENT']
        def pidm = PersonUtility.getPerson("GDP000005").pidm
        def emailTypeList = personalInformationCompositeService.fetchUpdateableEmailTypeList(pidm, roles, 10, 0, 's')

        assertLength 10, emailTypeList
        assertFalse emailTypeList.code.contains('CAMP')
    }

    @Test
    void testFetchUpdateableEmailTypeListSearchAndFind() {
        setupEmailRules()

        def roles = ['STUDENT']
        def pidm = PersonUtility.getPerson("GDP000005").pidm
        def phoneTypeList = personalInformationCompositeService.fetchUpdateableEmailTypeList(pidm, roles, 10, 0, 's')

        assertLength 10, phoneTypeList
        assertTrue phoneTypeList.code.contains('CAMP')
    }

    private def setupTelephoneRule() {
        def ssbRule = newSsbRuleSqlProcess(
                new Date()-1,
                "SELECT 'Y' FROM DUAL WHERE ( :ROLE_STUDENT = 'Y' OR :ROLE_FINAID = 'Y') AND :TELEPHONE_TYPE IN ('T2', 'T3')",
                'SSB_TELEPHONE_UPDATE'
        )
        ssbRule.save(failOnError: true, flush: true)
    }

    private def setupEmailRules() {
        def ssbRule1 = newSsbRuleSqlProcess(
                new Date()-1,
                "SELECT 'N' FROM DUAL WHERE ( :ROLE_EMPLOYEE = 'Y' OR :ROLE_FINAID = 'Y') AND :EMAIL_TYPE IN ('BI', 'CAMP', 'HOME')",
                'SSB_EMAIL_UPDATE'
        )
        ssbRule1.save(failOnError: true, flush: true)
        def ssbRule2 = newSsbRuleSqlProcess(
                new Date()-1,
                "SELECT 'Y' FROM DUAL",
                'SSB_EMAIL_UPDATE'
        )
        ssbRule2.sequenceNumber = 7
        ssbRule2.save(failOnError: true, flush: true)
    }

    private def newSsbRuleSqlProcess(def startDate, def sqlString, def rule) {
        def sqlProcess = new SqlProcess(
                sequenceNumber: 6,
                activeIndicator: true,
                validatedIndicator: true,
                startDate: startDate,
                selectFrom: "FROM",
                selectValue: null,
                whereClause: sqlString,
                endDate: startDate + 2,
                parsedSql: sqlString,
                systemRequiredIndicator: false,
                entriesForSqlProcess: EntriesForSqlProcesss.findByCode(rule),
                entriesForSql: EntriesForSql.findByCode(rule)
        )
        return sqlProcess
    }

}