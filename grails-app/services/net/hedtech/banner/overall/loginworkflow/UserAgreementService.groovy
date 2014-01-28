/*******************************************************************************
 Copyright 2013-2014 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.overall.loginworkflow

import groovy.sql.Sql
import org.apache.log4j.Logger
import java.sql.SQLException
import net.hedtech.banner.general.utility.InformationTextUtility

class UserAgreementService {
    static transactional = true
    def sessionFactory
    private final log = Logger.getLogger(getClass())

    private static final String POLICY_PAGE_NAME='TERMSOFUSAGE'
    private static final String TERMS_OF_USAGE_LABEL='terms.of.usage'

    public void updateUsageIndicator(String pidm,String usageIndicator)
    {
        def connection
        Sql sql
        try{
            connection = sessionFactory.currentSession.connection()
            sql = new Sql(connection)
            sql.call ("{call gb_third_party_access.p_update(p_pidm=>${pidm}, p_usage_accept_ind=>${usageIndicator})}")
            sql.commit()
        }catch (SQLException ae) {
            log.debug ae.stackTrace
            throw ae
        }
        catch (Exception ae) {
            log.debug ae.stackTrace
            throw ae
        }finally{
            connection.close()
       }

    }
}
