package net.hedtech.banner.general.communication.groupsend

import org.apache.log4j.Logger

/**
 * Created by mbrzycki on 12/5/14.
 */
class CommunicationGroupSendMonitorThread extends Thread {

    private boolean keepRunning = true;
    private CommunicationGroupSendMonitor monitor
    private Logger log = Logger.getLogger( this.getClass() )


    CommunicationGroupSendMonitorThread( CommunicationGroupSendMonitor monitor ) {
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
        println "monitorGroupSends() begin at " + new Date()
        try {
            if (keepRunning) {
                try {
                    monitor.monitorGroupSends()
                } catch (Throwable t) {
                    log.error( "Exception monitoring group sends", t );
                }
            }
        } finally {
            log.trace( "monitorGroupSends() end")
        }
    }


}
