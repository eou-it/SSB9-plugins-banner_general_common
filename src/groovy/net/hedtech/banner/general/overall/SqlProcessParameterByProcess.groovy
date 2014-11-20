/*******************************************************************************
Copyright 2014 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.general.overall

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinColumns
import javax.persistence.ManyToOne
import javax.persistence.NamedQueries
import javax.persistence.NamedQuery
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType
import javax.persistence.Version
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Type
import net.hedtech.banner.general.system.EntriesForSqlProcesss
import net.hedtech.banner.general.system.SqlProcessParameter

/**
 * SQL Process Parameter Table
 */
//TODO: NamedQueries that need to be ported:
 /**
    * Where clause on this entity present in forms:
  * Form Name: GORSQPA
  *  gorsqpa_sqpr_code = :KEY_BLOCK.PROCESS_CODE

  * Order by clause on this entity present in forms:
  * Form Name: GORSQPA
  *  gorsqpa_sqpa_code

*/
@Entity
@Table(name = "GORSQPA")
class SqlProcessParameterByProcess implements Serializable {

	/**
	 * Surrogate ID for GORSQPA
	 */
	@Id
	@Column(name="GORSQPA_SURROGATE_ID")
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
	 * SYSTEM REQUIRED INDICATOR: Indicates whether or not this parameter is system required.
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
	 * Foreign Key : FKV_GORSQPA_INV_GTVSQPR_CODE
	 */
	@ManyToOne
	@JoinColumns([
		@JoinColumn(name="GORSQPA_SQPR_CODE", referencedColumnName="GTVSQPR_CODE")
		])
	EntriesForSqlProcesss entriesForSqlProcesss

	/**
	 * Foreign Key : FKV_GORSQPA_INV_GTVSQPA_CODE
	 */
	@ManyToOne
	@JoinColumns([
		@JoinColumn(name="GORSQPA_SQPA_CODE", referencedColumnName="GTVSQPA_CODE")
		])
	SqlProcessParameter sqlProcessParameter


	public String toString() {
		"""SqlProcessParameterByProcess[
					id=$id,
					version=$version,
					systemRequiredIndicator=$systemRequiredIndicator,
					lastModified=$lastModified,
					lastModifiedBy=$lastModifiedBy,
					dataOrigin=$dataOrigin,
					entriesForSqlProcesss=$entriesForSqlProcesss,
					sqlProcessParameter=$sqlProcessParameter]"""
	}


	boolean equals(o) {
	    if (this.is(o)) return true
	    if (!(o instanceof SqlProcessParameterByProcess)) return false
	    SqlProcessParameterByProcess that = (SqlProcessParameterByProcess) o
        if(id != that.id) return false
        if(version != that.version) return false
        if(systemRequiredIndicator != that.systemRequiredIndicator) return false
        if(lastModified != that.lastModified) return false
        if(lastModifiedBy != that.lastModifiedBy) return false
        if(dataOrigin != that.dataOrigin) return false
        if(entriesForSqlProcesss != that.entriesForSqlProcesss) return false
        if(sqlProcessParameter != that.sqlProcessParameter) return false
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
        result = 31 * result + (entriesForSqlProcesss != null ? entriesForSqlProcesss.hashCode() : 0)
        result = 31 * result + (sqlProcessParameter != null ? sqlProcessParameter.hashCode() : 0)
        return result
	}

	static constraints = {
		systemRequiredIndicator(nullable:false)
		lastModified(nullable:true)
		lastModifiedBy(nullable:true, maxSize:30)
		dataOrigin(nullable:true, maxSize:30)
		entriesForSqlProcesss(nullable:false)
		sqlProcessParameter(nullable:false)
    }

    //Read Only fields that should be protected against update
    public static readonlyProperties = [ 'entriesForSqlProcesss', 'sqlProcessParameter' ]
}
