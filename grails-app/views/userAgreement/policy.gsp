<%@ page contentType="text/html;charset=UTF-8" %>
<%--
/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
--%>
<html>
<head>
    <title><g:message code="net.hedtech.banner.termsofuse.title"/></title>
    <meta name="layout" content="bannerSelfServicePage"/>
    <r:require module="userAgreement"/>
    <g:if test="${message(code: 'default.language.direction') == 'rtl'}">
        <r:require module="userAgreementRTL"/>
    </g:if>
</head>
<body>
<div id="content">
    <div id="bodyContainer">
        <div id="pageheader">
            <div id="pagetitle"><g:message code="net.hedtech.banner.termsofuse.title"/></div>
        </div>
        <div id="pagebody" class="loginterms level">
            <div id="contentHolder">
                <div id="contentBelt"></div>
                <div class="pagebodydiv" style="display: block;">
                    <div role="dialog" aria-describedby="terms-text-style">
                        <div class="termstextdiv">
                            <sanitizeMarkdown:renderHtml text="${infoText}" />
                        </div>
                        <div class="button-area">
                            <input type='button' value='<g:message code="net.hedtech.banner.termsofuse.button.continue"/>' id="policy-continue" class="secondary-button"
                                   data-endpoint="${createLink(controller: "userAgreement", action: "agreement")}"/>
                            <input type='button' value='<g:message code="net.hedtech.banner.termsofuse.button.exit"/>' id="policy-exit" class="secondary-button"
                                   data-endpoint="${createLink(controller: "logout")}"/>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
