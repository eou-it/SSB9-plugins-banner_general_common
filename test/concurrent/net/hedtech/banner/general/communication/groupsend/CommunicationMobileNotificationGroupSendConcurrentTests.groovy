package net.hedtech.banner.general.communication.groupsend

import groovy.json.JsonSlurper
import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.CommunicationBaseConcurrentTestCase
import net.hedtech.banner.general.communication.field.CommunicationField
import net.hedtech.banner.general.communication.field.CommunicationFieldStatus
import net.hedtech.banner.general.communication.field.CommunicationRuleStatementType
import net.hedtech.banner.general.communication.item.CommunicationMobileNotificationItem
import net.hedtech.banner.general.communication.item.CommunicationMobileNotificationItemService
import net.hedtech.banner.general.communication.job.CommunicationJob
import net.hedtech.banner.general.communication.merge.CommunicationRecipientData
import net.hedtech.banner.general.communication.population.CommunicationPopulationQuery
import net.hedtech.banner.general.communication.population.CommunicationPopulationSelectionList
import net.hedtech.banner.general.communication.template.CommunicationMobileNotificationTemplate
import org.apache.commons.logging.LogFactory
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

import java.util.concurrent.TimeUnit

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

class CommunicationMobileNotificationGroupSendConcurrentTests extends CommunicationBaseConcurrentTestCase {
    def log = LogFactory.getLog(this.class)
    def selfServiceBannerAuthenticationProvider
    CommunicationMobileNotificationTemplate defaultMobileNotificationTemplate


    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('BCMADMIN', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)
        super.setUp()

        communicationGroupSendMonitor.startMonitoring()
        communicationGroupSendItemProcessingEngine.startRunning()
        communicationJobProcessingEngine.startRunning()

        communicationSendMobileNotificationService.testOverride = [ externalUser: "cmobile" ]

        setUpDefaultMobileNotificationTemplate()
    }


    @After
    public void tearDown() {
        communicationSendMobileNotificationService.testOverride = null

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
        CommunicationPopulationQuery populationQuery = communicationPopulationQueryService.create(newPopulationQuery("testPop"))
        assertTrue(populationQuery.valid)

        Long populationSelectionListId = communicationPopulationExecutionService.execute(populationQuery.id)
        CommunicationPopulationSelectionList selectionList = communicationPopulationSelectionListService.get(populationSelectionListId)
        assertEquals(5, selectionList.getLastCalculatedCount())

        CommunicationGroupSendRequest request = new CommunicationGroupSendRequest(
                name: "testGroupSendRequestByTemplateByPopulationSendImmediately",
                populationId: populationSelectionListId,
                templateId: defaultMobileNotificationTemplate.id,
                organizationId: defaultOrganization.id,
                referenceId: UUID.randomUUID().toString()
        )

        groupSend = communicationGroupSendCommunicationService.sendAsynchronousGroupCommunication(request)
        assertNotNull(groupSend)

        assertEquals( 5, communicationGroupSendItemService.fetchByGroupSend( groupSend ).size() )

        def sendViewDetails = CommunicationGroupSendView.findAll()
        assertEquals(1, sendViewDetails.size())

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

//        MimeMessage[] messages = mailServer.getReceivedMessages();
//        assertNotNull(messages);
//        assertEquals(5, messages.length);
//
        sleepUntilGroupSendComplete( groupSend, 120 )
//
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
                templateId: defaultMobileNotificationTemplate.id,
                organizationId: defaultOrganization.id,
                referenceId: UUID.randomUUID().toString()
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

    @Test
    public void testPersonalization() {
        CommunicationGroupSend groupSend
        CommunicationPopulationQuery populationQuery = new CommunicationPopulationQuery(
                folder: defaultFolder,
                name: "testPersonalizationQuery",
                description: "test description",
                sqlString: "select gobtpac_pidm from gobtpac where gobtpac_external_user = 'cbeaver'"
        )
        populationQuery = communicationPopulationQueryService.create(populationQuery)
        assertTrue( populationQuery.valid )

        Long populationSelectionListId = communicationPopulationExecutionService.execute(populationQuery.id)
        CommunicationPopulationSelectionList selectionList = communicationPopulationSelectionListService.get(populationSelectionListId)
        assertEquals(1, selectionList.getLastCalculatedCount())

        CommunicationField communicationField = new CommunicationField(
                // Required fields
                folder: defaultFolder,
                immutableId: UUID.randomUUID().toString(),
                name: "name",
                returnsArrayArguments: false,

                formatString: "\$gobtpac_external_user\$",
                previewValue: "MB",
                renderAsHtml: false,
                status: CommunicationFieldStatus.DEVELOPMENT,
                statementType: CommunicationRuleStatementType.SQL_PREPARED_STATEMENT,
                ruleContent: "select gobtpac_external_user from gobtpac where gobtpac_external_user = 'cbeaver' and :pidm = :pidm"
        )
        communicationField = communicationFieldService.create( [domainModel: communicationField] )
        communicationField = communicationFieldService.publishDataField( [id: communicationField.id] )

        CommunicationMobileNotificationTemplate mobileTemplate = new CommunicationMobileNotificationTemplate (
                name: "testPersonalization",
                personal: false,
                oneOff: false,
                folder: defaultFolder,

                mobileHeadline: "testPersonalization from BCM",
                headline: "name = \$name\$",
                messageDescription: "test description",
                destinationLink: "http://www.amazon.com",
                destinationLabel: "Amazon",

                push: true,
                sticky: false
        )
        mobileTemplate = communicationMobileNotificationTemplateService.create( mobileTemplate )
        mobileTemplate = communicationMobileNotificationTemplateService.publish( mobileTemplate )

        CommunicationGroupSendRequest request = new CommunicationGroupSendRequest(
                name: "testPersonalization",
                populationId: populationSelectionListId,
                templateId: mobileTemplate.id,
                organizationId: defaultOrganization.id,
                referenceId: UUID.randomUUID().toString()
        )

        groupSend = communicationGroupSendCommunicationService.sendAsynchronousGroupCommunication(request)
        assertNotNull(groupSend)

        assertEquals( 1, communicationGroupSendItemService.fetchByGroupSend( groupSend ).size() )

        def sendViewDetails = CommunicationGroupSendView.findAll()
        assertEquals(1, sendViewDetails.size())

        List groupSendItemList = communicationGroupSendItemService.list()
        assertEquals( 1, groupSendItemList.size() )
        CommunicationGroupSendItem found = groupSendItemList.get( 0 ) as CommunicationGroupSendItem
        assertEquals( CommunicationGroupSendItemExecutionState.Ready, found.currentExecutionState)

        def sendItemViewDetails = CommunicationGroupSendItemView.findAll()
        assertEquals(1, sendItemViewDetails.size())

        assertEquals( 1, CommunicationGroupSendItem.fetchByReadyExecutionState().size() )

        sleepUntilGroupSendItemsComplete( groupSend, 1, 60 )

        int countCompleted = CommunicationGroupSendItem.fetchByCompleteExecutionStateAndGroupSend( groupSend ).size()
        assertEquals( 1, countCompleted )

        sleepUntilCommunicationJobsComplete( 1, 60 )
        countCompleted = CommunicationJob.fetchCompleted().size()
        assertEquals( 1, countCompleted )

        List itemList = communicationMobileNotificationItemService.list()
        assertEquals( 1, itemList.size() )
        CommunicationMobileNotificationItem item = itemList.get( 0 )
// the item now has serverResponse to store all the individual values.  The server response
// is a string formatted as [{"":"","":"",....}].  So strip of the begin and end characters and get the JSON map out
        def jsonSlurper = new JsonSlurper()
        def serverResponseMap = jsonSlurper.parseText(item.serverResponse.substring(1, item.serverResponse.length()-1))

        assert serverResponseMap instanceof Map
        assertEquals( "testPersonalization from BCM", serverResponseMap.mobileHeadline )
        assertEquals( "name = cbeaver", serverResponseMap.headline )
        assertEquals( "test description", serverResponseMap.description )
        assertEquals( "http://www.amazon.com", serverResponseMap.destination )
        assertEquals( "Amazon", serverResponseMap.destinationLabel )
        assertNotNull( serverResponseMap.createDate )
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


    protected void setUpDefaultMobileNotificationTemplate() {
        defaultMobileNotificationTemplate = CommunicationMobileNotificationTemplate.findByName( "CommunicationMobileNotificationGroupSendConcurrentTests_template" )
        if (!defaultMobileNotificationTemplate) {
            defaultMobileNotificationTemplate = new CommunicationMobileNotificationTemplate (
                    name: "CommunicationMobileNotificationGroupSendConcurrentTests_template",
                    personal: false,
                    oneOff: false,
                    folder: defaultFolder,

                    mobileHeadline: "test mobile headline",
                    headline: "test headline",
                    messageDescription: "test description",
                    destinationLink: "test.edu",
                    destinationLabel: "test edu",

                    push: true,
                    sticky: false
            )
            defaultMobileNotificationTemplate = communicationMobileNotificationTemplateService.create( defaultMobileNotificationTemplate )
            defaultMobileNotificationTemplate = communicationMobileNotificationTemplateService.publish( defaultMobileNotificationTemplate )
        }
    }
}
