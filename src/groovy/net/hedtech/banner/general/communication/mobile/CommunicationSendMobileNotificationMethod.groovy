/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.mobile

import groovy.time.DatumDependentDuration
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.HttpResponseException
import groovyx.net.http.ResponseParseException
import net.hedtech.banner.configuration.ApplicationConfigurationUtils
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.exceptions.CommunicationExceptionFactory
import net.hedtech.banner.general.communication.CommunicationErrorCode
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import net.hedtech.banner.general.communication.template.CommunicationDurationUnit
import net.sf.json.JSONArray
import net.sf.json.util.JSONUtils
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.apache.http.conn.HttpHostConnectException
import org.joda.time.format.ISODateTimeFormat

import javax.net.ssl.SSLPeerUnverifiedException

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
            throw CommunicationExceptionFactory.createApplicationException(CommunicationSendMobileNotificationMethod.class,
                    new RuntimeException('communication.error.message.mobileEndpoint.invalidUrl'),
                    CommunicationErrorCode.EMPTY_MOBILE_NOTIFICATION_ENDPOINT_URL.name()
            )
        }

        if (!message.externalUser || message.externalUser.trim().length() == 0) {
            throw CommunicationExceptionFactory.createApplicationException(
                    CommunicationSendMobileNotificationService.class,
                    new RuntimeException('communication.error.message.emptyExternalId'),
                    CommunicationErrorCode.EMPTY_MOBILE_NOTIFICATION_EXTERNAL_USER.name())
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

        def mobileEndPointUrl = rootOrganization.mobileEndPointUrl
        try {
            // Ex: 'https://mobiledev1.ellucian.com/'
            if (log.isDebugEnabled()) {
                log.debug( "Connecting to ${mobileEndPointUrl} as ${mobileApplicationName}")
            }

            HTTPBuilder httpBuilder = new HTTPBuilder(mobileEndPointUrl)
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
                    def jsonResponse = reader?.notifications
                    serverResponse = JSONUtils.valueToString( jsonResponse, 2, 0 )
                    // in case error message comes back
                    if (jsonResponse[0].messages[0]?:"" != "")
                        throw CommunicationExceptionFactory.createApplicationException(CommunicationSendMobileNotificationMethod.class,new RuntimeException((String) jsonResponse[0].messages[0]), CommunicationErrorCode.MOBILE_NOTIFICATION_POSSIBLE_SEND_ERROR.name())

                    if (log.isDebugEnabled()) {
                        log.debug( "Response is: " + serverResponse )
                    }
                }
            }
        } catch(IllegalStateException t) {
            log.error( 'Error trying to send mobile notification.', t );
            throw CommunicationExceptionFactory.createApplicationException(CommunicationSendMobileNotificationMethod.class, new RuntimeException("communication.error.message.mobileEndpoint.invalidUrl"), CommunicationErrorCode.INVALID_MOBILE_NOTIFICATION_ENDPOINT_URL.name())
        } catch(UnknownHostException t) {
            log.error( 'Error trying to send mobile notification.', t );
            throw CommunicationExceptionFactory.createApplicationException(CommunicationSendMobileNotificationMethod.class, new RuntimeException("communication.error.message.mobileEndpoint.unknownHost"), CommunicationErrorCode.MOBILE_NOTIFICATION_APPLICATION_ENDPOINT_UNKNOWN_HOST.name())
        } catch(HttpHostConnectException t) {
            log.error( 'Error trying to send mobile notification.', t );
            throw CommunicationExceptionFactory.createApplicationException(CommunicationSendMobileNotificationMethod.class, new RuntimeException("communication.error.message.mobileEndpoint.HostRefused"), CommunicationErrorCode.MOBILE_NOTIFICATION_APPLICATION_ENDPOINT_HOST_REFUSED.name())
        } catch(SSLPeerUnverifiedException t) {
            log.error( 'Error trying to send mobile notification.', t );
            throw CommunicationExceptionFactory.createApplicationException(CommunicationSendMobileNotificationMethod.class, new RuntimeException("communication.error.message.mobileEndpoint.SSLUnverified"), CommunicationErrorCode.MOBILE_NOTIFICATION_APPLICATION_ENDPOINT_SSL_UNVERIFIED.name())
        } catch(ResponseParseException t) {
            if (log.isErrorEnabled()) {
                String contentType
                try {
                    contentType = t.response?.contentType
                } catch(IllegalArgumentException e) {
                    contentType = e.getMessage()
                }
                log.error( "Error trying to send mobile notification. Response content type = ${contentType}; status line = '${t.response?.statusLine}'", t )
            }
            throw CommunicationExceptionFactory.createApplicationException(CommunicationSendMobileNotificationMethod.class, t, CommunicationErrorCode.UNKNOWN_ERROR.name())
        } catch(HttpResponseException t) {
            if (log.isErrorEnabled()) {
                String contentType
                try {
                    contentType = t.response?.contentType
                } catch(IllegalArgumentException e) {
                    contentType = e.getMessage()
                }
                log.error( "Error trying to send mobile notification. Response content type = ${contentType}; status line = '${t.response?.statusLine}'", t )
            }
            throw CommunicationExceptionFactory.createApplicationException(CommunicationSendMobileNotificationMethod.class, new RuntimeException("communication.error.message.unauthorizedMobile"), CommunicationErrorCode.INVALID_MOBILE_NOTIFICATION_APPLICATION_NAME_OR_KEY.name())
        } catch(ApplicationException t) {
            log.error( 'Error trying to send mobile notification.', t );
            throw CommunicationExceptionFactory.createApplicationException(CommunicationSendMobileNotificationMethod.class, t, CommunicationErrorCode.UNKNOWN_ERROR.name())
        }

        catch(Throwable t) {
            log.error( 'Error trying to send mobile notification.', t );
            throw CommunicationExceptionFactory.createApplicationException(CommunicationSendMobileNotificationMethod.class, t, CommunicationErrorCode.UNKNOWN_ERROR.name())
        }
    }


    private static boolean isEmpty(String s) {
        return ((!s) || (s == null) || (s.length() == 0) || (s == ""))
    }
}