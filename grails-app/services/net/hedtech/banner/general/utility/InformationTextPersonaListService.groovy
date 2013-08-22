/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ***************************************************************************** */
package net.hedtech.banner.general.utility

import groovy.sql.Sql
import org.apache.commons.lang.StringUtils

import java.text.ParseException
import java.text.SimpleDateFormat
import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH

class InformationTextPersonaListService {
    def sessionFactory

    def static webTailorRoleList = []


    static {
        if (CH?.config.webTailorRoleList.size() != 0) {
            webTailorRoleList = CH?.config.webTailorRoleList
        }
    }

    public List fetchInformationTextPersonas(String filter) {
        def sql
        def result = []
        def existsSql = """SELECT 'Y' as tableExists FROM ALL_TABLES WHERE TABLE_NAME = 'GURINFO'"""
        try {
            sql = new Sql(sessionFactory.currentSession.connection())
            result = sql.firstRow(existsSql)
        } finally {
            sql?.close()
        }
        if (!(result?.tableExists as boolean)) {
            return []
        }

        if (StringUtils.isBlank(filter)) {
            filter = "%"
        } else if (!(filter =~ /%/)) {
            filter += "%"
        }

        List filteredWebTailorRoleList = filterAndOrderWebTailorRoleList(webTailorRoleList, filter)

        List informationTextPersonaList = []
        filteredWebTailorRoleList?.each { webTailorRole ->
            informationTextPersonaList << new InformationTextPersona(
                    [
                            code: webTailorRole.code,
                            description: webTailorRole.description,
                            lastModifiedBy: webTailorRole.lastModifiedBy,
                            lastModified: getDate(webTailorRole.lastModified, "dd-MMM-yy")
                    ])

        }

        return informationTextPersonaList
    }

    private List filterAndOrderWebTailorRoleList(List webTailorRoleList, String filter) {
        String filterColumn = "code"

        String regEx = this.getRegEx(filter)

        List filteredResults
        filteredResults = webTailorRoleList.findAll {
            it."$filterColumn".toString().toLowerCase().matches(regEx)
        }

        //apply order by
        def orderByCodeDescLastMod = new OrderBy([{ it.code }, { it.description }, { it.lastModified }])
        filteredResults.sort(orderByCodeDescLastMod)

        return filteredResults
    }

    private String getRegEx(String val) {
        val = val.toLowerCase()

        if (!val.startsWith("%")) {
            val = "%" + val
        }
        if (!val.endsWith("%")) {
            val = val + "%"
        }

        val = val.replaceAll("%", "\\\\E" + ".*" + "\\\\Q")
        val = val.replaceAll("_", "\\\\E" + "." + "\\\\Q")
        val = "\\Q" + val + "\\E"

        String regEx = val

        return regEx
    }

    private Date getDate(String dateString, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format)
        sdf.setLenient(false)
        try {
            return sdf.parse(dateString)
        } catch (ParseException e) {
            // Ignore because its not the right format.
            log.warn(" Date was not in the $format format")
        }
        return null
    }
}
