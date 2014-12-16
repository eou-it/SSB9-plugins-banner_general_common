/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.asynchronous.task

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.asynchronous.AsynchronousActionPoolThreadFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.commons.GrailsApplicationFactoryBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.TransactionException;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A job processing engine that processes jobs asynchronously, while providing a
 * high Quality of Service (aka will not lose jobs).
 *
 * @author charlie hardt (Very significantly based upon work by Shane Riddell)
 */
public class AsynchronousTaskProcessingEngineImpl implements AsynchronousTaskProcessingEngine {

    private final Log log = LogFactory.getLog( this.getClass() );

    /*
     * Configuration Bean that this engine will work with
     */
    private AsynchronousTaskConfiguration config;
    
    /**
     * The configuration service from which to attain configuration information.
     */
    private GrailsApplicationFactoryBean grailsApplication;

    /**
     * The job manager for which this processing engine is configured to support.
     */
    protected AsynchronousTaskManager jobManager;

    /**
     * Time in milliseconds between polling for events.
     */
    private final long pollingInterval = 5 * 1000;

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
    private Set pendingJobs = Collections.synchronizedSet( new HashSet() );

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
    private boolean threadsRunning = false;

//  ------------------------- Initialization Method(s) -------------------------
    
    /**
     * Initializes the job processing engine.  This method starts the polling process.
     */
    public void init() {
        config = getJobConfiguration();
        if (config != null) {
            this.maxQueueSize = config.getMaxQueueSize();
            this.continuousPolling = config.isContinuousPolling();
            this.deleteSuccessfullyCompleted = config.isDeleteSuccessfullyCompleted();
            if (!config.isDisabled()) {
                startRunning();
            } else {
                log.warn( " JobProcessor for job type " + config.getJobType() + " is disabled; will not start" );
            }
        } else {
            log.fatal( "JobProcessingEngine " + this + " has not been configured!" );
            throw new RuntimeException( "JobProcessingEngine " + this + " has not been configured!" );
        }

        if (log.isInfoEnabled()) {
            log.info( "JobProcessingEngine " + this + " has been initialized with jobType "
                      + config.getJobType() + ", maxThreads=" + config.getMaxThreads()
                      + ", maxQueueSize=" + this.maxQueueSize + ", continuousPolling=" + this.continuousPolling );
        }
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
    public void setJobManager( AsynchronousTaskManager jobManager ) {
        this.jobManager = jobManager;
    }


    @Required
    public void setGrailsApplication(GrailsApplicationFactoryBean grailsApplication) {
        this.grailsApplication = grailsApplication;
    }


//  ----------------------- JobProcessingEngine Method(s) ----------------------


    /* (non-Javadoc)
     * @see net.hedtech.banner.general.services.commsupport.jobs.JobProcessingEngine#startRunning()
     */
    public void startRunning() {
        if (config.isDisabled()) {
            log.warn( "Job Processing engine disabled in configuration; will not start" );
            return;
        }
        if (!threadsRunning) {
            try {
                monitorThread = new MonitorThread();
                pollingThread = new PollingThread();
                executor = new ThreadPoolExecutor( getMaxProcessingThreads(),
                                                   getMaxProcessingThreads(),
                                                   0L, TimeUnit.MILLISECONDS,
                                                   new LinkedBlockingQueue( maxQueueSize ),
                                                   new AsynchronousActionPoolThreadFactory( jobManager.getJobType().getSimpleName() + "-GUID" + UUID.randomUUID() ) );
                // error handling pool is single threaded, unbounded queue, as we expect the work to be done by
                // this pool to be very small (ideally, no work)
                errorExecutor = Executors.newFixedThreadPool( 1 );
            } catch (ApplicationException e) {
                log.fatal( "JobProcessingEngine for " + jobManager.getJobType() + " caught " + e, e );
                throw new RuntimeException( e );
            }
            pollingThread.start();
            threadsRunning = true;
        }
        log.info( "JobProcessingEngine " + this + " has started running " );
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
                try { Thread.sleep( 50 ); } catch (InterruptedException e) {}
            }
            executor.shutdown();
            errorExecutor.shutdown();
            while (!executor.isTerminated()) {
                try { Thread.sleep( 50 ); } catch (InterruptedException e) {
                }
            }
            while (!errorExecutor.isTerminated()) {
                try { Thread.sleep( 50 ); } catch (InterruptedException e) {
                }
            }
            threadsRunning = false;
        }
        log.info( "JobProcessingEngine " + this + " has shutdown." );
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
        sb.append( super.toString() );
        sb.append( "<Configured for: " ).append( jobManager.getClass() ).append( "> " );
        return sb.toString();
    }

//---------------------------- protected methods -------------------------------
    /*
     * Provide ability for subclasses to override the configuration settings in
     * RecrutitingConfiguration.xml for MAX processing threads.     
     */
    protected int getMaxProcessingThreads() {
        return config.getMaxThreads();
    }   


    /*
     * Return the JobConfiguration Bean that this engine works with
     */
    protected AsynchronousTaskConfiguration getJobConfiguration() {
        AsynchronousTaskConfiguration configuration = null;

        try {
            grailsApplication.config?.jobProcessors?.each() {  jobProcessorName ->
                if (jobProcessorName.equals( jobManager.getJobType().getSimpleName())) {
                    configuration = new AsynchronousTaskConfiguration();
                    configuration.setContinuousPolling( jobProcessorConfig.getContinuousPolling() );
                    configuration.setDeleteSuccessfullyCompleted( jobProcessorConfig.getDeleteSuccessfullyCompleted() );
                    configuration.setDisabled( jobProcessorConfig.getDisabled() );
                    configuration.setJobType( jobProcessorConfig.getJobType() );
                    configuration.setMaxQueueSize( jobProcessorConfig.getMaxQueueSize().intValue() );
                    configuration.setMaxThreads( jobProcessorConfig.getMaxThreads().intValue() );
                    configuration.setPollingInterval( jobProcessorConfig.getPollingInterval().intValue() );
                }
            }

        } catch (Exception e) {
            log.fatal( "Can't access the configuration!", e );
            throw new RuntimeException( e );
        }
        return configuration;
    }
//---------------------------- private methods ---------------------------------


    /**
       * Polls the job manager for new jobs to process, and then processes any
       * available jobs.
       */
    private boolean poll() {
        boolean found = false;
        //to avoid putting duplicate jobs into the executor queue, we will only poll if all
        //the jobs enqueued from the previous poll have run to completion
        if (pendingJobs.size() > 0) return false;
        try {
            List jobs = jobManager.getPendingJobs( maxQueueSize );
            if (log.isTraceEnabled()) {
                log.trace( "Found " + jobs.size() + " jobs for processing using maxQueueSize " + maxQueueSize );
            }
            found = (jobs.size() > 0);

            for (AsynchronousTask job : jobs) {
                pendingJobs.add( job.getPrimaryKey() );
                try {
                    executor.execute( new Handler( job ) );
                } catch (RejectedExecutionException e) {
                    pendingJobs.remove( job.getPrimaryKey() );
                    log.warn( "JobProcessingEngine " + this + " handler queue is currently saturated", e );
                    break;
                }
            }
        } catch (TransactionException e) {
            log.error( "error polling", e );
        } catch (Throwable t) {
            log.error( "error polling", t );
        }
        return found;
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
//              ThreadCallerContext.set( new TrustedCallerContext() );
              boolean foundItems = false;
              while (keepRunning) {
                  isRunning = true;
                  foundItems = poll();
                  if (!foundItems || !continuousPolling) {
                      pause( pollingInterval );
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
          private void pause( long time ) {
              try {
                  synchronized (this) {
                      //check the keepRunning flag here - race conditions could have set the flag and issued a notify, and
                      //if we reach this point after this has happened, we wait the full pause time even if the thread
                      //has been ordered to shutdown.  so we only wait if we are still running
                      if (keepRunning) {
                          wait( time );
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
          private final Log log = LogFactory.getLog( MonitorThread.class.getName() );

          @SuppressWarnings("unchecked")
          @Override
          public void run() {
              while (_keepRunning) {
                  ArrayList monitored = null;
                  synchronized (monitoredThreads) {
                      monitored = (ArrayList) monitoredThreads.clone();
                  }
                  for (AsynchronousTaskMonitorRecord thread : monitored) {
                      update( thread );
                  }
                  synchronized (this) {
                      try {
                          wait( _updateInterval );
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

          void setUpdateInterval( long interval ) {
              _updateInterval = interval;
              synchronized (this) {
                  notify();
              }
          }

          void setUpdateIntervalToDefault() {
              setUpdateInterval( DEFAULT_UPDATE_INTERVAL );
          }

          void register( AsynchronousTaskMonitorRecord thread ) {
              if (log.isDebugEnabled()) {
                  log.debug( "Monitor thread adding " + thread );
              }
              synchronized (monitoredThreads) {
                  monitoredThreads.add( thread );
              }
          }

          void deregister( AsynchronousTaskMonitorRecord thread ) {
              if (log.isDebugEnabled()) {
                  log.debug( "Monitor thread removing " + thread );
              }
              synchronized (monitoredThreads) {
                  monitoredThreads.remove( thread );
              }
          }

          private void update( AsynchronousTaskMonitorRecord monitorRecord ) {
              if (log.isDebugEnabled()) {
                  log.debug( "Monitor thread will update " + monitorRecord.toString() );
              }
              try {
                  jobManager.updateMonitorRecord( monitorRecord );
              } catch (Throwable t) {
                  log.error( "Critical failure while monitoring an automated activity", t );
              }
          }
      }


      private class Handler implements Runnable {
          private final AsynchronousTask job;

          Handler( AsynchronousTask job ) {
              this.job = job;
          }

          public void run() {
//              CallerContext cc = ThreadCallerContext.get();
              try {
                  if (log.isDebugEnabled()) {
                      log.debug( "JobProcessingEngine " + this + " Handler will process job " + job.getPrimaryKey() );
                  }
//                  ThreadCallerContext.set( new TrustedCallerContext() );

                  // This is a short-lived transactional method, and if successful the job has been marked as acquired.
                  boolean acquired = jobManager.acquire( job );
                  if (!acquired) return;

                  if (log.isDebugEnabled()) {
                      log.debug( "JobProcessingEngine " + this + " - A handler has successfully processed job " + job.getPrimaryKey() );
                  }

                  monitorThread.register( new AsynchronousTaskMonitorRecord( Thread.currentThread().getName(), job.getPrimaryKey().getKeyValue() ) );

                  // TODO: Add a map of the job to this thread in a monitor table, so that a monitor
                  // thread can check to make sure this thread is alive, and update that monitor record's
                  // 'runningAsOf' timestamp.  If the monitor thread cannot find this thread, it would know that
                  // this thread did not successfully complete it's task (or at least never removed it's map from
                  // the monitor table)

                  // This is the potentially long-running and non-transactional processing...
                  jobManager.process( job );

                  // Now we'll mark the job as complete using another short transactional method
                  if (deleteSuccessfullyCompleted) {
                      jobManager.delete( job );
                  } else {
                      jobManager.markComplete( job );
                  }
                  monitorThread.deregister( new AsynchronousTaskMonitorRecord( Thread.currentThread().getName(), job.getPrimaryKey().getKeyValue() ) );
                  //Job has been successfully completed.  As the very last step, we remove it from the pendingJobs set
                  //to signal the polling thread that when all pending work has been completed, it should fetch more work.
                  pendingJobs.remove( job.getPrimaryKey() );


              } catch (ApplicationException e) {
                  log.error( "JobProcessingEngine " + this + " - A handler failed processing for job " + job.getPrimaryKey() + ", the job will be marked as failed", e );

                  errorExecutor.execute( new ErrorHandler( job, e ) );

              } catch (Throwable t) {
                  //log.error( "JobProcessingEngine " + this + " - A handler encountered a Throwable for job " + job.getPrimaryKey() + ", and will mark it as failed", t );

                  // Any other exceptions will be handled by another runnable, regardless
                  // of the above handling for the application exception.  This may
                  // facilitate alternative processing for system exceptions - for e.g., putting
                  // them back onto the queue for later processing... but for now, we'll just
                  // mark them as failed (just like the process method does for application exceptions above).
                  errorExecutor.execute( new ErrorHandler( job, t ) );
              } finally {
                  ThreadCallerContext.set( cc );
                  //We are finished, one way or another, with working on this job.  Remove it from the
                  //pending jobs set to signal the polling queue that when all pending work is completed,
                  //it should grab another block.
                  pendingJobs.remove( job.getPrimaryKey() );
              }
          }
      }


      private class ErrorHandler implements Runnable {
          private final AsynchronousTask job;
          private final Throwable cause;

          ErrorHandler( AsynchronousTask job, Throwable cause ) {
              this.job = job;
              this.cause = cause;
          }

          public void run() {
//              CallerContext cc = ThreadCallerContext.get();
              try {
//                  ThreadCallerContext.set( new TrustedCallerContext() );
                  jobManager.markFailed( job, cause );
              } catch (Throwable t) {
                  log.error( "JobProcessingEngine " + this + " - An error handler could not mark job " + job.getPrimaryKey() + " as in error", t );
              } finally {
                  ThreadCallerContext.set( cc );
              }
          }
      }

}