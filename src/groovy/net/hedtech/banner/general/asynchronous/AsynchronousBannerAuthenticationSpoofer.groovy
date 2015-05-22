package net.hedtech.banner.general.asynchronous

import groovy.sql.Sql
import net.hedtech.banner.security.BannerAuthenticationProvider
import net.hedtech.banner.security.FormContext
import net.hedtech.banner.security.SelfServiceBannerAuthenticationProvider
import org.apache.log4j.Logger
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.context.request.RequestContextHolder

import java.sql.SQLException

/**
 * An authentication provider for batch threads that authorize a user using behind a batch job thread.
 */
public class AsynchronousBannerAuthenticationSpoofer implements AuthenticationProvider {
    private static final Logger log = Logger.getLogger( "net.hedtech.banner.general.communication.batch.BatchAuthenticationProvider" )

    def dataSource  // injected by Spring


    public Authentication authenticate( String oracleUserName ) {
        log.debug( "Attempting to authenticate as ${oracleUserName}" )
        this.authenticate( new AsynchronousBannerToken( oracleUserName ) )
    }


    public Authentication authenticate( Authentication authentication ) {
        String oracleUserName =  ((AsynchronousBannerToken) authentication).oracleUserName
        log.debug "TrustedAuthenticationProvider.authenticate invoked for ${oracleUserName}"

        def conn
        try {
            conn = dataSource.unproxiedConnection
            Sql db = new Sql( conn )

            def authenticationResults = trustedAuthentication( oracleUserName, db )

            // Next, we'll verify the authenticationResults (and throw appropriate exceptions for expired pin, disabled account, etc.)
            BannerAuthenticationProvider.verifyAuthenticationResults( this, authentication, authenticationResults )
            authenticationResults['authorities'] = BannerAuthenticationProvider.determineAuthorities( authenticationResults, db )
            authenticationResults['fullName'] = getFullName( authenticationResults.name.toUpperCase(), dataSource ) as String
            return BannerAuthenticationProvider.newAuthenticationToken( this, authenticationResults )
        } finally {
            conn?.close()
        }
    }


    public boolean supports( Class clazz ) {
        log.debug "Saying supports for " + clazz
        return clazz instanceof AsynchronousBannerToken
    }


    private getFullName ( String name, dataSource ) {
        BannerAuthenticationProvider.getFullName( name, dataSource )
    }


    private def trustedAuthentication( oracleUserName, db ) {
        log.debug "TrustedAuthenticationProvider.trustedAuthentication doing trusted authentication"

        def pidm
        def authenticationResults
        String accountStatus

        try {
            // Determine if they map to a Banner Admin user
            def sqlStatement = '''SELECT gobeacc_pidm FROM gobeacc where gobeacc_username = ?'''
            db.eachRow( sqlStatement, [oracleUserName] ) { row ->
                pidm = row.gobeacc_pidm
            }

            if ( pidm || oracleUserName) {
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
                throw new UsernameNotFoundException( "$oracleUserName is not a valid username in gobeacc and can not be used for trusted authentication." )
            }
        } catch (SQLException e) {
            log.error "TrustedAuthenticationProvider not able to map $authentication.pidm to db user"
            throw e
        }
        log.trace "TrustedAuthenticationProvider.trustedAuthentication results are $authenticationResults"
        authenticationResults
    }

}
