/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.scheduler.quartz

import grails.util.Holders
import net.hedtech.banner.general.scheduler.SchedulerErrorContext
import net.hedtech.banner.general.scheduler.SchedulerJobContext
import net.hedtech.banner.general.scheduler.SchedulerJobService
import net.hedtech.banner.general.scheduler.SchedulerServiceMethodHandle
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.quartz.Job
import org.quartz.JobDetail
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException

import static org.quartz.JobBuilder.newJob

/**
 * A Quartz job which invokes a Grails / Spring service method
 * proxied as a specific Banner User.
 */
class BannerServiceMethodJob implements Job {
    private Log log = LogFactory.getLog( this.getClass() )

    static triggers = {
    }


    public BannerServiceMethodJob() {
    }


    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (log.isDebugEnabled()) {
            log.debug( "Executing BannerServiceMethodJob with context ${context}." )
        }
        SchedulerJobContext jobContext = createSchedulerJobContext( context )
        Long startTime = System.currentTimeMillis()
        try{
            context.result = invokeServiceMethod( jobContext.jobHandle, jobContext, jobContext.bannerUser, jobContext.mepCode )
        } catch(JobExecutionException e){
            log.error( e )
            invokeErrorServiceMethod( jobContext, e, startTime )
            throw e
        } catch(Throwable e){
            log.error( e )
            invokeErrorServiceMethod( jobContext, e, startTime )
            throw new JobExecutionException(e)
        }
    }

    private SchedulerJobContext createSchedulerJobContext( JobExecutionContext executionContext) {
        Map jobDetailMap = executionContext.jobDetail.jobDataMap
        SchedulerJobContext schedulerJobContext = new SchedulerJobContext( jobDetailMap.get( "jobId" ), jobDetailMap.get( "groupId" ) )
        .setBannerUser( jobDetailMap.get( "bannerUser" ) )
        .setMepCode( jobDetailMap.get( "mepCode" ) )
        .setJobHandle( jobDetailMap.get("service"), jobDetailMap.get("method") )
        .setScheduledStartDate( jobDetailMap.get( "scheduledStartDate" ) )
        .setCronSchedule( jobDetailMap.get( "cronSchedule" ))

        if (jobDetailMap.containsKey("errorService")) {
            schedulerJobContext.setErrorHandle( jobDetailMap.get("errorService"), jobDetailMap.get("errorMethod") )
        }

        for (Object key : jobDetailMap.keySet()) {
            String parameterKey = key as String
            schedulerJobContext.setParameter( parameterKey, jobDetailMap.get(parameterKey) )
        }

        return schedulerJobContext
    }

    private Object invokeServiceMethod( SchedulerServiceMethodHandle methodHandle, context, String oracleUserName, String mepCode ) {
        def asynchronousBannerAuthenticationSpoofer = Holders.applicationContext.getBean( "asynchronousBannerAuthenticationSpoofer" )
        def originalMap = null
        try {
            originalMap = asynchronousBannerAuthenticationSpoofer.authenticateAndSetFormContextForExecuteAndSave(oracleUserName, mepCode)
            SchedulerJobService schedulerJobService = Holders.applicationContext.getBean("schedulerJobService")
            schedulerJobService.invokeServiceMethodInNewTransaction(methodHandle.service, methodHandle.method, context)
        } finally {
            if (originalMap) {
                asynchronousBannerAuthenticationSpoofer.resetAuthAndFormContext( originalMap )
            }
        }
    }

    private Object invokeErrorServiceMethod( SchedulerJobContext jobContext, Throwable originalSin, Long startTime ) {
        try {
            SchedulerErrorContext errorContext = new SchedulerErrorContext()
            errorContext.jobContext = jobContext
            errorContext.cause = originalSin
            invokeServiceMethod( jobContext.errorHandle, errorContext, jobContext.bannerUser, jobContext.mepCode )
        } catch (Throwable t) {
            log.error( "Couldn't successfully complete error handler.", t )
        }
    }


}
