/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.groupsend

import net.hedtech.banner.general.asynchronous.AsynchronousBannerAuthenticationSpoofer
import net.hedtech.banner.security.FormContext
import net.hedtech.banner.security.MepContextHolder
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.annotation.Required
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder


/**
 * Created by mbrzycki on 12/5/14.
 */
class CommunicationGroupSendMonitor implements DisposableBean {
    private Log log = LogFactory.getLog( this.getClass() )
    private CommunicationGroupSendMonitorThread monitorThread
    private CommunicationGroupSendService communicationGroupSendService
    private CommunicationGroupSendItemService communicationGroupSendItemService
    private AsynchronousBannerAuthenticationSpoofer asynchronousBannerAuthenticationSpoofer
    public int monitorIntervalInSeconds = 10

    @Required
    void setAsynchronousBannerAuthenticationSpoofer(AsynchronousBannerAuthenticationSpoofer asynchronousBannerAuthenticationSpoofer) {
        this.asynchronousBannerAuthenticationSpoofer = asynchronousBannerAuthenticationSpoofer
    }

    @Required
    public void setCommunicationGroupSendService( CommunicationGroupSendService communicationGroupSendService ) {
        this.communicationGroupSendService = communicationGroupSendService
    }

    @Required
    public void setCommunicationGroupSendItemService( CommunicationGroupSendItemService communicationGroupSendItemService ) {
        this.communicationGroupSendItemService = communicationGroupSendItemService
    }

    public void init() {
        log.info( "Initialized." );
        this.monitorThread = new CommunicationGroupSendMonitorThread( this );
    }

    @Override
    void destroy() throws Exception {
        log.info( "Calling disposable bean method." );
        this.monitorThread.stopRunning()
    }

    public void startMonitoring() {
        log.info( "Monitor thread started.")
        this.monitorThread.start();
    }


    public void monitorGroupSends() {
        if (log.isDebugEnabled()) log.debug( "Checking group sends for status updates." )
        // begin setup
        if (!SecurityContextHolder.getContext().getAuthentication()) {
            FormContext.set( ['CMQUERYEXECUTE'] )
            String monitorOracleUserName = 'COMMMGR' //'BCMADMIN'
            Authentication auth = asynchronousBannerAuthenticationSpoofer.authenticate( monitorOracleUserName )
            SecurityContextHolder.getContext().setAuthentication( auth )
            if (log.isDebugEnabled()) log.debug( "Authenticated as ${monitorOracleUserName} for monitoring." )
        }

        try {
            List<CommunicationGroupSend> groupSendList = communicationGroupSendService.findRunning()
            if (log.isDebugEnabled()) log.debug( "Running group send count = " + groupSendList.size() + "." );

            for(CommunicationGroupSend groupSend:groupSendList) {
                if (groupSend.currentExecutionState.equals( CommunicationGroupSendExecutionState.Processing)) {
                    int runningCount = communicationGroupSendItemService.fetchRunningGroupSendItemCount( groupSend.id )
                    if (runningCount == 0) {
                        communicationGroupSendService.completeGroupSend( groupSend.id )
                    }
                }
            }
        } catch( Throwable t) {
            t.printStackTrace()
            log.error( t )
        }

//            List<CommunicationGroupSend> groupSendList = communicationGroupSendService.findRunning()
//            if (log.isDebugEnabled()) log.debug( "Group Send Monitor found " + records.size() + " records" );

//                for(CommunicationGroupSendMonitorRecord record:records) {
//                    if (record.getCurrentState().equals( GroupSendExecutionState.CalculatingSnapshot)) {
//                        if (log.isDebugEnabled()) log.debug( "Monitor picked up 'calculating snapshot' group send with key = " + record.getGroupSendKey().getKeyValue() + "." );
//                        checkGroupSendPopulationSnapshotCompleted( record.getGroupSendKey() );
//                    } else if (record.getCurrentState().equals( GroupSendExecutionState.Processing ) ) {
//                        if (log.isDebugEnabled()) log.debug( "Monitor picked up 'processing' group send with key = " + record.getGroupSendKey().getKeyValue() + "." );
//                        checkGroupSendCompleted( record.getGroupSendKey() );
//                    } else if (record.getCurrentState().isTerminal() && record.getPopulationSnapshotKey() != null) {
//                        if (log.isDebugEnabled()) {
//                            log.debug( "Monitor picked up 'terminal' group send with key = " + record.getGroupSendKey().getKeyValue() +
//                                " and population snapshot key = " + record.getPopulationSnapshotKey().getKeyValue() + "." );
//                        }
//                        checkTerminalGroupSendForPruning( record.getGroupSendKey() );
//                    } else {
//                        if (log.isDebugEnabled()) log.debug( "Monitor ignored group send with key = " + record.getGroupSendKey().getKeyValue() + "." );
//                    }
//                }
//            } catch (GroupSendLockNoWaitException e) {
//                if (log.isDebugEnabled()) log.debug( "Captured pessimistic lock exception (GroupSendLockNoWaitException), will try again on next poll cycle.");
//            } catch (OptimisticLockException e) {
//                //
//            } catch (Throwable t) {
//                t.printStackTrace()
//                log.error( t )
//            }
//        } as Callable )
    }


    public void shutdown() {
        log.debug( "Shutting down." );
        this.monitorThread.stopRunning();
        try {
            this.monitorThread.join();
        } catch (InterruptedException e) {
        }
    }



}
