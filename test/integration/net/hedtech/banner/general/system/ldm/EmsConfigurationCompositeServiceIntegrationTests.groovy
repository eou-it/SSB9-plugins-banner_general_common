/** *******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.system.ldm

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

class EmsConfigurationCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    def emsConfigurationCompositeService


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        initializeDataReferences()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    private void initializeDataReferences() {
    }


    @Test
    void testGet_ElevateConfiguration() {
        String id = "BANNER-ELEVATE"
        EmsConfiguration emsConfiguration = emsConfigurationCompositeService.get(id)
        assertNotNull emsConfiguration
        assertEquals id, emsConfiguration.id
        assertNotNull emsConfiguration.messageInConfig
        assertNull emsConfiguration.hasProperty("integrationHubConfig")
    }


    @Test
    void testGet_EthosConfiguration() {
        String id = "ETHOS-INTEGRATION"
        EmsConfiguration emsConfiguration = emsConfigurationCompositeService.get(id)
        assertNotNull emsConfiguration
        assertEquals id, emsConfiguration.id
        if (emsConfiguration.useIntegrationHub) {
            assertNotNull emsConfiguration.integrationHubConfig
        } else {
            assertNull emsConfiguration.integrationHubConfig
        }
        assertNull emsConfiguration.hasProperty("messageInConfig")
    }


    @Test
    void testGetInvalidId() {
        String id = "BANNER-UNKNOWN"
        String msg = shouldFail {
            emsConfigurationCompositeService.get(id)
        }
        String expectedMsg = "No bean named 'erpApiConfig-$id' is defined"
        assertEquals expectedMsg, msg
    }

}
