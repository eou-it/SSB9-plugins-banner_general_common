/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.common.GeneralCommonConstants
import net.hedtech.banner.general.common.GeneralValidationCommonConstants
import net.hedtech.banner.general.overall.ldm.GlobalUniqueIdentifier
import net.hedtech.banner.general.overall.ldm.LdmService
import net.hedtech.banner.general.overall.ldm.v6.CredentialV6
import net.hedtech.banner.general.overall.ldm.v6.NonPersonDecorator
import net.hedtech.banner.general.person.PersonEmail
import net.hedtech.banner.general.person.PersonEmailService
import net.hedtech.banner.general.person.PersonIdentificationNameCurrent
import net.hedtech.banner.general.person.PersonIdentificationNameCurrentService
import net.hedtech.banner.general.person.PersonTelephone
import net.hedtech.banner.general.person.PersonTelephoneService
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.general.person.ldm.v6.EmailV6
import net.hedtech.banner.general.person.ldm.v6.PhoneV6
import net.hedtech.banner.general.person.view.NonPersonPersonView
import net.hedtech.banner.general.person.view.NonPersonPersonViewService
import net.hedtech.banner.general.system.ldm.EmailTypeCompositeService
import net.hedtech.banner.general.system.ldm.PhoneTypeCompositeService
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
import org.junit.Before
import org.junit.Test

import javax.print.attribute.TextSyntax
import java.sql.Timestamp


class NonPersonCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    NonPersonCompositeService nonPersonCompositeService
    EmailTypeCompositeService emailTypeCompositeService
    PersonEmailService personEmailService
    NonPersonRoleCompositeService nonPersonRoleCompositeService
    PhoneTypeCompositeService phoneTypeCompositeService
    PersonTelephoneService personTelephoneService
    NonPersonPersonViewService nonPersonPersonViewService
    PersonIdentificationNameCurrentService personIdentificationNameCurrentService


    def person
    String guid

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    @Test
    void testList_NonPersons_v6() {
        setAcceptHeader("application/vnd.hedtech.integration.v6+json")
        def nonPersons = nonPersonCompositeService.list(params)
        assertFalse nonPersons.isEmpty()

        int actualCount = nonPersonCompositeService.count(params)
        assertNotNull actualCount

        int expectedCount = nonPersonPersonViewService.countByCriteria(params)
        assertNotNull expectedCount

        assertEquals actualCount, expectedCount

        List entitiesMapList = nonPersonPersonViewService.fetchAllWithGuidByCriteria(params, 500, 0)
        assertFalse entitiesMapList.isEmpty()

        assertEquals entitiesMapList.size(), nonPersons.size()

        Map vendorRoleMap = getPidmToVendorRoleMap(entitiesMapList.nonPersonPersonView.pidm)

        Iterator actualNonPersonIterator = nonPersons.iterator()
        Iterator expectedNonPersonIterator = entitiesMapList.iterator()

        while (actualNonPersonIterator.hasNext() && expectedNonPersonIterator.hasNext()) {
            NonPersonDecorator nonPersonDecorator = actualNonPersonIterator.next()
            Map entitiesMap = expectedNonPersonIterator.next()

            verifyResponse(nonPersonDecorator, entitiesMap, vendorRoleMap)
        }
    }

    @Test
    void testGet_NonPersons_v6() {
        setAcceptHeader("application/vnd.hedtech.integration.v6+json")
        def nonPersons = nonPersonCompositeService.list(params)
        assertFalse nonPersons.isEmpty()
        String guid = nonPersons[0].guid
        NonPersonDecorator decorator = nonPersonCompositeService.get(guid)
        assertNotNull decorator
        assertEquals guid, decorator.guid

        Map entitiesMap = nonPersonPersonViewService.fetchByGuid(guid)
        assertFalse entitiesMap.isEmpty()

        Map vendorRoleMap = getPidmToVendorRoleMap([entitiesMap.nonPersonPersonView.pidm])
        verifyResponse(decorator, entitiesMap, vendorRoleMap)
    }

    @Test
    void testGet_InvalidNonPersons_v6() {
        setAcceptHeader("application/vnd.hedtech.integration.v6+json")
        String guid = 'xxxxx'
        try {
            nonPersonCompositeService.get(guid)
            fail('Invalid guid')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'NotFoundException'
        }
    }


    @Test
    void testCreateWithMandatoryFileds(){
        prepareCreateRequest()
        NonPersonDecorator nonPersonDecorator = nonPersonCompositeService.create(params)
        assertNotNull nonPersonDecorator
        assertNotNull nonPersonDecorator.guid
        assertNotEquals nonPersonDecorator.guid, params.id
        assertEquals nonPersonDecorator.title, params.title
        assertNotNull nonPersonDecorator.roles
        assertFalse nonPersonDecorator.roles.isEmpty()
        assertEquals 1, nonPersonDecorator.roles.size()
        assertNotNull nonPersonDecorator.credentials
        assertFalse nonPersonDecorator.credentials.isEmpty()
    }

    @Test
    void testCreateWithGuidFiled(){
        prepareCreateRequest()
        params.id = 'XXXXX-XXXXX-XXXXXX'
        NonPersonDecorator nonPersonDecorator = nonPersonCompositeService.create(params)
        assertNotNull nonPersonDecorator
        assertNotNull nonPersonDecorator.guid
        assertEquals nonPersonDecorator.guid, params.id.toLowerCase()
        assertEquals nonPersonDecorator.title, params.title
        assertNotNull nonPersonDecorator.roles
        assertFalse nonPersonDecorator.roles.isEmpty()
        assertEquals 1, nonPersonDecorator.roles.size()
        assertNotNull nonPersonDecorator.credentials
        assertFalse nonPersonDecorator.credentials.isEmpty()
    }

    @Test
    void testCreateWithNullTitle(){
        prepareCreateRequest()
        params.title = ''
       def errorMessage = shouldFail(ApplicationException) {
            nonPersonCompositeService.create(params)
        }
        assertEquals errorMessage , 'title.required'
    }

    @Test
    void testCreateWithBannerIdCredentials(){
        prepareCreateRequest()
        params.credentials = [[type: 'bannerId', value:'TESTID']]
        NonPersonDecorator nonPersonDecorator = nonPersonCompositeService.create(params)
        assertNotNull nonPersonDecorator
        assertNotNull nonPersonDecorator.guid
        assertNotEquals nonPersonDecorator.guid, params.id
        assertEquals nonPersonDecorator.title, params.title
        assertNotNull nonPersonDecorator.roles
        assertFalse nonPersonDecorator.roles.isEmpty()
        assertEquals 1, nonPersonDecorator.roles.size()
        assertNotNull nonPersonDecorator.credentials
        assertFalse nonPersonDecorator.credentials.isEmpty()
        assertEquals nonPersonDecorator.credentials.get(0).type, 'bannerId'
        assertEquals nonPersonDecorator.credentials.get(0).value, 'TESTID'
    }

    @Test
    void testCreateWithElevateIdCredentials(){
        prepareCreateRequest()
        params.credentials = [[type: 'bannerId', value:'TESTID'],[type: 'elevateId', value:'ELEVATEID']]
        NonPersonDecorator nonPersonDecorator = nonPersonCompositeService.create(params)
        assertNotNull nonPersonDecorator
        assertNotNull nonPersonDecorator.guid
        assertNotEquals nonPersonDecorator.guid, params.id
        assertEquals nonPersonDecorator.title, params.title
        assertNotNull nonPersonDecorator.roles
        assertFalse nonPersonDecorator.roles.isEmpty()
        assertEquals 1, nonPersonDecorator.roles.size()
        assertNotNull nonPersonDecorator.credentials
        assertFalse nonPersonDecorator.credentials.isEmpty()
        assertEquals 2, nonPersonDecorator.credentials.size()
        assertEquals nonPersonDecorator.credentials.get(1).type, 'bannerId'
        assertEquals nonPersonDecorator.credentials.get(1).value, 'TESTID'
        assertEquals nonPersonDecorator.credentials.get(0).type, 'elevateId'
        assertEquals nonPersonDecorator.credentials.get(0).value, 'ELEVATEID'
    }

    @Test
    void testCreateWithColleaguePersonIdCredentials(){
        prepareCreateRequest()
        params.credentials = [[type: 'bannerId', value:'TESTID'],[type: 'elevateId', value:'ELEVATEID'],[type:'colleaguePersonId',value:"COLLEAGUEPERSONID"]]
        NonPersonDecorator nonPersonDecorator = nonPersonCompositeService.create(params)
        assertNotNull nonPersonDecorator
        assertNotNull nonPersonDecorator.guid
        assertNotEquals nonPersonDecorator.guid, params.id
        assertEquals nonPersonDecorator.title, params.title
        assertNotNull nonPersonDecorator.roles
        assertFalse nonPersonDecorator.roles.isEmpty()
        assertEquals 1, nonPersonDecorator.roles.size()
        assertNotNull nonPersonDecorator.credentials
        assertFalse nonPersonDecorator.credentials.isEmpty()
        assertEquals 3, nonPersonDecorator.credentials.size()
        assertEquals nonPersonDecorator.credentials.get(2).type, 'bannerId'
        assertEquals nonPersonDecorator.credentials.get(2).value, 'TESTID'
        assertEquals nonPersonDecorator.credentials.get(1).type, 'colleaguePersonId'
        assertEquals nonPersonDecorator.credentials.get(1).value, 'COLLEAGUEPERSONID'
        assertEquals nonPersonDecorator.credentials.get(0).type, 'elevateId'
        assertEquals nonPersonDecorator.credentials.get(0).value, 'ELEVATEID'
    }

    @Test
    void testCreateWithEmptyCredential(){
        prepareCreateRequest()
        params.credentials = [[type: 'bannerId', value:'']]
        def errorMessage = shouldFail(ApplicationException) {
            nonPersonCompositeService.create(params)
        }
        assertEquals errorMessage , 'invalid.credentialType'
    }

    @Test
    void testCreateWithWrongCredentialType(){
        prepareCreateRequest()
        params.credentials = [[type: 'BannerId', value:'123']]
        def errorMessage = shouldFail(ApplicationException) {
            nonPersonCompositeService.create(params)
        }
        assertEquals errorMessage , 'invalid.credentialType'
    }

    @Test
    void testCreateWithExistBannerIdCredentials(){
        prepareCreateRequest()
        params.credentials = [[type: 'bannerId', value:'TESTID']]
        NonPersonDecorator nonPersonDecorator = nonPersonCompositeService.create(params)
        assertNotNull nonPersonDecorator
        assertNotNull nonPersonDecorator.guid
        assertNotEquals nonPersonDecorator.guid, params.id
        assertEquals nonPersonDecorator.title, params.title
        assertNotNull nonPersonDecorator.roles
        assertFalse nonPersonDecorator.roles.isEmpty()
        assertEquals 1, nonPersonDecorator.roles.size()
        assertNotNull nonPersonDecorator.credentials
        assertFalse nonPersonDecorator.credentials.isEmpty()
        assertEquals nonPersonDecorator.credentials.get(0).type, 'bannerId'
        assertEquals nonPersonDecorator.credentials.get(0).value, 'TESTID'

        def errorMessage = shouldFail(ApplicationException) {
            nonPersonCompositeService.create(params)
        }
        assertEquals errorMessage , 'bannerId.already.exists'
    }


    @Test
    void testUpdateWithNoChanges(){
        prepareCreateRequest()
        NonPersonDecorator nonPersonDecorator = nonPersonCompositeService.create(params)
        assertNotNull nonPersonDecorator
        assertNotNull nonPersonDecorator.guid
        assertNotEquals nonPersonDecorator.guid, params.id
        assertEquals nonPersonDecorator.title, params.title
        assertNotNull nonPersonDecorator.roles
        assertFalse nonPersonDecorator.roles.isEmpty()
        assertEquals 1, nonPersonDecorator.roles.size()
        assertNotNull nonPersonDecorator.credentials
        assertFalse nonPersonDecorator.credentials.isEmpty()

        params.id = nonPersonDecorator.guid
        NonPersonDecorator updateNonPersonDecorator = nonPersonCompositeService.update(params)
        assertEquals nonPersonDecorator.guid, updateNonPersonDecorator.guid
        assertEquals nonPersonDecorator.title, updateNonPersonDecorator.title

    }

    @Test
    void testUpdateWithTitleChanges(){
        prepareCreateRequest()
        NonPersonDecorator nonPersonDecorator = nonPersonCompositeService.create(params)
        assertNotNull nonPersonDecorator
        assertNotNull nonPersonDecorator.guid
        assertNotEquals nonPersonDecorator.guid, params.id
        assertEquals nonPersonDecorator.title, params.title
        assertNotNull nonPersonDecorator.roles
        assertFalse nonPersonDecorator.roles.isEmpty()
        assertEquals 1, nonPersonDecorator.roles.size()
        assertNotNull nonPersonDecorator.credentials
        assertFalse nonPersonDecorator.credentials.isEmpty()

        params.id = nonPersonDecorator.guid
        params.title = 'update non person'
        NonPersonDecorator updateNonPersonDecorator = nonPersonCompositeService.update(params)
        assertEquals nonPersonDecorator.guid, updateNonPersonDecorator.guid
        assertNotEquals nonPersonDecorator.title, updateNonPersonDecorator.title
        assertEquals updateNonPersonDecorator.title, params.title
    }

    @Test
    void testUpdateWithBannerId(){
        prepareCreateRequest()
        params.credentials = [[type: 'bannerId', value:'TESTID']]
        NonPersonDecorator nonPersonDecorator = nonPersonCompositeService.create(params)
        assertNotNull nonPersonDecorator
        assertNotNull nonPersonDecorator.guid
        assertNotEquals nonPersonDecorator.guid, params.id
        assertEquals nonPersonDecorator.title, params.title
        assertNotNull nonPersonDecorator.roles
        assertFalse nonPersonDecorator.roles.isEmpty()
        assertEquals 1, nonPersonDecorator.roles.size()
        assertNotNull nonPersonDecorator.credentials
        assertFalse nonPersonDecorator.credentials.isEmpty()
        assertEquals nonPersonDecorator.credentials.get(0).type, 'bannerId'
        assertEquals nonPersonDecorator.credentials.get(0).value, 'TESTID'

        params.id = nonPersonDecorator.guid
        params.credentials = [[type: 'bannerId', value:'UPDATEID']]
        NonPersonDecorator updateNonPersonDecorator = nonPersonCompositeService.update(params)
        assertEquals nonPersonDecorator.guid, updateNonPersonDecorator.guid
        assertEquals nonPersonDecorator.title, updateNonPersonDecorator.title
        assertNotNull updateNonPersonDecorator.roles
        assertFalse updateNonPersonDecorator.roles.isEmpty()
        assertEquals 1, updateNonPersonDecorator.roles.size()
        assertNotNull updateNonPersonDecorator.credentials
        assertFalse updateNonPersonDecorator.credentials.isEmpty()
        assertEquals updateNonPersonDecorator.credentials.get(0).type, 'bannerId'
        assertEquals updateNonPersonDecorator.credentials.get(0).value, 'UPDATEID'


    }


    private void prepareCreateRequest(){
        params.put("title", "non person")
        params.put("id", GeneralValidationCommonConstants.NIL_GUID)
    }

    private void verifyResponse(NonPersonDecorator nonPersonDecorator, Map entitiesMap, Map vendorRoleMap) {
        NonPersonPersonView nonPersonPersonView = entitiesMap.nonPersonPersonView
        GlobalUniqueIdentifier globalUniqueIdentifier = entitiesMap.globalUniqueIdentifier


        Map bannerEmailTypeToHedmEmailTypeMap = emailTypeCompositeService.getBannerEmailTypeToHedmV6EmailTypeMap()
        assertFalse bannerEmailTypeToHedmEmailTypeMap.isEmpty()

        Map emailTypeCodeToGuidMap = emailTypeCompositeService.getEmailTypeCodeToGuidMap(bannerEmailTypeToHedmEmailTypeMap.keySet())
        assertFalse emailTypeCodeToGuidMap.isEmpty()


        Map bannerPhoneTypeToHedmV6PhoneTypeMap = phoneTypeCompositeService.getBannerPhoneTypeToHedmV6PhoneTypeMap()
        assertFalse bannerPhoneTypeToHedmV6PhoneTypeMap.isEmpty()

        Map phoneTypeCodeToGuidMap = phoneTypeCompositeService.getPhoneTypeCodeToGuidMap(bannerPhoneTypeToHedmV6PhoneTypeMap.keySet())
        assertFalse phoneTypeCodeToGuidMap.isEmpty()

        List<PersonEmail> personEmails = personEmailService.fetchAllActiveEmails([nonPersonPersonView.pidm], bannerEmailTypeToHedmEmailTypeMap.keySet())
        assertEquals personEmails.size(), nonPersonDecorator.emails?.size()?:0

        List<PersonTelephone> personTelephoneList = personTelephoneService.fetchAllActiveByPidmInListAndTelephoneTypeCodeInList([nonPersonPersonView.pidm], bannerPhoneTypeToHedmV6PhoneTypeMap.keySet())
        assertEquals personTelephoneList.size(), nonPersonDecorator.phones?.size()?:0

        String vendroRoleName = OrganizationRoleName.VENDOR.versionToEnumMap["v6"]
        String affiliateRoleName = OrganizationRoleName.AFFILIATE.versionToEnumMap["v6"]


        assertEquals nonPersonDecorator.title, nonPersonPersonView.lastName
        assertEquals nonPersonDecorator.guid, globalUniqueIdentifier.guid
        CredentialV6 credentialV6 =  nonPersonDecorator.credentials.find{
            it.type == CredentialType.BANNER_ID.versionToEnumMap["v6"]
        }
        assertNotNull credentialV6
       assertEquals credentialV6.value, nonPersonPersonView.bannerId

        if (vendorRoleMap.containsKey(nonPersonPersonView.pidm)) {
            assertEquals nonPersonDecorator.roles.size(), 2
            assertEquals nonPersonDecorator.roles.role, [vendroRoleName, affiliateRoleName]
        } else {
            assertEquals nonPersonDecorator.roles.size(), 1
            assertEquals nonPersonDecorator.roles.role, [affiliateRoleName]
        }

        if (nonPersonDecorator.emails) {
            assertFalse personEmails.isEmpty()
            personEmails.each {
                PersonEmail personEmail = it
                EmailV6 emailV6 = nonPersonDecorator.emails.find {it.type.code == personEmail.emailType.code}
                assertNotNull emailV6
                assertEquals emailV6.address, personEmail.emailAddress
                assertEquals emailV6.type.code, personEmail.emailType.code
                assertEquals emailV6.type.emailType, bannerEmailTypeToHedmEmailTypeMap.get(personEmail.emailType.code)
                assertEquals emailV6.type.detail.id, emailTypeCodeToGuidMap.get(personEmail.emailType.code)
                if (emailV6.preference) {
                    assertEquals emailV6.preference, 'primary'
                }
            }
        }
        if (nonPersonDecorator.phones) {
            assertFalse personTelephoneList.isEmpty()
            personTelephoneList.each {
                PersonTelephone personTelephone = it
                PhoneV6 phoneV6 = nonPersonDecorator.phones.find {it.type.code == personTelephone.telephoneType.code }
                assertNotNull phoneV6
                assertEquals phoneV6.type.code, personTelephone.telephoneType.code
                assertEquals phoneV6.type.phoneType, bannerPhoneTypeToHedmV6PhoneTypeMap.get(personTelephone.telephoneType.code)
                assertEquals phoneV6.type.detail.id, phoneTypeCodeToGuidMap.get(personTelephone.telephoneType.code)
                assertEquals phoneV6.countryCallingCode, personTelephone.countryPhone
                if (phoneV6.preference) {
                    assertEquals phoneV6.preference, 'primary'
                }
            }
        }


    }


    private def getPidmToVendorRoleMap(List<Integer> pidms) {
        def pidmToVendorRoleMap = [:]
        List<Object[]> rows = nonPersonRoleCompositeService.fetchVendorsByPIDMs(pidms)
        rows?.each {
            BigDecimal bdPidm = it[0]
            Timestamp startDate = it[1]
            Timestamp endDate = it[2]
            pidmToVendorRoleMap.put(bdPidm.toInteger(), [role: OrganizationRoleName.VENDOR, startDate: startDate, endDate: endDate])
        }
        return pidmToVendorRoleMap
    }

    private void setAcceptHeader(String mediaType) {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Accept", mediaType)
    }
}
