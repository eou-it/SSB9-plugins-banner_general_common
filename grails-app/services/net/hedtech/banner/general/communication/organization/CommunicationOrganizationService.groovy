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
    def log = Logger.getLogger( this.getClass() )


    def preCreate( domainModelOrMap ) {
        CommunicationOrganization communicationOrganization = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationOrganization
        if (communicationOrganization?.replyToMailboxAccountSettings?.getAt( 0 )?.clearTextPassword) {
            communicationOrganization?.replyToMailboxAccountSettings[0].encryptedPassword = encryptMailBoxAccountPassword( communicationOrganization.theReplyToMailboxAccount.clearTextPassword )
        }
        if (communicationOrganization?.senderMailboxAccountSettings?.getAt( 0 )?.clearTextPassword) {
            communicationOrganization?.senderMailboxAccountSettings[0].encryptedPassword = encryptMailBoxAccountPassword( communicationOrganization.theSenderMailboxAccount.clearTextPassword )
        }

        def existingOrg = CommunicationOrganization.findAll()
        if (existingOrg.size() > 0)
            throw new ApplicationException( CommunicationOrganization, "@@r1:onlyOneOrgCanExist@@" )

        if (communicationOrganization.dateFormat != null) {
            try {
                def datestring = validateDateFormat( communicationOrganization.dateFormat )
            } catch (Exception e) {
                throw new ApplicationException( CommunicationOrganization, "@@r1:invalid.date.format@@" )
            }
        }
        if (communicationOrganization.dayOfWeekFormat != null) {
            try {
                def datestring = validateDateFormat( communicationOrganization.dayOfWeekFormat )
            } catch (Exception e) {
                throw new ApplicationException( CommunicationOrganization, "@@r1:invalid.dayofweek.format@@" )
            }
        }
        if (communicationOrganization.timeOfDayFormat != null) {
            try {
                def datestring = validateDateFormat( communicationOrganization.timeOfDayFormat )
            } catch (Exception e) {
                throw new ApplicationException( CommunicationOrganization, "@@r1:invalid.timeofday.format@@" )
            }
        }

    }


    def preUpdate( domainModelOrMap ) {
        CommunicationOrganization communicationOrganization = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationOrganization
        if (communicationOrganization?.replyToMailboxAccountSettings?.getAt( 0 )?.clearTextPassword) {
            communicationOrganization?.replyToMailboxAccountSettings[0].encryptedPassword = encryptMailBoxAccountPassword( communicationOrganization.theReplyToMailboxAccount.clearTextPassword )
        }
        if (communicationOrganization?.senderMailboxAccountSettings?.getAt( 0 )?.clearTextPassword) {
            communicationOrganization?.senderMailboxAccountSettings[0].encryptedPassword = encryptMailBoxAccountPassword( communicationOrganization.theSenderMailboxAccount.clearTextPassword )
        }

        if (communicationOrganization.dateFormat != null) {
            try {
                def datestring = validateDateFormat( communicationOrganization.dateFormat )
            } catch (Exception e) {
                throw new ApplicationException( CommunicationOrganization, "@@r1:invalid.date.format@@" )
            }
        }
        if (communicationOrganization.dayOfWeekFormat != null) {
            try {
                def datestring = validateDateFormat( communicationOrganization.dayOfWeekFormat )
            } catch (Exception e) {
                throw new ApplicationException( CommunicationOrganization, "@@r1:invalid.dayofweek.format@@" )
            }
        }
        if (communicationOrganization.timeOfDayFormat != null) {
            try {
                def datestring = validateDateFormat( communicationOrganization.timeOfDayFormat )
            } catch (Exception e) {
                throw new ApplicationException( CommunicationOrganization, "@@r1:invalid.timeofday.format@@" )
            }
        }

    }


    def private validateDateFormat( String dateformat ) {
        /* The simpledateformat is very lenient in creating a new instance.  Doesnt error out for many invalid formats.
        Here we try to catch most of the very apparent ones by comparing the formatted date with the original format.
        if they are equal then the original format was not valid and the date was not formatted.
         */
        def datestring = new SimpleDateFormat( dateformat )
        datestring.setLenient( false )
        def convertedDate = datestring.format( new Date() )
        if (dateformat.equals( convertedDate.toString() ))
            throw new ApplicationException( CommunicationOrganization, "@@r1:invalid.date.format@@" )
        convertedDate
    }


    def decryptMailBoxAccountPassword( String encryptedPassword ) {
        def decryptedPassword
        String encryptionKey = Holders.config.communication?.security?.password?.encKey
        if (encryptionKey == null)
            throw new ApplicationException( CommunicationOrganization, "@@r1:security.keyMissing@@" )
        def sql = new Sql( sessionFactory.getCurrentSession().connection() )
        sql.call( "{$Sql.VARCHAR = call gckencr.decrypt_string (${encryptionKey},${encryptedPassword})}" ) {
            result -> decryptedPassword = result
        }
        decryptedPassword
    }


    def encryptMailBoxAccountPassword( String clearTextPassword ) {
        def encryptedPassword
        String encryptionKey = Holders.config.communication?.security?.password?.encKey
        if (encryptionKey == null)
              throw new ApplicationException( CommunicationOrganization, "@@r1:security.keyMissing@@" )
        def sql = new Sql( sessionFactory.getCurrentSession().connection() )
        sql.call( "{$Sql.VARCHAR = call gckencr.encrypt_string (${encryptionKey},${clearTextPassword})}" ) {
            result -> encryptedPassword = result
        }
        encryptedPassword
    }


    @Override
    public boolean isDirty( model ) {
        return true
    }
}
