/*********************************************************************************
  Copyright 2010-2018 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall

import groovy.transform.EqualsAndHashCode
import net.hedtech.banner.general.system.Term
import javax.persistence.*

/**
 * Cross List Section Repeating Table
 */

/**
 * Where clause on this entity present in forms:
 * Order by clause on this entity present in forms:
 * Form Name: SSAXLST
 *  ssrxlst_crn

 */
@Entity
@Table(name = "SSRXLST")
@EqualsAndHashCode(includeFields = true)
@NamedQueries(value = [
@NamedQuery(name = "SectionCrossListSection.fetchByTermAndXlstGroup",
query = """FROM  SectionCrossListSection a
		   WHERE a.term.code = :term
		   AND   a.xlstGroup = :xlstGroup
	       ORDER BY a.courseReferenceNumber """),
@NamedQuery(name = "SectionCrossListSection.fetchByTermAndCourseReferenceNumber",
query = """FROM  SectionCrossListSection a
		   WHERE a.term.code = :term
		   AND   a.courseReferenceNumber = :courseReferenceNumber
	       ORDER BY a.xlstGroup""")
])
class SectionCrossListSection implements Serializable {

    /**
     * Surrogate ID for SSRXLST
     */
    @Id
    @Column(name = "SSRXLST_SURROGATE_ID")
    @SequenceGenerator(name = "SSRXLST_SEQ_GEN", allocationSize = 1, sequenceName = "SSRXLST_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SSRXLST_SEQ_GEN")
    Long id

    /**
     * Cross List Group Identifier Number.
     */
    @Column(name = "SSRXLST_XLST_GROUP", nullable = false, length = 2)
    String xlstGroup

    /**
     * Corss List Section CRN.
     */
    @Column(name = "SSRXLST_CRN", nullable = false, length = 5)
    String courseReferenceNumber

    /**
     * This field identifies the most recent date a record was created or updated.
     */
    @Column(name = "SSRXLST_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * Version column which is used as a optimistic lock token for SSRXLST
     */
    @Version
    @Column(name = "SSRXLST_VERSION", length = 19)
    Long version

    /**
     * Last Modified By column for SSRXLST
     */
    @Column(name = "SSRXLST_USER_ID", length = 30)
    String lastModifiedBy

    /**
     * Data Origin column for SSRXLST
     */
    @Column(name = "SSRXLST_DATA_ORIGIN", length = 30)
    String dataOrigin

    /**
     * Foreign Key : FK1_SSRXLST_INV_STVTERM_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SSRXLST_TERM_CODE", referencedColumnName = "STVTERM_CODE")
    ])
    Term term


    public static readonlyProperties = ['term', 'xlstGroup', 'courseReferenceNumber']


    public static List fetchByTermAndXlstGroup(String termCode, String xlstGroup) {
        def sectionCrossListSections = []
        SectionCrossListSection.withSession {session ->
            sectionCrossListSections = session.getNamedQuery('SectionCrossListSection.fetchByTermAndXlstGroup').setString('term', termCode).setString('xlstGroup', xlstGroup).list()
        }
        return sectionCrossListSections
    }


    public static List fetchByTermAndCourseReferenceNumber(String termCode, String courseReferenceNumber) {
        def sectionCrossListSections = []
        SectionCrossListSection.withSession {session ->
            sectionCrossListSections = session.getNamedQuery('SectionCrossListSection.fetchByTermAndCourseReferenceNumber').setString('term', termCode).setString('courseReferenceNumber', courseReferenceNumber).list()
        }
        return sectionCrossListSections
    }


    public String toString() {
        """SectionCrossListSection[
					id=$id, 
					xlstGroup=$xlstGroup, 
					courseReferenceNumber=$courseReferenceNumber, 
					lastModified=$lastModified, 
					version=$version, 
					lastModifiedBy=$lastModifiedBy, 
					dataOrigin=$dataOrigin, 
					term=$term]"""
    }


    static constraints = {
        xlstGroup(nullable: false, maxSize: 2)
        courseReferenceNumber(nullable: false, maxSize: 5)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        term(nullable: false)


    }


}
