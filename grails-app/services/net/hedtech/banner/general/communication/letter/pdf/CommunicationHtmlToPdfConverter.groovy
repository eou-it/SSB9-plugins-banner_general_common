package net.hedtech.banner.general.communication.letter.pdf

import org.apache.log4j.Logger
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

/**
 * Created by mbrzycki on 6/8/16.
 */
class CommunicationHtmlToPdfConverter {
    Logger log = Logger.getLogger( this.getClass() )

    public CommunicationHtmlToPdfConverter() {

    }

    public void printITextRenderer() {
        ITextRenderer renderer = new ITextRenderer()
        ITextFontResolver fontResolver = renderer.getFontResolver()
        System.out.println( "The iText renderer = ${renderer} and fontResolver = ${fontResolver}.")
    }


    public byte[] toPdf( String html ) {
        byte[] pdf = null

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream()

            ITextRenderer renderer = new ITextRenderer()
            renderer.setDocumentFromString( html )
            renderer.layout()
            renderer.createPDF( outputStream, false )
            renderer.finishPDF()
            pdf = outputStream.toByteArray()
        } catch (Exception e) {
            e.printStackTrace()
        }

        return pdf
    }

    public String toPdfString( String html ) {
        byte[] pdf = toPdf( html )
        return new String( pdf, 0, pdf.length, "UTF-8" )
    }

}
