/********************************************************************************
  Copyright 2016-2019 Ellucian Company L.P. and its affiliates.
********************************************************************************/
package net.hedtech.banner.general.scheduler

import net.hedtech.banner.security.FormContext
import org.apache.commons.logging.LogFactory
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.quartz.JobDetail
import org.quartz.JobKey
import org.quartz.Trigger
import org.quartz.TriggerKey
import org.quartz.impl.StdScheduler
import org.quartz.impl.matchers.GroupMatcher
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder

import java.util.concurrent.TimeUnit

import static org.junit.Assert.assertNotNull

public class SchedulerJobServiceConcurrentTests extends Assert {
    static transactional = false // set to false so that everything "autocommits" i.e. doesn't rollback at the end of the test
    def log = LogFactory.getLog(this.class)
    def selfServiceBannerAuthenticationProvider
    StdScheduler quartzScheduler
    SchedulerJobService schedulerJobService


    @Before
    public void setUp() {
        FormContext.set( ['GUAGMNU','SELFSERVICE'] )
        Authentication authentication = selfServiceBannerAuthenticationProvider.authenticate( new UsernamePasswordAuthenticationToken( 'BCMADMIN', '111111' ) )
        SecurityContextHolder.getContext().setAuthentication( authentication )

        for (String groupName : quartzScheduler.getJobGroupNames()) {
            for (JobKey jobKey : quartzScheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                quartzScheduler.unscheduleJob( TriggerKey.triggerKey( jobKey.getName(), jobKey.getGroup() ) )
                quartzScheduler.deleteJob( jobKey )
            }
        }

        SchedulerTestOperation.reset()
    }


    @After
    public void tearDown() {
        FormContext.clear()
        SecurityContextHolder.getContext().setAuthentication( null )
        SchedulerTestOperation.reset()
    }


    @Test
    public void testScheduleServiceMethod() {
        String jobId = "testScheduleTestJob-" + UUID.randomUUID().toString()

        Calendar calendar = Calendar.getInstance() // gets a calendar using the default time zone and locale.
        calendar.add(Calendar.SECOND, 5)
        Date requestedRunTime = calendar.getTime()
        String mepCode = null

        SchedulerJobContext jobContext = new SchedulerJobContext( jobId )
        .setScheduledStartDate( requestedRunTime )
        .setBannerUser( 'BCMADMIN' )
        .setJobHandle( "schedulerJobService", "testOperationFired" )
        .setMepCode( mepCode )
        SchedulerJobReceipt receipt = schedulerJobService.scheduleServiceMethod( jobContext )

        JobKey jobKey = new JobKey( receipt.jobId, receipt.groupId )

        JobDetail jobDetail = quartzScheduler.getJobDetail( jobKey ) // JobDetailImpl
        assertNotNull( jobDetail )

        TriggerKey triggerKey = new TriggerKey( receipt.jobId, receipt.groupId )
        Trigger trigger = quartzScheduler.getTrigger( triggerKey ) // SimpleTriggerImpl
        assertNotNull( trigger )

        int retries = 20
        while (retries > 0) {
            retries--;
            TimeUnit.SECONDS.sleep( 5 );

            jobDetail = quartzScheduler.getJobDetail( jobKey )

            if (!jobDetail) {
                break;
            }
        }

        assertNull( jobDetail )
        trigger = quartzScheduler.getTrigger( triggerKey )
        assertNull( trigger )

        assertTrue( SchedulerTestOperation.didOperationFire() )
        assertTrue( SchedulerTestOperation.didOperationComplete() )
        assertFalse( SchedulerTestOperation.didOperationFail() )
    }


    @Test
    public void testScheduleNowServiceMethod() {
        String jobId = "testScheduleTestJob-" + UUID.randomUUID().toString()

        String mepCode = null

        SchedulerJobContext jobContext = new SchedulerJobContext( jobId )
                .setBannerUser( 'BCMADMIN' )
                .setJobHandle( "schedulerJobService", "testOperationFired" )
                .setMepCode( mepCode )
        SchedulerJobReceipt receipt = schedulerJobService.scheduleNowServiceMethod( jobContext )

        JobKey jobKey = new JobKey( receipt.jobId, receipt.groupId )

        JobDetail jobDetail = quartzScheduler.getJobDetail( jobKey ) // JobDetailImpl
        assertNotNull( jobDetail )

        TriggerKey triggerKey = new TriggerKey( receipt.jobId, receipt.groupId )
        Trigger trigger = quartzScheduler.getTrigger( triggerKey ) // SimpleTriggerImpl
        assertNotNull( trigger )

        int retries = 15
        while (retries > 0) {
            retries--;
            TimeUnit.SECONDS.sleep( 5 );

            jobDetail = quartzScheduler.getJobDetail( jobKey )

            if (!jobDetail) {
                break;
            }
        }

        assertNull( jobDetail )
        trigger = quartzScheduler.getTrigger( triggerKey )
        assertNull( trigger )

        assertTrue( SchedulerTestOperation.didOperationFire() )
        assertTrue( SchedulerTestOperation.didOperationComplete() )
        assertFalse( SchedulerTestOperation.didOperationFail() )
    }

    @Test
    public void testScheduleNowServiceMethodErrorHandling() {
        String jobId = "testScheduleTestJob-" + UUID.randomUUID().toString()

        String mepCode = null

        SchedulerJobContext jobContext = new SchedulerJobContext( jobId )
                .setBannerUser( 'BCMADMIN' )
                .setJobHandle( "schedulerJobService", "testOperationFired" )
                .setErrorHandle( "schedulerJobService", "testOperationFailed" )
                .setMepCode( mepCode )
                .setParameter( "simulateError", Boolean.TRUE )
        SchedulerJobReceipt receipt = schedulerJobService.scheduleNowServiceMethod( jobContext )

        JobKey jobKey = new JobKey( receipt.jobId, receipt.groupId )

        JobDetail jobDetail = quartzScheduler.getJobDetail( jobKey ) // JobDetailImpl
        assertNotNull( jobDetail )

        TriggerKey triggerKey = new TriggerKey( receipt.jobId, receipt.groupId )
        Trigger trigger = quartzScheduler.getTrigger( triggerKey ) // SimpleTriggerImpl
        assertNotNull( trigger )

        int retries = 15
        while (retries > 0) {
            retries--;
            TimeUnit.SECONDS.sleep( 5 );

            jobDetail = quartzScheduler.getJobDetail( jobKey )

            if (!jobDetail) {
                break;
            }
        }

        assertNull( jobDetail )
        trigger = quartzScheduler.getTrigger( triggerKey )
        assertNull( trigger )

        assertTrue( SchedulerTestOperation.didOperationFire() )
        assertFalse( SchedulerTestOperation.didOperationComplete() )
        assertTrue( SchedulerTestOperation.didOperationFail() )
    }

    @Test
    public void testScheduleCronServiceMethod() {
        String jobId = "testScheduleCronTestJob-" + UUID.randomUUID().toString()

        Calendar calendar = Calendar.getInstance() // gets a calendar using the default time zone and locale.
        Date requestedRunTime = calendar.getTime()
        String mepCode = null

        calendar.add(Calendar.SECOND, 30)
        int month = calendar.get(Calendar.MONTH) + 1;
        //Cron expression set to run 30 seconds from current time
        String cronExpression = "" + calendar.get(Calendar.SECOND) + " " + calendar.get(Calendar.MINUTE) + " " + calendar.get(Calendar.HOUR_OF_DAY) + " " + calendar.get(Calendar.DAY_OF_MONTH) + " " + month + " ? " + calendar.get(Calendar.YEAR);

        SchedulerJobContext jobContext = new SchedulerJobContext( jobId )
                .setScheduledStartDate( requestedRunTime )
                .setCronSchedule(cronExpression)
                .setCronScheduleTimezone(calendar.getTimeZone().getID())
                .setBannerUser( 'BCMADMIN' )
                .setJobHandle( "schedulerJobService", "testOperationFired" )
                .setMepCode( mepCode )
        SchedulerJobReceipt receipt = schedulerJobService.scheduleCronServiceMethod( jobContext )

        JobKey jobKey = new JobKey( receipt.jobId, receipt.groupId )

        JobDetail jobDetail = quartzScheduler.getJobDetail( jobKey ) // JobDetailImpl
        assertNotNull( jobDetail )

        TriggerKey triggerKey = new TriggerKey( receipt.jobId, receipt.groupId )
        Trigger trigger = quartzScheduler.getTrigger( triggerKey ) // SimpleTriggerImpl
        assertNotNull( trigger )

        int retries = 20
        while (retries > 0) {
            retries--;
            TimeUnit.SECONDS.sleep( 5 );

            jobDetail = quartzScheduler.getJobDetail( jobKey )

            if (!jobDetail) {
                break;
            }
        }

        assertNull( jobDetail )
        trigger = quartzScheduler.getTrigger( triggerKey )
        assertNull( trigger )

        assertTrue( SchedulerTestOperation.didOperationFire() )
        assertTrue( SchedulerTestOperation.didOperationComplete() )
        assertFalse( SchedulerTestOperation.didOperationFail() )
    }

}
