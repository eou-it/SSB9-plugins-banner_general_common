/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.groupsend

import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.CommunicationManagementTestingSupport
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQuery
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQueryExecutionResult
import net.hedtech.banner.general.communication.population.selectionlist.CommunicationPopulationSelectionList
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
 * Tests the group send dao service.
 */
class CommunicationGroupSendServiceIntegrationTests extends BaseIntegrationTestCase {

    def communicationOrganizationService
    def communicationGroupSendService
    def selfServiceBannerAuthenticationProvider
    def communicationPopulationQueryCompositeService
    def communicationPopulationQueryExecutionService
    def communicationFolderService
    def communicationGroupSendCompositeService
    def communicationTemplateService
    def communicationEmailTemplateService

    CommunicationOrganization organization
    BannerAuthenticationToken bannerAuthenticationToken
    CommunicationPopulationQuery populationQuery
    CommunicationEmailTemplate emailTemplate
    CommunicationPopulationSelectionList selectionList

    public void cleanUp() {
        def sql = null
        try {
            sessionFactory.currentSession.with { session ->
                sql = new Sql(session.connection())
                sql.executeUpdate("Delete from GCRORAN")
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
        organization = new CommunicationOrganization(name: "Test Org")
        organization = communicationOrganizationService.create(organization) as CommunicationOrganization

        Authentication authentication = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('BCMADMIN', '111111'))
        assertNotNull authentication
        SecurityContextHolder.getContext().setAuthentication( authentication )
        assertTrue( authentication instanceof BannerAuthenticationToken )
        bannerAuthenticationToken = authentication as BannerAuthenticationToken
        assertNotNull bannerAuthenticationToken.getPidm()

        populationQuery = new CommunicationPopulationQuery(
                folder: CommunicationManagementTestingSupport.newValidForCreateFolderWithSave(),
                createDate: new Date(),
                name: "Test Query",
                sqlString: "select 2086 spriden_pidm from dual"
        )
        populationQuery = communicationPopulationQueryCompositeService.createPopulationQuery( populationQuery )
        populationQuery = communicationPopulationQueryCompositeService.publishPopulationQuery( populationQuery )

        CommunicationFolder folder = CommunicationFolder.fetchByName("test folder")
        if (!folder) {
            folder = new CommunicationFolder(
                    name: "test folder"
            )
            folder = communicationFolderService.create(folder) as CommunicationFolder
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
                validFrom: new Date()-200,
                validTo: new Date()+200
        )
        communicationEmailTemplateService.create(emailTemplate) as CommunicationEmailTemplate
        assertNotNull emailTemplate.getId()

        CommunicationPopulationQueryExecutionResult queryExecutionResult = communicationPopulationQueryExecutionService.execute(populationQuery.id)
        selectionList = CommunicationPopulationSelectionList.get( queryExecutionResult.selectionListId )
        assertNotNull selectionList.getId()
    }


    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }


    @Test
    void testCreateGroupSend() {
        CommunicationGroupSend communicationGroupSend = createGroupSend()
        assertNotNull communicationGroupSend.getId()
        assertEquals( bannerAuthenticationToken.getOracleUserName(), communicationGroupSend.getCreatedBy() )
        assertEquals("test", communicationGroupSend.getName())
    }

    @Test
    void testUpdateGroupSend() {
        CommunicationGroupSend groupSend = createGroupSend()
        Long groupSendId = groupSend.id
        assertNull( groupSend.startedDate )

        // update the started date
        Date startedDate = new Date()
        groupSend.setStartedDate( startedDate )
        communicationGroupSendService.update( groupSend )

        CommunicationGroupSend found = (CommunicationGroupSend) communicationGroupSendService.get( groupSendId )
        assertNotNull found
        assertEquals( startedDate, found.startedDate )
    }

    @Test
    void testHardDelete() {
        CommunicationGroupSend communicationGroupSend = createGroupSend()
        Long groupSendId = communicationGroupSend.id

        assertNotNull( communicationGroupSendService.get( groupSendId ) )

        communicationGroupSendService.delete( communicationGroupSend )

        try {
            communicationGroupSendService.get( groupSendId )
        } catch (ApplicationException ignore) {
            // expected.
        }

    }

    @Test
    void testFindRunning() {
        createGroupSend()
        CommunicationGroupSend groupSendB = createGroupSend()
        CommunicationGroupSend groupSendC = createGroupSend()

        List runningList = communicationGroupSendService.findRunning()
        assertEquals( 3, runningList.size() )

        assertTrue( groupSendB.currentExecutionState.isRunning() )
        groupSendB = communicationGroupSendCompositeService.stopGroupSend( groupSendB.id )
        assertTrue( groupSendB.currentExecutionState.isTerminal() )

        runningList = communicationGroupSendService.findRunning()
        assertEquals( 2, runningList.size() )

        groupSendC = communicationGroupSendCompositeService.completeGroupSend( groupSendC.id )
        assertTrue( groupSendC.currentExecutionState.isTerminal() )

        runningList = communicationGroupSendService.findRunning()
        assertEquals( 1, runningList.size() )
    }


    @Test
    void testStopStoppedGroupSend() {
        CommunicationGroupSend groupSend = createGroupSend()

        List runningList = communicationGroupSendService.findRunning()
        assertEquals( 1, runningList.size() )

        assertTrue( groupSend.currentExecutionState.isRunning() )
        groupSend = communicationGroupSendCompositeService.stopGroupSend( groupSend.id )
        assertTrue( groupSend.currentExecutionState.equals( CommunicationGroupSendExecutionState.Stopped ) )
        assertTrue( groupSend.currentExecutionState.isTerminal() )

        runningList = communicationGroupSendService.findRunning()
        assertEquals( 0, runningList.size() )

        try{
            communicationGroupSendCompositeService.stopGroupSend( groupSend.id )
            fail( "Shouldn't be able to stop a group send that has already concluded." )
        } catch( ApplicationException e ) {
            assertEquals( "@@r1:cannotStopConcludedGroupSend@@", e.getWrappedException().getMessage() )
        }
    }

    @Test
    void testStopCompletedGroupSend() {
        def groupSend = createGroupSend()

        assertTrue(groupSend.currentExecutionState.isRunning())
        groupSend = communicationGroupSendCompositeService.completeGroupSend(groupSend.id)
        assertTrue( groupSend.currentExecutionState.equals( CommunicationGroupSendExecutionState.Complete ) )

        List runningList = communicationGroupSendService.findRunning()
        assertEquals(0, runningList.size())

        try {
            communicationGroupSendCompositeService.stopGroupSend(groupSend.id)
            fail("Shouldn't be able to stop a group send that has already concluded.")
        } catch (ApplicationException e) {
            assertEquals( "@@r1:cannotStopConcludedGroupSend@@", e.getWrappedException().getMessage() )
        }
    }

    @Test
    void testCreateGroupSendForScheduling() {
        CommunicationGroupSend communicationGroupSend = createGroupSendForScheduling()
        assertNotNull communicationGroupSend.getId()
        assertEquals( bannerAuthenticationToken.getOracleUserName(), communicationGroupSend.getCreatedBy() )
        assertEquals("test", communicationGroupSend.getName())
    }


    private CommunicationGroupSend createGroupSend() {
        return communicationGroupSendService.create(
            new CommunicationGroupSend(
                organizationId: organization.id,
                populationId: selectionList.id,
                templateId: emailTemplate.id,
                createdBy: bannerAuthenticationToken.getOracleUserName(),
                recalculateOnSend : new Boolean(false)
            )
        ) as CommunicationGroupSend
    }

    private CommunicationGroupSend createGroupSendForScheduling() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, 1);

        return communicationGroupSendService.create(
                new CommunicationGroupSend(
                        organizationId: organization.id,
                        populationId: selectionList.id,
                        templateId: emailTemplate.id,
                        createdBy: bannerAuthenticationToken.getOracleUserName(),
                        scheduledStartDate : c.getTime(),
                        recalculateOnSend : new Boolean(true)

                )
        ) as CommunicationGroupSend
    }
}
