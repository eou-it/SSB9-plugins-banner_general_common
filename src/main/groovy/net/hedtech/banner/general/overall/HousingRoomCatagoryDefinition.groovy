/*********************************************************************************
 Copyright 2010-2018 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/

package net.hedtech.banner.general.overall

import net.hedtech.banner.general.system.Building

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinColumns
import javax.persistence.ManyToOne
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType
import javax.persistence.Version

/**
 * Room Catagory Definition Table
 */
@Entity
@Table(name = "SLRBCAT")
class HousingRoomCatagoryDefinition implements Serializable {

    /**
     * Surrogate ID for SLRBCAT
     */
    @Id
    @Column(name = "SLRBCAT_SURROGATE_ID")
    @SequenceGenerator(name = "SLRBCAT_SEQ_GEN", allocationSize = 1, sequenceName = "SLRBCAT_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SLRBCAT_SEQ_GEN")
    Long id

    /**
     * Optimistic lock token for SLRBCAT
     */
    @Version
    @Column(name = "SLRBCAT_VERSION", nullable = false, precision = 19)
    Long version

    /**
     * This field defines code used to define the category
     */
    @Column(name = "SLRBCAT_CODE", nullable = false, unique = true, length = 4)
    String code

    /**
     * This field identifies the description associated with the category code
     */
    @Column(name = "SLRBCAT_DESC", nullable = false, length = 30)
    String description

    /**
     * This field identifies the date the record was created or last updated
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "SLRBCAT_ACTIVITY_DATE")
    Date lastModified

    /**
     * Last modified by column for SLRBCAT
     */
    @Column(name = "SLRBCAT_USER_ID", length = 30)
    String lastModifiedBy

    /**
     * Data origin column for SLRBCAT
     */
    @Column(name = "SLRBCAT_DATA_ORIGIN", length = 30)
    String dataOrigin

    /**
     * Foreign Key : FKV_SLRBCAT_INV_STVBLDG_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SLRBCAT_BLDG_CODE", referencedColumnName = "STVBLDG_CODE")
    ])
    Building building


    public String toString() {
        """HousingRoomCatagoryDefinition[
					id=$id,
					version=$version,
					code=$code,
					description=$description,
					lastModified=$lastModified,
					lastModifiedBy=$lastModifiedBy,
					dataOrigin=$dataOrigin,
					building=$building]"""
    }


    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof HousingRoomCatagoryDefinition)) return false
        HousingRoomCatagoryDefinition that = (HousingRoomCatagoryDefinition) o
        if (id != that.id) return false
        if (version != that.version) return false
        if (code != that.code) return false
        if (description != that.description) return false
        if (lastModified != that.lastModified) return false
        if (lastModifiedBy != that.lastModifiedBy) return false
        if (dataOrigin != that.dataOrigin) return false
        if (building != that.building) return false
        return true
    }


    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (code != null ? code.hashCode() : 0)
        result = 31 * result + (description != null ? description.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        result = 31 * result + (building != null ? building.hashCode() : 0)
        return result
    }


    static constraints = {
        code(nullable: false, maxSize: 4)
        description(nullable: false, maxSize: 30)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        building(nullable: false)
    }

    public static readonlyProperties = ['code', 'building']


    public static Object fetchByCodeOrDescription(filter) {
        def categoryList
        if (filter) {
            categoryList = HousingRoomCatagoryDefinition.findAllByCodeIlikeOrDescriptionIlike("%" + filter + "%", "%" + filter + "%", [sort: "code", order: "asc"])
        } else {
            categoryList = HousingRoomCatagoryDefinition.findAll([sort: "code", order: "asc"])
        }
        return [list: categoryList]
    }

}
