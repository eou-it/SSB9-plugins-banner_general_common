/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.communication.groupsend

import org.apache.log4j.Logger

/**
 * Thread class to monitor the group send cumulative status
 * and update it to Completed if all the corresponding items and jobs are completed.
 */
class CommunicationGroupSendCumulativeMonitorThread extends Thread {

    private boolean keepRunning = true;
    private CommunicationGroupSendMonitor monitor
    private static final log = Logger.getLogger(CommunicationGroupSendCumulativeMonitorThread.class)


    CommunicationGroupSendCumulativeMonitorThread(CommunicationGroupSendMonitor monitor ) {
        this.monitor = monitor
    }

    @Override
    public void run() {
        while (keepRunning) {
            monitorGroupSends();
            long nextMonitorTime = System.currentTimeMillis() + monitor.monitorIntervalInSeconds * 1000
            synchronized (this) {
                try {
                    while (System.currentTimeMillis() < nextMonitorTime) wait( monitor.monitorIntervalInSeconds * 1000);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    void stopRunning() {
        keepRunning = false;
        synchronized (this) {
            notify();
        }
    }

    private void monitorGroupSends() {
        log.trace( "monitorGroupSends() begin" )
        try {
            if (keepRunning) {
                try {
                    monitor.monitorGroupSendsCumulativeStatus()
                } catch (Throwable t) {
                    log.error( "Exception monitoring group sends cumulative status", t );
                }
            }
        } finally {
            log.trace( "monitorGroupSends() end")
        }
    }


}
