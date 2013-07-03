/** *******************************************************************************
 Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of 
 SunGard Higher Education and its subsidiaries. Any use of this software is limited 
 solely to SunGard Higher Education licensees, and is further subject to the terms 
 and conditions of one or more written license agreements between SunGard Higher 
 Education and the licensee in question. SunGard is either a registered trademark or
 trademark of SunGard Data Systems in the U.S.A. and/or other regions and/or countries.
 Banner and Luminis are either registered trademarks or trademarks of SunGard Higher 
 Education in the U.S.A. and/or other regions and/or countries.
 ********************************************************************************* */
package net.hedtech.banner.general.overall

import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.hibernate.annotations.Type
import javax.persistence.*

 /**
 * Meeting Time  model.
 */
@Entity
@Table(name = "GVQ_SSRMEET")

class MeetingTimeSearch {

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
     * Section Indicator. Note that the column name is misspelled in the ssrmeet table and is catagory
     * the view corrects to category!!!
     * Also note that this column is nullable in the table, but a required form item in the SSASECT form.
     * The existence of the category value will be enforced in the SectionMeetingTimeService.
     */
    @Column(name = "SSRMEET_CATEGORY")
    String category

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

    @Column(name= "ssrmeet_bldg_desc")
    String buildingDescription

/**
 *  Foreign Key : FKV_SSRMEET_INV_GTVFUNC_CODEection End Date.
 */

    @Column(name = "SSRMEET_FUNC_CODE")
    String function
/**
 * Section Meeting Time Sunday Indicator.
 */
    @Column(name = "SSRMEET_SUN_DAY")
    @Type(type = "yes_no")
    Boolean sunday

/**
 * Section Meeting Time Monday Indicator.
 */
    @Column(name = "SSRMEET_MON_DAY")
    @Type(type = "yes_no")
    Boolean monday

/**
 * Section Meeting Time Tuesday Indicator.
 */
    @Column(name = "SSRMEET_TUE_DAY")
    @Type(type = "yes_no")
    Boolean tuesday
/**
 * Section Meeting Time Wednesday Indicator.
 */
    @Column(name = "SSRMEET_WED_DAY")
    @Type(type = "yes_no")
    Boolean wednesday

/**
 * Section Meeting Time Thrusday Indicator.
 */
    @Column(name = "SSRMEET_THU_DAY")
    @Type(type = "yes_no")
    Boolean thursday

/**
 * Section Meeting Time Friday Indicator.
 */
    @Column(name = "SSRMEET_FRI_DAY")
    @Type(type = "yes_no")
    Boolean friday

/**
 * Section Meeting Time Saturday Indicator.
 */
    @Column(name = "SSRMEET_SAT_DAY")
    @Type(type = "yes_no")
    Boolean saturday

/**
 * Section Time Conflict Override Indicator.
 */
    @Column(name = "SSRMEET_OVER_RIDE")
    String override


    @Column(name = "SLBBLDG_CAMP_CODE")
    String campus

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
     * Section Meeting Hours per Week.
     */
    @Column(name = "SSRMEET_HRS_WEEK", precision = 5, scale = 2)
    Double hoursWeek

    @Column(name = "SSRMEET_DAYS_CODE")
    String dayOfWeek

    @Column(name = "SSRMEET_SCHD_CODE")
    String scheduleType

    @Column(name="ssrmeet_term_crn")
    String termCourseReferenceNumber


    public String toString() {
        """MeetingTimeSearch[
                   id=$id,
                   version=$version,
                   term=$term,
                   courseReferenceNumber=$courseReferenceNumber,
                   category=$category,
                   building=$building,
                   buildingDescription=$buildingDescription,
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
                   function=$function,
                   dayNumber=$dayNumber,
                   override=$override ,
                   hoursWeek=$hoursWeek,
                   meetNumber=$meetNumber,
                   creditHoursSession=$creditHourSession   ,
                   dayOfWeek=$dayOfWeek,
                   scheduleType=$scheduleType ,
                   termCourseReferenceNumber=$termCourseReferenceNumber
                   ]"""
    }

    /**
     * Class room meeting filter includes
     * @param meetingFilter
     * sunday : boolean :  is true or is false
     * monday  : boolean
     * tuesday  : boolean
     * wednesday  : boolean
     * thursday : boolean
     * friday   : boolean
     * saturday   : boolean
     * beginTime  : string  equals
     * endTime   : string   equals
     * term:  string required
     * courseReferneceNumber :  string required
     * @return list of meetings
     */

    def static countAll(def meetingFilter) {
        def query = parseQuery(meetingFilter, null)
        def returnListCount = MeetingTimeSearch.executeQuery(query)
        return returnListCount.size()
    }


    def static fetchSearch(def meetingFilter) {
        def query = parseQuery(meetingFilter, " order by a.category")
        return MeetingTimeSearch.findAll(query)
    }


    def static parseQuery(def meetingFilter, def orderBy) {
        String.metaClass.flattenString = {
            return delegate.replace("\n", "").replaceAll(/  */, " ")
        }

        def stringKeys = ["courseReferenceNumber", "term", "category", "building","termCourseReferenceNumber"]
        def booleanKeys = ["sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday"]

        def query = """FROM  MeetingTimeSearch a  """
        def cnt = 0
        meetingFilter.each { param ->
            def grailsDomainClass = new DefaultGrailsDomainClass(MeetingTimeSearch)
            def fields = grailsDomainClass.properties
            def field = fields.find { it.name == param.key}
            def fieldType = null
            if (field?.type?.name) fieldType = field.type.name
            else fieldType = "java.lang.String"
            if (field?.oneToOne) fieldType = "java.lang.String"

            if (cnt > 0) query += " and "
            else query += " where "
            cnt += 1
            if (stringKeys.contains(param.key)) {
                if (param.value instanceof Collection) {
                    def inClause = null
                    if (fieldType == "java.lang.String") {
                        param.value.each { val ->
                            if (inClause) inClause += ",'" + val + "'"
                            else inClause = "'" + val + "'"
                        }
                    }
                    else {
                        param.value.each { val ->
                            if (inClause) inClause += "," + val
                            else inClause = val
                        }
                    }
                    if (field?.oneToOne) {
                        query += " a.${param.key}.code  in (${inClause}) "
                    }
                    else {
                        query += " a.${param.key} in (${inClause}) "
                    }
                }
                else {
                    query += " a.${param.key}  = '${param.value}'  "
                }
            }
            else if (booleanKeys.contains(param.key)) {
                query += " a.${param.key} is ${param.value}   "
            }
            else if (param.key == "beginTime") {
                query += " nvl(a.beginTime,'0000') >= '${param.value}' "
            }
            else if (param.key == "endTime") {
                query += " nvl(a.endTime,'0000') <= '${param.value}' "
            }
        }

        if (orderBy) query += orderBy
        return query.flattenString().toString()
    }


    def static parseQueryString(def meetingFilter, String prefix) {

        def stringKeys = ["courseReferenceNumber", "term", "category", "building","termCourseReferenceNumber"]
        def booleanKeys = ["sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday"]

        def query = ""
        def cnt = 0
        meetingFilter.each { param ->
            def grailsDomainClass = new DefaultGrailsDomainClass(MeetingTimeSearch)
            def fields = grailsDomainClass.properties
            def field = fields.find { it.name == param.key}
            def fieldType = null
            if (field?.type?.name) fieldType = field.type.name
            else fieldType = "java.lang.String"
            if (field?.oneToOne) fieldType = "java.lang.String"

            if (cnt > 0) query += " and "
            cnt += 1
            if (stringKeys.contains(param.key)) {
                if (param.value instanceof Collection) {
                    def inClause = null
                    if (fieldType == "java.lang.String") {
                        param.value.each { val ->
                            if (inClause) inClause += ",'" + val + "'"
                            else inClause = "'" + val + "'"
                        }
                    }
                    else {
                        param.value.each { val ->
                            if (inClause) inClause += "," + val
                            else inClause = val
                        }
                    }
                    if (field?.oneToOne) {
                        query += " ${prefix}.${param.key}.code  in (${inClause}) "
                    }
                    else {
                        query += " ${prefix}.${param.key} in (${inClause}) "
                    }
                }
                else {
                    query += " ${prefix}.${param.key}  = '${param.value}'  "
                }
            }
            else if (booleanKeys.contains(param.key)) {
                query += " ${prefix}.${param.key} is ${param.value}   "
            }
            else if (param.key == "beginTime") {
                query += " nvl(${prefix}.beginTime,'0000') >= '${param.value}' "
            }
            else if (param.key == "endTime") {
                query += " nvl(${prefix}.endTime,'0000') <= '${param.value}' "
            }
        }

        return query
    }

}
