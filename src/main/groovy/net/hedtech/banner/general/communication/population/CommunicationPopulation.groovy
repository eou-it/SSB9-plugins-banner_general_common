/*******************************************************************************
 Copyright 2016-2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.population.selectionlist.CommunicationPopulationSelectionList
import org.hibernate.FlushMode
import org.hibernate.annotations.Type

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.NamedQueries
import javax.persistence.NamedQuery
import javax.persistence.OneToOne
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType
import javax.persistence.Version

@Entity
@EqualsAndHashCode
@ToString
@Table(name = "GCBPOPL")
@NamedQueries(value = [
        @NamedQuery(name = "CommunicationPopulation.fetchById",
                query = """ FROM CommunicationPopulation a
                    WHERE a.id = :id"""),
        @NamedQuery(name = "CommunicationPopulation.findAll",
                query = """ FROM CommunicationPopulation a
                        order by a.name"""),
        @NamedQuery(name = "CommunicationPopulation.fetchByPopulationNameAndFolderName",
                query = """ FROM CommunicationPopulation a
                    WHERE a.folder.name = :folderName
                      AND upper(a.name) = upper(:populationName)"""),
        @NamedQuery(name = "CommunicationPopulation.existsAnotherNameFolder",
                query = """ FROM CommunicationPopulation a
                    WHERE a.folder.name = :folderName
                    AND   upper(a.name) = upper(:populationName)
                    AND   a.id <> :id"""),
        @NamedQuery(name = "CommunicationPopulation.findAllByFolderName",
                query = """ FROM CommunicationPopulation a
                    WHERE a.folder.name = :folderName"""),
        @NamedQuery(name = "CommunicationPopulation.findAllByPopulationName",
                query = """ FROM CommunicationPopulation a
                    WHERE a.name like :populationName""")
])
class CommunicationPopulation implements Serializable {

    /**
     * KEY: Generated unique key.
     */
    @Id
    @Column(name = "GCBPOPL_SURROGATE_ID")
    @SequenceGenerator(name = "GCBPOPL_SEQ_GEN", allocationSize = 1, sequenceName = "GCBPOPL_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GCBPOPL_SEQ_GEN")
    Long id

    /**
     * NAME: Descriptive name of the population.
     */
    @Column(name = "GCBPOPL_NAME")
    String name

    /**
     * DESCRIPTION: Long description.
     */
    @Column(name = "GCBPOPL_DESCRIPTION")
    String description

    /**
     * Indicates if the population is a personal population. Personal populations are available
     * only for use by the owner and are not available to other users.
     */
    @Type(type = "yes_no")
    @Column(name = "GCBPOPL_PERSONAL")
    Boolean personal = false

    /**
     * Foreign key reference to the Folder under which this population is organized.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "GCBPOPL_FOLDER_ID", referencedColumnName = "GCRFLDR_SURROGATE_ID")
    @org.hibernate.annotations.ForeignKey(name = "FK1_GCBPOPL_INV_GCRFLDR_KEY")
    CommunicationFolder folder

    /**
     * CREATE_DATE: The date the record was created.
     */
    @Column(name = "GCBPOPL_CREATE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date createDate

    /**
     * CREATOR_ID: The Oracle username of the user who created the record.
     */
    @Column(name = "GCBPOPL_CREATOR_ID")
    String createdBy

    /**
     * SCHEDULED_DATE: The date when the population is scheduled to be calculated for the first time.
     */
    @Column(name = "GCBPOPL_SCHEDULED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date scheduledDate

    /**
     * Population status: SCHEDULED, PENDING_EXECUTION, ERROR, AVAILABLE.
     * Mostly used here only for SCHEDULED population, rest of the statuses are available via the Population Calculation
     */
    @Column(name = "GCBPOPL_STATUS")
    @Enumerated(EnumType.STRING)
    CommunicationPopulationCalculationStatus status

    /**
     * VERSION: Optimistic lock token
     */
    @Version
    @Column(name = "GCBPOPL_VERSION")
    Long version

    /**
     * ACTIVITY_DATE: Most current date record was created or changed.
     */
    @Column(name = "GCBPOPL_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * USER_ID: Oracle User ID of the person who last inserted or last updated the data.
     */
    @Column(name = "GCBPOPL_USER_ID")
    String lastModifiedBy

    @Column(name = "GCBPOPL_VPDI_CODE")
    String mepCode

    /**
     * DATA ORIGIN: Source system that created or updated the data.
     */
    @Column(name = "GCBPOPL_DATA_ORIGIN")
    String dataOrigin

    @OneToOne
    @JoinColumn(name = "GCBPOPL_INCLUDE_LIST_ID", referencedColumnName = "GCRSLIS_SURROGATE_ID")
    CommunicationPopulationSelectionList includeList

    @Type(type = "yes_no")
    @Column(name = "GCBPOPL_CHANGED_IND")
    Boolean changesPending = false

    /**
     * Indicates if the population was generated as part of a backend API call and should not be deleted or modified in any way.
     */
    @Type(type = "yes_no")
    @Column(name = "GCBPOPL_SYSTEM_REQ_IND")
    Boolean systemIndicator = false

    static constraints = {
        name(nullable: false)
        description(nullable:true)
        createDate(nullable: false)
        createdBy(nullable: false, maxSize: 30)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        mepCode(nullable:true)
        dataOrigin(nullable: true, maxSize: 30)
        includeList(nullable: true)
        changesPending(nullable: false)
        systemIndicator(nullable:false)
        scheduledDate(nullable:true)
        status(nullable:true)
    }

    public static CommunicationPopulation fetchById(Long id) {
        def population
        CommunicationPopulation.withSession { session ->
            population = session.getNamedQuery('CommunicationPopulation.fetchById')
                    .setLong('id', id).list()[0]

        }
        return population
    }

    public static List findAll() {

        def queries = []
        CommunicationPopulation.withSession { session ->
            queries = session.getNamedQuery('CommunicationPopulation.findAll').list()
        }
        return queries
    }


    public static CommunicationPopulation fetchByPopulationNameAndFolderName(String populationName, String folderName) {

        def population
        CommunicationPopulation.withSession { session ->
            population = session.getNamedQuery('CommunicationPopulation.fetchByPopulationNameAndFolderName').setString('folderName', folderName).setString('populationName', populationName).list()[0]
        }
        return population
    }

    public static Boolean existsAnotherNameFolder(Long populationId, String populationName, String folderName) {

        def population
        CommunicationPopulation.withSession { session ->
            session.setFlushMode(FlushMode.MANUAL);
            try {
                population = session.getNamedQuery('CommunicationPopulation.existsAnotherNameFolder')
                        .setString('folderName', folderName).setString('populationName', populationName).setLong('id', populationId).list()[0]
            } finally {
                session.setFlushMode(FlushMode.AUTO)
            }
        }
        return (population != null)
    }

    public static List findAllByFolderName(String folderName) {

        def populations = []
        CommunicationPopulation.withSession { session ->
            populations = session.getNamedQuery('CommunicationPopulation.findAllByFolderName').setString('folderName', folderName).list()
        }
        return populations
    }

    public static List findAllByPopulationName(String populationName) {

        def population
        CommunicationPopulation.withSession { session ->
            population = session.getNamedQuery('CommunicationPopulation.findAllByPopulationName').setString('populationName', populationName + "%").list()
        }
        return population
    }
}
