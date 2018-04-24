/*********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/

package net.hedtech.banner.general.overall

import net.hedtech.banner.exceptions.ApplicationException
import org.apache.log4j.Logger
import org.springframework.security.core.context.SecurityContextHolder

class UserRoleService {

    def log = Logger.getLogger(this.getClass())

    def hasUserRole( String role ) {
        try {
            def authorities = SecurityContextHolder?.context?.authentication?.principal?.authorities
            return authorities.any {it.getAssignedSelfServiceRole().contains( role )}
        } catch (MissingPropertyException it) {
            log.error( "principal lacks authorities - may be unauthenticated or session expired. Principal: ${SecurityContextHolder?.context?.authentication?.principal}" )
            log.error( it )
            throw new ApplicationException( 'UserRoleService', it )
        }
    }

    def getRoles() {
        [
            isStudent: hasUserRole( "STUDENT" ),
            isEmployee: hasUserRole( "EMPLOYEE" ),
            isAipAdmin: hasUserRole( "ACTIONITEMADMIN" )
        ]
    }
}
