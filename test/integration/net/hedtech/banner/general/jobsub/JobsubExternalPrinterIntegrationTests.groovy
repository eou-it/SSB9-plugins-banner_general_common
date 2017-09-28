/*********************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.jobsub

import org.junit.Before
import org.junit.Test
import org.junit.After

import grails.validation.ValidationException
import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import net.hedtech.banner.exceptions.ApplicationException
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException
import org.springframework.jdbc.UncategorizedSQLException

class JobsubExternalPrinterIntegrationTests extends BaseIntegrationTestCase {

    //Valid test data (For success tests)

    def i_success_printer = "saas1"
    def i_notsaas_printer = "notsaas"


    @Before
    public void setUp() {
        formContext = ['GUAGMNU'] // Since we are not testing a controller, we need to explicitly set this
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testFetchJobsubExternalPrinter() {
        def jobsubExternalPrinters = JobsubExternalPrinter.findAll()
        def jobsubExternalPrinter = jobsubExternalPrinters.find { it.job == "SARADMS" && it.oneUpNo == 6256 }
        assertNotNull jobsubExternalPrinter.id
        assertEquals jobsubExternalPrinter.printer, i_success_printer
        assertEquals jobsubExternalPrinter.fileName, "saradms_6256.lis"
        assertNull jobsubExternalPrinter.printDate
    }


    @Test
    void testFetchJobsubSaasOnlyPrinters() {
        def jobsubSavedOutput = JobsubSavedOutput.findAll()
        def jobsubNotSaas = jobsubSavedOutput.findAll { it.printer == i_notsaas_printer && it.printDate == null }
        assertTrue jobsubNotSaas.size() > 0

        def jobsubExternalPrinters = JobsubExternalPrinter.findAll()
        assertNull jobsubExternalPrinters.find { it.id == jobsubNotSaas[0].id }

    }


    @Test
    void testFetchByPrinter() {
        def jobsubExternalPrinterCheck = JobsubExternalPrinter.findAll()
        assertTrue jobsubExternalPrinterCheck.findAll { it.printer == i_success_printer }.size() > 0

        def jobsubExternalPrinters = JobsubExternalPrinter.fetchByPrinter(i_success_printer)
        assertTrue jobsubExternalPrinters.size() > 0

    }


    @Test
    void testFetchPendingPrintByPrinter() {
        def jobsubExternalPrinterCheck = JobsubExternalPrinter.findAll()
        assertTrue jobsubExternalPrinterCheck.findAll {
            it.printer == i_success_printer && it.printDate == null
        }.size() > 0

        def jobsubExternalPrinters = JobsubExternalPrinter.fetchPendingPrintByPrinter(i_success_printer)
        assertTrue jobsubExternalPrinters.size() > 0

    }


    @Test
    void testFetchPendingPrintLike() {
        def jobsubExternalPrinterCheck = JobsubExternalPrinter.findAll()
        def pending = jobsubExternalPrinterCheck.findAll { it.printDate == null }
        assertTrue pending.size() > 0

        def jobsubExternalPrinters = JobsubExternalPrinter.fetchPendingPrintByPrinter("%")
        assertEquals jobsubExternalPrinters.size(), pending.size()

    }


    @Test
    void testFetchPendingNoPrinter() {
        def jobsubExternalPrinterCheck = JobsubExternalPrinter.findAll()
        def pending = jobsubExternalPrinterCheck.findAll { it.printDate == null }
        assertTrue pending.size() > 0

        def jobsubExternalPrinters = JobsubExternalPrinter.fetchPendingPrintByPrinter("")
        assertEquals jobsubExternalPrinters.size(), pending.size()

    }


    @Test
    void testFetchPendingPrintByPrinterList() {
        def jobsubExternalPrinterCheck = JobsubExternalPrinter.findAll()
        assertTrue jobsubExternalPrinterCheck.findAll {
            it.printer == i_success_printer && it.printDate == null
        }.size() > 0

        def jobsubExternalPrinters = JobsubExternalPrinter.fetchPendingPrintByPrinter([i_success_printer, "sleepwake", "brothers"])
        assertTrue jobsubExternalPrinters.size() > 0
        assertTrue jobsubExternalPrinters.findAll { it.printer == i_success_printer && it.printDate == null }.size() > 0
    }


    @Test
    void testInsertNewPendingPrint() {
        def pendingPrint = new JobsubExternalPrinter(
                job: "SARADMS", oneUpNo: 1234, fileName: "saradms_1234.lis",
                createDate: new Date(), mime: "2",
                lastModified: new Date(), lastModifiedBy: "JOBSUB"
        )
        assertTrue pendingPrint instanceof JobsubExternalPrinter
        assertNotNull pendingPrint.job
        assertNotNull pendingPrint.fileName
        assertNotNull pendingPrint.mime
        assertNotNull pendingPrint.createDate
        pendingPrint.id = 99999
        assertNull JobsubExternalPrinter.get(99999)
        pendingPrint.version = 0
        assertNotNull pendingPrint.id
        assertNotNull pendingPrint.version

        shouldFail(UncategorizedSQLException) {
            pendingPrint.save(flush: true, failOnError: true)
        }


    }


    @Test
    void testUpdateNewPendingPrint() {
        def pendingPrint = JobsubExternalPrinter.findAll()[0]
        assertTrue pendingPrint instanceof JobsubExternalPrinter
        assertTrue pendingPrint.job != "TESTAPP"
        pendingPrint.job = "TESTAPP"

        shouldFail(UncategorizedSQLException) {
            pendingPrint.save(flush: true, failOnError: true)
        }

    }


    @Test
    void testDeleteNewPendingPrint() {
        def pendingPrint = JobsubExternalPrinter.findAll()[0]
        assertTrue pendingPrint instanceof JobsubExternalPrinter

        shouldFail(UncategorizedSQLException) {
            pendingPrint.delete(flush: true, failOnError: true)
        }

    }

}