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

    def setupAccountsForDelete(accounts) {
        def model = [:]
        model.toBeDeleted = []
        model.messages = []
        
        for (acct in accounts) {
            if(acct.hrIndicator == 'A' && acct.apIndicator == 'A') {
                // if account to be deleted is a legacy record set the appropiate indicator
                // to I instead of deleting it
                
                def apDelete = acct.apDelete

                // get a fresh version of account in case user has unsaved edits on the one 
                // submitted for delete
                acct = DirectDepositAccount.get(acct.id)

                if(apDelete) {
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
                def accts = DirectDepositAccount.fetchActiveByAccountData(acct.pidm, acct.bankRoutingInfo.bankRoutingNum, acct.bankAccountNum, acct.accountType)
                
                if( false ) {
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
                        if(accts.size() > 1)
                            model.messages.add([acct: acct.bankAccountNum, activeType: 'PR'])
                    }
                    else if(acct.hrIndicator == 'A' && !acct.apDelete) {
                        model.toBeDeleted.add(acct)
                        if(accts.size() > 1)
                            model.messages.add([acct: acct.bankAccountNum, activeType: 'AP'])
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
