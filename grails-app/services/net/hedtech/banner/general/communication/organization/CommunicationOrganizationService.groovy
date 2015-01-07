/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.organization

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.service.ServiceBase
import org.apache.log4j.Logger

import java.text.SimpleDateFormat

/**
 * Service for providing basic crud services on
 * Folder domain objects.
 */
class CommunicationOrganizationService extends ServiceBase {
    def log = Logger.getLogger(this.getClass())


    def preCreate(domainModelOrMap) {

        CommunicationOrganization commorgn = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationOrganization

        if (commorgn.dateFormat != null) {
            try {
                def datestring = new SimpleDateFormat(commorgn.dateFormat).format(new Date())
            } catch (Exception e) {
                throw new ApplicationException(CommunicationOrganization, "@@r1:invalid.date.format@@")
            }
        }
        if (commorgn.dayOfWeekFormat != null) {
            try {
                def datestring = new SimpleDateFormat(commorgn.dayOfWeekFormat).format(new Date())
            } catch (Exception e) {
                throw new ApplicationException(CommunicationOrganization, "@@r1:invalid.dayofweek.format@@")
            }
        }
        if (commorgn.timeOfDayFormat != null) {
            try {
                def datestring = new SimpleDateFormat(commorgn.timeOfDayFormat).format(new Date())
            } catch (Exception e) {
                throw new ApplicationException(CommunicationOrganization, "@@r1:invalid.timeofday.format@@")
            }
        }

    }


    def preUpdate(domainModelOrMap) {

        CommunicationOrganization commorgn = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationOrganization

        if (commorgn.dateFormat != null) {
            try {
                def datestring = new SimpleDateFormat(commorgn.dateFormat).format(new Date())
            } catch (Exception e) {
                throw new ApplicationException(CommunicationOrganization, "@@r1:invalid.date.format@@")
            }
        }
        if (commorgn.dayOfWeekFormat != null) {
            try {
                def datestring = new SimpleDateFormat(commorgn.dayOfWeekFormat).format(new Date())
            } catch (Exception e) {
                throw new ApplicationException(CommunicationOrganization, "@@r1:invalid.dayofweek.format@@")
            }
        }
        if (commorgn.timeOfDayFormat != null) {
            try {
                def datestring = new SimpleDateFormat(commorgn.timeOfDayFormat).format(new Date())
            } catch (Exception e) {
                throw new ApplicationException(CommunicationOrganization, "@@r1:invalid.timeofday.format@@")
            }
        }

    }

}
