/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.system.ldm.v6

/**
 *  Decorator for EEDM "persons" v6.
 */
class PersonV6 {

    String guid
    def privacyStatus = [:]
    List<NameV6> names = []
    CitizenshipStatusV6 citizenshipStatus = [:]
    VisaStatusV6 visaStatus

}
