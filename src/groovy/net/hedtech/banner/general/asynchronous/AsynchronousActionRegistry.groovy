/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.asynchronous;

import net.hedtech.banner.exceptions.ApplicationException;

/**
 * An interface for a registry of actions that supports retrieval by uri.
 * The mappping of actions to URIs is accomplished through configuration.
 *
 * @author charlie hardt
 */
public interface AsynchronousActionRegistry {


    /**
     * Returns the action associated to the supplied uri.
     * @param uri the uri to use for looking up an action
     * @return Action the action that is configured for the uri, or null if not found
     * @throws ApplicationException
     */
    @SuppressWarnings("unchecked")
    public AsynchronousAction getAction( String uri ) throws ApplicationException;

}
