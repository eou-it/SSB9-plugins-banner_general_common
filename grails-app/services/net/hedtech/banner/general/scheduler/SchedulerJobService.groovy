package net.hedtech.banner.general.scheduler

import net.hedtech.banner.general.scheduler.quartz.BannerServiceMethodJob
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.quartz.JobDetail

import static org.quartz.DateBuilder.evenMinuteDate;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import org.quartz.Trigger
import org.quartz.impl.StdScheduler

/**
 * Service for scheduling a quartz job that runs in the background. This service relies on
 * the quartzScheduler bean being configured in the host grails app.
 */
class SchedulerJobService {
    private Log log = LogFactory.getLog( this.getClass() )
    static transactional = false
    StdScheduler quartzScheduler

    /**
     * Schedules calling a service method using the quartz scheduler.
     *
     * @param groupId a group name (e.g., communication)
     * @param jobId a job identifier; a uuid is one way to go
     * @param runTime the date to wait until starting the task
     * @param bannerUser a banner id to proxy as before invoking the method
     * @param service the name of the service to call
     * @param method the method of the service to invoke
     * @param args an optional map to pass to to the service method
     */
    public void scheduleServiceMethod( String groupId, String jobId, Date runTime, String bannerUser, String service, String method, String[] args ) {
        JobDetail jobDetail = newJob(BannerServiceMethodJob.class).withIdentity( jobId, groupId ).build();
        jobDetail.getJobDataMap().put( "bannerUser", bannerUser )
        jobDetail.getJobDataMap().put( "service", service )
        jobDetail.getJobDataMap().put( "method", method )
        if (args) {
            for( int i; i<args.length; i++) {
                jobDetail.getJobDataMap().put( "args{i}", args[i] )
            }
        }
        Trigger trigger = newTrigger().withIdentity( jobId, groupId ).startAt( evenMinuteDate( runTime ) ).build()
        quartzScheduler.scheduleJob( jobDetail, trigger )
    }


    /**
     * Simple method that logs the current server date time provided for test purposes.
     */
    public void logDateTime() {
        log.info( "logDateTime at " + new Date() )
    }


}
