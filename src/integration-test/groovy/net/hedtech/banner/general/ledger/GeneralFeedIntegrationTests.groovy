/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.ledger

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

@Integration
@Rollback
class GeneralFeedIntegrationTests extends BaseIntegrationTestCase {
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
    public void testValidateReferenceNumber() {
        //null is allowed
        GeneralFeed generalFeed = createNewGenerealFeed([referenceNumber: null])
        assertNull(generalFeed.referenceNumber)
        generalFeed.validate()
        assertFalse(generalFeed.hasErrors())
        //length of referenceNumber is max 8 so below should fail on validation
        GeneralFeed generalFeed1 = createNewGenerealFeed([referenceNumber: "REF_NUM01"])
        generalFeed1.validate()
        assertTrue(generalFeed1.hasErrors())
        assertEquals(1, generalFeed1.errors.errorCount)
        assertEquals("maxSize.exceeded", generalFeed1.errors.getFieldError('referenceNumber').code)
    }

    @Test
    public void testValidateReferenceOrganization() {
        //null is allowed
        GeneralFeed generalFeed = createNewGenerealFeed([referenceOrganization: null])
        assertNull(generalFeed.referenceOrganization)
        generalFeed.validate()
        assertFalse(generalFeed.hasErrors())
        //length of referenceOrganization is max 9 so below should fail on validation
        GeneralFeed generalFeed1 = createNewGenerealFeed([referenceOrganization: "REF_ORGN01"])
        generalFeed1.validate()
        assertTrue(generalFeed1.hasErrors())
        assertEquals(1, generalFeed1.errors.errorCount)
        assertEquals("maxSize.exceeded", generalFeed1.errors.getFieldError('referenceOrganization').code)
    }

    @Test
    public void testValidateEncumbranceNumber() {
        //null is allowed
        GeneralFeed generalFeed = createNewGenerealFeed([encumbranceNumber: null])
        assertNull(generalFeed.encumbranceNumber)
        generalFeed.validate()
        assertFalse(generalFeed.hasErrors())
        //length of encumbranceNumber is max 8
        GeneralFeed generalFeed1 = createNewGenerealFeed([encumbranceNumber: 'E'*9])
        generalFeed1.validate()
        assertTrue(generalFeed1.hasErrors())
        assertEquals(1, generalFeed1.errors.errorCount)
        assertEquals("maxSize.exceeded", generalFeed1.errors.getFieldError('encumbranceNumber').code)
    }

    @Test
    public void testValidateEncumbranceItemNumber() {
        //null is allowed
        GeneralFeed generalFeed = createNewGenerealFeed([encumbranceItemNumber: null])
        assertNull(generalFeed.encumbranceItemNumber)
        generalFeed.validate()
        assertFalse(generalFeed.hasErrors())
        //max value for encumbranceItemNumber is 9999
        GeneralFeed generalFeed1 = createNewGenerealFeed([encumbranceItemNumber: 19999])
        generalFeed1.validate()
        assertTrue(generalFeed1.hasErrors())
        assertEquals(1, generalFeed1.errors.errorCount)
        assertEquals("max.exceeded", generalFeed1.errors.getFieldError('encumbranceItemNumber').code)
    }

    @Test
    public void testValidateEncumbranceSequenceNumber() {
        //null is allowed
        GeneralFeed generalFeed = createNewGenerealFeed([encumbranceSequenceNumber: null])
        assertNull(generalFeed.encumbranceSequenceNumber)
        generalFeed.validate()
        assertFalse(generalFeed.hasErrors())
        //max value for encumbranceSequenceNumber is 9999
        GeneralFeed generalFeed1 = createNewGenerealFeed([encumbranceSequenceNumber: 19999])
        generalFeed1.validate()
        assertTrue(generalFeed1.hasErrors())
        assertEquals(1, generalFeed1.errors.errorCount)
        assertEquals("max.exceeded", generalFeed1.errors.getFieldError('encumbranceSequenceNumber').code)
    }

    @Test
    public void testValidateBudgetOverride() {
        //null is allowed
        GeneralFeed generalFeed = createNewGenerealFeed([budgetOverride: null])
        assertNull(generalFeed.budgetOverride)
        generalFeed.validate()
        assertFalse(generalFeed.hasErrors())
    }

    @Test
    public void testValidateBudgetPeriod() {
        //null is allowed
        GeneralFeed generalFeed = createNewGenerealFeed([budgetPeriod: null])
        assertNull(generalFeed.budgetPeriod)
        generalFeed.validate()
        assertFalse(generalFeed.hasErrors())
        //length of budgetPeriod is max 2
        GeneralFeed generalFeed1 = createNewGenerealFeed([budgetPeriod: 'B'*3])
        generalFeed1.validate()
        assertTrue(generalFeed1.hasErrors())
        assertEquals(1, generalFeed1.errors.errorCount)
        assertEquals("maxSize.exceeded", generalFeed1.errors.getFieldError('budgetPeriod').code)
    }

    @Test
    public void testValidateTransactionNumber() {
        //null is not allowed
        GeneralFeed generalFeed = createNewGenerealFeed([transactionNumber: null])
        assertNull(generalFeed.transactionNumber)
        generalFeed.validate()
        assertTrue(generalFeed.hasErrors())
        assertEquals(1, generalFeed.errors.errorCount)
        assertEquals("nullable", generalFeed.errors.getFieldError('transactionNumber').code)
        //length of transactionNumber is max 8 so below should fail on validation
        GeneralFeed generalFeed1 = createNewGenerealFeed([transactionNumber: "TRAN_NUM1"])
        generalFeed1.validate()
        assertTrue(generalFeed1.hasErrors())
        assertEquals(1, generalFeed1.errors.errorCount)
        assertEquals("maxSize.exceeded", generalFeed1.errors.getFieldError('transactionNumber').code)
    }

    @Test
    public void testValidateLedgerDate() {
        //null is not allowed
        GeneralFeed generalFeed = createNewGenerealFeed([ledgerDate: null])
        assertNull(generalFeed.ledgerDate)
        generalFeed.validate()
        assertTrue(generalFeed.hasErrors())
        assertEquals(1, generalFeed.errors.errorCount)
        assertEquals("nullable", generalFeed.errors.getFieldError('ledgerDate').code)
    }

    @Test
    public void testValidateReferencePerson() {
        //null is allowed
        GeneralFeed generalFeed = createNewGenerealFeed([referencePerson: null])
        assertNull(generalFeed.referencePerson)
        generalFeed.validate()
        assertFalse(generalFeed.hasErrors())
        //max value for reference person is 99999999
        GeneralFeed generalFeed1 = createNewGenerealFeed([referencePerson: 199999999])
        generalFeed1.validate()
        assertTrue(generalFeed1.hasErrors())
        assertEquals(1, generalFeed1.errors.errorCount)
        assertEquals("max.exceeded", generalFeed1.errors.getFieldError('referencePerson').code)
    }

    @Test
    public void testValidateSequenceNumber() {
        //null is allowed
        GeneralFeed generalFeed = createNewGenerealFeed([sequenceNumber: null])
        assertNull(generalFeed.sequenceNumber)
        generalFeed.validate()
        assertFalse(generalFeed.hasErrors())
        //max value for sequence number is 9999
        GeneralFeed generalFeed1 = createNewGenerealFeed([sequenceNumber: 19999])
        generalFeed1.validate()
        assertTrue(generalFeed1.hasErrors())
        assertEquals(1, generalFeed1.errors.errorCount)
        assertEquals("max.exceeded", generalFeed1.errors.getFieldError('sequenceNumber').code)
    }

    @Test
    public void testValidateAccountingString() {
        //null is allowed
        GeneralFeed generalFeed = createNewGenerealFeed([accountingString: null])
        assertNull(generalFeed.accountingString)
        generalFeed.validate()
        assertFalse(generalFeed.hasErrors())
        //max size for accountingString is 60
        GeneralFeed generalFeed1 = createNewGenerealFeed([accountingString: 'A'*61])
        generalFeed1.validate()
        assertTrue(generalFeed1.hasErrors())
        assertEquals(1, generalFeed1.errors.errorCount)
        assertEquals("maxSize.exceeded", generalFeed1.errors.getFieldError('accountingString').code)
    }

    @Test
    public void testValidateChartOfAccountsCode() {
        //null is allowed
        GeneralFeed generalFeed = createNewGenerealFeed([chartOfAccountsCode: null])
        assertNull(generalFeed.chartOfAccountsCode)
        generalFeed.validate()
        assertFalse(generalFeed.hasErrors())
        //max size for chartOfAccountsCode is 1
        GeneralFeed generalFeed1 = createNewGenerealFeed([chartOfAccountsCode: "01"])
        generalFeed1.validate()
        assertTrue(generalFeed1.hasErrors())
        assertEquals(1, generalFeed1.errors.errorCount)
        assertEquals("maxSize.exceeded", generalFeed1.errors.getFieldError('chartOfAccountsCode').code)
    }

    @Test
    public void testValidateAccountIndexCode() {
        //null is allowed
        GeneralFeed generalFeed = createNewGenerealFeed([accountIndexCode: null])
        assertNull(generalFeed.accountIndexCode)
        generalFeed.validate()
        assertFalse(generalFeed.hasErrors())
        //max size for accountIndexCode is 6
        GeneralFeed generalFeed1 = createNewGenerealFeed([accountIndexCode: "ACCI_C1"])
        generalFeed1.validate()
        assertTrue(generalFeed1.hasErrors())
        assertEquals(1, generalFeed1.errors.errorCount)
        assertEquals("maxSize.exceeded", generalFeed1.errors.getFieldError('accountIndexCode').code)
    }

    @Test
    public void testValidateFundCode() {
        //null is allowed
        GeneralFeed generalFeed = createNewGenerealFeed([fundCode: null])
        assertNull(generalFeed.fundCode)
        generalFeed.validate()
        assertFalse(generalFeed.hasErrors())
        //max size for fundCode is 6
        GeneralFeed generalFeed1 = createNewGenerealFeed([fundCode: "FUND_C1"])
        generalFeed1.validate()
        assertTrue(generalFeed1.hasErrors())
        assertEquals(1, generalFeed1.errors.errorCount)
        assertEquals("maxSize.exceeded", generalFeed1.errors.getFieldError('fundCode').code)
    }

    @Test
    public void testValidateOrgnizationCode() {
        //null is allowed
        GeneralFeed generalFeed = createNewGenerealFeed([organizationCode: null])
        assertNull(generalFeed.organizationCode)
        generalFeed.validate()
        assertFalse(generalFeed.hasErrors())
        //max size for organizationCode is 6
        GeneralFeed generalFeed1 = createNewGenerealFeed([organizationCode: "ORGN_C1"])
        generalFeed1.validate()
        assertTrue(generalFeed1.hasErrors())
        assertEquals(1, generalFeed1.errors.errorCount)
        assertEquals("maxSize.exceeded", generalFeed1.errors.getFieldError('organizationCode').code)
    }

    @Test
    public void testValidateAccountCode() {
        //null is allowed
        GeneralFeed generalFeed = createNewGenerealFeed([accountCode: null])
        assertNull(generalFeed.accountCode)
        generalFeed.validate()
        assertFalse(generalFeed.hasErrors())
        //max size for accountCode is 6
        GeneralFeed generalFeed1 = createNewGenerealFeed([accountCode: "ACCT_C1"])
        generalFeed1.validate()
        assertTrue(generalFeed1.hasErrors())
        assertEquals(1, generalFeed1.errors.errorCount)
        assertEquals("maxSize.exceeded", generalFeed1.errors.getFieldError('accountCode').code)
    }

    @Test
    public void testValidateProgramCode() {
        //null is allowed
        GeneralFeed generalFeed = createNewGenerealFeed([programCode: null])
        assertNull(generalFeed.programCode)
        generalFeed.validate()
        assertFalse(generalFeed.hasErrors())
        //max size for programCode is 6
        GeneralFeed generalFeed1 = createNewGenerealFeed([programCode: "PROG_C1"])
        generalFeed1.validate()
        assertTrue(generalFeed1.hasErrors())
        assertEquals(1, generalFeed1.errors.errorCount)
        assertEquals("maxSize.exceeded", generalFeed1.errors.getFieldError('programCode').code)
    }

    @Test
    public void testValidateActivityCode() {
        //null is allowed
        GeneralFeed generalFeed = createNewGenerealFeed([activityCode: null])
        assertNull(generalFeed.activityCode)
        generalFeed.validate()
        assertFalse(generalFeed.hasErrors())
        //max size for activityCode is 6
        GeneralFeed generalFeed1 = createNewGenerealFeed([activityCode: "ACTV_C1"])
        generalFeed1.validate()
        assertTrue(generalFeed1.hasErrors())
        assertEquals(1, generalFeed1.errors.errorCount)
        assertEquals("maxSize.exceeded", generalFeed1.errors.getFieldError('activityCode').code)
    }

    @Test
    public void testValidateLocationCode() {
        //null is allowed
        GeneralFeed generalFeed = createNewGenerealFeed([locationCode: null])
        assertNull(generalFeed.locationCode)
        generalFeed.validate()
        assertFalse(generalFeed.hasErrors())
        //max size for locationCode is 6
        GeneralFeed generalFeed1 = createNewGenerealFeed([locationCode: "LOC_C01"])
        generalFeed1.validate()
        assertTrue(generalFeed1.hasErrors())
        assertEquals(1, generalFeed1.errors.errorCount)
        assertEquals("maxSize.exceeded", generalFeed1.errors.getFieldError('locationCode').code)
    }

    @Test
    public void testValidateProjectCode() {
        //null is allowed
        GeneralFeed generalFeed = createNewGenerealFeed([projectCode: null])
        assertNull(generalFeed.projectCode)
        generalFeed.validate()
        assertFalse(generalFeed.hasErrors())
        //max size for projectCode is 6
        GeneralFeed generalFeed1 = createNewGenerealFeed([projectCode: "PROJ_C123"])
        generalFeed1.validate()
        assertTrue(generalFeed1.hasErrors())
        assertEquals(1, generalFeed1.errors.errorCount)
        assertEquals("maxSize.exceeded", generalFeed1.errors.getFieldError('projectCode').code)
    }

    @Test
    public void testValidateBankCode() {
        //null is allowed
        GeneralFeed generalFeed = createNewGenerealFeed([bankCode: null])
        assertNull(generalFeed.bankCode)
        generalFeed.validate()
        assertFalse(generalFeed.hasErrors())
        //max size for bankCode is 2
        GeneralFeed generalFeed1 = createNewGenerealFeed([bankCode: "BC1"])
        generalFeed1.validate()
        assertTrue(generalFeed1.hasErrors())
        assertEquals(1, generalFeed1.errors.errorCount)
        assertEquals("maxSize.exceeded", generalFeed1.errors.getFieldError('bankCode').code)
    }

    @Test
    public void testValidateRuleClassCode() {
        //null is allowed
        GeneralFeed generalFeed = createNewGenerealFeed([ruleClassCode: null])
        assertNull(generalFeed.ruleClassCode)
        generalFeed.validate()
        assertFalse(generalFeed.hasErrors())
        //max size for ruleClassCode is 4
        GeneralFeed generalFeed1 = createNewGenerealFeed([ruleClassCode: "RC_C1"])
        generalFeed1.validate()
        assertTrue(generalFeed1.hasErrors())
        assertEquals(1, generalFeed1.errors.errorCount)
        assertEquals("maxSize.exceeded", generalFeed1.errors.getFieldError('ruleClassCode').code)
    }

    @Test
    public void testValidateDescription() {
        //null is allowed
        GeneralFeed generalFeed = createNewGenerealFeed([description: null])
        assertNull(generalFeed.description)
        generalFeed.validate()
        assertFalse(generalFeed.hasErrors())
        //max size for description is 35
        GeneralFeed generalFeed1 = createNewGenerealFeed([description: 'D' * 36])
        generalFeed1.validate()
        assertTrue(generalFeed1.hasErrors())
        assertEquals(1, generalFeed1.errors.errorCount)
        assertEquals("maxSize.exceeded", generalFeed1.errors.getFieldError('description').code)
    }

    @Test
    public void testValidateType() {
        //null is allowed
        GeneralFeed generalFeed = createNewGenerealFeed([type: null])
        assertNull(generalFeed.type)
        generalFeed.validate()
        assertFalse(generalFeed.hasErrors())
        //C is valid value for type
        GeneralFeed generalFeed1 = createNewGenerealFeed([type: 'C'])
        generalFeed1.validate()
        assertFalse(generalFeed1.hasErrors())
        //D is valid value for type
        GeneralFeed generalFeed2 = createNewGenerealFeed([type: 'D'])
        generalFeed2.validate()
        assertFalse(generalFeed2.hasErrors())
        //anthing other than C/D is not valid
        GeneralFeed generalFeed3 = createNewGenerealFeed([type: 'd'])
        generalFeed3.validate()
        assertTrue(generalFeed3.hasErrors())
        assertEquals(1, generalFeed3.errors.errorCount)
        assertEquals("invalid.type", generalFeed3.errors.getFieldError('type').code)
    }

    @Test
    public void testValidateAmount() {
        //null is not allowed
        GeneralFeed generalFeed = createNewGenerealFeed([amount: null])
        assertNull(generalFeed.amount)
        generalFeed.validate()
        assertTrue(generalFeed.hasErrors())
        assertEquals(1, generalFeed.errors.errorCount)
        assertEquals("nullable", generalFeed.errors.getFieldError('amount').code)
        //max value for amount is 999999999999999.99
        GeneralFeed generalFeed1 = createNewGenerealFeed([amount: 1999999999999999.00])
        generalFeed1.validate()
        assertTrue(generalFeed1.hasErrors())
        assertEquals(1, generalFeed1.errors.errorCount)
        assertEquals("max.exceeded", generalFeed1.errors.getFieldError('amount').code)
    }

    @Test
    public void testValidateCurrencyCode() {
        //null is allowed
        GeneralFeed generalFeed = createNewGenerealFeed([currencyCode: null])
        assertNull(generalFeed.currencyCode)
        generalFeed.validate()
        assertFalse(generalFeed.hasErrors())
        //max size for currencyCode is 4
        GeneralFeed generalFeed1 = createNewGenerealFeed([currencyCode: 'C'*5])
        generalFeed1.validate()
        assertTrue(generalFeed1.hasErrors())
        assertEquals(1, generalFeed1.errors.errorCount)
        assertEquals("maxSize.exceeded", generalFeed1.errors.getFieldError('currencyCode').code)
    }

    @Test
    public void testValidateSystemId() {
        //null is not allowed
        GeneralFeed generalFeed = createNewGenerealFeed([systemId: null])
        assertNull(generalFeed.systemId)
        generalFeed.validate()
        assertTrue(generalFeed.hasErrors())
        assertEquals(1, generalFeed.errors.errorCount)
        assertEquals("nullable", generalFeed.errors.getFieldError('systemId').code)
        //max size for systemId is 8
        GeneralFeed generalFeed1 = createNewGenerealFeed([systemId: "SYS_ID_01"])
        generalFeed1.validate()
        assertTrue(generalFeed1.hasErrors())
        assertEquals(1, generalFeed1.errors.errorCount)
        assertEquals("maxSize.exceeded", generalFeed1.errors.getFieldError('systemId').code)
    }

    @Test
    public void testValidateRecordTypeIndicator() {
        //null is not allowed
        GeneralFeed generalFeed = createNewGenerealFeed([recordTypeIndicator: null])
        assertNull(generalFeed.recordTypeIndicator)
        generalFeed.validate()
        assertTrue(generalFeed.hasErrors())
        assertEquals(1, generalFeed.errors.errorCount)
        assertEquals("nullable", generalFeed.errors.getFieldError('recordTypeIndicator').code)
        //1 is valid value for recordTypeIndicator
        GeneralFeed generalFeed1 = createNewGenerealFeed([recordTypeIndicator: '1'])
        generalFeed1.validate()
        assertFalse(generalFeed1.hasErrors())
        //2 is valid value for type
        GeneralFeed generalFeed2 = createNewGenerealFeed([recordTypeIndicator: '2'])
        generalFeed2.validate()
        assertFalse(generalFeed2.hasErrors())
        //anthing other than 1/2 is not valid
        GeneralFeed generalFeed3 = createNewGenerealFeed([recordTypeIndicator: '3'])
        generalFeed3.validate()
        assertTrue(generalFeed3.hasErrors())
        assertEquals(1, generalFeed3.errors.errorCount)
        assertEquals("invalid.recordTypeIndicator", generalFeed3.errors.getFieldError('recordTypeIndicator').code)
    }

    @Test
    public void testValidateSystemTimestamp() {
        //null is not allowed
        GeneralFeed generalFeed = createNewGenerealFeed([systemTimestamp: null])
        assertNull(generalFeed.systemTimestamp)
        generalFeed.validate()
        assertTrue(generalFeed.hasErrors())
        assertEquals(1, generalFeed.errors.errorCount)
        assertEquals("nullable", generalFeed.errors.getFieldError('systemTimestamp').code)
        //max size for systemTimestamp is 14
        GeneralFeed generalFeed1 = createNewGenerealFeed([systemTimestamp: "SYS_TIME_STAMP1"])
        generalFeed1.validate()
        assertTrue(generalFeed1.hasErrors())
        assertEquals(1, generalFeed1.errors.errorCount)
        assertEquals("maxSize.exceeded", generalFeed1.errors.getFieldError('systemTimestamp').code)
    }

    @Test
    public void testValidateActivityDate() {
        //null is not allowed
        GeneralFeed generalFeed = createNewGenerealFeed([activityDate: null])
        assertNull(generalFeed.activityDate)
        generalFeed.validate()
        assertTrue(generalFeed.hasErrors())
        assertEquals(1, generalFeed.errors.errorCount)
        assertEquals("nullable", generalFeed.errors.getFieldError('activityDate').code)
    }

    @Test
    public void testValidateUserId() {
        //null is not allowed
        GeneralFeed generalFeed = createNewGenerealFeed([userId: null])
        assertNull(generalFeed.userId)
        generalFeed.validate()
        assertTrue(generalFeed.hasErrors())
        assertEquals(1, generalFeed.errors.errorCount)
        assertEquals("nullable", generalFeed.errors.getFieldError('userId').code)
        //max size for userId is 30
        GeneralFeed generalFeed1 = createNewGenerealFeed([userId: 'U' * 31])
        generalFeed1.validate()
        assertTrue(generalFeed1.hasErrors())
        assertEquals(1, generalFeed1.errors.errorCount)
        assertEquals("maxSize.exceeded", generalFeed1.errors.getFieldError('userId').code)
    }

    @Test
    public void testValidateDepositNumber() {
        //null is allowed
        GeneralFeed generalFeed = createNewGenerealFeed([depositNumber: null])
        assertNull(generalFeed.depositNumber)
        generalFeed.validate()
        assertFalse(generalFeed.hasErrors())
        //max size for depositNumber is 8
        GeneralFeed generalFeed1 = createNewGenerealFeed([depositNumber: "DEPO_NUM1"])
        generalFeed1.validate()
        assertTrue(generalFeed1.hasErrors())
        assertEquals(1, generalFeed1.errors.errorCount)
        assertEquals("maxSize.exceeded", generalFeed1.errors.getFieldError('depositNumber').code)
    }

    @Test
    public void testCreate() {
        GeneralFeed generalFeed = createNewGenerealFeed()
        save generalFeed
        assertNotNull(generalFeed.id)
    }

    @Test
    public void testRead() {
        GeneralFeed generalFeed = createNewGenerealFeed()
        save generalFeed
        assertNotNull(generalFeed.id)
        assertNotNull(GeneralFeed.get(generalFeed.id))
    }

    @Test
    public void testUpdate() {
        GeneralFeed generalFeed = createNewGenerealFeed()
        save generalFeed
        assertNotNull(generalFeed.id)
        generalFeed.accountCode = "MODED";
        save generalFeed
        GeneralFeed generalFeed1 = GeneralFeed.get(generalFeed.id)
        assertNotNull(generalFeed1)
        assertEquals("MODED", generalFeed1.accountCode)
    }

    @Test
    public void testDelete() {
        GeneralFeed generalFeed = createNewGenerealFeed()
        save generalFeed
        assertNotNull(generalFeed.id)
        generalFeed.delete()
        GeneralFeed.withNewSession {
            assertNull(GeneralFeed.get(generalFeed.id))
        }
    }

    @Test
    public void testFetchAllByGuidInListNullList(){
        List<String> guids = null
        assertEquals([], GeneralFeedShadow.fetchAllByGuidInList(guids))
    }

    @Test
    public void testFetchAllByGuidInListEmptyList(){
        List<String> guids = []
        assertEquals([], GeneralFeedShadow.fetchAllByGuidInList(guids))
    }

    @Test
    public void testFetchAllByGuidInListInvalidValues(){
        List<String> guids = ["garbage-value","invalid-value","random-value"]
        assertEquals([], GeneralFeedShadow.fetchAllByGuidInList(guids))
    }

    /*
    This test cannot run the seed data without the seed data catalog being added as a dependency.
    The tests need to be redesigned to not use the seed data, seed data needs to be added as a dependency,
    or another method to access the seed data catalog without adding it as a dependency needs to be introduced in
    order to stop ignoring this test. As of 10/10/19 this test passes with the seed data dependency added.
    */
    @Ignore
    @Test
    public void testFetchAllByGuidInListValidValues(){
        runSeedData('general-ledger')
        String guid = GeneralFeedShadow.findAll().guid.unique()[0]
        assertNotNull(guid)
        List<String> guids = [guid,"invalid-value","random-value"]
        assertEquals([guid], GeneralFeedShadow.fetchAllByGuidInList(guids).guid.unique())
        String guid1 = GeneralFeedShadow.findAll().guid.unique()[1]
        assertNotNull(guid1)
        guids = [guid, guid1]
        assertEquals([guid, guid1].unique().sort(), GeneralFeedShadow.fetchAllByGuidInList(guids).guid.unique().sort())
        runSeedData('general-ledger-clean')
    }

    @Test
    public void testTransactionNumberExistNullList() {
        assertEquals(false, GeneralFeed.transactionNumberExist(null))
    }

    @Test
    public void testTransactionNumberExistEmptyList() {
        assertEquals(false, GeneralFeed.transactionNumberExist([]))
    }

    @Test
    public void testTransactionNumberExistInvalidList() {
        assertEquals(false, GeneralFeed.transactionNumberExist(['invalid1', 'invalid2']))
    }

    /*
    This test cannot run the seed data without the seed data catalog being added as a dependency.
    The tests need to be redesigned to not use the seed data, seed data needs to be added as a dependency,
    or another method to access the seed data catalog without adding it as a dependency needs to be introduced in
    order to stop ignoring this test. As of 10/10/19 this test passes with the seed data dependency added.
    */
    @Ignore
    @Test
    public void testTransactionNumberExist() {
       runSeedData('general-ledger-gurfeed')
        assertEquals(true, GeneralFeed.transactionNumberExist(['DCITTST']))
       runSeedData('general-ledger-gurfeed-clean')
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

        private createNewGenerealFeed(Map properties) {
        GeneralFeed generalFeed = new GeneralFeed()
        generalFeed.referenceNumber = "REF_NUM1"
        generalFeed.transactionNumber = "TRAN_NUM"
        generalFeed.ledgerDate = new Date()
        generalFeed.referencePerson = 12345678
        generalFeed.referenceOrganization = "REF_ORGN1"
        generalFeed.encumbranceNumber = 'E'*8
        generalFeed.encumbranceItemNumber = 4321
        generalFeed.encumbranceSequenceNumber = 2413
        generalFeed.budgetOverride = false
        generalFeed.budgetPeriod = 'BP'
        generalFeed.sequenceNumber = 1234
        generalFeed.accountingString = 'A'*60
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
        generalFeed.currencyCode = 'C'*4
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
    }
}
