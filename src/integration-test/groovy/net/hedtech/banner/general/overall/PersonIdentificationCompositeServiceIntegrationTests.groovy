/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import net.hedtech.banner.testing.BaseIntegrationTestCase
import grails.web.servlet.mvc.GrailsParameterMap
import org.junit.After
import org.junit.Before
import org.junit.Test

@Integration
@Rollback
class PersonIdentificationCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    def personIdentificationCompositeService

    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']
        super.setUp()
    }

    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    void testList() {
        Map map = [:]

        def lstBannerId = [] as List
        lstBannerId.add 'HOSP0001'
        map.put 'bannerId', lstBannerId

        def lstEnterpriseId = [] as List
        lstEnterpriseId.add 'FORTEST'
        map.put 'enterpriseId', lstEnterpriseId

        def lstSourcedId = [] as List
        lstSourcedId.add '2600'
        map.put 'sourcedId', lstSourcedId

        def lstLdapUserMapping = [] as List
        lstLdapUserMapping.add 'systest33'
        map.put 'ldapUserMapping', lstLdapUserMapping

        def lstExternalUser = [] as List
        lstExternalUser.add 'sgrim'
        map.put 'externalUser', lstExternalUser

        GrailsParameterMap params = new GrailsParameterMap(map, null)
        List<PersonIdentificationDecorator> personList = personIdentificationCompositeService.list(params)
        assertEquals 5, personList.size()

        PersonIdentificationDecorator personIdentificationDecorator = personList.get(0)
        assertEquals "HOSP0001", personIdentificationDecorator.bannerId
        assertEquals "GRAILS", personIdentificationDecorator.dataOrigin
        assertEquals "DSTERLIN", personIdentificationDecorator.enterpriseId
        assertEquals "Steve", personIdentificationDecorator.firstName
        assertEquals "Steve A Jorden", personIdentificationDecorator.fullName
        assertEquals "Elevate0001", personIdentificationDecorator.imsSourcedIdBase.sourcedId
        assertEquals "Jorden", personIdentificationDecorator.lastName
        assertEquals "A", personIdentificationDecorator.middleName
        assertEquals null, personIdentificationDecorator.surnamePrefix
        assertEquals 'sjorden', personIdentificationDecorator.thirdPartyAccess.externalUser
        assertEquals null, personIdentificationDecorator.thirdPartyAccess.ldapUserMapping

        personIdentificationDecorator = personList.get(1)
        assertEquals "GDP000011", personIdentificationDecorator.bannerId
        assertEquals "GRAILS", personIdentificationDecorator.dataOrigin
        assertEquals "FORTEST", personIdentificationDecorator.enterpriseId
        assertEquals "Nissan", personIdentificationDecorator.firstName
        assertEquals "Nissan Maxima", personIdentificationDecorator.fullName
        assertEquals "forTest", personIdentificationDecorator.imsSourcedIdBase.sourcedId
        assertEquals "Maxima", personIdentificationDecorator.lastName
        assertEquals null, personIdentificationDecorator.middleName
        assertEquals null, personIdentificationDecorator.surnamePrefix
        assertEquals "nmaxima", personIdentificationDecorator.thirdPartyAccess.externalUser
        assertEquals "systest32", personIdentificationDecorator.thirdPartyAccess.ldapUserMapping

        personIdentificationDecorator = personList.get(2)
        assertEquals "613238760", personIdentificationDecorator.bannerId
        assertEquals "025EB9EF4906F0D0E05018958B283255", personIdentificationDecorator.enterpriseId
        assertEquals "Karen", personIdentificationDecorator.firstName
        assertEquals "Karen S Hevermeyer", personIdentificationDecorator.fullName
        assertEquals "2600", personIdentificationDecorator.imsSourcedIdBase.sourcedId
        assertEquals "Hevermeyer", personIdentificationDecorator.lastName
        assertEquals "S", personIdentificationDecorator.middleName
        assertEquals null, personIdentificationDecorator.surnamePrefix
        assertEquals null, personIdentificationDecorator.thirdPartyAccess.externalUser
        assertEquals null, personIdentificationDecorator.thirdPartyAccess.ldapUserMapping

        personIdentificationDecorator = personList.get(3)
        assertEquals "A00000671", personIdentificationDecorator.bannerId
        assertEquals "2134", personIdentificationDecorator.enterpriseId
        assertEquals "Faculty", personIdentificationDecorator.firstName
        assertEquals "Faculty PCC", personIdentificationDecorator.fullName
        assertEquals "forTest2", personIdentificationDecorator.imsSourcedIdBase.sourcedId
        assertEquals "PCC", personIdentificationDecorator.lastName
        assertEquals null, personIdentificationDecorator.middleName
        assertEquals null, personIdentificationDecorator.surnamePrefix
        assertEquals "fpcc", personIdentificationDecorator.thirdPartyAccess.externalUser
        assertEquals "systest33", personIdentificationDecorator.thirdPartyAccess.ldapUserMapping

        personIdentificationDecorator = personList.get(4)
        assertEquals "SJGRIM", personIdentificationDecorator.bannerId
        assertEquals "025EB9EF618DF0D0E05018958B283255", personIdentificationDecorator.enterpriseId
        assertEquals "Warren", personIdentificationDecorator.firstName
        assertEquals "Warren Zevon Grim", personIdentificationDecorator.fullName
        assertEquals "forTest3", personIdentificationDecorator.imsSourcedIdBase.sourcedId
        assertEquals "Grim", personIdentificationDecorator.lastName
        assertEquals "Zevon", personIdentificationDecorator.middleName
        assertEquals null, personIdentificationDecorator.surnamePrefix
        assertEquals "sgrim", personIdentificationDecorator.thirdPartyAccess.externalUser
        assertEquals null, personIdentificationDecorator.thirdPartyAccess.ldapUserMapping
    }
}