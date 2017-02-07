/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall

import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

class DirectoryProfileCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    def directoryProfileCompositeService

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
    void testFetchDirectoryProfileItemsForUserWithNoneDisplayedOnDirectoryProfile() {
        def pidm = PersonUtility.getPerson("GDP000005").pidm

        def result = directoryProfileCompositeService.fetchDirectoryProfileItemsForUser(pidm)

        assertNotNull result

        assertEquals 0, result.size()
    }

    @Test
    void testFetchDirectoryProfileItemsForUserWithOneDisplayedOnDirectoryProfile() {
        // Make an item displayable for the purposes of this test -- only for current session's connection.
        executeUpdateSQL "update GOBDIRO set GOBDIRO_DISP_PROFILE_IND = 'Y' where GOBDIRO_SEQ_NO =?", 1

        def pidm = PersonUtility.getPerson("GDP000005").pidm

        def result = directoryProfileCompositeService.fetchDirectoryProfileItemsForUser(pidm)

        assertNotNull result

        assertNotNull result
        assertEquals 1, result.size()
        assertEquals "Name", result[0].description
    }

    @Test
    void testFetchAllDirectoryProfileItems() {
        def result = directoryProfileCompositeService.fetchAllDirectoryProfileItems()

        assertNotNull result
        assertEquals 21, result.size()
        assertEquals 'NAME', result[0].code
        assertEquals 'TELE_SMS', result[20].code
    }

    @Test
    void testIsMatchingRoleForDirectoryTypeAndIsMatch() {
        def pidm = PersonUtility.getPerson("GDP000005").pidm

        def result = directoryProfileCompositeService.isMatchingRoleForDirectoryType(pidm, 'C')

        assertTrue result
    }

    @Test
    void testIsMatchingRoleForDirectoryTypeAndIsNotMatch() {
        def pidm = PersonUtility.getPerson("GDP000005").pidm

        def result = directoryProfileCompositeService.isMatchingRoleForDirectoryType(pidm, 'K')

        assertFalse result
    }

    @Test
    void testGetItemProperties() {
        def pidm = PersonUtility.getPerson("GDP000004").pidm
        def profileItem = directoryProfileCompositeService.fetchAllDirectoryProfileItems()[0]

        def result = directoryProfileCompositeService.getItemProperties(pidm, profileItem)

        assertNotNull result
        assertEquals 1, result.size()
        assertEquals "Name", result[0].description
        assertFalse result[0].checked
        assertFalse result[0].changeable
        assertNotNull result[0].currentListing
        assertEquals 1, result[0].currentListing.size()
        assertEquals "Delihia Margot", result[0].currentListing[0]
    }

    @Test
    void testGetItemPropertiesWithAddressWithMaskingRule() {
        def pidm = PersonUtility.getPerson("GDP000004").pidm
        def profileItem = directoryProfileCompositeService.fetchAllDirectoryProfileItems()[0]
        profileItem.code = 'ADDR_OF' // Force to be an address
        profileItem.itemType = 'A' // Force to be an address
        def addrMaskingRule = getMaskingRuleForTest()


        def result = directoryProfileCompositeService.getItemProperties(pidm, profileItem, addrMaskingRule)

        assertNotNull result
        assertEquals 1, result.size()
        assertNotNull result[0].currentListing
        assertEquals 4, result[0].currentListing.size()
        assertEquals "111 Main Street", result[0].currentListing[0]
        assertEquals "Anytown", result[0].currentListing[1]
        assertEquals "Pennsylvania 19999", result[0].currentListing[2]
        assertEquals "street line 4", result[0].currentListing[3]
    }

    @Test
    void testGetItemPropertiesWithAddressWithoutMaskingRule() {
        def pidm = PersonUtility.getPerson("GDP000004").pidm
        def profileItem = directoryProfileCompositeService.fetchAllDirectoryProfileItems()[0]
        profileItem.code = 'ADDR_OF' // Force to be an address
        profileItem.itemType = 'A' // Force to be an address

        def result = directoryProfileCompositeService.getItemProperties(pidm, profileItem)

        assertNotNull result
        assertEquals 1, result.size()
        assertNotNull result[0].currentListing
        assertEquals 3, result[0].currentListing.size()
        assertEquals "111 Main Street", result[0].currentListing[0]
        assertEquals "Anytown", result[0].currentListing[1]
        assertEquals "Pennsylvania 19999", result[0].currentListing[2]
    }

    @Test
    void testGetItemPropertiesForAddressThatDoesNotExist() {
        def pidm = PersonUtility.getPerson("GDP000005").pidm
        def profileItem = directoryProfileCompositeService.fetchAllDirectoryProfileItems()[0]
        profileItem.code = 'ADDR_PR' // Force to be an address
        profileItem.itemType = 'A' // Force to be an address

        def result = directoryProfileCompositeService.getItemProperties(pidm, profileItem)

        assertNotNull result
        assertEquals 1, result.size()
        assertNotNull result[0].currentListing
        assertEquals 1, result[0].currentListing.size()
        assertEquals "Not Reported", result[0].currentListing[0]
    }

    @Test
    void testFetchDirectoryOptionItem() {
        def result = directoryProfileCompositeService.fetchDirectoryOptionItem('ADDR_OF')

        assertNotNull result
        assertEquals 1, result.size()
        assertEquals 'Office Address', result[0].description
    }

    @Test
    void testFetchDirectoryProfilePreference() {
        def pidm = PersonUtility.getPerson("GDP000005").pidm

        def result = directoryProfileCompositeService.fetchDirectoryProfilePreference(pidm, 'NAME')

        assertNotNull result
        assertEquals 1, result.size()
        assertEquals 'NAME', result[0].code
        assertTrue result[0].displayInDirectoryIndicator
    }

    @Test
    void testGetCurrentListingForDirectoryItemforName() {
        def pidm = PersonUtility.getPerson("GDP000005").pidm
        def profileItem = directoryProfileCompositeService.fetchAllDirectoryProfileItems()[0]
        profileItem.code = 'NAME'

        def result = directoryProfileCompositeService.getCurrentListingForDirectoryItem(pidm, profileItem)

        assertNotNull result
        assertEquals 1, result.size()
        assertNotNull result[0]
        assertEquals 1, result[0].size()
        assertEquals 'Delihia Gaddis', result[0][0]
    }

    @Test
    void testGetCurrentListingForDirectoryItemforEmail() {
        def pidm = PersonUtility.getPerson("GDP000005").pidm
        def profileItem = directoryProfileCompositeService.fetchAllDirectoryProfileItems()[0]
        profileItem.code = 'EMAIL'

        def result = directoryProfileCompositeService.getCurrentListingForDirectoryItem(pidm, profileItem)

        assertNotNull result
        assertEquals 1, result.size()
        assertNotNull result[0]
        assertEquals 4, result[0].size()
        assertNotNull result[0][0]
        assertEquals 'Campus E-Mail -', result[0][0].toString()
        assertEquals 'Seymour29302@Ellucian.edu', result[0][1]
        assertEquals 'PREFERRED Home E-Mail -', result[0][2].toString()
        assertEquals 'Seymour29301@Ellucian.edu', result[0][3]
    }

    @Test
    void testGetCurrentListingForDirectoryItemforClassYear() {
        def pidm = PersonUtility.getPerson("510000000").pidm
        def profileItem = directoryProfileCompositeService.fetchAllDirectoryProfileItems()[0]
        profileItem.code = 'CLASS_YR'

        def result = directoryProfileCompositeService.getCurrentListingForDirectoryItem(pidm, profileItem)

        assertNotNull result
        assertEquals 1, result.size()
        assertNotNull result[0]
        assertEquals 1, result[0].size()
        assertEquals '1975', result[0][0]
    }

    @Test
    void testGetCurrentListingForDirectoryItemforClassYearNotReported() {
        def pidm = PersonUtility.getPerson("GDP000005").pidm
        def profileItem = directoryProfileCompositeService.fetchAllDirectoryProfileItems()[0]
        profileItem.code = 'CLASS_YR'

        def result = directoryProfileCompositeService.getCurrentListingForDirectoryItem(pidm, profileItem)

        assertNotNull result
        assertEquals 1, result.size()
        assertNotNull result[0]
        assertEquals 1, result[0].size()
        assertEquals 'Not Reported', result[0][0]
    }

    @Test
    void testGetCurrentListingForDirectoryItemforCollege() {
        // Update college code for the purposes of this test -- only for current session's connection.
        executeUpdateSQL "update SGBSTDN set SGBSTDN_COLL_CODE_1 = 'BU' where SGBSTDN_PIDM =?", 29329

        def pidm = PersonUtility.getPerson("STUAFR329").pidm
        def profileItem = directoryProfileCompositeService.fetchAllDirectoryProfileItems()[0]
        profileItem.code = 'COLLEGE'

        def result = directoryProfileCompositeService.getCurrentListingForDirectoryItem(pidm, profileItem)

        assertNotNull result
        assertEquals 1, result.size()
        assertNotNull result[0]
        assertEquals 1, result[0].size()
        assertEquals 'Business', result[0][0]
    }

    @Test
    void testGetCurrentListingForDirectoryItemforGradYear() {
        // Update college code for the purposes of this test -- only for current session's connection.
        executeUpdateSQL "update SGBSTDN set SGBSTDN_COLL_CODE_1 = 'BU' where SGBSTDN_PIDM =?", 29329

        def pidm = PersonUtility.getPerson("STUAFR329").pidm
        def profileItem = directoryProfileCompositeService.fetchAllDirectoryProfileItems()[0]
        profileItem.code = 'GRD_YEAR'

        def result = directoryProfileCompositeService.getCurrentListingForDirectoryItem(pidm, profileItem)

        assertNotNull result
        assertEquals 1, result.size()
        assertNotNull result[0]
        assertEquals 1, result[0].size()
        assertEquals '2016', result[0][0]
    }

    @Test
    void testGetCurrentListingForDirectoryItemforDept() {
        def pidm = PersonUtility.getPerson("710000004").pidm
        def profileItem = directoryProfileCompositeService.fetchAllDirectoryProfileItems()[0]
        profileItem.code = 'DEPT'

        def result = directoryProfileCompositeService.getCurrentListingForDirectoryItem(pidm, profileItem)

        assertNotNull result
        assertEquals 1, result.size()
        assertNotNull result[0]
        assertEquals 1, result[0].size()
        assertEquals 'Div of Home Economics', result[0][0]
    }

    @Test
    void testGetCurrentListingForDirectoryItemforTitle() {
        def pidm = PersonUtility.getPerson("710000004").pidm
        def profileItem = directoryProfileCompositeService.fetchAllDirectoryProfileItems()[0]
        profileItem.code = 'TITLE'

        def result = directoryProfileCompositeService.getCurrentListingForDirectoryItem(pidm, profileItem)

        assertNotNull result
        assertEquals 1, result.size()
        assertNotNull result[0]
        assertEquals 1, result[0].size()
        assertNotNull result[0][0]
        assertEquals 'Assistant Professor (Div of Home Economics)', result[0][0].toString()
    }

    @Test
    void testGetCurrentListingForDirectoryItemforMaiden() {
        def pidm = PersonUtility.getPerson("510000000").pidm
        def profileItem = directoryProfileCompositeService.fetchAllDirectoryProfileItems()[0]
        profileItem.code = 'MAIDEN'

        def result = directoryProfileCompositeService.getCurrentListingForDirectoryItem(pidm, profileItem)

        assertNotNull result
        assertEquals 1, result.size()
        assertNotNull result[0]
        assertEquals 1, result[0].size()
        assertEquals 'Evans', result[0][0]
    }

    @Test
    void testGetCurrentListingForDirectoryItemforPreferredCollege() {
        def pidm = PersonUtility.getPerson("210009107").pidm
        def profileItem = directoryProfileCompositeService.fetchAllDirectoryProfileItems()[0]
        profileItem.code = 'PR_COLL'

        def result = directoryProfileCompositeService.getCurrentListingForDirectoryItem(pidm, profileItem)

        assertNotNull result
        assertEquals 1, result.size()
        assertNotNull result[0]
        assertEquals 1, result[0].size()
        assertEquals 'Arts  Science', result[0][0]
    }

    @Test
    void testGetCurrentListingForDirectoryItemforPhone() {
        def pidm = PersonUtility.getPerson("GDP000004").pidm
        def profileItem = directoryProfileCompositeService.fetchAllDirectoryProfileItems()[0]
        profileItem.code = 'TELE_PR' // Force to be a phone
        profileItem.itemType = 'T' // Force to be a phone

        def result = directoryProfileCompositeService.getCurrentListingForDirectoryItem(pidm, profileItem)

        assertNotNull result
        assertEquals 1, result.size()
        assertNotNull result[0]
        assertEquals 3, result[0].size()
        assertEquals '312 5568001', result[0][0] // Primary phone, so listed first
        assertEquals '215 2083094', result[0][1] // Next two listed by sequence number
        assertEquals 'Unlisted', result[0][2]
    }



    private getMaskingRuleForTest() {
        def maskingRule = [
                displayStreetLine4: true
        ]

        return maskingRule
    }

}