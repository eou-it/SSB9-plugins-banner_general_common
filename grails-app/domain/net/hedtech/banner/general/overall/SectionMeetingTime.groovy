/*********************************************************************************
  Copyright 2010-2013 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
/**
 Banner Automator Version: 0.1.1
 Generated: Wed Feb 09 15:13:35 EST 2011
 */
package net.hedtech.banner.general.overall

import net.hedtech.banner.service.DatabaseModifiesState
import net.hedtech.banner.general.system.*
import org.hibernate.annotations.Type
import javax.persistence.*

/**
 * Section Meeting Times model.
 */
@Entity
@Table( name = "SV_SSRMEET" )
@NamedQueries( value = [
@NamedQuery( name="SectionMeetingTime.fetchCountOfSchedulesByDateTimeAndLocation",
            query="""select count(smt.id) from SectionMeetingTime smt
                      where ((smt.beginTime between :beginTime and :endTime or smt.endTime between :beginTime and :endTime) or (:beginTime between smt.beginTime and smt.endTime))
                      and ((smt.startDate between :beginDate and :endDate or smt.endDate between :beginDate and :endDate) or (:beginDate between smt.startDate and smt.endDate))
                      and smt.building.code = :buildingCode and smt.room = :roomNumber and smt.building.code is not null and smt.room is not null
                      and (smt.monday = :monday or smt.tuesday = :tuesday or smt.wednesday = :wednesday or smt.thursday = :thursday or smt.friday = :friday or smt.saturday = :saturday or smt.sunday = :sunday)"""),
@NamedQuery( name = "SectionMeetingTime.fetchByTermAndCourseReferenceNumber",
             query = """FROM SectionMeetingTime a
		                WHERE a.term = :term
		                AND a.courseReferenceNumber = :courseReferenceNumber
		                order by a.startDate, a.monday, a.tuesday, a.wednesday, a.thursday, a.friday, a.saturday, a.sunday, a.beginTime""" ),
@NamedQuery( name = "SectionMeetingTime.fetchByTermCRNAndCategory",
             query = """FROM SectionMeetingTime a
		                WHERE a.term = :term
		                AND a.courseReferenceNumber = :courseReferenceNumber
		                AND a.category = :category
		                order by a.startDate, a.monday, a.tuesday, a.wednesday, a.thursday, a.friday, a.saturday, a.sunday, a.beginTime""" ),
@NamedQuery(name = "SectionMeetingTime.fetchByTermAndCourseReferenceNumberStartAndEndDate",
query = """select MIN(a.startDate), MAX(a.endDate) FROM SectionMeetingTime a
		                WHERE a.term = :term
		                AND a.courseReferenceNumber = :courseReferenceNumber
		                order by a.startDate, a.monday, a.tuesday, a.wednesday, a.thursday, a.friday, a.saturday, a.sunday, a.beginTime""")
] )
@DatabaseModifiesState
class SectionMeetingTime implements Serializable {

    /**
     * Surrogate ID for SSRMEET
     */
    @Id
    @Column(name = "SSRMEET_SURROGATE_ID")
    @SequenceGenerator(name = "SSRMEET_SEQ_GEN", allocationSize = 1, sequenceName = "SSRMEET_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SSRMEET_SEQ_GEN")
    Long id

    /**
     * Optimistic lock token for SSRMEET
     */
    @Version
    @Column(name = "SSRMEET_VERSION", nullable = false, precision = 19)
    Long version

    /**
     * This field is not displayed on the form (page 0).  It defines the Course Reference Number for the course section for which you are creating meeting times
     */
    @Column(name = "SSRMEET_CRN", nullable = false, length = 5)
    String courseReferenceNumber

    /**
     * This field is not displayed on the form (page 0).  It defines the day number as defined on the STVDAYS Validation Form
     * Day number does not appear to be saved to the table.  A local form variable is used to associate a numeric value to
     * each of the day codes.  This numeric value of 1..7 is then used to make sure that the selected meeting days fall between
     * the start and end dates.
     */
    @Column(name = "SSRMEET_DAY_NUMBER", precision = 1)
    Integer dayNumber

    /**
     * This field defines the Begin Time of the course section being scheduled.  It is a required field and is in the format HHMM using military times.  The SSRSECT (Schedule of Classes) converts this time to standard times.
     */
    @Column(name = "SSRMEET_BEGIN_TIME", length = 4)
    String beginTime

    /**
     * This field defines the End Time of the course section being scheduled.  It is a required field and is in the format HHMM using military times.  The SSRSECT (Schedule of Classes) converts this time to standard times.
     */
    @Column(name = "SSRMEET_END_TIME", length = 4)
    String endTime

    /**
     * This field defines the Room where the course section will be scheduled.  It is not required when scheduling course section meeting times.  It is required when scheduling a course section meeting building.
     */
    @Column(name = "SSRMEET_ROOM_CODE", length = 10)
    String room

    /**
     * Section Meeting Start Date.
     */
    @Column(name = "SSRMEET_START_DATE", nullable = false)
    @Temporal(TemporalType.DATE)
    Date startDate

    /**
     * Section End Date.
     */
    @Column(name = "SSRMEET_END_DATE", nullable = false)
    @Temporal(TemporalType.DATE)
    Date endDate

    /**
     * Section Indicator. Note that the column name is misspelled!!!
     * Also note that this column is nullable in the table, but a required form item in the SSASECT form.
     * The existence of the category value will be enforced in the SectionMeetingTimeService.
     */
    @Column(name = "SSRMEET_CATAGORY", length = 2)
    String category

    /**
     * Section Meeting Time Sunday Indicator.
     */
    @Column(name = "SSRMEET_SUN_DAY", length = 1)
    String sunday

    /**
     * Section Meeting Time Monday Indicator.
     */
    @Column(name = "SSRMEET_MON_DAY", length = 1)
    String monday

    /**
     * Section Meeting Time Tuesday Indicator.
     */
    @Column(name = "SSRMEET_TUE_DAY", length = 1)
    String tuesday
    /**
     * Section Meeting Time Wednesday Indicator.
     */
    @Column(name = "SSRMEET_WED_DAY", length = 1)
    String wednesday

    /**
     * Section Meeting Time Thrusday Indicator.
     */
    @Column(name = "SSRMEET_THU_DAY", length = 1)
    String thursday

    /**
     * Section Meeting Time Friday Indicator.
     */
    @Column(name = "SSRMEET_FRI_DAY", length = 1)
    String friday

    /**
     * Section Meeting Time Saturday Indicator.
     */
    @Column(name = "SSRMEET_SAT_DAY", length = 1)
    String saturday

    /**
     * Section Time Conflict Override Indicator.
     */
    @Column(name = "SSRMEET_OVER_RIDE", length = 1)
    String override

    /**
     * The session credit hours
     */
    @Column(name = "SSRMEET_CREDIT_HR_SESS", precision = 7, scale = 3)
    Double creditHourSession

    /**
     * Total Section Meeting Number which is system generated.
     */
    @Column(name = "SSRMEET_MEET_NO", precision = 4)
    Integer meetNumber

    /**
     * Section Metting Hours per Week.
     */
    @Column(name = "SSRMEET_HRS_WEEK", precision = 5, scale = 2)
    Double hoursWeek

    /**
     * This field specifies the most current date record was created or updated.
     */
    @Column(name = "SSRMEET_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * USER ID: User who inserted or last update the data
     */
    @Column(name = "SSRMEET_USER_ID", length = 30)
    String lastModifiedBy

    /**
     * DATA SOURCE: Source system that created or updated the row
     */
    @Column(name = "SSRMEET_DATA_ORIGIN", length = 30)
    String dataOrigin

    /**
     * Foreign Key : FKV_SSRMEET_INV_STVTERM_CODE
     * This field is not displayed on the form (page 0).  It defines the term for which you are creating meeting
     * times for the course section.  It is based on the Key Block Term.
     * The term normally has a many to one with stvterm, but the term is filled with the word
     * EVENT if this entity is from the general Event module
     */
    /*
    @ManyToOne
    @JoinColumns([
        @JoinColumn(name="SSRMEET_TERM_CODE", referencedColumnName="STVTERM_CODE")
        ])
    Term term
    */
    @Column(name = "SSRMEET_TERM_CODE", nullable = false, length = 6)
    String term

    /**
     * Foreign Key : FKV_SSRMEET_INV_STVDAYS_CODE
     * This field defines the Day code for which the Key Block section will be scheduled.  It is a required field to
     * enter a meeting time record.
     * NOTE:  It seems like this field is no longer used by the form. It is nullable in the table.
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SSRMEET_DAYS_CODE", referencedColumnName = "STVDAYS_CODE")
    ])
    DayOfWeek dayOfWeek

    /**
     * Foreign Key : FK1_SSRMEET_INV_STVBLDG_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SSRMEET_BLDG_CODE", referencedColumnName = "STVBLDG_CODE")
    ])
    Building building

    /**
     * Foreign Key : FK1_SSRMEET_INV_STVSCHD_CODE
     * This is being made a string because stvschd is part of the student
     * and not common.  The api uses dynamic sql to validate this value.
     */
    /*@ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SSRMEET_SCHD_CODE", referencedColumnName = "STVSCHD_CODE")
    ])  */
    //ScheduleType scheduleType
    @Column(name = "SSRMEET_SCHD_CODE", nullable = true, length = 3)
    String scheduleType

    /**
     * Foreign Key : FKV_SSRMEET_INV_GTVFUNC_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SSRMEET_FUNC_CODE", referencedColumnName = "GTVFUNC_CODE")
    ])
    Function function

    /**
     * Foreign Key : FK1_SSRMEET_INV_STVCOMT_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SSRMEET_COMT_CODE", referencedColumnName = "STVCOMT_CODE")
    ])
    CommitteeAndServiceType committee

    /**
     * Foreign Key : FKV_SSRMEET_INV_GTVSCHS_CODE
     * Schedule Status Code for use with Scheduling Tool Interface .  GTVSCHS
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SSRMEET_SCHS_CODE", referencedColumnName = "GTVSCHS_CODE")
    ])
    ScheduleToolStatus scheduleToolStatus

    /**
     * Foreign Key : FK1_SSRMEET_INV_GTVMTYP_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SSRMEET_MTYP_CODE", referencedColumnName = "GTVMTYP_CODE")
    ])
    MeetingType meetingType


    public String toString() {
        """SectionMeetingTime[
                    id=$id,
                    version=$version,
                    courseReferenceNumber=$courseReferenceNumber,
                    dayNumber=$dayNumber,
                    beginTime=$beginTime,
                    endTime=$endTime,
                    room=$room,
                    startDate=$startDate,
                    endDate=$endDate,
                    category=$category,
                    sunday=$sunday,
                    monday=$monday,
                    tuesday=$tuesday,
                    wednesday=$wednesday,
                    thursday=$thursday,
                    friday=$friday,
                    saturday=$saturday,
                    override=$override,
                    creditHourSession=$creditHourSession,
                    meetNumber=$meetNumber,
                    hoursWeek=$hoursWeek,
                    lastModified=$lastModified,
                    lastModifiedBy=$lastModifiedBy,
                    dataOrigin=$dataOrigin,
                    term=$term,
                    dayOfWeek=$dayOfWeek,
                    building=$building,
                    scheduleType=$scheduleType,
                    function=$function,
                    committee=$committee,
                    scheduleToolStatus=$scheduleToolStatus,
                    meetingType=$meetingType]"""
    }


    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof SectionMeetingTime)) return false
        SectionMeetingTime that = (SectionMeetingTime) o
        if (id != that.id) return false
        if (version != that.version) return false
        if (courseReferenceNumber != that.courseReferenceNumber) return false
        if (dayNumber != that.dayNumber) return false
        if (beginTime != that.beginTime) return false
        if (endTime != that.endTime) return false
        if (room != that.room) return false
        if (startDate != that.startDate) return false
        if (endDate != that.endDate) return false
        if (category != that.category) return false
        if (sunday != that.sunday) return false
        if (monday != that.monday) return false
        if (tuesday != that.tuesday) return false
        if (wednesday != that.wednesday) return false
        if (thursday != that.thursday) return false
        if (friday != that.friday) return false
        if (saturday != that.saturday) return false
        if (override != that.override) return false
        if (creditHourSession != that.creditHourSession) return false
        if (meetNumber != that.meetNumber) return false
        if (hoursWeek != that.hoursWeek) return false
        if (lastModified != that.lastModified) return false
        if (lastModifiedBy != that.lastModifiedBy) return false
        if (dataOrigin != that.dataOrigin) return false
        if (term != that.term) return false
        if (dayOfWeek != that.dayOfWeek) return false
        if (building != that.building) return false
        if (scheduleType != that.scheduleType) return false
        if (function != that.function) return false
        if (committee != that.committee) return false
        if (scheduleToolStatus != that.scheduleToolStatus) return false
        if (meetingType != that.meetingType) return false
        return true
    }


    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (courseReferenceNumber != null ? courseReferenceNumber.hashCode() : 0)
        result = 31 * result + (dayNumber != null ? dayNumber.hashCode() : 0)
        result = 31 * result + (beginTime != null ? beginTime.hashCode() : 0)
        result = 31 * result + (endTime != null ? endTime.hashCode() : 0)
        result = 31 * result + (room != null ? room.hashCode() : 0)
        result = 31 * result + (startDate != null ? startDate.hashCode() : 0)
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0)
        result = 31 * result + (category != null ? category.hashCode() : 0)
        result = 31 * result + (sunday != null ? sunday.hashCode() : 0)
        result = 31 * result + (monday != null ? monday.hashCode() : 0)
        result = 31 * result + (tuesday != null ? tuesday.hashCode() : 0)
        result = 31 * result + (wednesday != null ? wednesday.hashCode() : 0)
        result = 31 * result + (thursday != null ? thursday.hashCode() : 0)
        result = 31 * result + (friday != null ? friday.hashCode() : 0)
        result = 31 * result + (saturday != null ? saturday.hashCode() : 0)
        result = 31 * result + (override != null ? override.hashCode() : 0)
        result = 31 * result + (creditHourSession != null ? creditHourSession.hashCode() : 0)
        result = 31 * result + (meetNumber != null ? meetNumber.hashCode() : 0)
        result = 31 * result + (hoursWeek != null ? hoursWeek.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        result = 31 * result + (term != null ? term.hashCode() : 0)
        result = 31 * result + (dayOfWeek != null ? dayOfWeek.hashCode() : 0)
        result = 31 * result + (building != null ? building.hashCode() : 0)
        result = 31 * result + (scheduleType != null ? scheduleType.hashCode() : 0)
        result = 31 * result + (function != null ? function.hashCode() : 0)
        result = 31 * result + (committee != null ? committee.hashCode() : 0)
        result = 31 * result + (scheduleToolStatus != null ? scheduleToolStatus.hashCode() : 0)
        result = 31 * result + (meetingType != null ? meetingType.hashCode() : 0)
        return result
    }


    static constraints = {
        /*PROTECTED REGION ID(sectionmeetingtime_custom_constraints) ENABLED START*/
        term(nullable: false, maxSize: 6)
        courseReferenceNumber(nullable: false, maxSize: 5)
        dayOfWeek(nullable: true)
        dayNumber(nullable: true, min: 0, max: 9)
        beginTime(nullable: true, minSize: 4, maxSize: 4,
                validator: {val, obj ->
                    if ((val != null) && ((val < "0000") || (val > "2359") || (val.toString().substring(2, 3) > "59")))
                        return "invalid.begin_time"
                    if ((val != null) && (obj.endTime == null))
                        return "invalid.begin_end_time"
                    if ((val != null) && (obj.endTime != null) && (val >= obj.endTime))
                        return "invalid.begin_time_greater_than_end_time"
                })
        endTime(nullable: true, minSize: 4, maxSize: 4,
                validator: {val, obj ->
                    if ((val != null) && ((val < "0000") || (val > "2359") || (val.toString().substring(2, 3) > "59")))
                        return "invalid.end_time"
                    if ((val != null) && (obj.beginTime == null))
                        return "invalid.begin_end_time"
                })
        building(nullable: true)
        room(nullable: true, maxSize: 10)
        startDate(nullable: false,
                validator: {val, obj ->
                    if ((val != null && obj.endDate != null) && (val > obj.endDate))
                        return "invalid.start_greater_than_end_date"
                })
        endDate(nullable: false,
                validator: {val, obj ->
                    if ((val != null && obj.startDate != null) && (val < obj.startDate))
                        return "invalid.end_less_than_start_date"
                })
        category(nullable: true, maxSize: 2)
        sunday(nullable: true, maxSize: 1, inList: ["U"])
        monday(nullable: true, maxSize: 1, inList: ["M"])
        tuesday(nullable: true, maxSize: 1, inList: ["T"])
        wednesday(nullable: true, maxSize: 1, inList: ["W"])
        thursday(nullable: true, maxSize: 1, inList: ["R"])
        friday(nullable: true, maxSize: 1, inList: ["F"])
        saturday(nullable: true, maxSize: 1, inList: ["S"])
        scheduleType(nullable: true, maxSize: 3)
        override(nullable: true, maxSize: 1, inList: ["T", "O", "R"])
        creditHourSession(nullable: true, scale: 3, min: 0.000D, max: 9999.999D)
        meetNumber(nullable: true, min: 0, max: 9999)
        hoursWeek(nullable: true, scale: 2, min: 0.00D, max: 999.99D)
        function(nullable: true)
        committee(nullable: true)
        scheduleToolStatus(nullable: true,
                validator: {val, obj ->
                    if (val != null
                            &&
                            ((val.code == 'ASM') || (val.code == 'AXM') || (val.code == 'HSM') ||
                                    (val.code == 'VSM') || (val.code == '5SM') || (val.code == '5XM'))
                            &&
                            ((obj.building == null) || (obj.room == null)))
                        return "invalid.schedule_requires_building_room"
                })
        meetingType(nullable: true)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        /*PROTECTED REGION END*/
    }

    /**
     * Please put all the custom/transient attributes with @Transient annotations in this protected section to protect the code
     * from being overwritten on re-generation
     */
    /*PROTECTED REGION ID(sectionmeetingtime_custom_attributes) ENABLED START*/

    /*PROTECTED REGION END*/

    /*PROTECTED REGION ID(sectionmeetingtime_readonly_properties) ENABLED START*/
    //Read Only fields that should be protected against update
    public static readonlyProperties = ["term", "courseReferenceNumber"]
    /*PROTECTED REGION END*/

    /*PROTECTED REGION ID(sectionmeetingtime_custom_methods) ENABLED START*/

    /**
     * This fetchBy is used to retrieve all meeting times for a given term and crn.
     * A NamedQuery is required because there are multiple fields in the order by.
     */

    public static List fetchByTermAndCourseReferenceNumber(String term,
                                                           String courseReferenceNumber) {
        def sectionMeetingTimes
        SectionMeetingTime.withSession {session ->
            sectionMeetingTimes = session.getNamedQuery(
                    'SectionMeetingTime.fetchByTermAndCourseReferenceNumber').setString('term', term).setString('courseReferenceNumber', courseReferenceNumber).list()
        }
        return sectionMeetingTimes
    }

    /**
     * This fetchBy is used to retrieve all meeting times for a given term and crn.
     * A NamedQuery is required because there are multiple fields in the order by.
     */

    public static List fetchByTermCRNAndCategory(String term,
                                                           String courseReferenceNumber, String category) {
        def sectionMeetingTimes
        SectionMeetingTime.withSession {session ->
            sectionMeetingTimes = session.getNamedQuery(
                    'SectionMeetingTime.fetchByTermCRNAndCategory').setString('term', term).setString('courseReferenceNumber', courseReferenceNumber).setString('category', category).list()
        }
        return sectionMeetingTimes
    }


    /**
     * This fetchBy is used to retrieve Start Date and End Date for a given term and crn.
     *
     */
    public static SectionMeetingTime fetchByTermAndCourseReferenceNumberStartAndEndDate(String term,
                                                                                        String courseReferenceNumber) {
        def sectionMeetingDates
        SectionMeetingTime.withSession { session ->
            sectionMeetingDates = session.getNamedQuery(
                    'SectionMeetingTime.fetchByTermAndCourseReferenceNumberStartAndEndDate').setString('term', term).setString('courseReferenceNumber', courseReferenceNumber).list()[0]
        }

        return new SectionMeetingTime(startDate: sectionMeetingDates[0], endDate: sectionMeetingDates[1])
    }

    public static int fetchCountOfSchedulesByDateTimeAndLocation(String beginTime, String endTime, Date beginDate, Date endDate,
                                                                 String buildingCode, String roomNumber, String monday, String tuesday,
                                                                 String wednesday, String thursday, String friday, String saturday, String sunday)   {
        //if any of days are null put # for the search criteria. This is required as namedquery will look for the parameter and if not sent will result in error.
        if (!monday){ monday="#" }
        if (!tuesday){ tuesday="#" }
        if (!wednesday){ wednesday="#" }
        if (!thursday){ thursday="#" }
        if (!friday){ friday="#" }
        if (!saturday){ saturday="#" }
        if (!sunday){ sunday="#" }
        int count = 0
        SectionMeetingTime.withSession {session ->
            count = session.getNamedQuery(
                    'SectionMeetingTime.fetchCountOfSchedulesByDateTimeAndLocation').setDate('beginDate', beginDate).setDate('endDate', endDate).
                    setString('roomNumber', roomNumber).setString('buildingCode', buildingCode).
                    setString('beginTime', beginTime).setString('endTime', endTime).
                    setString('monday', monday).setString('tuesday', tuesday).
                    setString('wednesday', wednesday).setString('thursday', thursday).
                    setString('friday', friday).setString('saturday', saturday).setString('sunday', sunday).list()[0]
        }
        return count
    }
    /*PROTECTED REGION END*/
}
