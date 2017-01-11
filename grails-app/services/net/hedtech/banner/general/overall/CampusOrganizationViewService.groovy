/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.overall

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.service.ServiceBase

class CampusOrganizationViewService extends ServiceBase {

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

    public List<CampusOrganizationView> fetchAll(int max = 0, int offset = -1) {
        return CampusOrganizationView.withSession { session ->
            def query = session.createQuery('''from CampusOrganizationView''')
            if (max > 0) {
                query.setMaxResults(max)
            }
            if (offset > -1) {
                query.setFirstResult(offset)
            }
            query.list()
        }
    }


    CampusOrganizationView fetchByGuid(String guid) {
        CampusOrganizationView campusOrganizationsView
        if (guid) {
            CampusOrganizationView.withSession {
                session ->
                    def query = session.getNamedQuery('CampusOrganizationsView.fetchByGuid')
                    query.setString('guid', guid)
                    campusOrganizationsView = query.uniqueResult()
            }
        }
        return campusOrganizationsView
    }


    CampusOrganizationView fetchByCode(String code) {
        CampusOrganizationView campusOrganizationsView
        if (code) {
            CampusOrganizationView.withSession {
                session ->
                    def query = session.getNamedQuery('CampusOrganizationsView.fetchByCode')
                    query.setString('code', code)
                    campusOrganizationsView = query.uniqueResult()
            }
        }
        return campusOrganizationsView
    }

}
