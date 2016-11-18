/*********************************************************************************
 Copyright 2014-2016 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.person.ldm

import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.commonmatching.CommonMatchingSourceRule
import net.hedtech.banner.general.lettergeneration.PopulationSelectionExtract
import net.hedtech.banner.general.lettergeneration.PopulationSelectionExtractReadonly
import net.hedtech.banner.general.overall.IntegrationConfiguration
import net.hedtech.banner.general.overall.ldm.GlobalUniqueIdentifier
import net.hedtech.banner.general.overall.ldm.LdmService
import net.hedtech.banner.general.person.*
import net.hedtech.banner.general.system.*
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
import org.junit.After
import org.junit.Before
import org.junit.Test

class PersonCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    def personCompositeService
    def personBasicPersonBaseService
    def userRoleCompositeService

    private static final log = Logger.getLogger(getClass())

    //Test data for creating new domain instance
    //Valid test data (For success tests)
    def i_success_legacy
    def i_success_ethnicity
    def i_success_maritalStatus
    def i_success_religion
    def i_success_citizenType
    def i_success_stateBirth
    def i_success_stateDriver
    def i_success_nationDriver
    def i_success_pidm
    def i_success_ssn = "TTTTT"
    def i_success_birthDate = new Date()
    def i_success_sex = "M"
    def i_success_confidIndicator = "Y"
    def i_success_deadIndicator = "Y"
    def i_success_vetcFileNumber = "TTTTT"
    def i_success_legalName = "Levitch"
    def i_success_preferenceFirstName = "Happy"
    def i_success_namePrefix = "TTTTT"
    def i_success_nameSuffix = "TTTTT"
    def i_success_veraIndicator = "V"
    def i_success_citizenshipIndicator = "U"
    def i_success_deadDate = new Date()
    def i_success_hair = "TT"
    def i_success_eyeColor = "TT"
    def i_success_cityBirth = "TTTTT"
    def i_success_driverLicense = "TTTTT"
    def i_success_height = 1
    def i_success_weight = 1
    def i_success_sdvetIndicator = "Y"
    def i_success_licenseIssuedDate = new Date()
    def i_success_licenseExpiresDate = new Date()
    def i_success_incarcerationIndicator = "#"
    def i_success_itin = 1L
    def i_success_activeDutySeprDate = new Date()
    def i_success_ethnic = "1"
    def i_success_confirmedRe = "Y"
    def i_success_confirmedReDate = new Date()
    def i_success_armedServiceMedalVetIndicator = true
    def i_success_guid_personal = "abcd1234ttyy222223s"
    def i_success_emailAddress_personal = "xyz@ellucian.com"
    def i_success_emailType_personal = "Personal"
    def i_success_guid_institution = "xyzqweqw32312321zczx"
    def i_success_emailAddress_institution = "abc@ellucian.com"
    def i_success_emailType_institution = "Institution"
    def i_success_guid_work = "323123sdsdds3123asdasd123"
    def i_success_emailAddress_work = "123@ellucian.com"
    def i_success_emailType_work = "Work"
    def i_success_emailType_preferred = "Preferred"
    def i_success_first_name = "Mark"
    def i_success_middle_name = "TR"
    def i_success_last_name = "Mccallon"
    def i_success_primary_name_type = "Primary"
    def i_success_address_type_1 = "Mailing"
    def i_success_address_type_2 = "Home"
    def i_success_city = "Pavo"
    def i_success_state = "GA"
    def i_success_zip = "31778"
    def i_success_state_goriccr_data = "UNK"
    def i_success_zip_goriccr_data = "UNKNOWN"
    def i_success_street_line1 = "123 Main Line"
    def i_success_credential_id1 = "Elevate0001"
    def i_success_credential_type1 = "Banner Sourced ID"
    def i_success_credential_id2 = "sjorden"
    def i_success_credential_type2 = "Banner User Name"
    def i_success_credential_id3 = "DSTERLIN"
    def i_success_credential_type3 = "Banner UDC ID"
    def i_success_credential_id4 = "HOSP0001"
    def i_success_credential_type4 = "Banner ID"
    def i_update_credential_id = "TTTT"
    def i_update_credential_type = "Social Security Number"
    def i_success_alternate_first_name = "John"
    def i_success_alternate_middle_name = "A"
    def i_success_alternate_last_name = "Jorden"
    def i_success_alternate_birth_name_type = "Birth"
    def i_success_phone_type_work = "Work"
    def i_success_phone_type_mobile = "Mobile"
    def i_success_phone_type_home = "Home"
    def i_success_phone_type_residence = "Residence"
    def i_success_phone_extension = "2341643"
    def i_succes_invalid_phone_number_short1 = "12345678"
    def i_succes_invalid_phone_number_short2 = "+91-234-5678"
    def i_succes_invalid_phone_number_short3 = "123 456 78"
    def i_succes_invalid_phone_number_short4 = "+91 234 5678"
    def i_succes_invalid_phone_number_long1 = "+4412345678901235624351345314654756835651324135234647"
    def i_succes_invalid_phone_number_long2 = "+01-123-456-7890"
    def i_succes_invalid_phone_number_long3 = "123 456 7890123"
    def i_succes_invalid_phone_number_long4 = "+01 123 456 7890"
    static final String PERSON_UPDATESSN = "PERSON.UPDATESSN"
    static final String PROCESS_CODE = "HEDM"

    def i_success_credential_type4_filter="Banner ID"
    def i_failure_credential_type4_filter="BannerId"
    def i_success_credential_filter="ADVAF0021"
    def i_failure_credential_filter="HOSP00"



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
        i_success_legacy = Legacy.findByCode("M")
        i_success_ethnicity = Ethnicity.findByCode("1")
        i_success_maritalStatus = MaritalStatus.findByCode("S")
        i_success_religion = Religion.findByCode("JE")
        i_success_citizenType = CitizenType.findByCode("Y")
        i_success_stateBirth = State.findByCode("DE")
        i_success_stateDriver = State.findByCode("PA")
        i_success_nationDriver = Nation.findByCode("157")
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testListQapiWithValidFirstAndLastNameV3() {
        //we will forcefully set the content type so that the tests go through all possible code flows
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.v3+json")
        request.addHeader("Accept", "application/vnd.hedtech.integration.v3+json")

        Map params = getParamsWithReqiuredFields()
        def persons = personCompositeService.list(params)

        assertNotNull persons
        assertFalse persons.isEmpty()

        def firstPerson = persons.first()
        assertNotNull firstPerson

        persons.each { person ->
            def primaryName = person.names.find { primaryNameType ->
                primaryNameType.nameType == "Primary"
            }
            assertEquals primaryName.firstName, params.names[0].firstName
            assertEquals primaryName.lastName, params.names[0].lastName
        }
    }


    @Test
    void testListQapiWithInValidFirstAndLastName() {
        //we will forcefully set the content type so that the tests go through all possible code flows
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.v3+json")
        request.addHeader("Accept", "application/vnd.hedtech.integration.v3+json")

        Map params = getParamsWithReqiuredFields()
        params.names[0].firstName = "MarkTT"
        params.names[0].lastName = "Kole"
        def persons = personCompositeService.list(params)

        assertNotNull persons
        assertTrue persons.isEmpty()
    }


    @Test
    void testListQapiWithInValidDateOfBirthV3() {
        //we will forcefully set the content type so that the tests go through all possible code flows
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.v3+json")
        request.addHeader("Accept", "application/vnd.hedtech.integration.v3+json")

        Map params = getParamsWithReqiuredFields()

        params.dateOfBirth = "12-1973-30"
        try {
            personCompositeService.list(params)
            fail('This should have failed as the expected date formate is yyyy-mm-dd')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'date.invalid.format.message'
        }

        params.dateOfBirth = "DEC-30-1973"

        try {
            personCompositeService.list(params)
            fail('This should have failed as the expected date formate is yyyy-mm-dd')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'date.invalid.format.message'
        }
    }

    @Test
    void testListPersonQapiWithBirthNameTypeV3() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.v3+json")
        request.addHeader("Accept", "application/vnd.hedtech.integration.v3+json")

        Map content = newPersonWithAlternateNameHavingBirthNameType()

        def o_success_person_create = personCompositeService.create(content)

        assertNotNull o_success_person_create
        assertNotNull o_success_person_create.guid
        assertEquals 2, o_success_person_create.names?.size()

        def o_primary_name_create = o_success_person_create.names.find { it.nameType == "Primary" }
        def o_birth_name_create = o_success_person_create.names.find { it.nameType == "Birth" }

        assertNotNull o_primary_name_create
        assertEquals i_success_first_name, o_primary_name_create.firstName
        assertEquals i_success_middle_name, o_primary_name_create.middleName
        assertEquals i_success_last_name, o_primary_name_create.lastName
        assertEquals i_success_primary_name_type, o_primary_name_create.nameType
        assertEquals i_success_namePrefix, o_primary_name_create.title
        assertEquals i_success_nameSuffix, o_primary_name_create.pedigree
        assertEquals i_success_preferenceFirstName, o_primary_name_create.preferredName
        assertNotNull o_birth_name_create
        assertEquals i_success_alternate_first_name, o_birth_name_create.firstName
        assertEquals i_success_alternate_middle_name, o_birth_name_create.middleName
        assertEquals i_success_alternate_last_name, o_birth_name_create.lastName
        assertEquals i_success_alternate_birth_name_type, o_birth_name_create.nameType

        request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.v3+json")
        request.addHeader("Accept", "application/vnd.hedtech.integration.v3+json")

        Map params = getPersonBirthNameTypeFields()
        def o_success_persons = personCompositeService.list(params)

        assertNotNull o_success_persons
        assertFalse o_success_persons.isEmpty()
        def firstPerson = o_success_persons.first()
        assertNotNull firstPerson
        o_success_persons.each { person ->
            def birthName = person.names.find { it.nameType == i_success_alternate_birth_name_type }
            assertEquals i_success_alternate_first_name, birthName.firstName
            assertEquals i_success_alternate_last_name, birthName.lastName
        }
    }


    @Test
    void testCMSearchWithMultipleEmailsV3() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.v3+json")
        request.addHeader("Accept", "application/vnd.hedtech.integration.v3+json")

        def person = PersonUtility.getPerson('HOSFE2020')
        assertNotNull person
        def emails = PersonEmail.findAllByPidm(person.pidm)
        assertTrue emails.size() > 1
        // see that person has all of the integration emails
        def emailInstitutionRuleValue = IntegrationConfiguration.fetchAllByProcessCodeAndSettingNameAndTranslationValue(personCompositeService.PROCESS_CODE,
                personCompositeService.PERSON_EMAIL_TYPE, "Institution")[0]?.value
        assertNotNull emailInstitutionRuleValue
        def emailPersonalRuleValue = IntegrationConfiguration.fetchAllByProcessCodeAndSettingNameAndTranslationValue(personCompositeService.PROCESS_CODE,
                personCompositeService.PERSON_EMAIL_TYPE, "Personal")[0]?.value
        assertNotNull emailPersonalRuleValue
        def emailWorkRuleValue = IntegrationConfiguration.fetchAllByProcessCodeAndSettingNameAndTranslationValue(personCompositeService.PROCESS_CODE,
                personCompositeService.PERSON_EMAIL_TYPE, "Work")[0]?.value
        assertNotNull emailWorkRuleValue
        assertNotNull emails.find { it.emailType.code == emailWorkRuleValue }
        assertNotNull emails.find { it.emailType.code == emailPersonalRuleValue }
        assertNotNull emails.find { it.emailType.code == emailInstitutionRuleValue }

        def homeEmail = emails.find { it.emailType.code == emailPersonalRuleValue }.emailAddress
        def workEmail = emails.find { it.emailType.code == emailWorkRuleValue }.emailAddress
        def schoolEmail = emails.find { it.emailType.code == emailInstitutionRuleValue }.emailAddress
        // build content for common matching
        Map params = [action: [POST: "list"],
                      names : [[lastName: person.lastName, firstName: person.firstName, nameType: "Primary",]],
                      emails: [[emailAddress: homeEmail, emailType: emailPersonalRuleValue],
                               [emailAddress: schoolEmail, emailType: emailInstitutionRuleValue],
                               [emailAddress: workEmail, emailType: emailWorkRuleValue]]
        ]
        def matched_persons = personCompositeService.list(params)
        // assert that only one match comes back
        assertEquals 1, matched_persons.size()
        assertEquals matched_persons[0].pidm, person.pidm
    }


    @Test
    void testCMSearchWithMultipleEmailsSomeNotMatchingV3() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.v3+json")
        request.addHeader("Accept", "application/vnd.hedtech.integration.v3+json")

        def person = PersonUtility.getPerson('HOSFE2020')
        assertNotNull person
        def emails = PersonEmail.findAllByPidm(person.pidm)
        assertTrue emails.size() > 1
        // see that person has all of the integration emails
        def emailInstitutionRuleValue = IntegrationConfiguration.fetchAllByProcessCodeAndSettingNameAndTranslationValue(personCompositeService.PROCESS_CODE,
                personCompositeService.PERSON_EMAIL_TYPE, i_success_emailType_institution)[0]?.value
        assertNotNull emailInstitutionRuleValue
        def emailPersonalRuleValue = IntegrationConfiguration.fetchAllByProcessCodeAndSettingNameAndTranslationValue(personCompositeService.PROCESS_CODE,
                personCompositeService.PERSON_EMAIL_TYPE, i_success_emailType_personal)[0]?.value
        assertNotNull emailPersonalRuleValue
        def emailWorkRuleValue = IntegrationConfiguration.fetchAllByProcessCodeAndSettingNameAndTranslationValue(personCompositeService.PROCESS_CODE,
                personCompositeService.PERSON_EMAIL_TYPE, i_success_emailType_work)[0]?.value
        assertNotNull emailWorkRuleValue
        assertNotNull emails.find { it.emailType.code == emailWorkRuleValue }
        assertNotNull emails.find { it.emailType.code == emailPersonalRuleValue }
        assertNotNull emails.find { it.emailType.code == emailInstitutionRuleValue }
        def homeEmail = emails.find { it.emailType.code == emailPersonalRuleValue }.emailAddress
        def workEmail = 'work@email.com'
        def schoolEmail = 'test@email.com'
        // build content for common matching
        Map params = [action: [POST: "list"],
                      names : [[lastName: person.lastName, firstName: person.firstName, nameType: "Primary",]],
                      emails: [[emailAddress: homeEmail, emailType: i_success_emailType_personal],
                               [emailAddress: schoolEmail, emailType: i_success_emailType_institution],
                               [emailAddress: workEmail, emailType: i_success_emailType_work]]
        ]

        def matched_persons = personCompositeService.list(params)
        // assert that only one match comes back
        assertEquals 1, matched_persons.size()
        assertEquals matched_persons[0].pidm, person.pidm
    }


    @Test
    void testCMSearchWithMultipleEmailsNoneMatching() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.v3+json")
        request.addHeader("Accept", "application/vnd.hedtech.integration.v3+json")

        IntegrationConfiguration personMatchRule = IntegrationConfiguration.findByProcessCodeAndSettingName(personCompositeService.PROCESS_CODE, personCompositeService.PERSON_MATCH_RULE)
        assertNotNull personMatchRule?.value
        def sourceCode = CommonMatchingSource.findByCode(personMatchRule?.value)
        assertNotNull sourceCode

        def sources = CommonMatchingSourceRule.findAllByCommonMatchingSource(sourceCode)
        assertTrue sources.size() > 0
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def rules = sql.rows("select gorcmsr_column_name from gorcmsr where  GORCMSr_CMSC_CODE = ?", [sourceCode.code])
        assertTrue rules.size() > 0

        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPBPERS_BIRTH_DATE" }
        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPBPERS_SEX" }
        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPBPERS_SSN" }
        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "GOREMAL_EMAIL_ADDRESS" }

        def person = PersonUtility.getPerson('HOSFE2020')
        assertNotNull person
        def emails = PersonEmail.findAllByPidm(person.pidm)
        assertTrue emails.size() > 1
        // see that person has all of the integration emails
        def emailInstitutionRuleValue = IntegrationConfiguration.fetchAllByProcessCodeAndSettingNameAndTranslationValue(personCompositeService.PROCESS_CODE,
                personCompositeService.PERSON_EMAIL_TYPE, i_success_emailType_institution)[0]?.value
        assertNotNull emailInstitutionRuleValue
        def emailPersonalRuleValue = IntegrationConfiguration.fetchAllByProcessCodeAndSettingNameAndTranslationValue(personCompositeService.PROCESS_CODE,
                personCompositeService.PERSON_EMAIL_TYPE, i_success_emailType_personal)[0]?.value
        assertNotNull emailPersonalRuleValue
        def emailWorkRuleValue = IntegrationConfiguration.fetchAllByProcessCodeAndSettingNameAndTranslationValue(personCompositeService.PROCESS_CODE,
                personCompositeService.PERSON_EMAIL_TYPE, i_success_emailType_work)[0]?.value
        assertNotNull emailWorkRuleValue
        assertNotNull emails.find { it.emailType.code == emailWorkRuleValue }
        assertNotNull emails.find { it.emailType.code == emailPersonalRuleValue }
        assertNotNull emails.find { it.emailType.code == emailInstitutionRuleValue }
        // change the email addresses so they dont match
        def homeEmail = 'home@email.com'
        def workEmail = 'work@email.com'
        def schoolEmail = 'test@email.com'
        // build content for common matching
        Map params = [action: [POST: "list"],
                      names : [[lastName: person.lastName, firstName: person.firstName, nameType: "Primary",]],
                      emails: [[emailAddress: homeEmail, emailType: i_success_emailType_personal],
                               [emailAddress: schoolEmail, emailType: i_success_emailType_institution],
                               [emailAddress: workEmail, emailType: i_success_emailType_work]]
        ]

        def matched_persons = personCompositeService.list(params)
        // assert that no matches come  back
        assertEquals 0, matched_persons.size()
    }


    @Test
    void testCMSearchWithBirthDateV3() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.v3+json")
        request.addHeader("Accept", "application/vnd.hedtech.integration.v3+json")

        IntegrationConfiguration personMatchRule = IntegrationConfiguration.findByProcessCodeAndSettingName(personCompositeService.PROCESS_CODE, personCompositeService.PERSON_MATCH_RULE)
        assertNotNull personMatchRule?.value
        def sourceCode = CommonMatchingSource.findByCode(personMatchRule?.value)
        assertNotNull sourceCode

        def sources = CommonMatchingSourceRule.findAllByCommonMatchingSource(sourceCode)
        assertTrue sources.size() > 0
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def rules = sql.rows("select gorcmsr_column_name from gorcmsr where  GORCMSr_CMSC_CODE = ?", [sourceCode.code])
        assertTrue rules.size() > 0

        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPBPERS_BIRTH_DATE" }
        def person = PersonUtility.getPerson('HOSFE2020')
        assertNotNull person
        def bio = PersonBasicPersonBase.findByPidm(person.pidm)
        assertNotNull bio
        assertNotNull bio.birthDate
        assertEquals '03/17/1986', bio.birthDate.format('MM/dd/yyyy')

        // build content for common matching
        Map params = [action     : [POST: "list"],
                      names      : [[lastName: person.lastName, firstName: person.firstName, nameType: "Primary"]],
                      dateOfBirth: bio.birthDate.format('yyyy-MM-dd')
        ]
        def matched_persons = personCompositeService.list(params)
        // assert that only one match comes back
        assertEquals 1, matched_persons.size()
        assertEquals matched_persons[0].pidm, person.pidm
    }


    @Test
    void testCMSearchWithDifferentBirthDate() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.v3+json")
        request.addHeader("Accept", "application/vnd.hedtech.integration.v3+json")

        IntegrationConfiguration personMatchRule = IntegrationConfiguration.findByProcessCodeAndSettingName(personCompositeService.PROCESS_CODE, personCompositeService.PERSON_MATCH_RULE)
        assertNotNull personMatchRule?.value
        def sourceCode = CommonMatchingSource.findByCode(personMatchRule?.value)
        assertNotNull sourceCode

        def sources = CommonMatchingSourceRule.findAllByCommonMatchingSource(sourceCode)
        assertTrue sources.size() > 0
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def rules = sql.rows("select gorcmsr_column_name from gorcmsr where  GORCMSr_CMSC_CODE = ?", [sourceCode.code])
        assertTrue rules.size() > 0

        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPBPERS_BIRTH_DATE" }
        def person = PersonUtility.getPerson('HOSFE2020')
        assertNotNull person
        def bio = PersonBasicPersonBase.findByPidm(person.pidm)
        assertNotNull bio
        assertNotNull bio.birthDate
        assertEquals '03/17/1986', bio.birthDate.format('MM/dd/yyyy')

        // build content for common matching
        Map params = [action     : [POST: "list"],
                      names      : [[lastName: person.lastName, firstName: person.firstName, nameType: "Primary"]],
                      dateOfBirth: '1986-04-16'
        ]
        def matched_persons = personCompositeService.list(params)
        // assert that no matches comes back
        assertEquals 0, matched_persons.size()
    }


    @Test
    void testCMSearchWithSsnV3() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.v3+json")
        request.addHeader("Accept", "application/vnd.hedtech.integration.v3+json")

        IntegrationConfiguration personMatchRule = IntegrationConfiguration.findByProcessCodeAndSettingName(personCompositeService.PROCESS_CODE, personCompositeService.PERSON_MATCH_RULE)
        assertNotNull personMatchRule?.value
        def sourceCode = CommonMatchingSource.findByCode(personMatchRule?.value)
        assertNotNull sourceCode

        def sources = CommonMatchingSourceRule.findAllByCommonMatchingSource(sourceCode)
        assertTrue sources.size() > 0
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def rules = sql.rows("select gorcmsr_column_name from gorcmsr where  GORCMSr_CMSC_CODE = ?", [sourceCode.code])
        assertTrue rules.size() > 0

        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPBPERS_SSN" }

        def person = PersonUtility.getPerson('HOSFE2020')
        assertNotNull person
        def bio = PersonBasicPersonBase.findByPidm(person.pidm)
        assertNotNull bio
        assertNotNull bio.ssn
        assertEquals '000008899', bio.ssn

        // build content for common matching
        Map params = [action     : [POST: "list"],
                      names      : [[lastName: person.lastName, firstName: person.firstName, nameType: "Primary"]],
                      credentials: [[credentialType: "Social Security Number", credentialId: bio.ssn]]
        ]
        def matched_persons = personCompositeService.list(params)
        // assert that only one match comes back
        assertEquals 1, matched_persons.size()
        assertEquals matched_persons[0].pidm, person.pidm
    }


    @Test
    void testCMSearchWithDifferentSsn() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.v3+json")
        request.addHeader("Accept", "application/vnd.hedtech.integration.v3+json")

        IntegrationConfiguration personMatchRule = IntegrationConfiguration.findByProcessCodeAndSettingName(personCompositeService.PROCESS_CODE, personCompositeService.PERSON_MATCH_RULE)
        assertNotNull personMatchRule?.value
        def sourceCode = CommonMatchingSource.findByCode(personMatchRule?.value)
        assertNotNull sourceCode

        def sources = CommonMatchingSourceRule.findAllByCommonMatchingSource(sourceCode)
        assertTrue sources.size() > 0
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def rules = sql.rows("select gorcmsr_column_name from gorcmsr where  GORCMSr_CMSC_CODE = ?", [sourceCode.code])
        assertTrue rules.size() > 0

        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPBPERS_SSN" }

        def person = PersonUtility.getPerson('HOSFE2020')
        assertNotNull person
        def bio = PersonBasicPersonBase.findByPidm(person.pidm)
        assertNotNull bio
        assertNotNull bio.ssn
        assertEquals '000008899', bio.ssn

        // build content for common matching
        Map params = [action     : [POST: "list"],
                      names      : [[lastName: person.lastName, firstName: person.firstName, nameType: "Primary"]],
                      credentials: [[credentialType: "Social Security Number", credentialId: "000333444"]]
        ]
        def matched_persons = personCompositeService.list(params)
        // assert that no match comes back
        assertEquals 0, matched_persons.size()
    }


    @Test
    void testCMSearchWithGenderV3() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.v3+json")
        request.addHeader("Accept", "application/vnd.hedtech.integration.v3+json")

        IntegrationConfiguration personMatchRule = IntegrationConfiguration.findByProcessCodeAndSettingName(personCompositeService.PROCESS_CODE, personCompositeService.PERSON_MATCH_RULE)
        assertNotNull personMatchRule?.value
        def sourceCode = CommonMatchingSource.findByCode(personMatchRule?.value)
        assertNotNull sourceCode

        def sources = CommonMatchingSourceRule.findAllByCommonMatchingSource(sourceCode)
        assertTrue sources.size() > 0
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def rules = sql.rows("select gorcmsr_column_name from gorcmsr where  GORCMSr_CMSC_CODE = ?", [sourceCode.code])
        assertTrue rules.size() > 0

        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPBPERS_SSN" }

        def person = PersonUtility.getPerson('HOSFE2020')
        assertNotNull person
        def bio = PersonBasicPersonBase.findByPidm(person.pidm)
        assertNotNull bio
        assertEquals "F", bio.sex
        def gender = "Female"

        // build content for common matching
        Map params = [action: [POST: "list"],
                      names : [[lastName: person.lastName, firstName: person.firstName, nameType: "Primary"]],
                      gender: gender
        ]
        def matched_persons = personCompositeService.list(params)
        // assert that only one match comes back
        assertEquals 1, matched_persons.size()
        assertEquals matched_persons[0].pidm, person.pidm
    }


    @Test
    void testCMSearchWithDifferentGender() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.v3+json")
        request.addHeader("Accept", "application/vnd.hedtech.integration.v3+json")

        IntegrationConfiguration personMatchRule = IntegrationConfiguration.findByProcessCodeAndSettingName(personCompositeService.PROCESS_CODE, personCompositeService.PERSON_MATCH_RULE)
        assertNotNull personMatchRule?.value
        def sourceCode = CommonMatchingSource.findByCode(personMatchRule?.value)
        assertNotNull sourceCode

        def sources = CommonMatchingSourceRule.findAllByCommonMatchingSource(sourceCode)
        assertTrue sources.size() > 0
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def rules = sql.rows("select gorcmsr_column_name from gorcmsr where  GORCMSr_CMSC_CODE = ?", [sourceCode.code])
        assertTrue rules.size() > 0

        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPBPERS_SSN" }

        def person = PersonUtility.getPerson('HOSFE2020')
        assertNotNull person
        def bio = PersonBasicPersonBase.findByPidm(person.pidm)
        assertNotNull bio
        assertEquals "F", bio.sex

        // build content for common matching
        Map params = [action: [POST: "list"],
                      names : [[lastName: person.lastName, firstName: person.firstName, nameType: "Primary"]],
                      gender: "Male"
        ]
        def matched_persons = personCompositeService.list(params)
        // assert that no match comes back
        assertEquals 0, matched_persons.size()
    }


    @Test
    void testCMSearchWithBannerIdV3() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.v3+json")
        request.addHeader("Accept", "application/vnd.hedtech.integration.v3+json")

        IntegrationConfiguration personMatchRule = IntegrationConfiguration.findByProcessCodeAndSettingName(personCompositeService.PROCESS_CODE, personCompositeService.PERSON_MATCH_RULE)
        assertNotNull personMatchRule?.value
        def sourceCode = CommonMatchingSource.findByCode(personMatchRule?.value)
        assertNotNull sourceCode

        def sources = CommonMatchingSourceRule.findAllByCommonMatchingSource(sourceCode)
        assertTrue sources.size() > 0
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def rules = sql.rows("select gorcmsr_column_name from gorcmsr where  GORCMSr_CMSC_CODE = ?", [sourceCode.code])
        assertTrue rules.size() > 0

        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPRIDEN_ID" }

        def person = PersonUtility.getPerson('HOSFE2020')
        assertNotNull person

        // build content for common matching
        Map params = [action     : [POST: "list"],
                      names      : [[lastName: person.lastName, firstName: person.firstName, nameType: "Primary"]],
                      credentials: [[credentialType: "Banner ID", credentialId: person.bannerId]]
        ]
        def matched_persons = personCompositeService.list(params)
        // assert that only one match comes back
        assertEquals 1, matched_persons.size()
        assertEquals matched_persons[0].pidm, person.pidm
    }


    @Test
    void testCMSearchWithDifferentBannerId() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.v3+json")
        request.addHeader("Accept", "application/vnd.hedtech.integration.v3+json")

        IntegrationConfiguration personMatchRule = IntegrationConfiguration.findByProcessCodeAndSettingName(personCompositeService.PROCESS_CODE, personCompositeService.PERSON_MATCH_RULE)
        assertNotNull personMatchRule?.value
        def sourceCode = CommonMatchingSource.findByCode(personMatchRule?.value)
        assertNotNull sourceCode

        def sources = CommonMatchingSourceRule.findAllByCommonMatchingSource(sourceCode)
        assertTrue sources.size() > 0
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def rules = sql.rows("select gorcmsr_column_name from gorcmsr where  GORCMSr_CMSC_CODE = ?", [sourceCode.code])
        assertTrue rules.size() > 0

        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPRIDEN_ID" }

        def person = PersonUtility.getPerson('HOSFE2020')
        assertNotNull person

        // build content for common matching
        Map params = [action     : [POST: "list"],
                      names      : [[lastName: person.lastName, firstName: person.firstName, nameType: "Primary"]],
                      credentials: [[credentialType: "Banner ID", credentialId: "000333444"]]
        ]
        def matched_persons = personCompositeService.list(params)
        // assert that no match comes back
        assertEquals 0, matched_persons.size()
    }


    @Test
    void testCMSearchWithNameV3() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.v3+json")
        request.addHeader("Accept", "application/vnd.hedtech.integration.v3+json")

        IntegrationConfiguration personMatchRule = IntegrationConfiguration.findByProcessCodeAndSettingName(personCompositeService.PROCESS_CODE, personCompositeService.PERSON_MATCH_RULE)
        assertNotNull personMatchRule?.value
        personMatchRule.value = 'HEDM_LASTNAME_MATCH'
        personMatchRule.save(flush: true, failOnError: true)

        def sourceCode = CommonMatchingSource.findByCode(personMatchRule?.value)
        assertNotNull sourceCode

        def sources = CommonMatchingSourceRule.findAllByCommonMatchingSource(sourceCode)
        assertTrue sources.size() > 0
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def rules = sql.rows("select gorcmsr_column_name from gorcmsr where  GORCMSr_CMSC_CODE = ?", [sourceCode.code])
        assertTrue rules.size() > 0

        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPRIDEN_SEARCH_LAST_NAME" }
        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPRIDEN_LAST_NAME" }

        // build content for common matching
        Map params = [action: [POST: "list"],
                      names : [[lastName: "Jamison", firstName: "Emily", nameType: "Primary"]]
        ]
        def matched_persons = personCompositeService.list(params)
        // assert that only one match comes back
        assertTrue matched_persons.size() > 0
    }


    private void setAcceptHeader(String mediaType) {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Accept", mediaType)
    }


    @Test
    void testListapiWithRoleFacultyAndPaginationV3() {
        setAcceptHeader("application/vnd.hedtech.integration.v3+json")

        def params = [role: "Faculty", max: '10', offset: '5']

        def persons = personCompositeService.list(params)
        persons.each {
            it.roles.role == "Faculty"
        }
    }


    @Test
    void testListapiWithRoleStudentAndLargePagination() {
        setAcceptHeader("application/vnd.hedtech.integration.v3+json")

        def params1 = [role: "student"]
        Map resultCount = userRoleCompositeService.fetchAllByRole(params1)
        assertTrue resultCount.count > 500

        def params = [role: "Student", max: '2000', offset: '100']
        def persons = personCompositeService.list(params)
        // verify pagination capped at 500
        assertEquals 500, persons.size()
        persons.each {
            it.roles.role == "Student"
        }
    }


    @Test
    void testListapiWithRoleStudentAndPagination() {
        setAcceptHeader("application/vnd.hedtech.integration.v3+json")

        def params = [role: "Student", max: '10', offset: '5']

        def persons = personCompositeService.list(params)
        persons.each {
            it.roles.role == "Student"
        }
    }


    @Test
    void testListapiWithInvalidPersonfilter() {
        setAcceptHeader("application/vnd.hedtech.integration.v3+json")

        def params = [:]
        params.put("personFilter", "xxxx")

        try {
            personCompositeService.list(params)
            fail('This should have failed as person filter GUID is invalid')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'not.found.message'
        }
    }


    @Test
    void testGetapiWithInvalidGuid() {
        setAcceptHeader("application/vnd.hedtech.integration.v3+json")

        def invalidGuid = 'xxxxxx'

        try {
            personCompositeService.get(invalidGuid)
            fail('This should have failed as person filter GUID is invalid')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'NotFoundException'
        }
    }


    @Test
    void testListapiWithPersonfilterNull() {
        setAcceptHeader("application/vnd.hedtech.integration.v3+json")

        def params = [:]
        params.put("personFilter", "")

        try {
            personCompositeService.list(params)
            fail('This should have failed as person filter GUID is null')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'not.found.message'
        }
    }


    @Test
    void testListapiWithPersonfilterAndRole() {
        setAcceptHeader("application/vnd.hedtech.integration.v3+json")

        def params = [:]
        params.put("personFilter", "xxxx")
        params.put("role", "Faculty")

        try {
            personCompositeService.list(params)
            fail('This should have failed as person filter and role both present')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'UnsupportedFilterCombination'
        }
    }


    @Test
    void testListapiWithValidPersonfilterV3() {
        setAcceptHeader("application/vnd.hedtech.integration.v3+json")

        def popsel = PopulationSelectionExtractReadonly.fetchAllPidmsByApplicationSelectionCreatorIdLastModifiedBy("GENERAL", 'ALL', 'BANNER', 'GRAILS')
        assertEquals 2, popsel.size()
        def pidm1 = popsel[0].pidm
        def pidm2 = popsel[1].pidm

        def persons
        String guid = GlobalUniqueIdentifier.findByLdmNameAndDomainKey('person-filters', 'GENERAL-^ALL-^BANNER-^GRAILS')?.guid
        def params = [:]

        params.put("personFilter", guid)

        persons = personCompositeService.list(params)
        assertNotNull persons
        assertNotNull persons.find { it.person.pidm == pidm1 }
        assertNotNull persons.find { it.person.pidm == pidm2 }
    }


    @Test
    void testListapiWithValidPersonfilterAndPagination() {
        setAcceptHeader("application/vnd.hedtech.integration.v3+json")

        // verify our test case has 7 records
        def popsel = PopulationSelectionExtract.findAllByApplicationAndSelection("STUDENT", 'HEDM')
        assertEquals 7, popsel.size()
        assertEquals 7, popsel.findAll { it.creatorId == "BANNER" }?.size()
        assertEquals 7, popsel.findAll { it.lastModifiedBy == "GRAILS" }?.size()

        // set up params for call
        def persons = []
        String guid = GlobalUniqueIdentifier.findByLdmNameAndDomainKey('person-filters', 'STUDENT-^HEDM-^BANNER-^GRAILS')?.guid
        assertNotNull guid
        String guid2 = GlobalUniqueIdentifier.fetchByLdmNameAndDomainKey('person-filters', 'STUDENT-^HEDM-^BANNER-^GRAILS')[0]
        assertNotNull guid2
        def params = [personFilter: 'STUDENT-^HEDM-^BANNER-^GRAILS', max: '3', offset: '0']

        persons = personCompositeService.list(params)
        assertEquals 3, persons.size()

        // no pagination
        def params2 = [personFilter: 'STUDENT-^HEDM-^BANNER-^GRAILS']

        persons = personCompositeService.list(params2)
        assertEquals 7, persons.size()

    }


    @Test
    void testListapiWithValidPersonfilterAsGuidAndPagination() {
        setAcceptHeader("application/vnd.hedtech.integration.v3+json")

        // verify our test case has 7 records
        def popsel = PopulationSelectionExtract.findAllByApplicationAndSelection("STUDENT", 'HEDM')
        assertEquals 7, popsel.size()
        assertEquals 7, popsel.findAll { it.creatorId == "BANNER" }?.size()
        assertEquals 7, popsel.findAll { it.lastModifiedBy == "GRAILS" }?.size()

        // set up params for call
        def persons = []

        String guid2 = GlobalUniqueIdentifier.fetchByLdmNameAndDomainKey('person-filters', 'STUDENT-^HEDM-^BANNER-^GRAILS')[0].guid
        assertNotNull guid2
        def params = [personFilter: guid2, max: '3', offset: '0']

        persons = personCompositeService.list(params)
        assertEquals 3, persons.size()

        // no pagination
        def params2 = [personFilter: guid2]

        persons = personCompositeService.list(params2)
        assertEquals 7, persons.size()
    }


    @Test
    void testListapiWithValidPersonfilterAsGuidAndPaginationUniqueListsV3() {
        setAcceptHeader("application/vnd.hedtech.integration.v3+json")

        // remove if pop sel exists
        def popsel = PopulationSelectionExtract.findAllByApplicationAndSelection("STUDENT", 'HEDMPERFORM')
        if (popsel.size() > 0) {
            popsel.each {
                it.delete(flush: true, failOnError: true)
            }
        }
        // create big list
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def insertCount
        try {
            String idSql = """INSERT INTO GLBEXTR
                  (glbextr_key,
                   glbextr_application,
                   glbextr_selection,
                   glbextr_creator_id,
                   glbextr_user_id,
                   glbextr_sys_ind,
                   glbextr_activity_date)
                select
                   to_char(spriden_pidm),
                   'STUDENT',
                   'HEDMPERFORM',
                   'BANNER',
                   'GRAILS',
                   'S',
                   SYSDATE
                from spriden
                where spriden_CHANGE_ind is null
                and exists ( select 1 from spraddr where spraddr_pidm = spriden_pidm)
                and not exists ( select 'x' from glbextr old
                  where old.glbextr_key = to_char(spriden_pidm)
                  and old.glbextr_application = 'STUDENT'
                  and old.glbextr_selection = 'HEDMPERFORM') """
            insertCount = sql.executeUpdate(idSql)
        }
        finally {
            sql?.close()
        }
        assertTrue insertCount > 500
        def persextract = PopulationSelectionExtractReadonly.fetchAllPidmsByApplicationSelectionCreatorIdLastModifiedBy("STUDENT", "HEDMPERFORM", "BANNER", "GRAILS")
        assertTrue insertCount >= persextract.size()
        def pidmlist = persextract.groupBy { it.pidm }
        // verify the list of pidms is unique
        pidmlist.each {
            assertEquals 1, it.value.size()
        }
        // test pagination
        def persextract250 = PopulationSelectionExtractReadonly.fetchAllPidmsByApplicationSelectionCreatorIdLastModifiedBy("STUDENT", "HEDMPERFORM", "BANNER", "GRAILS", [max: '250', offset: '0'])
        def persextract2502 = PopulationSelectionExtractReadonly.fetchAllPidmsByApplicationSelectionCreatorIdLastModifiedBy("STUDENT", "HEDMPERFORM", "BANNER", "GRAILS", [max: '250', offset: '250'])
        assertEquals 250, persextract250.size()
        assertEquals 250, persextract2502.size()
        def matchextract = 0
        def cnt = 0
        // make sure lists have unique pidms
        persextract250.each { pers ->
            persextract2502.find { pers2 ->
                cnt += 1
                if (pers2.pidm == pers.pidm) {
                    matchextract += 1
                }
            }
        }
        assertEquals 0, matchextract
        matchextract = 0
        persextract2502.each { pers ->
            persextract250.find { pers2 ->
                cnt += 1
                if (pers2.pidm == pers.pidm) {
                    matchextract += 1
                }
            }
        }
        assertEquals 0, matchextract

        // set up params for call
        def persons = []

        String guid2 = GlobalUniqueIdentifier.fetchByLdmNameAndDomainKey('person-filters', 'STUDENT-^HEDMPERFORM-^BANNER-^GRAILS')[0].guid
        assertNotNull guid2

        // get first page
        def params = [personFilter: guid2, max: '250', offset: '0']
        persons = personCompositeService.list(params)
        assertEquals 250, persons.size()

        // get second page
        params = [personFilter: guid2, max: '250', offset: '250']
        def persons2 = personCompositeService.list(params)
        assertEquals 250, persons2.size()

        // make sure persons in list 1 are not in list 2
        matchextract = 0
        persons2.each { pers2 ->
            persons.find { pers ->
                cnt += 1
                if (pers2.names[0].personName.pidm == pers.names[0].personName.pidm) {
                    matchextract += 1
                }
            }
        }
        assertEquals 0, matchextract

    }

    @Test
    void testListapiWithValidPersonfilterAsGuidLargeListsNoPagination() {
        setAcceptHeader("application/vnd.hedtech.integration.v3+json")

        // remove if pop sel exists
        def popsel = PopulationSelectionExtract.findAllByApplicationAndSelection("STUDENT", 'HEDMPERFORM')
        if (popsel.size() > 0) {
            popsel.each {
                it.delete(flush: true, failOnError: true)
            }
        }
        // create big list
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def insertCount
        try {
            String idSql = """INSERT INTO GLBEXTR
                  (glbextr_key,
                   glbextr_application,
                   glbextr_selection,
                   glbextr_creator_id,
                   glbextr_user_id,
                   glbextr_sys_ind,
                   glbextr_activity_date)
                select
                   to_char(spriden_pidm),
                   'STUDENT',
                   'HEDMPERFORM',
                   'BANNER',
                   'GRAILS',
                   'S',
                   SYSDATE
                from spriden
                where spriden_CHANGE_ind is null
                and not exists ( select 'x' from glbextr old
                  where old.glbextr_key = to_char(spriden_pidm)
                  and old.glbextr_application = 'STUDENT'
                  and old.glbextr_selection = 'HEDMPERFORM') """
            insertCount = sql.executeUpdate(idSql)
        }
        finally {
            sql?.close()
        }
        assertTrue insertCount > 500
        def persextract = PopulationSelectionExtractReadonly.fetchAllPidmsByApplicationSelectionCreatorIdLastModifiedBy("STUDENT", "HEDMPERFORM", "BANNER", "GRAILS")
        assertTrue persextract.size() > 500

        // set up params for call
        def persons = []
        String guid2 = GlobalUniqueIdentifier.fetchByLdmNameAndDomainKey('person-filters', 'STUDENT-^HEDMPERFORM-^BANNER-^GRAILS')[0].guid
        assertNotNull guid2
        // get first page
        def params = [personFilter: guid2]
        persons = personCompositeService.list(params)
        assertEquals 500, persons.size()

    }


    @Test
    void testListapiWithValidPersonfilterAsGuidAndLargePaginationAndDetailedPerson() {
        setAcceptHeader("application/vnd.hedtech.integration.v3+json")

        // find our person hofse2000 who has all data
        def perId = PersonUtility.getPerson("HOSFE2000")
        assertNotNull perId
        def perbio = PersonBasicPersonBase.findByPidm(perId.pidm)
        assertNotNull perbio
        def perAddr = PersonAddress.findAllByPidm(perId.pidm)
        assertEquals 1, perAddr.size()
        assertNotNull perAddr[0]
        assertEquals "MA", perAddr[0].addressType.code
        def personTelephones = PersonTelephone.findAllByPidm(perId.pidm)
        assertEquals 2, personTelephones.size()
        def personTelephone = personTelephones.find {it.telephoneType.code = "CELL"}
        assertNotNull personTelephone
        assertEquals "CELL", personTelephone.telephoneType.code
        def perEmail = PersonEmail.findAllByPidm(perId.pidm)
        assertEquals 1, perEmail.size()
        assertNotNull perEmail[0]
        assertEquals "HOME", perEmail[0].emailType.code
        def perRace = PersonRace.findByPidm(perId.pidm)
        assertNotNull perRace
        assertEquals "WHT", perRace.race

        // set up our pop sel
        def popsel = PopulationSelectionExtract.findAllByApplicationAndSelection("STUDENT", 'HEDMPERFORM')
        if (popsel.size() > 0) {
            popsel.each {
                it.delete(flush: true, failOnError: true)
            }
        }
        // create big list
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def insertCount
        try {
            String idSql = """INSERT INTO GLBEXTR
                  (glbextr_key,
                   glbextr_application,
                   glbextr_selection,
                   glbextr_creator_id,
                   glbextr_user_id,
                   glbextr_sys_ind,
                   glbextr_activity_date)
                select
                   to_char(spriden_pidm),
                   'STUDENT',
                   'HEDMPERFORM',
                   'BANNER',
                   'GRAILS',
                   'S',
                   SYSDATE
                from spriden
                where spriden_CHANGE_ind is null
                and exists ( select 1 from spraddr where spraddr_pidm = spriden_pidm)
                and not exists ( select 'x' from glbextr old
                  where old.glbextr_key = to_char(spriden_pidm)
                  and old.glbextr_application = 'STUDENT'
                  and old.glbextr_selection = 'HEDMPERFORM') """
            insertCount = sql.executeUpdate(idSql)

        }
        finally {
            sql?.close()
        }
        assertTrue insertCount > 500
        def persextract = PopulationSelectionExtractReadonly.fetchAllPidmsByApplicationSelectionCreatorIdLastModifiedBy("STUDENT", "HEDMPERFORM", "BANNER", "GRAILS")

        assertTrue insertCount >= persextract.size()

        // find our test person HOSFE2000
        def cnt = 0
        def found = 0
        persextract.each { per ->
            cnt += 1
            if (per.bannerId == "HOSFE2000") {
                found = cnt
            }
        }
        assertTrue found > 300
        def offset = found - 300
        // set up params for call
        def persons = []

        String guid2 = GlobalUniqueIdentifier.fetchByLdmNameAndDomainKey('person-filters', 'STUDENT-^HEDMPERFORM-^BANNER-^GRAILS')[0].guid
        assertNotNull guid2

        def params = [personFilter: guid2, max: '2000', offset: offset.toString()]
        log.debug "turn logging on "

        persons = personCompositeService.list(params)
        assertEquals 500, persons.size()
    }


    @Test
    void testListapiWithValidPersonfilterAsGuidAndSinglePersonAndDetailedPersonV3() {
        setAcceptHeader("application/vnd.hedtech.integration.v3+json")

        // find our person hofse2000 who has all data
        def perId = PersonUtility.getPerson("HOSFE2000")
        assertNotNull perId
        def perbio = PersonBasicPersonBase.findByPidm(perId.pidm)
        assertNotNull perbio
        def perAddr = PersonAddress.findAllByPidm(perId.pidm)
        assertEquals 1, perAddr.size()
        assertNotNull perAddr[0]
        assertEquals "MA", perAddr[0].addressType.code
        def personTelephones = PersonTelephone.findAllByPidm(perId.pidm)
        assertEquals 2, personTelephones.size()
        def personTelephone = personTelephones.find {it.telephoneType.code = "CELL"}
        assertNotNull personTelephone
        assertEquals "CELL", personTelephone.telephoneType.code
        def perEmail = PersonEmail.findAllByPidm(perId.pidm)
        assertEquals 1, perEmail.size()
        assertNotNull perEmail[0]
        assertEquals "HOME", perEmail[0].emailType.code
        def perRace = PersonRace.findByPidm(perId.pidm)
        assertNotNull perRace
        assertEquals "WHT", perRace.race
        // set up our pop sel
        def popsel = PopulationSelectionExtract.findAllByApplicationAndSelection("STUDENT", 'HEDMPERFORM')
        if (popsel.size() > 0) {
            popsel.each {
                it.delete(flush: true, failOnError: true)
            }
        }
        // create big list
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def insertCount
        try {
            String idSql = """INSERT INTO GLBEXTR
                  (glbextr_key,
                   glbextr_application,
                   glbextr_selection,
                   glbextr_creator_id,
                   glbextr_user_id,
                   glbextr_sys_ind,
                   glbextr_activity_date)
                select
                   to_char(spriden_pidm),
                   'STUDENT',
                   'HEDMPERFORM',
                   'BANNER',
                   'GRAILS',
                   'S',
                   SYSDATE
                from spriden
                where spriden_CHANGE_ind is null
                and spriden_id = 'HOSFE2000'
                and not exists ( select 'x' from glbextr old
                  where old.glbextr_key = to_char(spriden_pidm)
                  and old.glbextr_application = 'STUDENT'
                  and old.glbextr_selection = 'HEDMPERFORM') """
            insertCount = sql.executeUpdate(idSql)

        }
        finally {
            sql?.close()
        }
        assertEquals 1, insertCount
        def persextract = PopulationSelectionExtractReadonly.fetchAllPidmsByApplicationSelectionCreatorIdLastModifiedBy("STUDENT", "HEDMPERFORM", "BANNER", "GRAILS")
        assertEquals insertCount, persextract.size()

        // find our test person HOSFE2000
        assertEquals perId.pidm, persextract[0].pidm
        // set up params for call
        def persons = []

        String guid2 = GlobalUniqueIdentifier.fetchByLdmNameAndDomainKey('person-filters', 'STUDENT-^HEDMPERFORM-^BANNER-^GRAILS')[0].guid
        assertNotNull guid2

        def params = [personFilter: guid2, max: '2000', offset: '0']
        log.debug "turn logging on "

        persons = personCompositeService.list(params)
        assertEquals 1, persons.size()

        def testPerson = persons.find { it.names[0].personName.bannerId == "HOSFE2000" }
        assertNotNull testPerson
        assertEquals testPerson.person.id, perbio.id
        assertEquals "MA", testPerson.addresses[0].address.addressType.code
        assertEquals perAddr[0].id, testPerson.addresses[0].address.id
        def phone = testPerson.phones.find {it.phone.telephoneType.code = "CELL"}?.phone
        assertNotNull phone
        assertEquals "CELL", phone.telephoneType.code
        assertEquals personTelephone.id, phone.id
        assertEquals "HOME", testPerson.emails[0].email.emailType.code
        assertEquals perEmail[0].id, testPerson.emails[0].email.id
        assertEquals perRace.race, testPerson.races[0].raceDecorator.race
        def facultyRole = testPerson.roles.find { it.role.equalsIgnoreCase("faculty") }
        assertNotNull facultyRole
        def studentRole = testPerson.roles.find { it.role.equalsIgnoreCase("student") }
        assertNotNull studentRole
    }

    //GET- Person by guid API
    @Test
    void testGetPersonCredentialsByGuidV3() {
        setAcceptHeader("application/vnd.hedtech.integration.v3+json")

        def person = PersonUtility.getPerson("HOSP0001")
        assertNotNull person
        String guid = GlobalUniqueIdentifier.findByLdmNameAndDomainKey('persons', person.pidm.toString())?.guid

        def persons = personCompositeService.get(guid)

        assertNotNull persons
        assertNotNull persons.credentials
        assertEquals 4, persons.credentials.size()
        assertEquals i_success_credential_id1, persons.credentials[0].credentialId
        assertEquals i_success_credential_type1, persons.credentials[0].credentialType
        assertEquals i_success_credential_id2, persons.credentials[1].credentialId
        assertEquals i_success_credential_type2, persons.credentials[1].credentialType
        assertEquals i_success_credential_id3, persons.credentials[2].credentialId
        assertEquals i_success_credential_type3, persons.credentials[2].credentialType
        assertEquals i_success_credential_id4, persons.credentials[3].credentialId
        assertEquals i_success_credential_type4, persons.credentials[3].credentialType
    }

    //GET Person By Guid API
    @Test
    void testGetPersonWithAlternateNameHavingBirthNameTypeV3() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.v3+json")
        request.addHeader("Accept", "application/vnd.hedtech.integration.v3+json")

        Map content = newPersonWithAlternateNameHavingBirthNameType()

        def o_success_person_create = personCompositeService.create(content)

        assertNotNull o_success_person_create
        assertNotNull o_success_person_create.guid
        assertEquals 2, o_success_person_create.names?.size()

        def o_primary_name_create = o_success_person_create.names.find { it.nameType == "Primary" }
        def o_birth_name_create = o_success_person_create.names.find { it.nameType == "Birth" }

        assertNotNull o_primary_name_create
        assertEquals i_success_first_name, o_primary_name_create.firstName
        assertEquals i_success_middle_name, o_primary_name_create.middleName
        assertEquals i_success_last_name, o_primary_name_create.lastName
        assertEquals i_success_primary_name_type, o_primary_name_create.nameType
        assertEquals i_success_namePrefix, o_primary_name_create.title
        assertEquals i_success_nameSuffix, o_primary_name_create.pedigree
        assertEquals i_success_preferenceFirstName, o_primary_name_create.preferredName
        assertNotNull o_birth_name_create
        assertEquals i_success_alternate_first_name, o_birth_name_create.firstName
        assertEquals i_success_alternate_middle_name, o_birth_name_create.middleName
        assertEquals i_success_alternate_last_name, o_birth_name_create.lastName
        assertEquals i_success_alternate_birth_name_type, o_birth_name_create.nameType

        def o_success_person_get = personCompositeService.get(o_success_person_create.guid)

        assertNotNull o_success_person_get
        assertNotNull o_success_person_get.guid
        assertEquals 2, o_success_person_get.names?.size()

        def o_primary_name_get = o_success_person_get.names.find { it.nameType == "Primary" }
        def o_birth_name_get = o_success_person_get.names.find { it.nameType == "Birth" }

        assertNotNull o_primary_name_get
        assertEquals o_primary_name_create.firstName, o_primary_name_get.firstName
        assertEquals o_primary_name_create.middleName, o_primary_name_get.middleName
        assertEquals o_primary_name_create.lastName, o_primary_name_get.lastName
        assertEquals o_primary_name_create.nameType, o_primary_name_get.nameType
        assertEquals o_primary_name_create.title, o_primary_name_get.title
        assertEquals o_primary_name_create.pedigree, o_primary_name_get.pedigree
        assertEquals o_primary_name_create.preferredName, o_primary_name_get.preferredName
        assertNotNull o_birth_name_get
        assertEquals o_birth_name_create.firstName, o_birth_name_get.firstName
        assertEquals o_birth_name_create.middleName, o_birth_name_get.middleName
        assertEquals o_birth_name_create.lastName, o_birth_name_get.lastName
        assertEquals o_birth_name_create.nameType, o_birth_name_get.nameType
    }

    //GET Person By Guid API
    @Test
    void testGetPersonWithUSEthnicityV3() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.v3+json")
        request.addHeader("Accept", "application/vnd.hedtech.integration.v3+json")

        Map content = newPersonWithUSEthnicity()

        def o_success_person_create = personCompositeService.create(content)

        assertNotNull o_success_person_create
        assertNotNull o_success_person_create.guid



        def o_success_person_get = personCompositeService.get(o_success_person_create.guid)

        assertNotNull o_success_person_get
        assertNotNull o_success_person_get.guid
        assertEquals o_success_person_create.guid, o_success_person_get.guid
        assertEquals o_success_person_create.ethnicityDetail.guid, o_success_person_get.ethnicityDetail.guid
    }

    //GET Person By Guid API
    @Test
    void testGetPersonWithInvalidPhoneNumber() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.v3+json")
        request.addHeader("Accept", "application/vnd.hedtech.integration.v3+json")

        Map content = newPersonRequestWithInvalidShortPhoneNumber()
        def o_success_person_create = personCompositeService.create(content)

        assertNotNull o_success_person_create
        assertNotNull o_success_person_create.guid

        def o_success_person_get = personCompositeService.get(o_success_person_create.guid)

        assertNotNull o_success_person_get
        assertNotNull o_success_person_get.guid
        assertEquals o_success_person_create.guid, o_success_person_get.guid

        def o_phone_type_mobile_create = o_success_person_create.phones.find { it.phoneType == "Mobile" }
        assertNotNull o_phone_type_mobile_create
        def o_phone_type_mobile_get = o_success_person_get.phones.find { it.phoneType == "Mobile" }
        assertNotNull o_phone_type_mobile_get
        assertEquals o_phone_type_mobile_create.phoneType, o_phone_type_mobile_get.phoneType
        assertEquals o_phone_type_mobile_create.phoneExtension, o_phone_type_mobile_get.phoneExtension
        assertEquals o_phone_type_mobile_create.phoneNumberDetail, o_phone_type_mobile_get.phoneNumberDetail
        String phoneNumberMobileGet = (o_phone_type_mobile_get.countryPhone ?: "") + (o_phone_type_mobile_get.phoneArea ?: "") + (o_phone_type_mobile_get.phoneNumber ?: "")
        assertEquals phoneNumberMobileGet, o_phone_type_mobile_get.phoneNumberDetail

        def o_phone_type_home_create = o_success_person_create.phones.find { it.phoneType == "Home" }
        assertNotNull o_phone_type_home_create
        def o_phone_type_home_get = o_success_person_get.phones.find { it.phoneType == "Home" }
        assertNotNull o_phone_type_home_get
        assertEquals o_phone_type_home_create.phoneType, o_phone_type_home_get.phoneType
        assertEquals o_phone_type_home_create.phoneExtension, o_phone_type_home_get.phoneExtension
        assertEquals o_phone_type_home_create.phoneNumberDetail, o_phone_type_home_get.phoneNumberDetail
        String phoneNumberHomeGet = (o_phone_type_home_get.countryPhone ?: "") + (o_phone_type_home_get.phoneArea ?: "") + (o_phone_type_home_get.phoneNumber ?: "")
        assertEquals phoneNumberHomeGet, o_phone_type_home_get.phoneNumberDetail

        def o_phone_type_work_create = o_success_person_create.phones.find { it.phoneType == "Work" }
        assertNotNull o_phone_type_work_create
        def o_phone_type_work_get = o_success_person_get.phones.find { it.phoneType == "Work" }
        assertNotNull o_phone_type_work_get
        assertEquals o_phone_type_work_create.phoneType, o_phone_type_work_get.phoneType
        assertEquals o_phone_type_work_create.phoneExtension, o_phone_type_work_get.phoneExtension
        assertEquals o_phone_type_work_create.phoneNumberDetail, o_phone_type_work_get.phoneNumberDetail
        String phoneNumberWorkGet = (o_phone_type_work_get.countryPhone ?: "") + (o_phone_type_work_get.phoneArea ?: "") + (o_phone_type_work_get.phoneNumber ?: "")
        assertEquals phoneNumberWorkGet, o_phone_type_work_get.phoneNumberDetail

        def o_phone_type_residence_create = o_success_person_create.phones.find { it.phoneType == "Residence" }
        assertNotNull o_phone_type_residence_create
        def o_phone_type_residence_get = o_success_person_get.phones.find { it.phoneType == "Residence" }
        assertNotNull o_phone_type_residence_get
        assertEquals o_phone_type_residence_create.phoneType, o_phone_type_residence_get.phoneType
        assertEquals o_phone_type_residence_create.phoneExtension, o_phone_type_residence_get.phoneExtension
        assertEquals o_phone_type_residence_create.phoneNumberDetail, o_phone_type_residence_get.phoneNumberDetail
        String phoneNumberResidenceGet = (o_phone_type_residence_get.countryPhone ?: "") + (o_phone_type_residence_get.phoneArea ?: "") + (o_phone_type_residence_get.phoneNumber ?: "")
        assertEquals phoneNumberResidenceGet, o_phone_type_residence_get.phoneNumberDetail
    }

    //POST- Person Create API
    @Test
    void testCreatePersonWithStateAndZipIntegrationSettingValue() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.v2+json")
        request.addHeader("Accept", "application/vnd.hedtech.integration.v2+json")

        Map content = newPersonWithAddressRequest()

        def o_success_person_create = personCompositeService.create(content)

        assertNotNull o_success_person_create
        assertNotNull o_success_person_create.guid
        assertEquals i_success_address_type_1, o_success_person_create.addresses[0].addressType
        assertEquals i_success_city, o_success_person_create.addresses[0].address.city
        assertEquals i_success_street_line1, o_success_person_create.addresses[0].address.streetLine1
        assertEquals i_success_zip, o_success_person_create.addresses[0].address.zip
        assertEquals i_success_state, o_success_person_create.addresses[0].address.state.code
        assertEquals i_success_address_type_2, o_success_person_create.addresses[1].addressType
        assertEquals i_success_city, o_success_person_create.addresses[1].address.city
        assertEquals i_success_street_line1, o_success_person_create.addresses[1].address.streetLine1
        assertEquals i_success_zip_goriccr_data, o_success_person_create.addresses[1].address.zip
        assertEquals i_success_state_goriccr_data, o_success_person_create.addresses[1].address.state.code
    }


    //POST- Person Create API
    @Test
    void testCreatePersonWithAlternateNameHavingBirthNameType() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.v3+json")
        request.addHeader("Accept", "application/vnd.hedtech.integration.v3+json")

        Map content = newPersonWithAlternateNameHavingBirthNameType()

        def o_success_person_create = personCompositeService.create(content)

        assertNotNull o_success_person_create
        assertNotNull o_success_person_create.guid
        assertEquals 2, o_success_person_create.names?.size()

        def o_primary_name_create = o_success_person_create.names.find { it.nameType == "Primary" }
        def o_birth_name_create = o_success_person_create.names.find { it.nameType == "Birth" }

        assertNotNull o_primary_name_create
        assertEquals i_success_first_name, o_primary_name_create.firstName
        assertEquals i_success_middle_name, o_primary_name_create.middleName
        assertEquals i_success_last_name, o_primary_name_create.lastName
        assertEquals i_success_primary_name_type, o_primary_name_create.nameType
        assertEquals i_success_namePrefix, o_primary_name_create.title
        assertEquals i_success_nameSuffix, o_primary_name_create.pedigree
        assertEquals i_success_preferenceFirstName, o_primary_name_create.preferredName
        assertNotNull o_birth_name_create
        assertEquals i_success_alternate_first_name, o_birth_name_create.firstName
        assertEquals i_success_alternate_middle_name, o_birth_name_create.middleName
        assertEquals i_success_alternate_last_name, o_birth_name_create.lastName
        assertEquals i_success_alternate_birth_name_type, o_birth_name_create.nameType
    }

    //POST- Person Create API
    @Test
    void testCreatePersonWithEthnicity() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.v2+json")
        request.addHeader("Accept", "application/vnd.hedtech.integration.v2+json")

        Map content = newPersonWithAddressRequest()
        def o_success_person_create = personCompositeService.create(content)

        assertNotNull o_success_person_create
        assertNotNull o_success_person_create.guid
        assertEquals content.ethnicityDetail.guid, o_success_person_create.ethnicityDetail.guid
    }

    //POST- Person Create API
    @Test
    void testCreatePersonWithUSethnicity() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.v3+json")
        request.addHeader("Accept", "application/vnd.hedtech.integration.v3+json")

        Map content = newPersonWithUSEthnicity()

        def o_success_person_create = personCompositeService.create(content)

        assertNotNull o_success_person_create
        assertNotNull o_success_person_create.guid
        assertEquals content.ethnicityDetail.guid, o_success_person_create.ethnicityDetail.guid
    }

    @Test
    void testCreatePersonWithInvalidUSethnicity() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.v3+json")
        request.addHeader("Accept", "application/vnd.hedtech.integration.v3+json")

        Map content = newPersonWithAddressRequest()
        try {
            personCompositeService.create(content)
            fail('This should have failed as US ethnicity GUID is invalid')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'not.found.message'
        }
    }

    //POST- Person Create API
    @Test
    void testCreatePersonWithInvalidShortPhoneNumber() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.v1+json")
        request.addHeader("Accept", "application/vnd.hedtech.integration.v1+json")

        Map content = newPersonRequestWithInvalidShortPhoneNumber()

        def o_success_person_create = personCompositeService.create(content)

        assertNotNull o_success_person_create
        assertNotNull o_success_person_create.guid
        def o_phone_type_mobile = o_success_person_create.phones.find { it.phoneType == "Mobile" }
        assertNotNull o_phone_type_mobile
        assertEquals i_success_phone_type_mobile, o_phone_type_mobile.phoneType
        assertEquals i_success_phone_extension, o_phone_type_mobile.phoneExtension
        assertEquals i_succes_invalid_phone_number_short1, o_phone_type_mobile.phoneNumberDetail
        String phoneNumberMobile = (o_phone_type_mobile.countryPhone ?: "") + (o_phone_type_mobile.phoneArea ?: "") + (o_phone_type_mobile.phoneNumber ?: "")
        assertEquals phoneNumberMobile, o_phone_type_mobile.phoneNumberDetail

        def o_phone_type_home = o_success_person_create.phones.find { it.phoneType == "Home" }
        assertNotNull o_phone_type_home
        assertEquals i_success_phone_type_home, o_phone_type_home.phoneType
        assertEquals i_success_phone_extension, o_phone_type_home.phoneExtension
        assertEquals i_succes_invalid_phone_number_short2, o_phone_type_home.phoneNumberDetail
        String phoneNumberHome = (o_phone_type_home.countryPhone ?: "") + (o_phone_type_home.phoneArea ?: "") + (o_phone_type_home.phoneNumber ?: "")
        assertEquals phoneNumberHome, o_phone_type_home.phoneNumberDetail

        def o_phone_type_work = o_success_person_create.phones.find { it.phoneType == "Work" }
        assertNotNull o_phone_type_work
        assertEquals i_success_phone_type_work, o_phone_type_work.phoneType
        assertEquals i_success_phone_extension, o_phone_type_work.phoneExtension
        assertEquals i_succes_invalid_phone_number_short3, o_phone_type_work.phoneNumberDetail
        String phoneNumberWork = (o_phone_type_work.countryPhone ?: "") + (o_phone_type_work.phoneArea ?: "") + (o_phone_type_work.phoneNumber ?: "")
        assertEquals phoneNumberWork, o_phone_type_work.phoneNumberDetail


        def o_phone_type_residence = o_success_person_create.phones.find { it.phoneType == "Residence" }
        assertNotNull o_phone_type_residence
        assertEquals i_success_phone_type_residence, o_phone_type_residence.phoneType
        assertEquals i_success_phone_extension, o_phone_type_residence.phoneExtension
        assertEquals i_succes_invalid_phone_number_short4, o_phone_type_residence.phoneNumberDetail
        String phoneNumberResidence = (o_phone_type_residence.countryPhone ?: "") + (o_phone_type_residence.phoneArea ?: "") + (o_phone_type_residence.phoneNumber ?: "")
        assertEquals phoneNumberResidence, o_phone_type_residence.phoneNumberDetail
    }

    //POST- Person Create API
    @Test
    void testCreatePersonWithInvalidLongPhoneNumber() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.v1+json")
        request.addHeader("Accept", "application/vnd.hedtech.integration.v1+json")

        Map content = newPersonRequestWithInvalidLongPhoneNumber()

        def o_success_person_create = personCompositeService.create(content)

        assertNotNull o_success_person_create
        assertNotNull o_success_person_create.guid
        def o_phone_type_mobile = o_success_person_create.phones.find { it.phoneType == "Mobile" }
        assertNotNull o_phone_type_mobile
        assertEquals i_success_phone_type_mobile, o_phone_type_mobile.phoneType
        assertEquals i_success_phone_extension, o_phone_type_mobile.phoneExtension
        assertEquals i_succes_invalid_phone_number_long1.substring(0, 22), o_phone_type_mobile.phoneNumberDetail
        String phoneNumberMobile = (o_phone_type_mobile.countryPhone ?: "") + (o_phone_type_mobile.phoneArea ?: "") + (o_phone_type_mobile.phoneNumber ?: "")
        assertEquals phoneNumberMobile, o_phone_type_mobile.phoneNumberDetail

        def o_phone_type_home = o_success_person_create.phones.find { it.phoneType == "Home" }
        assertNotNull o_phone_type_home
        assertEquals i_success_phone_type_home, o_phone_type_home.phoneType
        assertEquals i_success_phone_extension, o_phone_type_home.phoneExtension
        assertEquals i_succes_invalid_phone_number_long2, o_phone_type_home.phoneNumberDetail
        String phoneNumberHome = (o_phone_type_home.countryPhone ?: "") + (o_phone_type_home.phoneArea ?: "") + (o_phone_type_home.phoneNumber ?: "")
        assertEquals phoneNumberHome, o_phone_type_home.phoneNumberDetail

        def o_phone_type_work = o_success_person_create.phones.find { it.phoneType == "Work" }
        assertNotNull o_phone_type_work
        assertEquals i_success_phone_type_work, o_phone_type_work.phoneType
        assertEquals i_success_phone_extension, o_phone_type_work.phoneExtension
        assertEquals i_succes_invalid_phone_number_long3, o_phone_type_work.phoneNumberDetail
        String phoneNumberWork = (o_phone_type_work.countryPhone ?: "") + (o_phone_type_work.phoneArea ?: "") + (o_phone_type_work.phoneNumber ?: "")
        assertEquals phoneNumberWork, o_phone_type_work.phoneNumberDetail

        def o_phone_type_residence = o_success_person_create.phones.find { it.phoneType == "Residence" }
        assertNotNull o_phone_type_residence
        assertEquals i_success_phone_type_residence, o_phone_type_residence.phoneType
        assertEquals i_success_phone_extension, o_phone_type_residence.phoneExtension
        assertEquals i_succes_invalid_phone_number_long4, o_phone_type_residence.phoneNumberDetail
        String phoneNumberResidence = (o_phone_type_residence.countryPhone ?: "") + (o_phone_type_residence.phoneArea ?: "") + (o_phone_type_residence.phoneNumber ?: "")
        assertEquals phoneNumberResidence, o_phone_type_residence.phoneNumberDetail
    }

    //PUT- Person Update API
    @Test
    void testUpdatePersonWithExisitingInvalidPhoneNumber() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.v1+json")
        request.addHeader("Accept", "application/vnd.hedtech.integration.v1+json")

        Map content_create = newPersonRequestWithInvalidShortPhoneNumber()

        def o_success_person_create = personCompositeService.create(content_create)

        assertNotNull o_success_person_create
        assertNotNull o_success_person_create.guid
        def o_phone_type_mobile_create = o_success_person_create.phones.find { it.phoneType == "Mobile" }
        assertNotNull o_phone_type_mobile_create
        assertEquals i_success_phone_type_mobile, o_phone_type_mobile_create.phoneType
        assertEquals i_success_phone_extension, o_phone_type_mobile_create.phoneExtension
        assertEquals i_succes_invalid_phone_number_short1, o_phone_type_mobile_create.phoneNumberDetail
        String phoneNumberMobileCreate = (o_phone_type_mobile_create.countryPhone ?: "") + (o_phone_type_mobile_create.phoneArea ?: "") + (o_phone_type_mobile_create.phoneNumber ?: "")
        assertEquals phoneNumberMobileCreate, o_phone_type_mobile_create.phoneNumberDetail

        def o_phone_type_home_create = o_success_person_create.phones.find { it.phoneType == "Home" }
        assertNotNull o_phone_type_home_create
        assertEquals i_success_phone_type_home, o_phone_type_home_create.phoneType
        assertEquals i_success_phone_extension, o_phone_type_home_create.phoneExtension
        assertEquals i_succes_invalid_phone_number_short2, o_phone_type_home_create.phoneNumberDetail
        String phoneNumberHomeCreate = (o_phone_type_home_create.countryPhone ?: "") + (o_phone_type_home_create.phoneArea ?: "") + (o_phone_type_home_create.phoneNumber ?: "")
        assertEquals phoneNumberHomeCreate, o_phone_type_home_create.phoneNumberDetail

        def o_phone_type_work_create = o_success_person_create.phones.find { it.phoneType == "Work" }
        assertNotNull o_phone_type_work_create
        assertEquals i_success_phone_type_work, o_phone_type_work_create.phoneType
        assertEquals i_success_phone_extension, o_phone_type_work_create.phoneExtension
        assertEquals i_succes_invalid_phone_number_short3, o_phone_type_work_create.phoneNumberDetail
        String phoneNumberWorkCreate = (o_phone_type_work_create.countryPhone ?: "") + (o_phone_type_work_create.phoneArea ?: "") + (o_phone_type_work_create.phoneNumber ?: "")
        assertEquals phoneNumberWorkCreate, o_phone_type_work_create.phoneNumberDetail

        def o_phone_type_residence_create = o_success_person_create.phones.find { it.phoneType == "Residence" }
        assertNotNull o_phone_type_residence_create
        assertEquals i_success_phone_type_residence, o_phone_type_residence_create.phoneType
        assertEquals i_success_phone_extension, o_phone_type_residence_create.phoneExtension
        assertEquals i_succes_invalid_phone_number_short4, o_phone_type_residence_create.phoneNumberDetail
        String phoneNumberResidenceCreate = (o_phone_type_residence_create.countryPhone ?: "") + (o_phone_type_residence_create.phoneArea ?: "") + (o_phone_type_residence_create.phoneNumber ?: "")
        assertEquals phoneNumberResidenceCreate, o_phone_type_residence_create.phoneNumberDetail

        //update the phone number
        Map content_update = newPersonRequestWithInvalidLongPhoneNumber()
        content_update.put("id", o_success_person_create.guid)

        def o_success_person_update = personCompositeService.update(content_update)

        assertNotNull o_success_person_update
        assertNotNull o_success_person_update.guid
        assertEquals o_success_person_create.guid, o_success_person_update.guid

        def o_phone_type_mobile_update = o_success_person_update.phones.find { it.phoneType == "Mobile" }
        assertNotNull o_phone_type_mobile_update
        assertEquals i_success_phone_type_mobile, o_phone_type_mobile_update.phoneType
        assertEquals i_success_phone_extension, o_phone_type_mobile_update.phoneExtension
        assertEquals i_succes_invalid_phone_number_long1.substring(0, 22), o_phone_type_mobile_update.phoneNumberDetail
        String phoneNumberMobileUpdate = (o_phone_type_mobile_update.countryPhone ?: "") + (o_phone_type_mobile_update.phoneArea ?: "") + (o_phone_type_mobile_update.phoneNumber ?: "")
        assertEquals phoneNumberMobileUpdate, o_phone_type_mobile_update.phoneNumberDetail

        def o_phone_type_home_update = o_success_person_update.phones.find { it.phoneType == "Home" }
        assertNotNull o_phone_type_home_update
        assertEquals i_success_phone_type_home, o_phone_type_home_update.phoneType
        assertEquals i_success_phone_extension, o_phone_type_home_update.phoneExtension
        assertEquals i_succes_invalid_phone_number_long2, o_phone_type_home_update.phoneNumberDetail
        String phoneNumberHomeUpdate = (o_phone_type_home_update.countryPhone ?: "") + (o_phone_type_home_update.phoneArea ?: "") + (o_phone_type_home_update.phoneNumber ?: "")
        assertEquals phoneNumberHomeUpdate, o_phone_type_home_update.phoneNumberDetail

        def o_phone_type_work_update = o_success_person_update.phones.find { it.phoneType == "Work" }
        assertNotNull o_phone_type_work_update
        assertEquals i_success_phone_type_work, o_phone_type_work_update.phoneType
        assertEquals i_success_phone_extension, o_phone_type_work_update.phoneExtension
        assertEquals i_succes_invalid_phone_number_long3, o_phone_type_work_update.phoneNumberDetail
        String phoneNumberWorkUpdate = (o_phone_type_work_update.countryPhone ?: "") + (o_phone_type_work_update.phoneArea ?: "") + (o_phone_type_work_update.phoneNumber ?: "")
        assertEquals phoneNumberWorkUpdate, o_phone_type_work_update.phoneNumberDetail

        def o_phone_type_residence_update = o_success_person_update.phones.find { it.phoneType == "Residence" }
        assertNotNull o_phone_type_residence_update
        assertEquals i_success_phone_type_residence, o_phone_type_residence_update.phoneType
        assertEquals i_success_phone_extension, o_phone_type_residence_update.phoneExtension
        assertEquals i_succes_invalid_phone_number_long4, o_phone_type_residence_update.phoneNumberDetail
        String phoneNumberResidenceUpdate = (o_phone_type_residence_update.countryPhone ?: "") + (o_phone_type_residence_update.phoneArea ?: "") + (o_phone_type_residence_update.phoneNumber ?: "")
        assertEquals phoneNumberResidenceUpdate, o_phone_type_residence_update.phoneNumberDetail
    }

    //PUT- Person Update API
    @Test
    void testUpdatePersonWithNewInvalidPhoneNumber() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.v1+json")
        request.addHeader("Accept", "application/vnd.hedtech.integration.v1+json")

        Map content_create = newPersonWithAddressRequest()
        def o_success_person_create = personCompositeService.create(content_create)

        assertNotNull o_success_person_create
        assertNotNull o_success_person_create.guid

        //update the phone number
        Map content_update = newPersonRequestWithInvalidLongPhoneNumber()
        content_update.put("id", o_success_person_create.guid)

        def o_success_person_update = personCompositeService.update(content_update)

        assertNotNull o_success_person_update
        assertNotNull o_success_person_update.guid
        assertEquals o_success_person_create.guid, o_success_person_update.guid

        def o_phone_type_mobile_update = o_success_person_update.phones.find { it.phoneType == "Mobile" }
        assertNotNull o_phone_type_mobile_update
        assertEquals i_success_phone_type_mobile, o_phone_type_mobile_update.phoneType
        assertEquals i_success_phone_extension, o_phone_type_mobile_update.phoneExtension
        assertEquals i_succes_invalid_phone_number_long1.substring(0, 22), o_phone_type_mobile_update.phoneNumberDetail
        String phoneNumberMobileUpdate = (o_phone_type_mobile_update.countryPhone ?: "") + (o_phone_type_mobile_update.phoneArea ?: "") + (o_phone_type_mobile_update.phoneNumber ?: "")
        assertEquals phoneNumberMobileUpdate, o_phone_type_mobile_update.phoneNumberDetail

        def o_phone_type_home_update = o_success_person_update.phones.find { it.phoneType == "Home" }
        assertNotNull o_phone_type_home_update
        assertEquals i_success_phone_type_home, o_phone_type_home_update.phoneType
        assertEquals i_success_phone_extension, o_phone_type_home_update.phoneExtension
        assertEquals i_succes_invalid_phone_number_long2, o_phone_type_home_update.phoneNumberDetail
        String phoneNumberHomeUpdate = (o_phone_type_home_update.countryPhone ?: "") + (o_phone_type_home_update.phoneArea ?: "") + (o_phone_type_home_update.phoneNumber ?: "")
        assertEquals phoneNumberHomeUpdate, o_phone_type_home_update.phoneNumberDetail

        def o_phone_type_work_update = o_success_person_update.phones.find { it.phoneType == "Work" }
        assertNotNull o_phone_type_work_update
        assertEquals i_success_phone_type_work, o_phone_type_work_update.phoneType
        assertEquals i_success_phone_extension, o_phone_type_work_update.phoneExtension
        assertEquals i_succes_invalid_phone_number_long3, o_phone_type_work_update.phoneNumberDetail

        def o_phone_type_residence_update = o_success_person_update.phones.find { it.phoneType == "Residence" }
        assertNotNull o_phone_type_residence_update
        assertEquals i_success_phone_type_residence, o_phone_type_residence_update.phoneType
        assertEquals i_success_phone_extension, o_phone_type_residence_update.phoneExtension
        assertEquals i_succes_invalid_phone_number_long4, o_phone_type_residence_update.phoneNumberDetail
        String phoneNumberResidenceUpdate = (o_phone_type_residence_update.countryPhone ?: "") + (o_phone_type_residence_update.phoneArea ?: "") + (o_phone_type_residence_update.phoneNumber ?: "")
        assertEquals phoneNumberResidenceUpdate, o_phone_type_residence_update.phoneNumberDetail
    }

    @Test
    void testUpdatePersonFirstNameAndLastNameChangeWithCreatingPersonBase() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.v1+json")
        request.addHeader("Accept", "application/vnd.hedtech.integration.v1+json")

        PersonBasicPersonBase personBasicPersonBase = createPersonBasicPersonBase()
        PersonIdentificationNameCurrent personIdentificationNameCurrent = PersonIdentificationNameCurrent.findAllByPidmInList([personBasicPersonBase.pidm]).get(0)
        GlobalUniqueIdentifier uniqueIdentifier = GlobalUniqueIdentifier.findByLdmNameAndDomainKey("persons", personIdentificationNameCurrent.pidm)
        assertNotNull uniqueIdentifier
        Map params = getPersonWithFirstNameChangeRequest(personIdentificationNameCurrent, uniqueIdentifier.guid);
        //checking the update method creating the person base record if not present.
        def id = personBasicPersonBase.id
        personBasicPersonBaseService.delete([domainModel: personBasicPersonBase])
        assertNull "PersonBasicPersonBase should have been deleted", personBasicPersonBase.get(id)

        //update person with FirstName change and add the person base record
        personCompositeService.update(params)
        PersonIdentificationNameCurrent newPersonIdentificationName = PersonIdentificationNameCurrent.findAllByPidmInList([personBasicPersonBase.pidm]).get(0)
        assertEquals newPersonIdentificationName.firstName, 'CCCCCC'
        assertEquals newPersonIdentificationName.changeIndicator, null

        PersonIdentificationNameAlternate personIdentificationNameAlternate = PersonIdentificationNameAlternate.fetchAllByPidm(personBasicPersonBase.pidm).get(0)
        assertEquals personIdentificationNameAlternate.changeIndicator, 'N'
        params = getPersonWithLastNameChangeRequest(personIdentificationNameCurrent, uniqueIdentifier.guid);

        //update person with LastName change
        personCompositeService.update(params)
        assertEquals newPersonIdentificationName.lastName, 'CCCCCC'
        assertEquals newPersonIdentificationName.changeIndicator, null
        personIdentificationNameAlternate = PersonIdentificationNameAlternate.fetchAllByPidm(personBasicPersonBase.pidm).get(0)
        assertEquals personIdentificationNameAlternate.changeIndicator, 'N'
    }


    @Test
    void testUpdatePersonIDChange() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.v1+json")
        request.addHeader("Accept", "application/vnd.hedtech.integration.v1+json")

        PersonBasicPersonBase personBasicPersonBase = createPersonBasicPersonBase()
        PersonIdentificationNameCurrent personIdentificationNameCurrent = PersonIdentificationNameCurrent.findAllByPidmInList([personBasicPersonBase.pidm]).get(0)
        GlobalUniqueIdentifier uniqueIdentifier = GlobalUniqueIdentifier.findByLdmNameAndDomainKey("persons", personIdentificationNameCurrent.pidm)
        assertNotNull uniqueIdentifier

        Map params = getPersonWithIdChangeRequest(personIdentificationNameCurrent, uniqueIdentifier.guid);
        //update person with ID change
        personCompositeService.update(params)
        PersonIdentificationNameCurrent newPersonIdentificationName = PersonIdentificationNameCurrent.findAllByPidmInList([personBasicPersonBase.pidm]).get(0)
        assertEquals newPersonIdentificationName.bannerId, 'CHANGED'
        assertEquals newPersonIdentificationName.changeIndicator, null
        PersonIdentificationNameAlternate personIdentificationNameAlternate = PersonIdentificationNameAlternate.fetchAllByPidm(personBasicPersonBase.pidm).get(0)
        assertEquals personIdentificationNameAlternate.changeIndicator, 'I'
    }


    @Test
    void testUpdatePersonPersonBasicPersonBase() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.v1+json")
        request.addHeader("Accept", "application/vnd.hedtech.integration.v1+json")

        PersonBasicPersonBase personBasicPersonBase = createPersonBasicPersonBase()
        PersonIdentificationNameCurrent personIdentificationNameCurrent = PersonIdentificationNameCurrent.findAllByPidmInList([personBasicPersonBase.pidm]).get(0)
        GlobalUniqueIdentifier uniqueIdentifier = GlobalUniqueIdentifier.findByLdmNameAndDomainKey("persons", personIdentificationNameCurrent.pidm)
        Map params = getPersonWithPersonBasicPersonBaseChangeRequest(personIdentificationNameCurrent, uniqueIdentifier.guid);

        //update PersonBasicPersonBase info
        personCompositeService.update(params)
        PersonBasicPersonBase newPersonBasicPersonBase = PersonBasicPersonBase.fetchByPidmList([personBasicPersonBase.pidm]).get(0)

        assertEquals 'F', personBasicPersonBase.sex
        assertEquals 'CCCCC', personBasicPersonBase.preferenceFirstName
        assertEquals 'CCCCC', personBasicPersonBase.namePrefix
        assertEquals 'CCCCC', personBasicPersonBase.nameSuffix
        assertEquals 'TTTTT', personBasicPersonBase.ssn
    }


    @Test
    void testUpdatetestPersonAddressValidUpdate() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.v1+json")
        request.addHeader("Accept", "application/vnd.hedtech.integration.v1+json")

        PersonBasicPersonBase personBasicPersonBase = createPersonBasicPersonBase()
        PersonIdentificationNameCurrent personIdentificationNameCurrent = PersonIdentificationNameCurrent.findAllByPidmInList([personBasicPersonBase.pidm]).get(0)
        GlobalUniqueIdentifier uniqueIdentifier = GlobalUniqueIdentifier.findByLdmNameAndDomainKey("persons", personIdentificationNameCurrent.pidm)
        assertNotNull uniqueIdentifier

        Map params = getPersonWithNewAdressRequest(personIdentificationNameCurrent, uniqueIdentifier.guid);
        //update PersonBasicPersonBase info, since Address won't exists create new Adress through update
        def newAddressList = personCompositeService.update(params).addresses
        assertNotNull newAddressList
        assertTrue newAddressList.size > 0
        assertTrue newAddressList.size == 2

        newAddressList.each { currAddress ->
            if (currAddress.addressType == 'Mailing') {
                assertEquals 'Southeastern', currAddress.city
                assertEquals 'CA', currAddress.state
                assertEquals '5890 139th Ave', currAddress.streetLine1
                assertEquals '19398', currAddress.zip
            }
            if (currAddress.addressType == 'Home') {
                assertEquals 'Pavo', currAddress.city
                assertEquals 'GA', currAddress.state
                assertEquals '123 Main Line', currAddress.streetLine1
                assertEquals '31778', currAddress.zip
            }

        }
        //modify the Address and update
        Map modifiedAddress = getPersonWithModifiedAdressRequest(personIdentificationNameCurrent, uniqueIdentifier.guid);

        //update Modified Address
        def modifiedAddressList = personCompositeService.update(modifiedAddress).addresses

        assertTrue modifiedAddressList.size > 0
        assertTrue modifiedAddressList.size == 2

        modifiedAddressList.each { currAddress ->
            if (currAddress.addressType == 'Home') {
                assertEquals 'Southeastern', currAddress.city
                assertEquals 'CA', currAddress.state
                assertEquals '5890 139th Ave', currAddress.streetLine1
                assertEquals '19398', currAddress.zip
            }
            if (currAddress.addressType == 'Mailing') {
                assertEquals 'Pavo', currAddress.city
                assertEquals 'GA', currAddress.state
                assertEquals '123 Main Line', currAddress.streetLine1
                assertEquals '31778', currAddress.zip
            }

        }

        Map unchangedAddress = getPersonWithModifiedAdressRequest(personIdentificationNameCurrent, uniqueIdentifier.guid);

        //update with same Address
        def unchangedAddressList = personCompositeService.update(unchangedAddress).addresses

        assertTrue unchangedAddressList.size > 0
        assertTrue unchangedAddressList.size == 2

        unchangedAddressList.each { currAddress ->
            if (currAddress.addressType == 'Home') {
                assertEquals 'Southeastern', currAddress.city
                assertEquals 'CA', currAddress.state
                assertEquals '5890 139th Ave', currAddress.streetLine1
                assertEquals '19398', currAddress.zip
            }
            if (currAddress.addressType == 'Mailing') {
                assertEquals 'Pavo', currAddress.city
                assertEquals 'GA', currAddress.state
                assertEquals '123 Main Line', currAddress.streetLine1
                assertEquals '31778', currAddress.zip
            }
        }
    }


    @Test
    void testUpdatetestPersonPhonesValidUpdate() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.v1+json")
        request.addHeader("Accept", "application/vnd.hedtech.integration.v1+json")

        PersonBasicPersonBase personBasicPersonBase = createPersonBasicPersonBase()
        PersonIdentificationNameCurrent personIdentificationNameCurrent = PersonIdentificationNameCurrent.findAllByPidmInList([personBasicPersonBase.pidm]).get(0)
        GlobalUniqueIdentifier uniqueIdentifier = GlobalUniqueIdentifier.findByLdmNameAndDomainKey("persons", personIdentificationNameCurrent.pidm)
        assertNotNull uniqueIdentifier
        Map newPhones = getPersonWithNewPhonesRequest(personIdentificationNameCurrent, uniqueIdentifier.guid);
        // create new phone through update()
        def newPhonesList = personCompositeService.update(newPhones).phones

        assertTrue newPhonesList.size > 0
        assertTrue newPhonesList.size == 2

        newPhonesList.each { currPhone ->

            def phoneList = newPhones.phones
            phoneList.each { phone ->
                if (currPhone.phoneType == 'Home' && phone.phoneType == 'Home') {
                    def phoneNumber = (phone.countryPhone ?: "") + (phone.phoneArea ?: "") + (phone.phoneNumber ?: "")
                    assert phoneNumber, currPhone.phoneNumberDetail
                }
                if (currPhone.phoneType == 'Mobile' && phone.phoneType == 'Home') {
                    def phoneNumber = (phone.countryPhone ?: "") + (phone.phoneArea ?: "") + (phone.phoneNumber ?: "")
                    assert phoneNumber, currPhone.phoneNumberDetail
                }
            }
        }

        //modify the Phones
        Map modifiedPhones = getPersonWithModifiedPhonesRequest(personIdentificationNameCurrent, uniqueIdentifier.guid);

        //update Modified Phones
        def modifiedPhonesList = personCompositeService.update(modifiedPhones).phones

        assertTrue modifiedPhonesList.size > 0
        assertTrue modifiedPhonesList.size == 2

        modifiedPhonesList.each { currPhone ->
            def phoneList = modifiedPhones.phones
            phoneList.each { phone ->
                if (currPhone.phoneType == 'Home' && phone.phoneType == 'Home') {
                    def phoneNumber = (phone.countryPhone ?: "") + (phone.phoneArea ?: "") + (phone.phoneNumber ?: "")
                    assert phoneNumber, currPhone.phoneNumberDetail
                }
                if (currPhone.phoneType == 'Mobile' && phone.phoneType == 'Home') {
                    def phoneNumber = (phone.countryPhone ?: "") + (phone.phoneArea ?: "") + (phone.phoneNumber ?: "")
                    assert phoneNumber, currPhone.phoneNumberDetail
                }
            }
        }

        //unchanged  phones
        Map unchangedPhones = getPersonWithModifiedPhonesRequest(personIdentificationNameCurrent, uniqueIdentifier.guid);

        //update unchanged  phones
        def unchangedPhonesList = personCompositeService.update(unchangedPhones).phones

        assertTrue unchangedPhonesList.size > 0
        assertTrue unchangedPhonesList.size == 2

        unchangedPhonesList.each { currPhone ->
            def phoneList = unchangedPhones.phones
            phoneList.each { phone ->
                if (currPhone.phoneType == 'Home' && phone.phoneType == 'Home') {
                    def phoneNumber = (phone.countryPhone ?: "") + (phone.phoneArea ?: "") + (phone.phoneNumber ?: "")
                    assert phoneNumber, currPhone.phoneNumberDetail
                }
                if (currPhone.phoneType == 'Mobile' && phone.phoneType == 'Home') {
                    def phoneNumber = (phone.countryPhone ?: "") + (phone.phoneArea ?: "") + (phone.phoneNumber ?: "")
                    assert phoneNumber, currPhone.phoneNumberDetail
                }
            }
        }


    }


    @Test
    void testUpdatetestPersonRacesValidUpdate() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.v1+json")
        request.addHeader("Accept", "application/vnd.hedtech.integration.v1+json")

        PersonBasicPersonBase personBasicPersonBase = createPersonBasicPersonBase()
        PersonIdentificationNameCurrent personIdentificationNameCurrent = PersonIdentificationNameCurrent.findAllByPidmInList([personBasicPersonBase.pidm]).get(0)
        GlobalUniqueIdentifier uniqueIdentifier = GlobalUniqueIdentifier.findByLdmNameAndDomainKey("persons", personIdentificationNameCurrent.pidm)
        assertNotNull uniqueIdentifier
        def races = GlobalUniqueIdentifier.findAllByLdmName('races')
        Map newRaces = getPersonWithNewRacesRequest(personIdentificationNameCurrent, uniqueIdentifier.guid, races)

        // create new race through update()
        def raceList = personCompositeService.update(newRaces).races

        assertEquals raceList.size, 1
        assertEquals raceList[0].guid, races[0].guid

        //  update race using same request
        def unchangedRaceList = personCompositeService.update(newRaces).races
        assertEquals unchangedRaceList.size, 1
        assertEquals unchangedRaceList[0].guid, races[0].guid

        //update with new race
        Map modifiedRaces = getPersonWithModifiedRacesRequest(personIdentificationNameCurrent, uniqueIdentifier.guid, races)

        // create new race through update()
        def modifiedRacesList = personCompositeService.update(modifiedRaces).races

        assertEquals modifiedRacesList.size, 2
        assertEquals modifiedRacesList[0].guid, races[0].guid
        assertEquals modifiedRacesList[1].guid, races[1].guid
    }


    @Test
    void testUpdatetestPersonEthnicityValidUpdate() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.v2+json")
        request.addHeader("Accept", "application/vnd.hedtech.integration.v2+json")

        PersonBasicPersonBase personBasicPersonBase = createPersonBasicPersonBase()
        PersonIdentificationNameCurrent personIdentificationNameCurrent = PersonIdentificationNameCurrent.findAllByPidmInList([personBasicPersonBase.pidm]).get(0)
        GlobalUniqueIdentifier uniqueIdentifier = GlobalUniqueIdentifier.findByLdmNameAndDomainKey("persons", personIdentificationNameCurrent.pidm)
        assertNotNull uniqueIdentifier

        def ethnicity1 = GlobalUniqueIdentifier.findByLdmNameAndDomainKey('ethnicities', '3')
        Map newEthnicities = getPersonWithNewEthniciiesyRequest(personIdentificationNameCurrent, uniqueIdentifier.guid, ethnicity1)

        // create new Ethnicity through update()
        def ethnicityDetail = personCompositeService.update(newEthnicities).ethnicityDetail

        //assertEquals ethnicityDetail.size ,1
        assertEquals ethnicityDetail.guid, ethnicity1.guid
        assertEquals "Hispanic", ethnicityDetail.parentCategory

        //  update Ethnicity using same request
        def unchangedEthnicityDetail = personCompositeService.update(newEthnicities).ethnicityDetail
        assertEquals unchangedEthnicityDetail.guid, ethnicity1.guid
        assertEquals "Hispanic", ethnicityDetail.parentCategory

        //update with new Ethnicity
        def ethnicity2 = GlobalUniqueIdentifier.findByLdmNameAndDomainKey('ethnicities', '4')
        Map modifiedEthnicity = getPersonWithModifiedEthniciiesyRequest(personIdentificationNameCurrent, uniqueIdentifier.guid, ethnicity2)

        // create new Ethnicity through update()
        def modifiedEthnicityDetail = personCompositeService.update(modifiedEthnicity).ethnicityDetail
        assertEquals modifiedEthnicityDetail.guid, ethnicity2.guid
        assertEquals "Non-Hispanic", modifiedEthnicityDetail.parentCategory
    }

    //PUT- person update API
    @Test
    void testUpdatePersonEmailWithoutExistingEmailRecord() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.v2+json")
        request.addHeader("Accept", "application/vnd.hedtech.integration.v2+json")

        PersonBasicPersonBase personBasicPersonBase = createPersonBasicPersonBase()

        assertNotNull personBasicPersonBase
        assertNotNull personBasicPersonBase.pidm

        PersonIdentificationNameCurrent personIdentificationNameCurrent = PersonIdentificationNameCurrent.findAllByPidmInList([personBasicPersonBase.pidm]).get(0)

        assertNotNull personIdentificationNameCurrent
        assertNotNull personIdentificationNameCurrent.pidm

        String i_success_guid = GlobalUniqueIdentifier.findByLdmNameAndDomainKey("persons", personIdentificationNameCurrent.pidm)?.guid

        assertNotNull i_success_guid
        Map params = updatePersonWithPreferredEmailAddress(i_success_guid)

        // emailType "Preferred" is ignored in 9.4.0.1 SR.  So removing it from request
        params.emails.removeAll { it.emailType.trim() == i_success_emailType_preferred }

        //update PersonBasicPersonBase info
        def o_person_update = personCompositeService.update(params)

        assertNotNull o_person_update
        assertEquals i_success_guid, o_person_update.guid
        assertEquals 2, o_person_update.emails?.size()
        assertEquals i_success_guid_personal, o_person_update.emails[0].guid
        assertEquals i_success_emailType_personal, o_person_update.emails[0].emailType
        assertEquals i_success_emailAddress_personal, o_person_update.emails[0].emailAddress
        assertEquals i_success_guid_institution, o_person_update.emails[1].guid
        assertEquals i_success_emailType_institution, o_person_update.emails[1].emailType
        assertEquals i_success_emailAddress_institution, o_person_update.emails[1].emailAddress
    }


    @Test
    void testUpdatePerson_RetainPreferredFlagByEmailType() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.v3+json")
        request.addHeader("Accept", "application/vnd.hedtech.integration.v3+json")

        Map content = newPersonWithPreferredEmailRequest()
        // emailType "Preferred" is ignored in 9.4.0.1 SR.  So removing it from request.
        content.emails.removeAll { it.emailType.trim() == i_success_emailType_preferred }

        // Create person with 2 emails. One emailType is "Personal" and other emailType is "Institution".
        def o_success_person_create = personCompositeService.create(content)
        assertNotNull o_success_person_create
        assertNotNull o_success_person_create.guid
        assertEquals 2, o_success_person_create.emails?.size()
        assertEquals i_success_guid_personal, o_success_person_create.emails[0].guid
        assertEquals i_success_emailType_personal, o_success_person_create.emails[0].emailType
        assertEquals i_success_emailAddress_personal, o_success_person_create.emails[0].emailAddress
        assertEquals i_success_guid_institution, o_success_person_create.emails[1].guid
        assertEquals i_success_emailType_institution, o_success_person_create.emails[1].emailType
        assertEquals i_success_emailAddress_institution, o_success_person_create.emails[1].emailAddress

        String personGuid = o_success_person_create.guid
        GlobalUniqueIdentifier globalUniqueIdentifier = GlobalUniqueIdentifier.fetchByLdmNameAndGuid("persons", personGuid)
        Integer pidm = globalUniqueIdentifier.domainKey?.toInteger()

        List<PersonEmail> personEmails = PersonEmail.fetchByPidmAndStatus(pidm, "A")

        // Make emailType "Institution" as preferred
        PersonEmail instEmail = personEmails.find {
            it.emailAddress == i_success_emailAddress_institution
        }
        instEmail.preferredIndicator = true
        instEmail.save(flush: true, failOnError: true)

        content = updatePersonWithPreferredEmailAddress(personGuid)
        // emailType "Preferred" is ignored in 9.4.0.1 SR.  So removing it from request.
        content.emails.removeAll { it.emailType.trim() == i_success_emailType_preferred }

        // Change "Institution" email address in the request
        def obj = content.emails.find { it.emailType.trim() == i_success_emailType_institution }
        obj.guid = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"
        obj.emailAddress = "hoby@test.com"

        // Update person
        def o_person_update = personCompositeService.update(content)

        assertNotNull o_person_update
        assertEquals personGuid, o_person_update.guid
        assertEquals 3, o_person_update.emails?.size()
        assertEquals i_success_guid_personal, o_person_update.emails[0].guid
        assertEquals i_success_emailType_personal, o_person_update.emails[0].emailType
        assertEquals i_success_emailAddress_personal, o_person_update.emails[0].emailAddress
        assertEquals obj.guid, o_person_update.emails[1].guid
        assertEquals i_success_emailType_institution, o_person_update.emails[1].emailType
        assertEquals obj.emailAddress, o_person_update.emails[1].emailAddress
        assertEquals obj.guid, o_person_update.emails[2].guid
        assertEquals i_success_emailType_preferred, o_person_update.emails[2].emailType
        assertEquals obj.emailAddress, o_person_update.emails[2].emailAddress
    }

    //PUT- person update API
    @Test
    void testUpdatePersonCredentialWithExistingSSN() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.v3+json")
        request.addHeader("Accept", "application/vnd.hedtech.integration.v3+json")

        def pidm = PersonUtility.getPerson(i_success_credential_id4)?.pidm

        assertNotNull pidm
        String guid = GlobalUniqueIdentifier.findByLdmNameAndDomainKey('persons', pidm)?.guid
        Map params = updatePersonWithModifiedExistingSSN(guid)
        def o_success_person_ssn_update = personCompositeService.update(params)

        assertNotNull o_success_person_ssn_update
        def o_ssn_update = o_success_person_ssn_update.credentials.find {
            it.credentialType == "Social Security Number" || it.credentialType == "Social Insurance Number"
        }

        assertNotNull o_ssn_update
        assertEquals i_update_credential_type, o_ssn_update.credentialType
        IntegrationConfiguration personSSN = IntegrationConfiguration.fetchByProcessCodeAndSettingName(PROCESS_CODE, PERSON_UPDATESSN)

        def ssn = PersonBasicPersonBase.fetchByPidm(pidm).ssn
        if (personSSN?.value == 'Y' || (personSSN?.value == 'N' && ssn == null)) {
            assertEquals i_update_credential_id, o_ssn_update.credentialId
        } else {
            assertEquals o_ssn_update.credentialId, ssn
        }
    }

    //PUT- person update API
    @Test
    void testUpdatePersonWithAlternateNameHavingBirthNameType() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.v3+json")
        request.addHeader("Accept", "application/vnd.hedtech.integration.v3+json")

        Map content = newPersonWithPreferredEmailRequest()

        def o_success_person_create = personCompositeService.create(content)

        assertNotNull o_success_person_create
        assertNotNull o_success_person_create.guid
        def o_birth_name_create = o_success_person_create.names.find { it.nameType == "Birth" }
        assertNull o_birth_name_create

        //update the email records
        Map params = [id   : o_success_person_create.guid,
                      names: [[lastName: i_success_alternate_last_name, middleName: i_success_alternate_middle_name, firstName: i_success_alternate_first_name, nameType: i_success_alternate_birth_name_type]]
        ]

        def o_person_update = personCompositeService.update(params)

        def o_birth_name_update = o_person_update.names.find { it.nameType == "Birth" }

        assertNotNull o_birth_name_update
        assertEquals i_success_alternate_first_name, o_birth_name_update.firstName
        assertEquals i_success_alternate_middle_name, o_birth_name_update.middleName
        assertEquals i_success_alternate_last_name, o_birth_name_update.lastName
        assertEquals i_success_alternate_birth_name_type, o_birth_name_update.nameType
    }

    //PUT- person update API
    @Test
    void testUpdatePersonWithUSethnicity() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.v3+json")
        request.addHeader("Accept", "application/vnd.hedtech.integration.v3+json")

        Map content = newPersonWithUSEthnicity()

        def o_success_person_create = personCompositeService.create(content)

        assertNotNull o_success_person_create
        assertNotNull o_success_person_create.guid
        assertEquals content.ethnicityDetail.guid, o_success_person_create.ethnicityDetail.guid

        String ethnicityGuid = GlobalUniqueIdentifier.findByLdmNameAndDomainId('ethnicities-us', 2)?.guid
        Map content1 = [id: o_success_person_create.guid, ethnicityDetail: [guid: ethnicityGuid]]
        def o_success_person_update = personCompositeService.update(content1)

        assertNotNull o_success_person_update
        assertNotNull o_success_person_update.guid
        assertEquals o_success_person_create.guid, o_success_person_update.guid
        assertEquals content1.ethnicityDetail.guid, o_success_person_update.ethnicityDetail.guid
    }

    //Filter on CredentialId and Credential Type
    @Test
    public void testCredentialsFilterOnPersonV3() {
        setAcceptHeader("application/vnd.hedtech.integration.v3+json")

        def params = [:]
        def decorators = [:]

        params.put("credentialType", i_success_credential_type4_filter)
        params.put("credentialId", i_success_credential_filter);
        params.put("role", "Faculty")
        decorators = personCompositeService.list(params)
        assert decorators.size() > 0
        assertEquals 1, decorators.size()

        decorators.clear()
        params.clear()

        params.put("credentialType", i_success_credential_type4_filter)
        params.put("credentialId", i_success_credential_filter);
        params.put("role", "Student")
        decorators = personCompositeService.list(params);
        assert decorators.size() > 0
        assertEquals 1, decorators.size()

        decorators.clear()
        params.clear()
    }


    private def createPersonBasicPersonBase() {
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        String idSql = """select gb_common.f_generate_id bannerId, gb_common.f_generate_pidm pidm from dual """
        def bannerValues = sql.firstRow(idSql)
        def ibannerId = bannerValues.bannerId
        def ipidm = bannerValues.pidm
        def person = new PersonIdentificationName(
                pidm: ipidm,
                bannerId: ibannerId,
                lastName: "TTTTT",
                firstName: "TTTTT",
                middleName: "TTTTT",
                changeIndicator: null,
                entityIndicator: "P"
        )
        person.save(flush: true, failOnError: true)
        assert person.id

        def personBasicPersonBase = new PersonBasicPersonBase(
                pidm: ipidm,
                ssn: i_success_ssn,
                birthDate: i_success_birthDate,
                sex: i_success_sex,
                confidIndicator: i_success_confidIndicator,
                deadIndicator: i_success_deadIndicator,
                vetcFileNumber: i_success_vetcFileNumber,
                legalName: i_success_legalName,
                preferenceFirstName: i_success_preferenceFirstName,
                namePrefix: i_success_namePrefix,
                nameSuffix: i_success_nameSuffix,
                veraIndicator: i_success_veraIndicator,
                citizenshipIndicator: i_success_citizenshipIndicator,
                deadDate: i_success_deadDate,
                hair: i_success_hair,
                eyeColor: i_success_eyeColor,
                cityBirth: i_success_cityBirth,
                driverLicense: i_success_driverLicense,
                height: i_success_height,
                weight: i_success_weight,
                sdvetIndicator: i_success_sdvetIndicator,
                licenseIssuedDate: i_success_licenseIssuedDate,
                licenseExpiresDate: i_success_licenseExpiresDate,
                incarcerationIndicator: i_success_incarcerationIndicator,
                itin: i_success_itin,
                activeDutySeprDate: i_success_activeDutySeprDate,
                ethnic: i_success_ethnic,
                confirmedRe: i_success_confirmedRe,
                confirmedReDate: i_success_confirmedReDate,
                armedServiceMedalVetIndicator: i_success_armedServiceMedalVetIndicator,
                legacy: i_success_legacy,
                ethnicity: i_success_ethnicity,
                maritalStatus: i_success_maritalStatus,
                religion: i_success_religion,
                citizenType: i_success_citizenType,
                stateBirth: i_success_stateBirth,
                stateDriver: i_success_stateDriver,
                nationDriver: i_success_nationDriver,
                unitOfMeasureHeight: UnitOfMeasure.findByCode("LB"),
                unitOfMeasureWeight: UnitOfMeasure.findByCode("LB")
        )

        def map = [domainModel: personBasicPersonBase]
        personBasicPersonBase = personBasicPersonBaseService.create(map)
        return personBasicPersonBase
    }


    private Map getPersonWithFirstNameChangeRequest(personIdentificationNameCurrent, guid) {
        Map params = [id   : guid,
                      names: [[lastName: personIdentificationNameCurrent.lastName, middleName: personIdentificationNameCurrent.middleName, firstName: 'CCCCCC', nameType: 'Primary']]
        ]
        return params
    }


    private Map getPersonWithLastNameChangeRequest(personIdentificationNameCurrent, guid) {
        Map params = [id   : guid,
                      names: [[lastName: 'CCCCCC', middleName: personIdentificationNameCurrent.middleName, firstName: personIdentificationNameCurrent.firstName, nameType: 'Primary']]
        ]
        return params
    }


    private Map getPersonWithIdChangeRequest(personIdentificationNameCurrent, guid) {
        Map params = [id         : guid,
                      names      : [[lastName: personIdentificationNameCurrent.lastName, middleName: personIdentificationNameCurrent.middleName, firstName: personIdentificationNameCurrent.firstName, nameType: 'Primary']],
                      credentials: [[credentialType: 'Banner ID', credentialId: 'CHANGED']]
        ]
        return params
    }


    private Map getPersonWithPersonBasicPersonBaseChangeRequest(personIdentificationNameCurrent, guid) {
        Map params = [id   : guid,
                      names: [[lastName: personIdentificationNameCurrent.lastName, middleName: personIdentificationNameCurrent.middleName, firstName: personIdentificationNameCurrent.firstName, nameType: 'Primary', namePrefix: 'CCCCC', nameSuffix: 'CCCCC', preferenceFirstName: 'CCCCC']],
                      sex  : 'Female'

        ]
        return params
    }


    private Map getPersonWithNewAdressRequest(personIdentificationNameCurrent, guid) {
        Map params = [id       : guid,
                      names    : [[lastName: personIdentificationNameCurrent.lastName, middleName: personIdentificationNameCurrent.middleName, firstName: personIdentificationNameCurrent.firstName, nameType: 'Primary', namePrefix: 'CCCCC', nameSuffix: 'CCCCC', preferenceFirstName: 'CCCCC']],
                      sex      : 'Male',
                      addresses: [[addressType: 'Mailing', city: 'Southeastern', state: 'CA', streetLine1: '5890 139th Ave', streetLine2: 'New street', streetLine3: 'Wilson Garden', zip: '19398'], [addressType: 'Home', city: 'Pavo', state: 'GA', streetLine1: '123 Main Line', zip: '31778']]
        ]
        return params
    }


    private Map getPersonWithModifiedAdressRequest(personIdentificationNameCurrent, guid) {
        Map params = [id       : guid,
                      names    : [[lastName: personIdentificationNameCurrent.lastName, middleName: personIdentificationNameCurrent.middleName, firstName: personIdentificationNameCurrent.firstName, nameType: 'Primary', namePrefix: 'CCCCC', nameSuffix: 'CCCCC', preferenceFirstName: 'CCCCC']],
                      sex      : 'Male',
                      addresses: [[addressType: 'Mailing', city: 'Pavo', state: 'GA', streetLine1: '123 Main Line', streetLine2: 'New street', streetLine3: 'Wilson Garden', zip: '31778'], [addressType: 'Home', city: 'Southeastern', state: 'CA', streetLine1: '5890 139th Ave', zip: '19398', county: "Chester", nation: [code: "CA", value: "Canada"]]]
        ]
        return params
    }


    private Map getParamsWithReqiuredFields() {
        return [
                action     : [POST: "list"],
                names      : [[
                                      nameType : "Primary",
                                      firstName: "Mark",
                                      lastName : "Mccallon"
                              ]],
                dateOfBirth: "1973-12-30"
        ]
    }


    private Map getPersonWithNewPhonesRequest(personIdentificationNameCurrent, guid) {
        Map params = [id    : guid,
                      names : [[lastName: personIdentificationNameCurrent.lastName, middleName: personIdentificationNameCurrent.middleName, firstName: personIdentificationNameCurrent.firstName, nameType: 'Primary', namePrefix: 'TTTTT', nameSuffix: 'TTTTT', preferenceFirstName: 'TTTTT']],
                      sex   : 'Male',
                      phones: [[phoneNumber: '6107435302', phoneType: 'Mobile'], [phoneNumber: '2297795715', phoneType: 'Home']]
        ]
        return params
    }


    private Map getPersonWithModifiedPhonesRequest(personIdentificationNameCurrent, guid) {
        Map params = [id    : guid,
                      names : [[lastName: personIdentificationNameCurrent.lastName, middleName: personIdentificationNameCurrent.middleName, firstName: personIdentificationNameCurrent.firstName, nameType: 'Primary', namePrefix: 'TTTTT', nameSuffix: 'TTTTT', preferenceFirstName: 'TTTTT']],
                      sex   : 'Male',
                      phones: [[phoneNumber: '6107435333', phoneType: 'Mobile'], [phoneNumber: '2297795777', phoneType: 'Home']]
        ]
        return params
    }


    private Map getPersonWithNewRacesRequest(personIdentificationNameCurrent, guid, races) {
        Map params = [id   : guid,
                      names: [[lastName: personIdentificationNameCurrent.lastName, middleName: personIdentificationNameCurrent.middleName, firstName: personIdentificationNameCurrent.firstName, nameType: 'Primary', namePrefix: 'TTTTT', nameSuffix: 'TTTTT', preferenceFirstName: 'TTTTT']],
                      sex  : 'Male',
                      races: [[guid: races[0].guid]]
        ]
        return params
    }


    private Map getPersonWithModifiedRacesRequest(personIdentificationNameCurrent, guid, races) {
        Map params = [id   : guid,
                      names: [[lastName: personIdentificationNameCurrent.lastName, middleName: personIdentificationNameCurrent.middleName, firstName: personIdentificationNameCurrent.firstName, nameType: 'Primary', namePrefix: 'TTTTT', nameSuffix: 'TTTTT', preferenceFirstName: 'TTTTT']],
                      sex  : 'Male',
                      races: [[guid: races[0].guid], [guid: races[1].guid]]
        ]
        return params
    }


    private Map getPersonWithNewEthniciiesyRequest(personIdentificationNameCurrent, guid, ethnicity) {
        Map params = [id             : guid,
                      names          : [[lastName: personIdentificationNameCurrent.lastName, middleName: personIdentificationNameCurrent.middleName, firstName: personIdentificationNameCurrent.firstName, nameType: 'Primary', namePrefix: 'TTTTT', nameSuffix: 'TTTTT', preferenceFirstName: 'TTTTT']],
                      sex            : 'Male',
                      ethnicityDetail: [guid: ethnicity.guid]
        ]
        return params
    }


    private Map getPersonWithModifiedEthniciiesyRequest(personIdentificationNameCurrent, guid, ethnicity) {
        Map params = [id             : guid,
                      names          : [[lastName: personIdentificationNameCurrent.lastName, middleName: personIdentificationNameCurrent.middleName, firstName: personIdentificationNameCurrent.firstName, nameType: 'Primary', namePrefix: 'TTTTT', nameSuffix: 'TTTTT', preferenceFirstName: 'TTTTT']],
                      sex            : 'Male',
                      ethnicityDetail: [guid: ethnicity.guid]
        ]
        return params
    }


    private Map newPersonWithAddressRequest() {
        String ethnicityGuid = GlobalUniqueIdentifier.findByLdmNameAndDomainKey('ethnicities', i_success_ethnicity?.code)?.guid
        String maritalStatusGuid = GlobalUniqueIdentifier.findByLdmNameAndDomainKey('marital-status', i_success_maritalStatus?.code)?.guid

        Map params = [names              : [[lastName: i_success_last_name, middleName: i_success_middle_name, firstName: i_success_first_name, nameType: i_success_primary_name_type, namePrefix: i_success_namePrefix, nameSuffix: i_success_nameSuffix, preferenceFirstName: i_success_preferenceFirstName]],
                      addresses          : [[addressType: i_success_address_type_1, city: i_success_city, state: i_success_state, streetLine1: i_success_street_line1, zip: i_success_zip, county: "Chester", nation: [code: "CA", value: "Canada"]], [addressType: i_success_address_type_2, city: i_success_city, streetLine1: i_success_street_line1]],
                      credentials        : [[
                                                    credentialType: "Social Security Number",
                                                    credentialId  : "111111111"
                                            ],
                                            [
                                                    "credentialType": "Elevate ID",
                                                    "credentialId"  : "E11111111"
                                            ]],
                      ethnicityDetail    : [guid: ethnicityGuid],
                      maritalStatusDetail: [guid: maritalStatusGuid]
        ]

        return params
    }


    private Map newPersonWithPreferredEmailRequest() {
        Map params = [names : [[lastName: i_success_last_name, middleName: i_success_middle_name, firstName: i_success_first_name, nameType: i_success_primary_name_type, namePrefix: i_success_namePrefix, nameSuffix: i_success_nameSuffix, preferenceFirstName: i_success_preferenceFirstName]],
                      emails: [[guid: i_success_guid_personal, emailAddress: i_success_emailAddress_personal, emailType: i_success_emailType_personal], [guid: i_success_guid_institution, emailAddress: i_success_emailAddress_institution, emailType: i_success_emailType_institution], [emailAddress: i_success_emailAddress_personal, emailType: i_success_emailType_preferred]]
        ]

        return params
    }


    private Map updatePersonWithPreferredEmailAddress(String guid) {
        Map params = [id    : guid,
                      emails: [[guid: i_success_guid_personal, emailAddress: i_success_emailAddress_personal, emailType: i_success_emailType_personal], [guid: i_success_guid_institution, emailAddress: i_success_emailAddress_institution, emailType: i_success_emailType_institution], [emailAddress: i_success_emailAddress_personal, emailType: i_success_emailType_preferred]]
        ]

        return params
    }


    private Map updatePersonWithModifiedExistingSSN(guid) {
        Map params = [id         : guid,
                      credentials: [[credentialType: i_update_credential_type, credentialId: i_update_credential_id]],
        ]
        return params
    }


    private Map getParamsForPersonFilter() {
        return [
                action: [POST: "list"],
        ]
    }


    private Map newPersonWithAlternateNameHavingBirthNameType() {
        Map params = [names: [[lastName: i_success_last_name, middleName: i_success_middle_name, firstName: i_success_first_name, nameType: i_success_primary_name_type, namePrefix: i_success_namePrefix, nameSuffix: i_success_nameSuffix, preferenceFirstName: i_success_preferenceFirstName], [lastName: i_success_alternate_last_name, middleName: i_success_alternate_middle_name, firstName: i_success_alternate_first_name, nameType: i_success_alternate_birth_name_type]]
        ]
        return params
    }


    private Map getPersonBirthNameTypeFields() {
        return [
                action: [POST: "list"],
                names : [[
                                 nameType : i_success_alternate_birth_name_type,
                                 firstName: i_success_alternate_first_name,
                                 lastName : i_success_alternate_last_name
                         ]]
        ]
    }


    private Map newPersonWithUSEthnicity() {
        String ethnicityGuid = GlobalUniqueIdentifier.findByLdmNameAndDomainId('ethnicities-us', 1)?.guid
        Map params = [names          : [[lastName: i_success_last_name, middleName: i_success_middle_name, firstName: i_success_first_name, nameType: i_success_primary_name_type, namePrefix: i_success_namePrefix, nameSuffix: i_success_nameSuffix, preferenceFirstName: i_success_preferenceFirstName]],
                      ethnicityDetail: [guid: ethnicityGuid]
        ]

        return params
    }


    private Map newPersonRequestWithInvalidShortPhoneNumber() {
        Map params = [names : [[lastName: i_success_last_name, middleName: i_success_middle_name, firstName: i_success_first_name, nameType: i_success_primary_name_type, namePrefix: i_success_namePrefix, nameSuffix: i_success_nameSuffix, preferenceFirstName: i_success_preferenceFirstName]],
                      phones: [[phoneExtension: i_success_phone_extension, phoneNumber: i_succes_invalid_phone_number_short1, phoneType: i_success_phone_type_mobile], [phoneExtension: i_success_phone_extension, phoneNumber: i_succes_invalid_phone_number_short2, phoneType: i_success_phone_type_home], [phoneExtension: i_success_phone_extension, phoneNumber: i_succes_invalid_phone_number_short3, phoneType: i_success_phone_type_work], [phoneExtension: i_success_phone_extension, phoneNumber: i_succes_invalid_phone_number_short4, phoneType: i_success_phone_type_residence]]
        ]
        return params
    }


    private Map newPersonRequestWithInvalidLongPhoneNumber() {
        Map params = [names : [[lastName: i_success_last_name, middleName: i_success_middle_name, firstName: i_success_first_name, nameType: i_success_primary_name_type, namePrefix: i_success_namePrefix, nameSuffix: i_success_nameSuffix, preferenceFirstName: i_success_preferenceFirstName]],
                      phones: [[phoneExtension: i_success_phone_extension, phoneNumber: i_succes_invalid_phone_number_long1, phoneType: i_success_phone_type_mobile], [phoneExtension: i_success_phone_extension, phoneNumber: i_succes_invalid_phone_number_long2, phoneType: i_success_phone_type_home], [phoneExtension: i_success_phone_extension, phoneNumber: i_succes_invalid_phone_number_long3, phoneType: i_success_phone_type_work], [phoneExtension: i_success_phone_extension, phoneNumber: i_succes_invalid_phone_number_long4, phoneType: i_success_phone_type_residence]]
        ]
        return params
    }

}
