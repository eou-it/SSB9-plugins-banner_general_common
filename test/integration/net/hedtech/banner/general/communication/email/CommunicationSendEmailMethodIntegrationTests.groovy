/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.email

import com.icegreen.greenmail.util.GreenMailUtil
import net.hedtech.banner.general.communication.CommunicationBaseIntegrationTestCase
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.organization.CommunicationEmailServerConnectionSecurity
import net.hedtech.banner.general.communication.organization.CommunicationEmailServerProperties
import net.hedtech.banner.general.communication.organization.CommunicationEmailServerPropertiesType
import net.hedtech.banner.general.communication.organization.CommunicationMailboxAccount
import net.hedtech.banner.general.communication.organization.CommunicationMailboxAccountType
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import org.apache.commons.logging.LogFactory
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Test sending email using No Security settings
 */
class CommunicationSendEmailMethodIntegrationTests extends CommunicationBaseIntegrationTestCase {

    def log = LogFactory.getLog(this.class)
    def selfServiceBannerAuthenticationProvider
    CommunicationOrganization emailTestOrganization
    def userName = "bcm-sender"
    def clearTextPassword = "password"
    def localhost = "127.0.0.1"
    def receiverAddress = new CommunicationEmailAddress(
            mailAddress: "bprakash@ellucian.edu",
            displayName: "bprakash@ellucian.edu"
    )

    @Before
    public void setUp() {
        super.setUseTransactions( false )
        formContext = ['GUAGMNU','SELFSERVICE']
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('BCMADMIN', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)
        super.setUp()
        setUpEmailTestOrganization()
        mailServer.start()
    }


    @After
    public void tearDown() {
        super.tearDown()

        if (mailServer) mailServer.stop()
        sessionFactory.currentSession?.close()
        logout()
    }

    @Test
    public void testSendEmail() {

        def EMAIL_SUBJECT = "Test Subject SMTP"
        def EMAIL_TEXT = "Hello, this is test message for testing the SendEmail Method "
        //Create email message
        CommunicationEmailMessage emailMessage = new CommunicationEmailMessage();
        emailMessage.messageBody = EMAIL_TEXT
        emailMessage.subjectLine = EMAIL_SUBJECT
        emailMessage.messageBodyContentType = "text/html; charset=UTF-8";
        emailMessage.toList = [receiverAddress]

        //Override the default organization email server settings
        emailTestOrganization.sendEmailServerProperties.securityProtocol = CommunicationEmailServerConnectionSecurity.None;

        //Call the execute method of CommunicationSendEmailMethod
        CommunicationSendEmailMethod sendEmailMethod = new CommunicationSendEmailMethod( emailMessage, emailTestOrganization );
        sendEmailMethod.execute()

        def receivedMessages = mailServer.getReceivedMessages()
        assertEquals(1, receivedMessages.length);
        assertEquals(receivedMessages[0].getSubject(), EMAIL_SUBJECT);
        assertTrue(GreenMailUtil.getBody(receivedMessages[0]).contains(EMAIL_TEXT))
    }

    protected void setUpEmailTestOrganization()
    {
        emailTestOrganization = new CommunicationOrganization()
        emailTestOrganization.id = defaultOrganization.id
        emailTestOrganization.name = defaultOrganization.name
        emailTestOrganization.parent = defaultOrganization.parent
        emailTestOrganization.description = defaultOrganization.description
        emailTestOrganization.dateFormat = defaultOrganization.dateFormat
        emailTestOrganization.dayOfWeekFormat = defaultOrganization.dayOfWeekFormat
        emailTestOrganization.timeOfDayFormat = defaultOrganization.timeOfDayFormat
        emailTestOrganization.lastModifiedBy = defaultOrganization.lastModifiedBy
        emailTestOrganization.lastModified = defaultOrganization.lastModified
        emailTestOrganization.version = defaultOrganization.version
        emailTestOrganization.dataOrigin = defaultOrganization.dataOrigin

        def cesp = new CommunicationEmailServerProperties(
                securityProtocol: CommunicationEmailServerConnectionSecurity.None,
                host: localhost,
                port: smtp_port,
                organization: emailTestOrganization,
                type: CommunicationEmailServerPropertiesType.Send
        )
        emailTestOrganization.sendEmailServerProperties = cesp

        def cma = new CommunicationMailboxAccount(
                emailAddress: 'bcm-sender@ellucian.com',
                encryptedPassword: communicationMailboxAccountService.encryptPassword( clearTextPassword ),
                clearTextPassword: clearTextPassword,
                userName: userName,
                organization: defaultOrganization,
                type: CommunicationMailboxAccountType.Sender
        )
        emailTestOrganization.senderMailboxAccount = cma
    }

}