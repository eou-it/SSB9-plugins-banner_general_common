package net.hedtech.banner.general.overall

import grails.converters.JSON
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.crossproduct.BankRoutingInfo
import net.hedtech.banner.general.system.InstitutionalDescription
import org.springframework.web.context.request.RequestContextHolder

class DirectDepositAccountCompositeService {

    def directDepositAccountService
    def bankRoutingInfoService
    def directDepositPayrollHistoryService
    def currencyFormatService


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
        def model = [:]
        def lastPayDist =  directDepositPayrollHistoryService.getLastPayDistribution(pidm)
        def totalAmount = 0

        if (lastPayDist) {
            def totalLeft = lastPayDist.totalNet //Initially, the total left is the total from last distribution
            def allocations = directDepositAccountService.getActiveHrAccounts(pidm).sort {it.priority}
            def allocList = []

            // Calculate the amount for each allocation, going in priority order
            allocations.each {
                // Populate model, ignore generic domain fields
                def alloc = it
                def allocModel = alloc.class.declaredFields.findAll { !it.synthetic && it.name != 'constraints' && it.name != 'log' }.collectEntries {
                    [ (it.name):alloc."$it.name" ]
                }

                // Calculate allocated amount (i.e. currency) and allocation as set by user (e.g. 50% or "Remaining")
                def amt = it.amount
                def pct = it.percent
                def calcAmt = 0
                def allocationByUser = ""

                if (amt) {
                    calcAmt = (amt > totalLeft) ? totalLeft : amt

                    // Allocation as set by user
                    allocationByUser = formatCurrency(amt)
                } else if (pct) {
                    // Calculate amount based on percent and last pay distribution
                    calcAmt = totalLeft * pct / 100

                    if (calcAmt > totalLeft) {
                        calcAmt = totalLeft
                    }

                    // Allocation as set by user
                    // (If it's the last, i.e. lowest priority, allocation and is 100%, then it's
                    // labeled as "Remaining".)
                    allocationByUser = (it == allocations.last() && pct > 99.9) ? "Remaining" : pct.intValue() + "%"
                }

                totalLeft -= calcAmt

                totalAmount += calcAmt

                allocModel.calculatedAmount = formatCurrency(calcAmt)
                allocModel.allocation = allocationByUser

                // Add to list of allocations
                allocList.push(allocModel)
            }

            model.allocations = allocList
        }

        model.totalAmount = totalAmount ? formatCurrency(totalAmount) : ""

        model
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
        if(DirectDepositAccount.fetchByPidmAndAccountInfo(account.pidm, account.bankRoutingInfo.bankRoutingNum, account.bankAccountNum, account.apIndicator, account.hrIndicator)) {
            throw new ApplicationException(DirectDepositAccount, "@@r1:recordAlreadyExists@@")
        }
    }

    private def setNextPriority(account) {
        def lowestPriority = DirectDepositAccount.fetchByPidm(account.pidm)*.priority.max()
        account.priority = (lowestPriority ? lowestPriority+1 : 1)

        account
    }

    /**
     * Formats a Double or BigDecimal to currency.  This is null safe.
     */
    private formatCurrency(amount) {
        def formattedAmount

        if (amount instanceof BigDecimal
                || amount instanceof Double) {
            formattedAmount = currencyFormatService.format(getCurrencyCode(),
                    (amount instanceof BigDecimal ? amount : BigDecimal.valueOf(amount)))
        }

        return formattedAmount
    }

    public static getCurrencyCode() {
        def currencyCode
        def session = RequestContextHolder?.currentRequestAttributes()?.request?.session

        if (session?.getAttribute("baseCurrencyCode")) {
            currencyCode = session.getAttribute("baseCurrencyCode")
        } else {
            currencyCode = InstitutionalDescription.fetchByKey()?.baseCurrCode
            session.setAttribute("baseCurrencyCode", currencyCode)
        }

        return currencyCode
    }
}
