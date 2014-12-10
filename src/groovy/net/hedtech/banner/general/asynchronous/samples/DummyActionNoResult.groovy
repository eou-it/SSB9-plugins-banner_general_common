/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.asynchronous.samples;

import net.hedtech.banner.general.asynchronous.AsynchronousAction
import net.hedtech.banner.general.asynchronous.AsynchronousActionContainerContext;

import java.util.concurrent.TimeUnit;

/**
 * A dummy action that does not return any values, but just spends time
 * in order to simulate some processing.
 *
 * @author charlie hardt
 */
@SuppressWarnings("serial")
public class DummyActionNoResult implements AsynchronousAction {


    /**
     * Default constructor for a Dummy Action that is used for testing. Actions
     * should never have anything but a default constructor, or if they do must
     * ensure that they remain thread safe.
     */
    public DummyActionNoResult() { }


    def execute( Map actionContext, AsynchronousActionContainerContext containerContext ) {
//        long startTime = System.nanoTime();
        if (actionContext.get( "SLEEP_TIME" ) != null) {
            TimeUnit.MILLISECONDS.sleep( Long.parseLong( actionContext.get( "SLEEP_TIME" ) ) );
        }
//        long endTime = System.nanoTime();
//        long taskTime = endTime - startTime;
//        System.out.println( "DummyActionNoResult " + this + " took " +
//                TimeUnit.MILLISECONDS.convert( taskTime, TimeUnit.NANOSECONDS ) +
//                " milliseconds to execute." );
//        System.out.println("Executing DummyActionNoResult action request at " + new Date());
        return null;
     }

}
