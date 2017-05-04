/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall.ldm

/**
 *  Predefined constants for "daysOfWeek" inside recurrence
 *
 */
enum HedmDaysOfWeek {

    MONDAY([v4: 'monday'], "Monday"),
    TUESDAY([v4: 'tuesday'], "Tuesday"),
    WEDNESDAY([v4: 'wednesday'], "Wednesday"),
    THURSDAY([v4: 'thursday'], "Thursday"),
    FRIDAY([v4: 'friday'], "Friday"),
    SATURDAY([v4: 'saturday'], "Saturday"),
    SUNDAY([v4: 'sunday'], "Sunday")


    final Map<String, String> versionToEnumMap
    final String bannerValue


    HedmDaysOfWeek(Map<String, String> versionToEnumMap,String bannerValue) {
        this.versionToEnumMap = versionToEnumMap
        this.bannerValue = bannerValue
    }


    public static HedmDaysOfWeek getByDataModelValue(String value, String version) {
        if (value) {
            Iterator itr = HedmDaysOfWeek.values().iterator()
            while (itr.hasNext()) {
                HedmDaysOfWeek hedmDaysOfWeek = itr.next()
                if (hedmDaysOfWeek.versionToEnumMap.containsKey(version) && hedmDaysOfWeek.versionToEnumMap[version].equals(value)) {
                    return hedmDaysOfWeek
                }
            }
        }
        return null
    }

    public static HedmDaysOfWeek getByBannerValue(String value) {
        Iterator itr = HedmDaysOfWeek.values().iterator()
        while (itr.hasNext()) {
            HedmDaysOfWeek hedmDaysOfWeek = itr.next()
            if (hedmDaysOfWeek.bannerValue.equals(value)) {
                return hedmDaysOfWeek
            }
        }
        return null
    }


}
