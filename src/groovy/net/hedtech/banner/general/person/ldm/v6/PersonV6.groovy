/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm.v6

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.overall.ldm.v6.PersonCredential
import net.hedtech.banner.general.overall.ldm.v6.VisaStatusV6
import net.hedtech.banner.general.system.MaritalStatus
import net.hedtech.banner.general.system.ldm.v1.MaritalStatusDetail
import net.hedtech.banner.general.system.ldm.v6.CitizenshipStatusV6
import net.hedtech.banner.general.system.ldm.v6.EthnicityDecorator

/**
 *  Decorator for EEDM "persons" v6.
 */
@EqualsAndHashCode
@ToString(includeNames = true, includeFields = true)
class PersonV6 {

    String guid
    def privacyStatus
    List<NameV6> names
    CitizenshipStatusV6 citizenshipStatus
    def religion
    List<RoleV6> roles
    List<PersonCredential> credentials
    List<EmailV6> emails
    List<RaceV6> races
    EthnicityDecorator ethnicity
    List<PhoneV6> phones
    List interests
    List<PersonAddressDecorator> addresses
    def identityDocuments
    def languages
    Date dateOfBirth
    Date dateDeceased
    String gender
    MaritalStatusDetail maritialStatus
    String countryOfBirth
    String citizenshipCountry

    def getGender() {
        switch (gender) {
            case 'M':
                return 'Male'
            case 'F':
                return 'Female'
            case 'N':
                return 'Unknown'
            default:
                return gender
        }
    }

}
