/*******************************************************************************
 Copyright 2015-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
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

    /**
     * Ensure that amount values are valid before saving.
     * @param acct
     */
    def validateAccountAmounts(acct) {
        def amt = acct?.amount;
        def pct = acct?.percent;

        // Rule 1: Both amount and percent null is invalid
        // Rule 2: Both amount and percent not null is invalid
        if (amt == null) {
            if (pct == null) {
                throw new ApplicationException(DirectDepositAccount, "@@r1:noValueExists@@")
            }
        } else if (pct != null) {
            throw new ApplicationException(DirectDepositAccount, "@@r1:bothAmountAndPercentValuesExist@@")
        }

        // Rule 3: If amount is present, it must be greater than zero
        // Rule 4: If percent is present, it must be greater than zero and less than or equal to 100
        if (amt != null && amt <= 0) {
            throw new ApplicationException(DirectDepositAccount, "@@r1:invalidAmountValue@@")
        } else if (pct != null && (pct <= 0 || pct > 100)) {
            throw new ApplicationException(DirectDepositAccount, "@@r1:invalidPercentValue@@")
        }
    }

    def setupAccountsForDelete(accounts) {
        def model = [:]
        model.toBeDeleted = []
        model.messages = []
        
        for (acct in accounts) {
            // if the account is not active, no need to return message about still being active
            boolean ignoreMessage = acct.status == 'I'

            // if account to be deleted is a legacy record set the appropriate indicator
            // to I instead of deleting it
            if(acct.hrIndicator == 'A' && acct.apIndicator == 'A') {
                def apDelete = acct.apDelete

                // get a fresh version of account in case user has unsaved edits on the one 
                // submitted for delete
                acct = DirectDepositAccount.get(acct.id)

                if(apDelete) {
                    acct.apIndicator = 'I'
                    if(!ignoreMessage) {
                        model.messages.add([acct: acct.bankAccountNum, activeType: 'PR'])
                    }
                }
                else {
                    acct.hrIndicator = 'I'
                    if(!ignoreMessage) {
                        model.messages.add([acct: acct.bankAccountNum, activeType: 'AP'])
                    }
                }
                update(acct)
            }
            else {
                def accts = DirectDepositAccount.fetchActiveByAccountData(acct.pidm, acct.bankRoutingInfo.bankRoutingNum, acct.bankAccountNum, acct.accountType)

                if(acct.apIndicator == 'A' && acct.apDelete) {
                    model.toBeDeleted.add(acct)
                    if(accts.size() > 1 && !ignoreMessage) {
                        model.messages.add([acct: acct.bankAccountNum, activeType: 'PR'])
                    }
                }
                else if(acct.hrIndicator == 'A' && !acct.apDelete) {
                    model.toBeDeleted.add(acct)
                    if(accts.size() > 1 && !ignoreMessage) {
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

    def fetchApAccountsByPidm(Integer pidm) {
        def dirdAccounts

        DirectDepositAccount.withSession { session ->
            dirdAccounts = session.getNamedQuery(
                    'DirectDepositAccount.fetchApAccountsByPidm')
                    .setInteger('pidm', pidm).list()
        }

        return dirdAccounts
    }

    /**
     * Retrieve active AP accounts and convert to a list of maps.
     * @param pidm
     * @return List of account maps
     */
    def fetchApAccountsByPidmAsListOfMaps(pidm) {
        marshallAccountsToMinimalStateForUi(fetchApAccountsByPidm(pidm))
    }

    def getActiveHrAccounts(pidm) {
        def activeAccounts = DirectDepositAccount.fetchActiveHrAccountsByPidm(pidm)

        return activeAccounts
    }

    /**
     * Given an account object or list of account objects (i.e. payroll or AP), extract only the data needed for
     * the Direct Deposit UI.  Also unbinds from any associations with Hibernate.
     * @param account
     * @return Account map
     */
    static marshallAccountsToMinimalStateForUi(accounts) {
        if (!accounts) return accounts

        boolean isCollection = isCollectionOrArray(accounts)
        def accountList = isCollection ? accounts : [accounts]
        def marshalledAccounts = []

        accountList.each {
            // Routing info
            def bankRoutingInfo = [:]

            if (it.bankRoutingInfo) {
                bankRoutingInfo = [
                    id            : it.bankRoutingInfo.id,
                    version       : it.bankRoutingInfo.version,
                    bankRoutingNum: it.bankRoutingInfo.bankRoutingNum,
                    bankName      : it.bankRoutingInfo.bankName
                ]
            }

            // Account info
            def marshalledAccount = [
                id                         : it.id,
                version                    : it.version,
                pidm                       : it.pidm,
                status                     : it.status,
                documentType               : it.status,
                priority                   : it.priority,
                apIndicator                : it.apIndicator,
                hrIndicator                : it.hrIndicator,
                bankAccountNum             : it.bankAccountNum,
                bankRoutingInfo            : bankRoutingInfo,
                amount                     : it.amount,
                percent                    : it.percent,
                accountType                : it.accountType,
                intlAchTransactionIndicator: it.intlAchTransactionIndicator
            ]

            marshalledAccounts << marshalledAccount
        }

        return isCollection ? marshalledAccounts : marshalledAccounts.first()
    }

    static boolean isCollectionOrArray(object) {
        [Collection, Object[]].any { it.isAssignableFrom(object.getClass()) }
    }

}
