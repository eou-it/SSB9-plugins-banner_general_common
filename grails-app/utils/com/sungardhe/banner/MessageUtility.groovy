/** *****************************************************************************
 � 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner

import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.springframework.context.MessageSource

/**
 * This is a helper class that is used for retrieving Message from i18n messsage.properties
 *
 */
class MessageUtility {

    /**
     * @param String key
     * @param Object[] args
     * @param Locale locale
     */

    public static String message(key, args = null, locale = null) {

        String value = "";
        if (key){
              if(!locale) locale = Locale.getDefault()
              MessageSource messageSource = ApplicationHolder.application.mainContext.getBean('messageSource')
              value = messageSource.getMessage(key,args,locale)
        }
        return value
    }

}