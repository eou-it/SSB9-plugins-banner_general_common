package net.hedtech.banner.general.communication.letter

import com.lowagie.text.Document
import com.lowagie.text.DocumentException
import com.lowagie.text.Paragraph
import com.lowagie.text.pdf.PdfWriter
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Created by mbrzycki on 6/8/16.
 */
class CommunicationLetterPreviewServiceIntegrationTests {

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
    void directPdf() {
        Document document = new Document();
//        OutputStream os = new File( "direct.pdf" ).newOutputStream()
//        try {
//            PdfWriter.getInstance( document, os );
//            document.open();
//            document.add(new Paragraph("Hello world"));
//            document.close();
//        } catch (DocumentException e) { //handle the error
//            e.printStackTrace()
//        } finally {
//            os.close()
//        }
    }

}
