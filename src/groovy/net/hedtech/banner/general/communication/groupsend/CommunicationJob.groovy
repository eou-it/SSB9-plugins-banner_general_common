/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.groupsend

import groovy.transform.EqualsAndHashCode
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import net.hedtech.banner.general.communication.population.CommunicationPopulationSelectionList

import javax.persistence.*

/**
 * CommunicationJob
 *
 */
@Entity
@Table(name = "GCBCJOB")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    name = "GCBCJOB_TYPE",
    discriminatorType = DiscriminatorType.STRING
)
@EqualsAndHashCode
abstract class CommunicationJob implements Serializable {

    /**
     * KEY: Generated unique key.
     */
    @Id
    @Column(name = "GCBCJOB_SURROGATE_ID")
    @SequenceGenerator(name = "GCBCJOB_SEQ_GEN", allocationSize = 1, sequenceName = "GCBCJOB_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GCBCJOB_SEQ_GEN")
    Long id

    /**
     *  Optimistic lock token.
     */
    @Version
    @Column(name = "GCBCJOB_VERSION")
    Long version

    /**
     *  The user ID of the person who inserted or last updated this record.
     */
    @Column(name = "GCBCJOB_USER_ID")
    String lastModifiedBy

    /**
     *  Date that record was created or last updated.
     */
    @Column(name = "GCBCJOB_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     *  Source system that created or updated the data.
     */
    @Column(name = "GCBCJOB_DATA_ORIGIN")
    String dataOrigin

    @Column(name="GCBCJOB_VPDI_CODE" )
    String mepCode

    @Column(name="GCBCJOB_OWNER_PIDM" )
    Long ownerPidm;

    @JoinColumn(name="GCBCJOB_ORG_KEY" )
    @ManyToOne( fetch = FetchType.LAZY )
    CommunicationOrganization organization;

    @JoinColumn(name="GCBCJOB_POPLIST_KEY" )
    @ManyToOne( fetch = FetchType.LAZY )
    CommunicationPopulationSelectionList population;

    static constraints = {
        mepCode(nullable: true)
        population(nullable: false)
        organization(nullable: false)
        ownerPidm(nullable:false)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
    }

}

