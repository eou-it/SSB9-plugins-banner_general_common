/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall.ldm.v6;

public enum PersonVisaStatus {

    CURRENT("current"),
    EXPIRED("expired")

    private final String value;

    PersonVisaStatus(String value) {
        this.value = value;
    }

    public static PersonVisaStatus getByValue(String value) {
        PersonVisaStatus val
        PersonVisaStatus.values().each {
            if (it.value == value) {
                val = it
            }
        }
        return val
    }
}