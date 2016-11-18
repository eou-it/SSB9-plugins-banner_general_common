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

    public def fetchAllWithMaxSeqNumByIssuingNationCodeAndPidmInList(List<Integer> pidms, String issuingNationCode) {
        List entities = []
        if (pidms && issuingNationCode) {
            VisaInformation.withSession { session ->
                def namedQuery = session.getNamedQuery('VisaInformation.fetchAllWithMaxSeqNumByIssuingNationCodeAndPidmInList')
                namedQuery.with {
                    setParameterList("pidms", pidms)
                    setString('issuingNationCode', issuingNationCode)
                    entities = list()
                }
            }
        }
        return entities
    }

}
