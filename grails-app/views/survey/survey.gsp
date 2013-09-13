<%@ page contentType="text/html;charset=UTF-8" %>
<%--
/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
--%>
<html>
<head>
    <title>Race and Ethnicity Survey</title>
    <meta name="layout" content="bannerSelfServicePage"/>
    <meta name="menuEndPoint" content="${g.createLink(controller: 'selfServiceMenu', action: 'data')}"/>
    <meta name="menuBaseURL" content="${createLink(uri: '/ssb')}" />
    <meta name="menuDefaultBreadcrumbId" content=""/>
    <script type="text/javascript">
        document.getElementsByName('menuDefaultBreadcrumbId')[0].content = [
            "<g:message code="survey.breadcrumb.bannerSelfService"/>",
            "<g:message code="survey.breadcrumb.survey"/>"
            ].join('_');
    </script>
    <r:require modules="survey"/>
</head>

<body>
<div id="content">
    <div id="bodyContainer" class="ui-layout-center inner-center">
        <div id="pageheader" class="level4">
            <div id="pagetitle"><g:message code="survey.title" /></div>
        </div>
        <div id="pagebody" class="level4">
            <div id="contentHolder">
                <div id="contentBelt"></div>
                <div class="pagebodydiv" style="display: block;">
                    <div id="errorMessage"></div>
                    <form controller="survey" action="save" id='surveyForm' method='POST'>
                        <div id="ethnicity-race-wrapper">
                           <!--  _editSurvey.gsp-->
                            <div id="editSurvey">
                                <g:render template="editSurvey" model="${[personEthnicity: personEthnicity, regulatoryRace:regulatoryRace]}" />
                            </div>

                            <div id="confirmSurvey">
                                <g:render template="confirmSurvey"
                                          model="${[personEthnicity: personEthnicity, regulatoryRace: regulatoryRace]}"/>
                            </div>
                            <div class="button-area">
                                <input type='button'  id="ask-me-later-btn" value= "<g:message code='survey.edit.button.askMeLater' />" class="secondary-button" data-endpoint="${createLink(controller: "survey", action: "completed")}"/>
                                <input type='button'  id="save-btn" value="<g:message code='survey.edit.button.continue' />" class="primary-button" />
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
