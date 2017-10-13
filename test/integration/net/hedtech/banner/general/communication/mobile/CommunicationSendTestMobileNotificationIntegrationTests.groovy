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
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

class CommunicationSendTestMobileNotificationIntegrationTests extends BaseIntegrationTestCase {
    @Rule
    public ExpectedException thrown = ExpectedException.none()

    def log = Logger.getLogger(this.getClass())
    def communicationOrganizationService
    def communicationOrganizationCompositeService
    def communicationMailboxAccountService
    CommunicationOrganization organization
    CommunicationSendMobileNotificationService service
    def validPerson = 1
    def messageData = [description:"message desc", headline:"message headline", mobileHeadline:"message mobileHeadline"]

    @Before
    public void setUp() {
        super.setUseTransactions( false )
        formContext = ['GUAGMNU','SELFSERVICE']
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('BCMADMIN', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)
        super.setUp()
        service = new CommunicationSendMobileNotificationService()
        service.communicationOrganizationService = communicationOrganizationService

        setUpOrganization()
        service.testOverride = [ externalUser: "amandamason1" ]
    }

    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }

    @Test
    void testSendMobile () {
        def orgID = organization.id
        def sendTo = validPerson
        service.sendTest(orgID, sendTo, messageData)
    }

    @Test
    void testSendMobileInvalidOrgId () {
        thrown.expect(ApplicationException)
        thrown.expectMessage( ('communication.error.message.organizationNotFound'))
        def orgID = -1
        def sendTo = validPerson
        service.sendTest(orgID, sendTo, messageData)
    }


    @Test
    void testSendMobileInvalidPerson () {
        thrown.expect(ApplicationException)
        thrown.expectMessage( ('communication.error.message.externalUserInvalid'))
        def orgID = organization.id
        def sendTo = -1
        service.sendTest(orgID, sendTo, messageData)
    }

    @Test
    void testSendMobileNoEndpointUrl () {
        thrown.expect(ApplicationException)
        thrown.expectMessage( ('communication.error.message.mobileEndPointUrlNotFound'))
        organization.mobileEndPointUrl = null
        def orgID = organization.id
        def sendTo = validPerson
        service.sendTest(orgID, sendTo, messageData)
        organization.mobileEndPointUrl = "http://mobiledev3.ellucian.com/colleague-internal-mobileserver/api/notification/notifications/"
    }

    @Test
    void testSendMobileApplicationKeyIncorrect () {
        thrown.expect(ApplicationException)
        thrown.expectMessage( ('communication.error.message.unauthorizedMobile'))

        organization.clearMobileApplicationKey = "incorrect"
        organization.encryptedMobileApplicationKey = communicationMailboxAccountService.encryptPassword("incorrect")

        def orgID = organization.id
        def sendTo = validPerson
        service.sendTest(orgID, sendTo, messageData)
        organization.clearMobileApplicationKey = "ss-key-value"
        organization.encryptedMobileApplicationKey = communicationMailboxAccountService.encryptPassword("ss-key-value")
    }

    protected void setUpOrganization() {
        List organizations = communicationOrganizationCompositeService.listOrganizations()
        if (organizations.size() == 0) {
            organization = new CommunicationOrganization(name: "Test Org")

            organization.name = "CommunicationSendMobileNotificationMethodIntegrationTests Organization"
            organization.mobileEndPointUrl = "http://mobiledev3.ellucian.com/colleague-internal-mobileserver/api/notification/notifications/"
            organization.mobileApplicationName = "StudentSuccess"
            organization.clearMobileApplicationKey = "ss-key-value"
            organization.encryptedMobileApplicationKey = communicationMailboxAccountService.encryptPassword("ss-key-value")
            organization = communicationOrganizationCompositeService.createOrganization(organization) as CommunicationOrganization
        } else {
            organization = organizations.get(0) as CommunicationOrganization
            organization.name = "CommunicationSendMobileNotificationMethodIntegrationTests Organization"
            organization.mobileEndPointUrl = "http://mobiledev3.ellucian.com/colleague-internal-mobileserver/api/notification/notifications/"
            organization.mobileApplicationName = "StudentSuccess"
            organization.clearMobileApplicationKey = "ss-key-value"
            organization.encryptedMobileApplicationKey = communicationMailboxAccountService.encryptPassword("ss-key-value")
            communicationOrganizationCompositeService.updateOrganization(organization)
        }
    }


}

