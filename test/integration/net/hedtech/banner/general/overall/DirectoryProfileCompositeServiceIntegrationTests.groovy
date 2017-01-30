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


    // TODO: uncomment when implementation of test is completed.  Jim Caley
//    @Test
//    void testFetchDirectoryProfileItemsForUser() {
//        def pidm = PersonUtility.getPerson("GDP000005").pidm
//
//        def result = directoryProfileCompositeService.fetchDirectoryProfileItemsForUser(pidm)
//
//        // TODO: finish this test.  Jim Caley
//        assertNull result
//    }

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