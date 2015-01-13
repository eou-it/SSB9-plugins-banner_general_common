/*********************************************************************************
 Copyright 2010-2013 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner

import java.text.SimpleDateFormat

/**
 * This is a helper class that is used for Date utilities
 *
 */
class DateUtility {

    /**
     * @param String dateString
     * @param String dateFormat
     */

    public static validateDateFormat = { dateString, dateFormat = null ->

        boolean result = true
        try {
            if (!dateFormat) {
                dateFormat = MessageUtility.message("default.date.format")
            }
            Calendar calendar = Calendar.instance
            Date date = Date.parse(dateFormat, dateString)
            calendar.setTime(date)
            if(calendar.get(Calendar.YEAR).toString().length() == 4 || calendar.get(Calendar.YEAR).toString().length() == 2) {
                result = dateString.equals(date.format(dateFormat)) ?: false
            } else {
                result = false
            }
        } catch (Exception e) {
            result = false
        }

        /* if (dateFormat.length() != dateString.length()) {
            result = false
         }*/
        return result
    }


    public static Date parseDateString(String dateString, String dateFormat = null) {
        if (!dateFormat) {
            dateFormat = MessageUtility.message("default.date.format")
        }
        return new SimpleDateFormat(dateFormat).parse(dateString)
    }

    public static String getDateString(Date date, String dateFormat = null) {
        if (!dateFormat) {
            dateFormat = MessageUtility.message("default.date.format")
        }
        return new SimpleDateFormat(dateFormat).format(date);
    }

    public static String formatDate(Date date, String dateFormat = null) {
        if (!dateFormat) {
               dateFormat = MessageUtility.message("default.date.format")
        }
        return new SimpleDateFormat(dateFormat).format(date)
    }

}
