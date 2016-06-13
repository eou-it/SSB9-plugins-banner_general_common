/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.interaction

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import org.hibernate.FlushMode
import org.hibernate.annotations.Type
import org.hibernate.criterion.Order

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
@Table(name = "GCRITPE")
@NamedQueries(value = [
        @NamedQuery(name = "CommunicationInteractionType.fetchById",
                query = """ FROM CommunicationInteractionType a
                    WHERE a.id = :id"""),
        @NamedQuery(name = "CommunicationInteractionType.findAll",
                query = """ FROM CommunicationInteractionType a
                        order by a.name"""),
        @NamedQuery(name = "CommunicationInteractionType.fetchByInteractionTypeNameAndFolderName",
                query = """ FROM CommunicationInteractionType a
                    WHERE a.folder.name = :folderName
                      AND upper(a.name) = upper(:interactionTypeName)"""),
        @NamedQuery(name = "CommunicationInteractionType.existsAnotherNameFolder",
                query = """ FROM CommunicationInteractionType a
                    WHERE a.folder.name = :folderName
                    AND   upper(a.name) = upper(:interactionTypeName)
                    AND   a.id <> :id"""),
        @NamedQuery(name = "CommunicationInteractionType.fetchAvailable",
                query = """ FROM CommunicationInteractionType a
                    WHERE a.isAvailable = true""")
])
class CommunicationInteractionType implements Serializable {

    /**
     * KEY: Generated unique key.
     */
    @Id
    @Column(name = "GCRITPE_SURROGATE_ID")
    @SequenceGenerator(name = "GCRITPE_SEQ_GEN", allocationSize = 1, sequenceName = "GCRITPE_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GCRITPE_SEQ_GEN")
    Long id

    /**
     * NAME: Descriptive name of the interaction type.
     */
    @Column(name = "GCRITPE_NAME")
    String name

    /**
     * DESCRIPTION: Long description.
     */
    @Column(name = "GCRITPE_DESCRIPTION")
    String description

    /**
     * Foreign key reference to the Folder under which this interaction type is organized.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "GCRITPE_FOLDER_ID", referencedColumnName = "GCRFLDR_SURROGATE_ID")
    @org.hibernate.annotations.ForeignKey(name = "FK1_GCRITPE_INV_GCRFLDR_KEY")
    CommunicationFolder folder



    @Type(type = "yes_no")
    @Column(name = "GCRITPE_AVAILABLE_IND")
    Boolean isAvailable = false

    /**
     * ACTIVITY_DATE: Most current date record was created or changed.
     */
    @Column(name = "GCRITPE_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * USER_ID: Oracle User ID of the person who last inserted or last updated the data.
     */
    @Column(name = "GCRITPE_USER_ID")
    String lastModifiedBy

    /**
     * VERSION: Optimistic lock token
     */
    @Version
    @Column(name = "GCRITPE_VERSION")
    Long version

    /**
     * DATA ORIGIN: Source system that created or updated the data.
     */
    @Column(name = "GCRITPE_DATA_ORIGIN")
    String dataOrigin

    static constraints = {
        name(nullable: false, maxSize: 255)
        description(nullable:true, maxSize: 2000)
        folder(nullable:false)
        isAvailable(nullable: true)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
    }

    public static CommunicationInteractionType fetchById(Long id) {
        def interactionType
        CommunicationInteractionType.withSession { session ->
            interactionType = session.getNamedQuery('CommunicationInteractionType.fetchById')
                    .setLong('id', id).list()[0]

        }
        return interactionType
    }

    public static List<CommunicationInteractionType> findAll() {

        def interactionTypes = []
        CommunicationInteractionType.withSession { session ->
            interactionTypes = session.getNamedQuery('CommunicationInteractionType.findAll').list()
        }
        return interactionTypes
    }

    public static CommunicationInteractionType fetchByInteractionTypeNameAndFolderName(String interactionTypeName, String folderName) {

        def interactionType
        CommunicationInteractionType.withSession { session ->
            interactionType = session.getNamedQuery('CommunicationInteractionType.fetchByInteractionTypeNameAndFolderName').setString('folderName', folderName).setString('interactionTypeName', interactionTypeName).list()[0]
        }
        return interactionType
    }

    public static Boolean existsAnotherNameFolder(Long interactionTypeId, String interactionTypeName, String folderName) {

        def interactionType
        CommunicationInteractionType.withSession { session ->
            session.setFlushMode(FlushMode.MANUAL);
            try {
                interactionType = session.getNamedQuery('CommunicationInteractionType.existsAnotherNameFolder')
                        .setString('folderName', folderName).setString('interactionTypeName', interactionTypeName).setLong('id', interactionTypeId).list()[0]
            } finally {
                session.setFlushMode(FlushMode.AUTO)
            }
        }
        return (interactionType != null)
    }

    public static findByNameWithPagingAndSortParams(filterData, pagingAndSortParams) {

        def descdir = pagingAndSortParams?.sortDirection?.toLowerCase() == 'desc'

        def queryCriteria = CommunicationInteractionType.createCriteria()
        def results = queryCriteria.list(max: pagingAndSortParams.max, offset: pagingAndSortParams.offset) {
            ilike("name", CommunicationCommonUtility.getScrubbedInput(filterData?.params?.name))
            order((descdir ? Order.desc(pagingAndSortParams?.sortColumn) : Order.asc(pagingAndSortParams?.sortColumn)).ignoreCase())
        }
        return results
    }

    public static findAvailableByNameWithPagingAndSortParams(filterData, pagingAndSortParams) {

        def descdir = pagingAndSortParams?.sortDirection?.toLowerCase() == 'desc'

        def queryCriteria = CommunicationInteractionType.createCriteria()
        def results = queryCriteria.list(max: pagingAndSortParams.max, offset: pagingAndSortParams.offset) {
            ilike("name", CommunicationCommonUtility.getScrubbedInput(filterData?.params?.name))
            eq("isAvailable", true)
            order((descdir ? Order.desc(pagingAndSortParams?.sortColumn) : Order.asc(pagingAndSortParams?.sortColumn)).ignoreCase())
        }
        return results
    }

    public static List<CommunicationInteractionType> fetchAvailable() {
        def query
        CommunicationInteractionType.withSession { session ->
            query = session.getNamedQuery('CommunicationInteractionType.fetchAvailable').list()
        }
        return query
    }
}