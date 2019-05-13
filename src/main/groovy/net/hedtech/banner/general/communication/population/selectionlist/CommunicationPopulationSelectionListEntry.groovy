/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population.selectionlist

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.service.DatabaseModifiesState

import javax.persistence.*

/**
 * BannerPopulation list selection lists and corresponding pidms entity.
 */
@Entity
@EqualsAndHashCode
@ToString
@Table(name = "GCRLENT")
@DatabaseModifiesState
@NamedQueries(value = [
        @NamedQuery(name = "CommunicationPopulationSelectionListEntry.fetchBySelectionListId",
                query = """ FROM CommunicationPopulationSelectionListEntry a where a.populationSelectionList.id = :selectionListId """)
])
class CommunicationPopulationSelectionListEntry implements Serializable {

    /**
     *
     */
    @Id
    @Column(name = "GCRLENT_SURROGATE_ID")
    @SequenceGenerator(name = "GCRLENT_SEQ_GEN", allocationSize = 1, sequenceName = "GCRLENT_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GCRLENT_SEQ_GEN")
    Long id

    /**
     *
     */
    @Version
    @Column(name = "GCRLENT_VERSION")
    Long version

    /**
     *
     */
    @Column(name = "GCRLENT_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     *
     */
    @Column(name = "GCRLENT_USER_ID")
    String lastModifiedBy

    /**
     *
     */
    @Column(name = "GCRLENT_DATA_ORIGIN")
    String dataOrigin

    /**
     * Pidm that belong to a selection list.
     */
    @Column(name = "GCRLENT_PIDM")
    Long pidm

    /**
     *
     */
    @ManyToOne
    @JoinColumns([
            @JoinColumn(name = "GCRLENT_SLIS_ID", referencedColumnName = "GCRSLIS_SURROGATE_ID")
    ])
    CommunicationPopulationSelectionList populationSelectionList

    static constraints = {
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
    }

    // Read Only fields that should be protected against update
    public static readonlyProperties = ['id']


    public static List fetchBySelectionListId(Long selectionListId) {

        def populationSelectionListEntries
        CommunicationPopulationSelectionListEntry.withSession { session ->
            populationSelectionListEntries = session.getNamedQuery('CommunicationPopulationSelectionListEntry.fetchBySelectionListId')
                    .setLong('selectionListId', selectionListId).list()
        }
        return populationSelectionListEntries
    }

}
