package net.hedtech.banner.general.security

import groovy.sql.Sql
import net.hedtech.banner.security.BannerAuthenticationProvider
import net.hedtech.banner.security.SelfServiceBannerAuthenticationProvider
import org.apache.log4j.Logger
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UsernameNotFoundException

import java.sql.SQLException

/**
 * An authentication provider for batch threads that authorize a user using behind a batch job thread.
 */
public class TrustedBannerAuthenticationProvider implements AuthenticationProvider {
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
        def oracleUserName = ((TrustedBannerToken) authentication).getOracleUserName()
        log.debug "TrustedAuthenticationProvider.authenticate invoked for ${oracleUserName}"

        def conn
        try {
            conn = dataSource.unproxiedConnection
            Sql db = new Sql( conn )

            def authenticationResults = trustedAuthentication( oracleUserName, db )

            // Next, we'll verify the authenticationResults (and throw appropriate exceptions for expired pin, disabled account, etc.)
            BannerAuthenticationProvider.verifyAuthenticationResults this, authentication, authenticationResults

            if (isSsbEnabled()) {
                authenticationResults['authorities'] = SelfServiceBannerAuthenticationProvider.determineAuthorities( authenticationResults, db )
            }
            else {
                authenticationResults['authorities'] = BannerAuthenticationProvider.determineAuthorities( authenticationResults, db )
            }

            authenticationResults['fullName'] = getFullName( authenticationResults.name.toUpperCase(), dataSource ) as String
            return newAuthenticationToken( authenticationResults )
        } finally {
            conn?.close()
        }
    }


    def newAuthenticationToken( authentictionResults ) {
        BannerAuthenticationProvider.newAuthenticationToken( this, authentictionResults )
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

            if ( pidm ) {
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
