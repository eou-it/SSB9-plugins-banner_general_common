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
import net.hedtech.banner.general.communication.population.CommunicationPopulation
import net.hedtech.banner.general.communication.population.CommunicationPopulationCalculationStatus
import net.hedtech.banner.general.communication.population.CommunicationPopulationCompositeService
import net.hedtech.banner.general.communication.population.CommunicationPopulationVersion
import net.hedtech.banner.general.communication.population.CommunicationPopulationVersionQueryAssociation
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQuery
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQueryExecutionResult
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQueryVersion
import net.hedtech.banner.general.communication.population.selectionlist.CommunicationPopulationSelectionList
import net.hedtech.banner.general.communication.population.selectionlist.CommunicationPopulationSelectionListEntry
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
                templateId: defaultMobileNotificationTemplate.id,
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

        sleepUntilGroupSendItemsComplete( groupSend, 5, 30 )

        int countCompleted = CommunicationGroupSendItem.fetchByCompleteExecutionStateAndGroupSend( groupSend ).size()
        assertEquals( 5, countCompleted )

        sleepUntilCommunicationJobsComplete( 5, 120 )
        countCompleted = CommunicationJob.fetchCompleted().size()
        assertEquals( 5, countCompleted )

        sleepUntilGroupSendComplete( groupSend, 120 )

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
    public void testDeleteGroupSend() {
        testDeleteGroupSend( defaultMobileNotificationTemplate )
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
        populationQuery = communicationPopulationQueryCompositeService.createPopulationQuery( populationQuery )
        CommunicationPopulationQueryVersion queryVersion = communicationPopulationQueryCompositeService.publishPopulationQuery( populationQuery )

        CommunicationPopulation population = communicationPopulationCompositeService.createPopulationFromQuery( populationQuery, "testPersonalization population" )
        CommunicationPopulationVersion populationVersion = CommunicationPopulationVersion.findLatestByPopulationIdAndCreatedBy( population.id, 'BCMADMIN' )

        def isAvailable = {
            def aPopulationVersion = CommunicationPopulationVersion.get( it )
            aPopulationVersion.refresh()
            return aPopulationVersion.status == CommunicationPopulationCalculationStatus.AVAILABLE
        }
        assertTrueWithRetry( isAvailable, populationVersion.id, 30, 10 )

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
                populationId: population.id,
                templateId: mobileTemplate.id,
                organizationId: defaultOrganization.id,
                referenceId: UUID.randomUUID().toString(),
                recalculateOnSend: false
        )

        groupSend = communicationGroupSendCompositeService.sendAsynchronousGroupCommunication(request)
        assertNotNull(groupSend)

        sleepUntilGroupSendComplete( groupSend, 120 )

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
