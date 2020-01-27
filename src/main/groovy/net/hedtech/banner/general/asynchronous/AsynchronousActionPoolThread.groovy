/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.asynchronous

import groovy.util.logging.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A thread for use within the JobSubmissionThreadpool.
 * This is based upon a published example by Brian Goetz, from his book
 * Java Concurrency in Practice.
 *
 * @author Brian Goetz and Tim Peierls
 * @author charlie hardt
 */
@Slf4j
public class AsynchronousActionPoolThread extends Thread {

    public static final String DEFAULT_NAME = "AsynchronousActionPoolThread";

    private static volatile boolean debugLifecycle = false;
    private static final AtomicLong created = new AtomicLong();
    private static final AtomicInteger alive = new AtomicInteger();


//  ----------------------------- Constructors ---------------------------------


    /**
     * A thread specialized for use within the ThreadPool.
     * @param runnable a runnable that will be executed
     */
    public AsynchronousActionPoolThread( Runnable runnable ) {
        this( runnable, DEFAULT_NAME );
    }


    public AsynchronousActionPoolThread( Runnable runnable, String name ) {
        super( runnable, name + "-" + created.incrementAndGet() );
    }


//  ---------------------------- Thread Methods --------------------------------


    /* (non-Javadoc)
     * @see java.lang.Thread#run()
     */
    public void run() {
        // Copy debug flag to ensure consistent value throughout.
        boolean debug = debugLifecycle;
        if (debug) log.debug( "Created " + getName());
        try {
            alive.incrementAndGet();
            super.run();
        } finally {
            alive.decrementAndGet();
            if (debug) log.debug( "Exiting " + getName());
        }
    }


    /**
     * Returns the number of threads created.
     * @return long the number of threads
     */
    public static long getThreadsCreated() {
        return created.get();
    }


    /**
     * Returns the number of threads currently alive.
     * @return long the number of threads
     */
    public static int getThreadsAlive() {
        return alive.get();
    }


    /**
     * Returns true if debugging is currently enabled.
     * @return boolean true if debugging is enabled
     */
    public static boolean getDebug() {
        return debugLifecycle;
    }


    /**
     * Sets whether debugging should be enabled.
     * @param enabled an indicator of whether debugging should be enabled
     */
    public static void setDebug( boolean b ) {
        debugLifecycle = b;
    }
}
