/*******************************************************************************
 Copyright 2014-2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.person.ldm.v1.RoleDetail
import net.hedtech.banner.general.system.InstitutionalDescription
import net.hedtech.banner.general.overall.ldm.LdmService

import java.sql.SQLException

class UserRoleCompositeService extends LdmService{
    def sessionFactory
    def dateConvertHelperService
    private static final List<String> VERSIONS = ["v1","v4"]

/**
 *
 * @param [role :"Student|Faculty",sortAndPaging[sort:"firstName|lastName",...etc]
 * @return List of ordered pidms [12345,12344]
 * We are using executeQuery for performance reasons, only returning
 * what we really need, and using our ability to inner join un-related domains in executeQuery.
 */
    def fetchAllByRole(Map params) {
        def pidms = []
        def countResult = 0
        String sortField
        String order
        def institution = InstitutionalDescription.fetchByKey()
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
        def orderByString = " order by " + sortField + " " + order + ", a.spriden_id" + " " + order

        def offset = params.offset ? params.offset.trim()?.toInteger() : 0

        def max = params.max ? params.max.trim()?.toInteger() : 500
        def connection
        switch (params.role.toLowerCase()) {
            case 'faculty':
                if (institution.studentInstalled) {
                    try {
                        connection = sessionFactory.getCurrentSession()
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
                        def querySqlFlattenCount = countQuery.replace("\n", "").replaceAll(/  */, " ")
                        def queryCount = connection.createSQLQuery(querySqlFlattenCount)
                        countResult = queryCount.with {
                            uniqueResult()
                        }
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
                        pidms = rows.collect {
                            if (it instanceof BigDecimal) it.toInteger()
                            else it[0].toInteger()
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
                }
                break

            case 'student':
                if (institution.studentInstalled) {
                    try {
                        connection = sessionFactory.getCurrentSession()
                        def countQuery = """select count(a.spriden_pidm) from spriden a where exists
                                   (select 1 from sgbstdn b
                                   where a.spriden_pidm = b.sgbstdn_pidm)
                                   and a.spriden_change_ind is null"""
                        def querySqlFlattenCount = countQuery.replace("\n", "").replaceAll(/  */, " ")
                        def queryCount = connection.createSQLQuery(querySqlFlattenCount)
                        countResult = queryCount.with {
                            uniqueResult()
                        }
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
                        pidms = rows.collect {
                            if (it instanceof BigDecimal) it.toInteger()
                            else it[0].toInteger()
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
                }
                break
        }

        return [pidms : pidms, count: countResult]
    }


    Map<Integer, List<RoleDetail>> fetchAllRolesByPidmInList(List pidms, Boolean studentRole) {
        def results = [:]
        def timeZone = "v4".equalsIgnoreCase(LdmService.getAcceptVersion(VERSIONS))? dateConvertHelperService.getDBTimeZone() : ''
        def institution = InstitutionalDescription.fetchByKey()
        if (pidms.size()) {
            def connection
            if (institution.studentInstalled) {
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
                        newRole.effectiveStartDate ="v4".equalsIgnoreCase(LdmService.getAcceptVersion(VERSIONS)) ? dateConvertHelperService.convertDateIntoUTCFormat(faculty[1],timeZone):faculty[1]
                        newRole.effectiveEndDate = "v4".equalsIgnoreCase(LdmService.getAcceptVersion(VERSIONS)) ? dateConvertHelperService.convertDateIntoUTCFormat(faculty[2],timeZone):faculty[2]
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


                if (!studentRole) {
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
                        results = setStudentRole(rows, results)
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

                } else {
                    results = setStudentRole(pidms, results)
                }
            }
        }

        results
    }


    private def setStudentRole(def pidms, def results) {
        pidms?.each { it ->
            def roles = results.get(it) ?: []
            def newRole = new RoleDetail()
            newRole.role = 'Student'
            roles << newRole
            results.put(it, roles)
        }
        return results
    }

}
