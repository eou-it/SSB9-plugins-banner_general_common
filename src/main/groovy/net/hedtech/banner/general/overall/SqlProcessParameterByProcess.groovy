/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/

package net.hedtech.banner.general.overall

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType
import javax.persistence.Version
import org.hibernate.annotations.Type

/**
 * SQL Process Parameter Table
 */

@Entity
@Table(name = "GORSQPA")
class SqlProcessParameterByProcess implements Serializable {

    /**
     * Surrogate ID for GORSQPA
     */
    @Id
    @Column(name = "GORSQPA_SURROGATE_ID")
    @SequenceGenerator(name = "GORSQPA_SEQ_GEN", allocationSize = 1, sequenceName = "GORSQPA_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GORSQPA_SEQ_GEN")
    Long id

    /**
     * Optimistic lock token for GORSQPA
     */
    @Version
    @Column(name = "GORSQPA_VERSION", nullable = false, precision = 19)
    Long version

    /**
     * SYSTEM REQUIRED INDICATOR:Indicates Process Code/Rule Code combination is SCT delivered data. Valid values are (Y)es or (N)o.
     */
    @Type(type = "yes_no")
    @Column(name = "GORSQPA_SYS_REQ_IND", nullable = false, length = 1)
    Boolean systemRequiredIndicator

    /**
     * ACTIVITY DATE: The most recent date a record was created or updated
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "GORSQPA_ACTIVITY_DATE")
    Date lastModified

    /**
     * USER ID: The most recent user to create or update a record.
     */
    @Column(name = "GORSQPA_USER_ID", length = 30)
    String lastModifiedBy

    /**
     * Data origin column for GORSQPA
     */
    @Column(name = "GORSQPA_DATA_ORIGIN", length = 30)
    String dataOrigin

    /**
     * Foreign Key : SQL Process code
     */
    @Column(name = "GORSQPA_SQPR_CODE", length = 30)
    String entriesForSqlProcess

    /**
     * Parameter code
     */
    @Column(name = "GORSQPA_SQPA_CODE", length = 30)
    String parameterForSqlProcess


    public String toString() {
        """SqlProcessParameterByProcess[
					id=$id,
					version=$version,
					systemRequiredIndicator=$systemRequiredIndicator,
					lastModified=$lastModified,
					lastModifiedBy=$lastModifiedBy,
					dataOrigin=$dataOrigin,
					entriesForSqlProcess=$entriesForSqlProcess,
					entriesForSql=$parameterForSqlProcess]"""
    }


    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof SqlProcessParameterByProcess)) return false
        SqlProcessParameterByProcess that = (SqlProcessParameterByProcess) o
        if (id != that.id) return false
        if (version != that.version) return false
        if (systemRequiredIndicator != that.systemRequiredIndicator) return false
        if (lastModified != that.lastModified) return false
        if (lastModifiedBy != that.lastModifiedBy) return false
        if (dataOrigin != that.dataOrigin) return false
        if (entriesForSqlProcess != that.entriesForSqlProcess) return false
        if (parameterForSqlProcess != that.parameterForSqlProcess) return false
        return true
    }


    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (systemRequiredIndicator != null ? systemRequiredIndicator.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        result = 31 * result + (entriesForSqlProcess != null ? entriesForSqlProcess.hashCode() : 0)
        result = 31 * result + (parameterForSqlProcess != null ? parameterForSqlProcess.hashCode() : 0)
        return result
    }


    static constraints = {
        systemRequiredIndicator(nullable: false)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        entriesForSqlProcess(nullable: false)
        parameterForSqlProcess(nullable: false, maxSize: 30)
    }

    //Read Only fields that should be protected against update
    public static readonlyProperties = ['entriesForSqlProcess', 'parameterForSqlProcess']

}
