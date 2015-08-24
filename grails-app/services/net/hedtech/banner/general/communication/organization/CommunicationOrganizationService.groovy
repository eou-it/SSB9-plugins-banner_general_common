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
        if (communicationOrganization?.replyToMailboxAccountSettings?.getAt(0)?.clearTextPassword) {
            communicationOrganization?.replyToMailboxAccountSettings[0].encryptedPassword = encryptMailBoxAccountPassword(communicationOrganization.theReplyToMailboxAccount.clearTextPassword)
        }
        if (communicationOrganization?.senderMailboxAccountSettings?.getAt(0)?.clearTextPassword) {
            communicationOrganization?.senderMailboxAccountSettings[0].encryptedPassword = encryptMailBoxAccountPassword(communicationOrganization.theSenderMailboxAccount.clearTextPassword)
        }

        def rootOrg = CommunicationOrganization.fetchRoot()
        if (rootOrg == null)
            communicationOrganization.parent = null
        else if (communicationOrganization.parent == null)
            communicationOrganization.parent = rootOrg.id

        communicationOrganization.isAvailable = communicationOrganization.isAvailable ?: false;

        /* generate encrypted password if necessary*/
        if (communicationOrganization.clearTextPassword != null) {
            communicationOrganization.mobileApplicationKey = encryptMailBoxAccountPassword(communicationOrganization.clearTextPassword)
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

        def senderMailbox = domainModelOrMap?.senderMailboxAccountSettings ? domainModelOrMap?.senderMailboxAccountSettings[0] : null
        def replyToMailbox = domainModelOrMap?.replyToMailboxAccountSettings ? domainModelOrMap?.replyToMailboxAccountSettings[0] : null
        def sendEmailServerProperties = domainModelOrMap?.sendEmailServerProperties ? domainModelOrMap?.sendEmailServerProperties[0] : null
        def receiveEmailServerProperties = domainModelOrMap?.receiveEmailServerProperties ? domainModelOrMap?.receiveEmailServerProperties[0] : null

        if (sendEmailServerProperties) {
            if (communicationOrganization.sendEmailServerProperties?.getAt(0) == null) {
                communicationOrganization.sendEmailServerProperties?.add(new CommunicationEmailServerProperties())
            }
            communicationOrganization.sendEmailServerProperties[0].host = sendEmailServerProperties.host ? sendEmailServerProperties.host : null
            if (sendEmailServerProperties.port == null || sendEmailServerProperties.port == "") {
                communicationOrganization.sendEmailServerProperties[0].port = 0
            } else {
                communicationOrganization.sendEmailServerProperties[0].port = (sendEmailServerProperties.port instanceof String ? Integer.parseInt(sendEmailServerProperties.port) : sendEmailServerProperties.port)
            }
            if (sendEmailServerProperties?.securityProtocol instanceof String)
                communicationOrganization.sendEmailServerProperties[0].securityProtocol = sendEmailServerProperties?.securityProtocol
            else if (sendEmailServerProperties?.securityProtocol == null)
                communicationOrganization.sendEmailServerProperties[0].securityProtocol = CommunicationEmailServerConnectionSecurity.None
            else
                communicationOrganization.sendEmailServerProperties[0].securityProtocol = CommunicationEmailServerConnectionSecurity.valueOf(sendEmailServerProperties?.securityProtocol?.name)
        }

        if (receiveEmailServerProperties) {
            if (communicationOrganization.receiveEmailServerProperties?.getAt(0) == null) {
                communicationOrganization.receiveEmailServerProperties?.add(new CommunicationEmailServerProperties())
            }
            communicationOrganization.receiveEmailServerProperties[0].host = receiveEmailServerProperties.host ? receiveEmailServerProperties.host : null
            if (receiveEmailServerProperties.port == null || receiveEmailServerProperties.port == "") {
                communicationOrganization.receiveEmailServerProperties[0].port = 0
            } else {
                communicationOrganization.receiveEmailServerProperties[0].port = (receiveEmailServerProperties.port instanceof String ? Integer.parseInt(receiveEmailServerProperties.port) : receiveEmailServerProperties.port)
            }
            if (receiveEmailServerProperties?.securityProtocol instanceof String)
                communicationOrganization.receiveEmailServerProperties[0]?.securityProtocol = receiveEmailServerProperties?.securityProtocol
            else if (receiveEmailServerProperties?.securityProtocol == null)
                communicationOrganization.receiveEmailServerProperties[0]?.securityProtocol = CommunicationEmailServerConnectionSecurity.None
            else
                communicationOrganization.receiveEmailServerProperties[0]?.securityProtocol = CommunicationEmailServerConnectionSecurity.valueOf(receiveEmailServerProperties?.securityProtocol?.name)
        }

        if (senderMailbox) {
            if (communicationOrganization.senderMailboxAccountSettings?.getAt(0) == null) {
                communicationOrganization.senderMailboxAccountSettings?.add(new CommunicationMailboxAccount())
            }
            communicationOrganization.senderMailboxAccountSettings[0].userName = senderMailbox.userName
            communicationOrganization.senderMailboxAccountSettings[0].clearTextPassword = senderMailbox.clearTextPassword
            communicationOrganization.senderMailboxAccountSettings[0].encryptedPassword = senderMailbox.encryptedPassword
            communicationOrganization.senderMailboxAccountSettings[0].emailAddress = senderMailbox.emailAddress
            communicationOrganization.senderMailboxAccountSettings[0].emailDisplayName = senderMailbox.emailDisplayName ? senderMailbox.emailDisplayName : null
        }

        if (replyToMailbox) {
            if (communicationOrganization.replyToMailboxAccountSettings?.getAt(0) == null) {
                communicationOrganization.replyToMailboxAccountSettings.add(new CommunicationMailboxAccount())
            }

            communicationOrganization.replyToMailboxAccountSettings[0].emailDisplayName = replyToMailbox.emailDisplayName ? replyToMailbox.emailDisplayName : null
            communicationOrganization.replyToMailboxAccountSettings[0].emailAddress = replyToMailbox.emailAddress
            communicationOrganization.replyToMailboxAccountSettings[0].clearTextPassword = replyToMailbox.clearTextPassword
            communicationOrganization.replyToMailboxAccountSettings[0].encryptedPassword = replyToMailbox.encryptedPassword
            communicationOrganization.replyToMailboxAccountSettings[0].userName = replyToMailbox.userName
        }

/* ensure both username and email address are populated*/
        if (communicationOrganization?.replyToMailboxAccountSettings?.getAt(0) && !(communicationOrganization?.replyToMailboxAccountSettings?.getAt(0)?.emailAddress != null && communicationOrganization?.replyToMailboxAccountSettings?.getAt(0)?.userName != null)
            && !(communicationOrganization?.replyToMailboxAccountSettings?.getAt(0)?.emailAddress == null && communicationOrganization?.replyToMailboxAccountSettings?.getAt(0)?.userName == null)) {
            throw new ApplicationException(CommunicationOrganization, "@@r1:mailbox.nameAndAddress.required@@")
        }
        if (communicationOrganization?.senderMailboxAccountSettings?.getAt(0) && !(communicationOrganization?.senderMailboxAccountSettings?.getAt(0)?.emailAddress != null && communicationOrganization?.senderMailboxAccountSettings?.getAt(0)?.userName != null)
                  && !(communicationOrganization?.senderMailboxAccountSettings?.getAt(0)?.emailAddress == null && communicationOrganization?.senderMailboxAccountSettings?.getAt(0)?.userName == null)) {
            throw new ApplicationException(CommunicationOrganization, "@@r1:mailbox.nameAndAddress.required@@")
        }

        /* generate encrypted password if necessary*/
        if (communicationOrganization?.replyToMailboxAccountSettings?.getAt(0)?.clearTextPassword) {
            communicationOrganization?.replyToMailboxAccountSettings[0].encryptedPassword = encryptMailBoxAccountPassword(communicationOrganization.theReplyToMailboxAccount.clearTextPassword)
        }
        if (communicationOrganization?.senderMailboxAccountSettings?.getAt(0)?.clearTextPassword) {
            communicationOrganization?.senderMailboxAccountSettings[0].encryptedPassword = encryptMailBoxAccountPassword(communicationOrganization.theSenderMailboxAccount.clearTextPassword)
        }

        communicationOrganization.isAvailable = communicationOrganization.isAvailable ?: false;

        /* generate encrypted password if necessary*/
        if (communicationOrganization.clearTextPassword != null) {
            communicationOrganization.mobileApplicationKey = encryptMailBoxAccountPassword(communicationOrganization.clearTextPassword)
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


    def decryptMailBoxAccountPassword(String encryptedPassword) {
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


    def encryptMailBoxAccountPassword(String clearTextPassword) {
        def encryptedPassword
        String encryptionKey = Holders.config.communication?.security?.password?.encKey
        if (encryptionKey == null)
            throw new ApplicationException(CommunicationOrganization, "@@r1:security.keyMissing@@")
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.call("{$Sql.VARCHAR = call gckencr.encrypt_string (${encryptionKey},${clearTextPassword})}") {
            result -> encryptedPassword = result
        }
        encryptedPassword
    }


    @Override
    public boolean isDirty(model) {
        return true
    }
}
