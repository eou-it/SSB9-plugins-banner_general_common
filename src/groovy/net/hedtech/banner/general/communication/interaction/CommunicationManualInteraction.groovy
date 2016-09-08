/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.interaction

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.communication.organization.CommunicationOrganization

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.Lob
import javax.persistence.ManyToOne
import javax.persistence.NamedQueries
import javax.persistence.NamedQuery
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType
import javax.persistence.Version

@Entity
@EqualsAndHashCode
@ToString
@Table(name = "GCRMINT")
@NamedQueries(value = [
        @NamedQuery(name = "CommunicationManualInteraction.fetchById",
                query = """ FROM CommunicationManualInteraction a
                    WHERE a.id = :id"""),
        @NamedQuery(name = "CommunicationManualInteraction.findAll",
                query = """ FROM CommunicationManualInteraction a
                        order by a.interactionDate"""),
        @NamedQuery(name = "CommunicationManualInteraction.fetchByConstituentPidm",
                query = """ FROM CommunicationManualInteraction a
                    WHERE a.constituentPidm = :constituentPidm
                    order by a.interactionDate""")
])
class CommunicationManualInteraction implements Serializable {

    /**
     * KEY: Generated unique key.
     */
    @Id
    @Column(name = "GCRMINT_SURROGATE_ID")
    @SequenceGenerator(name = "GCRMINT_SEQ_GEN", allocationSize = 1, sequenceName = "GCRMINT_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GCRMINT_SEQ_GEN")
    Long id

    /**
     * CONSTITUENT PIDM: The PIDM of the Interactee.
     */
    @Column(name = "GCRMINT_CONSTITUENT_PIDM")
    Long constituentPidm

    /**
     * Foreign key reference to the Organization which the manual interaction belongs to
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "GCRMINT_ORGANIZATION_ID", referencedColumnName = "GCRORAN_SURROGATE_ID")
    @org.hibernate.annotations.ForeignKey(name = "FK1_GCRMINT_INV_GCRORAN_KEY")
    CommunicationOrganization organization

    /**
     * Foreign key reference to the type of the manual interaction
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "GCRMINT_INTERACTION_TYPE_ID", referencedColumnName = "GCRITPE_SURROGATE_ID")
    @org.hibernate.annotations.ForeignKey(name = "FK1_GCRMINT_INV_GCRITPE_KEY")
    CommunicationInteractionType interactionType

    /**
     * NAME: Descriptive name of the interaction type.
     */
    @Column(name = "GCRMINT_SUBJECT")
    String aSubject

    /**
     * DESCRIPTION: Long description.
     */
    @Lob
    @Column(name = "GCRMINT_DESCRIPTION")
    String description

    /**
     * INTERACTION_DATE: The date the manual interaction took place.
     */
    @Column(name = "GCRMINT_INTERACTION_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date interactionDate

    /**
     * INTERACTOR_PIDM: The PIDM of the Interactor.
     */
    @Column(name = "GCRMINT_INTERACTOR_PIDM")
    Long interactorPidm

    /**
     * CREATE_DATE: The date the record was created.
     */
    @Column(name = "GCRMINT_CREATED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date createDate

    /**
     * CREATOR_ID: The Oracle username of the user who created the record.
     */
    @Column(name = "GCRMINT_CREATOR_ID")
    String createdBy

    /**
     * ACTIVITY_DATE: Most current date record was created or changed.
     */
    @Column(name = "GCRMINT_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * USER_ID: Oracle User ID of the person who last inserted or last updated the data.
     */
    @Column(name = "GCRMINT_USER_ID")
    String lastModifiedBy

    /**
     * VERSION: Optimistic lock token
     */
    @Version
    @Column(name = "GCRMINT_VERSION")
    Long version

    /**
     * DATA ORIGIN: Source system that created or updated the data.
     */
    @Column(name = "GCRMINT_DATA_ORIGIN")
    String dataOrigin

    static constraints = {
        constituentPidm(nullable:false)
        aSubject(nullable: false, maxSize: 2000)
        description(nullable:true)
        organization(nullable:false)
        interactionType(nullable:false)
        interactionDate(nullable:false)
        interactorPidm(nullable:false)
        createDate(nullable:false)
        createdBy(nullable:false, maxSize: 30)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
    }

    public static CommunicationManualInteraction fetchById(Long id) {
        def manualInteraction
        CommunicationManualInteraction.withSession { session ->
            manualInteraction = session.getNamedQuery('CommunicationManualInteraction.fetchById')
                    .setLong('id', id).list()[0]

        }
        return manualInteraction
    }

    public static List findAll() {

        def manualInteractions = []
        CommunicationManualInteraction.withSession { session ->
            manualInteractions = session.getNamedQuery('CommunicationManualInteraction.findAll').list()
        }
        return manualInteractions
    }

    public static List fetchByConstituentPidm(Long constituentPidm) {

        def manualInteractions
        CommunicationManualInteraction.withSession { session ->
            manualInteractions = session.getNamedQuery('CommunicationManualInteraction.fetchByConstituentPidm')
                    .setLong('constituentPidm', constituentPidm).list()
        }
        return manualInteractions
    }
}
