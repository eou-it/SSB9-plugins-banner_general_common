/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.junit.After
import org.junit.Before
import org.junit.Test

class PersonIdentificationCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    def personIdentificationCompositeService

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
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
        lstBannerId.add '613238760'
        map.put 'bannerId', lstBannerId

        def lstEnterpriseId = [] as List
        lstEnterpriseId.add '683'
        map.put 'enterpriseId', lstEnterpriseId

        def lstSourcedId = [] as List
        lstSourcedId.add '252'
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
        assertEquals "613238760", personIdentificationDecorator.bannerId
        assertEquals "Banner", personIdentificationDecorator.dataOrigin
        assertEquals "025EB9EF4906F0D0E05018958B283255", personIdentificationDecorator.enterpriseId
        assertEquals "Karen", personIdentificationDecorator.firstName
        assertEquals "Karen S Hevermeyer", personIdentificationDecorator.fullName
        assertEquals "2600", personIdentificationDecorator.imsSourcedIdBase.sourcedId
        assertEquals "Hevermeyer", personIdentificationDecorator.lastName
        assertEquals "S", personIdentificationDecorator.middleName
        assertEquals null, personIdentificationDecorator.surnamePrefix
        assertEquals null, personIdentificationDecorator.thirdPartyAccess.externalUser
        assertEquals null, personIdentificationDecorator.thirdPartyAccess.ldapUserMapping

        personIdentificationDecorator = personList.get(1)
        assertEquals "SYS000001", personIdentificationDecorator.bannerId
        assertEquals "Banner", personIdentificationDecorator.dataOrigin
        assertEquals "683", personIdentificationDecorator.enterpriseId
        assertEquals "Terry", personIdentificationDecorator.firstName
        assertEquals "Terry Akers", personIdentificationDecorator.fullName
        assertEquals "189", personIdentificationDecorator.imsSourcedIdBase.sourcedId
        assertEquals "Akers", personIdentificationDecorator.lastName
        assertEquals null, personIdentificationDecorator.middleName
        assertEquals null, personIdentificationDecorator.surnamePrefix
        assertEquals "takers", personIdentificationDecorator.thirdPartyAccess.externalUser
        assertEquals null, personIdentificationDecorator.thirdPartyAccess.ldapUserMapping

        personIdentificationDecorator = personList.get(2)
        assertEquals "HP", personIdentificationDecorator.bannerId
        assertEquals "Banner", personIdentificationDecorator.dataOrigin
        assertEquals "025EB9EF5CCDF0D0E05018958B283255", personIdentificationDecorator.enterpriseId
        assertEquals "Harry The Great.............................................", personIdentificationDecorator.firstName
        assertEquals "Harry The Great............................................. Gotta Love Him ............................................. Potter Super Duper Extra Special Crazy Long Name ...........", personIdentificationDecorator.fullName
        assertEquals "252", personIdentificationDecorator.imsSourcedIdBase.sourcedId
        assertEquals "Potter Super Duper Extra Special Crazy Long Name ...........", personIdentificationDecorator.lastName
        assertEquals "Gotta Love Him .............................................", personIdentificationDecorator.middleName
        assertEquals null, personIdentificationDecorator.surnamePrefix
        assertEquals "harry", personIdentificationDecorator.thirdPartyAccess.externalUser
        assertEquals "HP", personIdentificationDecorator.thirdPartyAccess.ldapUserMapping

        personIdentificationDecorator = personList.get(3)
        assertEquals "A00000671", personIdentificationDecorator.bannerId
        assertEquals null, personIdentificationDecorator.dataOrigin
        assertEquals "2134", personIdentificationDecorator.enterpriseId
        assertEquals "Faculty", personIdentificationDecorator.firstName
        assertEquals "Faculty PCC", personIdentificationDecorator.fullName
        assertEquals "913", personIdentificationDecorator.imsSourcedIdBase.sourcedId
        assertEquals "PCC", personIdentificationDecorator.lastName
        assertEquals null, personIdentificationDecorator.middleName
        assertEquals null, personIdentificationDecorator.surnamePrefix
        assertEquals "fpcc", personIdentificationDecorator.thirdPartyAccess.externalUser
        assertEquals "systest33", personIdentificationDecorator.thirdPartyAccess.ldapUserMapping

        personIdentificationDecorator = personList.get(4)
        assertEquals "SJGRIM", personIdentificationDecorator.bannerId
        assertEquals "Banner", personIdentificationDecorator.dataOrigin
        assertEquals "025EB9EF618DF0D0E05018958B283255", personIdentificationDecorator.enterpriseId
        assertEquals "Warren", personIdentificationDecorator.firstName
        assertEquals "Warren Zevon Grim", personIdentificationDecorator.fullName
        assertEquals "1234", personIdentificationDecorator.imsSourcedIdBase.sourcedId
        assertEquals "Grim", personIdentificationDecorator.lastName
        assertEquals "Zevon", personIdentificationDecorator.middleName
        assertEquals null, personIdentificationDecorator.surnamePrefix
        assertEquals "sgrim", personIdentificationDecorator.thirdPartyAccess.externalUser
        assertEquals null, personIdentificationDecorator.thirdPartyAccess.ldapUserMapping
    }
}