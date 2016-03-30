/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.communication.folder.CommunicationFolder

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
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
                    WHERE a.name like :populationName"""),
        @NamedQuery(name = "CommunicationPopulation.fetchByNameAndId",
                query = """ FROM CommunicationPopulation a
                      where a.id = :id
                      and a.lastModifiedBy = upper( :userId ) """)
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

    /**
     * DATA ORIGIN: Source system that created or updated the data.
     */
    @Column(name = "GCBPOPL_DATA_ORIGIN")
    String dataOrigin

    static constraints = {
        name(nullable: false)
        description(nullable:true)
        createDate(nullable: false)
        createdBy(nullable: false, maxSize: 30)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
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
            population = session.getNamedQuery('CommunicationPopulation.existsAnotherNameFolder')
                    .setString('folderName', folderName).setString('populationName', populationName).setLong('id', populationId).list()[0]

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

    public static CommunicationPopulation fetchByNameAndId(Long populationId, String userId) {

        def populations = []
        CommunicationPopulation.withSession { session ->
            populations = session.getNamedQuery('CommunicationPopulationSelectionList.fetchByNameAndId').setLong('id', populationId).setString('userId', userId).list()
        }
        return populations.getAt(0)
    }
}
