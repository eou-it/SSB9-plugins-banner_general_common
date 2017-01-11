/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.service.ServiceBase

class PriorCollegeMinorService extends ServiceBase {

    def preUpdate(map) {
        throw new ApplicationException(PriorCollegeMinorService, "@@r1:unsupported.operation@@")
    }

    List<PriorCollegeMinor> fetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegree(List<Integer> pidmList, List<String> priorCollegeCodeList,
                                                                                                            List<Integer> degreeSequenceNumberList, List<String> degreeCodeList) {
        List<PriorCollegeMinor> priorCollegeMinorList = []
        if (pidmList && priorCollegeCodeList && degreeSequenceNumberList && degreeCodeList) {
            PriorCollegeMinor.withSession { session ->
                priorCollegeMinorList = session.getNamedQuery("PriorCollegeMinor.fetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegree")
                        .setParameterList("pidmList", pidmList)
                        .setParameterList("sourceAndBackgroundInstitutionCodeList", priorCollegeCodeList)
                        .setParameterList("degreeSequenceNumberList", degreeSequenceNumberList)
                        .setParameterList("degreeCodeList", degreeCodeList)
                        .list()

            }
        }
        return priorCollegeMinorList
    }
}
