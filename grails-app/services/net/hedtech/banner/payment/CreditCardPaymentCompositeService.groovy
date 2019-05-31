/** *****************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.payment

import groovy.sql.Sql
import net.hedtech.banner.MessageUtility
import net.hedtech.banner.exceptions.ApplicationException

import static net.hedtech.banner.general.GeneralCommonUtility.getAppGtvsdax

/**
 * Composite Service for Processing Ellucian Payment gateway process
 */
class CreditCardPaymentCompositeService extends CommonProcessPaymentCompositeService {
    def springSecurityService


    @Override
    def preValidation(param, messageMap) {
        def pidm = springSecurityService.getAuthentication().user.pidm
        param.pidm = pidm
        validateInputs(param)
        checkIfPreventPayment(messageMap)
        param.moduleName = 'BWCKCPMT'
    }


    @Override
    def prepareDataToProcess(param) {
        def pidm = springSecurityService.getAuthentication().user.pidm
        param << [sysi_code_in      : 'S',
                  retrieve_addr_in  : 'Y',
                  cc_use_addr_in    : 'Y',
                  aidm_in           : pidm,
                  term_code_in      : param.term,
                  pidm_in           : pidm,
                  amount_in         : param.amount,
                  failure_url_in    : 'bwskwtrr.P_Failure_Page?transaction_id=',
                  success_url_in    : 'bwckcpmt.P_DispSigPage_cc?transaction_id=',
                  update_function_in: 'bwckcpmt.F_update_accounts',
                  pay_trans_in      : getTransactionId()]
    }


    @Override
    def assignSubCode(param) {
        param << [sub_code_in: getSubCode(param.term)]
    }

    /**
     * Validates Input
     * @param param
     */
    def validateInputs(param) {
        if (!param.amount) {
            throw new ApplicationException(CreditCardPaymentCompositeService.class, MessageUtility.message('banner.payment.message.error.invalid.amount'))
        }
        if (!param.term) {
            throw new ApplicationException(CreditCardPaymentCompositeService.class, MessageUtility.message('banner.payment.message.error.invalid.term'))
        }
        if (param.amount instanceof String) {
            try {
                Double.parseDouble(param.amount)
            } catch (NumberFormatException e) {
                throw new ApplicationException(CreditCardPaymentCompositeService.class, MessageUtility.message('banner.payment.message.error.invalid.format.amount'))
            }
        }
        param.amount = param.amount as double
        if (param.amount <= 0) {
            throw new ApplicationException(CreditCardPaymentCompositeService.class, MessageUtility.message('banner.payment.message.error.invalid.format.amount'))
        }

    }

    /**
     * Checks if Payment is prevented
     * @param messageMap
     * @return
     */
    def checkIfPreventPayment(Map<String, String> messageMap) {
        boolean preventPayment = false
        def checkHoldMessage = checkIfPreventPayment()
        if (checkHoldMessage == 'N') {
            return preventPayment
        }
        if (checkHoldMessage in ['AC_REF_FOR_COLL', 'AC_REF_FOR_HOLD']) {
            throw new ApplicationException(CreditCardPaymentCompositeService.class, messageMap['payment.process.prevent.payment'])
        }
        preventPayment
    }

    /**
     * Checks if Payment is prevented
     * @return
     */
    def checkIfPreventPayment() {
        def pidm = springSecurityService.getAuthentication().user.pidm
        Sql sql
        def tbrcolcCount = 0
        def tbrcolcAgencyDate
        def allowCCPayment
        String preventPayment = 'N'
        String billingHolds
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.eachRow("""SELECT *
                              FROM tbrcolc
                             WHERE tbrcolc_pidm = ?""", [pidm]) { it ->
                tbrcolcCount = 1
                tbrcolcAgencyDate = it.tbrcolc_colc_agency_date
            }
            if (tbrcolcAgencyDate) {
                preventPayment = 'AC_REF_FOR_COLL';
            } else {
                allowCCPayment = getAppGtvsdax('WEBCCHOLDS', 'PAYMENTVENDOR')?.gtvsdaxValue ?: 'Y'
                if (allowCCPayment == 'N') {
                    callFunction("{?= call tsksels.f_check_billing_holds(? )}", [Sql.VARCHAR, pidm], { returnMessage ->
                        billingHolds = returnMessage
                    })
                    if (billingHolds == 'Y') {
                        preventPayment = 'AC_REF_FOR_HOLD';
                    }
                }
            }
        } finally {
            // sql?.close()
        }
        preventPayment
    }

    /**
     *
     * @param termCode
     * @return
     */
    def getSubCode(termCode) {
        def pidm = springSecurityService.getAuthentication().user.pidm
        def sub_code_out
        Sql sql
        def gtvsdax_rec = getAppGtvsdax('USESTUMMID', 'PAYMENTVENDOR')?.gtvsdaxValue
        def levl, college, campus
        if (gtvsdax_rec == 'Y') {
            if (!termCode) {
                termCode = getAppGtvsdax('DEFAULT', 'WEBCCDEFTERM')?.gtvsdaxValue
            }
            if (!termCode) {
                callFunction("{?= call gokfunc.f_get_default_subcode(group_in =>?, default_label_in=> ?, default_group_in =>  ? )}", [Sql.VARCHAR, 'WEBSTUCCID', 'DEFAULT', 'WEBDEFCCID'], { returnMessage ->
                    sub_code_out = returnMessage
                })
            } else {
                try {

                    sql = new Sql(sessionFactory.getCurrentSession().connection())
                    def recordFound = false
                    sql.eachRow("""SELECT *
                            FROM sgbstdn
                           WHERE sgbstdn_pidm = ?
                             AND sgbstdn_term_code_eff = (SELECT MAX (sgbstdn_term_code_eff)
                                                            FROM sgbstdn
                                                           WHERE sgbstdn_term_code_eff <= ?
                                                             AND sgbstdn_pidm = ?)""", [pidm, termCode, pidm]) { it ->
                        levl = it.sgbstdn_levl_code
                        college = it.sgbstdn_coll_code_1
                        campus = it.sgbstdn_camp_code
                        recordFound = true
                    }
                    if (!recordFound) {
                        callFunction("{?= call gokfunc.f_get_default_subcode(group_in =>?, default_label_in=> ?, default_group_in =>  ? )}", [Sql.VARCHAR, 'WEBSTUCCID', 'DEFAULT', 'WEBDEFCCID'], { returnMessage ->
                            sub_code_out = returnMessage
                        })
                    }

                } finally {
                    //sql?.close()
                }
            }
            if (!sub_code_out) {
                callFunction("{ call gokfunc.p_match_mmid(?, ?, 'COLLEGE',?, 'CAMPUS',?, 'LEVEL', ? )}", [Sql.VARCHAR, 'WEBSTUCCID', college, campus, levl], { returnMessage ->
                    sub_code_out = returnMessage
                })
            }
        }
        if (!sub_code_out) {
            callFunction("{?= call gokfunc.f_get_default_subcode(group_in =>?, default_label_in=> ?, default_group_in =>  ? )}", [Sql.VARCHAR, 'WEBSTUCCID', 'DEFAULT', 'WEBDEFCCID'], { returnMessage ->
                sub_code_out = returnMessage
            })
        }
        sub_code_out
    }
}
