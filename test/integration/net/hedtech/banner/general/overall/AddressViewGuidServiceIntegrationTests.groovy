/*********************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.overall

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.Before
import org.junit.Test

class AddressViewGuidServiceIntegrationTests extends BaseIntegrationTestCase {


    AddressViewGuidService addressViewGuidService


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    @Test
    void testCreateNotAllowed() {
        shouldFail(ApplicationException) {
            addressViewGuidService.create([AddressViewGuid: new AddressViewGuid()])
        }
    }


    @Test
    void testUpdateNotAllowed() {
        shouldFail(ApplicationException) {
            addressViewGuidService.update([AddressViewGuid: new AddressViewGuid()])
        }
    }


    @Test
    void testDeleteNotAllowed() {
        shouldFail(ApplicationException) {
            addressViewGuidService.delete([AddressViewGuid: new AddressViewGuid()])
        }
    }


    @Test
    void testReadAllowed() {
        List<AddressViewGuid> addressViewGuids = addressViewGuidService.fetchAll(10, 0)
        AddressViewGuid objReadUsingService = addressViewGuidService.read(addressViewGuids[0].id) as AddressViewGuid
        assertNotNull objReadUsingService
        assertEquals(addressViewGuids[0].id, objReadUsingService.id)
    }


    @Test
    void testFethAllWithParams() {
        List<AddressViewGuid> addressViewGuids = addressViewGuidService.fetchAll(10, 0)
        assertNotNull addressViewGuids
        assertNotNull addressViewGuids[0].id
    }

}
