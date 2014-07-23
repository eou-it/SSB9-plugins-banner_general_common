/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.ldm.person

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.person.PersonAddress
import net.hedtech.banner.general.person.PersonBasicPersonBase
import net.hedtech.banner.general.person.PersonEmail
import net.hedtech.banner.general.person.PersonRace
import net.hedtech.banner.general.person.PersonIdentificationNameCurrent
import net.hedtech.banner.general.person.PersonTelephone
import net.hedtech.banner.general.system.Race
import net.hedtech.banner.general.overall.ldm.GlobalUniqueIdentifier

import org.springframework.transaction.annotation.Transactional

@Transactional
class LdmPersonService {

    def personIdentificationNameCurrentService
    def personBasicPersonBaseService

    @Transactional(readOnly = true)
    def get(id) {
        // TODO: Exception handling / checking nulls
        def entity = GlobalUniqueIdentifier.findByGuid(id)
        if( !entity ) {
            throw new ApplicationException("Person","@@r1:not.found.message:Person::BusinessLogicValidationException@@")
        }
        PersonIdentificationNameCurrent personIdentificationName = PersonIdentificationNameCurrent.get(entity.domainId)
        if( !personIdentificationName ) {
            throw new ApplicationException("Person","@@r1:not.found.message:Person::BusinessLogicValidationException@@")
        }
        PersonBasicPersonBase personBase = PersonBasicPersonBase.findByPidm(personIdentificationName.pidm)

        // Get list of Race descriptions
        def races = []
        def raceCodes = PersonRace.executeQuery("select race from PersonRace where pidm = :pidm",
                [pidm: personIdentificationName.pidm])
        Race.findAllByRaceInList(raceCodes).each {
            races << it.description
        }

        //Store the credential we already have
        def credentials = []
        credentials << new CredentialV1("Person ID",
                personIdentificationName.bannerId,
                personIdentificationName.createDate,
                null)
        // TODO: Add other credentials.

        // Get and store addresses
        def addresses = []
        PersonAddress.fetchActiveAddressesByPidm([pidm: personIdentificationName.pidm]).list?.each { it ->
                addresses << new AddressV1(it)
        }
        // Get and store phones
        def phones = []
        PersonTelephone.fetchActiveTelephoneByPidm(personIdentificationName.pidm).each { it ->
            phones << new PhoneV1(it)
        }
        // Get and store emails
        def emails = []
        PersonEmail.findAllByPidmAndStatusIndicator(personIdentificationName.pidm, "A").each { it ->
            emails << new EmailV1(it)
        }

        // Create decorated object to return.
        new PersonV1( personBase, personIdentificationName, entity.guid, races,
                credentials, addresses, phones, emails)
    }

    def create(Map person) {
        Map newPerson = person
        newPerson.put('sex', person?.sex == 'Male' ? 'M':(this.person?.sex == 'Female' ? 'F' : 'N'))
        newPerson.put('pidm', null)
        // TODO: Allow specifying banner Id from credentials list.
        newPerson.put('bannerId','GENERATED')
        newPerson.put('changeIndicator', null)
        // TODO: Guessing which ethnicity to put here as several equal Non-Hispanic or Hispanic  Maybe this should look at ethnic on spriden instead?
        newPerson.put('ethnicity', person?.ethnicity == "Non-Hispanic" ? "6" : ( person?.ethnicity == "Hispanic" ? "3" : null))
        newPerson.put('armedServiceMedalVetIndicator',person?.armedServiceMedalVetIndicator == true ?: false) // TODO: should this be defaulting to false if not provided?
        newPerson.put('entityIndicator', newPerson.entityIndicator ?: 'P')

        //Create the new PersonIdent record
        PersonIdentificationNameCurrent newPersonIdentificationName = personIdentificationNameCurrentService.create(newPerson)
        //Fix the GUID as DB will assign one
        updateGuidValue(newPersonIdentificationName.id, person.guid)

        //Prepare map and create personBase record.
        newPerson.put('pidm', newPersonIdentificationName?.pidm)
        newPerson.remove('domainModel')
        PersonBasicPersonBase newPersonBase = personBasicPersonBaseService.create(newPerson)
        // TODO: Create the rest of the objects. Easier!


        //Build decorator to return LDM response.
        return new PersonV1( newPersonBase, newPersonIdentificationName, newPerson.guid, [], [], [], [], [])
    }

    // TODO: validate guid format.
    private void updateGuidValue(def id, def guid) {
        // Update the GUID to the one we received.
        GlobalUniqueIdentifier newEntity = GlobalUniqueIdentifier.findByLdmNameAndDomainId('persons', id)
        if( !newEntity ) {
            throw new ApplicationException("Person","@@r1:guid.record.not.found.message:Person@@")
        }
        if( !newEntity ) {
            throw new ApplicationException("Person","@@r1:guid.not.found.message:Person:BusinessLogicValidationException@@")
        }
        newEntity.guid = guid
        newEntity.save(failOnError: true)
    }

}
