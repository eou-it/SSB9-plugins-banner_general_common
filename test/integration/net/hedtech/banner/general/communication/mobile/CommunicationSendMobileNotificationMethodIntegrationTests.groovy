/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.mobile

import net.hedtech.banner.general.communication.CommunicationBaseIntegrationTestCase
import net.hedtech.banner.general.communication.organization.*
import org.apache.commons.logging.LogFactory
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Test sending basic mobile notification.
 */
class CommunicationSendMobileNotificationMethodIntegrationTests extends CommunicationBaseIntegrationTestCase {

    def log = LogFactory.getLog(this.class)
    def selfServiceBannerAuthenticationProvider
    CommunicationOrganization testOrganization


    @Before
    public void setUp() {
        super.setUseTransactions( false )
        formContext = ['SELFSERVICE']
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('BCMADMIN', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)
        super.setUp()
        setUpTestOrganization()
    }


    @After
    public void tearDown() {
        super.tearDown()
        if (mailServer) mailServer.stop()
        sessionFactory.currentSession?.close()
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

        message.externalUser = "cmobile"
        message.push = true

        CommunicationSendMobileNotificationMethod sendMethod = new CommunicationSendMobileNotificationMethod()
        sendMethod.execute( message, testOrganization )
    }


    protected void setUpTestOrganization() {
        testOrganization = new CommunicationOrganization()
        testOrganization.id = defaultOrganization.id
        testOrganization.name = defaultOrganization.name
        testOrganization.parent = defaultOrganization.parent
        testOrganization.description = defaultOrganization.description
        testOrganization.dateFormat = defaultOrganization.dateFormat
        testOrganization.dayOfWeekFormat = defaultOrganization.dayOfWeekFormat
        testOrganization.timeOfDayFormat = defaultOrganization.timeOfDayFormat
        testOrganization.lastModifiedBy = defaultOrganization.lastModifiedBy
        testOrganization.lastModified = defaultOrganization.lastModified
        testOrganization.version = defaultOrganization.version
        testOrganization.dataOrigin = defaultOrganization.dataOrigin

        testOrganization.mobileEndPointUrl = "https://mobiledev1.ellucian.com/banner-mobileserver/"
        testOrganization.mobileApplicationName = "StudentSuccess"
        testOrganization.clearMobileApplicationKey = "ss-key-value"
        testOrganization.encryptedMobileApplicationKey = communicationOrganizationService.encryptPassword( testOrganization.clearMobileApplicationKey )
    }

}