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

    def getActiveApAccounts(pidm) {
        def activeAccounts = DirectDepositAccount.fetchActiveApAccountsByPidm(pidm)

        return activeAccounts
    }
    
    def getActiveHrAccounts(pidm) {
        def activeAccounts = DirectDepositAccount.fetchActiveHrAccountsByPidm(pidm)

        return activeAccounts
    }

}
