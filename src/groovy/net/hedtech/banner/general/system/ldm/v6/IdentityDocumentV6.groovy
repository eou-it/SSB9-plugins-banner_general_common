/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.system.ldm.v6

import sun.applet.resources.MsgAppletViewer

/**
 * Decorator used in  "persons" V6
 *
 */
class IdentityDocumentV6 {

    String countryCode
    String documentId
    String issuingAuthority
    String expiresOn

    Map getType(){
        return [category:'passport']
    }
    Map getCountry(){
        return [code:countryCode]
    }
}
