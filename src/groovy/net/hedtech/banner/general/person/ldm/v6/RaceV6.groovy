/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm.v6

import net.hedtech.banner.general.common.GeneralValidationCommonConstants
import net.hedtech.banner.general.system.ldm.v6.ReportingDecorator

/**
 * Decorator used in  "persons" V6
 *
 */
class RaceV6  {

    String guid
    String parentCategory
    RaceV6(String guid, String parentCategory) {
        this.guid = guid
        this.parentCategory = parentCategory
    }


    Map getRace(){
        return [id:guid]
    }

    List<ReportingDecorator> getReporting(){
        if(parentCategory) {
            return  [new ReportingDecorator(GeneralValidationCommonConstants.RACE_LDM_NAME, parentCategory)]
        }
    }
}
