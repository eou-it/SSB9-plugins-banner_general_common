/*********************************************************************************
 Copyright 2009-2013 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.overall

/**
 * Decorator for the about service
 */
class AboutDecorator {

    def applicationName
    def applicationVersion

    //Ethos Standard property names
    def name
    def version

    AboutDecorator( Map<String,Object> about) {

        this.applicationName = about.applicationName
        this.applicationVersion = about.applicationVersion

        //Included new headers to meet Ethos standards
        //The old headers will be removed in a later release
        this.name = about.applicationName
        this.version = about.applicationVersion
    }


}
