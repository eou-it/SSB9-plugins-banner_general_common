/*******************************************************************************
 Copyright 2015-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.email

import com.icegreen.greenmail.util.GreenMailUtil
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.util.GrailsWebMockUtil
import grails.web.servlet.context.GrailsWebApplicationContext
import net.hedtech.banner.general.communication.CommunicationBaseIntegrationTestCase
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.organization.CommunicationEmailServerConnectionSecurity
import net.hedtech.banner.general.communication.organization.CommunicationEmailServerProperties
import net.hedtech.banner.general.communication.organization.CommunicationEmailServerPropertiesType
import net.hedtech.banner.general.communication.organization.CommunicationMailboxAccount
import net.hedtech.banner.general.communication.organization.CommunicationMailboxAccountType
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import org.apache.commons.logging.LogFactory
import org.grails.plugins.testing.GrailsMockHttpServletRequest
import org.grails.plugins.testing.GrailsMockHttpServletResponse
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.junit.Rule;
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.rules.ExpectedException;

@Integration
@Rollback
class CommunicationSendTestEmailServiceIntegrationTests extends CommunicationBaseIntegrationTestCase {

    @Rule
    public ExpectedException thrown = ExpectedException.none()
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
        formContext = ['GUAGMNU','SELFSERVICE']
        super.setUp()
        webAppCtx = new GrailsWebApplicationContext()
        mockRequest()
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('BCMADMIN', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)
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

/*
    void setUpData() {
        setUpEmailTestOrganization()
        mailServer.start()
    }

    void rollBackData() {
        if (mailServer) mailServer.stop()
    }

    @Test  // works and doesnt throw exception
    public void testSendTestEmail() {
        setUpData()
        // use receiverAddress for receiver
        def EMAIL_SUBJECT = "Test Subject SMTP"
        def EMAIL_TEXT = "Hello, this is test message for testing the SendEmail Method "
        //Create email message
        CommunicationEmailMessage emailMessage = new CommunicationEmailMessage();
        emailMessage.messageBody = EMAIL_TEXT
        emailMessage.subjectLine = EMAIL_SUBJECT
        emailMessage.messageBodyContentType = "text/html; charset=UTF-8";
        emailMessage.toList = [receiverAddress]
        emailTestOrganization.sendEmailServerProperties.securityProtocol = CommunicationEmailServerConnectionSecurity.None;

        CommunicationSendEmailService service = new CommunicationSendEmailService()
        service.sendTestImpl(emailTestOrganization, receiverAddress, [body:EMAIL_TEXT, subject:EMAIL_SUBJECT])

        def receivedMessages = mailServer.getReceivedMessages()

        assertEquals(1, receivedMessages.length)
        assertEquals(receivedMessages[0].getSubject(), EMAIL_SUBJECT)
        assertTrue(GreenMailUtil.getBody(receivedMessages[0]).contains(EMAIL_TEXT))
        rollBackData()
    }

    @Test
    public void testSendTestEmailNullServer() {
        setUpData()
        thrown.expect(ApplicationException)
        thrown.expectMessage( ('communication.error.message.server.hostNotFound'))

        emailTestOrganization.sendEmailServerProperties.host = null

        // use receiverAddress for receiver
        def EMAIL_SUBJECT = "Test Subject SMTP"
        def EMAIL_TEXT = "Hello, this is test message for testing the SendEmail Method "
        //Create email message
        CommunicationEmailMessage emailMessage = new CommunicationEmailMessage();
        emailMessage.messageBody = EMAIL_TEXT
        emailMessage.subjectLine = EMAIL_SUBJECT
        emailMessage.messageBodyContentType = "text/html; charset=UTF-8";
        emailMessage.toList = [receiverAddress]
        emailTestOrganization.sendEmailServerProperties.securityProtocol = CommunicationEmailServerConnectionSecurity.None;

        CommunicationSendEmailService service = new CommunicationSendEmailService()
        service.sendTestImpl(emailTestOrganization, receiverAddress, [body:EMAIL_TEXT, subject:EMAIL_SUBJECT])

        def receivedMessages = mailServer.getReceivedMessages()

        assertEquals(0, receivedMessages.length)
        rollBackData()
    }

    @Test
    public void testSendTestEmailInvalidServer() {
        setUpData()
        thrown.expect(ApplicationException)
        thrown.expectMessage( "communication.error.message.server.failed")
        emailTestOrganization.sendEmailServerProperties.host = 'invalidHostName'
        // use receiverAddress for receiver

        // use receiverAddress for receiver
        def EMAIL_SUBJECT = "Test Subject SMTP"
        def EMAIL_TEXT = "Hello, this is test message for testing the SendEmail Method "
        //Create email message
        CommunicationEmailMessage emailMessage = new CommunicationEmailMessage();
        emailMessage.messageBody = EMAIL_TEXT
        emailMessage.subjectLine = EMAIL_SUBJECT
        emailMessage.messageBodyContentType = "text/html; charset=UTF-8";
        emailMessage.toList = [receiverAddress]
        emailTestOrganization.sendEmailServerProperties.securityProtocol = CommunicationEmailServerConnectionSecurity.None;

        CommunicationSendEmailService service = new CommunicationSendEmailService()
        service.sendTestImpl(emailTestOrganization, receiverAddress, [body:EMAIL_TEXT, subject:EMAIL_SUBJECT])

        def receivedMessages = mailServer.getReceivedMessages()

        assertEquals(1, receivedMessages.length)
        assertEquals(receivedMessages[0].getSubject(), EMAIL_SUBJECT)
        assertTrue(GreenMailUtil.getBody(receivedMessages[0]).contains(EMAIL_TEXT))
        rollBackData()
    }

    @Test
    public void testSendTestEmailInvalidPort() {
        setUpData()
        thrown.expect(ApplicationException)
        thrown.expectMessage( "communication.error.message.server.failed")
        emailTestOrganization.sendEmailServerProperties.port += 1
        // use receiverAddress for receiver

        // use receiverAddress for receiver
        def EMAIL_SUBJECT = "Test Subject SMTP"
        def EMAIL_TEXT = "Hello, this is test message for testing the SendEmail Method "
        //Create email message
        CommunicationEmailMessage emailMessage = new CommunicationEmailMessage();
        emailMessage.messageBody = EMAIL_TEXT
        emailMessage.subjectLine = EMAIL_SUBJECT
        emailMessage.messageBodyContentType = "text/html; charset=UTF-8";
        emailMessage.toList = [receiverAddress]
        emailTestOrganization.sendEmailServerProperties.securityProtocol = CommunicationEmailServerConnectionSecurity.None;

        CommunicationSendEmailService service = new CommunicationSendEmailService()
        service.sendTestImpl(emailTestOrganization, receiverAddress, [body:EMAIL_TEXT, subject:EMAIL_SUBJECT])

        def receivedMessages = mailServer.getReceivedMessages()
        assertEquals(0, receivedMessages.length)
        rollBackData()
    }


    // TODO: find out how to make incorrect username or password reject
    @Test
    public void testSendTestEmailInvalidPassword() {
        setUpData()
        // use receiverAddress for receiver
        def EMAIL_SUBJECT = "Test Subject SMTP"
        def EMAIL_TEXT = "Hello, this is test message for testing the SendEmail Method "
        //Create email message
        CommunicationEmailMessage emailMessage = new CommunicationEmailMessage();
        emailMessage.messageBody = EMAIL_TEXT
        emailMessage.subjectLine = EMAIL_SUBJECT
        emailMessage.messageBodyContentType = "text/html; charset=UTF-8";
        emailMessage.toList = [receiverAddress]
        emailTestOrganization.sendEmailServerProperties.securityProtocol = CommunicationEmailServerConnectionSecurity.None;

        CommunicationSendEmailService service = new CommunicationSendEmailService()
        service.sendTestImpl(emailTestOrganization, receiverAddress, [body:EMAIL_TEXT, subject:EMAIL_SUBJECT])

        def receivedMessages = mailServer.getReceivedMessages()

        assertEquals(1, receivedMessages.length)
        assertEquals(receivedMessages[0].getSubject(), EMAIL_SUBJECT)
        assertTrue(GreenMailUtil.getBody(receivedMessages[0]).contains(EMAIL_TEXT))
        rollBackData()
    }

    @Test
    public void testSendTestEmailInvalidAccount() {
        setUpData()
        // use receiverAddress for receiver
        def EMAIL_SUBJECT = "Test Subject SMTP"
        def EMAIL_TEXT = "Hello, this is test message for testing the SendEmail Method "
        //Create email message
        CommunicationEmailMessage emailMessage = new CommunicationEmailMessage();
        emailMessage.messageBody = EMAIL_TEXT
        emailMessage.subjectLine = EMAIL_SUBJECT
        emailMessage.messageBodyContentType = "text/html; charset=UTF-8";
        emailMessage.toList = [receiverAddress]
        emailTestOrganization.sendEmailServerProperties.securityProtocol = CommunicationEmailServerConnectionSecurity.None;

        CommunicationSendEmailService service = new CommunicationSendEmailService()
        service.sendTestImpl(emailTestOrganization, receiverAddress, [body:EMAIL_TEXT, subject:EMAIL_SUBJECT])

        def receivedMessages = mailServer.getReceivedMessages()

        assertEquals(1, receivedMessages.length)
        assertEquals(receivedMessages[0].getSubject(), EMAIL_SUBJECT)
        assertTrue(GreenMailUtil.getBody(receivedMessages[0]).contains(EMAIL_TEXT))
        rollBackData()
    }

    @Test
    public void testSendTestEmailInvalidEmail() {
        setUpData()
        thrown.expect(ApplicationException)
        thrown.expectMessage('communication.error.message.invalidEmailGeneral')

        emailTestOrganization.senderMailboxAccount.emailAddress = 'invalid email'
        // use receiverAddress for receiver

        // use receiverAddress for receiver
        def EMAIL_SUBJECT = "Test Subject SMTP"
        def EMAIL_TEXT = "Hello, this is test message for testing the SendEmail Method "
        //Create email message
        CommunicationEmailMessage emailMessage = new CommunicationEmailMessage();
        emailMessage.messageBody = EMAIL_TEXT
        emailMessage.subjectLine = EMAIL_SUBJECT
        emailMessage.messageBodyContentType = "text/html; charset=UTF-8";
        emailMessage.toList = [receiverAddress]
        emailTestOrganization.sendEmailServerProperties.securityProtocol = CommunicationEmailServerConnectionSecurity.None;

        CommunicationSendEmailService service = new CommunicationSendEmailService()
        service.sendTestImpl(emailTestOrganization, receiverAddress, [body:EMAIL_TEXT, subject:EMAIL_SUBJECT])

        def receivedMessages = mailServer.getReceivedMessages()

        assertEquals(0, receivedMessages.length)
        rollBackData()
    }

    @Test
    public void testSendTestEmailNullEmail() {
        setUpData()
        thrown.expect(ApplicationException)
        thrown.expectMessage('communication.error.message.senderMailbox.emptySenderEmail')
        emailTestOrganization.senderMailboxAccount.emailAddress = null
        // use receiverAddress for receiver

        // use receiverAddress for receiver
        def EMAIL_SUBJECT = "Test Subject SMTP"
        def EMAIL_TEXT = "Hello, this is test message for testing the SendEmail Method "
        //Create email message
        CommunicationEmailMessage emailMessage = new CommunicationEmailMessage();
        emailMessage.messageBody = EMAIL_TEXT
        emailMessage.subjectLine = EMAIL_SUBJECT
        emailMessage.messageBodyContentType = "text/html; charset=UTF-8";
        emailMessage.toList = [receiverAddress]
        emailTestOrganization.sendEmailServerProperties.securityProtocol = CommunicationEmailServerConnectionSecurity.None;

        CommunicationSendEmailService service = new CommunicationSendEmailService()
        service.sendTestImpl(emailTestOrganization, receiverAddress, [body:EMAIL_TEXT, subject:EMAIL_SUBJECT])

        def receivedMessages = mailServer.getReceivedMessages()

        assertEquals(0, receivedMessages.length)
        rollBackData()
    }

    @Test
    public void testSendTestEmailNullReceiverDisplayName() {
        setUpData()
        receiverAddress.displayName = null
        // use receiverAddress for receiver

        // use receiverAddress for receiver
        def EMAIL_SUBJECT = "Test Subject SMTP"
        def EMAIL_TEXT = "Hello, this is test message for testing the SendEmail Method "
        //Create email message
        CommunicationEmailMessage emailMessage = new CommunicationEmailMessage();
        emailMessage.messageBody = EMAIL_TEXT
        emailMessage.subjectLine = EMAIL_SUBJECT
        emailMessage.messageBodyContentType = "text/html; charset=UTF-8";
        emailMessage.toList = [receiverAddress]
        emailTestOrganization.sendEmailServerProperties.securityProtocol = CommunicationEmailServerConnectionSecurity.None;

        CommunicationSendEmailService service = new CommunicationSendEmailService()
        service.sendTestImpl(emailTestOrganization, receiverAddress, [body:EMAIL_TEXT, subject:EMAIL_SUBJECT])

        def receivedMessages = mailServer.getReceivedMessages()

        assertEquals(1, receivedMessages.length)
        assertEquals(receivedMessages[0].getSubject(), EMAIL_SUBJECT)
        assertTrue(GreenMailUtil.getBody(receivedMessages[0]).contains(EMAIL_TEXT))

        rollBackData()
    }

//    @Test
//    public void testSendTestEmailSetupNullEmail() {
//        thrown.expect(ApplicationException)
//        thrown.expectMessage('communication.error.message.invalidReceiverEmail')
//        emailTestOrganization.senderMailboxAccount.emailAddress = null
//        // use receiverAddress for receiver
//        String str = null
//        service.sendTest(1, str)
//        def receivedMessages = mailServer.getReceivedMessages()
//
//        assertEquals(0, receivedMessages.length)
//    }
//
//    @Test
//    public void testSendTestEmailSetupEmail() {
//        thrown.expect(ApplicationException)
//        thrown.expectMessage('communication.error.message.organizationNotFound')
//        // use receiverAddress for receiver
//
//        service.sendTest(-1, receiverAddress.mailAddress)
//        def receivedMessages = mailServer.getReceivedMessages()
//
//        assertEquals(0, receivedMessages.length)
//    }

    protected void setUpEmailTestOrganization()
    {
        List organizations = communicationOrganizationCompositeService.listOrganizations()
        if (organizations.size() == 0) {
            emailTestOrganization = new CommunicationOrganization(name: "Test Org")
        }
//        emailTestOrganization = new CommunicationOrganization(name: "Test Org")
//        emailTestOrganization.id = defaultOrganization.id
//        emailTestOrganization.name = defaultOrganization.name
//        emailTestOrganization.parent = defaultOrganization.parent
//        emailTestOrganization.description = defaultOrganization.description
//        emailTestOrganization.dateFormat = defaultOrganization.dateFormat
//        emailTestOrganization.dayOfWeekFormat = defaultOrganization.dayOfWeekFormat
//        emailTestOrganization.timeOfDayFormat = defaultOrganization.timeOfDayFormat
//        emailTestOrganization.lastModifiedBy = defaultOrganization.lastModifiedBy
//        emailTestOrganization.lastModified = defaultOrganization.lastModified
//        emailTestOrganization.version = defaultOrganization.version
//        emailTestOrganization.dataOrigin = defaultOrganization.dataOrigin

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
                encryptedPassword: null,
                clearTextPassword: clearTextPassword,
                userName: userName,
                organization: emailTestOrganization,
                type: CommunicationMailboxAccountType.Sender
        )
        emailTestOrganization.senderMailboxAccount = cma
    }
*/

}
