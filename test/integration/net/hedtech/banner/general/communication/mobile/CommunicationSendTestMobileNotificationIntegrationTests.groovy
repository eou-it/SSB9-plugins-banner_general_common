/*********************************************************************************
 Copyright 2014-2017 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.mobile

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.apache.log4j.Logger
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import groovy.mock.interceptor.MockFor

class CommunicationSendTestMobileNotificationIntegrationTests extends BaseIntegrationTestCase {
    def log = Logger.getLogger(this.getClass())

    CommunicationOrganization organization
    CommunicationSendMobileNotificationService service
    def validOrgId = 1
    def validSendTo = 1
    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        service = new CommunicationSendMobileNotificationService()
        super.setUp()
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('BCMADMIN', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)
        refreshOrganization()

//        def CommunicationOrganization = new MockFor(CommunicationOrganization)
//        communicationSendMobileNotificationMethod.demand.execute(1..1) {
//            CommunicationMobileNotificationMessage message, CommunicationOrganization senderOrganization ->
//                throw new ApplicationException(CommunicationSendMobileNotificationMethod.class.simpleName, "Exception")
//        }
//        service.communicationSendMobileNotificationMethod = communicationSendMobileNotificationMethod.proxyInstance()

    }

    private void refreshOrganization () {
        organization = new CommunicationOrganization()
        organization.name = "CommunicationSendMobileNotificationMethodIntegrationTests Organization"
        organization.mobileEndPointUrl = "http://mobiledev3.ellucian.com/colleague-internal-mobileserver/api/notification/notifications/"
        organization.mobileApplicationName = "StudentSuccess"
        organization.clearMobileApplicationKey = "ss-key-value"
        organization.encryptedMobileApplicationKey = "%encrypted%"
    }


    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }

    @Test
    void testSendMobile () {
        def orgID = validOrgId
        def sendTo = validPerson
        service.sendTestMobileSetup(orgId, sendTo)

    }


}

