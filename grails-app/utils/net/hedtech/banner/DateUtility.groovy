/** *****************************************************************************
 � 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
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

    public static validateDateFormat = {dateString, dateFormat = null ->

        boolean result = true
        try {
            if (!dateFormat) {
                dateFormat = MessageUtility.message("default.date.format")
            }
            result = dateString.equals(Date.parse(dateFormat, dateString).format(dateFormat)) ?: false
        } catch (Exception e) {
            result = false
        }

        if (dateFormat.length() != dateString.length()) {
            result = false
        }
        return result
    }


    public static Date parseDateString(String dateString, String dateFormat = null) {
        if (!dateFormat) {
               dateFormat = MessageUtility.message("default.date.format")
        }
        return new SimpleDateFormat(dateFormat).parse(dateString)
    }


}
