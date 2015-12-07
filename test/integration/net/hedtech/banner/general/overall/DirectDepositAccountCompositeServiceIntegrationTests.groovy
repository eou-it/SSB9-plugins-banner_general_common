/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.crossproduct.BankRoutingInfo
import net.hedtech.banner.general.overall.DirectDepositAccountCompositeService
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 *
 */
class DirectDepositAccountCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    def directDepositAccountCompositeService

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
        pidm: 37859,
        status: 'P'
    ]
    
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
}