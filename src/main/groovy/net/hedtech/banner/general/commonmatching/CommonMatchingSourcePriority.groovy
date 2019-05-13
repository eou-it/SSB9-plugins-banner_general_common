/*********************************************************************************
 Copyright 2010-2013 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/

/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.commonmatching

import net.hedtech.banner.general.system.CommonMatchingSource

import javax.persistence.*

/**
 * Common Matching Source Priority Table.
 */
@Entity
@Table(name = "GV_GORCMSP")
class CommonMatchingSourcePriority implements Serializable {

    /**
     * Surrogate ID for GORCMSP
     */
    @Id
    @Column(name = "GORCMSP_SURROGATE_ID")
    @SequenceGenerator(name = "GORCMSP_SEQ_GEN", allocationSize = 1, sequenceName = "GORCMSP_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GORCMSP_SEQ_GEN")
    Long id

    /**
     * Optimistic lock token for GORCMSP
     */
    @Version
    @Column(name = "GORCMSP_VERSION")
    Long version

    /**
     * PRIORITY NUMBER: The priority number of rule to be processed.
     */
    @Column(name = "GORCMSP_PRIORITY_NO")
    Integer priorityNumber

    /**
     * DESCRIPTION: Priority number description.
     */
    @Column(name = "GORCMSP_DESC")
    String description

    /**
     * LONG DESCRIPTION: Column to hold a long description.
     */
    @Column(name = "GORCMSP_LONG_DESC")
    String longDescription

    /**
     * ACTIVITY DATE: Date record was created or last updated.
     */
    @Column(name = "GORCMSP_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * USER ID: User ID of the User who created or last updated the record.
     */
    @Column(name = "GORCMSP_USER_ID")
    String lastModifiedBy

    /**
     * DATA ORIGIN: Source system that created or updated the row
     */
    @Column(name = "GORCMSP_DATA_ORIGIN")
    String dataOrigin

    /**
     * Foreign Key : FKV_GORCMSP_INV_GTVCMSC_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "GORCMSP_CMSC_CODE", referencedColumnName = "GTVCMSC_CODE")
    ])
    CommonMatchingSource commonMatchingSource


    public String toString() {
        """CommonMatchingSourcePriority[
					id=$id, 
					version=$version, 
					priorityNumber=$priorityNumber, 
					description=$description, 
					longDescription=$longDescription, 
					lastModified=$lastModified, 
					lastModifiedBy=$lastModifiedBy, 
					dataOrigin=$dataOrigin, 
					commonMatchingSource=$commonMatchingSource]"""
    }


    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof CommonMatchingSourcePriority)) return false
        CommonMatchingSourcePriority that = (CommonMatchingSourcePriority) o
        if (id != that.id) return false
        if (version != that.version) return false
        if (priorityNumber != that.priorityNumber) return false
        if (description != that.description) return false
        if (longDescription != that.longDescription) return false
        if (lastModified != that.lastModified) return false
        if (lastModifiedBy != that.lastModifiedBy) return false
        if (dataOrigin != that.dataOrigin) return false
        if (commonMatchingSource != that.commonMatchingSource) return false
        return true
    }


    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (priorityNumber != null ? priorityNumber.hashCode() : 0)
        result = 31 * result + (description != null ? description.hashCode() : 0)
        result = 31 * result + (longDescription != null ? longDescription.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        result = 31 * result + (commonMatchingSource != null ? commonMatchingSource.hashCode() : 0)
        return result
    }


    static constraints = {
        priorityNumber(nullable: false, min: -99, max: 99)
        description(nullable: false, maxSize: 60)
        longDescription(nullable: true, maxSize: 4000)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        commonMatchingSource(nullable: false)
    }

    //Read Only fields that should be protected against update
    public static readonlyProperties = ['priorityNumber', 'commonMatchingSource']
}
