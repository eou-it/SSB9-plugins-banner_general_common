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
 * Common Matching Display Options Table 
 */
@Entity
@Table(name = "GV_GORCMDO")
class CommonMatchingDisplayOption implements Serializable {

    /**
     * Surrogate ID for GORCMDO
     */
    @Id
    @Column(name = "GORCMDO_SURROGATE_ID")
    @SequenceGenerator(name = "GORCMDO_SEQ_GEN", allocationSize = 1, sequenceName = "GORCMDO_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GORCMDO_SEQ_GEN")
    Long id

    /**
     * Optimistic lock token for GORCMDO
     */
    @Version
    @Column(name = "GORCMDO_VERSION")
    Long version

    /**
     * OBJECT NAME: The SCT Banner Form to be displayed and called based on the Common Matching Source Code in the Key-Block of the Match Entry Process Form.
     */
    @Column(name = "GORCMDO_OBJS_NAME")
    String objectName

    /**
     * SEQUENCE NUMBER: The order in which to display the form names.
     */
    @Column(name = "GORCMDO_SEQ_NO")
    Integer sequenceNumber

    /**
     * ACTIVITY DATE: Date record was created or last updated.
     */
    @Column(name = "GORCMDO_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * USER ID: User ID of the User who created or last updated the record.
     */
    @Column(name = "GORCMDO_USER_ID")
    String lastModifiedBy

    /**
     * DATA ORIGIN: Source system that created or updated the row
     */
    @Column(name = "GORCMDO_DATA_ORIGIN")
    String dataOrigin

    /**
     * Foreign Key : FKV_GORCMDO_INV_GTVCMSC_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "GORCMDO_CMSC_CODE", referencedColumnName = "GTVCMSC_CODE")
    ])
    CommonMatchingSource commonMatchingSource


    public String toString() {
        """CommonMatchingDisplayOption[
					id=$id, 
					version=$version, 
					objectName=$objectName, 
					sequenceNumber=$sequenceNumber, 
					lastModified=$lastModified, 
					lastModifiedBy=$lastModifiedBy, 
					dataOrigin=$dataOrigin, 
					commonMatchingSource=$commonMatchingSource]"""
    }


    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof CommonMatchingDisplayOption)) return false
        CommonMatchingDisplayOption that = (CommonMatchingDisplayOption) o
        if (id != that.id) return false
        if (version != that.version) return false
        if (objectName != that.objectName) return false
        if (sequenceNumber != that.sequenceNumber) return false
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
        result = 31 * result + (objectName != null ? objectName.hashCode() : 0)
        result = 31 * result + (sequenceNumber != null ? sequenceNumber.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        result = 31 * result + (commonMatchingSource != null ? commonMatchingSource.hashCode() : 0)
        return result
    }


    static constraints = {
        objectName(nullable: false, maxSize: 30)
        sequenceNumber(nullable: false, min: -9, max: 9)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        commonMatchingSource(nullable: false)
    }

    //Read Only fields that should be protected against update
    public static readonlyProperties = ['objectName', 'commonMatchingSource']
}
