/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import org.hibernate.annotations.Type

import javax.persistence.*

@Entity
@EqualsAndHashCode
@ToString
@Table(name = "GCRPOPV")
@NamedQueries(value = [
        @NamedQuery(name = "CommunicationPopulationVersion.findByPopulationId",
                query = """ FROM CommunicationPopulationVersion populationVersion
                WHERE populationVersion.population.id = :populationId order by populationVersion.createDate DESC"""),
        @NamedQuery(name = "CommunicationPopulationVersion.fetchById",
                query = """ FROM CommunicationPopulationVersion populationVersion
                WHERE populationVersion.id = :id""")
])
class CommunicationPopulationVersion implements Serializable {

    /**
     * KEY: Generated unique key.
     */
    @Id
    @Column(name = "GCRPOPV_SURROGATE_ID")
    @SequenceGenerator(name = "GCRPOPV_SEQ_GEN", allocationSize = 1, sequenceName = "GCRPOPV_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GCRPOPV_SEQ_GEN")
    Long id

    /**
     * Foreign key reference to the Folder under which this template is organized.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "GCRPOPV_POPL_ID", referencedColumnName = "GCBPOPL_SURROGATE_ID")
    @org.hibernate.annotations.ForeignKey(name = "FK1_GCRPOPV_INV_GCBPOPL")
    CommunicationPopulation population

    /**
     * CREATE_DATE: The date the record was created.
     */
    @Column(name = "GCRPOPV_CREATE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date createDate

    /**
     * CREATOR_ID: The Oracle username of the user who created the record.
     */
    @Column(name = "GCRPOPV_CREATOR_ID")
    String createdBy

    /**
     * CALC_COUUNT: The count of persons calculated by the populationQuery.
     */
    @Column(name = "GCRPOPV_CALC_COUNT")
    Long calculatedCount

    /**
     * CALC_BY: ID of the user who last calculated the populationQuery.
     */
    @Column(name = "GCRPOPV_CALC_BY")
    String calculatedBy

    /**
     * Selection List status: SCHEDULED, PENDING_EXECUTION, ERROR, AVAILABLE
     */
    @Column(name = "GCRPOPV_STATUS")
    @Enumerated(EnumType.STRING)
    CommunicationPopulationQueryExecutionStatus status

    /**
     * VERSION: Optimistic lock token
     */
    @Version
    @Column(name = "GCRPOPV_VERSION")
    Long version

    /**
     * ACTIVITY_DATE: Most current date record was created or changed.
     */
    @Column(name = "GCRPOPV_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified


    /**
     * USER_ID: Oracle User ID of the person who last inserted or last updated the data.
     */
    @Column(name = "GCRPOPV_USER_ID")
    String lastModifiedBy

    /**
     * DATA ORIGIN: Source system that created or updated the data.
     */
    @Column(name = "GCRPOPV_DATA_ORIGIN")
    String dataOrigin

    static constraints = {
        population(nullable: false)
        createDate(nullable: false)
        createdBy(nullable: false, maxSize: 30)
        calculatedBy(nullable: false)
        status(nullable: false)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
    }


    /**
     * Returns a list of population versions by the parent population id.
     * @param populationId the id of the owning population
     * @return a list of population versions
     */
    public static List findByPopulationId( Long populationId ) {
        def population
        CommunicationPopulationVersion.withSession { session ->
            population = session.getNamedQuery('CommunicationPopulationVersion.findByPopulationId').setLong( 'populationId', populationId ).list()
        }
        return population
    }

    public static CommunicationPopulationVersion fetchById(Long id) {

        def population
        CommunicationPopulationVersion.withSession { session ->
            population = session.getNamedQuery('CommunicationPopulationVersion.fetchById')
                    .setLong('id', id).list()[0]

        }
        return population
    }
}
