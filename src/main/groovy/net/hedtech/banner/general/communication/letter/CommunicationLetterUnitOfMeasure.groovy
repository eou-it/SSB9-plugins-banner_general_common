package net.hedtech.banner.general.communication.letter

public enum CommunicationLetterUnitOfMeasure {
    MILLIMETER ( "mm" ),
    INCH ( "in" );

    private String symbol

    private CommunicationLetterUnitOfMeasure( String symbol ) {
        this.symbol = symbol
    }

    public String getSymbol() {
        return symbol
    }
}

