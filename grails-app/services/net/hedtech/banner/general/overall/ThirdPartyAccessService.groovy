/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.overall

import grails.gorm.transactions.Transactional
import net.hedtech.banner.query.DynamicFinder
import net.hedtech.banner.query.operators.Operators
import net.hedtech.banner.service.ServiceBase

@Transactional
class ThirdPartyAccessService extends ServiceBase {

    def fetchAllByCriteria(Map content, int max = 0, int offset = -1) {

        def params = [:]
        def criteria = []
        def pagingAndSortParams = [:]

        buildCriteria(content, params, criteria)

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


    private void buildCriteria(Map content, LinkedHashMap params, ArrayList criteria) {

        if (content.externalUser) {
            params.put("externalUser", content.externalUser?.trim())
            criteria.add([key: "externalUser", binding: "externalUser", operator: Operators.CONTAINS])
        }

    }

    def private finderByAll = {
        def query = """FROM ThirdPartyAccess a"""
        return new DynamicFinder(ThirdPartyAccess.class, query, "a")
    }



}
