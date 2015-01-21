/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.person.ldm
import org.junit.Before
import org.junit.Test
import org.junit.After

import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.overall.ldm.GlobalUniqueIdentifier
import net.hedtech.banner.general.person.PersonBasicPersonBase
import net.hedtech.banner.general.person.PersonIdentificationName
import net.hedtech.banner.general.person.PersonIdentificationNameAlternate
import net.hedtech.banner.general.person.PersonIdentificationNameCurrent
import net.hedtech.banner.general.system.CitizenType
import net.hedtech.banner.general.system.Ethnicity
import net.hedtech.banner.general.system.Legacy
import net.hedtech.banner.general.system.MaritalStatus
import net.hedtech.banner.general.system.Nation
import net.hedtech.banner.general.system.Religion
import net.hedtech.banner.general.system.State
import net.hedtech.banner.general.system.UnitOfMeasure
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.Ignore


class PersonCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    def personCompositeService
    def personIdentificationNameService
    def personBasicPersonBaseService

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
    def i_success_emailAddress_personal = "xyz@ellucian.com"
    def i_success_emailType_personal = "Personal"
    def i_success_emailAddress_institution = "abc@ellucian.com"
    def i_success_emailType_institution = "Institution"
    def i_success_emailAddress_work = "123@ellucian.com"
    def i_success_emailType_work = "Work"
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

    @Ignore
    @Test
    void testListQapiWithValidFirstAndLastName() {
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

    @Ignore
    @Test
    void testListQapiWithInValidFirstAndLastName() {
        Map params = getParamsWithReqiuredFields()
        params.names[0].firstName = "MarkTT"
        params.names[0].lastName = "Kole"
        def persons = personCompositeService.list(params)

        assertNotNull persons
        assertTrue persons.isEmpty()
    }

    @Ignore
    @Test
    void testListQapiWithInValidDateOfBirth() {
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


    @Test
    void testUpdatePersonFirstNameAndLastNameChange() {
        PersonBasicPersonBase personBasicPersonBase = createPersonBasicPersonBase()
        PersonIdentificationNameCurrent personIdentificationNameCurrent = PersonIdentificationNameCurrent.findAllByPidmInList([personBasicPersonBase.pidm]).get(0)
        GlobalUniqueIdentifier uniqueIdentifier = GlobalUniqueIdentifier.findByLdmNameAndDomainKey("persons", personIdentificationNameCurrent.pidm)
        assertNotNull uniqueIdentifier
        Map params = getPersonWithFirstNameChangeRequest(personIdentificationNameCurrent, uniqueIdentifier.guid);

        //update person with FirstName change
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
        assertEquals 'CCCCC', personBasicPersonBase.ssn
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

        def ethnicities = GlobalUniqueIdentifier.findAllByLdmName('ethnicities')
        Map newEthnicities = getPersonWithNewEthniciiesyRequest(personIdentificationNameCurrent, uniqueIdentifier.guid, ethnicities)

        // create new Ethnicity through update()
        def ethnicityDetail = personCompositeService.update(newEthnicities).ethnicityDetail

        //assertEquals ethnicityDetail.size ,1
        assertEquals ethnicityDetail.guid, ethnicities[0].guid

        //  update Ethnicity using same request
        def unchangedEthnicityDetail = personCompositeService.update(newEthnicities).ethnicityDetail
        assertEquals unchangedEthnicityDetail.guid, ethnicities[0].guid

        //update with new Ethnicity
        Map modifiedEthnicity = getPersonWithModifiedEthniciiesyRequest(personIdentificationNameCurrent, uniqueIdentifier.guid, ethnicities)

        // create new Ethnicity through update()
        def modifiedEthnicityDetail = personCompositeService.update(modifiedEthnicity).ethnicityDetail
        assertEquals modifiedEthnicityDetail.guid, ethnicities[1].guid

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
        Map params = updatePersonWithEmailAddress(i_success_guid)

        //update PersonBasicPersonBase info
        def o_person_update = personCompositeService.update(params)

        assertNotNull o_person_update
        assertEquals i_success_guid, o_person_update.guid
        assertEquals 2, o_person_update.emails?.size()
        assertEquals i_success_emailType_personal, o_person_update.emails[0].emailType
        assertEquals i_success_emailAddress_personal, o_person_update.emails[0].emailAddress
        assertEquals i_success_emailType_institution, o_person_update.emails[1].emailType
        assertEquals i_success_emailAddress_institution, o_person_update.emails[1].emailAddress
    }

    //PUT- person update API
    @Test
    void testUpdatePersonEmailHavingExistingActiveEmailRecord() {
        PersonBasicPersonBase personBasicPersonBase = createPersonBasicPersonBase()

        assertNotNull personBasicPersonBase
        assertNotNull personBasicPersonBase.pidm

        PersonIdentificationNameCurrent personIdentificationNameCurrent = PersonIdentificationNameCurrent.findAllByPidmInList([personBasicPersonBase.pidm]).get(0)

        assertNotNull personIdentificationNameCurrent
        assertNotNull personIdentificationNameCurrent.pidm

        String i_success_guid = GlobalUniqueIdentifier.findByLdmNameAndDomainKey("persons", personIdentificationNameCurrent.pidm)?.guid

        assertNotNull i_success_guid
        Map params1 = updatePersonWithEmailAddress(i_success_guid)

        //create the email records
        def o_person_update1 = personCompositeService.update(params1)

        assertNotNull o_person_update1
        assertEquals i_success_guid, o_person_update1.guid
        assertEquals 2, o_person_update1.emails?.size()
        assertEquals i_success_emailType_personal, o_person_update1.emails[0].emailType
        assertEquals i_success_emailAddress_personal, o_person_update1.emails[0].emailAddress
        assertEquals i_success_emailType_institution, o_person_update1.emails[1].emailType
        assertEquals i_success_emailAddress_institution, o_person_update1.emails[1].emailAddress

        //update the email records
        Map params2 = [id    : i_success_guid,
                       emails: [[emailAddress: i_success_emailAddress_work, emailType: i_success_emailType_work]]
        ]

        def o_person_update2 = personCompositeService.update(params2)

        assertNotNull o_person_update2
        assertEquals i_success_guid, o_person_update2.guid
        assertEquals 1, o_person_update2.emails?.size()
        assertEquals i_success_emailType_work, o_person_update2.emails[0].emailType
        assertEquals i_success_emailAddress_work, o_person_update2.emails[0].emailAddress
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
        Map params = [id         : guid,
                      names      : [[lastName: personIdentificationNameCurrent.lastName, middleName: personIdentificationNameCurrent.middleName, firstName: 'CCCCCC', nameType: 'Primary']]
        ]
        return params
    }


    private Map getPersonWithLastNameChangeRequest(personIdentificationNameCurrent, guid) {
        Map params = [id         : guid,
                      names      : [[lastName: 'CCCCCC', middleName: personIdentificationNameCurrent.middleName, firstName: personIdentificationNameCurrent.firstName, nameType: 'Primary']]
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
        Map params = [id         : guid,
                      names      : [[lastName: personIdentificationNameCurrent.lastName, middleName: personIdentificationNameCurrent.middleName, firstName: personIdentificationNameCurrent.firstName, nameType: 'Primary', namePrefix: 'CCCCC', nameSuffix: 'CCCCC', preferenceFirstName: 'CCCCC']],
                      credentials: [[credentialType: 'Social Security Number', credentialId: 'CCCCC']],
                      sex        : 'Female'

        ]
        return params
    }


    private Map getPersonWithNewAdressRequest(personIdentificationNameCurrent, guid) {
        Map params = [id         : guid,
                      names      : [[lastName: personIdentificationNameCurrent.lastName, middleName: personIdentificationNameCurrent.middleName, firstName: personIdentificationNameCurrent.firstName, nameType: 'Primary', namePrefix: 'CCCCC', nameSuffix: 'CCCCC', preferenceFirstName: 'CCCCC']],
                      credentials: [[credentialType: 'Social Security Number', credentialId: 'TTTTT']],
                      sex        : 'Male',
                      addresses  : [[addressType: 'Mailing', city: 'Southeastern', state: 'CA', streetLine1: '5890 139th Ave', zip: '19398'], [addressType: 'Home', city: 'Pavo', state: 'GA', streetLine1: '123 Main Line', zip: '31778']]
        ]
        return params
    }


    private Map getPersonWithModifiedAdressRequest(personIdentificationNameCurrent, guid) {
        Map params = [id         : guid,
                      names      : [[lastName: personIdentificationNameCurrent.lastName, middleName: personIdentificationNameCurrent.middleName, firstName: personIdentificationNameCurrent.firstName, nameType: 'Primary', namePrefix: 'CCCCC', nameSuffix: 'CCCCC', preferenceFirstName: 'CCCCC']],
                      credentials: [[credentialType: 'Social Security Number', credentialId: 'TTTTT']],
                      sex        : 'Male',
                      addresses  : [[addressType: 'Mailing', city: 'Pavo', state: 'GA', streetLine1: '123 Main Line', zip: '31778'], [addressType: 'Home', city: 'Southeastern', state: 'CA', streetLine1: '5890 139th Ave', zip: '19398']]
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
        Map params = [id         : guid,
                      names      : [[lastName: personIdentificationNameCurrent.lastName, middleName: personIdentificationNameCurrent.middleName, firstName: personIdentificationNameCurrent.firstName, nameType: 'Primary', namePrefix: 'TTTTT', nameSuffix: 'TTTTT', preferenceFirstName: 'TTTTT']],
                      credentials: [[credentialType: 'Social Security Number', credentialId: 'TTTTT']],
                      sex        : 'Male',
                      phones     : [[phoneNumber: '6107435302', phoneType: 'Mobile'], [phoneNumber: '2297795715', phoneType: 'Home']]
        ]
        return params
    }


    private Map getPersonWithModifiedPhonesRequest(personIdentificationNameCurrent, guid) {
        Map params = [id         : guid,
                      names      : [[lastName: personIdentificationNameCurrent.lastName, middleName: personIdentificationNameCurrent.middleName, firstName: personIdentificationNameCurrent.firstName, nameType: 'Primary', namePrefix: 'TTTTT', nameSuffix: 'TTTTT', preferenceFirstName: 'TTTTT']],
                      credentials: [[credentialType: 'Social Security Number', credentialId: 'TTTTT']],
                      sex        : 'Male',
                      phones     : [[phoneNumber: '6107435333', phoneType: 'Mobile'], [phoneNumber: '2297795777', phoneType: 'Home']]
        ]
        return params
    }


    private Map getPersonWithNewRacesRequest(personIdentificationNameCurrent, guid, races) {
        Map params = [id         : guid,
                      names      : [[lastName: personIdentificationNameCurrent.lastName, middleName: personIdentificationNameCurrent.middleName, firstName: personIdentificationNameCurrent.firstName, nameType: 'Primary', namePrefix: 'TTTTT', nameSuffix: 'TTTTT', preferenceFirstName: 'TTTTT']],
                      credentials: [[credentialType: 'Social Security Number', credentialId: 'TTTTT']],
                      sex        : 'Male',
                      races      : [[guid: races[0].guid]]
        ]
        return params
    }


    private Map getPersonWithModifiedRacesRequest(personIdentificationNameCurrent, guid, races) {
        Map params = [id         : guid,
                      names      : [[lastName: personIdentificationNameCurrent.lastName, middleName: personIdentificationNameCurrent.middleName, firstName: personIdentificationNameCurrent.firstName, nameType: 'Primary', namePrefix: 'TTTTT', nameSuffix: 'TTTTT', preferenceFirstName: 'TTTTT']],
                      credentials: [[credentialType: 'Social Security Number', credentialId: 'TTTTT']],
                      sex        : 'Male',
                      races      : [[guid: races[0].guid], [guid: races[1].guid]]
        ]
        return params
    }


    private Map getPersonWithNewEthniciiesyRequest(personIdentificationNameCurrent, guid, ethniciies) {
        Map params = [id             : guid,
                      names          : [[lastName: personIdentificationNameCurrent.lastName, middleName: personIdentificationNameCurrent.middleName, firstName: personIdentificationNameCurrent.firstName, nameType: 'Primary', namePrefix: 'TTTTT', nameSuffix: 'TTTTT', preferenceFirstName: 'TTTTT']],
                      credentials    : [[credentialType: 'Social Security Number', credentialId: 'TTTTT']],
                      sex            : 'Male',
                      ethnicityDetail: [[guid: ethniciies[0].guid]]
        ]
        return params
    }


    private Map getPersonWithModifiedEthniciiesyRequest(personIdentificationNameCurrent, guid, ethniciies) {
        Map params = [id             : guid,
                      names          : [[lastName: personIdentificationNameCurrent.lastName, middleName: personIdentificationNameCurrent.middleName, firstName: personIdentificationNameCurrent.firstName, nameType: 'Primary', namePrefix: 'TTTTT', nameSuffix: 'TTTTT', preferenceFirstName: 'TTTTT']],
                      credentials    : [[credentialType: 'Social Security Number', credentialId: 'TTTTT']],
                      sex            : 'Male',
                      ethnicityDetail: [[guid: ethniciies[1].guid]]
        ]
        return params
    }


    private Map updatePersonWithEmailAddress(String guid) {
        Map params = [id    : guid,
                      emails: [[emailAddress: i_success_emailAddress_personal, emailType: i_success_emailType_personal], [emailAddress: i_success_emailAddress_institution, emailType: i_success_emailType_institution]]
        ]

        return params
    }


    private Map newPersonWithAddressRequest() {
        Map params = [names      : [[lastName: i_success_last_name, middleName: i_success_middle_name, firstName: i_success_first_name, nameType: i_success_name_type, namePrefix: i_success_namePrefix, nameSuffix: i_success_nameSuffix, preferenceFirstName: i_success_preferenceFirstName]],
                      addresses  : [[addressType: i_success_address_type_1, city: i_success_city, state: i_success_state, streetLine1: i_success_street_line1, zip: i_success_zip], [addressType: i_success_address_type_2, city: i_success_city, streetLine1: i_success_street_line1]]
        ]

        return params
    }

}
