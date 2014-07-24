/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm.v1

import net.hedtech.banner.general.person.PersonBasicPersonBase
import net.hedtech.banner.general.person.PersonIdentificationNameCurrent

/**
 * LDM Decorator for person resource.
 */
class Person {
    @Delegate private final PersonBasicPersonBase person
    String guid
    String maritalStatus
    List credentials = []
    List addresses = []
    List phones = []
    List emails = []
    List names = []

    def Person(PersonBasicPersonBase person,
             def guid,
             def credentials,
             def addresses,
             def phones,
             def emails,
             def names) {
        this.person = person ?: new PersonBasicPersonBase() // PersonBasicPersonBase is optional, create blank object if none exists.
        this.guid = guid instanceof String ? guid : ""
        this.credentials = credentials instanceof List ? credentials : null
        this.addresses = addresses instanceof List ? addresses : null
        this.phones = phones instanceof List ? phones : null
        this.emails = emails instanceof List ? emails : null
        this.names = names instanceof List ? names : null

    }

    def getMaritalStatus() {
        // TODO: support enumeration
        this.maritalStatus
    }

    def setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus
    }

    def getSex() {
        this.person?.sex == 'M' ? "Male":(this.person?.sex == 'F' ? "Female" : "Unknown")
    }

    def getEthnicity() {
        this.person?.ethnic == '1' ? "Non-Hispanic" :(this.person?.ethnic == '2' ? "Hispanic" : null)
    }
}
