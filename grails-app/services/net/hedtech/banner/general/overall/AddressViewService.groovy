/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.overall

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.service.ServiceBase

class AddressViewService extends ServiceBase {

    boolean transactional = true

    def preCreate(map) {
        throwUnsupportedException()
    }

    def preUpdate(map) {
        throwUnsupportedException()
    }

    def preDelete(map){
        throwUnsupportedException()
    }

    def throwUnsupportedException() {
        throw new ApplicationException(AddressView, "@@r1:unsupported.operation@@")
    }

    public static List<AddressView> fetchAll(int max=0, int offset=-1){
       return AddressView.withSession { session ->
           def query = session.createQuery('''from AddressView''')
           if (max > 0) {
               query.setMaxResults(max)
           }
           if (offset > -1) {
               query.setFirstResult(offset)
           }
       query.list()
        }
    }
}
