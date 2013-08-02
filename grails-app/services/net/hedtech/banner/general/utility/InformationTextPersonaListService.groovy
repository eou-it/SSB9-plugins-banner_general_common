/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ***************************************************************************** */
package net.hedtech.banner.general.utility

import groovy.sql.Sql
import org.apache.commons.lang.StringUtils

class InformationTextPersonaListService {
    def sessionFactory


    public List fetchInformationTextPersonas( String filter ) {
        def sql
        def result=[]
        def existsSql = """SELECT 'Y' as tableExists FROM ALL_TABLES WHERE TABLE_NAME = 'GURINFO'"""
        try{
            sql = new Sql( sessionFactory.currentSession.connection() )
            result = sql.firstRow(existsSql)
        }finally {
            sql?.close()
        }
        if(!(result?.tableExists as boolean)) return []

        if (StringUtils.isBlank( filter )) {
            filter = "%"
        }
        else if (!(filter =~ /%/)) {
            filter += "%"
        }

        def selSql = """SELECT TWTVROLE_CODE as code, TWTVROLE_DESC as description,
                               TWTVROLE_USER_ID as lastModifiedBy, TWTVROLE_ACTIVITY_DATE as lastModified
                        FROM TWTVROLE
                        WHERE TWTVROLE_CODE like ?
                        ORDER BY TWTVROLE_CODE asc, TWTVROLE_DESC asc, TWTVROLE_ACTIVITY_DATE desc"""

        def informationTextPersonaList = []
        try {
            sql = new Sql( sessionFactory.currentSession.connection() )
            sql.eachRow( selSql, [filter] ) {
                informationTextPersonaList << new InformationTextPersona( [code: it.toRowResult().CODE, description: it.toRowResult().DESCRIPTION,
                        lastModifiedBy: it.toRowResult().LASTMODIFIEDBY, lastModified: it.toRowResult().LASTMODIFIED] )
            }
        } finally {
            sql?.close()
        }
        return informationTextPersonaList
    }
}
