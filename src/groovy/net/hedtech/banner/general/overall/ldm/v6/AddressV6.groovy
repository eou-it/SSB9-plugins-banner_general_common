/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.overall.ldm.v6

import net.hedtech.banner.general.overall.AddressView

class AddressV6 {

    String guid
    List<String> addressLines
    PlaceV6 place
    List geographicAreas

    def AddressV6(AddressView addressView, String countryCode){
        this.guid = addressView.id
        this.addressLines = getAddressLines(addressView)
        if(countryCode) {
            CountryV6 country = new CountryV6(countryCode, addressView)
            this.place = new PlaceV6(country)
        }
    }


    List<String> getAddressLines(AddressView address) {
        List<String> addressLines = []
        if(address.addressLine1) {
            addressLines << address.addressLine1
        }
        if(address.addressLine2) {
            addressLines << address.addressLine2
        }
        if(address.addressLine3) {
            addressLines << address.addressLine3
        }
        if(address.addressLine4) {
            addressLines << address.addressLine4
        }
        if(!addressLines){
            addressLines = ["."]
        }
        return addressLines
    }
}
