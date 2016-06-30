/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.ledger

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

class GeneralFeedShadowServiceIntegrationTests extends BaseIntegrationTestCase {
    GeneralFeedShadowService generalFeedShadowService;

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
