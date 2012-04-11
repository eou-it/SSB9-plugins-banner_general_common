/*********************************************************************************
 Copyright 2009-2012 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of
 SunGard Higher Education and its subsidiaries. Any use of this software is limited
 solely to SunGard Higher Education licensees, and is further subject to the terms
 and conditions of one or more written license agreements between SunGard Higher
 Education and the licensee in question. SunGard is either a registered trademark or
 trademark of SunGard Data Systems in the U.S.A. and/or other regions and/or countries.
 Banner and Luminis are either registered trademarks or trademarks of SunGard Higher
 Education in the U.S.A. and/or other regions and/or countries.
 **********************************************************************************/

/**
 Banner Automator Version: 1.24
 Generated: Tue Aug 09 14:09:55 IST 2011
 */
package com.sungardhe.banner.general.overall

import com.sungardhe.banner.service.ServiceBase

// NOTE:
// This service is injected with create, update, and delete methods that may throw runtime exceptions (listed below).
// These exceptions must be caught and handled by the controller using this service.
//
// update and delete may throw com.sungardhe.banner.exceptions.NotFoundException if the entity cannot be found in the database
// update and delete may throw org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException a runtime exception if an optimistic lock failure occurs
// create, update, and delete may throw grails.validation.ValidationException a runtime exception when there is a validation failure

class SequenceNumberBaseService extends ServiceBase {

    boolean transactional = true

    /**
     * Please put all the custom methods in this protected section to protect the code
     * from being overwritten on re-generation
     */
    /*PROTECTED REGION ID(sequencenumberbase_custom_service_methods) ENABLED START*/
    /**
     * Function to get next sequence numbers(used for ids) for multiple domains
     * from SequenceNumberBase(SOBSEQN).
     * (Example courseReferenceNumber for HousingEventBase)
     * @param function ... string which is used as an identifier for each sequence
     * @param maxSequenceNumber ... maximum integer suffix allowed for the particular function
     * @return
     */
    public def getNextSequenceNumberBase(String function, Integer maxSequenceNumber){
        String nextSequence
        def maxLength = maxSequenceNumber.toString().length()
        SequenceNumberBase sequenceNumberBase = SequenceNumberBase.findByFunction(function)
        if(sequenceNumberBase){
            if(sequenceNumberBase.maximumSequenceNumber >= maxSequenceNumber){
                sequenceNumberBase.sequenceNumberPrefix = (char)(sequenceNumberBase.sequenceNumberPrefix.charAt(0) + 1)
                sequenceNumberBase.maximumSequenceNumber = 0
            }
            sequenceNumberBase.maximumSequenceNumber++
            String format = "${sequenceNumberBase.sequenceNumberPrefix}%0${maxLength}d";
            nextSequence = String.format(format, sequenceNumberBase.maximumSequenceNumber)

            insertUpdateDomain(sequenceNumberBase,this,new HashMap())
        }
        return nextSequence
    }


    /**
     *  Process insert or update of domain
     */
    private void insertUpdateDomain(domainEntry, service, Map keyBlockMap) {
        def map = [keyBlock: keyBlockMap, domainModel: domainEntry]

        service.createOrUpdate(map)
    }

    /**
     *  Delete  domain
     */
    private void deleteDomain(domainEntry, service, Map keyBlockMap) {
        domainEntry.each { comment ->
            service.delete([keyBlock: keyBlockMap, domainModel: comment])
        }
    }
    /*PROTECTED REGION END*/
}
