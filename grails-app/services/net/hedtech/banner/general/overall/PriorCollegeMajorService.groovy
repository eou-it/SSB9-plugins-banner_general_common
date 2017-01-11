/*******************************************************************************
 Copyright 2010-2016 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.service.ServiceBase

class PriorCollegeMajorService extends ServiceBase {

    def preUpdate(map) {
        throw new ApplicationException(PriorCollegeMajorService, "@@r1:unsupported.operation@@")
    }

    List<PriorCollegeMajor> fetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegree(List<Integer> pidmList, List<String> priorCollegeCodeList,
                                                                                                            List<Integer> degreeSequenceNumberList, List<String> degreeCodeList){
        List<PriorCollegeMajor> priorCollegeMajorList = []
        if(pidmList&&priorCollegeCodeList&&degreeSequenceNumberList&&degreeCodeList){
            PriorCollegeMajor.withSession{ session ->
                priorCollegeMajorList = session.getNamedQuery("PriorCollegeMajor.fetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegree")
                                                .setParameterList("pidmList", pidmList)
                                                .setParameterList("sourceAndBackgroundInstitutionCodeList", priorCollegeCodeList)
                                                .setParameterList("degreeSequenceNumberList", degreeSequenceNumberList)
                                                .setParameterList("degreeCodeList", degreeCodeList)
                                                .list()

            }
        }
        return priorCollegeMajorList
    }
}
