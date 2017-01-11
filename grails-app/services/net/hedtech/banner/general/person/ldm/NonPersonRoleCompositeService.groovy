/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm

import net.hedtech.banner.general.overall.ldm.LdmService
import net.hedtech.banner.general.system.InstitutionalDescription

class NonPersonRoleCompositeService {

    def sessionFactory

    /**
     * Fetch vendors from DB
     *
     * @param max maximum number of rows to retrieve
     * @param offset first row to retrieve (numbered from 0)
     */
    public def fetchVendors(int max, int offset) {
        List<Integer> pidms = []
        def totalCount = 0

        if (isFinanceInstalled()) {
            try {
                // Query for PIDMs
                def sql = getSQLforFetchingVendors()
                def results = executeNativeSQL(sql, max, offset)
                pidms = results.collect {
                    if (it instanceof BigDecimal) it.toInteger()
                    else it[0].toInteger()
                }
                // Query for total count
                sql = getSQLforFetchingVendors(true)
                totalCount = executeNativeSQL(sql, 0, 0, true)
            } catch (Exception ex) {
                log.error ex
            }
        }

        return [pidms: pidms, totalCount: totalCount]
    }


    public def fetchVendorsByPIDMs(List<Integer> pidms) {
        def results
        if (isFinanceInstalled() && pidms) {
            try {
                def sql = getSQLforFetchingVendorsByPIDMs()
                results = executeNativeSQL(sql, pidms)
            } catch (Exception ex) {
                log.error ex
            }
        }
        return results
    }

    private String getSQLforFetchingVendors(boolean count = false) {
        String sql = """ select """
        if (count) {
            sql += """  count(a.spriden_pidm) """
        } else {
            sql += """ a.spriden_pidm  """
        }
        sql += """ from spriden a, FTVVEND b
                   where a.spriden_pidm = b.FTVVEND_PIDM
                   and a.spriden_change_ind is null
                   and a.spriden_entity_ind = 'C' """

        return sql.replace("\n", "").replaceAll(/  */, " ")
    }


    private String getSQLforFetchingVendorsByPIDMs() {
        def sql = """ select  a.spriden_pidm as pidm,b.FTVVEND_EFF_DATE ,b.FTVVEND_TERM_DATE
                      from spriden a, FTVVEND b
                      where a.spriden_pidm = b.FTVVEND_PIDM
                      and a.spriden_entity_ind = 'C'
                      and a.spriden_change_ind is null
                      and a.spriden_pidm in (:pidms) """
        return sql.replace("\n", "").replaceAll(/  */, " ")
    }

    private def executeNativeSQL(String nativeSql, int max, int offset, boolean count = false) {
        def session = sessionFactory.getCurrentSession()
        def sqlQuery = session.createSQLQuery(nativeSql)
        log.debug "Executing native SQL..."
        log.debug nativeSql
        def results = sqlQuery.with {
            if (count) {
                uniqueResult()
            } else {
                if (max > 0) {
                    setMaxResults(max)
                }
                if (offset > -1) {
                    setFirstResult(offset)
                }
                list()
            }
        }
        log.debug "Executed native SQL successfully"
        return results
    }

    private def executeNativeSQL(String nativeSql, List<Integer> pidms) {
        def session = sessionFactory.getCurrentSession()
        def sqlQuery = session.createSQLQuery(nativeSql)
        log.debug "Executing native SQL..."
        log.debug nativeSql
        def results = sqlQuery.with {
            setParameterList("pidms", pidms)
            list()
        }
        log.debug "Executed native SQL successfully"
        return results
    }

    private Boolean isFinanceInstalled() {
        Boolean installed = false
        InstitutionalDescription instDesc = InstitutionalDescription.fetchByKey()
        if (instDesc) {
            installed = instDesc.financeInstalled
        }
        return installed
    }
}
