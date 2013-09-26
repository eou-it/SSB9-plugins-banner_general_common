package net.hedtech.banner.loginworkflow

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.apache.log4j.Logger
import java.sql.SQLException
import net.hedtech.banner.security.BannerGrantedAuthorityService

/** *****************************************************************************
 © 2013 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */

class UserAgreementFlow extends PostLoginWorkflow {
    def sessionFactory

    private final log = Logger.getLogger(getClass())

    public boolean isShowPage(request) {
        def session = request.getSession();
        String isDone = session.getAttribute("useraggrementdone")
        boolean displayPage = false
        if (isDone != "true") {
            String pidm = BannerGrantedAuthorityService.getPidm()
            String displayStatus = getTermsOfUsageDisplayStatus()
            if (displayStatus?.equals("Y")) {
                String usageIndicator = getUsageIndicator(pidm)
                if (usageIndicator?.equals("N")) {
                    displayPage = true
                }

            }
        }
        return displayPage
    }

    public String getControllerUri() {
        return "/ssb/userAgreement"
    }

    public String getControllerName() {
        return "userAgreement"
    }

    private String getTermsOfUsageDisplayStatus() {
        def connection
        Sql sql
        try {
            connection = sessionFactory.currentSession.connection()
            sql = new Sql(connection)
            GroovyRowResult row = sql.firstRow("""select TWGBWRUL_DISP_USAGE_IND from TWGBWRUL""")
            return row?.TWGBWRUL_DISP_USAGE_IND
        } catch (SQLException ae) {
            log.debug ae.stackTrace
            throw ae
        }
        catch (Exception ae) {
            log.debug ae.stackTrace
            throw ae
        } finally {
            connection.close()
        }
    }

    private String getUsageIndicator(String pidm) {
        def connection
        Sql sql
        try {
            connection = sessionFactory.currentSession.connection()
            sql = new Sql(connection)
            GroovyRowResult row = sql.firstRow("""select GOBTPAC_USAGE_ACCEPT_IND from GOBTPAC where GOBTPAC_PIDM = ${pidm}""")
            return row?.GOBTPAC_USAGE_ACCEPT_IND
        } catch (SQLException ae) {
            log.debug ae.stackTrace
            throw ae
        }
        catch (Exception ae) {
            log.debug ae.stackTrace
            throw ae
        } finally {
            connection.close()
        }
    }


}
