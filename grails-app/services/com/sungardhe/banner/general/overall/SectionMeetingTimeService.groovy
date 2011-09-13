/*********************************************************************************
 Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of 
 SunGard Higher Education and its subsidiaries. Any use of this software is limited 
 solely to SunGard Higher Education licensees, and is further subject to the terms 
 and conditions of one or more written license agreements between SunGard Higher 
 Education and the licensee in question. SunGard, Banner and Luminis are either 
 registered trademarks or trademarks of SunGard Higher Education in the U.S.A. 
 and/or other regions and/or countries.
 **********************************************************************************/

package com.sungardhe.banner.general.overall


import com.sungardhe.banner.service.ServiceBase
import com.sungardhe.banner.general.system.Term
import com.sungardhe.banner.exceptions.ApplicationException

// NOTE:
// This service is injected with create, update, and delete methods that may throw runtime exceptions (listed below).  
// These exceptions must be caught and handled by the controller using this service.
// 
// update and delete may throw com.sungardhe.banner.exceptions.NotFoundException if the entity cannot be found in the database
// update and delete may throw org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException a runtime exception if an optimistic lock failure occurs
// create, update, and delete may throw grails.validation.ValidationException a runtime exception when there is a validation failure

class SectionMeetingTimeService extends ServiceBase {

    boolean transactional = true
    def sessionFactory

    /**
     * Please put all the custom methods in this protected section to protect the code
     * from being overwritten on re-generation
     */
    /*PROTECTED REGION ID(sectionmeetingtime_custom_service_methods) ENABLED START*/

    def preCreate(Map map) {
        //Unable to validate against ScheduleUtility because this is in the General package.  API will enforce relationship.
        validateCodes(map.domainModel)
    }


    def preUpdate(Map map) {
        validateCodes(map.domainModel)
    }

    /**
     * Validate the SectionMeetingTime values which are not enforced with a foreign key constraint
     */

    private void validateCodes(SectionMeetingTime sectionMeetingTime) {
        validateTerm(sectionMeetingTime.term)
        //If section meeting time, the hoursWeek is required
        validateTimes(sectionMeetingTime)
        if (!sectionMeetingTime.hoursWeek && sectionMeetingTime.term != "EVENT"){
            throw new ApplicationException(SectionMeetingTime, "@@r1:missing_hours_week@@")
        }
    }

    /**
     * Method to validate that the term is in STVTERM or "EVENT"
     */

    private void validateTerm(String term) {
        if (term != "EVENT" && !Term.findByCode(term)) {
            throw new ApplicationException(SectionMeetingTime, "@@r1:invalid_term@@")
        }
    }


    private void validateTimes(sectionMeetingTime) {

        if ((sectionMeetingTime.beginTime != null && sectionMeetingTime.beginTime != "") && ((sectionMeetingTime.beginTime < "0000") || (sectionMeetingTime.beginTime > "2359") ||
                (sectionMeetingTime.beginTime.toString().substring(2, 3) > "59")))
            throw new ApplicationException(SectionMeetingTime, "@@r1:begin_time@@")
        if ((sectionMeetingTime.beginTime != null && sectionMeetingTime.beginTime != "") && (sectionMeetingTime.endTime == null))
            throw new ApplicationException(SectionMeetingTime, "@@r1:begin_end_time@@")
        if ((sectionMeetingTime.beginTime != null && sectionMeetingTime.beginTime != "") && (sectionMeetingTime.endTime != null && sectionMeetingTime.endTime != "") &&
                (sectionMeetingTime.beginTime >= sectionMeetingTime.endTime))
            throw new ApplicationException(SectionMeetingTime, "@@r1:begin_time_greater_than_end_time@@")
        if ((sectionMeetingTime.endTime != null && sectionMeetingTime.endTime != "") && ((sectionMeetingTime.endTime < "0000") || (sectionMeetingTime.endTime > "2359") ||
                (sectionMeetingTime.endTime.toString().substring(2, 3) > "59")))
            throw new ApplicationException(SectionMeetingTime, "@@r1:end_time@@")
        if ((sectionMeetingTime.endTime != null && sectionMeetingTime.endTime != "") && (sectionMeetingTime.beginTime == null))
            throw new ApplicationException(SectionMeetingTime, "@@r1:begin_end_time@@")
    }


    public static boolean isMeetingTimesForSession(String term, String crn, String category) {
        def meetingTimes = SectionMeetingTime.findAllWhere(term: term,
                courseReferenceNumber: crn,
                category: category)
        return (meetingTimes.size() > 0)
    }


    public boolean isBuildingRoomAndNoTimeDays(SectionMeetingTime sectionMeetingTime) {
        if ((sectionMeetingTime.building && sectionMeetingTime.room)
                && ((!sectionMeetingTime.beginTime && !sectionMeetingTime.endTime)
                || (!sectionMeetingTime.monday && !sectionMeetingTime.tuesday && !sectionMeetingTime.wednesday &&
                !sectionMeetingTime.thursday && !sectionMeetingTime.friday && !sectionMeetingTime.saturday &&
                !sectionMeetingTime.sunday))) {
            return true
        }
        return false

    }
    /*PROTECTED REGION END*/
}
