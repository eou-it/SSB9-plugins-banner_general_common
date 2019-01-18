/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.aip

import org.apache.log4j.Logger
import net.hedtech.banner.service.ServiceBase
import net.hedtech.banner.general.overall.IntegrationConfiguration
import org.springframework.dao.InvalidDataAccessResourceUsageException

import java.sql.SQLException

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

    private static final LOGGER = Logger.getLogger(AipNotificationService.class.name)

    /**
     * checks whether user has an active action item
     * @param pidm
     * @return Boolean
     * */
    public Boolean hasActiveActionItems(Integer pidm) {
        try {
            return pidm?UserActiveActionItem.checkIfActionItemPresent(pidm):false //gidm user will not have action items
        } catch (SQLException e) {
            LOGGER.warn("Exception while fetching aip enabled flag from goriccr" + e.getMessage())
            return false
        } catch (InvalidDataAccessResourceUsageException e) {
            LOGGER.warn("view GVQ_GCRACPN does not exist or does not have necessary privileges" + e.getMessage())
            return false
        }
    }

    /**
     * checks whether aip is enabled or not
     * @return String
     * */
    public String getAipEnabledFlag() {
        def aipEnabledStatus
        try {
            aipEnabledStatus = IntegrationConfiguration.fetchByProcessCodeAndSettingName(SQPR_CODE_GENERAL_SSB, ICSN_CODE_ENABLE_ACTION_ITEMS)?.value == YES ? ENABLED : DISABLED
            LOGGER.debug("AIP Enabled status [GENERAL_SSB,ENABLE.ACTION.ITEMS]=" + aipEnabledStatus)
        } catch (SQLException e) {
            aipEnabledStatus = ""
            LOGGER.warn("Exception while fetching aip enabled flag from goriccr" + e.getMessage())
        } catch (InvalidDataAccessResourceUsageException e) {
            aipEnabledStatus = ""
            LOGGER.warn("Exception while fetching aip enabled flag from goriccr" + e.getMessage())
        }

        return aipEnabledStatus
    }


}