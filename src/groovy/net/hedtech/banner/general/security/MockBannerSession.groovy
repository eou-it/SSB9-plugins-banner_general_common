package net.hedtech.banner.general.security

import org.apache.log4j.Logger

/**
 * BatchBannerSession implements enough of the HttpSession interface in order provide the mep code
 * requested by the banner authentication providers during BannerDS proxy authorization. BannerDS
 * assumes a http session has been established that stores the mep code. Usually this is done by one
 * of the servlet filters (AccessFilter). Since a batch thread has no http session, we fake it out.
 */
class MockBannerSession {
    private final Logger log = Logger.getLogger(getClass())

    String mep

    /**
     * Mimics the getAttribute( name ) method on a http session.
     * @param name the name of an attribute. Only "mep" is relevant.
     * @return the value of the named attribute or null if it is undefined
     */
    public Object getAttribute( String name ) {
        if (log.isDebugEnabled()) log.debug( "fetching attribute for ${name}, mep = ${mep}" )

        if (name?.equals( "mep" )) {
            return mep
        } else return null
    }
}
