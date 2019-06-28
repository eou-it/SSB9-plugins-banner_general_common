/*********************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.merge

import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.general.communication.item.CommunicationItem
import net.hedtech.banner.general.communication.template.CommunicationMessage
import net.hedtech.banner.general.communication.template.CommunicationTemplate
import net.hedtech.banner.general.system.LetterProcessLetter
import net.hedtech.banner.service.ServiceBase

/**
 * Manages insert/update into GURMAIL for bacs and/or mil tracking enabled communications.
 */
@Transactional
@Slf4j
class CommunicationGurmailTrackingService extends ServiceBase {

    def mailService

    public trackGURMAIL(CommunicationRecipientData recipientData, CommunicationItem item, CommunicationMessage message) {
        try {
            //get the letter code for the reference id
            CommunicationTemplate template = CommunicationTemplate.fetchByIdAndMepCode(recipientData.templateId, recipientData.mepCode)
            if(Holders?.config.communication.bacsEnabled && template?.type?.equalsIgnoreCase('B')) {
                //update gurmail as the template is a BACS template
                 doGurmailAndBacsUpdate(recipientData, item, retrieveLetterCode(recipientData.getReferenceId()))
            } else {
                // insert into gurmail table
                if (Holders?.config.communication.bannerMailTrackingEnabled) {
                    mailService.create(CommunicationCommonUtility.newBannerMailObject(recipientData, message.dateSent, retrieveLetterCode(recipientData.getReferenceId()), item.id))
                }
            }
        } catch (Exception e) {
            log.debug("Could not insert into gurmail: ${e.getMessage()}")
        }
    }

    private LetterProcessLetter retrieveLetterCode (refId) {
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        def letterid = ''
        LetterProcessLetter letter
        try {
            sql.eachRow("""SELECT gcrlmap_letr_id letterID  FROM gcrlmap WHERE gcrlmap_reference_id = ?""", [refId]) { row ->
                letterid = row.letterID
            }
        } catch (Exception le) {
            log.debug("Could not retrieve letter code for the reference id: ${le.getMessage()}")
        } finally {
            if (letterid != null) {
                letter = LetterProcessLetter.get(letterid)
            }
        }
        return letter
    }

    private  doGurmailAndBacsUpdate(CommunicationRecipientData crd, CommunicationItem citem, LetterProcessLetter letter) {

        def sqlString =
                """
                    SELECT
                        TBRISTL_REF_NUMBER,
                        TKRDDIC_SEQ_NO
                    FROM    GKRDIRD, GXRDIRD, TKRBACS, TKRDDIR, TKRDDIC, GURMAIL, TBRISTL
                     WHERE  GURMAIL_DATE_PRINTED IS NULL
                       AND  TBRISTL_PIDM         = ?
                       AND  GURMAIL_LETR_CODE    = ?
                       --
                       AND  TBRISTL_PIDM         = GURMAIL_PIDM
                       AND  TBRISTL_TERM_CODE    = GURMAIL_TERM_CODE
                       AND  TBRISTL_REF_NUMBER   = GURMAIL_MISC_NUM
                       --
                       AND  TKRDDIC_PIDM    =  TBRISTL_PIDM
                       AND  TKRDDIC_SEQ_NO  = (SELECT MAX(TKRDDIC_SEQ_NO)
                                                 FROM   TKRDDIC
                                                 WHERE  TKRDDIC_PIDM         = TBRISTL_PIDM
                                                 AND    TKRDDIC_PLAN_REF_NUM = TBRISTL_REF_NUMBER)
                       --
                       AND    TKRDDIR_INSTALLMENT_PLAN = TBRISTL_INSTALLMENT_PLAN
                       AND    TKRDDIR_TERM_CODE        = TBRISTL_TERM_CODE
                       AND    NVL(TKRDDIR_NOTIF_LETR_CODE,TKRBACS_NOTIF_LETR_CODE) = GURMAIL_LETR_CODE
                       --
                       AND    TKRBACS_CODE             = TKRDDIR_BACS_CODE
                       --
                       AND    GXRDIRD_PIDM     = TKRDDIC_PIDM
                       AND    GXRDIRD_PRIORITY = TKRDDIC_PRIORITY
                       --
                       AND    GKRDIRD_PIDM     = GXRDIRD_PIDM
                       AND    GKRDIRD_PRIORITY = GXRDIRD_PRIORITY
                """

        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        try {
            sql.eachRow(sqlString, [crd.pidm, letter.code]) { row ->
                def stmt = "{call tkkddnp.PR_SET_INST_PAYER_NOTIF(?,?,?,?,?}"
                def params = [crd.pidm, row.TBRISTL_REF_NUMBER, row.TKRDDIC_SEQ_NO, letter.code, citem.lastModified]
                sql.call stmt, params
            }
        }
        catch (Exception ae) {
            log.error("Exception when trying to update gurmail row for BACS ${ae} ")
        }
        finally {

        }
    }

}