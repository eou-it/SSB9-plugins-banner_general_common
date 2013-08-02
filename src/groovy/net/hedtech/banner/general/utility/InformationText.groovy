/*********************************************************************************
 Copyright 2010-2013 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.utility

import net.hedtech.banner.query.DynamicFinder

import javax.persistence.*

/**
 * Information Text table.
 */
@Entity
@Table(name = "GURINFO")
class InformationText implements Serializable {

    @Id
    @Column(name = "GURINFO_SURROGATE_ID")
    @SequenceGenerator(name = "GURINFO_SEQ_GEN", allocationSize = 1, sequenceName = "GURINFO_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GURINFO_SEQ_GEN")
    Long id

    /**
     * PAGE NAME: Name of associated page.
     */
    @Column(name = "GURINFO_PAGE_NAME")
    String pageName

    /**
     * LABEL: Short label, used to select which set of text items to print for a page.
     */
    @Column(name = "GURINFO_LABEL")
    String label

    /**
     * TEXT TYPE: Type of text.    Types - Notification, tooltip, Panel, Header
     */
    @Column(name = "GURINFO_TEXT_TYPE")
    String textType

    /**
     * SEQUENCE NUMBER : Sequence number for this text item.
     */
    @Column(name = "GURINFO_SEQUENCE_NUMBER")
    Integer sequenceNumber

    /**
     * ROLE CODE: Role associated with the text.
     */
    @Column(name = "GURINFO_ROLE_CODE")
    String persona

    /**
     * TEXT: Text to be displayed on web page when this item is selected.
     */
    @Column(name = "GURINFO_TEXT")
    String text

    /**
     * LOCALE: Locale of the text
     */
    @Column(name = "GURINFO_LOCALE")
    String locale

    /**
     * COMMENT: Comment about this text item.
     */
    @Column(name = "GURINFO_COMMENT")
    String comment

    /**
     * START DATE: The date from when the text should be displayed.
     */
    @Column(name = "GURINFO_START_DATE")
    @Temporal(TemporalType.DATE)
    Date startDate

    /**
     * END DATE: The date until when the text should be displayed.
     */
    @Column(name = "GURINFO_END_DATE")
    @Temporal(TemporalType.DATE)
    Date endDate

    /**
     * SOURCE INDICATOR : Source Indicator: This field indicates if the row is (B)aseline or (L)ocal. The default value is B.
     */
    @Column(name = "GURINFO_SOURCE_INDICATOR")
    String sourceIndicator = 'B'

    /**
     * DATA ORIGIN: Source system that created or updated the data.
     */
    @Column(name = "GURINFO_DATA_ORIGIN")
    String dataOrigin

    /**
     * ACTIVITY DATE: The date that information in this record was entered or last updated.
     */
    @Column(name = "GURINFO_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * USER ID: The user ID of the person who inserted or last updated this record.
     */
    @Column(name = "GURINFO_USER_ID")
    String lastModifiedBy

    /**
     * VERSION: Optimistic lock token.
     */
    @Version
    @Column(name = "GURINFO_VERSION",precision = 19)
    Long version

    public String toString() {
        """InformationText[
					id=$id,
					pageName=$pageName,
                    label=$label,
					textType=$textType,
					sequenceNumber=$sequenceNumber,
					persona=$persona,
					text=$text,
					locale=$locale,
                    comment=$comment,
                    startDate=$startDate,
                    endDate=$endDate,
                    sourceIndicator=$sourceIndicator,
					lastModified=$lastModified,
					lastModifiedBy=$lastModifiedBy,
                    version=$version,
					dataOrigin=$dataOrigin]"""
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof InformationText)) return false
        InformationText that = (InformationText) o
        if (id != that.id) return false
        if (pageName != that.pageName) return false
        if (label != that.label) return false
        if (textType != that.textType) return false
        if (sequenceNumber != that.sequenceNumber) return false
        if (persona != that.persona) return false
        if (text != that.text) return false
        if (locale != that.locale) return false
        if (comment != that.comment) return false
        if (startDate != that.startDate) return false
        if (endDate != that.endDate) return false
        if (sourceIndicator != that.sourceIndicator) return false
        if (version != that.version) return false
        if (lastModified != that.lastModified) return false
        if (lastModifiedBy != that.lastModifiedBy) return false
        if (dataOrigin != that.dataOrigin) return false
        return true
    }

    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (pageName != null ? pageName.hashCode() : 0)
        result = 31 * result + (label != null ? label.hashCode() : 0)
        result = 31 * result + (textType != null ? textType.hashCode() : 0)
        result = 31 * result + (sequenceNumber != null ? sequenceNumber.hashCode() : 0)
        result = 31 * result + (persona != null ? persona.hashCode() : 0)
        result = 31 * result + (locale != null ? locale.hashCode() : 0)
        result = 31 * result + (text != null ? text.hashCode() : 0)
        result = 31 * result + (comment != null ? comment.hashCode() : 0)
        result = 31 * result + (startDate != null ? startDate.hashCode() : 0)
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0)
        result = 31 * result + (sourceIndicator != null ? sourceIndicator.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        return result
    }

    static constraints = {
        pageName(nullable: false, maxSize: 20)
        label(nullable: false, maxSize: 20)
        textType(nullable: false, maxSize: 20)
        sequenceNumber(nullable: false, min: 0, max: 99999)
        persona(nullable: false, maxSize: 30)
        text(nullable: false, maxSize: 4000)
        locale(nullable: false, maxSize: 20)
        comment(nullable: false, maxSize: 200)
        sourceIndicator(nullable: false, maxSize: 1, inList:['B','L'])
        startDate(nullable: true)
        endDate(nullable: true)
        dataOrigin(nullable: true, maxSize: 30)
        lastModifiedBy(nullable: true, maxSize: 30)
        lastModified(nullable: true)
    }

    def static countAll(filterData) {
        finderByAll().count(filterData)
    }


    def static fetchSearch(filterData, pagingAndSortParams) {
        def informationTexts = finderByAll().find(filterData, pagingAndSortParams)
        return informationTexts
    }


    def private static finderByAll = {
        def query = """FROM  InformationText a"""
        return new DynamicFinder(InformationText.class, query, "a")
    }
}
