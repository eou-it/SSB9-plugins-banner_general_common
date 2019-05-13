/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.asynchronous;

import java.util.concurrent.ThreadFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A factory for creating Thread objects for use within a thread pool.
 *
 * @author charlie hardt
 */
public class AsynchronousActionPoolThreadFactory implements ThreadFactory {


    private final Log log = LogFactory.getLog( "AsynchronousActionPoolThreadFactory" );

    private final String baseName;


    /**
     * A factory for creating threads to be executed within a thread pool.
     * @param poolName the thread pool name that will manage the new thread
     */
    public AsynchronousActionPoolThreadFactory( String poolName ) {
        if (poolName == null || poolName.length() < 1) {
            poolName = "AsynchronousActionThreadPool";
        }
        this.baseName = poolName;
    }


    /**
     * Returns a new Thread configured for use within the thread pool.
     * @param runnable the runnable to be executed within the new thread
     * @return Thread the new thread
     */
    public Thread newThread( Runnable runnable ) {
        AsynchronousActionPoolThread thread =
            new AsynchronousActionPoolThread( runnable, baseName );

       thread.setUncaughtExceptionHandler( new Thread.UncaughtExceptionHandler() {
            public void uncaughtException( Thread t, Throwable e ) {
                log.error( "UNCAUGHT in thread " + t.getName(), e );
            }
        });
       return thread;
    }

}
