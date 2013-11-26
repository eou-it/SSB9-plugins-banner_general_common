/*******************************************************************************
 Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.loginworkflow

import net.hedtech.banner.security.BannerGrantedAuthorityService
import net.hedtech.banner.web.SsbURLRequest

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
        userAgreementService.updateUsageIndicator(pidm, UserAgreementFlow.TERMS_OF_USAGE_ANSWERED);
        request.getSession().setAttribute(UserAgreementFlow.USER_AGREEMENT_ACTION, ACTION_DONE)
        done();
    }

    def done() {
        String path = request.getSession().getAttribute(PostLoginWorkflow.URI_ACCESSED)
        if (path == null) {
            path = "/"
        } else {
            path = checkPath(path)
        }
        request.getSession().setAttribute(PostLoginWorkflow.URI_REDIRECTED, path)
        redirect uri: path
    }

    protected String checkPath(String path) {
        SsbURLRequest ssbURLRequest = new SsbURLRequest()
        String controllerName = ssbURLRequest.getControllerNameFromPath(path)
        List<PostLoginWorkflow> listOfFlows = PostLoginWorkflow.getListOfFlows()
        for (PostLoginWorkflow flow : listOfFlows) {
            if (flow.getControllerName().equals(controllerName)) {
                path = "/"
                return path
            }
        }
        return path
    }

}
