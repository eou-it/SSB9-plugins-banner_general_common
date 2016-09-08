/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population.selectionlist

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQueryExecutionStatus
import net.hedtech.banner.service.DatabaseModifiesState

import javax.persistence.*

/**
 * BannerPopulation Selection List definitions entity.
 */
@Entity
@EqualsAndHashCode
@ToString
@Table(name = "GCRSLIS")
@DatabaseModifiesState
@NamedQueries(value = [
        @NamedQuery(name = "CommunicationPopulationSelectionList.fetchById",
                query = """ FROM CommunicationPopulationSelectionList a
                    WHERE a.id = :id""")
])
class CommunicationPopulationSelectionList implements Serializable {

    /**
     * Generated unique numeric identifier for this entity.
     */
    @Id
    @Column(name = "GCRSLIS_SURROGATE_ID")
    @SequenceGenerator(name = "GCRSLIS_SEQ_GEN", allocationSize = 1, sequenceName = "GCRSLIS_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GCRSLIS_SEQ_GEN")
    Long id

    /**
     * VERSION: Optimistic lock token.
     */
    @Version
    @Column(name = "GCRSLIS_VERSION")
    Long version

    /**
     * Record creation date
     */
    @Column(name = "GCRSLIS_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * ID of user who created the Selection List
     */
    @Column(name = "GCRSLIS_USER_ID")
    String lastModifiedBy

    /**
     * DATA ORIGIN: Source system that created or updated the data.
     */
    @Column(name = "GCRSLIS_DATA_ORIGIN")
    String dataOrigin

    static constraints = {
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
    }

    // Read Only fields that should be protected against update
    public static readonlyProperties = ['id']

    public static CommunicationPopulationSelectionList fetchById(Long id) {
        def populationSelectionList
        CommunicationPopulationSelectionList.withSession { session ->
            populationSelectionList = session.getNamedQuery('CommunicationPopulationSelectionList.fetchById')
                    .setLong('id', id).list()[0]

        }
        return populationSelectionList
    }
}
