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
     *  Delete  domain
     */
    private void deleteDomain(sourceAndBackgroundInstitution, domainClass, service) {
        def records = domainClass.findAllBySourceAndBackgroundInstitution(sourceAndBackgroundInstitution)
        records.each {
            service.delete([domainModel: it])
        }
    }

//    private def deleteBase(SourceBackgroundInstitutionBase domain) {
//        def records = SourceBackgroundInstitutionBase.findAllBySourceAndBackgroundInstitution(domain.sourceAndBackgroundInstitution)
//        records.each {
//            sourceBackgroundInstitutionBaseService.delete([domainModel: it])
//        }
//    }
//
//
//    private def deleteComments(SourceBackgroundInstitutionBase domain) {
//        def records = SourceBackgroundInstitutionComment.findAllBySourceAndBackgroundInstitution(domain.sourceAndBackgroundInstitution)
//        records.each {
//            sourceBackgroundInstitutionCommentService.delete([domainModel: it])
//        }
//    }
//
//
//    private def deleteContacts(SourceBackgroundInstitutionBase domain) {
//        def records = SourceBackgroundInstitutionContactPerson.findAllBySourceAndBackgroundInstitution(domain.sourceAndBackgroundInstitution)
//        records.each {
//            sourceBackgroundInstitutionContactPersonService.delete([domainModel: it])
//        }
//    }
}
