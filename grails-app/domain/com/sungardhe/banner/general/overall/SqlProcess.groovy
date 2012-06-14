/** *****************************************************************************
 Copyright 2009-2012 SunGard Higher Education. All Rights Reserved.

 This copyrighted software contains confidential and proprietary information of
 SunGard Higher Education and its subsidiaries. Any use of this software is limited
 solely to SunGard Higher Education licensees, and is further subject to the terms
 and conditions of one or more written license agreements between SunGard Higher
 Education and the licensee in question. SunGard is either a registered trademark or
 trademark of SunGard Data Systems in the U.S.A. and/or other regions and/or countries.
 Banner and Luminis are either registered trademarks or trademarks of SunGard Higher
 Education in the U.S.A. and/or other regions and/or countries.
 ****************************************************************************** */
/**
 Banner Automator Version: 1.29
 Generated: Sun May 20 17:49:09 IST 2012
 */

package com.sungardhe.banner.general.overall

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
import org.hibernate.annotations.Type
import com.sungardhe.banner.general.system.EntriesForSqlProcesss
import com.sungardhe.banner.general.system.EntriesForSql

/**
 * SQL Process Rules Table
 */
/*PROTECTED REGION ID(sqlprocess_namedqueries) ENABLED START*/
//TODO: NamedQueries that needs to be ported:
/**
 * Where clause on this entity present in forms:
 * Form Name: GORRSQL
 *  where gorrsql_sqpr_code = :KEY_BLOCK.SQPR_CODE and
 gorrsql_sqru_code = :KEY_BLOCK.SQRU_CODE

 * Form Name: GOIRSQL
 *  where gorrsql_sqpr_code = :KEY_BLOCK.SQPR_CODE and
 gorrsql_sqru_code = :KEY_BLOCK.SQRU_CODE

 * Order by clause on this entity present in forms:
 * Form Name: GORRSQL
 *  gorrsql_seq_no

 * Form Name: GOIRSQL
 *  gorrsql_sqpr_code,gorrsql_sqru_code,gorrsql_seq_no

 */
/*PROTECTED REGION END*/
@Entity
@Table(name = "GORRSQL")
class SqlProcess implements Serializable {

    /**
     * Surrogate ID for GORRSQL
     */
    @Id
    @Column(name = "GORRSQL_SURROGATE_ID")
    @SequenceGenerator(name = "GORRSQL_SEQ_GEN", allocationSize = 1, sequenceName = "GORRSQL_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GORRSQL_SEQ_GEN")
    Long id

    /**
     * Optimistic lock token for GORRSQL
     */
    @Version
    @Column(name = "GORRSQL_VERSION", nullable = false, precision = 19)
    Long version

    /**
     * SQL RULE SEQUENCE NUMBER: Sequence for SEVIS SQL rule for uniqueness and process order.
     */
    @Column(name = "GORRSQL_SEQ_NO", nullable = false, unique = true, precision = 5)
    Integer sequenceNumber

    /**
     * ACTIVE INDICATOR: Indicator to show if the rule is currently active.
     */
    @Type(type = "yes_no")
    @Column(name = "GORRSQL_ACTIVE_IND", nullable = false, length = 1)
    Boolean activeIndicator

    /**
     * VALIDATED INDICATOR: Indicator to show if the SQL has been validated.
     */
    @Type(type = "yes_no")
    @Column(name = "GORRSQL_VALIDATED_IND", nullable = false, length = 1)
    Boolean validatedIndicator

    /**
     * START DATE: Date that the rule becomes effective.
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "GORRSQL_START_DATE", nullable = false)
    Date startDate

    /**
     * SELECT FROM VALUE: Tables and views that are in the FROM portion of the SQL statement.
     */
    @Column(name = "GORRSQL_SELECT_FROM", nullable = false, length = 4000)
    String selectFrom

    /**
     * SELECT VALUE: Value that is  in the SELECT portion of the SQL statement.
     */
    @Column(name = "GORRSQL_SELECT_VALUE", length = 4000)
    String selectValue

    /**
     * WHERE CLAUSE: WHERE clause of the SQL statement.
     */
    @Column(name = "GORRSQL_WHERE_CLAUSE", length = 4000)
    String whereClause

    /**
     * END DATE: Date that the rule becomes obsolete.
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "GORRSQL_END_DATE")
    Date endDate

    /**
     * PARSED SQL: Parsed SQL statement.
     */
    @Column(name = "GORRSQL_PARSED_SQL", length = 4000)
    String parsedSql

    /**
     * SYSTEM REQUIRED INDICATOR:Indicates Process Code/Rule Code combination is SCT delivered data. Valid values are (Y)es or (N)o.
     */
    @Type(type = "yes_no")
    @Column(name = "GORRSQL_SYS_REQ_IND", nullable = false, length = 1)
    Boolean systemRequiredIndicator

    /**
     * ACTIVITY DATE: The most recent date a record was created or updated
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "GORRSQL_ACTIVITY_DATE")
    Date lastModified

    /**
     * USER ID: The most recent user to create or update a record.
     */
    @Column(name = "GORRSQL_USER_ID", length = 30)
    String lastModifiedBy

    /**
     * Data origin column for GORRSQL
     */
    @Column(name = "GORRSQL_DATA_ORIGIN", length = 30)
    String dataOrigin

    /**
     * Foreign Key : FKV_GORRSQL_INV_GTVSQPR_CODE
     */
    @ManyToOne
    @JoinColumns([
        @JoinColumn(name = "GORRSQL_SQPR_CODE", referencedColumnName = "GTVSQPR_CODE")
    ])
    EntriesForSqlProcesss entriesForSqlProcesss

    /**
     * Foreign Key : FKV_GORRSQL_INV_GTVSQRU_CODE
     */
    @ManyToOne
    @JoinColumns([
        @JoinColumn(name = "GORRSQL_SQRU_CODE", referencedColumnName = "GTVSQRU_CODE")
    ])
    EntriesForSql entriesForSql


    public String toString() {
        """SqlProcess[
					id=$id,
					version=$version,
					sequenceNumber=$sequenceNumber,
					activeIndicator=$activeIndicator,
					validatedIndicator=$validatedIndicator,
					startDate=$startDate,
					selectFrom=$selectFrom,
					selectValue=$selectValue,
					whereClause=$whereClause,
					endDate=$endDate,
					parsedSql=$parsedSql,
					systemRequiredIndicator=$systemRequiredIndicator,
					lastModified=$lastModified,
					lastModifiedBy=$lastModifiedBy,
					dataOrigin=$dataOrigin,
					entriesForSqlProcesss=$entriesForSqlProcesss,
					entriesForSql=$entriesForSql]"""
    }


    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof SqlProcess)) return false
        SqlProcess that = (SqlProcess) o
        if (id != that.id) return false
        if (version != that.version) return false
        if (sequenceNumber != that.sequenceNumber) return false
        if (activeIndicator != that.activeIndicator) return false
        if (validatedIndicator != that.validatedIndicator) return false
        if (startDate != that.startDate) return false
        if (selectFrom != that.selectFrom) return false
        if (selectValue != that.selectValue) return false
        if (whereClause != that.whereClause) return false
        if (endDate != that.endDate) return false
        if (parsedSql != that.parsedSql) return false
        if (systemRequiredIndicator != that.systemRequiredIndicator) return false
        if (lastModified != that.lastModified) return false
        if (lastModifiedBy != that.lastModifiedBy) return false
        if (dataOrigin != that.dataOrigin) return false
        if (entriesForSqlProcesss != that.entriesForSqlProcesss) return false
        if (entriesForSql != that.entriesForSql) return false
        return true
    }


    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (sequenceNumber != null ? sequenceNumber.hashCode() : 0)
        result = 31 * result + (activeIndicator != null ? activeIndicator.hashCode() : 0)
        result = 31 * result + (validatedIndicator != null ? validatedIndicator.hashCode() : 0)
        result = 31 * result + (startDate != null ? startDate.hashCode() : 0)
        result = 31 * result + (selectFrom != null ? selectFrom.hashCode() : 0)
        result = 31 * result + (selectValue != null ? selectValue.hashCode() : 0)
        result = 31 * result + (whereClause != null ? whereClause.hashCode() : 0)
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0)
        result = 31 * result + (parsedSql != null ? parsedSql.hashCode() : 0)
        result = 31 * result + (systemRequiredIndicator != null ? systemRequiredIndicator.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        result = 31 * result + (entriesForSqlProcesss != null ? entriesForSqlProcesss.hashCode() : 0)
        result = 31 * result + (entriesForSql != null ? entriesForSql.hashCode() : 0)
        return result
    }


    static constraints = {
        sequenceNumber(nullable: false, min: -99999, max: 99999)
        activeIndicator(nullable: false)
        validatedIndicator(nullable: false)
        startDate(nullable: false)
        selectFrom(nullable: false, maxSize: 4000)
        selectValue(nullable: true, maxSize: 4000)
        whereClause(nullable: true, maxSize: 4000)
        endDate(nullable: true)
        parsedSql(nullable: true, maxSize: 4000)
        systemRequiredIndicator(nullable: false)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        entriesForSqlProcesss(nullable: false)
        entriesForSql(nullable: false)
        /**
         * Please put all the custom constraints in this protected section to protect the code
         * from being overwritten on re-generation
         */
        /*PROTECTED REGION ID(sqlprocess_custom_constraints) ENABLED START*/

        /*PROTECTED REGION END*/
    }

    /*PROTECTED REGION ID(sqlprocess_readonly_properties) ENABLED START*/
    //Read Only fields that should be protected against update
    public static readonlyProperties = ['sequenceNumber', 'entriesForSqlProcesss', 'entriesForSql']
    /*PROTECTED REGION END*/
    /**
     * Please put all the custom/transient attributes with @Transient annotations in this protected section to protect the code
     * from being overwritten on re-generation
     */
    /*PROTECTED REGION ID(sqlprocess_custom_attributes) ENABLED START*/

    /*PROTECTED REGION END*/

    /**
     * Please put all the custom methods/code in this protected section to protect the code
     * from being overwritten on re-generation
     */
    /*PROTECTED REGION ID(sqlprocess_custom_methods) ENABLED START*/
    /*PROTECTED REGION END*/
}
