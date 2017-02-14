/*******************************************************************************
 Copyright 2016-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm

import groovy.mock.interceptor.MockFor
import groovy.mock.interceptor.StubFor
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.common.GeneralCommonConstants
import net.hedtech.banner.general.common.GeneralValidationCommonConstants
import net.hedtech.banner.general.overall.ImsSourcedIdBase
import net.hedtech.banner.general.overall.IntegrationConfiguration
import net.hedtech.banner.general.overall.IntegrationConfigurationService
import net.hedtech.banner.general.overall.PidmAndUDCIdMapping
import net.hedtech.banner.general.overall.ThirdPartyAccess
import net.hedtech.banner.general.overall.ldm.GlobalUniqueIdentifier
import net.hedtech.banner.general.overall.ldm.LdmService
import net.hedtech.banner.general.overall.ldm.v8.PersonCredentialsV8
import net.hedtech.banner.general.person.PersonIdentificationNameCurrent
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
import org.junit.Before
import org.junit.Test


class PersonCredentialV8CompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    def personCredentialV8CompositeService

    //used to save alternative ids
    PersonCredentialService personCredentialService
    IntegrationConfigurationService integrationConfigurationService


    def person
    String guid
    String guidForSSN
    String guidForUpdates
    String guidForAltIds
    String bannerSourcedId
    String bannerUserName
    String bannerUdcId

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        initializeDataReferences()
    }

    private void initializeDataReferences() {
        person = PersonUtility.getPerson("HOSP0001")
        guid = GlobalUniqueIdentifier.fetchByDomainKeyAndLdmName(person?.pidm?.toString(), GeneralCommonConstants.PERSONS_LDM_NAME)?.guid
        bannerSourcedId = ImsSourcedIdBase.findByPidm(person?.pidm)?.sourcedId
        bannerUserName = ThirdPartyAccess.findByPidm(person?.pidm)?.externalUser
        bannerUdcId = PidmAndUDCIdMapping.findByPidm(person?.pidm)?.udcId

        guidForSSN = GlobalUniqueIdentifier.fetchByDomainKeyAndLdmName("371".toString(), GeneralCommonConstants.PERSONS_LDM_NAME)?.guid
        log.debug(guidForSSN)

        guidForAltIds = GlobalUniqueIdentifier.fetchByDomainKeyAndLdmName("254".toString(), GeneralCommonConstants.PERSONS_LDM_NAME)?.guid
        log.debug(guidForAltIds)


        guidForUpdates = GlobalUniqueIdentifier.fetchByDomainKeyAndLdmName("145".toString(), GeneralCommonConstants.PERSONS_LDM_NAME)?.guid

        Map additionalIdTypeCodeToIdMap = [:]

        def clgPersonId = integrationConfigurationService.fetchByProcessCodeAndSettingName(GeneralValidationCommonConstants.PROCESS_CODE, "CREDENTIALS.COLLEAGUE_ID")
        additionalIdTypeCodeToIdMap.put(clgPersonId.value, "_colleaguePersonId_")

        def elevateConfig = integrationConfigurationService.fetchByProcessCodeAndSettingName(GeneralValidationCommonConstants.PROCESS_CODE, "CREDENTIALS.ELEVATE_ID")
        additionalIdTypeCodeToIdMap.put(elevateConfig.value, "_elevateId_")

        def clgConfig = integrationConfigurationService.fetchByProcessCodeAndSettingName(GeneralValidationCommonConstants.PROCESS_CODE, "CREDENTIALS.COLLEAGUE_USERNAME")
        additionalIdTypeCodeToIdMap.put(clgConfig.value, "_colleagueUserName_")

        personCredentialService.createOrUpdateAdditionalIDs(254, additionalIdTypeCodeToIdMap)
    }

    @Test
    void testUpdate_PersonsCredentials_bannerId() {
        Map request = [id         : guidForUpdates,
                       credentials: [[type: "bannerId", value: "CHANGED20"]]
        ]

        personCredentialV8CompositeService.update(request)

        PersonCredentialsV8 decorator = personCredentialV8CompositeService.get(guidForUpdates)
        assertNotNull decorator
        assertEquals guidForUpdates, decorator.id
        assertEquals "CHANGED20", decorator.credentials.find { it.type == "bannerId" }.value
    }


    @Test
    void testUpdate_PersonsCredentials_ElevateId() {

        //test create
        Map request = [id         : guidForUpdates,
                       credentials: [[type: "elevateId", value: "elevateId_test"]]
        ]

        personCredentialV8CompositeService.update(request)

        PersonCredentialsV8 decorator = personCredentialV8CompositeService.get(guidForUpdates)
        assertNotNull decorator
        assertEquals guidForUpdates, decorator.id
        assertEquals "elevateId_test", decorator.credentials.find { it.type == "elevateId" }.value

        //test update
        request = [id         : guidForUpdates,
                   credentials: [[type: "elevateId", value: "elevateId_test2"]]]

        personCredentialV8CompositeService.update(request)

        decorator = personCredentialV8CompositeService.get(guidForUpdates)
        assertNotNull decorator
        assertEquals guidForUpdates, decorator.id
        assertEquals "elevateId_test2", decorator.credentials.find { it.type == "elevateId" }.value
    }

    @Test
    void testUpdate_PersonsCredentials_colleaguePersonId() {

        //test create
        Map request = [id         : guidForUpdates,
                       credentials: [[type: "colleaguePersonId", value: "colleaguePersonId_test"]]
        ]

        personCredentialV8CompositeService.update(request)

        PersonCredentialsV8 decorator = personCredentialV8CompositeService.get(guidForUpdates)
        assertNotNull decorator
        assertEquals guidForUpdates, decorator.id
        assertEquals "colleaguePersonId_test", decorator.credentials.find { it.type == "colleaguePersonId" }.value

        //test update
        request = [id         : guidForUpdates,
                   credentials: [[type: "colleaguePersonId", value: "colleaguePersonId_test2"]]]

        personCredentialV8CompositeService.update(request)

        decorator = personCredentialV8CompositeService.get(guidForUpdates)
        assertNotNull decorator
        assertEquals guidForUpdates, decorator.id
        assertEquals "colleaguePersonId_test2", decorator.credentials.find { it.type == "colleaguePersonId" }.value
    }

    @Test
    void testUpdate_PersonsCredentials_colleagueUserName() {

        //test create
        Map request = [id         : guidForUpdates,
                       credentials: [[type: "colleagueUserName", value: "colleagueUserName_test"]]
        ]

        personCredentialV8CompositeService.update(request)

        PersonCredentialsV8 decorator = personCredentialV8CompositeService.get(guidForUpdates)
        assertNotNull decorator
        assertEquals guidForUpdates, decorator.id
        assertEquals "colleagueUserName_test", decorator.credentials.find { it.type == "colleagueUserName" }.value

        //test update
        request = [id         : guidForUpdates,
                   credentials: [[type: "colleagueUserName", value: "colleagueUserName_test2"]]]

        personCredentialV8CompositeService.update(request)

        decorator = personCredentialV8CompositeService.get(guidForUpdates)
        assertNotNull decorator
        assertEquals guidForUpdates, decorator.id
        assertEquals "colleagueUserName_test2", decorator.credentials.find { it.type == "colleagueUserName" }.value
    }

    @Test
    void testUpdate_PersonsCredentials_ssn() {

        //test create
        Map request = [id         : guidForUpdates,
                       credentials: [[type: "ssn", value: "ssn_test"]]
        ]

        personCredentialV8CompositeService.update(request)

        PersonCredentialsV8 decorator = personCredentialV8CompositeService.get(guidForUpdates)
        assertNotNull decorator
        assertEquals guidForUpdates, decorator.id
        assertEquals "ssn_test", decorator.credentials.find { it.type == "ssn" }.value

        //test update

        request = [id         : guidForUpdates,
                   credentials: [[type: "ssn", value: "ssn_test2"]]]


        IntegrationConfigurationService.metaClass.canUpdatePersonSSN = { return true }
        personCredentialV8CompositeService.update(request)

        decorator = personCredentialV8CompositeService.get(guidForUpdates)
        assertNotNull decorator
        assertEquals guidForUpdates, decorator.id
        assertEquals "ssn_test2", decorator.credentials.find { it.type == "ssn" }.value

    }

    @Test
    void testList_PersonsCredentials() {
        setAcceptHeader("application/vnd.hedtech.integration.v8+json")
        def personsCredentials = personCredentialV8CompositeService.list([max: '500', offset: '0'])
        assertTrue personsCredentials.size() <= 500
        assertTrue personsCredentials.size() > 0
        assertTrue personsCredentials.size() <= personCredentialV8CompositeService.count([max: '500', offset: '0'])
        PersonCredentialsV8 decorator = personsCredentials[0]
        assertNotNull decorator
        GlobalUniqueIdentifier globalUniqueIdentifier = GlobalUniqueIdentifier.fetchByGuid(GeneralCommonConstants.PERSONS_LDM_NAME, decorator.id)
        assertNotNull globalUniqueIdentifier
        assertNotNull globalUniqueIdentifier.domainKey
        Integer pidm = globalUniqueIdentifier.domainKey.toInteger()
        String bannerId = PersonIdentificationNameCurrent.fetchByPidm(pidm)?.bannerId
        assertEquals bannerId, decorator.credentials.find { it.type == "bannerId" }.value
        String bannerSourcedId = ImsSourcedIdBase.findByPidm(pidm)?.sourcedId
        if (bannerSourcedId) {
            assertEquals bannerSourcedId, decorator.credentials.find { it.type == "bannerSourcedId" }.value
        }
        String bannerUserName = ThirdPartyAccess.findByPidm(pidm)?.externalUser
        if (bannerUserName) {
            assertEquals bannerUserName, decorator.credentials.find { it.type == "bannerUserName" }.value
        }
        String bannerUdcId = PidmAndUDCIdMapping.findByPidm(pidm)?.udcId
        if (bannerUdcId) {
            assertEquals bannerUdcId, decorator.credentials.find { it.type == "bannerUdcId" }.value
        }
    }

    @Test
    void testGet_PersonCredentials() {
        setAcceptHeader("application/vnd.hedtech.integration.v8+json")
        PersonCredentialsV8 decorator = personCredentialV8CompositeService.get(guid)
        assertNotNull decorator
        assertEquals guid, decorator.id
        assertEquals person.bannerId, decorator.credentials.find { it.type == "bannerId" }.value
        assertEquals bannerSourcedId, decorator.credentials.find { it.type == "bannerSourcedId" }.value
        assertEquals bannerUserName, decorator.credentials.find { it.type == "bannerUserName" }.value
        assertEquals bannerUdcId, decorator.credentials.find { it.type == "bannerUdcId" }.value
    }

    @Test
    void testGet_PersonCredentials_ssnCredentials() {
        setAcceptHeader("application/vnd.hedtech.integration.v8+json")
        PersonCredentialsV8 decorator = personCredentialV8CompositeService.get(guidForSSN)
        assertNotNull decorator
        assertEquals guidForSSN, decorator.id

        assertNotNull decorator.credentials.find {
            it.type == CredentialType.SOCIAL_SECURITY_NUMBER.versionToEnumMap["v8"]
        }.value
    }

    @Test
    void testGet_PersonCredentials_altCredentials() {

        PersonCredentialsV8 decorator = personCredentialV8CompositeService.get(guidForAltIds)
        assertNotNull decorator
        assertEquals guidForAltIds, decorator.id

        assertNotNull decorator.credentials.find {
            it.type == CredentialType.COLLEAGUE_USER_NAME.versionToEnumMap["v8"]
        }.value
        assertNotNull decorator.credentials.find { it.type == CredentialType.ELEVATE_ID.versionToEnumMap["v8"] }.value
        assertNotNull decorator.credentials.find {
            it.type == CredentialType.COLLEAGUE_PERSON_ID.versionToEnumMap["v8"]
        }.value
    }

    @Test
    void testGet_InvalidPersonCredentials() {
        setAcceptHeader("application/vnd.hedtech.integration.v8+json")
        String guid = 'xxxxx'
        try {
            personCredentialV8CompositeService.get(guid)
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
