/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

@Integration
@Rollback
class SanitizeUtilityIntegrationTests extends BaseIntegrationTestCase {

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
    void testSanitizeOnEmptyMap(){
        def map = [:]

        SanitizeUtility.sanitizeMap(map)

        assertEquals(0, map.size())
    }

    @Test
    void testSanitizeOnCleanMap(){
        def map = [
                accountNumber: 123,
                bankName: 'My Bank'
        ]

        SanitizeUtility.sanitizeMap(map)

        assertEquals(123, map.accountNumber)
        assertEquals('My Bank', map.bankName)
    }

    @Test
    void testSanitizeOnScriptTag(){
        def map = [
                accountNumber: 123,
                bankName: '<sCrIpT>alert(68541)<\\/sCrIpT>'
        ]

        SanitizeUtility.sanitizeMap(map)

        assertEquals(123, map.accountNumber)
        assertEquals('', map.bankName)
    }

    @Test
    void testSanitizeWithNestedMap(){
        def map = [
                accountNumber: 123,
                bankName: '<sCrIpT>alert(68541)<\\/sCrIpT>',
                nested: [
                        accountType: '<sCrIpT>alert(999)<\\/sCrIpT>',
                        addressSequenceNum: 5555,
                        apIndicator: 'I'
                ]
        ]

        SanitizeUtility.sanitizeMap(map)

        assertEquals(123, map.accountNumber)
        assertEquals('', map.bankName)
        assertEquals('', map.nested.accountType)
        assertEquals(5555, map.nested.addressSequenceNum)
        assertEquals('I', map.nested.apIndicator)
    }
}
