/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.aip

import org.apache.log4j.Logger
import net.hedtech.banner.service.ServiceBase
import net.hedtech.banner.general.overall.IntegrationConfiguration
import static net.hedtech.banner.general.aip.AipNotificationConstants.YES
import static net.hedtech.banner.general.aip.AipNotificationConstants.ENABLED
import static net.hedtech.banner.general.aip.AipNotificationConstants.DISABLED
import static net.hedtech.banner.general.aip.AipNotificationConstants.SQPR_CODE_GENERAL_SSB
import static net.hedtech.banner.general.aip.AipNotificationConstants.ICSN_CODE_ENABLE_ACTION_ITEMS


/**
 * Service Class for Aip Notification which holds methods
 * methods for checking action items and gorricr flag
 */
public class AipNotificationService extends ServiceBase {
    static transactional = true
    private static final LOGGER = Logger.getLogger(AipNotificationService.class.name)

    /**
     * checks whether user has an active action item
     * @param pidm
     * @return Boolean
     * */
    public Boolean hasActiveActionItems(Integer pidm) {
        println " hasActiveActionItems function ------"
        return UserActiveActionItem.checkIfActionItemPresent(pidm);
    }

    /**
     * checks whether aip is enabled or not
     * @return String
     * */
    public String getGoriicrFlag() {
        def aipEnabledStatus
        try{
            aipEnabledStatus = IntegrationConfiguration.fetchByProcessCodeAndSettingName(SQPR_CODE_GENERAL_SSB, ICSN_CODE_ENABLE_ACTION_ITEMS)?.value == YES ? ENABLED : DISABLED
            LOGGER.debug("AIP Enabled status [GENERAL_SSB,ENABLE.ACTION.ITEMS]=" + aipEnabledStatus);
        }catch (Exception e){
            aipEnabledStatus = ""
        }

        return aipEnabledStatus;
    }


}