/*********************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.system.ldm

/**
 *
 * Decorator to hold Ellucian Integration Hub Configuration.
 *
 */
class IntegrationHubConfig {

    /**
     * ID of the application from the hub admin UI
     */
    String applicationID

    /**
     * Name of the application from the hub admin UI
     */
    String applicationName

    /**
     * API Key of the application from the hub admin UI
     */
    String apiKey

    /**
     * URL for the hub token service
     */
    String tokenUrl

    /**
     * URL for the hub publish service
     */
    String publishUrl

    /**
     * URL for the hub subscribe service
     */
    String subscribeUrl

    /**
     * Media type for messages getting published and retrieved (i.e. application/vnd.hedtech.change-notifications.v2+json)
     */
    String hubMediaType

}