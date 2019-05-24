/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.common

import net.hedtech.banner.general.utility.InformationText
import net.hedtech.banner.general.utility.InformationTextPersonaListService
import net.hedtech.banner.general.utility.SourceIndicators
import net.hedtech.banner.security.BannerGrantedAuthorityService
import org.springframework.context.i18n.LocaleContextHolder
import org.apache.commons.lang.LocaleUtils

class GeneralInformationTextUtility {

    public static List<String> getFallbackLocaleNames( Locale locale ) {
        List<Locale> locales = LocaleUtils.localeLookupList( locale, Locale.default )
        return locales*.toString()

    }

    /**
     * Method getQueryParamForRoles method returns roles.
     * @return
     */

    private static List<String> getQueryParamForRoles() {
        List<String> roles = BannerGrantedAuthorityService.getSelfServiceDistinctUserRole()
        roles << InformationTextPersonaListService.PERSONA_DEFAULT
        roles = getParams( roles )
        return roles
    }


    private static List<String> getParams( List<String> roles ) {
        List localparams = []
        for (int i = 0; i < roles.size(); i++) {
            localparams << roles.get( i )
        }
        return localparams
    }


    public
    static List<InformationText> getResultSetPrioritzedForLocale( List<InformationText> resultSet, List<String> localeList ) {
        Map<String, InformationText> infoTextGroupedByLabel = resultSet.groupBy( {informationText -> informationText.label} )
        List<InformationText> infoTextFilteredForFallbackLocale = []
        infoTextGroupedByLabel?.each {label, infoTextsForLabel ->
            localeList.find {locale ->
                def foundLabelsForLocale = infoTextsForLabel.findAll {infoTextForLabel ->
                    infoTextForLabel.locale == locale
                }
                if (foundLabelsForLocale) {
                    infoTextFilteredForFallbackLocale += foundLabelsForLocale
                } else {
                    return false
                }
            }
        }
        return infoTextFilteredForFallbackLocale
    }

    /***
     *   Method returns a filtered result by removing baseline records if at least one local record is
     *   present for a set of labels returned by the query.
     * @param resultSet
     * @return
     */
    private static Collection<InformationText> getFilteredResultSet( List<InformationText> resultSet ) {
        Set<String> labels = new HashSet<String>();
        for (InformationText row : resultSet) {
            labels.add( row.getLabel() )
        }

        List<InformationText> modifiedResultSet = new ArrayList<InformationText>()
        labels.each {String label ->
            List<InformationText> resultSubSet = resultSet.findAll {row ->
                row.label == label
            }
            resultSubSet = getFilteredResultSetForLabel( resultSubSet )
            modifiedResultSet.addAll( resultSubSet )
        }
        return modifiedResultSet
    }
    /**
     *
     * Gives filtered resultSet of a message for a particular label by removing all baseline records if at least one local record is present
     * with start date specified.
     */
    private static Collection<InformationText> getFilteredResultSetForLabel( List<InformationText> resultSet ) {
        List<InformationText> localInfoTextsWithStartDate = resultSet.findAll {
            it.sourceIndicator == SourceIndicators.LOCAL.getCode() && it.startDate != null
        }

        List<InformationText> localInfoTexts = resultSet.findAll {
            it.sourceIndicator == SourceIndicators.LOCAL.getCode()
        }

        List<InformationText> baselineInfoTexts = resultSet - localInfoTexts

        if (localInfoTextsWithStartDate.size() > 0) {
            resultSet = getDefaultOrNonDefaultResultSet( localInfoTextsWithStartDate )
        } else {
            resultSet = getDefaultOrNonDefaultResultSet( baselineInfoTexts )
        }
        return resultSet
    }


    private static Collection<InformationText> getDefaultOrNonDefaultResultSet( List<InformationText> resultSet ) {
        List<InformationText> defaultInfoText = resultSet.findAll {
            it.persona == InformationTextPersonaListService.PERSONA_DEFAULT
        }
        List<InformationText> nonDefaultInfoText = resultSet - defaultInfoText
        if (nonDefaultInfoText.size() > 0) {
            resultSet = nonDefaultInfoText
        } else {
            resultSet = defaultInfoText
        }
        return resultSet
    }


    private static String getInfoText( infoText, infoTextResultSet ) {
        String text = ""
        String tempText = getTextBasedOnDateRange( infoTextResultSet )
        if (infoText == null || infoText == "") {
            text = tempText
        } else {
            if (tempText != "") {
                text = "\n" + tempText
            }
        }
        return text
    }


    private static String getTextBasedOnDateRange( InformationText row ) {
        if (row.sourceIndicator == "${SourceIndicators.LOCAL.getCode()}" && row.startDate == null) {
            return ""
        } else {
            String text = row.text
            text = text != null ? text : ""
            return text
        }
    }

    /*****
     * getMessages method returns information text message for the given pagename and locale.
     * GeneralInformationTextUtility.getMessages(<GURINFO_PAGE_NAME>)
     * This utility will just take a page name and return a map of all the information texts for the specific page.
     * The map will have the label as a infoTextKey and information text as the value.
     * Example Implementation - Map infoTexts = GeneralInformationTextUtility.getMessage("TERMSELECTION"); String infoText = infoTexts.get("termSelect.bodyTitle")
     * @param pageName
     * @param locale
     * @return
     */
    public static Map getMessages( String pageName, Locale locale = LocaleContextHolder.getLocale() ) {
        Map informationTexts = new HashMap<String, String>()
        Map defaultRoleInfoTexts = new HashMap<String, String>()
        List<String> localeList = getFallbackLocaleNames( locale )
        List<String> roleCode = getQueryParamForRoles()
        List<InformationText> resultSet;
        if (roleCode) {
            resultSet = InformationText.fetchInfoTextByRoles( pageName, roleCode, localeList )
            resultSet = getResultSetPrioritzedForLocale( resultSet, localeList )
            resultSet = getFilteredResultSet( resultSet )
            for (InformationText infoTextsGroupByRole : resultSet) {
                String infoText = ""
                if (infoTextsGroupByRole.persona == InformationTextPersonaListService.PERSONA_DEFAULT && infoTextsGroupByRole.startDate != null) {
                    infoText = defaultRoleInfoTexts.get( infoTextsGroupByRole.label )
                    infoText = infoText != null ? infoText : ""
                    infoText = infoText + getInfoText( infoText, infoTextsGroupByRole )
                    defaultRoleInfoTexts.put( infoTextsGroupByRole.label, infoText )
                } else {
                    infoText = informationTexts.get( infoTextsGroupByRole.label )
                    infoText = infoText != null ? infoText : ""
                    infoText = infoText + getInfoText( infoText, infoTextsGroupByRole )
                    informationTexts.put( infoTextsGroupByRole.label, infoText )
                }
            }

            defaultRoleInfoTexts.each {String infoTextKey, String value ->
                if (notExistsNonDefaultRole( informationTexts, infoTextKey )) {
                    informationTexts.put( infoTextKey, value )
                }
            }
        }
        return informationTexts
    }

    /***
     *
     * @param defaultRoleInfoTexts
     * @param informationTexts
     */
    private static boolean notExistsNonDefaultRole( Map informationTexts, String key ) {
        if (informationTexts.containsKey( key )) {
            return false
        } else {
            return true
        }
    }
}
