/*********************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.utility

import grails.gorm.transactions.Transactional
import net.hedtech.banner.security.BannerGrantedAuthorityService
import net.hedtech.banner.service.ServiceBase

/**
 * Service for providing basic crud services on
 * Mail (GURMAIL) objects.
 */
@Transactional
class MailService extends ServiceBase {

    def preCreate( domainModelOrMap ) {

    }

    def preUpdate( domainModelOrMap ) {

    }

    def preDelete(domainModelOrMap) {

    }

    List getMailDetails(Integer pidm, String termCode){
        List mailDetails = Mail.fetchByPidmAndTermCode(pidm, termCode)
        mailDetails
    }

    def getPrintTicketDetails(String systemInd, String termCode, char moduleCode, String letterCode, char pubGen) {
        Integer pidm = BannerGrantedAuthorityService.getPidm()
        def mailDetails = Mail.fetchByPidmTermSystemIndAndLetterCode(pidm, termCode, systemInd, letterCode)
        mailDetails
    }

}
