/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.ldm.person

import net.hedtech.banner.general.person.PersonBasicPersonBase
import net.hedtech.banner.general.person.PersonIdentificationNameCurrent

/**
 * LDM Decorator for person resource.
 */
class PersonV1 {
    @Delegate private final PersonBasicPersonBase person
    @Delegate private final PersonIdentificationNameCurrent personIdentificationName
    String guid
    List<String> races = []
    List credentials = []
    List addresses = []
    List phones = []
    List emails = []

    def PersonV1(PersonBasicPersonBase person,
             PersonIdentificationNameCurrent personIdentificationName,
             String guid,
             List races,
             List credentials,
             List addresses,
             List phones,
             List emails) {
        this.person = person ?: new PersonBasicPersonBase() // PersonBasicPersonBase is optional, create blank object if none exists.
        this.personIdentificationName = personIdentificationName
        this.guid = guid
        this.races = races
        this.credentials = credentials
        this.addresses = addresses
        this.phones = phones
        this.emails = emails

    }

    def getMaritalStatus() {
        // TODO: support enumeration
        this.person?.maritalStatus?.description
    }

    def getEthnicity() {
        // TODO: remove hardcoding? Possibly use ethnic field.
        this.person?.ethnicity?.ethnic == "1" ? "Non-Hispanic" : ( this.person?.ethnicity?.ethnic == "2" ? "Hispanic" : null)
    }

    def getSex() {
        this.person?.sex == 'M' ? "Male":(this.person?.sex == 'F' ? "Female" : "Unknown")
    }
}
