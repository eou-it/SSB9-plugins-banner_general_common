/*******************************************************************************
 Copyright 2015-2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import net.hedtech.banner.general.crossproduct.BankRoutingInfo
import net.hedtech.banner.general.person.PersonUtility
import org.junit.Before
import org.junit.Test
import org.junit.After

import net.hedtech.banner.general.overall.DirectDepositAccountService
import net.hedtech.banner.testing.BaseIntegrationTestCase
import net.hedtech.banner.exceptions.ApplicationException
import org.springframework.security.core.context.SecurityContextHolder

/**
 *
 */
@Integration
@Rollback
class DirectDepositAccountServiceIntegrationTests extends BaseIntegrationTestCase {

    private final DEFAULT_SESSION_PIDM = 95999
    private final RECORD_ALREADY_EXISTS_EXCEPTION = '@@r1:recordAlreadyExists@@'

    def directDepositAccountService
    def directDepositAccountCompositeService
    
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
        def activeAccounts = directDepositAccountService.getActiveApAccounts(-1) // No accounts

        // Assert domain values
        assertNotNull activeAccounts
        assertEquals 0, activeAccounts.size()
    }

    @Test
    void testFetchApAccountsByPidmAsListOfMapsWhereMultipleAccountsExist() {
        def pidm = PersonUtility.getPerson("HOSH00018").pidm
        def activeAccounts = directDepositAccountService.fetchApAccountsByPidmAsListOfMaps(pidm) // Multiple accounts

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

        // Second account
        userAccount = activeAccounts[1]

        assertNotNull userAccount.id
        assertEquals "38167543", userAccount.bankAccountNum
        assertEquals "C", userAccount.accountType
        assertEquals "A", userAccount.apIndicator
        assertEquals "I", userAccount.hrIndicator
        assertNotNull userAccount.version
    }

    @Test
    void testFetchApAccountsByPidmAsListOfMapsWhereNoAccountsExist() {
        def activeAccounts = directDepositAccountService.fetchApAccountsByPidmAsListOfMaps(-1) // No accounts

        // Assert domain values
        assertNotNull activeAccounts
        assertEquals 0, activeAccounts.size()
    }

    @Test
    void testGetActiveHrAccountsWhereOneAccountExists() {
        def pidm = PersonUtility.getPerson("GDP000003").pidm //49548
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
        def pidm = PersonUtility.getPerson("GDP000004").pidm //37700;
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
    void testMarshallAccountsToMinimalStateForUiWithSingleObject() {
        def directDepositAccount = newDirectDepositAccount()

        directDepositAccount.id = 1
        directDepositAccount.version = 0

        def marshalledAccount = directDepositAccountService.marshallAccountsToMinimalStateForUi(directDepositAccount)

        // Assert values
        assertNotNull marshalledAccount
        assertEquals 1, marshalledAccount.id
        assertEquals "P", marshalledAccount.status
        assertEquals "D", marshalledAccount.documentType
        assertEquals 16, marshalledAccount.priority
        assertEquals "36948575", marshalledAccount.bankAccountNum
        assertEquals "C", marshalledAccount.accountType
        assertEquals "I", marshalledAccount.apIndicator
        assertEquals "A", marshalledAccount.hrIndicator
        assertNotNull marshalledAccount.version
    }

    @Test
    void testMarshallAccountsToMinimalStateForUiWithNullAccount() {
        def marshalledAccount = directDepositAccountService.marshallAccountsToMinimalStateForUi(null)

        assertNull marshalledAccount
    }

    @Test
    void testMarshallAccountsToMinimalStateForUiWithList() {
        def directDepositAccount0 = newDirectDepositAccount()

        directDepositAccount0.id = 1
        directDepositAccount0.version = 0

        def directDepositAccount1 = newDirectDepositAccount()

        directDepositAccount1.id = 2
        directDepositAccount1.version = 4

        def accounts = []

        accounts.push(directDepositAccount0)
        accounts.push(directDepositAccount1)

        def marshalledAccounts = directDepositAccountService.marshallAccountsToMinimalStateForUi(accounts)

        assertEquals 2, marshalledAccounts.size()

        def marshalledAccount = marshalledAccounts[0]

        // Assert values
        assertNotNull marshalledAccount
        assertEquals 1, marshalledAccount.id
        assertEquals 0, marshalledAccount.version
        assertEquals "P", marshalledAccount.status
        assertEquals "D", marshalledAccount.documentType
        assertEquals 16, marshalledAccount.priority
        assertEquals "36948575", marshalledAccount.bankAccountNum
        assertEquals "C", marshalledAccount.accountType
        assertEquals "I", marshalledAccount.apIndicator
        assertEquals "A", marshalledAccount.hrIndicator
        assertNotNull marshalledAccount.version

        marshalledAccount = marshalledAccounts[1]

        // Assert values
        assertNotNull marshalledAccount
        assertEquals 2, marshalledAccount.id
        assertEquals 4, marshalledAccount.version
        assertEquals "P", marshalledAccount.status
        assertEquals "D", marshalledAccount.documentType
        assertEquals 16, marshalledAccount.priority
        assertEquals "36948575", marshalledAccount.bankAccountNum
        assertEquals "C", marshalledAccount.accountType
        assertEquals "I", marshalledAccount.apIndicator
        assertEquals "A", marshalledAccount.hrIndicator
        assertNotNull marshalledAccount.version
    }

    @Test
    void testMarshallAccountsToMinimalStateForUiWithNoAccounts() {
        def accounts = []
        def marshalledAccounts = directDepositAccountService.marshallAccountsToMinimalStateForUi(accounts)

        assertEquals 0, marshalledAccounts.size()
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


        def sessionPidm = 95999
        SecurityContextHolder?.context?.authentication?.principal?.pidm = sessionPidm

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

    @Test
    void testFetchApAccountsByPidm() {
        def directDepositAccount = newDirectDepositAccount()
        directDepositAccount.status = 'I'
        directDepositAccount.bankAccountNum = "982304444"
        directDepositAccount.apIndicator = 'A'
        directDepositAccount.hrIndicator = 'I'
        directDepositAccount = directDepositAccountService.create([domainModel: directDepositAccount])

        def directDepositAccount2 = newDirectDepositAccount()
        directDepositAccount2.priority = 17
        directDepositAccount2.apIndicator = 'A'
        directDepositAccount2.hrIndicator = 'I'
        directDepositAccount = directDepositAccountService.create([domainModel: directDepositAccount2])

        def results = directDepositAccountService.fetchApAccountsByPidm(directDepositAccount2.pidm)
        assertTrue results.size() >= 2
        assertTrue results.bankAccountNum.contains("982304444")
    }

    @Test
    void testDeleteOfAccountBelongingToPidm() {
        def pidm = 49776

        def newDirectDepositAccount = newDirectDepositAccount(pidm)
        def directDepositAccount = directDepositAccountService.create([domainModel: newDirectDepositAccount])

        directDepositAccount.discard()

        def savedDirectDepositAccount = directDepositAccountService.get(directDepositAccount.id)

        SecurityContextHolder?.context?.authentication?.principal?.pidm = pidm

        def success = directDepositAccountService.delete(savedDirectDepositAccount)

        assertTrue success
    }

    @Test
    void testDeleteOfAccountNotBelongingToPidm() {
        def directDepositAccount = newDirectDepositAccount()
        directDepositAccount = directDepositAccountService.create([domainModel: directDepositAccount])

        def sessionPidm = 49776
        SecurityContextHolder?.context?.authentication?.principal?.pidm = sessionPidm

        try {
            directDepositAccountService.delete(directDepositAccount)
            fail("I should have received an error but it passed; @@r1:operation.not.authorized@@")
        } catch (ApplicationException ae) {
            assertApplicationException ae, "@@r1:operation.not.authorized@@"
        }
    }

    @Test
    void testUpdateOfAccountBelongingToPidm() {
        def pidm = 49776

        def newDirectDepositAccount = newDirectDepositAccount(pidm)
        def directDepositAccount = directDepositAccountService.create([domainModel: newDirectDepositAccount])

        assertEquals "C", directDepositAccount.accountType

        // Update it
        directDepositAccount.accountType = "S"

        SecurityContextHolder?.context?.authentication?.principal?.pidm = pidm

        def updatedAccount = directDepositAccountService.update(directDepositAccount)

        assertNotNull updatedAccount
        assertEquals "S", updatedAccount.accountType
    }

    @Test
    void testUpdateOfAccountNotBelongingToPidm() {
        def directDepositAccount = newDirectDepositAccount()
        directDepositAccount = directDepositAccountService.create([domainModel: directDepositAccount])

        // Update it
        directDepositAccount.accountType = "S"

        def sessionPidm = 49776
        SecurityContextHolder?.context?.authentication?.principal?.pidm = sessionPidm

        try {
            directDepositAccountService.update(directDepositAccount)
            fail("I should have received an error but it passed; @@r1:operation.not.authorized@@")
        } catch (ApplicationException ae) {
            assertApplicationException ae, "@@r1:operation.not.authorized@@"
        }
    }

    @Test
    void testValidateNotDuplicateWithCreatingAccountHavingADuplicate() {
        def existingDirectDepositAccount = newDirectDepositAccount()
        directDepositAccountService.create([domainModel: existingDirectDepositAccount])

        // "Newly created" account
        def newDirectDepositAccount = newDirectDepositAccount()

        try {
            directDepositAccountService.validateNotDuplicate(newDirectDepositAccount)
            fail("Should have received an error, but it passed: ${RECORD_ALREADY_EXISTS_EXCEPTION}")
        } catch (ApplicationException ae) {
            assertApplicationException ae, RECORD_ALREADY_EXISTS_EXCEPTION
        }
    }

    @Test
    void testValidateNotDuplicateWithCreatingAccountHavingNoDuplicates() {
        def existingDirectDepositAccount = newDirectDepositAccount()
        directDepositAccountService.create([domainModel: existingDirectDepositAccount])

        // "Newly created" account
        def newDirectDepositAccount = newDirectDepositAccount()
        newDirectDepositAccount.accountType = 'S' // Make it not a duplicate to the one created above

        try {
            directDepositAccountService.validateNotDuplicate(newDirectDepositAccount)
        } catch (ApplicationException ae) {
            assertApplicationException ae, RECORD_ALREADY_EXISTS_EXCEPTION
            fail("Received an error, but it should have passed: ${RECORD_ALREADY_EXISTS_EXCEPTION}")
        }
    }

    @Test
    void testPreCreateWithAccountHavingADuplicate() {
        // Reset session PIDM
        SecurityContextHolder?.context?.authentication?.principal?.pidm = DEFAULT_SESSION_PIDM

        def existingDirectDepositAccount = newDirectDepositAccount()
        directDepositAccountService.create([domainModel: existingDirectDepositAccount])

        def newDirectDepositAccount = newDirectDepositAccount()
        newDirectDepositAccount.priority = 17 // Satisfies the GXRDIRD_KEY_INDEX2 constraint

        try {
            // Rather than calling preCreate directly, create is called.  This is because there is logic in
            // ServiceBase, e.g. invokeServicePreCreate, which needs to be executed before preCreate is called.
            directDepositAccountService.create([domainModel: newDirectDepositAccount])
            fail("Should have received an error, but it passed: ${RECORD_ALREADY_EXISTS_EXCEPTION}")
        } catch (ApplicationException ae) {
            assertApplicationException ae, RECORD_ALREADY_EXISTS_EXCEPTION
        }
    }

    @Test
    void testPreCreateWithAccountAsMapHavingADuplicate() {
        // Reset session PIDM
        SecurityContextHolder?.context?.authentication?.principal?.pidm = DEFAULT_SESSION_PIDM

        def existingDirectDepositAccount = newDirectDepositAccount()
        directDepositAccountService.create([domainModel: existingDirectDepositAccount])

        def newDirectDepositAccount = newDirectDepositAccount()
        newDirectDepositAccount.priority = 17 // Satisfies the GXRDIRD_KEY_INDEX2 constraint

        // Converting DirectDepositAccount object to map exercises preCreate logic dealing with map vs. object
        def map = convertDirectDepositAccountToMap(newDirectDepositAccount)
        map.bankRoutingInfo = directDepositAccountCompositeService.validateBankRoutingInfo(map.bankRoutingInfo.bankRoutingNum)

        try {
            // Rather than calling preCreate directly, create is called.  This is because there is logic in
            // ServiceBase, e.g. invokeServicePreCreate, which needs to be executed before preCreate is called.
            directDepositAccountService.create(map)
            fail("Should have received an error, but it passed: ${RECORD_ALREADY_EXISTS_EXCEPTION}")
        } catch (ApplicationException ae) {
            assertApplicationException ae, RECORD_ALREADY_EXISTS_EXCEPTION
        }
    }

    @Test
    void testPreCreateWithAccountHavingNoDuplicates() {
        def existingDirectDepositAccount = newDirectDepositAccount()

        try {
            // Rather than calling preCreate directly, create is called.  This is because there is logic in
            // ServiceBase, e.g. invokeServicePreCreate, which needs to be executed before preCreate is called.
            def result = directDepositAccountService.create(existingDirectDepositAccount)
            assertNotNull result
        } catch (ApplicationException ae) {
            ae.printStackTrace()
            assertApplicationException ae, RECORD_ALREADY_EXISTS_EXCEPTION
            fail("Received an error, but it should have passed: ${RECORD_ALREADY_EXISTS_EXCEPTION}")
        }
    }

    @Test
    void testPreCreateWithAccountAsMapHavingNoDuplicates() {
        // Converting DirectDepositAccount object to map exercises preCreate logic dealing with map vs. object
        def map = convertDirectDepositAccountToMap(newDirectDepositAccount())
        map.bankRoutingInfo = directDepositAccountCompositeService.validateBankRoutingInfo(map.bankRoutingInfo.bankRoutingNum)

        try {
            // Rather than calling preCreate directly, create is called.  This is because there is logic in
            // ServiceBase, e.g. invokeServicePreCreate, which needs to be executed before preCreate is called.
            def result = directDepositAccountService.create(map)
            assertNotNull result
        } catch (ApplicationException ae) {
            ae.printStackTrace()
            assertApplicationException ae, RECORD_ALREADY_EXISTS_EXCEPTION
            fail("Received an error, but it should have passed: ${RECORD_ALREADY_EXISTS_EXCEPTION}")
        }
    }

    @Test
    void testPreUpdateWithAccountHavingADuplicate() {
        // Reset session PIDM
        SecurityContextHolder?.context?.authentication?.principal?.pidm = DEFAULT_SESSION_PIDM

        def existingDirectDepositAccount = newDirectDepositAccount()
        directDepositAccountService.create([domainModel: existingDirectDepositAccount])

        def newDirectDepositAccount = newDirectDepositAccount()
        newDirectDepositAccount.accountType = 'S'
        newDirectDepositAccount.priority = 17 // Satisfies the GXRDIRD_KEY_INDEX2 constraint
        newDirectDepositAccount = directDepositAccountService.create([domainModel: newDirectDepositAccount])

        // Make change to be updated
        newDirectDepositAccount.accountType = 'C'

        try {
            // Rather than calling preUpdate directly, update is called.  This is because there is logic in
            // ServiceBase, e.g. invokeServicePreUpdate, which needs to be executed before preUpdate is called.
            directDepositAccountService.update(newDirectDepositAccount)
            fail("Should have received an error, but it passed: ${RECORD_ALREADY_EXISTS_EXCEPTION}")
        } catch (ApplicationException ae) {
            assertApplicationException ae, RECORD_ALREADY_EXISTS_EXCEPTION
        }
    }

    @Test
    void testPreUpdateWithAccountAsMapHavingADuplicate() {
        // Reset session PIDM
        SecurityContextHolder?.context?.authentication?.principal?.pidm = DEFAULT_SESSION_PIDM

        def existingDirectDepositAccount = newDirectDepositAccount()
        directDepositAccountService.create([domainModel: existingDirectDepositAccount])

        def newDirectDepositAccount = newDirectDepositAccount()
        newDirectDepositAccount.accountType = 'S'
        newDirectDepositAccount.priority = 17 // Satisfies the GXRDIRD_KEY_INDEX2 constraint
        newDirectDepositAccount = directDepositAccountService.create([domainModel: newDirectDepositAccount])

        // Converting DirectDepositAccount object to map exercises preUpdate logic dealing with map vs. object
        def map = convertDirectDepositAccountToMap(newDirectDepositAccount)
        map.accountType = 'C'

        try {
            // Rather than calling preUpdate directly, update is called.  This is because there is logic in
            // ServiceBase, e.g. invokeServicePreUpdate, which needs to be executed before preUpdate is called.
            directDepositAccountService.update(map)
            fail("Should have received an error, but it passed: ${RECORD_ALREADY_EXISTS_EXCEPTION}")
        } catch (ApplicationException ae) {
            assertApplicationException ae, RECORD_ALREADY_EXISTS_EXCEPTION
        }
    }

    @Test
    void testPreUpdateWithAccountHavingNoDuplicates() {
        def existingDirectDepositAccount = newDirectDepositAccount()
        existingDirectDepositAccount = directDepositAccountService.create([domainModel: existingDirectDepositAccount])

        def newDirectDepositAccount = newDirectDepositAccount()
        newDirectDepositAccount.accountType = 'S'
        newDirectDepositAccount.priority = 17
        newDirectDepositAccount = directDepositAccountService.create([domainModel: newDirectDepositAccount])

        // Make change to be updated
        newDirectDepositAccount.percent = 12

        try {
            // Rather than calling preUpdate directly, update is called.  This is because there is logic in
            // ServiceBase, e.g. invokeServicePreUpdate, which needs to be executed before preUpdate is called.
            def result = directDepositAccountService.update(newDirectDepositAccount)
            assertNotNull result
        } catch (ApplicationException ae) {
            ae.printStackTrace()
            assertApplicationException ae, RECORD_ALREADY_EXISTS_EXCEPTION
            fail("Received an error, but it should have passed: ${RECORD_ALREADY_EXISTS_EXCEPTION}")
        }
    }

    @Test
    void testPreUpdateWithAccountAsMapHavingNoDuplicates() {
        def existingDirectDepositAccount = newDirectDepositAccount()
        existingDirectDepositAccount = directDepositAccountService.create([domainModel: existingDirectDepositAccount])

        def newDirectDepositAccount = newDirectDepositAccount()
        newDirectDepositAccount.accountType = 'S'
        newDirectDepositAccount.priority = 17
        newDirectDepositAccount = directDepositAccountService.create([domainModel: newDirectDepositAccount])

        // Converting DirectDepositAccount object to map exercises preUpdate logic dealing with map vs. object
        def map = convertDirectDepositAccountToMap(newDirectDepositAccount)

        // Make change to be updated
        newDirectDepositAccount.percent = 12

        try {
            // Rather than calling preUpdate directly, update is called.  This is because there is logic in
            // ServiceBase, e.g. invokeServicePreUpdate, which needs to be executed before preUpdate is called.
            def result = directDepositAccountService.update(map)
            assertNotNull result
        } catch (ApplicationException ae) {
            ae.printStackTrace()
            assertApplicationException ae, RECORD_ALREADY_EXISTS_EXCEPTION
            fail("Received an error, but it should have passed: ${RECORD_ALREADY_EXISTS_EXCEPTION}")
        }
    }

    private def newDirectDepositAccount(pidm=DEFAULT_SESSION_PIDM) {
        def bankRoutingInfo = new BankRoutingInfo()

        bankRoutingInfo.bankRoutingNum = 234798944

        def domain = new DirectDepositAccount(
            pidm: pidm,
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

    private def convertDirectDepositAccountToMap(account) {
        [
                id: account.id,
                version: account.version,
                pidm: account.pidm,
                status: account.status,
                documentType: account.documentType,
                priority: account.priority,
                apIndicator: account.apIndicator,
                hrIndicator: account.hrIndicator,
                bankAccountNum: account.bankAccountNum,
                bankRoutingInfo: [
                        bankRoutingNum: account.bankRoutingInfo.bankRoutingNum
                ],
                amount: account.amount,
                percent: account.percent,
                accountType: account.accountType,
                intlAchTransactionIndicator: account.intlAchTransactionIndicator
        ]
    }
}
