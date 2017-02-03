/*******************************************************************************
 Copyright 2016-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.common.GeneralCommonConstants
import net.hedtech.banner.general.overall.ImsSourcedIdBase
import net.hedtech.banner.general.overall.PidmAndUDCIdMapping
import net.hedtech.banner.general.overall.ThirdPartyAccess
import net.hedtech.banner.general.overall.ldm.GlobalUniqueIdentifier
import net.hedtech.banner.general.overall.ldm.LdmService
import net.hedtech.banner.general.overall.ldm.v6.PersonCredentialsV6
import net.hedtech.banner.general.person.PersonIdentificationNameCurrent
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
import org.junit.Before
import org.junit.Test


class PersonCredentialV6CompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    def personCredentialV6CompositeService

    def person
    String guid
    String bannerSourcedId
    String bannerUserName
    String bannerUdcId

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        initializeDataReferences()
    }

    private void initializeDataReferences() {
        person = PersonUtility.getPerson("HOSP0001")
        guid = GlobalUniqueIdentifier.fetchByDomainKeyAndLdmName(person?.pidm?.toString(), GeneralCommonConstants.PERSONS_LDM_NAME)?.guid
        bannerSourcedId = ImsSourcedIdBase.findByPidm(person?.pidm)?.sourcedId
        bannerUserName = ThirdPartyAccess.findByPidm(person?.pidm)?.externalUser
        bannerUdcId = PidmAndUDCIdMapping.findByPidm(person?.pidm)?.udcId
    }

    @Test
    void testList_PersonsCredentials_v6() {
        setAcceptHeader("application/vnd.hedtech.integration.v6+json")
        def personsCredentials = personCredentialV6CompositeService.list([max: '500', offset: '0'])
        assertTrue personsCredentials.size() <= 500
        assertTrue personsCredentials.size() > 0
        assertTrue personsCredentials.size() <= personCredentialV6CompositeService.count([max: '500', offset: '0'])
        PersonCredentialsV6 decorator = personsCredentials[0]
        assertNotNull decorator
        GlobalUniqueIdentifier globalUniqueIdentifier = GlobalUniqueIdentifier.fetchByGuid(GeneralCommonConstants.PERSONS_LDM_NAME, decorator.guid)
        assertNotNull globalUniqueIdentifier
        assertNotNull globalUniqueIdentifier.domainKey
        Integer pidm = globalUniqueIdentifier.domainKey.toInteger()
        String bannerId = PersonIdentificationNameCurrent.fetchByPidm(pidm)?.bannerId
        assertEquals bannerId, decorator.credentials.find { it.type == "bannerId" }.value
        String bannerSourcedId = ImsSourcedIdBase.findByPidm(pidm)?.sourcedId
        if (bannerSourcedId) {
            assertEquals bannerSourcedId, decorator.credentials.find { it.type == "bannerSourcedId" }.value
        }
        String bannerUserName = ThirdPartyAccess.findByPidm(pidm)?.externalUser
        if (bannerUserName) {
            assertEquals bannerUserName, decorator.credentials.find { it.type == "bannerUserName" }.value
        }
        String bannerUdcId = PidmAndUDCIdMapping.findByPidm(pidm)?.udcId
        if (bannerUdcId) {
            assertEquals bannerUdcId, decorator.credentials.find { it.type == "bannerUdcId" }.value
        }
    }

    @Test
    void testGet_PersonCredentials_v6() {
        setAcceptHeader("application/vnd.hedtech.integration.v6+json")
        PersonCredentialsV6 decorator = personCredentialV6CompositeService.get(guid)
        assertNotNull decorator
        assertEquals guid, decorator.guid
        assertEquals person.bannerId, decorator.credentials.find { it.type == "bannerId" }.value
        assertEquals bannerSourcedId, decorator.credentials.find { it.type == "bannerSourcedId" }.value
        assertEquals bannerUserName, decorator.credentials.find { it.type == "bannerUserName" }.value
        assertEquals bannerUdcId, decorator.credentials.find { it.type == "bannerUdcId" }.value
    }

    @Test
    void testGet_InvalidPersonCredentials_v6() {
        setAcceptHeader("application/vnd.hedtech.integration.v6+json")
        String guid = 'xxxxx'
        try {
            personCredentialV6CompositeService.get(guid)
            fail('Invalid guid')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'NotFoundException'
        }
    }

    private void setAcceptHeader(String mediaType) {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Accept", mediaType)
    }


}
