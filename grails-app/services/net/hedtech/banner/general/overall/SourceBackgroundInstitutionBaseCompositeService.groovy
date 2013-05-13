/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.general.system.SourceAndBackgroundInstitution

class SourceBackgroundInstitutionBaseCompositeService {
    boolean transactional = true

    // Master (singular)
    def sourceBackgroundInstitutionBaseService

    // Details (singular)
    def sourceBackgroundInstitutionAcademicService
    def sourceBackgroundInstitutionCharacteristicService

    // Details (repeating)
    def sourceBackgroundInstitutionCommentService
    def sourceBackgroundInstitutionContactPersonService
    def sourceBackgroundInstitutionDegreesOfferedService
    def sourceBackgroundInstitutionDemographicService
    def sourceBackgroundInstitutionDiplomasOfferedService
    def sourceBackgroundInstitutionEthnicMakeUpService
    def sourceBackgroundInstitutionTestScoreService


    def deleteAll(SourceAndBackgroundInstitution sourceAndBackgroundInstitution) {
        if (sourceAndBackgroundInstitution) {
            // Delete all information that has a FK to sourceAndBackgroundInstitution
            deleteDomainAll(sourceAndBackgroundInstitution, SourceBackgroundInstitutionAcademic, sourceBackgroundInstitutionAcademicService)
            deleteDomainAll(sourceAndBackgroundInstitution, SourceBackgroundInstitutionCharacteristic, sourceBackgroundInstitutionCharacteristicService)
            deleteDomainAll(sourceAndBackgroundInstitution, SourceBackgroundInstitutionComment, sourceBackgroundInstitutionCommentService)
            deleteDomainAll(sourceAndBackgroundInstitution, SourceBackgroundInstitutionContactPerson, sourceBackgroundInstitutionContactPersonService)
            deleteDomainAll(sourceAndBackgroundInstitution, SourceBackgroundInstitutionDegreesOffered, sourceBackgroundInstitutionDegreesOfferedService)
            deleteDomainAll(sourceAndBackgroundInstitution, SourceBackgroundInstitutionDemographic, sourceBackgroundInstitutionDemographicService)
            deleteDomainAll(sourceAndBackgroundInstitution, SourceBackgroundInstitutionDiplomasOffered, sourceBackgroundInstitutionDiplomasOfferedService)
            deleteDomainAll(sourceAndBackgroundInstitution, SourceBackgroundInstitutionEthnicMakeUp, sourceBackgroundInstitutionEthnicMakeUpService)
            deleteDomainAll(sourceAndBackgroundInstitution, SourceBackgroundInstitutionTestScore, sourceBackgroundInstitutionTestScoreService)
            deleteDomainAll(sourceAndBackgroundInstitution, SourceBackgroundInstitutionBase, sourceBackgroundInstitutionBaseService)
        }
    }

    /**
     *  Delete all domains associated with the sourceAndBackgroundInstitution
     */
    private void deleteDomainAll(sourceAndBackgroundInstitution, domainClass, service) {
        def domains = domainClass.findAllWhere(sourceAndBackgroundInstitution: sourceAndBackgroundInstitution)
        service.delete(domains)
    }

}
