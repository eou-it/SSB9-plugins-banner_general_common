/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.general.system.*

import javax.persistence.*

/**
 * Visa International Information Table
 */
@Entity
@Table(name = "GOBINTL")
@NamedQueries(value = [
        @NamedQuery(name = "VisaInternationalInformation.fetchAllByPidmInList",
                query = """FROM VisaInternationalInformation a
        WHERE a.passportId IS NOT NULL AND a.pidm IN :pidms""")
])
class VisaInternationalInformation implements Serializable {

    /**
     * Surrogate ID for GOBINTL
     */
    @Id
    @Column(name = "GOBINTL_SURROGATE_ID")
    @SequenceGenerator(name = "GOBINTL_SEQ_GEN", allocationSize = 1, sequenceName = "GOBINTL_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GOBINTL_SEQ_GEN")
    Long id

    /**
     * Optimistic lock token for GOBINTL
     */
    @Version
    @Column(name = "GOBINTL_VERSION")
    Long version

    /**
     * PERSONAL ID NUMBER: This field indicates Personal Identification Number.
     */
    @Column(name = "GOBINTL_PIDM")
    Integer pidm

    /**
     * SPOUSE INDICATOR: This field indicates whether the Spouse Accompanied the person to the Country.
     */
    @Column(name = "GOBINTL_SPOUSE_IND")
    String spouseIndicator

    /**
     * SIGNATURE INDICATOR: This field indicates Signature Available for Funds.
     */
    @Column(name = "GOBINTL_SIGNATURE_IND")
    String signatureIndicator

    /**
     * PASSPORT NUMBER: This field indicates Passport Identification Number.
     */
    @Column(name = "GOBINTL_PASSPORT_ID")
    String passportId

    /**
     * ISSUING NATION CODE: This field indicates the Country Code of the nation issuing the passport.
     */
    @Column(name = "GOBINTL_NATN_CODE_ISSUE")
    String nationIssue

    /**
     * PASSPORT EXPIRATION DATE: This field indicates the date the passport expires.
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "GOBINTL_PASSPORT_EXP_DATE")
    Date passportExpenditureDate

    /**
     * I94 STATUS: This field indicates whether the passport status is for Admission or Departure.
     */
    @Column(name = "GOBINTL_I94_STATUS")
    String i94Status

    /**
     * I94 EXPIRATION DATE: This field indicates the date the Admission or Departure expires.
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "GOBINTL_I94_DATE")
    Date i94Date

    /**
     * REGISTRATION NUMBER: This field indicates a unique Passport Registration Number.
     */
    @Column(name = "GOBINTL_REG_NUMBER")
    String registrationNumber

    /**
     * DURATION: This field indicates whether the Admission or Departure is for the Duration of the Stay.
     */
    @Column(name = "GOBINTL_DURATION")
    String duration

    /**
     * CERTIFICATION NUMBER: This field indicates the Passport Certification Number.
     */
    @Column(name = "GOBINTL_CERT_NUMBER")
    String certificateNumber

    /**
     * CERTIFICATION ISSUE DATE: This field indicates the date the Passport Certification was issued.
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "GOBINTL_CERT_DATE_ISSUE")
    Date certificateDateIssue

    /**
     * CERTIFICATION RECEIPT DATE: This field indicates the date the Passport Certification was received.
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "GOBINTL_CERT_DATE_RECEIPT")
    Date certificateDateReceipt

    /**
     * BIRTH NATION CODE: This field indicates the Country Code of the nation of the person's Birth.
     */
    @Column(name = "GOBINTL_NATN_CODE_BIRTH")
    String nationBirth

    /**
     * LEGAL NATION CODE: This field indicates the Country Code of the nation of the person's Citizenship.
     */
    @Column(name = "GOBINTL_NATN_CODE_LEGAL")
    String nationLegal

    /**
     * FOREIGN TAX ID: This field indicates a unique identification number for tax purposes: such as SSN.
     */
    @Column(name = "GOBINTL_FOREIGN_SSN")
    String foreignSsn

    /**
     * CHILD NUMBER: This field indicates the Number of Children Accompaning the person to the Country.
     */
    @Column(name = "GOBINTL_CHILD_NUMBER")
    Integer childNumber

    /**
     * ACTIVITY DATE: The date that the information for the row was inserted or updated in the GOBINTL table.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "GOBINTL_ACTIVITY_DATE")
    Date lastModified

    /**
     * USER IDENTIFICATION: The unique identification of the user who changed the record.
     */
    @Column(name = "GOBINTL_USER_ID")
    String lastModifiedBy

    /**
     * Data origin column for GOBINTL
     */
    @Column(name = "GOBINTL_DATA_ORIGIN")
    String dataOrigin

    /**
     * Foreign Key : FKV_GOBINTL_INV_GTVCELG_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "GOBINTL_CELG_CODE", referencedColumnName = "GTVCELG_CODE")
    ])
    CertificationOfEligibility certificationOfEligibility

    /**
     * Foreign Key : FKV_GOBINTL_INV_STVADMR_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "GOBINTL_ADMR_CODE", referencedColumnName = "STVADMR_CODE")
    ])
    AdmissionRequest admissionRequest

    /**
     * Foreign Key : FKV_GOBINTL_INV_STVLANG_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "GOBINTL_LANG_CODE", referencedColumnName = "STVLANG_CODE")
    ])
    Language language

    /**
     * Foreign Key : FKV_GOBINTL_INV_STVSPON_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "GOBINTL_SPON_CODE", referencedColumnName = "STVSPON_CODE")
    ])
    InternationalSponsor internationalSponsor

    /**
     * Foreign Key : FKV_GOBINTL_INV_STVEMPT_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "GOBINTL_EMPT_CODE", referencedColumnName = "STVEMPT_CODE")
    ])
    EmploymentType employmentType


    public String toString() {
        """VisaInternationalInformation[
					id=$id,
					version=$version,
					pidm=$pidm,
					spouseIndicator=$spouseIndicator,
					signatureIndicator=$signatureIndicator,
					passportId=$passportId,
					nationIssue=$nationIssue,
					passportExpenditureDate=$passportExpenditureDate,
					i94Status=$i94Status,
					i94Date=$i94Date,
					registrationNumber=$registrationNumber,
					duration=$duration,
					certificateNumber=$certificateNumber,
					certificateDateIssue=$certificateDateIssue,
					certificateDateReceipt=$certificateDateReceipt,
					nationBirth=$nationBirth,
					nationLegal=$nationLegal,
					foreignSsn=$foreignSsn,
					childNumber=$childNumber,
					lastModified=$lastModified,
					lastModifiedBy=$lastModifiedBy,
					dataOrigin=$dataOrigin,
					certificationOfEligibility=$certificationOfEligibility,
					admissionRequest=$admissionRequest,
					language=$language,
					internationalSponsor=$internationalSponsor,
					employmentType=$employmentType]"""
    }


    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof VisaInternationalInformation)) return false
        VisaInternationalInformation that = (VisaInternationalInformation) o
        if (id != that.id) return false
        if (version != that.version) return false
        if (pidm != that.pidm) return false
        if (spouseIndicator != that.spouseIndicator) return false
        if (signatureIndicator != that.signatureIndicator) return false
        if (passportId != that.passportId) return false
        if (nationIssue != that.nationIssue) return false
        if (passportExpenditureDate != that.passportExpenditureDate) return false
        if (i94Status != that.i94Status) return false
        if (i94Date != that.i94Date) return false
        if (registrationNumber != that.registrationNumber) return false
        if (duration != that.duration) return false
        if (certificateNumber != that.certificateNumber) return false
        if (certificateDateIssue != that.certificateDateIssue) return false
        if (certificateDateReceipt != that.certificateDateReceipt) return false
        if (nationBirth != that.nationBirth) return false
        if (nationLegal != that.nationLegal) return false
        if (foreignSsn != that.foreignSsn) return false
        if (childNumber != that.childNumber) return false
        if (lastModified != that.lastModified) return false
        if (lastModifiedBy != that.lastModifiedBy) return false
        if (dataOrigin != that.dataOrigin) return false
        if (certificationOfEligibility != that.certificationOfEligibility) return false
        if (admissionRequest != that.admissionRequest) return false
        if (language != that.language) return false
        if (internationalSponsor != that.internationalSponsor) return false
        if (employmentType != that.employmentType) return false
        return true
    }


    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (pidm != null ? pidm.hashCode() : 0)
        result = 31 * result + (spouseIndicator != null ? spouseIndicator.hashCode() : 0)
        result = 31 * result + (signatureIndicator != null ? signatureIndicator.hashCode() : 0)
        result = 31 * result + (passportId != null ? passportId.hashCode() : 0)
        result = 31 * result + (nationIssue != null ? nationIssue.hashCode() : 0)
        result = 31 * result + (passportExpenditureDate != null ? passportExpenditureDate.hashCode() : 0)
        result = 31 * result + (i94Status != null ? i94Status.hashCode() : 0)
        result = 31 * result + (i94Date != null ? i94Date.hashCode() : 0)
        result = 31 * result + (registrationNumber != null ? registrationNumber.hashCode() : 0)
        result = 31 * result + (duration != null ? duration.hashCode() : 0)
        result = 31 * result + (certificateNumber != null ? certificateNumber.hashCode() : 0)
        result = 31 * result + (certificateDateIssue != null ? certificateDateIssue.hashCode() : 0)
        result = 31 * result + (certificateDateReceipt != null ? certificateDateReceipt.hashCode() : 0)
        result = 31 * result + (nationBirth != null ? nationBirth.hashCode() : 0)
        result = 31 * result + (nationLegal != null ? nationLegal.hashCode() : 0)
        result = 31 * result + (foreignSsn != null ? foreignSsn.hashCode() : 0)
        result = 31 * result + (childNumber != null ? childNumber.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        result = 31 * result + (certificationOfEligibility != null ? certificationOfEligibility.hashCode() : 0)
        result = 31 * result + (admissionRequest != null ? admissionRequest.hashCode() : 0)
        result = 31 * result + (language != null ? language.hashCode() : 0)
        result = 31 * result + (internationalSponsor != null ? internationalSponsor.hashCode() : 0)
        result = 31 * result + (employmentType != null ? employmentType.hashCode() : 0)
        return result
    }


    static constraints = {
        pidm(nullable: false, min: -99999999, max: 99999999)
        spouseIndicator(nullable: false, maxSize: 1, inList: ["Y", "T", "N"])
        signatureIndicator(nullable: false, maxSize: 1, inList: ["Y", "T", "N"])
        passportId(nullable: true, maxSize: 25)
        nationIssue(nullable: true, maxSize: 5)
        passportExpenditureDate(nullable: true)
        i94Status(nullable: true, maxSize: 3)
        i94Date(nullable: true)
        registrationNumber(nullable: true, maxSize: 11)
        duration(nullable: true, maxSize: 1)
        certificateNumber(nullable: true, maxSize: 11)
        certificateDateIssue(nullable: true)
        certificateDateReceipt(nullable: true)
        nationBirth(nullable: true, maxSize: 5)
        nationLegal(nullable: true, maxSize: 5)
        foreignSsn(nullable: true, maxSize: 15)
        childNumber(nullable: true, min: 0, max: 99)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        certificationOfEligibility(nullable: true)
        admissionRequest(nullable: true)
        language(nullable: true)
        internationalSponsor(nullable: true)
        employmentType(nullable: true)
    }

    //Read Only fields that should be protected against update
    public static readonlyProperties = ['pidm']
}
