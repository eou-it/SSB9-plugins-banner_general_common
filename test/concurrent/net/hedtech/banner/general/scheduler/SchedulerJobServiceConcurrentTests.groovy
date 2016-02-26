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
        FormContext.set( ['SELFSERVICE'] )
        Authentication authentication = selfServiceBannerAuthenticationProvider.authenticate( new UsernamePasswordAuthenticationToken( 'BCMADMIN', '111111' ) )
        SecurityContextHolder.getContext().setAuthentication( authentication )

        for (String groupName : quartzScheduler.getJobGroupNames()) {
            for (JobKey jobKey : quartzScheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                quartzScheduler.unscheduleJob( TriggerKey.triggerKey( jobKey.getName(), jobKey.getGroup() ) )
                quartzScheduler.deleteJob( jobKey )
            }
        }
    }


    @After
    public void tearDown() {
        FormContext.clear()
        SecurityContextHolder.getContext().setAuthentication( null )
    }


    @Test
    public void testScheduleTestJob() {
        String groupName = "testGroup"
        String jobName = "testScheduleTestJob-" + UUID.randomUUID().toString()

        Calendar calendar = Calendar.getInstance() // gets a calendar using the default time zone and locale.
        calendar.add(Calendar.SECOND, 5)
        Date requestedRunTime = calendar.getTime()

        schedulerJobService.scheduleServiceMethod( groupName, jobName, requestedRunTime, 'BCMADMIN', "schedulerJobService", "logDateTime", new String[0] )

        JobKey jobKey = new JobKey( jobName, groupName )

        JobDetail jobDetail = quartzScheduler.getJobDetail( jobKey ) // JobDetailImpl
        assertNotNull( jobDetail )

        TriggerKey triggerKey = new TriggerKey( jobName, groupName )
        Trigger trigger = quartzScheduler.getTrigger( triggerKey ) // SimpleTriggerImpl
        assertNotNull( trigger )

        int retries = 10
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
    }


}
