/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.recurrence

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.general.communication.template.CommunicationTemplate
import net.hedtech.banner.service.ServiceBase
import org.springframework.security.core.context.SecurityContextHolder

class CommunicationRecurrentMessageService extends ServiceBase {

    def preCreate( domainModelOrMap ) {
        CommunicationRecurrentMessage recurrentMessage = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationRecurrentMessage
        if (recurrentMessage.getCreatedBy() == null) {
            recurrentMessage.setCreatedBy( SecurityContextHolder?.context?.authentication?.principal?.getOracleUserName() )
        }
        if (recurrentMessage.getCreationDateTime() == null) {
            recurrentMessage.setCreationDateTime( new Date() )
        }
        if (recurrentMessage.getName() == null) {
            recurrentMessage.setName(CommunicationTemplate.get(recurrentMessage.templateId).getName())
        }
        recurrentMessage.setDeleted( false );
    }

    def preUpdate( domainModelOrMap ) {
        CommunicationRecurrentMessage recurrentMessage = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationRecurrentMessage

        if (recurrentMessage.id == null)
            throw new ApplicationException(CommunicationRecurrentMessage, "@@r1:recurrentMessageDoesNotExist@@")

        def oldRecurrentMessage = CommunicationRecurrentMessage.get(recurrentMessage.id)

        if (oldRecurrentMessage.id == null)
            throw new ApplicationException(CommunicationRecurrentMessage, "@@r1:recurrentMessageDoesNotExist@@")

        //check if user is authorized. user should be admin or author
        if (!CommunicationCommonUtility.userCanUpdateDeletePopulation(oldRecurrentMessage.createdBy)) {
            throw new ApplicationException(CommunicationRecurrentMessage, "@@r1:operation.not.authorized@@")
        }

        if (recurrentMessage.name == null || recurrentMessage.name == "") {
            throw new ApplicationException(CommunicationRecurrentMessage, "@@r1:nameCannotBeNull@@")
        }

        recurrentMessage.setDeleted( false );
    }
}
