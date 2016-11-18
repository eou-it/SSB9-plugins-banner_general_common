/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.service.ServiceBase

class PriorCollegeConcentrationAreaService extends ServiceBase {

    def preUpdate(map) {
        throw new ApplicationException(PriorCollegeConcentrationArea, "@@r1:unsupported.operation@@")
    }

    List<PriorCollegeConcentrationArea> fetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegree(
            List<Integer> pidmList, List<String> priorCollegeCodeList, List<Integer> degreeSequenceNumberList, List<String> degreeCodeList){
        List<PriorCollegeConcentrationArea> priorCollegeConcentrationAreaList = []
        if(pidmList&&priorCollegeCodeList&&degreeSequenceNumberList&&degreeCodeList){
            PriorCollegeConcentrationArea.withSession{ session ->
                priorCollegeConcentrationAreaList = session.getNamedQuery("PriorCollegeConcentrationArea.fetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegree")
                        .setParameterList("pidmList", pidmList)
                        .setParameterList("sourceAndBackgroundInstitutionCodeList", priorCollegeCodeList)
                        .setParameterList("degreeSequenceNumberList", degreeSequenceNumberList)
                        .setParameterList("degreeCodeList", degreeCodeList)
                        .list()

            }
        }
        return priorCollegeConcentrationAreaList
    }
}
