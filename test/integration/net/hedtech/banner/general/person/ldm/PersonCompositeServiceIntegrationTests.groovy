/*********************************************************************************
 Copyright 2014-2015 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.person.ldm

import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.lettergeneration.PopulationSelectionExtract
import net.hedtech.banner.general.lettergeneration.PopulationSelectionExtractReadonly
import net.hedtech.banner.general.overall.ldm.GlobalUniqueIdentifier
import net.hedtech.banner.general.overall.ldm.LdmService
import net.hedtech.banner.general.person.PersonBasicPersonBase
import net.hedtech.banner.general.person.PersonIdentificationName
import net.hedtech.banner.general.person.PersonIdentificationNameAlternate
import net.hedtech.banner.general.person.PersonIdentificationNameCurrent
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
    def i_success_name_type = "Primary"
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
    def i_failed_update_credential_id = "TTTT"
    def i_failed_update_credential_type = "Social Security Number"
    def i_success_alternate_first_name = "John"
    def i_success_alternate_middle_name = "A"
    def i_success_alternate_last_name = "Jorden"
    def i_success_alternate_birth_name_type = "Birth"


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
    void testListQapiWithValidFirstAndLastName() {
        //we will forcefully set the content type so that the tests go through all possible code flows
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/json")
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
        request.addHeader("Content-Type", "application/json")
        Map params = getParamsWithReqiuredFields()
        params.names[0].firstName = "MarkTT"
        params.names[0].lastName = "Kole"
        def persons = personCompositeService.list(params)

        assertNotNull persons
        assertTrue persons.isEmpty()
    }


    @Test
    void testListQapiWithInValidDateOfBirth() {
        //we will forcefully set the content type so that the tests go through all possible code flows
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", "application/json")
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
    void testListQapiWithInvalidPersonfilter() {

        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Accept", "application/vnd.hedtech.integration.v2+json")
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.person-filter.v2+json")

        //String guid = GlobalUniqueIdentifier.findByLdmNameAndDomainKey('person-filters', 'GENERAL-^ALL-^BANNER-^GRAILS')?.guid

        Map params = getParamsForPersonFilter()

        params.put("personFilter", "xxxx")

        try {
            personCompositeService.list(params)
            fail('This should have failed as person filter GUID is invalid')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'not.found.message'
        }
    }


    @Test
    void testListQapiWithPersonfilterNull() {

        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Accept", "application/vnd.hedtech.integration.v2+json")
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.person-filter.v2+json")

        //String guid = GlobalUniqueIdentifier.findByLdmNameAndDomainKey('person-filters', 'GENERAL-^ALL-^BANNER-^GRAILS')?.guid

        Map params = getParamsForPersonFilter()

        params.put("personFilter", "")

        try {
            personCompositeService.list(params)
            fail('This should have failed as person filter GUID is null')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'not.found.message'
        }
    }


    @Test
    void testListPersonQapiWithBirthNameType() {
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
        assertEquals i_success_name_type, o_primary_name_create.nameType
        assertEquals i_success_namePrefix, o_primary_name_create.title
        assertEquals i_success_nameSuffix, o_primary_name_create.pedigree
        assertEquals i_success_preferenceFirstName, o_primary_name_create.preferredName
        assertNotNull o_birth_name_create
        assertEquals i_success_alternate_first_name, o_birth_name_create.firstName
        assertEquals i_success_alternate_middle_name, o_birth_name_create.middleName
        assertEquals i_success_alternate_last_name, o_birth_name_create.lastName
        assertEquals i_success_alternate_birth_name_type, o_birth_name_create.nameType

        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Accept", "application/vnd.hedtech.integration.v3+json")
        request.addHeader("Content-Type", "application/json")

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
    void testListQapiWithValidPersonfilter() {


        def persons = [:]
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Accept", "application/vnd.hedtech.integration.v2+json")
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.person-filter.v2+json")

        String guid = GlobalUniqueIdentifier.findByLdmNameAndDomainKey('person-filters', 'GENERAL-^ALL-^BANNER-^GRAILS')?.guid

        Map params = getParamsForPersonFilter()

        params.put("personFilter", guid)

        persons = personCompositeService.list(params)
        assertNotNull persons

    }


    @Test
    void testListapiWithRoleFacultyAndPagination() {
        def params = [role: "faculty", max: '10', offset: '5']

        def persons = personCompositeService.list(params)
        persons.each {
            it.roles.role == "Faculty"
        }
    }


    @Test
    void testListapiWithRoleStudentAndPagination() {
        def params = [role: "student", max: '10', offset: '5']

        def persons = personCompositeService.list(params)
        persons.each {
            it.roles.role == "Student"
        }
    }


    @Test
    void testListapiWithInvalidPersonfilter() {

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

        def params = [:]
        params.put("personFilter", "xxxx")
        params.put("role", "faculty")

        try {
            personCompositeService.list(params)
            fail('This should have failed as person filter and role both present')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'UnsupportedFilterCombination'
        }
    }


    @Test
    void testListapiWithValidPersonfilter() {

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
    void testListapiWithValidPersonfilterAsGuidAndPaginationForPerformance() {
        // verify our test case has 7 records
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
                and exists ( select 1 from sfrstcr where sfrstcr_pidm = spriden_pidm)
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
        assertEquals insertCount, persextract.size()
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
        persextract250.each { pers ->
            persextract2502.find { pers2 ->
                cnt += 1
                if (pers2.pidm == pers.pidm) {
                    // println ("${cnt} pidms match ${pers2.pidm} ${pers.pidm}")
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
                    // println ("${cnt} pidms match ${pers2.pidm} ${pers.pidm}")
                    matchextract += 1
                }
            }
        }
        assertEquals 0, matchextract

        // set up params for call
        def persons = []

        String guid2 = GlobalUniqueIdentifier.fetchByLdmNameAndDomainKey('person-filters', 'STUDENT-^HEDMPERFORM-^BANNER-^GRAILS')[0].guid
        assertNotNull guid2


        def params = [personFilter: guid2, max: '250', offset: '0']
        log.debug "turn logging on "
        if (log.isDebugEnabled()) {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            try {
                sql.execute("ALTER SESSION SET SQL_TRACE TRUE")
                sql.execute("ALTER SESSION SET tracefile_identifier=person_api")
            }
            finally {
                sql?.close()
            }
        }

        persons = personCompositeService.list(params)
        assertEquals 250, persons.size()
        if (log.isDebugEnabled()) {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            try {
                sql.execute("ALTER SESSION SET SQL_TRACE FALSE")
            }
            finally {
                sql?.close()
            }
        }

        // second page

        params = [personFilter: guid2, max: '250', offset: '250']
        if (log.isDebugEnabled()) {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            try {
                sql.execute("ALTER SESSION SET SQL_TRACE TRUE")
                sql.execute("ALTER SESSION SET tracefile_identifier=person_api2")
            }
            finally {
                sql?.close()
            }
        }
        def persons2 = personCompositeService.list(params)
        assertEquals 250, persons2.size()
        if (log.isDebugEnabled()) {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            try {
                sql.execute("ALTER SESSION SET SQL_TRACE FALSE")
            }
            finally {
                sql?.close()
            }
        }

    }

    //GET- Person by guid API
    @Test
    void testGetPersonCredentialsByGuid() {
        String guid = GlobalUniqueIdentifier.findByLdmNameAndDomainKey('persons', '50199')?.guid

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
    void testGetPersonWithAlternateNameHavingBirthNameType() {
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
        assertEquals i_success_name_type, o_primary_name_create.nameType
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

    //POST- Person Create API
    @Test
    void testCreatePersonWithStateAndZipIntegrationSettingValue() {
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
    void testCreatePersonWithActiveAndPreferredEmail() {
        Map content = newPersonWithPreferredEmailRequest()

        def o_success_person_create = personCompositeService.create(content)

        assertNotNull o_success_person_create
        assertNotNull o_success_person_create.guid
        assertEquals 3, o_success_person_create.emails?.size()
        assertEquals i_success_guid_personal, o_success_person_create.emails[0].guid
        assertEquals i_success_emailType_personal, o_success_person_create.emails[0].emailType
        assertEquals i_success_emailAddress_personal, o_success_person_create.emails[0].emailAddress
        assertEquals i_success_guid_personal, o_success_person_create.emails[1].guid
        assertEquals i_success_emailType_preferred, o_success_person_create.emails[1].emailType
        assertEquals i_success_emailAddress_personal, o_success_person_create.emails[1].emailAddress
        assertEquals i_success_guid_institution, o_success_person_create.emails[2].guid
        assertEquals i_success_emailType_institution, o_success_person_create.emails[2].emailType
        assertEquals i_success_emailAddress_institution, o_success_person_create.emails[2].emailAddress
    }

    //POST- Person Create API
    @Test
    void testCreatePersonWithAlternateNameHavingBirthNameType() {
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
        assertEquals i_success_name_type, o_primary_name_create.nameType
        assertEquals i_success_namePrefix, o_primary_name_create.title
        assertEquals i_success_nameSuffix, o_primary_name_create.pedigree
        assertEquals i_success_preferenceFirstName, o_primary_name_create.preferredName
        assertNotNull o_birth_name_create
        assertEquals i_success_alternate_first_name, o_birth_name_create.firstName
        assertEquals i_success_alternate_middle_name, o_birth_name_create.middleName
        assertEquals i_success_alternate_last_name, o_birth_name_create.lastName
        assertEquals i_success_alternate_birth_name_type, o_birth_name_create.nameType
    }


    @Test
    void testUpdatePersonFirstNameAndLastNameChangeWithCreatingPersonBase() {
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
        PersonBasicPersonBase personBasicPersonBase = createPersonBasicPersonBase()

        assertNotNull personBasicPersonBase
        assertNotNull personBasicPersonBase.pidm

        PersonIdentificationNameCurrent personIdentificationNameCurrent = PersonIdentificationNameCurrent.findAllByPidmInList([personBasicPersonBase.pidm]).get(0)

        assertNotNull personIdentificationNameCurrent
        assertNotNull personIdentificationNameCurrent.pidm

        String i_success_guid = GlobalUniqueIdentifier.findByLdmNameAndDomainKey("persons", personIdentificationNameCurrent.pidm)?.guid

        assertNotNull i_success_guid
        Map params = updatePersonWithPreferredEmailAddress(i_success_guid)

        //update PersonBasicPersonBase info
        def o_person_update = personCompositeService.update(params)

        assertNotNull o_person_update
        assertEquals i_success_guid, o_person_update.guid
        assertEquals 3, o_person_update.emails?.size()
        assertEquals i_success_guid_personal, o_person_update.emails[0].guid
        assertEquals i_success_emailType_personal, o_person_update.emails[0].emailType
        assertEquals i_success_emailAddress_personal, o_person_update.emails[0].emailAddress
        assertEquals i_success_guid_personal, o_person_update.emails[1].guid
        assertEquals i_success_emailType_preferred, o_person_update.emails[1].emailType
        assertEquals i_success_emailAddress_personal, o_person_update.emails[1].emailAddress
        assertEquals i_success_guid_institution, o_person_update.emails[2].guid
        assertEquals i_success_emailType_institution, o_person_update.emails[2].emailType
        assertEquals i_success_emailAddress_institution, o_person_update.emails[2].emailAddress
    }

    //PUT- person update API
    @Test
    void testUpdatePreferredPersonEmailHavingExistingActiveEmailRecord() {
        PersonBasicPersonBase personBasicPersonBase = createPersonBasicPersonBase()

        assertNotNull personBasicPersonBase
        assertNotNull personBasicPersonBase.pidm

        PersonIdentificationNameCurrent personIdentificationNameCurrent = PersonIdentificationNameCurrent.findAllByPidmInList([personBasicPersonBase.pidm]).get(0)

        assertNotNull personIdentificationNameCurrent
        assertNotNull personIdentificationNameCurrent.pidm

        String i_success_guid = GlobalUniqueIdentifier.findByLdmNameAndDomainKey("persons", personIdentificationNameCurrent.pidm)?.guid

        assertNotNull i_success_guid
        Map params1 = updatePersonWithPreferredEmailAddress(i_success_guid)

        //create the email records
        def o_person_update1 = personCompositeService.update(params1)

        assertNotNull o_person_update1
        assertEquals i_success_guid, o_person_update1.guid
        assertEquals 3, o_person_update1.emails?.size()
        assertEquals i_success_guid_personal, o_person_update1.emails[0].guid
        assertEquals i_success_emailType_personal, o_person_update1.emails[0].emailType
        assertEquals i_success_emailAddress_personal, o_person_update1.emails[0].emailAddress
        assertEquals i_success_guid_personal, o_person_update1.emails[1].guid
        assertEquals i_success_emailType_preferred, o_person_update1.emails[1].emailType
        assertEquals i_success_emailAddress_personal, o_person_update1.emails[1].emailAddress
        assertEquals i_success_guid_institution, o_person_update1.emails[2].guid
        assertEquals i_success_emailType_institution, o_person_update1.emails[2].emailType
        assertEquals i_success_emailAddress_institution, o_person_update1.emails[2].emailAddress

        //update the email records
        Map params2 = [id    : i_success_guid,
                       emails: [[guid: i_success_guid_work, emailAddress: i_success_emailAddress_work, emailType: i_success_emailType_work], [emailAddress: i_success_emailAddress_work, emailType: i_success_emailType_preferred]]
        ]

        def o_person_update2 = personCompositeService.update(params2)

        assertNotNull o_person_update2
        assertEquals i_success_guid, o_person_update2.guid
        assertEquals 2, o_person_update2.emails?.size()
        assertEquals i_success_guid_work, o_person_update2.emails[0].guid
        assertEquals i_success_emailType_work, o_person_update2.emails[0].emailType
        assertEquals i_success_emailAddress_work, o_person_update2.emails[0].emailAddress
        assertEquals i_success_guid_work, o_person_update2.emails[1].guid
        assertEquals i_success_emailType_preferred, o_person_update2.emails[1].emailType
        assertEquals i_success_emailAddress_work, o_person_update2.emails[1].emailAddress
    }

    //PUT- person update API
    @Test
    void testUpdateFailedPersonCredentialWithExistingSSN() {
        String guid = GlobalUniqueIdentifier.findByLdmNameAndDomainKey('persons', '50199')?.guid
        Map params = updatePersonWithModifiedExistingSSN(guid)

        try {
            personCompositeService.update(params)
            fail("This should have failed as the course is missing")
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'ssn.value.exists.message'
        }

    }

    //PUT- person update API
    @Test
    void testUpdatePersonWithAlternateNameHavingBirthNameType() {
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

        Map params = [names              : [[lastName: i_success_last_name, middleName: i_success_middle_name, firstName: i_success_first_name, nameType: i_success_name_type, namePrefix: i_success_namePrefix, nameSuffix: i_success_nameSuffix, preferenceFirstName: i_success_preferenceFirstName]],
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
        Map params = [names : [[lastName: i_success_last_name, middleName: i_success_middle_name, firstName: i_success_first_name, nameType: i_success_name_type, namePrefix: i_success_namePrefix, nameSuffix: i_success_nameSuffix, preferenceFirstName: i_success_preferenceFirstName]],
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
                      credentials: [[credentialType: i_failed_update_credential_type, credentialId: i_failed_update_credential_id]],
        ]
        return params
    }


    private Map getParamsForPersonFilter() {
        return [
                action: [POST: "list"],
        ]
    }


    private Map newPersonWithAlternateNameHavingBirthNameType() {
        Map params = [names: [[lastName: i_success_last_name, middleName: i_success_middle_name, firstName: i_success_first_name, nameType: i_success_name_type, namePrefix: i_success_namePrefix, nameSuffix: i_success_nameSuffix, preferenceFirstName: i_success_preferenceFirstName], [lastName: i_success_alternate_last_name, middleName: i_success_alternate_middle_name, firstName: i_success_alternate_first_name, nameType: i_success_alternate_birth_name_type]]
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

}
