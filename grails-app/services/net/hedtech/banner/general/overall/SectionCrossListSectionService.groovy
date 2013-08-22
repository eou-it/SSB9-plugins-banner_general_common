/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */

package net.hedtech.banner.general.overall

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.system.Term
import net.hedtech.banner.service.ServiceBase

// NOTE:
// This service is injected with create, update, and delete methods that may throw runtime exceptions (listed below).  
// These exceptions must be caught and handled by the controller using this service.
// 
// update and delete may throw net.hedtech.banner.exceptions.NotFoundException if the entity cannot be found in the database
// update and delete may throw org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException a runtime exception if an optimistic lock failure occurs
// create, update, and delete may throw grails.validation.ValidationException a runtime exception when there is a validation failure

class SectionCrossListSectionService extends ServiceBase {

    boolean transactional = true


    def preCreate(map) {

        validateCodes(map.domainModel)
        checkSectionAlreadyExists(map.domainModel)
    }


    def preUpdate(map) {
        // Add exception for not allow of update
        validateCodes(map.domainModel)
    }


    private void validateCodes(SectionCrossListSection sectionCrossListBase) {
        if (Term.findByCode(sectionCrossListBase.term.code) == null)
            throw new ApplicationException(SectionCrossListSection, "@@r1:invalid.code.message:Term@@")
        if (!sectionCrossListBase.courseReferenceNumber)
            throw new ApplicationException(SectionCrossListSection, "@@r1:crn_required@@")
    }


    private def checkSectionAlreadyExists(map) {
        SectionCrossListSection ssr = SectionCrossListSection.findByTermAndCourseReferenceNumber(map.term, map?.courseReferenceNumber)
        if (ssr)
            throw new ApplicationException(SectionCrossListSection, "@@r1:section_already_cross_listed:${map?.courseReferenceNumber}:${ssr.xlstGroup}@@")

    }


    def List fetchByTermAndCrossListGroupIndicator(params) {

        return SectionCrossListSection.fetchByTermAndXlstGroup(
                params.term, params.xlstGroup)
    }

}
