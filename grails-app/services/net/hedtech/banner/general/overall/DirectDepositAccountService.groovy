package net.hedtech.banner.general.overall

import groovy.sql.Sql
import net.hedtech.banner.service.ServiceBase
import net.hedtech.banner.exceptions.ApplicationException

class DirectDepositAccountService extends ServiceBase{

    static transactional = true
    
    def validateAccountNumFormat(accountNum) {
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        def valid
        sql.call("{$Sql.VARCHAR = call goksels.f_validate_bank_acct_num(${accountNum})}") {result -> valid = result}
        
        if(valid != 'Y'){
            throw new ApplicationException(DirectDepositAccount, "@@r1:invalidAccountNumFmt@@")
        }
    }
    
    def syncApAndHrAccounts( account ) {
        def accts = DirectDepositAccount.fetchActiveByAccountNums(account.pidm, account.bankRoutingInfo.bankRoutingNum, account.bankAccountNum)
        
        if( accts.size() > 2 ) {
            // user should not have three or more accounts with same routing number and account number.
            // if they do, we cannot determine which account has the correct information since they can
            // change all the accounts at once, so we error out.
            throw new ApplicationException(DirectDepositAccount, "@@r1:tooManyAccountsFound@@")
        }
        else if( accts.size() == 2) {
            if(account.id == accts[0].id) {
                accts[1].accountType = account.accountType
                update(accts[1])
            }
            else {
                accts[0].accountType = account.accountType
                update(accts[0])
            }
        }
    }
    
    def setupAccountsForDelete(accounts) {
        def model = [:]
        model.toBeDeleted = []
        model.messages = []
        
        for (acct in accounts) {
            if(acct.hrIndicator == 'A' && acct.apIndicator == 'A') {
                // if account to be deleted is a legacy record set the appropiate indicator
                // to I instead of deleting it
                
                if(acct.apDelete) {
                    acct.apIndicator = 'I'
                    model.messages.add([acct: acct.bankAccountNum, activeType: 'PR'])
                }
                else {
                    acct.hrIndicator = 'I'
                    model.messages.add([acct: acct.bankAccountNum, activeType: 'AP'])
                }
                update(acct)
            }
            else {
                def accts = DirectDepositAccount.fetchActiveByAccountNums(acct.pidm, acct.bankRoutingInfo.bankRoutingNum, acct.bankAccountNum)
                
                if( accts.size() == 2) {
                    // if account is synced make sure to only delete the requested record
                    // and display the appropiate message
                    
                    if(acct.hrIndicator == 'A' && acct.apDelete){
                        if(acct.hrIndicator != accts[0].hrIndicator) {
                            model.toBeDeleted.add(accts[0])
                        }
                        else if(acct.hrIndicator != accts[1].hrIndicator) {
                            model.toBeDeleted.add(accts[1])
                        }
                        model.messages.add([acct: acct.bankAccountNum, activeType: 'PR'])
                    }
                    else if(!acct.apDelete) {
                        model.toBeDeleted.add(acct);
                        model.messages.add([acct: acct.bankAccountNum, activeType: 'AP'])
                    }
                }
                else { 
                    if(acct.apIndicator == 'A' && acct.apDelete) {
                        model.toBeDeleted.add(acct)
                    }
                    else if(acct.hrIndicator == 'A' && !acct.apDelete) {
                        model.toBeDeleted.add(acct)
                    }
                }
            }
        }
        
        return model;
    }

    def getActiveApAccounts(pidm) {
        def activeAccounts = DirectDepositAccount.fetchActiveApAccountsByPidm(pidm)

        return activeAccounts
    }
    
    def getActiveHrAccounts(pidm) {
        def activeAccounts = DirectDepositAccount.fetchActiveHrAccountsByPidm(pidm)

        return activeAccounts
    }

}
