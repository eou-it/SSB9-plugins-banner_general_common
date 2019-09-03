/*********************************************************************************
 Copyright 2010-2017 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.jobsub

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import net.hedtech.banner.testing.BaseIntegrationTestCase
import net.hedtech.banner.exceptions.ApplicationException
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.jdbc.UncategorizedSQLException

@Integration
@Rollback
class JobsubSavedOutputIntegrationTests extends BaseIntegrationTestCase {

    //Valid test data (For success tests)

    def i_success_printer = "saas1"
    def i_notsaas_printer = "notsaas"


    @Before
    public void setUp() {
        formContext = ['SELFSERVICE'] // Since we are not testing a controller, we need to explicitly set this
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testFetchJobsubSavedOutput() {
        def jobsubSavedOutputs = JobsubSavedOutput.findAll()
        def jobsubSavedOutput = jobsubSavedOutputs.find { it.job == "SARADMS" && it.oneUpNo == 6256 }
        assertNotNull jobsubSavedOutput.id
        assertEquals jobsubSavedOutput.printer, i_success_printer
        assertEquals jobsubSavedOutput.fileName, "saradms_6256.lis"
        assertNull jobsubSavedOutput.printDate

        def jobs = JobsubSavedOutput.fetchByJobAndOneUpNoAndFileName('SARADMS', 6256, "saradms_6256.lis")
        assertNotNull jobs
        assertTrue jobs instanceof JobsubSavedOutput
        assertEquals jobs.job, "SARADMS"
        assertEquals jobs.oneUpNo, 6256
        assertEquals jobs.fileName, "saradms_6256.lis"

    }

    @Test
    void testInsertSavedOutput() {
        def jobsubSavedOutputs = JobsubSavedOutput.findAll()
        assertNull jobsubSavedOutputs.findAll { it.job == "STUREPT" && it.oneUpNo == 3333}[0]

        def savedPrint = new JobsubSavedOutput(job: "STUREPT", oneUpNo: 3333,
           fileName: "sturpt_3333.lis", mime: "2", createDate: new Date(),
            creatorId: "JOBSUB", lastModified: new Date(), lastModifiedBy : 'GRAILS_USER')
        assertNotNull savedPrint
        assertTrue savedPrint instanceof JobsubSavedOutput
        savedPrint.save(flush: true, failOnError: true)

        def jobsubSavedOutput2 = JobsubSavedOutput.findAll()
        def newSavedPrint = jobsubSavedOutput2.findAll { it.job == "STUREPT" && it.oneUpNo == 3333}[0]
        assertNotNull newSavedPrint
        assertNotNull newSavedPrint.id
        assertEquals newSavedPrint.version, 0
        assertNull newSavedPrint.printDate
        assertEquals newSavedPrint.mime, "2"
        assertEquals newSavedPrint.fileName, "sturpt_3333.lis"
    }


    @Test
    void testUpdateSavedOutput() {
        def savedPrint = JobsubSavedOutput.findAll()[0]
        assertTrue savedPrint instanceof JobsubSavedOutput
        def saveId = savedPrint.id
        def updateDate = new Date() + 10
        assertTrue savedPrint.job != "TESTAPP"
        assertTrue savedPrint.printDate != updateDate
        savedPrint.job = "TESTAPP"
        savedPrint.printDate = updateDate
        savedPrint.save(flush: true, failOnError: true)

        def updatedPrint = JobsubSavedOutput.get(saveId)
        assertEquals updatedPrint.job, "TESTAPP"
        assertEquals updatedPrint.printDate, updateDate
    }

    @Test
    void testDeleteSavedOutput() {
        def savedPrint = JobsubSavedOutput.findAll()[0]
        assertTrue savedPrint instanceof JobsubSavedOutput
        def saveId = savedPrint.id
        savedPrint.delete(flush: true, failOnError: true)
        assertNull JobsubSavedOutput.get(saveId)
    }


}
