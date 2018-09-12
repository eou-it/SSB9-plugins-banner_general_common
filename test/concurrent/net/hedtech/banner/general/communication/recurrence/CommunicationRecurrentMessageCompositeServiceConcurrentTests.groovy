/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.recurrence

import grails.util.Holders
import net.hedtech.banner.general.asynchronous.AsynchronousBannerAuthenticationSpoofer
import net.hedtech.banner.general.communication.CommunicationBaseConcurrentTestCase
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSend
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSendDetailView
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSendItem
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSendItemView
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSendListView
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSendRequest
import net.hedtech.banner.general.communication.job.CommunicationJob
import net.hedtech.banner.general.communication.merge.CommunicationRecipientData
import net.hedtech.banner.general.communication.population.CommunicationPopulation
import net.hedtech.banner.general.communication.population.CommunicationPopulationCalculation
import net.hedtech.banner.general.communication.population.CommunicationPopulationCalculationStatus
import net.hedtech.banner.general.communication.population.CommunicationPopulationVersionQueryAssociation
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQuery
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQueryVersion
import net.hedtech.banner.general.communication.population.selectionlist.CommunicationPopulationSelectionListEntry
import net.hedtech.banner.general.communication.recurrence.CommunicationRecurrentMessage
import org.apache.commons.logging.LogFactory
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

import java.util.concurrent.TimeUnit

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

class CommunicationRecurrentMessageCompositeServiceConcurrentTests extends CommunicationBaseConcurrentTestCase {
    def log = LogFactory.getLog(this.class)
    def selfServiceBannerAuthenticationProvider
    def AsynchronousBannerAuthenticationSpoofer asynchronousBannerAuthenticationSpoofer = Holders.grailsApplication.mainContext.getBean('asynchronousBannerAuthenticationSpoofer')

    @Before
    public void setUp() {
        formContext = ['GUAGMNU', 'SELFSERVICE']
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
//         sessionFactory.currentSession?.close()
         logout()
    }

    @Test
    public void testRecurrentMessageRequestByTemplateByPopulation() {
        println "testRecurrentMessageRequestByTemplateByPopulation"
        CommunicationRecurrentMessage recurrentMessage
        CommunicationPopulationQuery populationQuery = communicationPopulationQueryCompositeService.createPopulationQuery(newPopulationQuery("testPop"))
        CommunicationPopulationQueryVersion queryVersion = communicationPopulationQueryCompositeService.publishPopulationQuery(populationQuery)
        populationQuery = queryVersion.query

        CommunicationPopulation population = communicationPopulationCompositeService.createPopulationFromQuery(populationQuery, "testPopulation")
        CommunicationPopulationCalculation populationCalculation = CommunicationPopulationCalculation.findLatestByPopulationIdAndCalculatedBy(population.id, 'BCMADMIN')
        assertEquals(populationCalculation.status, CommunicationPopulationCalculationStatus.PENDING_EXECUTION)
        def isAvailable = {
            def theCalculation = CommunicationPopulationCalculation.get(it)
            theCalculation.refresh()
            return theCalculation.status == CommunicationPopulationCalculationStatus.AVAILABLE
        }
        assertTrueWithRetry(isAvailable, populationCalculation.id, 15, 5)

        List queryAssociations = CommunicationPopulationVersionQueryAssociation.findByPopulationVersion(populationCalculation.populationVersion)
        assertEquals(1, queryAssociations.size())

        def selectionListEntryList = CommunicationPopulationSelectionListEntry.fetchBySelectionListId(populationCalculation.selectionList.id)
        assertNotNull(selectionListEntryList)
        assertEquals(5, selectionListEntryList.size())


        Calendar calendar = Calendar.getInstance(); // gets a calendar using the default time zone and locale.
        calendar.add(Calendar.SECOND, 5);
        Date date = calendar.getTime()
        String cronExpression = generateCronExpressionNow(Integer.toString(date.getSeconds()),Integer.toString(date.getMinutes()),
                Integer.toString(date.getHours()),Integer.toString(date.getDate()), Integer.toString(date.getMonth()+1), "?", Integer.toString(date.getYear()+1900));

        CommunicationGroupSendRequest request = new CommunicationGroupSendRequest(
                name: "testRecurrentMessageRequestByTemplateByPopulation",
                populationId: population.id,
                templateId: defaultEmailTemplate.id,
                organizationId: defaultOrganization.id,
                referenceId: UUID.randomUUID().toString(),
                cronExpression: cronExpression,
                recalculateOnSend: false
        )

        recurrentMessage = communicationRecurrentMessageCompositeService.sendRecurrentMessageCommunication(request)
        assertNotNull(recurrentMessage)

        def checkExpectedGroupSendCreated = {
            return CommunicationGroupSend.findByRecurrentMessageId( it ).size() == 1
        }
        assertTrueWithRetry(checkExpectedGroupSendCreated, recurrentMessage.id, 20, 5)

        CommunicationGroupSend groupSend = CommunicationGroupSend.findByRecurrentMessageId( recurrentMessage.id ).get(0)

        def checkExpectedGroupSendItemsCreated = {
            CommunicationGroupSend each = CommunicationGroupSend.get( it )
            return CommunicationGroupSendItem.fetchByGroupSend( each ).size() == 5
        }
        assertTrueWithRetry(checkExpectedGroupSendItemsCreated, groupSend.id, 15, 5)

        // Confirm group send view returns the correct results
        def sendViewDetails = CommunicationGroupSendDetailView.findAll()
        assertEquals(1, sendViewDetails.size())

        def sendListView = CommunicationGroupSendListView.findAll()
        assertEquals(1, sendListView.size())

        // Confirm group send item view returns the correct results
        def sendItemViewDetails = CommunicationGroupSendItemView.findAll()
        assertEquals(5, sendItemViewDetails.size())

        sleepUntilGroupSendItemsComplete(groupSend, 60)

        int countCompleted = CommunicationGroupSendItem.fetchByCompleteExecutionStateAndGroupSend(groupSend).size()
        assertEquals(5, countCompleted)

        sleepUntilCommunicationJobsComplete(10 * 60)
        countCompleted = CommunicationJob.fetchCompleted().size()
        assertEquals(5, countCompleted)

        sleepUntilGroupSendComplete(groupSend, 3 * 60)

        // test delete group send
        assertEquals(1, fetchGroupSendCount(groupSend.id))
        assertEquals(5, fetchGroupSendItemCount(groupSend.id))
        assertEquals(5, CommunicationJob.findAll().size())
        assertEquals(5, CommunicationRecipientData.findAll().size())
        communicationGroupSendCompositeService.deleteGroupSend(groupSend.id)
        assertEquals(0, fetchGroupSendCount(groupSend.id))
        assertEquals(0, fetchGroupSendItemCount(groupSend.id))
        assertEquals(0, CommunicationJob.findAll().size())
        assertEquals(0, CommunicationRecipientData.findAll().size())
    }

    private String generateCronExpressionNow(final  String seconds,final String minutes, final String hours, final String dayOfMonth, final String month, final String dayOfWeek, final String year) {
        StringBuilder sb = new StringBuilder()
        sb.append(seconds).append(" ").append(minutes).append(" ").append(hours).append(" ").append(dayOfMonth).append(" ").append(month).append(" ").append(dayOfWeek).append(" ").append(year);
        return sb.toString();
    }
}