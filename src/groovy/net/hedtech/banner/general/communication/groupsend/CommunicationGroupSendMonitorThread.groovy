package net.hedtech.banner.general.communication.groupsend

/**
 * Created by mbrzycki on 12/5/14.
 */
class CommunicationGroupSendMonitorThread extends Thread {

    private boolean keepRunning = true;
    private CommunicationGroupSendMonitor monitor


    CommunicationGroupSendMonitorThread( CommunicationGroupSendMonitor monitor ) {
        this.monitor = monitor
    }

    @Override
    public void run() {
        while (keepRunning) {
            synchronized (this) {
                try {
                    if (keepRunning) wait( monitor.monitorIntervalInSeconds * 1000);
                } catch (InterruptedException e) {
                }
            }
            monitorGroupSends();
        }
    }

    void stopRunning() {
        keepRunning = false;
        synchronized (this) {
            notify();
        }
    }

    private void monitorGroupSends() {
        if (keepRunning) {
            try {
                monitor.monitorGroupSends()
            } catch (Throwable t) {
                log.error( "Exception monitoring group sends", t );
            }
        }
    }


}
