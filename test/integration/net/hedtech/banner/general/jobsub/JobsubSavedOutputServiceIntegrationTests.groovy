/*********************************************************************************
 Copyright 2010-2017 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.jobsub

import net.hedtech.banner.testing.BaseIntegrationTestCase
import net.hedtech.banner.exceptions.ApplicationException
import org.junit.After
import org.junit.Before
import org.junit.Test

class JobsubSavedOutputServiceIntegrationTests extends BaseIntegrationTestCase {

    //Valid test data (For success tests)
    def jobsubSavedOutputService

    def i_success_printer = "saas1"


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
    void testGetJobsubSavedOutput() {
        def jobsubSavedOutputs = JobsubSavedOutput.findAll()
        def jobsubSavedOutput = jobsubSavedOutputs.find { it.job == "SARADMS" && it.oneUpNo == 6256 }
        assertNotNull jobsubSavedOutput.id
        assertEquals jobsubSavedOutput.printer, i_success_printer
        assertEquals jobsubSavedOutput.fileName, "saradms_6256.lis"
        assertNull jobsubSavedOutput.printDate

        def jobGet = jobsubSavedOutputService.get(jobsubSavedOutput.id)
        assertNotNull jobGet
        assertEquals jobGet.fileName, jobsubSavedOutput.fileName
        assertEquals jobGet.job, jobsubSavedOutput.job
    }


    @Test
    void testJobsubSavedOutputInsert() {

        def jobsubSavedOutputs = JobsubSavedOutput.findAll()
        assertNull jobsubSavedOutputs.findAll { it.job == "STUREPT" && it.oneUpNo == 3333 }[0]

        def savedPrint = new JobsubSavedOutput(job: "STUREPT", oneUpNo: 3333,
                fileName: "sturpt_3333.lis", mime: "2", createDate: new Date(),
                creatorId: "JOBSUB")
        assertNotNull savedPrint
        try {
            jobsubSavedOutputService.create([domainModel: savedPrint])
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "unsupported.operation"
        }

    }


    @Test
    void testJobsubSavedOutputDelete() {
        def jobsubSavedOutputs = JobsubSavedOutput.findAll()
        def jobsubSavedOutput = jobsubSavedOutputs.find { it.job == "SARADMS" && it.oneUpNo == 6256 }
        assertNotNull jobsubSavedOutput.id
        try {
            jobsubSavedOutputService.delete([domainModel: jobsubSavedOutput])
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "unsupported.operation"
        }
    }


    @Test
    void testJobsubSavedOutputUpdatePrintDate() {
        def jobsubSavedOutputs = JobsubSavedOutput.findAll()
        def jobsubSavedOutput = jobsubSavedOutputs.find { it.job == "SARADMS" && it.oneUpNo == 6256 }
        assertNotNull jobsubSavedOutput.id
        assertNull jobsubSavedOutput.printDate
        def saveId = jobsubSavedOutput.id
        def printDateSave = new Date() - 4
        jobsubSavedOutput.printDate = printDateSave

        assertTrue   (!jobsubSavedOutput.isDirty("job") &&
                !jobsubSavedOutput.isDirty("printer") &&
                !jobsubSavedOutput.isDirty("printForm") &&
                !jobsubSavedOutput.isDirty("oneUpNo") &&
                !jobsubSavedOutput.isDirty("mime") &&
                !jobsubSavedOutput.isDirty("createDate") &&
                !jobsubSavedOutput.isDirty("fileName") &&
                !jobsubSavedOutput.isDirty("creatorId"))

        jobsubSavedOutputService.update([domainModel: jobsubSavedOutput])

        def savedJob = JobsubSavedOutput.get(saveId)
        assertNotNull savedJob.printDate
        assertEquals savedJob.printDate, printDateSave

    }

    @Test
    void testJobsubSavedOutputUpdateFileNameAndPrintDate() {
        def jobsubSavedOutputs = JobsubSavedOutput.findAll()
        def jobsubSavedOutput = jobsubSavedOutputs.find { it.job == "SARADMS" && it.oneUpNo == 6256 }
        assertNotNull jobsubSavedOutput.id
        def printDateSave = new Date() - 4
        jobsubSavedOutput.printDate = printDateSave
        assertTrue jobsubSavedOutput.fileName == "saradms_6256.lis"
        jobsubSavedOutput.fileName == "saradms_6255.lis"

        assertTrue jobsubSavedOutput.isDirty("printDate")

        assertFalse   (!jobsubSavedOutput.isDirty("job") &&
                !jobsubSavedOutput.isDirty("printer") &&
                !jobsubSavedOutput.isDirty("printForm") &&
                !jobsubSavedOutput.isDirty("oneUpNo") &&
                !jobsubSavedOutput.isDirty("mime") &&
                !jobsubSavedOutput.isDirty("createDate") &&
                jobsubSavedOutput.isDirty("fileName") &&
                !jobsubSavedOutput.isDirty("creatorId"))
        try {
            jobsubSavedOutputService.update([domainModel: jobsubSavedOutput])
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "unsupported.update"
        }
    }

    @Test
    void testJobsubSavedOutputUpdateFileName() {
        def jobsubSavedOutputs = JobsubSavedOutput.findAll()
        def jobsubSavedOutput = jobsubSavedOutputs.find { it.job == "SARADMS" && it.oneUpNo == 6256 }
        assertNotNull jobsubSavedOutput.id
        assertTrue jobsubSavedOutput.fileName == "saradms_6256.lis"
        jobsubSavedOutput.fileName == "saradms_6255.lis"

        assertTrue !jobsubSavedOutput.isDirty("printDate")
        assertFalse   (!jobsubSavedOutput.isDirty("job") &&
                !jobsubSavedOutput.isDirty("printer") &&
                !jobsubSavedOutput.isDirty("printForm") &&
                !jobsubSavedOutput.isDirty("oneUpNo") &&
                !jobsubSavedOutput.isDirty("mime") &&
                !jobsubSavedOutput.isDirty("createDate") &&
                jobsubSavedOutput.isDirty("fileName") &&
                !jobsubSavedOutput.isDirty("creatorId"))
        try {
            jobsubSavedOutputService.update([domainModel: jobsubSavedOutput])
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "unsupported.update"
        }
    }


    @Test
    void testJobsubSavedOutputUpdateCreatorId() {
        def jobsubSavedOutputs = JobsubSavedOutput.findAll()
        def jobsubSavedOutput = jobsubSavedOutputs.find { it.job == "SARADMS" && it.oneUpNo == 6256 }
        assertNotNull jobsubSavedOutput.id
        assertEquals jobsubSavedOutput.creatorId,  "GRAILS_USER"
        jobsubSavedOutput.creatorId = "SAISUSR"

        assertTrue !jobsubSavedOutput.isDirty("printDate")
        assertTrue   (!jobsubSavedOutput.isDirty("job") &&
                !jobsubSavedOutput.isDirty("printer") &&
                !jobsubSavedOutput.isDirty("printForm") &&
                !jobsubSavedOutput.isDirty("oneUpNo") &&
                !jobsubSavedOutput.isDirty("mime") &&
                !jobsubSavedOutput.isDirty("createDate") &&
                !jobsubSavedOutput.isDirty("fileName") &&
                jobsubSavedOutput.isDirty("creatorId"))

        try {
            jobsubSavedOutputService.update([domainModel: jobsubSavedOutput])
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "unsupported.update"
        }
    }


    @Test
    void testJobsubSavedOutputUpdateCreateDate() {
        def jobsubSavedOutputs = JobsubSavedOutput.findAll()
        def jobsubSavedOutput = jobsubSavedOutputs.find { it.job == "SARADMS" && it.oneUpNo == 6256 }
        assertNotNull jobsubSavedOutput.id
        def createDate = new Date() - 3
        assertTrue jobsubSavedOutput.createDate != createDate
        jobsubSavedOutput.createDate = createDate

        assertTrue (!jobsubSavedOutput.isDirty("printDate"))
        assertTrue   (!jobsubSavedOutput.isDirty("job") &&
                !jobsubSavedOutput.isDirty("printer") &&
                !jobsubSavedOutput.isDirty("printForm") &&
                !jobsubSavedOutput.isDirty("oneUpNo") &&
                !jobsubSavedOutput.isDirty("mime") &&
                jobsubSavedOutput.isDirty("createDate") &&
                !jobsubSavedOutput.isDirty("fileName") &&
                !jobsubSavedOutput.isDirty("creatorId"))

        try {
            jobsubSavedOutputService.update([domainModel: jobsubSavedOutput])
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "unsupported.update"
        }
    }


    @Test
    void testJobsubSavedOutputUpdateMime() {
        def jobsubSavedOutputs = JobsubSavedOutput.findAll()
        def jobsubSavedOutput = jobsubSavedOutputs.find { it.job == "SARADMS" && it.oneUpNo == 6256 }
        assertNotNull jobsubSavedOutput.id
        assertTrue jobsubSavedOutput.mime == "2"
        jobsubSavedOutput.mime = "1"

        assertTrue (!jobsubSavedOutput.isDirty("printDate"))
        assertTrue   (!jobsubSavedOutput.isDirty("job") &&
                !jobsubSavedOutput.isDirty("printer") &&
                !jobsubSavedOutput.isDirty("printForm") &&
                !jobsubSavedOutput.isDirty("oneUpNo") &&
                jobsubSavedOutput.isDirty("mime") &&
                !jobsubSavedOutput.isDirty("createDate") &&
                !jobsubSavedOutput.isDirty("fileName") &&
                !jobsubSavedOutput.isDirty("creatorId"))

        try {
            jobsubSavedOutputService.update([domainModel: jobsubSavedOutput])
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "unsupported.update"
        }
    }


    @Test
    void testJobsubSavedOutputUpdateOneUpNo() {
        def jobsubSavedOutputs = JobsubSavedOutput.findAll()
        def jobsubSavedOutput = jobsubSavedOutputs.find { it.job == "SARADMS" && it.oneUpNo == 6256 }
        assertNotNull jobsubSavedOutput.id
        assertTrue jobsubSavedOutput.oneUpNo == 6256
        jobsubSavedOutput.oneUpNo = 6255

        assertTrue (!jobsubSavedOutput.isDirty("printDate"))
        assertTrue   (!jobsubSavedOutput.isDirty("job") &&
                !jobsubSavedOutput.isDirty("printer") &&
                !jobsubSavedOutput.isDirty("printForm") &&
                jobsubSavedOutput.isDirty("oneUpNo") &&
                !jobsubSavedOutput.isDirty("mime") &&
                !jobsubSavedOutput.isDirty("createDate") &&
                !jobsubSavedOutput.isDirty("fileName") &&
                !jobsubSavedOutput.isDirty("creatorId"))

        try {
            jobsubSavedOutputService.update([domainModel: jobsubSavedOutput])
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "unsupported.update"
        }
    }


    @Test
    void testJobsubSavedOutputUpdatePrintForm() {
        def jobsubSavedOutputs = JobsubSavedOutput.findAll()
        def jobsubSavedOutput = jobsubSavedOutputs.find { it.job == "SARADMS" && it.oneUpNo == 6256 }
        assertNotNull jobsubSavedOutput.id
        assertTrue jobsubSavedOutput.printForm != "FORM"
        jobsubSavedOutput.printForm = "FORM"

        assertTrue !jobsubSavedOutput.isDirty("printDate")
        assertTrue   (!jobsubSavedOutput.isDirty("job") &&
                !jobsubSavedOutput.isDirty("printer") &&
                jobsubSavedOutput.isDirty("printForm") &&
                !jobsubSavedOutput.isDirty("oneUpNo") &&
                !jobsubSavedOutput.isDirty("mime") &&
                !jobsubSavedOutput.isDirty("createDate") &&
                !jobsubSavedOutput.isDirty("fileName") &&
                !jobsubSavedOutput.isDirty("creatorId"))

        try {
            jobsubSavedOutputService.update([domainModel: jobsubSavedOutput])
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "unsupported.update"
        }
    }


    @Test
    void testJobsubSavedOutputUpdatePrinter() {
        def jobsubSavedOutputs = JobsubSavedOutput.findAll()
        def jobsubSavedOutput = jobsubSavedOutputs.find { it.job == "SARADMS" && it.oneUpNo == 6256 }
        assertNotNull jobsubSavedOutput.id
        assertNotNull jobsubSavedOutput.printer
        assertEquals jobsubSavedOutput.printer, i_success_printer
        def saveId = jobsubSavedOutput.id
        jobsubSavedOutput.printer = 'sleepwake'

        assertTrue !jobsubSavedOutput.isDirty("printDate")
        assertTrue   (!jobsubSavedOutput.isDirty("job") &&
                 jobsubSavedOutput.isDirty("printer") &&
                !jobsubSavedOutput.isDirty("printForm") &&
                !jobsubSavedOutput.isDirty("oneUpNo") &&
                !jobsubSavedOutput.isDirty("mime") &&
                !jobsubSavedOutput.isDirty("createDate") &&
                !jobsubSavedOutput.isDirty("fileName") &&
                !jobsubSavedOutput.isDirty("creatorId"))

        try {
            jobsubSavedOutputService.update([domainModel: jobsubSavedOutput])
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "unsupported.update"
        }
    }


    @Test
    void testJobsubSavedOutputUpdateJob() {
        def jobsubSavedOutputs = JobsubSavedOutput.findAll()
        def jobsubSavedOutput = jobsubSavedOutputs.find { it.job == "SARADMS" && it.oneUpNo == 6256 }
        assertNotNull jobsubSavedOutput.id
        assertEquals jobsubSavedOutput.job, "SARADMS"
        jobsubSavedOutput.job = "STURPT"

        assertTrue !jobsubSavedOutput.isDirty("printDate")
        assertTrue   ( jobsubSavedOutput.isDirty("job") &&
                !jobsubSavedOutput.isDirty("printer") &&
                !jobsubSavedOutput.isDirty("printForm") &&
                !jobsubSavedOutput.isDirty("oneUpNo") &&
                !jobsubSavedOutput.isDirty("mime") &&
                !jobsubSavedOutput.isDirty("createDate") &&
                !jobsubSavedOutput.isDirty("fileName") &&
                !jobsubSavedOutput.isDirty("creatorId"))

        try {
            jobsubSavedOutputService.update([domainModel: jobsubSavedOutput])
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "unsupported.update"
        }
    }

}
