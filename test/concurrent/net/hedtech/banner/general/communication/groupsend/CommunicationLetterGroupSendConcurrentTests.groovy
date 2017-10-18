/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.groupsend

import groovy.sql.Sql
import net.hedtech.banner.general.communication.CommunicationBaseConcurrentTestCase
import net.hedtech.banner.general.communication.CommunicationErrorCode
import net.hedtech.banner.general.communication.field.CommunicationField
import net.hedtech.banner.general.communication.field.CommunicationFieldStatus
import net.hedtech.banner.general.communication.field.CommunicationRuleStatementType
import net.hedtech.banner.general.communication.job.CommunicationJob
import net.hedtech.banner.general.communication.job.CommunicationJobStatus
import net.hedtech.banner.general.communication.letter.CommunicationLetterItemView
import net.hedtech.banner.general.communication.merge.CommunicationRecipientData
import net.hedtech.banner.general.communication.letter.CommunicationLetterItem
import net.hedtech.banner.general.communication.letter.CommunicationLetterTemplate
import net.hedtech.banner.general.communication.population.CommunicationPopulation
import net.hedtech.banner.general.communication.population.CommunicationPopulationCalculation
import net.hedtech.banner.general.communication.population.CommunicationPopulationCalculationStatus
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
        CommunicationPopulationCalculation populationCalculation = CommunicationPopulationCalculation.findLatestByPopulationIdAndCalculatedBy( population.id, 'BCMADMIN' )
        assertEquals( populationCalculation.status, CommunicationPopulationCalculationStatus.PENDING_EXECUTION )
        def isAvailable = {
            def theCalculation = CommunicationPopulationCalculation.get( it )
            theCalculation.refresh()
            return theCalculation.status == CommunicationPopulationCalculationStatus.AVAILABLE
        }
        assertTrueWithRetry( isAvailable, populationCalculation.id, 15, 5 )

        List queryAssociations = CommunicationPopulationVersionQueryAssociation.findByPopulationVersion( populationCalculation.populationVersion )
        assertEquals( 1, queryAssociations.size() )

        def selectionListEntryList = CommunicationPopulationSelectionListEntry.fetchBySelectionListId( populationCalculation.selectionList.id )
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
            return CommunicationGroupSendItem.fetchByGroupSend( each ).size() == 5
        }
        assertTrueWithRetry( checkExpectedGroupSendItemsCreated, groupSend.id, 15, 5 )

        // Confirm group send view returns the correct results
        def sendViewDetails = CommunicationGroupSendDetailView.findAll()
        assertEquals(1, sendViewDetails.size())

        def sendListView = CommunicationGroupSendListView.findAll()
        assertEquals(1, sendListView.size())

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
        CommunicationLetterItemView itemView = CommunicationLetterItemView.fetchByGroupSendItemId( sendItemViewDetails[0].id )
        assertTrue( itemView != null )
        assertEquals( sendItemViewDetails[0].id, itemView.groupSendItemId )

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
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def row = sql.rows("select GOBTPAC_EXTERNAL_USER, GOBTPAC_PIDM from GV_GOBTPAC where GOBTPAC_EXTERNAL_USER is not null and rownum = 1" )[0]
        String testExternalUser = row.GOBTPAC_EXTERNAL_USER

        CommunicationPopulationQuery populationQuery = new CommunicationPopulationQuery(
                folder: defaultFolder,
                name: "testPersonalizationQuery",
                description: "test description",
                queryString: "select gobtpac_pidm from gobtpac where gobtpac_external_user = '${testExternalUser}'"
        )
        populationQuery = communicationPopulationQueryCompositeService.createPopulationQuery( populationQuery )
        CommunicationPopulationQueryVersion queryVersion = communicationPopulationQueryCompositeService.publishPopulationQuery( populationQuery )

        CommunicationPopulation population = communicationPopulationCompositeService.createPopulationFromQuery( populationQuery, "testPersonalization population" )
        CommunicationPopulationCalculation populationCalculation = CommunicationPopulationCalculation.findLatestByPopulationIdAndCalculatedBy( population.id, 'BCMADMIN' )
        assertEquals( populationCalculation.status, CommunicationPopulationCalculationStatus.PENDING_EXECUTION )
        def isAvailable = {
            def theCalculation = CommunicationPopulationCalculation.get( it )
            theCalculation.refresh()
            return theCalculation.status == CommunicationPopulationCalculationStatus.AVAILABLE
        }
        assertTrueWithRetry( isAvailable, populationCalculation.id, 15, 5 )

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
                ruleContent: "select gobtpac_external_user from gobtpac where gobtpac_external_user = '${testExternalUser}' and :pidm = :pidm"
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
        assertTrueWithRetry( letterItemCreated, 1, 15, 5 )

        CommunicationLetterItem item = communicationLetterItemService.list().get( 0 )
        assertEquals( testExternalUser, item.toAddress )
        assert( item.content.indexOf( "test description ${testExternalUser}" ) >= 0 )
    }

    @Test
    public void testGroupSendRequestMissingToAddress() {
        CommunicationPopulationQuery populationQuery = communicationPopulationQueryCompositeService.createPopulationQuery(newPopulationQuery("testPop", 1))
        CommunicationPopulationQueryVersion queryVersion = communicationPopulationQueryCompositeService.publishPopulationQuery( populationQuery )
        populationQuery = queryVersion.query

        CommunicationPopulation population = communicationPopulationCompositeService.createPopulationFromQuery( populationQuery, "testPopulation" )
        CommunicationPopulationCalculation populationCalculation = CommunicationPopulationCalculation.findLatestByPopulationIdAndCalculatedBy( population.id, 'BCMADMIN' )
        assertEquals( populationCalculation.status, CommunicationPopulationCalculationStatus.PENDING_EXECUTION )
        def isAvailable = {
            def theCalculation = CommunicationPopulationCalculation.get( it )
            theCalculation.refresh()
            return theCalculation.status == CommunicationPopulationCalculationStatus.AVAILABLE
        }
        assertTrueWithRetry( isAvailable, populationCalculation.id, 15, 5 )

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
        sleepUntilCommunicationJobsComplete( 120 )
        List communicationJobList = CommunicationJob.list()
        assertEquals( 1, communicationJobList.size() )
        CommunicationJob communicationJob = (CommunicationJob) communicationJobList.get(0)
        assertEquals(CommunicationJobStatus.FAILED, communicationJob.status )
        assertEquals(CommunicationErrorCode.EMPTY_LETTER_TO_ADDRESS, communicationJob.errorCode )
        List letterItemViewList = CommunicationLetterItemView.list()
        assertEquals( 0, letterItemViewList.size() )
    }

    @Test
    public void testGroupSendRequestMissingContent() {
        CommunicationPopulationQuery populationQuery = communicationPopulationQueryCompositeService.createPopulationQuery(newPopulationQuery("testPop", 1))
        CommunicationPopulationQueryVersion queryVersion = communicationPopulationQueryCompositeService.publishPopulationQuery( populationQuery )
        populationQuery = queryVersion.query

        CommunicationPopulation population = communicationPopulationCompositeService.createPopulationFromQuery( populationQuery, "testPopulation" )
        CommunicationPopulationCalculation populationCalculation = CommunicationPopulationCalculation.findLatestByPopulationIdAndCalculatedBy( population.id, 'BCMADMIN' )
        assertEquals( populationCalculation.status, CommunicationPopulationCalculationStatus.PENDING_EXECUTION )
        def isAvailable = {
            def theCalculation = CommunicationPopulationCalculation.get( it )
            theCalculation.refresh()
            return theCalculation.status == CommunicationPopulationCalculationStatus.AVAILABLE
        }
        assertTrueWithRetry( isAvailable, populationCalculation.id, 15, 5 )

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
        sleepUntilCommunicationJobsComplete( 120 )
        List communicationJobList = CommunicationJob.list()
        assertEquals( 1, communicationJobList.size() )
        CommunicationJob communicationJob = (CommunicationJob) communicationJobList.get(0)
        assertEquals(CommunicationJobStatus.FAILED, communicationJob.status )
        assertEquals(CommunicationErrorCode.EMPTY_LETTER_CONTENT, communicationJob.errorCode )
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
