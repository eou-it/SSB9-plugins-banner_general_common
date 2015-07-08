package net.hedtech.banner.general.communication.groupsend

import grails.gorm.DetachedCriteria
import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.CommunicationBaseIntegrationTestCase
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.item.CommunicationEmailItem
import net.hedtech.banner.general.communication.job.CommunicationJob
import net.hedtech.banner.general.communication.merge.CommunicationRecipientData
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import net.hedtech.banner.general.communication.population.CommunicationPopulationQuery
import net.hedtech.banner.general.communication.population.CommunicationPopulationSelectionList
import net.hedtech.banner.general.communication.template.CommunicationEmailTemplate
import net.hedtech.banner.security.FormContext
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.apache.commons.logging.LogFactory
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

import javax.mail.internet.MimeMessage
import java.util.concurrent.TimeUnit

/**
 * Tests group send communication with all the services running.
 */
class CommunicationGroupSendCommunicationServiceIntegrationTests extends CommunicationBaseIntegrationTestCase {
    def log = LogFactory.getLog(this.class)
    def selfServiceBannerAuthenticationProvider

    @Before
    public void setUp() {
        //super.setUseTransactions( false )
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
        sessionFactory.currentSession?.close()
        logout()
    }

    @Test
    public void testGroupSendCommunication() {
        testGroupSendRequestByTemplateByPopulationSendImmediately()

        testDeleteGroupSend()
    }

    private void testGroupSendRequestByTemplateByPopulationSendImmediately() {
        mailServer.start()
        CommunicationGroupSend groupSend
        sessionFactory.currentSession.with { session ->  //Ensure a transaction is started and committed for async threads.
            def tx = session.beginTransaction()
            CommunicationPopulationQuery populationQuery = communicationPopulationQueryService.create(newPopulationQuery("testPop"))
            assertTrue(populationQuery.valid)

            Long populationSelectionListId = communicationPopulationExecutionService.execute(populationQuery.id)
            CommunicationPopulationSelectionList selectionList = communicationPopulationSelectionListService.get(populationSelectionListId)
            assertEquals(5, selectionList.getLastCalculatedCount())

            CommunicationGroupSendRequest request = new CommunicationGroupSendRequest(
                    populationId: populationSelectionListId,
                    templateId: defaultEmailTemplate.id,
                    organizationId: defaultOrganization.id,
                    referenceId: UUID.randomUUID().toString()
            )

            groupSend = communicationGroupSendCommunicationService.sendAsynchronousGroupCommunication(request)
            assertNotNull(groupSend)
            tx.commit()
        }

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
        for(MimeMessage each:messages) {
            System.out.println( "Display message content to out: ")
            System.out.println( messages )
        }
    }

    private void testDeleteGroupSend() {
        CommunicationGroupSend groupSend
        sessionFactory.currentSession.with { session ->  //Ensure a transaction is started and committed for async threads.
            def tx = session.beginTransaction()
            CommunicationPopulationQuery populationQuery = communicationPopulationQueryService.create(newPopulationQuery("testDeleteGroupSend"))
            assertTrue(populationQuery.valid)

            Long populationSelectionListId = communicationPopulationExecutionService.execute(populationQuery.id)
            CommunicationPopulationSelectionList selectionList = communicationPopulationSelectionListService.get(populationSelectionListId)
            assertEquals(5, selectionList.getLastCalculatedCount())

            CommunicationGroupSendRequest request = new CommunicationGroupSendRequest(
                    populationId: populationSelectionListId,
                    templateId: defaultEmailTemplate.id,
                    organizationId: defaultOrganization.id,
                    referenceId: UUID.randomUUID().toString()
            )

            groupSend = communicationGroupSendCommunicationService.sendAsynchronousGroupCommunication(request)
            assertNotNull(groupSend)
            tx.commit()
        }

        assertEquals( 1, fetchGroupSendCount( groupSend.id ) )
        assertEquals( 5, fetchGroupSendItemCount( groupSend.id ) )

        try {
            communicationGroupSendCommunicationService.deleteGroupSend( groupSend.id )
        } catch (ApplicationException e) {
            assertEquals( "@@r1:cannotStopRunningGroupSend@@", e.getWrappedException().getMessage() )
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
            println( result.rowcount )
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
