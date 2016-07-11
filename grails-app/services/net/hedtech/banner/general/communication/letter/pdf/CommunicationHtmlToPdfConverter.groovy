package net.hedtech.banner.general.communication.letter.pdf

import org.apache.log4j.Logger
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

/**
 * Created by mbrzycki on 6/8/16.
 */
class CommunicationHtmlToPdfConverter {
    Logger log = Logger.getLogger( this.getClass() )
    ByteArrayOutputStream outputStream
    ITextRenderer renderer
    int documentCount
    boolean finished

    public CommunicationHtmlToPdfConverter() {
        reset()
    }

    public void reset() {
        outputStream = new ByteArrayOutputStream()
        renderer = new ITextRenderer()
        documentCount = 0
        finished = false
    }

    public void writeHtml( String html ) {
        renderer.setDocumentFromString( html )
        renderer.layout()

        if (documentCount == 0) {
            renderer.createPDF( outputStream, false )
        } else {
            renderer.writeNextDocument()
        }
        documentCount++
    }

    public void finish() {
        renderer.finishPDF()
        finished = true
    }

    public byte[] toPdf() {
        assert( finished )
        return outputStream.toByteArray()
    }

}
