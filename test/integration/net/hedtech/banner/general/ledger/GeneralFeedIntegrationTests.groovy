/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.ledger

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

class GeneralFeedIntegrationTests extends BaseIntegrationTestCase {
    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
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
        GeneralFeed generalFeed = createNewGenerealFeed([orgnizationCode: null])
        assertNull(generalFeed.orgnizationCode)
        generalFeed.validate()
        assertFalse(generalFeed.hasErrors())
        //max size for orgnizationCode is 6
        GeneralFeed generalFeed1 = createNewGenerealFeed([orgnizationCode: "ORGN_C1"])
        generalFeed1.validate()
        assertTrue(generalFeed1.hasErrors())
        assertEquals(1, generalFeed1.errors.errorCount)
        assertEquals("maxSize.exceeded", generalFeed1.errors.getFieldError('orgnizationCode').code)
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
        GeneralFeed generalFeed1 = createNewGenerealFeed([projectCode: "PROJ_C1"])
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

    private createNewGenerealFeed(Map properties) {
        GeneralFeed generalFeed = new GeneralFeed()
        generalFeed.referenceNumber = "REF_NUM1"
        generalFeed.transactionNumber = "TRAN_NUM"
        generalFeed.ledgerDate = new Date()
        generalFeed.referencePerson = 12345678
        generalFeed.sequenceNumber = 1234
        generalFeed.chartOfAccountsCode = "1"
        generalFeed.accountIndexCode = "ACCI_C"
        generalFeed.fundCode = "FUND_C"
        generalFeed.orgnizationCode = "ORGN_C"
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
    }
}
