package net.hedtech.banner.general.communication.population

import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.CommunicationBaseConcurrentTestCase
import net.hedtech.banner.general.communication.groupsend.*
import net.hedtech.banner.general.communication.job.CommunicationJob
import net.hedtech.banner.general.communication.merge.CommunicationRecipientData
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQuery
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQueryVersion
import net.hedtech.banner.general.communication.population.selectionlist.CommunicationPopulationSelectionList
import org.apache.commons.logging.LogFactory
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.quartz.JobKey
import org.quartz.TriggerKey
import org.quartz.impl.matchers.GroupMatcher
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

import javax.mail.internet.MimeMessage
import java.util.concurrent.TimeUnit

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


    @Test
    public void testCreatePopulationFromQuery() {
        CommunicationPopulationQuery populationQuery = communicationPopulationQueryCompositeService.createPopulationQuery( newPopulationQuery( "testQuery" ) )
        CommunicationPopulationQueryVersion queryVersion = communicationPopulationQueryCompositeService.publishPopulationQuery( populationQuery )
        assertFalse( populationQuery.changesPending )

        CommunicationPopulation communicationPopulation = communicationPopulationCompositeService.createPopulationFromQuery( populationQuery, "testPopulation", "testPopulationDescription" )
        assertNotNull( communicationPopulation.id )
        assertEquals( "testPopulation", communicationPopulation.name )
        assertEquals( "testPopulationDescription", communicationPopulation.description )

        List associations = CommunicationPopulationQueryAssociation.findAllByPopulation( communicationPopulation )
        assertEquals( 1, associations.size() )
        CommunicationPopulationQueryAssociation association = associations.get( 0 ) as CommunicationPopulationQueryAssociation
        assertNotNull( association.id )
        assertEquals( communicationPopulation.id, association.population.id )
        assertEquals( populationQuery.id, association.populationQuery.id )
        assertNull( association.populationQueryVersion )

        CommunicationPopulationVersion populationVersion = CommunicationPopulationVersion.findLatestByPopulationIdAndCreatedBy( communicationPopulation.id, 'BCMADMIN' )
        assertNotNull( populationVersion )
        assertEquals( CommunicationPopulationCalculationStatus.PENDING_EXECUTION, populationVersion.status )

        def isAvailable = {
            def aPopulationVersion = CommunicationPopulationVersion.get( it )
            aPopulationVersion.refresh()
            return aPopulationVersion.status == CommunicationPopulationCalculationStatus.AVAILABLE ||
                    aPopulationVersion.status == CommunicationPopulationCalculationStatus.ERROR;
        }
        assertTrueWithRetry( isAvailable, populationVersion.id, 30, 10 )
    }


}
