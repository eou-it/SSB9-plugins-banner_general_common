/** *****************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.payment

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.util.Holders
import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Tests for Payment Process Composite Service
 */
@Integration
@Rollback
class CCPaymentCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    def creditCardPaymentCompositeService
    def configurationDataService


    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']
        loginSSB( 'HOSARUSR1', '111111' )
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }


    @Test
    void testCheckIfPreventPayment() {
        try {
            creditCardPaymentCompositeService.checkIfPreventPayment( ['payment.process.prevent.payment': "You either have a hold on your account or your account has been referred for collection. Please contact the Bursar's office for more information"] )
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "You either have a hold on your account or your account has been referred for collection. Please contact the Bursar's office for more information"
        }
    }


    @Test
    void testCheckIfPreventPaymentWithMessage() {
        sessionFactory.getCurrentSession().createSQLQuery( " insert into TBRCOLC (TBRCOLC_PIDM, TBRCOLC_COLC_AGENCY_PIDM, TBRCOLC_ACTIVITY_DATE, TBRCOLC_COLC_AGENCY_DATE) values (31415, 31415, sysdate, sysdate)" ).executeUpdate()
        try {
            creditCardPaymentCompositeService.checkIfPreventPayment( creditCardPaymentCompositeService.getPaymentInfoTexts() )
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "You either have a hold on your account or your account has been referred for collection. Please contact the Bursar's office for more information"
        }
    }


    @Test
    void testGetSubCode() {
        def ret = creditCardPaymentCompositeService.getSubCode( null )
        assert ret == '0'
    }


    @Test
    void testCheckIfOneActiveCreditCardAssociatedWithMerchant() {
        try {
            creditCardPaymentCompositeService.checkIfOneActiveCreditCardAssociatedWithMerchant(
                    [sub_code_in: '0', proc_code_in: 'WEBCCREGFEES', sysi_code: 'S'], creditCardPaymentCompositeService.getPaymentInfoTexts() )
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "Web credit card payments are not available at this time."
        }

    }


    @Test
    void testGetPaymentInfoTexts() {
        def ret = creditCardPaymentCompositeService.getPaymentInfoTexts()
        ret.each {key, val ->
            if (key == 'payment.process.process.code.WEBCCAPPFEES') {
                assert val == 'Application Fees'
            }
            if (key == 'payment.process.process.code.WEBCCARGATEWAY') {
                assert val == 'Registration Fees'
            }
            if (key == 'payment.process.process.code.DEFAULTDESCRIPTION') {
                assert val == 'Ellucian University'
            }
            if (key == 'payment.process.process.code.WEBCCARGATEWAY') {
                assert val == 'Ellucian University'
            }
        }
    }


    @Test
    void testGetTwgParamVendorUrlCheck() {
        Sql sql = new Sql( sessionFactory.getCurrentSession().connection() )
        sql.executeUpdate( "update gurocfg set  GUROCFG_VALUE ='https://test.com?'  where GUROCFG_GUBAPPL_APP_ID='GENERAL_SS' and GUROCFG_NAME ='banner.payment.vendor.url' " )
        def ret = creditCardPaymentCompositeService.getAppConfig( 'banner.payment.vendor.url', 'string' )
        assert ret == 'https://test.com?'
    }


    @Test
    void testGetSecuredEncodedTransactionId() {
        def ret = creditCardPaymentCompositeService.getSecuredEncodedTransactionId( '800' )
        assert ret != '800.00'
    }


    @Test
    void testGetLocaleBasedFormattedNumber() {
        def ret = creditCardPaymentCompositeService.getLocaleBasedFormattedNumber( 800, 2 )
        assert ret == '800.00'
    }


    @Test
    void testGetPaymentUrl() {
        Map messageMap = creditCardPaymentCompositeService.getPaymentInfoTexts()
        Sql sql = new Sql( sessionFactory.getCurrentSession().connection() )
        sql.executeUpdate( "update gurocfg set  GUROCFG_VALUE ='https://test.com?'  where GUROCFG_GUBAPPL_APP_ID='GENERAL_SS' and GUROCFG_NAME ='banner.payment.vendor.url' " )
        def vendorURL = creditCardPaymentCompositeService.getAppConfig( 'banner.payment.vendor.url', 'string' )
        Integer pidm = PersonUtility.getPerson( 'HOSARUSR1' ).pidm
        def procedureParam = [sub_code_in       : '0',
                              term_select_in    : 'Y',
                              proc_code_in      : 'WEBCCREGFEES',
                              proc_code_desc_in : messageMap['payment.process.process.code.WEBCCREGFEES'],
                              sysi_code_in      : 'S',
                              proc_pay_label    : 'PROCPAY',
                              term_label        : 'WEBCCTERM',
                              use_main_term     : 'N',
                              retrieve_addr_in  : 'Y',
                              cc_use_addr_in    : 'Y',
                              amount_in         : 200,
                              term_code_in      : '202020',
                              update_function_in: 'bwckcpmt.F_update_accounts',
                              failure_url_in    : 'bwskwtrr.P_Failure_Page?transaction_id=', //should call the common failure page TODO Need to check more on this
                              success_url_in    : 'bwckcpmt.P_DispSigPage_cc?transaction_id=',//should call the common Success page TODO Need to check more on this
                              id_in             : PersonUtility.getPerson( pidm )?.bannerId,
                              vendor_url_in     : vendorURL,
                              pidm_in           : pidm,
                              vendor_in         : creditCardPaymentCompositeService.getAppConfig( 'banner.payment.vendor', 'string' ),
                              pay_trans_in      : creditCardPaymentCompositeService.getTransactionId()]
        def ret = creditCardPaymentCompositeService.getPaymentUrl( procedureParam )
        assert ret.contains( 'https://test.com' );
        assert ret.contains( '&TransactionAmount=200&TransactionDescription=Registration+Fees&MerchantID=0' );
    }


    @Test
    void testGetTransactionId() {
        assert -1 != creditCardPaymentCompositeService.getTransactionId()
    }


    @Test
    void testProcessPaymentNullTerm() {
        Map param = [amount: 388]
        try {
            creditCardPaymentCompositeService.processPaymentCommon( param )
        } catch (ApplicationException ae) {
            assertApplicationException ae, "Should enter a valid Term"
        }
    }


    @Test
    void testProcessPayment() {
        Map param = [amount: 388, term: 201920]
        try {
            def ret = creditCardPaymentCompositeService.processPaymentCommon( param )
        } catch (ApplicationException e) {
            assertApplicationException( e, "You either have a hold on your account or your account has been referred for collection. Please contact the Bursar's office for more information" )
        }
    }


    @Test
    void testProcessPaymentNullAmount() {
        Map param = [term: 202020]
        try {
            creditCardPaymentCompositeService.processPaymentCommon( param )
        } catch (ApplicationException ae) {
            assertApplicationException ae, "Should enter a valid amount"
        }
    }


    @Test
    void testProcessPaymentWrongFormatAmount() {
        Map param = [amount: '28j', term: 202020]
        try {
            creditCardPaymentCompositeService.processPaymentCommon( param )
        } catch (ApplicationException ae) {
            assertApplicationException ae, "You have entered an invalid amount. Please re-enter the payment amount using a positive value, with or without decimals."
        }
    }


    @Test
    void testProcessPaymentNegativeAmount() {
        Map param = [amount: -300, term: 202020]
        try {
            creditCardPaymentCompositeService.processPaymentCommon( param )
        } catch (ApplicationException ae) {
            assertApplicationException ae, "You have entered an invalid amount. Please re-enter the payment amount using a positive value, with or without decimals."
        }
    }


    @Test
    void testGetTwgParamVendorCheck() {
        Sql sql = new Sql( sessionFactory.getCurrentSession().connection() )
        sql.executeUpdate( "update gurocfg set  GUROCFG_VALUE ='TOUCHNET'  where GUROCFG_NAME ='banner.payment.vendor' " )
        def ret = creditCardPaymentCompositeService.getAppConfig( 'banner.payment.vendor', 'string' )
        assert ret == 'TOUCHNET'
    }


    @Test
    void testGetPaymentConfiguration() {
        Sql sql = new Sql( sessionFactory.getCurrentSession().connection() )
        sql.executeUpdate( "update gurocfg set  GUROCFG_VALUE ='true'  where GUROCFG_NAME ='banner.pci.enabled.payment.gateway.available' " )
        sql.executeUpdate( "update gurocfg set  GUROCFG_VALUE ='testURL'  where GUROCFG_NAME ='banner.nonpci.payment.gateway.url' " )
        def config = creditCardPaymentCompositeService.getPaymentConfiguration()
        assert [pciEnabled: true,
                nonPciURL : null] == config

    }
}
