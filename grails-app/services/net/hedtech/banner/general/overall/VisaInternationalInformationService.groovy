/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.service.ServiceBase
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes

import java.sql.CallableStatement
import java.sql.SQLException

class VisaInternationalInformationService extends ServiceBase {

    def institutionalDescriptionService


    def preCreate(map) {
        validateAll(map.domainModel)
    }


    def preUpdate(map) {
        validateAll(map.domainModel)

        def domain = map.domainModel

        if (institutionalDescriptionService.findByKey().studentInstalled && domain.certificateDateReceipt && domain.admissionRequest)
            updateIntlChecklist(domain.pidm)
    }


    private void validateAll(domain) {
        defaultRequired(domain)
        validatePassport(domain)
        validateCertification(domain)
    }


    private void defaultRequired(domain) {
        if (!domain.spouseIndicator) domain.spouseIndicator = "T"
        if (!domain.signatureIndicator) domain.signatureIndicator = "T"
    }


    private void validatePassport(domain) {
        if (domain.passportId && !domain.nationIssue)
            throw new ApplicationException(VisaInternationalInformation, "@@r1:missingNationOfIssue@@")
    }


    private void validateCertification(domain) {
        if (domain.certificateDateIssue && domain.certificateDateReceipt)
            if (domain.certificateDateIssue > domain.certificateDateReceipt)
                throw new ApplicationException(VisaInternationalInformation, "@@r1:invalidCertificationDate@@")
    }


    public static void updateIntlChecklist(Integer pidm) {
        def ctx = ServletContextHolder.servletContext.getAttribute(GrailsApplicationAttributes.APPLICATION_CONTEXT)
        def sessionFactory = ctx.sessionFactory

        def sqlStrList = "{call sakchkb.P_intl_UpdChecklist(?, null)}"
        CallableStatement sqlCallList

        def sqlStrStatus = "{call sakchkb.P_UpdAppStatus(?, null)}"
        CallableStatement sqlCallStatus

        try {
            sqlCallList = sessionFactory.getCurrentSession().connection().prepareCall(sqlStrList)
            sqlCallList.setInt(1, pidm)
            sqlCallList.executeUpdate()

            sqlCallStatus = sessionFactory.getCurrentSession().connection().prepareCall(sqlStrStatus)
            sqlCallStatus.setInt(1, pidm)
            sqlCallStatus.executeUpdate()
        }
        catch (SQLException ae) {
            log.debug "SqlException in updateIntlChecklist exception ${ae}"
            log.debug ae.stackTrace
            throw ae
        }
        catch (Exception ae) {
            log.debug "Exception in updateIntlChecklist ${ae}"
            log.debug ae.stackTrace
            throw ae
        }
        finally {
            try {
                if (sqlCallList) sqlCallList.close()
                if (sqlCallStatus) sqlCallStatus.close()
            }
            catch (SQLException af) {
                // ignore
            }
        }
    }
}
