/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.overall

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.service.ServiceBase

class CampusOrganizationsViewService extends ServiceBase {

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

    public List<CampusOrganizationsView> fetchAll(int max = 0, int offset = -1) {
        return CampusOrganizationsView.withSession { session ->
            def query = session.createQuery('''from CampusOrganizationsView''')
            if (max > 0) {
                query.setMaxResults(max)
            }
            if (offset > -1) {
                query.setFirstResult(offset)
            }
            query.list()
        }
    }

    public CampusOrganizationsView fetchByGuid(String guid) {
        CampusOrganizationsView.fetchByGuid(guid)
    }
}
