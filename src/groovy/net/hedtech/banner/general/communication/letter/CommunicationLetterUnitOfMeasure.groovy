package net.hedtech.banner.general.communication.letter

public enum CommunicationLetterUnitOfMeasure {
    CENTIMETER ( "cm" ),
    INCH ( "in" );

    private String symbol

    private CommunicationLetterUnitOfMeasure( String symbol ) {
        this.symbol = symbol
    }

    public String getSymbol() {
        return symbol
    }
}

