/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.overall.ldm.v6

interface CountryPostalCodePatternConstants {

    String AUS = "^(0[289][0-9]{2})|([1345689][0-9]{3})|(2[0-8][0-9]{2})|(290[0-9])|(291[0-4])|(7[0-4][0-9]{2})|(7[8-9][0-9]{2})\$"
    String BRA = "^\\d{5}\\-\\d{3}\$"
    String CAN = "^[ABCEGHJKLMNPRSTVXYabceghjklmnprstvxy]{1}\\d{1}[A-Za-z]{1}\\d{1}[A-Za-z]{1}\\d{1}\$"
    String MEX = "^\\A\\d{5,5}\\Z\$"
    String NLD = "^(NL-)?(\\d{4})\\s*([A-Z]{2})\$"
    String GBR = "^(GIR 0AA)|((([A-Z-[QVX]][0-9][0-9]?)|(([A-Z-[QVX]][A-Z-[IJZ]][0-9][0-9]?)|(([A-Z-[QVX]][0-9][A-HJKPSTUW])|([A-Z-[QVX]][A-Z-[IJZ]][0-9][ABEHMNPRVWXY])))) [0-9][A-Z-[CIKMOV]]{2})\$"
    String USA = "^(\\d{5}(-\\d{4})?|[A-Z]\\d[A-Z] *\\d[A-Z]\\d)\$"

}
