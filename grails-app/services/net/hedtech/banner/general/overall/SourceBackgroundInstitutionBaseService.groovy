/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.service.ServiceBase

import java.sql.CallableStatement
import java.sql.SQLException

class SourceBackgroundInstitutionBaseService extends ServiceBase {

    def preCreate(map) {
        validateCodes(map.domainModel)
        checkAddress(map.domainModel)
    }


    def preUpdate(map) {
        validateCodes(map.domainModel)
        checkAddress(map.domainModel)
    }


    def getEnrollmentPlanningServiceCode(sourceAndBackgroundInstitution) {
        def sbgi = SourceBackgroundInstitutionBase.fetchBySourceAndBackgroundInstitution(sourceAndBackgroundInstitution)
        def epsCode

        if (sbgi) {
            try {
                CallableStatement sqlCall = sessionFactory.getCurrentSession().connection().prepareCall("{? = call f_EpscCode(?, ?, ?)}")
                sqlCall.registerOutParameter(1, java.sql.Types.VARCHAR)
                sqlCall.setString(2, sbgi?.state?.code)
                sqlCall.setString(3, sbgi?.zip)
                sqlCall.setString(4, sbgi?.county?.code)
                sqlCall.executeUpdate()
                epsCode = sqlCall.getString(1)
            }
            catch (SQLException ae) {
                log.debug "SqlException in getEnrollmentPlanningServiceCode exception ${ae}"
                log.debug ae.stackTrace
                throw ae
            }
            catch (Exception ae) {
                log.debug "Exception in getEnrollmentPlanningServiceCode ${ae} "
                log.debug ae.stackTrace
                throw ae
            }
        }

        return epsCode
    }


    private void validateCodes(domain) {
        if (domain?.sourceAndBackgroundInstitution?.code) {
            SourceAndBackgroundInstitution sourceAndBackgroundInstitution = SourceAndBackgroundInstitution.findByCode(domain.sourceAndBackgroundInstitution.code)
            if (!sourceAndBackgroundInstitution)
                throw new ApplicationException(SourceBackgroundInstitutionBase, "@@r1:invalidSourceAndBackgroundInstitution@@")
        }
    }


    private void checkAddress(domain) {

        if (domain?.state
                && !domain?.zip) {
            throw new ApplicationException(SourceBackgroundInstitutionBase, "@@r1:missingZip@@")
        }

        if (domain?.zip
                && !domain?.state
                && !domain?.nation) {
            throw new ApplicationException(SourceBackgroundInstitutionBase, "@@r1:missingStateAndNation@@")
        }

        if (!domain?.zip
                && !domain?.state
                && !domain?.nation) {
            throw new ApplicationException(SourceBackgroundInstitutionBase, "@@r1:missingStateAndZipAndNation@@")
        }
    }

}
