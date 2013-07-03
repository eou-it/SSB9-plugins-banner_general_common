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
 Banner Automator Version: 1.21
 Generated: Wed Jul 20 14:02:45 EDT 2011 
 */
package net.hedtech.banner.general.overall

import org.hibernate.annotations.Type
import javax.persistence.*

/**
 * Faculty Member Tenure Status Code Table
 */
/*PROTECTED REGION ID(facultytenurestatus_namedqueries) ENABLED START*/

/*PROTECTED REGION END*/
@Entity
@Table(name = "PTRTENR")
class FacultyTenureStatus implements Serializable {

    /**
     * Surrogate ID for PTRTENR
     */
    @Id
    @Column(name = "PTRTENR_SURROGATE_ID")
    @SequenceGenerator(name = "PTRTENR_SEQ_GEN", allocationSize = 1, sequenceName = "PTRTENR_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PTRTENR_SEQ_GEN")
    Long id

    /**
     * Optimistic lock token for PTRTENR
     */
    @Version
    @Column(name = "PTRTENR_VERSION", nullable = false, precision = 19)
    Long version

    /**
     * Faculty member tenure status code
     */
    @Column(name = "PTRTENR_CODE", nullable = false, unique = true, length = 2)
    String code

    /**
     * Description of faculty member tenure status code
     */
    @Column(name = "PTRTENR_DESC", nullable = false, length = 30)
    String description

    /**
     * Tenure Date Indicator
     */
    @Type(type = "yes_no")
    @Column(name = "PTRTENR_DATE_IND", nullable = false, length = 1)
    Boolean dateIndicator

    /**
     * Tenure Review Date Indicator
     */
    @Type(type = "yes_no")
    @Column(name = "PTRTENR_REVIEW_DATE_IND", nullable = false, length = 1)
    Boolean reviewDateIndicator

    /**
     * EEO Tenure Indicator.
     */
    @Column(name = "PTRTENR_EEO_TENURE_IND", nullable = false, length = 1)
    String eeoTenureIndicator

    /**
     * The date of the last insert or update of this record
     */
    @Column(name = "PTRTENR_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * Last modified by column for PTRTENR
     */
    @Column(name = "PTRTENR_USER_ID", length = 30)
    String lastModifiedBy

    /**
     * Data origin column for PTRTENR
     */
    @Column(name = "PTRTENR_DATA_ORIGIN", length = 30)
    String dataOrigin



    public String toString() {
        """FacultyTenureStatus[
					id=$id, 
					version=$version, 
					code=$code, 
					description=$description, 
					dateIndicator=$dateIndicator, 
					reviewDateIndicator=$reviewDateIndicator, 
					eeoTenureIndicator=$eeoTenureIndicator, 
					lastModified=$lastModified, 
					lastModifiedBy=$lastModifiedBy, 
					dataOrigin=$dataOrigin]"""
    }


    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof FacultyTenureStatus)) return false
        FacultyTenureStatus that = (FacultyTenureStatus) o
        if (id != that.id) return false
        if (version != that.version) return false
        if (code != that.code) return false
        if (description != that.description) return false
        if (dateIndicator != that.dateIndicator) return false
        if (reviewDateIndicator != that.reviewDateIndicator) return false
        if (eeoTenureIndicator != that.eeoTenureIndicator) return false
        if (lastModified != that.lastModified) return false
        if (lastModifiedBy != that.lastModifiedBy) return false
        if (dataOrigin != that.dataOrigin) return false
        return true
    }


    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (code != null ? code.hashCode() : 0)
        result = 31 * result + (description != null ? description.hashCode() : 0)
        result = 31 * result + (dateIndicator != null ? dateIndicator.hashCode() : 0)
        result = 31 * result + (reviewDateIndicator != null ? reviewDateIndicator.hashCode() : 0)
        result = 31 * result + (eeoTenureIndicator != null ? eeoTenureIndicator.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        return result
    }


    static constraints = {
        code(nullable: false, maxSize: 2)
        description(nullable: false, maxSize: 30)
        dateIndicator(nullable: false)
        reviewDateIndicator(nullable: false)
        eeoTenureIndicator(nullable: false, maxSize: 1, inList: ["I", "T", "O", "N"])
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        /**
         * Please put all the custom constraints in this protected section to protect the code
         * from being overwritten on re-generation
         */
        /*PROTECTED REGION ID(facultytenurestatus_custom_constraints) ENABLED START*/

        /*PROTECTED REGION END*/
    }

    /*PROTECTED REGION ID(facultytenurestatus_readonly_properties) ENABLED START*/
    //Read Only fields that should be protected against update
    public static readonlyProperties = ['code']
    /*PROTECTED REGION END*/
    /**
     * Please put all the custom/transient attributes with @Transient annotations in this protected section to protect the code
     * from being overwritten on re-generation
     */
    /*PROTECTED REGION ID(facultytenurestatus_custom_attributes) ENABLED START*/

    /*PROTECTED REGION END*/

    /**
     * Please put all the custom methods/code in this protected section to protect the code
     * from being overwritten on re-generation
     */
    /*PROTECTED REGION ID(facultytenurestatus_custom_methods) ENABLED START*/

    /*PROTECTED REGION END*/
}
