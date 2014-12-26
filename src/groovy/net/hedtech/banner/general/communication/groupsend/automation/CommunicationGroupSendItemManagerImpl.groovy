/*******************************************************************************

 � 2012 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD SCT AND IS NOT TO BE COPIED,
 REPRODUCED, LENT, OR DISPOSED OF, NOR USED FOR ANY PURPOSE OTHER THAN THAT
 WHICH IT IS SPECIFICALLY PROVIDED WITHOUT THE WRITTEN PERMISSION OF THE
 SAID COMPANY
 *******************************************************************************/
package net.hedtech.banner.general.communication.groupsend.automation;


import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.asynchronous.task.AsynchronousTask;
import net.hedtech.banner.general.asynchronous.task.AsynchronousTaskManager
import net.hedtech.banner.general.asynchronous.task.AsynchronousTaskMonitorRecord
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSendItem
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSendItemService
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Required
import org.springframework.transaction.annotation.Transactional;

/**
 * A job manager for asynchronous processing of group send items.
 *
 * @author Michael Brzycki
 */
public class CommunicationGroupSendItemManagerImpl implements AsynchronousTaskManager {

    private final Log log = LogFactory.getLog(this.getClass());

    /**
     * Used for testing purposes only.  If this is not null when a job is being
     * processed, that processing will throw this exception.
     */
    private Exception simulatedFailureException;
    private CommunicationGroupSendItemService communicationGroupSendItemService;

    @Required
    public void setCommunicationGroupSendItemService(CommunicationGroupSendItemService communicationGroupSendItemService) {
        this.communicationGroupSendItemService = communicationGroupSendItemService
    }

    public Class<CommunicationGroupSendItem> getJobType() {
        return GroupSendItem.class;
    }

    public AsynchronousTask create(AsynchronousTask job) throws ApplicationException {
        throw new NotImplementedException();
    }

    public void init() {
        log.debug( "Initialized." );
    }

    /**
     * Deletes an existing communication job from the persistent store.
     * @param job the communication job to remove
     */
    @Transactional
    public void delete( AsynchronousTask jobItem )  throws ApplicationException {
        CommunicationGroupSendItem groupSendItem = jobItem as CommunicationGroupSendItem
        communicationGroupSendItemService.delete( groupSendItem.getPrimaryKey() );
        if (log.isDebugEnabled()) {
            log.debug( "GroupSendItemManagerImpl deleted group send item " + groupSendItem.getPrimaryKey().getKeyValue() );
        }
    }


    /**
     * Returns jobs that have failed.
     * @return List<CommunicationJob> the failed jobs
     */
    @Transactional
    public List getFailedJobs() {
        throw new NotImplementedException();
    }

    // This method is called often from a polling thread, so it is imperative that
    // it be kept as performant as possible -- e.g., be careful about logging at debug level or higher
    /**
     * Returns pending jobs, sorting by oldest first.
     * @param max maximum number of jobs to return.
     * @return
     */
    @Transactional
    public List<CommunicationGroupSendItem> getPendingJobs( int max ) throws ApplicationException {
        List<CommunicationGroupSendItem> result = communicationGroupSendItemService.getPending( max );
        communicationGroupSendItemService.getNewGroupSendItemKeys( max );
        if (log.isTraceEnabled()) {
            log.trace( "GroupSendItemManagerImpl.getPending(max=" + max + ") is returning " + result.size() + " group send items." );
        }
        return result;
    }

    public boolean acquire( AsynchronousTask task ) throws ApplicationException {
        if (log.isInfoEnabled()) {
            CommunicationGroupSendItem groupSendItem = task as CommunicationGroupSendItem
            log.info( "Acquired group send item id = " + groupSendItem.getId() + ", pidm = " + groupSendItem.recipientPidm + "." )
        }
        return true;
    }


    /* (non-Javadoc)
     * @see com.sungardhe.common.services.commsupport.jobs.JobManager#markComplete(com.sungardhe.common.services.commsupport.jobs.Job)
     */
    public void markComplete( AsynchronousTask task ) throws ApplicationException {
        if (log.isInfoEnabled()) {
            CommunicationGroupSendItem groupSendItem = task as CommunicationGroupSendItem
            log.info( "Marking completed group send item id = " + groupSendItem.getId() + ", pidm = " + groupSendItem.recipientPidm + "." )
        }
    }


    /**
     * Performs work for the specified job.
     * @return a boolean indicating whether the job could be processed or not.  If true, the service was
     *         able to obtain an exclusive lock on the job and process it.  If false, the job was either
     *         already processed, or locked by another thread, and the call returned without doing any work
     */
    @Transactional
    public void process( AsynchronousTask task) throws ApplicationException {
        if (log.isInfoEnabled()) {
            CommunicationGroupSendItem groupSendItem = task as CommunicationGroupSendItem
            log.info( "Processing group send item id = " + groupSendItem.getId() + ", pidm = " + groupSendItem.recipientPidm + "." )
        }

        try {
            if (simulatedFailureException != null) {
                throw simulatedFailureException;
            }

            communicationGroupSendItemService.performGroupSendItem( task.getPrimaryKey() );

        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug( "GroupSendItemManagerImpl.process caught exception " + e.getMessage(), e );
            }

            // we MUST re-throw as the thread which invoked this method must
            // mark the job as failed by using another thread (as the
            // thread associated with this thread will likely be rolled back)
            if (e instanceof ApplicationException) {
                throw (ApplicationException) e;
            } else {
                throw new RuntimeException( e );
            }
        }
    }


    /**
     * Marks a job as having failed.
     * @param job the job that failed
     * @param cause the cause of the failure
     */
    @Transactional //(propagation= Propagation.REQUIRES_NEW, rollbackFor = { Throwable.class })
    public void markFailed( AsynchronousTask task, Throwable cause ) throws ApplicationException {
        communicationGroupSendItemService.failGroupSendItem( groupSendItem.getPrimaryKey(), StringHelper.stackTraceToString( cause ) );
        if (log.isDebugEnabled()) {
            log.debug( "GroupSendItemManager.markFailed(task=" + groupSendItem.getPrimaryKey() + ") has marked the task as failed " );
        }
    }


    /* (non-Javadoc)
     * @see com.sungardhe.common.services.commsupport.jobs.JobManager#updateMonitorRecord(com.sungardhe.common.services.commsupport.jobs.JobMonitorRecord)
     */
    @Transactional
    public AsynchronousTaskMonitorRecord updateMonitorRecord( AsynchronousTaskMonitorRecord monitorRecord ) {
        if (log.isDebugEnabled()) {
            CommunicationGroupSendItem groupSendItem = task as CommunicationGroupSendItem
            log.debug( "Continuing to process group send item id = " + groupSendItem.getId() + ", pidm = " + groupSendItem.recipientPidm + "." )
        }

        // Not implemented for CR1. The purpose of this service method is for debug monitoring active thread
        // processing.
        return null
    }

    /**
     * Sets the manager to fail processing a job with a simulated exception.
     * Used only for unit testing scenarios.
     * Should never be invoked in a production environment
     * @param cause the exception to throw when processing a job
     */
    public void setSimulatedFailureException( Exception cause ) {
        this.simulatedFailureException = cause;
    }

}
