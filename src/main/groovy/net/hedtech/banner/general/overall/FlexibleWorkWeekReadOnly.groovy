/*******************************************************************************
 Copyright 2020 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.hibernate.annotations.Type

import javax.persistence.*

/**
 * Flexible Work Week Read-only entity.
 */
@Entity
@Table(name = "GVQ_GURFWWK")
@ToString(includeNames = true, ignoreNulls = true)
@EqualsAndHashCode(includeFields = true)
@NamedQueries(value = [
        @NamedQuery(name = "FlexibleWorkWeekReadOnly.fetchByWorkWeekReference",
                query = """ FROM FlexibleWorkWeekReadOnly a
                            WHERE a.workWeekReference = :workWeekReference """),
        @NamedQuery(name = "FlexibleWorkWeekReadOnly.fetchByInstitutionDefault",
                query = """ FROM FlexibleWorkWeekReadOnly a
                            WHERE a.isInstitutionalDefault = true """),
        @NamedQuery(name = "FlexibleWorkWeekReadOnly.fetchBySystemRequired",
                query = """ FROM FlexibleWorkWeekReadOnly a
                            WHERE a.isSystemRequired = true """),
        @NamedQuery(name = 'FlexibleWorkWeekReadOnly.fetchByInstitutionDefaultAndSystemRequired',
                query = """ FROM FlexibleWorkWeekReadOnly a
                           WHERE a.isSystemRequired = true
                           AND a.isInstitutionalDefault = true """),
])
class FlexibleWorkWeekReadOnly implements Serializable {

    /**
     * SURROGATE ID: Immutable unique key.
     */
    @Id
    @Column(name = "GURFWWK_SURROGATE_ID")
    Long id

    /**
     * VERSION: Optimistic lock token.
     */
    @Version
    @Column(name = "GURFWWK_VERSION")
    Long version

    /**
     * ACTIVITY DATE: Date on which the record was created or last updated.
     */
    @Column(name = "GURFWWK_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * USER ID: User ID of the user who created or last updated the record.
     */
    @Column(name = "GURFWWK_USER_ID")
    String lastModifiedBy

    /**
     * DATA ORIGIN: Source system that created or updated the row.
     */
    @Column(name = "GURFWWK_DATA_ORIGIN")
    String dataOrigin

    /**
     * Workweek reference: Code for the reference of the workweek.
     */
    @Column(name = "GURFWWK_WEEK_REF")
    Integer workWeekReference

    /**
     * Workweek reference description: Description of the reference workweek.
     */
    @Column(name = "GURFWWK_DESC")
    String description

    /**
     * First weekend day: The first weekend day for reference workweek.
     */
    @Column(name = "GURFWWK_FIRST_WKND_DAY")
    Long firstWeekendDay

    /**
     * First weekend day ISO: The first weekend day for reference workweek represented as the ISO value 1-7 (Mon-Sun).
     */
    @Column(name = "GURFWWK_FIRST_WKND_DAY_ISO")
    Integer firstWeekendDayIso

    /**
     * Second weekend day: The second weekend day for reference workweek.
     */
    @Column(name = "GURFWWK_SECOND_WKND_DAY")
    Long secondWeekendDay

    /**
     * Second weekend day: The second weekend day for reference workweek represented as the ISO value 1-7 (Mon-Sun).
     */
    @Column(name = "GURFWWK_SECOND_WKND_DAY_ISO")
    Integer secondWeekendDayIso

    /**
     * Institution Default: Institution default indicator.
     */
    @Type(type = "yes_no")
    @Column(name = "GURFWWK_INST_IND")
    Boolean isInstitutionalDefault

    /**
     * System Required Indicator: This field denotes whether the Workweek Reference code is system required.  Valid values are (Y)es and (N)o.
     */
    @Type(type = "yes_no")
    @Column(name = "GURFWWK_SYS_REQ_IND")
    Boolean isSystemRequired

    /**
     * Campus Validation code for Flexible Work Week
     */
    @Column(name = "GURFWWK_CAMP_CODE")
    String campusCode


    def static fetchByWorkWeekReference(Integer workWeekReference) {
        def flexibleWorkWeek

        if (workWeekReference) {
            FlexibleWorkWeekReadOnly.withSession { session ->
                flexibleWorkWeek = session.getNamedQuery('FlexibleWorkWeekReadOnly.fetchByWorkWeekReference')
                        .setInteger('workWeekReference', workWeekReference).list()[0]
            }
        }

        return flexibleWorkWeek
    }


    def static fetchByInstitutionDefault() {
        def flexibleWorkWeek

        FlexibleWorkWeekReadOnly.withSession { session ->
            flexibleWorkWeek = session.getNamedQuery('FlexibleWorkWeekReadOnly.fetchByInstitutionDefault')
                    .list()[0]
        }

        return flexibleWorkWeek
    }


    def static fetchBySystemRequired() {
        def flexibleWorkWeek

        FlexibleWorkWeekReadOnly.withSession { session ->
            flexibleWorkWeek = session.getNamedQuery('FlexibleWorkWeekReadOnly.fetchBySystemRequired')
                    .list()[0]
        }

        return flexibleWorkWeek
    }

    def static fetchByInstitutionDefaultAndSystemRequired() {
        def flexibleWorkWeek

        FlexibleWorkWeekReadOnly.withSession { session ->
            flexibleWorkWeek = session.getNamedQuery('FlexibleWorkWeekReadOnly.fetchByInstitutionDefaultAndSystemRequired')
                    .list()[0]
        }

        return flexibleWorkWeek
    }


}
