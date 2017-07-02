/*********************************************************************************
 Copyright 2014-2017 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.mobile

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.CommunicationBaseIntegrationTestCase
import net.hedtech.banner.general.communication.organization.CommunicationEmailServerConnectionSecurity
import net.hedtech.banner.general.communication.organization.CommunicationEmailServerProperties
import net.hedtech.banner.general.communication.organization.CommunicationEmailServerPropertiesType
import net.hedtech.banner.general.communication.organization.CommunicationMailboxAccount
import net.hedtech.banner.general.communication.organization.CommunicationMailboxAccountService
import net.hedtech.banner.general.communication.organization.CommunicationMailboxAccountType
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
    def communicationOrganizationService
    def communicationOrganizationCompositeService
    def communicationMailboxAccountService
    CommunicationOrganization organization
    CommunicationSendMobileNotificationService service
    def validOrgId = 1
    def validPerson = 1

    @Before
    public void setUp() {
        super.setUseTransactions( false )
        formContext = ['GUAGMNU','SELFSERVICE']
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('BCMADMIN', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)
        super.setUp()
//        service = new CommunicationSendMobileNotificationService()
//        service.communicationOrganizationService = communicationOrganizationService

        setUpOrganization()
        //service.testOverride = [ externalUser: "amandamason1" ]
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
//        service.sendTestMobileSetup(orgID, sendTo)

    }

    protected void setUpOrganization() {
        List organizations = communicationOrganizationCompositeService.listOrganizations()
        if (organizations.size() == 0) {
            organization = new CommunicationOrganization(name: "Test Org")

            organization.name = "CommunicationSendMobileNotificationMethodIntegrationTests Organization"
            organization.mobileEndPointUrl = "http://mobiledev3.ellucian.com/colleague-internal-mobileserver/api/notification/notifications/"
            organization.mobileApplicationName = "StudentSuccess"
            organization.clearMobileApplicationKey = "ss-key-value"
            organization.encryptedMobileApplicationKey = "%encrypted%"
            organization = communicationOrganizationCompositeService.createOrganization(organization) as CommunicationOrganization
        } else {
            organization = organizations.get(0) as CommunicationOrganization
        }
    }


}

