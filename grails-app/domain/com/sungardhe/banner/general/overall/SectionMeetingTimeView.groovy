/** *****************************************************************************
 © 2011 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.general.overall

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.NamedQueries
import javax.persistence.NamedQuery
import javax.persistence.Table
import javax.persistence.Version
import org.hibernate.annotations.Formula
import org.hibernate.annotations.Type

/**
 * Section Meeting Time View model.
 */
@Entity
@Table(name = "SVQ_SSVMEET")
@NamedQueries(value = [
@NamedQuery(name = "SectionMeetingTimeView.fetchByTermAndCourseReferenceNumber",
query = """FROM SectionMeetingTimeView a
		                WHERE a.term = :term
		                AND a.courseReferenceNumber = :courseReferenceNumber
		                order by term, building,room, partOfTerm,startDate, beginTime
		                """)
])
class SectionMeetingTimeView {

    /**
     * Surrogate ID for SSRMEET
     */
    @Id
    @Column(name = "SSRMEET_SURROGATE_ID")
    Long id

    /**
     * Optimistic lock token for SSRMEET
     */
    @Version
    @Column(name = "SSRMEET_VERSION")
    Long version

    /**
     * This field is not displayed on the form (page 0).  It defines the Course Reference Number for the course section for which you are creating meeting times
     */
    @Column(name = "SSRMEET_CRN")
    String courseReferenceNumber

    /**
     * Foreign Key : FKV_SSRMEET_INV_STVTERM_CODE
     * This field is not displayed on the form (page 0).  It defines the term for which you are creating meeting
     * times for the course section.  It is based on the Key Block Term.
     * The term normally has a many to one with stvterm, but the term is filled with the word
     * EVENT if this entity is from the general Event module
     */


    @Column(name = "SSRMEET_TERM_CODE")
    String term

/**
 * This field is not displayed on the form (page 0).  It defines the day number as defined on the STVDAYS Validation Form
 * Day number does not appear to be saved to the table.  A local form variable is used to associate a numeric value to
 * each of the day codes.  This numeric value of 1..7 is then used to make sure that the selected meeting days fall between
 * the start and end dates.
 */
    @Column(name = "SSRMEET_DAY_NUMBER")
    Integer dayNumber

/**
 * This field defines the Begin Time of the course section being scheduled.  It is a required field and is in the format HHMM using military times.  The SSRSECT (Schedule of Classes) converts this time to standard times.
 */
    @Column(name = "SSrMEET_BEGIN_TIME")
    String beginTime

/**
 * This field defines the End Time of the course section being scheduled.  It is a required field and is in the format HHMM using military times.  The SSRSECT (Schedule of Classes) converts this time to standard times.
 */
    @Column(name = "SSRMEET_END_TIME")
    String endTime

/**
 * This field defines the Room where the course section will be scheduled.  It is not required when scheduling course section meeting times.  It is required when scheduling a course section meeting building.
 */
    @Column(name = "SSRMEET_ROOM_CODE")
    String room

/**
 * Section Meeting Start Date.
 */
    @Column(name = "SSRMEET_START_DATE")
    Date startDate

/**
 * Section End Date.
 */
    @Column(name = "SSRMEET_END_DATE")
    Date endDate

    /**
     * Foreign Key : FK1_SSRMEET_INV_STVBLDG_CODE
     */

    @Column(name = "SSRMEET_BLDG_CODE")
    String building

/**
 *  Foreign Key : FKV_SSRMEET_INV_GTVFUNC_CODEection End Date.
 */

    @Column(name = "SSRMEET_FUNC_CODE")
    String function
/**
 * Section Meeting Time Sunday Indicator.
 */
    @Formula(value = "decode(nvl(SSRMEET_SUN_DAY,'N'),'N','N','Y')")
    @Type(type = "yes_no")
    Boolean sunday

/**
 * Section Meeting Time Monday Indicator.
 */
    @Formula(value = "decode(nvl(SSRMEET_MON_DAY,'N'),'N','N','Y')")
    @Type(type = "yes_no")
    Boolean monday

/**
 * Section Meeting Time Tuesday Indicator.
 */

    @Formula(value = "decode(nvl(SSRMEET_TUE_DAY,'N'),'N','N','Y')")
    @Type(type = "yes_no")
    Boolean tuesday
/**
 * Section Meeting Time Wednesday Indicator.
 */

    @Formula(value = "decode(nvl(SSRMEET_WED_DAY,'N'),'N','N','Y')")
    @Type(type = "yes_no")
    Boolean wednesday

/**
 * Section Meeting Time Thrusday Indicator.
 */

    @Formula(value = "decode(nvl(SSRMEET_THU_DAY,'N'),'N','N','Y')")
    @Type(type = "yes_no")
    Boolean thursday

/**
 * Section Meeting Time Friday Indicator.
 */

    @Formula(value = "decode(nvl(SSRMEET_FRI_DAY,'N'),'N','N','Y')")
    @Type(type = "yes_no")
    Boolean friday

/**
 * Section Meeting Time Saturday Indicator.
 */

    @Formula(value = "decode(nvl(SSRMEET_SAT_DAY,'N'),'N','N','Y')")
    @Type(type = "yes_no")
    Boolean saturday

/**
 * Section Time Conflict Override Indicator.
 */
    @Column(name = "SSRMEET_OVER_RIDE")
    String override


    @Column(name = "SSBSECT_SUBJ_CODE")
    String subject

    @Column(name = "SSBSECT_CRSE_NUMB")
    String courseNumber

    @Column(name = "SSBSECT_PTRM_CODE")
    String partOfTerm

    @Column(name = "SLBBLDG_CAMP_CODE")
    String campus

    @Column(name = "SSRXLST_XLST_GROUP")
    String xlstGroup


    public String toString() {
        """SectionMeetingTimeView[
                   id=$id,
                   version=$version,
                   building=$building,
                   room=$room,
                   campus=$campus,
                   monday=$monday,
                   tuesday=$tuesday,
                   wednesday=$wednesday,
                   thursday=$thursday,
                   friday=$friday,
                   saturday=$saturday,
                   sunday=$sunday,
                   beginTime=$beginTime,
                   endTime=$endTime,
                   startDate=$startDate,
                   endDate=$endDate,
                   term=$term,
                   subject=$subject,
                   courseNumber=$courseNumber,
                   courseReferenceNumber=$courseReferenceNumber,
                   xlstGroup=$xlstGroup,
                   function=$function,
                   dayNumber=$dayNumber,
                   override=$override,
                   partOfTerm=$partOfTerm
                   ]"""
    }
    /**
     * This fetchBy is used to retrieve all meeting times for a given term and crn.
     * A NamedQuery is required because there are multiple fields in the order by.
     */

    public static List fetchByTermAndCourseReferenceNumber(String term,
                                                           String courseReferenceNumber) {
        def sectionMeetingTimes = []
        SectionMeetingTimeView.withSession {session ->
            sectionMeetingTimes = session.getNamedQuery(
                    'SectionMeetingTimeView.fetchByTermAndCourseReferenceNumber').setString('term', term).setString('courseReferenceNumber', courseReferenceNumber).list()
        }
        return sectionMeetingTimes
    }

    /**
     * Query finder for advanced search SSAMATX
     */

//    def static countAll(filterData) {
    //        finderByAll().count(filterData)
    //    }
    //
    //
    //    def static fetchSearch(filterData, pagingAndSortParams) {
    //        finderByAll().find(filterData, pagingAndSortParams)
    //    }
    //
    //
    //    def private static finderByAll = {
    //        def query = """FROM  SectionMeetingTimeView a   """
    //        return new com.sungardhe.query.DynamicFinder(SectionMeetingTimeView.class, query, "a")
    //    }
}
