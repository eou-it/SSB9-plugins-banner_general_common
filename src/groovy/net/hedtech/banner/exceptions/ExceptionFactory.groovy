/* ****************************************************************************
Copyright 2015 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.exceptions

/**
 * Convenience methods for creating an application exceptions. Largely the factory takes care of stricter
 * type checking in order to take some of the confusion out of exception naming conventions and
 * to alleviate the developer assembling @@r1 in the calling code.
 */
class ExceptionFactory {

    /**
     * Creates an application exception.
     *
     * The string in messages.properties should be the package name of the service + "." + the service class name + "." + resourceId.
     *
     * @param service the service whose namespace will be used for specifying the resourceId
     * @param resourceId the sub key of the string resource
     * @param parameter0 the first parameterized value which will be passed as the {0} of the string value
     * @return
     */
    public static ApplicationException createApplicationException( Class service, String resourceId ) {
        return new ApplicationException( service, "@@r1:${resourceId}@@" )
    }

    /**
     * Creates an application exception.
     *
     * The string in messages.properties should be the package name of the service + "." + the service class name + "." + resourceId.
     *
     * @param namespace the namespace will be used for specifying the resourceId
     * @param resourceId the sub key of the string resource
     * @param parameter0 the first parameterized value which will be passed as the {0} of the string value
     * @return
     */
    public static ApplicationException createApplicationException( String namespace, String resourceId ) {
        return new ApplicationException( namespace, "@@r1:${resourceId}@@" )
    }

    /**
     * Creates an application exception.
     * @param service the service whose namespace will be used for specifying the resourceId
     * @param resourceId the sub key of the string resource
     * @param parameter0 the first parameterized value which will be passed as the {0} of the string value
     * @return
     */
    public static ApplicationException createApplicationException( Class service, String resourceId, String parameter0 ) {
        return new ApplicationException( service, "@@r1:${resourceId}:${parameter0}@@" )
    }

    /**
     * Creates an application exception.
     * @param namespace the namespace will be used for specifying the resourceId
     * @param resourceId the sub key of the string resource
     * @param parameter0 the first parameterized value which will be passed as the {0} of the string value
     * @return
     */
    public static ApplicationException createApplicationException( String namespace, String resourceId, String parameter0 ) {
        return new ApplicationException( namespace, "@@r1:${resourceId}:${parameter0}@@" )
    }

    /**
     * Creates an application exception to wrap around a caught exception.
     * @param service the service which describes the originator of the exception
     * @param t the throwable that was caught
     */
    public static ApplicationException createApplicationException( Class service, Throwable t ) {
        return new ApplicationException( service, t )
    }

    /**
     * Convenience method for creating a not found exception which takes the id of the object in question
     * and the the class for which it belongs.
     * @param id the long value identifier for the object being searched
     * @param entityType the class for the entity object
     * @return
     */
    public static NotFoundException createNotFoundException( Long id, Class entityType ) {
        return new NotFoundException( id: id, entityClassName: entityType.simpleName )
    }

    /**
     * Creates an application exception to wrap around a caught exception.
     * @param service the service which describes the originator of the exception
     * @param t the throwable that was caught
     * @param errorCode the error code
     */
    public static ApplicationException createApplicationException( Class service, Throwable t, String errorCode ) {
        ApplicationException ae = new ApplicationException( service, t )
        ae.friendlyName = errorCode
        return ae
    }

    /**
     * Creates an application exception annotated with a friendly name.
     * @param service the service which describes the originator of the exception
     * @param t the throwable that was caught
     * @param errorCode a string that will be past along with the application exception as a 'friendlyName'
     */
    public static ApplicationException createFriendlyApplicationException( Class service, String errorCode, String resourceId ) {
        ApplicationException ae = ExceptionFactory.createApplicationException( service, resourceId )
        ae.friendlyName = errorCode
        return ae
    }

    /**
     * Creates an application exception annotated with a friendly name.
     * @param service the service which describes the originator of the exception
     * @param errorCode a string that will be past along with the application exception as a 'friendlyName'
     * @param resourceId the sub key of the string resource
     * @param parameter0 the first parameterized value which will be passed as the {0} of the string value
     */
    public static ApplicationException createFriendlyApplicationException( Class service, String errorCode, String resourceId, String parameter0 ) {
        ApplicationException ae = ExceptionFactory.createApplicationException( service, resourceId, parameter0 )
        ae.friendlyName = errorCode
        return ae
    }
}
