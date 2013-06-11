/** *****************************************************************************
 Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */

package net.hedtech.banner.general

import org.codehaus.groovy.grails.web.context.ServletContextHolder as SCH

import net.hedtech.banner.general.system.SdaCrosswalkConversion

/**
 * This is a utilty class for all of Banner
 *
 */
class GeneralCommonUtility {

    /**
     * Store gtvsdax values in  application variable to minimize trips to the DB
     * @param internal
     * @param internalGroup
     * @return external value
     */

    public static gtvsdaxForSession(def internal, def internalGroup) {
        def gtvsdaxList = SCH.servletContext.getAttribute("gtvsdax")
        if (!gtvsdaxList) gtvsdaxList = [:]
        def keycode =  internal + internalGroup
        def gtvsdaxValue = gtvsdaxList[keycode]

        if (!gtvsdaxValue) {
            gtvsdaxValue = SdaCrosswalkConversion.fetchAllByInternalAndInternalGroup(internal, internalGroup)[0]?.external
            gtvsdaxList.put((keycode), gtvsdaxValue)
            SCH.servletContext.setAttribute("gtvsdax", gtvsdaxList)
        }

        return gtvsdaxValue
    }


}
