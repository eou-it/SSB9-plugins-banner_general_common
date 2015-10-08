/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.overall.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.overall.IntegrationConfiguration
import net.hedtech.banner.general.overall.ldm.v4.RoomType
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
/**
 * Service for room-types
 */
@Transactional
class RoomTypeCompositeService extends LdmService{

    private static final String ROOM_TYPES_HEDM_NAME='room-types'
    private static final String SETTING_ROOM_LAYOUT = 'ROOM.ROOMTYPE'
    private static final String PROCESS_CODE='HEDM'

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    List<RoomType> list(Map params) {
        List<GlobalUniqueIdentifier> globalUniqueIdentifierDetails = getRoomTypeDetails()
        List<RoomType> roomTypes =[]
        globalUniqueIdentifierDetails.each {
            globalUniqueIdentifierDetail->
            IntegrationConfiguration integrationConfiguration = findAllByProcessCodeAndSettingNameAndValue(PROCESS_CODE,SETTING_ROOM_LAYOUT,globalUniqueIdentifierDetail?.domainKey)
            roomTypes << new RoomType(globalUniqueIdentifierDetail?.guid,integrationConfiguration?.translationValue,integrationConfiguration?.translationValue)
        }
        return roomTypes
    }


    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    Long count() {
        return GlobalUniqueIdentifier.countByLdmName(ROOM_TYPES_HEDM_NAME)
    }


    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    def get(String guid) {
        GlobalUniqueIdentifier globalUniqueIdentifier = GlobalUniqueIdentifier.findByGuid(guid)
        if(!globalUniqueIdentifier){
            throw new ApplicationException("roomtype", new NotFoundException())
        }
        IntegrationConfiguration integrationConfiguration = findAllByProcessCodeAndSettingNameAndValue(PROCESS_CODE,SETTING_ROOM_LAYOUT,globalUniqueIdentifier?.domainKey)
        return new RoomType(globalUniqueIdentifier?.guid,integrationConfiguration?.translationValue,integrationConfiguration?.translationValue)
    }


    /**
     * Room Types Details
     *
     * @return
     */
    List<GlobalUniqueIdentifier> getRoomTypeDetails() {
        return GlobalUniqueIdentifier.findAllByLdmName(ROOM_TYPES_HEDM_NAME)
    }
}
