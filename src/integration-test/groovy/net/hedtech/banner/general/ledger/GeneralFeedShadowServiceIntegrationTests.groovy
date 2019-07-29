/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.ledger

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.testing.BaseIntegrationTestCase
//import org.codehaus.groovy.grails.plugins.GrailsPluginUtils
import org.junit.After
import org.junit.Before
import org.junit.Test

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

    @Test
    public void testFetchAllByGuidInListValidValues() {
//        runSeedData('general-ledger')
        String guid = GeneralFeedShadow.findAll().guid.unique()[0]
        assertNotNull(guid)
        assertEquals([guid], generalFeedShadowService.fetchAllByGuidInList([guid]).guid.unique())
        String guid1 = GeneralFeedShadow.findAll().guid.unique()[1]
        assertNotNull(guid1)
        assertEquals([guid, guid1].unique().sort(), generalFeedShadowService.fetchAllByGuidInList([guid, guid1]).guid.unique().sort())
//        runSeedData('general-ledger-clean')
    }

    @Test
    public void testFetchByGuid() {
//        runSeedData('general-ledger')
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


/*    def runSeedData(String seedTestTarget) {
        def clazzInputData = Thread.currentThread().contextClassLoader.loadClass("net.hedtech.banner.seeddata.InputData")
        def inputData = clazzInputData.newInstance([dataSource: dataSource])

        def xmlFiles = inputData.targets.find { it.key == seedTestTarget }?.value
        if (!xmlFiles) xmlFiles = inputData.seleniumTargets.find { it.key == seedTestTarget }?.value

        def basedir = System.properties['base.dir']
        xmlFiles.each { xmlFileName ->
            inputData.xmlFile = GrailsPluginUtils.getPluginDirForName('banner-seeddata-catalog').path + xmlFileName.value
            inputData.tableCnts = []
            inputData.username = "baninst1"
            inputData.password = "u_pick_it"
            inputData.tableSize = 0
            def inputFile = new File(inputData.xmlFile)
            if (!inputFile.exists())
                inputData.xmlFile = "${basedir}${xmlFileName.value}"
            def seedDataLoader = new net.hedtech.banner.seeddata.SeedDataLoader(inputData)
            seedDataLoader.execute()
        }
    }*/
}
