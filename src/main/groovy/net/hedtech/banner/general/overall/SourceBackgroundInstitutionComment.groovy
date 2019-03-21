/*********************************************************************************
  Copyright 2010-2018 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall

import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.query.DynamicFinder

import javax.persistence.*

/**
 * Source/Background Institution Comments Repeating Table
 */
@NamedQueries(value = [
@NamedQuery(
        name = "SourceBackgroundInstitutionComment.fetchNextSequenceNumber",
        query = """SELECT NVL(MAX(a.sequenceNumber),0) + 1
                     FROM SourceBackgroundInstitutionComment a
                    WHERE a.sourceAndBackgroundInstitution.code = :sourceAndBackgroundInstitutionCode""")
,
@NamedQuery(
        name = "SourceBackgroundInstitutionComment.fetchBySourceAndBackgroundInstitution",
        query = """  FROM SourceBackgroundInstitutionComment a
                    WHERE a.sourceAndBackgroundInstitution.code = :sourceAndBackgroundInstitutionCode
                    ORDER BY a.sequenceNumber asc""")
])

@Entity
@Table(name = "SORBCMT")
class SourceBackgroundInstitutionComment implements Serializable {

    /**
     * Surrogate ID for SORBCMT
     */
    @Id
    @Column(name = "SORBCMT_SURROGATE_ID")
    @SequenceGenerator(name = "SORBCMT_SEQ_GEN", allocationSize = 1, sequenceName = "SORBCMT_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SORBCMT_SEQ_GEN")
    Long id

    /**
     * Optimistic lock token for SORBCMT
     */
    @Version
    @Column(name = "SORBCMT_VERSION")
    Long version

    /**
     * This field does not display on the form.  It assigns an internal sequence       number to each comment created.
     */
    @Column(name = "SORBCMT_SEQNO")
    Integer sequenceNumber

    /**
     * This field identifies each comment created.
     */
    @Column(name = "SORBCMT_COMMENT")
    String commentData

    /**
     * This field identifies the most current date a record was created or changed.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "SORBCMT_ACTIVITY_DATE")
    Date lastModified

    /**
     * Last modified by column for SORBCMT
     */
    @Column(name = "SORBCMT_USER_ID")
    String lastModifiedBy

    /**
     * Data origin column for SORBCMT
     */
    @Column(name = "SORBCMT_DATA_ORIGIN")
    String dataOrigin

    /**
     * Foreign Key : FK1_SORBCMT_INV_STVSBGI_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SORBCMT_SBGI_CODE", referencedColumnName = "STVSBGI_CODE")
    ])
    SourceAndBackgroundInstitution sourceAndBackgroundInstitution


    public String toString() {
        """SourceBackgroundInstitutionComment[
					id=$id, 
					version=$version, 
					sequenceNumber=$sequenceNumber, 
					commentData=$commentData, 
					lastModified=$lastModified, 
					lastModifiedBy=$lastModifiedBy, 
					dataOrigin=$dataOrigin, 
					sourceAndBackgroundInstitution=$sourceAndBackgroundInstitution]"""
    }


    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof SourceBackgroundInstitutionComment)) return false
        SourceBackgroundInstitutionComment that = (SourceBackgroundInstitutionComment) o
        if (id != that.id) return false
        if (version != that.version) return false
        if (sequenceNumber != that.sequenceNumber) return false
        if (commentData != that.commentData) return false
        if (lastModified != that.lastModified) return false
        if (lastModifiedBy != that.lastModifiedBy) return false
        if (dataOrigin != that.dataOrigin) return false
        if (sourceAndBackgroundInstitution != that.sourceAndBackgroundInstitution) return false
        return true
    }


    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (sequenceNumber != null ? sequenceNumber.hashCode() : 0)
        result = 31 * result + (commentData != null ? commentData.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        result = 31 * result + (sourceAndBackgroundInstitution != null ? sourceAndBackgroundInstitution.hashCode() : 0)
        return result
    }


    static constraints = {
        sequenceNumber(nullable: false, min: -999, max: 999)
        commentData(nullable: true, maxSize: 4000)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        sourceAndBackgroundInstitution(nullable: false)
    }

    //Read Only fields that should be protected against update
    public static readonlyProperties = ['sequenceNumber', 'sourceAndBackgroundInstitution']


    static int fetchNextSequenceNumber(String sourceAndBackgroundInstitutionCode) {
        def nextSequenceNumber = SourceBackgroundInstitutionComment.withSession { session ->
            session.getNamedQuery('SourceBackgroundInstitutionComment.fetchNextSequenceNumber')
                    .setString('sourceAndBackgroundInstitutionCode', sourceAndBackgroundInstitutionCode)
                    .list()
        }
        return nextSequenceNumber[0]
    }


    static int fetchNextSequenceNumber(SourceAndBackgroundInstitution sourceAndBackgroundInstitution) {
        fetchNextSequenceNumber(sourceAndBackgroundInstitution.code)
    }


    static def fetchBySourceAndBackgroundInstitution(String sourceAndBackgroundInstitutionCode) {
        def list = SourceBackgroundInstitutionComment.withSession { session ->
            session.getNamedQuery('SourceBackgroundInstitutionComment.fetchBySourceAndBackgroundInstitution')
                    .setString('sourceAndBackgroundInstitutionCode', sourceAndBackgroundInstitutionCode)
                    .list()
        }
        return list
    }


    static def fetchBySourceAndBackgroundInstitution(SourceAndBackgroundInstitution sourceAndBackgroundInstitution) {
        fetchBySourceAndBackgroundInstitution(sourceAndBackgroundInstitution.code)
    }


    def static countAll(filterData) {
        finderByAll().count(filterData)
    }


    def static fetchSearch(filterData, pagingAndSortParams) {
        finderByAll().find(filterData, pagingAndSortParams)
    }


    def private static finderByAll = {
        def query = """ FROM SourceBackgroundInstitutionComment a
	                   WHERE a.sourceAndBackgroundInstitution.code = :sourceAndBackgroundInstitutionCode
	            	"""
        return new DynamicFinder(SourceBackgroundInstitutionComment.class, query, "a")
    }
}
