/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.common.GeneralCommonConstants
import net.hedtech.banner.general.overall.ldm.GlobalUniqueIdentifier
import net.hedtech.banner.general.overall.ldm.LdmService
import net.hedtech.banner.general.overall.ldm.v6.NonPersonDecorator
import net.hedtech.banner.general.person.PersonEmail
import net.hedtech.banner.general.person.PersonEmailService
import net.hedtech.banner.general.person.PersonIdentificationNameCurrent
import net.hedtech.banner.general.person.PersonTelephone
import net.hedtech.banner.general.person.PersonTelephoneService
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

import java.sql.Timestamp


class NonPersonCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    NonPersonCompositeService nonPersonCompositeService
    EmailTypeCompositeService emailTypeCompositeService
    PersonEmailService personEmailService
    NonPersonRoleCompositeService nonPersonRoleCompositeService
    PhoneTypeCompositeService phoneTypeCompositeService
    PersonTelephoneService personTelephoneService
    NonPersonPersonViewService nonPersonPersonViewService


    def person
    String guid

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        initializeDataReferences()
    }

    private void initializeDataReferences() {
        person = PersonIdentificationNameCurrent.fetchByBannerId("A00010018")
        guid = GlobalUniqueIdentifier.fetchByDomainKeyAndLdmName(person?.pidm?.toString(), GeneralCommonConstants.NON_PERSONS_LDM_NAME)?.guid
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
        assertTrue([CredentialType.BANNER_ID.versionToEnumMap["v6"]].containsAll(nonPersonDecorator.credentials.type))
        assertEquals nonPersonDecorator.credentials.value, [nonPersonPersonView.bannerId]

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
