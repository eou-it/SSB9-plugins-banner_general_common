package net.hedtech.banner.general.communication.testsend

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.CommunicationErrorCode
import net.hedtech.banner.general.communication.email.CommunicationEmailAddress
import net.hedtech.banner.general.communication.email.CommunicationEmailMessage
import net.hedtech.banner.general.communication.email.CommunicationEmailTemplate
import net.hedtech.banner.general.communication.email.CommunicationMergedEmailTemplate
import net.hedtech.banner.general.communication.email.CommunicationSendEmailMethod
import net.hedtech.banner.general.communication.email.CommunicationSendEmailService
import net.hedtech.banner.general.communication.exceptions.CommunicationExceptionFactory
import net.hedtech.banner.general.communication.item.CommunicationChannel
import net.hedtech.banner.general.communication.job.CommunicationJob
import net.hedtech.banner.general.communication.job.CommunicationMessageGenerator
import net.hedtech.banner.general.communication.letter.CommunicationLetterItem
import net.hedtech.banner.general.communication.letter.CommunicationLetterMessage
import net.hedtech.banner.general.communication.letter.CommunicationLetterTemplate
import net.hedtech.banner.general.communication.letter.CommunicationMergedLetterTemplate
import net.hedtech.banner.general.communication.merge.CommunicationRecipientData
import net.hedtech.banner.general.communication.merge.CommunicationRecipientDataFactory
import net.hedtech.banner.general.communication.mobile.CommunicationMobileNotificationMessage
import net.hedtech.banner.general.communication.mobile.CommunicationMobileNotificationTemplate
import net.hedtech.banner.general.communication.organization.CommunicationMailboxAccountService
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import net.hedtech.banner.general.communication.template.CommunicationTemplate
import net.hedtech.banner.general.communication.template.CommunicationTemplateMergeService
import net.hedtech.banner.general.communication.template.CommunicationTemplateService
import org.apache.commons.lang.StringUtils

import javax.mail.internet.AddressException
import javax.mail.internet.InternetAddress

class CommunicationTestSendCompositeService  {

    CommunicationTemplateService service
    def communicationTemplateMergeService
    def communicationJobService
    def communicationMailboxAccountService
    def communicationRecipientDataService
    def asynchronousBannerAuthenticationSpoofer
    def communicationFieldCalculationService
    def communicationSendEmailService
    CommunicationRecipientDataFactory communicationRecipientDataFactory

    def recipientData
    def channel

    def sendTest (Long pidm, Long organizationId, Long templateId) {
        switch (channel) {
            case CommunicationChannel.MOBILE_NOTIFICATION:
                sendTestMobileNotification(pidm, organizationId, templateId)
                break
            case CommunicationChannel.EMAIL:
                sendTestEmail(pidm, organizationId, templateId)
                break
            case CommunicationChannel.LETTER:
                return sendTestLetter(pidm, organizationId, templateId)
                break
            default:
                throw CommunicationExceptionFactory.createApplicationException(this.class, new RuntimeException("communication.error.message.templateErrorUnknown"), CommunicationErrorCode.TEMPLATE_ERROR_UNKNOWN.name())
        }
    }

//    def sendTestEmail (Long pidm, Long organizationId, Long templateId) {
//        def organization = fetchOrg(organizationId)
//        try {
//            checkOrgEmail(organization)
//        } catch (Throwable t) {
//            log.error(t)
//            def root = CommunicationOrganization.fetchRoot()
//            if (root) {
//                if (!organization.sendEmailServerProperties)
//                    organization.sendEmailServerProperties = root.sendEmailServerProperties
//                if (!organization.senderMailboxAccount)
//                    organization.senderMailboxAccount = root.senderMailboxAccount
//            }
//        }
//        checkOrgEmail(organization)
//        CommunicationTemplate template = fetchTemplate(templateId)
//        def fieldNames = visitEmail(template as CommunicationEmailTemplate)
//        def recipientData = createCommunicationRecipientData( pidm, organizationId,  templateId, fieldNames)
//        createCommunicationJob(recipientData)
//    }

    def sendTestEmail (Long pidm, Long organizationId, Long templateId) {
        def organization = fetchOrg(organizationId)
        try {
            checkOrgEmail(organization)
        } catch (Throwable t) {
            log.error(t)
            def root = CommunicationOrganization.fetchRoot()
            if (root) {
                if (!organization.sendEmailServerProperties)
                    organization.sendEmailServerProperties = root.sendEmailServerProperties
                if (!organization.senderMailboxAccount)
                    organization.senderMailboxAccount = root.senderMailboxAccount
            }
        }
        checkOrgEmail(organization)
        CommunicationTemplate template = fetchTemplate(templateId)
        def fieldNames = visitEmail(template as CommunicationEmailTemplate)
        def recipientData = createCommunicationRecipientData( pidm, organizationId,  templateId, fieldNames)
        sendTestEmail ( recipientData,  organization,  template)
    }

    def sendTestMobileNotification (Long pidm, Long organizationId, Long templateId) {
        def organization = fetchOrg(organizationId)
        try {
            checkOrgMobile(organization)
        } catch (Throwable t) {
            log.error(t)
            def root = CommunicationOrganization.fetchRoot()
            if (root) {
                if (!organization.mobileApplicationName)
                    organization.mobileApplicationName = root.mobileApplicationName
                if (!organization.mobileEndPointUrl)
                    organization.mobileEndPointUrl = root.mobileEndPointUrl
                if (!organization.encryptedMobileApplicationKey)
                    organization.encryptedMobileApplicationKey = root.encryptedMobileApplicationKey
                if (!organization.clearMobileApplicationKey)
                    organization.clearMobileApplicationKey = root.clearMobileApplicationKey
            }
        }
        checkOrgMobile(organization)
        CommunicationTemplate template = fetchTemplate(templateId)
        def fieldNames = visitMobileNotification(template as CommunicationMobileNotificationTemplate)
        def recipientData = createCommunicationRecipientData( pidm, organizationId,  templateId, fieldNames)
        checkExternalIdExists(pidm)
        createCommunicationJob(recipientData)
    }

    def sendTestLetter (Long pidm, Long organizationId, Long templateId) {
        def organization = fetchOrg(organizationId)
        if (!organization)
            throw CommunicationExceptionFactory.createApplicationException(this.class, new RuntimeException("communication.error.message.organizationNotFound"), CommunicationErrorCode.ORGANIZATION_NOT_FOUND.name())

        CommunicationTemplate template = fetchTemplate(templateId)
        def recipientData = visitLetter(template as CommunicationLetterTemplate, pidm, organizationId, templateId)

        CommunicationMergedLetterTemplate mergedLetterTemplate = communicationTemplateMergeService.mergeLetterTemplate(template as CommunicationLetterTemplate, recipientData )
        CommunicationLetterMessage message = new CommunicationLetterMessage()
        message.toAddress = mergedLetterTemplate.toAddress
        message.content = mergedLetterTemplate.content
        message.style = mergedLetterTemplate.style

        def letter = createLetter(organization, message , recipientData)
        return letter
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

    def visitLetter (CommunicationLetterTemplate template, Long pidm, Long organizationId, Long templateId) {
        // Can this list be cached somewhere for similar processing on the same template but different user
        List<String> fieldNames = communicationTemplateMergeService.extractTemplateVariables(template.toAddress?.toString())
        communicationTemplateMergeService.extractTemplateVariables(template.content?.toString()).each {
            fieldNames << it
        }
        fieldNames = fieldNames.unique()
        def recipientData = createCommunicationRecipientData(pidm, organizationId, templateId, fieldNames)
        //escape xml 5 special characters in the field values to enable pdf generation for letters
        def String[] fromstring = ["&", "<", "\"", "'", ">"]
        def String[] tostring = ["&amp;", "&lt;", "&quot;", "&apos;", "&gt;"]
        recipientData.fieldValues.each { it -> it.value.value = StringUtils.replaceEach(it.value.value, fromstring, tostring) }
        return recipientData
    }

    private void createCommunicationJob(CommunicationRecipientData recipientData) {
        communicationRecipientDataService.create(recipientData)
        CommunicationJob communicationJob
        try {
            communicationJob = new CommunicationJob(referenceId: recipientData.referenceId)
            communicationJob = communicationJobService.create(communicationJob)
//            trackEmail(pidm, organization, recipientData, template)
        } catch (ApplicationException e) {
            log.error(e)
            throw e
        } catch (Throwable t) {
            log.error(t)
            /* throw unknown error */
            throw CommunicationExceptionFactory.createApplicationException(this.class, new RuntimeException("communication.error.message.templateErrorUnknown"), CommunicationErrorCode.TEMPLATE_ERROR_UNKNOWN.name())
        }
    }


    private Map calculateFieldsForUser( List<String> fieldNames, Long pidm ) {
        def originalMap = null
        try {
            originalMap = asynchronousBannerAuthenticationSpoofer.authenticateAndSetFormContextForExecuteAndSave( )
            return communicationFieldCalculationService.calculateFieldsByPidmWithNewTransaction(
                    (List<String>)  fieldNames,
                    (Map) [:],
                    (Long) pidm,
                    ""
            )
        } finally {
            if (originalMap) {
                asynchronousBannerAuthenticationSpoofer.resetAuthAndFormContext( originalMap )
            }
        }
    }

    private CommunicationRecipientData createCommunicationRecipientData(Long pidm, Long organizationId, Long templateId, List<String> fieldNames ) {
        Map fieldNameValueMap = calculateFieldsForUser( fieldNames, pidm )
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

    private static boolean isEmpty(String s) {
        return ((!s) || (s == null) || (s.length() == 0) || (s == ""))
    }

    def createLetter(CommunicationOrganization organization, CommunicationLetterMessage message, CommunicationRecipientData recipientData ) {

        if (isEmpty(message.toAddress)) {
            throw CommunicationExceptionFactory.createFriendlyApplicationException(CommunicationGenerateLetterService.class,
                    CommunicationErrorCode.EMPTY_LETTER_TO_ADDRESS.toString(),
                    "emptyLetterToAddress"
            )
        }

        if (isEmpty(message.content)) {
            throw CommunicationExceptionFactory.createFriendlyApplicationException(CommunicationGenerateLetterService.class,
                    CommunicationErrorCode.EMPTY_LETTER_CONTENT.toString(),
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


    def sendTestEmail (CommunicationRecipientData recipientData, CommunicationOrganization organization, CommunicationTemplate template) {
        CommunicationMergedEmailTemplate mergedTemplate = communicationTemplateMergeService.mergeEmailTemplate(template, recipientData)
        CommunicationEmailMessage emailMessage = new CommunicationEmailMessage()
        emailMessage.setSubjectLine(mergedTemplate.subject)
        emailMessage.setMessageBody(mergedTemplate.content)
        emailMessage.setMessageBodyContentType("text/html; charset=UTF-8")
//        def toList = []
//        for (def i = 0; i < mergedTemplate.toList.length(); i++) {
//            def toAddress = new CommunicationEmailAddress(
//                    mailAddress: mergedTemplate.toList[i],
//                    displayName: mergedTemplate.toList[i]
//            )
//            if (toAddress)
//                toList.push(toAddress)
//        }
//        emailMessage.setToList(toList.toSet())

        if (mergedTemplate.toList && mergedTemplate.toList.trim().length() > 0) {
            emailMessage.setToList( createAddresses( mergedTemplate.toList.trim(), ";" ) )
        }

        if (organization?.senderMailboxAccount?.encryptedPassword != null)
            organization.senderMailboxAccount.clearTextPassword = communicationMailboxAccountService.decryptPassword( organization.senderMailboxAccount.encryptedPassword )

        CommunicationSendEmailMethod sendEmailMethod = new CommunicationSendEmailMethod(emailMessage, organization)

        try {
            sendEmailMethod.execute()
        } catch (ApplicationException e) {
            log.error(e)
            throw e
        }
    }

    private HashSet<CommunicationEmailAddress> createAddresses( String listOfEmails, String separator ) throws AddressException {
        HashSet<CommunicationEmailAddress> emailAddresses = new HashSet<CommunicationEmailAddress>();
        String[] tempEA = listOfEmails.trim().split( separator );
        CommunicationEmailAddress emailAddress;

        for (String string : tempEA) {

            emailAddress = new CommunicationEmailAddress();

            try {
                // Try to parse the personal display name out of the address first.
                InternetAddress[] internetAddresses = InternetAddress.parse( string.trim(), true );
                if (internetAddresses.length == 1) {
                    InternetAddress internetAddress = internetAddresses[0];
                    emailAddress.setMailAddress( internetAddress.getAddress() );
                    emailAddress.setDisplayName( internetAddress.getPersonal() );
                } else {
                    if (log.isDebugEnabled()) log.debug( "Did not find exactly one internet address parsing: " + string );
                    emailAddress.setMailAddress( string.trim() );
                }
            } catch (AddressException e) {
                if (log.isDebugEnabled()) log.debug( "AddressException attempting to parse: " + string );
                emailAddress.setMailAddress( string.trim() );
            }

            emailAddresses.add( emailAddress );
        }

        return emailAddresses;
    }
}
