/*******************************************************************************
 Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.loginworkflow

import net.hedtech.banner.security.BannerGrantedAuthorityService

class UserAgreementController {

    def userAgreementService

    static defaultAction = "index"
    private static final ACTION_DONE = "true"

    def index() {
        def infoText = userAgreementService.getTermsOfUseInfoText()
        def model = [infoText: infoText]
        log.info("rendering view")
        render view: "policy", model: model
    }

    def agreement() {
        String pidm = BannerGrantedAuthorityService.getPidm()
        userAgreementService.updateUsageIndicator(pidm, UserAgreementFlow.TERMS_OF_USAGE_ANSWERED );
        request.getSession().setAttribute(UserAgreementFlow.USER_AGREEMENT_ACTION, ACTION_DONE)
        done();
    }

    def done() {
        String path = request.getSession().getAttribute(PostLoginWorkflow.URI_ACCESSED)
        if (path == null) {
            path = "/"
        }
        redirect uri: path
    }
}
