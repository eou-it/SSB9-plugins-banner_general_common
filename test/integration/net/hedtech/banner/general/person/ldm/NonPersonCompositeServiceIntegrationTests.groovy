/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.common.GeneralCommonConstants
import net.hedtech.banner.general.overall.ldm.GlobalUniqueIdentifier
import net.hedtech.banner.general.overall.ldm.LdmService
import net.hedtech.banner.general.overall.ldm.v6.NonPersonDecorator
import net.hedtech.banner.general.person.PersonEmail
import net.hedtech.banner.general.person.PersonEmailService
import net.hedtech.banner.general.person.PersonIdentificationNameCurrent
import net.hedtech.banner.general.person.PersonTelephoneService
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.general.system.ldm.EmailTypeCompositeService
import net.hedtech.banner.general.system.ldm.PhoneTypeCompositeService
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
import org.junit.Before
import org.junit.Ignore
import org.junit.Test


class NonPersonCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    def nonPersonCompositeService


    def person
    String guid

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        initializeDataReferences()
    }

    private void initializeDataReferences() {
        person = PersonIdentificationNameCurrent.fetchByBannerId("A00010018")
        guid = GlobalUniqueIdentifier.fetchByDomainKeyAndLdmName(person?.pidm?.toString(), GeneralCommonConstants.NON_PERSONS_LDM_NAME)?.guid
    }

    @Ignore
    @Test
    void testList_NonPersons_v6() {
        setAcceptHeader("application/vnd.hedtech.integration.v6+json")
        def nonPersons = nonPersonCompositeService.list([max: '500', offset: '0'])
        assertTrue nonPersons.size() <= 500
        assertTrue nonPersons.size() > 0
        assertTrue nonPersons.size() <= nonPersonCompositeService.count([max: '500', offset: '0'])
        NonPersonDecorator decorator = nonPersons[0]
        assertNotNull decorator
        GlobalUniqueIdentifier globalUniqueIdentifier = GlobalUniqueIdentifier.fetchByGuid(GeneralCommonConstants.NON_PERSONS_LDM_NAME, decorator.guid)
        assertNotNull globalUniqueIdentifier
        assertNotNull globalUniqueIdentifier.domainKey
        Integer pidm = globalUniqueIdentifier.domainKey.toInteger()
        PersonIdentificationNameCurrent person = PersonIdentificationNameCurrent.fetchByPidm(pidm)
        assertNotNull person
        assertEquals person.bannerId, decorator.credentials.find { it.type == "bannerId" }.value
        assertEquals person.lastName, decorator.title
        assertNotNull decorator.roles.find { it.role == "affiliate" }
    }

    @Ignore
    @Test
    void testGet_NonPersons_v6() {
        setAcceptHeader("application/vnd.hedtech.integration.v6+json")
        NonPersonDecorator decorator = nonPersonCompositeService.get(guid)
        assertNotNull decorator
        assertEquals guid, decorator.guid
        assertEquals person.bannerId, decorator.credentials.find { it.type == "bannerId" }.value
        assertEquals person.lastName, decorator.title
        assertFalse decorator.roles.isEmpty()
        assertNotNull OrganizationRoleName.values().versionToEnumMap["v6"].containsAll(decorator.roles.role)
        assertFalse decorator.emails.isEmpty()
       def person =  PersonUtility.getPerson(decorator.credentials.value[0])
        PersonEmail.fetchByPidmsAndActiveStatus([person.pidm])
        decorator.emails.each{ email ->
        }


    }

    @Test
    void testGet_InvalidNonPersons_v6() {
        setAcceptHeader("application/vnd.hedtech.integration.v6+json")
        String guid = 'xxxxx'
        try {
            nonPersonCompositeService.get(guid)
            fail('Invalid guid')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'NotFoundException'
        }
    }

    private void setAcceptHeader(String mediaType) {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Accept", mediaType)
    }
}
