/** *****************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.payment

import grails.util.Holders
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration

import static net.hedtech.banner.accountsreceivable.studentaccounts.common.AccountsReceivableCommonConstants.Tests.HOSARUSER1

/**
 * Tests for Payment Process Composite Service
 */
@Integration
@Rollback
class DirectProcessingCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    def depositProcessingPaymentCompositeService


    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']
        loginSSB( HOSARUSER1, '111111' )
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }


    @Test
    void testGetSubCode() {
        def ret = depositProcessingPaymentCompositeService.getSubCode( null )
        assert ret == '<UPDATE_ME>'
    }


    @Test
    void testCheckIfOneActiveCreditCardAssociatedWithMerchant() {
        try {
            depositProcessingPaymentCompositeService.checkIfOneActiveCreditCardAssociatedWithMerchant(
                    [sub_code_in: '0', proc_code_in: 'WEBCCARGATEWAY', sysi_code: 'S'], depositProcessingPaymentCompositeService.getPaymentInfoTexts() )
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "Web credit card payments are not available at this time."
        }

    }


    @Test
    void testGetPaymentInfoTexts() {
        def ret = depositProcessingPaymentCompositeService.getPaymentInfoTexts()

        ret.each {key, val ->
            if (key == 'payment.process.process.code.WEBCCAPPFEES') {
                assert val == 'Application Fees'
            }
            if (key == 'payment.process.process.code.DEFAULTDESCRIPTION') {
                assert val == 'Sungard HE University'
            }
            if (key == 'payment.process.process.code.WEBCCARGATEWAY') {
                assert val == 'DO NOT KNOW'
            }
        }
    }


    @Test
    void testGetTwgParamVendorUrlCheck() {
        Holders.config['banner.payment.vendor.url'] = 'https://test.com'
        def ret = depositProcessingPaymentCompositeService.getAppConfig( 'banner.payment.vendor.url' )
        assert ret == 'https://test.com'
    }


    @Test
    void testGetSecuredEncodedTransactionId() {
        def ret = depositProcessingPaymentCompositeService.getSecuredEncodedTransactionId( '800' )
        assert ret != '800.00'
    }


    @Test
    void testGetLocaleBasedFormattedNumber() {
        def ret = depositProcessingPaymentCompositeService.getLocaleBasedFormattedNumber( 800, 2 )
        assert ret == '800.00'
    }


    @Test
    void testGetPaymentUrl() {
        Map messageMap = depositProcessingPaymentCompositeService.getPaymentInfoTexts()
        Holders.config['banner.payment.vendor.url'] = '<UPDATE ME>'
        def vendorURL = depositProcessingPaymentCompositeService.getAppConfig( 'banner.payment.vendor.url' )
        Integer pidm = PersonUtility.getPerson( HOSARUSER1 ).pidm
        def procedureParam = [sub_code_in       : '0',
                              term_select_in    : 'Y',
                              proc_code_in      : 'WEBCCARGATEWAY',
                              proc_code_desc_in : messageMap['payment.process.process.code.WEBCCARGATEWAY'],
                              sysi_code_in      : 'S',
                              proc_pay_label    : 'PROCPAY',
                              term_label        : 'WEBCCTERM',
                              use_main_term     : 'N',
                              retrieve_addr_in  : 'Y',
                              cc_use_addr_in    : 'N',
                              amount_in         : 200,
                              term_code_in      : '202020',
                              update_function_in: 'bwckcpmt.F_update_accounts',
                              failure_url_in    : 'bwskwtrr.P_Failure_Page?transaction_id=', //should call the common failure page TODO Need to check more on this
                              success_url_in    : 'bwckcpmt.P_DispSigPage_cc?transaction_id=',//should call the common Success page TODO Need to check more on this
                              id_in             : PersonUtility.getPerson( pidm )?.bannerId,
                              vendor_url_in     : vendorURL,
                              pidm_in           : pidm,
                              vendor_in         : depositProcessingPaymentCompositeService.getAppConfig( 'banner.payment.vendor' ),
                              pay_trans_in      : depositProcessingPaymentCompositeService.getTransactionId()]
        def ret = depositProcessingPaymentCompositeService.getPaymentUrl( procedureParam )
        assert ret.contains( '<UPDATE ME>TransactionId=' );
        assert ret.contains( '&TransactionAmount=200.00&TransactionDescription=DO+NOT+KNOW&MerchantID=0' );
    }


    @Test
    void testGetTransactionId() {
        assert -1 != depositProcessingPaymentCompositeService.getTransactionId()
    }


    @Test
    void testProcessPaymentNullTerm() {
        Map param = [amount: 388]
        try {
            depositProcessingPaymentCompositeService.processPaymentCommon( param )
        } catch (ApplicationException ae) {
            assertApplicationException ae, "Should enter a valid Term"
        }
    }


    @Test
    void testProcessPaymentNullAmount() {
        Map param = [term: 202020]
        try {
            depositProcessingPaymentCompositeService.processPaymentCommon( param )
        } catch (ApplicationException ae) {
            assertApplicationException ae, "Should enter a valid amount"
        }
    }


    @Test
    void testProcessPaymentWrongFormatAmount() {
        Map param = [amount: '28j', term: 202020]
        try {
            depositProcessingPaymentCompositeService.processPaymentCommon( param )
        } catch (ApplicationException ae) {
            assertApplicationException ae, "You have entered an invalid amount. Please re-enter the payment amount using a positive value, with or without decimals."
        }
    }


    @Test
    void testProcessPaymentNegativeAmount() {
        Map param = [amount: -300, term: 202020]
        try {
            depositProcessingPaymentCompositeService.processPaymentCommon( param )
        } catch (ApplicationException ae) {
            assertApplicationException ae, "You have entered an invalid amount. Please re-enter the payment amount using a positive value, with or without decimals."
        }
    }


    @Test
    void testGetTwgParamVendorCheck() {
        Holders.config.banner.payment.vendor = 'TOUCHNET'
        def ret = depositProcessingPaymentCompositeService.getAppConfig( 'banner.payment.vendor' )
        assert ret == 'TOUCHNET'
    }
}
