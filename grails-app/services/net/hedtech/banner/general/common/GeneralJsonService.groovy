/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.common

import net.hedtech.banner.general.common.GeneralJson
import grails.gorm.transactions.Transactional
import net.hedtech.banner.service.ServiceBase

@Transactional
class GeneralJsonService extends ServiceBase{

    def fetchJsonData(Long id) {
        return GeneralJson.fetchJsonData(id)
    }

}
