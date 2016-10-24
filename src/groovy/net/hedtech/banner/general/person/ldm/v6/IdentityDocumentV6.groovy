/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm.v6

/**
 * Decorator used in  "persons" V6
 *
 */
class IdentityDocumentV6 {

    String countryCode
    String documentId
    String expiresOn

    Map getType() {
        return [category: 'passport']
    }

    Map getCountry() {
        return [code: countryCode]
    }
}
