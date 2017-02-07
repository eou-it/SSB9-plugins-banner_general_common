/*******************************************************************************
 Copyright 2016-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm

import net.hedtech.banner.general.overall.ldm.v6.CredentialV6
import net.hedtech.banner.general.overall.ldm.v6.PersonCredentialsV6
import net.hedtech.banner.general.person.view.PersonAdvancedSearchView
import org.springframework.transaction.annotation.Transactional

@Transactional
class PersonCredentialV6CompositeService extends AbstractPersonCredentialCompositeService {


    protected def createPersonCredentialDataModel(final Map dataMapForSingle) {

        PersonCredentialsV6 persCredentialsDecorator = new PersonCredentialsV6(dataMapForSingle.guid)

        persCredentialsDecorator.credentials = createCredentialObjects(dataMapForSingle.credentials)

        return persCredentialsDecorator
    }


    def createCredentialObjects(def credentials) {
        def decorators = []
        if (credentials) {
            credentials.each {

                CredentialV6 personCredential
                if (it.type && it.value) {
                    personCredential = new CredentialV6(it.type.versionToEnumMap["v6"], it.value)
                }

                decorators << personCredential
            }
        }
        return decorators
    }

    protected def getCredentialTypeToAdditionalIdTypeCodeMap() {
        return [:]
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
    protected void prepareDataMapForAll_ListExtension(Collection<Map> entities, Map dataMapForAll) {

    }

    @Override
    protected void prepareDataMapForSingle_ListExtension(Map entity, Map dataMapForAll, Map dataMapForSingle) {
        dataMapForSingle.credentials.removeAll(  { it.type==CredentialType.SOCIAL_SECURITY_NUMBER })
    }


}