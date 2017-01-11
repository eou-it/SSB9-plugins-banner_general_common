/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.overall.ldm.v6

import net.hedtech.banner.general.overall.AddressView

import java.util.regex.Matcher
import java.util.regex.Pattern

class CountryV6 {

    String code
    String title
    String postalTitle
    RegionV6 region
    RegionV6 subRegion
    String locality
    String postalCode
    String deliveryPoint
    String carrierRoute
    String correctionDigit

    CountryV6(String iso3CountryCode, AddressView addressView, String defaultCountryTitle) {
        this.code = iso3CountryCode
        if (addressView.countryTitle) {
            this.title = addressView.countryTitle
        } else {
            this.title = defaultCountryTitle
        }

        this.postalTitle = getPostalTitleForAddress(iso3CountryCode)
        if (addressView.countryRegionCode || addressView.countryRegionTitle) {
            this.region = new RegionV6(addressView.countryRegionCode, addressView.countryRegionTitle)
        }
        if (addressView.countrySubRegionCode || addressView.countrySubRegionTitle) {
            this.subRegion = new RegionV6(addressView.countrySubRegionCode, addressView.countrySubRegionTitle)
        }
        this.locality = addressView.countryLocality
        this.postalCode = getPostalCodeForAddress(addressView, iso3CountryCode)
        this.deliveryPoint = addressView.deliveryPoint
        this.carrierRoute = addressView.carrierRoute
        this.correctionDigit = addressView.correctionDigit
    }


    private String getPostalCodeForAddress(AddressView addressView, String iso3CountryCode) {
        String pattern = getPostalCodePattern(iso3CountryCode)
        String postalCode = null
        if(pattern){
            if (isValueMatchesPattern(pattern, addressView.countryPostalCode)) {
                postalCode = addressView.countryPostalCode
            }
        } else {
            //pattern will be null for generic countries and validation not required
            postalCode = addressView.countryPostalCode
        }
        return postalCode
    }

    private boolean isValueMatchesPattern(String patternStr, String value) {
        boolean matches = false
        if (patternStr && value) {
            Pattern pattern = Pattern.compile(patternStr)
            Matcher matcher = pattern.matcher(value);
            matches = matcher.matches()
        }
        return matches
    }


    private String getPostalCodePattern(String iso3CountryCode) {
        // NULL if Generic Country
        String pattern = null
        HedmCountry hedmCountry = HedmCountry.getByString(iso3CountryCode)
        if (hedmCountry) {
            pattern = hedmCountry.postalCodePattern
        }
        return pattern
    }


    private String getPostalTitleForAddress(String iso3CountryCode) {
        // NULL if Generic Country
        String addrTitle = null
        HedmCountry hedmCountry = HedmCountry.getByString(iso3CountryCode)
        if (hedmCountry) {
            addrTitle = hedmCountry.postalTitle
        }
        return addrTitle
    }

}
