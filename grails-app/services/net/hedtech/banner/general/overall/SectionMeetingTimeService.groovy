/*******************************************************************************
 Copyright 2013-2014 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */

package net.hedtech.banner.general.overall


import net.hedtech.banner.service.ServiceBase
import net.hedtech.banner.general.system.Term
import net.hedtech.banner.exceptions.ApplicationException

class SectionMeetingTimeService extends ServiceBase {

    private static final String[] numericCategorySequence
    static {
        numericCategorySequence = new String[100]
        int counter = 0
        int asciiZero = 48
        int asciiNine = 57
        for (int i = asciiZero; i <= asciiNine; i++)
            for (int j = asciiZero; j <= asciiNine; ++j)
                numericCategorySequence[counter++] = Character.toChars( i ).toString() + Character.toChars( j )
    }

    boolean transactional = true
    def sessionFactory

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
        if (!sectionMeetingTime.hoursWeek && sectionMeetingTime.term != "EVENT") {
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


    public void validateCreditHourSession(section, courseGeneralInformation, sectionMeetingTimes) {

        def creditHourSessionTotal = 0

        sectionMeetingTimes.each { domain ->
            if (domain.creditHourSession) {
                creditHourSessionTotal += domain.creditHourSession
            }
        }

        if (section?.creditHours && creditHourSessionTotal > section.creditHours) {
            throw new ApplicationException( SectionMeetingTime, "@@r1:sessionHoursExceedsSectionHigh:${section.creditHours}@@" )
        }

        if (!section?.creditHours && !courseGeneralInformation?.creditHourIndicator && creditHourSessionTotal > courseGeneralInformation?.creditHourLow) {
            throw new ApplicationException( SectionMeetingTime, "@@r1:sessionHoursExceedsSectionHigh:${courseGeneralInformation?.creditHourLow}@@" )
        }

        if (!section?.creditHours && courseGeneralInformation?.creditHourIndicator == 'TO' && (creditHourSessionTotal < courseGeneralInformation?.creditHourLow || creditHourSessionTotal > courseGeneralInformation?.creditHourHigh)) {
            throw new ApplicationException( SectionMeetingTime, "@@r1:sessionHoursNotBetweenSectionHours:${courseGeneralInformation?.creditHourLow}:${courseGeneralInformation?.creditHourHigh}@@" )
        }

        if (!section?.creditHours && courseGeneralInformation?.creditHourIndicator == 'OR' && creditHourSessionTotal > courseGeneralInformation?.creditHourHigh) {
            throw new ApplicationException( SectionMeetingTime, "@@r1:sessionHoursExceedsSectionHigh:${courseGeneralInformation?.creditHourHigh}@@" )
        }
    }


    public String generateCategoryValue( String term, String courseReferenceNumber ) {
           String category = numericCategorySequence[0]
           List categories = SectionMeetingTime.fetchCategoryByTermAndCourseReferenceNumber( term, courseReferenceNumber )
           if (categories) {
               // Categories are unique and are in sequence. Starts with 00..99, then AA..ZZ and finally aa..zz
               // Since its likely that section meeting time in between could be deleted, we are trying to utilize the limited available sequence.
               // Hence iterating through the list and assigning the next available category.
               if (categories.size() <= numericCategorySequence.length) {
                   int counter = 0
                   for (String innerCategory : categories) {
                       if (innerCategory != numericCategorySequence[counter++]) {
                           category = numericCategorySequence[counter - 1]
                           break
                       }
                   }
                   category = numericCategorySequence[counter]
               }

               // Number of possible values between AA..ZZ is 26*26 =676 + No of values in numericCategorySequence
               if (categories.size() > numericCategorySequence.length && categories.size() <= 776) {
                   String[] capitalsAlphabeticalSequence = new String[676]
                   int counter = 0
                   int asciiA = 65
                   int asciiZ = 90
                   for (int i = asciiA; i <= asciiZ; i++)
                       for (int j = asciiA; j <= asciiZ; j++)
                           capitalsAlphabeticalSequence[counter++] = Character.toChars( i ).toString() + Character.toChars( j )

                   for (String innerCategory : categories) {
                       if (innerCategory != capitalsAlphabeticalSequence[counter++]) {
                           category = capitalsAlphabeticalSequence[counter - 1]
                           break
                       }
                   }
                   category = capitalsAlphabeticalSequence[counter]
                   capitalsAlphabeticalSequence = null
               }

               // Number of possible values between aa..zz is 26*26 =676 + 676+ No of values in numericCategorySequence
               if (categories.size() > 776 && categories.size() <= 1452) {
                   String[] smallAlphabeticalSequence = new String[676]
                   int counter = 0
                   int asciiA = 97
                   int asciiZ = 122
                   for (int i = asciiA; i <= asciiZ; i++)
                       for (int j = asciiA; j <= asciiZ; j++)
                           smallAlphabeticalSequence[counter++] = Character.toChars( i ).toString() + Character.toChars( j )

                   for (String innerCategory : categories) {
                       if (innerCategory != smallAlphabeticalSequence[counter++]) {
                           category = smallAlphabeticalSequence[counter - 1]
                           break
                       }
                   }
                   category = smallAlphabeticalSequence[counter]
                   smallAlphabeticalSequence = null
               }

               // If the number of possible combinations have exceeded, throw an error
               if (categories.size() > 1452) {
                   throw new ApplicationException( SectionMeetingTime, "@@r1:session_indicator_cannot_be_calculated@@" )
               }
           }
           return category
       }


}
