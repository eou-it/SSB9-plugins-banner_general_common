/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.overall.ldm.v6

import net.hedtech.banner.general.overall.AddressView
import net.hedtech.banner.general.overall.ldm.CountryName
import net.hedtech.banner.general.overall.ldm.CountryPostalCodePattern

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

    CountryV6(String isoCode, AddressView addressView){
        this.code = isoCode
        this.title = addressView.countryTitle
        this.postalTitle = getPostalTitleByISOCode(isoCode)
        if(addressView.countryRegionCode || addressView.countryRegionTitle) {
            this.region = new RegionV6(addressView.countryRegionCode, addressView.countryRegionTitle)
        }
        if(addressView.countrySubRegionCode || addressView.countrySubRegionTitle) {
            this.subRegion = new RegionV6(addressView.countrySubRegionCode, addressView.countrySubRegionTitle)
        }
        this.locality = addressView.countryLocality
        this.postalCode = getPostalCode(addressView,isoCode)
        this.deliveryPoint = addressView.deliveryPoint
        this.carrierRoute = addressView.carrierRoute
        this.correctionDigit = addressView.correctionDigit
    }


    String getPostalCode(AddressView addressView, String isoCode){
        if(isMatch(getPostalPatternByISOCode(isoCode), addressView.countryPostalCode)){
            return addressView.countryPostalCode
        }
        return null
    }

    boolean isMatch(String countryPattern, String postalCode) {
        if(countryPattern && postalCode) {
            Pattern pattern = Pattern.compile(countryPattern)
            Matcher matcher = pattern.matcher(postalCode);
            return matcher.matches()
        } else {
            return false
        }
    }


    String getPostalPatternByISOCode(String isoCode){
        String pattern
        Iterator postalPatterns = CountryPostalCodePattern.values().iterator()
        while(postalPatterns.hasNext()) {
            String countryCode = postalPatterns.next()
            if(countryCode.equals(isoCode)) {
                return CountryPostalCodePattern.valueOf(isoCode).getPostalCodePattern()
            }
        }
        return pattern
    }


    String getPostalTitleByISOCode(String isoCode){
        String title = null
        Iterator countryCodes = CountryName.values().iterator()
        while(countryCodes.hasNext()) {
            String countryCode = countryCodes.next()
            if(countryCode.equals(isoCode)){
                return CountryName.valueOf(isoCode).getCountryTitle()
            }
        }
        return title
    }
}
