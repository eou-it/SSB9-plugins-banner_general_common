/*********************************************************************************
 Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of 
 SunGard Higher Education and its subsidiaries. Any use of this software is limited 
 solely to SunGard Higher Education licensees, and is further subject to the terms 
 and conditions of one or more written license agreements between SunGard Higher 
 Education and the licensee in question. SunGard, Banner and Luminis are either 
 registered trademarks or trademarks of SunGard Higher Education in the U.S.A. 
 and/or other regions and/or countries.
 **********************************************************************************/
package com.sungardhe.banner.general.overall

import com.sungardhe.banner.general.system.IntegrationPartner
import com.sungardhe.banner.service.DatabaseModifiesState

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.Version
import javax.persistence.SequenceGenerator
import javax.persistence.GenerationType
import javax.persistence.ManyToOne
import javax.persistence.JoinColumns
import org.hibernate.annotations.Type
import javax.persistence.JoinColumn
import javax.persistence.Temporal
import javax.persistence.TemporalType

/**
 * Integration Partner System Rule Table.
 */
@Entity
@Table( name="GV_GORINTG" )
@DatabaseModifiesState 
class IntegrationPartnerSystemRule implements Serializable {
	
	/**
	 * Surrogate ID for GORINTG
    */
  	@Id
   	@SequenceGenerator(name = "GORINTG_SEQ_GEN", allocationSize = 1, sequenceName = "GORINTG_SURROGATE_ID_SEQUENCE")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GORINTG_SEQ_GEN")
	@Column(name = "GORINTG_SURROGATE_ID")
 	Long id

	/**
	 * INTEGRATION CODE: User Defined Integration Code that is used with external partner.
	 */
	@Column(name="GORINTG_INTEGRATION_CDE", nullable = false, length=5)
	String code

	/**
	 * Description: User Description of the Integration Code.
	 */
	@Column(name="GORINTG_DESC", nullable = false, length=30)
	String description

	/**
	 * INTEGRATION PARTNER SYSTEM CODE: Code defined on GTVINTP that associates with the User Integration Code.
	 */
	//@Column(name="INTEGRATIONPARTNER", nullable = false, length=5)
   @ManyToOne
   @JoinColumns([
   @JoinColumn(name = "GORINTG_INTP_CODE", referencedColumnName = "gtvintp_code")
   ])
   IntegrationPartner integrationPartner

	/**
	 * USER ID: The unique identification of the user.
	 */
	@Column(name="GORINTG_USER_ID", length=30)
	String lastModifiedBy

	/**
	 * ACTIVITY DATE: The date that the information for the row was inserted or updated in the GORINTG table.
	 */
	@Column(name="GORINTG_ACTIVITY_DATE" )
	@Temporal(TemporalType.TIMESTAMP)
	Date lastModified

	/**
	 * DATA ORIGIN: Source system that created or updated the row
	 */
	@Column(name="GORINTG_DATA_ORIGIN", length=30)
	String dataOrigin

	/**
	 * Version column which is used as a optimistic lock token for GORINTG
	 */
    @Version
	@Column(name="GORINTG_VERSION", nullable = false, length=19)
	Long version
  	
	
	public String toString() {
		"IntegrationPartnerSystemRule[id=$id, code=$code, description=$description, integrationPartner=$integrationPartner, lastModifiedBy=$lastModifiedBy, lastModified=$lastModified, dataOrigin=$dataOrigin, version=$version]"
	}


    boolean equals(o) {
        if (this.is(o)) return true;

        if (!(o instanceof IntegrationPartnerSystemRule)) return false;

        IntegrationPartnerSystemRule that = (IntegrationPartnerSystemRule) o;

        if (code != that.code) return false;
        if (dataOrigin != that.dataOrigin) return false;
        if (description != that.description) return false;
        if (id != that.id) return false;
        if (integrationPartner != that.integrationPartner) return false;
        if (lastModified != that.lastModified) return false;
        if (lastModifiedBy != that.lastModifiedBy) return false;
        if (version != that.version) return false;

        return true;
    }


    int hashCode() {
        int result;

        result = (id != null ? id.hashCode() : 0);
        result = 31 * result + (code != null ? code.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (integrationPartner != null ? integrationPartner.hashCode() : 0);
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0);
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0);
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }


    static constraints = {
		code(nullable:false, maxSize:5)
		description(nullable:false, maxSize:30)
		integrationPartner(nullable:false)
		lastModifiedBy(nullable:true, maxSize:30)
		lastModified(nullable:true)
		dataOrigin(nullable:true, maxSize:30)



		/**
	     * Please put all the custom tests in this protected section to protect the code
	     * from being overwritten on re-generation
	     */
	    /*PROTECTED REGION ID(integrationpartnersystemrule_custom_constraints) ENABLED START*/
	    
	    /*PROTECTED REGION END*/
    }
    
    /**
     * Please put all the custom methods/code in this protected section to protect the code
     * from being overwritten on re-generation
     */
    /*PROTECTED REGION ID(integrationpartnersystemrule_custom_methods) ENABLED START*/
    
    /*PROTECTED REGION END*/
}
