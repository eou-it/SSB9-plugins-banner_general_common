/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.scheduler.quartz

import grails.util.Holders
import net.hedtech.banner.general.scheduler.SchedulerJobService
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException

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
        try{
            String service = context.getJobDetail().getJobDataMap().getString( "service" )
            String method = context.getJobDetail().getJobDataMap().getString( "method" )

            Map parameters = [:]
            for( Object key:context.getJobDetail().getJobDataMap().keySet() ) {
                Object value = context.getJobDetail().getJobDataMap().get( key )
                parameters.put( key, value )
            }

            String bannerUser = context.getJobDetail().getJobDataMap().getString( "bannerUser" )
            String mepCode = context.getJobDetail().getJobDataMap().getString( "mepCode" )
            context.result = invokeServiceMethod( service, method, parameters, bannerUser, mepCode )
        } catch(JobExecutionException e){
            log.error( e )
            throw e
        } catch(e){
            log.error( e )
            throw new JobExecutionException(e)
        }
    }


    private Object invokeServiceMethod( String serviceName, String method, Map parameters, String oracleUserName, String mepCode ) {
        def asynchronousBannerAuthenticationSpoofer = Holders.applicationContext.getBean( "asynchronousBannerAuthenticationSpoofer" )
        def originalMap = null
        try {
            originalMap = asynchronousBannerAuthenticationSpoofer.authenticateAndSetFormContextForExecuteAndSave( oracleUserName, mepCode )
            SchedulerJobService schedulerJobService = Holders.applicationContext.getBean( "schedulerJobService" )
            schedulerJobService.invokeServiceMethodInNewTransaction( serviceName, method, parameters )
        } finally {
            if (originalMap) {
                asynchronousBannerAuthenticationSpoofer.resetAuthAndFormContext( originalMap )
            }
        }
    }


}
