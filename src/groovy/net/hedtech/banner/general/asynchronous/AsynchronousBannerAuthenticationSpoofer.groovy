/*********************************************************************************
 Copyright 2015-2017 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.asynchronous

import groovy.sql.Sql
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.mep.MultiEntityProcessingService
import net.hedtech.banner.security.AuthenticationProviderUtility
import net.hedtech.banner.security.BannerAuthenticationProvider
import net.hedtech.banner.security.FormContext
import net.hedtech.banner.security.SelfServiceBannerAuthenticationProvider
import org.apache.log4j.Logger
import grails.util.Holders
import org.springframework.context.ApplicationContext
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException

import java.sql.SQLException

/**
 * An authentication provider for batch threads that authorize a user using behind a batch job thread.
 */
public class AsynchronousBannerAuthenticationSpoofer implements AuthenticationProvider {
    private static final log = Logger.getLogger(AsynchronousBannerAuthenticationSpoofer.class)
    public static final monitorOracleUserName = 'COMMMGR'

    def dataSource  // injected by Spring
    MultiEntityProcessingService multiEntityProcessingService


    public Authentication authenticate(String oracleUserName, String mepCode = null) {
        log.debug("Attempting to authenticate as ${oracleUserName}")
        this.authenticate(new AsynchronousBannerToken(oracleUserName), mepCode)
    }


    public authenticateAndSetFormContextForExecute() {

        if (!SecurityContextHolder.getContext().getAuthentication()) {
            FormContext.set(['CMQUERYEXECUTE'])
            Authentication auth
            try {
                auth = authenticate(monitorOracleUserName)
            } catch (Throwable t) {
                log.error(t)
                t.printStackTrace()
            }
            SecurityContextHolder.getContext().setAuthentication(auth)
            if (log.isDebugEnabled()) log.debug("Authenticated as ${monitorOracleUserName} for async task process monitor thread.")
        } else {
            log.debug("Already authenticated as ${SecurityContextHolder.getContext().getAuthentication().principal.toString()}.")
        }
    }


    public authenticateAndSetFormContextForExecuteAndSave(String username = null, String mepCode = null) {
        List<String> originalFormContext = FormContext.get()
        Authentication originalAuthentication = SecurityContextHolder.getContext().getAuthentication()
        FormContext.set(['CMQUERYEXECUTE'])
        Authentication auth = authenticate(username ?: CommunicationCommonUtility.getUserOracleUserName(), mepCode)
        SecurityContextHolder.getContext().setAuthentication(auth)
        return [originalFormContext: originalFormContext, originalAuthentication: originalAuthentication]
    }


    public resetAuthAndFormContext(map) {
        FormContext.set(map?.originalFormContext)
        SecurityContextHolder.getContext().setAuthentication(map?.originalAuthentication)
    }


    public Authentication authenticate(Authentication authentication, String mepCode = null) {
        String oracleUserName = ((AsynchronousBannerToken) authentication).oracleUserName
        log.debug "TrustedAuthenticationProvider.authenticate invoked for ${oracleUserName}"

        def conn
        try {
            conn = dataSource.unproxiedConnection
            setMep(conn, oracleUserName, mepCode)
            Sql db = new Sql(conn)

            def authenticationResults = trustedAuthentication(oracleUserName, db)

            // Next, we'll verify the authenticationResults (and throw appropriate exceptions for expired pin, disabled account, etc.)
            AuthenticationProviderUtility.verifyAuthenticationResults(this, authentication, authenticationResults)
            authenticationResults['authorities']        = (Collection<GrantedAuthority>) SelfServiceBannerAuthenticationProvider.determineAuthorities( authenticationResults, db )
            authenticationResults['fullName'] = getFullName(authenticationResults.name.toUpperCase(), dataSource) as String
            return AuthenticationProviderUtility.newAuthenticationToken(this, authenticationResults)
        } finally {
            conn?.close()
        }
    }


    public setMepContext(conn, String mepCode = null) {
        log.info("setting mep. The mep context value is ${mepCode}")

        if (isMEP(conn) && mepCode) {
                getMultiEntityProcessingService().setHomeContext(mepCode, conn)
                getMultiEntityProcessingService().setProcessContext(mepCode, conn)
        }
    }


    public setMepProcessContext(conn, mepCode) {
        if (isMEP(conn) && (mepCode)) {
            getMultiEntityProcessingService().setProcessContext(mepCode, conn)
        }
    }


    public boolean supports(Class clazz) {
        log.debug "Saying supports for " + clazz
        return clazz instanceof AsynchronousBannerToken
    }


    private MultiEntityProcessingService getMultiEntityProcessingService() {
        if (!multiEntityProcessingService) {
            ApplicationContext ctx = (ApplicationContext) Holders.getGrailsApplication().getMainContext()
            multiEntityProcessingService = (MultiEntityProcessingService) ctx.getBean("multiEntityProcessingService")
        }
        multiEntityProcessingService
    }

    private isMEP(con = null) {

            def mepEnabled
            if (!con)
                con = new Sql(sessionFactory.getCurrentSession().connection())
            Sql sql = new Sql(con)
            try {
                sql.call("{$Sql.VARCHAR = call g\$_vpdi_security.g\$_is_mif_enabled_str()}") { mifEnabled -> mepEnabled = mifEnabled.toLowerCase().toBoolean() }
            } catch (e) {
                log.error("ERROR: Could not establish mif context. $e")
                throw e
            } finally {

            }

        return mepEnabled
    }


    private setMep(conn, user, mepCode = null) {
        if (isMEP(conn)) {
            if (mepCode != null) {
                getMultiEntityProcessingService().setHomeContext(mepCode, conn)
                getMultiEntityProcessingService().setProcessContext(mepCode, conn)
            } else {
                getMultiEntityProcessingService().setMepOnAccess(user.toUpperCase(), conn)
            }
        }
    }


    private getFullName(String name, dataSource) {
        BannerAuthenticationProvider.getFullName(name, dataSource)
    }


    private def trustedAuthentication(oracleUserName, db) {
        log.debug "TrustedAuthenticationProvider.trustedAuthentication doing trusted authentication"

        def pidm
        def authenticationResults
        String accountStatus

        try {
            // Determine if they map to a Banner Admin user
            def sqlStatement = '''SELECT gobeacc_pidm FROM gobeacc where gobeacc_username = ?'''
            db.eachRow(sqlStatement, [oracleUserName]) { row ->
                pidm = row.gobeacc_pidm
            }

            if (pidm || oracleUserName) {
                // check if the oracle user account is locked
                def sqlStatement1 = '''select account_status,lock_date from dba_users where username=?'''
                db.eachRow(sqlStatement1, [oracleUserName.toUpperCase()]) { row ->
                    accountStatus = row.account_status
                }
                if (accountStatus.contains("LOCKED")) {
                    authenticationResults = [locked: true]
                } else if (accountStatus.contains("EXPIRED")) {
                    authenticationResults = [expired: true]
                } else {
                    authenticationResults = [name: oracleUserName, pidm: pidm, oracleUserName: oracleUserName, valid: true].withDefault { k -> false }
                }
            } else {
                throw new UsernameNotFoundException("$oracleUserName is not a valid username in gobeacc and can not be used for trusted authentication.")
            }
        } catch (SQLException e) {
            log.error "TrustedAuthenticationProvider not able to map $oracleUserName to a pidm"
            throw e
        }
        log.trace "TrustedAuthenticationProvider.trustedAuthentication results are $authenticationResults"
        authenticationResults
    }

}
