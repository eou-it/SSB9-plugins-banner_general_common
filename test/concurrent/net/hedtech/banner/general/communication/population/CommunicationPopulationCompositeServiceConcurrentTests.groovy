/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import net.hedtech.banner.general.communication.CommunicationBaseConcurrentTestCase
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQuery
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQueryVersion
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
        formContext = ['SELFSERVICE']
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


//    @Test
//    public void testCreatePopulationFromQuery() {
//        CommunicationPopulationQuery populationQuery = communicationPopulationQueryCompositeService.createPopulationQuery( newPopulationQuery( "testQuery" ) )
//        CommunicationPopulationQueryVersion queryVersion = communicationPopulationQueryCompositeService.publishPopulationQuery( populationQuery )
//        assertFalse( populationQuery.changesPending )
//
//        CommunicationPopulation population = communicationPopulationCompositeService.createPopulationFromQuery( populationQuery, "testPopulation", "testPopulationDescription" )
//        assertNotNull( population.id )
//        assertEquals( "testPopulation", population.name )
//        assertEquals( "testPopulationDescription", population.description )
//
//        List associations = CommunicationPopulationQueryAssociation.findAllByPopulation( population )
//        assertEquals( 1, associations.size() )
//        CommunicationPopulationQueryAssociation association = associations.get( 0 ) as CommunicationPopulationQueryAssociation
//        assertNotNull( association.id )
//        assertEquals( population.id, association.population.id )
//        assertEquals( populationQuery.id, association.populationQuery.id )
//        assertNull( association.populationQueryVersion )
//
//        CommunicationPopulationCalculation populationCalculation = CommunicationPopulationCalculation.findLatestByPopulationIdAndCalculatedBy( population.id, 'BCMADMIN' )
//        assertEquals( populationCalculation.status, CommunicationPopulationCalculationStatus.PENDING_EXECUTION )
//        def isAvailable = {
//            def theCalculation = CommunicationPopulationCalculation.get( it )
//            theCalculation.refresh()
//            return theCalculation.status == CommunicationPopulationCalculationStatus.AVAILABLE
//        }
//        assertTrueWithRetry( isAvailable, populationCalculation.id, 30, 10 )
//    }


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
        assertTrue( populationCalculation.calculatedCount >= 10000 )
    }

//    @Test
//    public void testCreatePopulationSelectionExtractQuery() {
//        CommunicationPopulationQueryExtractStatement extractStatement = new CommunicationPopulationQueryExtractStatement()
//        extractStatement.application = 'ADMISSIONS'
//        extractStatement.selection = '199610_APPLICANTS'
//        extractStatement.creatorId = 'SAISUSR'
//        extractStatement.userId = 'SAISUSR'
//        String extractQueryString = extractStatement.getQueryString()
//
//        CommunicationPopulationQuery populationQuery = new CommunicationPopulationQuery(
//            folder: defaultFolder,
//            name: "testCreatePopulationSelectionExtractQuery",
//            description: "test description",
//            type: CommunicationPopulationQueryType.POPULATION_SELECTION_EXTRACT,
//            queryString: extractQueryString
//        )
//
//        populationQuery = communicationPopulationQueryCompositeService.createPopulationQuery( populationQuery )
//        CommunicationPopulationQueryVersion queryVersion = communicationPopulationQueryCompositeService.publishPopulationQuery( populationQuery )
//        assertFalse( populationQuery.changesPending )
//
//        CommunicationPopulation population = communicationPopulationCompositeService.createPopulationFromQuery( populationQuery, "testPopulation", "testPopulationDescription" )
//        assertNotNull( population.id )
//        assertEquals( "testPopulation", population.name )
//        assertEquals( "testPopulationDescription", population.description )
//
//        List associations = CommunicationPopulationQueryAssociation.findAllByPopulation( population )
//        assertEquals( 1, associations.size() )
//        CommunicationPopulationQueryAssociation association = associations.get( 0 ) as CommunicationPopulationQueryAssociation
//        assertNotNull( association.id )
//        assertEquals( population.id, association.population.id )
//        assertEquals( populationQuery.id, association.populationQuery.id )
//        assertNull( association.populationQueryVersion )
//
//        CommunicationPopulationCalculation populationCalculation = CommunicationPopulationCalculation.findLatestByPopulationIdAndCalculatedBy( population.id, 'BCMADMIN' )
//        def isAvailable = {
//            def theCalculation = CommunicationPopulationCalculation.get( it )
//            theCalculation.refresh()
//            return theCalculation.status == CommunicationPopulationCalculationStatus.AVAILABLE ||
//                    theCalculation.status == CommunicationPopulationCalculationStatus.ERROR
//        }
//        assertTrueWithRetry( isAvailable, populationCalculation.id, 30, 10 )
//    }

}
