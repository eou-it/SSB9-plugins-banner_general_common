/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.system.ldm.v6

import net.hedtech.banner.general.person.PersonBasicPersonBase

/**
 * EEDM Decorator for person resource v6.
 */
class Person {

    @Delegate private final PersonBasicPersonBase person
    String guid

    def Person(PersonBasicPersonBase person, String guid) {
        // PersonBasicPersonBase is optional, create blank object if none exists.
        this.person = person ?: new PersonBasicPersonBase()
        this.guid = guid instanceof String ? guid : ""
    }

    def Person(PersonBasicPersonBase person) {
        // PersonBasicPersonBase is optional, create blank unpersisted object if none exists.
        this.person = person ?: new PersonBasicPersonBase()
    }

}
