/*******************************************************************************
 Copyright 2015-2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.util.Holders
import groovy.sql.Sql
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.crossproduct.BankRoutingInfo
import net.hedtech.banner.general.overall.DirectDepositAccountCompositeService
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import net.hedtech.banner.security.BannerAuthenticationToken
import net.hedtech.banner.security.BannerAuthenticationToken
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.context.i18n.LocaleContextHolder
import org.apache.log4j.Logger
import org.springframework.web.context.request.RequestContextHolder


/**
 *
 */
@Integration
@Rollback
class DirectDepositAccountCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    def directDepositAccountCompositeService
    def selfServiceBannerAuthenticationProvider
    def userRoleService
    private static final log = Logger.getLogger(DirectDepositAccountCompositeServiceIntegrationTests.class)

    def oldHoldersConfig = Holders.config
    def testBankRoutingInfo0 = [
        bankRoutingNum: '234798944',
        bankName:'TTTT'
    ]

    def testAccountMap0 = [
        accountType: 'C',
        bankAccountNum: '22334455',
        bankRoutingInfo: testBankRoutingInfo0,
        documentType: 'D',
        id: 0,
        apIndicator: 'A',
        hrIndicator: 'I',
        intlAchTransactionIndicator: 'N',
        pidm: 95999,
        status: 'P'
    ]

    def testAccountMap1 = [
            accountType: 'C',
            bankAccountNum: '22334455',
            bankRoutingInfo: testBankRoutingInfo0,
            documentType: 'D',
            id: 0,
            apIndicator: 'I',
            hrIndicator: 'A',
            intlAchTransactionIndicator: 'N',
            pidm: 95999,
            status: 'P'
    ]

    BannerAuthenticationToken bannerAuthenticationToken

    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']
        super.setUp()
        if (oldHoldersConfig == null && Holders.config != null) {
            oldHoldersConfig = Holders.config
        }
    }


    @After
    public void tearDown() {
        super.tearDown()
        Holders.config = oldHoldersConfig
        super.logout()
    }

    @Test
    void testAddorUpdateAccount() {
        def account

        account = directDepositAccountCompositeService.addorUpdateAccount(testAccountMap0)

        assertEquals("22334455", account.bankAccountNum)
    }

    @Test
    void testAddDuplicateAccount() {
        def account1, account2

        account1 = directDepositAccountCompositeService.addorUpdateAccount(testAccountMap1)

        try {
            account2 = directDepositAccountCompositeService.addorUpdateAccount(testAccountMap1)
            fail("I should have received an error but it passed; @@r1:recordAlreadyExists@@ ")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "@@r1:recordAlreadyExists@@"
        }
    }

    @Test
    void testAddApAccountToPayroll() {
        def account1, account2

        account1 = directDepositAccountCompositeService.addorUpdateAccount(testAccountMap0)

        testAccountMap0.hrIndicator = "A"
        testAccountMap0.apIndicator = "I"
        account2 = directDepositAccountCompositeService.addorUpdateAccount(testAccountMap0)

        assertNotEquals(account1.id, account2.id)
        assertEquals("A", account2.hrIndicator)
    }


    @Test
    void testRePrioritizeExistingAccount() {
        def pidm = PersonUtility.getPerson("GDP000001").pidm
        SecurityContextHolder?.context?.authentication?.principal?.pidm = pidm

        //   def existingItem = DirectDepositAccount.findById(1164) as DirectDepositAccount
        def account1

        testAccountMap0.pidm = pidm
        testAccountMap0.hrIndicator = "A"
        testAccountMap0.apIndicator = "I"
        account1 = directDepositAccountCompositeService.addorUpdateAccount(testAccountMap0)

        def testBankRoutingInfo1 = [
                bankRoutingNum: '748972234'
        ]

        def existingItemMap0 = [
                accountType: 'C',
                bankAccountNum: '22334455',
                bankRoutingInfo: testBankRoutingInfo0,
                documentType: 'D',
                id: account1.id,
                apIndicator: 'I',
                hrIndicator: 'A',
                intlAchTransactionIndicator: 'N',
                pidm: pidm,
                status: 'P',
                priority: account1.priority
        ]


        //   def itemMap = existingItem.properties
        def itemMap = existingItemMap0
        itemMap.accountType = "C"
        itemMap.percent = 65

        def newPosition = 1

        def list = directDepositAccountCompositeService.rePrioritizeAccounts(itemMap, newPosition)

        def list1 = list.sort{it.priority}

        list1.each {
            def dd=it as DirectDepositAccount
            log.debug("account: " + dd.bankAccountNum );
            log.info("account: " + dd.percent );
            log.info("account: " + dd.priority );
        }


        assertEquals true, list1.size() > 0
    }

    @Test
    void testRePrioritizeExistingAccountByMovingFirstToSecondAndCheckingLastModified() {
        def pidm = PersonUtility.getPerson("GDP000005").pidm
        def accts = directDepositAccountCompositeService.getUserHrAllocations(pidm).allocations //36743
        SecurityContextHolder?.context?.authentication?.principal?.pidm = pidm

        def firstAcct = accts[0]

        def existingItemMap0 = [
                accountType: firstAcct.accountType,
                bankAccountNum: firstAcct.bankAccountNum,
                bankRoutingInfo: [bankRoutingNum: firstAcct.bankRoutingInfo.bankRoutingNum],
                documentType: firstAcct.documentType,
                id: firstAcct.id,
                apIndicator: firstAcct.apIndicator,
                hrIndicator: firstAcct.hrIndicator,
                intlAchTransactionIndicator: firstAcct.intlAchTransactionIndicator,
                pidm: firstAcct.pidm,
                status: firstAcct.status,
                priority: firstAcct.priority
        ]

        def newPriority = 2

        def result = directDepositAccountCompositeService.rePrioritizeAccounts(existingItemMap0, newPriority)

        result.sort{it.priority}

        assertEquals 3, result.size()
        assertTrue('Allocation "modified by" times differ.', Math.abs(result[0].lastModified.getTime() - result[1].lastModified.getTime()) < 1800)
        assertTrue('Allocation "modified by" times are similar.', Math.abs(result[1].lastModified.getTime() - result[2].lastModified.getTime()) > 1800)
    }

    @Test
    void testRePrioritizeExistingAccountByMovingLastToFirstAndCheckingLastModified() {
        def pidm = PersonUtility.getPerson("GDP000005").pidm
        def accts = directDepositAccountCompositeService.getUserHrAllocations(pidm).allocations //36743
        SecurityContextHolder?.context?.authentication?.principal?.pidm = pidm

        def lastAcct = accts[accts.size()-1]

        def existingItemMap0 = [
                accountType: lastAcct.accountType,
                bankAccountNum: lastAcct.bankAccountNum,
                bankRoutingInfo: [bankRoutingNum: lastAcct.bankRoutingInfo.bankRoutingNum],
                documentType: lastAcct.documentType,
                id: lastAcct.id,
                apIndicator: lastAcct.apIndicator,
                hrIndicator: lastAcct.hrIndicator,
                intlAchTransactionIndicator: lastAcct.intlAchTransactionIndicator,
                pidm: lastAcct.pidm,
                status: lastAcct.status,
                priority: lastAcct.priority
        ]

        def newPriority = 1

        def result = directDepositAccountCompositeService.rePrioritizeAccounts(existingItemMap0, newPriority)

        result.sort{it.priority}

        assertEquals 3, result.size()
        assertTrue('Allocation "modified by" times differ.', Math.abs(result[0].lastModified.getTime() - result[1].lastModified.getTime()) < 1800)
        assertTrue('Allocation "modified by" times differ.', Math.abs(result[1].lastModified.getTime() - result[2].lastModified.getTime()) < 1800)
    }

    @Test
    void testRePrioritizeExistingAccountByMovingFirstToLastWhereLastIsRemainingAndCheckingLastModified() {
        def pidm = PersonUtility.getPerson("GDP000005").pidm
        def accts = directDepositAccountCompositeService.getUserHrAllocations(pidm).allocations //36743
        SecurityContextHolder?.context?.authentication?.principal?.pidm = pidm

        def firstAcct = accts[0]

        def existingItemMap0 = [
                accountType: firstAcct.accountType,
                bankAccountNum: firstAcct.bankAccountNum,
                bankRoutingInfo: [bankRoutingNum: firstAcct.bankRoutingInfo.bankRoutingNum],
                documentType: firstAcct.documentType,
                id: firstAcct.id,
                apIndicator: firstAcct.apIndicator,
                hrIndicator: firstAcct.hrIndicator,
                intlAchTransactionIndicator: firstAcct.intlAchTransactionIndicator,
                pidm: firstAcct.pidm,
                status: firstAcct.status,
                priority: firstAcct.priority
        ]

        def newPriority = 3

        def result = directDepositAccountCompositeService.rePrioritizeAccounts(existingItemMap0, newPriority)

        result.sort{it.priority}

        assertEquals 3, result.size()
        assertTrue('Allocation "modified by" times differ.', Math.abs(result[0].lastModified.getTime() - result[1].lastModified.getTime()) < 1800)

        // "Modified by" time on last record has not been updated (as middle record has),
        // because last one is "Remaining" and is forced to stay in last position.
        assertTrue('Allocation "modified by" times are similar.', Math.abs(result[1].lastModified.getTime() - result[2].lastModified.getTime()) > 1800)
    }

    @Test
    void testRePrioritizeExistingAccountByMovingLastToFirstWhereLastIsNotRemainingAndCheckingLastModified() {
        def pidm = PersonUtility.getPerson("GDP000005").pidm
        def accts = directDepositAccountCompositeService.getUserHrAllocations(pidm).allocations //36743
        SecurityContextHolder?.context?.authentication?.principal?.pidm = pidm

        def lastAcct = accts[accts.size()-1]

        def existingItemMap0 = [
                accountType: lastAcct.accountType,
                bankAccountNum: lastAcct.bankAccountNum,
                bankRoutingInfo: [bankRoutingNum: lastAcct.bankRoutingInfo.bankRoutingNum],
                documentType: lastAcct.documentType,
                id: lastAcct.id,
                apIndicator: lastAcct.apIndicator,
                hrIndicator: lastAcct.hrIndicator,
                intlAchTransactionIndicator: lastAcct.intlAchTransactionIndicator,
                pidm: lastAcct.pidm,
                status: lastAcct.status,
                priority: lastAcct.priority
        ]

        existingItemMap0.percent = 10

        def newPriority = 1

        def result = directDepositAccountCompositeService.rePrioritizeAccounts(existingItemMap0, newPriority)

        result.sort{it.priority}

        assertEquals 3, result.size()
        assertTrue('Allocation "modified by" times differ.', Math.abs(result[0].lastModified.getTime() - result[1].lastModified.getTime()) < 1800)
        assertTrue('Allocation "modified by" times differ.', Math.abs(result[1].lastModified.getTime() - result[2].lastModified.getTime()) < 1800)
    }


    @Test
    void testRePrioritizeNewAccount() {
        def pidm = PersonUtility.getPerson("GDP000001").pidm

        def testBankRoutingInfo1 = [
                bankRoutingNum: '748972234'
        ]

        def newAccountMap0 = [
                accountType: 'C',
                bankAccountNum: '777777',
                bankRoutingInfo: testBankRoutingInfo1,
                documentType: 'D',
//                id: 1329,
                percent: 10,
                apIndicator: 'A',
                hrIndicator: 'A',
                priority: 3,
                intlAchTransactionIndicator: 'N',
                pidm: 6,
                status: 'P'
        ]

        def itemMap = newAccountMap0

        def newPosition = 1

        def list = directDepositAccountCompositeService.rePrioritizeAccounts(itemMap, newPosition)

        def list1 = list.sort{it.priority}

        assertEquals true, list1.size() > 0

    }

    @Test
    void testRePrioritizeAddingNewAccountToFirstPositionAndCheckingLastModified() {
        def pidm = PersonUtility.getPerson("GDP000005").pidm
        SecurityContextHolder?.context?.authentication?.principal?.pidm = pidm

        def newAccountMap0 = [
                accountType: 'C',
                bankAccountNum: '777777',
                bankRoutingInfo: [bankRoutingNum: '748972234'],
                documentType: 'D',
                percent: 10,
                apIndicator: 'A',
                hrIndicator: 'A',
                priority: 3,
                intlAchTransactionIndicator: 'N',
                pidm: pidm,
                status: 'P'
        ]

        def newPosition = 1

        def result = directDepositAccountCompositeService.rePrioritizeAccounts(newAccountMap0, newPosition)

        result.sort{it.priority}

        assertEquals 4, result.size()
        assertTrue('Allocation "modified by" times differ.', Math.abs(result[0].lastModified.getTime() - result[1].lastModified.getTime()) < 1800)
        assertTrue('Allocation "modified by" times differ.', Math.abs(result[1].lastModified.getTime() - result[2].lastModified.getTime()) < 1800)
        assertTrue('Allocation "modified by" times differ.', Math.abs(result[2].lastModified.getTime() - result[3].lastModified.getTime()) < 1800)
    }

    @Test
    void testRePrioritizeAddingNewAccountToSecondPositionAndCheckingLastModified() {
        def pidm = PersonUtility.getPerson("GDP000005").pidm
        SecurityContextHolder?.context?.authentication?.principal?.pidm = pidm

        def newAccountMap0 = [
                accountType: 'C',
                bankAccountNum: '777777',
                bankRoutingInfo: [bankRoutingNum: '748972234'],
                documentType: 'D',
                percent: 10,
                apIndicator: 'A',
                hrIndicator: 'A',
                priority: 3,
                intlAchTransactionIndicator: 'N',
                pidm: pidm,
                status: 'P'
        ]

        def newPosition = 2

        def result = directDepositAccountCompositeService.rePrioritizeAccounts(newAccountMap0, newPosition)

        result.sort{it.priority}

        assertEquals 4, result.size()
        assertTrue('Allocation "modified by" times are similar.', Math.abs(result[0].lastModified.getTime() - result[1].lastModified.getTime()) > 1800)
        assertTrue('Allocation "modified by" times differ.', Math.abs(result[1].lastModified.getTime() - result[2].lastModified.getTime()) < 1800)
        assertTrue('Allocation "modified by" times differ.', Math.abs(result[2].lastModified.getTime() - result[3].lastModified.getTime()) < 1800)
    }

    @Test
    void testRePrioritizeAddingNewAccountToLastPositionWhereLastIsRemainingAndCheckingLastModified() {
        def pidm = PersonUtility.getPerson("GDP000005").pidm
        SecurityContextHolder?.context?.authentication?.principal?.pidm = pidm

        def newAccountMap0 = [
                accountType: 'C',
                bankAccountNum: '777777',
                bankRoutingInfo: [bankRoutingNum: '748972234'],
                documentType: 'D',
                percent: 10,
                apIndicator: 'A',
                hrIndicator: 'A',
                priority: 3,
                intlAchTransactionIndicator: 'N',
                pidm: pidm,
                status: 'P'
        ]

        def newPosition = 4

        def result = directDepositAccountCompositeService.rePrioritizeAccounts(newAccountMap0, newPosition)

        result.sort{it.priority}

        assertEquals 4, result.size()
        assertTrue('Allocation "modified by" times differ.', Math.abs(result[0].lastModified.getTime() - result[1].lastModified.getTime()) < 1800)
        assertTrue('Allocation "modified by" times are similar.', Math.abs(result[1].lastModified.getTime() - result[2].lastModified.getTime()) > 1800)
        assertTrue('Allocation "modified by" times differ.', Math.abs(result[2].lastModified.getTime() - result[3].lastModified.getTime()) < 1800)
    }


    @Test
    void testGetLastPayDistribution() {
        def pidm = PersonUtility.getPerson("HOP510001").pidm

        def lastPayDist = directDepositAccountCompositeService.getLastPayDistribution(pidm)

        assertEquals true, lastPayDist.hasPayrollHist
    }

    @Test
    void testGetLastPayDistributionWithHrNotInstalled() {
        def pidm = PersonUtility.getPerson("HOP510001").pidm
        executeCustomSQL "update GUBINST set GUBINST_HUMANRE_INSTALLED = 'N'"

        def lastPayDist = directDepositAccountCompositeService.getLastPayDistribution(pidm)

        assertNotNull lastPayDist
        assertEquals 0, lastPayDist.size()
    }


    @Test
    /**
     * No accounts
     */
    void testGetUserHrAllocationsNoAccounts() {
        def pidm = PersonUtility.getPerson("GDP000001").pidm
        def allocationsObj = directDepositAccountCompositeService.getUserHrAllocations(pidm) //36732 // No accounts

        // Assert domain values
        assertNotNull allocationsObj
        assertNotNull allocationsObj.allocations
        assertEquals 0, allocationsObj.allocations.size()
    }

    @Test
    /**
     * One account:
     *   100% allocated
     */
    void testGetUserHrAllocationsOneAccount() {
        def pidm = PersonUtility.getPerson("GDP000003").pidm
        def allocationsObj = directDepositAccountCompositeService.getUserHrAllocations(pidm) //49548 // One account

        // Assert domain values
        assertNotNull allocationsObj
        assertNotNull allocationsObj.allocations
        assertEquals 1, allocationsObj.allocations.size()

        def allocation = allocationsObj.allocations[0]

        assertNotNull allocation.id
        assertEquals "1293902", allocation.bankAccountNum
        assertEquals "C", allocation.accountType
        assertNull allocation.amount
        assertTrue(99.9 < allocation.percent && allocation.percent < 100.1)
    }

    @Test
    /**
     * Three accounts:
     *   Priority 1) $77.00 allocated
     *   Priority 2) 50% allocated
     *   Priority 3) 100% allocated
     */
    void testGetUserHrAllocationsThreeAccounts() {
        def pidm = PersonUtility.getPerson("GDP000005").pidm
        def allocationsObj = directDepositAccountCompositeService.getUserHrAllocations(pidm) //36743 // Two accounts

        // Assert domain values
        assertNotNull allocationsObj
        assertNotNull allocationsObj.allocations
        assertEquals 3, allocationsObj.allocations.size()

        def allocation = allocationsObj.allocations[0]

        assertNotNull allocation.id
        assertEquals "736900542", allocation.bankAccountNum
        assertEquals "C", allocation.accountType
        assertTrue(76.9 < allocation.amount && allocation.amount < 77.1)
        assertNull allocation.percent

        allocation = allocationsObj.allocations[1]

        assertNotNull allocation.id
        assertEquals "95003546", allocation.bankAccountNum
        assertEquals "S", allocation.accountType
        assertNull allocation.amount
        assertTrue(49.9 < allocation.percent && allocation.percent < 50.1)

        allocation = allocationsObj.allocations[2]

        assertNotNull allocation.id
        assertEquals "67674852", allocation.bankAccountNum
        assertEquals "C", allocation.accountType
        assertNull allocation.amount
        assertTrue(99.9 < allocation.percent && allocation.percent < 100.1)
    }

    @Test
    /**
     * No accounts
     */
    void testGetUserHrAllocationsAsListOfMapsNoAccounts() {
        def pidm = PersonUtility.getPerson("GDP000001").pidm
        def allocationsObj = directDepositAccountCompositeService.getUserHrAllocationsAsListOfMaps(pidm) //36732 // No accounts

        // Assert domain values
        assertNotNull allocationsObj
        assertNotNull allocationsObj.allocations
        assertEquals 0, allocationsObj.allocations.size()
    }

    @Test
    /**
     * One account:
     *   100% allocated
     */
    void testGetUserHrAllocationsAsListOfMapsOneAccount() {
        def pidm = PersonUtility.getPerson("GDP000003").pidm
        def allocationsObj = directDepositAccountCompositeService.getUserHrAllocationsAsListOfMaps(pidm) //49548 // One account

        // Assert domain values
        assertNotNull allocationsObj
        assertNotNull allocationsObj.allocations
        assertEquals 1, allocationsObj.allocations.size()

        def allocation = allocationsObj.allocations[0]

        assertNotNull allocation.id
        assertEquals "1293902", allocation.bankAccountNum
        assertEquals "C", allocation.accountType
        assertNull allocation.amount
        assertTrue(99.9 < allocation.percent && allocation.percent < 100.1)
    }

    @Test
    /**
     * Three accounts:
     *   Priority 1) $77.00 allocated
     *   Priority 2) 50% allocated
     *   Priority 3) 100% allocated
     */
    void testGetUserHrAllocationsAsListOfMapsThreeAccounts() {
        def pidm = PersonUtility.getPerson("GDP000005").pidm
        def allocationsObj = directDepositAccountCompositeService.getUserHrAllocationsAsListOfMaps(pidm) //36743 // Two accounts

        // Assert domain values
        assertNotNull allocationsObj
        assertNotNull allocationsObj.allocations
        assertEquals 3, allocationsObj.allocations.size()

        def allocation = allocationsObj.allocations[0]

        assertNotNull allocation.id
        assertEquals "736900542", allocation.bankAccountNum
        assertEquals "C", allocation.accountType
        assertTrue(76.9 < allocation.amount && allocation.amount < 77.1)
        assertNull allocation.percent

        allocation = allocationsObj.allocations[1]

        assertNotNull allocation.id
        assertEquals "95003546", allocation.bankAccountNum
        assertEquals "S", allocation.accountType
        assertNull allocation.amount
        assertTrue(49.9 < allocation.percent && allocation.percent < 50.1)

        allocation = allocationsObj.allocations[2]

        assertNotNull allocation.id
        assertEquals "67674852", allocation.bankAccountNum
        assertEquals "C", allocation.accountType
        assertNull allocation.amount
        assertTrue(99.9 < allocation.percent && allocation.percent < 100.1)
    }

    @Test
    void testGetCurrencySymbolPreDigits() {
        def symbol = directDepositAccountCompositeService.getCurrencySymbol()

        assertEquals '$', symbol
    }

    @Test
    void testGetCurrencySymbolPostDigits() {
        Locale originalLocale = LocaleContextHolder.getLocale()
        LocaleContextHolder.setLocale(new Locale("fr-CA"))
        try {
            def symbol = directDepositAccountCompositeService.getCurrencySymbol()

        assertEquals '\u00A0$\u00A0US', symbol
        } finally {
            LocaleContextHolder.setLocale( originalLocale )
        }
    }

    @Test
    void testReorderAccounts() {
        def pidm = PersonUtility.getPerson("GDP000005").pidm
        def accts = directDepositAccountCompositeService.getUserHrAllocations(pidm).allocations //36743
        SecurityContextHolder?.context?.authentication?.principal?.pidm = pidm

        accts[0].priority = 2
        accts[1].priority = 1

        def oldId = accts[0].id

        def result = directDepositAccountCompositeService.reorderAccounts(accts);

        def newId = result[1].id

        assertEquals false, oldId == newId
    }

    @Test
    void testReorderAccountsBySwappingFirstTwoAndCheckingLastModified() {
        def pidm = PersonUtility.getPerson('GDP000005').pidm
        def accts = directDepositAccountCompositeService.getUserHrAllocations(pidm).allocations //36743
        SecurityContextHolder?.context?.authentication?.principal?.pidm = pidm

        accts[0].priority = 2
        accts[1].priority = 1

        def result = directDepositAccountCompositeService.reorderAccounts(accts);

        assertEquals 4, result.size() // Results of any delete operation are in first element; the rest are the accounts
        assertTrue('Allocation "modified by" times differ.', Math.abs(result[1].lastModified.getTime() - result[2].lastModified.getTime()) < 1800)
        assertTrue('Allocation "modified by" times are similar.', Math.abs(result[2].lastModified.getTime() - result[3].lastModified.getTime()) > 1800)
    }

    @Test
    void testReorderAccountsBySwappingLastTwoAndCheckingLastModified() {
        def pidm = PersonUtility.getPerson('GDP000005').pidm
        def accts = directDepositAccountCompositeService.getUserHrAllocations(pidm).allocations //36743
        SecurityContextHolder?.context?.authentication?.principal?.pidm = pidm

        accts[1].priority = 3
        accts[2].priority = 2

        def result = directDepositAccountCompositeService.reorderAccounts(accts);

        assertEquals 4, result.size() // Results of any delete operation are in first element; the rest are the accounts
        assertTrue('Allocation "modified by" times are similar.', Math.abs(result[1].lastModified.getTime() - result[2].lastModified.getTime()) > 1800)
        assertTrue('Allocation "modified by" times differ.', Math.abs(result[2].lastModified.getTime() - result[3].lastModified.getTime()) < 1800)
    }

    @Test
    void testReorderAccountsBySwappingFirstWithLastAndCheckingLastModified() {
        def pidm = PersonUtility.getPerson('GDP000005').pidm
        def accts = directDepositAccountCompositeService.getUserHrAllocations(pidm).allocations //36743
        SecurityContextHolder?.context?.authentication?.principal?.pidm = pidm

        accts[0].priority = 3
        accts[2].priority = 1

        def result = directDepositAccountCompositeService.reorderAccounts(accts);

        assertEquals 4, result.size() // Results of any delete operation are in first element; the rest are the accounts
        assertTrue('Allocation "modified by" times are similar.', Math.abs(result[1].lastModified.getTime() - result[2].lastModified.getTime()) > 1800)
        assertTrue('Allocation "modified by" times are similar.', Math.abs(result[2].lastModified.getTime() - result[3].lastModified.getTime()) > 1800)
        assertTrue('Allocation "modified by" times differ.', Math.abs(result[1].lastModified.getTime() - result[3].lastModified.getTime()) < 1800)
    }

    @Test
    void testReorderAccountsByMovingFirstToLastAndCheckingLastModified() {
        def pidm = PersonUtility.getPerson('GDP000005').pidm
        def accts = directDepositAccountCompositeService.getUserHrAllocations(pidm).allocations //36743
        SecurityContextHolder?.context?.authentication?.principal?.pidm = pidm

        accts[0].priority = 3
        accts[1].priority = 1
        accts[2].priority = 2

        def result = directDepositAccountCompositeService.reorderAccounts(accts);

        assertEquals 4, result.size() // Results of any delete operation are in first element; the rest are the accounts
        assertTrue('Allocation "modified by" times differ.', Math.abs(result[1].lastModified.getTime() - result[2].lastModified.getTime()) < 1800)
        assertTrue('Allocation "modified by" times differ.', Math.abs(result[2].lastModified.getTime() - result[3].lastModified.getTime()) < 1800)
    }

    @Test
    void testReorderAccountsByMovingLastToFirstAndCheckingLastModified() {
        def pidm = PersonUtility.getPerson('GDP000005').pidm
        def accts = directDepositAccountCompositeService.getUserHrAllocations(pidm).allocations //36743
        SecurityContextHolder?.context?.authentication?.principal?.pidm = pidm

        accts[0].priority = 2
        accts[1].priority = 3
        accts[2].priority = 1

        def result = directDepositAccountCompositeService.reorderAccounts(accts);

        assertEquals 4, result.size() // Results of any delete operation are in first element; the rest are the accounts
        assertTrue('Allocation "modified by" times differ.', Math.abs(result[1].lastModified.getTime() - result[2].lastModified.getTime()) < 1800)
        assertTrue('Allocation "modified by" times differ.', Math.abs(result[2].lastModified.getTime() - result[3].lastModified.getTime()) < 1800)
    }

    @Test
    void testValidateOnlyOneAP() {
        def pidm = PersonUtility.getPerson("HOSH00018").pidm
        def accountMap = [pidm: pidm, apIndicator: 'A']
        try {
            directDepositAccountCompositeService.validateOnlyOneAP(accountMap)
            fail("I should have received an error but it passed; @@r1:apAccountAlreadyExists@@ ")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "apAccountAlreadyExists"
        }
    }

    @Test
    void testFetchEmployeeUpdatableSetting() {
        def updatable = directDepositAccountCompositeService.fetchEmployeeUpdatableSetting()

        assertNotNull updatable
        assertEquals "Y", updatable
    }

    @Test
    void testGetEmployeeUpdatableSettingWhenNotAlreadyStoredInSession() {
        def updatable = directDepositAccountCompositeService.getEmployeeUpdatableSetting()

        assertNotNull updatable
        assertEquals "Y", updatable
    }

    @Test
    void testGetEmployeeUpdatableSettingWhenStoredInSession() {
        def updatable = directDepositAccountCompositeService.fetchEmployeeUpdatableSetting()
        def session = RequestContextHolder.currentRequestAttributes().request.session
        session.setAttribute('EMPLOYEE_UPDATABLE_SETTING', updatable)
        updatable = directDepositAccountCompositeService.getEmployeeUpdatableSetting()

        assertNotNull updatable
        assertEquals "Y", updatable
    }

    @Test
    void testAreAccountsUpdatableWithStudent() {
        setupHoldersConfigWithRolesAllowingUpdates()
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('CSRSTU002', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)

        assertTrue userRoleService.hasUserRole('STUDENT')
        assertTrue directDepositAccountCompositeService.areAccountsUpdatable()
    }

    @Test
    void testAreAccountsUpdatableWithEmployee() {
        setupHoldersConfigWithRolesAllowingUpdates()
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('HOP510001', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)

        assertTrue userRoleService.hasUserRole('EMPLOYEE')
        assertTrue directDepositAccountCompositeService.areAccountsUpdatable()
    }

    @Test
    void testAreAccountsUpdatableWithStudentWithRolesNotAllowingUpdates() {
        setupHoldersConfigWithRolesNotAllowingUpdates()
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('CSRSTU002', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)

        assertTrue userRoleService.hasUserRole('STUDENT')
        assertFalse directDepositAccountCompositeService.areAccountsUpdatable()
    }

    @Test
    void testAreAccountsUpdatableWithEmployeeWithRolesNotAllowingUpdates() {
        setupHoldersConfigWithRolesNotAllowingUpdates()
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('HOP510001', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)

        assertTrue userRoleService.hasUserRole('EMPLOYEE')
        assertFalse directDepositAccountCompositeService.areAccountsUpdatable()
    }


    private executeCustomSQL( String updateStatement, id = null ) {
        def sql
        try {
            sql = new Sql( sessionFactory.getCurrentSession().connection() )

            if (id) {
                sql.executeUpdate( updateStatement, [ id ] )
            } else {
                sql.executeUpdate( updateStatement )
            }
        } finally {
//            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
    }

    private setupHoldersConfigWithRolesAllowingUpdates() {
        Holders.setConfig(oldHoldersConfig.merge(["grails.plugin.springsecurity.interceptUrlMap":[[pattern:'/ssb/UpdateAccount/**', access: ['ROLE_SELFSERVICE-EMPLOYEE_BAN_DEFAULT_M', 'ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_M']]]]))
    }

    private setupHoldersConfigWithRolesNotAllowingUpdates() {
        Holders.setConfig(oldHoldersConfig.merge(["grails.plugin.springsecurity.interceptUrlMap":[[pattern:'/ssb/UpdateAccount/**', access: ['ROLE_SELFSERVICE-EMPLOYEE_BAN_DEFAULT_Q', 'ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_Q']]]]))
    }
}
