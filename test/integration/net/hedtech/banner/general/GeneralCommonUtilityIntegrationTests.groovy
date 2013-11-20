/*********************************************************************************
 Copyright 2010-2013 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/

package net.hedtech.banner.general
import net.hedtech.banner.general.system.SdaCrosswalkConversion
import net.hedtech.banner.testing.BaseIntegrationTestCase

class GeneralCommonUtilityIntegrationTests extends BaseIntegrationTestCase {


    protected void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    protected void tearDown() {
        super.tearDown()
    }


    void testCreateSdaxMapForAppSessionList() {
        def gtvsdaxValue = SdaCrosswalkConversion.fetchAllByInternalAndInternalGroup('SCHBYDATE', 'WEBREG')[0]?.external
        assertNotNull gtvsdaxValue
        List sdaxList = []
        def sdaxMap = GeneralCommonUtility.createSdaxMapForAppSessionList('SCHBYDATE', 'WEBREG', sdaxList)
        assertEquals gtvsdaxValue, sdaxMap.gtvsdaxValue
        assertEquals 1, sdaxMap.appGtvsdaxList.size()
    }


    void testAppGtvsdaxNewValueNoList() {
        def gtvsdaxValue = SdaCrosswalkConversion.fetchAllByInternalAndInternalGroup('SCHBYDATE', 'WEBREG')[0]?.external
        assertNotNull gtvsdaxValue
        def value = GeneralCommonUtility.getAppGtvsdax('SCHBYDATE', 'WEBREG')
        assertEquals gtvsdaxValue, value.gtvsdaxValue
        assertTrue value.appGtvsdaxList instanceof List
        assertEquals 1, value.appGtvsdaxList.size()
        assertEquals gtvsdaxValue, value.appGtvsdaxList[0].external

    }


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


    def testValidatePin() {
        def pidm = net.hedtech.banner.general.person.PersonUtility.getPerson("HOS00001").pidm
        assertTrue GeneralCommonUtility.validatePin("111111", "" + pidm)
        assertTrue !GeneralCommonUtility.validatePin("111112", "" + pidm)
    }

}
