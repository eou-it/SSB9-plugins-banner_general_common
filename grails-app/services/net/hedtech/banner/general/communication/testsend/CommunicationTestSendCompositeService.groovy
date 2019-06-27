/********************************************************************************
  Copyright 2017-2018 Ellucian Company L.P. and its affiliates.
********************************************************************************/
package net.hedtech.banner.general.communication.testsend

import groovy.util.logging.Slf4j
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.general.communication.CommunicationErrorCode
import net.hedtech.banner.general.communication.email.CommunicationEmailMessage
import net.hedtech.banner.general.communication.email.CommunicationEmailTemplate
import net.hedtech.banner.general.communication.exceptions.CommunicationExceptionFactory
import net.hedtech.banner.general.communication.field.CommunicationFieldCalculationService
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
import org.springframework.transaction.annotation.Propagation
import grails.gorm.transactions.Transactional

@Slf4j
@Transactional
class CommunicationTestSendCompositeService  {

    //private Log log = LogFactory.getLog( this.getClass() )

    def service
    def communicationTemplateMergeService
    def communicationRecipientDataService
    def asynchronousBannerAuthenticationSpoofer
    def communicationFieldCalculationService
    def communicationSendEmailService
    def communicationSendMobileNotificationService
    def communicationGenerateLetterService
    def communicationInteractionCompositeService

    def recipientData
    def channel

    def sendTest (String bannerId, Long organizationId, Long templateId, Map parameterNameValuesMap) {

        def person = communicationInteractionCompositeService.getPersonOrNonPerson(bannerId)
        if (person.pidm == null || person.pidm <= 0) {
            throw new ApplicationException(CommunicationFieldCalculationService,"@@r1:idInvalid@@")
        }
        switch (channel) {
            case CommunicationChannel.MOBILE_NOTIFICATION:
                sendTestMobileNotification(person.pidm, organizationId, templateId, parameterNameValuesMap)
                break
            case CommunicationChannel.EMAIL:
                sendTestEmail(person.pidm, organizationId, templateId, parameterNameValuesMap)
                break
            case CommunicationChannel.LETTER:
                return sendTestLetter(person.pidm, organizationId, templateId, parameterNameValuesMap)
                break
            default:
                throw CommunicationExceptionFactory.createApplicationException(this.class, new RuntimeException("communication.error.message.templateErrorUnknown"), CommunicationErrorCode.TEMPLATE_ERROR_UNKNOWN.name())
        }
    }

    @Transactional(propagation=Propagation.REQUIRES_NEW, readOnly = false, rollbackFor = Throwable.class )
    public  sendCommunicationWithNewTransaction(recipientDataToBeCreated, template, organization, organizationId, Long pidm) {

        def recipientData = communicationRecipientDataService.create(recipientDataToBeCreated) as CommunicationRecipientData
        CommunicationMessageGenerator messageGenerator = new CommunicationMessageGenerator(
                communicationTemplateMergeService: communicationTemplateMergeService
        )
        CommunicationMessage message = messageGenerator.generate(template, recipientData)
        switch (channel) {
            case CommunicationChannel.MOBILE_NOTIFICATION:
                communicationSendMobileNotificationService.send(organizationId, message as CommunicationMobileNotificationMessage, recipientData)
                break
            case CommunicationChannel.EMAIL:
                communicationSendEmailService.send(organizationId, message as CommunicationEmailMessage, recipientData , pidm)
                break
            case CommunicationChannel.LETTER:
                def letter = createLetter(organization, (CommunicationLetterMessage) message , recipientData)
                communicationGenerateLetterService.testTemplate = true
                letter.id = communicationGenerateLetterService.send(organizationId, message as CommunicationLetterMessage, recipientData )
                return letter
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
            if (!root)
                    throw CommunicationExceptionFactory.createApplicationException(this.class, new RuntimeException("communication.error.message.organizationNotFound"), CommunicationErrorCode.ORGANIZATION_NOT_FOUND.name())
            if (!root.sendEmailServerProperties || !root.sendEmailServerProperties?.id)
                    throw CommunicationExceptionFactory.createApplicationException(this.class, new RuntimeException("communication.error.message.serverPropertiesNotFound"), CommunicationErrorCode.SERVER_PROPERTIES_NOT_FOUND.name())

        }
        CommunicationTemplate template = fetchTemplate(templateId)
        def fieldNames = visitEmail(template as CommunicationEmailTemplate)
        def recipientData = createCommunicationRecipientData( pidm, organizationId,  templateId, fieldNames, parameterNameValuesMap, template.mepCode)

        try {
            sendCommunicationWithNewTransaction(recipientData, template, organization, organizationId, pidm)
        } catch (Throwable t) {
            log.error(t)
            if (t instanceof ApplicationException)
                throw t
            throw CommunicationExceptionFactory.createApplicationException(this.class, new RuntimeException("communication.error.message.testSendTemplate.email"), CommunicationErrorCode.TEMPLATE_ERROR_UNKNOWN.name())
        } finally {
            recipientData = CommunicationRecipientData.fetchByReferenceId(recipientData.referenceId)
            communicationRecipientDataService.delete(recipientData)
        }
    }

    def sendTestMobileNotification (Long pidm, Long organizationId, Long templateId, Map parameterNameValuesMap) {
        def organization = fetchOrg(organizationId)
        try {
            checkOrgMobile(organization)
        } catch (Throwable t) {
            log.error(t)
            def root = CommunicationOrganization.fetchRoot()
            checkOrgMobile(root)
        }

        CommunicationTemplate template = fetchTemplate(templateId)
        checkExternalIdExists(pidm)
        def fieldNames = visitMobileNotification(template as CommunicationMobileNotificationTemplate)
        def recipientData = createCommunicationRecipientData(pidm, organizationId, templateId, fieldNames, parameterNameValuesMap, template.mepCode)

        try {
            sendCommunicationWithNewTransaction(recipientData, template, organization, organizationId, pidm)
        } catch (Throwable t) {
            log.error(t)
            if (t instanceof ApplicationException)
                throw t
            throw CommunicationExceptionFactory.createApplicationException(this.class, new RuntimeException("communication.error.message.testSendTemplate.mobile"), CommunicationErrorCode.TEMPLATE_ERROR_UNKNOWN.name())
        } finally {
            recipientData = CommunicationRecipientData.fetchByReferenceId(recipientData.referenceId)
            communicationRecipientDataService.delete(recipientData)
        }
    }

    def sendTestLetter (Long pidm, Long organizationId, Long templateId, Map parameterNameValuesMap) {
        def organization = fetchOrg(organizationId)
        if (!organization)
            throw CommunicationExceptionFactory.createApplicationException(this.class, new RuntimeException("communication.error.message.organizationNotFound"), CommunicationErrorCode.ORGANIZATION_NOT_FOUND.name())

        CommunicationTemplate template = fetchTemplate(templateId)
        def recipientData = visitLetter(template as CommunicationLetterTemplate, pidm, organizationId, templateId, parameterNameValuesMap)
        try {
            return sendCommunicationWithNewTransaction(recipientData, template, organization, organizationId, pidm)
        } catch (Throwable t) {
            log.error(t)
            if (t instanceof ApplicationException)
                throw t
            throw CommunicationExceptionFactory.createApplicationException(this.class, new RuntimeException("communication.error.message.testSendTemplate.letter"), CommunicationErrorCode.TEMPLATE_ERROR_UNKNOWN.name())
        } finally {
            recipientData = CommunicationRecipientData.fetchByReferenceId(recipientData.referenceId)
            communicationRecipientDataService.delete(recipientData)
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
        def recipientData = createCommunicationRecipientData(pidm, organizationId, templateId, fieldNames, parameterNameValuesMap, template.mepCode, true)
        return recipientData
    }

    private Map calculateFieldsForUser( List<String> fieldNames, Map parameterNameValuesMap, Long pidm, String mepCode=null, Boolean escapeFieldValue=false  ) {
        def originalMap = null
        try {
            originalMap = asynchronousBannerAuthenticationSpoofer.authenticateAndSetFormContextForExecuteAndSave(CommunicationCommonUtility.userOracleUserName, mepCode)
            return communicationFieldCalculationService.calculateFieldsByPidmWithNewTransaction(
                    (List<String>)  fieldNames,
                    parameterNameValuesMap,
                    (Long) pidm,
                    mepCode,
                    true,
                    escapeFieldValue
            )
        } finally {
            if (originalMap) {
                asynchronousBannerAuthenticationSpoofer.resetAuthAndFormContext( originalMap )
            }
        }
    }

    private CommunicationRecipientData createCommunicationRecipientData(Long pidm, Long organizationId, Long templateId, List<String> fieldNames, Map parameterNameValuesMap, String mepCode=null, Boolean escapeFieldValue=false ) {
        Map fieldNameValueMap = calculateFieldsForUser( fieldNames, parameterNameValuesMap, pidm, mepCode, escapeFieldValue )
        return new CommunicationRecipientData(
                pidm: pidm,
                referenceId: UUID.randomUUID().toString(),
                ownerId: CommunicationCommonUtility.userOracleUserName,
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
        if (org.encryptedMobileApplicationKey == null)
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
