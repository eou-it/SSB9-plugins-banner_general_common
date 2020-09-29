/*********************************************************************************
 Copyright 2010-2020 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/

package net.hedtech.banner.general

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.junit.Before
import org.junit.Test
import org.junit.After
import net.hedtech.banner.general.system.SdaCrosswalkConversion
import net.hedtech.banner.testing.BaseIntegrationTestCase

@Integration
@Rollback
class GeneralCommonUtilityIntegrationTests extends BaseIntegrationTestCase {


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
    void testCreateSdaxMapForAppSessionList() {
        def gtvsdaxValue = SdaCrosswalkConversion.fetchAllByInternalAndInternalGroup('SCHBYDATE', 'WEBREG')[0]?.external
        assertNotNull gtvsdaxValue
        List sdaxList = []
        def sdaxMap = GeneralCommonUtility.createSdaxMapForAppSessionList('SCHBYDATE', 'WEBREG', sdaxList)
        assertEquals gtvsdaxValue, sdaxMap.gtvsdaxValue
        assertEquals 1, sdaxMap.appGtvsdaxList.size()
    }


    @Test
    void testAppGtvsdaxNewValueNoList() {
        def gtvsdaxValue = SdaCrosswalkConversion.fetchAllByInternalAndInternalGroup('SCHBYDATE', 'WEBREG')[0]?.external
        assertNotNull gtvsdaxValue
        def value = GeneralCommonUtility.getAppGtvsdax('SCHBYDATE', 'WEBREG')
        assertEquals gtvsdaxValue, value.gtvsdaxValue
        assertTrue value.appGtvsdaxList instanceof List
        assertEquals 1, value.appGtvsdaxList.size()
        assertEquals gtvsdaxValue, value.appGtvsdaxList[0].external

    }


    @Test
    void testAppGtvsdaxNewValueExisingList() {
        def gtvsdaxValue = SdaCrosswalkConversion.fetchAllByInternalAndInternalGroup('SCHBYDATE', 'WEBREG')[0]?.external
        assertNotNull gtvsdaxValue
        def sdaxList = []
        sdaxList << [internal: 'SCHBYDATE', internalGroup: 'WEBREG', external: gtvsdaxValue, testApp: "YES"]

        def value = GeneralCommonUtility.getAppGtvsdax('SCHBYDATE', 'WEBREG', sdaxList)
        assertEquals gtvsdaxValue, value.gtvsdaxValue
        assertTrue value.appGtvsdaxList instanceof List
        assertEquals 1, value.appGtvsdaxList.size()
        assertEquals gtvsdaxValue, value.appGtvsdaxList[0].external
        assertEquals "YES", value.appGtvsdaxList[0].testApp

    }


    @Test
    void testAppGtvsdaxNewValueExisingListMultipleEntries() {
        def gtvsdaxValue = SdaCrosswalkConversion.fetchAllByInternalAndInternalGroup('SCHBYDATE', 'WEBREG')[0]?.external
        assertNotNull gtvsdaxValue
        def sdaxList = []
        sdaxList << [internal: 'SCHBYDATE', internalGroup: 'WEBREG', external: gtvsdaxValue]
        def gtvsdaxValue2 = SdaCrosswalkConversion.fetchAllByInternalAndInternalGroup('WEBALTPINA', 'WEBREG')[0]?.external
        sdaxList << [internal: 'WEBALTPINA', internalGroup: 'WEBREG', external: gtvsdaxValue]

        def termDate = SdaCrosswalkConversion.fetchAllByInternalAndInternalGroup('WEBTRMDTE', 'STUWEB')[0]?.external

        def value = GeneralCommonUtility.getAppGtvsdax('WEBTRMDTE', 'STUWEB', sdaxList)
        assertEquals termDate, value.gtvsdaxValue
        assertTrue value.appGtvsdaxList instanceof List
        assertEquals 3, value.appGtvsdaxList.size()
        assertNotNull "WEBTRMDTE", value.appGtvsdaxList.find { it.internal == "WEBTRMDTE" }
        assertNotNull "SCHBYDATE", value.appGtvsdaxList.find { it.internal == "SCHBYDATE" }
        assertNotNull "WEBALTPINA", value.appGtvsdaxList.find { it.internal == "WEBALTPINA" }

    }


    @Test
    void testValidatePin() {
        def pidm = net.hedtech.banner.general.person.PersonUtility.getPerson("HOS00001").pidm
        assertTrue GeneralCommonUtility.validatePin("111111", "" + pidm)
        assertTrue !GeneralCommonUtility.validatePin("111112", "" + pidm)
    }

    @Test
    void testValidateUserPin() {
        def pidm = net.hedtech.banner.general.person.PersonUtility.getPerson("HOS00001").pidm
        //valid password
        int statusFlag = GeneralCommonUtility.validateUserPin("111111", "" + pidm)
        assertTrue  statusFlag != GeneralCommonUtility.INVALID_PIN

        statusFlag = GeneralCommonUtility.validateUserPin("111112", "" + pidm)
        assertTrue  statusFlag == GeneralCommonUtility.INVALID_PIN
       //TODO --Need to write test cases for Expired and Disable scenarios
    }

    @Test
    void testCheckUserRoleTrue(){
        loginSSB('HOSS001', '111111')
        def hasRole = GeneralCommonUtility.checkUserRole('STUDENT')
        assertTrue hasRole
        logout()
    }

    @Test
    void testCheckUserRoleFalse(){
        loginSSB('JABS-0001', '111111')
        def hasRole = GeneralCommonUtility.checkUserRole('STUDENT')
        assertFalse hasRole
        logout()
    }

}
