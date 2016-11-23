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
        def ssbRule = newSsbRuleSqlProcess(new Date()-1)
        ssbRule.save(failOnError: true, flush: true)

        def roles = ['EMPLOYEE', 'STUDENT']
        def pidm = PersonUtility.getPerson("GDP000005").pidm
        def phoneType = [code: 'T2']
        personalInformationCompositeService.validateTelephoneTypeRule(phoneType, pidm, roles)
    }

    @Test
    void testValidateTelephoneTypeRuleInvalid() {
        def ssbRule = newSsbRuleSqlProcess(new Date()-1)
        ssbRule.save(failOnError: true, flush: true)

        def roles = ['EMPLOYEE']
        def pidm = PersonUtility.getPerson("GDP000005").pidm
        def phoneType = [code: 'T2']
        try {
            personalInformationCompositeService.validateTelephoneTypeRule(phoneType, pidm, roles)
            fail("I should have received an error but it passed; @@r1:invalidTelephoneType@@ ")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "invalidTelephoneType"
        }
    }

    @Test
    void testFetchUpdateableTelephoneTypeList() {
        def ssbRule = newSsbRuleSqlProcess(new Date()-1)
        ssbRule.save(failOnError: true, flush: true)

        def roles = ['EMPLOYEE', 'STUDENT']
        def pidm = PersonUtility.getPerson("GDP000005").pidm
        def phoneTypeList = personalInformationCompositeService.fetchUpdateableTelephoneTypeList(pidm, roles)

        assertLength 2, phoneTypeList
        assertTrue phoneTypeList.code.contains('T2')
        assertTrue phoneTypeList.code.contains('T3')
    }

    @Test
    void testFetchUpdateableTelephoneTypeListSearch() {
        def ssbRule = newSsbRuleSqlProcess(new Date()-1)
        ssbRule.save(failOnError: true, flush: true)

        def roles = ['EMPLOYEE', 'STUDENT']
        def pidm = PersonUtility.getPerson("GDP000005").pidm
        def phoneTypeList = personalInformationCompositeService.fetchUpdateableTelephoneTypeList(pidm, roles, 10, 0, 's')

        assertLength 0, phoneTypeList
    }

    private def newSsbRuleSqlProcess(def startDate) {
        def sqlString = "SELECT 'Y' FROM DUAL WHERE ( :ROLE_STUDENT = 'Y' OR :ROLE_FINAID = 'Y') AND :TELEPHONE_TYPE IN ('T2', 'T3')"
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
                entriesForSqlProcess: EntriesForSqlProcesss.findByCode('SSB_TELEPHONE_UPDATE'),
                entriesForSql: EntriesForSql.findByCode('SSB_TELEPHONE_UPDATE')
        )
        return sqlProcess
    }

}