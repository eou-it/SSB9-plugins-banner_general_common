/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm.v6

import net.hedtech.banner.general.person.PersonIdentificationNameCurrent
import net.hedtech.banner.general.person.ldm.BasePersonBuilder
import net.hedtech.banner.general.system.ldm.v6.Person

class PersonBulider extends BasePersonBuilder {

    @Override
    Map build(List personList, Boolean studentRole) {
        Map persons = [:]
        List pidms = []
        personList?.each {
            pidms << it.pidm
            persons.put(it.pidm, null) //Preserve list order.
        }
        if (pidms.size() < 1) {
            return persons
        }

        List domainIds = []
        personList.each {
            Person currentRecord = persons.get(it.pidm) ?: new Person(null)
            PersonIdentificationNameCurrent identificationNameCurrent = PersonIdentificationNameCurrent.fetchByPidm(it.pidm)
            domainIds << identificationNameCurrent.id
            persons.put(it.pidm, currentRecord)
        }

        persons = buildPersonGuids(domainIds, persons)

        // Map of person objects with pidm as index.
        return persons
    }
}
