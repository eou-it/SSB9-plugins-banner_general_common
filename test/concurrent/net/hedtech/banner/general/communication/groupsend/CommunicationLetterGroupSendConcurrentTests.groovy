/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.groupsend

import net.hedtech.banner.general.communication.CommunicationBaseConcurrentTestCase
import net.hedtech.banner.general.communication.CommunicationErrorCode
import net.hedtech.banner.general.communication.field.CommunicationField
import net.hedtech.banner.general.communication.field.CommunicationFieldStatus
import net.hedtech.banner.general.communication.field.CommunicationRuleStatementType
import net.hedtech.banner.general.communication.job.CommunicationJob
import net.hedtech.banner.general.communication.letter.CommunicationLetterItemView
import net.hedtech.banner.general.communication.merge.CommunicationRecipientData
import net.hedtech.banner.general.communication.letter.CommunicationLetterItem
import net.hedtech.banner.general.communication.letter.CommunicationLetterTemplate
import net.hedtech.banner.general.communication.population.CommunicationPopulation
import net.hedtech.banner.general.communication.population.CommunicationPopulationCalculationStatus
import net.hedtech.banner.general.communication.population.CommunicationPopulationVersion
import net.hedtech.banner.general.communication.population.CommunicationPopulationVersionQueryAssociation
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQuery
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQueryVersion
import net.hedtech.banner.general.communication.population.selectionlist.CommunicationPopulationSelectionListEntry
import org.apache.commons.logging.LogFactory
import org.hibernate.ScrollMode;
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

class CommunicationLetterGroupSendConcurrentTests extends CommunicationBaseConcurrentTestCase {
    def log = LogFactory.getLog(this.class)
    def selfServiceBannerAuthenticationProvider
    CommunicationLetterTemplate defaultLetterTemplate


    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('BCMADMIN', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)
        super.setUp()

        communicationGroupSendMonitor.startMonitoring()
        communicationGroupSendItemProcessingEngine.startRunning()
        communicationJobProcessingEngine.startRunning()

        setUpDefaultLetterTemplate()
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
                templateId: defaultLetterTemplate.id,
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

        sleepUntilGroupSendItemsComplete( groupSend, 60 )

        int countCompleted = CommunicationGroupSendItem.fetchByCompleteExecutionStateAndGroupSend( groupSend ).size()
        assertEquals( 5, countCompleted )

        sleepUntilCommunicationJobsComplete( 10 * 60 )
        countCompleted = CommunicationJob.fetchCompleted().size()
        assertEquals( 5, countCompleted )

        sleepUntilGroupSendComplete( groupSend, 3 * 60 )

        CommunicationLetterItemView.withSession() { session ->
            assertEquals( 5, CommunicationLetterItemView.fetchCountByGroupSendId( groupSend.id ) )

            def criteria = CommunicationLetterItemView.createCriteria().buildCriteria() {
                eq( 'groupSendId', groupSend.id )
                order('lastName', 'asc')
            }
            def result = criteria.scroll(ScrollMode.FORWARD_ONLY)
            int letterItemViewCount = 0;
            while (result.next()) {
                letterItemViewCount++
                CommunicationLetterItemView letterItemView = (CommunicationLetterItemView) result.get()[0]
                assertEquals( groupSend.id, letterItemView.groupSendId )
                assertNotNull( letterItemView.sentDate )
            }
            assertEquals( 5, letterItemViewCount )
        }

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
        testDeleteGroupSend( defaultLetterTemplate )
    }


    @Test
    public void testPersonalization() {
        CommunicationPopulationQuery populationQuery = new CommunicationPopulationQuery(
                folder: defaultFolder,
                name: "testPersonalizationQuery",
                description: "test description",
                queryString: "select gobtpac_pidm from gobtpac where gobtpac_external_user = 'cbeaver'"
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
        assertEquals( 1, populationVersion.calculatedCount )

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

        CommunicationLetterTemplate letterTemplate = new CommunicationLetterTemplate (
                name: "testPersonalization",
                personal: false,
                oneOff: false,
                folder: defaultFolder,

                toAddress: "\$name\$",
                content: "test description \$name\$",

                push: true,
                sticky: false
        )
        letterTemplate = communicationLetterTemplateService.create( letterTemplate )
        letterTemplate = communicationLetterTemplateService.publish( letterTemplate )

        CommunicationGroupSendRequest request = new CommunicationGroupSendRequest(
                name: "testPersonalization",
                populationId: population.id,
                templateId: letterTemplate.id,
                organizationId: defaultOrganization.id,
                referenceId: UUID.randomUUID().toString(),
                recalculateOnSend: false
        )

        CommunicationGroupSend groupSend = communicationGroupSendCompositeService.sendAsynchronousGroupCommunication(request)
        assertNotNull(groupSend)

        sleepUntilGroupSendComplete( groupSend, 120 )

        def letterItemCreated = {
            return communicationLetterItemService.list().size() == it
        }
        assertTrueWithRetry( letterItemCreated, 1, 30, 10 )

        CommunicationLetterItem item = communicationLetterItemService.list().get( 0 )
        assertEquals( "cbeaver", item.toAddress )
        assert( item.content.indexOf( "test description cbeaver" ) >= 0 )
    }

    @Test
    public void testGroupSendRequestMissingToAddress() {
        CommunicationPopulationQuery populationQuery = communicationPopulationQueryCompositeService.createPopulationQuery(newPopulationQuery("testPop", 1))
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
                ruleContent: "select '' name from dual where :pidm = :pidm"
        )
        communicationField = communicationFieldService.create( [domainModel: communicationField] )
        communicationField = communicationFieldService.publishDataField( [id: communicationField.id] )

        CommunicationLetterTemplate letterTemplate = new CommunicationLetterTemplate (
                name: "testPersonalization",
                personal: false,
                oneOff: false,
                folder: defaultFolder,

                toAddress: "\$name\$",
                content: "test description \$name\$",

                push: true,
                sticky: false
        )
        letterTemplate = communicationLetterTemplateService.create( letterTemplate )
        letterTemplate = communicationLetterTemplateService.publish( letterTemplate )

        CommunicationGroupSendRequest request = new CommunicationGroupSendRequest(
                name: "testGroupSendRequestMissingToAddress",
                populationId: population.id,
                templateId: letterTemplate.id,
                organizationId: defaultOrganization.id,
                referenceId: UUID.randomUUID().toString(),
                recalculateOnSend: false
        )

        CommunicationGroupSend groupSend = communicationGroupSendCompositeService.sendAsynchronousGroupCommunication(request)
        assertNotNull(groupSend)

        sleepUntilGroupSendComplete( groupSend, 120 )
        List groupSendItemList = CommunicationGroupSendItem.list()
        assertEquals( 1, groupSendItemList.size() )
        List communicationJobList = CommunicationJob.list()
        assertEquals( 1, communicationJobList.size() )
        assertEquals(CommunicationErrorCode.EMPTY_LETTER_TO_ADDRESS, communicationJobList.get(0).errorCode )
        List letterItemViewList = CommunicationLetterItemView.list()
        assertEquals( 0, letterItemViewList.size() )
    }

    @Test
    public void testGroupSendRequestMissingContent() {
        CommunicationPopulationQuery populationQuery = communicationPopulationQueryCompositeService.createPopulationQuery(newPopulationQuery("testPop", 1))
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
                ruleContent: "select '' name from dual where :pidm = :pidm"
        )
        communicationField = communicationFieldService.create( [domainModel: communicationField] )
        communicationField = communicationFieldService.publishDataField( [id: communicationField.id] )

        CommunicationLetterTemplate letterTemplate = new CommunicationLetterTemplate (
                name: "testPersonalization",
                personal: false,
                oneOff: false,
                folder: defaultFolder,

                toAddress: "whatever",
                content: "\$name\$",

                push: true,
                sticky: false
        )
        letterTemplate = communicationLetterTemplateService.create( letterTemplate )
        letterTemplate = communicationLetterTemplateService.publish( letterTemplate )

        CommunicationGroupSendRequest request = new CommunicationGroupSendRequest(
                name: "testGroupSendRequestMissingContent",
                populationId: population.id,
                templateId: letterTemplate.id,
                organizationId: defaultOrganization.id,
                referenceId: UUID.randomUUID().toString(),
                recalculateOnSend: false
        )

        CommunicationGroupSend groupSend = communicationGroupSendCompositeService.sendAsynchronousGroupCommunication(request)
        assertNotNull(groupSend)

        sleepUntilGroupSendComplete( groupSend, 120 )
        List groupSendItemList = CommunicationGroupSendItem.list()
        assertEquals( 1, groupSendItemList.size() )
        List communicationJobList = CommunicationJob.list()
        assertEquals( 1, communicationJobList.size() )
        assertEquals(CommunicationErrorCode.EMPTY_LETTER_CONTENT, communicationJobList.get(0).errorCode )
        List letterItemViewList = CommunicationLetterItemView.list()
        assertEquals( 0, letterItemViewList.size() )
    }

    protected void setUpDefaultLetterTemplate() {
        defaultLetterTemplate = CommunicationLetterTemplate.findByName( "CommunicationLetterGroupSendConcurrentTests_template" )
        if (!defaultLetterTemplate) {
            defaultLetterTemplate = new CommunicationLetterTemplate (
                    name: "CommunicationLetterGroupSendConcurrentTests_template",
                    personal: false,
                    oneOff: false,
                    folder: defaultFolder,

                    toAddress: "my to address",
                    content: "my content"
            )
            assertNotNull( defaultLetterTemplate )
            defaultLetterTemplate = communicationLetterTemplateService.create( defaultLetterTemplate )
            assertNotNull( defaultLetterTemplate )
            assertNotNull( defaultLetterTemplate.id )
            defaultLetterTemplate = communicationLetterTemplateService.publish( defaultLetterTemplate )
        }
    }
}
