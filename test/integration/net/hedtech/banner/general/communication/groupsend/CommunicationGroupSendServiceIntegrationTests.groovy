/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.groupsend

import net.hedtech.banner.general.communication.CommunicationManagementTestingSupport
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import net.hedtech.banner.general.communication.population.CommunicationPopulationQuery
import net.hedtech.banner.general.communication.population.CommunicationPopulationSelectionList
import net.hedtech.banner.general.communication.template.CommunicationEmailTemplate
import net.hedtech.banner.security.BannerAuthenticationToken
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Tests the group send service.
 */
class CommunicationGroupsendServiceIntegrationTests extends BaseIntegrationTestCase {

    def communicationOrganizationService
    def selfServiceBannerAuthenticationProvider
    def communicationPopulationQueryService
    def communicationPopulationExecutionService
    def communicationFolderService
    def communicationEmailTemplateService


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }


    @Test
    void testCreateGroupSend() {
        CommunicationOrganization organization = new CommunicationOrganization(name: "Test Org", isRoot: true)
        organization = communicationOrganizationService.create(organization) as CommunicationOrganization

        Authentication auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('BCMADMIN', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)
        assertTrue(auth instanceof BannerAuthenticationToken)
        BannerAuthenticationToken bannerAuthenticationToken = auth as BannerAuthenticationToken
        assertNotNull bannerAuthenticationToken.getPidm()

        CommunicationPopulationQuery populationQuery = new CommunicationPopulationQuery(
                folder: CommunicationManagementTestingSupport.newValidForCreateFolderWithSave(),
                createDate: new Date(),
                locked: false,
                name: "Test Query",
                sqlString: "select 2086 spriden_pidm from dual",
                published: true
        )
        populationQuery = communicationPopulationQueryService.create(populationQuery) as CommunicationPopulationQuery

        def populationId = communicationPopulationExecutionService.execute(populationQuery.id)
        CommunicationPopulationSelectionList population = CommunicationPopulationSelectionList.get(populationId)
        assertNotNull population.getId()

        CommunicationFolder folder = new CommunicationFolder(
                name: "test folder"
        )
        folder = communicationFolderService.create(folder) as CommunicationFolder
        assertNotNull folder.getId()

        CommunicationEmailTemplate emailTemplate = new CommunicationEmailTemplate(
                active: true,
                description: "test email template description",
                personal: true,
                name: "test",
                oneOff: false,
                published: true,
                folder: folder,
                content: "test content",
                fromList: "testfrom",
                subject: "test subject",
                toList: "testto"
        )
        communicationEmailTemplateService.create(emailTemplate) as CommunicationEmailTemplate
        assertNotNull emailTemplate.getId()

        CommunicationGroupSend groupSend = new CommunicationGroupSend(
                organization: organization,
                population: population,
                template: emailTemplate,
                ownerPidm: bannerAuthenticationToken.getPidm()
        )
        groupSend = communicationOrganizationService.create(groupSend) as CommunicationGroupSend
        assertNotNull groupSend.getId()
    }
}
