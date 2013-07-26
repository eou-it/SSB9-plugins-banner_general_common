/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.service.ServiceBase

class VisaInformationService extends ServiceBase {
    def preCreate(map) {
        def rec = map?.domainModel
        map?.domainModel?.sequenceNumber =
            VisaInformation.fetchNextSequenceNumber(rec?.pidm)
    }
}
