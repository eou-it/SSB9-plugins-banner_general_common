/*******************************************************************************
 Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */

package net.hedtech.banner.loginworkflow

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.apache.log4j.Logger
import java.sql.SQLException
import net.hedtech.banner.security.BannerGrantedAuthorityService

class UserAgreementFlow extends PostLoginWorkflow {
    def sessionFactory

    private final log = Logger.getLogger(getClass())
    private static final USAGE_INDICATOR="N"
    private static final DISPLAY_STATUS="Y"

    public boolean isShowPage(request) {
        def session = request.getSession();
        String isDone = session.getAttribute(UserAgreementController.USER_AGREEMENT_ACTION)
        boolean displayPage = false
        if (isDone != UserAgreementController.ACTION_DONE) {
            String pidm = BannerGrantedAuthorityService.getPidm()
            String displayStatus = getTermsOfUsageDisplayStatus()
            if (displayStatus?.equals(DISPLAY_STATUS)) {
                String usageIndicator = getUsageIndicator(pidm)
                if (usageIndicator?.equals(USAGE_INDICATOR)) {
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
