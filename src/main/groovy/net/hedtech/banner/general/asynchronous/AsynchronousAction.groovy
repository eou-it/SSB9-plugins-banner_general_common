/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.asynchronous
/**
 * An interface for an action that may be executed through an execute
 * invocation (i.e., that implements the Command Pattern).  Note that actions
 * must return something that is Serialible.
 *
 * @author charlie hardt
 * @author Michael Brzycki
 */
public interface AsynchronousAction extends Serializable {


    /**
     * Executes an action and returns a result.
     * @param actionContext the action-specific executable context
     * @param containerContext access to environment to get services, etc
     * @return T the result of the execution
     * @throws Exception if the action encounters a checked exception
     */
    def execute( Map actionContext, AsynchronousActionContainerContext containerContext ) throws Exception;

}
