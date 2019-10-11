/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.ledger

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
//import net.hedtech.banner.seeddata.InputData
//import net.hedtech.banner.seeddata.SeedDataLoader
//import java.nio.file.Path
//import java.nio.file.Paths

import static groovy.test.GroovyAssert.*

@Integration
@Rollback
class GeneralFeedShadowServiceIntegrationTests extends BaseIntegrationTestCase {
    GeneralFeedShadowService generalFeedShadowService;

    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    public void testCreate() {
        GeneralFeedShadow generalFeedShadow = generalFeedShadowService.create(createNewGenerealFeed())
        assertNotNull(generalFeedShadow)
        assertNotNull(generalFeedShadow.id)
    }

    @Test
    public void testRead() {
        GeneralFeedShadow generalFeedShadow = generalFeedShadowService.create(createNewGenerealFeed())
        assertNotNull(generalFeedShadow)
        assertNotNull(generalFeedShadow.id)
        GeneralFeedShadow generalFeedShadow1 = generalFeedShadowService.read(generalFeedShadow.id)
        assertNotNull(generalFeedShadow1)
        assertEquals(generalFeedShadow, generalFeedShadow1)
    }

    @Test
    public void testUpdate() {
        GeneralFeedShadow generalFeedShadow = generalFeedShadowService.create(createNewGenerealFeed())
        assertNotNull(generalFeedShadow)
        assertNotNull(generalFeedShadow.id)
        generalFeedShadow.amount = 100
        generalFeedShadowService.update(generalFeedShadow)
        GeneralFeedShadow generalFeedShadow1 = generalFeedShadowService.read(generalFeedShadow.id)
        assertNotNull(generalFeedShadow1)
        assertEquals(100, generalFeedShadow1.amount, 0)
    }

    @Test
    public void testDelete() {
        GeneralFeedShadow generalFeedShadow = generalFeedShadowService.create(createNewGenerealFeed())
        assertNotNull(generalFeedShadow)
        assertNotNull(generalFeedShadow.id)
        generalFeedShadowService.delete(generalFeedShadow)
        shouldFail(ApplicationException) {
            generalFeedShadowService.read(generalFeedShadow.id)
        }
    }

    /*
    This test cannot run the seed data without the seed data catalog being added as a dependency.
    The tests need to be redesigned to not use the seed data, seed data needs to be added as a dependency,
    or another method to access the seed data catalog without adding it as a dependency needs to be introduced in
    order to stop ignoring this test. As of 10/10/19 this test passes with the seed data dependency added.
    */
    @Ignore
    @Test
    public void testFetchAllByGuidInListValidValues() {
        runSeedData('general-ledger')
        String guid = GeneralFeedShadow.findAll().guid.unique()[0]
        assertNotNull(guid)
        assertEquals([guid], generalFeedShadowService.fetchAllByGuidInList([guid]).guid.unique())
        String guid1 = GeneralFeedShadow.findAll().guid.unique()[1]
        assertNotNull(guid1)
        assertEquals([guid, guid1].unique().sort(), generalFeedShadowService.fetchAllByGuidInList([guid, guid1]).guid.unique().sort())
        runSeedData('general-ledger-clean')
    }

    /*
    This test cannot run the seed data without the seed data catalog being added as a dependency.
    The tests need to be redesigned to not use the seed data, seed data needs to be added as a dependency,
    or another method to access the seed data catalog without adding it as a dependency needs to be introduced in
    order to stop ignoring this test. As of 10/10/19 this test passes with the seed data dependency added.
    */
    @Ignore
    @Test
    public void testFetchByGuid() {
        runSeedData('general-ledger')
        String guid = GeneralFeedShadow.findAll().guid.unique()[0]
        assertNotNull(guid)
        assertEquals([guid], generalFeedShadowService.fetchByGuid(guid).guid.unique())
    }

    private createNewGenerealFeed(Map properties) {
        GeneralFeedShadow generalFeedShadow = new GeneralFeedShadow()
        generalFeedShadow.guid = 'G' * 36
        generalFeedShadow.processMode = 'PM' * 15
        generalFeedShadow.referenceNumber = "REF_NUM1"
        generalFeedShadow.transactionNumber = "TRAN_NUM"
        generalFeedShadow.transactionType = 'T' * 50
        generalFeedShadow.ledgerDate = new Date()
        generalFeedShadow.referencePerson = 12345678
        generalFeedShadow.referenceOrganization = "REF_ORGN1"
        generalFeedShadow.transactionTypeReferenceDate = new Date()
        generalFeedShadow.sequenceNumber = 1234
        generalFeedShadow.accountingString = 'A' * 60
        generalFeedShadow.description = '_DESC' * 7
        generalFeedShadow.type = "C"
        generalFeedShadow.amount = 999999999999999.99
        generalFeedShadow.currencyCode = 'C' * 4
        generalFeedShadow.systemTimestamp = "SYS_TIME_STAMP"
        generalFeedShadow.activityDate = new Date()


        if (properties && properties.size() > 0) {
            properties.keySet().each {
                generalFeedShadow."$it" = properties.get(it)
            }
        }

        return generalFeedShadow
    }

/*
    private void runSeedData(String seedTestTarget) {

        InputData inputData = new InputData()
        inputData.username = 'baninst1'
        inputData.password = 'u_pick_it'
        inputData.hostname = 'localhost'
        inputData.instance = 'BAN83'

        def basedir = System.properties['base.dir']
        Path currentWorkingFolder = Paths.get("").toAbsolutePath()
        Path seedDataPluginPath
        if (currentWorkingFolder.toString().contains('plugins')) {
            seedDataPluginPath = currentWorkingFolder.getParent().resolve("banner_seeddata_catalog.git")
        } else {
            seedDataPluginPath = currentWorkingFolder.resolve('plugins').resolve("banner_seeddata_catalog.git")
        }

        def xmlFiles = inputData.targets.find { it.key == seedTestTarget }?.value
        if (!xmlFiles) {
            xmlFiles = inputData.seleniumTargets.find { it.key == seedTestTarget }?.value
        }
        if (!xmlFiles) {
            xmlFiles = inputData.aipTargets.find { it.key == seedTestTarget }?.value
        }
        if (!xmlFiles) {
            xmlFiles = inputData.bcmTargets.find { it.key == seedTestTarget }?.value
        }
        if (!xmlFiles) {
            xmlFiles = inputData.calbTargets.find { it.key == seedTestTarget }?.value
        }

        xmlFiles.each {
            InputData xmlInputData = new InputData()
            xmlInputData.username = 'baninst1'
            xmlInputData.password = 'u_pick_it'
            xmlInputData.hostname = 'localhost'
            xmlInputData.instance = 'BAN83'

            xmlInputData.xmlFile = "$seedDataPluginPath/$it.value"
            def inputFile = new File(xmlInputData.xmlFile)
            if (!inputFile.exists()) {
                xmlInputData.xmlFile = "${basedir}${it.value}"
            }
            xmlInputData.replaceData = true
            SeedDataLoader seedDataLoader = new SeedDataLoader(xmlInputData)
            seedDataLoader.execute()
        }
    }
    */
}
