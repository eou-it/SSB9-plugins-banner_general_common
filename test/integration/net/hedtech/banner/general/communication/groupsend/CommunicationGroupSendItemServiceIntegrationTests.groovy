/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.groupsend

import groovy.sql.Sql
import net.hedtech.banner.general.communication.CommunicationManagementTestingSupport
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQuery
import net.hedtech.banner.general.communication.population.selectionlist.CommunicationPopulationSelectionList
import net.hedtech.banner.general.communication.template.CommunicationEmailTemplate
import net.hedtech.banner.security.BannerAuthenticationToken
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Tests the group send dao service.
 */
class CommunicationGroupSendItemServiceIntegrationTests extends BaseIntegrationTestCase {

    def communicationOrganizationService
    def communicationGroupSendService
    def communicationGroupSendItemService
    def selfServiceBannerAuthenticationProvider
    def communicationPopulationQueryCompositeService
    def communicationPopulationExecutionService
    def communicationFolderService
    def communicationTemplateService
    def communicationEmailTemplateService

    CommunicationOrganization organization
    BannerAuthenticationToken bannerAuthenticationToken
    CommunicationPopulationQuery populationQuery
    CommunicationEmailTemplate emailTemplate
    CommunicationPopulationSelectionList population


    public void cleanUp() {
        def sql
        try {
            sessionFactory.currentSession.with { session ->
                sql = new Sql( session.connection() )
                sql.executeUpdate( "Delete from GCRORAN" )
            }
        } finally {
            sql?.close()
        }
    }


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()

        cleanUp()
        organization = new CommunicationOrganization( name: "Test Org" )
        organization = communicationOrganizationService.create( organization ) as CommunicationOrganization

        def auth = selfServiceBannerAuthenticationProvider.authenticate( new UsernamePasswordAuthenticationToken( 'BCMADMIN', '111111' ) )
        assertNotNull auth
        SecurityContextHolder.getContext().setAuthentication( auth )
        assertTrue( auth instanceof BannerAuthenticationToken )
        bannerAuthenticationToken = auth as BannerAuthenticationToken
        assertNotNull bannerAuthenticationToken.getPidm()

        populationQuery = new CommunicationPopulationQuery(
                folder: CommunicationManagementTestingSupport.newValidForCreateFolderWithSave(),
                createDate: new Date(),
                name: "Test Query",
                sqlString: "select 2086 spriden_pidm from dual"
        )
        populationQuery = communicationPopulationQueryCompositeService.createPopulationQuery( populationQuery )
        populationQuery = communicationPopulationQueryCompositeService.publishPopulationQuery( populationQuery )

        CommunicationFolder folder = CommunicationFolder.fetchByName( "test folder" )
        if (!folder) {
            folder = new CommunicationFolder(
                    name: "test folder"
            )
            folder = communicationFolderService.create( folder ) as CommunicationFolder
        }
        assertNotNull folder.getId()

        emailTemplate = new CommunicationEmailTemplate(
                description: "test email template description",
                personal: true,
                name: "test",
                oneOff: false,
                published: true,
                folder: folder,
                content: "test content",
                fromList: "testfrom",
                subject: "test subject",
                toList: "testto",
                validFrom: new Date() - 200,
                validTo: new Date() + 200
        )
        communicationEmailTemplateService.create( emailTemplate ) as CommunicationEmailTemplate
        assertNotNull emailTemplate.getId()

        def populationId = communicationPopulationExecutionService.execute( populationQuery.id )
        population = CommunicationPopulationSelectionList.get( populationId )
        assertNotNull population.getId()
    }


    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }


    @Test
    void testFetchRunningGroupSendItemCount() {
        CommunicationGroupSend groupSend = createGroupSend()
        CommunicationGroupSendItem groupSendItem1 = createGroupSendItem( groupSend, 101 )
        CommunicationGroupSendItem groupSendItem2 = createGroupSendItem( groupSend, 102 )

        int runningCount = communicationGroupSendItemService.fetchRunningGroupSendItemCount( 200L ) // bogus
        assertEquals( 0, runningCount )

        runningCount = communicationGroupSendItemService.fetchRunningGroupSendItemCount( groupSend.id )
        assertEquals( 2, runningCount )

        groupSendItem1.currentExecutionState = CommunicationGroupSendItemExecutionState.Stopped
        groupSendItem1.stopDate = new Date()
        groupSendItem1 = communicationGroupSendItemService.update( groupSendItem1 )

        runningCount = communicationGroupSendItemService.fetchRunningGroupSendItemCount( groupSend.id )
        assertEquals( 1, runningCount )

        groupSendItem2.currentExecutionState = CommunicationGroupSendItemExecutionState.Complete
        groupSendItem2.stopDate = new Date()
        groupSendItem2 = communicationGroupSendItemService.update( groupSendItem2 )

        runningCount = communicationGroupSendItemService.fetchRunningGroupSendItemCount( groupSend.id )
        assertEquals( 0, runningCount )
    }


    private CommunicationGroupSend createGroupSend() {
        return communicationGroupSendService.create(
                new CommunicationGroupSend(
                        organizationId: organization.id,
                        populationId: population.id,
                        templateId: emailTemplate.id,
                        ownerPidm: bannerAuthenticationToken.getPidm(),
                        recalculateOnSend: new Boolean(false)
                )
        ) as CommunicationGroupSend
    }


    private CommunicationGroupSendItem createGroupSendItem( CommunicationGroupSend groupSend, Long recipientPidm ) {
        if (!groupSend.id) throw new IllegalArgumentException( "groupSend must be persisted first" )

        Date now = new Date()
        return communicationGroupSendItemService.create(
                new CommunicationGroupSendItem(
                        communicationGroupSend: groupSend,
                        recipientPidm: recipientPidm,
                        currentExecutionState: CommunicationGroupSendItemExecutionState.Ready,
                        startedDate: now,
                        referenceId: UUID.randomUUID().toString()
                )
        ) as CommunicationGroupSendItem
    }


}
