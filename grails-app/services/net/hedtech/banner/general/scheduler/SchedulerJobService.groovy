/********************************************************************************
  Copyright 2016-2018 Ellucian Company L.P. and its affiliates.
********************************************************************************/
package net.hedtech.banner.general.scheduler

import grails.util.Holders
import net.hedtech.banner.general.communication.CommunicationErrorCode
import net.hedtech.banner.general.communication.exceptions.CommunicationExceptionFactory
import net.hedtech.banner.general.scheduler.quartz.BannerServiceMethodJob
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.quartz.CronScheduleBuilder
import org.quartz.CronTrigger
import org.quartz.JobDetail
import org.quartz.JobKey
import org.quartz.SchedulerException
import org.quartz.SimpleTrigger
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import org.quartz.TriggerKey
import org.springframework.transaction.annotation.Propagation

import static org.quartz.DateBuilder.evenMinuteDate
import static org.quartz.DateBuilder.evenSecondDate;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

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
    public SchedulerJobReceipt scheduleServiceMethod( SchedulerJobContext jobContext ) {
        JobDetail jobDetail = createJobDetail( jobContext )

        TriggerBuilder builder = newTrigger().withIdentity( jobContext.jobId, jobContext.groupId ).
                withSchedule(simpleSchedule().withRepeatCount(0).withMisfireHandlingInstructionFireNow())
        if(jobContext.scheduledStartDate != null) {
            builder.startAt(evenMinuteDate(jobContext.scheduledStartDate))
        }
        SimpleTrigger trigger = builder.build()
        scheduleJob( jobDetail, trigger )

        return new SchedulerJobReceipt( groupId: jobContext.groupId, jobId: jobContext.jobId )
    }

    /**
     * Schedules calling a recurring service method using the quartz scheduler CRON schedule.
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
    public SchedulerJobReceipt scheduleCronServiceMethod( SchedulerJobContext jobContext ) {
        JobDetail jobDetail = createJobDetail( jobContext )

        try {
            TriggerBuilder builder = newTrigger().withIdentity(jobContext.jobId, jobContext.groupId)
                    .startAt(jobContext.scheduledStartDate)
                    .withSchedule(CronScheduleBuilder.cronSchedule(jobContext.cronSchedule)
                        .inTimeZone(TimeZone.getTimeZone(jobContext.cronScheduleTimezone))
                        .withMisfireHandlingInstructionFireAndProceed())

            if (jobContext.endDate != null) {
                builder.endTime = jobContext.endDate
            }
            CronTrigger trigger = builder.build()

            scheduleJob(jobDetail, trigger)
        } catch(SchedulerException e) {
            log.error(e)
            throw CommunicationExceptionFactory.createApplicationException(SchedulerJobService.class, e, CommunicationErrorCode.SCHEDULER_ERROR.name())
        } catch(Throwable t) {
            log.error(t)
            throw CommunicationExceptionFactory.createApplicationException(SchedulerJobService.class, t, CommunicationErrorCode.UNKNOWN_ERROR.name())
        }
        return new SchedulerJobReceipt( groupId: jobContext.groupId, jobId: jobContext.jobId )
    }

    /**
     * Schedules calling a service method using the quartz scheduler to run immediately.
     * The service method invoked should take a single map as a parameter.
     *
     * @param jobContext a schedule job context with the information needed to set the schedule and parameters for invoking the scheduled callback method
     * @return a scheduler job receipt with the jobId and the a groupId consisting of the service and method named concatenated together
     */
    public SchedulerJobReceipt scheduleNowServiceMethod( SchedulerJobContext jobContext ) {
        JobDetail jobDetail = createJobDetail( jobContext )

        SimpleTrigger trigger = (SimpleTrigger) newTrigger().withIdentity( jobContext.jobId, jobContext.groupId ).
            withSchedule(simpleSchedule().withRepeatCount(0).withMisfireHandlingInstructionFireNow()).
            startNow().build()
        scheduleJob( jobDetail, trigger )

        return new SchedulerJobReceipt( groupId: jobContext.groupId, jobId: jobContext.jobId )
    }


    @Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor = Throwable.class )
    public Object invokeServiceMethodInNewTransaction( String serviceName, String method, context ) {
        def serviceReference = Holders.applicationContext.getBean( serviceName )
        return serviceReference.invokeMethod( method, context )
    }

    public boolean deleteScheduledJob(String jobId, String service, String method) {
        String groupId = service + "." + method
        deleteScheduledJob( jobId, groupId )
    }

    public boolean deleteScheduledJob(String jobId, String groupId) {
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
    public void testOperationFired( SchedulerJobContext jobContext ) {
        log.info( "testOperationFired at " + new Date() )
        SchedulerTestOperation.markOperationFired()

        if (jobContext.getParameter( "simulateError" )) {
            throw new RuntimeException( "Simulated Error" )
        } else {
            SchedulerTestOperation.markOperationCompleted()
        }
    }

    /**
     * Simple method that logs the current server date time provided for test purposes.
     */
    public void testOperationFailed( SchedulerErrorContext errorContext ) {
        log.info( "testOperationFailed at " + new Date() )
        SchedulerTestOperation.markOperationFailed()
    }


    private void assignParameters(JobDetail jobDetail, Map parameters) {
        if (parameters) {
            for (Object key : parameters.keySet()) {
                String parameterKey = key as String
                jobDetail.getJobDataMap().put(parameterKey, parameters.get(parameterKey))
            }
        }
    }


    private Date scheduleJob(JobDetail jobDetail, Trigger trigger) {
        try {
            return quartzScheduler.scheduleJob(jobDetail, trigger)
        } catch (NoClassDefFoundError e) {
            log.error( e )
            if ("weblogic/jdbc/vendor/oracle/OracleThinBlob".equals( e.message ) && isConfiguredForWeblogic()) {
                throw CommunicationExceptionFactory.createApplicationException( SchedulerJobService.class, "weblogicOracleDriverNotFound" )
            } else {
                throw e
            }
        } catch(SchedulerException e) {
            log.error(e)
            throw CommunicationExceptionFactory.createApplicationException(SchedulerJobService.class, e, CommunicationErrorCode.SCHEDULER_ERROR.name())
        }
    }

    public boolean unScheduleJob(String jobId, String service, String method) {
        String groupId = service + "." + method
        unScheduleJob( jobId, groupId )
    }

    public boolean unScheduleJob(String jobId, String groupId) {
        TriggerKey triggerKey = new TriggerKey(jobId, groupId)
        boolean result
        try {
            result = quartzScheduler.unscheduleJob(triggerKey)
        } catch(Throwable t) {
            log.error(t.getMessage())
        }
    }

    private JobDetail createJobDetail( SchedulerJobContext jobContext ) {
        JobDetail jobDetail = newJob(BannerServiceMethodJob.class).withIdentity(jobContext.jobId, jobContext.groupId).requestRecovery().build();
        jobDetail.getJobDataMap().put("jobId", jobContext.jobId)
        jobDetail.getJobDataMap().put("groupId", jobContext.groupId)
        jobDetail.getJobDataMap().put("bannerUser", jobContext.bannerUser)
        jobDetail.getJobDataMap().put("mepCode", jobContext.mepCode)
        jobDetail.getJobDataMap().put("service", jobContext.jobHandle.service)
        jobDetail.getJobDataMap().put("method", jobContext.jobHandle.method)
        if (jobContext.errorHandle) {
            jobDetail.getJobDataMap().put("errorService", jobContext.errorHandle.service)
            jobDetail.getJobDataMap().put("errorMethod", jobContext.errorHandle.method)
        }
        jobDetail.getJobDataMap().put( "scheduledStartDate", jobContext.scheduledStartDate)
        jobDetail.getJobDataMap().put( "cronSchedule", jobContext.cronSchedule)
        jobDetail.getJobDataMap().put( "endDate", jobContext.endDate)
        assignParameters( jobDetail, jobContext.parameters )
        return jobDetail
    }

    private boolean isConfiguredForWeblogic() {
        return grails.util.Holders.getConfig()?.communication?.weblogicDeployment == true
    }
}
