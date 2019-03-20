/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.asynchronous;

//import com.sungardhe.framework.ApplicationException;
//import com.sungardhe.framework.SystemException;
//import com.sungardhe.framework.entitysupport.NotFoundException;
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import java.util.Map.Entry;

/**
 * An action registry that supports retrieval of Actions by uri. This registry
 * is configured with Spring.
 *
 * @author charlie hardt
 */
public class AsynchronousActionRegistryImpl implements AsynchronousActionRegistry, InitializingBean {


    private Log log = LogFactory.getLog( AsynchronousActionRegistryImpl.class );

    /**
     * The map between uri's and actions, that is constructed by instantiating
     * the injected maps of Java classes.  The instantiation is done separately
     * because we do not want the actions to be spring-managed beans.
     * @see #uriActionClassMap
     */
    @SuppressWarnings("unchecked")
    private Map uriActionMap = new HashMap();

    /**
     * The map between uri's and class names of actions. This map is injected
     * and used to seed the map of uri's to instantiated Action instances.
     * The instances are not injected from the container directly in order to
     * avoid overhead of proxy creation and subsequent issues with interface
     * resolution.  This map holds static configuration that a user cannot
     * modify (without hand-editing the configuration file and bouncing the
     * server).
     * @see #uriActionMap
     */
    private Map<String,String> uriActionClassMap = null;



//  ------------------------------ Constructor(s) ------------------------------
// (and initialization method(s)


    /**
     * Default constructor for a action registry.
     */
    public AsynchronousActionRegistryImpl() { }


    /**
     * Initializes the Action Registry by instantiating Actions and putting
     * them into the uriActionMap.  Note that instantiation is done here
     * rather than in the container to circumvent proxy creation, as we don't
     * want these Actions to be 'spring managed beans'.
     */
    @Override
    void afterPropertiesSet() throws Exception {
        synchronized (uriActionMap) {
            for (Entry entry : uriActionClassMap.entrySet()) {
                uriActionMap.put( (String) entry.getKey(),
                                  instantiateAction( (String) entry.getValue() ));
            }
        }
    }


    public AsynchronousAction getAction( String uri ) throws ApplicationException {
        AsynchronousAction result = uriActionMap.get( uri );
        if (result == null) {
            throw new NotFoundException( id:uri,  entityClassName:AsynchronousAction.class.simpleName );
        }
        return result;
    }


//  ------------------------ Getters and Setters -------------------------------


    /**
     * Sets the uriActionClassMap for this ActionRegistryImpl.
     * @param uriActionClassMap the uriActionClassMap to set
     */
    @Required
    public final void setUriActionClassMap( Map<String,String> uriActionClassMap ) {
        this.uriActionClassMap = uriActionClassMap;
        if (log.isDebugEnabled()) dumpMap( "uriActionClassMap", uriActionClassMap );
    }


//  ---------------------------- Helper Methods --------------------------------


    /**
     * Returns true if this action URI is mapped to an Action through XML
     * configuration.  This does not indicate whether or not an action is
     * mapped to this URI within the persistent store.
     * @param actionURI the action URI to check
     * @return true if this action registry has a configuration for this URI
     */
    private boolean isConfigured( String actionURI ) {
        if (uriActionClassMap.containsKey( actionURI )) return true;
        else                                            return false;
    }


    /**
     * Logs the supplied Map content to the logger configured for this action
     * registry.
     * @param String name the name to use for the log
     * @param Map the map for which to write contents to the log
     */
    private void dumpMap( String name, Map map ) {
        for (Map.Entry entry : map.entrySet()) {
            log.info( "URI: " + entry.getKey() + " mapped to: " + entry.getValue() );
        }
    }


    /**
     * Returns the class having the supplied class name.
     * @param className the name of the Class to return
     * @return Class the Class with the supplield class name
     */
    private Class getClassFor( String className ) {
        assert className != null;
        try {
            return Class.forName( className );
        } catch (ClassNotFoundException e) {
            log.error( "ClassNotFound for Action class name: " + className, e );
            throw new RuntimeException( "ActionRegistryImpl.ClassNotFoundException" );
        }
    }


    /**
     * Returns a new instance of the supplied class, of type Action.
     * @param actionClass the Class from which to create an instance
     * @return Action the newly instantiated instance
     */
    @SuppressWarnings("unchecked")
    private AsynchronousAction instantiateAction( Class actionClass ) {
        try {
            return (AsynchronousAction) actionClass.newInstance();
        } catch (InstantiationException e) {
            log.error( "Could not instantiate Action from class: " + actionClass.getName(), e );
            throw new RuntimeException( "ActionRegistryImpl.InstantiationException" );
        } catch (IllegalAccessException e) {
            log.error( "Encountered IllegalAccessException when creating Action " +
                    "from class: " + actionClass.getName(), e );
            throw new RuntimeException( "ActionRegistryImpl.IllegalAccessException" );
        }
    }


    /**
     * Instantiates an Action based upon the supplied class name.
     * @param className the name of the class to instantiate, must not be null
     * @return Action the newly instantiated Action object
     */
    @SuppressWarnings("unchecked")
    private AsynchronousAction instantiateAction( String className ) {
        return instantiateAction( getClassFor( className ) );
    }

}
