/** *******************************************************************************
 Copyright 2014-2015 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.system.ldm.v4

import net.hedtech.banner.general.system.ldm.v1.Metadata
import net.hedtech.banner.general.system.ldm.v1.SiteDetail

/**
 * Decorator for Site LDM (/base/domain/site/v1/site.json-schema)
 *
 */
class SiteDetailV4 extends SiteDetail{

    def SiteDetailV4(String guid, def campus, def buildings, Metadata metadata) {
        super(guid,campus,buildings,null)
    }

    String getDescription(){
        if(null == super.getDescription()){
            return super.getCode()
        }
        return super.getDescription()
    }

}
