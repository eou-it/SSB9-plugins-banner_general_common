package net.hedtech.banner.general.communication.groupsend

import net.hedtech.banner.general.communication.CommunicationBaseIntegrationTestCase
import net.hedtech.banner.general.communication.field.CommunicationField
import net.hedtech.banner.general.communication.job.CommunicationJob
import net.hedtech.banner.general.communication.population.CommunicationPopulationQuery
import net.hedtech.banner.general.communication.population.CommunicationPopulationSelectionList
import net.hedtech.banner.general.communication.template.CommunicationEmailTemplate
import net.hedtech.banner.security.FormContext
import org.apache.commons.logging.LogFactory
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

import java.util.concurrent.TimeUnit

import static junit.framework.Assert.assertTrue

/**
 * CommunicationGroupSendAccessControlIntegrationTests tests proxying of user with insufficient privileges when enabled.
 */
class CommunicationGroupSendAccessControlIntegrationExample {
} // extends CommunicationBaseIntegrationTestCase {
//    def log = LogFactory.getLog( this.class )
//    def selfServiceBannerAuthenticationProvider
//
//    @Before
//    public void setUp() {
//        //super.setUseTransactions( false )
//        FormContext.set(['SELFSERVICE'])
//        def auth = selfServiceBannerAuthenticationProvider.authenticate( new UsernamePasswordAuthenticationToken( 'BCMRECR', '111111' ) )
//        SecurityContextHolder.getContext().setAuthentication( auth )
//        super.setUp()
//
//        communicationGroupSendMonitor.startMonitoring()
//        communicationGroupSendItemProcessingEngine.startRunning()
//        communicationJobProcessingEngine.startRunning()
//    }
//
//
//    @After
//    public void tearDown() {
//        communicationGroupSendMonitor.shutdown()
//        communicationGroupSendItemProcessingEngine.stopRunning()
//        communicationJobProcessingEngine.stopRunning()
//
//        super.tearDown()
//        sessionFactory.currentSession?.close()
//        logout()
//    }
//
//    @Test
//    public void testGroupSendUserAccess() {
//        mailServer.start()
//        CommunicationGroupSend groupSend
//        sessionFactory.currentSession.with { session ->  //Ensure a transaction is started and committed for async threads.
//            def tx = session.beginTransaction()
//            CommunicationPopulationQuery populationQuery = communicationPopulationQueryService.create(newPopulationQuery("testPop"))
//            assertTrue(populationQuery.valid)
//
//            Long populationSelectionListId = communicationPopulationExecutionService.execute(populationQuery.id)
//            CommunicationPopulationSelectionList selectionList = communicationPopulationSelectionListService.get(populationSelectionListId)
//            assertEquals(5, selectionList.getLastCalculatedCount())
//
//            CommunicationField testDataField = new CommunicationField(
//                    folder: defaultFolder,
//                    name: "testDataField",
//                    returnsArrayArguments: false,
//                    formatString: "\$spriden_id\$",
//                    ruleContent: "SELECT spraddr_street_line1 FROM spraddr WHERE spraddr_pidm = :pidm"
//            )
//            communicationFieldService.create( testDataField )
//
//            CommunicationEmailTemplate emailTemplate = new CommunicationEmailTemplate (
//                    name: "testGroupSendUserAccess_template",
//                    personal: false,
//                    active: true,
//                    oneOff: false,
//                    folder: defaultFolder,
//                    toList: "test@test.edu",
//                    subject: "test subject",
//                    content: "test content is \$testDataField\$"
//            )
//            emailTemplate = communicationEmailTemplateService.create( emailTemplate )
//            emailTemplate = communicationEmailTemplateService.publish( emailTemplate )
//
//            CommunicationGroupSendRequest request = new CommunicationGroupSendRequest(
//                    populationId: populationSelectionListId,
//                    templateId: emailTemplate.id,
//                    organizationId: defaultOrganization.id,
//                    referenceId: UUID.randomUUID().toString()
//            )
//
//            groupSend = communicationGroupSendCommunicationService.sendAsynchronousGroupCommunication(request)
//            assertNotNull(groupSend)
//            tx.commit()
//        }
//
//        assertEquals( 5, communicationGroupSendItemService.fetchByGroupSend( groupSend ).size() )
//
//        def sendviewdetails = CommunicationGroupSendView.findAll()
//        assertEquals(1, sendviewdetails.size())
//
//        List groupSendItemList = communicationGroupSendItemService.list()
//        assertEquals( 5, groupSendItemList.size() )
//        CommunicationGroupSendItem found = groupSendItemList.get( 0 ) as CommunicationGroupSendItem
//        assertEquals( CommunicationGroupSendItemExecutionState.Ready, found.currentExecutionState)
//
//        assertEquals( 5, CommunicationGroupSendItem.fetchByReadyExecutionState().size() )
//
//        sleepUntilGroupSendItemsFailed( groupSend, 5, 30 )
//
//        int countCompleted = CommunicationGroupSendItem.fetchByFailedExecutionStateAndGroupSend( groupSend ).size()
//        assertEquals( 5, countCompleted )
//    }
//
//    private void sleepUntilGroupSendItemsFailed( CommunicationGroupSend groupSend, long totalNumJobs, int maxSleepTime ) {
//        final int interval = 2;                 // test every second
//        int count = maxSleepTime / interval;    // calculate max loop count
//        while (count > 0) {
//            count--;
//            TimeUnit.SECONDS.sleep( interval );
//
//            int countCompleted = CommunicationGroupSendItem.fetchByCompleteExecutionStateAndGroupSend( groupSend ).size()
//
//            if ( countCompleted >= totalNumJobs) {
//                break;
//            }
//        }
//    }
//
//    private def newPopulationQuery( String queryName ) {
//        def populationQuery = new CommunicationPopulationQuery(
//                // Required fields
//                folder: defaultFolder,
//                name: queryName,
//                description: "test description",
//                sqlString: "select spriden_pidm from spriden where rownum < 6 and spriden_change_ind is null"
//        )
//
//        return populationQuery
//    }
//
//
//}
