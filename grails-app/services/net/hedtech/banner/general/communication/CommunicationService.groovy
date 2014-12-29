/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.field.CommunicationField
import net.hedtech.banner.general.communication.field.CommunicationFieldCalculationService
import net.hedtech.banner.general.communication.merge.CommunicationFieldValue
import net.hedtech.banner.general.communication.merge.CommunicationRecipientData
import net.hedtech.banner.general.communication.merge.CommunicationRecipientDataService
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import net.hedtech.banner.general.communication.organization.CommunicationOrganizationService
import net.hedtech.banner.general.communication.template.CommunicationEmailTemplate
import net.hedtech.banner.general.communication.template.CommunicationTemplate
import net.hedtech.banner.general.communication.template.CommunicationTemplateMergeService
import net.hedtech.banner.general.communication.template.CommunicationTemplateService
import net.hedtech.banner.general.security.TrustedBannerAuthenticationProvider
import net.hedtech.banner.general.security.TrustedBannerToken
import net.hedtech.banner.security.FormContext
import org.apache.log4j.Logger
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

/**
 * Communication service is the main interface for sending a single template based communication
 * to a single recipient from either a group send, campaign, workflow, api, etc.
 */
@Transactional(readOnly = false, propagation = Propagation.REQUIRED )
class CommunicationService {

    def log = Logger.getLogger( this.getClass() )
    CommunicationRecipientDataService communicationRecipientDataService
    CommunicationOrganizationService communicationOrganizationService
    CommunicationTemplateMergeService communicationTemplateMergeService
    CommunicationFieldCalculationService communicationFieldCalculationService
    CommunicationTemplateService communicationTemplateService
    TrustedBannerAuthenticationProvider trustedBannerAuthenticationProvider


    public CommunicationReceipt sendCommunication( CommunicationRequest request ) {
        if (log.isDebugEnabled()) log.debug( "Processing request for recipient pidm = ${request.recipientPidm}." )
        // Illegal argument exceptions are programmer errors in not assigning values that are required for this method interface.
        if (request == null) throw new IllegalArgumentException( "request may not be null" )
        if (request.initiatorUserId == null) throw new IllegalArgumentException( "request.initiatorUserId may not be null" )
        if (request.organizationId == null) throw new IllegalArgumentException( "request.organizationId may not be null" )
        if (request.recipientPidm == null) throw new IllegalArgumentException( "request.recipientPidm may not be null" )
        if (request.templateId == null) throw new IllegalArgumentException( "request.templateId may not be null" )

        String referenceId = request.getReferenceId()
        if (null == referenceId) {
            referenceId = UUID.randomUUID().toString()
        }

        CommunicationTemplate template = communicationTemplateService.get( request.templateId )
        if (template == null) throw new ApplicationException(CommunicationService.class, "@@r1:net.hedtech.banner.general.communication.CommunicationService.templateNotFound@@")
        if (!template.published) throw new ApplicationException(CommunicationService.class, "@@r1:net.hedtech.banner.general.communication.CommunicationService.templateNotPublished@@")
        if (!template.active) throw new ApplicationException(CommunicationService.class, "@@r1:net.hedtech.banner.general.communication.CommunicationService.templateNotActive@@")
        if (template.personal && !template.getCreatedBy().equals( request.initiatorUserId )) throw new ApplicationException(CommunicationService.class, "@@r1:net.hedtech.banner.general.communication.CommunicationService.personalTemplateMustBeSentBySameCreatorId@@")

        CommunicationOrganization organization = communicationTemplateService.get( request.organizationId )
        if (organization == null) throw new ApplicationException(CommunicationService.class, "@@r1:net.hedtech.banner.general.communication.CommunicationService.organizationNotFound@@")

        CommunicationEmailTemplate emailTemplate = template as CommunicationEmailTemplate
        List fieldNames = communicationTemplateMergeService.extractTemplateVariables( emailTemplate )

        CommunicationRecipientData recipientData = new CommunicationRecipientData()
        recipientData.referenceId = referenceId
        recipientData.pidm = request.recipientPidm
        recipientData.templateId = request.templateId
        recipientData.organization = organization
        recipientData.ownerId = request.initiatorUserId
        recipientData.fieldValues = calculateFieldValuesMap(fieldNames, request)
        recipientData = communicationRecipientDataService.create( recipientData ) as CommunicationRecipientData

        // Create communication job here!

        CommunicationReceipt communicationReceipt = new CommunicationReceipt()
        communicationReceipt.recipientData = recipientData

        return communicationReceipt
    }

    private Map calculateFieldValuesMap(List<String> fieldNames, CommunicationRequest request) {
        Authentication originalAuthentication = SecurityContextHolder.getContext().getAuthentication()
        try {
            FormContext.set( ['SELFSERVICE'] )
            Authentication initiatorAuthentication = trustedBannerAuthenticationProvider.authenticate( new TrustedBannerToken( request.initiatorUserId ) )
            SecurityContextHolder.getContext().setAuthentication( initiatorAuthentication )

            if (log.isDebugEnabled()) log.debug( "Authenticated as ${initiatorAuthentication} for calculating field values." )

            Map fieldToValueMap = [:]
            for (String fieldName : fieldNames) {
                CommunicationField field = CommunicationField.fetchByName(fieldName)
                String value = communicationFieldCalculationService.calculateFieldByPidm(fieldName, request.recipientPidm)
                fieldToValueMap.put(field, new CommunicationFieldValue(value: value, renderAsHtml: field.renderAsHtml))
            }

            return fieldToValueMap
        } finally {
            SecurityContextHolder.getContext().setAuthentication( originalAuthentication )
        }
    }

}