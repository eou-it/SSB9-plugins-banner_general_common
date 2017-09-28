/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.jobsub

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.apache.commons.lang.StringUtils
import net.hedtech.banner.general.system.SystemUtility

import javax.persistence.*

/**
 * Jobsubmission saved output
 */

@EqualsAndHashCode
@ToString
@Entity
@Table(name = "GVQ_GJRJLIS")
@NamedQueries(value = [
        @NamedQuery(name = "JobsubExternalPrinter.fetchPendingPrintByPrinter",
                query = """FROM JobsubExternalPrinter a
                   WHERE a.printer like :printer and a.printDate is null
                   order by a.job, a.oneUpNo desc """),
        @NamedQuery(name = "JobsubExternalPrinter.fetchPendingPrintByPrinters",
                query = """FROM JobsubExternalPrinter a
                   WHERE a.printer in :printer and a.printDate is null
                   order by a.job, a.oneUpNo desc """),
        @NamedQuery(name = "JobsubExternalPrinter.fetchByPrinter",
                query = """FROM JobsubExternalPrinter a
                   WHERE a.printer = :printer  
                   order by a.job, a.oneUpNo desc """)])
class JobsubExternalPrinter  {

    /**
     * Surrogate ID for GJRJLIS
     */
    @Id
    @Column(name = "GJRJLIS_SURROGATE_ID")
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

    /**
     * PRINT COMMAND: Printer command
     */
    @Column(name = "GTVPRNT_PRINTER_COMMAND")
    String printerCommand



    public static def fetchByPrinter(String printer) {
        def externalPrinter = JobsubExternalPrinter.withSession { session ->
            session.getNamedQuery('JobsubExternalPrinter.fetchByPrinter')
                    .setString('printer', printer).list()
        }

        return externalPrinter
    }


    public static def fetchPendingPrintByPrinter(String printer) {

        if (StringUtils.isBlank(printer)) {
            printer = "%"
        } else if (!(printer =~ /%/)) {
            printer = "%" + printer + "%"
        } else printer = printer

        def externalPrinter = JobsubExternalPrinter.withSession { session ->
            session.getNamedQuery('JobsubExternalPrinter.fetchPendingPrintByPrinter')
                    .setString('printer', printer).list()
        }

        return externalPrinter
    }


    public static def fetchPendingPrintByPrinter(List printer) {
        def partitionList = SystemUtility.splitList(printer, 100)
        def externalPrinter = []

        partitionList.each { printerList ->
            def print   = JobsubExternalPrinter.withSession { session ->
                session.getNamedQuery('JobsubExternalPrinter.fetchPendingPrintByPrinters')
                        .setParameterList('printer', printerList).list()
            }
            externalPrinter.addAll(print)
        }

        return externalPrinter
    }
}