/*********************************************************************************
 Copyright 2016-2017 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/

package net.hedtech.banner.general.communication.letter.pdf

import com.lowagie.text.pdf.BaseFont
import net.hedtech.banner.general.communication.letter.CommunicationLetterPageSize
import net.hedtech.banner.general.communication.letter.CommunicationLetterUnitOfMeasure
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.io.support.ClassPathResource
import org.springframework.context.i18n.LocaleContextHolder as LCH
import org.xhtmlrenderer.pdf.ITextFontResolver
import org.xhtmlrenderer.pdf.ITextRenderer;

/**
 * Builds a pdf from a letter html fragment.
 */
class CommunicationLetterPdfBuilder {
    private static final log = Logger.getLogger(CommunicationLetterPdfBuilder.class)
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

        if (LCH.getLocale().getLanguage().equalsIgnoreCase("ar")) {
            ITextFontResolver fontResolver = renderer.getFontResolver();
            try {
                font_path = this.class.classLoader.getResource("fonts");
                File f = new File(font_path.getPath());
                if (f.isDirectory()) {
                    File[] files = f.listFiles(new FilenameFilter() {
                        public boolean accept(File dir, String name) {
                            String lower = name.toLowerCase();
                            return lower.endsWith(".otf") || lower.endsWith(".ttf");
                        }
                    });
                    for (int i = 0; i < files.length; i++) {
                        try {
                            fontResolver.addFont(files[i].getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                            log.debug("Added font " + files[i].getAbsolutePath());
                        } catch (Exception e) {
                            e.printStackTrace()
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace()
            }
        }
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
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" );
        stringBuilder.append( "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">" );
        if (LCH.getLocale().getLanguage().equalsIgnoreCase("ar")) {
            stringBuilder.append( "<html xmlns=\"http://www.w3.org/1999/xhtml\" dir=\"rtl\">" );
        } else {
            stringBuilder.append( "<html xmlns=\"http://www.w3.org/1999/xhtml\">" );
        }
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
        stringBuilder.append( "} " );

        if (LCH.getLocale().getLanguage().equalsIgnoreCase("ar")) {
            stringBuilder.append("body { font-family: Geeza Pro; } ");

            stringBuilder.append("@font-face { ");
            stringBuilder.append("src: url('");
            stringBuilder.append(new ClassPathResource("fonts").getURL().toExternalForm());
            stringBuilder.append("/geeza-pro-webfont.ttf'); ");
            stringBuilder.append("-fs-pdf-font-embed: embed; ");
            stringBuilder.append("-fs-pdf-font-encoding: Identity-H; ");
            stringBuilder.append("}\n");
        }
        stringBuilder.append( "</style>\n" );

        return stringBuilder.toString();
    }

}
