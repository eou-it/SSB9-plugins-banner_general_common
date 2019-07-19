/*******************************************************************************
 Copyright 2016-2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import grails.gorm.transactions.Transactional
import grails.testing.mixin.integration.Integration
import grails.util.GrailsWebMockUtil
import grails.web.servlet.context.GrailsWebApplicationContext
import net.hedtech.banner.general.communication.CommunicationBaseConcurrentTestCase
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQuery
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQueryExtractStatement
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQueryType
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQueryVersion
import net.hedtech.banner.general.communication.population.selectionlist.CommunicationPopulationSelectionList
import net.hedtech.banner.general.communication.population.selectionlist.CommunicationPopulationSelectionListEntry
import org.grails.plugins.testing.GrailsMockHttpServletRequest
import org.grails.plugins.testing.GrailsMockHttpServletResponse
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.hibernate.SessionFactory
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.quartz.JobKey
import org.quartz.TriggerKey
import org.quartz.impl.matchers.GroupMatcher
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.transaction.annotation.Propagation

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

@Integration
class CommunicationPopulationCompositeServiceConcurrentTests extends CommunicationBaseConcurrentTestCase {
    def selfServiceBannerAuthenticationProvider
    def quartzScheduler
    def asynchronousBannerAuthenticationSpoofer;
    def grailsApplication

//    static transactional = false

    @Before
    public void setUp() {
        formContext = ['GUAGMNU','SELFSERVICE']
        webAppCtx = new GrailsWebApplicationContext()
        mockRequest()
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('BCMADMIN', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)
        super.setUp()

//        SessionFactory sessionFactory = grailsApplication.getMainContext().getBean('sessionFactory')
        asynchronousBannerAuthenticationSpoofer.authenticateAndSetFormContextForExecute()

        for (String groupName : quartzScheduler.getJobGroupNames()) {
            for (JobKey jobKey : quartzScheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                quartzScheduler.unscheduleJob( TriggerKey.triggerKey( jobKey.getName(), jobKey.getGroup() ) )
                quartzScheduler.deleteJob( jobKey )
            }
        }

//        communicationGroupSendMonitor.startMonitoring()
//        communicationGroupSendItemProcessingEngine.startRunning()
//        communicationJobProcessingEngine.startRunning()
    }

    public GrailsWebRequest mockRequest() {
        GrailsMockHttpServletRequest mockRequest = new GrailsMockHttpServletRequest();
        GrailsMockHttpServletResponse mockResponse = new GrailsMockHttpServletResponse();
        GrailsWebMockUtil.bindMockWebRequest(webAppCtx, mockRequest, mockResponse)
    }

    @After
    public void tearDown() {
        communicationGroupSendMonitor.shutdown()
        communicationGroupSendItemProcessingEngine.stopRunning()
        communicationJobProcessingEngine.stopRunning()

//        deleteAll()
        super.tearDown()
        sessionFactory.currentSession?.close()
        logout()
    }

    public void setUpData() {
        deleteAll()
        setUpDefaultOrganization()
        setUpDefaultFolder()
    }

    @Test
    @Transactional
    public void testAllSpridenPopulationFromQuery() {
//        setUpData()
        setUpDefaultFolder()
        CommunicationPopulationQuery populationQuery = new CommunicationPopulationQuery(
                folder: defaultFolder,
                name: "testAllSpridenPopulationFromQuery",
                description: "test description",
                queryString: "select SPRIDEN_PIDM from SPRIDEN"
        )

        populationQuery = communicationPopulationQueryCompositeService.createPopulationQuery( populationQuery )
        CommunicationPopulationQueryVersion queryVersion = communicationPopulationQueryCompositeService.publishPopulationQuery( populationQuery )
//        populationQuery.refresh()
//        assertFalse( populationQuery.changesPending )

        CommunicationPopulation population = communicationPopulationCompositeService.createPopulationFromQuery( populationQuery, "testAllSpridenPopulationFromQuery" )
        assertNotNull( population.id )
        assertEquals( "testAllSpridenPopulationFromQuery", population.name )

        List associations = CommunicationPopulationQueryAssociation.findAllByPopulation( population )
        assertEquals( 1, associations.size() )
        CommunicationPopulationQueryAssociation association = associations.get( 0 ) as CommunicationPopulationQueryAssociation
        assertNotNull( association.id )
        assertEquals( population.id, association.population.id )
        assertEquals( populationQuery.id, association.populationQuery.id )
        assertNull( association.populationQueryVersion )

        CommunicationPopulationCalculation populationCalculation = CommunicationPopulationCalculation.findLatestByPopulationIdAndCalculatedBy(population.id, 'BCMADMIN')
        assertEquals( populationCalculation.status, CommunicationPopulationCalculationStatus.PENDING_EXECUTION )
        def isAvailable = {
            def theCalculation = CommunicationPopulationCalculation.get( it )
            theCalculation.refresh()
            return theCalculation.status == CommunicationPopulationCalculationStatus.AVAILABLE
        }
        assertTrueWithRetry( isAvailable, populationCalculation.id, 15, 5 )
        assertTrue( populationCalculation.calculatedCount >= 1000 )
    }

    @Test
    public void testCreatePopulationSelectionExtractQuery() {
        setUpData()
        CommunicationPopulationQueryExtractStatement extractStatement = new CommunicationPopulationQueryExtractStatement()
        extractStatement.application = 'ADMISSIONS'
        extractStatement.selection = '199610_APPLICANTS'
        extractStatement.creatorId = 'SAISUSR'
        extractStatement.userId = 'SAISUSR'
        String extractQueryString = extractStatement.getQueryString()

        CommunicationPopulationQuery populationQuery = new CommunicationPopulationQuery(
            folder: defaultFolder,
            name: "testCreatePopulationSelectionExtractQuery",
            description: "test description",
            type: CommunicationPopulationQueryType.POPULATION_SELECTION_EXTRACT,
            queryString: extractQueryString
        )

        populationQuery = communicationPopulationQueryCompositeService.createPopulationQuery( populationQuery )
        CommunicationPopulationQueryVersion queryVersion = communicationPopulationQueryCompositeService.publishPopulationQuery( populationQuery )
        assertFalse( populationQuery.changesPending )

        CommunicationPopulation population = communicationPopulationCompositeService.createPopulationFromQuery( populationQuery, "testPopulation", "testPopulationDescription" )
        assertNotNull( population.id )
        assertEquals( "testPopulation", population.name )
        assertEquals( "testPopulationDescription", population.description )

        List associations = CommunicationPopulationQueryAssociation.findAllByPopulation( population )
        assertEquals( 1, associations.size() )
        CommunicationPopulationQueryAssociation association = associations.get( 0 ) as CommunicationPopulationQueryAssociation
        assertNotNull( association.id )
        assertEquals( population.id, association.population.id )
        assertEquals( populationQuery.id, association.populationQuery.id )
        assertNull( association.populationQueryVersion )

        waitForPopulationCalculationToFinish( population, 'BCMADMIN' )
    }

    @Test
    void testPopulationWithOnlyIncludeList() {
        setUpData()
        CommunicationPopulation population = communicationPopulationCompositeService.createPopulation( defaultFolder, "testPopulation", "testPopulation description" )
        List<String> persons = ['BCMADMIN', 'BCMUSER', 'BCMAUTHOR']

        CommunicationPopulationSelectionListBulkResults results = communicationPopulationCompositeService.addPersonsToIncludeList( population, persons )
        assertNotNull( results.population.includeList )
        def entryCount = CommunicationPopulationSelectionListEntry.countByPopulationSelectionList( population.includeList )
        assertEquals( 3, entryCount )
        assertEquals( 3, results.insertedCount )
        assertEquals( 0, results.notExistCount )
        assertEquals( 0, results.ignoredCount)
        assertEquals( 0, results.duplicateCount)

        persons = [ 'CMOORE', '710000029' ]
        results = communicationPopulationCompositeService.addPersonsToIncludeList( population, persons )
        entryCount = CommunicationPopulationSelectionListEntry.countByPopulationSelectionList( results.population.includeList )
        assertEquals( 4, entryCount )
        assertEquals( 1, results.insertedCount )
        assertEquals( 1, results.notExistCount )
        assertEquals( 0, results.duplicateCount)
        assertEquals( 1, results.ignoredCount )

        assertEquals( 0, CommunicationPopulationQueryAssociation.countByPopulation( population ) )

        CommunicationPopulationSelectionList copy = communicationPopulationCompositeService.cloneSelectionList( population.includeList )
        assertNotNull( copy.id )

        assertNull( communicationPopulationCompositeService.calculatePopulationForUser( population ) )
    }

    @Test
    void testPopulationWithQueryAndIncludeList() {
        setUpData()
        CommunicationPopulationQuery populationQuery = new CommunicationPopulationQuery(
                folder: defaultFolder,
                name: "testQuery",
                description: "test description",
                queryString: "select SPRIDEN_PIDM from SPRIDEN where SPRIDEN_CHANGE_IND is null and rownum < 6"
        )

        populationQuery = communicationPopulationQueryCompositeService.createPopulationQuery( populationQuery )
        CommunicationPopulationQueryVersion queryVersion = communicationPopulationQueryCompositeService.publishPopulationQuery( populationQuery )
        assertFalse( populationQuery.changesPending )

        CommunicationPopulation population = communicationPopulationCompositeService.createPopulationFromQuery( populationQuery, "testQuery" )
        waitForPopulationCalculationToFinish( population, 'BCMADMIN' )
        assertEquals( 1, CommunicationPopulationVersion.countByPopulation( population ) )
        assertEquals( 1, CommunicationPopulationCalculation.count() )

        List<String> persons = ['BCMADMIN', 'BCMUSER', 'BCMAUTHOR']
        CommunicationPopulationSelectionListBulkResults results = communicationPopulationCompositeService.addPersonsToIncludeList( population, persons )
        assertNotNull( results.population.includeList )
        population.refresh()
        assertTrue( population.changesPending )

        CommunicationPopulationListView populationListView = CommunicationPopulationListView.fetchLatestByPopulation( population )
        int totalCount = CommunicationPopulationProfileView.findTotalCountByPopulation( populationListView )
        assertEquals( 8, totalCount )

        communicationPopulationCompositeService.calculatePopulationForUser( population )
        waitForPopulationCalculationToFinish( population, 'BCMADMIN' )
        assertEquals( 1, CommunicationPopulationVersion.countByPopulation( population ) )
        assertEquals( 1, CommunicationPopulationCalculation.count() )

        communicationPopulationCompositeService.calculatePopulationForUser( population )
        waitForPopulationCalculationToFinish( population, 'BCMADMIN' )
        assertEquals( 1, CommunicationPopulationVersion.countByPopulation( population ) )
        assertEquals( 1, CommunicationPopulationCalculation.count() )

    }

    @Test
    public void testScheduledAllSpridenPopulationFromQuery() {
        setUpData()
        CommunicationPopulationQuery populationQuery = new CommunicationPopulationQuery(
                folder: defaultFolder,
                name: "testAllSpridenPopulationFromQuery",
                description: "test description",
                queryString: "select SPRIDEN_PIDM from SPRIDEN"
        )

        populationQuery = communicationPopulationQueryCompositeService.createPopulationQuery( populationQuery )
        CommunicationPopulationQueryVersion queryVersion = communicationPopulationQueryCompositeService.publishPopulationQuery( populationQuery )
        assertFalse( populationQuery.changesPending )

        Calendar now = Calendar.getInstance()
        now.add(Calendar.SECOND, 10)

        CommunicationPopulation population = communicationPopulationCompositeService.createPopulationFromQuery( populationQuery, "testAllSpridenPopulationFromQuery", "", now.getTime() )
        assertNotNull( population.id )
        assertEquals( "testAllSpridenPopulationFromQuery", population.name )
        assertEquals( CommunicationPopulationCalculationStatus.SCHEDULED, population.status)

        List associations = CommunicationPopulationQueryAssociation.findAllByPopulation( population )
        assertEquals( 1, associations.size() )
        CommunicationPopulationQueryAssociation association = associations.get( 0 ) as CommunicationPopulationQueryAssociation
        assertNotNull( association.id )
        assertEquals( population.id, association.population.id )
        assertEquals( populationQuery.id, association.populationQuery.id )
        assertNull( association.populationQueryVersion )

        CommunicationPopulationCalculation populationCalculation = CommunicationPopulationCalculation.findLatestByPopulationIdAndCalculatedBy( population.id, 'BCMADMIN' )
        assertEquals( populationCalculation.status, CommunicationPopulationCalculationStatus.PENDING_EXECUTION )
        def isAvailable = {
            def theCalculation = CommunicationPopulationCalculation.get( it )
            theCalculation.refresh()
            return theCalculation.status == CommunicationPopulationCalculationStatus.AVAILABLE
        }
        assertTrueWithRetry( isAvailable, populationCalculation.id, 15, 5 )
        assertTrue( populationCalculation.calculatedCount >= 1000 )
    }

    private void waitForPopulationCalculationToFinish(CommunicationPopulation population, String calculatedByBannerId) {
        CommunicationPopulationCalculation populationCalculation = CommunicationPopulationCalculation.findLatestByPopulationIdAndCalculatedBy(population.id, calculatedByBannerId)
        def isAvailable = {
            def theCalculation = CommunicationPopulationCalculation.get(it)
            theCalculation.refresh()
            return theCalculation.status == CommunicationPopulationCalculationStatus.AVAILABLE ||
                    theCalculation.status == CommunicationPopulationCalculationStatus.ERROR
        }
        assertTrueWithRetry(isAvailable, populationCalculation.id, 30, 10)
    }
}
