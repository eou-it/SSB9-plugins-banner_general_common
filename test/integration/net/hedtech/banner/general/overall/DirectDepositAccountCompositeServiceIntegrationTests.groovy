/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall

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


/**
 *
 */
class DirectDepositAccountCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    def directDepositAccountCompositeService
    def selfServiceBannerAuthenticationProvider

    def testBankRoutingInfo0 = [
        bankRoutingNum: '234798944'
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

    BannerAuthenticationToken bannerAuthenticationToken
    
    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()

    }


    @After
    public void tearDown() {
        super.tearDown()
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

        account1 = directDepositAccountCompositeService.addorUpdateAccount(testAccountMap0)

        try {
            account2 = directDepositAccountCompositeService.addorUpdateAccount(testAccountMap0)
            fail("I should have received an error but it passed; @@r1:recordAlreadyExists@@ ")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "recordAlreadyExists"
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
    void testGetLastPayDistribution() {
        def pidm = PersonUtility.getPerson("HOP510001").pidm

        def lastPayDist = directDepositAccountCompositeService.getLastPayDistribution(pidm)

        assertEquals true, lastPayDist.hasPayrollHist
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
    void testGetCurrencySymbolPreDigits() {
        def symbol = directDepositAccountCompositeService.getCurrencySymbol()

        assertEquals '$', symbol
    }

    @Test
    void testGetCurrencySymbolPostDigits() {
        LocaleContextHolder.setLocale(new Locale("fr-CA"))
        def symbol = directDepositAccountCompositeService.getCurrencySymbol()

        assertEquals '\u00A0$US', symbol
    }

    @Test
    void testReorderAccounts() {
        def pidm = PersonUtility.getPerson("GDP000005").pidm
        def accts = directDepositAccountCompositeService.getUserHrAllocations(pidm).allocations //36743

        accts[0].priority = 2
        accts[1].priority = 1

        def oldId = accts[0].id

        def result = directDepositAccountCompositeService.reorderAccounts(accts);

        def newId = result[1].id

        assertEquals false, oldId == newId
    }
}
