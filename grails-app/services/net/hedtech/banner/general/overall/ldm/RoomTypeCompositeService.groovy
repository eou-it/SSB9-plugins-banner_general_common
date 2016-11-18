/*******************************************************************************
 Copyright 2015-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.overall.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.common.GeneralCommonConstants
import net.hedtech.banner.general.common.GeneralValidationCommonConstants
import net.hedtech.banner.general.overall.IntegrationConfiguration
import net.hedtech.banner.general.overall.ldm.v4.RoomType
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
/**
 * Service for room-types
 */
@Transactional
class RoomTypeCompositeService extends LdmService{

    private static final List<String> VERSIONS = [GeneralValidationCommonConstants.VERSION_V4]


    private static final String ROOMTYPE = "roomtype"

    def globalUniqueIdentifierService

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    List<RoomType> list(Map params) {
        String acceptVersion = getAcceptVersion(VERSIONS)
        List<GlobalUniqueIdentifier> globalUniqueIdentifierDetails = getRoomTypeDetails()
        List<RoomType> roomTypes =[]
        globalUniqueIdentifierDetails.each {
            globalUniqueIdentifierDetail->
                IntegrationConfiguration integrationConfiguration = findAllByProcessCodeAndSettingNameAndValue(GeneralCommonConstants.PROCESS_CODE,GeneralCommonConstants.SETTING_ROOM_LAYOUT,globalUniqueIdentifierDetail?.domainKey)
                if(!integrationConfiguration){
                    throw new ApplicationException(ROOMTYPE, new NotFoundException())
                }
                roomTypes << new RoomType(globalUniqueIdentifierDetail?.guid,integrationConfiguration?.translationValue,integrationConfiguration?.translationValue)
        }
        return roomTypes
    }


    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    Long count() {
        return globalUniqueIdentifierService.fetchCountByLdmName(GeneralCommonConstants.ROOM_TYPES_HEDM_NAME)
    }


    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    def get(String guid) {
        String acceptVersion = getAcceptVersion(VERSIONS)
        GlobalUniqueIdentifier globalUniqueIdentifier = globalUniqueIdentifierService.fetchByLdmNameAndGuid(GeneralCommonConstants.ROOM_TYPES_HEDM_NAME,guid)
        if(!globalUniqueIdentifier){
            throw new ApplicationException(ROOMTYPE, new NotFoundException())
        }
        IntegrationConfiguration integrationConfiguration = findAllByProcessCodeAndSettingNameAndValue(GeneralCommonConstants.PROCESS_CODE,GeneralCommonConstants.SETTING_ROOM_LAYOUT,globalUniqueIdentifier?.domainKey)

        if(!integrationConfiguration){
            throw new ApplicationException(ROOMTYPE, new NotFoundException())
        }

        return new RoomType(globalUniqueIdentifier?.guid,integrationConfiguration?.translationValue,integrationConfiguration?.translationValue)
    }


    /**
     * Room Types Details
     *
     * @return
     */
    List<GlobalUniqueIdentifier> getRoomTypeDetails() {
        return globalUniqueIdentifierService.fetchByLdmName(GeneralCommonConstants.ROOM_TYPES_HEDM_NAME)
    }
}
