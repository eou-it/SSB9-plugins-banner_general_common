/*********************************************************************************
 Copyright 2020 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.utility

import grails.gorm.transactions.Transactional
import net.hedtech.banner.service.ServiceBase

/**
 * Service for providing basic crud services on
 * Mail (GURMAIL) objects.
 */
@Transactional
class MailService extends ServiceBase {

    def preCreate(domainModelOrMap) {

    }

    def preUpdate(domainModelOrMap) {

    }

    def preDelete(domainModelOrMap) {

    }

    List getMailDetails(Integer pidm, String termCode) {
        List mailDetails = Mail.fetchByPidmAndTermCode(pidm, termCode)
        mailDetails
    }

    List<Mail> getMaiDetailsByPidmTermSystemIndAndLettrCode(Integer pidm, String termCode, String systemIndicator, String letterCode) {
        List<Mail> mail = Mail.fetchByPidmTermCodeSystemIndAndLettrCode(pidm, termCode, systemIndicator, letterCode)
        mail
    }

}
