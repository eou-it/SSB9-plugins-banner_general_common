/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.interaction

import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSend
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSendItem
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSendItemView
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSendListView
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSendRequest
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSendDetailView
import net.hedtech.banner.general.communication.job.CommunicationJob
import net.hedtech.banner.general.communication.merge.CommunicationRecipientData
import net.hedtech.banner.general.communication.population.CommunicationPopulation
import net.hedtech.banner.general.communication.population.CommunicationPopulationCalculation
import net.hedtech.banner.general.communication.population.CommunicationPopulationCalculationStatus
import net.hedtech.banner.general.communication.population.CommunicationPopulationVersionQueryAssociation
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQuery
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQueryVersion
import net.hedtech.banner.general.communication.population.selectionlist.CommunicationPopulationSelectionListEntry
import org.apache.commons.logging.LogFactory
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

import javax.mail.internet.MimeMessage

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import net.hedtech.banner.general.communication.CommunicationBaseConcurrentTestCase

class CommunicationInteractionViewConcurrentTests extends CommunicationBaseConcurrentTestCase {
    def log = LogFactory.getLog(this.class)
    def selfServiceBannerAuthenticationProvider

    @Before
    public void setUp() {
        formContext = ['GUAGMNU','SELFSERVICE']
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
    public void testCommunicationInteractionViewForGroupSendItems() {
        CommunicationGroupSend groupSend
        CommunicationPopulationQuery populationQuery = communicationPopulationQueryCompositeService.createPopulationQuery(newPopulationQuery("testPop"))
        CommunicationPopulationQueryVersion queryVersion = communicationPopulationQueryCompositeService.publishPopulationQuery(populationQuery)
        populationQuery = queryVersion.query

        CommunicationPopulation population = communicationPopulationCompositeService.createPopulationFromQuery(populationQuery, "testPopulation")
        CommunicationPopulationCalculation populationCalculation = CommunicationPopulationCalculation.findLatestByPopulationIdAndCalculatedBy( population.id, 'BCMADMIN' )
        assertEquals( populationCalculation.status, CommunicationPopulationCalculationStatus.PENDING_EXECUTION )
        def isAvailable = {
            def theCalculation = CommunicationPopulationCalculation.get( it )
            theCalculation.refresh()
            return theCalculation.status == CommunicationPopulationCalculationStatus.AVAILABLE
        }
        assertTrueWithRetry( isAvailable, populationCalculation.id, 15, 5 )

        List queryAssociations = CommunicationPopulationVersionQueryAssociation.findByPopulationVersion( populationCalculation.populationVersion )
        assertEquals(1, queryAssociations.size())

        def selectionListEntryList = CommunicationPopulationSelectionListEntry.fetchBySelectionListId( populationCalculation.selectionList.id )
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
            CommunicationGroupSend each = CommunicationGroupSend.get(it)
            return CommunicationGroupSendItem.fetchByGroupSend( each ).size() == 5
        }
        assertTrueWithRetry(checkExpectedGroupSendItemsCreated, groupSend.id, 30, 10)

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

        MimeMessage[] messages = mailServer.getReceivedMessages();
        assertNotNull(messages);
        assertEquals(5, messages.length);

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

        List interactions = CommunicationInteractionView.findByConstituentNameOrBannerId('%');
        // Assert domain values
        assertNotNull interactions
        assertEquals(5, interactions.size())

        CommunicationInteractionView interaction = interactions.get(0)
        assertNotNull interaction?.surrogateId
        assertEquals defaultEmailTemplate.description, interaction.subject
        assertEquals defaultOrganization.name, interaction.organizationName
        assertEquals defaultEmailTemplate.folder.name, interaction.folderName
        assertEquals defaultEmailTemplate.name, interaction.templateName
        assertNotNull(interaction.interactionDate)
    }
}