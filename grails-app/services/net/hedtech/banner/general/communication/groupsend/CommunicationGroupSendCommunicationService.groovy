/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.groupsend

import groovy.sql.Sql
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSend
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSendExecutionState
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSendItemExecutionState
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import net.hedtech.banner.general.communication.population.CommunicationPopulationSelectionList
import net.hedtech.banner.general.communication.template.CommunicationTemplate
import net.hedtech.banner.security.BannerUser
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.transaction.annotation.Transactional

import java.sql.SQLException


/**
 * GroupSendCommunicationService is responsible for initiating and processing group send communications.
 *
 * @author Michael Brzycki
 */
@Transactional
class CommunicationGroupSendCommunicationService {

    def log = Logger.getLogger( this.getClass() )
    def communicationGroupSendService
    def asynchronousActionSchedulingService
    def communicationTemplateService
    def communicationPopulationSelectionListService
    def communicationOrganizationService


    /**
     * Initiate the sending of a communication to a set of prospect recipients
     * @param request the communication to initiate
     */
    public CommunicationGroupSend sendAsynchronousGroupCommunication( CommunicationGroupSendRequest request ) {
        if (log.isDebugEnabled()) log.debug( "Method sendAsynchronousGroupCommunication reached." );
        if (!request) throw new IllegalArgumentException( "request may not be null!" )

        CommunicationTemplate template = communicationTemplateService.get( request.getTemplateId() )
        if (!template) {
            throw new NotFoundException( id: request.getTemplateId(), entityClassName: CommunicationTemplate.class.simpleName )
        }

        CommunicationPopulationSelectionList population = communicationPopulationSelectionListService.get( request.getPopulationId() )
        if (!population) {
            throw new NotFoundException( id: request.getPopulationId(), entityClassName: CommunicationPopulationSelectionList.class.simpleName )
        }

        CommunicationOrganization organization = communicationOrganizationService.get( request.getOrganizationId() )
        if (!organization) {
            throw new NotFoundException( id: request.getOrganizationId(), entityClassName: CommunicationOrganization.class.simpleName )
        }

        BannerUser owner = SecurityContextHolder?.context?.authentication?.principal as BannerUser;

        CommunicationGroupSend groupSend = new CommunicationGroupSend();
        groupSend.template = template
        groupSend.population = population
        groupSend.organization = organization
        groupSend.ownerPidm = owner.pidm
        groupSend.currentExecutionState = CommunicationGroupSendExecutionState.New
        // TODO: We'll grab the mep code from the logged in user's home context for now. Look into if we should
        // be grabbing from RequestAttributes currentRequestAttributes() in the controller and passing it in as a
        // parameter on the request
        groupSend.mepCode = owner.mepHomeContext

        groupSend = communicationGroupSendService.create( groupSend )

        // We'll created the group send items synchronously for now until we have support for scheduling.
        // The individual group send items will still be processed asynchronously via the framework.
        createGroupSendItems( groupSend )
        groupSend = communicationGroupSendService.update( groupSend )
        return groupSend
    }


    private void createGroupSendItems( CommunicationGroupSend groupSend ) {
        def sql
        try {
            def ctx = ServletContextHolder.servletContext.getAttribute(GrailsApplicationAttributes.APPLICATION_CONTEXT)
            def sessionFactory = ctx.sessionFactory
            def session = sessionFactory.currentSession
            sql = new Sql(session.connection())

            sql.execute(
            [
                state:CommunicationGroupSendItemExecutionState.Ready.toString(),
                group_send_key:groupSend.id
            ],
            """ INSERT INTO gcrgsim (gcrgsim_gcbgsnd_id, gcrgsim_pidm, gcrgsim_creationdatetime, gcrgsim_current_state, gcrgsim_user_id, gcrgsim_activity_date)
                                 SELECT gcbgsnd_surrogate_id, gcrlent_pidm, SYSDATE, :state, gcbgsnd_user_id, SYSDATE
                                   FROM gcrslis, gcrlent, gcbgsnd
                                  WHERE     gcbgsnd_poplist_id = gcrslis_surrogate_id
                                        AND gcrlent_slis_id = gcrslis_surrogate_id
                                        and gcbgsnd_surrogate_id = :group_send_key
            """ )

            if (log.isDebugEnabled()) log.debug( "Created " + sql.updateCount + " group send item records for group send with id = " + groupSend.id )
        } catch (SQLException ae) {
            log.debug "SqlException in INSERT INTO gcrgsim ${ae}"
            log.debug ae.stackTrace
            throw ae
        } catch (Exception ae) {
            log.debug "Exception in INSERT INTO gcrgsim ${ae}"
            log.debug ae.stackTrace
            throw ae
        } finally {
            sql?.close()
        }

    }

}
