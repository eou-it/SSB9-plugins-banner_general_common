/*********************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.item

import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import net.hedtech.banner.general.communication.job.CommunicationJobStatus
import net.hedtech.banner.service.ServiceBase

import java.sql.Connection
import java.sql.PreparedStatement

/**
 *  DAO service interface for communication group send item objects.
 */
@Slf4j
@Transactional
class CommunicationSendItemService extends ServiceBase {

    def preCreate( domainModelOrMap ) {
        CommunicationSendItem sendItem = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationSendItem
        if (sendItem.getCreationDateTime() == null) {
            sendItem.setCreationDateTime(new Date())
        }
    }

    public List fetchPending( Integer max = Integer.MAX_VALUE ) {
        List found = CommunicationSendItem.fetchPending( max )
        log.debug( "Found ${found.size()} pending communication send items." )
        return found
    }

    /**
     * Returns true if the communication send item was acquired for the current thread.
     */
    public boolean acquire( Long sendItemId ) {
        log.debug( "Attempting to acquire communication send item id = ${sendItemId}.")
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        try {
            int rows = sql.executeUpdate("update GCRSITM set GCRSITM_STATUS = ? where GCRSITM_SURROGATE_ID = ? and GCRSITM_STATUS = ?",
                    [CommunicationJobStatus.DISPATCHED.toString(), sendItemId, CommunicationJobStatus.PENDING.toString() ] )
            if (rows == 1) {
                log.debug( "Communication send item withid = ${sendItemId} acquired" )
                return true
            } else if (rows == 0) {
                log.debug( "Communication send item with id = ${sendItemId} not available." )
                return false
            } else {
                log.error( "CommunicationSendItemService.acquire found more than one record with send item id = ${sendItemId}." )
                throw new RuntimeException( "CommunicationSendItemService.acquire acquire found ${rows} with send item id = ${sendItemId} and status = ${CommunicationJobStatus.PENDING.toString()}." )
            }
        } catch (Exception e) {
            log.error( e.message )
            throw e
        } finally {
//            sql?.close()
        }
    }

    /**
     * Returns true if the communication send item was acquired for the current thread.
     */
    public void markCompleted( Long sendItemId ) {
        log.debug( "Attempting to mark communication send item id = ${sendItemId} as completed.")
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        try {
            int rows = sql.executeUpdate("update GCRSITM set GCRSITM_STATUS = ? where GCRSITM_SURROGATE_ID = ?",
                    [ CommunicationJobStatus.COMPLETED.toString(), sendItemId ] )
            if (rows == 0) {
                log.debug( "No communication send item with id = ${sendItemId} to update.")
            } else if (rows == 1) {
                log.debug( "Communication send item with id = ${sendItemId} marked as completed." )
            } else if (rows > 1) {
                log.error( "CommunicationSendItemService.markCompleted updated more than one record with send item id = ${sendItemId}." )
                throw new RuntimeException( "CommunicationSendItemService.markCompleted attempted to update ${rows} with send item id = ${sendItemId}." )
            }
        } catch (Exception e) {
            log.error( e.message )
            throw e
        } finally {
//            sql?.close()
        }
    }

    /**
     * Returns true if the communication send item was acquired for the current thread.
     */
    public void markCompleted( List<Long> sendItemIds ) {
        log.debug( "Attempting to mark communication send item id = ${sendItemIds} as completed.")
        Connection connection = sessionFactory.getCurrentSession().connection()
//        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        PreparedStatement preparedStatement = connection.prepareStatement("update GCRSITM set GCRSITM_STATUS = ?, GCRSITM_SENT_DATE = SYSDATE where GCRSITM_SURROGATE_ID = ?")
        try {
            for(Long sendItemId : sendItemIds) {
                preparedStatement.setString(1, CommunicationJobStatus.COMPLETED.toString())
                preparedStatement.setLong(2, sendItemId)
                preparedStatement.addBatch()
            }
            int[] rows = preparedStatement.executeBatch()
            if (rows) {
                log.debug( "Communication send item with id = ${sendItemIds} marked as completed." )
            }
        } catch (Exception e) {
            log.error( e.message )
            throw e
        } finally {
            preparedStatement.close()
//            sql?.close()
        }
    }

    public List fetchPendingTextMessages( Integer max = Integer.MAX_VALUE ) {
        List found = CommunicationSendItem.fetchPendingTextMessages( max )
        log.debug( "Found ${found.size()} pending communication send text message items." )
        return found
    }
}
