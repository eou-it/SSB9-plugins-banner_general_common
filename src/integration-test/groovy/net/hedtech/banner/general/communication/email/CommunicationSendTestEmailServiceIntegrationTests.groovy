/*******************************************************************************
 Copyright 2015-2017 Ellucian Company L.P. and its affiliates.
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.junit.Rule;
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.rules.ExpectedException;

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

    @Test  // works and doesnt throw exception
    public void testSendTestEmail() {
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
    }

    @Test
    public void testSendTestEmailNullServer() {
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
    }

    @Test
    public void testSendTestEmailInvalidServer() {
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
    }

    @Test
    public void testSendTestEmailInvalidPort() {
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
    }


    // TODO: find out how to make incorrect username or password reject
    @Test
    public void testSendTestEmailInvalidPassword() {
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
    }

    @Test
    public void testSendTestEmailInvalidAccount() {
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
    }

    @Test
    public void testSendTestEmailInvalidEmail() {
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
    }

    @Test
    public void testSendTestEmailNullEmail() {
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
    }

    @Test
    public void testSendTestEmailNullReceiverDisplayName() {
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
                encryptedPassword: null,
                clearTextPassword: clearTextPassword,
                userName: userName,
                organization: defaultOrganization,
                type: CommunicationMailboxAccountType.Sender
        )
        emailTestOrganization.senderMailboxAccount = cma
    }
}
