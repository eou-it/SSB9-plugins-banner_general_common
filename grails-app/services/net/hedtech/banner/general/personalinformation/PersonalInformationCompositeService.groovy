/*********************************************************************************
 Copyright 2016-2019 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.personalinformation

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.system.EmailType

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
        return fetchUpdateableTypesList('TELEPHONE', pidm, roles, max, offset, searchString)
    }

    def validateTelephoneTypeRule(phoneType, pidm, roles) {
        def params = buildUserMapForRules(pidm, roles)
        params.TELEPHONE_TYPE = phoneType.code
        if(!sqlProcessCompositeService.getSsbRuleResult('SSB_TELEPHONE_UPDATE', params)) {
            throw new ApplicationException(net.hedtech.banner.general.system.TelephoneType, "@@r1:invalidTelephoneTypeUpdate@@")
        }
    }

    def populateTelephoneUpdateableStatus(telephones, roles) {
        telephones.each { telephone ->
            telephone.isUpdateable = true

            try {
                validateTelephoneTypeRule(telephone.telephoneType, telephone.pidm, roles)
            } catch (ApplicationException e) {
                telephone.isUpdateable = false
            }
        }
    }

    def fetchUpdateableEmailTypeList(pidm, roles, int max = 10, int offset = 0, String searchString = '') {
        return fetchUpdateableTypesList('EMAIL', pidm, roles, max, offset, searchString)
    }

    def validateEmailTypeRule(emailType, pidm, roles) {
        def params = buildUserMapForRules(pidm, roles)
        params.EMAIL_TYPE = emailType.code
        if(!sqlProcessCompositeService.getSsbRuleResult('SSB_EMAIL_UPDATE', params)) {
            throw new ApplicationException(net.hedtech.banner.general.system.EmailType, "@@r1:invalidEmailTypeUpdate@@")
        }
    }

    def populateEmailUpdateableStatus(emails, roles) {
        emails.each { email ->
            email.isUpdateable = EmailType.fetchByCodeAndWebDisplayable(email.emailType.code).size() > 0

            try {
                validateEmailTypeRule(email.emailType, email.pidm, roles)
            } catch (ApplicationException e) {
                email.isUpdateable = false
            }
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

    private def fetchUpdateableTypesList(type, pidm, roles, int max, int offset, String searchString) {
        Closure fetchList
        String rule, typeName
        if(type == 'TELEPHONE') {
            fetchList = { int maximum, int ofst, String searchStr ->
                telephoneTypeService.fetchUpdateableTelephoneTypeList(maximum, ofst, searchStr)
            }
            rule = 'SSB_TELEPHONE_UPDATE'
            typeName = 'TELEPHONE_TYPE'
        }
        else {
            fetchList = { int maximum, int ofst, String searchStr ->
                emailTypeService.fetchEmailTypeList(maximum, ofst, searchStr)
            }
            rule = 'SSB_EMAIL_UPDATE'
            typeName = 'EMAIL_TYPE'
        }
        def typeList = fetchList(25, 0, searchString)
        def validTypeList = []
        def userParams = buildUserMapForRules(pidm, roles)

        int i = 0, count = 0, rawOffset = 0;
        while (i < typeList.size() && count < (max+offset)) {
            def params = [:]
            params[typeName] = typeList[i].code
            params.putAll(userParams)

            if (sqlProcessCompositeService.getSsbRuleResult(rule, params)) {
                count++
                if(count > offset)
                    validTypeList << typeList[i]
            }
            i++
            if(i == typeList.size()) {
                rawOffset += typeList.size()
                typeList = fetchList(25, rawOffset, searchString)
                if(typeList.size() > 0) i = 0
            }
        }

        validTypeList
    }
}
