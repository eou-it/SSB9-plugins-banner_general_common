/********************************************************************************
  Copyright 2017 Ellucian Company L.P. and its affiliates.
********************************************************************************/
package net.hedtech.banner.general.communication.testsend

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.CommunicationErrorCode
import net.hedtech.banner.general.communication.email.CommunicationEmailMessage
import net.hedtech.banner.general.communication.email.CommunicationEmailTemplate
import net.hedtech.banner.general.communication.exceptions.CommunicationExceptionFactory
import net.hedtech.banner.general.communication.item.CommunicationChannel
import net.hedtech.banner.general.communication.job.CommunicationMessageGenerator
import net.hedtech.banner.general.communication.letter.CommunicationGenerateLetterService
import net.hedtech.banner.general.communication.letter.CommunicationLetterItem
import net.hedtech.banner.general.communication.letter.CommunicationLetterMessage
import net.hedtech.banner.general.communication.letter.CommunicationLetterTemplate
import net.hedtech.banner.general.communication.merge.CommunicationRecipientData
import net.hedtech.banner.general.communication.mobile.CommunicationMobileNotificationMessage
import net.hedtech.banner.general.communication.mobile.CommunicationMobileNotificationTemplate
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import net.hedtech.banner.general.communication.template.CommunicationMessage
import net.hedtech.banner.general.communication.template.CommunicationTemplate
import org.apache.commons.lang.StringUtils
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

class CommunicationTestSendCompositeService  {

    private Log log = LogFactory.getLog( this.getClass() )

    def service
    def communicationTemplateMergeService
    def communicationJobService
    def communicationRecipientDataService
    def asynchronousBannerAuthenticationSpoofer
    def communicationFieldCalculationService
    def communicationSendEmailService
    def communicationSendMobileNotificationService
    def communicationGenerateLetterService

    def recipientData
    def channel

    def sendTest (Long pidm, Long organizationId, Long templateId, Map parameterNameValuesMap) {
        switch (channel) {
            case CommunicationChannel.MOBILE_NOTIFICATION:
                sendTestMobileNotification(pidm, organizationId, templateId, parameterNameValuesMap)
                break
            case CommunicationChannel.EMAIL:
                sendTestEmail(pidm, organizationId, templateId, parameterNameValuesMap)
                break
            case CommunicationChannel.LETTER:
                return sendTestLetter(pidm, organizationId, templateId, parameterNameValuesMap)
                break
            default:
                throw CommunicationExceptionFactory.createApplicationException(this.class, new RuntimeException("communication.error.message.templateErrorUnknown"), CommunicationErrorCode.TEMPLATE_ERROR_UNKNOWN.name())
        }
    }

    def sendTestEmail (Long pidm, Long organizationId, Long templateId, Map parameterNameValuesMap) {
        def organization = fetchOrg(organizationId)
        try {
            checkOrgEmail(organization)
        } catch (Throwable t) {
            log.error(t)
            def root = CommunicationOrganization.fetchRoot()
            if (root)
                organization.sendEmailServerProperties = root.sendEmailServerProperties
            checkOrgEmail(organization)
        }
        CommunicationTemplate template = fetchTemplate(templateId)
        def fieldNames = visitEmail(template as CommunicationEmailTemplate)
        def recipientData = createCommunicationRecipientData( pidm, organizationId,  templateId, fieldNames, parameterNameValuesMap)
        saveRecipientData(recipientData)
        try {
            CommunicationMessageGenerator messageGenerator = new CommunicationMessageGenerator(
                    communicationTemplateMergeService: communicationTemplateMergeService
            )
            CommunicationMessage message = messageGenerator.generate(template, recipientData)
            communicationSendEmailService.send(organizationId, message as CommunicationEmailMessage, recipientData, pidm)
        } catch (Throwable t) {
            log.error(t)
            if (t instanceof ApplicationException)
                throw t
            throw CommunicationExceptionFactory.createApplicationException(this.class, new RuntimeException("communication.error.message.testSendTemplate.email"), CommunicationErrorCode.TEMPLATE_ERROR_UNKNOWN.name())
        }
    }

    def sendTestMobileNotification (Long pidm, Long organizationId, Long templateId, Map parameterNameValuesMap) {
        def organization = fetchOrg(organizationId)
        try {
            checkOrgMobile(organization)
        } catch (Throwable t) {
            log.error(t)
            def root = CommunicationOrganization.fetchRoot()
            if (root) {
                organization.mobileApplicationName = root.mobileApplicationName
                organization.mobileEndPointUrl = root.mobileEndPointUrl
                organization.encryptedMobileApplicationKey = root.encryptedMobileApplicationKey
                organization.clearMobileApplicationKey = root.clearMobileApplicationKey
            }
        }
        checkOrgMobile(organization)
        CommunicationTemplate template = fetchTemplate(templateId)
        def fieldNames = visitMobileNotification(template as CommunicationMobileNotificationTemplate)
        def recipientData = createCommunicationRecipientData(pidm, organizationId, templateId, fieldNames, parameterNameValuesMap)
        checkExternalIdExists(pidm)
        saveRecipientData(recipientData)
        try {
            CommunicationMessageGenerator messageGenerator = new CommunicationMessageGenerator(
                    communicationTemplateMergeService: communicationTemplateMergeService
            )
            CommunicationMessage message = messageGenerator.generate(template, recipientData)
            communicationSendMobileNotificationService.send(organizationId, message as CommunicationMobileNotificationMessage, recipientData)
        } catch (Throwable t) {
            log.error(t)
            if (t instanceof ApplicationException)
                throw t
            throw CommunicationExceptionFactory.createApplicationException(this.class, new RuntimeException("communication.error.message.testSendTemplate.mobile"), CommunicationErrorCode.TEMPLATE_ERROR_UNKNOWN.name())
        }
    }

    def sendTestLetter (Long pidm, Long organizationId, Long templateId, Map parameterNameValuesMap) {
        def organization = fetchOrg(organizationId)
        if (!organization)
            throw CommunicationExceptionFactory.createApplicationException(this.class, new RuntimeException("communication.error.message.organizationNotFound"), CommunicationErrorCode.ORGANIZATION_NOT_FOUND.name())

        CommunicationTemplate template = fetchTemplate(templateId)
        def recipientData = visitLetter(template as CommunicationLetterTemplate, pidm, organizationId, templateId, parameterNameValuesMap)
        saveRecipientData(recipientData)
        try {
            CommunicationMessageGenerator messageGenerator = new CommunicationMessageGenerator(
                    communicationTemplateMergeService: communicationTemplateMergeService
            )
            CommunicationMessage message = messageGenerator.generate(template, recipientData)
            def letter = createLetter(organization, (CommunicationLetterMessage) message , recipientData)

            communicationGenerateLetterService.testTemplate = true
            letter.id = communicationGenerateLetterService.send(organizationId, message as CommunicationLetterMessage, recipientData )
            return letter
        } catch (Throwable t) {
            log.error(t)
            if (t instanceof ApplicationException)
                throw t
            throw CommunicationExceptionFactory.createApplicationException(this.class, new RuntimeException("communication.error.message.testSendTemplate.letter"), CommunicationErrorCode.TEMPLATE_ERROR_UNKNOWN.name())
        }
    }

    // HELPER FUNCTIONS

    def visitEmail(CommunicationEmailTemplate template) {
        // Can this list be cached somewhere for similar processing on the same template but different user
        List<String> fieldNames = communicationTemplateMergeService.extractTemplateVariables(template.toList?.toString())
        communicationTemplateMergeService.extractTemplateVariables(template.subject?.toString()).each {
            fieldNames << it
        }
        communicationTemplateMergeService.extractTemplateVariables(template.content?.toString()).each {
            fieldNames << it
        }
        fieldNames = fieldNames.unique()
        return fieldNames
    }

    def visitMobileNotification(CommunicationMobileNotificationTemplate template) {
        // Can this list be cached somewhere for similar processing on the same template but different user
        List<String> fieldNames = new ArrayList<String>()
        communicationTemplateMergeService.extractTemplateVariables(template.mobileHeadline?.toString()).each {
            fieldNames << it
        }
        communicationTemplateMergeService.extractTemplateVariables(template.headline?.toString()).each {
            fieldNames << it
        }
        communicationTemplateMergeService.extractTemplateVariables(template.messageDescription?.toString()).each {
            fieldNames << it
        }
        communicationTemplateMergeService.extractTemplateVariables(template.destinationLink?.toString()).each {
            fieldNames << it
        }
        communicationTemplateMergeService.extractTemplateVariables(template.destinationLabel?.toString()).each {
            fieldNames << it
        }
        fieldNames = fieldNames.unique()
        return fieldNames
    }

    def visitLetter (CommunicationLetterTemplate template, Long pidm, Long organizationId, Long templateId, Map parameterNameValuesMap) {
        // Can this list be cached somewhere for similar processing on the same template but different user
        List<String> fieldNames = communicationTemplateMergeService.extractTemplateVariables(template.toAddress?.toString())
        communicationTemplateMergeService.extractTemplateVariables(template.content?.toString()).each {
            fieldNames << it
        }
        fieldNames = fieldNames.unique()
        def recipientData = createCommunicationRecipientData(pidm, organizationId, templateId, fieldNames, parameterNameValuesMap)
        //escape xml 5 special characters in the field values to enable pdf generation for letters
        def String[] fromstring = ["&", "<", "\"", "'", ">"]
        def String[] tostring = ["&amp;", "&lt;", "&quot;", "&apos;", "&gt;"]
        recipientData.fieldValues.each { it -> it.value.value = StringUtils.replaceEach(it.value.value, fromstring, tostring) }
        return recipientData
    }

    private void saveRecipientData(CommunicationRecipientData recipientData) {
        CommunicationRecipientData data = communicationRecipientDataService.create(recipientData) as CommunicationRecipientData
        data.ownerId = data.lastModifiedBy
        communicationRecipientDataService.update(recipientData)
    }

    private Map calculateFieldsForUser( List<String> fieldNames, Map parameterNameValuesMap, Long pidm, Boolean escapeFieldValue=false  ) {
        def originalMap = null
        try {
            originalMap = asynchronousBannerAuthenticationSpoofer.authenticateAndSetFormContextForExecuteAndSave( )
            return communicationFieldCalculationService.calculateFieldsByPidmWithNewTransaction(
                    (List<String>)  fieldNames,
                    parameterNameValuesMap,
                    (Long) pidm,
                    "",
                    true,
                    escapeFieldValue
            )
        } finally {
            if (originalMap) {
                asynchronousBannerAuthenticationSpoofer.resetAuthAndFormContext( originalMap )
            }
        }
    }

    private CommunicationRecipientData createCommunicationRecipientData(Long pidm, Long organizationId, Long templateId, List<String> fieldNames, Map parameterNameValuesMap ) {
        Map fieldNameValueMap = calculateFieldsForUser( fieldNames, parameterNameValuesMap, pidm )
        return new CommunicationRecipientData(
                pidm: pidm,
                referenceId: UUID.randomUUID().toString(),
                ownerId: -1,
                fieldValues: fieldNameValueMap,
                organizationId: organizationId,
                communicationChannel: channel,
                templateId: templateId
        )
    }

    def checkExternalIdExists (Long pidm) {
        try {
            def externalId = CommunicationMessageGenerator.fetchExternalLoginIdByPidm( pidm )
            if (!externalId)
                throw new RuntimeException("External Id not found.")
        } catch (Throwable e) {
            log.error(e)
            throw CommunicationExceptionFactory.createApplicationException(this.class, new RuntimeException("communication.error.message.emptyExternalId"), CommunicationErrorCode.EMPTY_MOBILE_NOTIFICATION_EXTERNAL_USER.name())
        }
    }


    private CommunicationOrganization fetchOrg (Long organizationId) {
        try {
            return CommunicationOrganization.fetchById(organizationId)
        } catch (Throwable t) {
            log.error(t)
            throw CommunicationExceptionFactory.createApplicationException(this.class, new RuntimeException("communication.error.message.organizationNotFound"), CommunicationErrorCode.ORGANIZATION_NOT_FOUND.name())
        }
    }


    private void checkOrgEmail (CommunicationOrganization org) {
        if (!org)
            throw CommunicationExceptionFactory.createApplicationException(this.class, new RuntimeException("communication.error.message.organizationNotFound"), CommunicationErrorCode.ORGANIZATION_NOT_FOUND.name())
        if (!org.sendEmailServerProperties)
            throw CommunicationExceptionFactory.createApplicationException(this.class, new RuntimeException("communication.error.message.serverPropertiesNotFound"), CommunicationErrorCode.SERVER_PROPERTIES_NOT_FOUND.name())
        if (!org.senderMailboxAccount)
                throw CommunicationExceptionFactory.createApplicationException(this.class, new RuntimeException("communication.error.message.invalidSenderMailbox"), CommunicationErrorCode.INVALID_SENDER_MAILBOX.name())
    }


    private void checkOrgMobile (CommunicationOrganization org) {
        if (!org)
            throw CommunicationExceptionFactory.createApplicationException(this.class, new RuntimeException("communication.error.message.organizationNotFound"), CommunicationErrorCode.ORGANIZATION_NOT_FOUND.name())
        if (org == null)
            throw CommunicationExceptionFactory.createApplicationException(this.class, new RuntimeException("communication.error.message.organizationNotFound"), CommunicationErrorCode.ORGANIZATION_NOT_FOUND.name())
        if (org.mobileEndPointUrl == null || org.mobileEndPointUrl.trim().length() == 0)
            throw CommunicationExceptionFactory.createApplicationException(this.class, new RuntimeException("communication.error.message.mobileEndPointUrlNotFound"), CommunicationErrorCode.EMPTY_MOBILE_NOTIFICATION_ENDPOINT_URL.name())
        if (org.encryptedMobileApplicationKey == null && org.clearMobileApplicationKey == null)
            throw CommunicationExceptionFactory.createApplicationException(this.class, new RuntimeException("communication.error.message.mobileApplicationKeyNotFound"), CommunicationErrorCode.EMPTY_MOBILE_NOTIFICATION_APPLICATION_KEY.name())
        if (org.mobileApplicationName == null)
            throw CommunicationExceptionFactory.createApplicationException(this.class, new RuntimeException("communication.error.message.mobileApplicationNameNotFound"), CommunicationErrorCode.EMPTY_MOBILE_NOTIFICATION_APPLICATION_NAME.name())
    }


    private CommunicationTemplate fetchTemplate (Long templateId) {
        def template
        try {
            template = service.get(templateId)
            if (!template)
                throw CommunicationExceptionFactory.createApplicationException(this.class, new RuntimeException("communication.error.message.templateNotFound"), CommunicationErrorCode.TEMPLATE_NOT_FOUND.name())

            service.validatePublished(template)
            return template
        } catch (ApplicationException e) {
            log.error(e)
            throw e
        } catch (Throwable t) {
            log.error(t)
            throw CommunicationExceptionFactory.createApplicationException(this.class, new RuntimeException("communication.error.message.templateErrorUnknown"), CommunicationErrorCode.TEMPLATE_ERROR_UNKNOWN.name())
        }
    }

    def createLetter(CommunicationOrganization organization, CommunicationLetterMessage message, CommunicationRecipientData recipientData ) {
        if (isEmpty(message?.toAddress)) {
            throw CommunicationExceptionFactory.createFriendlyApplicationException(CommunicationGenerateLetterService.class,
                    CommunicationErrorCode.EMPTY_LETTER_TO_ADDRESS,
                    "emptyLetterToAddress"
            )
        }
        if (isEmpty(message?.content)) {
            throw CommunicationExceptionFactory.createFriendlyApplicationException(CommunicationGenerateLetterService.class,
                    CommunicationErrorCode.EMPTY_LETTER_CONTENT,
                    "emptyLetterContent"
            )
        }

        CommunicationLetterItem item = new CommunicationLetterItem()
        // standard communication log entries
        item.setOrganizationId( organization.id )
        item.setReferenceId( recipientData.getReferenceId() )
        item.setRecipientPidm( recipientData.pidm )
        item.setCreatedBy( recipientData.ownerId )
        item.setSentDate( message.dateSent )
        item.setTemplateId(recipientData.templateId)
        // letter specific fields
        item.toAddress = message.toAddress
        item.content = message.content
        item.style = message.style
        return item
    }

    private boolean isEmpty(String s) {
        return (s == null) || s.trim().length() == 0
    }
}
