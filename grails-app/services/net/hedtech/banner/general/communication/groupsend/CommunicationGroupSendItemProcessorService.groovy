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
import net.hedtech.banner.service.ServiceBase
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
            CommunicationRecipientData recipientData = buildRecipientData( groupSend.createdBy, groupSend.template, groupSendItem.referenceId, groupSendItem.recipientPidm, groupSend.organization )
            recipientData = communicationRecipientDataService.create( recipientData )
            log.debug( "Created recipient data with referenceId = " + groupSendItem.referenceId + "." )

            log.debug( "Creating communication job with reference id = " + recipientData.referenceId )
            CommunicationJob communicationJob = new CommunicationJob( referenceId: recipientData.referenceId )
            communicationJob = communicationJobService.create( communicationJob )

            log.debug( "Updating group send item to mark it complete with reference id = " + recipientData.referenceId )

            def groupSendItemParamMap = [
                id : groupSendItem.id,
                version : groupSendItem.version,
                currentExecutionState : CommunicationGroupSendItemExecutionState.Complete,
                stopDate: new Date()
            ]
            communicationGroupSendItemService.update( groupSendItemParamMap )
        } else {
            def groupSendItemParamMap = [
                id : groupSendItem.id,
                version : groupSendItem.version,
                currentExecutionState : CommunicationGroupSendItemExecutionState.Stopped,
                stopDate: new Date()
            ]
            communicationGroupSendItemService.update( groupSendItemParamMap )
        }

    }

    public void failGroupSendItem( Long groupSendItemId, String errorText ) {
        CommunicationGroupSendItem groupSendItem = communicationGroupSendItemService.get( groupSendItemId )
        groupSendItem.setCurrentExecutionState( CommunicationGroupSendItemExecutionState.Failed )
        groupSendItem.setErrorText( errorText )
        log.warn( "Group send item failed id = ${groupSendItemId}, errorText = ${errorText}.")
        groupSendItem.setStopDate( new Date() )
        communicationGroupSendItemService.update( groupSendItem )
    }


    private CommunicationRecipientData buildRecipientData( String senderOracleUserName, CommunicationTemplate template, String referenceId, Long recipientPidm, CommunicationOrganization organization ) {
        log.debug( "Creating recipient data with referenceId = " + referenceId + "." )

        CommunicationEmailTemplate emailTemplate
        if (template instanceof CommunicationEmailTemplate) {
            emailTemplate = (CommunicationEmailTemplate) template
        }

        Authentication originalAuthentication = SecurityContextHolder.getContext().getAuthentication()
        try {
            FormContext.set( ['CMQUERYEXECUTE'] )
            if (log.isDebugEnabled()) log.debug( "Spoofed as ${senderOracleUserName} for creating recipient data." )

            // Can this list be cached somewhere for similar processing
            List<String> fieldNames = communicationTemplateMergeService.extractTemplateVariables( emailTemplate?.toList?.toString() )
            communicationTemplateMergeService.extractTemplateVariables( emailTemplate?.subject?.toString() ).each {
                fieldNames << it
            }
            communicationTemplateMergeService.extractTemplateVariables( emailTemplate?.subject?.toString() ).each {
                fieldNames << it
            }
            communicationTemplateMergeService.extractTemplateVariables( emailTemplate?.content?.toString() ).each {
                fieldNames << it
            }
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

            return new CommunicationRecipientData(
                pidm: recipientPidm,
                templateId: template.id,
                referenceId: referenceId,
                ownerId: senderOracleUserName,
                fieldValues: nameToValueMap,
                organization: organization
            )
        } finally {
            SecurityContextHolder.getContext().setAuthentication( originalAuthentication )
        }

    }
}
