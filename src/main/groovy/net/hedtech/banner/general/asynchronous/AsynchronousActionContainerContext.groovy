/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.asynchronous

import org.codehaus.groovy.grails.commons.GrailsApplication;


/**
 * Execution context that may be used within a thread (e.g., processing an
 * event, executing an action, or executing a rule).
 *
 * @author charlie hardt
 */
@SuppressWarnings("serial")
public class AsynchronousActionContainerContext {

   // While available from the application context, the action registry is
   // required by every job.  It is provided explicitly to avoid redundant
   // lookups.
   /**
    * The action registry that may be used to retrieve actions given their URI.
    */
   private AsynchronousActionRegistry actionRegistry;

   private GrailsApplication grailsApplication


//  ----------------------------- Constructors ---------------------------------


    /**
     * Default constructor for an Container Context.  Container context is
     * used to provide execution context information that is from the container
     * (as opposed to ExecutionContext that provides execution-specific context
     * information that must be serializable).
     * @see ExecutionContext
     */
    public AsynchronousActionContainerContext( ) { }


    /**
     * Default constructor for an Container Context.  Container context is
     * used to provide execution context information that is from the container
     * (as opposed to ExecutionContext that provides execution-specific context
     * information that must be serializable).
     * @see ExecutionContext
     * @param applicationContext an application context available during execution
     */
    public AsynchronousActionContainerContext( GrailsApplication grailsApplication, AsynchronousActionRegistry actionRegistry ) {
        this.grailsApplication = grailsApplication
        this.actionRegistry = actionRegistry
     }


    /**
     * Returns the actionRegistry.
     * @return the actionRegistry
     */
    public final AsynchronousActionRegistry getActionRegistry() {
        return actionRegistry;
    }

    /**
     * Returns the grails application context from which resources may be retrieved,
     * or null if an application context has not yet been set.
     * @return ApplicationContext the application context
     */
    public GrailsApplication getGrailsApplication() {
        return this.grailsApplication
    }


    /**
     * Sets the actionRegistry for this ContainerContext.
     * @param actionRegistry the actionRegistry to set
     */
    public final void setActionRegistry( AsynchronousActionRegistry actionRegistry ) {
        this.actionRegistry = actionRegistry;
    }
}
