/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall.ldm.v6;

public enum PersonVisaCategory {

    IMMIGRANT("immigrant"),
    NONIMMIGRANT("nonImmigrant")

    private final String value;

    PersonVisaCategory(String value) {
        this.value = value;
    }

    public static PersonVisaCategory getByValue(String value) {
        PersonVisaCategory val
        PersonVisaCategory.values().each {
            if (it.value == value) {
                val = it
            }
        }
        return val
    }
}