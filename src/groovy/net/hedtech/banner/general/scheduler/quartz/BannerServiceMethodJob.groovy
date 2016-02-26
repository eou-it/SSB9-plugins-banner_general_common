package net.hedtech.banner.general.scheduler.quartz

import grails.util.Holders
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
        println "BannerServiceMethodJob.BannerServiceMethodJob"
    }


    public void execute(JobExecutionContext context) throws JobExecutionException {
        println "BannerServiceMethodJob.execute"
        try{
            String service = context.getJobDetail().getJobDataMap().getString( "service" )
            String method = context.getJobDetail().getJobDataMap().getString( "method" )
            def arguments = context.getJobDetail().getJobDataMap().get( "arguments" )
            def serviceReference = Holders.applicationContext.getBean(service)
            context.result = serviceReference.invokeMethod( method, makeArguments())
        }catch(JobExecutionException e){
            throw e
        }
        catch(e){
            throw new JobExecutionException(e)
        }
    }


    private Object[] makeArguments( def arguments ){
        if(!arguments) return new Object[0]
        if(arguments.class.isArray()) return arguments
        if(arguments instanceof Collection) return arguments.toArray()
        //its a single argument then
        def o = new Object[1]
        o[0]=arguments
        return o
    }

}
