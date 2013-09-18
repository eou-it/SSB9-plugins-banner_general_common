package net.hedtech.banner.loginworkflow

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import net.hedtech.banner.security.BannerUser
import net.hedtech.banner.utility.DateUtility
import org.apache.log4j.Logger
import org.springframework.security.core.context.SecurityContextHolder

import java.sql.SQLException
import java.sql.Timestamp

/** *****************************************************************************
 © 2013 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
class SurveyFlow extends PostLoginWorkflow {

    def sessionFactory
    private final log = Logger.getLogger(getClass())

    @Override
    public boolean showPage(request) {
        def pidm = getPidm()
        def session = request.getSession()
        String isDone = session.getAttribute("surveydone")
        boolean pushSurvey = false

        if (isSurveyAvailableForUserAuthority() && isDone != "true") {
            // Survey is not yet taken.
            Map startAndEndDates = getStartAndEndDates()
            Timestamp surveyStartDate = startAndEndDates.startDate
            Timestamp today = new Timestamp(DateUtility.getTodayDate().getTime())
            Timestamp surveyEndDate = startAndEndDates.endDate ?: today

            // Survey start date is not null & Today is between Survey start and end dates
            if (surveyStartDate && (surveyStartDate <= today && today <= surveyEndDate)) {
                if (getSurveyConfirmedIndicator(pidm) != 'Y') {
                    pushSurvey = true
                }
            }
            return pushSurvey
        } else {
            // Do not show Survey page as Survey has already been taken.
            return pushSurvey
        }
    }

    public String getControllerUri() {
        return "/ssb/survey/survey"
    }

    public String getControllerName() {
        return "survey"
    }


    static def getPidm() {
        def user = SecurityContextHolder?.context?.authentication?.principal
        if (user instanceof BannerUser) {
            return user.pidm
        }
        return null
    }


    private static def isSurveyAvailableForUserAuthority() {
        def authorities = SecurityContextHolder?.context?.authentication?.principal?.authorities
        def userAuthorities = authorities?.collect { it.objectName }
        return (userAuthorities?.contains('SELFSERVICE-STUDENT') || userAuthorities?.contains('SELFSERVICE-EMPLOYEE'))
    }

    private def getSurveyConfirmedIndicator(pidm) {
        def connection
        Sql sql
        try {
            connection = sessionFactory.currentSession.connection()
            sql = new Sql(connection)
            GroovyRowResult row = sql.firstRow("""select SPBPERS_CONFIRMED_RE_CDE from spbpers where spbpers_pidm = ${pidm}""")
            return row?.SPBPERS_CONFIRMED_RE_CDE
        } catch (SQLException ae) {
            sql.close()
            log.debug ae.stackTrace
            throw ae
        }
        catch (Exception ae) {
            log.debug ae.stackTrace
            throw ae
        }
    }

    private Map getStartAndEndDates() {
        def connection
        Sql sql
        Map startAndEndDates = [:]
        try {
            connection = sessionFactory.currentSession.connection()
            sql = new Sql(connection)
            def startEndDateRows = sql.rows("""SELECT GTVSDAX_INTERNAL_CODE,TRUNC(GTVSDAX_REPORTING_DATE) as GTVSDAX_REPORTING_DATE
                                       FROM GTVSDAX
                                        WHERE GTVSDAX_INTERNAL_CODE_SEQNO = 1
                                        AND GTVSDAX_INTERNAL_CODE_GROUP = 'SSMREDATE'
                                        AND GTVSDAX_SYSREQ_IND          = 'Y'""")

            startEndDateRows.each {
                if (it.GTVSDAX_INTERNAL_CODE == "REENDDATE") {
                    startAndEndDates.put("endDate", it.GTVSDAX_REPORTING_DATE)
                }
                else if (it.GTVSDAX_INTERNAL_CODE == "RESTARTDAT") {
                    startAndEndDates.put("startDate", it.GTVSDAX_REPORTING_DATE)
                }
            }
            return startAndEndDates
        } catch (SQLException ae) {
            sql.close()
            log.debug ae.stackTrace
            throw ae
        }
        catch (Exception ae) {
            log.debug ae.stackTrace
            throw ae
        }
    }

}