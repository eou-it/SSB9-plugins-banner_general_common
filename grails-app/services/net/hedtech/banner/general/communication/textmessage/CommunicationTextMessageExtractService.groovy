package net.hedtech.banner.general.communication.textmessage

import grails.gorm.transactions.Transactional
import grails.testing.mixin.integration.Integration
import groovy.util.logging.Slf4j
import net.hedtech.banner.MessageUtility
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.folder.CommunicationFolderService
import net.hedtech.banner.general.communication.job.CommunicationJob

@Transactional
@Slf4j
class CommunicationTextMessageExtractService {

    def communicationSendItemService

    def list(Map params) {
        log.trace MessageUtility.message("communication.list.invoked.args", [this.class.simpleName, params] as Object[])

        def max
        if (params.containsKey("max")) max = params.max
        else max = Integer.MAX_VALUE

        def smscommunications = getSMSDetails(max)

        log.trace MessageUtility.message("communication.list.returning.value", [this.class.simpleName, smscommunications] as Object[])
        return smscommunications
    }

    def count( Map params ) {
        log.trace MessageUtility.message( "communication.count.invoked.info", [this.class.simpleName] as Object[] )

        def max
        if (params.containsKey("max")) max = params.max
        else max = Integer.MAX_VALUE
        List<CommunicationTextMessage> smscommunications = getSMSDetails(max)

        log.trace MessageUtility.message( "communication.count.returning.info", [this.class.simpleName,smscommunications?.size()] as Object[] )
        return smscommunications?.size()
    }

    private List<CommunicationTextMessage> getSMSDetails(Integer max) {
        List<CommunicationTextMessage> smscommunications = []

        def textMessages = communicationSendItemService.fetchPendingTextMessages(max)
        List textMessageSurrogateIds = new ArrayList();

        textMessages?.each { textMessage ->
            smscommunications << new CommunicationTextMessage(
                    toList: textMessage.toList,
                    messageContent: textMessage.content,
                    referenceId: textMessage.referenceId
            )
            textMessageSurrogateIds.add(textMessage.id)
        }
        log.debug("reponse: ${smscommunications}")

        //Mark the status of all messages to completed.
        communicationSendItemService.markCompleted(textMessageSurrogateIds);

        return smscommunications
    }
}
