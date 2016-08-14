/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm.v6

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.person.PersonTelephone
import net.hedtech.banner.general.system.ldm.v4.PhoneTypeDecorator

/**
 * Person Phones Decorator
 */
@EqualsAndHashCode(includeFields = true)
@ToString(includeNames = true, includeFields = true)
class PhoneV6 {

    PhoneTypeDecorator type
    String preference
    String countryCallingCode
    String number
    String extension

    static PhoneV6 createPhoneV6(PersonTelephone personTelephone, String phoneTypeGuid, String hedmPhoneType) {
        PhoneV6 phoneV6 = new PhoneV6()
        phoneV6.countryCallingCode = personTelephone.countryPhone
        phoneV6.number = (personTelephone.phoneArea ?: "") + (personTelephone.phoneNumber ?: "")
        phoneV6.extension = personTelephone.phoneExtension
        phoneV6.type = new PhoneTypeDecorator(personTelephone.telephoneType?.code, personTelephone.telephoneType?.description, phoneTypeGuid, hedmPhoneType)
        if (personTelephone.primaryIndicator) {
            phoneV6.preference = 'primary'
        }
        return phoneV6
    }

    /**
     * For 'person-guardians' V7 schema
     *
     * @return
     */
    def getPhoneTypeDetail() {
        return ["phoneType": type.phoneType, "detail": type.getDetail()]
    }


}
