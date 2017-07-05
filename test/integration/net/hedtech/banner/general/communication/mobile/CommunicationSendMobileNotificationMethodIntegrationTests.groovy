/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.mobile

import net.hedtech.banner.general.communication.CommunicationErrorCode
import net.hedtech.banner.general.communication.exceptions.CommunicationApplicationException
import net.hedtech.banner.general.communication.organization.*
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.apache.commons.logging.LogFactory
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Test sending basic mobile notification.
 */
class CommunicationSendMobileNotificationMethodIntegrationTests extends BaseIntegrationTestCase {

    def log = LogFactory.getLog(this.class)
    def selfServiceBannerAuthenticationProvider
    def communicationOrganizationCompositeService
    def communicationOrganizationService
    CommunicationOrganization rootOrganization


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('BCMADMIN', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)
        setUpTestOrganization()
    }


    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }


    @Test
    public void testSend() {
        CommunicationMobileNotificationMessage message = new CommunicationMobileNotificationMessage()
        message.mobileHeadline = "Test Send from BCM"
        message.headline = "Test Send from BCM at " + new Date()
        message.messageDescription = "CommunicationSendMobileNotificationMethodIntegrationTests.testSend"
        message.destinationLabel = "Eagles"
        message.destinationLink = "http://www.philadelphiaeagles.com"
        message.referenceId = UUID.randomUUID().toString()

        message.externalUser = "amandamason1"
        message.push = true

        CommunicationSendMobileNotificationMethod sendMethod = new CommunicationSendMobileNotificationMethod( communicationOrganizationService: communicationOrganizationService )
        sendMethod.execute( message, rootOrganization )
    }

    @Test
    public void testSendChildOrganization() {
        // test that child organization inherits endpoint from root organization
        CommunicationOrganization childOrganization = new CommunicationOrganization()
        childOrganization.name = "CommunicationSendMobileNotificationMethodIntegrationTests Organization"

        CommunicationMobileNotificationMessage message = new CommunicationMobileNotificationMessage()
        message.mobileHeadline = "testSendChildOrganization"
        message.referenceId = UUID.randomUUID().toString()
        message.externalUser = "amandamason1"
        message.push = true

        CommunicationSendMobileNotificationMethod sendMethod = new CommunicationSendMobileNotificationMethod( communicationOrganizationService: communicationOrganizationService )
        sendMethod.execute( message, childOrganization )

        rootOrganization.mobileApplicationName = ""
        rootOrganization.encryptedMobileApplicationKey = ""
        rootOrganization = communicationOrganizationService.update( rootOrganization )

        // Get new unique reference ID. Calling sendMethod.execute() twice with same refId will throw exception...
        message.referenceId = UUID.randomUUID().toString()

        childOrganization.mobileApplicationName = "StudentSuccess"
        childOrganization.clearMobileApplicationKey = "ss-key-value"
        childOrganization.encryptedMobileApplicationKey = communicationOrganizationService.encryptPassword( childOrganization.clearMobileApplicationKey )
        sendMethod.execute( message, childOrganization )
    }

    @Test
    public void testSendBadEndpoint() {
        CommunicationMobileNotificationMessage message = new CommunicationMobileNotificationMessage()
        message.mobileHeadline = "testSendBadEndpoint"
        message.referenceId = UUID.randomUUID().toString()
        message.externalUser = "amandamason1"
        message.push = true

        rootOrganization.mobileEndPointUrl = ""
        rootOrganization = communicationOrganizationService.update( rootOrganization )

        CommunicationSendMobileNotificationMethod sendMethod = new CommunicationSendMobileNotificationMethod( communicationOrganizationService: communicationOrganizationService )
        try {
            sendMethod.execute( message, rootOrganization )
            fail( "Expected EMPTY_MOBILE_NOTIFICATION_ENDPOINT_URL." )
        } catch( CommunicationApplicationException e ) {
            assertEquals( CommunicationErrorCode.EMPTY_MOBILE_NOTIFICATION_ENDPOINT_URL.toString(), e.type )
        }

        rootOrganization.mobileEndPointUrl = "bogus"
        rootOrganization = communicationOrganizationService.update( rootOrganization )
        try {
            sendMethod.execute( message, rootOrganization )
            fail( "Expected INVALID_MOBILE_NOTIFICATION_ENDPOINT_URL." )
        } catch( CommunicationApplicationException e ) {
            assertEquals( CommunicationErrorCode.INVALID_MOBILE_NOTIFICATION_ENDPOINT_URL.toString(), e.type )
        }

        rootOrganization.mobileEndPointUrl = "https://mobiledev"
        rootOrganization = communicationOrganizationService.update( rootOrganization )
        try {
            sendMethod.execute( message, rootOrganization )
            fail( "Expected MOBILE_NOTIFICATION_APPLICATION_ENDPOINT_UNKNOWN_HOST." )
        } catch( CommunicationApplicationException e ) {
            assertTrue( CommunicationErrorCode.MOBILE_NOTIFICATION_APPLICATION_ENDPOINT_UNKNOWN_HOST.toString() == e.type ||
            e.type == CommunicationErrorCode.MOBILE_NOTIFICATION_APPLICATION_ENDPOINT_SSL_UNVERIFIED .toString())
        }
    }


    @Test
    public void testSendMissingExternalId() {
        CommunicationMobileNotificationMessage message = new CommunicationMobileNotificationMessage()
        message.mobileHeadline = "testSendMissingExternalId"
        message.referenceId = UUID.randomUUID().toString()

        message.externalUser = ""
        message.push = true

        CommunicationSendMobileNotificationMethod sendMethod = new CommunicationSendMobileNotificationMethod( communicationOrganizationService: communicationOrganizationService )
        try {
            sendMethod.execute( message, rootOrganization )
            fail( "Expected EMPTY_MOBILE_NOTIFICATION_EXTERNAL_USER." )
        } catch( CommunicationApplicationException e ) {
            assertEquals( CommunicationErrorCode.EMPTY_MOBILE_NOTIFICATION_EXTERNAL_USER.toString(), e.type )
        }
    }


    @Test
    public void testSendBadApplicationName() {
        CommunicationMobileNotificationMessage message = new CommunicationMobileNotificationMessage()
        message.mobileHeadline = "testSendBadApplicationName"
        message.referenceId = UUID.randomUUID().toString()

        message.externalUser = "amandamason1"
        message.push = true

        CommunicationOrganization badOrganization = new CommunicationOrganization()
        badOrganization.name = "CommunicationSendMobileNotificationMethodIntegrationTests Organization"
        badOrganization.mobileEndPointUrl = "http://mobiledev3.ellucian.com/colleague-internal-mobileserver/api/notification/notifications/"
        badOrganization.mobileApplicationName = "StudentSuccess"

        CommunicationSendMobileNotificationMethod sendMethod = new CommunicationSendMobileNotificationMethod( communicationOrganizationService: communicationOrganizationService )

        badOrganization.clearMobileApplicationKey = ""
        badOrganization.encryptedMobileApplicationKey = ""
        try {
            sendMethod.execute( message, badOrganization )
            fail( "Expected EMPTY_MOBILE_NOTIFICATION_APPLICATION_KEY." )
        } catch( CommunicationApplicationException e ) {
            assertEquals( CommunicationErrorCode.EMPTY_MOBILE_NOTIFICATION_APPLICATION_KEY.toString(), e.type )
        }

        badOrganization.clearMobileApplicationKey = ""
        badOrganization.encryptedMobileApplicationKey = communicationOrganizationService.encryptPassword( badOrganization.clearMobileApplicationKey )
        try {
            sendMethod.execute( message, badOrganization )
            fail( "Expected EMPTY_MOBILE_NOTIFICATION_APPLICATION_KEY." )
        } catch( CommunicationApplicationException e ) {
            assertEquals( CommunicationErrorCode.EMPTY_MOBILE_NOTIFICATION_APPLICATION_KEY.toString(), e.type )
        }

        badOrganization.clearMobileApplicationKey = "bogus_bogus_bogus"
        badOrganization.encryptedMobileApplicationKey = communicationOrganizationService.encryptPassword( badOrganization.clearMobileApplicationKey )
        try {
            sendMethod.execute( message, badOrganization )
            fail( "Expected INVALID_MOBILE_NOTIFICATION_APPLICATION_NAME_OR_KEY." )
        } catch( CommunicationApplicationException e ) {
            assertEquals( CommunicationErrorCode.INVALID_MOBILE_NOTIFICATION_APPLICATION_NAME_OR_KEY.toString(), e.type )
        }
    }


    @Test
    public void testSendBadApplicationKey() {
        CommunicationMobileNotificationMessage message = new CommunicationMobileNotificationMessage()
        message.mobileHeadline = "testSendBadApplicationKey"
        message.referenceId = UUID.randomUUID().toString()

        message.externalUser = "amandamason1"
        message.push = true

        CommunicationOrganization badOrganization = new CommunicationOrganization()
        badOrganization.name = "CommunicationSendMobileNotificationMethodIntegrationTests Organization"
        badOrganization.mobileEndPointUrl = "http://mobiledev3.ellucian.com/colleague-internal-mobileserver/api/notification/notifications/"
        badOrganization.mobileApplicationName = ""
        badOrganization.clearMobileApplicationKey = "ss-key-value"
        badOrganization.encryptedMobileApplicationKey = communicationOrganizationService.encryptPassword( rootOrganization.clearMobileApplicationKey )

        rootOrganization.mobileApplicationName = ""
        rootOrganization = communicationOrganizationService.update( rootOrganization )

        CommunicationSendMobileNotificationMethod sendMethod = new CommunicationSendMobileNotificationMethod( communicationOrganizationService: communicationOrganizationService )
        try {
            sendMethod.execute( message, badOrganization )
            fail( "Expected EMPTY_MOBILE_NOTIFICATION_APPLICATION_NAME." )
        } catch( CommunicationApplicationException e ) {
            assertEquals( CommunicationErrorCode.EMPTY_MOBILE_NOTIFICATION_APPLICATION_NAME.toString(), e.type )
        }

        badOrganization.mobileApplicationName = "bogus_bogus_bogus"
        try {
            sendMethod.execute( message, badOrganization )
            fail( "Expected INVALID_MOBILE_NOTIFICATION_APPLICATION_NAME_OR_KEY." )
        } catch( CommunicationApplicationException e ) {
            assertEquals( CommunicationErrorCode.INVALID_MOBILE_NOTIFICATION_APPLICATION_NAME_OR_KEY.toString(), e.type )
        }
    }


    @Test
    public void testSendDuration() {
        CommunicationMobileNotificationMessage message = new CommunicationMobileNotificationMessage()
        message.expirationPolicy = CommunicationMobileNotificationExpirationPolicy.DURATION
        message.duration = 1
        message.mobileHeadline = "Test Send from BCM with duration ${message.duration} day ahead"
        message.headline = "Test Send from BCM at " + new Date()
        message.messageDescription = "CommunicationSendMobileNotificationMethodIntegrationTests.testSendDuration"
        message.destinationLabel = "Eagles"
        message.destinationLink = "http://www.philadelphiaeagles.com"
        message.referenceId = UUID.randomUUID().toString()

        message.externalUser = "amandamason1"
        message.push = true

        CommunicationSendMobileNotificationMethod sendMethod = new CommunicationSendMobileNotificationMethod( communicationOrganizationService: communicationOrganizationService )
        sendMethod.execute( message, rootOrganization )
    }


    @Test
    public void testSendDateTime() {
        CommunicationMobileNotificationMessage message = new CommunicationMobileNotificationMessage()
        message.expirationPolicy = CommunicationMobileNotificationExpirationPolicy.DATE_TIME

        Calendar calendar = Calendar.getInstance();
        calendar.setTime( new Date() );
        calendar.add( Calendar.DATE, 1 );

        message.expirationDateTime = calendar.getTime()
        message.mobileHeadline = "Test Send from BCM with datetime set to ${message.expirationDateTime} day ahead"
        message.headline = "Test Send from BCM at " + new Date()
        message.messageDescription = "CommunicationSendMobileNotificationMethodIntegrationTests.testSendDateTime"
        message.destinationLabel = "Eagles"
        message.destinationLink = "http://www.philadelphiaeagles.com"
        message.referenceId = UUID.randomUUID().toString()

        message.externalUser = "amandamason1"
        message.push = true

        CommunicationSendMobileNotificationMethod sendMethod = new CommunicationSendMobileNotificationMethod( communicationOrganizationService: communicationOrganizationService )
        sendMethod.execute( message, rootOrganization )
    }


    protected void setUpTestOrganization() {
        rootOrganization = CommunicationOrganization.fetchRoot()
        if (rootOrganization == null) {
            rootOrganization = new CommunicationOrganization()
            rootOrganization.name = "CommunicationSendMobileNotificationMethodIntegrationTests Organization"
            rootOrganization.mobileEndPointUrl = "http://mobiledev3.ellucian.com/colleague-internal-mobileserver/api/notification/notifications/"
            rootOrganization.mobileApplicationName = "StudentSuccess"
            rootOrganization.clearMobileApplicationKey = "ss-key-value"
            rootOrganization.encryptedMobileApplicationKey = communicationOrganizationService.encryptPassword( rootOrganization.clearMobileApplicationKey )
            communicationOrganizationCompositeService.createOrganization( rootOrganization )
        } else {
            rootOrganization.name = "CommunicationSendMobileNotificationMethodIntegrationTests Organization"
            rootOrganization.mobileEndPointUrl = "http://mobiledev3.ellucian.com/colleague-internal-mobileserver/api/notification/notifications/"
            rootOrganization.mobileApplicationName = "StudentSuccess"
            rootOrganization.clearMobileApplicationKey = "ss-key-value"
            rootOrganization.encryptedMobileApplicationKey = communicationOrganizationService.encryptPassword( rootOrganization.clearMobileApplicationKey )
            communicationOrganizationCompositeService.updateOrganization( rootOrganization )
        }
    }

}