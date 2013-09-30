/*********************************************************************************
 Copyright 2010-2013 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
/** *****************************************************************************
 Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */

package net.hedtech.banner.general

import net.hedtech.banner.general.system.SdaCrosswalkConversion
import org.codehaus.groovy.grails.web.context.ServletContextHolder as SCH

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
        def keycode = internal + internalGroup
        def gtvsdaxValue = gtvsdaxList[keycode]

        if (!gtvsdaxValue) {
            gtvsdaxValue = SdaCrosswalkConversion.fetchAllByInternalAndInternalGroup(internal, internalGroup)[0]?.external
            gtvsdaxList.put((keycode), gtvsdaxValue)
            SCH.servletContext.setAttribute("gtvsdax", gtvsdaxList)
        }

        return gtvsdaxValue
    }

    /**
     * Store the gtvsdax values used by an App in a list that gets passed and stored in the app session
     * @param internal
     * @param internalGroup
     * @param appGtvsdaxList
     * @return map with the external value and the list
     */
    public static def getAppGtvsdax(def internal, def internalGroup, List appGtvsdaxList = []) {
        def gtvsdaxMap

        def gtvsdaxValue = appGtvsdaxList?.find { it.internal == internal && it.internalGroup == internalGroup }?.external
        if (!gtvsdaxValue) {
            gtvsdaxMap = createSdaxMapForAppSessionList(internal, internalGroup, appGtvsdaxList)
        } else {
            gtvsdaxMap = [gtvsdaxValue: gtvsdaxValue, appGtvsdaxList: appGtvsdaxList]
        }

        return gtvsdaxMap

    }


    public static def createSdaxMapForAppSessionList(def internal, def internalGroup, List appGtvsdaxList) {
        def gtvsdaxValue = SdaCrosswalkConversion.fetchAllByInternalAndInternalGroup(internal, internalGroup)[0]?.external
        appGtvsdaxList << [internal: internal, external: gtvsdaxValue, internalGroup: internalGroup]
        return [gtvsdaxValue: gtvsdaxValue, appGtvsdaxList: appGtvsdaxList]
    }
}
