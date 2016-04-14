package net.hedtech.banner.general.communication.groupsend

import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.job.CommunicationJob
import net.hedtech.banner.general.communication.merge.CommunicationRecipientData
import net.hedtech.banner.general.communication.population.CommunicationPopulation
import net.hedtech.banner.general.communication.population.CommunicationPopulationCalculationStatus
import net.hedtech.banner.general.communication.population.CommunicationPopulationVersion
import net.hedtech.banner.general.communication.population.CommunicationPopulationVersionQueryAssociation
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQuery
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQueryExecutionResult
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQueryVersion
import net.hedtech.banner.general.communication.population.selectionlist.CommunicationPopulationSelectionList
import net.hedtech.banner.general.communication.population.selectionlist.CommunicationPopulationSelectionListEntry
import net.hedtech.banner.general.communication.template.CommunicationEmailTemplate
import net.hedtech.banner.general.communication.template.CommunicationTemplate
import org.apache.commons.logging.LogFactory
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

import javax.mail.internet.MimeMessage
import java.util.concurrent.TimeUnit

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

import net.hedtech.banner.general.communication.CommunicationBaseConcurrentTestCase

import static org.junit.Assert.assertNull

class CommunicationGroupSendCompositeServiceConcurrentTests extends CommunicationBaseConcurrentTestCase {
    def log = LogFactory.getLog(this.class)
    def selfServiceBannerAuthenticationProvider

    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('BCMADMIN', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)
        super.setUp()

        communicationGroupSendMonitor.startMonitoring()
        communicationGroupSendItemProcessingEngine.startRunning()
        communicationJobProcessingEngine.startRunning()
    }


    @After
    public void tearDown() {
        communicationGroupSendMonitor.shutdown()
        communicationGroupSendItemProcessingEngine.stopRunning()
        communicationJobProcessingEngine.stopRunning()

        super.tearDown()
//        sessionFactory.currentSession?.close()
        logout()
    }

    @Test
    public void testGroupSendRequestByTemplateByPopulationSendImmediately() {
        CommunicationGroupSend groupSend
        CommunicationPopulationQuery populationQuery = communicationPopulationQueryCompositeService.createPopulationQuery(newPopulationQuery("testPop"))
        CommunicationPopulationQueryVersion queryVersion = communicationPopulationQueryCompositeService.publishPopulationQuery( populationQuery )
        populationQuery = queryVersion.query

        CommunicationPopulation population = communicationPopulationCompositeService.createPopulationFromQuery( populationQuery, "testPopulation" )
        CommunicationPopulationVersion populationVersion = CommunicationPopulationVersion.findLatestByPopulationIdAndCreatedBy( population.id, 'BCMADMIN' )
        assertNotNull( populationVersion )
        assertEquals( CommunicationPopulationCalculationStatus.PENDING_EXECUTION, populationVersion.status )

        def isAvailable = {
            def aPopulationVersion = CommunicationPopulationVersion.get( it )
            aPopulationVersion.refresh()
            return aPopulationVersion.status == CommunicationPopulationCalculationStatus.AVAILABLE
        }
        assertTrueWithRetry( isAvailable, populationVersion.id, 30, 10 )

        List queryAssociations = CommunicationPopulationVersionQueryAssociation.findByPopulationVersion( populationVersion )
        assertEquals( 1, queryAssociations.size() )
        CommunicationPopulationVersionQueryAssociation queryAssociation = queryAssociations.get( 0 )
        queryAssociation.refresh()
        assertNotNull( queryAssociation.selectionList )

        def selectionListEntryList = CommunicationPopulationSelectionListEntry.fetchBySelectionListId( queryAssociation.selectionList.id )
        assertNotNull(selectionListEntryList)
        assertEquals(5, selectionListEntryList.size())

        CommunicationGroupSendRequest request = new CommunicationGroupSendRequest(
                name: "testGroupSendRequestByTemplateByPopulationSendImmediately",
                populationId: population.id,
                templateId: defaultEmailTemplate.id,
                organizationId: defaultOrganization.id,
                referenceId: UUID.randomUUID().toString(),
                recalculateOnSend: false
        )

        groupSend = communicationGroupSendCompositeService.sendAsynchronousGroupCommunication(request)
        assertNotNull(groupSend)

        def checkExpectedGroupSendItemsCreated = {
            CommunicationGroupSend each = CommunicationGroupSend.get( it )
            return communicationGroupSendItemService.fetchByGroupSend( each ).size() == 5
        }
        assertTrueWithRetry( checkExpectedGroupSendItemsCreated, groupSend.id, 30, 10 )

        // Confirm group send view returns the correct results
        def sendViewDetails = CommunicationGroupSendView.findAll()
        assertEquals(1, sendViewDetails.size())

        // Confirm group send item view returns the correct results
        def sendItemViewDetails = CommunicationGroupSendItemView.findAll()
        assertEquals(5, sendItemViewDetails.size())

        sleepUntilGroupSendItemsComplete( groupSend, 5, 60 )

        int countCompleted = CommunicationGroupSendItem.fetchByCompleteExecutionStateAndGroupSend( groupSend ).size()
        assertEquals( 5, countCompleted )

        sleepUntilCommunicationJobsComplete( 5, 10 * 60 )
        countCompleted = CommunicationJob.fetchCompleted().size()
        assertEquals( 5, countCompleted )

        MimeMessage[] messages = mailServer.getReceivedMessages();
        assertNotNull(messages);
        assertEquals(5, messages.length);

        sleepUntilGroupSendComplete( groupSend, 3 * 60 )

        // test delete group send
        assertEquals( 1, fetchGroupSendCount( groupSend.id ) )
        assertEquals( 5, fetchGroupSendItemCount( groupSend.id ) )
        assertEquals( 5, CommunicationJob.findAll().size() )
        assertEquals( 5, CommunicationRecipientData.findAll().size() )
        communicationGroupSendCompositeService.deleteGroupSend( groupSend.id )
        assertEquals( 0, fetchGroupSendCount( groupSend.id ) )
        assertEquals( 0, fetchGroupSendItemCount( groupSend.id ) )
        assertEquals( 0, CommunicationJob.findAll().size() )
        assertEquals( 0, CommunicationRecipientData.findAll().size() )
    }

    @Test
    void testFindRunning() {

        CommunicationGroupSendRequest request = createGroupSendRequest()
        CommunicationGroupSend groupSend = communicationGroupSendCompositeService.sendAsynchronousGroupCommunication(request)
        assertNotNull(groupSend)

        CommunicationGroupSend groupSendB = communicationGroupSendCompositeService.sendAsynchronousGroupCommunication(request)
        CommunicationGroupSend groupSendC = communicationGroupSendCompositeService.sendAsynchronousGroupCommunication(request)

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
        CommunicationGroupSendRequest request = createGroupSendRequest()
        CommunicationGroupSend groupSend = communicationGroupSendCompositeService.sendAsynchronousGroupCommunication(request)
        assertNotNull(groupSend)

        List runningList = communicationGroupSendService.findRunning()
        assertEquals( 1, runningList.size() )

        assertTrue( groupSend.currentExecutionState.isRunning() )
        sleepUntilGroupSendItemsComplete( groupSend, 5, 30 )
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
        CommunicationGroupSendRequest request = createGroupSendRequest()
        CommunicationGroupSend groupSend = communicationGroupSendCompositeService.sendAsynchronousGroupCommunication(request)
        assertNotNull(groupSend)

        assertTrue(groupSend.currentExecutionState.isRunning())
        sleepUntilGroupSendItemsComplete( groupSend, 5, 30 )
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
    public void testScheduledPopulationGroupSend() {
        CommunicationPopulationQuery populationQuery = communicationPopulationQueryCompositeService.createPopulationQuery(newPopulationQuery("testPop"))
        CommunicationPopulationQueryVersion queryVersion = communicationPopulationQueryCompositeService.publishPopulationQuery( populationQuery )
        populationQuery = queryVersion.query

        CommunicationPopulation population = communicationPopulationCompositeService.createPopulationFromQuery( populationQuery, "testPopulation" )
        CommunicationPopulationVersion populationVersion = CommunicationPopulationVersion.findLatestByPopulationIdAndCreatedBy( population.id, 'BCMADMIN' )
        assertNotNull( populationVersion )
        assertEquals( CommunicationPopulationCalculationStatus.PENDING_EXECUTION, populationVersion.status )

        def isAvailable = {
            def aPopulationVersion = CommunicationPopulationVersion.get( it )
            aPopulationVersion.refresh()
            return aPopulationVersion.status == CommunicationPopulationCalculationStatus.AVAILABLE
        }
        assertTrueWithRetry( isAvailable, populationVersion.id, 30, 10 )

        List queryAssociations = CommunicationPopulationVersionQueryAssociation.findByPopulationVersion( populationVersion )
        assertEquals( 1, queryAssociations.size() )
        CommunicationPopulationVersionQueryAssociation queryAssociation = queryAssociations.get( 0 )
        queryAssociation.refresh()
        assertNotNull( queryAssociation.selectionList )

        def selectionListEntryList = CommunicationPopulationSelectionListEntry.fetchBySelectionListId( queryAssociation.selectionList.id )
        assertNotNull(selectionListEntryList)
        assertEquals(5, selectionListEntryList.size())

        Calendar now = Calendar.getInstance()
        now.add(Calendar.SECOND, 10)

        CommunicationGroupSendRequest request = new CommunicationGroupSendRequest(
                name: "testScheduledRecalculatePopulationGroupSend",
                populationId: population.id,
                templateId: defaultEmailTemplate.id,
                organizationId: defaultOrganization.id,
                referenceId: UUID.randomUUID().toString(),
                scheduledStartDate: now.getTime(),
                recalculateOnSend: false
        )

        CommunicationGroupSend groupSend = communicationGroupSendCompositeService.sendAsynchronousGroupCommunication(request)
        assertNotNull(groupSend)

        def checkExpectedGroupSendItemsCreated = {
            CommunicationGroupSend each = CommunicationGroupSend.get( it )
            return communicationGroupSendItemService.fetchByGroupSend( each ).size() == 5
        }
        assertTrueWithRetry( checkExpectedGroupSendItemsCreated, groupSend.id, 30, 10 )

        // Confirm group send view returns the correct results
        def sendViewDetails = CommunicationGroupSendView.findAll()
        assertEquals(1, sendViewDetails.size())

        // Confirm group send item view returns the correct results
        def sendItemViewDetails = CommunicationGroupSendItemView.findAll()
        assertEquals(5, sendItemViewDetails.size())

        sleepUntilGroupSendItemsComplete( groupSend, 5, 60 )

        int countCompleted = CommunicationGroupSendItem.fetchByCompleteExecutionStateAndGroupSend( groupSend ).size()
        assertEquals( 5, countCompleted )

        sleepUntilCommunicationJobsComplete( 5, 10 * 60 )
        countCompleted = CommunicationJob.fetchCompleted().size()
        assertEquals( 5, countCompleted )

        MimeMessage[] messages = mailServer.getReceivedMessages();
        assertNotNull(messages);
        assertEquals(5, messages.length);

        sleepUntilGroupSendComplete( groupSend, 3 * 60 )

        // test delete group send
        assertEquals( 1, fetchGroupSendCount( groupSend.id ) )
        assertEquals( 5, fetchGroupSendItemCount( groupSend.id ) )
        assertEquals( 5, CommunicationJob.findAll().size() )
        assertEquals( 5, CommunicationRecipientData.findAll().size() )
        communicationGroupSendCompositeService.deleteGroupSend( groupSend.id )
        assertEquals( 0, fetchGroupSendCount( groupSend.id ) )
        assertEquals( 0, fetchGroupSendItemCount( groupSend.id ) )
        assertEquals( 0, CommunicationJob.findAll().size() )
        assertEquals( 0, CommunicationRecipientData.findAll().size() )
    }

    @Test
    public void testScheduledRecalculatePopulationGroupSend() {
        CommunicationPopulationQuery populationQuery = communicationPopulationQueryCompositeService.createPopulationQuery(newPopulationQuery("testPop"))
        CommunicationPopulationQueryVersion queryVersion = communicationPopulationQueryCompositeService.publishPopulationQuery( populationQuery )
        populationQuery = queryVersion.query

        CommunicationPopulation population = communicationPopulationCompositeService.createPopulationFromQuery( populationQuery, "testPopulation" )
        CommunicationPopulationVersion populationVersion = CommunicationPopulationVersion.findLatestByPopulationIdAndCreatedBy( population.id, 'BCMADMIN' )
        assertNotNull( populationVersion )
        assertEquals( CommunicationPopulationCalculationStatus.PENDING_EXECUTION, populationVersion.status )

        def isAvailable = {
            def aPopulationVersion = CommunicationPopulationVersion.get( it )
            aPopulationVersion.refresh()
            return aPopulationVersion.status == CommunicationPopulationCalculationStatus.AVAILABLE
        }
        assertTrueWithRetry( isAvailable, populationVersion.id, 30, 10 )

        List queryAssociations = CommunicationPopulationVersionQueryAssociation.findByPopulationVersion( populationVersion )
        assertEquals( 1, queryAssociations.size() )
        CommunicationPopulationVersionQueryAssociation queryAssociation = queryAssociations.get( 0 )
        queryAssociation.refresh()
        assertNotNull( queryAssociation.selectionList )

        def selectionListEntryList = CommunicationPopulationSelectionListEntry.fetchBySelectionListId( queryAssociation.selectionList.id )
        assertNotNull(selectionListEntryList)
        assertEquals(5, selectionListEntryList.size())

        Calendar now = Calendar.getInstance()
        now.add(Calendar.SECOND, 10)

        CommunicationGroupSendRequest request = new CommunicationGroupSendRequest(
                name: "testScheduledRecalculatePopulationGroupSend",
                populationId: population.id,
                templateId: defaultEmailTemplate.id,
                organizationId: defaultOrganization.id,
                referenceId: UUID.randomUUID().toString(),
                scheduledStartDate: now.getTime(),
                recalculateOnSend: true
        )

        CommunicationGroupSend groupSend = communicationGroupSendCompositeService.sendAsynchronousGroupCommunication(request)
        assertNotNull(groupSend)

        def checkExpectedGroupSendItemsCreated = {
            CommunicationGroupSend each = CommunicationGroupSend.get( it )
            return communicationGroupSendItemService.fetchByGroupSend( each ).size() == 5
        }
        assertTrueWithRetry( checkExpectedGroupSendItemsCreated, groupSend.id, 30, 10 )

        // Confirm group send view returns the correct results
        def sendViewDetails = CommunicationGroupSendView.findAll()
        assertEquals(1, sendViewDetails.size())

        // Confirm group send item view returns the correct results
        def sendItemViewDetails = CommunicationGroupSendItemView.findAll()
        assertEquals(5, sendItemViewDetails.size())

        sleepUntilGroupSendItemsComplete( groupSend, 5, 60 )

        int countCompleted = CommunicationGroupSendItem.fetchByCompleteExecutionStateAndGroupSend( groupSend ).size()
        assertEquals( 5, countCompleted )

        sleepUntilCommunicationJobsComplete( 5, 10 * 60 )
        countCompleted = CommunicationJob.fetchCompleted().size()
        assertEquals( 5, countCompleted )

        MimeMessage[] messages = mailServer.getReceivedMessages();
        assertNotNull(messages);
        assertEquals(5, messages.length);

        sleepUntilGroupSendComplete( groupSend, 3 * 60 )

        // test delete group send
        assertEquals( 1, fetchGroupSendCount( groupSend.id ) )
        assertEquals( 5, fetchGroupSendItemCount( groupSend.id ) )
        assertEquals( 5, CommunicationJob.findAll().size() )
        assertEquals( 5, CommunicationRecipientData.findAll().size() )
        communicationGroupSendCompositeService.deleteGroupSend( groupSend.id )
        assertEquals( 0, fetchGroupSendCount( groupSend.id ) )
        assertEquals( 0, fetchGroupSendItemCount( groupSend.id ) )
        assertEquals( 0, CommunicationJob.findAll().size() )
        assertEquals( 0, CommunicationRecipientData.findAll().size() )
    }

    private CommunicationGroupSendRequest createGroupSendRequest() {
        CommunicationPopulationQuery populationQuery = communicationPopulationQueryCompositeService.createPopulationQuery(newPopulationQuery("testPop"))
        CommunicationPopulationQueryVersion queryVersion = communicationPopulationQueryCompositeService.publishPopulationQuery( populationQuery )
        populationQuery = queryVersion.query

        CommunicationPopulation population = communicationPopulationCompositeService.createPopulationFromQuery( populationQuery, "testPopulation" )
        CommunicationPopulationVersion populationVersion = CommunicationPopulationVersion.findLatestByPopulationIdAndCreatedBy( population.id, 'BCMADMIN' )
        assertNotNull( populationVersion )
        assertEquals( CommunicationPopulationCalculationStatus.PENDING_EXECUTION, populationVersion.status )

        def isAvailable = {
            def aPopulationVersion = CommunicationPopulationVersion.get( it )
            aPopulationVersion.refresh()
            return aPopulationVersion.status == CommunicationPopulationCalculationStatus.AVAILABLE
        }
        assertTrueWithRetry( isAvailable, populationVersion.id, 30, 10 )

        List queryAssociations = CommunicationPopulationVersionQueryAssociation.findByPopulationVersion( populationVersion )
        assertEquals( 1, queryAssociations.size() )
        CommunicationPopulationVersionQueryAssociation queryAssociation = queryAssociations.get( 0 )
        queryAssociation.refresh()
        assertNotNull( queryAssociation.selectionList )

        def selectionListEntryList = CommunicationPopulationSelectionListEntry.fetchBySelectionListId( queryAssociation.selectionList.id )
        assertNotNull(selectionListEntryList)
        assertEquals(5, selectionListEntryList.size())

        CommunicationGroupSendRequest request = new CommunicationGroupSendRequest(
                name: "testGroupSendRequestByTemplateByPopulationSendImmediately",
                populationId: population.id,
                templateId: defaultEmailTemplate.id,
                organizationId: defaultOrganization.id,
                referenceId: UUID.randomUUID().toString(),
                recalculateOnSend: false
        )

        return request
    }

    @Test
    public void testDeleteGroupSend() {
        testDeleteGroupSend( defaultEmailTemplate )
    }


}
