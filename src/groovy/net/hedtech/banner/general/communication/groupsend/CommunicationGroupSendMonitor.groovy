/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.groupsend

import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.security.TrustedBannerAuthenticationProvider
import net.hedtech.banner.general.security.TrustedBannerToken
import net.hedtech.banner.security.FormContext
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Required
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder


/**
 * Created by mbrzycki on 12/5/14.
 */
class CommunicationGroupSendMonitor {
    private Log log = LogFactory.getLog( this.getClass() )
    private CommunicationGroupSendMonitorThread monitorThread
    private CommunicationGroupSendService communicationGroupSendService
    private CommunicationGroupSendItemService communicationGroupSendItemService
    private TrustedBannerAuthenticationProvider trustedBannerAuthenticationProvider
    public int monitorIntervalInSeconds = 10

    @Required
    void setTrustedBannerAuthenticationProvider(TrustedBannerAuthenticationProvider trustedBannerAuthenticationProvider) {
        this.trustedBannerAuthenticationProvider = trustedBannerAuthenticationProvider
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


    public void startMonitoring() {
        log.info( "Monitor thread started.")
        this.monitorThread.start();
    }


    public void monitorGroupSends() {
        // begin setup
        if (!SecurityContextHolder.getContext().getAuthentication()) {
            FormContext.set( ['SELFSERVICE'] )

            Authentication auth = trustedBannerAuthenticationProvider.authenticate( new TrustedBannerToken( 'BCMADMIN' ) )
            SecurityContextHolder.getContext().setAuthentication( auth )
        }

        try {
            System.out.println( "after openSession" )
            List<CommunicationGroupSend> groupSendList = communicationGroupSendService.findRunning()
            System.out.println( "after findRunning" )
            if (log.isDebugEnabled()) log.debug( "Group Send Monitor found " + records.size() + " records" );

             if (communicationFolderService.findAll().size() < 10) {
                 CommunicationFolder folder = new CommunicationFolder()
                 folder.setName( "Folder " + new Date() )
                 communicationFolderService.create( folder )
             }

        } catch( Throwable t) {
            t.printStackTrace()
            log.error( t )
        }

//        System.out.println( "before call" )
//        Session session = sessionFactory.openSession()
//        try {
//            System.out.println( "after openSession" )
//            List<CommunicationGroupSend> groupSendList = communicationGroupSendService.findRunning()
//            System.out.println( "after findRunning" )
//            if (log.isDebugEnabled()) log.debug( "Group Send Monitor found " + records.size() + " records" );
//
//             if (communicationFolderService.findAll().size() < 10) {
//                 CommunicationFolder folder = new CommunicationFolder()
//                 folder.setName( "Folder " + groupSendList.size() )
//                 communicationFolderService.create( folder )
//             }
//
//        } catch( Throwable t) {
//            t.printStackTrace()
//            log.error( t )
//        } finally {
//            session.close()
//        }


//        executorService.execute( {
//            try {
//             System.out.println( "before call" )
//             List groupSendList = CommunicationGroupSend.findAll()
//             System.out.println( "Displaying group sends from monitor" )
//             groupSendList.each { gs ->
//                 System.out.print( "group send id = " + gs )
//             }
//             if (groupSendList.size() < 10) {
//                 CommunicationFolder folder = new CommunicationFolder()
//                 folder.setName( "Folder " + groupSendList.size() )
//                 CommunicationFolder.withTransaction { status ->
//                    folder.save()
//                 }
//             }

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
