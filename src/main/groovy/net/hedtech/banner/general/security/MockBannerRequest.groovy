/*********************************************************************************
 Copyright 2014-2017 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.security

import org.apache.log4j.Logger
import org.codehaus.groovy.runtime.StackTraceUtils
import org.springframework.web.context.request.RequestAttributes

/**
 * A mock request attribute for handing back a batch banner session.
 */
class MockBannerRequest implements RequestAttributes {
    private static final log = Logger.getLogger(MockBannerRequest.class)
    def session

    def getSession() {
        if (log.traceEnabled) log.trace( getCurrentMethodName() )
        return session
    }

    void setSession(session) {
        if (log.traceEnabled) log.trace( getCurrentMethodName() )
        this.session = session
    }

    @Override
    Object getAttribute(String name, int scope) {
        if (log.traceEnabled) log.trace( getCurrentMethodName() )
        return null
    }

    @Override
    void setAttribute(String name, Object value, int scope) {
        if (log.traceEnabled) log.trace( getCurrentMethodName() )

    }

    @Override
    void removeAttribute(String name, int scope) {
        if (log.traceEnabled) log.trace( getCurrentMethodName() )

    }

    @Override
    String[] getAttributeNames(int scope) {
        if (log.traceEnabled) log.trace( getCurrentMethodName() )

        return new String[0]
    }

    @Override
    void registerDestructionCallback(String name, Runnable callback, int scope) {
        if (log.traceEnabled) log.trace( getCurrentMethodName() )

    }

    @Override
    Object resolveReference(String key) {
        if (log.traceEnabled) log.trace( getCurrentMethodName() )

        return null
    }

    @Override
    String getSessionId() {
        if (log.traceEnabled) log.trace( getCurrentMethodName() )

        return null
    }

    @Override
    Object getSessionMutex() {
        if (log.traceEnabled) log.trace( getCurrentMethodName() )

        return null
    }

    private String getCurrentMethodName(){
      def marker = new Throwable()
      return StackTraceUtils.sanitize(marker).stackTrace[1].methodName
    }
}
