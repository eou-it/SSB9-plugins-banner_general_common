/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.person.ldm

import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.common.GeneralCommonConstants
import net.hedtech.banner.general.commonmatching.CommonMatchingSourceRule
import net.hedtech.banner.general.overall.IntegrationConfiguration
import net.hedtech.banner.general.overall.ldm.GlobalUniqueIdentifier
import net.hedtech.banner.general.overall.ldm.GlobalUniqueIdentifierService
import net.hedtech.banner.general.overall.ldm.LdmService
import net.hedtech.banner.general.person.*
import net.hedtech.banner.general.person.ldm.v1.RoleDetail
import net.hedtech.banner.general.person.ldm.v6.PersonAddressDecorator
import net.hedtech.banner.general.person.ldm.v6.PersonV6
import net.hedtech.banner.general.system.*
import net.hedtech.banner.general.system.ldm.*
import net.hedtech.banner.general.utility.DateConvertHelperService
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
import org.junit.Before
import org.junit.Test

class PersonV6CompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    PersonV6CompositeService personV6CompositeService
    PersonCompositeService personCompositeService
    UserRoleCompositeService userRoleCompositeService
    GlobalUniqueIdentifierService globalUniqueIdentifierService
    CitizenshipStatusCompositeService citizenshipStatusCompositeService
    VisaTypeCompositeService visaTypeCompositeService
    ReligionCompositeService religionCompositeService
    NameTypeService nameTypeService
    PersonNameTypeCompositeService personNameTypeCompositeService
    PersonIdentificationNameAlternateService personIdentificationNameAlternateService
    static final String BANNER_ID_WITH_TYPE_BIRTH = 'HOSR24787'
    String i_succes_person_banner_id = 'HOSFE2000'
    GlobalUniqueIdentifier personGlobalUniqueIdentifier
    String raceGlobalUniqueIdentifier1
    String raceGlobalUniqueIdentifier2
    String religionGlobalUniqueIdentifier1
    String religionGlobalUniqueIdentifier2
    String ethnicityGlobalUniqueIdentifier1
    String ethnicityGlobalUniqueIdentifier2
    String citizenshipStatusGlobalUniqueIdentifier1
    String citizenshipStatusGlobalUniqueIdentifier2
    String citizenshipStatusCategory1 = 'citizen'
    String emailAddress1 = 'john@gmail.com'
    String emailAddress2 = 'john1@gmail.com'
    String citizenshipStatusCategory2 = 'nonCitizen'
    String countryOfBirth1 = 'GBR'
    String countryOfBirth2 = 'USA'

    PersonIdentificationName personIdentificationName
    PersonAddressService personAddressService
    PersonAddressAdditionalPropertyService personAddressAdditionalPropertyService
    PersonBasicPersonBaseService personBasicPersonBaseService



    def i_success_first_name = "Mark"
    def i_success_middle_name = "TR"
    def i_success_last_name = "Mccallon"
    def i_success_primary_name_type = "personal"
    def i_success_namePrefix = "TTTTT"
    def i_success_nameSuffix = "TTTTT"
    def i_success_birth_first_name = "John"
    def i_success_birth_middle_name = "A"
    def i_success_birth_last_name = "Jorden"
    def i_success_birth_name_type = "birth"
    def i_success_legal_first_name = "marry"
    def i_success_legal_middle_name = "A"
    def i_success_legal_last_name = "Jorden"
    def i_success_legal_name_type = "legal"
    def i_success_full_name = "Test"
    def i_success_surnamePrefix = "Van"

    def i_success_bannerId = "TestBann"
    def i_success_ssn = "1234"
    def i_success_sin = "1111"
    def i_success_elevateId = "12345"

    def i_success_privacy_satus = "unrestricted"
    def i_success_update_privacy_satus = "restricted"

    def i_success_maritalStatus1 = "single"
    def i_success_maritalStatus2 = "married"

    def i_success_legacy
    def i_success_ethnicity
    def i_success_maritalStatus
    def i_success_religion
    def i_success_citizenType
    def i_success_stateBirth
    def i_success_stateDriver
    def i_success_nationDriver
    def i_success_pidm
    def i_success_birthDate = new Date()
    def i_success_birthDate_String = "1972-12-10"
    def i_success_sex = "M"
    def i_success_confidIndicator = "Y"
    def i_success_deadIndicator = "Y"
    def i_success_vetcFileNumber = "TTTTT"
    def i_success_legalName = "Levitch"
    def i_success_preferenceFirstName = "Happy"
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


    void initializeTestDataForReferences() {
        personIdentificationName = PersonUtility.getPerson(i_succes_person_banner_id)
        personGlobalUniqueIdentifier = GlobalUniqueIdentifier.fetchByDomainKeyAndLdmName(personIdentificationName.pidm.toString(), 'persons')
        raceGlobalUniqueIdentifier1 = GlobalUniqueIdentifier.findByLdmNameAndDomainKey('races', 'ASI')?.guid
        raceGlobalUniqueIdentifier2 = GlobalUniqueIdentifier.findByLdmNameAndDomainKey('races', 'WHT')?.guid
        religionGlobalUniqueIdentifier1 = GlobalUniqueIdentifier.findByLdmNameAndDomainKey('religions', 'CH')?.guid
        religionGlobalUniqueIdentifier2 = GlobalUniqueIdentifier.findByLdmNameAndDomainKey('religions', 'CA')?.guid
        ethnicityGlobalUniqueIdentifier1 = GlobalUniqueIdentifier.findByLdmNameAndDomainId('ethnicities-us',1)?.guid
        ethnicityGlobalUniqueIdentifier2 = GlobalUniqueIdentifier.findByLdmNameAndDomainId('ethnicities-us',2)?.guid
        citizenshipStatusGlobalUniqueIdentifier1 = GlobalUniqueIdentifier.findByLdmNameAndDomainKey('citizenship-statuses','Y')?.guid
        citizenshipStatusGlobalUniqueIdentifier2 = GlobalUniqueIdentifier.findByLdmNameAndDomainKey('citizenship-statuses','CH')?.guid

       // countryBirth = Nation.

    }


    @Test
    void testListPersonV6InvalidRoleRequired() {
        setAcceptHeader("application/vnd.hedtech.integration.v6+json")

        try {
            personV6CompositeService.listApi(params)
            fail('Role is Required')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'role.required'
        }
    }


    @Test
    void testListPersonV6InvalidForRoleFaculty() {
        setAcceptHeader("application/vnd.hedtech.integration.v6+json")

        Map params = [role: "faculty"]

        try {
            personV6CompositeService.listApi(params)
            fail('Invalid role for Person V6')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'role.supported.v6'
        }
    }


    @Test
    void testListPersonValidV6ForRoleInstructor() {
        setAcceptHeader("application/vnd.hedtech.integration.v6+json")

        Map params = [role: "instructor"]
        def o_success_persons = personV6CompositeService.listApi(params)

        assertNotNull o_success_persons
        assertFalse o_success_persons.isEmpty()
        assertNotNull o_success_persons[0].guid

        List personRoles = getPersonRoles(o_success_persons[0])
        assertTrue personRoles.contains('Faculty')
    }


    @Test
    void testListPersonValidV6ForRoleStudent() {
        setAcceptHeader("application/vnd.hedtech.integration.v6+json")

        Map params = [role: "student"]
        def o_success_persons = personV6CompositeService.listApi(params)

        assertNotNull o_success_persons
        assertFalse o_success_persons.isEmpty()
        assertNotNull o_success_persons[0].guid

        List personRoles = getPersonRoles(o_success_persons[0])
        assertTrue personRoles.contains('Student')
    }


    @Test
    void testListPersonValidV6ForRoleAlumni() {
        setAcceptHeader("application/vnd.hedtech.integration.v6+json")

        Map params = [role: "alumni"]
        def o_success_persons = personV6CompositeService.listApi(params)

        assertNotNull o_success_persons
        assertFalse o_success_persons.isEmpty()
        assertNotNull o_success_persons[0].guid

        assertTrue o_success_persons[0].roles.role.contains("alumni")
    }


    @Test
    void testListPersonValidV6ForRoleEmployees() {
        setAcceptHeader("application/vnd.hedtech.integration.v6+json")

        Map params = [role: "employee"]
        def o_success_persons = personV6CompositeService.listApi(params)

        assertNotNull o_success_persons
        assertFalse o_success_persons.isEmpty()
        assertNotNull o_success_persons[0].guid
        assertTrue o_success_persons[0].roles.role.contains("employee")

    }


    @Test
    void testListPersonValidV6ForRoleVendor() {
        setAcceptHeader("application/vnd.hedtech.integration.v6+json")

        Map params = [role: "vendor"]
        PersonV6 o_success_persons = personV6CompositeService.listApi(params).get(0)

        assertNotNull o_success_persons
        assertNotNull o_success_persons.guid

        List<RoleDetail> personRoles = o_success_persons.roles
        assertNotNull personRoles
        List vendorRoles = []
        personRoles.each { roles ->
            vendorRoles.add(roles.role)
        }
        assertNotNull vendorRoles
        assertTrue vendorRoles.contains('vendor')

    }


    @Test
    void testListPersonValidV6ForAdvisorRole() {
        setAcceptHeader("application/vnd.hedtech.integration.v6+json")

        Map params = [role: RoleName.ADVISOR.versionToEnumMap["v6"]]
        List<PersonV6> o_success_persons = personV6CompositeService.listApi(params)

        assertNotNull o_success_persons
        assertFalse o_success_persons.isEmpty()

        o_success_persons.each {
            assertNotNull it.guid
            assertTrue it.roles.role.contains(params.role)
        }

    }


    @Test
    void testListPersonValidV6ForProspectiveStudentRole() {
        setAcceptHeader("application/vnd.hedtech.integration.v6+json")

        Map params = [role: RoleName.PROSPECTIVE_STUDENT.versionToEnumMap["v6"]]
        List<PersonV6> o_success_persons = personV6CompositeService.listApi(params)

        assertNotNull o_success_persons
        assertFalse o_success_persons.isEmpty()

        o_success_persons.each {
            assertNotNull it.guid
            assertTrue it.roles.role.contains(params.role)
        }

    }


    @Test
    void testListapiWithRoleStudentAndLargePagination() {
        def params1 = [role: "student"]
        Map resultCount = userRoleCompositeService.fetchAllByRole(params1)
        assertTrue resultCount.count > 500

        def params = [role: "student", max: '2000', offset: '100']

        def persons = personV6CompositeService.listApi(params)
        // verify pagination capped at 500
        assertEquals 500, persons.size()
        /*
        persons.each {
            it.roles.role == "Student"
        }
        */
    }


    @Test
    void testListapiWithRoleStudentAndPaginationMaxTen() {
        def params = [role: "student", max: '10', offset: '5']

        def persons = personV6CompositeService.listApi(params)
        assertNotNull persons
        assertEquals params.max, persons.size().toString()
        /*
        persons.each {
            it.roles.role == "Student"
        }
        */
    }


    @Test
    void testGetValid() {
        def params = [role: "student", max: '5']

        def persons = personV6CompositeService.listApi(params)
        assertNotNull persons
        assertEquals params.max, persons.size().toString()

        def person = personV6CompositeService.get(persons[0].guid)
        assertNotNull person
        assertEquals persons[0].guid, person.guid

        String pidm = (globalUniqueIdentifierService.fetchByLdmNameAndGuid(GeneralCommonConstants.PERSONS_LDM_NAME, person.guid)).domainKey
        def personBase = PersonBasicPersonBase.fetchByPidm(Integer.parseInt(pidm))
        if (!personBase) {
            def personBasicPersonBase = new PersonBasicPersonBase(pidm: Integer.parseInt(pidm), armedServiceMedalVetIndicator: true)
            personBasicPersonBase.save(failOnError: true, flush: true)
            personBase = PersonBasicPersonBase.fetchByPidm(Integer.parseInt(pidm))
        }

        if (!person.citizenshipStatus) {
            def citizenTypeObj = CitizenType.findAll()[0]
            personBase.citizenType = citizenTypeObj
            person = personV6CompositeService.get(persons[0].guid)
        }

        def guids1 = citizenshipStatusCompositeService.fetchGUIDs([personBase.citizenType.code])
        def citizenShipStatus = citizenshipStatusCompositeService.get(guids1.values()[0])
        assertEquals guids1.values()[0], person.citizenshipStatus.detail.id
        assertEquals citizenShipStatus.category, person.citizenshipStatus.category

        if (!person.religion) {
            def religionObj = Religion.findAll()[0]
            personBase.religion = religionObj
            person = personV6CompositeService.get(persons[0].guid)
        }

        def religionGuids = religionCompositeService.fetchGUIDs([personBase.religion.code])
        def religion = religionCompositeService.get(religionGuids.values()[0])
        assertEquals religionGuids.values()[0], person.religion.id
        assertEquals religion.id, person.religion.id
    }


    @Test
    void testListSortByFirstNameASC() {
        def params = [role: "student", sort: "firstName", order: "ASC"]

        def persons = personV6CompositeService.listApi(params)
        assertNotNull persons
        assertListIsSortedOnField(persons, params.sort, params.order)
    }


    @Test
    void testListNamesWithTypePersonal() {
        def params = [role: "student"]

        def persons = personV6CompositeService.listApi(params)
        assertNotNull persons

        def personDetails = findOnePersonWithGivenBannerID(persons)
        def personToTest = persons.find { person ->
            person.guid == personDetails.guid
        }
        assertNotNull personToTest
        assertEquals personToTest.guid, personDetails.guid
        def personName = personToTest.names.find { categoryType ->
            categoryType.type.category == 'personal'
        }
        assertEquals personName.type.category, "personal"
        //assertEquals personName.fullName, getPersonFullName(personDetails.personBase)
        assertEquals personName.title, personDetails.personBase.namePrefix
        assertEquals personName.firstName, personDetails.personIdentificationName.firstName
        assertEquals personName.middleName, personDetails.personIdentificationName.middleName
        assertEquals personName.lastNamePrefix, personDetails.personIdentificationName.surnamePrefix
        assertEquals personName.lastName, personDetails.personIdentificationName.lastName
        assertEquals personName.pedigree, personDetails.personBase.nameSuffix
    }


    @Test
    void testGetNamesWithTypeBirth() {
        def personIdentificationName = PersonUtility.getPerson(BANNER_ID_WITH_TYPE_BIRTH)
        assertNotNull personIdentificationName

        GlobalUniqueIdentifier personGUID = GlobalUniqueIdentifier.fetchByLdmNameAndDomainKey(GeneralCommonConstants.PERSONS_LDM_NAME, String.valueOf(personIdentificationName.pidm))[0]
        def person = personV6CompositeService.get(personGUID.guid)
        assertNotNull person

        def birthNameType = getPersonBirthNameTypeByPidm([personIdentificationName.pidm])
        //assertNotNull birthNameType
        //TODO: validate all attributes with returned birthname of person
    }


    @Test
    void testList_DuplicateCheck_PersonalLastNameSearch() {
        setContentTypeHeader("application/vnd.hedtech.integration.v6+json")
        setAcceptHeader("application/vnd.hedtech.integration.v6+json")

        // Common Matching Rule
        IntegrationConfiguration intConf = IntegrationConfiguration.fetchByProcessCodeAndSettingName("HEDM", "PERSON.MATCHRULE")
        String commonMatchingSourceCode = intConf?.value
        CommonMatchingSource commonMatchingSource = CommonMatchingSource.findByCode(commonMatchingSourceCode)
        assertNotNull commonMatchingSource
        List<CommonMatchingSourceRule> commonMatchingSourceRules = CommonMatchingSourceRule.findAllByCommonMatchingSource(commonMatchingSource)
        assertTrue commonMatchingSourceRules.size() > 0
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def gorcmsrRows = sql.rows("select gorcmsr_column_name from gorcmsr where  gorcmsr_cmsc_code = ?", [commonMatchingSource.code])
        assertTrue gorcmsrRows.size() > 0
        assertNotNull gorcmsrRows.find { it.GORCMSR_COLUMN_NAME == "SPRIDEN_SEARCH_LAST_NAME" }
        assertNotNull gorcmsrRows.find { it.GORCMSR_COLUMN_NAME == "SPRIDEN_SEARCH_FIRST_NAME" }
        assertNotNull gorcmsrRows.find { it.GORCMSR_COLUMN_NAME == "SPRIDEN_SEARCH_MI" }

        // Prepare QAPI duplicate check request
        PersonIdentificationNameCurrent personCurrent = PersonIdentificationNameCurrent.fetchByBannerId(BANNER_ID_WITH_TYPE_BIRTH)
        assertNotNull personCurrent
        assertEquals 'P', personCurrent.entityIndicator
        GlobalUniqueIdentifier globalUniqueIdentifier = globalUniqueIdentifierService.fetchByDomainKeyAndLdmName(personCurrent.pidm.toString(), GeneralCommonConstants.PERSONS_LDM_NAME)
        assertNotNull globalUniqueIdentifier
        Map content = [
                action: [POST: "list"],
                names : [[type: [category: NameTypeCategory.PERSONAL.versionToEnumMap["v6"]], firstName: personCurrent.firstName, lastName: personCurrent.lastName, middleName: personCurrent.middleName]],
                sort  : "lastName",
                order : "asc"
        ]

        // Call the service
        def requestProcessingResult = personV6CompositeService.listQApi(content)
        def response = personV6CompositeService.createPersonDataModels(content, requestProcessingResult)
        assertNotNull response
        assertTrue response.size() > 0
        def obj = response.find { it.guid == globalUniqueIdentifier.guid }
        assertNotNull obj
        def personalName = obj.names.find { it.type.category == NameTypeCategory.PERSONAL.versionToEnumMap["v6"] }
        assertNotNull personalName
        assertEquals personCurrent.lastName, personalName.lastName
        assertEquals personCurrent.firstName, personalName.firstName
        assertEquals personCurrent.middleName, personalName.middleName
    }


    @Test
    void testList_DuplicateCheck_BirthLastNameSearch() {
        setContentTypeHeader("application/vnd.hedtech.integration.v6+json")
        setAcceptHeader("application/vnd.hedtech.integration.v6+json")

        // Common Matching Rule
        IntegrationConfiguration intConf = IntegrationConfiguration.fetchByProcessCodeAndSettingName("HEDM", "PERSON.MATCHRULE")
        String commonMatchingSourceCode = intConf?.value
        CommonMatchingSource commonMatchingSource = CommonMatchingSource.findByCode(commonMatchingSourceCode)
        assertNotNull commonMatchingSource
        List<CommonMatchingSourceRule> commonMatchingSourceRules = CommonMatchingSourceRule.findAllByCommonMatchingSource(commonMatchingSource)
        assertTrue commonMatchingSourceRules.size() > 0
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def gorcmsrRows = sql.rows("select gorcmsr_column_name from gorcmsr where  gorcmsr_cmsc_code = ?", [commonMatchingSource.code])
        assertTrue gorcmsrRows.size() > 0
        assertNotNull gorcmsrRows.find { it.GORCMSR_COLUMN_NAME == "SPRIDEN_SEARCH_LAST_NAME" }
        assertNotNull gorcmsrRows.find { it.GORCMSR_COLUMN_NAME == "SPRIDEN_SEARCH_FIRST_NAME" }
        assertNotNull gorcmsrRows.find { it.GORCMSR_COLUMN_NAME == "SPRIDEN_SEARCH_MI" }

        // Prepare QAPI duplicate check request
        PersonIdentificationNameCurrent personCurrent = PersonIdentificationNameCurrent.fetchByBannerId(BANNER_ID_WITH_TYPE_BIRTH)
        assertNotNull personCurrent
        assertEquals 'P', personCurrent.entityIndicator

        def personIdentificationNameAlternate = new PersonIdentificationNameAlternate(
                pidm: personCurrent.pidm,
                bannerId: BANNER_ID_WITH_TYPE_BIRTH,
                changeIndicator: "N",
                lastName: personCurrent.lastName,
                firstName: personCurrent.firstName,
                middleName: personCurrent.middleName,
                entityIndicator: personCurrent.entityIndicator,
                nameType: personCurrent.nameType,
                lastModifiedBy: "grails"
        )


        personIdentificationNameAlternate.save(flush: true, failOnError: true)

        GlobalUniqueIdentifier globalUniqueIdentifier = globalUniqueIdentifierService.fetchByDomainKeyAndLdmName(personCurrent.pidm.toString(), GeneralCommonConstants.PERSONS_LDM_NAME)
        assertNotNull globalUniqueIdentifier
        PersonIdentificationNameAlternate personAlternate = PersonIdentificationNameAlternate.fetchAllByPidm(personCurrent.pidm).get(0)
        assertNotNull personAlternate
        Map content = [
                action: [POST: "list"],
                names : [[type: [category: NameTypeCategory.BIRTH.versionToEnumMap["v6"]], firstName: personAlternate.firstName, lastName: personAlternate.lastName, middleName: personAlternate.middleName]],
                sort  : "lastName",
                order : "asc"
        ]

        // Call the service
        def requestProcessingResult = personV6CompositeService.listQApi(content)
        def response = personV6CompositeService.createPersonDataModels(content, requestProcessingResult)
        assertNotNull response
        assertTrue response.size() > 0
        def obj = response.find { it.guid == globalUniqueIdentifier.guid }
        assertNotNull obj
        def birthName = obj.names.find { it.type.category == NameTypeCategory.BIRTH.versionToEnumMap["v6"] }
        assertNotNull birthName
        assertEquals personAlternate.lastName, birthName.lastName
        assertEquals personAlternate.firstName, birthName.firstName
        assertEquals personAlternate.middleName, birthName.middleName
    }


    @Test
    void testList_DuplicateCheck_BannerIdSsnSearch() {
        setContentTypeHeader("application/vnd.hedtech.integration.v6+json")
        setAcceptHeader("application/vnd.hedtech.integration.v6+json")

        // Common Matching Rule
        IntegrationConfiguration intConf = IntegrationConfiguration.fetchByProcessCodeAndSettingName("HEDM", "PERSON.MATCHRULE")
        String commonMatchingSourceCode = intConf?.value
        CommonMatchingSource commonMatchingSource = CommonMatchingSource.findByCode(commonMatchingSourceCode)
        assertNotNull commonMatchingSource
        List<CommonMatchingSourceRule> commonMatchingSourceRules = CommonMatchingSourceRule.findAllByCommonMatchingSource(commonMatchingSource)
        assertTrue commonMatchingSourceRules.size() > 0
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def gorcmsrRows = sql.rows("select gorcmsr_column_name from gorcmsr where  gorcmsr_cmsc_code = ?", [commonMatchingSource.code])
        assertTrue gorcmsrRows.size() > 0
        assertNotNull gorcmsrRows.find { it.GORCMSR_COLUMN_NAME == "SPRIDEN_ID" }
        assertNotNull gorcmsrRows.find { it.GORCMSR_COLUMN_NAME == "SPBPERS_SSN" }

        // Prepare QAPI duplicate check request
        PersonIdentificationNameCurrent personCurrent = PersonIdentificationNameCurrent.fetchByBannerId(BANNER_ID_WITH_TYPE_BIRTH)
        assertNotNull personCurrent
        assertEquals 'P', personCurrent.entityIndicator
        GlobalUniqueIdentifier globalUniqueIdentifier = globalUniqueIdentifierService.fetchByDomainKeyAndLdmName(personCurrent.pidm.toString(), GeneralCommonConstants.PERSONS_LDM_NAME)
        assertNotNull globalUniqueIdentifier
        PersonBasicPersonBase personBase = PersonBasicPersonBase.fetchByPidm(personCurrent.pidm)
        assertNotNull personBase
        Map content = [
                action     : [POST: "list"],
                names      : [[type: [category: NameTypeCategory.PERSONAL.versionToEnumMap["v6"]], firstName: personCurrent.firstName, lastName: personCurrent.lastName]],
                credentials: [[type: CredentialType.SOCIAL_SECURITY_NUMBER.versionToEnumMap["v6"], value: personBase.ssn],
                              [type: CredentialType.BANNER_ID.versionToEnumMap["v6"], value: personCurrent.bannerId]],
                sort       : "lastName",
                order      : "asc"
        ]

        // Call service
        def requestProcessingResult = personV6CompositeService.listQApi(content)
        def response = personV6CompositeService.createPersonDataModels(content, requestProcessingResult)
        assertNotNull response
        assertTrue response.size() > 0
        response.each {
            it.credentials.each { credential ->
                if (credential.type == CredentialType.SOCIAL_SECURITY_NUMBER.versionToEnumMap["v6"]) {
                    assertEquals personBase.ssn, credential.value
                } else if (credential.type == CredentialType.BANNER_ID.versionToEnumMap["v6"]) {
                    assertEquals personCurrent.bannerId, credential.value
                }
            }
        }
    }


    @Test
    void testShow_ValidPersonAddresses_v6() {
        setAcceptHeader("application/vnd.hedtech.integration.v6+json")
        PersonV6 getPersonV6 = personV6CompositeService.get(personGlobalUniqueIdentifier.guid)
        assertNotNull getPersonV6
        assertEquals personGlobalUniqueIdentifier.guid, getPersonV6.guid
        PersonAddress getPersonAddress = personAddressService.fetchAllByActiveStatusPidmsAndAddressTypes([personIdentificationName.pidm], ['MA'])[0]
        assertNotNull getPersonAddress
        PersonAddressAdditionalProperty getPersonAddressExtendedProperties = personAddressAdditionalPropertyService.fetchAllBySurrogateIds([getPersonAddress.id])[0]
        assertNotNull getPersonAddressExtendedProperties
        PersonAddressDecorator getPersonAddressDecorator = getPersonV6.addresses.find {
            it.addressGuid == getPersonAddressExtendedProperties.addressGuid
        }
        assertNotNull getPersonAddressDecorator
        String getPersonAddressGuid = GlobalUniqueIdentifier.fetchByDomainKeyAndLdmName('MA', 'address-types')?.guid
        assertNotNull getPersonAddressGuid
        assertEquals getPersonAddressExtendedProperties.addressGuid, getPersonAddressDecorator.addressGuid
        assertEquals getPersonAddressGuid, getPersonAddressDecorator.type.id
        assertEquals 'mailing', getPersonAddressDecorator.type.addressType
        assertEquals DateConvertHelperService.convertDateIntoUTCFormat(getPersonAddress.fromDate), getPersonAddressDecorator.startOn
        assertEquals DateConvertHelperService.convertDateIntoUTCFormat(getPersonAddress.toDate), getPersonAddressDecorator.endOn
    }


    private Map getPersonBirthNameTypeByPidm(List<Integer> pidms) {
        Map dataMap = getPersonAlternateNamesByPidm(pidms)
        /*
        dataMap.find { person ->
            person.nameType.code == 'birth'
        }*/
        //TODO: return person with name type birth
        return null
    }


    private Map getPersonAlternateNamesByPidm(List<Integer> pidms) {
        Map dataMap = [:]
        def bannerNameTypeToHedmNameTypeMap = personNameTypeCompositeService.getBannerNameTypeToHedmV6NameTypeMap()
        List<PersonIdentificationNameAlternate> entities = personIdentificationNameAlternateService.fetchAllMostRecentlyCreated(pidms, bannerNameTypeToHedmNameTypeMap.keySet().toList())
        Map pidmToAlternateNamesMap = [:]
        entities.each {
            List<PersonIdentificationNameAlternate> personAlternateNames = []
            if (pidmToAlternateNamesMap.containsKey(it.pidm)) {
                personAlternateNames = pidmToAlternateNamesMap.get(it.pidm)
            } else {
                pidmToAlternateNamesMap.put(it.pidm, personAlternateNames)
            }
            personAlternateNames.add(it)
        }
        dataMap.put("pidmToAlternateNamesMap", pidmToAlternateNamesMap)
        return dataMap
    }


    private Map findOnePersonWithGivenBannerID(def persons) {
        def personIdentificationName
        def personBase
        Map personDetails = [:]
        for (def person : persons) {
            def bannerId = person.credentials.find { credentialType ->
                credentialType.type == 'bannerId'
            }
            personIdentificationName = PersonUtility.getPerson(bannerId.value)
            personBase = PersonBasicPersonBase.findByPidm(personIdentificationName.pidm)
            personDetails = ["guid": person.guid, "personBase": personBase, "personIdentificationName": personIdentificationName]
            if (personBase && personIdentificationName) {
                break
            }
        }
        return personDetails
    }


    private void assertListIsSortedOnField(def list, String field, String sortOrder = "ASC") {
        def prevListItemVal
        list.each {
            String curListItemVal = it['names'][0].getAt(field)
            if (!prevListItemVal) {
                prevListItemVal = curListItemVal
            }
            if (sortOrder == "ASC") {
                assertTrue prevListItemVal.compareTo(curListItemVal) < 0 || prevListItemVal.compareTo(curListItemVal) == 0
            } else {
                assertTrue prevListItemVal.compareTo(curListItemVal) > 0 || prevListItemVal.compareTo(curListItemVal) == 0
            }
            prevListItemVal = curListItemVal
        }
    }


    private List getPersonRoles(PersonV6 o_success_person) {
        GlobalUniqueIdentifier globalUniqueIdentifier =
                globalUniqueIdentifierService.fetchByLdmNameAndGuid(GeneralCommonConstants.PERSONS_LDM_NAME, o_success_person.guid)
        List pidms = [globalUniqueIdentifier.domainKey]
        Map returnList
        returnList = userRoleCompositeService.fetchAllRolesByPidmInList(pidms, true)
        assertNotNull returnList
        assertTrue returnList.size() > 0
        List<RoleDetail> personRoleDetails = returnList.get(Integer.valueOf(pidms[0]))
        List personRoles = []
        personRoleDetails.each { roles ->
            personRoles.add(roles.role)
        }
        personRoles
    }


    private void setAcceptHeader(String mediaType) {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Accept", mediaType)
    }


    private void setContentTypeHeader(String mediaType) {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", mediaType)
    }


    //POST- Person Create API
    @Test
    void testCreatePersonWithAlternateNameHavingBirthNameType() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.v6+json")
        request.addHeader("Accept", "application/vnd.hedtech.integration.v6+json")

        Map content = newPersonWithAlternateNameHavingBirthNameType()

        def o_success_person_create = personV6CompositeService.create(content)

        assertNotNull o_success_person_create
        assertNotNull o_success_person_create.personGuid
        assertEquals 2, o_success_person_create.personAlternateNames?.size()

        def o_primary_name_create = o_success_person_create.personIdentificationNameCurrent
        def o_birth_name_create = o_success_person_create.personAlternateNames.get(0)
        def o_legal_name_create = o_success_person_create.personAlternateNames.get(1)
        def o_person_base = o_success_person_create.personBase


        assertNotNull o_primary_name_create
        assertEquals i_success_first_name, o_primary_name_create.firstName
        assertEquals i_success_middle_name, o_primary_name_create.middleName
        assertEquals i_success_last_name, o_primary_name_create.lastName
        assertEquals i_success_namePrefix, o_person_base.namePrefix
        assertEquals i_success_nameSuffix, o_person_base.nameSuffix
        assertEquals i_success_surnamePrefix, o_primary_name_create.surnamePrefix
        assertNotNull o_birth_name_create
        assertEquals i_success_birth_first_name, o_birth_name_create.firstName
        assertEquals i_success_birth_middle_name, o_birth_name_create.middleName
        assertEquals i_success_birth_last_name, o_birth_name_create.lastName
        assertEquals "BRTH", o_birth_name_create.nameType.code

        assertEquals i_success_legal_first_name, o_legal_name_create.firstName
        assertEquals i_success_legal_middle_name, o_legal_name_create.middleName
        assertEquals i_success_legal_last_name, o_legal_name_create.lastName
        assertEquals "LEGL", o_legal_name_create.nameType.code

    }

    private Map newPersonWithAlternateNameHavingBirthNameType() {
        Map params = [names: [
                     [lastName: i_success_last_name, middleName: i_success_middle_name, firstName: i_success_first_name,fullName: i_success_full_name, type:[category:i_success_primary_name_type], namePrefix: i_success_namePrefix, nameSuffix: i_success_nameSuffix, surnamePrefix: i_success_surnamePrefix],
                     [lastName: i_success_birth_last_name, middleName: i_success_birth_middle_name, firstName: i_success_birth_first_name,fullName: i_success_full_name, type:[category:i_success_birth_name_type]],
                     [lastName: i_success_legal_last_name, middleName: i_success_legal_middle_name, firstName: i_success_legal_first_name,fullName: i_success_full_name, type:[category:i_success_legal_name_type]]
        ]
        ]
        return params
    }


    private Map newPersonWithAlternateNameHavingAdditionProperties() {
        Map params = [names: [
                [lastName: i_success_last_name, middleName: i_success_middle_name, firstName: i_success_first_name,fullName: i_success_full_name, type:[category:i_success_primary_name_type], namePrefix: i_success_namePrefix, nameSuffix: i_success_nameSuffix, surnamePrefix: i_success_surnamePrefix]
        ]
        ]
        return params
    }


    //POST- Person Create API
    @Test
    void testCreatePersonWithCredetials() {
        Map content = newPersonWithCredentials()
        def o_success_person_create = personV6CompositeService.create(content)
        def o_person_base = o_success_person_create.personBase

        assertNotNull o_success_person_create
        assertNotNull o_success_person_create.personGuid

        assertEquals i_success_ssn,o_person_base.ssn
        assertEquals  i_success_elevateId ,o_success_person_create.additionalIds[0].additionalId
        assertNotEquals i_success_bannerId , o_success_person_create.personIdentificationNameCurrent.bannerId
    }


    @Test
    void testCreatePersonWithPrivacyStatus() {
        Map content = newPersonWithAlternateNameHavingBirthNameType()
        content.privacyStatus = [privacyCategory: i_success_privacy_satus]
        def dataMap = personV6CompositeService.create(content)
        def o_success_person_create = personV6CompositeService.createPersonDataModel(dataMap)

        assertNotNull o_success_person_create
        assertNotNull o_success_person_create.guid
        assertEquals  i_success_privacy_satus ,o_success_person_create.privacyStatus.privacyCategory
    }

    @Test
    void testCreatePersonWithAdditionProperties() {
        Map content = newPersonWithAlternateNameHavingBirthNameType()
        content.birthDate = i_success_birthDate_String
        def dataMap = personV6CompositeService.create(content)
        def o_success_person_create = personV6CompositeService.createPersonDataModel(dataMap)

        assertNotNull o_success_person_create
        assertNotNull o_success_person_create.guid
        assertEquals  i_success_privacy_satus ,o_success_person_create.privacyStatus.privacyCategory
    }



    private Map newPersonWithCredentials() {
        Map params = [names: [
                [lastName: i_success_last_name, middleName: i_success_middle_name, firstName: i_success_first_name,fullName: i_success_full_name, type:[category:i_success_primary_name_type], namePrefix: i_success_namePrefix, nameSuffix: i_success_nameSuffix, surnamePrefix: i_success_surnamePrefix]
        ],
        credentials: [
                [type:"bannerId",value:i_success_bannerId ],
                [type:"ssn",value:i_success_ssn ],
                [type:"elevateId",value:i_success_elevateId ]
        ]
        ]
        return params
    }


    //POST- Person Create API
    @Test
    void testUpdatePersonWithCredetials() {
        Map content = newPersonWithCredentials()
        def o_success_person_create = personV6CompositeService.create(content)
        def o_person_base = o_success_person_create.personBase

        assertNotNull o_success_person_create
        assertNotNull o_success_person_create.personGuid

        assertEquals i_success_ssn,o_person_base.ssn
        assertEquals  i_success_elevateId ,o_success_person_create.additionalIds[0].additionalId
        assertNotEquals i_success_bannerId , o_success_person_create.personIdentificationNameCurrent.bannerId

        Map updateContent = newPersonWithCredentials()
        updateContent.id = o_success_person_create.personGuid
        def o_success_person_update = personV6CompositeService.update(updateContent)

        assertEquals i_success_ssn,o_person_base.ssn
        assertEquals  i_success_elevateId ,o_success_person_update.additionalIds[0].additionalId
        assertEquals i_success_bannerId , o_success_person_update.personIdentificationNameCurrent.bannerId
    }


    //POST- Person Create API
    @Test
    void testCreatePersonWithCredetialsSSNandSIN() {
        Map content = newPersonWithCredentialsSSNandSIN()
        def exceptionMessage = shouldFail(ApplicationException) {
             personV6CompositeService.create(content)
        }
        assertEquals "ssn.sin.both.not.valid", exceptionMessage
    }

    private Map newPersonWithCredentialsSSNandSIN() {
        Map params = [names: [
                [lastName: i_success_last_name, middleName: i_success_middle_name, firstName: i_success_first_name,fullName: i_success_full_name, type:[category:i_success_primary_name_type], namePrefix: i_success_namePrefix, nameSuffix: i_success_nameSuffix, surnamePrefix: i_success_surnamePrefix]
                 ],
                      credentials: [
                              [type:"bannerId",value:i_success_bannerId ],
                              [type:"sin",value:i_success_sin ],
                              [type:"ssn",value:i_success_ssn ],
                              [type:"elevateId",value:i_success_elevateId ]
                      ]
                ]
        return params
    }

    @Test
    void testUpdatePersonWithPrivacyStatus() {
        Map content = newPersonWithAlternateNameHavingBirthNameType()
        content.privacyStatus = [privacyCategory: i_success_privacy_satus]
        def dataMap = personV6CompositeService.create(content)
        def o_success_person_create = personV6CompositeService.createPersonDataModel(dataMap)

        assertNotNull o_success_person_create
        assertNotNull o_success_person_create.guid
        assertEquals  i_success_privacy_satus ,o_success_person_create.privacyStatus.privacyCategory


        Map updateContent = newPersonWithCredentials()
        updateContent.id = o_success_person_create.guid
        updateContent.privacyStatus = [privacyCategory: i_success_update_privacy_satus]
        dataMap = personV6CompositeService.update(updateContent)
        def o_success_person_update = personV6CompositeService.createPersonDataModel(dataMap)

        assertNotNull o_success_person_create
        assertNotNull o_success_person_create.guid
        assertEquals  i_success_update_privacy_satus ,o_success_person_update.privacyStatus.privacyCategory
    }

    @Test
    void testUpdatePersonPersonBasicPersonBase() {
       /* GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.v1+json")
        request.addHeader("Accept", "application/vnd.hedtech.integration.v1+json")*/

        PersonBasicPersonBase personBasicPersonBase = createPersonBasicPersonBase()
        PersonIdentificationNameCurrent personIdentificationNameCurrent = PersonIdentificationNameCurrent.findAllByPidmInList([personBasicPersonBase.pidm]).get(0)
        GlobalUniqueIdentifier uniqueIdentifier = GlobalUniqueIdentifier.findByLdmNameAndDomainKey("persons", personIdentificationNameCurrent.pidm)
        Map params = getPersonWithPersonBasicPersonBaseChangeRequest(personIdentificationNameCurrent, uniqueIdentifier.guid);

        //update PersonBasicPersonBase info
        personV6CompositeService.update(params)
        PersonBasicPersonBase newPersonBasicPersonBase = PersonBasicPersonBase.fetchByPidmList([personBasicPersonBase.pidm]).get(0)

        assertEquals 'F', newPersonBasicPersonBase.sex
        assertEquals 'CCCCC', newPersonBasicPersonBase.namePrefix
        assertEquals 'CCCCC', newPersonBasicPersonBase.nameSuffix
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

    private Map getPersonWithPersonBasicPersonBaseChangeRequest(personIdentificationNameCurrent, guid) {
        Map params = [id   : guid,
                      names: [
                              [lastName: personIdentificationNameCurrent.lastName, middleName: personIdentificationNameCurrent.middleName, firstName: personIdentificationNameCurrent.firstName,fullName: i_success_full_name, type:[category:i_success_primary_name_type], namePrefix:  'CCCCC', nameSuffix: 'CCCCC', surnamePrefix: i_success_surnamePrefix]
                      ],
                      gender  : 'female'

        ]
        return params
    }


    @Test
    void testCreatePersonWithRacesGender() {
        Map content = newPersonWithAlternateNameHavingBirthNameType()
        content.races = []
        content.races << [race:[id: raceGlobalUniqueIdentifier1]]
        content.gender =  'male'
        def dataMap = personV6CompositeService.create(content)
        def o_success_person_create = personV6CompositeService.createPersonDataModel(dataMap)

        assertNotNull o_success_person_create
        assertNotNull o_success_person_create.guid
        assertEquals  raceGlobalUniqueIdentifier1 ,o_success_person_create.races[0].race.id
        assertEquals  'male' ,o_success_person_create.gender
    }


    @Test
    void testUpdatePersonWithRacesGenderReligionMaritalStatusEthnicityCitizenshipStatus() {
        Map content = newPersonWithAlternateNameHavingBirthNameType()
        content.races = []
        content.races << [race:[id: raceGlobalUniqueIdentifier1]]
        content.gender =  'male'
        content.religion = [id: religionGlobalUniqueIdentifier1]
        content.maritalStatus = [maritalCategory: i_success_maritalStatus1]
        content.ethnicity = [ethnicGroup:[id: ethnicityGlobalUniqueIdentifier1]]
        content.citizenshipStatus = [detail:[id: citizenshipStatusGlobalUniqueIdentifier1],category:citizenshipStatusCategory1]
        def dataMap = personV6CompositeService.create(content)
        def o_success_person_create = personV6CompositeService.createPersonDataModel(dataMap)


        assertNotNull o_success_person_create
        assertNotNull o_success_person_create.guid
        assertEquals  raceGlobalUniqueIdentifier1 ,o_success_person_create.races[0].race.id
        assertEquals  'male' ,o_success_person_create.gender
        assertEquals  religionGlobalUniqueIdentifier1 ,o_success_person_create.religion.id
        assertEquals  i_success_maritalStatus1 ,o_success_person_create.maritalStatus.parentCategory
        assertEquals  ethnicityGlobalUniqueIdentifier1 ,o_success_person_create.ethnicity.id
        assertEquals  citizenshipStatusGlobalUniqueIdentifier1 ,o_success_person_create.citizenshipStatus.detail.id


        Map updateContent = newPersonWithCredentials()
        updateContent.id = o_success_person_create.guid
        updateContent.races = []
        updateContent.races << [race:[id: raceGlobalUniqueIdentifier2]]
        updateContent.gender =  'female'
        updateContent.religion = [id: religionGlobalUniqueIdentifier2]
        updateContent.maritalStatus = [maritalCategory: i_success_maritalStatus2]
        updateContent.ethnicity = [ethnicGroup:[id: ethnicityGlobalUniqueIdentifier2]]
        updateContent.citizenshipStatus = [detail:[id: citizenshipStatusGlobalUniqueIdentifier2],category:citizenshipStatusCategory2]
        dataMap = personV6CompositeService.update(updateContent)
        def o_success_person_update = personV6CompositeService.createPersonDataModel(dataMap)

        assertNotNull o_success_person_update
        assertNotNull o_success_person_update.guid
        assertEquals  raceGlobalUniqueIdentifier2 ,o_success_person_update.races[0].race.id
        assertEquals  'female' ,o_success_person_update.gender
        assertEquals  religionGlobalUniqueIdentifier2 ,o_success_person_update.religion.id
        assertEquals  i_success_maritalStatus2 ,o_success_person_update.maritalStatus.parentCategory
        assertEquals  ethnicityGlobalUniqueIdentifier2 ,o_success_person_update.ethnicity.id
        assertEquals  citizenshipStatusGlobalUniqueIdentifier2 ,o_success_person_update.citizenshipStatus.detail.id
    }



    @Test
    void testUpdatePersonCitizenshipStatusCountryOfBirth() {
        Map content = newPersonWithAlternateNameHavingBirthNameType()
        content.citizenshipStatus = [category:citizenshipStatusCategory1]
        //content.nationBirth = countryOfBirth1
        def dataMap = personV6CompositeService.create(content)
        def o_success_person_create = personV6CompositeService.createPersonDataModel(dataMap)


        assertNotNull o_success_person_create
        assertNotNull o_success_person_create.guid
        assertEquals  citizenshipStatusCategory1 ,o_success_person_create.citizenshipStatus.category
        //assertEquals  countryOfBirth1 ,o_success_person_create.countryOfBirth


        Map updateContent = newPersonWithCredentials()
        updateContent.id = o_success_person_create.guid
        updateContent.citizenshipStatus = [category:citizenshipStatusCategory2]
        //updateContent.nationBirth = countryOfBirth2
        dataMap = personV6CompositeService.update(updateContent)
        def o_success_person_update = personV6CompositeService.createPersonDataModel(dataMap)

        assertNotNull o_success_person_update
        assertNotNull o_success_person_update.guid
        assertEquals  citizenshipStatusCategory2 ,o_success_person_update.citizenshipStatus.category
        //assertEquals  countryOfBirth2 ,o_success_person_update.countryOfBirth
    }


    @Test
    void testUpdatePersonEmails() {
        Map content = newPersonWithAlternateNameHavingBirthNameType()
        content.emails = []
        content.emails << [address:emailAddress1,preference:"primary",type:[emailType:"personal"]]
        def dataMap = personV6CompositeService.create(content)
        def o_success_person_create = personV6CompositeService.createPersonDataModel(dataMap)


        assertNotNull o_success_person_create
        assertNotNull o_success_person_create.guid
        assertEquals  emailAddress1 ,o_success_person_create.emails.address.get(0)


        Map updateContent = newPersonWithCredentials()
        updateContent.id = o_success_person_create.guid
        updateContent.emails = []
        updateContent.emails << [address:emailAddress2,preference:"primary",type:[emailType:"personal"]]
        dataMap = personV6CompositeService.update(updateContent)
        def o_success_person_update = personV6CompositeService.createPersonDataModel(dataMap)

        assertNotNull o_success_person_update
        assertNotNull o_success_person_update.guid
        assertEquals  emailAddress2 ,o_success_person_update.emails.address.get(0)
    }




}
