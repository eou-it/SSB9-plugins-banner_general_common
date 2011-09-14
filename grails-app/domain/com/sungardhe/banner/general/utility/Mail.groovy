
/*********************************************************************************
 Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of 
 SunGard Higher Education and its subsidiaries. Any use of this software is limited 
 solely to SunGard Higher Education licensees, and is further subject to the terms 
 and conditions of one or more written license agreements between SunGard Higher 
 Education and the licensee in question. SunGard is either a registered trademark or
 trademark of SunGard Data Systems in the U.S.A. and/or other regions and/or countries.
 Banner and Luminis are either registered trademarks or trademarks of SunGard Higher 
 Education in the U.S.A. and/or other regions and/or countries.
 **********************************************************************************/
/**
 Banner Automator Version: 1.26
 Generated: Thu Aug 11 12:05:30 EDT 2011
 */
package com.sungardhe.banner.general.utility

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.Version
import javax.persistence.NamedQueries
import javax.persistence.NamedQuery
import javax.persistence.Transient

import javax.persistence.GenerationType
import javax.persistence.SequenceGenerator

import javax.persistence.JoinColumn
import javax.persistence.JoinColumns
import javax.persistence.ManyToOne
import org.hibernate.annotations.GenericGenerator

import org.hibernate.annotations.Type

import com.sungardhe.banner.general.system.Term
import com.sungardhe.banner.general.system.LetterProcessLetter
import com.sungardhe.banner.general.system.Initials
import javax.persistence.Temporal
import javax.persistence.TemporalType
//TODO: The general common plugin cannot have a dependency on a student package. This needs to be resolved after discussions
//import com.sungardhe.banner.student.system.CommunicationPlan


/**
 * Mail Table
 */
/*PROTECTED REGION ID(mail_namedqueries) ENABLED START*/
//TODO: NamedQueries that needs to be ported:
 /**
    * Where clause on this entity present in forms:
  * Form Name: AUAMAIL
  *  where gurmail_pidm = :pidm and gurmail_system_ind = 'A'

  * Form Name: GUIMAIL
  *  where gurmail_pidm = :pidm

  * Form Name: FUAMAIL
  *  where gurmail_pidm = :pidm and gurmail_system_ind = 'F'

  * Form Name: SUAMAIL
  *  where gurmail_pidm = :pidm and gurmail_system_ind = 'S'

  * Form Name: RUAMAIL
  *  where gurmail_pidm = :pidm and gurmail_system_ind = 'R' and ((gurmail_aidy_code = :keyblck_aidy_code and :keyblck_aidy_code is not null) or :keyblck_aidy_code is null)
and ROKFGAC.F_FIND_IF_FINAID(:pidm, gurmail_aidy_code) = 'Y'

  * Order by clause on this entity present in forms:
  * Form Name: AUAMAIL
  *  order by gurmail_system_ind, gurmail_term_code desc, gurmail_module_code, gurmail_letr_code

  * Form Name: GUIMAIL
  *  order by gurmail_system_ind, gurmail_term_code desc, gurmail_module_code, gurmail_letr_code

  * Form Name: FUAMAIL
  *  order by gurmail_system_ind, gurmail_letr_code

  * Form Name: SUAMAIL
  *  order by gurmail_system_ind, gurmail_term_code desc, gurmail_module_code, gurmail_letr_code

  * Form Name: RUAMAIL
  *  order by gurmail_system_ind, gurmail_date_printed desc, gurmail_date_init desc, gurmail_letr_code asc

*/
/*PROTECTED REGION END*/
@Entity
@Table(name = "GURMAIL")
class Mail implements Serializable {

	/**
	 * Surrogate ID for GURMAIL
	 */
	@Id
	@Column(name="GURMAIL_SURROGATE_ID")
	@SequenceGenerator(name ="GURMAIL_SEQ_GEN", allocationSize =1, sequenceName  ="GURMAIL_SURROGATE_ID_SEQUENCE")
	@GeneratedValue(strategy =GenerationType.SEQUENCE, generator ="GURMAIL_SEQ_GEN")
	Long id

	/**
	 * Optimistic lock token for GURMAIL
	 */
	@Version
	@Column(name = "GURMAIL_VERSION", nullable = false, precision = 19)
	Long version

	/**
	 * Internal system identification number of person.
	 */
	@Column(name = "GURMAIL_PIDM", nullable = false, precision = 8)
	Integer pidm

	/**
	 * SYSTEM INDICATOR: Indicates which BANNER system this record pertains to.  Values are from the GTVSYSI form.
	 */
	@Column(name = "GURMAIL_SYSTEM_IND", nullable = false, length = 2)
	String systemIndicator

	/**
	 * MODULE CODE: The module associated with this letter.
	 */
	@Column(name = "GURMAIL_MODULE_CODE", length = 1)
	String module

	/**
	 * ADMIN IDENTIFIER: Application Number (BANNER Student only).
	 */
	@Column(name = "GURMAIL_ADMIN_IDENTIFIER", precision = 2)
	Integer adminIdentifier

	/**
	 * MATERIALS: The Materials code (from STVMATL).
	 */
	@Column(name = "GURMAIL_MATL_CODE_MOD", length = 4)
	String materialMod

	/**
	 * DATE INITIATED: Date the letter was initiated.
	 */
	@Temporal(TemporalType.DATE)
	@Column(name = "GURMAIL_DATE_INIT")
	Date dateInitial

	/**
	 * DATE PRINTED: Date the letter was printed.
	 */
	@Temporal(TemporalType.DATE)
	@Column(name = "GURMAIL_DATE_PRINTED")
	Date datePrinted

	/**
	 * USER: The USERID which initiated this letter.
	 */
	@Column(name = "GURMAIL_USER", length = 30)
	String userData

	/**
	 * WAIT DAYS: The number of days between initiating and printing the letter.
	 */
	@Column(name = "GURMAIL_WAIT_DAYS", precision = 3)
	Integer waitDays

	/**
	 * PUBLISHED/GENERATED: Code indicating published or generated materials (P/G).
	 */
	@Column(name = "GURMAIL_PUB_GEN", length = 1)
	String publishedGenerated

	/**
	 * ORIGIN: S = System-generated record; blank = entered on Mail form.
	 */
	@Column(name = "GURMAIL_ORIG_IND", length = 1)
	String originalIndicator

	/**
	 * AID YEAR CODE: The aid year to be associated with information in this record.  EXAMPLES: 9091, 9192, 9293, etc.
	 */
	@Column(name = "GURMAIL_AIDY_CODE", length = 4)
	String aidYear

	/**
	 * Quantity of materials to be sent.
	 */
	@Column(name = "GURMAIL_QTY", precision = 3)
	Integer quantity

	/**
	 * None
	 */
	@Column(name = "GURMAIL_MISC_VC2", length = 15)
	String miscellaneousVc2

	/**
	 * None
	 */
	@Temporal(TemporalType.DATE)
	@Column(name = "GURMAIL_MISC_DATE")
	Date miscellaneousDate

	/**
	 * None
	 */
	@Column(name = "GURMAIL_MISC_NUM", precision = 0)
	Integer miscellaneousNumber

	/**
	 * ACTIVITY DATE: Date the record was created or last updated.
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "GURMAIL_ACTIVITY_DATE")
	Date lastModified

	/**
	 * Last modified by column for GURMAIL
	 */
	@Column(name = "GURMAIL_USER_ID", length = 30)
	String lastModifiedBy

	/**
	 * Data origin column for GURMAIL
	 */
	@Column(name = "GURMAIL_DATA_ORIGIN", length = 30)
	String dataOrigin


	/**
	 * Foreign Key : FKV_GURMAIL_INV_STVTERM_CODE
	 */
	@ManyToOne
	@JoinColumns([
		@JoinColumn(name="GURMAIL_TERM_CODE", referencedColumnName="STVTERM_CODE")
		])
	Term term

	/**
	 * Foreign Key : FKV_GURMAIL_INV_GTVLETR_CODE
	 */
	@ManyToOne
	@JoinColumns([
		@JoinColumn(name="GURMAIL_LETR_CODE", referencedColumnName="GTVLETR_CODE")
		])
	LetterProcessLetter letterProcessLetter

	/**
	 * Foreign Key : FKV_GURMAIL_INV_STVINIT_CODE
	 */
	@ManyToOne
	@JoinColumns([
		@JoinColumn(name="GURMAIL_INIT_CODE", referencedColumnName="STVINIT_CODE")
		])
	Initials initials


    //TODO: This field needs to have a ManyToOne relationship with the CommunicationPlan domain
    /**
	 * Foreign Key : FKV_GURMAIL_INV_STVCPLN_CODE
	 */
    /*
	@ManyToOne
	@JoinColumns([
		@JoinColumn(name="GURMAIL_CPLN_CODE", referencedColumnName="STVCPLN_CODE")
		])
	CommunicationPlan communicationPlan
    */
    @Column(name = "GURMAIL_CPLN_CODE", length = 4)
    String communicationPlan

	public String toString() {
		"""Mail[
					id=$id,
					version=$version,
					pidm=$pidm,
					systemIndicator=$systemIndicator,
					module=$module,
					adminIdentifier=$adminIdentifier,
					materialMod=$materialMod,
					dateInitial=$dateInitial,
					datePrinted=$datePrinted,
					userData=$userData,
					waitDays=$waitDays,
					publishedGenerated=$publishedGenerated,
					originalIndicator=$originalIndicator,
					aidYear=$aidYear,
					quantity=$quantity,
					miscellaneousVc2=$miscellaneousVc2,
					miscellaneousDate=$miscellaneousDate,
					miscellaneousNumber=$miscellaneousNumber,
					lastModified=$lastModified,
					lastModifiedBy=$lastModifiedBy,
					dataOrigin=$dataOrigin,
					term=$term,
					letterProcessLetter=$letterProcessLetter,
					initials=$initials,
					communicationPlan=$communicationPlan]"""
	}


	boolean equals(o) {
	    if (this.is(o)) return true
	    if (!(o instanceof Mail)) return false
	    Mail that = (Mail) o
        if(id != that.id) return false
        if(version != that.version) return false
        if(pidm != that.pidm) return false
        if(systemIndicator != that.systemIndicator) return false
        if(module != that.module) return false
        if(adminIdentifier != that.adminIdentifier) return false
        if(materialMod != that.materialMod) return false
        if(dateInitial != that.dateInitial) return false
        if(datePrinted != that.datePrinted) return false
        if(userData != that.userData) return false
        if(waitDays != that.waitDays) return false
        if(publishedGenerated != that.publishedGenerated) return false
        if(originalIndicator != that.originalIndicator) return false
        if(aidYear != that.aidYear) return false
        if(quantity != that.quantity) return false
        if(miscellaneousVc2 != that.miscellaneousVc2) return false
        if(miscellaneousDate != that.miscellaneousDate) return false
        if(miscellaneousNumber != that.miscellaneousNumber) return false
        if(lastModified != that.lastModified) return false
        if(lastModifiedBy != that.lastModifiedBy) return false
        if(dataOrigin != that.dataOrigin) return false
        if(term != that.term) return false
        if(letterProcessLetter != that.letterProcessLetter) return false
        if(initials != that.initials) return false
        if(communicationPlan != that.communicationPlan) return false
        return true
    }


	int hashCode() {
		int result
	    result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (pidm != null ? pidm.hashCode() : 0)
        result = 31 * result + (systemIndicator != null ? systemIndicator.hashCode() : 0)
        result = 31 * result + (module != null ? module.hashCode() : 0)
        result = 31 * result + (adminIdentifier != null ? adminIdentifier.hashCode() : 0)
        result = 31 * result + (materialMod != null ? materialMod.hashCode() : 0)
        result = 31 * result + (dateInitial != null ? dateInitial.hashCode() : 0)
        result = 31 * result + (datePrinted != null ? datePrinted.hashCode() : 0)
        result = 31 * result + (userData != null ? userData.hashCode() : 0)
        result = 31 * result + (waitDays != null ? waitDays.hashCode() : 0)
        result = 31 * result + (publishedGenerated != null ? publishedGenerated.hashCode() : 0)
        result = 31 * result + (originalIndicator != null ? originalIndicator.hashCode() : 0)
        result = 31 * result + (aidYear != null ? aidYear.hashCode() : 0)
        result = 31 * result + (quantity != null ? quantity.hashCode() : 0)
        result = 31 * result + (miscellaneousVc2 != null ? miscellaneousVc2.hashCode() : 0)
        result = 31 * result + (miscellaneousDate != null ? miscellaneousDate.hashCode() : 0)
        result = 31 * result + (miscellaneousNumber != null ? miscellaneousNumber.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        result = 31 * result + (term != null ? term.hashCode() : 0)
        result = 31 * result + (letterProcessLetter != null ? letterProcessLetter.hashCode() : 0)
        result = 31 * result + (initials != null ? initials.hashCode() : 0)
        result = 31 * result + (communicationPlan != null ? communicationPlan.hashCode() : 0)
        return result
	}

	static constraints = {
		pidm(nullable:false, min: -99999999, max: 99999999)
		systemIndicator(nullable:false, maxSize:2)
		module(nullable:true, maxSize:1)
		adminIdentifier(nullable:true, min: -99, max: 99)
		materialMod(nullable:true, maxSize:4)
		dateInitial(nullable:true)
		datePrinted(nullable:true)
		userData(nullable:true, maxSize:30)
		waitDays(nullable:true, min: -999, max: 999)
		publishedGenerated(nullable:true, maxSize:1)
		originalIndicator(nullable:true, maxSize:1, inList:["U","M","S","E"])
		aidYear(nullable:true, maxSize:4)
		quantity(nullable:true, min: -999, max: 999)
		miscellaneousVc2(nullable:true, maxSize:15)
		miscellaneousDate(nullable:true)
		miscellaneousNumber(nullable:true)
		lastModified(nullable:true)
		lastModifiedBy(nullable:true, maxSize:30)
		dataOrigin(nullable:true, maxSize:30)
		term(nullable:true)
		letterProcessLetter(nullable:true)
		initials(nullable:true)
		communicationPlan(nullable:true)
		/**
	     * Please put all the custom constraints in this protected section to protect the code
	     * from being overwritten on re-generation
	     */
	    /*PROTECTED REGION ID(mail_custom_constraints) ENABLED START*/

	    /*PROTECTED REGION END*/
    }

    /**
     * Please put all the custom/transient attributes with @Transient annotations in this protected section to protect the code
     * from being overwritten on re-generation
     */
    /*PROTECTED REGION ID(mail_custom_attributes) ENABLED START*/

    /*PROTECTED REGION END*/

    /**
     * Please put all the custom methods/code in this protected section to protect the code
     * from being overwritten on re-generation
     */
    /*PROTECTED REGION ID(mail_custom_methods) ENABLED START*/

    /*PROTECTED REGION END*/
}
