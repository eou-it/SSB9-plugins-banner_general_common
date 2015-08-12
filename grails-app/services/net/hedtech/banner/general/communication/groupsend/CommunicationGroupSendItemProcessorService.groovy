/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.groupsend

import groovy.sql.Sql
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

import java.sql.SQLException

/**
 * Process a group send item to the point of creating recipient merge data values and submitting an individual communication job
 * for the recipient.
 */
class CommunicationGroupSendItemProcessorService {
    boolean transactional = true
    def log = Logger.getLogger(this.getClass())
    def communicationGroupSendItemService
    def communicationTemplateMergeService
    def communicationFieldCalculationService
    def communicationJobService
    def communicationRecipientDataService
    def communicationOrganizationService
    def sessionFactory
    def asynchronousBannerAuthenticationSpoofer
    private static final int noWaitErrorCode = 54;


    public void performGroupSendItem(Long groupSendItemId) {
        log.debug("Performing group send item id = " + groupSendItemId)
        boolean locked = lockGroupSendItem(groupSendItemId, CommunicationGroupSendItemExecutionState.Ready);
        if (!locked) {
            // Do nothing
            return;
        }

        CommunicationGroupSendItem groupSendItem = (CommunicationGroupSendItem) communicationGroupSendItemService.get(groupSendItemId)
        CommunicationGroupSend groupSend = groupSendItem.communicationGroupSend
        asynchronousBannerAuthenticationSpoofer.setMepProcessContext(sessionFactory.currentSession.connection(), groupSendItem.mepCode)
        if (!groupSend.getCurrentExecutionState().isTerminal()) {
            CommunicationRecipientData recipientData = buildRecipientData(groupSend.createdBy, groupSend.template, groupSendItem.referenceId, groupSendItem.recipientPidm, groupSendItem.mepCode,groupSend.organization)
            recipientData = (CommunicationRecipientData) communicationRecipientDataService.create(recipientData)
            log.debug("Created recipient data with referenceId = " + groupSendItem.referenceId + ".")

            log.debug("Creating communication job with reference id = " + recipientData.referenceId)
            CommunicationJob communicationJob = new CommunicationJob(referenceId: recipientData.referenceId)
            communicationJobService.create(communicationJob)

            log.debug("Updating group send item to mark it complete with reference id = " + recipientData.referenceId)

            def groupSendItemParamMap = [
                    id                   : groupSendItem.id,
                    version              : groupSendItem.version,
                    currentExecutionState: CommunicationGroupSendItemExecutionState.Complete,
                    stopDate             : new Date()
            ]
            communicationGroupSendItemService.update(groupSendItemParamMap)
        } else {
            def groupSendItemParamMap = [
                    id                   : groupSendItem.id,
                    version              : groupSendItem.version,
                    currentExecutionState: CommunicationGroupSendItemExecutionState.Stopped,
                    stopDate             : new Date()
            ]
            communicationGroupSendItemService.update(groupSendItemParamMap)
        }

    }


    public void failGroupSendItem(Long groupSendItemId, String errorCode, String errorText ) {
        CommunicationGroupSendItem groupSendItem = (CommunicationGroupSendItem) communicationGroupSendItemService.get(groupSendItemId)
        def groupSendItemParamMap = [
                id                   : groupSendItem.id,
                version              : groupSendItem.version,
                currentExecutionState: CommunicationGroupSendItemExecutionState.Failed,
                stopDate             : new Date(),
                errorText            : errorText,
                errorCode            : errorCode
        ]

        log.warn("Group send item failed id = ${groupSendItemId}, errorText = ${errorText}.")

        communicationGroupSendItemService.update(groupSendItemParamMap)
    }


    private CommunicationRecipientData buildRecipientData(String senderOracleUserName, CommunicationTemplate template, String referenceId, Long recipientPidm, String mepCode, CommunicationOrganization organization) {
        log.debug("Creating recipient data with referenceId = " + referenceId + ".")

        CommunicationEmailTemplate emailTemplate
        if (template instanceof CommunicationEmailTemplate) {
            emailTemplate = (CommunicationEmailTemplate) template
        } else {
            throw new RuntimeException("Template of type ${template?.getClass()?.getSimpleName()} is not supported.")
        }

        if (log.isDebugEnabled()) log.debug("Spoofed as ${senderOracleUserName} for creating recipient data.")

        // Can this list be cached somewhere for similar processing
        List<String> fieldNames = communicationTemplateMergeService.extractTemplateVariables(emailTemplate?.toList?.toString())
        communicationTemplateMergeService.extractTemplateVariables(emailTemplate?.subject?.toString()).each {
            fieldNames << it
        }
        communicationTemplateMergeService.extractTemplateVariables(emailTemplate?.subject?.toString()).each {
            fieldNames << it
        }
        communicationTemplateMergeService.extractTemplateVariables(emailTemplate?.content?.toString()).each {
            fieldNames << it
        }
        fieldNames = fieldNames.unique()

        def nameToValueMap = [:]
        fieldNames.each { fieldName ->
            CommunicationField communicationField = CommunicationField.fetchByName(fieldName)
            // Will ignore any not found communication fields (field may have been renamed or deleted, will skip for now.
            // Will come back to this to figure out desired behavior.
            if (communicationField) {

                String value = calculateFieldForUser(communicationField, senderOracleUserName, recipientPidm, mepCode)

                CommunicationFieldValue communicationFieldValue = new CommunicationFieldValue(
                        value: value,
                        renderAsHtml: communicationField.renderAsHtml
                )
                nameToValueMap.put(communicationField.name, communicationFieldValue)
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
    }

    /**
     * Attempts to create a pessimistic lock on the group send item record.
     * @param groupSendItemId the primary key of the group send item.
     * @param state the group send item execution state
     * @return true if the record was successfully locked and false otherwise
     */
    public boolean lockGroupSendItem(final Long groupSendItemId, final CommunicationGroupSendItemExecutionState state) {
        Sql sql = null
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            def rows = sql.rows("select GCRGSIM_SURROGATE_ID from GCRGSIM where GCRGSIM_SURROGATE_ID = ? and GCRGSIM_CURRENT_STATE = ? for update nowait",
                    [groupSendItemId, state.name()],
                    0, 2
            )

            if (rows.size() > 1) {
                throw new RuntimeException("Found more than one GCRGSIM row for a single group send item id")
            } else {
                return rows.size() == 1
            }

        } catch (SQLException e) {
            if (e.getErrorCode() == noWaitErrorCode) {
                return false
            } else {
                throw e
            }
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
    }


    private String calculateFieldForUser(CommunicationField communicationField, String senderOracleUserName, Long recipientPidm, String mepCode) {

        def origmap
        try {
            origmap = asynchronousBannerAuthenticationSpoofer.authenticateAndSetFormContextForExecuteAndSave(senderOracleUserName, mepCode)
            if (log.isDebugEnabled()) log.debug("Authenticated as ${senderOracleUserName} for monitoring.")
            // This method will start a nested transaction (see REQUIRES_NEW annotation) and consequently pick up a new db connection with the current oracle user name
            return communicationFieldCalculationService.calculateFieldByPidmWithNewTransaction(
                    communicationField.getRuleContent(),
                    communicationField.returnsArrayArguments,
                    communicationField.getFormatString(),
                    recipientPidm,
                    mepCode
            )
        }
        finally {
            asynchronousBannerAuthenticationSpoofer.resetAuthAndFormContext(origmap)
        }
    }
}
