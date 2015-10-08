/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication

import net.hedtech.banner.i18n.DateConverterService

import java.text.SimpleDateFormat

/**
 * Service extends the common date converterservice.  This is temporary
 */
class CommunicationDateConverterService extends DateConverterService {


    public parseGregorianToDefaultCalendar(value, dateformat) {

        try {
            if(value instanceof Date) {
                SimpleDateFormat sdf = new SimpleDateFormat(localizerService(code: dateformat));
                value = sdf.format(new Date(value.getTime()));
            }
            String defaultDateFormat = localizerService(code: dateformat)
            def tempValue = convert(value,
                    getULocaleStringForCalendar('gregorian'),
                    getULocaleTranslationStringForCalendar(localizerService(code: "default.calendar",default:'gregorian')),
                    defaultDateFormat ,
                    defaultDateFormat)

            //Check if date passed is valid or not.
            def checkValue = convert(tempValue,
                    getULocaleTranslationStringForCalendar(localizerService(code: "default.calendar",default:'gregorian')),
                    getULocaleStringForCalendar('gregorian'),
                    defaultDateFormat ,
                    defaultDateFormat)

            if(tempValue != "error") {
                if(value.equals(checkValue)) {
                    //Date is valid
                    value = tempValue
                }
            }
        } catch (Exception e) {
            //If an exception occurs ignore and return original value.
        }
        return value
    }

    public parseDefaultCalendarToGregorian(value, dateformat) {
        String defaultDateFormat = localizerService(code: dateformat)
        if (! isGregorianDate(value, defaultDateFormat)) {
            try {
                def tempValue = convert(value,
                        getULocaleTranslationStringForCalendar(localizerService(code: "default.calendar", default: 'gregorian')),
                        getULocaleStringForCalendar('gregorian'),
                        defaultDateFormat,
                        defaultDateFormat)
                //Check if date passed is valid or not.
                def checkValue = convert(tempValue,
                        getULocaleStringForCalendar('gregorian'),
                        getULocaleTranslationStringForCalendar(localizerService(code: "default.calendar", default: 'gregorian')),
                        defaultDateFormat,
                        defaultDateFormat)
                if (tempValue != "error") {
                    if (value.equals(checkValue)) {
                        //Date is valid
                        value = tempValue
                    }
                }
            } catch (Exception e ) {
                //If an exception occurs ignore and return original value.
            }
        }
        return value
    }

}
