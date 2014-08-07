/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 


package net.hedtech.banner.general.overall

import net.hedtech.banner.general.person.PersonIdentificationName

class PersonIdentificationDecorator {

    @Delegate private final PersonIdentificationName personIdentificationName
    ThirdPartyAccess thirdPartyAccess
    ImsSourcedIdBase imsSourcedIdBase  
    PidmAndUDCIdMapping pidmAndUDCIdMapping

    PersonIdentificationDecorator(PersonIdentificationName personIdentificationName,
                                      ThirdPartyAccess thirdPartyAccess,
                                      ImsSourcedIdBase imsSourcedIdBase,
                                      PidmAndUDCIdMapping pidmAndUDCIdMapping) {
        this.personIdentificationName = personIdentificationName
        this.thirdPartyAccess = thirdPartyAccess
        this.imsSourcedIdBase = imsSourcedIdBase
        this.pidmAndUDCIdMapping = pidmAndUDCIdMapping
    }


    PersonIdentificationDecorator(Map personIdentification) {
        this.personIdentificationName = personIdentification['personidentificationname']
        this.thirdPartyAccess = personIdentification['thirdpartyaccess']
        this.imsSourcedIdBase = personIdentification['imssourcedidbase']
        this.pidmAndUDCIdMapping = personIdentification['pidmandudcidmapping']
    }


    PersonIdentificationDecorator() {
        this.personIdentificationName = null
        this.thirdPartyAccess = null
        this.imsSourcedIdBase = null
        this.pidmAndUDCIdMapping = null
    }


    public String toString() {
    }


}
