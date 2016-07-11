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

    /**
     * Returns a portion of HTML header.
     *
     * @return String a portion of HTML header
     */
    private String getHTMLHeader() {
        StringBuilder sbTmp = new StringBuilder();
        sbTmp.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" );
        sbTmp.append( "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">" );
        sbTmp.append( "<html xmlns=\"http://www.w3.org/1999/xhtml\">" );
        sbTmp.append( "<head><title></title>" );
//        sbTmp.append( cssPageSizeMarginSettings( letterTemplate ) );
        sbTmp.append( "</head>" );
        sbTmp.append( "<body>" );
        //sbTmp.append( cssPageFooter() );

        return sbTmp.toString();
    }

    /**
     * Return a portion of HTML footer.
     *
     * @return String a portion of HTML footer
     */
    private String getHTMLFooter() {
        StringBuilder sbTmp = new StringBuilder();
        sbTmp.append( "</body></html>" );

        return sbTmp.toString();
    }

    private String getStyleDeclaration( String pageSize, String pageDimension,
                                        float topMargin, float bottomMargin, float leftMargin, float rightMargin ) {
        StringBuilder sbTmp = new StringBuilder();

        sbTmp.append( "<style type=\"text/css\">" );
        sbTmp.append( "@page { " );
        sbTmp.append( "size: " ).append( pageSize ).append( "; " );
        sbTmp.append( "margin-top: " ).append( topMargin ).append( pageDimension ).append( "; " );
        sbTmp.append( "margin-bottom: " ).append( bottomMargin ).append( pageDimension ).append( "; " );
        sbTmp.append( "margin-left: " ).append( leftMargin ).append( pageDimension ).append( "; " );
        sbTmp.append( "margin-right: " ).append( rightMargin ).append( pageDimension ).append( "; " );
        sbTmp.append( "}\n" );
        sbTmp.append( "</style>\n" );

        return sbTmp.toString();
    }

}
