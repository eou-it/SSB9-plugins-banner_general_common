/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/


package net.hedtech.banner.general.overall

import grails.validation.Validateable
import net.hedtech.banner.general.person.PersonIdentificationName
import org.grails.datastore.gorm.GormValidateable
import org.grails.datastore.mapping.dirty.checking.DirtyCheckable

class PersonIdentificationDecorator implements GormValidateable, DirtyCheckable, Validateable{

    @Delegate private final PersonIdentificationName personIdentificationName
    ThirdPartyAccess thirdPartyAccess
    ImsSourcedIdBase imsSourcedIdBase
    String enterpriseId
    String guid

    PersonIdentificationDecorator(PersonIdentificationName personIdentificationName,
                                      ThirdPartyAccess thirdPartyAccess,
                                      ImsSourcedIdBase imsSourcedIdBase,
                                      PidmAndUDCIdMapping pidmAndUDCIdMapping,
                                      String guid) {
        this.personIdentificationName = personIdentificationName ?: new PersonIdentificationName()
        this.thirdPartyAccess = thirdPartyAccess ?: new ThirdPartyAccess()
        this.imsSourcedIdBase = imsSourcedIdBase ?: new ImsSourcedIdBase()
        this.enterpriseId = pidmAndUDCIdMapping?.udcId
        this.guid = guid
    }


    PersonIdentificationDecorator(Map personIdentification) {
        this.personIdentificationName = personIdentification.personidentificationname ?: new PersonIdentificationName()
        this.thirdPartyAccess = personIdentification.thirdpartyaccess ?: new ThirdPartyAccess()
        this.imsSourcedIdBase = personIdentification.imssourcedidbase ?: new ImsSourcedIdBase()
        this.enterpriseId = personIdentification.pidmandudcidmapping?.udcId
        this.guid = personIdentification.guid ?: null
    }


    PersonIdentificationDecorator() {
        this.personIdentificationName = new PersonIdentificationName()
        this.thirdPartyAccess = new ThirdPartyAccess()
        this.imsSourcedIdBase = new ImsSourcedIdBase()
        this.enterpriseId = null
        this.guid = null
    }


    @Override
    public String toString() {
        return "PersonIdentificationDecorator{" +
                "personIdentificationName=" + personIdentificationName +
                ", thirdPartyAccess=" + thirdPartyAccess +
                ", imsSourcedIdBase=" + imsSourcedIdBase +
                ", enterpriseId='" + enterpriseId + '\'' +
                ", guid='" + guid + '\'' +
                '}';
    }
}
