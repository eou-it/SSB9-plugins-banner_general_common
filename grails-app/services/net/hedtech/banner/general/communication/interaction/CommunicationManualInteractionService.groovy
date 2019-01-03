/*******************************************************************************
 Copyright 2016-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.interaction

import grails.util.Holders
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.general.communication.exceptions.CommunicationExceptionFactory
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSendService
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import net.hedtech.banner.service.ServiceBase
import org.apache.log4j.Logger
import org.springframework.security.core.context.SecurityContextHolder

class CommunicationManualInteractionService extends ServiceBase {

    private static final log = Logger.getLogger(CommunicationManualInteractionService.class)

    def preCreate(domainModelOrMap) {

        CommunicationManualInteraction manualInteraction = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationManualInteraction
        manualInteraction.organization = (manualInteraction.organization ?: domainModelOrMap.organization)
        manualInteraction.interactionType = (manualInteraction.interactionType ?: domainModelOrMap.interactionType)

        if (manualInteraction.getaSubject() == null || manualInteraction.getaSubject().trim() == "")
            throw new ApplicationException(CommunicationManualInteraction, "@@r1:subjectCannotBeNull@@")

        if (manualInteraction.getOrganization() == null)
            throw new ApplicationException(CommunicationManualInteraction, "@@r1:organizationCannotBeNull@@")
        else
            validateOrganization( manualInteraction.getOrganization().id )

        if (manualInteraction.getInteractionType() == null)
            throw new ApplicationException(CommunicationManualInteraction, "@@r1:interactionTypeCannotBeNull@@")
        else
            validateInteractionType( manualInteraction.getInteractionType().id )

        if (manualInteraction.getConstituentPidm() == null)
            throw new ApplicationException(CommunicationManualInteraction, "@@r1:constituentIdCannotBeNull@@")

        if (manualInteraction.getInteractorPidm() == null)
            throw new ApplicationException(CommunicationManualInteraction, "@@r1:interactorIdCannotBeNull@@")

        if (manualInteraction.getInteractionDate() == null) {
            throw new ApplicationException(CommunicationManualInteraction, "@@r1:interactionDateCannotBeNull@@")
        } else {
            Date now = new Date(System.currentTimeMillis())
            if ((manualInteraction.getInteractionDate()).after(now)) {
                throw CommunicationExceptionFactory.createApplicationException(CommunicationManualInteractionService.class, "invalidInteractionDate")
            }
        }

        def creatorId = CommunicationCommonUtility.getUserOracleUserName();

        manualInteraction.setCreatedBy(creatorId)
        manualInteraction.setCreateDate(new Date())
    }


    def preUpdate(domainModelOrMap) {
        CommunicationManualInteraction manualInteraction = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationManualInteraction
        manualInteraction.organization = (manualInteraction.organization ?: domainModelOrMap.organization)
        manualInteraction.interactionType = (manualInteraction.interactionType ?: domainModelOrMap.interactionType)

        if (manualInteraction.id == null)
            throw new ApplicationException(CommunicationManualInteraction, "@@r1:manualInteractionDoesNotExist@@")

        def oldManualInteraction = CommunicationManualInteraction.get(manualInteraction.id)

        if (oldManualInteraction.id == null)
            throw new ApplicationException(CommunicationManualInteraction, "@@r1:manualInteractionDoesNotExist@@")

        //check if user is authorized. user should be same as created by user of the old record
        if (!CommunicationCommonUtility.getUserOracleUserName().equals(oldManualInteraction.createdBy )) {
            throw new ApplicationException(CommunicationManualInteraction, "@@r1:operation.not.authorized@@")
        }

        if (manualInteraction.getaSubject() == null || manualInteraction.getaSubject().trim() == "")
            throw new ApplicationException(CommunicationManualInteraction, "@@r1:subjectCannotBeNull@@")

        if (manualInteraction.getOrganization() == null)
            throw new ApplicationException(CommunicationManualInteraction, "@@r1:organizationCannotBeNull@@")
        else
            validateOrganization( manualInteraction.getOrganization().id )

        if (manualInteraction.getInteractionType() == null)
            throw new ApplicationException(CommunicationManualInteraction, "@@r1:interactionTypeCannotBeNull@@")
        else
            validateInteractionType( manualInteraction.getInteractionType().id )

        if (manualInteraction.getConstituentPidm() == null)
            throw new ApplicationException(CommunicationManualInteraction, "@@r1:constituentIdCannotBeNull@@")

        if (manualInteraction.getInteractorPidm() == null)
            throw new ApplicationException(CommunicationManualInteraction, "@@r1:interactorIdCannotBeNull@@")

        if (manualInteraction.getInteractionDate() == null) {
            throw new ApplicationException(CommunicationManualInteraction, "@@r1:interactionDateCannotBeNull@@")
        } else {
            Date now = new Date(System.currentTimeMillis())
            if ((manualInteraction.getInteractionDate()).after(now)) {
                throw CommunicationExceptionFactory.createApplicationException(CommunicationManualInteractionService.class, "invalidInteractionDate")
            }
        }
    }

    def preDelete(domainModelOrMap) {
       throw new ApplicationException(CommunicationManualInteraction, "@@r1:unsupported.operation@@")
    }


    void validateOrganization(Long organizationId) {
        def fetchedOrganization = CommunicationOrganization.fetchById(organizationId)
        if (fetchedOrganization == null || fetchedOrganization.id == null) {
            throw new NotFoundException(id: organizationId, entityClassName: domainClass.simpleName)
        }
    }

    void validateInteractionType(Long interactionTypeId) {
        def fetchedInteractionType = CommunicationInteractionType.fetchById(interactionTypeId)
        if (fetchedInteractionType == null || fetchedInteractionType.id == null) {
            throw new NotFoundException(id: interactionTypeId, entityClassName: domainClass.simpleName)
        }
    }
}
