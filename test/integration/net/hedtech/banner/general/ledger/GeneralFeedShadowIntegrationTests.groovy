/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.ledger

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

class GeneralFeedShadowIntegrationTests extends BaseIntegrationTestCase {
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
    public void testValidateGuid() {
        //null is not allowed
        GeneralFeedShadow generalFeedShadow = createNewGenerealFeed([guid: null])
        assertNull(generalFeedShadow.guid)
        generalFeedShadow.validate()
        assertTrue(generalFeedShadow.hasErrors())
        assertEquals(1, generalFeedShadow.errors.errorCount)
        assertEquals("nullable", generalFeedShadow.errors.getFieldError('guid').code)
        //length of guid is max 36
        GeneralFeedShadow generalFeedShadow1 = createNewGenerealFeed([guid: 'G'*37])
        generalFeedShadow1.validate()
        assertTrue(generalFeedShadow1.hasErrors())
        assertEquals(1, generalFeedShadow1.errors.errorCount)
        assertEquals("maxSize.exceeded", generalFeedShadow1.errors.getFieldError('guid').code)
    }

    @Test
    public void testValidateReferenceNumber() {
        //null is allowed
        GeneralFeedShadow generalFeedShadow = createNewGenerealFeed([referenceNumber: null])
        assertNull(generalFeedShadow.referenceNumber)
        generalFeedShadow.validate()
        assertFalse(generalFeedShadow.hasErrors())
        //length of referenceNumber is max 8 so below should fail on validation
        GeneralFeedShadow generalFeedShadow1 = createNewGenerealFeed([referenceNumber: "REF_NUM01"])
        generalFeedShadow1.validate()
        assertTrue(generalFeedShadow1.hasErrors())
        assertEquals(1, generalFeedShadow1.errors.errorCount)
        assertEquals("maxSize.exceeded", generalFeedShadow1.errors.getFieldError('referenceNumber').code)
    }

    @Test
    public void testValidateReferenceOrganization() {
        //null is allowed
        GeneralFeedShadow generalFeedShadow = createNewGenerealFeed([referenceOrganization: null])
        assertNull(generalFeedShadow.referenceOrganization)
        generalFeedShadow.validate()
        assertFalse(generalFeedShadow.hasErrors())
        //length of referenceOrganization is max 9 so below should fail on validation
        GeneralFeedShadow generalFeedShadow1 = createNewGenerealFeed([referenceOrganization: "REF_ORGN01"])
        generalFeedShadow1.validate()
        assertTrue(generalFeedShadow1.hasErrors())
        assertEquals(1, generalFeedShadow1.errors.errorCount)
        assertEquals("maxSize.exceeded", generalFeedShadow1.errors.getFieldError('referenceOrganization').code)
    }

    @Test
    public void testValidateTransactionTypeReferenceDate() {
        //null is allowed
        GeneralFeedShadow generalFeedShadow = createNewGenerealFeed([transactionTypeReferenceDate: null])
        assertNull(generalFeedShadow.transactionTypeReferenceDate)
        generalFeedShadow.validate()
        assertFalse(generalFeedShadow.hasErrors())
    }

    @Test
    public void testValidateEncumbranceNumber() {
        //null is allowed
        GeneralFeedShadow generalFeedShadow = createNewGenerealFeed([encumbranceNumber: null])
        assertNull(generalFeedShadow.encumbranceNumber)
        generalFeedShadow.validate()
        assertFalse(generalFeedShadow.hasErrors())
        //length of encumbranceNumber is max 8
        GeneralFeedShadow generalFeedShadow1 = createNewGenerealFeed([encumbranceNumber: 'E'*9])
        generalFeedShadow1.validate()
        assertTrue(generalFeedShadow1.hasErrors())
        assertEquals(1, generalFeedShadow1.errors.errorCount)
        assertEquals("maxSize.exceeded", generalFeedShadow1.errors.getFieldError('encumbranceNumber').code)
    }

    @Test
    public void testValidateEncumbranceItemNumber() {
        //null is allowed
        GeneralFeedShadow generalFeedShadow = createNewGenerealFeed([encumbranceItemNumber: null])
        assertNull(generalFeedShadow.encumbranceItemNumber)
        generalFeedShadow.validate()
        assertFalse(generalFeedShadow.hasErrors())
        //max value for encumbranceItemNumber is 9999
        GeneralFeedShadow generalFeedShadow1 = createNewGenerealFeed([encumbranceItemNumber: 19999])
        generalFeedShadow1.validate()
        assertTrue(generalFeedShadow1.hasErrors())
        assertEquals(1, generalFeedShadow1.errors.errorCount)
        assertEquals("max.exceeded", generalFeedShadow1.errors.getFieldError('encumbranceItemNumber').code)
    }

    @Test
    public void testValidateEncumbranceSequenceNumber() {
        //null is allowed
        GeneralFeedShadow generalFeedShadow = createNewGenerealFeed([encumbranceSequenceNumber: null])
        assertNull(generalFeedShadow.encumbranceSequenceNumber)
        generalFeedShadow.validate()
        assertFalse(generalFeedShadow.hasErrors())
        //max value for encumbranceSequenceNumber is 9999
        GeneralFeedShadow generalFeedShadow1 = createNewGenerealFeed([encumbranceSequenceNumber: 19999])
        generalFeedShadow1.validate()
        assertTrue(generalFeedShadow1.hasErrors())
        assertEquals(1, generalFeedShadow1.errors.errorCount)
        assertEquals("max.exceeded", generalFeedShadow1.errors.getFieldError('encumbranceSequenceNumber').code)
    }

    @Test
    public void testValidateBudgetOverride() {
        //null is allowed
        GeneralFeedShadow generalFeedShadow = createNewGenerealFeed([budgetOverride: null])
        assertNull(generalFeedShadow.budgetOverride)
        generalFeedShadow.validate()
        assertFalse(generalFeedShadow.hasErrors())
    }

    @Test
    public void testValidateBudgetPeriod() {
        //null is allowed
        GeneralFeedShadow generalFeedShadow = createNewGenerealFeed([budgetPeriod: null])
        assertNull(generalFeedShadow.budgetPeriod)
        generalFeedShadow.validate()
        assertFalse(generalFeedShadow.hasErrors())
        //length of budgetPeriod is max 2
        GeneralFeedShadow generalFeedShadow1 = createNewGenerealFeed([budgetPeriod: 'B'*3])
        generalFeedShadow1.validate()
        assertTrue(generalFeedShadow1.hasErrors())
        assertEquals(1, generalFeedShadow1.errors.errorCount)
        assertEquals("maxSize.exceeded", generalFeedShadow1.errors.getFieldError('budgetPeriod').code)
    }

    @Test
    public void testValidateTransactionNumber() {
        //null is not allowed
        GeneralFeedShadow generalFeedShadow = createNewGenerealFeed([transactionNumber: null])
        assertNull(generalFeedShadow.transactionNumber)
        generalFeedShadow.validate()
        assertTrue(generalFeedShadow.hasErrors())
        assertEquals(1, generalFeedShadow.errors.errorCount)
        assertEquals("nullable", generalFeedShadow.errors.getFieldError('transactionNumber').code)
        //length of transactionNumber is max 8 so below should fail on validation
        GeneralFeedShadow generalFeedShadow1 = createNewGenerealFeed([transactionNumber: "TRAN_NUM1"])
        generalFeedShadow1.validate()
        assertTrue(generalFeedShadow1.hasErrors())
        assertEquals(1, generalFeedShadow1.errors.errorCount)
        assertEquals("maxSize.exceeded", generalFeedShadow1.errors.getFieldError('transactionNumber').code)
    }

    @Test
    public void testValidateTransactionType() {
        //null is not allowed
        GeneralFeedShadow generalFeedShadow = createNewGenerealFeed([transactionType: null])
        assertNull(generalFeedShadow.transactionType)
        generalFeedShadow.validate()
        assertTrue(generalFeedShadow.hasErrors())
        assertEquals(1, generalFeedShadow.errors.errorCount)
        assertEquals("nullable", generalFeedShadow.errors.getFieldError('transactionType').code)
        //length of transactionType is max 50
        GeneralFeedShadow generalFeedShadow1 = createNewGenerealFeed([transactionType: 'T'*51])
        generalFeedShadow1.validate()
        assertTrue(generalFeedShadow1.hasErrors())
        assertEquals(1, generalFeedShadow1.errors.errorCount)
        assertEquals("maxSize.exceeded", generalFeedShadow1.errors.getFieldError('transactionType').code)
    }

    @Test
    public void testValidateLedgerDate() {
        //null is not allowed
        GeneralFeedShadow generalFeedShadow = createNewGenerealFeed([ledgerDate: null])
        assertNull(generalFeedShadow.ledgerDate)
        generalFeedShadow.validate()
        assertTrue(generalFeedShadow.hasErrors())
        assertEquals(1, generalFeedShadow.errors.errorCount)
        assertEquals("nullable", generalFeedShadow.errors.getFieldError('ledgerDate').code)
    }

    @Test
    public void testValidateReferencePerson() {
        //null is allowed
        GeneralFeedShadow generalFeedShadow = createNewGenerealFeed([referencePerson: null])
        assertNull(generalFeedShadow.referencePerson)
        generalFeedShadow.validate()
        assertFalse(generalFeedShadow.hasErrors())
        //max value for reference person is 99999999
        GeneralFeedShadow generalFeedShadow1 = createNewGenerealFeed([referencePerson: 199999999])
        generalFeedShadow1.validate()
        assertTrue(generalFeedShadow1.hasErrors())
        assertEquals(1, generalFeedShadow1.errors.errorCount)
        assertEquals("max.exceeded", generalFeedShadow1.errors.getFieldError('referencePerson').code)
    }

    @Test
    public void testValidateSequenceNumber() {
        //null is not allowed
        GeneralFeedShadow generalFeedShadow = createNewGenerealFeed([sequenceNumber: null])
        assertNull(generalFeedShadow.sequenceNumber)
        generalFeedShadow.validate()
        assertTrue(generalFeedShadow.hasErrors())
        assertEquals(1, generalFeedShadow.errors.errorCount)
        assertEquals("nullable", generalFeedShadow.errors.getFieldError('sequenceNumber').code)
        //max value for sequence number is 9999
        GeneralFeedShadow generalFeedShadow1 = createNewGenerealFeed([sequenceNumber: 19999])
        generalFeedShadow1.validate()
        assertTrue(generalFeedShadow1.hasErrors())
        assertEquals(1, generalFeedShadow1.errors.errorCount)
        assertEquals("max.exceeded", generalFeedShadow1.errors.getFieldError('sequenceNumber').code)
    }

    @Test
    public void testValidateAccountingString() {
        //null is not allowed
        GeneralFeedShadow generalFeedShadow = createNewGenerealFeed([accountingString: null])
        assertNull(generalFeedShadow.accountingString)
        generalFeedShadow.validate()
        assertTrue(generalFeedShadow.hasErrors())
        assertEquals(1, generalFeedShadow.errors.errorCount)
        assertEquals("nullable", generalFeedShadow.errors.getFieldError('accountingString').code)
        //max size for accountingString is 60
        GeneralFeedShadow generalFeedShadow1 = createNewGenerealFeed([accountingString: 'A'*61])
        generalFeedShadow1.validate()
        assertTrue(generalFeedShadow1.hasErrors())
        assertEquals(1, generalFeedShadow1.errors.errorCount)
        assertEquals("maxSize.exceeded", generalFeedShadow1.errors.getFieldError('accountingString').code)
    }

    @Test
    public void testValidateDescription() {
        //null is not allowed
        GeneralFeedShadow generalFeedShadow = createNewGenerealFeed([description: null])
        assertNull(generalFeedShadow.description)
        generalFeedShadow.validate()
        assertTrue(generalFeedShadow.hasErrors())
        assertEquals(1, generalFeedShadow.errors.errorCount)
        assertEquals("nullable", generalFeedShadow.errors.getFieldError('description').code)
        //max size for description is 35
        GeneralFeedShadow generalFeedShadow1 = createNewGenerealFeed([description: 'D' * 36])
        generalFeedShadow1.validate()
        assertTrue(generalFeedShadow1.hasErrors())
        assertEquals(1, generalFeedShadow1.errors.errorCount)
        assertEquals("maxSize.exceeded", generalFeedShadow1.errors.getFieldError('description').code)
    }

    @Test
    public void testValidateType() {
        //null is not allowed
        GeneralFeedShadow generalFeedShadow = createNewGenerealFeed([type: null])
        assertNull(generalFeedShadow.type)
        generalFeedShadow.validate()
        assertTrue(generalFeedShadow.hasErrors())
        assertEquals(1, generalFeedShadow.errors.errorCount)
        assertEquals("nullable", generalFeedShadow.errors.getFieldError('type').code)
        //C is valid value for type
        GeneralFeedShadow generalFeedShadow1 = createNewGenerealFeed([type: 'C'])
        generalFeedShadow1.validate()
        assertFalse(generalFeedShadow1.hasErrors())
        //D is valid value for type
        GeneralFeedShadow generalFeedShadow2 = createNewGenerealFeed([type: 'D'])
        generalFeedShadow2.validate()
        assertFalse(generalFeedShadow2.hasErrors())
        //anthing other than C/D is not valid
        GeneralFeedShadow generalFeedShadow3 = createNewGenerealFeed([type: 'd'])
        generalFeedShadow3.validate()
        assertTrue(generalFeedShadow3.hasErrors())
        assertEquals(1, generalFeedShadow3.errors.errorCount)
        assertEquals("invalid.type", generalFeedShadow3.errors.getFieldError('type').code)
    }

    @Test
    public void testValidateAmount() {
        //null is not allowed
        GeneralFeedShadow generalFeedShadow = createNewGenerealFeed([amount: null])
        assertNull(generalFeedShadow.amount)
        generalFeedShadow.validate()
        assertTrue(generalFeedShadow.hasErrors())
        assertEquals(1, generalFeedShadow.errors.errorCount)
        assertEquals("nullable", generalFeedShadow.errors.getFieldError('amount').code)
        //max value for amount is 999999999999999.99
        GeneralFeedShadow generalFeedShadow1 = createNewGenerealFeed([amount: 1999999999999999.00])
        generalFeedShadow1.validate()
        assertTrue(generalFeedShadow1.hasErrors())
        assertEquals(1, generalFeedShadow1.errors.errorCount)
        assertEquals("max.exceeded", generalFeedShadow1.errors.getFieldError('amount').code)
    }

    @Test
    public void testValidateCurrencyCode() {
        //null is not allowed
        GeneralFeedShadow generalFeedShadow = createNewGenerealFeed([currencyCode: null])
        assertNull(generalFeedShadow.currencyCode)
        generalFeedShadow.validate()
        assertTrue(generalFeedShadow.hasErrors())
        assertEquals(1, generalFeedShadow.errors.errorCount)
        assertEquals("nullable", generalFeedShadow.errors.getFieldError('currencyCode').code)
        //max size for currencyCode is 4
        GeneralFeedShadow generalFeedShadow1 = createNewGenerealFeed([currencyCode: 'C'*5])
        generalFeedShadow1.validate()
        assertTrue(generalFeedShadow1.hasErrors())
        assertEquals(1, generalFeedShadow1.errors.errorCount)
        assertEquals("maxSize.exceeded", generalFeedShadow1.errors.getFieldError('currencyCode').code)
    }

    @Test
    public void testValidateSystemId() {
        //null is not allowed
        GeneralFeedShadow generalFeedShadow = createNewGenerealFeed([systemId: null])
        assertNull(generalFeedShadow.systemId)
        generalFeedShadow.validate()
        assertTrue(generalFeedShadow.hasErrors())
        assertEquals(1, generalFeedShadow.errors.errorCount)
        assertEquals("nullable", generalFeedShadow.errors.getFieldError('systemId').code)
        //max size for systemId is 8
        GeneralFeedShadow generalFeedShadow1 = createNewGenerealFeed([systemId: "SYS_ID_01"])
        generalFeedShadow1.validate()
        assertTrue(generalFeedShadow1.hasErrors())
        assertEquals(1, generalFeedShadow1.errors.errorCount)
        assertEquals("maxSize.exceeded", generalFeedShadow1.errors.getFieldError('systemId').code)
    }

    @Test
    public void testValidateSystemTimestamp() {
        //null is not allowed
        GeneralFeedShadow generalFeedShadow = createNewGenerealFeed([systemTimestamp: null])
        assertNull(generalFeedShadow.systemTimestamp)
        generalFeedShadow.validate()
        assertTrue(generalFeedShadow.hasErrors())
        assertEquals(1, generalFeedShadow.errors.errorCount)
        assertEquals("nullable", generalFeedShadow.errors.getFieldError('systemTimestamp').code)
        //max size for systemTimestamp is 14
        GeneralFeedShadow generalFeedShadow1 = createNewGenerealFeed([systemTimestamp: "SYS_TIME_STAMP1"])
        generalFeedShadow1.validate()
        assertTrue(generalFeedShadow1.hasErrors())
        assertEquals(1, generalFeedShadow1.errors.errorCount)
        assertEquals("maxSize.exceeded", generalFeedShadow1.errors.getFieldError('systemTimestamp').code)
    }

    @Test
    public void testValidateActivityDate() {
        //null is not allowed
        GeneralFeedShadow generalFeedShadow = createNewGenerealFeed([activityDate: null])
        assertNull(generalFeedShadow.activityDate)
        generalFeedShadow.validate()
        assertTrue(generalFeedShadow.hasErrors())
        assertEquals(1, generalFeedShadow.errors.errorCount)
        assertEquals("nullable", generalFeedShadow.errors.getFieldError('activityDate').code)
    }

    @Test
    public void testValidateUserId() {
        //null is not allowed
        GeneralFeedShadow generalFeedShadow = createNewGenerealFeed([userId: null])
        assertNull(generalFeedShadow.userId)
        generalFeedShadow.validate()
        assertTrue(generalFeedShadow.hasErrors())
        assertEquals(1, generalFeedShadow.errors.errorCount)
        assertEquals("nullable", generalFeedShadow.errors.getFieldError('userId').code)
        //max size for userId is 30
        GeneralFeedShadow generalFeedShadow1 = createNewGenerealFeed([userId: 'U' * 31])
        generalFeedShadow1.validate()
        assertTrue(generalFeedShadow1.hasErrors())
        assertEquals(1, generalFeedShadow1.errors.errorCount)
        assertEquals("maxSize.exceeded", generalFeedShadow1.errors.getFieldError('userId').code)
    }

    @Test
    public void testValidateDepositNumber() {
        //null is allowed
        GeneralFeedShadow generalFeedShadow = createNewGenerealFeed([depositNumber: null])
        assertNull(generalFeedShadow.depositNumber)
        generalFeedShadow.validate()
        assertFalse(generalFeedShadow.hasErrors())
        //max size for depositNumber is 8
        GeneralFeedShadow generalFeedShadow1 = createNewGenerealFeed([depositNumber: "DEPO_NUM1"])
        generalFeedShadow1.validate()
        assertTrue(generalFeedShadow1.hasErrors())
        assertEquals(1, generalFeedShadow1.errors.errorCount)
        assertEquals("maxSize.exceeded", generalFeedShadow1.errors.getFieldError('depositNumber').code)
    }

    @Test
    public void testCreate() {
        GeneralFeedShadow generalFeedShadow = createNewGenerealFeed()
        save generalFeedShadow
        assertNotNull(generalFeedShadow.id)
    }

    @Test
    public void testRead() {
        GeneralFeedShadow generalFeedShadow = createNewGenerealFeed()
        save generalFeedShadow
        assertNotNull(generalFeedShadow.id)
        assertNotNull(GeneralFeedShadow.get(generalFeedShadow.id))
    }

    @Test
    public void testUpdate() {
        GeneralFeedShadow generalFeedShadow = createNewGenerealFeed()
        save generalFeedShadow
        assertNotNull(generalFeedShadow.id)
        generalFeedShadow.amount = 100;
        save generalFeedShadow
        GeneralFeedShadow generalFeedShadow1 = GeneralFeedShadow.get(generalFeedShadow.id)
        assertNotNull(generalFeedShadow1)
        assertEquals(100, generalFeedShadow1.amount, 0)
    }

    @Test
    public void testDelete() {
        GeneralFeedShadow generalFeedShadow = createNewGenerealFeed()
        save generalFeedShadow
        assertNotNull(generalFeedShadow.id)
        generalFeedShadow.delete()
        GeneralFeedShadow.withNewSession {
            assertNull(GeneralFeedShadow.get(generalFeedShadow.id))
        }
    }

    private createNewGenerealFeed(Map properties) {
        GeneralFeedShadow generalFeedShadow = new GeneralFeedShadow()
        generalFeedShadow.guid = 'G'*36
        generalFeedShadow.referenceNumber = "REF_NUM1"
        generalFeedShadow.transactionNumber = "TRAN_NUM"
        generalFeedShadow.transactionType = 'T'*50
        generalFeedShadow.ledgerDate = new Date()
        generalFeedShadow.referencePerson = 12345678
        generalFeedShadow.referenceOrganization = "REF_ORGN1"
        generalFeedShadow.transactionTypeReferenceDate = new Date()
        generalFeedShadow.encumbranceNumber = 'E'*8
        generalFeedShadow.encumbranceItemNumber = 4321
        generalFeedShadow.encumbranceSequenceNumber = 2413
        generalFeedShadow.budgetOverride = false
        generalFeedShadow.budgetPeriod = 'BP'
        generalFeedShadow.sequenceNumber = 1234
        generalFeedShadow.accountingString = 'A'*60
        generalFeedShadow.description = '_DESC' * 7
        generalFeedShadow.type = "C"
        generalFeedShadow.amount = 999999999999999.99
        generalFeedShadow.currencyCode = 'C'*4
        generalFeedShadow.systemId = "SYS_ID_1"
        generalFeedShadow.systemTimestamp = "SYS_TIME_STAMP"
        generalFeedShadow.activityDate = new Date()
        generalFeedShadow.userId = '_USER' * 6


        if (properties && properties.size() > 0) {
            properties.keySet().each {
                generalFeedShadow."$it" = properties.get(it)
            }
        }

        return generalFeedShadow
    }
}
