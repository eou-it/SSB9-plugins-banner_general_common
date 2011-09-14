
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
 Banner Automator Version: 1.24
 Generated: Tue Aug 09 14:09:38 IST 2011 
 */
package com.sungardhe.banner.general.overall

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

import org.hibernate.annotations.GenericGenerator


/**
 * Sequence Number Base Table
 */
/*PROTECTED REGION ID(sequencenumberbase_namedqueries) ENABLED START*/
//TODO: NamedQueries that needs to be ported:
 /**
    * Where clause on this entity present in forms:
  * Order by clause on this entity present in forms:
*/
/*PROTECTED REGION END*/
@Entity
@Table(name = "SOBSEQN")
class SequenceNumberBase implements Serializable {
	
	/**
	 * Surrogate ID for SOBSEQN
	 */
	@Id
	@Column(name="SOBSEQN_SURROGATE_ID")
	@SequenceGenerator(name ="SOBSEQN_SEQ_GEN", allocationSize =1, sequenceName  ="SOBSEQN_SURROGATE_ID_SEQUENCE")
	@GeneratedValue(strategy =GenerationType.SEQUENCE, generator ="SOBSEQN_SEQ_GEN")
	Long id

	/**
	 * Optimistic lock token for SOBSEQN
	 */
	@Version
	@Column(name = "SOBSEQN_VERSION", nullable = false, precision = 19)
	Long version

	/**
	 * Sequence Number Function.
	 */
	@Column(name = "SOBSEQN_FUNCTION", nullable = false, unique = true, length = 30)
	String function

	/**
	 * Sequence Number Prefix.
	 */
	@Column(name = "SOBSEQN_SEQNO_PREFIX", length = 1)
	String sequenceNumberPrefix

	/**
	 * Maxmum Sequence Number.
	 */
	@Column(name = "SOBSEQN_MAXSEQNO", nullable = false, precision = 8)
	Integer maximumSequenceNumber

	/**
	 * This field identifies the most recent date a record was created or updated.
	 */
	@Column(name = "SOBSEQN_ACTIVITY_DATE")
	Date lastModified

	/**
	 * Last modified by column for SOBSEQN
	 */
	@Column(name = "SOBSEQN_USER_ID", length = 30)
	String lastModifiedBy

	/**
	 * Data origin column for SOBSEQN
	 */
	@Column(name = "SOBSEQN_DATA_ORIGIN", length = 30)
	String dataOrigin

	
	
	public String toString() {
		"""SequenceNumberBase[
					id=$id, 
					version=$version, 
					function=$function, 
					sequenceNumberPrefix=$sequenceNumberPrefix, 
					maximumSequenceNumber=$maximumSequenceNumber, 
					lastModified=$lastModified, 
					lastModifiedBy=$lastModifiedBy, 
					dataOrigin=$dataOrigin]"""
	}

	
	boolean equals(o) {
	    if (this.is(o)) return true
	    if (!(o instanceof SequenceNumberBase)) return false
	    SequenceNumberBase that = (SequenceNumberBase) o
        if(id != that.id) return false
        if(version != that.version) return false
        if(function != that.function) return false
        if(sequenceNumberPrefix != that.sequenceNumberPrefix) return false
        if(maximumSequenceNumber != that.maximumSequenceNumber) return false
        if(lastModified != that.lastModified) return false
        if(lastModifiedBy != that.lastModifiedBy) return false
        if(dataOrigin != that.dataOrigin) return false
        return true
    }

	
	int hashCode() {
		int result
	    result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (function != null ? function.hashCode() : 0)
        result = 31 * result + (sequenceNumberPrefix != null ? sequenceNumberPrefix.hashCode() : 0)
        result = 31 * result + (maximumSequenceNumber != null ? maximumSequenceNumber.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        return result
	}

	static constraints = {
		function(nullable:false, maxSize:30)
		sequenceNumberPrefix(nullable:true, maxSize:1)
		maximumSequenceNumber(nullable:false, min: -99999999, max: 99999999)
		lastModified(nullable:true)
		lastModifiedBy(nullable:true, maxSize:30)
		dataOrigin(nullable:true, maxSize:30)
		/**
	     * Please put all the custom constraints in this protected section to protect the code
	     * from being overwritten on re-generation
	     */
	    /*PROTECTED REGION ID(sequencenumberbase_custom_constraints) ENABLED START*/
	    
	    /*PROTECTED REGION END*/
    }
    
    /*PROTECTED REGION ID(sequencenumberbase_readonly_properties) ENABLED START*/
    //Read Only fields that should be protected against update
    public static readonlyProperties = [ 'function' ]
    /*PROTECTED REGION END*/        
    /**
     * Please put all the custom/transient attributes with @Transient annotations in this protected section to protect the code
     * from being overwritten on re-generation
     */
    /*PROTECTED REGION ID(sequencenumberbase_custom_attributes) ENABLED START*/
    
    /*PROTECTED REGION END*/
        
    /**
     * Please put all the custom methods/code in this protected section to protect the code
     * from being overwritten on re-generation
     */
    /*PROTECTED REGION ID(sequencenumberbase_custom_methods) ENABLED START*/
    
    /*PROTECTED REGION END*/
}
