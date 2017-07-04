/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.system.ldm.v6

import net.hedtech.banner.general.system.EducationalInstitutionView

class EducationalInstitutionV6 {

    String guid
    String title
    String type
    String homeInstitution
    List<EducationalInstitutionAddressV6> addresses

    public static class EducationalInstitutionAddressV6 {
        def address
        def type

        EducationalInstitutionAddressV6(def address, def type) {
            this.address = address
            this.type = type
        }
    }

    EducationalInstitutionV6(EducationalInstitutionView educationalInstitutionView, String addressType) {
        this.guid = educationalInstitutionView.id
        this.title = educationalInstitutionView.title
        this.type = educationalInstitutionView.type
        this.homeInstitution = educationalInstitutionView.homeInstitution
        if (educationalInstitutionView.addressGuid) {
            EducationalInstitutionAddressV6 address1 = new EducationalInstitutionAddressV6([id: educationalInstitutionView.addressGuid], [addressType: addressType])
            this.addresses = [address1]
        }
    }

}
