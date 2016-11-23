/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.personalinformation

import net.hedtech.banner.exceptions.ApplicationException

class PersonalInformationCompositeService {

    def addressRolePrivilegesCompositeService
    def countyService
    def stateService
    def nationService
    def relationshipService
    def sqlProcessCompositeService
    def telephoneTypeService
    def emailTypeService

    def getPersonValidationObjects(map, roles = null) {
        // inner entities need to be actual domain objects
        if(map.addressType?.code)
            map.addressType = addressRolePrivilegesCompositeService.fetchAddressType(roles, map.addressType.code)
        if(map.county?.code)
            map.county = countyService.fetchCounty(map.county.code)
        if(map.state?.code)
            map.state = stateService.fetchState(map.state.code)
        if(map.nation?.code)
            map.nation = nationService.fetchNation(map.nation.code)
        if(map.relationship?.code)
            map.relationship = relationshipService.fetchRelationship(map.relationship.code)

        map
    }

    def fetchUpdateableTelephoneTypeList(pidm, roles, int max = 10, int offset = 0, String searchString = '') {
        def phoneTypeList = telephoneTypeService.fetchUpdateableTelephoneTypeList(25, offset, searchString)
        def validPhoneTypeList = []
        def userParams = buildUserMapForRules(pidm, roles)

        int i = 0, count = 0;
        while (i < phoneTypeList.size() && count < max) {
            def params = [:]
            params.TELEPHONE_TYPE = phoneTypeList[i].code
            params.putAll(userParams)

            if (sqlProcessCompositeService.getSsbRuleResult('SSB_TELEPHONE_UPDATE', params)) {
                validPhoneTypeList << phoneTypeList[i]
                count++
            }
            i++
            if(i == phoneTypeList.size()){
                offset += phoneTypeList.size()
                phoneTypeList = telephoneTypeService.fetchUpdateableTelephoneTypeList(25, offset, searchString)
                if(phoneTypeList.size() > 0) i = 0
            }
        }

        validPhoneTypeList
    }

    def validateTelephoneTypeRule(phoneType, pidm, roles) {
        def params = buildUserMapForRules(pidm, roles)
        params.TELEPHONE_TYPE = phoneType.code
        if(!sqlProcessCompositeService.getSsbRuleResult('SSB_TELEPHONE_UPDATE', params)) {
            throw new ApplicationException(net.hedtech.banner.general.system.TelephoneType, "@@r1:invalidTelephoneTypeUpdate@@")
        }
    }

    def fetchUpdateableEmailTypeList(pidm, roles, int max = 10, int offset = 0, String searchString = '') {
        def emailTypeList = emailTypeService.fetchEmailTypeList(25, offset, searchString)
        def validEmailTypeList = []
        def userParams = buildUserMapForRules(pidm, roles)

        int i = 0, count = 0;
        while (i < emailTypeList.size() && count < max) {
            def params = [:]
            params.EMAIL_TYPE = emailTypeList[i].code
            params.putAll(userParams)

            if (sqlProcessCompositeService.getSsbRuleResult('SSB_EMAIL_UPDATE', params)) {
                validEmailTypeList << emailTypeList[i]
                count++
            }
            i++
            if(i == emailTypeList.size()){
                offset += emailTypeList.size()
                emailTypeList = emailTypeService.fetchEmailTypeList(25, offset, searchString)
                if(emailTypeList.size() > 0) i = 0
            }
        }

        validEmailTypeList
    }

    def validateEmailTypeRule(emailType, pidm, roles) {
        def params = buildUserMapForRules(pidm, roles)
        params.EMAIL_TYPE = emailType.code
        if(!sqlProcessCompositeService.getSsbRuleResult('SSB_EMAIL_UPDATE', params)) {
            throw new ApplicationException(net.hedtech.banner.general.system.EmailType, "@@r1:invalidEmailTypeUpdate@@")
        }
    }

    private def buildUserMapForRules(pidm, roles) {
        def paramMap = [PIDM: pidm]
        roles.each {
            switch (it) {
                case 'ALUMNI':
                    paramMap.ROLE_ALUMNI = 'Y'
                    break
                case 'BSAC':
                    paramMap.ROLE_BSAC = 'Y'
                    break
                case 'EMPLOYEE':
                    paramMap.ROLE_EMPLOYEE = 'Y'
                    break
                case 'FACULTY':
                    paramMap.ROLE_FACULTY = 'Y'
                    break
                case 'FINAID':
                    paramMap.ROLE_FINAID = 'Y'
                    break
                case 'FINANCE':
                    paramMap.ROLE_FINANCE = 'Y'
                    break
                case 'FRIEND':
                    paramMap.ROLE_FRIEND = 'Y'
                    break
                case 'STUDENT':
                    paramMap.ROLE_STUDENT = 'Y'
                    break
            }
        }

        paramMap
    }
}