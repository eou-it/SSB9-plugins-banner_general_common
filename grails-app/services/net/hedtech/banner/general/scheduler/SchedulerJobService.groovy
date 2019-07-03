/********************************************************************************
  Copyright 2016-2019 Ellucian Company L.P. and its affiliates.
********************************************************************************/
package net.hedtech.banner.general.scheduler

import grails.util.Holders
import groovy.util.logging.Slf4j
import net.hedtech.banner.general.communication.CommunicationErrorCode
import net.hedtech.banner.general.communication.exceptions.CommunicationApplicationException
import net.hedtech.banner.general.communication.exceptions.CommunicationExceptionFactory
import net.hedtech.banner.general.scheduler.quartz.BannerServiceMethodJob
import org.quartz.CronScheduleBuilder
import org.quartz.CronTrigger
import org.quartz.JobDetail
import org.quartz.JobKey
import org.quartz.SchedulerException
import org.quartz.SimpleScheduleBuilder
import org.quartz.SimpleTrigger
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import org.quartz.TriggerKey
import org.quartz.TriggerUtils
import org.quartz.impl.calendar.BaseCalendar
import org.quartz.spi.OperableTrigger
import org.springframework.transaction.annotation.Propagation

import static org.quartz.DateBuilder.evenMinuteDate
import static org.quartz.DateBuilder.evenMinuteDateAfterNow
import static org.quartz.JobBuilder.newJob;

import org.quartz.impl.StdScheduler
import grails.gorm.transactions.Transactional

/**
 * Service for scheduling a quartz job that runs in the background. This service relies on
 * the quartzScheduler bean being configured in the host grails app.
 */
@Slf4j
//@Transactional
class SchedulerJobService {
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

        TriggerBuilder builder = TriggerBuilder.newTrigger().withIdentity( jobContext.jobId, jobContext.groupId ).
                withSchedule(SimpleScheduleBuilder.simpleSchedule().withRepeatCount(0).withMisfireHandlingInstructionFireNow())
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
            TriggerBuilder builder = TriggerBuilder.newTrigger().withIdentity(jobContext.jobId, jobContext.groupId)
                    .startAt(jobContext.scheduledStartDate)
                    .withSchedule(CronScheduleBuilder.cronSchedule(jobContext.cronSchedule)
                        .inTimeZone(TimeZone.getTimeZone(jobContext.cronScheduleTimezone))
                        .withMisfireHandlingInstructionFireAndProceed())

            CronTrigger trigger = builder.build()

            Date endDate
            if (jobContext.endDate != null) {
                endDate = jobContext.endDate
            } else if(jobContext.noOfOccurrences) {
                endDate = TriggerUtils.computeEndTimeToAllowParticularNumberOfFirings((OperableTrigger) trigger,
                        new BaseCalendar(Calendar.getInstance().getTimeZone()), jobContext.noOfOccurrences.intValue())
            }

            if(endDate) {
                trigger = trigger.getTriggerBuilder().endAt(endDate).build()
            } else {
                trigger = trigger.getTriggerBuilder().build()
            }
            scheduleJob(jobDetail, trigger)
        } catch(CommunicationApplicationException e) {
            log.error(e.getMessage())
            throw e
        } catch(Throwable t) {
            log.error(t.getMessage())
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

        SimpleTrigger trigger = (SimpleTrigger) TriggerBuilder.newTrigger().withIdentity( jobContext.jobId, jobContext.groupId ).
            withSchedule(SimpleScheduleBuilder.simpleSchedule().withRepeatCount(0).withMisfireHandlingInstructionFireNow()).
            startAt(evenMinuteDateAfterNow()).build()
        scheduleJob( jobDetail, trigger )

        return new SchedulerJobReceipt( groupId: jobContext.groupId, jobId: jobContext.jobId )
    }

    /**
     * Re-Schedules calling a recurring service method using the quartz scheduler CRON schedule. Used when the recurring schedule needs to be updated.
     * The service method invoked should take a single map as a parameter.
     *
     * @param runTime the date to wait until starting the task
     * @param jobId a job identifier; a uuid is one way to go
     * @param bannerUser a banner id to proxy as before invoking the method
     * @param mepCode mep or vpdi code, may be null if the db is not a mep database
     * @param service the name of the service to call
     * @param method the method of the service to invoke
     * @param parameters an optional map to pass to to the service method
     */
    public void reScheduleCronServiceMethod( SchedulerJobContext jobContext ) {
        Trigger oldTrigger = quartzScheduler.getTrigger( new TriggerKey(jobContext.jobId, jobContext.groupId));
        TriggerBuilder builder = oldTrigger.getTriggerBuilder();

        try {
            builder = builder.startAt(jobContext.scheduledStartDate)
                    .withSchedule(CronScheduleBuilder.cronSchedule(jobContext.cronSchedule)
                    .inTimeZone(TimeZone.getTimeZone(jobContext.cronScheduleTimezone))
                    .withMisfireHandlingInstructionFireAndProceed())

            CronTrigger newTrigger = builder.build()

            Date endDate
            if (jobContext.endDate != null) {
                endDate = jobContext.endDate
            } else if(jobContext.noOfOccurrences) {
                endDate = TriggerUtils.computeEndTimeToAllowParticularNumberOfFirings((OperableTrigger) newTrigger,
                        new BaseCalendar(Calendar.getInstance().getTimeZone()), jobContext.noOfOccurrences.intValue())
            }

            if(endDate) {
                newTrigger = newTrigger.getTriggerBuilder().endAt(endDate).build()
            } else {
                newTrigger = newTrigger.getTriggerBuilder().build()
            }
            rescheduleJob(oldTrigger.getKey(), newTrigger)
        } catch(CommunicationApplicationException e) {
            log.error(e.getMessage())
            throw e
        } catch(Throwable t) {
            log.error(t.getMessage())
            throw CommunicationExceptionFactory.createApplicationException(SchedulerJobService.class, t, CommunicationErrorCode.UNKNOWN_ERROR.name())
        }
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
            log.error( e.getMessage() )
            if ("weblogic/jdbc/vendor/oracle/OracleThinBlob".equals( e.message ) && isConfiguredForWeblogic()) {
                throw CommunicationExceptionFactory.createApplicationException( SchedulerJobService.class, "weblogicOracleDriverNotFound" )
            } else {
                throw e
            }
        } catch(SchedulerException e) {
            log.error(e.getMessage())
            throw CommunicationExceptionFactory.createApplicationException(SchedulerJobService.class, e, CommunicationErrorCode.SCHEDULER_ERROR.name())
        }
    }

    private Date rescheduleJob(TriggerKey triggerKey, Trigger newTrigger) {
        try {
            return quartzScheduler.rescheduleJob(triggerKey, newTrigger)
        } catch (NoClassDefFoundError e) {
            log.error( e.getMessage() )
            if ("weblogic/jdbc/vendor/oracle/OracleThinBlob".equals( e.message ) && isConfiguredForWeblogic()) {
                throw CommunicationExceptionFactory.createApplicationException( SchedulerJobService.class, "weblogicOracleDriverNotFound" )
            } else {
                throw e
            }
        } catch(SchedulerException e) {
            log.error(e.getMessage())
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
        jobDetail.getJobDataMap().put( "noOfOccurrences", jobContext.noOfOccurrences)
        assignParameters( jobDetail, jobContext.parameters )
        return jobDetail
    }

    private boolean isConfiguredForWeblogic() {
        return grails.util.Holders.getConfig()?.communication?.weblogicDeployment == true
    }
}
