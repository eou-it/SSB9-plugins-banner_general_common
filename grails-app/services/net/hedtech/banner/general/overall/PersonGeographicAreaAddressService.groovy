/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.overall

import grails.gorm.transactions.Transactional
import net.hedtech.banner.service.ServiceBase

@Transactional
class PersonGeographicAreaAddressService extends ServiceBase {


    List<PersonGeographicAreaAddress> fetchActivePersonGeographicAreaAddress(Integer pidm) {
        List entities = PersonGeographicAreaAddress.withSession { session ->
            session.getNamedQuery('PersonGeographicAreaAddress.fetchActivePersonGeographicAreaAddress')
                    .setParameterList('pidm', pidm)
                    .list()
        }
        return entities
    }
}
