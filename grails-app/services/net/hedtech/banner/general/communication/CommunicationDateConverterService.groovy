/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication

import net.hedtech.banner.i18n.DateConverterService
import org.springframework.context.i18n.LocaleContextHolder

import java.text.SimpleDateFormat

/**
 * Service extends the common date converterservice.
 */
class CommunicationDateConverterService extends DateConverterService {


    public parseGregorianToDefaultCalendar(value, dateformat) {

        def origValue = value
        def tempValue

        try {

            String defaultDateFormat = localizerService(code: dateformat)
            if (getULocaleStringForCalendar('gregorian') != getULocaleTranslationStringForCalendar(localizerService(code: "default.calendar",default:'gregorian'))) {
                if(value instanceof Date) {
                    SimpleDateFormat sdfwithoutlocale = new SimpleDateFormat(localizerService(code: dateformat));
                    value = sdfwithoutlocale.format(new Date(value.getTime()));
                }
                tempValue = convert(origValue,
                        getULocaleStringForCalendar('gregorian'),
                        getULocaleTranslationStringForCalendar(localizerService(code: "default.calendar", default: 'gregorian')),
                        defaultDateFormat,
                        defaultDateFormat)
            } else {
                if(value instanceof Date) {
                    SimpleDateFormat sdf = new SimpleDateFormat(localizerService(code: dateformat),LocaleContextHolder?.getLocale());
                    value = sdf.format(new Date(value.getTime()));
                }
                tempValue = convert(value,
                    getULocaleStringForCalendar('gregorian'),
                    getULocaleTranslationStringForCalendar(localizerService(code: "default.calendar",default:'gregorian')),
                    defaultDateFormat ,
                    defaultDateFormat)
            }

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
