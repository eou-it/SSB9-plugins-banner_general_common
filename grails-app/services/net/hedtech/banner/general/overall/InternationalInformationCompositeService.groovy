/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.exceptions.ApplicationException

class InternationalInformationCompositeService {

    static transactional = true

    def visaInformationService      // Master
    def documentInformationService  // Detail

    def visaInternationalInformationService // Single


    def createOrUpdate(map) {
        // deletes Parent first.  This way the API dependency errors can float up.
        if (map?.deleteVisaInformations)
            deleteDomain(map.deleteVisaInformations, visaInformationService)

        if (map?.deleteDocumentInformations)
            deleteDomain(map.deleteDocumentInformations, documentInformationService)

        if (map?.deleteVisaInternationalInformation)
            deleteDomain([map.deleteVisaInternationalInformation], visaInternationalInformationService)

        // inserts or updates
        if (map?.visaInformations)
            createOrUpdateDomain(map.visaInformations, visaInformationService)

        if (map?.documentInformations)
            createOrUpdateDocumentDomain(map.visaInformations, map.documentInformations, documentInformationService)

        if (map?.visaInternationalInformation)
            createOrUpdateDomain([map.visaInternationalInformation], visaInternationalInformationService)
    }

    /**
     *  Create or Update domains.
     */
    private void createOrUpdateDomain(domains, service) {
        domains.each { domain ->
            if (domain.id)
                service.update([domainModel: domain])
            else
                service.create([domainModel: domain])
        }
    }

    /**
     *  Create or Update Document domains.
     */
    private void createOrUpdateDocumentDomain(visaDomains, domains, service) {
        domains.each { domain ->
            validateDocument(domain)

            if (domain.id)
                service.update([domainModel: domain])
            else {
                if (domain.sequenceNumber < 0)  // temporary sequenceNumber; need to find the permament one.
                    domain.sequenceNumber = getVisaSequenceNumber(visaDomains, domain.sequenceNumber)

                service.create([domainModel: domain])
            }
        }
    }


    private void deleteDomain(domains, service) {
        domains.each { domain ->
            service.delete([domainModel: domain])
            domain.id = null
        }
    }


    private int getVisaSequenceNumber(degreeDomains, tempSeqNo) {
        // set a default value; this way if a permament value is not found the API error for "missing_parent" can float up.
        def sequenceNumber = 0

        degreeDomains.each { domain ->
            if (domain.tempSeqNo == tempSeqNo)
                sequenceNumber = domain.sequenceNumber
        }
        return sequenceNumber
    }


    private void validateDocument(domain) {
        def docs = DocumentInformation.fetchDuplicateRecord(domain)
        if (docs.size() > 0)
            throw new ApplicationException(InternationalInformationCompositeService, "@@r1:duplicateDocument@@")
    }
}
