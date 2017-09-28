/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.jobsub

import grails.transaction.Transactional
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.service.ServiceBase

@Transactional
class JobsubSavedOutputService extends ServiceBase {


    void preCreate(map) {
        throwUnsupportedException()
    }


    void preDelete(map) {
        throwUnsupportedException()
    }


    void throwUnsupportedException() {
        throw new ApplicationException(JobsubSavedOutputService, "@@r1:unsupported.operation@@")
    }


    void preUpdate(map) {
        def jobOutput = map.domainModel
        //only print date can be updated
        if (jobOutput.isDirty("job") ||
                jobOutput.isDirty("printer") ||
                jobOutput.isDirty("printForm") ||
                jobOutput.isDirty("oneUpNo") ||
                jobOutput.isDirty("mime") ||
                jobOutput.isDirty("createDate") ||
                jobOutput.isDirty("fileName") ||
                jobOutput.isDirty("creatorId")) {
            throw new ApplicationException(JobsubSavedOutputService, "@@r1:unsupported.update@@")
        }

    }


}
