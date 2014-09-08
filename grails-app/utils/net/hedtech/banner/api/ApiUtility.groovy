/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ***************************************************************************** */
package net.hedtech.banner.api

import groovy.sql.Sql
import net.hedtech.banner.service.ServiceBase
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes

class ApiUtility {

    public static boolean isDomainPropertyDirty( def domainClass, def domainObj, String property ) {
        return (property in getDirtyProperties( domainClass, domainObj ))
    }


    public static List getDirtyProperties( def domainClass, def domainObj ) {
        def content = ServiceBase.extractParams( domainClass, domainObj )
        def domainObject = domainClass?.get( content?.id )
        domainObject.properties = content

        return domainObject?.dirtyPropertyNames
    }


    public static void commit() {
        // ServletContextHolder.servletContext.getAttribute( GrailsApplicationAttributes.APPLICATION_CONTEXT ).sessionFactory.currentSession.getTransaction().commit()
        // ServletContextHolder.servletContext.getAttribute( GrailsApplicationAttributes.APPLICATION_CONTEXT ).sessionFactory.currentSession.flush()
        def sql = new Sql( ServletContextHolder.servletContext.getAttribute( GrailsApplicationAttributes.APPLICATION_CONTEXT ).sessionFactory.getCurrentSession().connection() )
        try {
            sql.execute "{ call gb_common.p_commit() }"
        } finally {
            sql.close()
        }
    }

}
