/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.commonmatching

import grails.transaction.Transactional
import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException

import java.sql.CallableStatement
import java.sql.SQLException

@Transactional
class CommonMatchingCompositeService {

    def sessionFactory


    def commonMatching(Map map) {

        def commonMatchRules = CommonMatchingSourceRule.fetchBySource(map.source)
        if (!commonMatchRules.size()) {
            throw new ApplicationException(CommonMatchingCompositeService, "invalid_source")
        }
        log.debug "Common matching parameters: ${map}"

        CallableStatement cmmeCall
        CallableStatement sqlCall
        try {
            def connection = sessionFactory.currentSession.connection()
            String matchPersonQuery = "{ call gokcmpk.p_insert_gotcmme(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) }"
            cmmeCall = connection.prepareCall(matchPersonQuery)

            cmmeCall.setString(1, map.lastName)
            cmmeCall.setString(2,"P")
            cmmeCall.setString(3, map.firstName)
            cmmeCall.setString(4, map.mi)
            cmmeCall.setString(5, map.bannerId)
            cmmeCall.setString(6, map.street1)
            cmmeCall.setString(7, map.street2)
            cmmeCall.setString(8, map.street3)
            cmmeCall.setString(9, map.city)
            cmmeCall.setString(10, map.state)
            cmmeCall.setString(11, map.zip)
            cmmeCall.setString(12, map.nation)
            cmmeCall.setString(13, map.county)
            cmmeCall.setString(14, map.phone)
            cmmeCall.setString(15, map.phoneNumber)
            cmmeCall.setString(16, map.phoneExt)
            cmmeCall.setString(17, map.ssn)
            cmmeCall.setString(18, map.birthDay)
            cmmeCall.setString(19, map.birthMonth)
            cmmeCall.setString(20, map.birthYear)
            cmmeCall.setString(21, map.sex)
            cmmeCall.setString(22, map.email)
            cmmeCall.setString(23, map.addrType)
            cmmeCall.setString(24, map.teleType)
            cmmeCall.setString(25, map.emailType)
            cmmeCall.setString(26, map.addressSource)
            cmmeCall.setString(27, map.additionalIdCode)
            cmmeCall.setString(28, map.additionalId)
            cmmeCall.executeUpdate()

            String commonMatching = "{ call gokcmpk.p_common_matching(?,?,?) }"
            sqlCall = connection.prepareCall(commonMatching)
            sqlCall.setString(1, map.source)
            sqlCall.registerOutParameter(2, java.sql.Types.VARCHAR)
            sqlCall.registerOutParameter(3, java.sql.Types.INTEGER)
            sqlCall.executeUpdate()

        } catch (SQLException ae) {
            log.error("SqlException in Common Matching ${ae}")
            log.error(ae.cause)
            throw ae
        }
        catch (Exception ae) {
            log.error("Exception in Common Matching ${ae} ")
            log.error(ae.cause)
            throw ae
        }
        finally {
            sqlCall?.close()
            cmmeCall?.close()
        }
        def personList = CommonMatchingPersonResult.fetchAllMatchResults(map)
        def totalCount = CommonMatchingPersonResult.fetchCountMatchResults()
        return [personList : personList, count: totalCount]
    }


    def commonMatchingCleanup(Map map) {

        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        try {
            sql.executeUpdate("delete gotcmrt")
            sql.executeUpdate("delete gotcmme")
        }
        catch (SQLException ae) {
            log.error("SqlException in CM Cleanup ${ae}")
            throw ae
        }
        catch (Exception ae) {
            log.error("Exception in CM Cleanup ${ae} ")
            throw ae
        }
        finally {
            sql?.close()
        }
    }
}
