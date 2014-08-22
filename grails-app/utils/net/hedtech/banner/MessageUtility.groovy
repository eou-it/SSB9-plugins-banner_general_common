/*********************************************************************************
  Copyright 2010-2014 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner

import net.hedtech.banner.i18n.MessageHelper

/**
 * This is a helper class that is used for retrieving Message from i18n messsage.properties
 *
 */
class MessageUtility {

    public static String message(key, args = null, locale = null) {
       return MessageHelper.message(key, args)
   }

}
