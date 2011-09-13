/*********************************************************************************
 Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of 
 SunGard Higher Education and its subsidiaries. Any use of this software is limited 
 solely to SunGard Higher Education licensees, and is further subject to the terms 
 and conditions of one or more written license agreements between SunGard Higher 
 Education and the licensee in question. SunGard, Banner and Luminis are either 
 registered trademarks or trademarks of SunGard Higher Education in the U.S.A. 
 and/or other regions and/or countries.
 **********************************************************************************/

package com.sungardhe.banner.general.overall

import com.sungardhe.banner.exceptions.ApplicationException
import com.sungardhe.banner.general.system.Term
import com.sungardhe.banner.service.ServiceBase

// NOTE:
// This service is injected with create, update, and delete methods that may throw runtime exceptions (listed below).  
// These exceptions must be caught and handled by the controller using this service.
// 
// update and delete may throw com.sungardhe.banner.exceptions.NotFoundException if the entity cannot be found in the database
// update and delete may throw org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException a runtime exception if an optimistic lock failure occurs
// create, update, and delete may throw grails.validation.ValidationException a runtime exception when there is a validation failure

class SectionCrossListSectionService extends ServiceBase {

    boolean transactional =  true

    /**
     * Please put all the custom methods in this protected section to protect the code
     * from being overwritten on re-generation
     */
    /*PROTECTED REGION ID(sectioncrosslistsection_custom_service_methods) ENABLED START*/
    /*PROTECTED REGION END*/


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
        if ( !sectionCrossListBase.courseReferenceNumber )
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
