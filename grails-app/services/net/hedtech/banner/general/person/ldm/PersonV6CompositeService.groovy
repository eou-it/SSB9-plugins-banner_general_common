/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm

import net.hedtech.banner.general.overall.ldm.LdmService
import net.hedtech.banner.general.person.ldm.v6.PersonBulider as PersonBuliderV6
import org.springframework.transaction.annotation.Transactional

@Transactional
class PersonV6CompositeService extends LdmService {

    Map list(Map params, List personList, Boolean studentRole){
        BasePersonBuilder personBuilderV6 = new PersonBuliderV6()
        return personBuilderV6.build(personList, studentRole)
    }
}
