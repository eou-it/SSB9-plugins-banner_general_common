/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.person.ldm

import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.common.GeneralCommonConstants
import net.hedtech.banner.general.commonmatching.CommonMatchingSourceRule
import net.hedtech.banner.general.overall.IntegrationConfiguration
import net.hedtech.banner.general.overall.VisaInformation
import net.hedtech.banner.general.overall.ldm.GlobalUniqueIdentifier
import net.hedtech.banner.general.overall.ldm.GlobalUniqueIdentifierService
import net.hedtech.banner.general.overall.ldm.LdmService
import net.hedtech.banner.general.person.*
import net.hedtech.banner.general.person.ldm.v1.RoleDetail
import net.hedtech.banner.general.person.ldm.v6.PersonV6
import net.hedtech.banner.general.system.*
import net.hedtech.banner.general.system.ldm.*
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
import org.junit.After
import org.junit.Before
import org.junit.Test

class PersonV6CompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    PersonV6CompositeService personV6CompositeService
    UserRoleCompositeService userRoleCompositeService
    GlobalUniqueIdentifierService globalUniqueIdentifierService
    CitizenshipStatusCompositeService citizenshipStatusCompositeService
    VisaTypeCompositeService visaTypeCompositeService
    ReligionCompositeService religionCompositeService
    NameTypeService nameTypeService
    PersonNameTypeCompositeService personNameTypeCompositeService
    PersonIdentificationNameAlternateService personIdentificationNameAlternateService
    static final String BANNER_ID_WITH_TYPE_BIRTH = 'HOSR24789'


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
    void testListPersonV6InvalidRoleRequired() {
        setAcceptHeader("application/vnd.hedtech.integration.v6+json")

        try {
            personV6CompositeService.list(params)
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
            personV6CompositeService.list(params)
            fail('Invalid role for Person V6')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'role.supported.v6'
        }
    }


    @Test
    void testListPersonValidV6ForRoleInstructor() {
        setAcceptHeader("application/vnd.hedtech.integration.v6+json")

        Map params = [role: "instructor"]
        def o_success_persons = personV6CompositeService.list(params)

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
        def o_success_persons = personV6CompositeService.list(params)

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
        def o_success_persons = personV6CompositeService.list(params)

        assertNotNull o_success_persons
        assertFalse o_success_persons.isEmpty()
        assertNotNull o_success_persons[0].guid

        assertTrue o_success_persons[0].roles.role.contains("alumni")
    }


    @Test
    void testListPersonValidV6ForRoleEmployees() {
        setAcceptHeader("application/vnd.hedtech.integration.v6+json")

        Map params = [role: "employee"]
        def o_success_persons = personV6CompositeService.list(params)

        assertNotNull o_success_persons
        assertFalse o_success_persons.isEmpty()
        assertNotNull o_success_persons[0].guid
        assertTrue o_success_persons[0].roles.role.contains("employee")

    }


    @Test
    void testListPersonValidV6ForRoleVendor() {
        setAcceptHeader("application/vnd.hedtech.integration.v6+json")

        Map params = [role: "vendor"]
        PersonV6 o_success_persons = personV6CompositeService.list(params).get(0)

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
        List<PersonV6> o_success_persons = personV6CompositeService.list(params)

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
        List<PersonV6> o_success_persons = personV6CompositeService.list(params)

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

        def persons = personV6CompositeService.list(params)
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

        def persons = personV6CompositeService.list(params)
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

        def persons = personV6CompositeService.list(params)
        assertNotNull persons
        assertEquals params.max, persons.size().toString()

        def person = personV6CompositeService.get(persons[0].guid)
        assertNotNull person
        assertEquals persons[0].guid, person.guid

        String pidm = (globalUniqueIdentifierService.fetchByLdmNameAndGuid(GeneralCommonConstants.PERSONS_LDM_NAME, person.guid)).domainKey
        def personBase = PersonBasicPersonBase.fetchByPidm(Integer.parseInt(pidm))
        def visaInformation = VisaInformation.fetchAllWithMaxSeqNumByPidmInList([Integer.parseInt(pidm)])
        if (!personBase) {
            def personBasicPersonBase = new PersonBasicPersonBase(pidm: Integer.parseInt(pidm), armedServiceMedalVetIndicator: true)
            personBasicPersonBase.save(failOnError: true, flush: true)
            personBase = PersonBasicPersonBase.fetchByPidm(Integer.parseInt(pidm))
        }
        if (!visaInformation[0]) {
            def visaType = VisaType.findAll()[0]
            def visaInfo = new VisaInformation(pidm: Integer.parseInt(pidm), sequenceNumber: 1, visaType: visaType, entryIndicator: false,
                    visaIssueDate: new Date(), visaExpireDate: new Date())
            visaInfo.save(failOnError: true, flush: true)
            visaInformation = VisaInformation.fetchAllWithMaxSeqNumByPidmInList([Integer.parseInt(pidm)])
            person = personV6CompositeService.get(persons[0].guid)
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

        def guids2 = visaTypeCompositeService.fetchGUIDs([visaInformation[0].visaType.code])
        def visaType = visaTypeCompositeService.get(guids2.values()[0])
        assertEquals guids2.values()[0], person.visaStatus.detail.id
        assertEquals visaType.category, person.visaStatus.category
        assertNotNull person.visaStatus.startOn
        assertNotNull person.visaStatus.endOn
        assertNotNull person.visaStatus.status
    }


    @Test
    void testListSortByFirstNameASC() {
        def params = [role: "student", sort: "firstName", order: "ASC"]

        def persons = personV6CompositeService.list(params)
        assertNotNull persons
        assertListIsSortedOnField(persons, params.sort, params.order)
    }


    @Test
    void testListNamesWithTypePersonal() {
        def params = [role: "student"]

        def persons = personV6CompositeService.list(params)
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
        setContentTypeHeader("application/vnd.hedtech.integration.duplicate-check.v6+json")
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
        def response = personV6CompositeService.createDecorators(content, requestProcessingResult)
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
        setContentTypeHeader("application/vnd.hedtech.integration.duplicate-check.v6+json")
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
        PersonIdentificationNameAlternate personAlternate = PersonIdentificationNameAlternate.findByPidmAndNameType(personCurrent.pidm, NameType.findByCode("BRTH"))
        assertNotNull personAlternate
        Map content = [
                action: [POST: "list"],
                names : [[type: [category: NameTypeCategory.BIRTH.versionToEnumMap["v6"]], firstName: personAlternate.firstName, lastName: personAlternate.lastName, middleName: personAlternate.middleName]],
                sort  : "lastName",
                order : "asc"
        ]

        // Call the service
        def requestProcessingResult = personV6CompositeService.listQApi(content)
        def response = personV6CompositeService.createDecorators(content, requestProcessingResult)
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
        setContentTypeHeader("application/vnd.hedtech.integration.duplicate-check.v6+json")
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
        def response = personV6CompositeService.createDecorators(content, requestProcessingResult)
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

}
