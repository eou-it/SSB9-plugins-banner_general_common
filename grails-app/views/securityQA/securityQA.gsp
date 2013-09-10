<%@ page contentType="text/html;charset=UTF-8" %>
<%--
/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
--%>
<html>
<head>
    <title>Security Question and Answer</title>
    <meta name="layout" content="bannerSelfServicePage"/>
    <r:require modules="securityQA"/>
    <r:script disposition="head">
               window.securityQAInitErrors = {
                   notification: "${notification}"
               };
    </r:script>
</head>

<body>
<script>
    var userDefinedQuesFlag = "${userDefinedQuesFlag}";
    var questions = new Array();
    var selectedQues = new Array();
    questions.push("Not Selected");
    <g:each var="ques" in="${questions}">
    questions.push("${ques}");
    </g:each>

    <g:if test="${selectedQues.size()>0}">
    <g:each var="selques" in="${selectedQues}">
    selectedQues.push("${selques}");
    </g:each>
    </g:if>

    var prev = "";
    var questionMinimumLength =${questionMinimumLength};
    var answerMinimumLength =${answerMinimumLength};
    var selectedAns =${selectedAns};
    var selectedUserDefinedQues = new Array();

    <g:each var="selques" in="${selectedUserDefinedQues}">
    selectedUserDefinedQues.push("${selques}");
    </g:each>

    <g:each var="selques" in="${selectedAns}">
    selectedAns.push("${selques}");
    </g:each>

    function updateSelection(elm) {
        var selectElements = $('select#question');
        if (elm.value != "null") {
            for (var i = 0; i < selectElements.length; i++) {
                if (prev != elm.value) {
                    $($('select#question')[i]).find('option[value="' + prev + '"]').show();
                }
                if (!($('select#question')[i] == elm)) {
                    $($('select#question')[i]).find('option[value="' + elm.value + '"]').hide();
                }
            }
        }
        else {
            for (var i = 0; i < selectElements.length; i++) {
                $($('select#question')[i]).find('option[value="' + prev + '"]').show();
            }
        }
    }

    function handleClick(elm) {
        prev = $(elm).find(":selected").val();
    }





</script>

<div id="content">
    <div id="bodyContainer" class="ui-layout-center inner-center">
        <div id="pageheader" class="level4">
            <div id="pagetitle"><g:message code="securityQA.title"/></div>
        </div>

        <div id="pagebody" class="level4">
            <div id="contentHolder">
                <div id="contentBelt"></div>

                <div class="pagebodydiv" style="display: block;">
                    <div id="errorMessage">

                    </div>

                    <div>
                        <span><i class="informationImage"></i></span><label><div
                            class="section-message"><g:message
                            code="securityQA.information"/></label></div>
                </div>
                <br/>
                <br/>

                <form action='${createLink(controller: "securityQA", action: "save")}' id='securityForm'
                      method='POST'>
                    <div class="section-wrapper">
                        <div class="question-label">
                            <label><g:message code="securityQA.confirmpin.label"/></label>
                        </div>

                        <div id="textLabel">
                            <g:field name="pin" class="section-text" type="password"></g:field>
                        </div>
                    </div>
                </br>
                    <g:each in="${1..noOfquestions}" status="i" var="ques">
                        <div class="section-wrapper">
                            <div class="question-label"><label><g:message code="securityQA.question.label"
                                                                          args="[i + 1]"/></label></div>

                            <div class="select-wrapper">
                                <select class="select" id="question" name="question">
                                    <option value="question0">Not Selected</option>
                                    <g:each in="${questions}" status="j" var="innerQues">
                                        <option value="question${j + 1}">${innerQues}</option>
                                    </g:each>
                                </select>
                            </div>
                            <g:if test="${userDefinedQuesFlag == 'Y'}">
                                <label><g:message code="securityQA.or.label"/></label>

                                <div class="section-wrapper">
                                    <div class="question-label"><label><g:message
                                            code="securityQA.userdefinedquestion.label"
                                            args="[i + 1]"/></label></div>

                                    <div><g:textField name="userDefinedQuestion" id="userDefinedQuestion"
                                                      value="${selectedUserDefinedQues[i]}"
                                                      class="section-text"></g:textField></div>
                                </div>
                            </g:if>
                        </div>
                        <br/>

                        <div class="section-wrapper">
                            <div class="question-label"><label><g:message code="securityQA.answer.label"
                                                                          args="[i + 1]"/></label></div>

                            <div><g:textField name="answer" class="section-text"
                                              value="${selectedAns[i]}"></g:textField></div>
                        </div>
                        <br/>
                    </g:each>
                    <div class="button-area">
                        <input type='button' value="Cancel" id="security-cancel-btn" class="secondary-button"
                               x data-endpoint="${createLink(controller: "logout")}"/>
                        <input type='button' value="Continue" id="security-save-btn" class="primary-button"/>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
</div>
</body>
</html>
