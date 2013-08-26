/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ***************************************************************************** */
package net.hedtech.banner.general.utility

import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes
import org.springframework.context.ApplicationContext

/**
 * Object to define the elements in the Persona list that appears in the
 * persona lookup which is fetched from the webtailor roles table.
 */
class InformationTextPersonaList {
    InformationTextPersona informationTextPersona

    def static informationTextPersonaListService


    public static ApplicationContext getApplicationContext( ) {
        return (ApplicationContext) ServletContextHolder.getServletContext().getAttribute( GrailsApplicationAttributes.APPLICATION_CONTEXT );
    }


    public String toString( ) {
        """InformationTextPersonaList[informationTextPersona=$informationTextPersona]"""
    }


    public static def fetchInformationTextPersonas( ) {
        def filter = "%"
        if ( informationTextPersonaListService == null ) {
            informationTextPersonaListService = getApplicationContext().getBean( "informationTextPersonaListService" )
        }
        def returnList = informationTextPersonaListService.fetchInformationTextPersonas( filter )
        def returnObj = [list: returnList]
        return returnObj
    }


    public static def fetchInformationTextPersonas( filter ) {
        if ( informationTextPersonaListService == null ) {
            informationTextPersonaListService = getApplicationContext().getBean( "informationTextPersonaListService" )
        }
        def returnList = informationTextPersonaListService.fetchInformationTextPersonas( filter )
        def returnObj = [list: returnList]
        return returnObj
    }


    public static InformationTextPersona fetchValidInformationTextPersona( String filter ) {
        if ( informationTextPersonaListService == null ) {
            informationTextPersonaListService = getApplicationContext().getBean( "informationTextPersonaListService" )
        }
        def returnObj = informationTextPersonaListService.fetchInformationTextPersonas( filter )[0]
        return returnObj?.code ? returnObj : null
    }
}
