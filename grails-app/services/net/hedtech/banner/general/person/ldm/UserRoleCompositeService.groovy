/*******************************************************************************
 Copyright 2014-2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm

import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.person.PersonIdentificationNameCurrent
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
        String order = params.sortAndPaging?.order ? params.sortAndPaging?.order.trim() : "asc"
        if (params.sortAndPaging?.sort == "firstName") {
            sortField = "a.spriden_first_name"
        } else {
            sortField = "a.spriden_last_name"
        }
        def orderByString = " order by " + sortField + " " + order

        def offset = params.offset ? params.offset.trim()?.toInteger() : 0

        def max = params.max? params.max.trim()?.toInteger() + offset : 500

        switch (params.role.toLowerCase()) {
            case 'faculty':
                def sql
                try {
                    sql = new Sql(sessionFactory.getCurrentSession().connection())
                    if (count) {
                        def countQuery = "select count(a.spriden_pidm) from spriden a," +
                                " svq_sibinst_access b" +
                                " where a.spriden_pidm = b.sibinst_pidm" +
                                " and b.stvfcst_active_ind = 'A'" +
                                " and b.sibinst_schd_ind = 'Y'" +
                                " and b.sibinst_term_code_eff = ( select min(c.sibinst_term_code_eff)" +
                                "                       from svq_sibinst_access c, stvterm e" +
                                "                       where c.sibinst_pidm = b.sibinst_pidm" +
                                "                       and c.end_term = e.stvterm_code" +
                                "                       and c.stvfcst_active_ind = 'A'" +
                                "                       and c.sibinst_schd_ind = 'Y'" +
                                "                       and sysdate < e.stvterm_end_date)" +
                                " and a.spriden_change_ind is null"

                        results = sql.firstRow(countQuery)[0]
                    } else {
                        def query = "select * from " +
                                " (select pidms.*, rownum rn from" +
                                " (select a.spriden_pidm from spriden a," +
                                " svq_sibinst_access b" +
                                " where a.spriden_pidm = b.sibinst_pidm" +
                                " and b.stvfcst_active_ind = 'A'" +
                                " and b.sibinst_schd_ind = 'Y'" +
                                " and b.sibinst_term_code_eff = ( select min(c.sibinst_term_code_eff)" +
                                "                       from svq_sibinst_access c, stvterm e" +
                                "                       where c.sibinst_pidm = b.sibinst_pidm" +
                                "                       and c.end_term = e.stvterm_code" +
                                "                       and c.stvfcst_active_ind = 'A'" +
                                "                       and c.sibinst_schd_ind = 'Y'" +
                                "                       and sysdate < e.stvterm_end_date)" +
                                " and a.spriden_change_ind is null" +
                                orderByString +
                                " ) pidms " +
                                " where rownum <= $max) " +
                                " where rn > $offset"
                        results = sql.rows(query)?.collect { it.spriden_pidm?.toInteger() }
                    }
                }
                catch (ClassNotFoundException e) {
                    log.debug "Student faculty plugin not present, unable to process Faculty roles $e"
                }
                catch (ApplicationException ae) {
                    log.debug "Student faculty list application exception $ae"
                }
                catch (Exception ae) {
                    log.debug "Student faculty list exception $ae"
                }
                finally {
                    sql.close()
                }

                break

            case 'student':
                def sql
                try {
                    sql = new Sql(sessionFactory.getCurrentSession().connection())
                    if (count) {
                        def countQuery = "select count(a.spriden_pidm) from spriden a where exists" +
                                "  (select 1 from sgbstdn b" +
                                "  where a.spriden_pidm = b.sgbstdn_pidm)" +
                                "  and a.spriden_change_ind is null"

                        results = sql.firstRow(countQuery)[0]
                    } else {
                        def query = "select * from " +
                                " (select pidms.*, rownum rn from" +
                                " (select a.spriden_pidm from spriden a where exists" +
                                "  (select 1 from sgbstdn b" +
                                "  where a.spriden_pidm = b.sgbstdn_pidm)" +
                                "  and a.spriden_change_ind is null" +
                                orderByString +
                                " ) pidms " +
                                " where rownum <= $max) " +
                                " where rn > $offset"
                        results = sql.rows(query)?.collect { it.spriden_pidm?.toInteger() }
                    }
                } catch (SQLException e) {
                    log.debug "Student Person sql exception not present, unable to process student roles $e"
                }
                catch (ApplicationException ae) {
                    log.debug "Student Person list application exception $ae"
                }
                catch (Exception ae) {
                    log.debug "Student Person list exception $ae"
                }
                finally {
                    sql.close()
                }

                break
        }

        results
    }


    Map<Integer, List<RoleDetail>> fetchAllRolesByPidmInList(List pidms, Boolean studentRole) {
        def results = [:]
        if (pidms.size()) {
            def sql
            try {
                sql = new Sql(sessionFactory.getCurrentSession().connection())
                def query = "select a.spriden_pidm, d.stvterm_start_date, f.stvterm_end_date from spriden a," +
                        " svq_sibinst_access b, stvterm d, stvterm f" +
                        " where a.spriden_pidm = b.sibinst_pidm" +
                        " and d.stvterm_code = b.sibinst_term_code_eff " +
                        " and f.stvterm_code = b.end_term" +
                        " and b.stvfcst_active_ind = 'A'" +
                        " and b.sibinst_schd_ind = 'Y'" +
                        " and b.sibinst_term_code_eff = ( select min(c.sibinst_term_code_eff)" +
                        "                       from svq_sibinst_access c, stvterm e" +
                        "                       where c.sibinst_pidm = b.sibinst_pidm" +
                        "                       and c.end_term = e.stvterm_code" +
                        "                       and c.stvfcst_active_ind = 'A'" +
                        "                       and c.sibinst_schd_ind = 'Y'" +
                        "                       and sysdate < e.stvterm_end_date) " +
                        " and a.spriden_change_ind is null" +
                        " and b.sibinst_pidm in (" + pidms.join(',') + ")"
                sql.rows(query).each { faculty ->
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
                log.debug "Student Faculty sql exception not present, unable to process Faculty roles $e"
            }
            catch (ApplicationException ae) {
                log.debug "Student Faculty list application exception $ae"
            }
            catch (Exception ae) {
                log.debug "Student Faculty list exception $ae"
            }
            finally {
                sql.close()
            }
            try {
                if (!studentRole) {
                    sql = new Sql(sessionFactory.getCurrentSession().connection())
                    def query = "select a.spriden_pidm from spriden a where exists" +
                            " (select 1 from sgbstdn b" +
                            " where a.spriden_pidm = b.sgbstdn_pidm)" +
                            " and a.spriden_change_ind is null " +
                            " and a.spriden_pidm in (" + pidms.join(',') + ")"
                    pidms = sql.rows(query)?.collect { it.spriden_pidm?.toInteger() }
                }
                pidms?.each { it ->
                    def roles = results.get(it) ?: []
                    def newRole = new RoleDetail()
                    newRole.role = 'Student'
                    roles << newRole
                    results.put(it, roles)
                }
            }
            catch (SQLException e) {
                log.debug "Person Student sql exception not present, unable to process Faculty roles $e"
            }
            catch (ApplicationException ae) {
                log.debug "Person Student list application exception $ae"
            }
            catch (Exception ae) {
                log.debug "Person Student list exception $ae"
            }
            finally {
                sql.close()
            }
        }

        results
    }

}
