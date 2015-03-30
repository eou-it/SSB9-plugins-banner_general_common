/*********************************************************************************
 Copyright 2nullnull9-2null13 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.overall

import net.hedtech.banner.general.overall.ldm.GlobalUniqueIdentifier
import net.hedtech.banner.general.person.PersonIdentificationName

class PersonIdentificationCompositeService {

    static final String ldmName = 'persons'

    def list(Map params) {

        def identificationEntities = [:]
        def bannerIds
        def enterpriseIds
        def sourcedIds
        def ldapUserMappings
        def externalUsers

        if( params.bannerId ) {
            bannerIds = params.list('bannerId')
            PersonIdentificationName.findAllByBannerIdInList(bannerIds).each {
                def identificationEntity = identificationEntities[it.pidm]
                if( it.changeIndicator ) {
                    identificationEntities.put(it.pidm, addEntityToMap(identificationEntity, null))
                }
                else {
                    identificationEntities.put(it.pidm, addEntityToMap(identificationEntity, it))
                }
            }
        }
        if( params.enterpriseId ) {
            enterpriseIds = params.list('enterpriseId')
            PidmAndUDCIdMapping.fetchByUdcList(enterpriseIds).each {
                def identificationEntity = identificationEntities[it.pidm]
                identificationEntities.put(it.pidm, addEntityToMap(identificationEntity, it) )
            }
        }
        if( params.sourcedId ) {
            sourcedIds = params.list('sourcedId')
            ImsSourcedIdBase.findAllBySourcedIdInList(sourcedIds).each {
                def identificationEntity = identificationEntities[it.pidm]
                identificationEntities.put(it.pidm, addEntityToMap(identificationEntity, it))
            }

        }
        if( params.ldapUserMapping ) {
            ldapUserMappings = params.list('ldapUserMapping')
            ThirdPartyAccess.findAllByLdapUserMappingInList(ldapUserMappings).each {
                def identificationEntity = identificationEntities[it.pidm]
                identificationEntities.put(it.pidm, addEntityToMap(identificationEntity, it))
            }

        }
        if( params.externalUser ) {
            externalUsers = params.list('externalUser')
            ThirdPartyAccess.findAllByExternalUserInList(externalUsers).each {
                def identificationEntity = identificationEntities[it.pidm]
                identificationEntities.put(it.pidm, addEntityToMap(identificationEntity, it))
            }

        }
        if( buildSelectionList(identificationEntities,
                'personidentificationname').size() > 0)
        identificationEntities = processResults(identificationEntities,
                                                PersonIdentificationName.fetchBannerPersonList(buildSelectionList(identificationEntities,
                                                                                                                   'personidentificationname')))
        identificationEntities = processResults(identificationEntities,
                                                PidmAndUDCIdMapping.findAllByPidmInList(buildSelectionList(identificationEntities,
                                                                                                        'pidmandudcidmapping')))
        identificationEntities = processResults(identificationEntities,
                                                ImsSourcedIdBase.findAllByPidmInList(buildSelectionList(identificationEntities,
                                                                                                        'imssourcedidbase')))
        identificationEntities = processResults(identificationEntities,
                                                ThirdPartyAccess.findAllByPidmInList(buildSelectionList(identificationEntities,
                                                                                                        'thirdpartyaccess')))

        def domainIds = []
        identificationEntities.each { key, value ->
            domainIds << value.personidentificationname.id
        }

        buildPersonGuids(domainIds, identificationEntities)

        def results = []
        identificationEntities.each { key, value ->
            results << new PersonIdentificationDecorator(value)
        }


        def resultsList
        try {  // Avoid restful-api plugin dependencies.
            resultsList = this.class.classLoader.loadClass('net.hedtech.restfulapi.PagedResultArrayList').newInstance(results, results.size())
        }
        catch (ClassNotFoundException e) {
            resultsList = results
        }
        resultsList
    }

    def buildSelectionList( Map entities, String type ) {
        def toProcess = []
        entities.each { key, value ->
            if( value[type] == null ) toProcess << key
        }
        toProcess
    }

    def processResults( Map entities, def results ) {
        results.each { result ->
            def child = entities[result?.pidm]
            child.put(result.class.simpleName.toLowerCase(), result)
            entities.put(result?.pidm, child)
        }
        entities
    }

    def addEntityToMap( def identificationEntity, entity ) {
        if (!(identificationEntity instanceof Map)) {
            identificationEntity = ['personidentificationname': null,
                                    'pidmandudcdmapping': null,
                                    'thirdpartyaccess': null,
                                    'imssourcedidbase': null]
        }
        identificationEntity.put( (entity ? entity.class.simpleName.toLowerCase() : 'personidentificationname'), entity)
        identificationEntity
    }


    def buildPersonGuids(List domainIds, Map identificationEntities) {
        GlobalUniqueIdentifier.findAllByLdmNameAndDomainIdInList(ldmName, domainIds).each { guid ->
            def currentRecord = identificationEntities.get(guid.domainKey.toInteger())
            currentRecord.guid = guid.guid
            identificationEntities.put(guid.domainKey.toInteger(), currentRecord)
        }
        identificationEntities
    }

}
