
/** *****************************************************************************
 © 2011 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.general.overall

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.Version
import javax.persistence.JoinColumn
import javax.persistence.JoinColumns
import javax.persistence.ManyToOne
import javax.persistence.NamedQueries
import javax.persistence.NamedQuery
import com.sungardhe.banner.general.system.Term
import javax.persistence.GenerationType
import org.hibernate.annotations.Type
import javax.persistence.SequenceGenerator
import javax.persistence.Temporal
import javax.persistence.TemporalType

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
@Table(name="SSRXLST")
@NamedQueries(value = [
@NamedQuery(name = "SectionCrossListSection.fetchByTermAndXlstGroup",
query = """FROM  SectionCrossListSection a
		   WHERE a.term.code = :term
		   AND   a.xlstGroup = :xlstGroup
	       ORDER BY a.courseReferenceNumber """)
])
class SectionCrossListSection implements Serializable {
	
	/**
	 * Surrogate ID for SSRXLST
	 */
	@Id
	@Column(name="SSRXLST_SURROGATE_ID")
    @SequenceGenerator(name = "SSRXLST_SEQ_GEN", allocationSize = 1, sequenceName = "SSRXLST_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SSRXLST_SEQ_GEN")
	Long id

	/**
	 * Cross List Group Identifier Number.
	 */
	@Column(name="SSRXLST_XLST_GROUP", nullable = false, length=2)
	String xlstGroup

	/**
	 * Corss List Section CRN.
	 */
	@Column(name="SSRXLST_CRN", nullable = false, length=5)
	String courseReferenceNumber

	/**
	 * This field identifies the most recent date a record was created or updated.
	 */
	@Column(name="SSRXLST_ACTIVITY_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	Date lastModified

	/**
	 * Version column which is used as a optimistic lock token for SSRXLST
	 */
	@Version
	@Column(name="SSRXLST_VERSION", length=19)
	Long version

	/**
	 * Last Modified By column for SSRXLST
	 */
	@Column(name="SSRXLST_USER_ID", length=30)
	String lastModifiedBy

	/**
	 * Data Origin column for SSRXLST
	 */
	@Column(name="SSRXLST_DATA_ORIGIN", length=30)
	String dataOrigin

	
	/**
	 * Foreign Key : FK1_SSRXLST_INV_STVTERM_CODE
	 */
	@ManyToOne
	@JoinColumns([
		@JoinColumn(name="SSRXLST_TERM_CODE", referencedColumnName="STVTERM_CODE")
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

	
	boolean equals(o) {
	    if (this.is(o)) return true
	    if (!(o instanceof SectionCrossListSection)) return false
	    SectionCrossListSection that = (SectionCrossListSection) o
        if(id != that.id) return false
        if(xlstGroup != that.xlstGroup) return false
        if(courseReferenceNumber != that.courseReferenceNumber) return false
        if(lastModified != that.lastModified) return false
        if(version != that.version) return false
        if(lastModifiedBy != that.lastModifiedBy) return false
        if(dataOrigin != that.dataOrigin) return false
        if(term != that.term) return false      
        return true
    }

	
	int hashCode() {
		int result
	    result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (id != null ? id.hashCode() : 0)
        result = 31 * result + (xlstGroup != null ? xlstGroup.hashCode() : 0)
        result = 31 * result + (courseReferenceNumber != null ? courseReferenceNumber.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        result = 31 * result + (term != null ? term.hashCode() : 0)
        return result
	}

	static constraints = {
		xlstGroup(nullable:false, maxSize:2)
		courseReferenceNumber(nullable:false, maxSize:5)
		lastModified(nullable:true)
		lastModifiedBy(nullable:true, maxSize:30)
		dataOrigin(nullable:true, maxSize:30)
		term(nullable:false)

		/**
	     * Please put all the custom tests in this protected section to protect the code
	     * from being overwritten on re-generation
	     */
	    /*PROTECTED REGION ID(sectioncrosslistsection_custom_constraints) ENABLED START*/
	    
	    /*PROTECTED REGION END*/
    }
    
    /**
     * Please put all the custom methods/code in this protected section to protect the code
     * from being overwritten on re-generation
     */
    /*PROTECTED REGION ID(sectioncrosslistsection_custom_methods) ENABLED START*/
    
    /*PROTECTED REGION END*/
}
