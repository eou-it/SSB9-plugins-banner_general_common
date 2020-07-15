/********************************************************************************
  Copyright 2016-2019 Ellucian Company L.P. and its affiliates.
********************************************************************************/
package net.hedtech.banner.general.overall

import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.InterceptedUrl
import grails.util.Holders
import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.crossproduct.BankRoutingInfo
import net.hedtech.banner.security.BannerAccessDecisionVoter
import org.grails.web.json.JSONObject
import org.springframework.security.access.AccessDecisionVoter
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.context.request.RequestContextHolder
import net.hedtech.banner.general.system.InstitutionalDescription
import org.codehaus.groovy.runtime.InvokerHelper

@Transactional
class DirectDepositAccountCompositeService {

    def directDepositAccountService
    def bankRoutingInfoService
    def currencyFormatHelperService
    def userRoleService
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
            validateOnlyOneAP(account)
            directDepositAccountService.validateNotDuplicate(account)
            account = setNextPriority(account)
        }
        else {
            account.priority = map.priority
        }

        account = directDepositAccountService.createOrUpdate( [domainModel: account] )

        return account
    }

    def reorderAccounts( accounts ) {
        def result = []
        def accountsForResult = []
        def unchanged = []
        def toBeUpdated = []
        def currentAllocations = directDepositAccountService.getActiveHrAccounts(accounts[0].pidm).collectEntries {[it.id as Long, it]}

        accounts.each {
            def existingAcct = currentAllocations[it.id as Long]

            if (isAccountsMatch(existingAcct, it)) {
                unchanged << it
            } else {
                toBeUpdated << it
            }
        }

        // Delete changed records and store results of delete operation in first element of result list
        result << directDepositAccountService.delete(toBeUpdated)

        // Insert updated records
        toBeUpdated.each {
            it.id = null
            accountsForResult << addorUpdateAccount(it, false)
        }

        // Add in the unchanged records
        unchanged.each {
            accountsForResult << currentAllocations[it.id as Long]
        }

        result.addAll(accountsForResult.sort{it.priority})

        result
    }

    /**
     * @desc This method retrieves user payroll allocations and calculates the currency
     *       amount of each allocation based on most recent pay distribution.
     * @return
     */
    def getUserHrAllocations(pidm) {
        def model = [:]

        def allocations = directDepositAccountService.getActiveHrAccounts(pidm).sort {it.priority}
        def allocList = []

        // Create model for each allocation
        allocations.each {
            // Populate model, ignoring generic domain fields
            def alloc = it
            def allocModel = alloc.class.declaredFields.findAll { !it.synthetic && it.name != 'constraints' && it.name != 'log' }.collectEntries {
                [ (it.name):alloc."$it.name" ]
            }

            // Add to list of allocations
            allocList.push(allocModel)
        }

        model.allocations = allocList

        model
    }

    /**
     * Retrieve user payroll allocations and convert to a list of maps.
     * @param pidm
     * @return List of allocation maps
     */
    def getUserHrAllocationsAsListOfMaps(pidm) {
        def allocations = getUserHrAllocations(pidm).allocations

        return [
            allocations: directDepositAccountService.marshallAccountsToMinimalStateForUi(allocations)
        ]

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

    def validateOnlyOneAP(account) {
        if(account.apIndicator == 'A'){
            if(directDepositAccountService.fetchApAccountsByPidm(account.pidm).size() > 0) {
                throw new ApplicationException(DirectDepositAccount, "@@r1:apAccountAlreadyExists@@")
            }
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

    private boolean isAccountsMatch(acct1, acct2) {
        if (!(acct1 && acct2)) {
            return false
        }

        // TODO: ensure that the newly implemented code for Grails 3 below meets the intended functionality (commented)
        // before clearing the commented lines.  JDC 4/19
//        def acct1Priority = acct1.priority == JSONObject.NULL ? null : acct1.priority
//        def acct2Priority = acct2.priority == JSONObject.NULL ? null : acct2.priority
//        def acct1Amount = acct1.amount == JSONObject.NULL ? null : acct1.amount
//        def acct2Amount = acct2.amount == JSONObject.NULL ? null : acct2.amount
//        def acct1Percent = acct1.percent == JSONObject.NULL ? null : acct1.percent
//        def acct2Percent = acct2.percent == JSONObject.NULL ? null : acct2.percent
//
//        def priorityMatch = acct1Priority == acct2Priority
//        def amountMatch = acct1Amount == acct2Amount
//        def percentMatch = acct1Percent == acct2Percent

        def priorityMatch = (acct1.priority == acct2.priority)
        def amountMatch = (acct1.amount == acct2.amount)
        def percentMatch = (acct1.percent == acct2.percent)

        priorityMatch && amountMatch && percentMatch
    }

    def getCurrencySymbol() {
        def zeroAmt = currencyFormatHelperService.formatCurrency(0.0).toString()
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
            lastPayStubRec = sql.firstRow(lastPaySql, [pidm])
        } finally {
//            sql?.close()
        }
        return lastPayStubRec
    }

    /* Check if there are Direct Deposits in the last pay period for sequence zero*/
    def lastPayDirectDeposit( pidm,year,pictCode,payNo ) {
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        def directDeposit = 'N'

       def lastPayDDSql =
       """    SELECT 'Y' ddInd
                FROM PHRDOCM x
               WHERE phrdocm_pidm         = ?
                 AND phrdocm_year         = ?
                 AND phrdocm_pict_code    = ?
                 AND phrdocm_payno        = ?
                 AND phrdocm_seq_no       = 0
                 AND phrdocm_doc_type     = 'D'
               ORDER BY phrdocm_doc_date desc"""


        try {
            directDeposit = sql.firstRow(lastPayDDSql, [pidm,year,pictCode,payNo])?.ddInd
        } finally {
//            sql?.close()
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
//            sql?.close()
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
            lastPayAmtRec = sql.firstRow(lastPayAmtSql, [pidm])
        } finally {
//            sql?.close()
        }
        return lastPayAmtRec
    }

    def getLastPayDistribution(pidm) {

        def model = [:]

        if (checkIfHrInstalled()) {
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
                def directDeposit = lastPayDirectDeposit(pidm,
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

                        model.docAccts[i].net = currencyFormatHelperService.formatCurrency(model.docAccts[i].net)
                    }
                } else {

                    def lastPayAmtRec = lastPayAmount(pidm)
                    if (lastPayAmtRec) {
                        model.totalNet = lastPayAmtRec.netAmount
                    }

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

        def priorityList = []
        def adjustedMapList = []
        def newAcct = false
        def itemBeingAdjusted = map

        //checking for mandatory values
        validateRoutingNumExistsInMap(itemBeingAdjusted)
        itemBeingAdjusted?.bankRoutingInfo = validateBankRoutingInfo(itemBeingAdjusted.bankRoutingInfo.bankRoutingNum)

        def accountList = directDepositAccountService.getActiveHrAccounts(itemBeingAdjusted?.pidm)
        accountList.sort { it.priority };

        def accountListOriginalValues = accountList.collectEntries {
            [it.id as Long, [priority: it.priority, amount: it.amount, percent: it.percent]]
        }

        //convert map to object and add it to the account list. both new or existing.
        def domainObject = new DirectDepositAccount()
        def routingObject = new BankRoutingInfo()

        // TODO: the below block was updated for grails 3 due to 'Cannot cast object... due to:
        // "groovy.lang.ReadOnlyPropertyException: Cannot set readonly property: class for class:
        // net.hedtech.banner.general.crossproduct.BankRoutingInfo"' exception in integration test.
        // Perhaps the "JSON Converter changes" section in http://docs.grails.org/3.1.0.RC2/guide/upgrading.html#
        // would be a help in reverting to the commented code here??  If not, use the newly implemented code below. JDC 4/19
//        use(InvokerHelper) {
//            domainObject.setProperties(itemBeingAdjusted)
//            routingObject.setProperties(itemBeingAdjusted.bankRoutingInfo)
//        }
        use(InvokerHelper) {
            def bankRoutingInfo = itemBeingAdjusted.bankRoutingInfo
            routingObject.setProperties(bankRoutingInfo?.getProperties())
            itemBeingAdjusted.bankRoutingInfo = null
            domainObject.setProperties(itemBeingAdjusted)
            domainObject.bankRoutingInfo = routingObject
            itemBeingAdjusted.bankRoutingInfo = bankRoutingInfo
        }

        domainObject.bankRoutingInfo = routingObject

        if (!itemBeingAdjusted.containsKey("id") || itemBeingAdjusted?.id == null) {
            itemBeingAdjusted.priority = setNextPriority(itemBeingAdjusted).priority
            itemBeingAdjusted.id = -1
            directDepositAccountService.validateNotDuplicate(itemBeingAdjusted)
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
            throw new ApplicationException(DirectDepositAccount, "@@r1:oneRemaining@@")
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
        priorityList = (accountList*.priority).sort{it}

        def toBeUpdated = []
        int j=0

        accountList.each {
            def item = it as DirectDepositAccount

            // Set up the values that will be checked to determine if this allocation has been changed.  Note that
            // we use the priority as it *will* be upon save, not what it is now.  We can't actually change the
            // priority yet, because doing so would result in a "unique constraint exception" when the delete
            // operation is attempted.
            def itemValuesToCheck = [priority: priorityList[j++], amount: item.amount, percent: item.percent]

            if (!isAccountsMatch(itemValuesToCheck, accountListOriginalValues[item.id])) {
                if (item.id != -1) {
                    directDepositAccountService.delete(item)
                }

                item.version = null
                item.id = null
                toBeUpdated << item
            }
        }

        // Assign the priority numbers in order on all the accounts in the list.
        // (Updating accountList rather than toBeUpdated to keep the priority values matched up to the correct
        // accounts.  The updated priority will occur in toBeUpdated as well, of course, since its elements are
        // pointers to the same elements accountList is pointing to.)
        j=0

        accountList.each {
            it.priority = priorityList[j++]
        }

        directDepositAccountService.create(toBeUpdated)

        directDepositAccountService.getActiveHrAccounts(map.pidm)
    }

    def fetchEmployeeUpdatableSetting () {
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        def ddUpdatableIndRec

        def ddUpdatableIndSql = """SELECT PTRINST_DD_WEB_UPDATE_IND FROM PTRINST"""

        try {
            ddUpdatableIndRec = sql.firstRow(ddUpdatableIndSql)
        } finally {
//            sql?.close()
        }

        ddUpdatableIndRec?.PTRINST_DD_WEB_UPDATE_IND
    }

    /**
     * Obtain from session cache if available, otherwise fetch from database.
     * @return
     */
    def getEmployeeUpdatableSetting () {
        def EMPLOYEE_UPDATABLE_SETTING = 'EMPLOYEE_UPDATABLE_SETTING'
        def session = RequestContextHolder.currentRequestAttributes().request.session
        def updatable = session.getAttribute(EMPLOYEE_UPDATABLE_SETTING)

        if (!updatable) {
            updatable = fetchEmployeeUpdatableSetting()
            session.setAttribute(EMPLOYEE_UPDATABLE_SETTING, updatable)
        }

        updatable
    }

    def areAccountsUpdatablePerUrlRoleMapping() {
        def urlList = Holders.config.grails.plugin?.springsecurity?.interceptUrlMap
        if (urlList == null) {
            urlList = Holders.config.grails.plugins?.springsecurity?.interceptUrlMap
        }
        String updateUrl = '/ssb/UpdateAccount/**'
        def updateList = []
        def accessKeyName = null

        if (urlList) {
            accessKeyName = urlList[0] instanceof InterceptedUrl ? 'configAttributes' : urlList[0].containsKey('access') ? 'access' : null
        }

        if (accessKeyName) {
            /*When the UpdateAccount roles are different than the roles set in the configuration, there is a new entry in the URLList appended to the list.
            * Because the customized roles in the database should take precedence over the default roles, the updateList should only be set according to the roles in the
            * LAST entry that matches the pattern, as that is the entry based off of the database roles.*/
            urlList.each {
                if (it.pattern == updateUrl) {
                    // This list contains Spring SecurityConfig objects, which implement Spring's ConfigAttribute,
                    // which is what role config items need to be for the vote call below.
                    updateList = it[accessKeyName]
                }
            }
        }

        if (updateList == []) {
            return false
        }

        def voter = new BannerAccessDecisionVoter()
        return AccessDecisionVoter.ACCESS_GRANTED == voter.vote(SecurityContextHolder?.context?.authentication, updateUrl, updateList);
    }

    /**
     * If the URL-to-role mappings do not allow access to update accounts, then, of course, accounts are not updatable.
     * Otherwise:
     * Accounts are always updatable for students, who only have access to AP accounts.
     * Accounts are updatable for employees in the following two cases:
     *   1) the Banner admin pages PTRINST "Employee May Update Direct Deposit Records" indicator is checked
     *   2) the HR product is not installed
     * @return True or false
     */
    def areAccountsUpdatable() {
        if (!areAccountsUpdatablePerUrlRoleMapping()) {
            return false
        }

        if (userRoleService.hasUserRole('EMPLOYEE') && checkIfHrInstalled()) {
            def updatable = getEmployeeUpdatableSetting()
            return updatable ? updatable == 'Y' : true
        } else {
            return true
        }
    }

    def isVerifyAccountNumberEnabled() {
        def isVerifyAccountNumberEnabled = Holders?.config?.'directDeposit.verifyAccountNumberFieldEnabled'
        return isVerifyAccountNumberEnabled ? isVerifyAccountNumberEnabled : false
    }

}
