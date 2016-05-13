/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm

import net.hedtech.banner.general.overall.ldm.GlobalUniqueIdentifierService
import net.hedtech.banner.general.system.ldm.v6.Person


public abstract class BasePersonBuilder {

    static final String ldmName = 'persons'

    GlobalUniqueIdentifierService globalUniqueIdentifierService

    public BasePersonBuilder(){
        super()
    }

    public abstract Map build(List personList, Boolean studentRole)

    Map buildPersonGuids(List domainIds, Map persons) {
        globalUniqueIdentifierService.fetchByLdmNameAndDomainIds(ldmName, domainIds).each { guid ->
            Person currentRecord = persons.get(guid.domainKey.toInteger())
            currentRecord.guid = guid.guid
            persons.put(guid.domainKey.toInteger(), currentRecord)
        }
        return persons
    }
}
