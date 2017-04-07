/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.overall

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.person.PersonAddress
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.general.system.AddressType
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
        List<AddressView> addressViews = addressViewService.fetchAll(10, 0)
        AddressView objReadUsingService = addressViewService.read(addressViews[0].id) as AddressView
        assertNotNull objReadUsingService
        assertEquals(addressViews[0].id, objReadUsingService.id)
    }

    @Test
    void testFethAllWithParams() {
        List<AddressView> addressViews = addressViewService.fetchAll(10, 0)
        assertNotNull addressViews
        assertNotNull addressViews[0].id
    }

    @Test
    void testFetchByGuid() {
        List<AddressView> addressViews = addressViewService.fetchAll(1, 0)
        assertEquals 1, addressViews.size()
        def guid = addressViews[0].id
        def result = addressViewService.fetchByGuid(guid)
        assertNotNull result
        assertTrue result instanceof AddressView
    }

    @Test
    void testFetchByGuidList() {
        List<AddressView> addressViews = addressViewService.fetchAll(10, 0)
        assertTrue addressViews.size() > 0
        def guids = addressViews.id
        def results = addressViewService.fetchByGuidList(guids)
        assertTrue results.size() > 0
        assertTrue results[0] instanceof AddressView
        assertEquals addressViews.size(), results.size()
    }

    @Test
    void testFetchAllByGuidsAndAddressTypeCodes() {
        List<AddressView> addressViews = addressViewService.fetchAll(10, 0)
        assertTrue addressViews.size() > 0
        //get the index that has the atyp code populated otherwise the test fails
        def atypIndex = addressViews.findIndexOf {it.addressTypeCode != null}
        if (atypIndex == 0) {
            //there could be a case where the first 10 rows selected have no atyp so get first 30
            addressViews = addressViewService.fetchAll(30, 0)
        }
        atypIndex = addressViews.findIndexOf {it.addressTypeCode != null}
        def results = addressViewService.fetchAllByGuidsAndAddressTypeCodes([addressViews[atypIndex].id], [addressViews[atypIndex].addressTypeCode])
        assertTrue results.size() > 0
        assertTrue results[0] instanceof AddressView
    }

}
