/*********************************************************************************
 Copyright 2014-2015 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall.ldm

import net.hedtech.banner.service.ServiceBase
import net.hedtech.banner.exceptions.ApplicationException
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
class SectionMeetingTimeHedmDetailService extends ServiceBase {
    public def create( domainModelOrMap, flushImmediately = true ) {
        throwUnsupportedException()
    }

    public def create( List domainModelsOrMaps, flushImmediately = true ) {
        throwUnsupportedException()
    }

    public def createOrUpdate( domainModelOrMap, flushImmediately = true ) {
        throwUnsupportedException()
    }

    public def createOrUpdate( List domainModelsOrMaps, flushImmediately = true ) {
        throwUnsupportedException()
    }

    public def update( domainModelOrMap, flushImmediately = true ) {
        throwUnsupportedException()
    }

    public def update( List domainModelsOrMaps, flushImmediately = true ) {
        throwUnsupportedException()
    }

    public def delete( domainModelOrMapOrId, flushImmediately = true ) {
        throwUnsupportedException()
    }

    public def delete( List domainModelsOrMapsOrIds, flushImmediately = true ) {
        throwUnsupportedException()
    }

    private void throwUnsupportedException(){
        throw new ApplicationException(SectionMeetingTimeHedmDetail.class, "unsupported.operation")
    }
}
