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
    void testFetchDirectoryProfileItemsForUser() {
        def pidm = PersonUtility.getPerson("GDP000005").pidm

        def result = directoryProfileCompositeService.fetchDirectoryProfileItemsForUser(pidm)

        assertNotNull result
        assertEquals 10, result.size()
        assertEquals 'Name', result[0].description
        assertEquals 'Delihia Gaddis', result[0].currentListing[0]
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

}