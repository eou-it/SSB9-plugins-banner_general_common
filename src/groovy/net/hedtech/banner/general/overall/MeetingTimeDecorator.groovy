/** *****************************************************************************
 Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */

package net.hedtech.banner.general.overall

import net.hedtech.banner.general.system.Building
import net.hedtech.banner.general.system.Campus

class MeetingTimeDecorator {

    String courseReferenceNumber
    String term
    String category
    /**
     * This field defines the Begin Time of the course section being scheduled.  It is a required field and is in the format HHMM using military times.  The SSRSECT (Schedule of Classes) converts this time to standard times.
     */
    String beginTime

    /**
     * This field defines the End Time of the course section being scheduled.  It is a required field and is in the format HHMM using military times.  The SSRSECT (Schedule of Classes) converts this time to standard times.
     */
    String endTime

    /**
     * Building class meets in
     */
    String building

    String buildingDescription
    /**
     * This field defines the Room where the course section will be scheduled.  It is not required when scheduling course section meeting times.  It is required when scheduling a course section meeting building.
     */
    String room

    /**
     * Section Meeting Start Date.
     */
    Date startDate

    /**
     * Section End Date.
     */
    Date endDate

    /**
     * Section Meeting Time Sunday Indicator.
     */
    Boolean sunday

    /**
     * Section Meeting Time Monday Indicator.
     */
    Boolean monday

    /**
     * Section Meeting Time Tuesday Indicator.
     */
    Boolean tuesday
    /**
     * Section Meeting Time Wednesday Indicator.
     */
    Boolean wednesday

    /**
     * Section Meeting Time Thrusday Indicator.
     */
    Boolean thursday

    /**
     * Section Meeting Time Friday Indicator.
     */
    Boolean friday

    /**
     * Section Meeting Time Saturday Indicator.
     */
    Boolean saturday

    String meetingScheduleType

    /**
     * The session credit hours
     */
    Double creditHourSession

    /**
     * Section Meeting Hours per Week.
     */
    Double hoursWeek

    String campus

    String campusDescription



    MeetingTimeDecorator(MeetingTimeSearch section) {
        this.term = section.term
        this.courseReferenceNumber = section.courseReferenceNumber
        this.category = section.category
        this.beginTime = section.beginTime
        this.endTime = section.endTime
        this.room = section.room
        this.startDate = section.startDate
        this.endDate = section.endDate
        this.sunday = section.sunday
        this.monday = section.monday
        this.tuesday = section.tuesday
        this.wednesday = section.wednesday
        this.thursday = section.thursday
        this.friday = section.friday
        this.saturday = section.saturday
        this.creditHourSession = section.creditHourSession
        this.hoursWeek = section.hoursWeek
        this.building = section.building
        if (this.building && !this.buildingDescription) {
            this.buildingDescription = Building.executeQuery("select description from Building where code = ?", [this.building])[0]
        }
        this.meetingScheduleType = section.scheduleType
        this.campus = section.campus
        if (this.campus && !this.campusDescription) {
            this.campusDescription = Campus.executeQuery("select description from Campus where code = ?",[this.campus])[0]
        }
    }


    MeetingTimeDecorator() {
        this.term = null
        this.courseReferenceNumber = null
        this.category = null
        this.beginTime = null
        this.endTime = null
        this.room = null
        this.startDate = null
        this.endDate = null
        this.sunday = null
        this.monday = null
        this.tuesday = null
        this.wednesday = null
        this.thursday = null
        this.friday = null
        this.saturday = null
        this.creditHourSession = null
        this.hoursWeek = null
        this.building = null
        this.buildingDescription = null
        this.meetingScheduleType = null
        this.campus = null
        this.campusDescription = null
    }


    public String toString() {
        """MeetingTimeDecorator[
                   term=$term,
	               courseReferenceNumber=$courseReferenceNumber,
	               category=$category,
                   beginTime=$beginTime,
                   endTime=$endTime,
                   room=$room,
                   startDate=$startDate,
                   endDate=$endDate,
                   sunday=$sunday,
                   monday=$monday,
                   tuesday=$tuesday,
                   wednesday=$wednesday,
                   thursday=$thursday,
                   friday=$friday,
                   saturday=$saturday,
                   creditHourSession=$creditHourSession,
                   hoursWeek=$hoursWeek,
                   building=$building,
                   buildingDescription=$buildingDescription,
                   meetingScheduleType=$meetingScheduleType,
                   campus=$campus,
                   campusDescription=$campusDescription
		           ]"""
    }


}
