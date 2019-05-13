/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.service.DatabaseModifiesState
import org.hibernate.annotations.Type

import javax.persistence.*

/**
 * Flexible Work Week View entity.
 */
@Entity
@Table(name = "GV_GURFWWK")
@ToString(includeNames = true, ignoreNulls = true)
@EqualsAndHashCode(includeFields = true)
@NamedQueries(value = [
        @NamedQuery(name = "FlexibleWorkWeek.fetchByWorkWeekReference",
                query = """ FROM FlexibleWorkWeek a
                            WHERE a.workWeekReference = :workWeekReference """)
])
@DatabaseModifiesState
class FlexibleWorkWeek implements Serializable {

    /**
     * SURROGATE ID: Immutable unique key.
     */
    @Id
    @Column(name = "GURFWWK_SURROGATE_ID")
    @SequenceGenerator(name = "GURFWWK_SEQ_GEN", allocationSize = 1, sequenceName = "GURFWWK_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GURFWWK_SEQ_GEN")
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
     * Second weekend day: The second weekend day for reference workweek.
     */
    @Column(name = "GURFWWK_SECOND_WKND_DAY")
    Long secondWeekendDay

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


    static constraints = {
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        workWeekReference(nullable: false, max: 9999)
        description(nullable: false, maxSize: 50)
        firstWeekendDay(nullable: false, max: 9999999999,
                validator: { val, obj ->
                    if (![0, 2451911, 2451912, 2451913, 2451914, 2451915, 2451916, 2451917].contains(val.intValue())) {
                        return "invalid.firstWeekendDay"
                    }
                })
        secondWeekendDay(nullable: false, max: 9999999999,
                validator: { val, obj ->
                    if (![0, 2451911, 2451912, 2451913, 2451914, 2451915, 2451916, 2451917].contains(val.intValue())) {
                        return "invalid.secondWeekendDay"
                    }
                })
        isInstitutionalDefault(nullable: false)
        isSystemRequired(nullable: false)
        campusCode(nullable: true, maxSize: 3)
    }

    // Read Only fields that should be protected against update
    public static readonlyProperties = ['workWeekReference']


    def static fetchByWorkWeekReference(Integer workWeekReference) {
        def flexibleWorkWeek

        if (workWeekReference) {
            FlexibleWorkWeek.withSession { session ->
                flexibleWorkWeek = session.getNamedQuery('FlexibleWorkWeek.fetchByWorkWeekReference')
                        .setInteger('workWeekReference', workWeekReference).list()[0]
            }
        }

        return flexibleWorkWeek
    }

}
