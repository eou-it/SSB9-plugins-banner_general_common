/*********************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.overall

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.service.ServiceBase

class AddressViewGuidService extends ServiceBase {

    boolean transactional = true

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
        throw new ApplicationException(AddressViewGuid, "@@r1:unsupported.operation@@")
    }

    public List<AddressViewGuid> fetchAll(int max = 0, int offset = -1) {
        return AddressViewGuid.withSession { session ->
            def orderBy = " order by id"
            def query = session.createQuery('''from AddressViewGuid''' + orderBy)
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
