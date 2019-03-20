/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.overall

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.Before
import org.junit.Test

/**
 * Integration test cases for ThirdPartyAccessService
 */
class ThirdPartyAccessServiceIntegrationTests extends BaseIntegrationTestCase {

    ThirdPartyAccessService thirdPartyAccessService

    @Before
    public void setUp() {
        formContext = ['GUAGMNU'] // Since we are not testing a controller, we need to explicitly set this
        super.setUp()
    }

    @Test
    void testFetchAllByCriteria(){
       List<ThirdPartyAccess>  entities =  thirdPartyAccessService.list([max:1])
       assertFalse entities.isEmpty()
        assertEquals 1, entities.size()
        ThirdPartyAccess thirdPartyAccess = entities[0]
        assertNotNull thirdPartyAccess
        List<ThirdPartyAccess> actualEntities = thirdPartyAccessService.fetchAllByCriteria([externalUser:thirdPartyAccess.externalUser])
        assertFalse actualEntities.isEmpty()
        assertEquals 1, actualEntities.size()
        ThirdPartyAccess actualThirdPartyAccess = actualEntities[0]
        assertEquals actualThirdPartyAccess, thirdPartyAccess
        assertEquals actualEntities.size(),  thirdPartyAccessService.countByCriteria([externalUser:thirdPartyAccess.externalUser])
    }
}
