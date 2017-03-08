/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import net.hedtech.banner.general.communication.CommunicationBaseConcurrentTestCase
import net.hedtech.banner.general.communication.CommunicationErrorCode
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQuery
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQueryExtractStatement
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQueryType
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQueryVersion
import net.hedtech.banner.general.communication.population.selectionlist.CommunicationPopulationSelectionList
import net.hedtech.banner.general.communication.population.selectionlist.CommunicationPopulationSelectionListEntry
import org.apache.commons.logging.LogFactory
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.quartz.JobKey
import org.quartz.TriggerKey
import org.quartz.impl.matchers.GroupMatcher
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

class CommunicationPopulationCompositeServiceConcurrentTests extends CommunicationBaseConcurrentTestCase {
    def log = LogFactory.getLog(this.class)
    def selfServiceBannerAuthenticationProvider
    def quartzScheduler

    @Before
    public void setUp() {
        formContext = ['GUAGMNU','SELFSERVICE']
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('BCMADMIN', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)
        super.setUp()

        for (String groupName : quartzScheduler.getJobGroupNames()) {
            for (JobKey jobKey : quartzScheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                quartzScheduler.unscheduleJob( TriggerKey.triggerKey( jobKey.getName(), jobKey.getGroup() ) )
                quartzScheduler.deleteJob( jobKey )
            }
        }

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
    public void testAllSpridenPopulationFromQuery() {
        CommunicationPopulationQuery populationQuery = new CommunicationPopulationQuery(
                folder: defaultFolder,
                name: "testAllSpridenPopulationFromQuery",
                description: "test description",
                queryString: "select SPRIDEN_PIDM from SPRIDEN"
        )

        populationQuery = communicationPopulationQueryCompositeService.createPopulationQuery( populationQuery )
        CommunicationPopulationQueryVersion queryVersion = communicationPopulationQueryCompositeService.publishPopulationQuery( populationQuery )
        assertFalse( populationQuery.changesPending )

        CommunicationPopulation population = communicationPopulationCompositeService.createPopulationFromQuery( populationQuery, "testAllSpridenPopulationFromQuery" )
        assertNotNull( population.id )
        assertEquals( "testAllSpridenPopulationFromQuery", population.name )
        assertEquals( "", population.description )

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
        assertTrueWithRetry( isAvailable, populationCalculation.id, 30, 10 )
        assertTrue( populationCalculation.calculatedCount >= 1000 )
    }

    @Test
    public void testCreatePopulationSelectionExtractQuery() {
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

    @Test void testPopulationWithOnlyIncludeList() {
        CommunicationPopulation population = communicationPopulationCompositeService.createPopulation( defaultFolder, "testPopulation", "testPopulation description" )
        List<String> persons = ['BCMADMIN', 'BCMUSER', 'BCMAUTHOR']

        CommunicationPopulationSelectionListBulkResults results = communicationPopulationCompositeService.addPersonsToIncludeList( population, persons )
        assertNotNull( results.population.includeList )
        def entryCount = CommunicationPopulationSelectionListEntry.countByPopulationSelectionList( population.includeList )
        assertEquals( 3, entryCount )
        assertEquals( 3, results.entryResults.size() )

        persons = [ 'CMOORE', '710000051' ]
        results = communicationPopulationCompositeService.addPersonsToIncludeList( population, persons )
        entryCount = CommunicationPopulationSelectionListEntry.countByPopulationSelectionList( results.population.includeList )
        assertEquals( 4, entryCount )
        assertEquals( 2, results.entryResults.size() )
        assertEquals( CommunicationErrorCode.BANNER_ID_NOT_FOUND, results.entryResults.get(0).errorCode )
        assertNull( results.entryResults.get(1).errorCode )

        assertEquals( 0, CommunicationPopulationQueryAssociation.countByPopulation( population ) )

        CommunicationPopulationSelectionList copy = communicationPopulationCompositeService.cloneSelectionList( population.includeList )
        assertNotNull( copy.id )

        assertNull( communicationPopulationCompositeService.calculatePopulationForUser( population ) )
    }

    @Test void testPopulationWithQueryAndIncludeList() {
        CommunicationPopulationQuery populationQuery = new CommunicationPopulationQuery(
                folder: defaultFolder,
                name: "testQuery",
                description: "test description",
                queryString: "select SPRIDEN_PIDM from SPRIDEN where SPRIDEN_CHANGE_IND is not null and rownum < 6"
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
