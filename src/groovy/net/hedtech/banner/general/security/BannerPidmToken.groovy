package net.hedtech.banner.general.security
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority

/**
 * Created by mbrzycki on 8/12/14.
 */
class BannerPidmToken implements Authentication {
    private Integer pidm

    public BannerPidmToken( int thePidm ) {
        this.pidm = thePidm
    }

    public Integer getPidm() {
        pidm
    }

    @Override
    Collection<GrantedAuthority> getAuthorities() {
        return null
    }

    @Override
    Object getCredentials() {
        return null
    }

    @Override
    Object getDetails() {
        return null
    }

    @Override
    Object getPrincipal() {
        return null
    }

    @Override
    boolean isAuthenticated() {
        return false
    }

    @Override
    void setAuthenticated(boolean b) throws IllegalArgumentException {

    }

    @Override
    String getName() {
        return null
    }
}
