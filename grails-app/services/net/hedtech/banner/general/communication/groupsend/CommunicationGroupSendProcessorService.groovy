/*******************************************************************************
 Copyright 2015-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.groupsend

import groovy.sql.Sql
import groovy.util.logging.Slf4j
import grails.gorm.transactions.Transactional
import net.hedtech.banner.general.communication.job.CommunicationJob
import net.hedtech.banner.general.communication.merge.CommunicationRecipientData
import net.hedtech.banner.general.communication.merge.CommunicationRecipientDataFactory

import java.sql.SQLException

/**
 * Process a group send item to the point of creating recipient merge data values and submitting an individual communication job
 * for the recipient.
 */
@Slf4j
@Transactional
class CommunicationGroupSendProcessorService {
   // private static final log = Logger.getLogger(CommunicationGroupSendProcessorService.class)
    def communicationGroupSendItemService
    def communicationTemplateMergeService
    def communicationFieldCalculationService
    def communicationJobService
    def communicationRecipientDataService
    def sessionFactory
    def asynchronousBannerAuthenticationSpoofer
    private static final int noWaitErrorCode = 54;

    public void performGroupSendItem(Long groupSendItemId) {
        log.debug( "Performing group send item id = " + groupSendItemId )
        boolean locked = lockGroupSendItem( groupSendItemId, CommunicationGroupSendItemExecutionState.Ready );
        if (!locked) {
            // Do nothing
            return;
        }

        CommunicationGroupSendItem groupSendItem = (CommunicationGroupSendItem) communicationGroupSendItemService.get( groupSendItemId )
        CommunicationGroupSend groupSend = groupSendItem.communicationGroupSend

        asynchronousBannerAuthenticationSpoofer.setMepProcessContext(sessionFactory.currentSession.connection(), groupSendItem.mepCode)
        if (log.isDebugEnabled()) log.debug("Spoofed as ${groupSend.createdBy} for creating recipient data.")

        if (!groupSend.getCurrentExecutionState().isTerminal()) {
            CommunicationRecipientData recipientData = getRecipientDataFactory().create( groupSendItem )
            log.debug("Created recipient data with referenceId = " + groupSendItem.referenceId + ".")

            log.debug("Creating communication job with reference id = " + recipientData.referenceId)
            CommunicationJob communicationJob = new CommunicationJob( referenceId: recipientData.referenceId )
            communicationJobService.create( communicationJob )

            log.debug("Updating group send item to mark it complete with reference id = " + recipientData.referenceId)
            def groupSendItemParamMap = [
                id                   : groupSendItem.id,
                version              : groupSendItem.version,
                currentExecutionState: CommunicationGroupSendItemExecutionState.Complete,
                stopDate             : new Date()
            ]
            communicationGroupSendItemService.update( groupSendItemParamMap )
        } else {
            def groupSendItemParamMap = [
                id                   : groupSendItem.id,
                version              : groupSendItem.version,
                currentExecutionState: CommunicationGroupSendItemExecutionState.Stopped,
                stopDate             : new Date()
            ]
            communicationGroupSendItemService.update( groupSendItemParamMap )
        }
    }

    public void failGroupSendItem(Long groupSendItemId, String errorCode, String errorText ) {
        CommunicationGroupSendItem groupSendItem = (CommunicationGroupSendItem) communicationGroupSendItemService.get( groupSendItemId )
        def groupSendItemParamMap = [
                id                   : groupSendItem.id,
                version              : groupSendItem.version,
                currentExecutionState: CommunicationGroupSendItemExecutionState.Failed,
                stopDate             : new Date(),
                errorText            : errorText,
                errorCode            : errorCode
        ]

        log.warn("Group send item failed id = ${groupSendItemId}, errorText = ${errorText}.")

        communicationGroupSendItemService.update(groupSendItemParamMap)
    }


    /**
     * Attempts to create a pessimistic lock on the group send item record.
     * @param groupSendItemId the primary key of the group send item.
     * @param state the group send item execution state
     * @return true if the record was successfully locked and false otherwise
     */
    public boolean lockGroupSendItem(final Long groupSendItemId, final CommunicationGroupSendItemExecutionState state) {
        Sql sql = null
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            def rows = sql.rows("select GCRGSIM_SURROGATE_ID from GCRGSIM where GCRGSIM_SURROGATE_ID = ? and GCRGSIM_CURRENT_STATE = ? for update nowait",
                    [groupSendItemId, state.name()],
                    0, 2
            )

            if (rows.size() > 1) {
                throw new RuntimeException("Found more than one GCRGSIM row for a single group send item id")
            } else {
                return rows.size() == 1
            }

        } catch (SQLException e) {
            if (e.getErrorCode() == noWaitErrorCode) {
                return false
            } else {
                throw e
            }
        } finally {
//            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
    }

    private CommunicationRecipientDataFactory getRecipientDataFactory() {
        return new CommunicationRecipientDataFactory (
            communicationTemplateMergeService: communicationTemplateMergeService,
            communicationFieldCalculationService: communicationFieldCalculationService,
            communicationRecipientDataService: communicationRecipientDataService,
            asynchronousBannerAuthenticationSpoofer: asynchronousBannerAuthenticationSpoofer
        )
    }
}
