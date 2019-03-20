/*********************************************************************************
  Copyright 2010-2018 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall

import groovy.transform.EqualsAndHashCode
import net.hedtech.banner.general.system.AdmissionRequest
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.query.DynamicFinder

import javax.persistence.*

@NamedQueries(value = [
@NamedQuery(name = "PriorCollege.fetchByPidmAndAdmrExcludingID",
        query = """FROM PriorCollege a
          WHERE a.pidm = :pidm
            AND a.admissionRequest.code = :admissionRequest
            AND a.id <> :id"""),
@NamedQuery(name = "PriorCollege.fetchByPidmAndAdmr",
        query = """FROM PriorCollege a
          WHERE a.pidm = :pidm
            AND a.admissionRequest.code = :admissionRequest"""),
@NamedQuery(name = "PriorCollege.fetchByPidmList",
        query = """FROM PriorCollege a
          WHERE a.pidm IN :pidm""")
])
/**
 * Prior College Table
 */
@Entity
@Table(name = "SV_SORPCOL")
@EqualsAndHashCode(includeFields = true)
class PriorCollege implements Serializable {

    /**
     * Surrogate ID for SORPCOL
     */
    @Id
    @Column(name = "SORPCOL_SURROGATE_ID")
    @SequenceGenerator(name = "SORPCOL_SEQ_GEN", allocationSize = 1, sequenceName = "SORPCOL_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SORPCOL_SEQ_GEN")
    Long id

    /**
     * Optimistic lock token for SORPCOL
     */
    @Version
    @Column(name = "SORPCOL_VERSION")
    Long version

    /**
     * PIDM: Internal identification number of the student
     */
    @Column(name = "SORPCOL_PIDM")
    Integer pidm

    /**
     * TRANSCRIPT RECEIVED DATE: The date an academic transcript was received from the prior college
     */
    @Column(name = "SORPCOL_TRANS_RECV_DATE")
    @Temporal(TemporalType.DATE)
    Date transactionRecvDate

    /**
     * TRANSCRIPT REVIEWED DATE: The date an academic transcript was reviewed by the institution
     */
    @Column(name = "SORPCOL_TRANS_REV_DATE")
    @Temporal(TemporalType.DATE)
    Date transactionRevDate

    /**
     * OFFICIAL TRANSCRIPT INDICATOR: Designates the transcript received was an official transcript. Y denotes yes
     */
    @Column(name = "SORPCOL_OFFICIAL_TRANS")
    String officialTransaction

    /**
     * ACTIVITY DATE: Most current date record was created or changed
     */
    @Column(name = "SORPCOL_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * USER ID: The Oracle ID of the user who changed the record
     */
    @Column(name = "SORPCOL_USER_ID")
    String lastModifiedBy

    /**
     * DATA ORIGIN: Source system that created or updated the row
     */
    @Column(name = "SORPCOL_DATA_ORIGIN")
    String dataOrigin

    /**
     * Foreign Key : FK1_SORPCOL_INV_STVSBGI_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SORPCOL_SBGI_CODE", referencedColumnName = "STVSBGI_CODE")
    ])
    SourceAndBackgroundInstitution sourceAndBackgroundInstitution

    /**
     * Foreign Key : FK1_SORPCOL_INV_STVADMR_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SORPCOL_ADMR_CODE", referencedColumnName = "STVADMR_CODE")
    ])
    AdmissionRequest admissionRequest


    public String toString() {
        """PriorCollege[
					id=$id,
					version=$version,
					pidm=$pidm,
					transactionRecvDate=$transactionRecvDate,
					transactionRevDate=$transactionRevDate,
					officialTransaction=$officialTransaction,
					lastModified=$lastModified,
					lastModifiedBy=$lastModifiedBy,
					dataOrigin=$dataOrigin,
					sourceAndBackgroundInstitution=$sourceAndBackgroundInstitution,
					admissionRequest=$admissionRequest]"""
    }


    static constraints = {
        pidm(nullable: false, min: -99999999, max: 99999999)
        transactionRecvDate(nullable: true)
        transactionRevDate(nullable: true)
        officialTransaction(nullable: true, maxSize: 1)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        sourceAndBackgroundInstitution(nullable: false)
        admissionRequest(nullable: true)
    }

    //Read Only fields that should be protected against update
    public static readonlyProperties = ['pidm', 'sourceAndBackgroundInstitution']


    def static countAll(filterData) {
        finderByAll().count(filterData)
    }


    def static fetchSearch(filterData, pagingAndSortParams) {
        finderByAll().find(filterData, pagingAndSortParams)
    }


    def private static finderByAll = {
        def query = """ FROM PriorCollege a
	                   WHERE a.pidm = :pidm
	            	"""
        return new DynamicFinder(PriorCollege.class, query, "a")
    }


    static def fetchByPidmAndAdmrExcludingID(Map map)  {
        if (map.pidm) {
            if (map.id) {
                PriorCollege.withSession { session ->
                List priorColleges = session.getNamedQuery('PriorCollege.fetchByPidmAndAdmrExcludingID').setInteger('pidm', map?.pidm).setString('admissionRequest',map?.admissionRequest.code).setLong('id',map?.id).list()
                return [list:priorColleges]
                }
            }else {
                    PriorCollege.withSession { session ->
                    List priorColleges = session.getNamedQuery('PriorCollege.fetchByPidmAndAdmr').setInteger('pidm', map?.pidm).setString('admissionRequest',map?.admissionRequest.code).list()
                    return [list:priorColleges]
                }
            }
        } else {
            return null
        }
    }


    static List<PriorCollege> fetchByPidmList(List pidm) {
        def priorCollege = PriorCollege.withSession { session ->
            session.getNamedQuery('PriorCollege.fetchByPidmList').setParameterList('pidm', pidm).list()
        }

        return priorCollege
    }
}
