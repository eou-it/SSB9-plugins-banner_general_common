/*********************************************************************************
  Copyright 2010-2013 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall

import javax.persistence.*

/**
 * Section Meeting Time Conflict View
 *
 */
@Entity
@Table(name = "SVQ_SSRMEET_TIMECONFLICT")
@NamedQueries(value = [
@NamedQuery(name = "SectionMeetingTimeConflictView.fetchByTermAndCourseReferenceNumber",
        query = """FROM SectionMeetingTimeConflictView a
		                WHERE a.term = :term
		                AND a.courseReferenceNumber = :courseReferenceNumber
		                and a.courseReferenceNumberConflict = :courseReferenceNumberConflict
		                """)
])
class SectionMeetingTimeConflictView {

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
     * This field is not displayed on the form (page 0).  It defines the Course Reference Number for the course section for which you are creating meeting times
     */
    @Column(name = "SSRMEET_CRN_CONFLICT")
    String courseReferenceNumberConflict

    /**
     * Foreign Key : FKV_SSRMEET_INV_STVTERM_CODE
     * This field is not displayed on the form (page 0).  It defines the term for which you are creating meeting
     * times for the course section.  It is based on the Key Block Term.
     * The term normally has a many to one with stvterm, but the term is filled with the word
     * EVENT if this entity is from the general Event module
     */
    @Column(name = "SSRMEET_TERM_CODE")
    String term




    public String toString() {
        """SectionMeetingTimeConflictView[
                   id=$id,
                   version=$version
                   term=$term,
                   courseReferenceNumber=$courseReferenceNumber ,
                   courseReferenceNumberConflict=$courseReferenceNumberConflict
                   ]"""
    }

    /**
     * This fetchBy is used to retrieve all meeting times for a given term and crn.
     * A NamedQuery is required because there are multiple fields in the order by.
     */
    public static List fetchByTermAndCourseReferenceNumber(String term,
                                                           String courseReferenceNumber,
                                                           String courseReferenceNumberConflict) {
        def sectionMeetingTimes = []
        SectionMeetingTimeConflictView.withSession { session ->
            sectionMeetingTimes = session.getNamedQuery(
                    'SectionMeetingTimeConflictView.fetchByTermAndCourseReferenceNumber')
                    .setString('term', term).setString('courseReferenceNumber', courseReferenceNumber)
                    .setString('courseReferenceNumberConflict', courseReferenceNumberConflict).list()
        }
        return sectionMeetingTimes
    }


}
