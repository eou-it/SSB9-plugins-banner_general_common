/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.service.ServiceBase

class GeneralForStoringResponsesAndPinQuestionService extends ServiceBase {

    boolean transactional = true

    def fetchQuestionForPidm(int pidm) {
        GeneralForStoringResponsesAndPinQuestion.withSession { session ->
            def generalForStoringResponsesAndPinQuestion = session.getNamedQuery('GeneralForStoringResponsesAndPinQuestion.fetchQuestionForPidm')
                    .setInteger('pidm', pidm)
                    .list()
            return generalForStoringResponsesAndPinQuestion
        }
    }

    def fetchCountOfSameQuestionForPidmById(int pidm, String questionDescription, int id) {
        GeneralForStoringResponsesAndPinQuestion.withSession { session ->
            def duplicateQuestionCount = session.getNamedQuery('GeneralForStoringResponsesAndPinQuestion.fetchCountOfSameQuestionForPidmById')
                    .setInteger('pidm', pidm)
                    .setString('questionDescription', questionDescription)
                    .setInteger('id', id)
                    .list()[0]
            return duplicateQuestionCount
        }
    }
}
