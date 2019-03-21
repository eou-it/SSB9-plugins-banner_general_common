/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */

package net.hedtech.banner.general.overall

import grails.gorm.transactions.Transactional
import net.hedtech.banner.service.ServiceBase

@Transactional
class SequenceNumberBaseService extends ServiceBase {

    

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

}
