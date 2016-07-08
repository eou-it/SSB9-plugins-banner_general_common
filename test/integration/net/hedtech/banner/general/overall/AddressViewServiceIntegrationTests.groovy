/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.overall

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.Before
import org.junit.Test

class AddressViewServiceIntegrationTests extends BaseIntegrationTestCase {

    AddressViewService addressViewService

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    @Test
    void testCreateNotAllowed() {
        shouldFail(ApplicationException) {
            addressViewService.create([AddressView: new AddressView()])
        }
    }

    @Test
    void testUpdateNotAllowed() {
        shouldFail(ApplicationException) {
            addressViewService.update([AddressView: new AddressView()])
        }
    }

    @Test
    void testDeleteNotAllowed() {
        shouldFail(ApplicationException) {
            addressViewService.delete([AddressView: new AddressView()])
        }
    }

    @Test
    void testReadAllowed() {
        AddressView addressView = AddressView.findAll()[0]
        AddressView objReadUsingService = addressViewService.read(addressView.id) as AddressView
        assertNotNull objReadUsingService
        assertEquals(addressView.id, objReadUsingService.id)
    }

    @Test
    void testFethAllWithParams() {
        List<AddressView> addressViews = addressViewService.fetchAll(100,0)
        assertNotNull addressViews
        assertNotNull addressViews[0].id
    }

    @Test
    void testFethAllInList() {
        List<AddressView> addressViews = addressViewService.fetchAll()
        assertNotNull addressViews
        assertNotNull addressViews[0].id
    }
}
