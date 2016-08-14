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
    private String addressTypeGuid
    private String hedmAddressType

    AddressV6(AddressView addressView, String iso3CountryCode, String defaultCountryTitle, String hedmAddressType) {
        this.guid = addressView.id
        this.addressLines = getAddressLinesForAddress(addressView)
        if (!this.addressLines) {
            addressLines = ["."]
        }
        if (iso3CountryCode) {
            CountryV6 country = new CountryV6(iso3CountryCode, addressView, defaultCountryTitle)
            this.place = new PlaceV6(country)
        }
        this.addressTypeGuid = addressView.addressTypeGuid
        this.hedmAddressType = hedmAddressType
    }

    /**
     * For 'person-guardians' V7 schema
     *
     * @return
     */
    def getType() {
        def obj
        if (hedmAddressType) {
            obj = ["addressType": hedmAddressType, "detail": ["id": addressTypeGuid]]
        }
        return obj
    }


    private List<String> getAddressLinesForAddress(AddressView addressView) {
        List<String> addressLines = []
        if (addressView.addressLine1) {
            addressLines << addressView.addressLine1
        }
        if (addressView.addressLine2) {
            addressLines << addressView.addressLine2
        }
        if (addressView.addressLine3) {
            addressLines << addressView.addressLine3
        }
        if (addressView.addressLine4) {
            addressLines << addressView.addressLine4
        }
        return addressLines
    }

}
