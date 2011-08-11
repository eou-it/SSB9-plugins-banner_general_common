/*******************************************************************************
 © 2011 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 *******************************************************************************/
/**
 Banner Automator Version: 0.1.1
 Generated: Fri Feb 11 16:36:47 EST 2011 
 */
package com.sungardhe.banner.general.overall

import com.sungardhe.banner.service.DatabaseModifiesState
import com.sungardhe.banner.general.system.*
import org.hibernate.annotations.Type
import javax.persistence.*

/**
 * Location/Building Description Table
 */
/*PROTECTED REGION ID(housinglocationbuildingdescription_namedqueries) ENABLED START*/
/*PROTECTED REGION END*/
@Entity
@Table( name = "SV_SLBBLDG" )
@NamedQueries( value = [
@NamedQuery( name = "HousingLocationBuildingDescription.fetchAllByBuilding",
             query = """FROM  HousingLocationBuildingDescription a
                        where  a.building like  :building
	  	                order by  a.building """),
@NamedQuery( name = "HousingLocationBuildingDescription.fetchValidateBuilding",
             query = """FROM  HousingLocationBuildingDescription a
                        WHERE a.building = :building """ )
])
@DatabaseModifiesState 
class HousingLocationBuildingDescription implements Serializable {
	
	/**
	 * Surrogate ID for SLBBLDG
	 */
	@Id
	@Column(name="SLBBLDG_SURROGATE_ID")
	@SequenceGenerator(name ="SLBBLDG_SEQ_GEN", allocationSize =1, sequenceName  ="SLBBLDG_SURROGATE_ID_SEQUENCE")
	@GeneratedValue(strategy =GenerationType.SEQUENCE, generator ="SLBBLDG_SEQ_GEN")
	Long id

	/**
	 * Optimistic lock token for SLBBLDG
	 */
	@Version
	@Column(name = "SLBBLDG_VERSION", nullable = false, precision = 19)
	Long version

	/**
	 * This column identifies the capacity of the building
	 */
	@Column(name = "SLBBLDG_CAPACITY", nullable = false, precision = 5)
	Integer capacity

	/**
	 * This column identifies the maximum capacity of the building
	 */
	@Column(name = "SLBBLDG_MAXIMUM_CAPACITY", precision = 5)
	Integer maximumCapacity

	/**
	 * This column identifies the first street line for the address of the building
	 */
	@Column(name = "SLBBLDG_STREET_LINE1", length = 75)
	String streetLine1

	/**
	 * This column identifies the second street line of the address of the building
	 */
	@Column(name = "SLBBLDG_STREET_LINE2", length = 75)
	String streetLine2

	/**
	 * This column identifies the third street line of the address of the building
	 */
	@Column(name = "SLBBLDG_STREET_LINE3", length = 75)
	String streetLine3

	/**
	 * This column identifies the city where the building is located
	 */
	@Column(name = "SLBBLDG_CITY", length = 50)
	String city

	/**
	 * This column identifies the zip code of the building
	 */
	@Column(name = "SLBBLDG_ZIP", length = 30)
	String zip

	/**
	 * This column identifies the area code of the phone number for the building
	 */
	@Column(name = "SLBBLDG_PHONE_AREA", length = 6)
	String phoneArea

	/**
	 * This column identifies the phone number for the building
	 */
	@Column(name = "SLBBLDG_PHONE_NUMBER", length = 12)
	String phoneNumber

	/**
	 * This column identifies the phone extension of the phone number for the building
	 */
	@Column(name = "SLBBLDG_PHONE_EXTENSION", length = 10)
	String phoneExtension

	/**
	 * This column identifies the gender associated with the building
	 */
	@Column(name = "SLBBLDG_SEX", length = 1)
	String sex

	/**
	 * This column identifies the number of the key to the building
	 */
	@Column(name = "SLBBLDG_KEY_NUMBER", length = 5)
	String keyNumber

	/**
	 * COUNTRY CODE: Telephone code that designates the region and country.
	 */
	@Column(name = "SLBBLDG_CTRY_CODE_PHONE", length = 4)
	String countryPhone

	/**
	 * HOUSE NUMBER: Building or lot number on a street or in an area.
	 */
	@Column(name = "SLBBLDG_HOUSE_NUMBER", length = 10)
	String houseNumber

	/**
	 * STREET LINE 4: This column identifies the fourth street line of the address of the building
	 */
	@Column(name = "SLBBLDG_STREET_LINE4", length = 75)
	String streetLine4

	/**
	 * This column identifies the date the record was created or last updated
	 */
	@Column(name = "SLBBLDG_ACTIVITY_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	Date lastModified

	/**
	 * USER ID: User who inserted or last update the data
	 */
	@Column(name = "SLBBLDG_USER_ID", length = 30)
	String lastModifiedBy

	/**
	 * DATA SOURCE: Source system that created or updated the row
	 */
	@Column(name = "SLBBLDG_DATA_ORIGIN", length = 30)
	String dataOrigin

	
	/**
	 * Foreign Key : FK1_SLBBLDG_INV_STVBLDG_CODE
	 */
	@ManyToOne
	@JoinColumns([
		@JoinColumn(name="SLBBLDG_BLDG_CODE", referencedColumnName="STVBLDG_CODE")
		])
	Building building

	/**
	 * Foreign Key : FKV_SLBBLDG_INV_STVCAMP_CODE
	 */
	@ManyToOne
	@JoinColumns([
		@JoinColumn(name="SLBBLDG_CAMP_CODE", referencedColumnName="STVCAMP_CODE")
		])
	Campus campus

	/**
	 * Foreign Key : FKV_SLBBLDG_INV_STVRRCD_CODE
	 */
	@ManyToOne
	@JoinColumns([
		@JoinColumn(name="SLBBLDG_RRCD_CODE", referencedColumnName="STVRRCD_CODE")
		])
	RoomRate roomRate

	/**
	 * Foreign Key : FKV_SLBBLDG_INV_STVPRCD_CODE
	 */
	@ManyToOne
	@JoinColumns([
		@JoinColumn(name="SLBBLDG_PRCD_CODE", referencedColumnName="STVPRCD_CODE")
		])
	PhoneRate phoneRate

	/**
	 * Foreign Key : FKV_SLBBLDG_INV_STVSITE_CODE
	 */
	@ManyToOne
	@JoinColumns([
		@JoinColumn(name="SLBBLDG_SITE_CODE", referencedColumnName="STVSITE_CODE")
		])
	Site site

	/**
	 * Foreign Key : FKV_SLBBLDG_INV_STVSTAT_CODE
	 */
	@ManyToOne
	@JoinColumns([
		@JoinColumn(name="SLBBLDG_STAT_CODE", referencedColumnName="STVSTAT_CODE")
		])
	State state

	/**
	 * Foreign Key : FKV_SLBBLDG_INV_STVCNTY_CODE
	 */
	@ManyToOne
	@JoinColumns([
		@JoinColumn(name="SLBBLDG_CNTY_CODE", referencedColumnName="STVCNTY_CODE")
		])
	County county

	/**
	 * Foreign Key : FKV_SLBBLDG_INV_STVCOLL_CODE
	 */
	@ManyToOne
	@JoinColumns([
		@JoinColumn(name="SLBBLDG_COLL_CODE", referencedColumnName="STVCOLL_CODE")
		])
	College college

	/**
	 * Foreign Key : FKV_SLBBLDG_INV_STVDEPT_CODE
	 */
	@ManyToOne
	@JoinColumns([
		@JoinColumn(name="SLBBLDG_DEPT_CODE", referencedColumnName="STVDEPT_CODE")
		])
	Department department

	/**
	 * Foreign Key : FKV_SLBBLDG_INV_GTVPARS_CODE
	 */
	@ManyToOne
	@JoinColumns([
		@JoinColumn(name="SLBBLDG_PARS_CODE", referencedColumnName="GTVPARS_CODE")
		])
	Partition partition

	
	public String toString() {
		"""HousingLocationBuildingDescription[
					id=$id, 
					version=$version, 
					capacity=$capacity, 
					maximumCapacity=$maximumCapacity, 
					streetLine1=$streetLine1, 
					streetLine2=$streetLine2, 
					streetLine3=$streetLine3, 
					city=$city, 
					zip=$zip, 
					phoneArea=$phoneArea, 
					phoneNumber=$phoneNumber, 
					phoneExtension=$phoneExtension, 
					sex=$sex, 
					keyNumber=$keyNumber, 
					countryPhone=$countryPhone, 
					houseNumber=$houseNumber, 
					streetLine4=$streetLine4, 
					lastModified=$lastModified, 
					lastModifiedBy=$lastModifiedBy, 
					dataOrigin=$dataOrigin, 
					building=$building, 
					campus=$campus, 
					roomRate=$roomRate, 
					phoneRate=$phoneRate, 
					site=$site, 
					state=$state, 
					county=$county, 
					college=$college, 
					department=$department, 
					partition=$partition]"""
	}

	
	boolean equals(o) {
	    if (this.is(o)) return true
	    if (!(o instanceof HousingLocationBuildingDescription)) return false
	    HousingLocationBuildingDescription that = (HousingLocationBuildingDescription) o
        if(id != that.id) return false
        if(version != that.version) return false
        if(capacity != that.capacity) return false
        if(maximumCapacity != that.maximumCapacity) return false
        if(streetLine1 != that.streetLine1) return false
        if(streetLine2 != that.streetLine2) return false
        if(streetLine3 != that.streetLine3) return false
        if(city != that.city) return false
        if(zip != that.zip) return false
        if(phoneArea != that.phoneArea) return false
        if(phoneNumber != that.phoneNumber) return false
        if(phoneExtension != that.phoneExtension) return false
        if(sex != that.sex) return false
        if(keyNumber != that.keyNumber) return false
        if(countryPhone != that.countryPhone) return false
        if(houseNumber != that.houseNumber) return false
        if(streetLine4 != that.streetLine4) return false
        if(lastModified != that.lastModified) return false
        if(lastModifiedBy != that.lastModifiedBy) return false
        if(dataOrigin != that.dataOrigin) return false
        if(building != that.building) return false      
        if(campus != that.campus) return false      
        if(roomRate != that.roomRate) return false      
        if(phoneRate != that.phoneRate) return false      
        if(site != that.site) return false      
        if(state != that.state) return false      
        if(county != that.county) return false      
        if(college != that.college) return false      
        if(department != that.department) return false      
        if(partition != that.partition) return false      
        return true
    }

	
	int hashCode() {
		int result
	    result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (capacity != null ? capacity.hashCode() : 0)
        result = 31 * result + (maximumCapacity != null ? maximumCapacity.hashCode() : 0)
        result = 31 * result + (streetLine1 != null ? streetLine1.hashCode() : 0)
        result = 31 * result + (streetLine2 != null ? streetLine2.hashCode() : 0)
        result = 31 * result + (streetLine3 != null ? streetLine3.hashCode() : 0)
        result = 31 * result + (city != null ? city.hashCode() : 0)
        result = 31 * result + (zip != null ? zip.hashCode() : 0)
        result = 31 * result + (phoneArea != null ? phoneArea.hashCode() : 0)
        result = 31 * result + (phoneNumber != null ? phoneNumber.hashCode() : 0)
        result = 31 * result + (phoneExtension != null ? phoneExtension.hashCode() : 0)
        result = 31 * result + (sex != null ? sex.hashCode() : 0)
        result = 31 * result + (keyNumber != null ? keyNumber.hashCode() : 0)
        result = 31 * result + (countryPhone != null ? countryPhone.hashCode() : 0)
        result = 31 * result + (houseNumber != null ? houseNumber.hashCode() : 0)
        result = 31 * result + (streetLine4 != null ? streetLine4.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        result = 31 * result + (building != null ? building.hashCode() : 0)
        result = 31 * result + (campus != null ? campus.hashCode() : 0)
        result = 31 * result + (roomRate != null ? roomRate.hashCode() : 0)
        result = 31 * result + (phoneRate != null ? phoneRate.hashCode() : 0)
        result = 31 * result + (site != null ? site.hashCode() : 0)
        result = 31 * result + (state != null ? state.hashCode() : 0)
        result = 31 * result + (county != null ? county.hashCode() : 0)
        result = 31 * result + (college != null ? college.hashCode() : 0)
        result = 31 * result + (department != null ? department.hashCode() : 0)
        result = 31 * result + (partition != null ? partition.hashCode() : 0)
        return result
	}

	static constraints = {
		capacity(nullable:false, min: -99999, max: 99999)
		maximumCapacity(nullable:true, min: -99999, max: 99999)
		streetLine1(nullable:true, maxSize:75)
		streetLine2(nullable:true, maxSize:75)
		streetLine3(nullable:true, maxSize:75)
		city(nullable:true, maxSize:50)
		zip(nullable:true, maxSize:30)
		phoneArea(nullable:true, maxSize:6)
		phoneNumber(nullable:true, maxSize:12)
		phoneExtension(nullable:true, maxSize:10)
		sex(nullable:true, maxSize:1)
		keyNumber(nullable:true, maxSize:5)
		countryPhone(nullable:true, maxSize:4)
		houseNumber(nullable:true, maxSize:10)
		streetLine4(nullable:true, maxSize:75)
		lastModified(nullable:true)
		lastModifiedBy(nullable:true, maxSize:30)
		dataOrigin(nullable:true, maxSize:30)
		building(nullable:false)
		campus(nullable:false)
		roomRate(nullable:true)
		phoneRate(nullable:true)
		site(nullable:true)
		state(nullable:true)
		county(nullable:true)
		college(nullable:true)
		department(nullable:true)
		partition(nullable:true)
		/**
	     * Please put all the custom constraints in this protected section to protect the code
	     * from being overwritten on re-generation
	     */
	    /*PROTECTED REGION ID(housinglocationbuildingdescription_custom_constraints) ENABLED START*/
	    
	    /*PROTECTED REGION END*/
    }
    
    /*PROTECTED REGION ID(housinglocationbuildingdescription_readonly_properties) ENABLED START*/
    //Read Only fields that should be protected against update
    public static readonlyProperties = [ 'building' ]
    /*PROTECTED REGION END*/        
    /**
     * Please put all the custom/transient attributes with @Transient annotations in this protected section to protect the code
     * from being overwritten on re-generation
     */
    /*PROTECTED REGION ID(housinglocationbuildingdescription_custom_attributes) ENABLED START*/
    
    /*PROTECTED REGION END*/
        
    /**
     * Please put all the custom methods/code in this protected section to protect the code
     * from being overwritten on re-generation
     */
    /*PROTECTED REGION ID(housinglocationbuildingdescription_custom_methods) ENABLED START*/


    public static Object fetchBySomeHousingLocationBuildingDescriptionBuilding() {
        def returnObj = [list: HousingLocationBuildingDescription.list().sort {it.building}]
        return returnObj
    }


    public static Object fetchBySomeHousingLocationBuildingDescriptionBuilding(Map params) {
        def building = HousingLocationBuildingDescription.withSession {session ->
            session.getNamedQuery('HousingLocationBuildingDescription.fetchAllByBuilding').setString('building', "%" + params?.building?.code + "%").list()
        }
        return [list:building]
    }

    public static Object fetchBySomeHousingLocationBuildingDescriptionBuilding(String filter) {
        def building = HousingLocationBuildingDescription.withSession {session ->
            session.getNamedQuery('HousingLocationBuildingDescription.fetchAllByBuilding').setString('building', "%" + filter.toUpperCase() + "%").list()
        }
        return [list:building]
    }


    public static Object fetchValidBuilding(String buildingCode) {
        def building = HousingLocationBuildingDescription.withSession {session ->
            session.getNamedQuery('HousingLocationBuildingDescription.fetchValidateBuilding').setString('building', buildingCode.toUpperCase()).list()
        }

        return building[0]
    }
    public static Object fetchValidBuilding(Building building) {
        def validBuilding = HousingLocationBuildingDescription.withSession {session ->
            session.getNamedQuery('HousingLocationBuildingDescription.fetchValidateBuilding').setString('building', building.code).list()
        }

        return validBuilding[0]
    }
    /*PROTECTED REGION END*/
}
