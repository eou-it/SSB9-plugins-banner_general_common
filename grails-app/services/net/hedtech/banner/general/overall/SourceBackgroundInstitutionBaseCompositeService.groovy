/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

class SourceBackgroundInstitutionBaseCompositeService {
    boolean transactional = true

    // Master
    def sourceBackgroundInstitutionBaseService

    // Details
    def sourceBackgroundInstitutionCommentService
    def sourceBackgroundInstitutionContactPersonService


    def createOrUpdate(map) {
        if (map?.deleteSourceBackgroundInstitutionBases) {
            map.deleteSourceBackgroundInstitutionBases.each { sourceBackgroundInstitutionBase ->
                def sourceAndBackgroundInstitution = sourceBackgroundInstitutionBase.sourceAndBackgroundInstitution

                // Before deleting the Master record do cascade deletions
                deleteDomainAll(sourceAndBackgroundInstitution, SourceBackgroundInstitutionComment, sourceBackgroundInstitutionCommentService)
                deleteDomainAll(sourceAndBackgroundInstitution, SourceBackgroundInstitutionContactPerson, sourceBackgroundInstitutionContactPersonService)

                // Delete Master
                deleteDomainAll(sourceAndBackgroundInstitution, SourceBackgroundInstitutionBase, sourceBackgroundInstitutionBaseService)
            }

        } else {
            // Delete detailed records that are marked for deletions
            if (map?.deleteSourceBackgroundInstitutionComments)
                deleteDomain(map.deleteSourceBackgroundInstitutionComments, sourceBackgroundInstitutionCommentService)

            if (map?.deleteSourceBackgroundInstitutionContactPersons)
                deleteDomain(map.deleteSourceBackgroundInstitutionContactPersons, sourceBackgroundInstitutionContactPersonService)
        }

        if (map?.sourceBackgroundInstitutionBases)
            map.sourceBackgroundInstitutionBases =
                createOrUpdateDomain(map.sourceBackgroundInstitutionBases, sourceBackgroundInstitutionBaseService)

        if (map?.sourceBackgroundInstitutionComments)
            map.sourceBackgroundInstitutionComments =
                createOrUpdateDomain(map.sourceBackgroundInstitutionComments, sourceBackgroundInstitutionCommentService)

        if (map?.sourceBackgroundInstitutionContactPersons)
            map.sourceBackgroundInstitutionContactPersons =
                createOrUpdateDomain(map.sourceBackgroundInstitutionContactPersons, sourceBackgroundInstitutionContactPersonService)
    }


    /**
     *  Delete all domains associated with the sourceAndBackgroundInstitution
     */
    private void deleteDomainAll(sourceAndBackgroundInstitution, domainClass, service) {
        def domains = domainClass.findAllWhere(sourceAndBackgroundInstitution: sourceAndBackgroundInstitution)
        domains.each { domain ->
            service.delete([domainModel: domain])
        }
    }


    /**
     *  Delete domains
     */
    private void deleteDomain(domains, service) {
        domains.each { domain ->
            if (domain.id)
                service.delete([domainModel: domain])
        }
    }


    /**
     *  Create or Update domains
     */
    private void createOrUpdateDomain(domains, service) {
        domains.each { domain ->
            if (domain.id)
                service.update([domainModel: domain])
            else
                service.create([domainModel: domain])
        }
    }
}
