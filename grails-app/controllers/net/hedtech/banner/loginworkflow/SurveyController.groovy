/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */

package net.hedtech.banner.loginworkflow

import net.hedtech.banner.general.person.PersonBasicPersonBase
import net.hedtech.banner.general.person.PersonRace
import net.hedtech.banner.general.system.Race
import net.hedtech.banner.general.system.RegulatoryRace
import net.hedtech.banner.security.BannerUser
import org.springframework.security.core.context.SecurityContextHolder

class SurveyController {
    static defaultAction = "index"
    def surveyService

    def survey() {
        def pidm = getPidm()
        def raceMap = [:]
        def regulatoryRaces = RegulatoryRace.fetchRequiredRegulatoryRaces()
        regulatoryRaces.each { regulatoryRace ->
            def races = []
            races = Race.fetchAllByRegulatoryRace(regulatoryRace.code)
            if (!races.isEmpty()) {
                raceMap.put(regulatoryRace.code, races)
            }
        }
        // Get all Race Categories by system indicator = 'Y'
        // Loop over the Categories, get the Races under each Category and put them inside model.
        // Find a way to differentiate selected Races by the User from the not selected Races.
        def personRaces = PersonRace.fetchByPidm(pidm)
        def personRaceCodes = []
        personRaces?.each {
            personRaceCodes.add(it.race)
        }
        def personBasicPersonBase = PersonBasicPersonBase.fetchByPidm(pidm)
        def personEthnicity = personBasicPersonBase?.ethnic
        session.setAttribute("raceMap", raceMap)
        session.setAttribute("regulatoryRaces", regulatoryRaces)
        def model = [raceMap: raceMap, regulatoryRaces: regulatoryRaces, personRaceCodes: personRaceCodes, personEthnicity: personEthnicity, postUrl: "${request.contextPath}/survey/save"]
        render view: "survey", model: model
    }


    def save = {
        def pidm = getPidm()
        surveyService.saveSurveyResponse(pidm, params.ethnicity, params.race)
        completed()
    }


    def completed() {
        request.getSession().setAttribute("surveydone", "true")
        done();
    }


    def done() {
        String path = request.getSession().getAttribute("URI_ACCESSED")
        if (path == null) {
            path = "/"
        }
        redirect uri: path
    }


    public static def getPidm() {
        def user = SecurityContextHolder?.context?.authentication?.principal
        if (user instanceof BannerUser) {
            return user.pidm
        }
        return null
    }
}
