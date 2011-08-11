
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
import org.hibernate.annotations.Type
import javax.persistence.GenerationType
import javax.persistence.SequenceGenerator
import javax.persistence.Temporal
import javax.persistence.TemporalType

/**
 * Event Base Table
 */

 
// TODO change all code fields to Many to One with their perspective tables
@Entity
@Table(name="SLBEVNT")
class HousingEventBase implements Serializable {
	
	/**
	 * Surrogate ID for SLBEVNT
	 */
	@Id
	@Column(name="SLBEVNT_SURROGATE_ID")
    @SequenceGenerator(name = "SLBEVNT_SEQ_GEN", allocationSize = 1, sequenceName = "SLBEVNT_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SLBEVNT_SEQ_GEN")
	Long id

	/**
	 * This field is the oneup number that identifies the event to the system
	 */
	@Column(name="SLBEVNT_CRN", nullable = false, length=5)
	String courseReferenceNumber

	/**
	 * This field defines type associated with the event
	 */
	@Column(name="SLBEVNT_ETYP_CODE", nullable = false, length=4)
	String event

	/**
	 * This field identifies the description of the event
	 */
	@Column(name="SLBEVNT_DESC", length=30)
	String description

	/**
	 * This field identifies the agency pidm associated with the event
	 */
	@Column(name="SLBEVNT_AGENCY_PIDM", length=22)
	BigDecimal agencyPidm

	/**
	 * This field identifies the name of the person to contact within the agency associated with the event
	 */
	@Column(name="SLBEVNT_CONTACT_NAME", length=230)
	String contactName

	/**
	 * This field identifies the campus that is associated with the event
	 */
	@Column(name="SLBEVNT_CAMP_CODE", length=3)
	String campus

	/**
	 * This field identifies the site that is associated with the event
	 */
	@Column(name="SLBEVNT_SITE_CODE", length=3)
	String site

	/**
	 * This field identifies the college that is associated with the event
	 */
	@Column(name="SLBEVNT_COLL_CODE", length=2)
	String college

	/**
	 * This field identifies the department that is associated with the event
	 */
	@Column(name="SLBEVNT_DEPT_CODE", length=4)
	String department

	/**
	 * This field identifies the date the record was created or last updated
	 */
	@Column(name="SLBEVNT_ACTIVITY_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	Date lastModified

	/**
	 * This field identifies the area code of the contact or agency associated with the event
	 */
	@Column(name="SLBEVNT_PHONE_AREA", length=6)
	String phoneArea

	/**
	 * This field identifies the phone number of the contact or agency associated with the event
	 */
	@Column(name="SLBEVNT_PHONE", length=7)
	String phone

	/**
	 * This field identifies the phone extension number of the contact or agency associated with the event
	 */
	@Column(name="SLBEVNT_PHONE_EXT", length=10)
	String phoneExt

	/**
	 * Internal Person Identification Number of the ID representing the Contact assigned to the event.
	 */
	@Column(name="SLBEVNT_CONTACT_PIDM", length=22)
	BigDecimal contactPidm

	/**
	 * Address type of the Contact assigned to the event.  Only populated if Contact Pidm is not null.
	 */
	@Column(name="SLBEVNT_ATYP_CODE", length=2)
	String addressType

	/**
	 * Code indicating the product that created the event.
	 */
	@Column(name="SLBEVNT_SYSI_CODE", length=2)
	String system

	/**
	 * Committee/Service Indicator.
	 */
	@Type(type = "yes_no")
	@Column(name="SLBEVNT_COMM_IND", nullable = false)
	Boolean committeeIndicator

	/**
	 * District/Division Code.
	 */
	@Column(name="SLBEVNT_DICD_CODE", length=3)
	String district

	/**
	 * COUNTRY CODE: Telephone code that designates the region and country.
	 */
	@Column(name="SLBEVNT_CTRY_CODE_PHONE", length=4)
	String countryPhone

	/**
	 * Version column which is used as a optimistic lock token for SLBEVNT
	 */
	@Version
	@Column(name="SLBEVNT_VERSION", nullable = false, length=19)
	Long version

	/**
	 * Last Modified By column for SLBEVNT
	 */
	@Column(name="SLBEVNT_USER_ID", length=30)
	String lastModifiedBy

	/**
	 * Data Origin column for SLBEVNT
	 */
	@Column(name="SLBEVNT_DATA_ORIGIN", length=30)
	String dataOrigin

	
	
	public String toString() {
		"""HousingEventBase[id=$id,
          courseReferenceNumber=$courseReferenceNumber,
          event=$event,
          description=$description,
          agencyPidm=$agencyPidm,
          contactName=$contactName,
          campus=$campus,
          site=$site,
          college=$college,
          department=$department,
          lastModified=$lastModified,
          phoneArea=$phoneArea,  phone=$phone, phoneExt=$phoneExt,
          contactPidm=$contactPidm,
          addressType=$addressType,
          system=$system,
          committeeIndicator=$committeeIndicator,
          dicdCode=$district,
          ctryCodePhone=$countryPhone,
          version=$version, lastModifiedBy=$lastModifiedBy, dataOrigin=$dataOrigin]"""
	}


    boolean equals(o) {
        if (this.is(o)) return true;

        if (!(o instanceof HousingEventBase)) return false;

        HousingEventBase that = (HousingEventBase) o;

        if (addressType != that.addressType) return false;
        if (agencyPidm != that.agencyPidm) return false;
        if (campus != that.campus) return false;
        if (college != that.college) return false;
        if (committeeIndicator != that.committeeIndicator) return false;
        if (contactName != that.contactName) return false;
        if (contactPidm != that.contactPidm) return false;
        if (countryPhone != that.countryPhone) return false;
        if (courseReferenceNumber != that.courseReferenceNumber) return false;
        if (dataOrigin != that.dataOrigin) return false;
        if (department != that.department) return false;
        if (description != that.description) return false;
        if (district != that.district) return false;
        if (event != that.event) return false;
        if (id != that.id) return false;
        if (lastModified != that.lastModified) return false;
        if (lastModifiedBy != that.lastModifiedBy) return false;
        if (phone != that.phone) return false;
        if (phoneArea != that.phoneArea) return false;
        if (phoneExt != that.phoneExt) return false;
        if (site != that.site) return false;
        if (system != that.system) return false;
        if (version != that.version) return false;

        return true;
    }


    int hashCode() {
        int result;

        result = (id != null ? id.hashCode() : 0);
        result = 31 * result + (courseReferenceNumber != null ? courseReferenceNumber.hashCode() : 0);
        result = 31 * result + (event != null ? event.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (agencyPidm != null ? agencyPidm.hashCode() : 0);
        result = 31 * result + (contactName != null ? contactName.hashCode() : 0);
        result = 31 * result + (campus != null ? campus.hashCode() : 0);
        result = 31 * result + (site != null ? site.hashCode() : 0);
        result = 31 * result + (college != null ? college.hashCode() : 0);
        result = 31 * result + (department != null ? department.hashCode() : 0);
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0);
        result = 31 * result + (phoneArea != null ? phoneArea.hashCode() : 0);
        result = 31 * result + (phone != null ? phone.hashCode() : 0);
        result = 31 * result + (phoneExt != null ? phoneExt.hashCode() : 0);
        result = 31 * result + (contactPidm != null ? contactPidm.hashCode() : 0);
        result = 31 * result + (addressType != null ? addressType.hashCode() : 0);
        result = 31 * result + (system != null ? system.hashCode() : 0);
        result = 31 * result + (committeeIndicator != null ? committeeIndicator.hashCode() : 0);
        result = 31 * result + (district != null ? district.hashCode() : 0);
        result = 31 * result + (countryPhone != null ? countryPhone.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0);
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0);
        return result;
    }


    static constraints = {
		courseReferenceNumber(nullable:false, maxSize:5)
		event(nullable:false, maxSize:4)
		description(nullable:true, maxSize:30)
		agencyPidm(nullable:true, scale:0)
		contactName(nullable:true, maxSize:230)
		campus(nullable:true, maxSize:3)
		site(nullable:true, maxSize:3)
		college(nullable:true, maxSize:2)
		department(nullable:true, maxSize:4)
		lastModified(nullable:true)
		phoneArea(nullable:true, maxSize:6)
		phone(nullable:true, maxSize:7)
		phoneExt(nullable:true, maxSize:10)
		contactPidm(nullable:true, scale:0)
		addressType(nullable:true, maxSize:2)
		system(nullable:true, maxSize:2)
		committeeIndicator(nullable:false)
		district(nullable:true, maxSize:3)
		countryPhone(nullable:true, maxSize:4)
		lastModifiedBy(nullable:true, maxSize:30)
		dataOrigin(nullable:true, maxSize:30)



		/**
	     * Please put all the custom tests in this protected section to protect the code
	     * from being overwritten on re-generation
	     */
	    /*PROTECTED REGION ID(housingeventbase_custom_constraints) ENABLED START*/
	    
	    /*PROTECTED REGION END*/
    }
    
    /**
     * Please put all the custom methods/code in this protected section to protect the code
     * from being overwritten on re-generation
     */
    /*PROTECTED REGION ID(housingeventbase_custom_methods) ENABLED START*/
    
    /*PROTECTED REGION END*/
}
