/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner

import net.hedtech.banner.security.XssSanitizer

class SanitizeUtility {

    /**
     * Recursively sanitize all values in map to eliminate cross-site scripting (XSS) vulnerabilities.
     * @param map
     */
    def static sanitizeMap(Map map) {
        map.each { element ->
            def v = element.value

            if (v in Map) {
                sanitizeMap(v)
            } else if (v in String) {
                element.value = XssSanitizer.sanitize(v)
            }
        }
    }
}
