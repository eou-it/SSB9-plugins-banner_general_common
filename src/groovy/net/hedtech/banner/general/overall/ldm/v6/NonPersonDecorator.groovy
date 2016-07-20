/** *******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.overall.ldm.v6

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.person.ldm.v6.EmailV6
import net.hedtech.banner.general.person.ldm.v6.PhoneV6
import net.hedtech.banner.general.person.ldm.v6.RoleV6

@EqualsAndHashCode(includeFields = true)
@ToString(includeNames = true, includeFields = true)
class NonPersonDecorator {

    String guid
    String title
    List<PersonCredential> credentials
    List<EmailV6> emails
    List<PhoneV6> phones
    List<RoleV6> roles

}
