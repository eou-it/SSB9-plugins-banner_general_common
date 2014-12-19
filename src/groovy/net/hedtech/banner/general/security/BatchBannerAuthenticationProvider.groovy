package net.hedtech.banner.general.security

import groovy.sql.Sql
import net.hedtech.banner.security.BannerAuthenticationProvider
import net.hedtech.banner.security.SelfServiceBannerAuthenticationProvider
import org.apache.log4j.Logger
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication

import java.sql.SQLException

/**
 * An authentication provider for batch threads that authorize a user using behind a batch job thread.
 */
public class BatchBannerAuthenticationProvider implements AuthenticationProvider {
    private static final Logger log = Logger.getLogger( "net.hedtech.banner.general.communication.batch.BatchAuthenticationProvider" )

    def dataSource  // injected by Spring


    public boolean supports( Class clazz ) {
        log.debug "Saying supports for " + clazz
        return clazz instanceof BannerPidmToken
    }


    public static def isSsbEnabled() {
        SelfServiceBannerAuthenticationProvider.isSsbEnabled()
    }


    public Authentication authenticate( Authentication authentication ) {
        def pidm = ((BannerPidmToken) authentication).getPidm()
        log.debug "BatchAuthenticationProvider.authenticate invoked for ${pidm}"

        def conn
        try {
            conn = dataSource.unproxiedConnection
            Sql db = new Sql( conn )

            def authenticationResults = batchAuthentication( pidm, db )


            // Next, we'll verify the authenticationResults (and throw appropriate exceptions for expired pin, disabled account, etc.)
            BannerAuthenticationProvider.verifyAuthenticationResults this, authentication, authenticationResults

            if (isSsbEnabled()) {
                authenticationResults['authorities'] = SelfServiceBannerAuthenticationProvider.determineAuthorities( authenticationResults, db )
            }
            else {
                authenticationResults['authorities'] = BannerAuthenticationProvider.determineAuthorities( authenticationResults, db )
            }

//            def applicationContext = (ApplicationContext) ServletContextHolder.getServletContext().getAttribute( GrailsApplicationAttributes.APPLICATION_CONTEXT )
//            applicationContext.publishEvent( new BannerAuthenticationEvent( authenticationResults.name, true, '', '', new Date(), '' ) )

            authenticationResults['fullName'] = getFullName( authenticationResults.name.toUpperCase(), dataSource ) as String
            return newAuthenticationToken( authenticationResults )
        } finally {
            conn?.close()
        }
    }


    def newAuthenticationToken( authentictionResults ) {
        BannerAuthenticationProvider.newAuthenticationToken( this, authentictionResults )
    }


    public static getFullName ( String name, dataSource ) {
        BannerAuthenticationProvider.getFullName( name, dataSource )
    }


    private def batchAuthentication( pidm, db ) {
        log.debug "BatchAuthenticationProvider.batchAuthentication doing Batch authentication"

        def oracleUserName
        def spridenId
        def authenticationResults
        String accountStatus

        try {
            // Determine if they map to a Banner Admin user
            def sqlStatement = '''SELECT gobeacc_username FROM gobeacc where gobeacc_pidm = ?'''
            db.eachRow( sqlStatement, [pidm] ) { row ->
                oracleUserName = row.gobeacc_username
            }
            if ( oracleUserName ) {

                // check if the oracle user account is locked
                def sqlStatement1 = '''select account_status,lock_date from dba_users where username=?'''
                db.eachRow( sqlStatement1, [oracleUserName.toUpperCase()] ) { row ->
                    accountStatus = row.account_status
                }
                if ( accountStatus.contains("LOCKED")) {
                    authenticationResults = [locked : true]
                } else if ( accountStatus.contains("EXPIRED")) {
                    authenticationResults = [expired : true]
                } else {
                    authenticationResults = [ name: oracleUserName, pidm: pidm, oracleUserName: oracleUserName, valid: true ].withDefault { k -> false }
                }
            } else {
                // Not an Admin user, must map to a self service user
                def sqlStatement2 = '''SELECT spriden_id FROM spriden WHERE spriden_pidm = ? AND spriden_change_ind is null'''
                db.eachRow( sqlStatement2, [pidm] ) { row ->
                    spridenId = row.spriden_id
                }
                authenticationResults = [ name: spridenId, pidm: pidm, valid: (spridenId && pidm), oracleUserName: null ].withDefault { k -> false }
            }
        } catch (SQLException e) {
            log.error "BatchAuthenticationProvider not able to map $authentication.pidm to db user"
            throw e
        }
        log.trace "BatchAuthenticationProvider.batchAuthentication results are $authenticationResults"
        authenticationResults
    }

}
