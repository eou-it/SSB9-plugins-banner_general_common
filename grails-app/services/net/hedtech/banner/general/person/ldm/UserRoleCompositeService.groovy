/*******************************************************************************
 Copyright 2014-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.common.GeneralCommonConstants
import net.hedtech.banner.general.overall.ldm.LdmService
import net.hedtech.banner.general.person.ldm.v1.RoleDetail
import net.hedtech.banner.general.system.InstitutionalDescription
import net.hedtech.banner.general.utility.DateConvertHelperService

import java.sql.SQLException

class UserRoleCompositeService extends LdmService {

    def sessionFactory
    private static final List<String> VERSIONS = [GeneralCommonConstants.VERSION_V1]

/**
 *
 * @param [role :"Student|Faculty",sortAndPaging[sort:"firstName|lastName",...etc]
 * @return List of ordered pidms [12345,12344]
 * We are using executeQuery for performance reasons, only returning
 * what we really need, and using our ability to inner join un-related domains in executeQuery.
 */
    @Deprecated
    def fetchAllByRole(Map params) {
        def pidms = []
        def countResult = 0

        def max = params.max ? params.max.trim()?.toInteger() : 500
        def offset = params.offset ? params.offset.trim()?.toInteger() : 0

        switch (params.role.toLowerCase()) {
            case 'faculty':
                def resultMap = fetchFaculties(params.sort?.trim(), params.order?.trim(), max, offset)
                pidms = resultMap.pidms
                countResult = resultMap.totalCount
                break

            case 'student':
                def resultMap = fetchStudents(params.sort?.trim(), params.order?.trim(), max, offset)
                pidms = resultMap.pidms
                countResult = resultMap.totalCount
                break
        }

        return [pidms: pidms, count: countResult]
    }


    @Deprecated
    Map<Integer, List<RoleDetail>> fetchAllRolesByPidmInList(List pidms, Boolean studentRole) {
        def results = [:]
        String version = LdmService.getAcceptVersion(VERSIONS)
        def institution = InstitutionalDescription.fetchByKey()
        if (pidms.size()) {
            if (institution.studentInstalled) {
                try {
                    def nativeSql = getSQLforFetchingFacultiesByPIDMs()
                    def rows = executeNativeSQL(nativeSql, pidms)

                    rows.each { faculty ->
                        def roles = results.get(faculty[0].toInteger()) ?: []
                        def newRole = new RoleDetail()
                        newRole.role = 'Faculty'
                        newRole.effectiveStartDate = GeneralCommonConstants.VERSION_V4.equalsIgnoreCase(version) ? DateConvertHelperService.convertDateIntoUTCFormat(faculty[1]) : faculty[1]
                        newRole.effectiveEndDate = GeneralCommonConstants.VERSION_V4.equalsIgnoreCase(version) ? DateConvertHelperService.convertDateIntoUTCFormat(faculty[2]) : faculty[2]
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
                        def nativeSql = getSQLforFetchingStudentsByPIDMs()
                        def rows = executeNativeSQL(nativeSql, pidms)

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
            def roles = results.get(it.toInteger()) ?: []
            def newRole = new RoleDetail()
            newRole.role = 'Student'
            roles << newRole
            results.put(it.toInteger(), roles)
        }
        return results
    }

    /**
     * Fetch faculties from DB
     *
     * @param sortField "firstName" or "lastName"
     * @param sortOrder "asc" or "desc"
     * @param max maximum number of rows to retrieve
     * @param offset first row to retrieve (numbered from 0)
     */
    public def fetchFaculties(String sortField, String sortOrder, int max, int offset) {
        List<Integer> pidms = []
        def totalCount = 0

        if (sortField?.trim() == "firstName") {
            sortField = "a.spriden_first_name"
        } else {
            sortField = "a.spriden_last_name"
        }
        if (!["asc", "desc"].contains(sortOrder?.trim()?.toLowerCase())) {
            sortOrder = "asc"
        }

        if (isStudentInstalled()) {
            try {
                // Query for PIDMs
                def sql = getSQLforFetchingFaculties(sortField, sortOrder)
                def results = executeNativeSQL(sql, max, offset)
                pidms = results.collect {
                    if (it instanceof BigDecimal) it.toInteger()
                    else it[0].toInteger()
                }
                // Query for total count
                sql = getSQLforFetchingFaculties(null, null, true)
                totalCount = executeNativeSQL(sql, 0, 0, true)
            } catch (Exception ex) {
                log.error ex
            }
        }

        return [pidms: pidms, totalCount: totalCount]
    }


    public def fetchFacultiesByPIDMs(List<Integer> pidms) {
        def results
        if (isStudentInstalled() && pidms) {
            try {
                def sql = getSQLforFetchingFacultiesByPIDMs()
                results = executeNativeSQL(sql, pidms)
            } catch (Exception ex) {
                log.error ex
            }
        }
        return results
    }

    /**
     * Fetch students from DB
     *
     * @param sortField "firstName" or "lastName"
     * @param sortOrder "asc" or "desc"
     * @param max maximum number of rows to retrieve
     * @param offset first row to retrieve (numbered from 0)
     */
    public def fetchStudents(String sortField, String sortOrder, int max, int offset) {
        List<Integer> pidms = []
        def totalCount = 0

        if (sortField?.trim() == "firstName") {
            sortField = "a.spriden_first_name"
        } else {
            sortField = "a.spriden_last_name"
        }
        if (!["asc", "desc"].contains(sortOrder?.trim()?.toLowerCase())) {
            sortOrder = "asc"
        }

        if (isStudentInstalled()) {
            try {
                // Query for PIDMs
                def sql = getSQLforFetchingStudents(sortField, sortOrder)
                def results = executeNativeSQL(sql, max, offset)
                pidms = results.collect {
                    if (it instanceof BigDecimal) it.toInteger()
                    else it[0].toInteger()
                }
                // Query for total count
                sql = getSQLforFetchingStudents(null, null, true)
                totalCount = executeNativeSQL(sql, 0, 0, true)
            } catch (Exception ex) {
                log.error ex
            }
        }

        return [pidms: pidms, totalCount: totalCount]
    }


    /**
     * Fetch students from DB
     *
     * @param sortField "firstName" or "lastName"
     * @param sortOrder "asc" or "desc"
     * @param max maximum number of rows to retrieve
     * @param offset first row to retrieve (numbered from 0)
     */
    public def fetchProspectiveStudent(String sortField, String sortOrder, int max, int offset) {
        List<Integer> pidms = []
        def totalCount = 0

        if (sortField?.trim() == "firstName") {
            sortField = "a.spriden_first_name"
        } else {
            sortField = "a.spriden_last_name"
        }
        if (!["asc", "desc"].contains(sortOrder?.trim()?.toLowerCase())) {
            sortOrder = "asc"
        }

        if (isStudentInstalled()) {
            try {
                // Query for PIDMs
                def sql = getSQLforFetchingStudents(sortField, sortOrder)
                def results = executeNativeSQL(sql, max, offset)
                pidms = results.collect {
                    if (it instanceof BigDecimal) it.toInteger()
                    else it[0].toInteger()
                }
                // Query for total count
                sql = getSQLforFetchingStudents(null, null, true)
                totalCount = executeNativeSQL(sql, 0, 0, true)
            } catch (Exception ex) {
                log.error ex
            }
        }

        return [pidms: pidms, totalCount: totalCount]
    }



    public def fetchStudentsByPIDMs(List<Integer> pidms) {
        def results
        if (isStudentInstalled() && pidms) {
            try {
                def sql = getSQLforFetchingStudentsByPIDMs()
                results = executeNativeSQL(sql, pidms)
            } catch (Exception ex) {
                log.error ex
            }
        }
        return results
    }


    private Boolean isStudentInstalled() {
        Boolean installed = false
        InstitutionalDescription instDesc = InstitutionalDescription.fetchByKey()
        if (instDesc) {
            installed = instDesc.studentInstalled
        }
        return installed
    }


    private String getSQLforFetchingFaculties(String sortField, String sortOrder, boolean count = false) {
        String sql = """ select """
        if (count) {
            sql += """  count(a.spriden_pidm) """
        } else {
            sql += """ a.spriden_pidm  """
        }
        sql += """ from spriden a, svq_sibinst_access b
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
                   and a.spriden_entity_ind = 'P' """
        if (!count) {
            sql += """ order by $sortField $sortOrder, a.spriden_id $sortOrder """
        }
        return sql.replace("\n", "").replaceAll(/  */, " ")
    }


    private String getSQLforFetchingStudents(String sortField, String sortOrder, boolean count = false) {
        String sql = """ select """
        if (count) {
            sql += """  count(a.spriden_pidm) """
        } else {
            sql += """ a.spriden_pidm  """
        }
        sql += """ from spriden a
                   where exists (select 1 from sgbstdn b where b.sgbstdn_pidm = a.spriden_pidm)
                   and a.spriden_change_ind is null
                   and a.spriden_entity_ind = 'P' """
        if (!count) {
            sql += """ order by $sortField $sortOrder, a.spriden_id $sortOrder """
        }
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


    private String getSQLforFetchingFacultiesByPIDMs() {
        def sql = """ select a.spriden_pidm, d.stvterm_start_date, f.stvterm_end_date
                      from spriden a, svq_sibinst_access b, stvterm d, stvterm f
                      where a.spriden_pidm = b.sibinst_pidm
                      and d.stvterm_code = b.sibinst_term_code_eff
                      and f.stvterm_code = b.end_term
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
                      and a.spriden_entity_ind = 'P'
                      and b.sibinst_pidm in (:pidms) """
        return sql.replace("\n", "").replaceAll(/  */, " ")
    }


    private String getSQLforFetchingStudentsByPIDMs() {
        def sql = """ select a.spriden_pidm
                      from spriden a
                      where exists (select 1 from sgbstdn b where b.sgbstdn_pidm = a.spriden_pidm)
                      and a.spriden_change_ind is null
                      and a.spriden_entity_ind = 'P'
                      and a.spriden_pidm in (:pidms) """
        return sql.replace("\n", "").replaceAll(/  */, " ")
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

}
