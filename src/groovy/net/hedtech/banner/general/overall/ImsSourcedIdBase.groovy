/*********************************************************************************
 Copyright 2009-2013 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.overall

import org.hibernate.annotations.Type
import javax.persistence.*
import net.hedtech.banner.service.DatabaseModifiesState

/**
 * IMS Sourced Id Base model.
 */

@Entity
@Table(name = "GOBSRID")
@DatabaseModifiesState
class ImsSourcedIdBase implements Serializable {

    /**
     * Surrogate ID for GOBSRID
     */
    @Id
    @Column(name = "GOBSRID_SURROGATE_ID")
    @SequenceGenerator(name = "GOBSRID_SEQ_GEN", allocationSize = 1, sequenceName = "GOBSRID_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GOBSRID_SEQ_GEN")
    Integer id

    /**
     * Optimistic lock token for GOBSRID
     */
    @Version
    @Column(name = "GOBSRID_VERSION")
    Integer version

    /**
     * Internal identification number of the person.
     */
    @Column(name = "GOBSRID_PIDM")
    Integer pidm

    /**
     * Permanent, unique identifier required for IMS data transfers which is now not required to be a number..
     */
    @Column(name = "GOBSRID_SOURCED_ID")
    @SequenceGenerator(name = "GOBISEQ", allocationSize = 1, sequenceName = "GOBISEQ")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GOBISEQ")
    String sourcedId

    /**
     * This field identifies the system user signon id creating this record.
     * **lastModifiedBy User is always set back to the original user by API
     */
    @Column(name = "GOBSRID_USER_ID")
    String lastModifiedBy

    /**
     * This field defines most current date record is created or changed.
     */
    @Column(name = "GOBSRID_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * DATA SOURCE: Source system that created or updated the row
     */
    @Column(name = "GOBSRID_DATA_ORIGIN")
    String dataOrigin


    public static readonlyProperties = ['pidm']


    public String toString() {
        """ImsSourcedIdBase[
		            id=$id,
		            version=$version,
					pidm=$pidm,
                    sourcedId=$sourcedId,
					lastModified=$lastModified,
					lastModifiedBy=$lastModifiedBy,
					dataOrigin=$dataOrigin]"""
    }


    static constraints = {
        pidm(nullable: false, maxsize: 22)
        sourcedId(nullable: false, maxsize: 16) 
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
    }


    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof ImsSourcedIdBase)) return false
        ImsSourcedIdBase that = (ImsSourcedIdBase) o
        if (id != that.id) return false
        if (version != that.version) return false
        if (pidm != that.pidm) return false
        if (sourcedId != that.sourcedId) return false
        if (lastModified != that.lastModified) return false
        if (lastModifiedBy != that.lastModifiedBy) return false
        if (dataOrigin != that.dataOrigin) return false
        return true
    }


    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (pidm != null ? pidm.hashCode() : 0)
        result = 31 * result + (sourcedId != null ? sourcedId.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        return result
    }


}
