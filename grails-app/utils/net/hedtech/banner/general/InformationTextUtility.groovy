package net.hedtech.banner.general

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.context.i18n.LocaleContextHolder
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes
import groovy.sql.Sql
/** *****************************************************************************
 © 2013 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
class InformationTextUtility {

    public static String getMessages(String pageName, Locale locale = LocaleContextHolder.getLocale()) {
        String localeParam = locale.toString().toUpperCase();
        List roles = getRoleFromAuthorities(SecurityContextHolder?.context?.authentication?.principal?.authorities)

        def params = [pageName]
        def inClauseParams = null
        roles.each { val ->
            if (inClauseParams) {
                inClauseParams += ", ? "
            }
            else {
                inClauseParams = "?"
            }

            params << val
        }
        params << localeParam

        def ctx = ServletContextHolder.servletContext.getAttribute(GrailsApplicationAttributes.APPLICATION_CONTEXT)
        def sessionFactory = ctx.sessionFactory
        def session = sessionFactory.currentSession
        def sql = new Sql(session.connection())
        /*def sqlQueryString = """ select * from gurinfo
                                 where gurinfo_page_name = ?
                                 and GURINFO_ROLE_CODE in ( ${inClauseParams} )
                                 and GURINFO_LOCALE = ?
                                 order by GURINFO_SEQUENCE_NUMBER"""*/

        def sqlQueryString = """  SELECT * FROM gurinfo a
                                  WHERE gurinfo_page_name = ?
                                  AND GURINFO_ROLE_CODE IN (${inClauseParams})
                                  AND GURINFO_LOCALE = ?
                                  AND GURINFO_SOURCE_INDICATOR =
                                  (
                                      SELECT nvl( MAX(GURINFO_SOURCE_INDICATOR ),'B')
                                      FROM GURINFO
                                      WHERE gurinfo_page_name = a.gurinfo_page_name
                                      AND GURINFO_LABEL = a.GURINFO_LABEL
                                      AND GURINFO_SEQUENCE_NUMBER = a.GURINFO_SEQUENCE_NUMBER
                                      AND GURINFO_ROLE_CODE = a.GURINFO_ROLE_CODE
                                      AND GURINFO_LOCALE = a.GURINFO_LOCALE
                                      AND GURINFO_SOURCE_INDICATOR ='L'
                                      AND TRUNC(SYSDATE) BETWEEN TRUNC(NVL( GURINFO_START_DATE, (SYSDATE - 1) ) ) AND TRUNC( NVL( GURINFO_END_DATE, (SYSDATE + 1) ))
                                  )
                                  ORDER BY GURINFO_LABEL, GURINFO_SEQUENCE_NUMBER"""

        //def infoText = ""
        Map informationTexts = new HashMap()
        def resultSet = sql.rows(sqlQueryString, params)
        resultSet.each { t ->
            //if same key concatentate
            String text = informationTexts.get(t.GURINFO_LABEL)
            if(text == null) {
                text = getTextBasedOnDateRange(t)
            }
            else {
                text += "\n" + getTextBasedOnDateRange(t)
            }
            informationTexts.put(t.GURINFO_LABEL, text)
        }

        return informationTexts
    }

    private static String getTextBasedOnDateRange(row) {
        if(row.GURINFO_SOURCE_INDICATOR == "L" && row.GURINFO_START_DATE == null) {
            return ""
        }
        else {
            row.GURINFO_TEXT
        }
    }

    public static String getMessage(String pageName, String label, Locale locale = LocaleContextHolder.getLocale()) {
        String localeParam = locale.toString().toUpperCase();
        List roles = getRoleFromAuthorities(SecurityContextHolder?.context?.authentication?.principal?.authorities)
        def params = [pageName]
        def inClauseParams = null
        roles.each { val ->
            if (inClauseParams) {
                inClauseParams += ", ? "
            }
            else {
                inClauseParams = "?"
            }

            params << val
        }
        params << localeParam
        params << label


        def ctx = ServletContextHolder.servletContext.getAttribute(GrailsApplicationAttributes.APPLICATION_CONTEXT)
        def sessionFactory = ctx.sessionFactory
        def session = sessionFactory.currentSession
        def sql = new Sql(session.connection())
        /*def sqlQueryString = """ select * from gurinfo
                                 where gurinfo_page_name = ?
                                 and GURINFO_ROLE_CODE in ( ${inClauseParams} )
                                 and GURINFO_LOCALE = ?
                                 AND GURINFO_LABEL = ?
                                 order by GURINFO_SEQUENCE_NUMBER"""*/

        def sqlQueryString = """  SELECT * FROM gurinfo a
                                  WHERE gurinfo_page_name = ?
                                  AND GURINFO_ROLE_CODE IN (${inClauseParams})
                                  AND GURINFO_LOCALE = ?
                                  AND GURINFO_LABEL = ?
                                  AND GURINFO_SOURCE_INDICATOR =
                                  (
                                      SELECT nvl( MAX(GURINFO_SOURCE_INDICATOR ),'B')
                                      FROM GURINFO
                                      WHERE gurinfo_page_name = a.gurinfo_page_name
                                      AND GURINFO_LABEL = a.GURINFO_LABEL
                                      AND GURINFO_SEQUENCE_NUMBER = a.GURINFO_SEQUENCE_NUMBER
                                      AND GURINFO_ROLE_CODE = a.GURINFO_ROLE_CODE
                                      AND GURINFO_LOCALE = a.GURINFO_LOCALE
                                      AND GURINFO_SOURCE_INDICATOR ='L'
                                      AND TRUNC(SYSDATE) BETWEEN TRUNC(NVL( GURINFO_START_DATE, (SYSDATE - 1) ) ) AND TRUNC( NVL( GURINFO_END_DATE, (SYSDATE + 1) ))
                                  )
                                  ORDER BY GURINFO_LABEL, GURINFO_SEQUENCE_NUMBER"""

        def infoText = null
        def resultSet = sql.rows(sqlQueryString, params)
        resultSet.each {t ->
            //infoText += t.GURINFO_TEXT + "\n"
            if(infoText == null) {
                infoText = getTextBasedOnDateRange(t)
            }
            else {
                infoText += "\n" + getTextBasedOnDateRange(t)
            }
        }
        if(infoText == null) {
            infoText = label
        }
        return infoText
    }

    private static List getRoleFromAuthorities(Set authorities) {
        List roles = new ArrayList();
        //ROLE_SELFSERVICE-FACULTY_BAN_DEFAULT_M
        //ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_M
        authorities.each {
            String authority = it.authority
            String role = authority.substring("ROLE_SELFSERVICE".length() + 1 )
            role = role.split("_")[0]
            roles << role
        }
        roles
    }
}
