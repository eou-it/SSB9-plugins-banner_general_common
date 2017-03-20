/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall.ldm.v8

/**
 *  Predefined constants for "daysOfWeek" inside recurrence section for instructional-events HeDM for Version 4
 *
 */
enum WeekDays {

    monday("Monday"), tuesday("Tuesday"), wednesday("Wednesday"), thursday("Thursday"), friday("Friday"), saturday("Saturday"), sunday("Sunday")

    private final String value


    WeekDays(String value) { this.value = value }


    public String getValue() { return value }


    static boolean contains(String val) {
        boolean found = false
        def obj = values().find { it.value == val }
        if (obj) {
            found = true
        }
        return found
    }

}
