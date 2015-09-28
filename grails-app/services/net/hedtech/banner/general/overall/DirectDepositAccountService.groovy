package net.hedtech.banner.general.overall

import groovy.sql.Sql
import net.hedtech.banner.service.ServiceBase
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.overall.DirectDepositAccount
import net.hedtech.banner.general.crossproduct.BankRoutingInfo

class DirectDepositAccountService extends ServiceBase{

    static transactional = true
    
    def preCreate(domainModelOrMap) {
        def domain = domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap

        if (domain) {
            validateRoutingNumber(domain.bankRoutingNum)
            validateAccountNumFormat(domain.bankAccountNum)
            validateNotDuplicate(domain)
            setNextPriority(domain)
        }
    }
    
    def validateNotDuplicate(domain) {
        if(DirectDepositAccount.fetchByPidmAndAccountInfo(domain.pidm, domain.bankRoutingNum, domain.bankAccountNum, domain.accountType)) {
            throw new ApplicationException(DirectDepositAccount, "@@r1:recordAlreadyExists@@")
        }
    }
    
    def validateRoutingNumber(routingNum) {
        validateRoutingNumFormat(routingNum)
        
        def bankInfo = BankRoutingInfo.fetchByRoutingNum(routingNum) 
        
        if(bankInfo){
            return bankInfo;
        }
        else {
            throw new ApplicationException(DirectDepositAccount, "@@r1:invalidRoutingNum@@")
        }
    }
    
    def validateRoutingNumFormat(routingNum) {
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        def valid
        sql.call("{$Sql.VARCHAR = call goksels.f_validate_bank_rout_num(${routingNum})}") {result -> valid = result}
        
        if(valid != 'Y'){
            throw new ApplicationException(DirectDepositAccount, "@@r1:invalidRoutingNumFmt@@")
        }
    }
    
    def validateAccountNumFormat(accountNum) {
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        def valid
        sql.call("{$Sql.VARCHAR = call goksels.f_validate_bank_acct_num(${accountNum})}") {result -> valid = result}
        
        if(valid != 'Y'){
            throw new ApplicationException(DirectDepositAccount, "@@r1:invalidAccountNumFmt@@")
        }
    }
    
    def setNextPriority(domain) {
        def lowestPriority = DirectDepositAccount.fetchByPidm(domain.pidm)*.priority.max()
        domain.priority = (lowestPriority ? lowestPriority+1 : 1)
    }

    def getActiveApAccounts(pidm) {
        def activeAccounts = DirectDepositAccount.fetchActiveApAccountsByPidm(pidm)

        return activeAccounts
    }

}
