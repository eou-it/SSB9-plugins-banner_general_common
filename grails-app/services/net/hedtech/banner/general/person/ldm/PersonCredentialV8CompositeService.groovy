/*******************************************************************************
 Copyright 2016-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm

import net.hedtech.banner.general.common.GeneralValidationCommonConstants
import net.hedtech.banner.general.overall.IntegrationConfiguration
import net.hedtech.banner.general.overall.IntegrationConfigurationService
import net.hedtech.banner.general.overall.ldm.v8.CredentialV8
import net.hedtech.banner.general.overall.ldm.v8.PersonCredentialsV8
import net.hedtech.banner.general.person.PersonBasicPersonBase
import net.hedtech.banner.general.person.view.PersonAdvancedSearchView
import org.springframework.transaction.annotation.Transactional

@Transactional
class PersonCredentialV8CompositeService extends AbstractPersonCredentialCompositeService {

    IntegrationConfigurationService integrationConfigurationService

    protected def createPersonCredentialDataModel(final Map dataMapForSingle) {

        PersonCredentialsV8 persCredentialsDecorator = new PersonCredentialsV8(dataMapForSingle.guid)

        persCredentialsDecorator.credentials = createCredentialObjects(dataMapForSingle.credentials)

        return persCredentialsDecorator
    }


    def createCredentialObjects(def credentials) {
        def decorators = []
        if (credentials) {
            credentials.each {

                CredentialV8 personCredential
                if (it.type && it.value) {
                    personCredential = new CredentialV8(it.type.versionToEnumMap["v8"], it.value)
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


    @Override
    protected void prepareDataMapForAll_ListExtension(Collection<Map> entities, Map dataMapForAll) {

    }

    @Override
    protected void prepareDataMapForSingle_ListExtension(Map entity, Map dataMapForAll, Map dataMapForSingle) {

    }


}
