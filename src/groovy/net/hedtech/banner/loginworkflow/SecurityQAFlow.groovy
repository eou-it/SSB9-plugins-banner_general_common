/*******************************************************************************
 Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.loginworkflow

import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.springframework.context.ApplicationContext

import java.sql.SQLException
import net.hedtech.banner.security.BannerGrantedAuthorityService

class SecurityQAFlow extends PostLoginWorkflow {

    def securityQAService

    public boolean showPage(request) {
        def session = request.getSession();
        String isDone = session.getAttribute("securityqadone")
        boolean displayPage = false
        if(isDone != "true"){
            initializeSecurityQAService()
            Map map = getUserDefinedPreference()
            def noOfQuestions = getNumberOfQuestions(map)
            if(getDisableForgetPinIndicator(map).equals("N") && noOfQuestions > 0 && !isUserAlreadyAnsweredSecurityQA(noOfQuestions)) {
                displayPage = true
            }
        }

        return displayPage
    }

    public String getControllerUri() {
        return "/ssb/securityQA"
    }

    public String getControllerName() {
        return "securityQA"
    }

    private void initializeSecurityQAService() {
        ApplicationContext ctx = (ApplicationContext) ApplicationHolder.getApplication().getMainContext()
        securityQAService = (SecurityQAService) ctx.getBean("securityQAService")
    }

    private Map getUserDefinedPreference() {
        return securityQAService.getUserDefinedPreference()
    }

    private def getNumberOfQuestions(Map map){
        return map?.GUBPPRF_NO_OF_QSTNS
    }

    private String getDisableForgetPinIndicator(Map map){
        return map?.GUBPPRF_DISABLE_FORGET_PIN_IND
    }

    private boolean isUserAlreadyAnsweredSecurityQA(noOfQuestions){

        if(noOfQuestions > securityQAService.getNumberOfQuestionsAnswered(BannerGrantedAuthorityService.getPidm())) {
            return false
        }

        return true
    }
}
