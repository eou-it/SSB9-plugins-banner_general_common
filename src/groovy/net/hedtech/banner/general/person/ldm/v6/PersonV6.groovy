/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm.v6

import net.hedtech.banner.general.overall.ldm.v6.PersonCredential
import net.hedtech.banner.general.overall.ldm.v6.VisaStatusV6
import net.hedtech.banner.general.system.ldm.v6.CitizenshipStatusV6
import net.hedtech.banner.general.system.ldm.v6.EmailV6
import net.hedtech.banner.general.system.ldm.v6.RaceV6

/**
 *  Decorator for EEDM "persons" v6.
 */
class PersonV6 {

    String guid
    def privacyStatus
    List<NameV6> names
    CitizenshipStatusV6 citizenshipStatus
    VisaStatusV6 visaStatus
    def religion
    List<RoleV6> roles
    List<PersonCredential> credentials
    List<EmailV6> emails
    List<RaceV6> races


}
