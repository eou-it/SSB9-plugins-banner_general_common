package net.hedtech.banner.general.communication.letter.pdf

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Tests converting html to pdf.
 */
class CommunicationHtmlToPdfConverterIntegrationTests extends BaseIntegrationTestCase {

    def selfServiceBannerAuthenticationProvider


    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']
        def authentication = selfServiceBannerAuthenticationProvider.authenticate( new UsernamePasswordAuthenticationToken( 'BCMADMIN', '111111' ) )
        SecurityContextHolder.getContext().setAuthentication( authentication )
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }

    @Test
    void simpleTest() {
        String htmlContent = """
<h1>My First Heading</h1>
<p>My first paragraph.</p>
This is a test.
"""
        CommunicationHtmlToPdfConverter converter = new CommunicationHtmlToPdfConverter()
        converter.writeHtml( htmlContent )
        converter.finish()
        byte[] pdfContent = converter.toPdf()

        assertNotNull( pdfContent )
        assertTrue( new String( pdfContent ).startsWith( "%PDF" ) )
    }

}
