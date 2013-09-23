package net.hedtech.banner.loginworkflow

import net.hedtech.banner.security.BannerGrantedAuthorityService

class UserAgreementController {

    def userAgreementService

    static defaultAction = "index"

    def index() {
        def infoText = userAgreementService.getTermsOfUseInfoText()
        def model = [infoText: infoText]
        log.info("rendering view")
        render view: "policy", model: model
    }

    def agreement() {
        String pidm = BannerGrantedAuthorityService.getPidm()
        userAgreementService.updateUsageIndicator(pidm, "Y");
        request.getSession().setAttribute("useraggrementdone", "true")
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
