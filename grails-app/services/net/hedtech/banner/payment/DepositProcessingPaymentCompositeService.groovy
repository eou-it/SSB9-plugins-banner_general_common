/** *****************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.payment

import groovy.sql.Sql
import net.hedtech.banner.MessageUtility
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.person.PersonIdentificationName
import net.hedtech.banner.general.person.PersonUtility

/**
 * Composite Service for Processing Ellucian Payment gateway process
 */
class DepositProcessingPaymentCompositeService extends CommonProcessPaymentCompositeService {
    def springSecurityService


    @Override
    def preValidation( param, messageMap ) {
        def pidm = springSecurityService.getAuthentication().user.pidm
        param.pidm = pidm
        validateInputs( param )
        param.moduleName = 'BWSKPAYG'
    }


    @Override
    def assignSubCode( param ) {
        param << [sub_code_in: getSubCode()]
    }


    @Override
    def prepareDataToProcess( param ) {
        processCollectionOfTransactionRecords( param )
        PersonIdentificationName person = PersonUtility.getPerson( springSecurityService.getAuthentication().user.pidm )
        param << [first_name_in: person.firstName, last_name_in: person.lastName]
        param.pidm = springSecurityService.getAuthentication().user.pidm
        getAddress( param, 'WPAYADDR', 'S' )
        param << [proc_code_in      : processCodeModule['BWSKPAYG'],
                  sysi_code_in      : 'S',
                  retrieve_addr_in  : 'N',
                  cc_use_addr_in    : 'Y',
                  pidm_in           : springSecurityService.getAuthentication().user.pidm,
                  term_code_in      : param.term,
                  amount_in         : param.amount,
                  failure_url_in    : 'bwskpayg.P_DispFailure?transaction_id=',
                  success_url_in    : 'bwskpayg.P_DispSuccess?transaction_id=',
                  update_function_in: 'bwskpayg.f_update_payments']
    }

    /**
     * Validates Input parameters
     * @param param
     */
    def validateInputs( param ) {
        if (!param.amount) {
            throw new ApplicationException( DepositProcessingPaymentCompositeService.class, MessageUtility.message( 'banner.payment.message.error.invalid.amount' ) )
        }
        if (!param.term) {
            throw new ApplicationException( DepositProcessingPaymentCompositeService.class, MessageUtility.message( 'banner.payment.message.error.invalid.term' ) )
        }
        if (param.amount instanceof String) {
            try {
                Double.parseDouble( param.amount )
            } catch (NumberFormatException) {
                throw new ApplicationException( DepositProcessingPaymentCompositeService.class, MessageUtility.message( 'banner.payment.message.error.invalid.format.amount' ) )
            }
        }
        param.amount = param.amount as double
        if (param.amount <= 0) {
            throw new ApplicationException( DepositProcessingPaymentCompositeService.class, MessageUtility.message( 'banner.payment.message.error.invalid.format.amount' ) )
        }
        validatesTotals( param )
    }

    /**
     * Gets Sub code
     * @return
     */
    def getSubCode( defaultGroup = null ) {
        def subCode
        callFunction( "{ ? = call gokfunc.f_get_default_subcode(group_in =>  ?, default_label_in => ?, default_group_in => ? )}", [Sql.VARCHAR, 'WEBPAYGCCID', 'DEFAULT', defaultGroup], {returnMessage ->
            subCode = returnMessage
        } )
        println( 'subCode' + subCode )
        subCode
    }

    /**
     * Process Transaction records
     * @param param
     * @return
     */
    def processCollectionOfTransactionRecords( param ) {
        def transId
        Sql sql = new Sql( sessionFactory.getCurrentSession().connection() )
        sql = new Sql( sessionFactory.getCurrentSession().connection() )
        transId = getTransactionId()
        def user = springSecurityService.getAuthentication().user
        def dataOrigin = 'Banner'
        sql.eachRow( "select * FROM tbrpytr WHERE tbrpytr_pay_trans_id = ?", [user.pidm * -1] ) {it ->
            sql.call( """{call tb_pay_trans.p_create(
                         p_pay_trans_id => ?,
                         p_data_origin => ?,
                         p_ptyp_code    => ?,
                         p_process => ?,
                         p_code => ?,
                         p_amount => ?,
                         p_dep_release_ind => ?,
                         p_dep_rel_date    => ?,
                         p_dep_exp_date    => ?,
                         p_dep_min_amount  => ?,
                         p_rowid_out => ?
                        )
                    }""", [transId, dataOrigin, it.tbrpytr_ptyp_code, it.tbrpytr_process, it.tbrpytr_code, it.tbrpytr_amount,
                           it.tbrpytr_dep_release_ind, it.tbrpytr_dep_rel_date, it.tbrpytr_dep_exp_date, it.tbrpytr_dep_min_amount, Sql.VARCHAR] )
        }
        sql.call( """{call tb_pay_trans.p_delete_all( p_pay_trans_id => ?)}""", [user.pidm * -1] )
        param << [pay_trans_in: transId]
    }

    /**
     *
     * @param param
     */
    def validatesTotals( param ) {
        def pidm = springSecurityService.getAuthentication().user.pidm
        def verifyTotalAmount = 0
        callFunction( "{? = call tb_pay_trans.f_get_total(p_pay_trans_tab => tb_pay_trans.f_query_set(p_pay_trans_id => ? *-1))}", [Sql.NUMERIC, pidm], {returnMessage ->
            verifyTotalAmount = returnMessage
        } )
        if (verifyTotalAmount != param.amount) {
            throw new ApplicationException( DepositProcessingPaymentCompositeService.class, MessageUtility.message( 'banner.payment.message.error.totals.not.match' ) )
        }
    }
}
