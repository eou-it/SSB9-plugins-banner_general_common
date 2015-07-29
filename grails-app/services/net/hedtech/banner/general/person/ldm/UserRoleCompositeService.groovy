/*******************************************************************************
 Copyright 2014-2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.person.ldm.v1.RoleDetail

import java.sql.SQLException

class UserRoleCompositeService {
    def sessionFactory

/**
 *
 * @param [role :"Student|Faculty",sortAndPaging[sort:"firstName|lastName",...etc]
 * @return List of ordered pidms [12345,12344]
 * We are using executeQuery for performance reasons, only returning
 * what we really need, and using our ability to inner join un-related domains in executeQuery.
 */
    def fetchAllByRole(Map params, boolean count = false) {
        def results = []
        String sortField
        String order
        if (params.order) {
            order = params.order.trim()
        } else {
            order = "asc"
        }

        if (params.sort?.trim() == "firstName") {
            sortField = "a.spriden_first_name"
        } else {
            sortField = "a.spriden_last_name"
        }
        def orderByString = " order by " + sortField + " " + order

        def offset = params.offset ? params.offset.trim()?.toInteger() : 0

        def max = params.max ? params.max.trim()?.toInteger() : 500
        def connection
        switch (params.role.toLowerCase()) {
            case 'faculty':
                def sql
                try {
                    connection = sessionFactory.getCurrentSession()
                    if (count) {
                        def countQuery = """select count(a.spriden_pidm) from spriden a,
                                  svq_sibinst_access b
                                  where a.spriden_pidm = b.sibinst_pidm
                                  and b.stvfcst_active_ind = 'A'
                                  and b.sibinst_schd_ind = 'Y'
                                  and b.sibinst_term_code_eff = ( select min(c.sibinst_term_code_eff)
                                                        from svq_sibinst_access c, stvterm e
                                                        where c.sibinst_pidm = b.sibinst_pidm
                                                        and c.end_term = e.stvterm_code
                                                        and c.stvfcst_active_ind = 'A'
                                                        and c.sibinst_schd_ind = 'Y'
                                                        and sysdate < e.stvterm_end_date)
                                  and a.spriden_change_ind is null"""
                        def querySqlFlatten = countQuery.replace("\n", "").replaceAll(/  */, " ")
                        def query = connection.createSQLQuery(querySqlFlatten)
                        def row = query.with {
                            uniqueResult()
                        }
                        results = row
                    } else {
                        def querySql = """select a.spriden_pidm from spriden a, svq_sibinst_access b
                                  where a.spriden_pidm = b.sibinst_pidm
                                  and b.stvfcst_active_ind = 'A'
                                  and b.sibinst_schd_ind = 'Y'
                                  and b.sibinst_term_code_eff = (select min(c.sibinst_term_code_eff)
                                                        from svq_sibinst_access c, stvterm e
                                                        where c.sibinst_pidm = b.sibinst_pidm
                                                        and c.end_term = e.stvterm_code
                                                        and c.stvfcst_active_ind = 'A'
                                                        and c.sibinst_schd_ind = 'Y'
                                                        and sysdate < e.stvterm_end_date)
                                  and a.spriden_change_ind is null
                                  $orderByString"""
                        def querySqlFlatten = querySql.replace("\n", "").replaceAll(/  */, " ")
                        def query = connection.createSQLQuery(querySqlFlatten)
                        def rows = query.with {
                            setMaxResults(max)
                            setFirstResult(offset)
                            list()
                        }
                        results = rows.collect {
                            if (it instanceof BigDecimal) it.toInteger()
                            else it[0].toInteger()
                        }
                    }
                } catch (SQLException e) {
                    log.error "Person faculty sql exception not present, unable to process faculty roles $e"
                }
                catch (ApplicationException ae) {
                    log.error "Person faculty list application exception $ae"
                }
                catch (Exception ae) {
                    log.error "Person faculty list exception $ae"
                }

                break

            case 'student':
                def sql
                try {
                    connection = sessionFactory.getCurrentSession()
                    if (count) {
                        def countQuery = """select count(a.spriden_pidm) from spriden a where exists
                                   (select 1 from sgbstdn b
                                   where a.spriden_pidm = b.sgbstdn_pidm)
                                   and a.spriden_change_ind is null"""
                        def querySqlFlatten = countQuery.replace("\n", "").replaceAll(/  */, " ")
                        def query = connection.createSQLQuery(querySqlFlatten)
                        def row = query.with {
                            uniqueResult()
                        }
                        results = row
                    } else {
                        def querySql = """select a.spriden_pidm pidm from spriden a where exists
                                         (select 1 from sgbstdn b
                                         where a.spriden_pidm = b.sgbstdn_pidm)
                                         and a.spriden_change_ind is null
                                         $orderByString"""
                        def querySqlFlatten = querySql.replace("\n", "").replaceAll(/  */, " ")
                        def query = connection.createSQLQuery(querySqlFlatten)
                        def rows = query.with {
                            setMaxResults(max)
                            setFirstResult(offset)
                            list()
                        }
                        results = rows.collect {
                            if (it instanceof BigDecimal) it.toInteger()
                            else it[0].toInteger()
                        }
                    }
                } catch (SQLException e) {
                    log.error "Person student sql exception not present, unable to process student roles $e"
                }
                catch (ApplicationException ae) {
                    log.error "Person student list application exception $ae"
                }
                catch (Exception ae) {
                    log.error "Person student list exception $ae"
                }

                break
        }

        results
    }


    Map<Integer, List<RoleDetail>> fetchAllRolesByPidmInList(List pidms, Boolean studentRole) {
        def results = [:]
        if (pidms.size()) {
            def connection
            try {
                connection = sessionFactory.getCurrentSession()
                def querySql = """select a.spriden_pidm, d.stvterm_start_date, f.stvterm_end_date from spriden a,
                          svq_sibinst_access b, stvterm d, stvterm f
                          where a.spriden_pidm = b.sibinst_pidm
                          and d.stvterm_code = b.sibinst_term_code_eff
                          and f.stvterm_code = b.end_term
                          and b.stvfcst_active_ind = 'A'
                          and b.sibinst_schd_ind = 'Y'
                          and b.sibinst_term_code_eff = ( select min(c.sibinst_term_code_eff)
                                                from svq_sibinst_access c, stvterm e
                                                where c.sibinst_pidm = b.sibinst_pidm
                                                and c.end_term = e.stvterm_code
                                                and c.stvfcst_active_ind = 'A'
                                                and c.sibinst_schd_ind = 'Y'
                                                and sysdate < e.stvterm_end_date)
                          and a.spriden_change_ind is null
                          and b.sibinst_pidm in (:pidms)"""
                def querySqlFlatten = querySql.replace("\n", "").replaceAll(/  */, " ")
                def query = connection.createSQLQuery(querySqlFlatten)
                def rows = query.with {
                    setParameterList("pidms", pidms)
                    list()
                }

                rows.each { faculty ->
                    def roles = results.get(faculty[0].toInteger()) ?: []
                    def newRole = new RoleDetail()
                    newRole.role = 'Faculty'
                    newRole.effectiveStartDate = faculty[1]
                    newRole.effectiveEndDate = faculty[2]
                    roles << newRole
                    results.put(faculty[0].toInteger(), roles)
                }
            }
            catch (SQLException e) {
                log.error "Person faculty sql exception not present, unable to process faculty roles $e"
            }
            catch (ApplicationException ae) {
                log.error "Person faculty list application exception $ae"
            }
            catch (Exception ae) {
                log.error "Person faculty list exception $ae"
            }


            if (studentRole) {
                try {

                    connection = sessionFactory.getCurrentSession()
                    def querySql = """select a.spriden_pidm from spriden a
                             where exists (select 1 from sgbstdn b
                                           where a.spriden_pidm = b.sgbstdn_pidm)
                              and a.spriden_change_ind is null
                              and a.spriden_pidm in (:pidms) """
                    def querySqlFlatten = querySql.replace("\n", "").replaceAll(/  */, " ")
                    def query = connection.createSQLQuery(querySqlFlatten)
                    def rows = query.with {
                        setParameterList("pidms", pidms)
                        list()
                    }
                    rows?.each { it ->
                        def roles = results.get(it) ?: []
                        def newRole = new RoleDetail()
                        newRole.role = 'Student'
                        roles << newRole
                        results.put(it, roles)
                    }

                }
                catch (SQLException e) {
                    log.error "Person student sql exception not present, unable to process student roles $e"
                }
                catch (ApplicationException ae) {
                    log.error "Person student list application exception $ae"
                }
                catch (Exception ae) {
                    log.error "Person student list exception $ae"
                }

            }
        }

        results
    }

}
