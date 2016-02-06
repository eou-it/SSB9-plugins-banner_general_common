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
@NamedQueries(value = [
    @NamedQuery(name = "CommunicationPopulationQueryVersion.findByQueryId",
            query = """ FROM CommunicationPopulationQueryVersion queryVersion
                WHERE queryVersion.query.id = :queryId order by queryVersion.createDate DESC"""),
    @NamedQuery(name = "CommunicationPopulationQueryVersion.fetchById",
            query = """ FROM CommunicationPopulationQueryVersion queryVersion
            WHERE queryVersion.id = :id""")
])
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
        createDate(nullable: false)
        createdBy(nullable: false, maxSize: 30)
        sqlString(nullable: false)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
    }


    /**
     * Returns a list of query versions by the parent query id.
     * @param queryId the id of the owning query
     * @return a list of query versions
     */
    public static List findByQueryId( Long queryId ) {
        def query
        CommunicationPopulationQueryVersion.withSession { session ->
            query = session.getNamedQuery('CommunicationPopulationQueryVersion.findByQueryId').setLong( 'queryId', queryId ).list()
        }
        return query
    }

    public static CommunicationPopulationQueryVersion fetchById(Long id) {

        def query
        CommunicationPopulationQueryVersion.withSession { session ->
            query = session.getNamedQuery('CommunicationPopulationQueryVersion.fetchById')
                    .setLong('id', id).list()[0]

        }
        return query
    }
}
