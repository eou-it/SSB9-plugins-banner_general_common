/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.groupsend

import net.hedtech.banner.general.communication.field.CommunicationField
import net.hedtech.banner.general.communication.job.CommunicationJob
import net.hedtech.banner.general.communication.merge.CommunicationFieldValue
import net.hedtech.banner.general.communication.merge.CommunicationRecipientData
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import net.hedtech.banner.general.communication.template.CommunicationEmailTemplate
import net.hedtech.banner.general.communication.template.CommunicationTemplate
import net.hedtech.banner.security.FormContext
import org.apache.log4j.Logger
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Process a group send item to the point of creating recipient merge data values and submitting an individual communication job
 * for the recipient.
 */
class CommunicationGroupSendItemProcessorService {
    boolean transactional = true

    def log = Logger.getLogger( this.getClass() )
    def communicationGroupSendService
    def communicationGroupSendItemService
    def asynchronousBannerAuthenticationSpoofer
    def communicationTemplateMergeService
    def communicationFieldCalculationService
    def communicationJobService
    def communicationRecipientDataService


//    def communicationTemplateService
//    def communicationPopulationSelectionListService
    def communicationOrganizationService


    public void performGroupSendItem( Long groupSendItemId ) {
        log.debug( "Performing group send item id = " + groupSendItemId )
//        boolean locked = groupSendItemJdbcDaoSupport.lockGroupSendItem( groupSendItemKey, GroupSendItemExecutionState.Ready);
//        if (!locked) {
//            // Do nothing
//            return;
//        }

        CommunicationGroupSendItem groupSendItem = communicationGroupSendItemService.get( groupSendItemId )
        CommunicationGroupSend groupSend = groupSendItem.communicationGroupSend

        if (!groupSend.getCurrentExecutionState().isTerminal()) {
            CommunicationRecipientData recipientData = createRecipientData( groupSend.createdBy, groupSend.template, groupSendItem.referenceId, groupSendItem.recipientPidm, groupSend.organization )
            CommunicationJob communicationJob = new CommunicationJob( referenceId: recipientData.referenceId )
            communicationJob = communicationRecipientDataService.create( communicationJob )

            groupSendItem.setCurrentExecutionState( CommunicationGroupSendItemExecutionState.Complete );
            groupSendItem.setStopDate( new Date() );
            communicationGroupSendItemService.update( groupSendItem );
        } else {
            groupSendItem.setCurrentExecutionState( CommunicationGroupSendItemExecutionState.Stopped );
            groupSendItem.setStopDate( new Date() );
            communicationGroupSendItemService.update( groupSendItem );
        }

    }


    private void createRecipientData( String senderOracleUserName, CommunicationTemplate template, String referenceId, Long recipientPidm, CommunicationOrganization organization ) {
        log.debug( "Creating recipient data with referenceId = " + referenceId + "." )
        CommunicationEmailTemplate emailTemplate = template as CommunicationEmailTemplate

        Authentication originalAuthentication = SecurityContextHolder.getContext().getAuthentication()
        try {
            FormContext.set( ['CMQUERYEXECUTE'] )
            if (log.isDebugEnabled()) log.debug( "Spoofed as ${senderOracleUserName} for creating recipient data." )

            // Can this list be cached somewhere for similar processing
            List<String> fieldNames = communicationTemplateMergeService.extractTemplateVariables( emailTemplate.toList.toString() )
            fieldNames << communicationTemplateMergeService.extractTemplateVariables( emailTemplate.subject.toString() )
            fieldNames << communicationTemplateMergeService.extractTemplateVariables( emailTemplate.content.toString() )
            fieldNames = fieldNames.unique()

            def nameToValueMap = [:]
            fieldNames.each { fieldName ->
                CommunicationField communicationField = CommunicationField.fetchByName( fieldName )
                // Will ignore any not found communication fields (field may have been renamed or deleted, will skip for now.
                // Will come back to this to figure out desired behavior.
                if (communicationField) {
                    String value = communicationFieldCalculationService.calculateFieldByPidm( communicationField.immutableId, recipientPidm )
                    CommunicationFieldValue communicationFieldValue = new CommunicationFieldValue(
                        value: value,
                        renderAsHtml: communicationField.renderAsHtml
                    )
                    nameToValueMap.put( communicationField.name, communicationFieldValue )
                }
            }

            CommunicationRecipientData recipient = new CommunicationRecipientData(
                pidm: recipientPidm,
                templateId: template.id,
                referenceId: referenceId,
                ownerId: senderOracleUserName,
                fieldValues: nameToValueMap,
                organization: this.organization
            )
            recipient = communicationRecipientDataService.create( recipient )
            log.debug( "Created recipient data with referenceId = " + referenceId + "." )
        } finally {
            SecurityContextHolder.getContext().setAuthentication( originalAuthentication )
        }

    }
}
