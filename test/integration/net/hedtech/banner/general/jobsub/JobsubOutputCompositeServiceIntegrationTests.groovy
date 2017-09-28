/*********************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.jobsub

import org.junit.After
import org.junit.Before
import org.junit.Test
import net.hedtech.banner.testing.BaseIntegrationTestCase
import net.hedtech.banner.exceptions.ApplicationException
import java.text.SimpleDateFormat

class JobsubOutputCompositeServiceIntegrationTests  extends BaseIntegrationTestCase {

    //Valid test data (For success tests)

    def i_success_printer = "saas1"
    def jobsubOutputCompositeService
    def date = new SimpleDateFormat('MM/dd/yyyy')


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
    void testFetchPendingPrintByPrinter(){
        def jobsubExternalPrinterCheck = JobsubExternalPrinter.findAll()
        def printJobs = jobsubExternalPrinterCheck.findAll{it.printer == i_success_printer && it.printDate == null}
        assertTrue printJobs.size() > 0

        def map = [pluralizedResourceName: "jobsub-pending-print", printer: i_success_printer]
        def jobsubExternalPrinters = jobsubOutputCompositeService.list(map)
        assertTrue jobsubExternalPrinters.size() > 0
        jobsubExternalPrinters.each {
            assertEquals it.printer, i_success_printer
            assertNull it.printDate
        }
    }

    @Test
    void testFetchPendingPrintByPrinterCount(){
        def jobsubExternalPrinterCheck = JobsubExternalPrinter.findAll()
        def printJobs = jobsubExternalPrinterCheck.findAll{it.printer == i_success_printer && it.printDate == null}
        assertTrue printJobs.size() > 0

        def map = [pluralizedResourceName: "jobsub-pending-print", printer: i_success_printer]
        def jobsubExternalPrinters = jobsubOutputCompositeService.count(map)
        assertEquals jobsubExternalPrinters  , printJobs.size()

    }

    @Test
    void testShowJobsubSavedOutput(){
        def jobsubExternalPrinterCheck = JobsubExternalPrinter.findAll()
        def printJobs = jobsubExternalPrinterCheck.findAll{it.printer == i_success_printer && it.printDate == null}
        assertTrue printJobs.size() > 0
        def saveId = printJobs[0].id

        def jobsubSavedOutput  = jobsubOutputCompositeService.show([pluralizedResourceName : "jobsub-pending-print",
                                                                        id: saveId])
        assertNotNull jobsubSavedOutput
        assertTrue jobsubSavedOutput instanceof JobsubSavedOutput
        assertEquals jobsubSavedOutput.id, saveId
        assertNotNull jobsubSavedOutput.jobsubOutput
        assertTrue jobsubSavedOutput.jobsubOutput instanceof FileOutputStream

    }

    @Test
    void testUpdateJobsubSavedOutput(){
        def jobsubExternalPrinterCheck = JobsubExternalPrinter.findAll()
        def printJobs = jobsubExternalPrinterCheck.findAll{it.printer == i_success_printer && it.printDate == null}
        assertTrue printJobs.size() > 0
        def saveId = printJobs[0].id
        assertNull printJobs[0].printDate
        def printDate = new Date()

        jobsubOutputCompositeService.update([job: "SARADMS"], [pluralizedResourceName : "jobsub-pending-print",
                                                                    id: saveId]  )
        def updatedPrint = JobsubSavedOutput.get(saveId)
        assertNotNull updatedPrint
        assertEquals date.format(updatedPrint.printDate), date.format(printDate)

    }


    @Test
    void testFetchJobOutputFile() {
        // get the seed data record
        def jobsubExternalPrinterCheck = JobsubExternalPrinter.findAll()
        def printJobs = jobsubExternalPrinterCheck.findAll{it.printer == i_success_printer && it.printDate == null}
        assertTrue printJobs.size() > 0
        def saveId = printJobs[0].id
        def saveVersion = printJobs[0].version

        def jobFile = jobsubOutputCompositeService.fetchJobOutputFile(saveId )
        assertNotNull jobFile
        assertTrue jobFile instanceof FileOutputStream

    }

}
