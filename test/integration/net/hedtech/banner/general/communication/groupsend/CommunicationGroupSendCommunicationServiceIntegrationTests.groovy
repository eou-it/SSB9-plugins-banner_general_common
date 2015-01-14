package net.hedtech.banner.general.communication.groupsend

import grails.gorm.DetachedCriteria
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

import java.util.concurrent.TimeUnit

/**
 * Created by mbrzycki on 12/3/14.
 */
class CommunicationGroupSendCommunicationServiceIntegrationTests extends CommunicationBaseIntegrationTestCase {
    def log = LogFactory.getLog( this.class )
    def selfServiceBannerAuthenticationProvider

    @Before
    public void setUp() {
        super.useTransactions = false
        formContext = ['SELFSERVICE']
        super.setUp()
        def auth = selfServiceBannerAuthenticationProvider.authenticate( new UsernamePasswordAuthenticationToken( 'BCMADMIN', '111111' ) )
        SecurityContextHolder.getContext().setAuthentication( auth )

        if (!communicationGroupSendItemProcessingEngine.threadsRunning) communicationGroupSendItemProcessingEngine.startRunning()
        if (!communicationJobProcessingEngine.threadsRunning) communicationJobProcessingEngine.startRunning()
    }


    @After
    public void tearDown() {
        if (communicationGroupSendItemProcessingEngine.threadsRunning) communicationGroupSendItemProcessingEngine.stopRunning()
        if (communicationJobProcessingEngine.threadsRunning) communicationJobProcessingEngine.stopRunning()

//        super.tearDown()
        logout()
    }


    @Test
    public void testGroupSendRequestByTemplateByPopulationSendImmediately() {
        CommunicationPopulationQuery populationQuery = communicationPopulationQueryService.create( newPopulationQuery( "testPop" ) )
        assertTrue( populationQuery.valid )

        Long populationSelectionListId = communicationPopulationExecutionService.execute( populationQuery.id )
        CommunicationPopulationSelectionList selectionList = communicationPopulationSelectionListService.get( populationSelectionListId )
        assertEquals( 5, selectionList.getLastCalculatedCount() )

        CommunicationGroupSendRequest request = new CommunicationGroupSendRequest(
                populationId: populationSelectionListId,
                templateId: defaultEmailTemplate.id,
                organizationId: defaultOrganization.id,
                referenceId: UUID.randomUUID().toString()
        )

        CommunicationGroupSend groupSend = communicationGroupSendCommunicationService.sendAsynchronousGroupCommunication( request )
        assertNotNull( groupSend )

        assertEquals( 5, communicationGroupSendItemService.fetchByGroupSend( groupSend ).size() )

        def sendviewdetails = CommunicationGroupSendView.findAll()
        assertEquals(1, sendviewdetails.size())

        List groupSendItemList = communicationGroupSendItemService.list()
        assertEquals( 5, groupSendItemList.size() )
        CommunicationGroupSendItem found = groupSendItemList.get( 0 ) as CommunicationGroupSendItem
        assertEquals( CommunicationGroupSendItemExecutionState.Ready, found.currentExecutionState)

        assertEquals( 5, CommunicationGroupSendItem.fetchByReadyExecutionState().size() )

        sleepUntilGroupSendItemsComplete( groupSend, 5, 30 )

        int countCompleted = CommunicationGroupSendItem.fetchByCompleteExecutionStateAndGroupSend( groupSend ).size()
        assertEquals( 5, countCompleted )

        sleepUntilCommunicationJobsComplete( 5, 30 )
        countCompleted = CommunicationJob.fetchCompleted().size()
        assertEquals( 5, countCompleted )
    }

    private void sleepUntilGroupSendItemsComplete( CommunicationGroupSend groupSend, long totalNumJobs, int maxSleepTime ) {
        final int interval = 1;                 // test every second
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
        final int interval = 1;                 // test every second
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

//    private createEmailTemplate() {
//        def template = newValidForCreateEmailTemplate(folder1)
//        def newTemplate = communicationEmailTemplateService.create([domainModel: template])
//        //Test if the service set the created date, and the infrastructure set the modifiedby and date
//        assertNotNull newTemplate.createDate
//        assertNotNull(newTemplate.lastModified)
//        assertNotNull(newTemplate.lastModifiedBy)
//
//        // Now test findall
//        def foundEmailTemplates = communicationEmailTemplateService.findAll()
//        assertEquals(1, foundEmailTemplates.size())
//    }

//    private void waitForGroupSendToGetPickedUp( long groupSendId ) {
//        CommunicationGroupSend groupSend = communicationGroupSendService.fetch( groupSendId );
//        long start = System.currentTimeMillis();
//        while (!groupSend.isStarted() && (System.currentTimeMillis() - start < 300 * 1000)) {
//            try {
//                Thread.sleep( 100 );
//            } catch (InterruptedException e) {
//            }
//            groupSend = getResourceLocator().getGroupSendService().fetch( groupSendKey );
//        }
//        assertTrue( groupSend.isStarted() );
//    }
//
//    protected void waitForItemsToStop( final GroupSendKey groupSendKey ) throws Exception {
//        new PatientRequest( "getRunningGroupSendItems" ) {
//            @Override
//            public boolean runImpl() throws Exception {
//                try {
//                    List<GroupSendItem> items = getResourceLocator().getGroupSendItemService().findByGroupSend( groupSendKey );
//                    for(GroupSendItem item:items) {
//                        if (item.isRunning()) return false;
//                    }
//                    return true;
//                } catch (Exception e) {
//                    throw e;
//                }
//            }
//        }.run();
//    }


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
