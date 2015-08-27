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
        assertEquals "123478902", directDepositAccount.bankRoutingNum
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
        def activeAccounts = DirectDepositAccount.fetchActiveApAccountsByPidm(498) // One account

        // Assert domain values
        assertNotNull activeAccounts
        assertEquals 1, activeAccounts.size()

        def account = activeAccounts[0]

        assertNotNull account.id
        assertEquals "11111111111111111", account.bankAccountNum
        assertEquals "234798944", account.bankRoutingNum
        assertEquals "C", account.accountType
        assertEquals "A", account.apIndicator
        assertEquals "I", account.hrIndicator
        assertNotNull account.version
        assertNotNull account.lastModifiedBy
        assertNotNull account.lastModified
    }

    @Test
    void testGetActiveApAccountsWhereMultipleAccountsExist() {
        def activeAccounts = DirectDepositAccount.fetchActiveApAccountsByPidm(1820) // Multiple accounts

        // Assert domain values
        assertNotNull activeAccounts
        assertEquals 2, activeAccounts.size()

        // First account
        activeAccounts = activeAccounts.sort{it.id}
        def account = activeAccounts[0]

        assertNotNull account.id
        assertEquals "910487", account.bankAccountNum
        assertEquals "234798944", account.bankRoutingNum
        assertEquals "C", account.accountType
        assertEquals "A", account.apIndicator
        assertEquals "I", account.hrIndicator
        assertNotNull account.version
        assertNotNull account.lastModifiedBy
        assertNotNull account.lastModified

        // Second account
        account = activeAccounts[1]

        assertNotNull account.id
        assertEquals "24678", account.bankAccountNum
        assertEquals "150321456", account.bankRoutingNum
        assertEquals "C", account.accountType
        assertEquals "A", account.apIndicator
        assertEquals "I", account.hrIndicator
        assertNotNull account.version
        assertNotNull account.lastModifiedBy
        assertNotNull account.lastModified
    }

    @Test
    void testGetActiveApAccountsWhereNoAccountsExist() {
        def activeAccounts = DirectDepositAccount.fetchActiveApAccountsByPidm(-1) // One account

        // Assert domain values
        assertNotNull activeAccounts
        assertEquals 0, activeAccounts.size()
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
            bankRoutingNum: 123478902,
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