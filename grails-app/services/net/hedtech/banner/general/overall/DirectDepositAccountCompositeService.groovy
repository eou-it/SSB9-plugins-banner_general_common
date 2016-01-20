package net.hedtech.banner.general.overall

import grails.converters.JSON
import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.crossproduct.BankRoutingInfo
import net.hedtech.banner.general.system.InstitutionalDescription
import org.springframework.web.context.request.RequestContextHolder
import net.hedtech.banner.general.system.InstitutionalDescription

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
                    def unroundedAmt = totalLeft * pct / 100
                    calcAmt = roundAsCurrency(unroundedAmt)

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
        def directDeposit

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
                lastPayAmtRec = row[0]
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
                model.totalNet = lastPayAmtRec.netAmount
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

}
