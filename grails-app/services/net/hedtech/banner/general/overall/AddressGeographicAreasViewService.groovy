/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.overall

import grails.gorm.transactions.Transactional
import net.hedtech.banner.exceptions.ApplicationException

@Transactional
class AddressGeographicAreasViewService {

    def preCreate(map) {
        throwUnsupportedException()
    }


    def preUpdate(map) {
        throwUnsupportedException()
    }


    def preDelete(map) {
        throwUnsupportedException()
    }


    def throwUnsupportedException() {
        throw new ApplicationException(AddressView, "@@r1:unsupported.operation@@")
    }


    public List<AddressGeographicAreasView> fetchAllByPidmOrCodeInList(List pidms) {
        List result = []
        if (pidms && (pidms.size() > 0)) {
            AddressGeographicAreasView.withSession { session ->
                result = session.getNamedQuery('AddressGeographicAreasView.fetchAllByPidmOrCodeInList')
                        .setParameterList('pidms', pidms).list()
            }
        }
        return result
    }

}
