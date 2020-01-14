package net.hedtech.banner.general.communication.textmessage

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.util.GrailsWebMockUtil
import grails.web.servlet.context.GrailsWebApplicationContext
import net.hedtech.banner.general.communication.email.CommunicationEmailItem
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.item.CommunicationChannel
import net.hedtech.banner.general.communication.item.CommunicationSendItem
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.grails.plugins.testing.GrailsMockHttpServletRequest
import org.grails.plugins.testing.GrailsMockHttpServletResponse
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

@Integration
@Rollback
class CommunicationSendTextMessageItemIntegrationTests extends BaseIntegrationTestCase {

    def selfServiceBannerAuthenticationProvider


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
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

    @Test
    void testFetchTextMessageItem() {
        CommunicationSendTextMessageItem textMessageItem = new CommunicationSendTextMessageItem()
        textMessageItem.communicationChannel = CommunicationChannel.TEXT_MESSAGE;
        textMessageItem.referenceId = "123";
        textMessageItem.toList = "bprakash";
        textMessageItem.content = "This is a sample text message";
        textMessageItem.source = "Banner";
        textMessageItem.createdBy = "BCMADMIN";
        textMessageItem.creationDateTime = new Date();
        textMessageItem.lastModified = new Date();
        textMessageItem.lastModifiedBy = "BCMADMIN";

        textMessageItem.save(failOnError: true, flush: true);
        assertNotNull(textMessageItem.id);

        List<CommunicationSendTextMessageItem> textMessageItems = CommunicationSendItem.fetchPendingTextMessages(Integer.MAX_VALUE);
        assertNotNull(textMessageItems)
        assertEquals(1, textMessageItems.size())
    }

}
