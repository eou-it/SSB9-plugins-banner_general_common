/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import groovy.transform.EqualsAndHashCode
import net.hedtech.banner.service.DatabaseModifiesState

import javax.persistence.*

/**
 * BannerPopulation Selection List definitions entity.
 */
@Entity
@EqualsAndHashCode
@Table(name = "GCRSLIS")
@DatabaseModifiesState
@NamedQueries(value = [
        @NamedQuery(name = "CommunicationPopulationSelectionList.fetch",
                query = """ FROM CommunicationPopulationSelectionList a"""),
        @NamedQuery(name = "CommunicationPopulationSelectionList.fetchByNameAndId",
                query = """ FROM CommunicationPopulationSelectionList a
                      where a.populationQueryId = :populationQueryId
                      and a.lastModifiedBy = upper( :userId ) """)
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
     *
     */
    @Column(name = "GCRSLIS_QUERY_ID")
    Long populationQueryId

    /**
     * Selection List status: SCHEDULED, PENDING_EXECUTION, ERROR, AVAILABLE
     */
    @Column(name = "GCRSLIS_STATUS")
    @Enumerated(EnumType.STRING)
    CommunicationPopulationQueryExecutionStatus status

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

    /**
     * LAST_CALC_COUUNT: The count of persons calculated by the populationQuery
     */
    @Column(name = "GCRSLIS_LAST_CALC_COUNT")
    Long lastCalculatedCount

    /**
     * LAST_CALC_BY: ID of the user who last calculated the populationQuery.
     */
    @Column(name = "GCRSLIS_LAST_CALC_BY")
    String lastCalculatedBy

    /**
     * LAST_CALC_TIME: Timestamp of when the populationQuery was last calculated.
     */
    @Column(name = "GCRSLIS_LAST_CALC_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastCalculatedTime


    static constraints = {
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        populationQueryId(nullable: false)
        status(nullable: false)
        lastCalculatedCount(nullable: true)
        lastCalculatedBy(nullable: true, maxSize: 30)
        lastCalculatedTime(nullable: true)
    }

    // Read Only fields that should be protected against update
    public static readonlyProperties = ['id']


    public static CommunicationPopulationSelectionList fetchByNameAndId(Long populationQueryId, String userId) {

        def populationSelectionLists = CommunicationPopulationSelectionList.withSession { session ->
            org.hibernate.Query query = session.getNamedQuery('CommunicationPopulationSelectionList.fetchByNameAndId')
                    .setLong('populationQueryId', populationQueryId).setString('userId', userId); query.list()
        }
        return populationSelectionLists.getAt(0)
    }


    @Override
    public String toString() {
        return "PopulationSelectionList{" +
                "id=" + id +
                ", populationQueryId=" + populationQueryId +
                ", status=" + status +
                ", version=" + version +
                ", lastModified=" + lastModified +
                ", lastModifiedBy='" + lastModifiedBy + '\'' +
                ", dataOrigin='" + dataOrigin + '\'' +
                ", lastCalculatedCount=" + lastCalculatedCount +
                ", lastCalculatedBy='" + lastCalculatedBy + '\'' +
                ", lastCalculatedTime=" + lastCalculatedTime +
                '}';
    }
}
