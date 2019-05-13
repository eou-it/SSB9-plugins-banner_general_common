package net.hedtech.banner.general.overall

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.overall.AddressRolePrivileges
import net.hedtech.banner.service.ServiceBase
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

@Integration
@Rollback
class AddressRolePrivilegesCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    def addressRolePrivilegesCompositeService

    @Before
    public void setUp() {
        formContext = ['GUAGMNU'] // Since we are not testing a controller, we need to explicitly set this
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }

    //TODO fix tests, need seed data
    @Test
    void testGetUpdateableAddressTypes() {
        def roles = ['STUDENT']
        def addressTypeList = addressRolePrivilegesCompositeService.getUpdateableAddressTypes(roles)

       // assertEquals 5, addressTypeList.size()
        assertTrue addressTypeList.size() >= 1
    }

    @Test
    void testGetNoUpdateableAddressTypes() {
        def roles = ['NOTAROLE']
        try{
            def addressTypeList = addressRolePrivilegesCompositeService.getUpdateableAddressTypes(roles)
            fail("I should have received an error but it passed; @@r1:noPrivilegesAvailable@@ ")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "noPrivilegesAvailable"
        }
    }

    @Test
    void testGetUpdateableAddressTypesWithMultipleRoles() {
        def roles = ['STUDENT', 'EMPLOYEE']
        def entityResult = AddressRolePrivileges.fetchUpdatePrivsByRoleList(roles)
        def serviceResult = addressRolePrivilegesCompositeService.getUpdateableAddressTypes(roles)

        assertTrue "Number of results from service should be less than that returned by entity method",
                   serviceResult.size() < entityResult.size()
    }

    //TODO fix tests, need seed data
    @Test
    void testFetchUpdateableAddressTypeListFifty() {
        def roles = ['STUDENT', 'EMPLOYEE']
        def addressTypeList = addressRolePrivilegesCompositeService.fetchUpdateableAddressTypeList(roles, 50)

       //assertEquals 50, addressTypeList.size()
        assertTrue addressTypeList.size() >= 1
        assertEquals 'Billing', addressTypeList[0].description
        assertEquals 'Permanent', addressTypeList[9].description
    }

    //TODO fix tests, need seed data
    @Test
    void testFetchUpdateableAddressTypeListMidList() {
        def roles = ['STUDENT', 'EMPLOYEE']
        def addressTypeList = addressRolePrivilegesCompositeService.fetchUpdateableAddressTypeList(roles, 10, 5)

       // assertEquals 12, addressTypeList.size()
        assertTrue addressTypeList.size() >= 1
        assertEquals 'Other', addressTypeList[0].description
    }

    //TODO fix tests, need seed data
    @Test
    void testFetchArUpdateableAddressTypesList() {
        def roles = ['STUDENT']
        def addressTypeList = addressRolePrivilegesCompositeService.fetchUpdateableAddressTypeList(roles, 10, 0, 'ar')

        //assertEquals 10, addressTypeList.size()
        assertTrue addressTypeList.size() >= 1

        assertEquals 'Parent 1', addressTypeList[0].description
    }

    //TODO fix tests, need seed data
    @Test
    void testFetchAddressType() {
        def roles = ['STUDENT']
        def addressType = addressRolePrivilegesCompositeService.fetchAddressType(roles, 'PR')

        assertEquals 'Permanent', addressType.description
    }

    @Test
    void testFetchNoAddressType() {
        def roles = ['STUDENT']
        try{
            def addressTypeList = addressRolePrivilegesCompositeService.fetchAddressType(roles, 'XJ')
            fail("I should have received an error but it passed; @@r1:invalidAddressType@@ ")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "invalidAddressType"
        }
    }

}
