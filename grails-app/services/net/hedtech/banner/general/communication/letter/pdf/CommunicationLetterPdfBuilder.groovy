/*********************************************************************************
 Copyright 2016-2018 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/

package net.hedtech.banner.general.communication.letter.pdf

import groovy.util.logging.Slf4j
import net.hedtech.banner.general.communication.letter.CommunicationLetterPageSize
import net.hedtech.banner.general.communication.letter.CommunicationLetterUnitOfMeasure
import org.xhtmlrenderer.pdf.ITextRenderer

/**
 * Builds a pdf from a letter html fragment.
 */
@Slf4j
class CommunicationLetterPdfBuilder {
   // private static final log = Logger.getLogger(CommunicationLetterPdfBuilder.class)
    private ByteArrayOutputStream outputStream
    private ITextRenderer renderer
    private int documentCount
    private boolean finished
    private URL font_path

    CommunicationLetterPageSize pageSize = CommunicationLetterPageSize.LETTER
    CommunicationLetterUnitOfMeasure unitOfMeasure = CommunicationLetterUnitOfMeasure.INCH
    String topMargin = "1.0"
    String bottomMargin = "1.0"
    String leftMargin = "1.0"
    String rightMargin = "1.0"

    public CommunicationLetterPdfBuilder() {
        reset()
    }

    public void reset() {
        outputStream = new ByteArrayOutputStream()
        renderer = new ITextRenderer()
        documentCount = 0
        finished = false
    }

    public void writeHtml( String html ) {
        renderer.setDocumentFromString( getHTMLHeader() + html + getHTMLFooter() )
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

    public getXHTMLString(String content) {
        return getHTMLHeader() + content + getHTMLFooter()
    }

    /**
     * Returns a portion of HTML header.
     *
     * @return String a portion of HTML header
     */
    private String getHTMLHeader() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" );
        stringBuilder.append( "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">" );
        stringBuilder.append( "<html xmlns=\"http://www.w3.org/1999/xhtml\">" );
        stringBuilder.append( "<head><title></title>" );
        stringBuilder.append( getStyleDeclaration() );
        stringBuilder.append( "</head>" );
        stringBuilder.append( "<body>" );
        return stringBuilder.toString();
    }

    /**
     * Return a portion of HTML footer.
     *
     * @return String a portion of HTML footer
     */
    private String getHTMLFooter() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append( "</body></html>" );

        return stringBuilder.toString();
    }

    private String getStyleDeclaration() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append( "<style type=\"text/css\">" );
        stringBuilder.append( "@page { " );
        stringBuilder.append( "size: " ).append( pageSize.cssSize ).append( "; " );
        stringBuilder.append( "margin-top: " ).append( topMargin ).append( unitOfMeasure.getSymbol() ).append( "; " );
        stringBuilder.append( "margin-bottom: " ).append( bottomMargin ).append( unitOfMeasure.getSymbol() ).append( "; " );
        stringBuilder.append( "margin-left: " ).append( leftMargin ).append( unitOfMeasure.getSymbol() ).append( "; " );
        stringBuilder.append( "margin-right: " ).append( rightMargin ).append( unitOfMeasure.getSymbol() ).append( "; " );
        stringBuilder.append( "@top-left { content: element(pageHeader); } ");
        stringBuilder.append( "@bottom-left { content: element(pageFooter); } ");
        stringBuilder.append( "} " );

        stringBuilder.append( " #pageHeader{ position: running(pageHeader); } ");
        stringBuilder.append( " #pageFooter{ position: running(pageFooter); } ");
        stringBuilder.append( "</style>\n" );

        return stringBuilder.toString();
    }

}
