/*********************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.overall

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.apache.log4j.Logger
import org.hibernate.annotations.Type

import javax.persistence.*

/**
 * Represents a Directory Profile preference for a particular Directory Profile item for a given user
 */
@EqualsAndHashCode(includeFields = true)
@ToString(includeNames = true, includeFields = true)
@Entity
@Table(name = "GV_GORDPRF")
@NamedQueries(value = [
        @NamedQuery(name = "DirectoryProfilePreference.fetchByPidm",
                query = """FROM DirectoryProfilePreference a
                            WHERE a.pidm = :pidm""")
])
class DirectoryProfilePreference implements Serializable {

    private static final log = Logger.getLogger(DirectoryProfilePreference.class)

    /**
     * Surrogate ID for GORDPRF
     */
    @Id
    @Column(name = "GORDPRF_SURROGATE_ID")
    @SequenceGenerator(name = "GORDPRF_SEQ_GEN", allocationSize = 1, sequenceName = "GORDPRF_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GORDPRF_SEQ_GEN")
    Long id

    /**
     * Optimistic lock token for GORDPRF
     */
    @Version
    @Column(name = "GORDPRF_VERSION")
    Long version

    /**
     * The pidm of the entity who owns this Directory Profile information
     */
    @Column(name = "GORDPRF_PIDM")
    Integer pidm

    /**
     * The Directory Profile item code
     */
    @Column(name = "GORDPRF_DIRO_CODE")
    String code

    /**
     * The Directory Profile indicator showing whether this item should be displayed
     */
    @Type(type = "yes_no")
    @Column(name = "GORDPRF_DISP_DIRECTORY_IND")
    Boolean displayInDirectoryIndicator

    /**
     * The user id when the row was added or modified.
     */
    @Column(name = "GORDPRF_USER_ID")
    String lastModifiedBy

    /**
     * The date on which the row was added or modified.
     */
    @Column(name = "GORDPRF_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * DATA ORIGIN: Source system that created or updated the row
     */
    @Column(name = "GORDPRF_DATA_ORIGIN")
    String dataOrigin


    static constraints = {
        pidm(nullable: false, min: -99999999, max: 99999999)
        code(nullable: false, maxSize: 8)
        displayInDirectoryIndicator(nullable: false, maxSize: 1)
        lastModifiedBy(nullable: true, maxSize: 30)
        lastModified(nullable: true)
        dataOrigin(nullable: true, maxSize: 30)

    }

    //Read Only fields that should be protected against update
    public static readonlyProperties = ['pidm']

}
