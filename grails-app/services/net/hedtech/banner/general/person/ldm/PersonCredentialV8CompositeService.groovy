/*******************************************************************************
 Copyright 2016-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm

import net.hedtech.banner.general.common.GeneralValidationCommonConstants
import net.hedtech.banner.general.overall.IntegrationConfiguration
import net.hedtech.banner.general.overall.IntegrationConfigurationService
import net.hedtech.banner.general.overall.ldm.v8.PersonCredential
import net.hedtech.banner.general.overall.ldm.v8.PersonCredentialsDecorator
import net.hedtech.banner.general.person.PersonBasicPersonBase
import org.springframework.transaction.annotation.Transactional

@Transactional
class PersonCredentialV8CompositeService extends AbstractPersonCredentialCompositeService {

    IntegrationConfigurationService integrationConfigurationService

    protected def createPersonCredentialDataModel(final Map dataMapForSingle) {

        PersonCredentialsDecorator persCredentialsDecorator = new PersonCredentialsDecorator(dataMapForSingle.guid)

        persCredentialsDecorator.credentials = createCredentialObjects(dataMapForSingle.credentials)

        return persCredentialsDecorator
    }


    def createCredentialObjects(def credentials) {
        def decorators = []
        if (credentials) {
            credentials.each {

                PersonCredential personCredential
                if (it.type && it.value) {
                    personCredential = new PersonCredential(it.type.versionToEnumMap["v8"], it.value)
                }

                decorators << personCredential
            }
        }
        return decorators
    }

    protected def getCredentialTypeToAdditionalIdTypeCodeMap() {
        def map = [:]

        IntegrationConfiguration intConfig = integrationConfigurationService.fetchByProcessCodeAndSettingName(GeneralValidationCommonConstants.PROCESS_CODE, "CREDENTIALS.ELEVATE_ID")
        map.put(CredentialType.ELEVATE_ID, intConfig.value)

        intConfig = integrationConfigurationService.fetchByProcessCodeAndSettingName(GeneralValidationCommonConstants.PROCESS_CODE, "CREDENTIALS.COLLEAGUE_ID")
        map.put(CredentialType.COLLEAGUE_PERSON_ID, intConfig.value)

        intConfig = integrationConfigurationService.fetchByProcessCodeAndSettingName(GeneralValidationCommonConstants.PROCESS_CODE, "CREDENTIALS.COLLEAGUE_USERNAME")
        map.put(CredentialType.COLLEAGUE_USER_NAME, intConfig.value)

        return map
    }

    /**
     * Creates map with data to be used in POST/PUT operations
     * @param content request payload
     * @return a Map with the request data
     */
    @Override
    protected extractDataFromRequestBody(Map content) {
        return null
    }

    @Override
    protected void prepareDataMapForAll_ListExtension(Collection<Object[]> entities, Map dataMapForAll) {

    }

    @Override
    protected void prepareDataMapForSingle_ListExtension(Object[] entity, Map dataMapForAll, Map dataMapForSingle) {
        PersonBasicPersonBase personBase = dataMapForAll.pidmToPersonBaseMap.get(dataMapForSingle.pidm)
        def existingSsn = dataMapForSingle.credentials?.find { it.type == CredentialType.SOCIAL_SECURITY_NUMBER }
        if (!existingSsn && personBase?.ssn) {
            dataMapForSingle.credentials << [type: CredentialType.SOCIAL_SECURITY_NUMBER, value: personBase.ssn]
        }
    }

}
