/*******************************************************************************
 Copyright 2013-2016 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.service.ServiceBase

class VisaInformationService extends ServiceBase {
    def preCreate(map) {
        def rec = map?.domainModel
        map?.domainModel?.sequenceNumber =
            VisaInformation.fetchNextSequenceNumber(rec?.pidm)
    }

    public static List<VisaInformation> fetchAllWithMaxSeqNumByPidmInList(List<Integer> pidmList) {
        return VisaInformation.fetchAllWithMaxSeqNumByPidmInList(pidmList)
    }
}
