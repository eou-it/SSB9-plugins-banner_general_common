/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.mobile

import groovy.time.DatumDependentDuration
import groovyx.net.http.HTTPBuilder
import net.hedtech.banner.general.communication.exceptions.CommunicationExceptionFactory
import net.hedtech.banner.general.communication.CommunicationErrorCode
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import net.hedtech.banner.general.communication.template.CommunicationDurationUnit
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.joda.time.format.ISODateTimeFormat

import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.Method.POST

/**
 * Performs the details of assembling and sending out an Ellucian Go mobile notification.
 */
class CommunicationSendMobileNotificationMethod {
    private Log log = LogFactory.getLog(this.getClass())
    String serverResponse
    def communicationOrganizationService

    public void execute(CommunicationMobileNotificationMessage message, CommunicationOrganization senderOrganization) {
        log.trace("Begin send mobile notification.")
        assert (senderOrganization)
        assert (message)
        assert (message.referenceId)
        assert( communicationOrganizationService != null )

        serverResponse = null
        CommunicationOrganization rootOrganization = CommunicationOrganization.fetchRoot()

        if (isEmpty(rootOrganization.mobileEndPointUrl)) {
            throw CommunicationExceptionFactory.createFriendlyApplicationException(CommunicationSendMobileNotificationMethod.class,
                    CommunicationErrorCode.EMPTY_MOBILE_NOTIFICATION_ENDPOINT_URL,
                    "emptyMobileNotificationEndpointUrl",
                    senderOrganization.name
            )
        }

        if (!message.externalUser || message.externalUser.trim().length() == 0) {
            throw CommunicationExceptionFactory.createFriendlyApplicationException(CommunicationSendMobileNotificationService.class,
                    CommunicationErrorCode.EMPTY_MOBILE_NOTIFICATION_EXTERNAL_USER.toString(),
                    "noExternalUser"
            )
        }

        String mobileApplicationName
        String mobileApplicationKey

        if (senderOrganization?.mobileApplicationName && senderOrganization?.mobileApplicationName.trim().length() > 0) {
            mobileApplicationName = senderOrganization.mobileApplicationName
            mobileApplicationKey = communicationOrganizationService.decryptPassword( senderOrganization.encryptedMobileApplicationKey )
        } else {
            mobileApplicationName = rootOrganization?.mobileApplicationName
            mobileApplicationKey = communicationOrganizationService.decryptPassword( rootOrganization.encryptedMobileApplicationKey )
        }

        if (isEmpty(mobileApplicationName)) {
            throw CommunicationExceptionFactory.createFriendlyApplicationException(CommunicationSendMobileNotificationMethod.class,
                    CommunicationErrorCode.EMPTY_MOBILE_NOTIFICATION_APPLICATION_NAME,
                    "emptyMobileNotificationApplicationName",
                    senderOrganization.name
            )
        }

        if (isEmpty(mobileApplicationKey)) {
            throw CommunicationExceptionFactory.createFriendlyApplicationException(CommunicationSendMobileNotificationMethod.class,
                    CommunicationErrorCode.EMPTY_MOBILE_NOTIFICATION_APPLICATION_KEY,
                    "emptyMobileNotificationApplicationKey",
                    senderOrganization.name
            )
        }

        try {
            // Ex: 'https://mobiledev1.ellucian.com/'
            HTTPBuilder httpBuilder = new HTTPBuilder(rootOrganization.mobileEndPointUrl)
            httpBuilder.auth.basic mobileApplicationName, mobileApplicationKey
            httpBuilder.request(POST, JSON) { request ->
                headers.Accept = 'application/json'

                def messageMap = [
                        mobileHeadline: message.mobileHeadline,
                        push          : message.push,
                        sticky        : message.sticky,
                        uuid          : message.referenceId
                ]
                messageMap.put( "recipients", [ [ idType : 'loginId', id: message.externalUser ] ] )

                if (message.headline) {
                    messageMap.put("headline", message?.headline)
                }

                if (message.messageDescription) {
                    messageMap.put("description", message.messageDescription)
                }

                if (message.destinationLabel) {
                    messageMap.put("destinationLabel", message.destinationLabel)
                }

                if (message.destinationLink) {
                    messageMap.put("destination", message.destinationLink)
                }

                switch(message.expirationPolicy) {
                    case CommunicationMobileNotificationExpirationPolicy.DURATION:
                        Date today = new Date()
                        DatumDependentDuration period
                        switch(message.durationUnit) {
                            case CommunicationDurationUnit.DAY:
                                period = new DatumDependentDuration(0, 0, message.duration, 0, 0, 0, 0)
                                break;
                            case CommunicationDurationUnit.HOUR:
                                period = new DatumDependentDuration(0, 0, 0, message.duration, 0, 0, 0)
                                break;
                            case CommunicationDurationUnit.MINUTE:
                            default:
                                period = new DatumDependentDuration(0, 0, 0, 0, message.duration, 0, 0)
                                break;
                        }

                        Date expirationDateTime = period + today
                        messageMap.put( "expires", ISODateTimeFormat.dateTime().print( expirationDateTime.time ) )
                        break
                    case CommunicationMobileNotificationExpirationPolicy.DATE_TIME:
                        messageMap.put( "expires", ISODateTimeFormat.dateTime().print( message.expirationDateTime.time ) )
                        break
                }

                log.debug( messageMap )
                body = messageMap

                response.success = { theResponse, reader ->
                    if (reader?.notifications) {
                        def builder = new groovy.json.JsonBuilder( reader.notifications )
                        serverResponse = builder.content.toString()
                        if (log.isDebugEnabled()) {
                            log.debug( new groovy.json.JsonBuilder( reader.notifications ).toPrettyString() )
                        }
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug( "Response is null." )
                        }
                    }
                }
            }
        } catch (Throwable t) {
            log.error( 'Error trying to send mobile notification.', t );

            if (t instanceof java.lang.IllegalStateException) {
                throw CommunicationExceptionFactory.createApplicationException(CommunicationSendMobileNotificationMethod.class, t, CommunicationErrorCode.INVALID_MOBILE_NOTIFICATION_ENDPOINT_URL.name())
            }

            if ((t instanceof java.net.UnknownHostException) || (t instanceof org.apache.http.conn.HttpHostConnectException) || (t instanceof javax.net.ssl.SSLPeerUnverifiedException)) {
                throw CommunicationExceptionFactory.createApplicationException(CommunicationSendMobileNotificationMethod.class, t, CommunicationErrorCode.UNKNOWN_MOBILE_NOTIFICATION_APPLICATION_ENDPOINT.name())
            }

            if (t instanceof groovyx.net.http.HttpResponseException) {
                throw CommunicationExceptionFactory.createApplicationException(CommunicationSendMobileNotificationMethod.class, t, CommunicationErrorCode.INVALID_MOBILE_NOTIFICATION_APPLICATION_NAME_OR_KEY.name())
            }

            throw CommunicationExceptionFactory.createApplicationException(CommunicationSendMobileNotificationMethod.class, t, CommunicationErrorCode.UNKNOWN_ERROR.name())
        }
    }


    private boolean isEmpty(String s) {
        return !s || s.length() == 0
    }
}