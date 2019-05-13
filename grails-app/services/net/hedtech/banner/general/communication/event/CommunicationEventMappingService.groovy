/*********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.communication.event

import grails.gorm.transactions.Transactional
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.service.ServiceBase

/**
 * Service for providing basic crud services on
 * Folder domain objects.
 */
@Transactional
class CommunicationEventMappingService extends ServiceBase {

    def preCreate( domainModelOrMap ) {
        if (!CommunicationCommonUtility.userCanAuthorContent()) {
            throw new ApplicationException(CommunicationEventMapping, "@@r1:operation.not.authorized@@")
        }

        CommunicationEventMapping eventMapping = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationEventMapping

        if (eventMapping.getEventName() == null || eventMapping.getEventName() == "")
            throw new ApplicationException( CommunicationEventMapping, "@@r1:nameCannotBeNull@@" )

        if (CommunicationEventMapping.fetchByName( eventMapping.eventName )) {
            throw new ApplicationException( CommunicationEventMapping, "@@r1:eventMappingExists:"+eventMapping.eventName + "@@")
        }
    }


    def preUpdate( domainModelOrMap ) {

        CommunicationEventMapping eventMapping = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationEventMapping

        if (eventMapping.id == null)
            throw new ApplicationException(CommunicationEventMapping, "@@r1:eventMappingDoesNotExist@@")

        def oldEventMapping = CommunicationEventMapping.get(eventMapping?.id)

        if (oldEventMapping.id == null)
            throw new ApplicationException(CommunicationEventMapping, "@@r1:eventMappingDoesNotExist@@")

        //check if user is authorized. user should be admin or author
        if (!CommunicationCommonUtility.userCanUpdateDeleteContent(oldEventMapping.lastModifiedBy)) {
            throw new ApplicationException(CommunicationEventMapping, "@@r1:operation.not.authorized@@")
        }

        if (eventMapping.getEventName() == null || eventMapping.getEventName() == "")
            throw new ApplicationException( CommunicationEventMapping, "@@r1:nameCannotBeNull@@" )

        if (CommunicationEventMapping.existsAnotherSameNameEvent( eventMapping.id, eventMapping.eventName ))
            throw new ApplicationException( CommunicationEventMapping, "@@r1:eventMappingExists:" + eventMapping.eventName  + "@@")
    }

    def preDelete(domainModelOrMap) {

        if ((domainModelOrMap.id == null) && (domainModelOrMap?.domainModel.id == null))
            throw new ApplicationException(CommunicationEventMapping, "@@r1:eventMappingDoesNotExist@@")

        def oldEventMapping = CommunicationEventMapping.get(domainModelOrMap.id ?: domainModelOrMap?.domainModel.id)

        if (oldEventMapping.id == null)
            throw new ApplicationException(CommunicationEventMapping, "@@r1:eventMappingDoesNotExist@@")

        if (!CommunicationCommonUtility.userCanUpdateDeleteContent(oldEventMapping.lastModifiedBy)) {
            throw new ApplicationException(CommunicationEventMapping, "@@r1:operation.not.authorized@@")
        }
    }

}
