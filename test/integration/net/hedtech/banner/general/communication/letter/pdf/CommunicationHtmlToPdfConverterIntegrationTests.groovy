package net.hedtech.banner.general.communication.letter.pdf

import com.lowagie.text.Document
import com.lowagie.text.DocumentException
import com.lowagie.text.Paragraph
import com.lowagie.text.pdf.PdfWriter
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Created by mbrzycki on 6/8/16.
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

//    @Test
//    void htmlToPdfTest() {
//        String File_To_Convert = "test.htm";
//        String url = new File(File_To_Convert).toURI().toURL().toString();
//        System.out.println(""+url);
//        String HTML_TO_PDF = "ConvertedFile.pdf";
//        OutputStream os = new FileOutputStream(HTML_TO_PDF);
//        ITextRenderer renderer = new ITextRenderer();
//        renderer.setDocument(url);
//        renderer.layout();
//        renderer.createPDF(os);
//        os.close();
//    }

    @Test
    void simpleTest() {
        String htmlContent =
"""<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <title>
                Generated By Ellucian Communication Management
        </title>
        <style type="text/css">
        @page { size: A4;  margin-top: 1in; margin-bottom: 1in; margin-left: 1in; margin-right: 1in; }
        </style>
    </head>
    <body>
        <h1>My First Heading</h1>
        <p>My first paragraph.</p>
        This is a test.
    </body>
</html>
"""
        CommunicationHtmlToPdfConverter converter = new CommunicationHtmlToPdfConverter()
        byte[] pdfContent = converter.toPdf( htmlContent )
        assertNotNull( pdfContent )
        assertTrue( new String( pdfContent ).startsWith( "%PDF" ) )

//        File file = new File( "simpleTest.pdf" )
//        file.setBytes( pdfContent )

        String pdfContentAsString = converter.toPdfString( htmlContent ).replace( "iText", "BCM Rules!" )
        assertNotNull( pdfContentAsString )
        assertTrue( pdfContentAsString.startsWith( "%PDF" ) )
//        File file2 = new File( "simpleTest2.pdf" )
//        file2.setBytes( pdfContent )
    }

}