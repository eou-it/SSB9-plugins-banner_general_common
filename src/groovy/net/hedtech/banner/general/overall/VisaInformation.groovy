/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.general.system.PortOfEntry
import net.hedtech.banner.general.system.VisaIssuingAuthority
import net.hedtech.banner.general.system.VisaType
import net.hedtech.banner.query.DynamicFinder
import org.hibernate.annotations.Type

import javax.persistence.*

/**
 * Visa Information Table
 */
@NamedQueries(value = [
@NamedQuery(
        name = "VisaInformation.fetchNextSequenceNumber",
        query = """SELECT NVL(MAX(a.sequenceNumber),0) + 1
                     FROM VisaInformation a
                    WHERE a.pidm = :pidm"""),
@NamedQuery(
        name = "VisaInformation.overlappingExpireDateExists",
        query = """SELECT 'Y'
                     FROM VisaInformation a
                    WHERE a.pidm            = :pidm
                      AND a.sequenceNumber <> :sequenceNumber
                      AND (   (:visaStartDate  BETWEEN a.visaStartDate AND a.visaExpireDate)
                           OR (:visaExpireDate BETWEEN a.visaStartDate AND a.visaExpireDate))"""),
@NamedQuery(name = "VisaInformation.fetchByPidmListAndDateCompare",
query = """FROM VisaInformation a
          WHERE a.pidm IN :pidm
            AND TRUNC(:compareDate) BETWEEN TRUNC(a.visaStartDate) AND TRUNC(a.visaExpireDate)""")
])

@Entity
@Table(name = "GV_GORVISA")
class VisaInformation implements Serializable {

    /**
     * Surrogate ID for GORVISA
     */
    @Id
    @Column(name = "GORVISA_SURROGATE_ID")
    @SequenceGenerator(name = "GORVISA_SEQ_GEN", allocationSize = 1, sequenceName = "GORVISA_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GORVISA_SEQ_GEN")
    Long id

    /**
     * Optimistic lock token for GORVISA
     */
    @Version
    @Column(name = "GORVISA_VERSION")
    Long version

    /**
     * PIDM:  Internal Identification number.
     */
    @Column(name = "GORVISA_PIDM")
    Integer pidm

    /**
     * SEQUENCE NUMBER: A Record Sequence Number.
     */
    @Column(name = "GORVISA_SEQ_NO")
    Integer sequenceNumber

    /**
     * VISA NUMBER: A unique identification number assigned to a Visa.
     */
    @Column(name = "GORVISA_VISA_NUMBER")
    String visaNumber

    /**
     * ISSUING NATION CODE: The Country Code of the nation issuing the visa.
     */
    @Column(name = "GORVISA_NATN_CODE_ISSUE")
    String nationIssue

    /**
     * VISA START DATE: The date the visa begins.
     */
    @Column(name = "GORVISA_VISA_START_DATE")
    @Temporal(TemporalType.DATE)
    Date visaStartDate

    /**
     * VISA EXPIRE DATE: The date the visa expires.
     */
    @Column(name = "GORVISA_VISA_EXPIRE_DATE")
    @Temporal(TemporalType.DATE)
    Date visaExpireDate

    /**
     * ENTRY INDICATOR: This field indicates whether the visa is for entry into the country.
     */
    @Type(type = "yes_no")
    @Column(name = "GORVISA_ENTRY_IND")
    Boolean entryIndicator

    /**
     * VISA REQUEST DATE: The date the Visa was requested.
     */
    @Column(name = "GORVISA_VISA_REQ_DATE")
    @Temporal(TemporalType.DATE)
    Date visaRequiredDate

    /**
     * VISA ISSUE DATE: The date the Visa was Issued.
     */
    @Column(name = "GORVISA_VISA_ISSUE_DATE")
    @Temporal(TemporalType.DATE)
    Date visaIssueDate

    /**
     * NUMBER OF ENTRIES: The Number of Entries into the country.  Valid values:  M, S and number 1-99.
     */
    @Column(name = "GORVISA_NO_ENTRIES")
    String numberEntries

    /**
     * ACTIVITY DATE:  Date of last activity (insert or update) on the record.
     */
    @Column(name = "GORVISA_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * USER ID: The Oracle ID of the user who changed the record.
     */
    @Column(name = "GORVISA_USER_ID")
    String lastModifiedBy

    /**
     * DATA ORIGIN: Source system that created or updated the row.
     */
    @Column(name = "GORVISA_DATA_ORIGIN")
    String dataOrigin

    /**
     * Foreign Key : FKV_GORVISA_INV_STVVTYP_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "GORVISA_VTYP_CODE", referencedColumnName = "STVVTYP_CODE")
    ])
    VisaType visaType

    /**
     * Foreign Key : FKV_GORVISA_INV_GTVVISS_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "GORVISA_VISS_CODE", referencedColumnName = "GTVVISS_CODE")
    ])
    VisaIssuingAuthority visaIssuingAuthority

    /**
     * Foreign Key : FKV_GORVISA_INV_STVPENT_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "GORVISA_PENT_CODE", referencedColumnName = "STVPENT_CODE")
    ])
    PortOfEntry portOfEntry

    /*
     * Temporary number; used by child records; needed when both parent and child records are created by UI in one transaction.
     */
    @Transient
    Integer tempSeqNo = 0


    public String toString() {
        """VisaInformation[
					id=$id,
					version=$version,
					pidm=$pidm,
					sequenceNumber=$sequenceNumber,
					visaNumber=$visaNumber,
					nationIssue=$nationIssue,
					visaStartDate=$visaStartDate,
					visaExpireDate=$visaExpireDate,
					entryIndicator=$entryIndicator,
					visaRequiredDate=$visaRequiredDate,
					visaIssueDate=$visaIssueDate,
					numberEntries=$numberEntries,
					lastModified=$lastModified,
					lastModifiedBy=$lastModifiedBy,
					dataOrigin=$dataOrigin,
					visaType=$visaType,
					visaIssuingAuthority=$visaIssuingAuthority,
					portOfEntry=$portOfEntry]"""
    }


    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof VisaInformation)) return false
        VisaInformation that = (VisaInformation) o
        if (id != that.id) return false
        if (version != that.version) return false
        if (pidm != that.pidm) return false
        if (sequenceNumber != that.sequenceNumber) return false
        if (visaNumber != that.visaNumber) return false
        if (nationIssue != that.nationIssue) return false
        if (visaStartDate != that.visaStartDate) return false
        if (visaExpireDate != that.visaExpireDate) return false
        if (entryIndicator != that.entryIndicator) return false
        if (visaRequiredDate != that.visaRequiredDate) return false
        if (visaIssueDate != that.visaIssueDate) return false
        if (numberEntries != that.numberEntries) return false
        if (lastModified != that.lastModified) return false
        if (lastModifiedBy != that.lastModifiedBy) return false
        if (dataOrigin != that.dataOrigin) return false
        if (visaType != that.visaType) return false
        if (visaIssuingAuthority != that.visaIssuingAuthority) return false
        if (portOfEntry != that.portOfEntry) return false
        return true
    }


    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (pidm != null ? pidm.hashCode() : 0)
        result = 31 * result + (sequenceNumber != null ? sequenceNumber.hashCode() : 0)
        result = 31 * result + (visaNumber != null ? visaNumber.hashCode() : 0)
        result = 31 * result + (nationIssue != null ? nationIssue.hashCode() : 0)
        result = 31 * result + (visaStartDate != null ? visaStartDate.hashCode() : 0)
        result = 31 * result + (visaExpireDate != null ? visaExpireDate.hashCode() : 0)
        result = 31 * result + (entryIndicator != null ? entryIndicator.hashCode() : 0)
        result = 31 * result + (visaRequiredDate != null ? visaRequiredDate.hashCode() : 0)
        result = 31 * result + (visaIssueDate != null ? visaIssueDate.hashCode() : 0)
        result = 31 * result + (numberEntries != null ? numberEntries.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        result = 31 * result + (visaType != null ? visaType.hashCode() : 0)
        result = 31 * result + (visaIssuingAuthority != null ? visaIssuingAuthority.hashCode() : 0)
        result = 31 * result + (portOfEntry != null ? portOfEntry.hashCode() : 0)
        return result
    }


    static constraints = {
        pidm(nullable: false, min: -99999999, max: 99999999)
        sequenceNumber(nullable: false, min: -999, max: 999)
        visaNumber(nullable: true, maxSize: 18)
        nationIssue(nullable: true, maxSize: 5)
        visaStartDate(nullable: true)
        visaExpireDate(nullable: true)
        entryIndicator(nullable: false)
        visaRequiredDate(nullable: true)
        visaIssueDate(nullable: true)
        numberEntries(nullable: true, maxSize: 2)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        visaType(nullable: false)
        visaIssuingAuthority(nullable: true)
        portOfEntry(nullable: true)
    }

    //Read Only fields that should be protected against update
    public static readonlyProperties = ['pidm', 'sequenceNumber', 'visaType']


    static boolean overlappingExpireDateExists(def pidm, def sequenceNumber, def visaStartDate, def visaExpireDate) {
        def exists = VisaInformation.withSession { session ->
            session.getNamedQuery('VisaInformation.overlappingExpireDateExists')
                    .setInteger('pidm', pidm)
                    .setInteger('sequenceNumber', sequenceNumber)
                    .setDate('visaStartDate', visaStartDate)
                    .setDate('visaExpireDate', visaExpireDate)
                    .list()
        }
        return exists.size() > 0
    }


	static List<VisaInformation> fetchByPidmListAndDateCompare(List pidm, Date compareDate) {
		def visas = VisaInformation.withSession { session ->
			session.getNamedQuery('VisaInformation.fetchByPidmListAndDateCompare').setParameterList('pidm', pidm).setDate('compareDate', compareDate).list()
		}
		return visas
	}


    static Integer fetchNextSequenceNumber(Integer pidm) {
        def nextSequenceNumber = VisaInformation.withSession { session ->
            session.getNamedQuery('VisaInformation.fetchNextSequenceNumber').setInteger('pidm', pidm).list()
        }
        return nextSequenceNumber[0]
    }


    def static countAll(filterData) {
        finderByAll().count(filterData)
    }


    def static fetchSearch(filterData, pagingAndSortParams) {
        finderByAll().find(filterData, pagingAndSortParams)
    }


    def private static finderByAll = {
        def query = """ FROM VisaInformation a
	                   WHERE a.pidm = :pidm
                    """
        return new DynamicFinder(VisaInformation.class, query, "a")
    }
}
