/** *****************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.payment

import groovy.sql.Sql
import net.hedtech.banner.MessageUtility
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.ConfigurationData
import net.hedtech.banner.general.common.GeneralInformationTextUtility
import net.hedtech.banner.general.person.PersonUtility
import org.springframework.context.i18n.LocaleContextHolder

import java.text.NumberFormat
import java.text.SimpleDateFormat

/**
 * Common Composite Service for Processing Ellucian Payment gateway process
 */
abstract class CommonProcessPaymentCompositeService {
    def sessionFactory
    def processCodeModule = [BWSKPAYG: 'WEBCCARGATEWAY', BWCKCPMT: 'WEBCCREGFEES']
    def private final DEFAULT_PROCESS_CODE = 'DEFAULTDESCRIPTION'

    /**
     * Process payment
     * @param param
     * @return
     */
    def processPaymentCommon( Map param ) {
        commonValidation( param.pageName )
        def messageMap = getPaymentInfoTexts()
        preValidation( param, messageMap )
        assignProcessCode( param )
        prepareDataToProcess( param )
        assignSubCode( param )
        processPayment( param, messageMap )
    }

    /**
     * Perform caller pre validation
     * @param param
     * @param messageMap
     * @return
     */
    abstract def preValidation( param, messageMap )

    /**
     * Prepares Data
     * @param param
     * @return
     */
    abstract def prepareDataToProcess( param )

    /**
     * Assigns Sub Code
     * @param param
     * @return
     */
    abstract def assignSubCode( param )

    /**
     * Assign Process code
     * @param param
     * @return
     */
    def assignProcessCode( param ) {
        param << [proc_code_in: processCodeModule[param.moduleName]]
    }

    /**
     * Performs common validation
     * @return
     */
    def commonValidation( pageName = '' ) {
        boolean touchnetEnabled = getAppConfig( 'banner.pci.enabled.payment.gateway.available', 'boolean' )
        String nonTouchnetURL = getAppConfig( 'banner.nonpci.payment.gateway.url', 'string' )
        if (!touchnetEnabled) {
            if (nonTouchnetURL) {
                return nonTouchnetURL // Client might have maintained their own payment gateway model
            }
            throw new ApplicationException( CommonProcessPaymentCompositeService.class, MessageUtility.message( 'banner.payment.message.error.gateway.not.configured.nor.url' ) )
        }
        def vendorURL = getAppConfig( 'banner.payment.vendor.url', 'string' )
        if (!vendorURL) {
            throw new ApplicationException( CommonProcessPaymentCompositeService.class, MessageUtility.message( 'banner.payment.message.no.vendor.url' ) )
        }
    }

    /**
     * Process Payment
     * @param param
     * @param messageMap
     * @return a payment URL
     */
    def processPayment( Map param, messageMap ) {
        // set initial procedure parameter
        def procedureParam = [
                proc_code_in      : param.proc_code_in,
                proc_code_desc_in : messageMap["payment.process.process.code.$param.proc_code_in"] ?: messageMap["payment.process.process.code.$DEFAULT_PROCESS_CODE"],
                sysi_code_in      : param.sysi_code_in,
                use_main_term     : param.use_main_term,
                retrieve_addr_in  : param.retrieve_addr_in,
                cc_use_addr_in    : param.cc_use_addr_in,
                amount_in         : param.amount_in,
                term_code_in      : param.term_code_in,
                update_function_in: param.update_function_in,
                failure_url_in    : param.failure_url_in, //should call the common failure page TODO Need to check more on this
                success_url_in    : param.success_url_in,//should call the common Success page TODO Need to check more on this
                id_in             : param.id_in ?: PersonUtility.getPerson( param.pidm )?.bannerId,
                vendor_url_in     : getAppConfig( 'banner.payment.vendor.url', 'string' ),
                pidm_in           : param.pidm_in,
                aidm_in           : param.aidm_in,
                vendor_in         : getAppConfig( 'banner.payment.vendor', 'string' ),
                sub_code_in       : param.sub_code_in,
                first_name_in     : param.first_name_in,
                last_name_in      : param.last_name_in,
                address1_in       : param.address1_in,
                address2_in       : param.address2_in,
                city_in           : param.city_in,
                state_in          : param.state_in,
                zip_in            : param.zip_in,
                natn_in           : param.natn_in,
                pay_trans_in      : param.pay_trans_in,
                user_extra_data_in: param.user_extra_data_in,
                appl_no_in        : param.appl_no_in,
                house_no_fetch    : param.house_no_fetch

        ]
        checkIfOneActiveCreditCardAssociatedWithMerchant( procedureParam, messageMap )
        checkIfReadyToGetPaymentURL( procedureParam )
        def finalUrl = getPaymentUrl( procedureParam )
        finalUrl
    }

    /**
     *
     * @return
     */
    def getTransactionId() {
        Sql sql
        def transId
        sql = new Sql( sessionFactory.getCurrentSession().connection() )
        sql.eachRow( "SELECT ban_payment_sequence.nextval FROM dual" ) {
            transId = it.NEXTVAL
        }
        String.valueOf( transId )
    }

    /**
     * Checks if all validation are checked
     * @param procedureParam
     */
    def checkIfReadyToGetPaymentURL( procedureParam ) {
        if (!procedureParam.pidm_in && !procedureParam.aidm_in) {
            throw new ApplicationException( CommonProcessPaymentCompositeService.class, MessageUtility.message( 'banner.payment.message.error.pdmoraidm.not.available' ) )
        }
        if (!procedureParam.id_in) {
            throw new ApplicationException( CommonProcessPaymentCompositeService.class, MessageUtility.message( 'banner.payment.message.error.spriden.id.not.available' ) )
        }
        if (!procedureParam.proc_code_in) {
            throw new ApplicationException( CommonProcessPaymentCompositeService.class, MessageUtility.message( 'banner.payment.message.error.processCode.not.available' ) )
        }
        if (!procedureParam.sub_code_in) {
            throw new ApplicationException( CommonProcessPaymentCompositeService.class, MessageUtility.message( 'banner.payment.message.error.subCode.not.available' ) )
        }
        if (!procedureParam.sysi_code_in) {
            throw new ApplicationException( CommonProcessPaymentCompositeService.class, MessageUtility.message( 'banner.payment.message.error.sysiCoded.not.available' ) )
        }
        if (!procedureParam.update_function_in) {
            throw new ApplicationException( CommonProcessPaymentCompositeService.class, MessageUtility.message( 'banner.payment.message.error.updateFunction.not.available' ) )
        }
        if (!procedureParam.success_url_in) {
            throw new ApplicationException( CommonProcessPaymentCompositeService.class, MessageUtility.message( 'banner.payment.message.error.successUrl.not.available' ) )
        }
        if (!procedureParam.failure_url_in) {
            throw new ApplicationException( CommonProcessPaymentCompositeService.class, MessageUtility.message( 'banner.payment.message.error.failureUrl.not.available' ) )
        }
    }

    /**
     * Get Payment related information texts
     * @return
     */
    def getPaymentInfoTexts() {
        GeneralInformationTextUtility.getMessages( 'AR_PAYMENTPROCESSUPDATE' )
    }

    /**
     * Gets application configuration for given key
     * @param name
     * @param type
     * @return
     */
    public String getAppConfig( String name, String type ) {
        ConfigurationData.fetchByNameAndType( name, type, 'GENERAL_SS' )?.value
    }

    /**
     * Check if Card is configured
     * @param params
     * @param messageMap
     */
    def checkIfOneActiveCreditCardAssociatedWithMerchant( params, Map<String, String> messageMap ) {
        //one active credit card associated with the merchant id
        def retCount = 0
        callFunction( "{? = call goksels.f_get_gormerc_count(merchant_id_in =>?, proc_code_in =>?, sysi_code_in =>?,  status_ind_in => ?)}",
                      [Sql.NUMERIC, params.sub_code_in, params.proc_code_in, params.sysi_code_in, 'Y'], {ret ->
            retCount = ret
        } )
        if (retCount == 0) {
            throw new ApplicationException( CommonProcessPaymentCompositeService.class, messageMap['payment.process.goamred.record.not.found'] )
        }


    }

    /**
     * Gets Address
     * @param param
     * @param addressHierarchy
     * @param sysInd
     * @return
     */
    def getAddress( param, addressHierarchy, sysInd ) {
        Sql sql
        sql = new Sql( sessionFactory.getCurrentSession().connection() )
        sql.eachRow( """SELECT spraddr_house_number, spraddr_street_line1, spraddr_street_line2,  spraddr_street_line3,
                    spraddr_street_line4,spraddr_city,
                               spraddr_stat_code, spraddr_zip, spraddr_natn_code FROM   spraddr
                        WHERE  spraddr.rowid = f_get_address_rowid(?,
                               ?,'A',SYSDATE,1,?,'')""", [param.pidm, addressHierarchy, sysInd] ) {
            param << [house_no_fetch: it.spraddr_house_number, address1_in: it.spraddr_street_line1,
                      address2_in   : it.spraddr_street_line2, address3_in: it.spraddr_street_line3,
                      address4_in   : it.spraddr_street_line4, city_in: it.spraddr_city,
                      state_in      : it.spraddr_stat_code, zip_in: it.spraddr_zip,
                      natn_in       : it.spraddr_natn_code]
        }
    }


    def getPersonDetails( param ) {
        Sql sql
        sql = new Sql( sessionFactory.getCurrentSession().connection() )
        sql.eachRow( """SELECT
                        spriden_first_name,
                        spriden_mi,
                        spriden_last_name,
                        spriden_surname_prefix,
                        spbpers_name_prefix,
                        spbpers_name_suffix
                      FROM spriden, spbpers
                      WHERE spriden_pidm = ?
                            AND spriden_change_ind IS NULL
                            AND spriden_pidm = spbpers_pidm (+)""", [param.pidm] ) {
            param << [first_name_in    : it.spriden_first_name, last_name_in: it.spriden_last_name, middle_name_in: it.spriden_mi,
                      surname_prefix_in: it.spriden_surname_prefix, name_prefix_in: it.spbpers_name_prefix, name_suffix_in: it.spbpers_name_suffix]
        }
    }

    /**
     * Get locale base amount
     * @param amount
     * @param fractionDigits
     * @return
     */
    static getLocaleBasedFormattedNumber( amount, fractionDigits ) {
        amount = amount ?: 0.0
        Locale fmtLocale = LocaleContextHolder.getLocale()
        NumberFormat formatter = NumberFormat.getInstance( fmtLocale );
        formatter.setMaximumFractionDigits( fractionDigits );
        formatter.setMinimumFractionDigits( fractionDigits );
        formatter.format( amount )
    }

    /**
     *
     * @param tnxId
     * @return
     */
    def getSecuredEncodedTransactionId( String tnxId ) {
        def encodedTnxId
        callFunction( "{? = call GOKBSSF.F_ENCODE( str => ?)}",
                      [Sql.VARCHAR, tnxId], {ret ->
            encodedTnxId = ret
        } )
        encodedTnxId
    }

    /**
     *
     * @return
     */
    def encode( String inputString ) {
        if (inputString) {
            inputString = inputString.replaceAll( '%', '%25' )
                    .replaceAll( '\\+', '%2B' )
                    .replaceAll( ' ', '+' )
                    .replaceAll( '/', '%2F' )
                    .replaceAll( ':', '%3A' )
                    .replaceAll( ';', '%3B' )
                    .replaceAll( '@', '%40' )
                    .replaceAll( '&', '%26' )
                    .replaceAll( '=', '%3D' )
                    .replaceAll( '\\?', '%3F' )
                    .replaceAll( "'", '%27' )
                    .replaceAll( '"', '%22' )
                    .replaceAll( '#', '%23' )
        }
        inputString
    }
    /**
     * Process data to get payment URL
     * @param procedureParam
     * @return
     */
    def getPaymentUrl( Map procedureParam ) {
        String createInfo
        if (procedureParam.retrieve_addr_in == 'Y' && procedureParam.cc_use_addr_in == 'Y') {
            getAddress( procedureParam, 'WEBCCADDR', 'P' )
        }
        if (!procedureParam.last_name_in && procedureParam.pidm_in) {
            getPersonDetails( procedureParam )
        }
        String statusStartedMsg = MessageUtility.message( 'banner.payment.message.debug.start.message' )
        def statusStarted = '00-' + new SimpleDateFormat( "HHmmss" ).format( new Date() )
        callFunction( """{call gb_payment.p_create(
                                  p_id                => ?,
                                  p_pidm              => ?,
                                  p_aidm              => ?,
                                  p_term_code         => ?,
                                  p_location          => NULL,
                                  p_detail            => NULL,
                                  p_house_number      => ?,
                                  p_street_line1      => ?,
                                  p_street_line2      => ?,
                                  p_street_line3      => ?,
                                  p_street_line4      => ?,
                                  p_city              => ?,
                                  p_stat_code         => ?,
                                  p_zip               => ?,
                                  p_natn_code         => ?,
                                  p_amount            => ?,
                                  p_merchant_id       => ?,
                                  p_debug_msg         => ?,
                                  p_status            => ?,
                                  p_pay_trans_id      => ?,
                                  p_vendor_status     => NULL,
                                  p_banner_status     => NULL,
                                  p_vendor_error_msg  => NULL,
                                  p_application_data  => ?,
                                  p_update_function   => ?,
                                  p_success_url       => ?,
                                  p_failure_url       => ?,
                                  p_vendor_auth_code  => NULL,
                                  p_vendor_refer_no   => NULL,
                                  p_sub_code          => ?,
                                  p_proc_code         => ?,
                                  p_sysi_code         => ?,
                                  p_appl_no           => ?,
                                  p_gift_no           => ?,
                                  p_last_name         => ?,
                                  p_first_name        => ?,
                                  p_middle_name       => ?,
                                  p_surname_prefix    => ?,
                                  p_name_prefix       => ?,
                                  p_name_suffix       => ?,
                                  p_rowid_out         => ?)}""", [procedureParam.id_in,
                                                                  procedureParam.pidm_in,
                                                                  procedureParam.aidm_in,
                                                                  procedureParam.term_code_in,
                                                                  procedureParam.house_no_fetch,
                                                                  procedureParam.address1_in,
                                                                  procedureParam.address2_in,
                                                                  procedureParam.address3_in,
                                                                  procedureParam.address4_in,
                                                                  procedureParam.city_in,
                                                                  procedureParam.state_in,
                                                                  procedureParam.zip_in,
                                                                  procedureParam.natn_in,
                                                                  procedureParam.amount_in,
                                                                  procedureParam.sub_code_in,
                                                                  statusStarted,
                                                                  statusStartedMsg,
                                                                  procedureParam.pay_trans_in,
                                                                  procedureParam.user_extra_data_in,
                                                                  procedureParam.update_function_in,
                                                                  procedureParam.success_url_in,
                                                                  procedureParam.failure_url_in,
                                                                  procedureParam.sub_code_in,
                                                                  procedureParam.proc_code_in,
                                                                  procedureParam.sysi_code_in,
                                                                  procedureParam.appl_no_in,
                                                                  procedureParam.gift_no_in,
                                                                  procedureParam.last_name_in,
                                                                  procedureParam.first_name_in,
                                                                  procedureParam.middle_name_in,
                                                                  procedureParam.surname_prefix_in,
                                                                  procedureParam.name_prefix_in,
                                                                  procedureParam.name_suffix_in,
                                                                  Sql.VARCHAR], {returnMessage ->
            createInfo = returnMessage
                      } )
        String amountStr = procedureParam.amount_in;
        amountStr = encode( amountStr )

        String transactionId = encode( getSecuredEncodedTransactionId( procedureParam.pay_trans_in.toString() ) )
        String procCodeDesc = encode( procedureParam.proc_code_desc_in?.trim() )
        String subCode = encode( procedureParam.sub_code_in )
        String vendor = procedureParam.vendor_in
        def procCode = procedureParam.proc_code_in
        def finalUrl = "${procedureParam.vendor_url_in}TransactionId=$transactionId&TransactionAmount=$amountStr&TransactionDescription=$procCodeDesc&MerchantID=$subCode"
        if (vendor.toUpperCase().replaceAll( ' ', '' ).equals( 'TOUCHNET' )) {
            finalUrl += '&ProcessCode=' + encode( procCode )
        }
        finalUrl
    }

    /**
     * Utility to call stored procedure
     * @param callFunction
     * @param params
     * @param resultClosure
     * @return
     */
    def callFunction( String callFunction, params, Closure resultClosure ) {
        Sql sql
        log.debug( "callFunction :: callFunction : $callFunction :: params : $params" )
        sql = new Sql( sessionFactory.getCurrentSession().connection() )
        sql.call( callFunction, params, resultClosure )
    }

    /**
     * Get Payment Configuration.
     */
    def getPaymentConfiguration( params = [:] ) {
        def pciEnabled = getAppConfig( 'banner.pci.enabled.payment.gateway.available', 'boolean' )
        def paymentServiceURL = null
        if (!pciEnabled?.toBoolean()) {
            paymentServiceURL = getAppConfig( 'banner.nonpci.payment.gateway.url', 'string' )
        }
        [pciEnabled: pciEnabled?.toBoolean(),
         nonPciURL : paymentServiceURL]
    }
}
