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
            def sourceAndBackgroundInstitution = map.deleteSourceBackgroundInstitutionBases.sourceAndBackgroundInstitution

            // Before deleting the Master record do cascade deletions
            deleteDomain(sourceAndBackgroundInstitution, SourceBackgroundInstitutionComment, sourceBackgroundInstitutionCommentService)
            deleteDomain(sourceAndBackgroundInstitution, SourceBackgroundInstitutionContactPerson, sourceBackgroundInstitutionContactPersonService)

            // Delete Master
            deleteDomain(sourceAndBackgroundInstitution, SourceBackgroundInstitutionBase, sourceBackgroundInstitutionBaseService)

        } else {
            // Delete detailed records that are marked for deletions
            if (map?.deleteSourceBackgroundInstitutionComments) {
                map.deleteSourceBackgroundInstitutionComments.each {
                    sourceBackgroundInstitutionCommentService.delete([domainModel: it])
                }
            }
            if (map?.deleteSourceBackgroundInstitutionContactPersons) {
                map.deleteSourceBackgroundInstitutionContactPersons.each {
                    sourceBackgroundInstitutionContactPersonService.delete([domainModel: it])
                }
            }
        }
    }


    /**
     *  Delete domain
     */
    private void deleteDomain(sourceAndBackgroundInstitution, domainClass, service) {
        def records = domainClass.findAllWhere(sourceAndBackgroundInstitution: sourceAndBackgroundInstitution)
        records.each {
            service.delete([domainModel: it])
        }
    }
}
