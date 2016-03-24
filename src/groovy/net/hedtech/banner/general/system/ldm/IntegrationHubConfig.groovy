/*********************************************************************************
 Copyright 2015-2016 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.system.ldm

/**
 *
 * Decorator to hold Ellucian Integration Hub Configuration.
 *
 */
class IntegrationHubConfig {

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
     * URL for the hub error reporting service
     */
    String errorUrl

    /**
     * Media type for messages getting published and retrieved (i.e. application/vnd.hedtech.change-notifications.v2+json)
     */
    String hubMediaType

    /**
     * if Banner environment is MEP enabled
     */
    boolean isMepEnvironment

    /**
     * Different combinations of vpdiCode - apiKey in Banner MEP environment
     */
    def mepApiKeyMappings = []

}
