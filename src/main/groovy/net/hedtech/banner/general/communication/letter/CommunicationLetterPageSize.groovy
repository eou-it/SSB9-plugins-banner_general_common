package net.hedtech.banner.general.communication.letter

public enum CommunicationLetterPageSize {
    LETTER ("8.5in 11in"),
    NOTE ("7.5in 10in"),
    LEGAL ("8.5in 14in"),
    TABLOID ("11in 17in"),
    LEDGER ("17in 11in"),
    EXECUTIVE ("7.25in 10.5in"),
    POSTCARD ("4in 6in"),
    A0 ("841mm 1189mm"),
    A1 ("594mm 841mm"),
    A2 ("420mm 594mm"),
    A3 ("297mm 420mm"),
    A4 ("210mm 297mm"),
    A5 ("148mm 210mm"),
    A6 ("105mm 148mm"),
    A7 ("74mm 105mm"),
    A8 ("52mm 74mm"),
    A9 ("37mm 52mm"),
    A10 ("26mm 37mm"),
    B0 ("1000mm 1414mm"),
    B1 ("707mm 1000mm"),
    B2 ("500mm 707mm"),
    B3 ("353mm 500mm"),
    B4 ("250mm 353mm"),
    B5 ("176mm 250mm"),
    B6 ("125mm 176mm"),
    B7 ("88mm 125mm"),
    B8 ("62mm 88mm"),
    B9 ("44mm 62mm"),
    B10 ("31mm 44mm");

    private String cssSize

    CommunicationLetterPageSize( String cssSize ) {
        this.cssSize = cssSize
    }

    public String getCssSize() {
        return this.cssSize
    }

}

