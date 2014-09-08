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
        this.personIdentificationName = personIdentificationName ?: new PersonIdentificationName()
        this.thirdPartyAccess = thirdPartyAccess ?: new ThirdPartyAccess()
        this.imsSourcedIdBase = imsSourcedIdBase ?: new ImsSourcedIdBase()
        this.pidmAndUDCIdMapping = pidmAndUDCIdMapping ?: new PidmAndUDCIdMapping()
    }


    PersonIdentificationDecorator(Map personIdentification) {
        this.personIdentificationName = personIdentification.personidentificationname ?: new PersonIdentificationName()
        this.thirdPartyAccess = personIdentification.thirdpartyaccess ?: new ThirdPartyAccess()
        this.imsSourcedIdBase = personIdentification.imssourcedidbase ?: new ImsSourcedIdBase()
        this.pidmAndUDCIdMapping = personIdentification.pidmandudcidmapping ?: new PidmAndUDCIdMapping()
    }


    PersonIdentificationDecorator() {
        this.personIdentificationName = new PersonIdentificationName()
        this.thirdPartyAccess = new ThirdPartyAccess()
        this.imsSourcedIdBase = new ImsSourcedIdBase()
        this.pidmAndUDCIdMapping = new PidmAndUDCIdMapping()
    }


    public String toString() {
    }


}
