package net.hedtech.banner.general.overall

import grails.converters.JSON
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.crossproduct.BankRoutingInfo

class DirectDepositAccountCompositeService {

    def directDepositAccountService
    def bankRoutingInfoService
    def directDepositPayrollHistoryService

    /**
     * @desc This method creates or updates a Direct Deposit account
     * @param map
     * @return
     */
    def addorUpdateAccount( def map ) {
        def account = [:]

        directDepositAccountService.validateAccountNumFormat(map.bankAccountNum)

        account.bankAccountNum = map.bankAccountNum
        account.id = map.id
        account.pidm = map.pidm
        account.status = map.status
        account.apIndicator = map.apIndicator
        account.hrIndicator = map.hrIndicator
        account.amount = map.amount
        account.percent = map.percent
        account.accountType = map.accountType
        account.documentType = map.documentType
        account.intlAchTransactionIndicator = map.intlAchTransactionIndicator

        def routingNum = validateRoutingNumExistsInMap(map)
        bankRoutingInfoService.validateRoutingNumber(routingNum)

        account.bankRoutingInfo = validateBankRoutingInfo(routingNum)

        validateNotDuplicate(account)
        account = setNextPriority(account)

        account = directDepositAccountService.createOrUpdate( [domainModel: account] )

        return account
    }

    /**
     * @desc This method retrieves user payroll allocations and calculates the currency
     *       amount of each allocation based on most recent pay distribution.
     * @return
     */
    def getUserHrAllocations(pidm) {
        def modelList = []
        def lastPayDist =  directDepositPayrollHistoryService.getLastPayDistribution(pidm)

        if (lastPayDist) {
            def total = lastPayDist.totalNet
            def totalLeft = total
            def allocations = directDepositAccountService.getActiveHrAccounts(pidm).sort {it.priority}
            allocations = []

            // Calculate the amount for each allocation, going in priority order
            allocations.each {
                // Populate model
                def model = [:]

                model.bankRoutingInfo = [:]

                if (it.bankRoutingInfo) {
                    model.bankRoutingInfo.bankName = it.bankRoutingInfo.bankName
                    model.bankRoutingInfo.bankRoutingNum = it.bankRoutingInfo.bankRoutingNum
                }

                model.bankAccountNum = it.bankAccountNum
                model.accountType = it.accountType
                model.status = it.status
                model.priority = it.priority
                model.amount = it.amount
                model.percent = it.percent

                // Calculate allocated amount
                def amt = it.amount
                def pct = it.percent
                def calcAmt = 0

                if (amt) {
                    calcAmt = (amt > totalLeft) ? totalLeft : amt
                } else if (pct) {
                    calcAmt = total * pct / 100

                    if (calcAmt > totalLeft) {
                        calcAmt = totalLeft
                    }
                }

                totalLeft -= calcAmt

                model.calculatedAmount = calcAmt

                // Add to list of allocations
                modelList.push(model)
            }
        }

        modelList
    }

    private def validateRoutingNumExistsInMap(directDepositAccountMap) {
        def routingNum = directDepositAccountMap?.bankRoutingInfo?.bankRoutingNum

        if (routingNum == null) {
            throw new ApplicationException(this, "@@r1:missingRoutingNum@@", "Bank routing number missing.")
        }

        routingNum
    }

    private def validateBankRoutingInfo(routingNum) {
        def routingInfo = BankRoutingInfo.fetchByRoutingNum(routingNum)

        if (!routingInfo) {
            throw new ApplicationException(this, "@@r1:missingRoutingInfo@@", "Bank routing information missing.")
        }

        routingInfo.first()
    }

    private def validateNotDuplicate(account) {
        if(DirectDepositAccount.fetchByPidmAndAccountInfo(account.pidm, account.bankRoutingInfo.bankRoutingNum, account.bankAccountNum, account.accountType)) {
            throw new ApplicationException(DirectDepositAccount, "@@r1:recordAlreadyExists@@")
        }
    }

    private def setNextPriority(account) {
        def lowestPriority = DirectDepositAccount.fetchByPidm(account.pidm)*.priority.max()
        account.priority = (lowestPriority ? lowestPriority+1 : 1)

        account
    }
}
