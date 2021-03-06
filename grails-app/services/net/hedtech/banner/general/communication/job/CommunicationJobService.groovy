/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.job

import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import net.hedtech.banner.service.ServiceBase

/**
 *  DAO service interface for communication group send item objects.
 */
@Slf4j
@Transactional
class CommunicationJobService extends ServiceBase {
   // private final Log log = LogFactory.getLog(this.getClass());

    def preCreate( domainModelOrMap ) {
        CommunicationJob job = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationJob
        if (job.getCreationDateTime() == null) {
            job.setCreationDateTime(new Date())
        }
    }

    public List fetchPending( Integer max = Integer.MAX_VALUE ) {
        List found = CommunicationJob.fetchPending( max )
        log.debug( "Found ${found.size()} pending communication jobs." )
        return found
    }

    /**
     * Returns true if the communication job was acquired for the current thread.
     */
    public boolean acquire( Long jobId ) {
        log.debug( "Attempting to acquire communication job id = ${jobId}.")
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        try {
            int rows = sql.executeUpdate("update GCBCJOB set GCBCJOB_STATUS = ? where GCBCJOB_SURROGATE_ID = ? and GCBCJOB_STATUS = ? ",
                [ CommunicationJobStatus.DISPATCHED.toString(), jobId, CommunicationJobStatus.PENDING.toString() ] )
            if (rows == 1) {
                log.debug( "Communication job withid = ${jobId} acquired" )
                return true
            } else if (rows == 0) {
                log.debug( "Communication job withid = ${jobId} not available." )
                return false
            } else {
                log.error( "CommunicationJobService.acqure found more than one record with job id = ${jobId}." )
                throw new RuntimeException( "CommunicationJobService.acquire aquire found ${rows} with job id = ${jobId} and status = ${CommunicationJobStatus.PENDING.toString()}." )
            }
        } catch (Exception e) {
            log.error( e.message )
            throw e
        } finally {
//            sql?.close()
        }
    }

    /**
     * Returns true if the communication job was acquired for the current thread.
     */
    public void markCompleted( Long jobId ) {
        log.debug( "Attempting to mark communication job id = ${jobId} as completed.")
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        try {
            int rows = sql.executeUpdate("update GCBCJOB set GCBCJOB_STATUS = ? where GCBCJOB_SURROGATE_ID = ?",
                [ CommunicationJobStatus.COMPLETED.toString(), jobId ] )
            if (rows == 0) {
                log.debug( "No communication job with id = ${jobId} to update.")
            } else if (rows == 1) {
                log.debug( "Communication job with id = ${jobId} marked as completed." )
            } else if (rows > 1) {
                log.error( "CommunicationJobService.markCompleted updated more than one record with job id = ${jobId}." )
                throw new RuntimeException( "CommunicationJobService.markCompleted attempted to update ${rows} with job id = ${jobId}." )
            }
        } catch (Exception e) {
            log.error( e.message )
            throw e
        } finally {
//            sql?.close()
        }
    }


}
