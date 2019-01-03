/*********************************************************************************
 Copyright 2010-2018 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general

import grails.util.Holders
import org.apache.commons.lang.StringUtils
import org.apache.log4j.Logger
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.security.core.context.SecurityContextHolder

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * This is a utilty class for Communication Management
 *
 */
class CommunicationCommonUtility {

    private static final log = Logger.getLogger(CommunicationCommonUtility.class)

    //TODO enhance these regex to make it more comprehensive

    static
    def Pattern sqlpattern = Pattern.compile("\\*|(;)|(\\b(ALTER|CREATE|DATABASE|DELETE|DROP|EXEC(UTE){0,1}|INSERT( +INTO){0,1}|MERGE|UPDATE)\\b)")
    static def Pattern multipattern = Pattern.compile("SELECT(.*?)FROM", Pattern.DOTALL);
    static
    def Pattern scrubPattern = Pattern.compile("[,.;!()/:={}<>\\p{Z}\\|\\\"\\+\\~\\[\\]\\@\\#\\&\\\$\\\\]")
    static def wildcardChar = "%"

    /**
     * Checks if a user input sqlstring has disallowed reserved words
     * @param sqlstring
     * @return true if disallowed words are found false otherwise
     */
    public static Boolean sqlStatementNotAllowed(String sqlstring, Boolean isDataFieldQuery = true) {

        //replace disallowed words with this for testing
        def stringToReplace = "**ZZZZ**"

        //if the input string is null then return false
        if (sqlstring == null || sqlstring == "")
            return false;

        log.debug("The sql string before pattern matching - " + sqlstring)

        //Apply the compiled pattern to the input string
        def sqlToMatch = sqlpattern.matcher(sqlstring.toUpperCase()).replaceAll(stringToReplace)

        log.debug("The sql after pattern matching - " + sqlToMatch)

        //make sure the sql statement only selects one value if requested in the params

        if (!isDataFieldQuery) {
            // Must be a population based query
            String upperCase = sqlstring.toUpperCase()
            Matcher matcher = multipattern.matcher( upperCase )
            if (matcher.find()) {
                if (matcher.group(1).contains(",")) {
                    return true
                }
                if (!matcher.group(1).contains("PIDM")) {
                    return true
                }
                if (upperCase.matches( "(.*)FROM(.*)ORDER\\s+BY(.*)" )) {
                    return true
                }
            }


        };

        //return true if disallowed pattern was found, false if string was clean
        return sqlToMatch.contains(stringToReplace)
    }

    /**
     * this method will scrub user input of characters in the scrub pattern.
     * comma, fullstop, semicolon, exclamation, parenthesis, curly, angle and square brackets
     * @ # $ & / \ : = | + dash double and single quotes tilde accent blank
     *
     * this pattern will check for the following characters. oracle Wildcard character percent, underscore not included.
     * asterisk will be replaced with percent.  ? will be replaced with underscore
     * The input will be converted to upper case and enclosed in wildcard character
     *
     */
    public static getScrubbedInput(String userinput) {

        if (userinput == null || userinput == "") {
            return ""
        }
        def String[] fromstring = ["*", "?"]
        def String[] tostring = ["%", "_"]

        //commenting out the scrubber as it was interfering with other language characters
        //def scrubbedInput = scrubPattern.matcher(userinput).replaceAll("").toLowerCase()

        def scrubbedInput = userinput
        scrubbedInput = StringUtils.replaceEach(scrubbedInput, fromstring, tostring)
        scrubbedInput = wildcardChar + scrubbedInput + wildcardChar

        return scrubbedInput;
    }

//get the oracle userid associated with the login banner id.  If there is a gobeacc record, that oracle user will be
//returned. If no gobeacc record then the banner id will be returned.  Creating queries, templates and datafields needs a oracle user id associated with it
    // as this involves sql statements which need specific banner table permissions.
    def static getUserOracleUserName() {
        def creatorId = SecurityContextHolder?.context?.authentication?.principal?.getOracleUserName() ?: SecurityContextHolder?.context?.authentication?.principal?.username
        return creatorId?.toUpperCase()
    }


    public static getCommunicationUserRoleMap() {
        def map = [:]
        def isUser = false
        def isAuthor = false
        def isAdmin = false
        def canExecuteQuery = false
        def canCreatePopulation = false

        try {
            def authorities = SecurityContextHolder?.context?.authentication?.principal?.authorities

            if (authorities.any { it.getAssignedSelfServiceRole().contains("COMMUNICATIONUSER") }) {
                isUser = true;
            }
            if (authorities.any { it.getAssignedSelfServiceRole().contains("COMMUNICATIONCONTENTADMIN") }) {
                isUser = true
                isAuthor = true
            }
            if (authorities.any { it.getAssignedSelfServiceRole().contains("COMMUNICATIONADMIN") }) {
                isUser = true
                isAuthor = true
                isAdmin = true
            }
            if (authorities.any { it.objectName == "CMQUERYEXECUTE" } && authorities.any {it.objectName == "CMQUERY"}) {
                canExecuteQuery = true
            }
            if (authorities.any { it.objectName == "CMQUERYEXECUTE" } && SecurityContextHolder?.context?.authentication?.principal?.pidm == null ) {
                //Used for manual population creation from the backend API when there is no query involved
                canCreatePopulation = true
            }

            map.put("isUser", isUser)
            map.put("isAuthor", isAuthor)
            map.put("isAdmin", isAdmin)
            map.put("canExecuteQuery", canExecuteQuery)
            map.put("canCreatePopulation", canCreatePopulation)

//get the oracle userid associated with the login banner id.  If there is a gobeacc record, that oracle user will be
//returned. If no gobeacc record then the banner id will be returned
            map.put("userId", getUserOracleUserName())
            map.put("bannerId", (SecurityContextHolder?.context?.authentication?.principal?.username?.toUpperCase()))

            return map
        } catch (Exception it) {
            log.error("principal lacks authorities - may be unauthenticated or session expired. Principal: ${SecurityContextHolder?.context?.authentication?.principal}")
            log.error(it)
            throw it
        }
    }


    public static userCanAuthorContent() {
        try {
            def usermap = getCommunicationUserRoleMap()
            if (usermap.isAuthor)
                return true;

            return false;
        } catch (Exception e) {
            log.error("principal lacks authorities - may be unauthenticated or session expired. Principal: ${SecurityContextHolder?.context?.authentication?.principal}")
            log.error(e)
            throw e
        }
    }


    public static userCanCreatePopulation() {
        try {
            def usermap = getCommunicationUserRoleMap()
            return (usermap.isUser && usermap.canExecuteQuery) || usermap.canCreatePopulation
        } catch (Exception e) {
            log.error("principal lacks authorities - may be unauthenticated or session expired. Principal: ${SecurityContextHolder?.context?.authentication?.principal}")
            log.error(e)
            throw e
        }
    }


    public static userCanUpdateDeleteContent(String createdBy) {
        try {
            def usermap = getCommunicationUserRoleMap()
            if (usermap.isAdmin)
                return true;
            else if (usermap.isAuthor && (usermap.userId)?.equals(createdBy?.toUpperCase()))
                return true;
            else if (usermap.isAuthor && (usermap.bannerId)?.equals(createdBy?.toUpperCase()))
                return true;
            return false;
        } catch (Exception e) {
            log.error("principal lacks authorities - may be unauthenticated or session expired. Principal: ${SecurityContextHolder?.context?.authentication?.principal}")
            log.error(e)
            throw e
        }
    }


    public static userCanUpdateDeletePopulation(String createdBy) {
        try {
            def usermap = getCommunicationUserRoleMap()
            if (usermap.isAdmin)
                return true;
            else if (usermap.isUser && (usermap.userId)?.equals(createdBy?.toUpperCase()))
                return true;
            else if (usermap.isUser && (usermap.bannerId)?.equals(createdBy?.toUpperCase()))
                return true;
            else if (usermap.canCreatePopulation)
                return true;
            return false;
        } catch (Exception e) {
            log.error("principal lacks authorities - may be unauthenticated or session expired. Principal: ${SecurityContextHolder?.context?.authentication?.principal}")
            log.error(e)
            throw e
        }
    }


    public static userCanExecuteQuery() {
        try {
            def usermap = getCommunicationUserRoleMap()
            if (usermap.canExecuteQuery)
                return true;

            return false;
        } catch (Exception e) {
            log.error("principal lacks authorities - may be unauthenticated or session expired. Principal: ${SecurityContextHolder?.context?.authentication?.principal}")
            log.error(e)
            throw e
        }
    }


    public static setLocaleInDatabase(sql) {

        def userlocale
        try {
            userlocale = LocaleContextHolder?.getLocale()?.toString()?.replaceAll('-','_')
            sql.call("""{call g\$_nls_utility.p_set_nls(${userlocale})}""")
        } catch (Exception e) {
            //We eat this exception and let it just  default to the default language of the database.
            //this avoids dependency on general 8.7.5 which had the utility function called above
            log.debug "There was an exception while setting nls for locale ${userlocale}:" + e.getMessage()
        }

    }

}
