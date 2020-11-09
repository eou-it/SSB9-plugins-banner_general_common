/*********************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.utility

import grails.gorm.transactions.Transactional
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.system.LetterProcessLetter
import net.hedtech.banner.general.system.Term
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

    boolean saveMailDetails(String systemInd, String termCode, char moduleCode, String letterCode, char pubGen) {
        Integer pidm = BannerGrantedAuthorityService.getPidm()
        boolean success = false
        def mailDetails = Mail.fetchByPidmTermSystemIndAndLetterCode(pidm, termCode, systemInd, letterCode)
        try {
            if (mailDetails) {
                mailDetails.pidm = pidm
                mailDetails.systemIndicator = systemInd
                mailDetails.term = Term.findByCode(termCode)
                mailDetails.module = moduleCode
                mailDetails.letterProcessLetter = LetterProcessLetter.findByCode(letterCode)
                mailDetails.dateInitial = new Date()
                mailDetails.publishedGenerated = pubGen
                mailDetails.userData = "GRAILS"
                mailDetails.lastModified = new Date()
                this.update([domainModel: mailDetails], true)
            } else {
                mailDetails = new Mail(
                        pidm: pidm,
                        systemIndicator: systemInd,
                        term: Term.findByCode(termCode),
                        module: moduleCode,
                        letterProcessLetter: LetterProcessLetter.findByCode(letterCode),
                        dateInitial: new Date(),
                        publishedGenerated: pubGen,
                        userData: "GRAILS",
                        lastModified: new Date()
                )
                this.create([domainModel: mailDetails], true)
            }
            success = true
        }
        catch(ApplicationException e) {
            success = false
            throw new ApplicationException(this.class, "create/update mail details failed")
        }
        success
    }

}
