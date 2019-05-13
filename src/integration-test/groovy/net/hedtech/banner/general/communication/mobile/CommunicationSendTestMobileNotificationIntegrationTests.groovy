/*********************************************************************************
 Copyright 2014-2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.mobile

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.util.GrailsWebMockUtil
import grails.web.servlet.context.GrailsWebApplicationContext
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.apache.log4j.Logger
import org.grails.plugins.testing.GrailsMockHttpServletRequest
import org.grails.plugins.testing.GrailsMockHttpServletResponse
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

@Integration
@Rollback
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
        webAppCtx = new GrailsWebApplicationContext()
        mockRequest()
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('BCMADMIN', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)
        super.setUp()
    }

    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }

    public GrailsWebRequest mockRequest() {
        GrailsMockHttpServletRequest mockRequest = new GrailsMockHttpServletRequest();
        GrailsMockHttpServletResponse mockResponse = new GrailsMockHttpServletResponse();
        GrailsWebMockUtil.bindMockWebRequest(webAppCtx, mockRequest, mockResponse)
    }

    void setUpData() {
        service = new CommunicationSendMobileNotificationService()
        service.communicationOrganizationService = communicationOrganizationService

        setUpOrganization()
        service.testOverride = [ externalUser: "amandamason1" ]
    }

    @Test
    void testSendMobile () {
        setUpData()
        def orgID = organization.id
        def sendTo = validPerson
        service.sendTest(orgID, sendTo, messageData)
    }

    @Test
    void testSendMobileInvalidOrgId () {
        setUpData()
        thrown.expect(ApplicationException)
        thrown.expectMessage( ('communication.error.message.organizationNotFound'))
        def orgID = -1
        def sendTo = validPerson
        service.sendTest(orgID, sendTo, messageData)
    }


    @Test
    void testSendMobileInvalidPerson () {
        setUpData()
        thrown.expect(ApplicationException)
        thrown.expectMessage( ('communication.error.message.externalUserInvalid'))
        def orgID = organization.id
        def sendTo = -1
        service.sendTest(orgID, sendTo, messageData)
    }

    @Test
    void testSendMobileNoEndpointUrl () {
        setUpData()
        thrown.expect(ApplicationException)
        thrown.expectMessage( ('communication.error.message.mobileEndPointUrlNotFound'))
        organization.mobileEndPointUrl = null
        def orgID = organization.id
        def sendTo = validPerson
        service.sendTest(orgID, sendTo, messageData)
        organization.mobileEndPointUrl = "http://mobiledev1.ellucian.com/colleague-internal-mobileserver/api/notification/notifications/"
    }

    @Test
    void testSendMobileApplicationKeyIncorrect () {
        setUpData()
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
            organization.mobileEndPointUrl = "http://mobiledev1.ellucian.com/colleague-internal-mobileserver/api/notification/notifications/"
            organization.mobileApplicationName = "StudentSuccess"
            organization.clearMobileApplicationKey = "ss-key-value"
            organization.encryptedMobileApplicationKey = communicationMailboxAccountService.encryptPassword("ss-key-value")
            organization = communicationOrganizationCompositeService.createOrganization(organization) as CommunicationOrganization
        } else {
            organization = CommunicationOrganization.fetchRoot() as CommunicationOrganization
            organization.name = "CommunicationSendMobileNotificationMethodIntegrationTests Organization"
            organization.mobileEndPointUrl = "http://mobiledev1.ellucian.com/colleague-internal-mobileserver/api/notification/notifications/"
            organization.mobileApplicationName = "StudentSuccess"
            organization.clearMobileApplicationKey = "ss-key-value"
            organization.encryptedMobileApplicationKey = communicationMailboxAccountService.encryptPassword("ss-key-value")
            communicationOrganizationCompositeService.updateOrganization(organization)
        }
    }


}

