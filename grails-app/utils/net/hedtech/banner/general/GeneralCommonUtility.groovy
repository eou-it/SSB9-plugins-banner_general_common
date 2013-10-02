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
     * Store the gtvsdax values used by an App in a list that gets passed and stored in the app session
     * @param internal
     * @param internalGroup
     * @param appGtvsdaxList
     * @return map with the external value and the list
     */
    public static def getAppGtvsdax(def internal, def internalGroup, List appGtvsdaxList = []) {
        def gtvsdaxMap

        def gtvsdaxValue = appGtvsdaxList?.find { it.internal == internal && it.internalGroup == internalGroup }?.external
        if (gtvsdaxValue) {
            gtvsdaxMap = [gtvsdaxValue: gtvsdaxValue, appGtvsdaxList: appGtvsdaxList]
        } else {
            gtvsdaxMap = createSdaxMapForAppSessionList(internal, internalGroup, appGtvsdaxList)
        }
        return gtvsdaxMap
    }


    public static def createSdaxMapForAppSessionList(def internal, def internalGroup, List appGtvsdaxList) {
        def gtvsdaxValue = SdaCrosswalkConversion.fetchAllByInternalAndInternalGroup(internal, internalGroup)[0]?.external
        if (appGtvsdaxList?.size()) {
            appGtvsdaxList << [internal: internal, external: gtvsdaxValue, internalGroup: internalGroup]

        } else {
            appGtvsdaxList = []
            appGtvsdaxList.add([internal: internal, external: gtvsdaxValue, internalGroup: internalGroup])
        }
        return [gtvsdaxValue: gtvsdaxValue, appGtvsdaxList: appGtvsdaxList]
    }
}
