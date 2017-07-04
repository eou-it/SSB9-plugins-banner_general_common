/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.system

import net.hedtech.banner.query.DynamicFinder
import net.hedtech.banner.query.operators.Operators
import net.hedtech.banner.service.ServiceBase
import net.hedtech.banner.exceptions.ApplicationException

class EducationalInstitutionViewService extends ServiceBase {

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
        throw new ApplicationException(EducationalInstitutionView, "@@r1:unsupported.operation@@")
    }

    List<EducationalInstitutionView> fetchAllByCriteria(Map content, String sortField = null, String sortOrder = null, int max = 0, int offset = -1) {
        def params = [:]
        def criteria = []
        def pagingAndSortParams = [:]

        buildCriteria(content, params, criteria)

        if (sortField) {
            pagingAndSortParams.sortColumn = sortField.trim()
            if (["asc", "desc"].contains(sortOrder?.trim()?.toLowerCase())) {
                pagingAndSortParams.sortDirection = sortOrder.trim()
            }
        }

        if (max > 0) {
            pagingAndSortParams.max = max
        }
        if (offset > -1) {
            pagingAndSortParams.offset = offset
        }

        return finderByAll().find([params: params, criteria: criteria], pagingAndSortParams)
    }

    def countByCriteria(Map content) {
        def params = [:]
        def criteria = []

        buildCriteria(content, params, criteria)
        return finderByAll().count([params: params, criteria: criteria])
    }


    private void buildCriteria(Map content, def params, def criteria) {
        if (content.type) {
            params.put("type", content.type?.trim())
            criteria.add([key: "type", binding: "type", operator: Operators.EQUALS])
        }
    }

    def private finderByAll = {
        def query = """FROM EducationalInstitutionView a"""
        return new DynamicFinder(EducationalInstitutionView.class, query, "a")
    }

}
