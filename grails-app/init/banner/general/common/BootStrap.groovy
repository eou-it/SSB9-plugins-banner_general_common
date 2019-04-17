package banner.general.common
/*******************************************************************************
Copyright 2014-2017 Ellucian Company L.P. and its affiliates.
*******************************************************************************/


import grails.util.Holders
import groovy.util.logging.Slf4j
import net.hedtech.banner.converters.json.JSONBeanMarshaller
import net.hedtech.banner.converters.json.JSONDomainMarshaller
import net.hedtech.banner.general.communication.CommunicationEnum
import grails.converters.JSON
import grails.util.Environment
import org.apache.commons.logging.LogFactory
import org.grails.plugins.web.taglib.ValidationTagLib
import org.grails.web.converters.configuration.ConvertersConfigurationHolder
import org.springframework.context.i18n.LocaleContextHolder
import org.grails.web.converters.configuration.DefaultConverterConfiguration

import java.sql.Timestamp
import java.text.SimpleDateFormat

/**
 * Executes arbitrary code at bootstrap time.
 * Code executed includes:
 * -- Configuring the dataSource to ensure connections are tested prior to use
 * */
@Slf4j
class BootStrap {

    def localizer = { mapToLocalize ->
        new ValidationTagLib().message(mapToLocalize)
    }

    def grailsApplication
    def resourceService
    def scheduler
    def dateConverterService
    def communicationDateConverterService
    def communicationGroupSendMonitor
    def communicationGroupSendItemProcessingEngine
    def communicationJobProcessingEngine

    def init = { servletContext ->
        log.info( "Executing Bootstrap.init" )
//        def ctx = servletContext.getAttribute(ApplicationAttributes.APPLICATION_CONTEXT)

        if( Holders.config.communication?.communicationGroupSendMonitor?.enabled && (Environment.current != Environment.TEST) ) {
            communicationGroupSendMonitor.startMonitoring()
        }

        if( communicationGroupSendItemProcessingEngine?.enabled && (Environment.current != Environment.TEST) ) {
            communicationGroupSendItemProcessingEngine.startRunning()
        }

        if( communicationJobProcessingEngine?.enabled && (Environment.current != Environment.TEST) ) {
            communicationJobProcessingEngine.startRunning()
        }

        /**
         * Using dataSource to set properties is not allowed after grails 1.3. dataSourceUnproxied should be used instead
         * Disabling it for now to avoid compatibility issue.
         */
        // Configure the dataSource to ensure connections are tested prior to use
        /*        ctx.dataSourceUnproxied.with {
            setMinEvictableIdleTimeMillis( 1000 * 60 * 30 )
            setTimeBetweenEvictionRunsMillis( 1000 * 60 * 30 )
            setNumTestsPerEvictionRun( 3 )
            setTestOnBorrow( true )
            setTestWhileIdle( false )
            setTestOnReturn( false )
            setValidationQuery( "select 1 from dual" )
        }*/

        if (Environment.current != Environment.TEST) {
            // println("Reading format from ${servletContext.getRealPath("/xml/application.navigation.conf.xml" )}")
            // NavigationConfigReader.readConfigFile( servletContext.getRealPath("/xml/application.navigation.conf.xml" ) )
        }


        grailsApplication.controllerClasses.each {
            log.info "adding log property to controller: $it"
            // Note: weblogic throws an error if we try to inject the method if it is already present
            if (!it.metaClass.methods.find { m -> m.name.matches("getLog") }) {
                def name = it.name // needed as this 'it' is not visible within the below closure...
                try {
                    it.metaClass.getLog = { LogFactory.getLog("$name") }
                }
                catch (e) { } // rare case where we'll bury it...
            }
        }

        grailsApplication.allClasses.each {
            if (it.name?.contains("plugin.resource")) {
                log.info "adding log property to plugin.resource: $it"

                // Note: weblogic throws an error if we try to inject the method if it is already present
                if (!it.metaClass.methods.find { m -> m.name.matches("getLog") }) {
                    def name = it.name // needed as this 'it' is not visible within the below closure...
                    try {
                        it.metaClass.getLog = { LogFactory.getLog("$name") }
                    }
                    catch (e) { } // rare case where we'll bury it...
                }
            }
        }

        // Register the JSON Marshallers for format conversion and XSS protection
        registerJSONMarshallers()

        //TODO - Commenting out to run integration tests
//        resourceService.reloadAll()

//        if (grailsApplication?.config?.asynchronousActionScheduler?.disabled) {
//            if (log.infoEnabled) log.info( "Background scheduler configured to disabled." )
//        } else {
//            if (log.infoEnabled) log.info( "Requesting to start background scheduler." )
//            scheduler.start()
//        }

    }


    def destroy = {
        log.info( "Executing Bootstrap.destroy" )

        if( Holders.config.communication?.communicationGroupSendMonitor?.enabled && (Environment.current != Environment.TEST) ) {
            communicationGroupSendMonitor.shutdown()
        }

        if( communicationGroupSendItemProcessingEngine?.enabled && (Environment.current != Environment.TEST) ) {
            communicationGroupSendItemProcessingEngine.stopRunning()
        }

        if( communicationJobProcessingEngine?.enabled && (Environment.current != Environment.TEST) ) {
            communicationJobProcessingEngine.stopRunning()
        }
    }

    private String timestampToString( Date timestamp ) {
        SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss.sssZ");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String isoFormattedUtcDate = sdf.format( timestamp )
        return isoFormattedUtcDate
    }

    private def registerJSONMarshallers() {
        DefaultConverterConfiguration defaultConverterConfiguration = (DefaultConverterConfiguration) ConvertersConfigurationHolder.getNamedConverterConfiguration ("deep", JSON.class);

        Closure dateMarshaller = { Date date ->
            //      dateConverterService.parseGregorianToDefaultCalendar(LocalizeUtil.formatDate(it))
            def value
            if (date instanceof Timestamp && (!LocaleContextHolder?.getLocale()?.toString()?.startsWith("ar"))) {
                value = timestampToString( (Timestamp) date )
            } else {
                value = communicationDateConverterService.parseGregorianToDefaultCalendar( date, "default.cm.datetime.format")
            }

            return value
        }
        JSON.registerObjectMarshaller(Date, dateMarshaller)
        defaultConverterConfiguration.registerObjectMarshaller(Date, dateMarshaller);

        Closure communicationEnumMarshaller = { CommunicationEnum commEnum ->
            return commEnum.name()
        }
        JSON.registerObjectMarshaller(CommunicationEnum, communicationEnumMarshaller)
        defaultConverterConfiguration.registerObjectMarshaller(CommunicationEnum, communicationEnumMarshaller)


//        Closure populationQueryTypeMarshaller = { CommunicationPopulationQueryType queryType ->
//            queryType.toString()
//        }
//        JSON.registerObjectMarshaller(CommunicationPopulationQueryType, populationQueryTypeMarshaller)
//        defaultConverterConfiguration.registerObjectMarshaller(Date, dateMarshaller);

        def localizeMap = [
        ]

        JSON.registerObjectMarshaller(new JSONBeanMarshaller( localizeMap ), 1) // for decorators and maps
        JSON.registerObjectMarshaller(new JSONDomainMarshaller( localizeMap, true), 2) // for domain objects


    }
}
