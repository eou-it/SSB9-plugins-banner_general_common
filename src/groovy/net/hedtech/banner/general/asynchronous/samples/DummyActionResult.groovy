/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.asynchronous.samples;

import net.hedtech.banner.general.asynchronous.AsynchronousActionResult;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A result returned when executing a DummyAction.
 *
 * @author charlie hardt
 */
@SuppressWarnings("serial")
public class DummyActionResult implements AsynchronousActionResult {

    private static AtomicInteger counter = new AtomicInteger();
    private String threadName;
    private long sleepTime;


    /**
     * A dummy action result that is used solely for unit testing purposes.
     */
    public DummyActionResult( long sleepTime ) {
        counter.addAndGet( 1 );
        threadName = Thread.currentThread().toString();
        this.sleepTime = sleepTime;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append( "DummyActionResult[count=" ).append( counter );
        sb.append( ", executed by thread: " ).append( threadName ).append( "]" );
        sb.append( "Slept time was: " + sleepTime );
        return sb.toString();
    }
}

