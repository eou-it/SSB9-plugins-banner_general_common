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

    def preDelete(map) {
        throwUnsupportedException()
    }

    def throwUnsupportedException() {
        throw new ApplicationException(AddressView, "@@r1:unsupported.operation@@")
    }

    public List<AddressView> fetchAll(int max = 0, int offset = -1) {
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

    AddressView fetchByGuid(String guid) {
        AddressView addressView
        if (guid) {
            AddressView.withSession {
                session ->
                    def query = session.getNamedQuery('AddressView.fetchByGuid')
                    query.setString('guid', guid)
                    addressView = query.uniqueResult()
            }
        }
        return addressView
    }

    def fetchAllByGuidsAndAddressTypeCodes(Collection<String> guids, Collection<String> addressTypeCodes) {
        List entities = []
        if (guids && addressTypeCodes) {
            AddressView.withSession { session ->
                def query = session.createQuery('''from AddressView a where a.id in (:guids) and a.addressTypeCode in (:addressTypeCodes)''')
                query.with {
                    setParameterList('guids', guids.unique())
                    setParameterList('addressTypeCodes', addressTypeCodes.unique())
                    entities = list()
                }
            }
        }
        return entities
    }

}
