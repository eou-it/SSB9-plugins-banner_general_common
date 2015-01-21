/*******************************************************************************
 Copyright 2014-2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm

import net.hedtech.banner.exceptions.BusinessLogicValidationException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.overall.IntegrationConfiguration
import net.hedtech.banner.general.overall.ldm.GlobalUniqueIdentifierService
import net.hedtech.banner.general.person.AdditionalID
import net.hedtech.banner.general.person.PersonAddress
import net.hedtech.banner.general.person.PersonBasicPersonBase
import net.hedtech.banner.general.person.PersonEmail
import net.hedtech.banner.general.person.PersonIdentificationNameAlternate
import net.hedtech.banner.general.person.PersonIdentificationNameCurrent
import net.hedtech.banner.general.person.PersonRace
import net.hedtech.banner.general.person.PersonTelephone
import net.hedtech.banner.general.person.ldm.v1.Address
import net.hedtech.banner.general.person.ldm.v1.Credential
import net.hedtech.banner.general.overall.ldm.GlobalUniqueIdentifier
import net.hedtech.banner.general.overall.ldm.LdmService
import net.hedtech.banner.general.person.ldm.v1.Email
import net.hedtech.banner.general.person.ldm.v1.Name
import net.hedtech.banner.general.person.ldm.v1.Person
import net.hedtech.banner.general.person.ldm.v1.Phone
import net.hedtech.banner.general.system.AdditionalIdentificationType
import net.hedtech.banner.general.system.AddressType
import net.hedtech.banner.general.system.County
import net.hedtech.banner.general.system.EmailType
import net.hedtech.banner.general.system.Ethnicity
import net.hedtech.banner.general.system.InstitutionalDescription
import net.hedtech.banner.general.system.MaritalStatus
import net.hedtech.banner.general.system.Nation
import net.hedtech.banner.general.system.State
import net.hedtech.banner.general.system.TelephoneType
import net.hedtech.banner.general.system.ldm.v1.Metadata
import net.hedtech.banner.general.system.ldm.v1.RaceDetail
import net.hedtech.banner.restfulapi.RestfulApiValidationUtility
import org.apache.log4j.Logger
import org.springframework.transaction.annotation.Propagation
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes
import org.springframework.transaction.annotation.Transactional
import java.sql.CallableStatement
import java.sql.SQLException

@Transactional
class PersonCompositeService extends LdmService {

    def personIdentificationNameCurrentService
    def personBasicPersonBaseService
    def personAddressService
    def personTelephoneService
    def personEmailService
    def globalUniqueIdentifierService
    def maritalStatusCompositeService
    def ethnicityCompositeService
    def raceCompositeService
    def personRaceService
    def userRoleCompositeService
    def additionalIDService

    static final String ldmName = 'persons'
    static final String PERSON_ADDRESS_TYPE = "PERSON.ADDRESSES.ADDRESSTYPE"
    static final String PERSON_REGION = "PERSON.ADDRESSES.REGION"
    static final String PERSON_POSTAL_CODE = "PERSON.ADDRESSES.POSTAL.CODE"
    static final String PERSON_PHONE_TYPE = "PERSON.PHONES.PHONETYPE"
    static final String PERSON_EMAIL_TYPE = "PERSON.EMAILS.EMAILTYPE"
    static final String PROCESS_CODE = "LDM"
    static final String PERSON_MATCH_RULE = "PERSON.MATCHRULE"
    private static final String DOMAIN_KEY_DELIMITER = '-^'
    private static final String PERSON_EMAILS_LDM_NAME = "person-emails"
    private static final String PERSON_EMAIL_TYPE_PREFERRED = "Preferred"


    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    def get(id) {
        def entity = GlobalUniqueIdentifier.fetchByLdmNameAndGuid(ldmName, id)
        if (!entity) {
            throw new ApplicationException(GlobalUniqueIdentifierService.API, new NotFoundException(id: Person.class.simpleName))
        }
        List<PersonIdentificationNameCurrent> personIdentificationList =
                PersonIdentificationNameCurrent.findAllByPidmInList([entity.domainKey?.toInteger()])
        def resultList = buildLdmPersonObjects(personIdentificationList)
        resultList.get(entity.domainKey?.toInteger())
    }


    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    def list(params) {
        def pidms = []
        def resultList = [:]

        def sortParams = [:]
        def allowedSortFields = ["firstName", "lastName"]
        if (params.containsKey('sort')) sortParams.put('sort', params.sort)
        if (params.containsKey('order')) sortParams.put('order', params.order)
        if (params.sort) {
            RestfulApiValidationUtility.validateSortField(sortParams.sort, allowedSortFields)
        } else {
            sortParams.put('sort', allowedSortFields[1])
        }

        if (sortParams.order) {
            RestfulApiValidationUtility.validateSortOrder(sortParams.order)
        } else {
            sortParams.put('order', "asc")
        }

        // Check if it is qapi request, if so do matching
        if (RestfulApiValidationUtility.isQApiRequest(params)) {
            log.info "Person Duplicate service:"
            log.debug "Request parameters: ${params}"

            def primaryName = params.names.find { primaryNameType ->
                primaryNameType.nameType == "Primary"
            }

            if (primaryName?.firstName && primaryName?.lastName) {
                pidms = searchPerson(params)
            } else {
                throw new ApplicationException("Person", new BusinessLogicValidationException("missing.first.last.name",[]))
            }
        } else {
            //Add DynamicFinder on PersonIdentificationName in future.
            if (params.role) {
                pidms = userRoleCompositeService.fetchAllByRole([role:params.role, sortAndPaging:sortParams])
            } else {
                throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("role.required",[]))
            }

        }
        if( pidms.size() ) {
            def pageParams = [:]
            //Need to provide pre-sorted full lists of pidms for count...
            if (params.containsKey('max')) pageParams.put('max', params.max)
            if (params.containsKey('offset')) pageParams.put('offset', params.offset)
            RestfulApiValidationUtility.correctMaxAndOffset(pageParams, 500, 0)
            pageParams.offset = pageParams.offset ?: "0"
            def endCount = (pageParams.max.toInteger() + pageParams.offset.toInteger()) > (pidms.size() - 1) ?
                    pidms.size() - 1 : pageParams.max.toInteger() + pageParams.offset.toInteger() - 1

            List<PersonIdentificationNameCurrent> personIdentificationList =
                    PersonIdentificationNameCurrent.findAllByPidmInList(pidms[pageParams.offset.toInteger()..endCount], sortParams)
            resultList = buildLdmPersonObjects(personIdentificationList)
        }
        try {  // Avoid restful-api plugin dependencies.
            resultList = this.class.classLoader.loadClass('net.hedtech.restfulapi.PagedResultArrayList').newInstance(resultList?.values() ?: [], pidms?.size())
        }
        catch (ClassNotFoundException e) {
            resultList = resultList.values()
        }
        resultList
    }


    def create(Map person) {
        Map<Integer, Person> persons = [:]
        def newPersonIdentification

        if (person?.credentials instanceof List) {
            person?.credentials?.each { it ->
                if (it instanceof Map) {
                    def allowedCredentialTypes = ["Social Security Number", "Social Insurance Number", "Banner ID", Credential.additionalIdMap.ELV8]
                    validateCredentialType(it.credentialType, allowedCredentialTypes, it.credentialId)
                    person = createSSN(it.credentialType, it.credentialId, person)
                }
            }
        }

        if (person.names instanceof List) {
            person?.names?.each { it ->
                if (it instanceof Map) {
                    if (it.nameType == 'Primary') {
                        newPersonIdentification = it
                    }
                }
            }
        }
        Map metadata = person.metadata
        newPersonIdentification.put('bannerId', 'GENERATED')
        newPersonIdentification.put('entityIndicator', 'P')
        newPersonIdentification.put('changeIndicator', null)
        newPersonIdentification.put('dataOrigin', metadata?.dataOrigin)
        newPersonIdentification.remove('nameType') // ID won't generate if this is set.
        //Create the new PersonIdentification record
        PersonIdentificationNameCurrent newPersonIdentificationName = personIdentificationNameCurrentService.create(newPersonIdentification)
        //Fix the GUID if provided as DB will assign one
        if (person.guid) {
            updateGuidValue(newPersonIdentificationName.id, person.guid, ldmName)

        } else {
            def entity = GlobalUniqueIdentifier.findByLdmNameAndDomainId(ldmName, newPersonIdentificationName.id)
            person.put('guid', entity?.guid)
        }

        def additionalIds = []
        if (person?.credentials instanceof List) {
            person?.credentials?.each { it ->
                if (it instanceof Map) {
                    if (it.credentialType && Credential.additionalIdMap.containsValue(it.credentialType)) {
                        additionalIds << createOrUpdateAdditionalId(newPersonIdentificationName, it, metadata)
                    }
                }
            }
        }
        //Copy personBase attributes into person map from Primary names object.
        person.put('dataOrigin', metadata?.dataOrigin)
        person.put('namePrefix', newPersonIdentification.get('namePrefix'))
        person.put('nameSuffix', newPersonIdentification.get('nameSuffix'))
        person.put('preferenceFirstName', newPersonIdentification.get('preferenceFirstName'))
        //Translate enumerations and defaults
        person.put('sex', person?.sex == 'Male' ? 'M' : (person?.sex == 'Female' ? 'F' : (person?.sex == 'Unknown' ? 'N' : null)))
        def maritalStatus
        try {
            maritalStatus = person.maritalStatusDetail instanceof Map && person.maritalStatusDetail?.guid ? maritalStatusCompositeService.get(person.maritalStatusDetail?.guid) : null
        } catch (ApplicationException e) {
            throw new ApplicationException("PersonCompositeService", new BusinessLogicValidationException("maritalStatus.invalid",[]))
        }
        person.put('maritalStatus', maritalStatus ? MaritalStatus.findByCode(maritalStatus?.code) : null)
        def ethnicity
        try {
            if (person.ethnicityDetail instanceof Map) {
                ethnicity = person.ethnicityDetail?.guid ? ethnicityCompositeService.get(person.ethnicityDetail?.guid) : null
            }
        }
        catch (ApplicationException e) {
            if (e.wrappedException instanceof NotFoundException) {
                throw new ApplicationException("PersonCompositeService", new BusinessLogicValidationException("ethnicity.invalid",[]))
                ethnicity = null
            } else {
                throw e
            }
        }
        person.put('ethnicity', ethnicity ? Ethnicity.findByCode(ethnicity?.code) : null)
        person.put('ethnic', ethnicity ? ethnicity.ethnic : null)
        person.put('deadIndicator', person.get('deadDate') ? 'Y' : null)
        person.put('pidm', newPersonIdentificationName?.pidm)
        person.put('armedServiceMedalVetIndicator', false)
        PersonBasicPersonBase newPersonBase = personBasicPersonBaseService.create(person)
        def currentRecord = new Person(newPersonBase)
        currentRecord.guid = person.guid
        currentRecord.maritalStatusDetail = maritalStatus
        currentRecord.ethnicityDetail = ethnicity
        def name = new Name(newPersonIdentificationName, newPersonBase)
        name.setNameType("Primary")
        currentRecord.names << name
        //Store the credential we already have
        currentRecord.credentials = []
        currentRecord.credentials << new Credential("Banner ID", newPersonIdentificationName.bannerId, null, null)
        if (newPersonBase.ssn) {
            currentRecord.credentials << new Credential("Social Security Number",
                    newPersonBase.ssn,
                    null,
                    null)
        }
        if (person.addresses instanceof List) {
            person.addresses.collect { address ->
                getStateAndZip(address)
            }
        }
        persons.put(newPersonIdentificationName.pidm, currentRecord)
        def addresses = createAddresses(newPersonIdentificationName.pidm, metadata,
                person.addresses instanceof List ? person.addresses : [])
        persons = buildPersonAddresses(addresses, persons)
        def phones = createPhones(newPersonIdentificationName.pidm, metadata,
                person.phones instanceof List ? person.phones : [])
        persons = buildPersonTelephones(phones, persons)
        def emails = createPersonEmails(newPersonIdentificationName.pidm, metadata,
                person.emails instanceof List ? person.emails : [])
        persons = buildPersonEmails(emails, persons)
        def races = createRaces(newPersonIdentificationName.pidm, metadata,
                person.races instanceof List ? person.races : [])
        persons = buildPersonRaces(races, persons)
        persons = buildPersonRoles(persons)
        persons = buildPersonAdditionalIds(additionalIds, persons)
        persons.get(newPersonIdentificationName.pidm)
    }

    /**
     * Updates the Person Information like PersonIdentificationNameCurrent, PersonBasicPersonBase, Address
     * Telephones and Emails
     * @param person - Map containing the changes person details
     * @return person
     */
    def update(Map person) {
        String personGuid = person?.id?.trim()?.toLowerCase()
        GlobalUniqueIdentifier globalUniqueIdentifier = GlobalUniqueIdentifier.fetchByLdmNameAndGuid(ldmName, personGuid)

        if (personGuid) {
            if (!globalUniqueIdentifier) {
                if (!person.get('guid'))
                    person.put('guid', personGuid)
                //Per strategy when a GUID was provided, the create should happen.
                return create(person)
            }
        } else {
            throw new ApplicationException(GlobalUniqueIdentifierService.API, new NotFoundException(id: Person.class.simpleName))
        }

        if (person?.credentials instanceof List) {
            person?.credentials?.each { it ->
                if (it instanceof Map) {
                    def allowedCredentialTypes = ["Social Security Number", "Social Insurance Number", "Banner ID", Credential.additionalIdMap.ELV8]
                    validateCredentialType(it.credentialType, allowedCredentialTypes, it.credentialId)
                }
            }
        }

        def primaryName
        person?.names?.each { it ->
            if (it.nameType == 'Primary') {
                primaryName = it
            }
        }
        def pidmToUpdate = globalUniqueIdentifier.domainKey?.toInteger()
        List<PersonIdentificationNameCurrent> personIdentificationList = PersonIdentificationNameCurrent.findAllByPidmInList([pidmToUpdate])

        PersonIdentificationNameCurrent personIdentification
        personIdentificationList.each { identification ->
            if (identification.changeIndicator == null) {
                personIdentification = identification
            }
        }
        //update PersonIdentificationNameCurrent
        PersonIdentificationNameCurrent newPersonIdentificationName
        PersonIdentificationNameCurrent oldPersonIdentificationName = new PersonIdentificationNameCurrent(personIdentification.properties)
        if (primaryName) {
            if(primaryName.containsKey('firstName')) personIdentification.firstName = primaryName.firstName
            if(primaryName.containsKey('lastName')) personIdentification.lastName = primaryName.lastName
            if(primaryName.containsKey('middleName')) personIdentification.middleName = primaryName.middleName
            if(primaryName.containsKey('surnamePrefix')) personIdentification.surnamePrefix = primaryName.surnamePrefix
            if (!personIdentification.equals(oldPersonIdentificationName)) {
                PersonIdentificationNameAlternate.findAllByPidm(oldPersonIdentificationName.pidm).each { oldRecord ->
                    if (oldPersonIdentificationName.firstName == oldRecord.firstName &&
                            oldPersonIdentificationName.lastName == oldRecord.lastName &&
                            oldPersonIdentificationName.middleName == oldRecord.middleName &&
                            oldPersonIdentificationName.surnamePrefix == oldRecord.surnamePrefix &&
                            oldPersonIdentificationName.bannerId == oldRecord.bannerId &&
                            oldPersonIdentificationName.nameType == oldRecord.nameType &&
                            oldRecord.changeIndicator == 'N'
                    ) {
                        //Can't get around this, Hibernate updates before it deletes, triggering table-api errors.
                        PersonIdentificationNameAlternate.executeUpdate("delete from PersonIdentificationNameAlternate where id = :id", [id: oldRecord.id])
                    }
                }
                newPersonIdentificationName = personIdentificationNameCurrentService.update(personIdentification)
            }
        }
        person?.credentials?.each { it ->
            if (it.credentialType == 'Banner ID') {
                personIdentification.bannerId = it?.credentialId
                newPersonIdentificationName = personIdentificationNameCurrentService.update(personIdentification)
            } else if (it.credentialType == 'Elevate ID') {
                createOrUpdateAdditionalId(personIdentification, it, person?.metadata)
            }
        }
        if (!newPersonIdentificationName)
            newPersonIdentificationName = personIdentification
        //update PersonBasicPersonBase
        PersonBasicPersonBase newPersonBase = updatePersonBasicPersonBase(pidmToUpdate, newPersonIdentificationName, person, primaryName)
        def credentials = []
        if (newPersonBase && newPersonBase.ssn) {
            credentials << new Credential("Social Security Number",
                    newPersonBase.ssn,
                    null,
                    null)
        }

        if (newPersonIdentificationName && newPersonIdentificationName.bannerId) {
            credentials << new Credential("Banner ID",
                    newPersonIdentificationName.bannerId,
                    null,
                    null)
        }
        def names = []
        def name = new Name(newPersonIdentificationName, newPersonBase)
        name.setNameType("Primary")
        names << name

        def ethnicityDetail = newPersonBase.ethnicity ? ethnicityCompositeService.fetchByEthnicityCode(newPersonBase.ethnicity?.code) : null
        def maritalStatusDetail = newPersonBase.maritalStatus ? maritalStatusCompositeService.fetchByMaritalStatusCode(newPersonBase.maritalStatus?.code) : null
        //update Address
        def addresses = []

        if (person.containsKey('addresses') && person.addresses instanceof List)
            addresses = updateAddresses(pidmToUpdate, person.metadata, person.addresses)

        //update Telephones
        def phones = []
        if (person.containsKey('phones') && person.phones instanceof List)
            phones = updatePhones(pidmToUpdate, person.metadata, person.phones)

        def emails = []

        //update races
        def races = []
        if (person.containsKey('races') && person.races instanceof List)
            races = updateRaces(pidmToUpdate, person.metadata, person.races)
        //Build decorator to return LDM response.
        def personDecorator = new Person(newPersonBase, personGuid, credentials, addresses, phones, emails, names, maritalStatusDetail, ethnicityDetail, races, [])
        Map personMap = [:]
        personMap.put(pidmToUpdate, personDecorator)
        if (addresses.size() == 0)
            personMap = buildPersonAddresses(PersonAddress.fetchActiveAddressesByPidmInList([pidmToUpdate]), personMap)
        if (phones.size() == 0)
            personMap = buildPersonTelephones(PersonTelephone.fetchActiveTelephoneByPidmInList([pidmToUpdate]), personMap)
        //update Emails
        if (person.containsKey('emails') && person.emails instanceof List) {
            emails = updatePersonEmails(pidmToUpdate, person.metadata, person.emails)
            buildPersonEmails(emails, personMap)
        }

        if (races.size() == 0)
            personMap = buildPersonRaces(PersonRace.findAllByPidmInList([pidmToUpdate]), personMap)
        def additionalIdTypes = Credential.additionalIdMap.keySet().asList()
        personMap = buildPersonAdditionalIds(AdditionalID.fetchByPidmInListAndAdditionalIdentificationTypeInList([pidmToUpdate], additionalIdTypes), personMap)
        personDecorator = buildPersonRoles(personMap).get(pidmToUpdate)
    }


    private List<Integer> searchPerson(Map params) {
        def ctx = ServletContextHolder.servletContext.getAttribute(GrailsApplicationAttributes.APPLICATION_CONTEXT)
        def sessionFactory = ctx.sessionFactory

        List<Integer> personList = []
        IntegrationConfiguration personMatchRule = IntegrationConfiguration.findByProcessCodeAndSettingName(PROCESS_CODE, PERSON_MATCH_RULE)

        def primaryName = params.names.find { primaryNameType ->
            primaryNameType.nameType == "Primary"
        }
        def ssnCredentials = params.credentials.find { credential ->
            credential.credentialType == "Social Security Number"
        }
        def bannerIdCredentials = params.credentials.find { credential ->
            credential.credentialType == "Banner ID"
        }
        def emailInstitution = params.emails.find { email ->
            email.emailType == "Institution"
        }
        def emailInstitutionRuleValue
        if (emailInstitution?.emailType) {
            emailInstitutionRuleValue = IntegrationConfiguration.fetchAllByProcessCodeAndSettingNameAndTranslationValue(PROCESS_CODE, PERSON_EMAIL_TYPE, emailInstitution['emailType'])[0]?.value
        }

        def emailPersonal = params.emails.find { email ->
            email.emailType == "Personal"
        }
        def emailPersonalRuleValue
        if (emailPersonal?.emailType) {
            emailPersonalRuleValue = IntegrationConfiguration.fetchAllByProcessCodeAndSettingNameAndTranslationValue(PROCESS_CODE, PERSON_EMAIL_TYPE, emailPersonal['emailType'])[0]?.value
        }

        def emailWork = params.emails.find { email ->
            email.emailType == "Work"
        }
        def emailWorkRuleValue
        if (emailWork?.emailType) {
            emailWorkRuleValue = IntegrationConfiguration.fetchAllByProcessCodeAndSettingNameAndTranslationValue(PROCESS_CODE, PERSON_EMAIL_TYPE, emailWork['emailType'])[0]?.value
        }

        String dob = null
        if (params?.dateOfBirth) {
            Date date = LdmService.convertString2Date(params?.dateOfBirth)
            dob = date.format("dd-MMM-yyyy")
        }

        CallableStatement sqlCall
        try {
            def connection = sessionFactory.currentSession.connection()
            String matchPersonQuery = "{ call spkcmth.p_common_mtch(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) }"
            sqlCall = connection.prepareCall(matchPersonQuery)

            sqlCall.setString(1, personMatchRule?.value)
            sqlCall.setString(2, primaryName?.firstName)
            sqlCall.setString(3, primaryName?.lastName)
            sqlCall.setString(4, primaryName?.middleName)
            sqlCall.setString(5, dob)
            sqlCall.setString(6, params?.gender)
            sqlCall.setString(7, ssnCredentials?.credentialType)
            sqlCall.setString(8, ssnCredentials?.credentialId)
            sqlCall.setString(9, bannerIdCredentials?.credentialType ?: null)
            sqlCall.setString(10, bannerIdCredentials?.credentialId ?: null)
            sqlCall.setString(11, emailInstitutionRuleValue ?: null)
            sqlCall.setString(12, emailInstitution?.emailAddress)
            sqlCall.setString(13, emailPersonalRuleValue ?: null)
            sqlCall.setString(14, emailPersonal?.emailAddress)
            sqlCall.setString(15, emailWorkRuleValue ?: null)
            sqlCall.setString(16, emailWork?.emailAddress)

            sqlCall.registerOutParameter(17, java.sql.Types.VARCHAR)
            sqlCall.executeQuery()

            String errorCode = sqlCall.getString(17)
            if (!errorCode) {
                personList = getCommonMatchingResults()
            } else {
                throw new ApplicationException(this.class.name, errorCode)
            }
        }
        catch (SQLException sqlEx) {
            log.error "Error executing spkcmth.p_common_mtch: " + sqlEx.stackTrace
            throw new ApplicationException(this.class.name, sqlEx)
        }
        catch (Exception ex) {
            log.error "Exception while searching person ${ex}" + ex.stackTrace
            throw new ApplicationException(this.class.name, ex)
        }
        finally {
            try {
                sqlCall?.close()
            } catch (SQLException sqlEx) {
                log.trace "Sql Statement is already closed, no need to close it."
            }
        }

        return personList
    }


    private def getCommonMatchingResults() {
        List<Integer> personPidmList = []
        def ctx = ServletContextHolder.servletContext.getAttribute(GrailsApplicationAttributes.APPLICATION_CONTEXT)
        def log = Logger.getLogger(this.getClass())
        def sessionFactory = ctx.sessionFactory
        def session = sessionFactory.currentSession
        def sql
        try {
            sql = new Sql(session.connection())
            def commonMatchSql = """SELECT govcmrt_pidm pidm, govcmrt_id
                                      FROM govcmrt
                                     WHERE govcmrt_result_ind = 'M'"""

            sql.eachRow(commonMatchSql) { commonMatchPerson ->
                personPidmList << commonMatchPerson.pidm.intValue()
            }
        }
        catch (SQLException ae) {
            log.error "SqlException while fetching person details from govcmrt ${ae}"
            throw ae
        }
        catch (Exception ae) {
            log.error "Exception while fetching person details ${ae} "
            throw ae
        }
        finally {
            sql?.close()
        }
        return personPidmList
    }


    public Integer getPidm(String guid) {
        def entity = GlobalUniqueIdentifier.fetchByLdmNameAndGuid(ldmName, guid?.toLowerCase())
        if (!entity)
            throw new ApplicationException(GlobalUniqueIdentifierService.API, new NotFoundException(id: Person.class.simpleName))
        return entity.domainKey?.toInteger()
    }


    private void updateGuidValue(def id, def guid, String ldmName) {
        // Update the GUID to the one we received.
        GlobalUniqueIdentifier newEntity = GlobalUniqueIdentifier.findByLdmNameAndDomainId(ldmName, id)
        if (!newEntity) {
            throw new ApplicationException(GlobalUniqueIdentifierService.API, new NotFoundException(id: Person.class.simpleName))
        }
        if (!newEntity) {
            throw new ApplicationException(GlobalUniqueIdentifierService.API, new NotFoundException(id: Person.class.simpleName))
        }
        newEntity.guid = guid
        globalUniqueIdentifierService.update(newEntity)
    }


    private PersonBasicPersonBase createPersonBasicPersonBase(person, newPersonIdentificationName, newPersonIdentification) {
        PersonBasicPersonBase newPersonBase
        if (person.guid) {
            updateGuidValue(newPersonIdentificationName.id, person.guid, ldmName)

        } else {
            def entity = GlobalUniqueIdentifier.findByLdmNameAndDomainId(ldmName, newPersonIdentificationName.id)
            person.put('guid', entity)
        }
        if (person?.credentials instanceof List) {
            person?.credentials?.each { it ->
                if (it instanceof Map) {
                    person = createSSN(it.credentialType, it.credentialId, person)
                }
            }
        }

        //Copy personBase attributes into person map from Primary names object.
        person.put('dataOrigin', person?.metadata?.dataOrigin)
        if (newPersonIdentification) {
            if (newPersonIdentification.containsKey('namePrefix')) {
                person.put('namePrefix', newPersonIdentification.get('namePrefix'))
            }
            if (newPersonIdentification.containsKey('nameSuffix')) {
                person.put('nameSuffix', newPersonIdentification.get('nameSuffix'))
            }
            if (newPersonIdentification.containsKey('preferenceFirstName')) {
                person.put('preferenceFirstName', newPersonIdentification.get('preferenceFirstName'))
            }
        }
        //Translate enumerations and defaults
        person.put('sex', person?.sex == 'Male' ? 'M' : (person?.sex == 'Female' ? 'F' : (person?.sex == 'Unknown' ? 'N' : null)))
        def maritalStatus
        try {
            maritalStatus = person.maritalStatusDetail instanceof Map && person.maritalStatusDetail?.guid ? maritalStatusCompositeService.get(person.maritalStatusDetail?.guid) : null
        } catch (ApplicationException e) {
            throw new ApplicationException("PersonCompositeService", new BusinessLogicValidationException("maritalStatus.invalid",[]))
        }
        person.put('maritalStatus', maritalStatus ? MaritalStatus.findByCode(maritalStatus?.code) : null)
        def ethnicity
        try {
            if (person.ethnicityDetail instanceof Map) {
                ethnicity = person.ethnicityDetail?.guid ? ethnicityCompositeService.get(person.ethnicityDetail?.guid) : null
            }
        }
        catch (ApplicationException e) {
            if (e.wrappedException instanceof NotFoundException) {
                throw new ApplicationException("PersonCompositeService", new BusinessLogicValidationException("ethnicity.invalid",[]))
                ethnicity = null
            } else {
                throw e
            }
        }
        person.put('ethnicity', ethnicity ? Ethnicity.findByCode(ethnicity?.code) : null)
        person.put('ethnic', ethnicity ? ethnicity.ethnic : null)
        person.put('deadIndicator', person.get('deadDate') ? 'Y' : null)
        person.put('pidm', newPersonIdentificationName?.pidm)
        person.put('armedServiceMedalVetIndicator', false)
        newPersonBase = personBasicPersonBaseService.create(person)
        newPersonBase
    }


    List<PersonAddress> createAddresses(def pidm, Map metadata, List<Map> newAddresses) {
        def addresses = []
        newAddresses?.each { activeAddress ->
            if (activeAddress instanceof Map) {
                IntegrationConfiguration rule = fetchAllByProcessCodeAndSettingNameAndTranslationValue(
                        PROCESS_CODE, PERSON_ADDRESS_TYPE, activeAddress.addressType)
                if (!rule) {
                    throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("goriccr.not.found.message",[PERSON_ADDRESS_TYPE]))
                }
                if (rule.translationValue == activeAddress.addressType && !addresses.contains {
                    it.addressType == rule?.value
                }) {
                    activeAddress.put('addressType', AddressType.findByCode(rule?.value))

                    if (activeAddress?.nation?.containsKey('code')) {
                        if (activeAddress.nation.code) {
                            Nation nation = Nation.findByScodIso(activeAddress?.nation?.code)
                            if (nation) {
                                activeAddress.put('nation', nation)
                            } else {
                                log.error "Nation not found for code: ${activeAddress?.nation?.code}"
                                throw new ApplicationException("Person", new BusinessLogicValidationException("country.not.found.message",[]))
                            }
                        } else {
                            activeAddress.put('nation', null)
                        }
                    }
                    if (activeAddress.containsKey('county')) {
                        if (activeAddress.county) {
                            County country = County.findByDescription(activeAddress.county)
                            if (country) {
                                activeAddress.put('county', country)
                            } else {
                                log.error "County not found for code: ${activeAddress.county}"
                                throw new ApplicationException("Person", new BusinessLogicValidationException("county.not.found.message",[]))
                            }
                        } else {
                            activeAddress.put('county', null)
                        }
                    }
                    activeAddress.put('pidm', pidm)
                    activeAddress.put('dataOrigin', metadata?.dataOrigin)
                    activeAddress.put('fromDate', new Date())
                    validateAddressRequiredFields(activeAddress)
                    addresses << personAddressService.create(activeAddress)
                }
            }
        }
        addresses
    }


    List<PersonRace> createRaces(def pidm, Map metadata, List<Map> newRaces) {
        def races = []
        newRaces?.each { activeRace ->
            if (activeRace instanceof Map && activeRace?.guid) {
                def race
                try {
                    race = raceCompositeService.get(activeRace?.guid)
                }
                catch (ApplicationException e) {
                    if (e.wrappedException instanceof NotFoundException) {
                        throw new ApplicationException("PersonCompositeService", new BusinessLogicValidationException("race.invalid",[]))
                    } else {
                        throw e
                    }
                }
                def newRace = new PersonRace()
                newRace.pidm = pidm
                newRace.race = race.race
                newRace.dataOrigin = metadata?.dataOrigin
                PersonRace personRace = PersonRace.fetchByPidmAndRace(pidm, newRace.race)
                if (personRace == null) {
                    races << personRaceService.create(newRace)
                } else {
                    throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException('race.exists',[race.guid]))
                }

            } else {
                throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("race.invalid",[]))
            }
        }
        races
    }


    def createPhones(def pidm, Map metadata, List<Map> newPhones) {
        def phones = []
        newPhones?.each { activePhone ->
            if (activePhone instanceof Map) {
                IntegrationConfiguration rule = fetchAllByProcessCodeAndSettingNameAndTranslationValue(PROCESS_CODE, PERSON_PHONE_TYPE, activePhone.phoneType)
                if (!rule) {
                    log.error "Rule not found for phone:" + activePhone.toString()
                    throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("goriccr.not.found.message",[PERSON_PHONE_TYPE]))
                }
                if (rule?.translationValue == activePhone.phoneType &&
                        !phones.contains { activePhone.phoneType == rule?.value }) {
                    activePhone.put('telephoneType', TelephoneType.findByCode(rule?.value))
                    activePhone.put('pidm', pidm)
                    activePhone.put('dataOrigin', metadata?.dataOrigin)
                    validatePhoneRequiredFields(activePhone)
                    Map phoneNumber = parsePhoneNumber(activePhone.phoneNumber)
                    phoneNumber.keySet().each { key ->
                        activePhone.put(key, phoneNumber.get(key))
                    }
                    phones << personTelephoneService.create(activePhone)
                }
            }
        }
        phones
    }

    private def createPersonEmails(def pidm, Map metadata, List<Map> emailsInRequest) {
        def personEmailsList = []
        List<String> processedEmailTypes = []

        def preferredEmail = getPreferredEmail(emailsInRequest)

        Boolean tempPreferredIndicator = false
        emailsInRequest?.each {
            validateEmailRequiredFields(it)
            if (it instanceof Map) {
                if (!processedEmailTypes.contains { it.emailType.trim() }) {
                    Boolean preferredIndicator = false
                    if ("v2".equals(getRequestedVersion()) && preferredEmail && it.emailAddress == preferredEmail.emailAddress && !tempPreferredIndicator) {
                        preferredIndicator = true
                        tempPreferredIndicator = preferredIndicator
                    }
                    PersonEmail personEmail = createPersonEmail(it.guid?.trim()?.toLowerCase(), pidm, metadata, it, preferredIndicator)
                    personEmailsList << personEmail
                    processedEmailTypes << it.emailType.trim()
                }
            }
        }

        return personEmailsList
    }


    private PersonEmail createPersonEmail(String guid, def pidm, Map metadata, def emailInRequest, Boolean preferredIndicator) {
        PersonEmail personEmail

        IntegrationConfiguration rule = fetchAllByProcessCodeAndSettingNameAndTranslationValue(PROCESS_CODE, PERSON_EMAIL_TYPE, emailInRequest.emailType.trim())
        if (!rule) {
            throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("goriccr.not.found.message",[PERSON_EMAIL_TYPE]))
        }
        if (rule.value) {
            personEmail = new PersonEmail(pidm: pidm, emailAddress: emailInRequest.emailAddress, statusIndicator: "A", emailType: EmailType.findByCode(rule.value), dataOrigin: metadata?.dataOrigin, preferredIndicator: preferredIndicator)
            personEmail = personEmailService.create([domainModel: personEmail])

            String domainKey = "${personEmail.pidm}${DOMAIN_KEY_DELIMITER}${personEmail.emailType}${DOMAIN_KEY_DELIMITER}${personEmail.emailAddress}"
            if (guid) {
                // Overwrite the GUID created by DB insert trigger, with the one provided in the request body
                updateGuidValue(personEmail.id, guid, PERSON_EMAILS_LDM_NAME)
            }

            log.debug("GUID: ${guid}   DomainKey: ${domainKey}")
        }

        return personEmail
    }


    private def updatePersonEmails(def pidm, Map metadata, def emailsInRequest) {
        List<PersonEmail> personEmails = []
        def bannerEmailTypes = []
        def rules = IntegrationConfiguration.findAllByProcessCodeAndSettingName(PROCESS_CODE, PERSON_EMAIL_TYPE)
        rules?.each {
            bannerEmailTypes << it.value
        }

        List<PersonEmail> existingPersonEmails = PersonEmail.fetchListByPidmAndEmailTypes(pidm, bannerEmailTypes) ?: []
        existingPersonEmails.each {
            it.statusIndicator = "I"
            it.preferredIndicator = false
            personEmailService.update([domainModel: it])
        }

        def preferredEmail = getPreferredEmail(emailsInRequest)

        Boolean tempPreferredIndicator = false
        List<String> processedEmailTypes = []
        PersonEmail personEmail
        emailsInRequest?.each {
            validateEmailRequiredFields(it)
            if (!processedEmailTypes.contains(it.emailType.trim())) {
                IntegrationConfiguration rule = fetchAllByProcessCodeAndSettingNameAndTranslationValue(PROCESS_CODE, PERSON_EMAIL_TYPE, it.emailType.trim())
                if (!rule) {
                    throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("goriccr.not.found.message",[PERSON_EMAIL_TYPE]))
                }
                if (rule.value) {
                    PersonEmail existingPersonEmail = existingPersonEmails?.find { existingPersonEmail -> existingPersonEmail.emailType.code == rule.value && existingPersonEmail.emailAddress == it.emailAddress }
                    if (existingPersonEmail) {
                        existingPersonEmail.statusIndicator = "A"
                        if ("v2".equals(getRequestedVersion()) && preferredEmail && it.emailAddress == preferredEmail.emailAddress && !tempPreferredIndicator) {
                            existingPersonEmail.preferredIndicator = true
                            tempPreferredIndicator = true
                        }
                        personEmail = personEmailService.update([domainModel: existingPersonEmail])
                        String domainKey = "${existingPersonEmail.pidm}${DOMAIN_KEY_DELIMITER}${existingPersonEmail.emailType}${DOMAIN_KEY_DELIMITER}${existingPersonEmail.emailAddress}"
                        String guid = it?.guid?.trim()?.toLowerCase()
                        if (guid) {
                            // Overwrite the GUID created by DB insert trigger, with the one provided in the request body
                            updateGuidValue(existingPersonEmail.id, guid, PERSON_EMAILS_LDM_NAME)
                        }
                        log.debug("GUID: ${guid}   DomainKey: ${domainKey}")

                        existingPersonEmails.remove(existingPersonEmail)
                    } else {
                        Boolean preferredIndicator = false
                        if ("v2".equals(getRequestedVersion()) && preferredEmail && it.emailAddress == preferredEmail.emailAddress && !tempPreferredIndicator) {
                            preferredIndicator = true
                            tempPreferredIndicator = true
                        }
                        personEmail = createPersonEmail(it.guid?.trim()?.toLowerCase(), pidm, metadata, it, preferredIndicator)
                    }
                    personEmails << personEmail
                    processedEmailTypes << it.emailType.trim()
                }
            }
        }

        return personEmails
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    def buildLdmPersonObjects(List<PersonIdentificationNameCurrent> personIdentificationList) {
        def persons = [:]
        def pidms = []
        personIdentificationList.each { personIdentification ->
            pidms << personIdentification.pidm
            persons.put(personIdentification.pidm, null) //Preserve list order.
        }
        if (pidms.size() < 1) {
            return persons
        } else if (pidms.size() > 1000) {
            throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("max.results.exceeded",[]))
        }
        List<PersonBasicPersonBase> personBaseList = PersonBasicPersonBase.findAllByPidmInList(pidms)
        List<PersonAddress> personAddressList = PersonAddress.fetchActiveAddressesByPidmInList(pidms)
        List<PersonTelephone> personTelephoneList = PersonTelephone.fetchActiveTelephoneByPidmInList(pidms)
        def personEmailList = PersonEmail.findAllByStatusIndicatorAndPidmInList('A', pidms)
        List<PersonRace> personRaceList = PersonRace.findAllByPidmInList(pidms)
        personBaseList.each { personBase ->
            Person currentRecord = new Person(personBase)
            currentRecord.maritalStatusDetail = maritalStatusCompositeService.fetchByMaritalStatusCode(personBase.maritalStatus?.code)
            currentRecord.ethnicityDetail = personBase.ethnicity?.code ? ethnicityCompositeService.fetchByEthnicityCode(personBase.ethnicity?.code) : null
            /*  if( personBase.ssn ) {
                currentRecord.credentials << new Credential("Social Security Number",
                        personBase.ssn,
                        null,
                        null)
            } Not spitting out SSNs at this time*/

            persons.put(currentRecord.pidm, currentRecord)
        }
        def domainIds = []
        personIdentificationList.each { identification ->
            Person currentRecord = persons.get(identification.pidm) ?: new Person(null)
            def name = new Name(identification, currentRecord)
            name.setNameType("Primary")
            domainIds << identification.id
            currentRecord.metadata = new Metadata(identification.dataOrigin)
            currentRecord.names << name
            currentRecord.credentials << new Credential("Banner ID", identification.bannerId, null, null)
            persons.put(identification.pidm, currentRecord)
        }
        persons = buildPersonGuids(domainIds, persons)
        persons = buildPersonAddresses(personAddressList, persons)
        persons = buildPersonTelephones(personTelephoneList, persons)
        persons = buildPersonEmails(personEmailList, persons)
        persons = buildPersonRaces(personRaceList, persons)
        persons = buildPersonRoles(persons)
        persons // Map of person objects with pidm as index.
    }


    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    def buildPersonEmails(personEmailList, persons) {
        personEmailList.each { PersonEmail activeEmail ->
            Person currentRecord = persons.get(activeEmail.pidm)
            IntegrationConfiguration rule = findAllByProcessCodeAndSettingNameAndValue(PROCESS_CODE, PERSON_EMAIL_TYPE, activeEmail?.emailType.code)
            if (rule?.value == activeEmail?.emailType?.code && !currentRecord.emails.contains {
                it.emailType == rule?.translationValue
            }) {
                GlobalUniqueIdentifier globalUniqueIdentifier = GlobalUniqueIdentifier.findByLdmNameAndDomainId(PERSON_EMAILS_LDM_NAME, activeEmail.id)
                String guid = globalUniqueIdentifier.guid
                def email = new Email(guid, activeEmail)
                email.emailType = rule?.translationValue
                currentRecord.emails << email
                if (activeEmail.preferredIndicator) {
                    def preferredEmail = new Email(guid, activeEmail)
                    preferredEmail.emailType = PERSON_EMAIL_TYPE_PREFERRED
                    currentRecord.emails << preferredEmail
                }
                persons.put(activeEmail.pidm, currentRecord)
            }
        }
        persons
    }


    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    def buildPersonTelephones(List<PersonTelephone> personTelephoneList, Map persons) {
        personTelephoneList.each { activePhone ->
            Person currentRecord = persons.get(activePhone.pidm)
            IntegrationConfiguration rule = findAllByProcessCodeAndSettingNameAndValue(PROCESS_CODE, PERSON_PHONE_TYPE, activePhone?.telephoneType.code)
            if (rule?.value == activePhone?.telephoneType?.code &&
                    !(currentRecord.phones.contains { it.phoneType == rule?.translationValue })) {
                def phone = new Phone(activePhone)
                phone.phoneType = rule?.translationValue
                phone.phoneNumberDetail = formatPhoneNumber((phone.countryPhone ? "+" + phone.countryPhone : "") + (phone.phoneArea ?: "") + (phone.phoneNumber ?: ""))
                currentRecord.phones << phone
            }
            persons.put(activePhone.pidm, currentRecord)
        }
        persons
    }


    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    def buildPersonAddresses(List<PersonAddress> personAddressList, Map persons) {
        personAddressList.each { activeAddress ->
            Person currentRecord = persons.get(activeAddress.pidm)
            IntegrationConfiguration addressTypeRule = findAllByProcessCodeAndSettingNameAndValue(PROCESS_CODE, PERSON_ADDRESS_TYPE, activeAddress.addressType.code)
            if (addressTypeRule?.value == activeAddress.addressType?.code &&
                    !currentRecord.addresses.contains { it.addressType == addressTypeRule?.translationValue }) {
                def address = new Address(activeAddress)
                address.addressType = addressTypeRule?.translationValue
                currentRecord.addresses << address
            }
            persons.put(activeAddress.pidm, currentRecord)
        }
        persons
    }


    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    def buildPersonGuids(List domainIds, Map persons) {
        GlobalUniqueIdentifier.findAllByLdmNameAndDomainIdInList(ldmName, domainIds).each { guid ->
            Person currentRecord = persons.get(guid.domainKey.toInteger())
            currentRecord.guid = guid.guid
            persons.put(guid.domainKey.toInteger(), currentRecord)
        }
        persons
    }


    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    def buildPersonRaces(List<PersonRace> personRacesList, Map persons) {
        personRacesList.each { activeRace ->
            Person currentRecord = persons.get(activeRace.pidm)
            def race = raceCompositeService.fetchByRaceCode(activeRace.race)
            race.dataOrigin = activeRace.dataOrigin
            currentRecord.races << race
            persons.put(activeRace.pidm, currentRecord)
        }
        persons
    }


    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    def buildPersonRoles(Map persons) {
        def pidms = []
        persons.each { key, value ->
            pidms << key
        }
        userRoleCompositeService.fetchAllRolesByPidmInList(pidms).each { role ->
            Person currentRecord = persons.get(role.key)
            currentRecord.roles = role.value
        }
        persons
    }


    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    def buildPersonAdditionalIds(List<AdditionalID> additionalIds, Map persons) {
        additionalIds.each { credential ->
            Person currentRecord = persons.get(credential.pidm)
            currentRecord.credentials << new Credential(Credential.additionalIdMap[credential.additionalIdentificationType.code],
                    credential.additionalId, null, null)
            persons.put(credential.pidm, currentRecord)
        }
        persons
    }


    private PersonBasicPersonBase updatePersonBasicPersonBase(pidmToUpdate, newPersonIdentificationName, person, changedPersonIdentification) {
        List<PersonBasicPersonBase> personBaseList = PersonBasicPersonBase.findAllByPidmInList([pidmToUpdate])
        PersonBasicPersonBase newPersonBase

        if (personBaseList.size() == 0) {
            //if there is no person base then create new PersonBase
            newPersonBase = createPersonBasicPersonBase(person, newPersonIdentificationName, changedPersonIdentification)
        } else {
            personBaseList.each { personBase ->
                //Copy personBase attributes into person map from Primary names object.
                if (person?.credentials instanceof List) {
                    person?.credentials?.each { it ->
                        if (it instanceof Map) {
                            personBase = updateSSN(it.credentialType, it.credentialId, personBase)
                        }
                    }
                }
                if (changedPersonIdentification) {
                    if (changedPersonIdentification.containsKey('namePrefix')) {
                        personBase.namePrefix = changedPersonIdentification.get('namePrefix')
                    }
                    if (changedPersonIdentification.containsKey('nameSuffix')) {
                        personBase.nameSuffix = changedPersonIdentification.get('nameSuffix')
                    }
                    if (changedPersonIdentification.containsKey('preferenceFirstName')) {
                        personBase.preferenceFirstName = changedPersonIdentification.get('preferenceFirstName')
                    }
                }
                //Translate enumerations and defaults
                if (person.containsKey('sex')) {
                    personBase.sex = person?.sex == 'Male' ? 'M' : (person?.sex == 'Female' ? 'F' : (person?.sex == 'Unknown' ? 'N' : null))
                }
                if (person.containsKey('maritalStatusDetail')) {
                    def maritalStatus
                    try {
                        maritalStatus = person.maritalStatusDetail instanceof Map && person.maritalStatusDetail?.guid ? maritalStatusCompositeService.get(person.maritalStatusDetail?.guid) : null
                    } catch (ApplicationException e) {
                        throw new ApplicationException("PersonCompositeService", new BusinessLogicValidationException("maritalStatus.invalid",[]))
                    }
                    personBase.maritalStatus = maritalStatus ? MaritalStatus.findByCode(maritalStatus?.code) : null

                }

                if (person.containsKey('ethnicityDetail')) {
                    def ethnicity = person.ethnicityDetail?.guid ? ethnicityCompositeService.get(person.ethnicityDetail?.guid) : null
                    personBase.ethnicity = ethnicity ? Ethnicity.findByCode(ethnicity?.code) : null
                    personBase.ethnic = person.ethnic == "Non-Hispanic" ? '1' : (person.ethnic == "Hispanic" ? '2' : null)
                }
                if (person.containsKey('deadDate')) {
                    personBase.deadIndicator = person.get('deadDate') != null ? 'Y' : null
                    personBase.deadDate = person.get('deadDate')
                    if (personBase.deadDate != null && personBase.birthDate != null && personBase.deadDate.before(personBase.birthDate)) {
                        throw new ApplicationException("PersonCompositeService", new BusinessLogicValidationException('dateDeceased.invalid',[personBase.deadDate]))
                    }
                }
                if (person.containsKey('birthDate')) {
                    personBase.birthDate = person.get('birthDate')
                }
                if (person.containsKey('metadata') && person.metadata.containsKey('dataOrigin')) {
                    personBase.dataOrigin = person.metadata.get('dataOrigin')
                }


                newPersonBase = personBasicPersonBaseService.update(personBase)
            }
        }
        return newPersonBase
    }

    private updateAddresses(def pidm, Map metadata, List<Map> newAddresses) {
        def addresses = []
        newAddresses.collect { activeAddress ->
            getStateAndZip(activeAddress)
        }
        List<PersonAddress> currentAddresses = PersonAddress.fetchActiveAddressesByPidm(['pidm': pidm]).get('list')
        currentAddresses.each { currentAddress ->
            if (findAllByProcessCodeAndSettingNameAndValue(PROCESS_CODE, PERSON_ADDRESS_TYPE, currentAddress.addressType.code)) {
                def activeAddresses = newAddresses.findAll { it ->
                    fetchAllByProcessCodeAndSettingNameAndTranslationValue(PROCESS_CODE, PERSON_ADDRESS_TYPE,
                            it.addressType)?.value == currentAddress.addressType.code
                }
                log.debug "NewAddresses:" + newAddresses.toString()
                log.debug "ActiveAddresses:" + activeAddresses.toString()
                log.debug "CurrentAddress:" + currentAddress.toString()
                def invalidAddress = false
                if (activeAddresses.size() > 0) {
                    activeAddresses.each { activeAddress ->
                        switch (activeAddress?.addressType) {
                            default:

                                if (activeAddress.state.code != currentAddress.state.code) {
                                    log.debug "State different"
                                    invalidAddress = true
                                }


                                if (activeAddress.zip != currentAddress.zip) {
                                    log.debug "Zip different"
                                    invalidAddress = true
                                    break;
                                }
                                if (activeAddress?.nation?.containsKey('code')) {
                                    def nation
                                    if (activeAddress.nation?.code) {
                                        nation = Nation.findByScodIso(activeAddress?.nation?.code)
                                        if (!nation) {
                                            log.error "Nation not found for code: ${activeAddress?.country?.code}"
                                            throw new ApplicationException("Person", new BusinessLogicValidationException("country.not.found.message",[]))
                                        }
                                    }
                                    if (nation?.code != currentAddress.nation?.code) {
                                        log.debug "Nation different:" + nation.code + " : " + currentAddress.nation?.code
                                        invalidAddress = true
                                        break;
                                    }
                                }
                                if (activeAddress.containsKey('county')) {
                                    def county
                                    if (activeAddress.county) {
                                        county = County.findByDescription(activeAddress.county)
                                        if (!county) {
                                            log.error "County not found for code: ${activeAddress.county}"
                                            throw new ApplicationException("Person", new BusinessLogicValidationException("county.not.found.message",[]))
                                        }
                                    }
                                    if (county?.code != currentAddress.county?.code) {
                                        log.debug "County different"
                                        invalidAddress = true
                                        break;
                                    }
                                }
                                if (activeAddress.containsKey('streetLine1')) {
                                    if (activeAddress.streetLine1 != currentAddress.streetLine1) {
                                        log.debug "Street1 different"
                                        invalidAddress = true
                                        break;
                                    }
                                }
                                if (activeAddress.containsKey('streetLine2')) {
                                    if (activeAddress.streetLine2 != currentAddress.streetLine2) {
                                        log.debug "Street2 different"
                                        invalidAddress = true
                                        break;
                                    }
                                }
                                if (activeAddress.containsKey('streetLine3')) {
                                    if (activeAddress.streetLine3 != currentAddress.streetLine3) {
                                        log.debug "Street3 different"
                                        invalidAddress = true
                                        break;
                                    }
                                }
                                break;
                        }
                        if (invalidAddress) {
                            currentAddress.statusIndicator = 'I'
                            log.debug "Inactivating address:" + currentAddress.toString()
                            personAddressService.update(currentAddress)
                        } else {
                            def addressDecorator = new Address(currentAddress)
                            addressDecorator.addressType = activeAddress.addressType
                            addresses << addressDecorator
                            newAddresses.remove(activeAddress)
                            log.debug "After match, and removal of match from new to create:" + newAddresses.toString()
                        }
                    }
                } else {
                    currentAddress.statusIndicator = 'I'
                    log.debug "Inactivating address:" + currentAddress.toString()
                    personAddressService.update(currentAddress)
                }
            }
        }

        createAddresses(pidm, metadata, newAddresses).each {
            currentAddress ->
                def addressDecorator = new Address(currentAddress)
                addressDecorator.addressType = findAllByProcessCodeAndSettingNameAndValue(PROCESS_CODE, PERSON_ADDRESS_TYPE, currentAddress.addressType.code)?.translationValue
                addresses << addressDecorator
        }
        addresses

    }


    private updatePhones(def pidm, Map metadata, List<Map> newPhones) {
        def phones = []
        List<PersonTelephone> personTelephoneList = PersonTelephone.fetchActiveTelephoneByPidmInList([pidm]).each { currentPhone ->
            def thisType = findAllByProcessCodeAndSettingNameAndValue(PROCESS_CODE, PERSON_PHONE_TYPE, currentPhone.telephoneType?.code)?.translationValue
            def activePhones = newPhones.findAll { it ->
                it.phoneType == thisType
            }
            if (activePhones.size() > 0) {
                def invalidPhone = false
                activePhones.each { activePhone ->
                    if (activePhone.containsKey('phoneExtension')) {
                        if (activePhone.phoneExtension != currentPhone.phoneExtension) {
                            log.debug "Phone extension different"
                            invalidPhone = true
                        }
                    }
                    if (activePhone.containsKey('phoneNumber')) {
                        def parsedResult = parsePhoneNumber(activePhone.phoneNumber)
                        if ((parsedResult.phoneNumber ? parsedResult.phoneNumber.toString() : null) != currentPhone.phoneNumber) {
                            log.debug "Phone number different"
                            invalidPhone = true
                        }
                        if ((parsedResult.phoneArea ? parsedResult.phoneArea.toString() : null) != currentPhone.phoneArea) {
                            log.debug "Phone area code different"
                            invalidPhone = true
                        }
                        if ((parsedResult.countryPhone ? parsedResult.countryPhone.toString() : null) != currentPhone.countryPhone) {
                            log.debug "Phone country code different:" + parsedResult.countryPhone + " : " + currentPhone.countryPhone
                            invalidPhone = true
                        }
                    }
                    if (invalidPhone) {
                        currentPhone.statusIndicator = 'I'
                        log.debug "Inactivating phone:" + currentPhone.toString()
                        personTelephoneService.update(currentPhone)
                    } else {
                        def phoneDecorator = new Phone(currentPhone)
                        phoneDecorator.phoneType = activePhone.phoneType
                        phoneDecorator.phoneNumberDetail = formatPhoneNumber((currentPhone.countryPhone ? "+" + currentPhone.countryPhone : "") +
                                (currentPhone.phoneArea ?: "") + (currentPhone.phoneNumber ?: ""))
                        phones << phoneDecorator
                        newPhones.remove(activePhone)
                    }
                }
            } else {
                currentPhone.statusIndicator = 'I'
                log.debug "Inactivating phone:" + currentPhone.toString()
                personTelephoneService.update(currentPhone)
            }
        }
        createPhones(pidm, metadata, newPhones).each { currentPhone ->
            def phoneDecorator = new Phone(currentPhone)
            phoneDecorator.phoneType = findAllByProcessCodeAndSettingNameAndValue(PROCESS_CODE, PERSON_PHONE_TYPE, currentPhone.telephoneType.code)?.translationValue
            phoneDecorator.phoneNumberDetail = formatPhoneNumber((currentPhone.countryPhone ? "+" + currentPhone.countryPhone : "") +
                    (currentPhone.phoneArea ?: "") + (currentPhone.phoneNumber ?: ""))
            phones << phoneDecorator
        }
        phones
    }


    List<RaceDetail> updateRaces(def pidm, Map metadata, List<Map> newRaces) {
        def races = []
        List<PersonRace> personRaceList = PersonRace.fetchByPidm(pidm)
        personRaceList.each { currentRace ->
            def raceGuid = GlobalUniqueIdentifier.findByLdmNameAndDomainKey('races', currentRace.race)?.guid
            def activeRaces = newRaces.findAll { it ->
                it.guid == raceGuid
            }
            log.debug "currentRace:" + currentRace.toString() + " : " + raceGuid
            log.debug "Races matching:" + activeRaces.toString()
            log.debug "Races to create:" + newRaces.toString()
            if (activeRaces.size() > 0) {
                newRaces.remove(activeRaces[0])
                def race = raceCompositeService.get(activeRaces[0].guid)
                races << race
            } else {
                personRaceService.delete(currentRace)
                log.debug "Removing race:" + currentRace.toString()
            }
        }
        createRaces(pidm, metadata, newRaces).each { currentRace ->
            def race = raceCompositeService.fetchByRaceCode(currentRace.race)
            races << race
        }
        races
    }

    def createOrUpdateAdditionalId(PersonIdentificationNameCurrent personIdentification, Map credential, Map metadata) {
        def idCode = Credential.additionalIdMap.find { key, value ->
            value == credential.credentialType
        }?.key
        def idType = AdditionalIdentificationType.findByCode(idCode)
        List<AdditionalID> existingIds = AdditionalID.fetchByPidmInListAndAdditionalIdentificationTypeInList([personIdentification.pidm], [idCode])
        AdditionalID existingId
        if (existingIds.size() > 0) {
            existingId = existingIds.get(0)
            existingId.additionalId = credential?.credentialId
        } else
            existingId = new AdditionalID(pidm: personIdentification.pidm,
                    additionalIdentificationType: idType,
                    additionalId: credential?.credentialId,
                    dataOrigin: metadata?.dataOrigin
            )
        log.error "Existing ID: ${existingId}"
        additionalIDService.createOrUpdate(existingId)
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    Map parsePhoneNumber(String phoneNumber) {
        Map parsedNumber = [:]
        List<InstitutionalDescription> institutions = InstitutionalDescription.list()
        def institution = institutions.size() > 0 ? institutions[0] : null
        Nation countryLdmCode
        if (institution?.natnCode) {
            countryLdmCode = Nation.findByCode(institution.natnCode)
        }
        Phonenumber.PhoneNumber parsedResult
        try {
            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance()
            parsedResult = phoneUtil.parse(phoneNumber, countryLdmCode?.scodIso ?: 'US')
            if (phoneUtil.isValidNumber(parsedResult)) {
                String nationalNumber = parsedResult.getNationalNumber()
                def nationalDestinationCodeLength = phoneUtil.getLengthOfNationalDestinationCode(parsedResult);
                if (nationalDestinationCodeLength > 0) {
                    parsedNumber.put('phoneArea', nationalNumber[0..(nationalDestinationCodeLength - 1)])
                    parsedNumber.put('phoneNumber', nationalNumber[nationalDestinationCodeLength..-1])
                } else {
                    parsedNumber.put('phoneNumber', nationalNumber)
                }
                parsedNumber.put('countryPhone', parsedResult.getCountryCode())
            } else {
                if (phoneNumber.length() < 12) {
                    parsedNumber.put('phoneNumber', phoneNumber)
                }
                else {
                    throw new ApplicationException("PersonCompositeService", new BusinessLogicValidationException("phoneNumber.malformed",[phoneNumber]))
                }
            }
        }
        catch (Exception e) {
            log.debug e.toString()
            throw new ApplicationException("PersonCompositeService", new BusinessLogicValidationException("phoneNumber.malformed",[phoneNumber]))

        }
        if (parsedResult.getExtension()) {
            parsedNumber.put('phoneExtension', parsedResult.getExtension())
        }
        parsedNumber
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    String formatPhoneNumber(String phoneNumber) {
        List<InstitutionalDescription> institutions = InstitutionalDescription.list()
        def institution = institutions.size() > 0 ? institutions[0] : null
        Nation countryLdmCode
        if (institution?.natnCode) {
            countryLdmCode = Nation.findByCode(institution.natnCode)
        }
        Phonenumber.PhoneNumber parsedResult
        try {
            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance()
            parsedResult = phoneUtil.parse(phoneNumber, countryLdmCode?.scodIso ?: 'US')
            log.debug "AfterPhone:" + phoneUtil.format(parsedResult, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL)
            return phoneUtil.format(parsedResult, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL)
        }
        catch (Exception e) {
            log.debug e.toString()
            return phoneNumber
            //throw new ApplicationException("PersonCompositeService", new BusinessLogicValidationException("phoneNumber.malformed",[]))

        }
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    private def getAddressRegion(activeAddress) {
        State state
        if (activeAddress.state) {
            state = State.findByCode(activeAddress?.state)
            if (!state) {
                log.error "State not found for code: ${activeAddress.state}"
                throw new ApplicationException("Person", new BusinessLogicValidationException("state.not.found.message",[]))
            }
            activeAddress.put('state', state)
        } else {
            IntegrationConfiguration intConf
            intConf = IntegrationConfiguration.findByProcessCodeAndSettingName(PROCESS_CODE, PERSON_REGION)
            if (!intConf) {
                throw new ApplicationException(Person, new BusinessLogicValidationException("goriccr.not.found.message",[PERSON_REGION]))
            }
            state = State.findByCode(intConf?.value)
            if (!state) {
                throw new ApplicationException(Person, new BusinessLogicValidationException("goriccr.invalid.value.message",[PERSON_REGION]))
            }
            activeAddress.put('state', state)
        }

        return activeAddress
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    private def getAddressPostalCode(activeAddress) {
        if (activeAddress.zip) {
            activeAddress.put('zip', activeAddress.zip)
        } else {
            IntegrationConfiguration intConf
            intConf = IntegrationConfiguration.findByProcessCodeAndSettingName(PROCESS_CODE, PERSON_POSTAL_CODE)
            if (!intConf) {
                throw new ApplicationException(Person, new BusinessLogicValidationException("goriccr.not.found.message",[PERSON_POSTAL_CODE]))
            }

            if(intConf.value == "UPDATE_ME") {
                throw new ApplicationException(Person, new BusinessLogicValidationException("goriccr.invalid.value.message",[PERSON_POSTAL_CODE]))
            }
            activeAddress.put('zip', intConf.value)
        }

        return activeAddress
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    private def getStateAndZip(activeAddress) {
        getAddressPostalCode(getAddressRegion(activeAddress))
    }


    def validateAddressRequiredFields(address) {
        if (!address.addressType) {
            throw new ApplicationException("PersonCompositeService", new BusinessLogicValidationException("addressType.invalid",[]))
        }
        if (!address.streetLine1) {
            throw new ApplicationException("PersonCompositeService", new BusinessLogicValidationException("streetAddress.invalid",[]))
        }
        if (!address.city) {
            throw new ApplicationException("PersonCompositeService", new BusinessLogicValidationException("city.invalid",[]))
        }
    }


    def validatePhoneRequiredFields(phone) {
        if (!phone.telephoneType) {
            throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("phoneType.invalid",[]))
        }
        if (!phone.phoneNumber) {
            throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("phoneNumber.invalid",[]))
        }
    }


    def validateEmailRequiredFields(email) {
        if ("v2".equals(getRequestedVersion()) && !email.guid) {
            throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("emailGuid.invalid",[]))
        }
        if (!email.emailType) {
            throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("emailType.invalid",[]))
        }
        if (!email.emailAddress) {
            throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("emailAddress.invalid",[]))
        }
    }


    private def validateCredentialType(String inputCredentialType, def allowedCredentialTypes, String credentialId) {
        if (!allowedCredentialTypes.contains(inputCredentialType)) {
            throw new ApplicationException('Person', new BusinessLogicValidationException("invalid.code.message:credentialType",[]))
        }
        if (inputCredentialType == 'Social Security Number' || inputCredentialType == 'Social Insurance Number') {
            if (credentialId == null) {
                throw new ApplicationException('Person', new BusinessLogicValidationException("ssn.credentialId.null.message",[]))
            }
            if (credentialId.trim() == '') {
                throw new ApplicationException('Person', new BusinessLogicValidationException("ssn.credentialId.empty.message",[]))
            }
            if (credentialId.length() > 9) {
                throw new ApplicationException('Person', new BusinessLogicValidationException("credentialId.length.message",[]))
            }
        }
    }


    private def createSSN(String inputCredentialType, String credentialId, Map person) {
        if (inputCredentialType == 'Social Security Number' || inputCredentialType == 'Social Insurance Number') {
            //Copy ssn attribute from credential to person map.
            person.put('ssn', credentialId)
        }

        return person
    }


    private def updateSSN(String inputCredentialType, String credentialId, def personBase) {
        if (inputCredentialType == 'Social Security Number' || inputCredentialType == 'Social Security Number') {
            if (personBase.ssn == null || personBase.ssn != credentialId) {
                personBase.ssn = credentialId
            }
        }

        return personBase
    }


    private String getRequestedVersion() {
        String representationVersion = LdmService.getRepresentationVersion()
        if (representationVersion == null) {
            // Assume latest (current) version
            representationVersion = "v2"
        }
        return representationVersion
    }


    private def getPreferredEmail(List<Map> emailsInRequest) {
        def preferredEmail = null
        if ("v2".equals(getRequestedVersion())) {
            preferredEmail = emailsInRequest.findAll { it.get("emailType")?.trim() == PERSON_EMAIL_TYPE_PREFERRED }[0]
            if (preferredEmail) {
                emailsInRequest.removeAll { it.get("emailType").trim() == PERSON_EMAIL_TYPE_PREFERRED }
            }
        }

        return preferredEmail
    }

}
