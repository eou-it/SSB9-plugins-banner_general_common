/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.overall.ldm.v6

class AddressV6 {

    String guid
    List<String> addressLines
    PlaceV6 place

    def AddressV6(String guid,List<String> addressLines, String countryCode){
        this.guid=guid
        this.addressLines=addressLines
        if(countryCode) {
            CountryV6 country = new CountryV6(countryCode)
            this.place = new PlaceV6(country)
        }
    }
}
