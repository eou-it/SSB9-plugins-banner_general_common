package net.hedtech.banner.general.overall

import net.hedtech.banner.service.ServiceBase
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.overall.DirectDepositAccount

class DirectDepositAccountService extends ServiceBase{

    static transactional = true
    
    def preCreate(domainModelOrMap) {
        def domain = domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap

        if (domain) {
            //TODO: validateRoutingNumber(domain.bankRoutingNum)
            /* 
             * IBAN/Non-IBAN format edits 
             * not sure how to do this yet 
             * validateAccountNumFormat(domain.bankAccountNum) -> peklibs.f_valid_format_bank_rout_num
             * validateRoutingNumFormat(domain.bankRoutingNum) -> peklibs.f_valid_format_bank_acct_num
             */
            validateNotDuplicate(domain)
            setNextPriority(domain)
        }
    }
    
    def validateNotDuplicate(domain) {
        if(DirectDepositAccount.fetchByPidmAndAccountInfo(domain.pidm, domain.bankRoutingNum, domain.bankAccountNum, domain.accountType)) {
            throw new ApplicationException(DirectDepositAccount, "@@r1:recordAlreadyExists@@")
        }
    }
    
    def setNextPriority(domain) {
        def lowestPriority = DirectDepositAccount.fetchByPidm(domain.pidm)*.priority.max()
        domain.priority = (lowestPriority ? lowestPriority+1 : 1)
    }

}
