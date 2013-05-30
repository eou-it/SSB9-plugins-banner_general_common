/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.service.ServiceBase

class PriorCollegeDegreeService extends ServiceBase {
    def preCreate(map) {
        map?.domainModel?.degreeSequenceNumber = 0 // 0 means a new seq will be created by the PL/SQL API
    }
}
