package net.hedtech.banner.general.communication.letter.pdf

import grails.util.GrailsWebMockUtil
import grails.web.servlet.context.GrailsWebApplicationContext
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.grails.plugins.testing.GrailsMockHttpServletRequest
import org.grails.plugins.testing.GrailsMockHttpServletResponse
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration

/**
 * Tests converting html to pdf.
 */
@Integration
@Rollback
class CommunicationLetterPdfBuilderIntegrationTests extends BaseIntegrationTestCase {

    def selfServiceBannerAuthenticationProvider


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        webAppCtx = new GrailsWebApplicationContext()
        mockRequest()
        def authentication = selfServiceBannerAuthenticationProvider.authenticate( new UsernamePasswordAuthenticationToken( 'BCMADMIN', '111111' ) )
        SecurityContextHolder.getContext().setAuthentication( authentication )

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
    void simpleTest() {
        String htmlContent = """
            <h1>My First Heading</h1>
            &nbsp;
            <p>My first paragraph.</p>
            This is a test.
            """
        CommunicationLetterPdfBuilder converter = new CommunicationLetterPdfBuilder()
        converter.writeHtml( htmlContent )
        converter.finish()
        byte[] pdfContent = converter.toPdf()

        assertNotNull( pdfContent )
        assertTrue( new String( pdfContent ).startsWith( "%PDF" ) )
    }

}
