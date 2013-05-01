/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

class SourceBackgroundInstitutionBaseCompositeService {
    boolean transactional = true

    // Master (singular)
    def sourceBackgroundInstitutionBaseService

    // Details (singular)
//    def sourceBackgroundInstitutionAcademicService
//    def sourceBackgroundInstitutionCharacteristicService

    // Details (repeating)
    def sourceBackgroundInstitutionCommentService
    def sourceBackgroundInstitutionContactPersonService
//    def sourceBackgroundInstitutionDegreesOfferedService
//    def sourceBackgroundInstitutionDemographicService
//    def sourceBackgroundInstitutionDiplomasOfferedService
//    def sourceBackgroundInstitutionEthnicMakeUpService
//    def sourceBackgroundInstitutionTestScoreService


    def createOrUpdate(map) {
        if (map?.deleteSourceBackgroundInstitutionBase) {
            def sourceAndBackgroundInstitution = map.deleteSourceBackgroundInstitutionBase.sourceAndBackgroundInstitution

            // Before deleting the Master record do cascade deletions
//            deleteDomainAll(sourceAndBackgroundInstitution, SourceBackgroundInstitutionAcademic, sourceBackgroundInstitutionAcademicService)
//            deleteDomainAll(sourceAndBackgroundInstitution, SourceBackgroundInstitutionCharacteristic, sourceBackgroundInstitutionCharacteristicService)
            deleteDomainAll(sourceAndBackgroundInstitution, SourceBackgroundInstitutionComment, sourceBackgroundInstitutionCommentService)
            deleteDomainAll(sourceAndBackgroundInstitution, SourceBackgroundInstitutionContactPerson, sourceBackgroundInstitutionContactPersonService)
//            deleteDomainAll(sourceAndBackgroundInstitution, SourceBackgroundInstitutionDegreesOffered, sourceBackgroundInstitutionDegreesOfferedService)
//            deleteDomainAll(sourceAndBackgroundInstitution, SourceBackgroundInstitutionDemographic, sourceBackgroundInstitutionDemographicService)
//            deleteDomainAll(sourceAndBackgroundInstitution, SourceBackgroundInstitutionDiplomasOffered, sourceBackgroundInstitutionDiplomasOfferedService)
//            deleteDomainAll(sourceAndBackgroundInstitution, SourceBackgroundInstitutionEthnicMakeUp, sourceBackgroundInstitutionEthnicMakeUpService)
//            deleteDomainAll(sourceAndBackgroundInstitution, SourceBackgroundInstitutionTestScore, sourceBackgroundInstitutionTestScoreService)

            // Delete Master
            deleteDomain([map.deleteSourceBackgroundInstitutionBase], sourceBackgroundInstitutionBaseService)

        } else { // Delete detailed records that are marked for deletions
            // singular
//            if (map?.deleteSourceBackgroundInstitutionAcademic)
//                deleteDomain([map.deleteSourceBackgroundInstitutionAcademic], sourceBackgroundInstitutionAcademicService)
//
//            if (map?.deleteSourceBackgroundInstitutionCharacteristic)
//                deleteDomain([map.deleteSourceBackgroundInstitutionCharacteristic], sourceBackgroundInstitutionCharacteristicService)

            // repeating
            if (map?.deleteSourceBackgroundInstitutionComments)
                deleteDomain(map.deleteSourceBackgroundInstitutionComments, sourceBackgroundInstitutionCommentService)

            if (map?.deleteSourceBackgroundInstitutionContactPersons)
                deleteDomain(map.deleteSourceBackgroundInstitutionContactPersons, sourceBackgroundInstitutionContactPersonService)

//            if (map?.deleteSourceBackgroundInstitutionDegreesOffereds)
//                deleteDomain(map.deleteSourceBackgroundInstitutionDegreesOffereds, sourceBackgroundInstitutionDegreesOfferedService)
//
//            if (map?.deleteSourceBackgroundInstitutionDemographics)
//                deleteDomain(map.deleteSourceBackgroundInstitutionDemographics, sourceBackgroundInstitutionDemographicService)
//
//            if (map?.deleteSourceBackgroundInstitutionDiplomasOffereds)
//                deleteDomain(map.deleteSourceBackgroundInstitutionDiplomasOffereds, sourceBackgroundInstitutionDiplomasOfferedService)
//
//            if (map?.deleteSourceBackgroundInstitutionEthnicMakeUps)
//                deleteDomain(map.deleteSourceBackgroundInstitutionEthnicMakeUps, sourceBackgroundInstitutionEthnicMakeUpService)
//
//            if (map?.deleteSourceBackgroundInstitutionTestScores)
//                deleteDomain(map.deleteSourceBackgroundInstitutionTestScores, sourceBackgroundInstitutionTestScoreService)
        }

        // singular
        if (map?.sourceBackgroundInstitutionBase)
            map.sourceBackgroundInstitutionBase =
                createOrUpdateDomain([map.sourceBackgroundInstitutionBase], sourceBackgroundInstitutionBaseService)

//        if (map?.sourceBackgroundInstitutionAcademic)
//            map.sourceBackgroundInstitutionAcademic =
//                createOrUpdateDomain([map.sourceBackgroundInstitutionAcademic], sourceBackgroundInstitutionAcademicService)
//
//        if (map?.sourceBackgroundInstitutionCharacteristic)
//            map.sourceBackgroundInstitutionCharacteristic =
//                createOrUpdateDomain([map.sourceBackgroundInstitutionCharacteristic], sourceBackgroundInstitutionCharacteristicService)
//
        // repeating
        if (map?.sourceBackgroundInstitutionComments)
            map.sourceBackgroundInstitutionComments =
                createOrUpdateDomain(map.sourceBackgroundInstitutionComments, sourceBackgroundInstitutionCommentService)

        if (map?.sourceBackgroundInstitutionContactPersons)
            map.sourceBackgroundInstitutionContactPersons =
                createOrUpdateDomain(map.sourceBackgroundInstitutionContactPersons, sourceBackgroundInstitutionContactPersonService)

//        if (map?.sourceBackgroundInstitutionDegreesOffereds)
//            map.sourceBackgroundInstitutionDegreesOffereds =
//                createOrUpdateDomain(map.sourceBackgroundInstitutionDegreesOffereds, sourceBackgroundInstitutionDegreesOfferedService)
//
//        if (map?.sourceBackgroundInstitutionDemographics)
//            map.sourceBackgroundInstitutionDemographics =
//                createOrUpdateDomain(map.sourceBackgroundInstitutionDemographics, sourceBackgroundInstitutionDemographicService)
//
//        if (map?.sourceBackgroundInstitutionDiplomasOffereds)
//            map.sourceBackgroundInstitutionDiplomasOffereds =
//                createOrUpdateDomain(map.sourceBackgroundInstitutionDiplomasOffereds, sourceBackgroundInstitutionDiplomasOfferedService)
//
//        if (map?.sourceBackgroundInstitutionEthnicMakeUps)
//            map.sourceBackgroundInstitutionEthnicMakeUps =
//                createOrUpdateDomain(map.sourceBackgroundInstitutionEthnicMakeUps, sourceBackgroundInstitutionEthnicMakeUpService)
//
//        if (map?.sourceBackgroundInstitutionTestScores)
//            map.sourceBackgroundInstitutionTestScores =
//                createOrUpdateDomain(map.sourceBackgroundInstitutionTestScores, sourceBackgroundInstitutionTestScoreService)
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
