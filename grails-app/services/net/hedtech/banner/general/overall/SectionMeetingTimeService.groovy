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
        numericCategorySequence = new String[99]
        int counter = 0
        int asciiZero = 48
        int asciiNine = 57
        for (int i = asciiZero; i <= asciiNine; i++) {
            for (int j = asciiZero; j <= asciiNine; ++j) {
                if (i == asciiZero && j == asciiZero)
                    continue
                numericCategorySequence[counter++] = Character.toChars( i ).toString() + Character.toChars( j )
            }
        }
    }

    boolean transactional = true
    def sessionFactory


    def preCreate( Map map ) {
        //Unable to validate against ScheduleUtility because this is in the General package.  API will enforce relationship.
        validateCodes( map.domainModel )
    }


    def preUpdate( Map map ) {
        validateCodes( map.domainModel )
    }

    /**
     * Validate the SectionMeetingTime values which are not enforced with a foreign key constraint
     */

    private void validateCodes( SectionMeetingTime sectionMeetingTime ) {
        validateTerm( sectionMeetingTime.term )
        //If section meeting time, the hoursWeek is required
        validateTimes( sectionMeetingTime )
        if (sectionMeetingTime.hoursWeek == null && sectionMeetingTime.term != "EVENT") {
            throw new ApplicationException( SectionMeetingTime, "@@r1:missing_hours_week@@" )
        }
    }

    /**
     * Method to validate that the term is in STVTERM or "EVENT"
     */

    private void validateTerm( String term ) {
        if (term != "EVENT" && !Term.findByCode( term )) {
            throw new ApplicationException( SectionMeetingTime, "@@r1:invalid_term@@" )
        }
    }


    private void validateTimes( sectionMeetingTime ) {
        if ((sectionMeetingTime.beginTime != null && sectionMeetingTime.beginTime != "") && ((sectionMeetingTime.beginTime.length() != 4) || (sectionMeetingTime.beginTime < "0000") || (sectionMeetingTime.beginTime > "2359") ||
                (sectionMeetingTime.beginTime.toString().substring( 2, 3 ) > "59")))
            throw new ApplicationException( SectionMeetingTime, "@@r1:begin_time@@" )
        if ((sectionMeetingTime.endTime != null && sectionMeetingTime.endTime != "") && ((sectionMeetingTime.endTime.length() != 4) || (sectionMeetingTime.endTime < "0000") || (sectionMeetingTime.endTime > "2359") ||
                (sectionMeetingTime.endTime.toString().substring( 2, 3 ) > "59")))
            throw new ApplicationException( SectionMeetingTime, "@@r1:end_time@@" )
        if ((sectionMeetingTime.beginTime != null && sectionMeetingTime.beginTime != "") && (sectionMeetingTime.endTime == null))
            throw new ApplicationException( SectionMeetingTime, "@@r1:begin_end_time@@" )
        if ((sectionMeetingTime.endTime != null && sectionMeetingTime.endTime != "") && (sectionMeetingTime.beginTime == null))
            throw new ApplicationException( SectionMeetingTime, "@@r1:begin_end_time@@" )
        if ((sectionMeetingTime.beginTime != null && sectionMeetingTime.beginTime != "") && (sectionMeetingTime.endTime != null && sectionMeetingTime.endTime != "") &&
                (sectionMeetingTime.beginTime >= sectionMeetingTime.endTime))
            throw new ApplicationException( SectionMeetingTime, "@@r1:begin_time_greater_than_end_time@@" )
    }


    public static boolean isMeetingTimesForSession( String term, String crn, String category ) {
        def meetingTimes = SectionMeetingTime.findAllWhere( term: term,
                                                            courseReferenceNumber: crn,
                                                            category: category )
        return (meetingTimes.size() > 0)
    }


    public boolean isBuildingRoomAndNoTimeDays( SectionMeetingTime sectionMeetingTime ) {
        if ((sectionMeetingTime.building && sectionMeetingTime.room)
                && ((!sectionMeetingTime.beginTime && !sectionMeetingTime.endTime)
                || (!sectionMeetingTime.monday && !sectionMeetingTime.tuesday && !sectionMeetingTime.wednesday &&
                !sectionMeetingTime.thursday && !sectionMeetingTime.friday && !sectionMeetingTime.saturday &&
                !sectionMeetingTime.sunday))) {
            return true
        }
        return false
    }


    public void validateCreditHourSession( section, courseGeneralInformation, sectionMeetingTimes ) {

        def creditHourSessionTotal = 0

        sectionMeetingTimes.each {domain ->
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
        String category
        List categories = SectionMeetingTime.fetchCategoryByTermAndCourseReferenceNumber( term, courseReferenceNumber )

        if (categories) {
            // Categories are unique and are in sequence. Starts with 00..99, then AA..ZZ and finally aa..zz
            // Since its likely that section meeting time in between could be deleted, we are trying to utilize the limited available sequence.
            // Hence iterating through the list and assigning the next available category.

            for (int i = 0; i <= numericCategorySequence.length; i++) {
                if (!category && !categories.contains( numericCategorySequence[i] )) {
                    category = numericCategorySequence[i]
                    break
                }
            }

            // Number of possible values between AA..ZZ is 26*26 =676
            if (!category) {
                String[] capitalsAlphabeticalSequence = new String[676]
                int counter = 0
                int asciiA = 65
                int asciiZ = 90
                for (int i = asciiA; i <= asciiZ; i++)
                    for (int j = asciiA; j <= asciiZ; j++)
                        capitalsAlphabeticalSequence[counter++] = Character.toChars( i ).toString() + Character.toChars( j )

                for (int i = 0; i <= capitalsAlphabeticalSequence.length; i++) {
                    if (!category && !categories.contains( capitalsAlphabeticalSequence[i] )) {
                        category = capitalsAlphabeticalSequence[i]
                        break
                    }
                }
                capitalsAlphabeticalSequence = null
            }

            // Number of possible values between aa..zz is 26*26 =676
            if (!category) {
                String[] smallAlphabeticalSequence = new String[676]
                int counter = 0
                int asciiA = 97
                int asciiZ = 122
                for (int i = asciiA; i <= asciiZ; i++)
                    for (int j = asciiA; j <= asciiZ; j++)
                        smallAlphabeticalSequence[counter++] = Character.toChars( i ).toString() + Character.toChars( j )

                for (int i = 0; i <= capitalsAlphabeticalSequence.length; i++) {
                    if (!category && !categories.contains( smallAlphabeticalSequence[i] )) {
                        category = smallAlphabeticalSequence[i]
                        break
                    }
                }
                smallAlphabeticalSequence = null
            }

            // If the number of possible combinations have exceeded, throw an error i.e 676+676+99
            if (categories.size() > 1451) {
                throw new ApplicationException( SectionMeetingTime, "@@r1:session_indicator_cannot_be_calculated@@" )
            }
        }

        if (category)
            category = numericCategorySequence[0]

        return category
    }


    public Double calculateHoursPerWeek( SectionMeetingTime sectionMeetingTime, Integer calculatedDeviationFactor ) {
        def beginTime = sectionMeetingTime.beginTime
        def endTime = sectionMeetingTime.endTime
        def sValue = 0.0
        if (beginTime && endTime && beginTime.length() == 4 && endTime.length() == 4) {
            int numberOfDays = 0
            if (sectionMeetingTime.monday) ++numberOfDays
            if (sectionMeetingTime.tuesday) ++numberOfDays
            if (sectionMeetingTime.wednesday) ++numberOfDays
            if (sectionMeetingTime.thursday) ++numberOfDays
            if (sectionMeetingTime.friday) ++numberOfDays
            if (sectionMeetingTime.saturday) ++numberOfDays
            if (sectionMeetingTime.sunday) ++numberOfDays

            def num = numberOfDays * ((endTime[0, 1].toInteger() * 60 + endTime[2, 3].toInteger()) - (beginTime[0, 1].toInteger() * 60 + beginTime[2, 3].toInteger()))
            if (!calculatedDeviationFactor)
                calculatedDeviationFactor = 60
            sValue = String.valueOf( num / calculatedDeviationFactor )
            if (sValue.indexOf( '.' ) != -1 && sValue.substring( sValue.indexOf( '.' ), sValue.length() ).length() > 3) {
                sValue = sValue.substring( 0, sValue.indexOf( '.' ) + 3 )
            }
        }
        return new Double( sValue )
    }


}
