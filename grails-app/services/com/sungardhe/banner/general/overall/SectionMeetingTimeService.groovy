/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */

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


    public static boolean isMeetingTimesForSession(String term, String crn, String category) {
        def meetingTimes = SectionMeetingTime.findAllWhere(term: term,
                courseReferenceNumber: crn,
                category: category)
        return (meetingTimes.size() > 0)
    }
    /*PROTECTED REGION END*/
}
