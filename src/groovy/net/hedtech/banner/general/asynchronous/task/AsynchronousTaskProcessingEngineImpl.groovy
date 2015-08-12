/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.asynchronous.task

import grails.util.Holders
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.asynchronous.AsynchronousActionPoolThreadFactory
import net.hedtech.banner.general.asynchronous.AsynchronousBannerAuthenticationSpoofer
import net.hedtech.banner.general.communication.CommunicationErrorCode
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.annotation.Required
import org.springframework.transaction.TransactionException

import java.util.concurrent.*

/**
 * A job processing engine that processes jobs asynchronously, while providing a
 * high Quality of Service (aka will not lose jobs).
 *
 * @author charlie hardt (Very significantly based upon work by Shane Riddell)
 */
public class AsynchronousTaskProcessingEngineImpl implements AsynchronousTaskProcessingEngine, DisposableBean {

    private final Log log = LogFactory.getLog(this.getClass());

    /*
     * Configuration Bean that this engine will work with
     */
    private AsynchronousTaskConfiguration config;

    /**
     * The job manager for which this processing engine is configured to support.
     */
    protected AsynchronousTaskManager jobManager;

    /**
     * Time in milliseconds between polling for events.
     */
    private long pollingInterval = 5 * 1000;

    /**
     * If true, and the last poll found jobs to perform, immediately poll again for more jobs.
     * Otherwise, always wait for the pollingInterval before checking for more jobs.
     */
    private boolean continuousPolling = false;

    /**
     * Maximum queue size.
     */
    private int maxQueueSize = 5000;

    /**
     * Indicator of whether or not to delete successfully completed jobs, or
     * just mark them as completed.
     */
    private boolean deleteSuccessfullyCompleted = false;

    /**
     * The polling thread.
     */
    private PollingThread pollingThread;

    /**
     * Internal queue of jobs that have been polled and placed into the executor queue, but that have not
     * fully completed processing.  Used to prevent the polling thread from pushing the same job to the
     * executor more than once.
     * <p/>
     * Before submitting a job to the executor, the polling thread will first place the PK of the job
     * into this set.  If the executor rejects the job, the polling thread immediately removes it from the set.
     * If the executor accepts the job, then its Handler is responsible for ensuring that the job's PK is removed
     * from the pendingJobs set no matter what the outcome of processing the job is.  In this case, that means that
     * the Handler removes the job as the last thing it does on if processing is successfuly.  If processing is not
     * sucessfuly, then the ErrorExecutor is given an ErrorHandler wrapping the job, and the ErrorHandler ensure that
     * the job is removed from the pendingJobs set once the error handling is complete.
     */
    private Set pendingJobs = Collections.synchronizedSet(new HashSet());

    /**
     * The monitor thread.
     */
    private MonitorThread monitorThread;

    /**
     * Executor service for processing jobs.
     * This must use an unlimited size blocking queue (i.e., it must never reject a submitted job,
     * otherwise the job processing engine may stop processing work altogether).
     */
    private ExecutorService executor;

    /**
     * Executor service for marking jobs as failed.
     */
    private ExecutorService errorExecutor;

    /**
     * Tracks if the polling thread and thread pools are running
     */
    public boolean threadsRunning = false;

    private int maxThreads

    private boolean isDisabled = false

    private AsynchronousBannerAuthenticationSpoofer asynchronousBannerAuthenticationSpoofer

//  ------------------------- Initialization Method(s) -------------------------

    /**
     * Initializes the job processing engine.  This method starts the polling process.
     */
    public void init() {
        isDisabled = Holders.config.communication.engine.isEnabled ? false : true
        log.info("Initialized with isDisabled = ${isDisabled}, maxThreads = ${maxThreads}, maxQueueSize = ${maxQueueSize}, continuousPolling = ${continuousPolling}, pollingInterval = ${pollingInterval}, and deleteSuccessfullyCompleted = ${deleteSuccessfullyCompleted}.")
    }

    /**
     * Brings down the processing engine by instructing the thread pools to gracefully
     * shutdown.
     */
    public void shutdown() {
        stopRunning();
    }

//  -------------------------- Getters and Setters -----------------------------

    /* (non-Javadoc)
     * @see net.hedtech.banner.general.services.commsupport.jobs.JobProcessingEngine#setJobManager(net.hedtech.banner.general.services.commsupport.jobs.JobManager)
     */


    @Required
    public void setJobManager(AsynchronousTaskManager jobManager) {
        this.jobManager = jobManager;
    }


    @Required
    void setAsynchronousBannerAuthenticationSpoofer(asynchronousBannerAuthenticationSpoofer) {
        this.asynchronousBannerAuthenticationSpoofer = asynchronousBannerAuthenticationSpoofer
    }


    @Override
    void destroy() throws Exception {
        log.info("Calling disposable bean method.");
        this.stopRunning()
    }

//  ----------------------- JobProcessingEngine Method(s) ----------------------

    /* (non-Javadoc)
     * @see net.hedtech.banner.general.services.commsupport.jobs.JobProcessingEngine#startRunning()
     */


    public void startRunning() {
        if (isDisabled) {
            log.warn("Asynchronous Task Processing engine disabled in configuration; will not start");
            return;
        }

        log.info("Asynchronous Task Processing engine starting.");

        if (!threadsRunning) {
            try {
                monitorThread = new MonitorThread();
                pollingThread = new PollingThread();
                executor = new ThreadPoolExecutor(getMaxProcessingThreads(),
                        getMaxProcessingThreads(),
                        0L, TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue(maxQueueSize),
                        new AsynchronousActionPoolThreadFactory(jobManager.getJobType().getSimpleName() + "-GUID" + UUID.randomUUID()));
                // error handling pool is single threaded, unbounded queue, as we expect the work to be done by
                // this pool to be very small (ideally, no work)
                errorExecutor = Executors.newFixedThreadPool(1);
            } catch (ApplicationException e) {
                log.fatal("JobProcessingEngine for " + jobManager.getJobType() + " caught " + e, e);
                throw new RuntimeException(e);
            }
            pollingThread.start();
            threadsRunning = true;
        }
        log.info("JobProcessingEngine " + this + " has started running ");
    }

    /* (non-Javadoc)
     * @see net.hedtech.banner.general.services.commsupport.jobs.JobProcessingEngine#stopRunning()
     */


    public void stopRunning() {
        if (threadsRunning) {
            pollingThread.stopRunning();
            monitorThread.stopRunning();
            synchronized (pollingThread) {
                pollingThread.notify();
            }
            while (pollingThread.isRunning()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                }
            }
            executor.shutdown();
            errorExecutor.shutdown();
            while (!executor.isTerminated()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                }
            }
            while (!errorExecutor.isTerminated()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                }
            }
            threadsRunning = false;
        }
        log.info("JobProcessingEngine " + this + " has shutdown.");
    }

    /**
     * Returns true if the manager's polling thread and thread pools are running.
     * Will be true while the manager is shutting down as well.  Once it returns false, this instance and all its
     * managed threads have cleanly terminated.
     */
    public boolean isRunning() {
        return threadsRunning;
    }

//  ----------------------------- Object Methods -------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append("<Configured for: ").append(jobManager.getClass()).append("> ");
        return sb.toString();
    }


    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads
    }


    public void setMaxQueueSize(int maxQueueSize) {
        this.maxQueueSize = maxQueueSize
    }


    public void setContinuousPolling(boolean continuousPolling) {
        this.continuousPolling = continuousPolling
    }


    public void setPollingInterval(int pollingInterval) {
        this.pollingInterval = pollingInterval
    }


    public void setDeleteSuccessfullyCompleted(boolean deleteSuccessfullyCompleted) {
        this.deleteSuccessfullyCompleted = deleteSuccessfullyCompleted
    }
//---------------------------- protected methods -------------------------------
    /*
     * Provide ability for subclasses to override the configuration settings in
     * RecrutitingConfiguration.xml for MAX processing threads.
     */


    protected int getMaxProcessingThreads() {
        return maxThreads
    }

    /**
     * Polls the job manager for new jobs to process, and then processes any
     * available jobs.
     */
    private boolean poll() {
        log.debug("polling");

        boolean found = false;
        //to avoid putting duplicate jobs into the executor queue, we will only poll if all
        //the jobs enqueued from the previous poll have run to completion
        if (pendingJobs.size() > 0) {
            log.debug("Pending jobs still queued (size=${pendingJobs.size()}).")
            return false
        } else {
            log.debug("Get more pending jobs")
        }
        try {
            List jobs = jobManager.getPendingJobs(maxQueueSize);
            if (log.isDebugEnabled()) {
                log.debug("Found " + jobs.size() + " jobs for processing using maxQueueSize " + maxQueueSize);
            }
            found = (jobs.size() > 0);

            for (AsynchronousTask job : jobs) {
                pendingJobs.add(job.getId());
                try {
                    executor.execute(new AsynchronousTaskHandler(job) {
                        @Override
                        void run() {
                            log.debug("Going to make it rock")
                            handleTask(getJob())
                        }
                    })

                } catch (RejectedExecutionException e) {
                    pendingJobs.remove(job.getId());
                    log.warn("JobProcessingEngine " + this + " handler queue is currently saturated", e);
                    break;
                }
            }
        } catch (TransactionException e) {
            log.error("error polling", e);
        } catch (Throwable t) {
            log.error("error polling", t);
        }
        return found;
    }


    void handleTask(AsynchronousTask job) {
        log.debug("Asynchronous Task Processing Engine handler will process job " + job.getId());
        try {
            asynchronousBannerAuthenticationSpoofer.authenticateAndSetFormContextForExecute()

            // This is a short-lived transactional method, and if successful the job has been marked as acquired.
            log.debug("Acquiring job " + job.getId())
            boolean acquired = jobManager.acquire(job);
            if (!acquired) return;

            if (log.isDebugEnabled()) {
                log.debug("JobProcessingEngine " + this + " - A handler has successfully processed job " + job.getId());
            }

            monitorThread.register(new AsynchronousTaskMonitorRecord(Thread.currentThread().getName(), job.getId()));

            // TODO: Add a map of the job to this thread in a monitor table, so that a monitor
            // thread can check to make sure this thread is alive, and update that monitor record's
            // 'runningAsOf' timestamp.  If the monitor thread cannot find this thread, it would know that
            // this thread did not successfully complete it's task (or at least never removed it's map from
            // the monitor table)

            // This is the potentially long-running and non-transactional processing...
            log.debug("Processing job " + job.getId())
            jobManager.process(job);

            // Now we'll mark the job as complete using another short transactional method
            if (deleteSuccessfullyCompleted) {
                log.debug("Deleting job " + job.getId())
                jobManager.delete(job);
            } else {
                log.debug("Marking complete job " + job.getId())
                jobManager.markComplete(job);
            }
            monitorThread.deregister(new AsynchronousTaskMonitorRecord(Thread.currentThread().getName(), job.getId()));
            //Job has been successfully completed.  As the very last step, we remove it from the pendingJobs set
            //to signal the polling thread that when all pending work has been completed, it should fetch more work.
            pendingJobs.remove(job.getId());


        } catch (ApplicationException e) {
            log.error("JobProcessingEngine " + this + " - A handler failed processing for job " + job.getId() + ", the job will be marked as failed", e);

            errorExecutor.execute(new ErrorHandler(job, e));

        } catch (Throwable t) {
            log.error("Async job handler caught an unexpected error and will mark the job as having an error state")
            //log.error( "JobProcessingEngine " + this + " - A handler encountered a Throwable for job " + job.getId() + ", and will mark it as failed", t );

            // Any other exceptions will be handled by another runnable, regardless
            // of the above handling for the application exception.  This may
            // facilitate alternative processing for system exceptions - for e.g., putting
            // them back onto the queue for later processing... but for now, we'll just
            // mark them as failed (just like the process method does for application exceptions above).
            errorExecutor.execute(new ErrorHandler(job, t));
        } finally {
            //We are finished, one way or another, with working on this job.  Remove it from the
            //pending jobs set to signal the polling queue that when all pending work is completed,
            //it should grab another block.
            pendingJobs.remove(job.getId());
        }
    }

// =============================== Inner Classes ===============================


    private class PollingThread extends Thread {

        private boolean isRunning = false;
        private boolean keepRunning = true;
        /**
         * Run method for the thread.
         */
        @Override
        public void run() {
            asynchronousBannerAuthenticationSpoofer.authenticateAndSetFormContextForExecute()
            boolean foundItems = false;
            while (keepRunning) {
                isRunning = true;
                foundItems = poll();
                if (!foundItems || !continuousPolling) {
                    pause(pollingInterval);
                }
            }
            isRunning = false;
        }


        void stopRunning() {
            keepRunning = false;
        }


        boolean isRunning() {
            return isRunning;
        }

        /**
         * Pauses this thread for the specified time.
         * @param time the time in milliseconds to pause
         */
        private void pause(long time) {
            try {
                synchronized (this) {
                    //check the keepRunning flag here - race conditions could have set the flag and issued a notify, and
                    //if we reach this point after this has happened, we wait the full pause time even if the thread
                    //has been ordered to shutdown.  so we only wait if we are still running
                    if (keepRunning) {
                        wait(time);
                    }
                }
            } catch (InterruptedException e) {
            }
        }
    }


    private class MonitorThread extends Thread {

        private boolean _keepRunning = true;
        private final ArrayList monitoredThreads = new ArrayList();
        private static final long DEFAULT_UPDATE_INTERVAL = 60 * 1000;
        private long _updateInterval = DEFAULT_UPDATE_INTERVAL;
        private final Log log = LogFactory.getLog(MonitorThread.class.getName());


        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            while (_keepRunning) {
                asynchronousBannerAuthenticationSpoofer.authenticateAndSetFormContextForExecute()
                ArrayList monitored = null;
                synchronized (monitoredThreads) {
                    monitored = (ArrayList) monitoredThreads.clone();
                }
                for (AsynchronousTaskMonitorRecord thread : monitored) {
                    update(thread);
                }
                synchronized (this) {
                    try {
                        wait(_updateInterval);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }


        void stopRunning() {
            _keepRunning = false;
            synchronized (this) {
                notify();
            }
        }


        void setUpdateInterval(long interval) {
            _updateInterval = interval;
            synchronized (this) {
                notify();
            }
        }


        void setUpdateIntervalToDefault() {
            setUpdateInterval(DEFAULT_UPDATE_INTERVAL);
        }


        void register(AsynchronousTaskMonitorRecord thread) {
            if (log.isDebugEnabled()) {
                log.debug("Monitor thread adding " + thread);
            }
            synchronized (monitoredThreads) {
                monitoredThreads.add(thread);
            }
        }


        void deregister(AsynchronousTaskMonitorRecord thread) {
            if (log.isDebugEnabled()) {
                log.debug("Monitor thread removing " + thread);
            }
            synchronized (monitoredThreads) {
                monitoredThreads.remove(thread);
            }
        }


        private void update(AsynchronousTaskMonitorRecord monitorRecord) {
            if (log.isDebugEnabled()) {
                log.debug("Monitor thread will update " + monitorRecord.toString());
            }
            try {
                jobManager.updateMonitorRecord(monitorRecord);
            } catch (Throwable t) {
                log.error("Critical failure while monitoring an automated activity", t);
            }
        }
    }


    private class ErrorHandler implements Runnable {
        private final AsynchronousTask job;
        private final Throwable cause;

        ErrorHandler(AsynchronousTask job, Throwable cause) {
            this.job = job;
            this.cause = cause;
        }


        public void run() {
            asynchronousBannerAuthenticationSpoofer.authenticateAndSetFormContextForExecute()
            try {
//                  ThreadCallerContext.set( new TrustedCallerContext() );
                if(cause instanceof ApplicationException) {
                    jobManager.markFailed(job, cause.getType(), cause );
                }
                else
                {
                    jobManager.markFailed(job, CommunicationErrorCode.UNKNOWN_ERROR.name(), cause);
                }
            } catch (Throwable t) {
                log.error("JobProcessingEngine " + this + " - An error handler could not mark job " + job.getId() + " as in error", t);
            } finally {
//                  ThreadCallerContext.set( cc );
            }
        }
    }

}
