/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.communication.CommunicationErrorCode
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQueryVersion
import net.hedtech.banner.general.communication.population.selectionlist.CommunicationPopulationSelectionList
import org.hibernate.annotations.Type

import javax.persistence.*

@Entity
@EqualsAndHashCode
@ToString
@Table(name = "GCRPOPC")
@NamedQueries(value = [
        @NamedQuery(name = "CommunicationPopulationCalculation.fetchById",
                query = """ FROM CommunicationPopulationCalculation a
                    WHERE a.id = :id"""),
        @NamedQuery(name = "CommunicationPopulationCalculation.findByPopulationVersionId",
                query = """ FROM CommunicationPopulationCalculation calculation
                WHERE calculation.populationVersion.id = :populationVersionId order by calculation.createDate DESC"""),
        @NamedQuery(name = "CommunicationPopulationCalculation.findLatestByPopulationIdAndCreatedBy",
                query = """ FROM CommunicationPopulationCalculation calculation
                WHERE calculation.populationVersion.population.id = :populationId and calculation.createdBy = upper( :userId ) order by calculation.createDate DESC"""),
        @NamedQuery(name = "CommunicationPopulationCalculation.findLatestByPopulationVersionIdAndCreatedBy",
                query = """ FROM CommunicationPopulationCalculation calculation
                WHERE calculation.populationVersion.id = :populationVersionId and calculation.createdBy = upper( :userId ) order by calculation.createDate DESC""")
])
class CommunicationPopulationCalculation implements Serializable {

    /**
     * KEY: Generated unique key.
     */
    @Id
    @Column(name = "GCRPOPC_SURROGATE_ID")
    @SequenceGenerator(name = "GCRPOPC_SEQ_GEN", allocationSize = 1, sequenceName = "GCRPOPC_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GCRPOPC_SEQ_GEN")
    Long id

    /**
     * Foreign key reference to the Folder under which this template is organized.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "GCRPOPC_POPV_ID", referencedColumnName = "GCRPOPV_SURROGATE_ID")
    @org.hibernate.annotations.ForeignKey(name = "FK1_GCRPOPC_INV_GCRPOPV")
    CommunicationPopulationVersion populationVersion

    @ManyToOne(optional = false)
    @JoinColumn(name = "GCRPOPC_QRYV_ID", referencedColumnName = "GCRQRYV_SURROGATE_ID")
    @org.hibernate.annotations.ForeignKey(name = "FK1_GCRPOPC_INV_GCRQRYV")
    CommunicationPopulationQueryVersion populationQueryVersion

    /**
     * CREATE_DATE: The date the record was created.
     */
    @Column(name = "GCRPOPC_CREATE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date createDate

    /**
     * CREATOR_ID: The Oracle username of the user who created the record.
     */
    @Column(name = "GCRPOPC_CREATOR_ID")
    String createdBy

    /**
     * Returns true if the user requested the calculation; false if machine initiated in behalf of a scheduled group send.
     * In practice, createdBy with consist of a Banner Oracle ID with UI access; while false is done by the COMMMGR agent.
     */
    @Type(type = "yes_no")
    @Column(name = "GCRPOPC_USER_REQUESTED")
    Boolean userRequested

    /**
     * CALC_COUUNT: The count of persons calculated by the populationQuery.
     */
    @Column(name = "GCRPOPC_CALC_COUNT")
    Long calculatedCount

    /**
     * CALC_BY: ID of the user who last calculated the populationQuery.
     */
    @Column(name = "GCRPOPC_CALC_BY")
    String calculatedBy

    /**
     * Population Calculation status: SCHEDULED, PENDING_EXECUTION, ERROR, AVAILABLE
     */
    @Column(name = "GCRPOPC_STATUS")
    @Enumerated(EnumType.STRING)
    CommunicationPopulationCalculationStatus status

    /**
     * Foreign key reference to the population selection list.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "GCRPOPC_SLIS_ID", referencedColumnName = "GCRSLIS_SURROGATE_ID")
    @org.hibernate.annotations.ForeignKey(name = "FK1_GCRPOPC_INV_GCRSLIS")
    CommunicationPopulationSelectionList selectionList

    /**
     * The error code if calculation results in an error
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "GCRPOPC_ERROR_CODE")
    CommunicationErrorCode errorCode

    /**
     * The error text or stacktrace if calculation results in an error
     */
    @Column(name = "GCRPOPC_ERROR_TEXT")
    String errorText

    /**
     * JOB ID : UUID of the job for the population version calculation
     */
    @Column(name = "GCRPOPC_JOB_ID")
    String jobId

    /**
     * VERSION: Optimistic lock token
     */
    @Version
    @Column(name = "GCRPOPC_VERSION")
    Long version

    /**
     * ACTIVITY_DATE: Most current date record was created or changed.
     */
    @Column(name = "GCRPOPC_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified


    /**
     * USER_ID: Oracle User ID of the person who last inserted or last updated the data.
     */
    @Column(name = "GCRPOPC_USER_ID")
    String lastModifiedBy

    /**
     * DATA ORIGIN: Source system that created or updated the data.
     */
    @Column(name = "GCRPOPC_DATA_ORIGIN")
    String dataOrigin

    @Column(name = "GCRPOPC_VPDI_CODE")
    String mepCode

    static constraints = {
        populationVersion(nullable: false)
        populationQueryVersion(nullable: false)
        createDate(nullable: false)
        createdBy(nullable: false, maxSize: 30)
        calculatedCount(nullable: true)
        calculatedBy(nullable: false)
        status(nullable: false)
        selectionList(nullable: true)
        errorCode(nullable: true)
        errorText(nullable: true)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        mepCode(nullable: true)
        jobId(nullable: true)
        userRequested(nullable: false)
    }

    // Read Only fields that should be protected against update
    public static readonlyProperties = ['id']

    /**
     * Returns a list of population versions by the parent population id.
     * @param populationId the id of the owning population
     * @return a list of population versions
     */
    public static List findByPopulationVersionId( Long populationVersionId ) {
        def calculation = null
        CommunicationPopulationCalculation.withSession { session ->
            calculation = session.getNamedQuery('CommunicationPopulationCalculation.findByPopulationVersionId').setLong( 'populationVersionId', populationVersionId ).list()
        }
        return calculation
    }

    public static CommunicationPopulationCalculation findLatestByPopulationIdAndCreatedBy( Long populationId, String userId ) {
        CommunicationPopulationCalculation calculation = null
        CommunicationPopulationCalculation.withSession { session ->
            calculation = session.getNamedQuery('CommunicationPopulationCalculation.findLatestByPopulationIdAndCreatedBy').
                    setLong( 'populationId', populationId ).setString( 'userId', userId ).list()[0]
        }
        return calculation
    }

    public static CommunicationPopulationCalculation findLatestByPopulationVersionIdAndCreatedBy( Long populationVersionId, String userId ) {
        CommunicationPopulationCalculation calculation = null
        CommunicationPopulationCalculation.withSession { session ->
            calculation = session.getNamedQuery('CommunicationPopulationCalculation.findLatestByPopulationVersionIdAndCreatedBy').
                setLong( 'populationVersionId', populationVersionId ).setString( 'userId', userId ).list()[0]
        }
        return calculation
    }

    public static CommunicationPopulationCalculation fetchById(Long id) {
        def calculation
        CommunicationPopulationCalculation.withSession { session ->
            calculation = session.getNamedQuery('CommunicationPopulationCalculation.fetchById')
                    .setLong('id', id).list()[0]
        }
        return calculation
    }

}
