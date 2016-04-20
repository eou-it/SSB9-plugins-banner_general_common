package net.hedtech.banner.general.scheduler

import grails.transaction.Transactional
import grails.util.Holders
import net.hedtech.banner.general.communication.population.CommunicationPopulationVersion
import net.hedtech.banner.general.scheduler.quartz.BannerServiceMethodJob
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.quartz.JobDetail
import org.quartz.JobKey
import org.springframework.transaction.annotation.Propagation

import static org.quartz.DateBuilder.evenMinuteDate;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import org.quartz.Trigger
import org.quartz.impl.StdScheduler
import org.springframework.transaction.annotation.Transactional

/**
 * Service for scheduling a quartz job that runs in the background. This service relies on
 * the quartzScheduler bean being configured in the host grails app.
 */
class SchedulerJobService {
    private Log log = LogFactory.getLog( this.getClass() )
    static transactional = false
    StdScheduler quartzScheduler

    /**
     * Schedules calling a service method using the quartz scheduler at a specific time in the future.
     * The service method invoked should take a single map as a parameter.
     *
     * @param runTime the date to wait until starting the task
     * @param jobId a job identifier; a uuid is one way to go
     * @param bannerUser a banner id to proxy as before invoking the method
     * @param mepCode mep or vpdi code, may be null if the db is not a mep database
     * @param service the name of the service to call
     * @param method the method of the service to invoke
     * @param parameters an optional map to pass to to the service method
     * @return a scheduler job receipt with the jobId and the a groupId consisting of the service and method named concatenated together
     */
    public SchedulerJobReceipt scheduleServiceMethod( Date runTime, String jobId, String bannerUser, String mepCode, String service, String method, Map parameters = null ) {
        String groupId = service + "." + method
        JobDetail jobDetail = createJobDetail(jobId, groupId, bannerUser, mepCode, service, method, parameters )

        Trigger trigger = newTrigger().withIdentity( jobId, groupId ).startAt( evenMinuteDate( runTime ) ).build()
        quartzScheduler.scheduleJob( jobDetail, trigger )

        return new SchedulerJobReceipt( groupId: groupId, jobId: jobId )
    }


    /**
     * Schedules calling a service method using the quartz scheduler to run immediately.
     * The service method invoked should take a single map as a parameter.
     *
     * @param jobId a job identifier; a uuid is one way to go
     * @param bannerUser a banner id to proxy as before invoking the method
     * @param mepCode mep or vpdi code, may be null if the db is not a mep database
     * @param service the name of the service to call
     * @param method the method of the service to invoke
     * @param parameters an optional map to pass to to the service method
     * @return a scheduler job receipt with the jobId and the a groupId consisting of the service and method named concatenated together
     */
    public SchedulerJobReceipt scheduleNowServiceMethod( String jobId, String bannerUser, String mepCode, String service, String method, Map parameters = null ) {
        String groupId = service + "." + method
        JobDetail jobDetail = createJobDetail(jobId, groupId, bannerUser, mepCode, service, method, parameters )

        Trigger trigger = newTrigger().withIdentity( jobId, groupId ).startNow().build()
        quartzScheduler.scheduleJob( jobDetail, trigger )

        return new SchedulerJobReceipt( groupId: groupId, jobId: jobId )
    }


    @Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor = Throwable.class )
    public Object invokeServiceMethodInNewTransaction( String serviceName, String method, Map parameters ) {
        def serviceReference = Holders.applicationContext.getBean( serviceName )
        return serviceReference.invokeMethod( method, parameters )
    }

    public boolean deleteScheduledJob(String jobId, String service, String method) {
        String groupId = service + "." + method
        JobKey jobKey = new JobKey(jobId, groupId)
        boolean result
        try {
            result = quartzScheduler.deleteJob(jobKey)
        } catch(Throwable t) {
            log.error(t.getMessage())
        }
    }

    /**
     * Simple method that logs the current server date time provided for test purposes.
     */
    public void logDateTime( Map parameters ) {
        log.info( "logDateTime at " + new Date() )
    }


    private void assignParameters(JobDetail jobDetail, Map parameters) {
        if (parameters) {
            for (Object key : parameters.keySet()) {
                String parameterKey = key as String
                jobDetail.getJobDataMap().put(parameterKey, parameters.get(parameterKey))
            }
        }
    }


    private JobDetail createJobDetail(String jobId, String groupId, String bannerUser, String mepCode, String service, String method, Map parameters) {
        JobDetail jobDetail = newJob(BannerServiceMethodJob.class).withIdentity(jobId, groupId).build();
        jobDetail.getJobDataMap().put("bannerUser", bannerUser)
        jobDetail.getJobDataMap().put("mepCode", mepCode)
        jobDetail.getJobDataMap().put("service", service)
        jobDetail.getJobDataMap().put("method", method)
        assignParameters( jobDetail, parameters )
        return jobDetail
    }
}
