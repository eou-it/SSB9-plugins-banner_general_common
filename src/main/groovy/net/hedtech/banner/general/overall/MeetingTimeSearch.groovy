/*********************************************************************************
  Copyright 2010-2020 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall

import org.hibernate.annotations.Type
import grails.util.Holders
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.OneToOne

import javax.persistence.*

/**
 * Meeting Time  model.
 */
@Entity
@Table(name = "GVQ_SSRMEET")
@NamedQueries(value = [
@NamedQuery(name = "MeetingTimeSearch.fetchByTermAndCourseReferenceNumber",
        query = """FROM  MeetingTimeSearch a
        WHERE a.term = :term
        and a.courseReferenceNumber = :crn
        order by a.term, a.courseReferenceNumber, a.startDate, a.monday, a.tuesday, a.wednesday,
        a.thursday, a.friday, a.saturday, a.sunday, a.beginTime """),
@NamedQuery(name = "MeetingTimeSearch.fetchListMeetingTimeDetailByTermAndCourseReferenceNumber",
        query = """FROM  MeetingTimeSearch a
        WHERE a.term IN (:termCode)
        AND   a.courseReferenceNumber IN (:courseReferenceNumbers)"""),
@NamedQuery(name = "MeetingTimeSearch.fetchTotalHoursByTermAndCourseReferenceNumber",
        query = """FROM  MeetingTimeSearch a
        WHERE a.term = :term
        and a.courseReferenceNumber = :crn""")
])
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

    @Column(name = "ssrmeet_bldg_desc")
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

    @Column(name = "ssrmeet_term_crn")
    String termCourseReferenceNumber

    @Column(name = "SSRMEET_MTYP_CODE")
    String meetingType

    /**
     * The total hours
     */
    @Column(name = "ssrmeet_hrs_total", precision = 7, scale = 2)
    Double totalHours

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
                   creditHoursSession=$creditHourSession,
                   dayOfWeek=$dayOfWeek,
                   scheduleType=$scheduleType,
                   termCourseReferenceNumber=$termCourseReferenceNumber,
                   meetingType=$meetingType,
				   totalHours=$totalHours	
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


    def static parseQuery(def meetingFilter, String orderBy) {
        String.metaClass.flattenString = {
            return delegate.replace("\n", "").replaceAll(/  */, " ")
        }

        def stringKeys = ["courseReferenceNumber", "term", "category", "building", "termCourseReferenceNumber"]
        def booleanKeys = ["sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday"]

        String query = """FROM  MeetingTimeSearch a  """
        def cnt = 0
        meetingFilter.each { param ->
            // get meta data for the parameter to parse the condition
            PersistentEntity grailsDomainClass = Holders.getGrailsApplication().getMappingContext().getPersistentEntity(MeetingTimeSearch.name)
            PersistentProperty[] fields = grailsDomainClass.getPersistentProperties()

            def field = fields.find { it.name == param.key }
            def fieldType = null
            if (field?.type?.name) fieldType = field.type.name
            else fieldType = "java.lang.String"
            if (field instanceof OneToOne) fieldType = "java.lang.String"

            if (cnt > 0) query += " and "
            else query += " where "
            cnt += 1
            if (stringKeys.contains(param.key)) {
                if (param.value instanceof Collection) {
                    String inClause = null
                    if (fieldType == "java.lang.String") {
                        param.value.each { val ->
                            if (inClause) inClause += ",'" + val + "'"
                            else inClause = "'" + val + "'"
                        }
                    } else {
                        param.value.each { val ->
                            if (inClause) inClause += "," + val
                            else inClause = val
                        }
                    }
                    if (field instanceof OneToOne) {
                        query += " a.${param.key}.code  in (${inClause}) "
                    } else {
                        query += " a.${param.key} in (${inClause}) "
                    }
                } else {
                    query += " a.${param.key}  = '${param.value}'  "
                }
            } else if (booleanKeys.contains(param.key)) {
                query += " a.${param.key} is ${param.value}   "
            } else if (param.key == "beginTime") {
                query += " nvl(a.beginTime,'0000') >= '${param.value}' "
            } else if (param.key == "endTime") {
                query += " nvl(a.endTime,'0000') <= '${param.value}' "
            }
        }

        if (orderBy) query += orderBy
        return query.flattenString().toString()
    }


    def static parseQueryString(def meetingFilter, String prefix, Map filterCriteria) {

        def stringKeys = ["courseReferenceNumber", "term", "category", "building", "termCourseReferenceNumber"]
        def booleanKeys = ["sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday"]

        String query = ""
        def cnt = 0
        meetingFilter.each { param ->
            // get meta data for the parameter to parse the condition
            PersistentEntity grailsDomainClass = Holders.getGrailsApplication().getMappingContext().getPersistentEntity(MeetingTimeSearch.name)
            PersistentProperty[] fields = grailsDomainClass.getPersistentProperties()

            def field = fields.find { it.name == param.key }
            def fieldType = null
            if (field?.type?.name) fieldType = field.type.name
            else fieldType = "java.lang.String"
            if (field instanceof OneToOne) fieldType = "java.lang.String"

            if (cnt > 0) query += " and "
            cnt += 1
            if (stringKeys.contains(param.key)) {
                if (param.value instanceof Collection) {
                    String inClause = null
                    if (fieldType == "java.lang.String") {
                        param.value.each { val ->
                            if (inClause) inClause += ",'" + val + "'"
                            else inClause = "'" + val + "'"
                        }
                    } else {
                        param.value.each { val ->
                            if (inClause) inClause += "," + val
                            else inClause = val
                        }
                    }
                    if (field instanceof OneToOne) {
                        query += " ${prefix}.${param.key}.code  in (:${param.key}) "
                    } else {
                        query += " ${prefix}.${param.key} in (:${param.key}) "
                    }
                    filterCriteria.put(param.key, param.value)
                } else {
                    query += " ${prefix}.${param.key}  = :${param.key}   "
                    filterCriteria.put(param.key, param.value)
                }
            } else if (booleanKeys.contains(param.key)) {
                if (param.value instanceof String && param.value =~ "not"){
                    query += " ${prefix}.${param.key} is not :${param.key}   "
                    filterCriteria.put(param.key, true)
                }
                else {
                    query += " ${prefix}.${param.key} is :${param.key}   "
                    filterCriteria.put(param.key, param.value)
                }
            } else if (param.key == "beginTime") {
                query += " nvl(${prefix}.beginTime,'0000') >= :${param.key}  "
                filterCriteria.put(param.key, param.value)
            } else if (param.key == "endTime") {
                query += " nvl(${prefix}.endTime,'0000') <= :${param.key}  "
                filterCriteria.put(param.key, param.value)
            }
        }

        return [query: query,  filter: filterCriteria]
    }


    public static List fetchByTermAndCourseReferenceNumber(String term, String crn) {
        def meetingTimeSearchList = MeetingTimeSearch.withSession { session ->
            session.getNamedQuery(
                    'MeetingTimeSearch.fetchByTermAndCourseReferenceNumber').setString('term', term).setString('crn', crn).list()
        }
        return meetingTimeSearchList
    }

    public static def fetchListMeetingTimeDetailByTermAndCourseReferenceNumber(def termList, def courseReferenceNumbers) {
        termList = termList.size()==1 ? termList[0] : termList
        def meetingTimeResultList = MeetingTimeSearch.withSession { session ->
            session.getNamedQuery('MeetingTimeSearch.fetchListMeetingTimeDetailByTermAndCourseReferenceNumber').setParameterList('termCode', termList).setParameterList('courseReferenceNumbers', courseReferenceNumbers.size()>0?courseReferenceNumbers:'').list()
        }
        return meetingTimeResultList
    }

    public static def getTotalHoursObjectList(String term, String crn){
        def meetingTimeSearchList = MeetingTimeSearch.withSession { session ->
            session.getNamedQuery(
                    'MeetingTimeSearch.fetchTotalHoursByTermAndCourseReferenceNumber').setString('term', term).setString('crn', crn).list()
        }
        return meetingTimeSearchList
    }

}
