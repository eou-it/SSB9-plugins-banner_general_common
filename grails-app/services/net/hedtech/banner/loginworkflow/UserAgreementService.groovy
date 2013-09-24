/** *****************************************************************************
 © 2013 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package net.hedtech.banner.loginworkflow

import groovy.sql.Sql
import org.apache.log4j.Logger
import java.sql.SQLException
import net.hedtech.banner.general.utility.InformationTextUtility

/**
 * UserAgreementService.
 *
 * Date: 7/22/13
 * Time: 2:55 PM
 */
class UserAgreementService {
    static transactional = true
    def sessionFactory
    private final log = Logger.getLogger(getClass())

    private static final String POLICY_PAGE_NAME='TERMSOFUSAGE'
    private static final String LABEL='terms.of.usage'

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

    public String getTermsOfUseInfoText(){
        return InformationTextUtility.getMessage(POLICY_PAGE_NAME, LABEL)
    }

}
