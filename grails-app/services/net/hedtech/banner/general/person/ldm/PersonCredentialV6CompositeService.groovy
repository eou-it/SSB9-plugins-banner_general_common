/*******************************************************************************
 Copyright 2016-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm

import net.hedtech.banner.general.common.GeneralValidationCommonConstants
import net.hedtech.banner.general.overall.ldm.v6.PersonCredential
import net.hedtech.banner.general.overall.ldm.v6.PersonCredentialsDecorator
import net.hedtech.banner.general.person.ldm.v1.Credential
import org.springframework.transaction.annotation.Transactional

@Transactional
class PersonCredentialV6CompositeService extends AbstractPersonCredentialCompositeService {


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
                    personCredential = new PersonCredential(it.type.versionToEnumMap["v6"], it.value)
                }

                decorators << personCredential
            }
        }
        return decorators
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

    }


}
