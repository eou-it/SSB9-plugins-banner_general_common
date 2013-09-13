<div id="editSurvey">
   <div id="ethnicity-wrapper">
       <div id="ethnicity-header" class="section-header">
           <span class="section-header-text"><g:message code="survey.ethnicity.header" /></span>
       </div>
       <div id="ethnicity">
           <input id="chkEthn_1" name="ethnicity" value="1" type="checkbox" <g:if test="${personEthnicity == '1'}">checked="true"</g:if> />
           <label for="chkEthn_1" class="content-label"><g:message code="survey.ethnicity.nothispanic" /></label>
           <br/>
           <input id="chkEthn_2" name="ethnicity" value="2" type="checkbox" <g:if test="${personEthnicity == '2'}">checked="true"</g:if> />
           <label for="chkEthn_2" class="content-label"><g:message code="survey.ethnicity.hispanic" /></label>
       </div>
   </div>
   <div id="race-wrapper">
       <div id="race-header" class="section-header">
           <span class="section-header-text"><g:message code="survey.race.header" /></span>
       </div>
       <g:each in="${regulatoryRaces}" var="regulatoryRace">
           <div id="race-category_${regulatoryRace.code}" class="race-category-area">
               <div id="race-category-desc" class="race-category-header">${regulatoryRace.description}</div>
               <div id="races_${regulatoryRace.code}" class="races-content">
                   <g:each in="${raceMap[regulatoryRace.code]}" var="race">
                       <input id="chkRace_${race.race}" name="race" value="${race.race}" type="checkbox" <g:if test="${personRaceCodes.contains(race.race)}">checked="true"</g:if>/>
                       <label for="chkRace_${race.race}" class="content-label">${race.description}</label>
                       <br/>
                   </g:each>
               </div>
           </div>
       </g:each>
   </div>
</div>
