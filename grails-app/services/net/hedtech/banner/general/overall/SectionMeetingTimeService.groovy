/*******************************************************************************
 Copyright 2013-2019 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import groovy.sql.Sql
import grails.gorm.transactions.Transactional
import net.hedtech.banner.DateUtility
import net.hedtech.banner.service.ServiceBase
import net.hedtech.banner.general.system.Term
import net.hedtech.banner.exceptions.ApplicationException

import java.sql.SQLException

@Transactional
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

    def sessionFactory


    def preCreate( Map map ) {
        //Unable to validate against ScheduleUtility because this is in the General package.  API will enforce relationship.
        validateData( map.domainModel )
    }


    def preUpdate( Map map ) {
        validateData( map.domainModel )
    }

    /**
     * Validate the SectionMeetingTime values which are not enforced with a foreign key constraint
     */

    private void validateData( SectionMeetingTime sectionMeetingTime ) {
        validateTerm(sectionMeetingTime.term)
        if (sectionMeetingTime.term != "EVENT") {
            def section = getSection(sectionMeetingTime.term, sectionMeetingTime.courseReferenceNumber)
            validateDates(section, sectionMeetingTime)
        }
        validateTimes( sectionMeetingTime )
        validateHoursPerWeek( sectionMeetingTime )
    }


    private void validateHoursPerWeek( SectionMeetingTime sectionMeetingTime ) {
        //If section meeting time, the hoursWeek is required
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

        if (!category)
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


    private void validateDates( section, sectionMeetingTime ) {

        if (section?.partOfTermEndDate) {
            if (sectionMeetingTime.endDate > section.partOfTermEndDate || sectionMeetingTime.endDate == null) {
                throw new ApplicationException( SectionMeetingTime, "@@r1:endDateAfterSectionEndDate:${DateUtility.formatDate(section.partOfTermEndDate)}@@" )
            }
        }

        if (section?.partOfTermStartDate) {
            if (sectionMeetingTime.startDate < section.partOfTermStartDate || sectionMeetingTime.startDate == null) {
                throw new ApplicationException( SectionMeetingTime, "@@r1:startDateBeforeSectionStartDate:${DateUtility.formatDate(section.partOfTermStartDate)}@@" )
            }
        }

        //For OLR courses, start date and end date are to be validated against Learner First and last start dates
        if (section?.learnerRegStartFromDate || section?.learnerRegStartToDate) {
            def startDate = sectionMeetingTime.startDate
            def endDate = sectionMeetingTime.endDate

            if (startDate == null || startDate < section.learnerRegStartFromDate) {
                throw new ApplicationException( SectionMeetingTime, "@@r1:startDate.invalid.start_date_greater_than_learner_first:${DateUtility.formatDate(section.learnerRegStartFromDate)}@@" )
            } else if (startDate > section.learnerRegStartToDate) {
                throw new ApplicationException( SectionMeetingTime, "@@r1:startDate.invalid.start_date_less_than_learner_last:${DateUtility.formatDate(section.learnerRegStartToDate)}@@" )
            } else if (endDate == null) {
                throw new ApplicationException( SectionMeetingTime, "@@r1:endDate.invalid.end_less_than_start_date@@" )
            }
        }

    }


    private Map getSection( String term, String courseReferenceNumber ) {
        Map section = new HashMap()
        def sql

        if(term && courseReferenceNumber) {
            try {
                sql = new Sql( sessionFactory.getCurrentSession().connection() )
                def result = sql.rows( "select ssbsect_ptrm_start_date, ssbsect_ptrm_end_date, ssbsect_learner_regstart_fdate, ssbsect_learner_regstart_tdate from ssbsect where ssbsect_term_code = ? and ssbsect_crn = ?", [term, courseReferenceNumber] )[0]
                section.partOfTermStartDate = result[0] ? new Date( result[0]?.getTime() ) : null
                section.partOfTermEndDate = result[1] ? new Date( result[1]?.getTime() ) : null
                section.learnerRegStartFromDate = result[2] ? new Date( result[2]?.getTime() ) : null
                section.learnerRegStartToDate = result[3] ? new Date( result[3]?.getTime() ) : null
            } catch (e) {
                log.error "Error while obtaining section details : " + e.stackTrace
                throw e
            }
            finally {
                try {
                   // sql?.close()
                } catch (SQLException se) { /* squash it*/
                    log.trace getClass().simpleName + " : Sql Statement is already closed, no need to close it."
                }
            }
        }
        return section
    }


    def fetchAllSectionMeetingTimeByCRNAndTermCode(Collection<String> crns, Collection<String> termCodes ) {
        String queryString = """ FROM GlobalUniqueIdentifier a, SectionMeetingTime c
                            WHERE   c.term in :termCodes
                            AND     c.courseReferenceNumber in :crns
                            AND     a.ldmName = 'instructional-events'
                            AND     a.domainId = c.id
                            """

        def queryResult = SectionMeetingTime.withSession { session ->
            def query = session.createQuery(queryString)
            query.setParameterList('termCodes', termCodes)
            query.setParameterList('crns', crns)

            return query.list()
        }
        List entities = []
        queryResult.each {
            Map entitiesMap = [sectionMeetingTime : it[1], globalUniqueIdentifier : it[0]]
            entities << entitiesMap
        }

        return entities
    }
    public List fetchByTermAndCourseReferenceNumber(term, crn){
        def meetingData = SectionMeetingTime.fetchByTermAndCourseReferenceNumber( term, crn)
        return  meetingData
    }
}
