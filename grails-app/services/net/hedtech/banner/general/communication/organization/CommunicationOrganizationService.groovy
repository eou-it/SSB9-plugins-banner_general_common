/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.organization

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.service.ServiceBase
import org.apache.log4j.Logger
import org.hibernate.Hibernate

import java.sql.Blob
import java.text.SimpleDateFormat

/**
 * Service for providing basic crud services on
 * Folder domain objects.
 */
class CommunicationOrganizationService extends ServiceBase {
    def log = Logger.getLogger( this.getClass() )


    def preCreate( domainModelOrMap ) {
        CommunicationOrganization communicationOrganization = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationOrganization

        if (communicationOrganization.senderMailboxAccountSettings?.password != null) {
            Blob blob = Hibernate.getLobCreator( sessionFactory.getCurrentSession().connection() )
                    .createBlob( communicationOrganization.senderMailboxAccountSettings?.password, 5000 );
            communicationOrganization.senderMailboxAccountSettings.password = blob
        }

        if (communicationOrganization.replyToMailboxAccountSettings?.password != null) {
            Blob blob = Hibernate.getLobCreator( sessionFactory.getCurrentSession().connection() )
                    .createBlob( communicationOrganization.replyToMailboxAccountSettings?.password, 5000 );
            communicationOrganization.replyToMailboxAccountSettings.password = blob
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


    def preUpdate( domainModelOrMap ) {

        CommunicationOrganization communicationOrganization = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationOrganization

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


}
