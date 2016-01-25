/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import org.hibernate.annotations.Type

import javax.persistence.*

/**
 * Communication Query Version captures the static portions of a query at the
 * point of publishing on a specific date and time.
 */
@Entity
@EqualsAndHashCode
@ToString
@Table(name = "GCRQRYV")
//@NamedQueries(value = [
//        @NamedQuery(name = "CommunicationPopulationQuery.findAll",
//                query = """ FROM CommunicationPopulationQuery a
//                        order by a.name"""),
//        @NamedQuery(name = "CommunicationPopulationQuery.findAllByFolderName",
//                query = """ FROM CommunicationPopulationQuery a
//                    WHERE a.folder.name = :folderName"""),
//        @NamedQuery(name = "CommunicationPopulationQuery.fetchByQueryNameAndFolderName",
//                query = """ FROM CommunicationPopulationQuery a
//                    WHERE a.folder.name = :folderName
//                      AND upper(a.name) = upper(:queryName)"""),
//        @NamedQuery(name = "CommunicationPopulationQuery.existsAnotherNameFolder",
//                query = """ FROM CommunicationPopulationQuery a
//                    WHERE a.folder.name = :folderName
//                    AND   upper(a.name) = upper(:queryName)
//                    AND   a.id <> :id"""),
//        @NamedQuery(name = "CommunicationPopulationQuery.fetchById",
//                query = """ FROM CommunicationPopulationQuery a
//                    WHERE a.id = :id"""),
//        @NamedQuery(name = "CommunicationPopulationQuery.findAllByQueryName",
//                query = """ FROM CommunicationPopulationQuery a
//                    WHERE a.name like :queryName""")
//])
class CommunicationPopulationQueryVersion implements Serializable {

    @Id
    @Column(name = "GCRQRYV_SURROGATE_ID")
    @SequenceGenerator(name = "GCBQURY_SEQ_GEN", allocationSize = 1, sequenceName = "GCBQURY_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GCBQURY_SEQ_GEN")
    Long id

    /** Parent population query **/
    @ManyToOne
    @JoinColumn(name="GCRQRYV_QUERY_ID", referencedColumnName="GCBQURY_SURROGATE_ID")
    CommunicationPopulationQuery query

    /** The date the population query was published. **/
    @Column(name = "GCRQRYV_PUBLISHED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date publishedDate

    /** The date the record was created. **/
    @Column(name = "GCRQRYV_CREATE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date createDate

    /** The Oracle username of the user who created the record. **/
    @Column(name = "GCRQRYV_CREATOR_ID")
    String createdBy

    /** Optimistic lock token **/
    @Version
    @Column(name = "GCRQRYV_VERSION")
    Long version

    /** Most current date record was created or changed. **/
    @Column(name = "GCRQRYV_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /** Oracle User ID of the person who last inserted or last updated the data. **/
    @Column(name = "GCRQRYV_USER_ID")
    String lastModifiedBy

    /** Source system that created or updated the data. **/
    @Column(name = "GCRQRYV_DATA_ORIGIN")
    String dataOrigin

    /** The text of the statement that will be executed. **/
    @Lob
    @Column(name = "GCRQRYV_QUERY_STRING")
    String sqlString

    static constraints = {
        query(nullable: false)
        publishedDate(nullable: false)
        createDate(nullable: false)
        createdBy(nullable: false, maxSize: 30)
        sqlString(nullable: false)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
    }
}
