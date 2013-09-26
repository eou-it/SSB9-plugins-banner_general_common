<%--
/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
--%>
<div id="editSurvey">
    <div id="ethnicity-wrapper" role="dialog" aria-describedby="section-header-text1">
        <div id="ethnicity-header" class="section-header">
            <span class="section-header-text" id="section-header-text1" role="header">${infoTexts."ethnicity.header"}%{--<g:message code="survey.ethnicity.header" />--}%</span>
        </div>
        <div id="ethnicity">
            <div>
                <input id="chkEthn_1" name="ethnicity" role="checkbox" value="1" type="checkbox" <g:if test="${personEthnicity == '1'}">checked="true"</g:if> />
                <label for="chkEthn_1" class="content-label"><g:message code="survey.ethnicity.nothispanic" /></label>
            </div>
            <div>
                <input id="chkEthn_2" name="ethnicity" role="checkbox" value="2" type="checkbox" <g:if test="${personEthnicity == '2'}">checked="true"</g:if> />
                <label for="chkEthn_2" class="content-label"><g:message code="survey.ethnicity.hispanic" /></label>
            </div>
        </div>
    </div>
    <div id="race-wrapper">
        <div id="race-header" class="section-header">
            <span class="section-header-text" role="alert" id="section-header-text2">${infoTexts."race.header"}%{--<g:message code="survey.race.header" />--}%</span>
        </div>
        <g:each in="${regulatoryRaces}" var="regulatoryRace">
            <div id="race-category_${regulatoryRace.code}" class="race-category-area">
                <div id="race-category-desc" class="race-category-header">${regulatoryRace.description}</div>
                <div id="races_${regulatoryRace.code}" class="races-content">
                    <g:each in="${raceMap[regulatoryRace.code]}" var="race">
                        <div>
                            <input id="chkRace_${race.race}" class="raceCheckbox" role="checkbox" name="race" value="${race.race}" type="checkbox" <g:if test="${personRaceCodes.contains(race.race)}">checked="true"</g:if>/>
                            <label for="chkRace_${race.race}" class="content-label">${race.description}</label>
                        </div>
                    </g:each>
                </div>
            </div>
        </g:each>
    </div>
</div>
