/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.groupsend

import net.hedtech.banner.general.asynchronous.AsynchronousBannerAuthenticationSpoofer
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.annotation.Required
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException

/**
 * Created by mbrzycki on 12/5/14.
 */
class CommunicationGroupSendMonitor implements DisposableBean {
    private Log log = LogFactory.getLog(this.getClass())
    private CommunicationGroupSendMonitorThread monitorThread
    private CommunicationGroupSendService communicationGroupSendService
    private CommunicationGroupSendItemService communicationGroupSendItemService
    private CommunicationGroupSendCompositeService communicationGroupSendCompositeService
    private AsynchronousBannerAuthenticationSpoofer asynchronousBannerAuthenticationSpoofer
    private int monitorIntervalInSeconds = 10

    @Required
    void setMonitorIntervalInSeconds(int monitorIntervalInSeconds) {
        this.monitorIntervalInSeconds = monitorIntervalInSeconds
    }

    int getMonitorIntervalInSeconds() {
        return monitorIntervalInSeconds
    }

    @Required
    void setAsynchronousBannerAuthenticationSpoofer(AsynchronousBannerAuthenticationSpoofer asynchronousBannerAuthenticationSpoofer) {
        this.asynchronousBannerAuthenticationSpoofer = asynchronousBannerAuthenticationSpoofer
    }


    @Required
    public void setCommunicationGroupSendService(CommunicationGroupSendService communicationGroupSendService) {
        this.communicationGroupSendService = communicationGroupSendService
    }


    @Required
    public void setCommunicationGroupSendItemService(CommunicationGroupSendItemService communicationGroupSendItemService) {
        this.communicationGroupSendItemService = communicationGroupSendItemService
    }


    @Required
    public void setCommunicationGroupSendCompositeService(CommunicationGroupSendCompositeService communicationGroupSendCompositeService) {
        this.communicationGroupSendCompositeService = communicationGroupSendCompositeService
    }


    public void init() {
        log.info("Initialized.");

    }


    @Override
    void destroy() throws Exception {
        println "IN THE DESTROY OF THE MONITOR THREAD"
        log.info("Calling disposable bean method.");
        if (monitorThread) {
            monitorThread.stopRunning()
        }
    }


    public void startMonitoring() {
        log.info("Monitor thread started.")
        if (!monitorThread) {
            monitorThread = new CommunicationGroupSendMonitorThread(this);
        }
        monitorThread.start();
    }


    public void shutdown() {
        println "SHUTTING DOWN THE MONITOR THREAD"
        log.debug("Shutting down.");
        if (monitorThread) {
            monitorThread.stopRunning();
            try {
                println "Now joining the thread and waiting"
                this.monitorThread.join(10000);
            } catch (InterruptedException e) {
            }
        }
        println "The monitor thread has been stopped"
        monitorThread = null
    }


    public void monitorGroupSends() {
        if (log.isDebugEnabled()) log.debug("Checking group sends for status updates.")
        // begin setup
        asynchronousBannerAuthenticationSpoofer.authenticateAndSetFormContextForExecute()
        try {
            List<CommunicationGroupSend> groupSendList = CommunicationGroupSend.findRunning()
            if (log.isDebugEnabled()) log.debug("Running group send count = " + groupSendList.size() + ".");

            for (CommunicationGroupSend groupSend : groupSendList) {
                if (groupSend.currentExecutionState.equals(CommunicationGroupSendExecutionState.Processing)) {
                    int runningCount = communicationGroupSendItemService.fetchRunningGroupSendItemCount(groupSend.id)
                    if (runningCount == 0) {
                        completeGroupSend( groupSend.id )
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace()
            log.error(t)
        }
    }

    /**
     * Calls the compete group send method of service class.
     * @param groupSendId the id of the group send.
     * @return the updated group send
     */
    private CommunicationGroupSend completeGroupSend( Long groupSendId ) {
        if (log.isDebugEnabled()) log.debug( "Completing group send with id = " + groupSendId + "." )

        int retries = 2
        while(retries > 0) {
            retries--
            try {
                return communicationGroupSendCompositeService.completeGroupSend( groupSendId )
            } catch (HibernateOptimisticLockingFailureException e) {
                if (retries == 0) {
                    throw e
                }
            }
        }
    }
}
