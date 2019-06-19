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
}