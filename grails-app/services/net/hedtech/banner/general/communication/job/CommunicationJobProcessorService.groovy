/********************************************************************************
  Copyright 2017-2019 Ellucian Company L.P. and its affiliates.
********************************************************************************/
package net.hedtech.banner.general.communication.job

import net.hedtech.banner.general.communication.letter.CommunicationGenerateLetterService
import net.hedtech.banner.general.communication.merge.CommunicationRecipientData
import net.hedtech.banner.general.communication.template.CommunicationMessage
import net.hedtech.banner.general.communication.template.CommunicationTemplate
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory


/**
 * CommunicationJobProcessorService is responsible for orchestrating the communication send in behalf of a communication job.
 */
class CommunicationJobProcessorService {
    private final Log log = LogFactory.getLog(this.getClass());
    def communicationJobService
    def communicationTemplateMergeService
    def communicationSendEmailService
    def communicationSendMobileNotificationService
    def communicationTemplateService
    def communicationGenerateLetterService
    def communicationSendTextMessageService

    public void performCommunicationJob( Long jobId ) {
        log.debug( "performed communication job with job id = ${jobId}." )



        CommunicationJob job = communicationJobService.get( jobId )
        List<CommunicationRecipientData> recipientDataList = CommunicationRecipientData.fetchByReferenceId( job.referenceId )
        CommunicationRecipientData recipientData = recipientDataList.size() ? recipientDataList[0] : null

        CommunicationTemplate template = CommunicationTemplate.fetchByIdAndMepCode( recipientData.templateId, recipientData.mepCode )

        CommunicationMessageGenerator messageGenerator = new CommunicationMessageGenerator(
            communicationTemplateMergeService: communicationTemplateMergeService
        )
        CommunicationMessage message = messageGenerator.generate( template, recipientData )

        CommunicationMessageDispatcher messageDispatcher = new CommunicationMessageDispatcher(
            communicationSendEmailService: communicationSendEmailService,
            communicationSendMobileNotificationService: communicationSendMobileNotificationService,
            communicationGenerateLetterService: communicationGenerateLetterService,
            communicationSendTextMessageService: communicationSendTextMessageService
        )
        messageDispatcher.dispatch( template, recipientData, message )
    }

}
