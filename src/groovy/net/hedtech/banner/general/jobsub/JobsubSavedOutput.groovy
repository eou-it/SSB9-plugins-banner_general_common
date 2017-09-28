/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.jobsub

import javax.persistence.*
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import javax.persistence.NamedQueries
import javax.persistence.NamedQuery
import javax.persistence.Table
import java.sql.Blob

/**
 * Jobsubmission saved output
 */

@EqualsAndHashCode
@ToString

@NamedQueries(value = [
        @NamedQuery(name = "JobsubSavedOutput.fetchByJobAndOneUpNoAndFileName",
                query = """FROM JobsubSavedOutput a
                   WHERE a.job = :job
                   and a.oneUpNo = :oneUpNo
                   and a.fileName = :fileName""")])
@Entity
@Table(name = "GJRJLIS")
class JobsubSavedOutput implements Serializable {

    /**
     * Surrogate ID for GJRJLIS
     */
    @Id
    @Column(name = "GJRJLIS_SURROGATE_ID")
    @SequenceGenerator(name = "GJRJLIS_SEQ_GEN", allocationSize = 1, sequenceName = "GJRJLIS_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GJRJLIS_SEQ_GEN")
    Long id

    /**
     * Optimistic lock token for GJRJLIS
     */
    @Version
    @Column(name = "GJRJLIS_VERSION")
    Long version

    /**
     * CREATOR ID: The user id of the person who created this population selection.
     */
    @Column(name = "GJRJLIS_CREATE_USER_ID")
    String creatorId

    /**
     * ACTIVITY DATE: Date that record was created or last updated.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "GJRJLIS_ACTIVITY_DATE")
    Date lastModified

    /**
     * Last modified by column for GJRJLIS
     */
    @Column(name = "GJRJLIS_USER_ID")
    String lastModifiedBy

    /**
     * Data origin column for GJRJLIS
     */
    @Column(name = "GJRJLIS_DATA_ORIGIN")
    String dataOrigin

    /**
     *  JOB: Name of the job run from Banner Job Submission.
     */
    @Column(name = "GJRJLIS_JOB")
    String job

    /**
     *    ONE UP NUMBER: One up number to uniquely identify the job.
     */
    @Column(name = "GJRJLIS_ONE_UP_NO")
    Integer oneUpNo

    /**
     *  FILE NAME: Name of the report output created by Job that is uploaded into GJRJLIS table.
     */
    @Column(name = "GJRJLIS_FILE_NAME")
    String fileName

    /**
     * CREATE DATE: Date output record is created.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "GJRJLIS_CREATE_DATE")
    Date createDate

    /**
     * MIME CODE: File type, 1=PDF, 2=Plain Text.
     */
    @Column(name = "GJRJLIS_MIME_CDE")
    String mime

    /**
     * PRINT CODE: Printer code from GTVPRNT to identify print commands.
     */
    @Column(name = "GJRJLIS_PRNT_CODE")
    String printer

    /**
     * PRINT FORM: Special Print Form for printer.
     */
    @Column(name = "GJRJLIS_PRINT_FORM")
    String printForm

    /**
     * PRINT DATE: Date PDF is printed to local printer.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "GJRJLIS_PRINT_DATE")
    Date printDate

    @Transient
    FileOutputStream jobsubOutput

    /**
     * Fetch list of print jobs with blank print date for list of printers
     * @param printers
     * @return
     */
    public static def fetchByJobAndOneUpNoAndFileName(def job, def oneUpNo, def fileName) {

        def jobOutput = JobsubSavedOutput.withSession { session ->
            session.getNamedQuery('JobsubSavedOutput.fetchByJobAndOneUpNoAndFileName')
                    .setString('job', job).setInteger('oneUpNo', oneUpNo).setString('fileName', fileName).list()[0]
        }

        return jobOutput
    }


}

