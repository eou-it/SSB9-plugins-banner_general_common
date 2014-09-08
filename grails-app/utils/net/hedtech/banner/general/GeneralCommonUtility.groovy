/*********************************************************************************
 Copyright 2010-2013 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
/** *****************************************************************************
 Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */

package net.hedtech.banner.general

import groovy.sql.Sql
import net.hedtech.banner.general.system.SdaCrosswalkConversion
import net.hedtech.banner.service.ServiceBase
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.codehaus.groovy.grails.web.context.ServletContextHolder as SCH
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes

import java.sql.CallableStatement

/**
 * This is a utilty class for all of Banner
 *
 */
class GeneralCommonUtility {

    public static int INVALID_PIN = 1
    public static int DISABLED_PIN = 2
    public static int EXPIRED_PIN = 3

    /**
     * Store the gtvsdax values used by an App in a list that gets passed and stored in the app session
     * @param internal
     * @param internalGroup
     * @param appGtvsdaxList
     * @return map with the external value and the list
     */
    public static def getAppGtvsdax( def internal, def internalGroup, List appGtvsdaxList = [] ) {
        def gtvsdaxMap

        def gtvsdaxValue = appGtvsdaxList?.find {it.internal == internal && it.internalGroup == internalGroup}?.external
        if (gtvsdaxValue) {
            gtvsdaxMap = [gtvsdaxValue: gtvsdaxValue, appGtvsdaxList: appGtvsdaxList]
        } else {
            gtvsdaxMap = createSdaxMapForAppSessionList( internal, internalGroup, appGtvsdaxList )
        }
        return gtvsdaxMap
    }


    public static def createSdaxMapForAppSessionList( def internal, def internalGroup, List appGtvsdaxList ) {
        def gtvsdaxValue = SdaCrosswalkConversion.fetchAllByInternalAndInternalGroup( internal, internalGroup )[0]?.external
        if (appGtvsdaxList?.size()) {
            appGtvsdaxList << [internal: internal, external: gtvsdaxValue, internalGroup: internalGroup]

        } else {
            appGtvsdaxList = []
            appGtvsdaxList.add( [internal: internal, external: gtvsdaxValue, internalGroup: internalGroup] )
        }
        return [gtvsdaxValue: gtvsdaxValue, appGtvsdaxList: appGtvsdaxList]
    }


    public static Boolean validatePin( String pin, String pidm ) {
        def ctx = SCH.servletContext.getAttribute( GrailsApplicationAttributes.APPLICATION_CONTEXT )
        def sessionFactory = ctx.sessionFactory

        def connection
        boolean isValidPin = false
        int funcRetValue
        try {
            connection = sessionFactory.currentSession.connection()
            String queryString = "BEGIN " +
                    "  ? := CASE gb_third_party_access.f_validate_pin(?,?,?,?) " +
                    "         WHEN TRUE THEN 1 " +
                    "         ELSE 0 " +
                    "         END; " +
                    "END; "
            CallableStatement cs = connection.prepareCall( queryString )
            cs.registerOutParameter( 1, java.sql.Types.INTEGER )
            cs.setString( 2, pidm )
            cs.setString( 3, pin )
            cs.registerOutParameter( 4, java.sql.Types.VARCHAR )
            cs.registerOutParameter( 5, java.sql.Types.VARCHAR )
            cs.executeQuery()
            funcRetValue = cs.getInt( 1 );
            if (funcRetValue == 1) {
                isValidPin = true;
            } else {
                isValidPin = false;
            }
        } finally {
            connection.close()
        }
        return isValidPin
    }

    /**
     * Store the gtvsdax values used by an App in a list that gets passed and stored in the app session
     * @param pin
     * @param pidm
     * @return statusFlag :
     *  0 - Pin is valid
     *  1 - Invalid Pin
     *  2 - Disabled Pin
     *  3 - Expired Pin
     */
    public static int validateUserPin( String pin, String pidm ) {
        def ctx = SCH.servletContext.getAttribute( GrailsApplicationAttributes.APPLICATION_CONTEXT )
        def sessionFactory = ctx.sessionFactory
        def connection
        int statusFlag = 0
        int isPinValid = 0
        String isPinExpired = ""
        String isPinDisabled = ""
        try {
            connection = sessionFactory.currentSession.connection()
            String queryString = "BEGIN " +
                    "  ? := CASE gb_third_party_access.f_validate_pin(?,?,?,?) " +
                    "         WHEN TRUE THEN 1 " +
                    "         ELSE 0 " +
                    "         END; " +
                    "END; "
            CallableStatement cs = connection.prepareCall( queryString )
            cs.registerOutParameter( 1, java.sql.Types.INTEGER )
            cs.setString( 2, pidm )
            cs.setString( 3, pin )
            cs.registerOutParameter( 4, java.sql.Types.VARCHAR )
            cs.registerOutParameter( 5, java.sql.Types.VARCHAR )
            cs.executeQuery()
            isPinValid = cs.getInt( 1 )
            isPinExpired = cs.getString( 4 )
            isPinDisabled = cs.getString( 5 )
            if (isPinValid == 0) {
                statusFlag = INVALID_PIN
            } else if (isPinDisabled.equals( "Y" )) {
                statusFlag = DISABLED_PIN
            } else if (isPinExpired.equals( "Y" )) {
                statusFlag = EXPIRED_PIN
            }
        } finally {
            connection.close()
        }
        return statusFlag
    }


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
