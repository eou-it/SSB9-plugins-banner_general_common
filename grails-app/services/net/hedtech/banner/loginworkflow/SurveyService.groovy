/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */

package net.hedtech.banner.loginworkflow

import net.hedtech.banner.general.person.PersonBasicPersonBase
import net.hedtech.banner.general.person.PersonRace
import net.hedtech.banner.service.ServiceBase


class SurveyService extends ServiceBase {

    boolean transactional = true
    def personRaceService
    def personBasicPersonBaseService

    def saveSurveyResponse(pidm, ethnicity, races) {
        def personBasicPersonBase = PersonBasicPersonBase.fetchByPidm(pidm)
        if (personBasicPersonBase) {
            personBasicPersonBase.ethnic = ethnicity
            personBasicPersonBase.confirmedRe = 'Y'
            personBasicPersonBase.confirmedReDate = new Date()
            personBasicPersonBaseService.update(personBasicPersonBase)
        } else {
            personBasicPersonBase = new PersonBasicPersonBase(
                    pidm: pidm,
                    ethnic: ethnicity,
                    confirmedRe: 'Y',
                    confirmedReDate: new Date(),
                    armedServiceMedalVetIndicator: false
            )
            personBasicPersonBaseService.create(personBasicPersonBase)
        }
        // Save races
        def createPersonRaces = []
        def deletePersonRaces = []
        def personRace
        if (races instanceof String) {
            personRace = PersonRace.fetchByPidmAndRace(pidm, races)
            createPersonRaces << (personRace ?: new PersonRace(pidm: pidm, race: races))
        } else {
            races?.each { race ->
                personRace = PersonRace.fetchByPidmAndRace(pidm, race)
                createPersonRaces << (personRace ?: new PersonRace(pidm: pidm, race: race))
            }
        }
        def savedPersonRaces = PersonRace.fetchByPidm(pidm)
        deletePersonRaces = savedPersonRaces - createPersonRaces
        personRaceService.createOrUpdate([createPersonRaces: createPersonRaces, deletePersonRaces: deletePersonRaces])

    }

}
