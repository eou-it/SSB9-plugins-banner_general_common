/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.query.DynamicFinder
import net.hedtech.banner.service.ServiceBase

public class PersonVisaService extends ServiceBase {
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
        throw new ApplicationException(PersonVisa, "@@r1:unsupported.operation@@")
    }

    List<PersonVisa> fetchByCriteria(Map params, List criteria, Map pagingAndSorting) {
        DynamicFinder df = getFinderForFetchAll()
        return df.find([params: params, criteria: criteria], pagingAndSorting)
    }

    private DynamicFinder getFinderForFetchAll() {
        return new DynamicFinder(PersonVisa.class, "FROM PersonVisa a", "a")
    }

    int countByCriteria(Map params, List criteria) {
        DynamicFinder df = getFinderForFetchAll()
        return df.count([params: params, criteria: criteria])
    }
}