/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall

import org.junit.Before
import org.junit.Test
import org.junit.After

import grails.validation.ValidationException
import groovy.sql.Sql
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
        assertEquals 3, directDepositAccount.priority
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
    void testCreateDuplicateAccount() {
        try {
            def directDepositAccount1 = newDirectDepositAccount()
            directDepositAccount1 = directDepositAccountService.create([domainModel: directDepositAccount1])
            
            def directDepositAccount2 = newDirectDepositAccount()
            directDepositAccount2 = directDepositAccountService.create([domainModel: directDepositAccount2])
            
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "recordAlreadyExists"
        }
    }
    
    @Test
    void testGetActiveApAccountsWhereOneAccountExists() {
        def activeAccounts = directDepositAccountService.getActiveApAccounts(38010) // One account

        // Assert domain values
        assertNotNull activeAccounts
        assertEquals 1, activeAccounts.size()

        def userAccountInfoSet = activeAccounts[0]
        def userAccount = userAccountInfoSet[0]

        assertNotNull userAccount.id
        assertEquals "9876543", userAccount.bankAccountNum
        assertEquals "C", userAccount.accountType
        assertEquals "A", userAccount.apIndicator
        assertEquals "I", userAccount.hrIndicator
        assertNotNull userAccount.version
        assertNotNull userAccount.lastModifiedBy
        assertNotNull userAccount.lastModified
    }

    @Test
    void testGetActiveApAccountsWhereMultipleAccountsExist() {
        def activeAccounts = directDepositAccountService.getActiveApAccounts(37859) // Multiple accounts

        // Assert domain values
        assertNotNull activeAccounts
        assertEquals 2, activeAccounts.size()

        // First account
        activeAccounts = activeAccounts.sort{it[0].id}
        def userAccountInfoSet = activeAccounts[0]
        def userAccount = userAccountInfoSet[0] // DirectDepositAccount element

        assertNotNull userAccount.id
        assertEquals "9876543", userAccount.bankAccountNum
        assertEquals "C", userAccount.accountType
        assertEquals "A", userAccount.apIndicator
        assertEquals "I", userAccount.hrIndicator
        assertNotNull userAccount.version
        assertNotNull userAccount.lastModifiedBy
        assertNotNull userAccount.lastModified

        // Second account
        userAccountInfoSet = activeAccounts[1]
        userAccount = userAccountInfoSet[0] // DirectDepositAccount element

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
    void testRoutingNumberValidation() {
        def routingNumber
        try {
            directDepositAccountService.validateRoutingNumber("103448999");
            fail("I should have received an error but it passed; @@r1:invalidRoutingNum@@ ")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "invalidRoutingNum"
        }
    }

    @Test
    void testRoutingNumberFormatValidation() {
        try {
            directDepositAccountService.validateRoutingNumFormat("fail1234abc");
            fail("I should have received an error but it passed; @@r1:invalidRoutingNumFmt@@ ")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "invalidRoutingNumFmt"
        }
    }
    
    @Test
    void testAccountNumberFormatValidation() {
        try {
            directDepositAccountService.validateAccountNumFormat("1954601TOOLONG74321");
            fail("I should have received an error but it passed; @@r1:invalidAccountNumFmt@@ ")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "invalidAccountNumFmt"
        }
    }

    private def newDirectDepositAccount() {
        def domain = new DirectDepositAccount(
            pidm: 37859, //49758,
            status: "P",
            documentType: "D",
            priority: 16,
            apIndicator: "I",
            hrIndicator: "A",
//            lastModified: $lastModified,
//            lastModifiedBy: $lastModifiedBy,
            bankAccountNum: 36948575,
            bankRoutingNum: 234798944,
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