/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall

import net.hedtech.banner.general.crossproduct.BankRoutingInfo
import net.hedtech.banner.general.person.PersonUtility
import org.junit.Before
import org.junit.Test
import org.junit.After

import net.hedtech.banner.general.overall.DirectDepositAccountService
import net.hedtech.banner.testing.BaseIntegrationTestCase
import net.hedtech.banner.exceptions.ApplicationException

/**
 *
 */
class DirectDepositAccountServiceIntegrationTests extends BaseIntegrationTestCase {

    def directDepositAccountService
    
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
    void testCreateDirectDepositAccount() {
        def directDepositAccount = newDirectDepositAccount()
        directDepositAccount = directDepositAccountService.create([domainModel: directDepositAccount])

        // Assert domain values
        assertNotNull directDepositAccount
        assertNotNull directDepositAccount.id
        assertEquals 16, directDepositAccount.priority
        assertEquals "36948575", directDepositAccount.bankAccountNum
        assertEquals "C", directDepositAccount.accountType
        assertEquals "I", directDepositAccount.apIndicator
        assertEquals "A", directDepositAccount.hrIndicator
        assertNotNull directDepositAccount.version
        assertNotNull directDepositAccount.dataOrigin
        assertNotNull directDepositAccount.lastModifiedBy
        assertNotNull directDepositAccount.lastModified
        
        def id = directDepositAccount.id

        directDepositAccount = directDepositAccount.get(id)
        assertNotNull directDepositAccount
    }

    @Test
    void testGetActiveApAccountsWhereMultipleAccountsExist() {
        def pidm = PersonUtility.getPerson("HOSH00018").pidm
        def activeAccounts = directDepositAccountService.getActiveApAccounts(pidm) // Multiple accounts

         //Assert domain values
        assertNotNull activeAccounts
        assertEquals 2, activeAccounts.size()

        // First account
        activeAccounts = activeAccounts.sort{it.id}
        def userAccount = activeAccounts[0]

        assertNotNull userAccount.id
        assertEquals "9876543", userAccount.bankAccountNum
        assertEquals "C", userAccount.accountType
        assertEquals "A", userAccount.apIndicator
        assertEquals "I", userAccount.hrIndicator
        assertNotNull userAccount.version
        assertNotNull userAccount.lastModifiedBy
        assertNotNull userAccount.lastModified

        // Second account
        userAccount = activeAccounts[1]

        assertNotNull userAccount.id
        assertEquals "38167543", userAccount.bankAccountNum
        assertEquals "C", userAccount.accountType
        assertEquals "A", userAccount.apIndicator
        assertEquals "I", userAccount.hrIndicator
        assertNotNull userAccount.version
        assertNotNull userAccount.lastModifiedBy
        assertNotNull userAccount.lastModified
    }

    @Test
    void testGetActiveApAccountsWhereNoAccountsExist() {
        def activeAccounts = directDepositAccountService.getActiveApAccounts(-1) // One account

        // Assert domain values
        assertNotNull activeAccounts
        assertEquals 0, activeAccounts.size()
    }

    @Test
    void testGetActiveHrAccountsWhereOneAccountExists() {
        def pidm = PersonUtility.getPerson("MYE000003").pidm //49548
        def activeAccounts = directDepositAccountService.getActiveHrAccounts(pidm) // One account

        // Assert domain values
        assertNotNull activeAccounts
        assertEquals 1, activeAccounts.size()

        def userAccount = activeAccounts[0]

        assertNotNull userAccount.id
        assertEquals "1293902", userAccount.bankAccountNum
        assertEquals "C", userAccount.accountType
        assertEquals "I", userAccount.apIndicator
        assertEquals "A", userAccount.hrIndicator
        assertNotNull userAccount.version
        assertNotNull userAccount.lastModifiedBy
        assertNotNull userAccount.lastModified
    }

    @Test
    void testGetActiveHrAccountsWhereMultipleAccountsExist() {
        def pidm = PersonUtility.getPerson("MYE000004").pidm //37700;
        def activeAccounts = directDepositAccountService.getActiveHrAccounts(pidm) // Multiple accounts

        // Assert domain values
        assertNotNull activeAccounts
        assertEquals 2, activeAccounts.size()

        // First account
        activeAccounts = activeAccounts.sort{it.priority}
        def userAccount = activeAccounts[0]

        assertNotNull userAccount.id
        assertEquals "95003546", userAccount.bankAccountNum
        assertEquals "S", userAccount.accountType
        assertEquals "I", userAccount.apIndicator
        assertEquals "A", userAccount.hrIndicator
        assertNotNull userAccount.version
        assertNotNull userAccount.lastModifiedBy
        assertNotNull userAccount.lastModified

        // Second account
        userAccount = activeAccounts[1]

        assertNotNull userAccount.id
        assertEquals "736900542", userAccount.bankAccountNum
        assertEquals "C", userAccount.accountType
        assertEquals "I", userAccount.apIndicator
        assertEquals "A", userAccount.hrIndicator
        assertNotNull userAccount.version
        assertNotNull userAccount.lastModifiedBy
        assertNotNull userAccount.lastModified
    }

    @Test
    void testGetActiveHrAccountsWhereNoAccountsExist() {
        def activeAccounts = directDepositAccountService.getActiveHrAccounts(-1)

        // Assert domain values
        assertNotNull activeAccounts
        assertEquals 0, activeAccounts.size()
    }

    @Test
    void testAccountNumberFormatValidation() {
        try {
            directDepositAccountService.validateAccountNumFormat("123456789X123456789X123456789X12345");
            fail("I should have received an error but it passed; @@r1:invalidAccountNumFmt@@ ")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "invalidAccountNumFmt"
        }
    }

    @Test
    void testSetupAccountsForDelete() {
        def directDepositAccount1 = newDirectDepositAccount()
        directDepositAccount1 = directDepositAccountService.create([domainModel: directDepositAccount1])

        def directDepositAccountMap1 = [
                id: directDepositAccount1.id,
                pidm: 95999, //49758,
                status: "P",
                documentType: "D",
                priority: 16,
                apIndicator: "I",
                hrIndicator: "A",
                bankAccountNum: "36948575",
                bankRoutingInfo: [bankRoutingNum: "234798944"],
                amount: null,
                percent: 11.0,
                accountType: "C",
                intlAchTransactionIndicator: "N"
        ]

        def directDepositAccount2 = newDirectDepositAccount()
        directDepositAccount2.accountType = "S"
        directDepositAccount2.priority = 17
        directDepositAccount2 = directDepositAccountService.create([domainModel: directDepositAccount2])

        def directDepositAccountMap2 = [
                id: directDepositAccount2.id,
                pidm: 95999, //49758,
                status: "P",
                documentType: "D",
                priority: 16,
                apIndicator: "I",
                hrIndicator: "A",
                bankAccountNum: "36948575",
                bankRoutingInfo: [bankRoutingNum: "234798944"],
                amount: null,
                percent: 11.0,
                accountType: "S",
                intlAchTransactionIndicator: "N"
        ]

        def list = []
        list[0] = directDepositAccountMap1
        list[1] = directDepositAccountMap2

        def result = directDepositAccountService.setupAccountsForDelete(list)

        assert true, result.toBeDeleted.size() == 2
        assert true, result.messages.size() == 0
    }

    @Test
    void testSetupAccountsForDeleteAP() {
        def directDepositAccount1 = newDirectDepositAccount()
        directDepositAccount1 = directDepositAccountService.create([domainModel: directDepositAccount1])

        def directDepositAccountMap1 = [
                id: directDepositAccount1.id,
                pidm: 95999, //49758,
                status: "P",
                documentType: "D",
                priority: 16,
                apIndicator: "I",
                hrIndicator: "A",
                bankAccountNum: "36948575",
                bankRoutingInfo: [bankRoutingNum: "234798944"],
                amount: null,
                percent: 11.0,
                accountType: "C",
                intlAchTransactionIndicator: "N"
        ]

        def directDepositAccount2 = newDirectDepositAccount()
        directDepositAccount2.apIndicator = "A"
        directDepositAccount2.hrIndicator = "I"
        directDepositAccount2.priority = 17
        directDepositAccount2.percent = 100
        directDepositAccount2 = directDepositAccountService.create([domainModel: directDepositAccount2])

        def directDepositAccountMap2 = [
                id: directDepositAccount2.id,
                pidm: 95999, //49758,
                status: "P",
                documentType: "D",
                priority: 17,
                apIndicator: "A",
                hrIndicator: "I",
                bankAccountNum: "36948575",
                bankRoutingInfo: [bankRoutingNum: "234798944"],
                amount: null,
                percent: 100.0,
                accountType: "C",
                intlAchTransactionIndicator: "N",
                apDelete: true
        ]

        def list = []
        list[0] = directDepositAccountMap2

        def result = directDepositAccountService.setupAccountsForDelete(list)

        assert true, result.toBeDeleted.size() == 1
        assert 'PR', result.messages[0].activeType
    }

    @Test
    void testSetupAccountsForDeleteLegacy() {
        def directDepositAccount = newDirectDepositAccount()
        directDepositAccount.apIndicator = 'A'
        directDepositAccount = directDepositAccountService.create([domainModel: directDepositAccount])

        def directDepositAccountMap = [
            id: directDepositAccount.id,
            pidm: 95999, //49758,
            status: "P",
            documentType: "D",
            priority: 16,
            apIndicator: "A",
            hrIndicator: "A",
            bankAccountNum: "36948575",
            bankRoutingInfo: [bankRoutingNum: "234798944"],
            amount: null,
            percent: 11.0,
            accountType: "C",
            intlAchTransactionIndicator: "N"
        ]

        def list = []
        list[0] = directDepositAccountMap

        def result = directDepositAccountService.setupAccountsForDelete(list)

        assert true, result.toBeDeleted.size() == 0
        assert 'AP', result.messages[0].activeType
    }

    // TESTS TO VALIDATE ACCOUNT AMOUNTS
    @Test
    void testValidateAccountAmountsWithValidAmount() {
        def acct = [
                amount: 33,
                percent: null
        ]

        try {
            directDepositAccountService.validateAccountAmounts(acct);
        } catch (ApplicationException e) {
            fail("Unexpected exception was thrown: " + e.getMessage())
        }
    }

    @Test
    void testValidateAccountAmountsWithValidPercent() {
        def acct = [
                amount: null,
                percent: 66
        ]

        try {
            directDepositAccountService.validateAccountAmounts(acct);
        } catch (ApplicationException e) {
            fail("Unexpected exception was thrown: " + e.getMessage())
        }
    }

    @Test
    void testValidateAccountAmountsWhenBothValuesExist() {
        def acct = [
                amount: 1,
                percent: 1
        ]

        try {
            directDepositAccountService.validateAccountAmounts(acct);
            fail("I should have received an error but it passed; @@r1:bothAmountAndPercentValuesExist@@ ")
        } catch (ApplicationException ae) {
            assertApplicationException ae, "bothAmountAndPercentValuesExist"
        }
    }

    @Test
    void testValidateAccountAmountsWhenBothValuesAreNull() {
        def acct = [
                amount: null,
                percent: null
        ]

        try {
            directDepositAccountService.validateAccountAmounts(acct);
            fail("I should have received an error but it passed; @@r1:noValueExists@@ ")
        } catch (ApplicationException ae) {
            assertApplicationException ae, "noValueExists"
        }
    }

    @Test
    void testValidateAccountAmountsWithNoAmountProperty() {
        def acct = [
                percent: 10
        ]

        try {
            directDepositAccountService.validateAccountAmounts(acct);
        } catch (ApplicationException e) {
            fail("Unexpected exception was thrown: " + e.getMessage())
        }
    }

    @Test
    void testValidateAccountAmountsWithNoPercentProperty() {
        def acct = [
                amount: 10
        ]

        try {
            directDepositAccountService.validateAccountAmounts(acct);
        } catch (ApplicationException e) {
            fail("Unexpected exception was thrown: " + e.getMessage())
        }
    }

    @Test
    void testValidateAccountAmountsWithAmountAndZeroPercent() {
        def acct = [
                amount: 10,
                percent: 0
        ]

        try {
            directDepositAccountService.validateAccountAmounts(acct);
            fail("I should have received an error but it passed; @@r1:bothAmountAndPercentValuesExist@@ ")
        } catch (ApplicationException ae) {
            assertApplicationException ae, "bothAmountAndPercentValuesExist"
        }
    }

    @Test
    void testValidateAccountAmountsWithNegativeAmount() {
        def acct = [
                amount: -10
        ]

        try {
            directDepositAccountService.validateAccountAmounts(acct);
            fail("I should have received an error but it passed; @@r1:invalidAmountValue@@ ")
        } catch (ApplicationException ae) {
            assertApplicationException ae, "invalidAmountValue"
        }
    }

    @Test
    void testValidateAccountAmountsWithZeroAmount() {
        def acct = [
                amount: 0,
                percent: null
        ]

        try {
            directDepositAccountService.validateAccountAmounts(acct);
            fail("I should have received an error but it passed; @@r1:invalidAmountValue@@ ")
        } catch (ApplicationException ae) {
            assertApplicationException ae, "invalidAmountValue"
        }
    }

    @Test
    void testValidateAccountAmountsWithNegativePercent() {
        def acct = [
                percent: -10
        ]

        try {
            directDepositAccountService.validateAccountAmounts(acct);
            fail("I should have received an error but it passed; @@r1:invalidPercentValue@@ ")
        } catch (ApplicationException ae) {
            assertApplicationException ae, "invalidPercentValue"
        }
    }

    @Test
    void testValidateAccountAmountsWithZeroPercent() {
        def acct = [
                percent: 0
        ]

        try {
            directDepositAccountService.validateAccountAmounts(acct);
            fail("I should have received an error but it passed; @@r1:invalidPercentValue@@ ")
        } catch (ApplicationException ae) {
            assertApplicationException ae, "invalidPercentValue"
        }
    }

    @Test
    void testValidateAccountAmountsWithTooLargePercent() {
        def acct = [
                percent: 100.1
        ]

        try {
            directDepositAccountService.validateAccountAmounts(acct);
            fail("I should have received an error but it passed; @@r1:invalidPercentValue@@ ")
        } catch (ApplicationException ae) {
            assertApplicationException ae, "invalidPercentValue"
        }
    }
    // END: TESTS TO VALIDATE ACCOUNT AMOUNTS

    private def newDirectDepositAccount() {
        def bankRoutingInfo = new BankRoutingInfo()

        bankRoutingInfo.bankRoutingNum = 234798944

        def domain = new DirectDepositAccount(
            pidm: 95999, //49758,
            status: "P",
            documentType: "D",
            priority: 16,
            apIndicator: "I",
            hrIndicator: "A",
//            lastModified: $lastModified,
//            lastModifiedBy: $lastModifiedBy,
            bankAccountNum: 36948575,
            bankRoutingInfo: bankRoutingInfo,
            amount: null,
            percent: 11.0,
            accountType: "C",
//            addressTypeCode: $addressTypeCode,
//            addressSequenceNum: $addressSequenceNum,
            intlAchTransactionIndicator: "N"
//            isoCode: $isoCode,
//            apAchTransactionTypeCode: $apAchTransactionTypeCode
//            iatAddressTypeCode: $iatAddressTypeCode
//            iatAddessSequenceNum: $iatAddessSequenceNum
        )

        return domain
    }
}
