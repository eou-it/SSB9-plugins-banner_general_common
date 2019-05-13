package net.hedtech.banner.general.asynchronous
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority

/**
 * Created by mbrzycki on 8/12/14.
 */
class AsynchronousBannerToken implements Authentication {
    private String oracleUserName

    public AsynchronousBannerToken( String oracleUserName ) {
        this.oracleUserName = oracleUserName
    }

    public String getOracleUserName() {
        oracleUserName
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
