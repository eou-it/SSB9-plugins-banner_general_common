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
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
import org.junit.Before
import org.junit.Test


class PersonCredentialCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    def personCredentialCompositeService

    def person
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
        bannerSourcedId = ImsSourcedIdBase.findByPidm(person?.pidm)?.sourcedId
        bannerUserName = ThirdPartyAccess.findByPidm(person?.pidm)?.externalUser
        bannerUdcId = PidmAndUDCIdMapping.findByPidm(person?.pidm)?.udcId
    }

    @Test
    void testList_PersonsCredentials_v6() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Accept", "application/vnd.hedtech.integration.v6+json")
        def personsCredentials = personCredentialCompositeService.list([max: '500', offset: '0'])
        assertTrue personsCredentials.size() > 0
    }

    @Test
    void testCount_PersonsCredentials_v6() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Accept", "application/vnd.hedtech.integration.v6+json")
        def personsCredentialsCount = personCredentialCompositeService.count([max: '500', offset: '0'])
        assertTrue personsCredentialsCount > 0
    }

    @Test
    void testGet_PersonCredentials_v6() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Accept", "application/vnd.hedtech.integration.v6+json")
        String guid = GlobalUniqueIdentifier.fetchByDomainKeyAndLdmName(person?.pidm?.toString(), GeneralCommonConstants.PERSONS_LDM_NAME)?.guid
        assertNotNull guid
        def personCredentials = personCredentialCompositeService.get(guid)
        assertNotNull personCredentials
        assertEquals guid, personCredentials.guid
        assertEquals person.bannerId, personCredentials.credentials.find { it.type == "bannerId" }.value
        assertEquals bannerSourcedId, personCredentials.credentials.find { it.type == "bannerSourcedId" }.value
        assertEquals bannerUserName, personCredentials.credentials.find { it.type == "bannerUserName" }.value
        assertEquals bannerUdcId, personCredentials.credentials.find { it.type == "bannerUdcId" }.value
    }

    @Test
    void testGet_InvalidPersonCredentials_v6() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Accept", "application/vnd.hedtech.integration.v6+json")
        String guid = 'xxxxx'
        try {
            personCredentialCompositeService.get(guid)
            fail('Invalid guid')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'NotFoundException'
        }
    }


}
