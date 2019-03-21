/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.asynchronous

import grails.core.GrailsApplication;
import grails.core.support.GrailsApplicationAware


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.task.TaskExecutor;

/**
 * A very simple extension of ThreadPoolExecutor that captures some timing
 * information for logging, as well as some basic statistics.
 * This is based upon a published example by Brian Goetz, from his book
 * Java Concurrency in Practice.
 *
 * @author Brian Goetz and Tim Peierls
 * @author charlie hardt
 */
public class AsynchronousActionThreadPool extends ThreadPoolExecutor implements TaskExecutor, GrailsApplicationAware {

    GrailsApplication grailsApplication
    private final Log log = LogFactory.getLog( "AsynchronousActionThreadPool" );

    private final ThreadLocal startTime = new ThreadLocal();
    private final AtomicLong numTasks = new AtomicLong();
    private final AtomicLong totalTime = new AtomicLong();



//  ----------------------------- Constructors ---------------------------------


    /**
     * Constructor for a AsynchronousActionThreadPool.
     * @param corePoolSize the minimum and initial pool size
     * @param maxPoolSize the maximum number of theads that can be used
     * @param keepAliveTime the time a thread is kept alive when not being used
     * @param queue a blocking queue used to feed work into the pool
     * @param threadFactory the thread factory to use for creating threads
     * @param rejectedExecHandler a handler that will allow logging of rejected jobs
     */
    public AsynchronousActionThreadPool(BlockingQueue queue, ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecHandler )
    {
        // this is a bit awkward, but ThreadPoolExecutor requires these values passed in a constructor, so we can't
        // do normal spring-injection, so we use static methods to extract the values from the config.
        super( 2, 10, 400,
//                Integer.parseInt( grailsApplication.config.jobSubmission.corePoolSize as String ),
//               Integer.parseInt( grailsApplication.config.jobSubmission.maxPoolSize as String ),
//               Long.parseLong( grailsApplication.config.jobSubmission.keepAlive as String ),
               TimeUnit.MILLISECONDS,
               // TODO: MB - Creating these here from now because of error reading and passing from xml definition
               queue,
               threadFactory,
               rejectedExecHandler );


        super.prestartCoreThread();
        if (log.isDebugEnabled()) {
            log.debug( "AsynchronousActionThreadPool has been constructed" );
        }
    }


//  ------------------ ThreadPoolExecutor Callback Methods ---------------------


    /* (non-Javadoc)
     * @see java.util.concurrent.ThreadPoolExecutor#beforeExecute(java.lang.Thread, java.lang.Runnable)
     */
    @Override
    final protected void beforeExecute( Thread t, Runnable r ) {
        super.beforeExecute(t, r);
        if (log.isDebugEnabled()) {
            log.debug( String.format( "Thread %s: start %s", t, r ) );
        }
        startTime.set( System.nanoTime() );
    }


    /* (non-Javadoc)
     * @see java.util.concurrent.ThreadPoolExecutor#afterExecute(java.lang.Runnable, java.lang.Throwable)
     */
    @Override
    final protected void afterExecute( Runnable r, Throwable t ) {
        try {
            long endTime = System.nanoTime();
            long taskTime = endTime - startTime.get();
            numTasks.incrementAndGet();
            totalTime.addAndGet(taskTime);
            if (log.isDebugEnabled()) {
                log.debug( String.format("Thread %s: end %s, time=%dns", t, r, taskTime));
            }
        } finally {
            super.afterExecute(r, t);
        }
    }


    /* (non-Javadoc)
     * @see java.util.concurrent.ThreadPoolExecutor#terminated()
     */
    @Override
    final protected void terminated() {
        try {
            if (log.isDebugEnabled()) {
                log.debug( String.format( "Terminated: avg time=%dns",
                           totalTime.get() / numTasks.get() ) );
            }
        } finally {
            super.terminated();
        }
    }


//  ---------------------------- Public Methods --------------------------------


    /**
     * Returns the total number of jobs that have been executed.
     * @see #getTotalTime( TimeUnit )
     * @return long the total number of jobs
     */
    final public long getTotalCompletedActions() {
        return numTasks.longValue();
    }


    /**
     * Returns the total time that all executed jobs needed to execute.  Note
     * that jobs may have executed between calling this method and the
     * getTotalCompletedActions method, so understand that any averages calculated
     * are 'ball park' at best -- unless the pool is in a terminated state.
     * @param unit the TimeUnit in which to return the time (defaults to Millis)
     * @see #getTotalCompletedActions()
     * @return long the total time that jobs needed to execute
     */
    final public long getTotalTime( TimeUnit unit ) {
        if (unit == null) {
            unit = TimeUnit.MILLISECONDS;
        }
        return unit.convert( totalTime.longValue(), TimeUnit.NANOSECONDS );
    }

}
