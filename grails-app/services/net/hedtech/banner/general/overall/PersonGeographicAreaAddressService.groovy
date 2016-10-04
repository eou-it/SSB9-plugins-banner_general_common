/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.overall

import net.hedtech.banner.service.ServiceBase

class PersonGeographicAreaAddressService extends ServiceBase {

    boolean transactional = true

    List<PersonGeographicAreaAddress> fetchActivePersonGeographicAreaAddress(Integer pidm) {
        List entities = PersonGeographicAreaAddress.withSession { session ->
            session.getNamedQuery('PersonGeographicAreaAddress.fetchActivePersonGeographicAreaAddress')
                    .setParameterList('pidm', pidm)
                    .list()
        }
        return entities
    }
}
