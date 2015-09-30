/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.mobile

import groovyx.net.http.HTTPBuilder
import net.hedtech.banner.exceptions.ExceptionFactory
import net.hedtech.banner.general.communication.CommunicationErrorCode
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import net.hedtech.banner.general.communication.template.CommunicationMobileNotificationExpirationPolicy
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.Method.POST
import org.joda.time.format.ISODateTimeFormat


/**
 * Performs the details of assembling and sending out an Ellucian Go mobile notification.
 */
class CommunicationSendMobileNotificationMethod {
    private Log log = LogFactory.getLog(this.getClass())


    public void execute(CommunicationMobileNotificationMessage message, CommunicationOrganization senderOrganization) {
        log.trace("Begin send mobile notification.")
        assert (senderOrganization)
        assert (message)
        assert (message.externalUser)
        assert (message.referenceId)

        if (senderOrganization.encryptedMobileApplicationKey) {
            assert senderOrganization.clearMobileApplicationKey
        }

        if (isEmpty(senderOrganization.mobileEndPointUrl)) {
            throw ExceptionFactory.createFriendlyApplicationException(CommunicationSendMobileNotificationMethod.class,
                    CommunicationErrorCode.EMPTY_MOBILE_NOTIFICATION_ENDPOINT_URL.name,
                    "emptyMobileNotificationEndpointUrl",
                    senderOrganization.name
            )
        }

        if (isEmpty(senderOrganization.mobileApplicationName)) {
            throw ExceptionFactory.createApplicationException(CommunicationSendMobileNotificationMethod.class,
                    CommunicationErrorCode.EMPTY_MOBILE_NOTIFICATION_APPLICATION_NAME.name,
                    "emptyMobileNotificationApplicationName",
                    senderOrganization.name
            )
        }

        if (isEmpty(senderOrganization.encryptedMobileApplicationKey)) {
            throw ExceptionFactory.createFriendlyApplicationException(CommunicationSendMobileNotificationMethod.class,
                    CommunicationErrorCode.EMPTY_MOBILE_NOTIFICATION_APPLICATION_KEY.name,
                    "emptyMobileNotificationApplicationKey",
                    senderOrganization.name
            )
        }

        if (!message.externalUser || message.externalUser.trim().length() == 0) {
            throw ExceptionFactory.createFriendlyApplicationException( CommunicationSendMobileNotificationService.class,
                    CommunicationErrorCode.EMPTY_MOBILE_NOTIFICATION_EXTERNAL_USER.name,
                    "noExternalUser"
            )
        }

        try {
            // Ex: 'https://mobiledev1.ellucian.com/'
            HTTPBuilder httpBuilder = new HTTPBuilder(senderOrganization.mobileEndPointUrl)
            httpBuilder.auth.basic senderOrganization.mobileApplicationName, senderOrganization.clearMobileApplicationKey
            httpBuilder.request(POST, JSON) { request ->
                uri.path = '/banner-mobileserver/api/notification/notifications/'
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

                //            def expiresDateFormat = new SimpleDateFormat("YYYY-MM-DDhh:mm:ssZ")
                //            if (notificationInstance?.expires) bodyMap.put("expires", expiresDateFormat.format(notificationInstance?.expires))

                if (message.destinationLabel) {
                    messageMap.put("destinationLabel", message.destinationLabel)
                }
                if (message.destinationLink) {
                    messageMap.put("destination", message.destinationLink)
                }



                switch(message.expirationPolicy) {
                    case CommunicationMobileNotificationExpirationPolicy.DURATION:
                        messageMap.put( "expires", String.valueOf( message.duration ) )
                        break
                    case CommunicationMobileNotificationExpirationPolicy.DATE_TIME:
                        messageMap.put( "expires", ISODateTimeFormat.dateTime().print( message.expirationDateTime.time ) )
                        break
                }

                log.debug( messageMap )
                body = messageMap

                response.success = { theResponse, reader ->
                    if (log.isDebugEnabled()) {
                        log.debug("Got response: ${theResponse.statusLine}")
                        log.debug("Content-Type: ${theResponse.headers.'Content-Type'}")
                        log.debug(reader.text)
                        log.debug(((reader != null) && (reader.text != null)))
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace()
            log.error( 'Error trying to send mobile notification.', t );
            throw ExceptionFactory.createApplicationException(CommunicationSendMobileNotificationMethod.class, t, CommunicationErrorCode.UNKNOWN_ERROR.name())
        }
    }


    private boolean isEmpty(String s) {
        return !s || s.length() == 0
    }
}