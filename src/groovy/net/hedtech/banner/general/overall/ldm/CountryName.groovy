/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall.ldm


enum CountryName {

    AUS("Australia"),
    BRA("Brazil"),
    CAN("Canada"),
    MEX("Mexico"),
    NLD("Netherlands"),
    GBR("United Kingdom of Great Britain and Northern Ireland"),
    USA("United States of America")

    private String countryTitle


    CountryName(String title) {
        this.countryTitle = title
    }


    String getCountryTitle() {
        return countryTitle
    }
}
