/*********************************************************************************
 Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of 
 SunGard Higher Education and its subsidiaries. Any use of this software is limited 
 solely to SunGard Higher Education licensees, and is further subject to the terms 
 and conditions of one or more written license agreements between SunGard Higher 
 Education and the licensee in question. SunGard is either a registered trademark or
 trademark of SunGard Data Systems in the U.S.A. and/or other regions and/or countries.
 Banner and Luminis are either registered trademarks or trademarks of SunGard Higher 
 Education in the U.S.A. and/or other regions and/or countries.
 **********************************************************************************/

package net.hedtech.banner.general.utility

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.overall.SectionCrossListSection
import net.hedtech.banner.general.system.Term
import net.hedtech.banner.service.ServiceBase

// NOTE:
// This service is injected with create, update, and delete methods that may throw runtime exceptions (listed below).  
// These exceptions must be caught and handled by the controller using this service.
// 
// update and delete may throw net.hedtech.banner.exceptions.NotFoundException if the entity cannot be found in the database
// update and delete may throw org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException a runtime exception if an optimistic lock failure occurs
// create, update, and delete may throw grails.validation.ValidationException a runtime exception when there is a validation failure

class InformationTextService extends ServiceBase {

    boolean transactional =  true

    void preCreate(map) {
        validatePageNameForCreateUpdate(map.domainModel)
        validateLabelForCreateUpdate(map.domainModel)
        validateTextTypeForCreateUpdate(map.domainModel)
        validateSequenceNumberForCreateUpdate(map.domainModel)
        validatePersonaNumberForCreateUpdate(map.domainModel)
        validateTextForCreateUpdate(map.domainModel)
        validateLocaleForCreateUpdate(map.domainModel)
    }


    void preUpdate(map) {
        validatePageNameForCreateUpdate(map.domainModel)
        validateLabelForCreateUpdate(map.domainModel)
        validateTextTypeForCreateUpdate(map.domainModel)
        validateSequenceNumberForCreateUpdate(map.domainModel)
        validatePersonaNumberForCreateUpdate(map.domainModel)
        validateTextForCreateUpdate(map.domainModel)
        validateLocaleForCreateUpdate(map.domainModel)
    }



    private void validatePageNameForCreateUpdate(informationText) {
        if (informationText.pageName == null || informationText.pageName == "") {
            throw new ApplicationException(InformationText, "@@r1:pageName.nullable.error@@")
        }
    }
    private void validateLabelForCreateUpdate(informationText) {
        if (informationText.label == null || informationText.label == "") {
            throw new ApplicationException(InformationText, "@@r1:label.nullable.error@@")
        }
    }
    private void validateTextTypeForCreateUpdate(informationText) {
        if (informationText.textType == null || informationText.textType == "") {
            throw new ApplicationException(InformationText, "@@r1:textType.nullable.error@@")
        }
    }
    private void validateSequenceNumberForCreateUpdate(informationText) {
        if (informationText.sequenceNumber == null || informationText.sequenceNumber == "") {
            throw new ApplicationException(InformationText, "@@r1:sequenceNumber.nullable.error@@")
        }
    }
    private void validatePersonaNumberForCreateUpdate(informationText) {
        if (informationText.persona == null || informationText.persona == "") {
            throw new ApplicationException(InformationText, "@@r1:persona.nullable.error@@")
        }
    }
    private void validateTextForCreateUpdate(informationText) {
        if (informationText.text == null || informationText.text == "") {
            throw new ApplicationException(InformationText, "@@r1:text.nullable.error@@")
        }
    }
    private void validateLocaleForCreateUpdate(informationText) {
        if (informationText.locale == null || informationText.locale == "") {
            throw new ApplicationException(InformationText, "@@r1:locale.nullable.error@@")
        }
    }
}
