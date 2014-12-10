/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.asynchronous.samples

import net.hedtech.banner.general.asynchronous.AsynchronousAction
import net.hedtech.banner.general.asynchronous.AsynchronousActionContainerContext;

import java.util.concurrent.TimeUnit;

/**
 * A dummy action used for testing.
 *
 * @author charlie hardt
 */
public class DummyActionWithResult implements AsynchronousAction {

    /**
     * Default constructor for a Dummy Action used for testing. This action
     * does no work other than sleep (if the supplied context during execution
     * sets a sleep time.
     */
    public DummyActionWithResult() { }


    def execute( Map actionContext, AsynchronousActionContainerContext containerContext ) {

        long startTime = System.nanoTime();
        if (actionContext.get( "SLEEP_TIME" ) != null) {
            TimeUnit.MILLISECONDS.sleep( Long.parseLong( actionContext.get( "SLEEP_TIME" ) ) );
        }
        long endTime = System.nanoTime();
        long taskTime = endTime - startTime;

        return new DummyActionResult(
                TimeUnit.MILLISECONDS.convert( taskTime, TimeUnit.NANOSECONDS ) );
    }

}


