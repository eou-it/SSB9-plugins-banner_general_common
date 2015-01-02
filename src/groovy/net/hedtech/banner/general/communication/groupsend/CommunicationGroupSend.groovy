/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.groupsend

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import net.hedtech.banner.general.communication.population.CommunicationPopulationSelectionList
import net.hedtech.banner.general.communication.template.CommunicationTemplate
import org.hibernate.annotations.Type

import javax.persistence.*

/**
 * CommunicationJob
 *
 */
@Entity
@Table(name = "GCBGSND")
@EqualsAndHashCode
@ToString
@NamedQueries(value = [
    @NamedQuery( name = "CommunicationGroupSend.findRunning",
        query = """ FROM CommunicationGroupSend gs
                    WHERE gs.currentExecutionState = :new_ or
                          gs.currentExecutionState = :processing_ """
    )
])
class CommunicationGroupSend implements Serializable {

    /**
     * KEY: Generated unique key.
     */
    @Id
    @Column(name = "gcbgsnd_SURROGATE_ID")
    @SequenceGenerator(name = "gcbgsnd_SEQ_GEN", allocationSize = 1, sequenceName = "gcbgsnd_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gcbgsnd_SEQ_GEN")
    Long id

    /**
     *  Optimistic lock token.
     */
    @Version
    @Column(name = "gcbgsnd_VERSION")
    Long version

    /** The oracle user name of the person that submitted the group send. **/
    @Column(name = "gcbgsnd_CREATOR_ID")
    String createdBy

    /**
     *  The user ID of the person who inserted or last updated this record.
     */
    @Column(name = "gcbgsnd_USER_ID")
    String lastModifiedBy

    /**
     *  Date that record was created or last updated.
     */
    @Column(name = "gcbgsnd_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     *  Source system that created or updated the data.
     */
    @Column(name = "gcbgsnd_DATA_ORIGIN")
    String dataOrigin

    @Column(name="gcbgsnd_VPDI_CODE" )
    String mepCode

    @JoinColumn(name="gcbgsnd_organization_id" )
    @ManyToOne( fetch = FetchType.LAZY )
    CommunicationOrganization organization;

    @JoinColumn(name="gcbgsnd_POPLIST_ID" )
    @ManyToOne( fetch = FetchType.LAZY )
    CommunicationPopulationSelectionList population;

    @JoinColumn(name="gcbgsnd_TEMPLATE_ID" )
    @ManyToOne( fetch = FetchType.LAZY )
    CommunicationTemplate template;

    @Column(name="gcbgsnd_STARTED_DATE", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    Date startedDate;

    @Column(name="gcbgsnd_CREATIONDATETIME", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    Date creationDateTime;

    @Column(name="gcbgsnd_CURRENT_STATE", nullable = false)
    @Enumerated(EnumType.STRING)
    CommunicationGroupSendExecutionState currentExecutionState = CommunicationGroupSendExecutionState.New;

    @Column(name="gcbgsnd_STOP_DATE", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    Date stopDate;

    @Column(name="gcbgsnd_DELETED", nullable = false)
    @Type(type="yes_no")
    boolean deleted = false;

    static constraints = {
        mepCode(nullable: true)
        population(nullable: false)
        organization(nullable: false)
        createdBy(nullable:false, maxSize: 30)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        startedDate(nullable: true)
        stopDate(nullable: true)
        creationDateTime(nullable: false)
        currentExecutionState(nullable: false)
    }

    public static List findRunning() {
        def query
        CommunicationGroupSend.withSession { session ->
            query = session.getNamedQuery( 'CommunicationGroupSend.findRunning' )
                .setParameter( 'new_', CommunicationGroupSendExecutionState.New )
                .setParameter( 'processing_', CommunicationGroupSendExecutionState.Processing )
                .list()
        }
        return query
    }

}

