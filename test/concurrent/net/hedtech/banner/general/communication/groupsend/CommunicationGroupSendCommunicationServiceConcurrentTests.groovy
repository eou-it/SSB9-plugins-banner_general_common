package net.hedtech.banner.general.communication.groupsend

import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.job.CommunicationJob
import net.hedtech.banner.general.communication.merge.CommunicationRecipientData
import net.hedtech.banner.general.communication.population.CommunicationPopulationQuery
import net.hedtech.banner.general.communication.population.CommunicationPopulationSelectionList
import net.hedtech.banner.general.communication.template.CommunicationEmailTemplate
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

class CommunicationGroupSendCommunicationServiceConcurrentTests extends CommunicationBaseConcurrentTestCase {
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
        mailServer.start()
        CommunicationGroupSend groupSend
        CommunicationPopulationQuery populationQuery = communicationPopulationQueryService.create(newPopulationQuery("testPop"))
        assertTrue(populationQuery.valid)

        Long populationSelectionListId = communicationPopulationExecutionService.execute(populationQuery.id)
        CommunicationPopulationSelectionList selectionList = communicationPopulationSelectionListService.get(populationSelectionListId)
        assertEquals(5, selectionList.getLastCalculatedCount())

        CommunicationGroupSendRequest request = new CommunicationGroupSendRequest(
                name: "testGroupSendRequestByTemplateByPopulationSendImmediately",
                populationId: populationSelectionListId,
                templateId: defaultEmailTemplate.id,
                organizationId: defaultOrganization.id,
                referenceId: UUID.randomUUID().toString(),
                recalculateOnSend: false
        )

        groupSend = communicationGroupSendCommunicationService.sendAsynchronousGroupCommunication(request)
        assertNotNull(groupSend)

        assertEquals( 5, communicationGroupSendItemService.fetchByGroupSend( groupSend ).size() )

        def sendviewdetails = CommunicationGroupSendView.findAll()
        assertEquals(1, sendviewdetails.size())

        List groupSendItemList = communicationGroupSendItemService.list()
        assertEquals( 5, groupSendItemList.size() )
        CommunicationGroupSendItem found = groupSendItemList.get( 0 ) as CommunicationGroupSendItem
        assertEquals( CommunicationGroupSendItemExecutionState.Ready, found.currentExecutionState)

        def sendItemViewDetails = CommunicationGroupSendItemView.findAll()
        assertEquals(5, sendItemViewDetails.size())

        assertEquals( 5, CommunicationGroupSendItem.fetchByReadyExecutionState().size() )

        sleepUntilGroupSendItemsComplete( groupSend, 5, 30 )

        int countCompleted = CommunicationGroupSendItem.fetchByCompleteExecutionStateAndGroupSend( groupSend ).size()
        assertEquals( 5, countCompleted )

        sleepUntilCommunicationJobsComplete( 5, 60 )
        countCompleted = CommunicationJob.fetchCompleted().size()
        assertEquals( 5, countCompleted )

        MimeMessage[] messages = mailServer.getReceivedMessages();
        assertNotNull(messages);
        assertEquals(5, messages.length);

        sleepUntilGroupSendComplete( groupSend, 120 )

        // test delete group send
        assertEquals( 1, fetchGroupSendCount( groupSend.id ) )
        assertEquals( 5, fetchGroupSendItemCount( groupSend.id ) )
        assertEquals( 5, CommunicationJob.findAll().size() )
        assertEquals( 5, CommunicationRecipientData.findAll().size() )
        communicationGroupSendCommunicationService.deleteGroupSend( groupSend.id )
        assertEquals( 0, fetchGroupSendCount( groupSend.id ) )
        assertEquals( 0, fetchGroupSendItemCount( groupSend.id ) )
        assertEquals( 0, CommunicationJob.findAll().size() )
        assertEquals( 0, CommunicationRecipientData.findAll().size() )
    }

    @Test
    public void testDeleteGroupSend() {
        CommunicationGroupSend groupSend
        CommunicationPopulationQuery populationQuery = communicationPopulationQueryService.create(newPopulationQuery("testDeleteGroupSend"))
        assertTrue(populationQuery.valid)

        Long populationSelectionListId = communicationPopulationExecutionService.execute(populationQuery.id)
        CommunicationPopulationSelectionList selectionList = communicationPopulationSelectionListService.get(populationSelectionListId)
        assertEquals(5, selectionList.getLastCalculatedCount())

        CommunicationGroupSendRequest request = new CommunicationGroupSendRequest(
                name: "testDeleteGroupSend",
                populationId: populationSelectionListId,
                templateId: defaultEmailTemplate.id,
                organizationId: defaultOrganization.id,
                referenceId: UUID.randomUUID().toString(),
                recalculateOnSend: false
        )

        groupSend = communicationGroupSendCommunicationService.sendAsynchronousGroupCommunication(request)
        assertNotNull(groupSend)

        assertEquals( 1, fetchGroupSendCount( groupSend.id ) )
        assertEquals( 5, fetchGroupSendItemCount( groupSend.id ) )

        try {
            communicationGroupSendCommunicationService.deleteGroupSend( groupSend.id )
        } catch (ApplicationException e) {
            assertEquals( "@@r1:cannotDeleteRunningGroupSend@@", e.getWrappedException().getMessage() )
        }

        groupSend = communicationGroupSendCommunicationService.completeGroupSend( groupSend.id )

        communicationGroupSendCommunicationService.deleteGroupSend( groupSend.id )

        assertEquals( 0, fetchGroupSendCount( groupSend.id ) )
        assertEquals( 0, fetchGroupSendItemCount( groupSend.id ) )
    }

    private int fetchGroupSendCount( Long groupSendId ) {
        def sql
        def result
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            result = sql.firstRow( "select count(*) as rowcount from GCBGSND where GCBGSND_SURROGATE_ID = ${groupSendId}" )
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
        return result.rowcount
    }

    private int fetchGroupSendItemCount( Long groupSendId ) {
        def sql
        def result
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            result = sql.firstRow( "select count(*) as rowcount from GCRGSIM where GCRGSIM_GROUP_SEND_ID = ${groupSendId}" )
            println( result.rowcount )
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
        return result.rowcount
    }

    private void sleepUntilGroupSendItemsComplete( CommunicationGroupSend groupSend, long totalNumJobs, int maxSleepTime ) {
        final int interval = 2;                 // test every second
        int count = maxSleepTime / interval;    // calculate max loop count
        while (count > 0) {
            count--;
            TimeUnit.SECONDS.sleep( interval );

            int countCompleted = CommunicationGroupSendItem.fetchByCompleteExecutionStateAndGroupSend( groupSend ).size()

            if ( countCompleted >= totalNumJobs) {
                break;
            }
        }
    }

    private void sleepUntilCommunicationJobsComplete( long totalNumJobs, int maxSleepTime ) {
        final int interval = 2;                 // test every second
        int count = maxSleepTime / interval;    // calculate max loop count
        while (count > 0) {
            count--;
            TimeUnit.SECONDS.sleep( interval );

            int countCompleted = CommunicationJob.fetchCompleted().size()

            if ( countCompleted >= totalNumJobs) {
                break;
            }
        }
    }

    private void sleepUntilGroupSendComplete( CommunicationGroupSend groupSend, int maxSleepTime ) {
        final int interval = 2;                 // test every second
        int count = maxSleepTime / interval;    // calculate max loop count
        while (count > 0) {
            count--;
            TimeUnit.SECONDS.sleep( interval );

            sessionFactory.currentSession.flush()
            sessionFactory.currentSession.clear()

            groupSend = CommunicationGroupSend.get( groupSend.id )

            if ( groupSend.currentExecutionState.equals( CommunicationGroupSendExecutionState.Complete ) ) {
                break;
            }
        }

        assertEquals( CommunicationGroupSendExecutionState.Complete, groupSend.getCurrentExecutionState() )
    }

    private def newPopulationQuery( String queryName ) {
        def populationQuery = new CommunicationPopulationQuery(
                // Required fields
                folder: defaultFolder,
                name: queryName,
                description: "test description",
                sqlString: "select spriden_pidm from spriden where rownum < 6 and spriden_change_ind is null"
        )

        return populationQuery
    }


}
