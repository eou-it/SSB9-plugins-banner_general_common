/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.overall.ldm

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.Before
import org.junit.Test

class AddressCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    AddressCompositeService addressCompositeService

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }

    @Test
    void testValidList(){
        def paginationParams = [:]
        def addresses = addressCompositeService.list(paginationParams)
        assertNotNull addresses
    }
}
