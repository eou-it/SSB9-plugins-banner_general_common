package net.hedtech.banner.general.overall

import grails.converters.JSON
import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.crossproduct.BankRoutingInfo
import net.hedtech.banner.general.system.InstitutionalDescription
import org.springframework.web.context.request.RequestContextHolder
import net.hedtech.banner.general.system.InstitutionalDescription
import org.codehaus.groovy.runtime.InvokerHelper

class DirectDepositAccountCompositeService {

    def directDepositAccountService
    def bankRoutingInfoService
    def currencyFormatService
    def sessionFactory


    /**
     * @desc This method creates or updates a Direct Deposit account
     * @param map
     * @return
     */
    def addorUpdateAccount( def map, setPriority = true ) {
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

        if(setPriority){
            validateNotDuplicate(account)
            account = setNextPriority(account)
        }
        else {
            account.priority = map.priority
        }

        account = directDepositAccountService.createOrUpdate( [domainModel: account] )

        return account
    }
    
    def reorderAccounts( accounts ) {
        def result = [];
        
        // clear out original records
        result.add(directDepositAccountService.delete(accounts))

        // insert records in their new order
        for (acct in accounts) {
            acct.id = null;
            result.add(addorUpdateAccount(acct, false))
        }

        // make sure their account types are correct
        for (acct in accounts) {
            directDepositAccountService.syncApAndHrAccounts(acct)
        }

        result
    }

    /**
     * @desc This method retrieves user payroll allocations and calculates the currency
     *       amount of each allocation based on most recent pay distribution.
     * @return
     */
    def getUserHrAllocations(pidm) {
        def model = [:]
        def lastPayDist

        if (checkIfHrInstalled()) {
            lastPayDist=getLastPayDistribution(pidm)
        }

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

                // Calculate allocated amount (i.e. currency) and allocation as set by user (e.g. 50% or $100)
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
                    def unroundedAmt = totalLeft * pct / 100
                    calcAmt = roundAsCurrency(unroundedAmt)

                    if (calcAmt > totalLeft) {
                        calcAmt = totalLeft
                    }

                    // Allocation as set by user
                    allocationByUser = pct.intValue() + "%"
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
        def accts = DirectDepositAccount.fetchByPidm(account.pidm)
        
        // AP records are created with a priority of 0
        if(account.hrIndicator != 'A' && account.apIndicator == 'A' && accts.size() == 0) {
            account.priority = 0
        }
        else {
            def lowestPriority = accts*.priority.max()
            account.priority = (lowestPriority ? lowestPriority+1 : 1)
        }
        
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

    def getCurrencySymbol() {
        def zeroAmt = formatCurrency(0.0).toString()
        def currencySymbol = ""

        if(zeroAmt.charAt(0) == '0'){
            // curreny symbol should be after digits
            def i = zeroAmt.size()-1

            while(i > 0 && zeroAmt.charAt(i) != '0'){
                i--
            }
            currencySymbol = zeroAmt.substring(i+1)
        }
        else{
            // currency symbol is before digits
            def i = 0

            while(i < zeroAmt.size() && zeroAmt.charAt(i) != '0'){
                i++
            }
            currencySymbol = zeroAmt.substring(0, i)
        }

        currencySymbol
    }

    public def lastPayStub( pidm ) {
       Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
  //   def   sql = new Sql(HibernateSessionFactoryUtil.getConnection())
        def lastPayStubRec

        def lastPaySql =
                """SELECT phrhist_event_date eventDate,
               phrhist_year year,
               phrhist_pict_code pictCode,
               phrhist_payno payNo,
               phrhist_seq_no seqNo,
               phrhist_type_ind typeInd
        FROM  PHRHIST x
        WHERE phrhist_pidm       = ?
        AND   phrhist_disp       >= 42
        AND   phrhist_event_date =
                (SELECT MAX (phrhist_event_date)
                        FROM PHRHIST
                        WHERE phrhist_pidm  = x.phrhist_pidm
                        AND   phrhist_disp  >= 42)"""


        try {
            sql.eachRow(lastPaySql, [pidm]) { row ->
                lastPayStubRec = row.toRowResult()
            }
        } finally {
            sql?.close()
        }
        return lastPayStubRec
    }

    /* Check if there are Direct Deposits in the last pay period for sequence zero*/
    def lastPayDirectDeposit( pidm,year,pictCode,payNo ) {
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        def directDeposit = 'N'

       def lastPayDDSql =
       """    SELECT 'Y'
                FROM PHRDOCM x
               WHERE phrdocm_pidm         = ?
                 AND phrdocm_year         = ?
                 AND phrdocm_pict_code    = ?
                 AND phrdocm_payno        = ?
                 AND phrdocm_seq_no       = 0
                 AND phrdocm_doc_type     = 'D'
               ORDER BY phrdocm_doc_date desc"""


        try {
            sql.eachRow(lastPayDDSql, [pidm,year,pictCode,payNo]) { row ->
                directDeposit = row[0]
            }
        } finally {
            sql?.close()
        }
        return directDeposit
    }

    def payrollDocs( pidm,year,pictCode,payNo,seqNo ) {
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        def payDocs = []

        def payrollDocsSql =
                """    SELECT phrdocm_bank_code bank_rout,
             phrdocm_bank_acct_no,
             phrdocm_acct_type,
             phrdocm_net,
             phrdocm_doc_date,
             phrdocm_doc_type,
             phrdocm_year,
             phrdocm_pict_code,
             phrdocm_payno,
             GXVDIRD_DESC bankName
             FROM  phrdocm  x, GXVDIRD y
             WHERE ( (phrdocm_seq_no = 0)
             OR      (phrdocm_seq_no > 0
             AND EXISTS (select 'x'
                    from phrdocm
                     where phrdocm_pidm      = x.phrdocm_pidm
                     and   phrdocm_year      = x.phrdocm_year
                     and   phrdocm_pict_code = x.phrdocm_pict_code
                     and   phrdocm_payno     = x.phrdocm_payno
                     and   phrdocm_doc_type  = 'D'
                     and   phrdocm_seq_no    = ?
                     and   phrdocm_recon_ind = 'V')  )
                   )
             AND  phrdocm_pidm = ?
             AND  phrdocm_year = ?
             AND  phrdocm_pict_code = ?
             AND  phrdocm_payno = ?
             AND  y.GXVDIRD_CODE_BANK_ROUT_NUM=x.phrdocm_bank_code """


        try {
            sql.eachRow(payrollDocsSql, [seqNo,pidm,year,pictCode,payNo]) { row ->
                payDocs << row.toRowResult()
            }
        } finally {
            sql?.close()
        }
        return payDocs
    }

    def lastPayAmount( pidm ) {
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        def count = 0
        def lastPayAmtRec

        def lastPayAmtSql =
                """SELECT NVL(phrhist_net,0) netAmount, ptrcaln_end_date
                     FROM PTRCALN,
                          PHRHIST X
                    WHERE ptrcaln_year            =  phrhist_year
                      AND ptrcaln_pict_code       =  phrhist_pict_code
                      AND ptrcaln_payno           =  phrhist_payno
                      AND phrhist_pidm            =  ?
                      AND TO_NUMBER(phrhist_disp) >  5
                      AND phrhist_type_ind        <> 'V'
                      AND NOT EXISTS
                            (SELECT 'x'
                               FROM PHRHIST
                              WHERE phrhist_year      = X.phrhist_year
                                AND phrhist_pict_code = X.phrhist_pict_code
                                AND phrhist_payno     = X.phrhist_payno
                                AND phrhist_pidm      = X.phrhist_pidm
                                AND phrhist_seq_no    = X.phrhist_adj_by_seq_no
                                AND phrhist_disp      >= 50
                                AND phrhist_type_ind  = 'V')
                              ORDER BY ptrcaln_end_date desc"""


        try {
            sql.eachRow(lastPayAmtSql, [pidm]) { row ->
                lastPayAmtRec = row.toRowResult()
            }
        } finally {
            sql?.close()
        }
        return lastPayAmtRec
    }

    def getLastPayDistribution(pidm) {

        def model = [:]

        def lastPayStub = lastPayStub(pidm)

        if (lastPayStub) {

            def lastPaidDate = lastPayStub.eventDate
            def pictCode = lastPayStub.pictCode
            def year = lastPayStub.year
            def payNo = lastPayStub.payNo
            def seqNo = lastPayStub.seqNo
            def payTypeInd = lastPayStub.typeInd

            model.hasPayrollHist = true
            model.totalNet = 0
            model.payDate = lastPaidDate
            model.docAccts = []
            def directDeposit = lastPayDirectDeposit (pidm,
                                                      year,
                                                      pictCode,
                                                      payNo)
            if (directDeposit == 'Y') {
                def docs
                if (payTypeInd != 'M') {
                    docs = payrollDocs(pidm, year, pictCode, payNo, 0)
                } else {
                    docs = payrollDocs(pidm, year, pictCode, payNo, seqNo)
                }

                docs.eachWithIndex { acct, i ->
                    model.docAccts[i] = [:]
                    model.docAccts[i].bankName = acct.bankName
                    model.docAccts[i].bankRoutingNumber = acct.bank_rout
                    model.docAccts[i].bankAccountNumber = acct.phrdocm_bank_acct_no
                    model.docAccts[i].accountType = acct.phrdocm_acct_type
                    model.docAccts[i].net = acct.phrdocm_net

                    model.totalNet += acct.phrdocm_net
                }
            } else {

                def lastPayAmtRec = lastPayAmount(pidm)
                if (lastPayAmtRec) {
                    model.totalNet = lastPayAmtRec.netAmount
                }

            }
        }
        return model
    }


    def checkIfHrInstalled() {

        boolean isHrInstalled
        def session = RequestContextHolder?.currentRequestAttributes()?.request?.session

        if (session?.getAttribute("isHrInstalled") != null) {
            isHrInstalled = session.getAttribute("isHrInstalled")
        }else {
            isHrInstalled = InstitutionalDescription.fetchByKey()?.hrInstalled
            session.setAttribute("isHrInstalled",isHrInstalled)
        }
        return isHrInstalled
    }

    /**
     * Round to two decimals, using "half up" rounding (1.765 rounds to 1.77).
     * MORE DETAIL: Round so that 5 reliably rounds up, matching the behavior of the Oracle "round" function when used
     * with a NUMBER.  Without taking care here, a number like 1069.975 coming in as a Double can get changed during the
     * rounding process to 1069.97499999999990905052982270717620849609375, which rounds *down* to 1069.97, while we're
     * expecting 1069.98.  The (convoluted-looking) logic here makes rounding go "half up," as we expect.
     * We're matching the Oracle rounding behavior because that's what's used in the Banner 8 SSB Direct Deposit
     * app that this one is replacing, and we want to be consistent.
     * @param n Number to be rounded
     * @return The rounded number
     */
    private def roundAsCurrency(n) {
        // This "valueOf" line is key in making it round "half up."  If we simply create a BigDecimal as:
        //     def d = new BigDecimal(n)
        // where n is 1069.975, d ends up with a value of 1069.97499999999990905052982270717620849609375,
        // resulting in a round down to 1069.97, while we expected a round up to 1069.98.  Using "valueOf"
        // converts it to a clean 1069.975 which then rounds up correctly.
        def d = BigDecimal.valueOf(n)

        d.setScale(2, BigDecimal.ROUND_HALF_UP)
    }


    def moveToLast(def list, def pos) {
        def result
        if (pos == 0) {
            result = list[pos + 1..list.size() - 1, pos]
        } else if (pos > 0 && pos < list.size() - 1) {
            result = list[0..pos - 1, pos + 1..list.size() - 1, pos]
        } else {
            result = list
        }
        return result
    }


    Closure moveInList={list, item, newIndex->
        assert list && item && newIndex!=null
        int oldIndex=list.indexOf(item)
        if(oldIndex==-1) return null

        list.remove(item)
        return list.plus(newIndex,item)
    }

    def rePrioritizeAccounts(def map, def newPosition) {

        def reOrderInd = true
        def priorityList = []

        def adjustedMapList = []
        def prioritizedList = []

        def newAcct = false

        def itemBeingAdjusted = map

        //checking for mandatory values
        validateRoutingNumExistsInMap(itemBeingAdjusted)
        validateBankRoutingInfo(itemBeingAdjusted.bankRoutingInfo.bankRoutingNum)



        def accountList = directDepositAccountService.getActiveHrAccounts(itemBeingAdjusted?.pidm)
        accountList.sort { it.priority };

        //convert map to object and add it to the account list. both new or existing.
        def domainObject = new DirectDepositAccount()
        def routingObject = new BankRoutingInfo()
//        use(InvokerHelper) {
//            domainObject.setProperties(itemBeingAdjusted)
//            routingObject.setProperties(itemBeingAdjusted.bankRoutingInfo)
//        }
        domainObject.properties = itemBeingAdjusted
        routingObject.properties = itemBeingAdjusted.bankRoutingInfo
        domainObject.bankRoutingInfo = routingObject

        if (!itemBeingAdjusted.containsKey("id") || itemBeingAdjusted?.id == null) {
            itemBeingAdjusted.priority = setNextPriority(itemBeingAdjusted).priority
            itemBeingAdjusted.id = -1
            validateNotDuplicate(itemBeingAdjusted)
            newAcct = true
            domainObject.id = -1
            domainObject.priority = itemBeingAdjusted.priority
            accountList << domainObject
        } else if (itemBeingAdjusted?.id != null) {
            def pos = accountList.findIndexOf { iterator ->
                iterator.id == itemBeingAdjusted.id
            }
            domainObject.id = itemBeingAdjusted.id
            domainObject.priority = (accountList[pos] as DirectDepositAccount).priority
            accountList[pos] = domainObject
        }
        //make sure there are no more than one 100 percent record.
        if (accountList.count { it.percent == 100 } > 1) {
            throw new ApplicationException(DirectDepositAccount, "@@r1:recordAlreadyExists@@")
        }

        //make sure new position is not greater than the number of hr accounts.
        if (!newAcct) {
            if (newPosition > accountList.size()) {
                throw new ApplicationException(DirectDepositAccount, "@@r1:recordAlreadyExists@@")
            }
        }

        //DETERMINING the remaining position
        def remainingIndex = accountList.findIndexOf { iterator ->
            iterator.percent == 100
        }
        //if remaining is found
        def remainingPosition
        if (remainingIndex > -1) {
            remainingPosition = remainingIndex+1
            //remaining should be at the end. otherwise move remaining to Last
            if (remainingPosition != accountList.size()) {
                accountList = moveToLast(accountList, remainingIndex)

                remainingIndex = accountList.findIndexOf { iterator ->
                    iterator.percent == 100
                }
                remainingPosition = remainingIndex+1
            }
        }

        //DETERMINING the new position for the item that is sent.
        //If remaining is present, it should be at the end at this point.
        //for existing account, if remaining is attempted to change its position, should prevent that.
        if (remainingIndex > -1) {
            //if remaining is sent, newPosition should be last.
            //if the new position that is sent, happens to be the position of "Remaining" record, then
            //change the new position to move back by one position.
            if (itemBeingAdjusted?.percent == 100) {
                newPosition = accountList.size()
            } else if (newPosition == remainingPosition || newPosition > remainingPosition) {
                newPosition = remainingPosition - 1
            }
        }


        //DETERMINING the existing position of the account being adjusted.
        //if the new position is same as the position of the item being adjusted,
        //then no reordering will be done.
        def positionBeingUpdated
        if (newAcct) {
            positionBeingUpdated = (accountList.findIndexOf { iterator ->
                iterator.id == itemBeingAdjusted.id
            })+1
        } else {
            positionBeingUpdated = (accountList.findIndexOf { iterator ->
                iterator.id == itemBeingAdjusted?.id
            }) + 1
        }

        //with all the values figured out, actual reordering happens for the accountList
        if (reOrderInd) {
            //move the input account to the new position in the list.
            def adjItem = [:]
            adjItem.id = itemBeingAdjusted.id
            adjItem.newPosition = newPosition
            adjustedMapList << adjItem

            if (newPosition > positionBeingUpdated) {
                for (int i = newPosition; i > positionBeingUpdated; i--) {
                    def adjustedPosition = i - 1
                    def adjItem1 = [:]
                    adjItem1.id = (accountList[adjustedPosition] as DirectDepositAccount).id
                    adjItem1.newPosition = adjustedPosition
                    adjustedMapList << adjItem1
                }
            }

            if (newPosition < positionBeingUpdated) {
                for (int i = newPosition; i < positionBeingUpdated; i++) {
                    def adjustedPosition = i + 1
                    def adjItem1 = [:]
                    adjItem1.id = (accountList[i - 1] as DirectDepositAccount).id
                    adjItem1.newPosition = adjustedPosition
                    adjustedMapList << adjItem1
                }
            }

            //move the accounts in the accountList
            adjustedMapList.sort { it.newPosition };
            adjustedMapList.each {
                def acct = accountList.find { p -> p.id == it.id } as DirectDepositAccount
                accountList=moveInList(accountList,acct,it.newPosition-1)
            }

            //collect the priority numbers
            accountList.each {
                priorityList << it.priority
            }
            priorityList.sort { it }

            //remove the records before updating hibernate objects
            accountList.each {
                def pid = it.id
                if (pid != -1) {
                    directDepositAccountService.delete(it)
                }
            }

            //Assign the priority numbers in order all the accounts in the list.
            int j=0
            accountList.each {
                it.priority = priorityList[j++]
            }

            accountList.sort { it.priority }

            accountList.each {
                def item = it as DirectDepositAccount
                item.version = null
                item.id = null
                directDepositAccountService.create(item)
            }
        }
        return directDepositAccountService.getActiveHrAccounts(map.pidm)
    }

}
