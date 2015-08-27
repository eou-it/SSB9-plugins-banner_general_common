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