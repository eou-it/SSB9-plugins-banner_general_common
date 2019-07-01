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
class GeneralFeedServiceIntegrationTests extends BaseIntegrationTestCase {
    GeneralFeedService generalFeedService;

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }

/*    @Test
    public void testCreate() {
        GeneralFeed generalFeed = generalFeedService.create(createNewGenerealFeed())
        assertNotNull(generalFeed)
        assertNotNull(generalFeed.id)
    }

    @Test
    public void testRead() {
        GeneralFeed generalFeed = generalFeedService.create(createNewGenerealFeed())
        assertNotNull(generalFeed)
        assertNotNull(generalFeed.id)
        GeneralFeed generalFeed1 = generalFeedService.read(generalFeed.id)
        assertNotNull(generalFeed1)
        assertEquals(generalFeed, generalFeed1)
    }

    @Test
    public void testUpdate() {
        GeneralFeed generalFeed = generalFeedService.create(createNewGenerealFeed())
        assertNotNull(generalFeed)
        assertNotNull(generalFeed.id)
        generalFeed.accountCode = "MODED"
        generalFeedService.update(generalFeed)
        GeneralFeed generalFeed1 = generalFeedService.read(generalFeed.id)
        assertNotNull(generalFeed1)
        assertEquals("MODED", generalFeed1.accountCode)
    }

    @Test
    public void testDelete() {
        GeneralFeed generalFeed = generalFeedService.create(createNewGenerealFeed())
        assertNotNull(generalFeed)
        assertNotNull(generalFeed.id)
        generalFeedService.delete(generalFeed)
        shouldFail(ApplicationException) {
            generalFeedService.read(generalFeed.id)
        }
    }

    @Test
    public void testTransactionExistDataPresent() {
        runSeedData('general-ledger-gurfeed')
        assertEquals(true, generalFeedService.transactionNumberExist(['DCITTST']))
        runSeedData('general-ledger-gurfeed-clean')
    }

    @Test
    public void testTransactionExistNoDataPresent() {
        assertEquals(false, generalFeedService.transactionNumberExist(['DCITTST']))
    }

    def runSeedData(String seedTestTarget) {
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
    }

    private createNewGenerealFeed(Map properties) {
        GeneralFeed generalFeed = new GeneralFeed()
        generalFeed.referenceNumber = "REF_NUM1"
        generalFeed.transactionNumber = "TRAN_NUM"
        generalFeed.ledgerDate = new Date()
        generalFeed.referencePerson = 12345678
        generalFeed.referenceOrganization = "REF_ORGN1"
        generalFeed.sequenceNumber = 1234
        generalFeed.accountingString = 'A' * 60
        generalFeed.chartOfAccountsCode = "1"
        generalFeed.accountIndexCode = "ACCI_C"
        generalFeed.fundCode = "FUND_C"
        generalFeed.organizationCode = "ORGN_C"
        generalFeed.accountCode = "ACCT_C"
        generalFeed.programCode = "PROG_C"
        generalFeed.activityCode = "ACTV_C"
        generalFeed.locationCode = "LOC_C1"
        generalFeed.projectCode = "PROJ_C"
        generalFeed.bankCode = "BC"
        generalFeed.ruleClassCode = "RC_C"
        generalFeed.description = '_DESC' * 7
        generalFeed.type = "C"
        generalFeed.amount = 999999999999999.99
        generalFeed.systemId = "SYS_ID_1"
        generalFeed.recordTypeIndicator = "1"
        generalFeed.systemTimestamp = "SYS_TIME_STAMP"
        generalFeed.activityDate = new Date()
        generalFeed.userId = '_USER' * 6
        generalFeed.depositNumber = "DEPO_NUM"

        if (properties && properties.size() > 0) {
            properties.keySet().each {
                generalFeed."$it" = properties.get(it)
            }
        }

        return generalFeed
    }*/
}