package net.hedtech.banner.general.overall

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.system.AddressType

class AddressRolePrivilegesCompositeService {

    def getUpdateableAddressTypes(roles) {
        // Filter out results having no addressType
        def addressTypeList = AddressRolePrivileges.fetchUpdatePrivsByRoleList(roles).findAll {
            it.addressType
        }

        addressTypeList = addressTypeList.collect {
            it.addressType
        }

        if(addressTypeList.size() == 0) {
            throw new ApplicationException(AddressRolePrivileges, "@@r1:noPrivilegesAvailable@@")
        }

        addressTypeList.unique()  // If multiple roles are used above, duplicate types can be returned
    }

    def fetchUpdateableAddressTypeList(roles, int max = 10, int offset = 0, String searchString = '') {
        def addressTypeList = getUpdateableAddressTypes(roles)
        def results

        addressTypeList = addressTypeList.sort {
            it.description
        }

        if(searchString.size() > 0) {
            results = addressTypeList.findAll {
                def searchee = it.description.toUpperCase()
                def searcher = searchString.toUpperCase()
                searchee.indexOf(searcher) >= 0
            }
        }
        else {
            results = addressTypeList
        }

        if(offset >= results.size()) {
            results = []
        }
        else {
            results = results.subList(offset, results.size())
        }

        if(max >= results.size()) {
            return results
        }
        else {
            return results.subList(0, max)
        }
    }

    def fetchAddressType(roles, code) {
        def privs = AddressRolePrivileges.fetchUpdatePrivByCodeAndRoles(roles, code)
        if(!privs) {
            throw new ApplicationException(AddressType, "@@r1:invalidAddressType@@")
        }
        else {
            return privs.addressType
        }
    }
}