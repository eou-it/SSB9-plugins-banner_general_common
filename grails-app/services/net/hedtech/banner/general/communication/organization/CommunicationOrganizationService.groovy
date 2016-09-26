/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.organization

import grails.util.Holders
import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
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
        CommunicationOrganization communicationOrganization = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationOrganization

        def rootOrg = CommunicationOrganization.fetchRoot()
        if (rootOrg == null)
            communicationOrganization.parent = null
        else if (communicationOrganization.parent == null)
            communicationOrganization.parent = rootOrg.id

        communicationOrganization.isAvailable = communicationOrganization.isAvailable ?: false;

        // Generate encrypted password if necessary
        if (communicationOrganization.clearMobileApplicationKey) {
            communicationOrganization.encryptedMobileApplicationKey = encryptPassword(communicationOrganization.clearMobileApplicationKey)
        }

        if (communicationOrganization.dateFormat != null) {
            try {
                def datestring = validateDateFormat(communicationOrganization.dateFormat)
            } catch (Exception e) {
                throw new ApplicationException(CommunicationOrganization, "@@r1:invalid.date.format@@")
            }
        }
        if (communicationOrganization.dayOfWeekFormat != null) {
            try {
                def datestring = validateDateFormat(communicationOrganization.dayOfWeekFormat)
            } catch (Exception e) {
                throw new ApplicationException(CommunicationOrganization, "@@r1:invalid.dayofweek.format@@")
            }
        }
        if (communicationOrganization.timeOfDayFormat != null) {
            try {
                def datestring = validateDateFormat(communicationOrganization.timeOfDayFormat)
            } catch (Exception e) {
                throw new ApplicationException(CommunicationOrganization, "@@r1:invalid.timeofday.format@@")
            }
        }

    }

    def preUpdate(domainModelOrMap) {
        CommunicationOrganization communicationOrganization = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationOrganization
        communicationOrganization.isAvailable = communicationOrganization.isAvailable ?: false;

        /* generate encrypted password if necessary*/
        if (communicationOrganization.clearMobileApplicationKey && communicationOrganization.clearMobileApplicationKey.length() > 0) {
            communicationOrganization.encryptedMobileApplicationKey = encryptPassword(communicationOrganization.clearMobileApplicationKey)
        } else if (communicationOrganization.mobileApplicationName == null || communicationOrganization.mobileApplicationName == "") {
            communicationOrganization.encryptedMobileApplicationKey = null
        }

        if (communicationOrganization.dateFormat != null) {
            try {
                def datestring = validateDateFormat(communicationOrganization.dateFormat)
            } catch (Exception e) {
                throw new ApplicationException(CommunicationOrganization, "@@r1:invalid.date.format@@")
            }
        }
        if (communicationOrganization.dayOfWeekFormat != null) {
            try {
                def datestring = validateDateFormat(communicationOrganization.dayOfWeekFormat)
            } catch (Exception e) {
                throw new ApplicationException(CommunicationOrganization, "@@r1:invalid.dayofweek.format@@")
            }
        }
        if (communicationOrganization.timeOfDayFormat != null) {
            try {
                def datestring = validateDateFormat(communicationOrganization.timeOfDayFormat)
            } catch (Exception e) {
                throw new ApplicationException(CommunicationOrganization, "@@r1:invalid.timeofday.format@@")
            }
        }

    }

    def private validateDateFormat(String dateformat) {
        /* The simpledateformat is very lenient in creating a new instance.  Doesnt error out for many invalid formats.
        Here we try to catch most of the very apparent ones by comparing the formatted date with the original format.
        if they are equal then the original format was not valid and the date was not formatted.
         */
        def datestring = new SimpleDateFormat(dateformat)
        datestring.setLenient(false)
        def convertedDate = datestring.format(new Date())
        if (dateformat.equals(convertedDate.toString()))
            throw new ApplicationException(CommunicationOrganization, "@@r1:invalid.date.format@@")
        convertedDate
    }

    def decryptPassword(String encryptedPassword) {
        def decryptedPassword
        String encryptionKey = Holders.config.communication?.security?.password?.encKey
        if (encryptionKey == null)
            throw new ApplicationException(CommunicationOrganization, "@@r1:security.keyMissing@@")
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.call("{$Sql.VARCHAR = call gckencr.decrypt_string (${encryptionKey},${encryptedPassword})}") {
            result -> decryptedPassword = result
        }
        decryptedPassword
    }

    def encryptPassword(String clearTextPassword) {
        def encryptedPassword
        String encryptionKey = Holders.config.communication?.security?.password?.encKey
        if (encryptionKey == null)
            throw new ApplicationException(CommunicationOrganization, "@@r1:security.keyMissing@@")
        try {
            def sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.call("{$Sql.VARCHAR = call gckencr.encrypt_string (${encryptionKey},${clearTextPassword})}") {
                result -> encryptedPassword = result
            }
        } catch (Exception e) {
            throw new net.hedtech.banner.exceptions.ApplicationException(CommunicationOrganization, "@@r1:password.encrypt.error@@")
        }
        encryptedPassword
    }


    @Override
    public boolean isDirty(model) {
        return true
    }
}
