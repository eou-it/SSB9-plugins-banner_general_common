/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.item

import grails.gorm.DetachedCriteria
import net.hedtech.banner.service.ServiceBase

class CommunicationEmailItemService extends ServiceBase {

    def preCreate( map ) {
        map?.domainModel?.createDate = new Date()
    }

    public void deleteAll() {
        //TODO: remove
    }
}
