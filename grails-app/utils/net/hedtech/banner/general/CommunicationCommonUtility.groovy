/*********************************************************************************
 Copyright 2010-2015 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general

import grails.util.Holders
import org.apache.commons.lang.StringUtils
import org.apache.log4j.Logger
import org.springframework.security.core.context.SecurityContextHolder

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * This is a utilty class for Communication Management
 *
 */
class CommunicationCommonUtility {

    static def log = Logger.getLogger('net.hedtech.banner.general.communication.CommunicationCommonUtility')

    //TODO enhance these regex to make it more comprehensive

    static
    def Pattern sqlpattern = Pattern.compile("\\*|(;)|(\\b(ALTER|CREATE|DATABASE|DELETE|DROP|EXEC(UTE){0,1}|INSERT( +INTO){0,1}|MERGE|UPDATE)\\b)")
    static def Pattern multipattern = Pattern.compile("SELECT(.*?)FROM");
    static
    def Pattern scrubPattern = Pattern.compile("[,.;!()/:={}<>\\p{Z}\\|\\\"\\\'\\`\\-\\+\\~\\[\\]\\@\\#\\&\\^\\\$\\\\]")
    static def wildcardChar = "%"

    /**
     * Checks if a user input sqlstring has disallowed reserved words
     * @param sqlstring
     * @return true if disallowed words are found false otherwise
     */
    public static Boolean sqlStatementNotAllowed(String sqlstring, Boolean multiSelectColumnAllowed = true) {

        //replace disallowed words with this for testing
        def stringToReplace = "**ZZZZ**"

        //if the input string is null then return false
        if (sqlstring == null || sqlstring == "")
            return false;

        log.info("The sql string before pattern matching - " + sqlstring)

        //Apply the compiled pattern to the input string
        def sqlToMatch = sqlpattern.matcher(sqlstring.toUpperCase()).replaceAll(stringToReplace)

        log.info("The sql after pattern matching - " + sqlToMatch)

        //make sure the sql statement only selects one value if requested in the params
        //TODO Review this. should we test for pidm and commma for the whole sql including union and subqueries or just the first.

        if (!multiSelectColumnAllowed) {
            def z = 0
            Matcher matcher = multipattern.matcher(sqlstring.toUpperCase())
            while (matcher.find() && z == 0) {
                if (matcher.group(1).contains(",")) {
                    return true
                }
                if (!matcher.group(1).contains("PIDM")) {
                    return true
                }
                z = 1
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

        def scrubbedInput = scrubPattern.matcher(userinput).replaceAll("").toLowerCase()
        if (scrubbedInput == null || scrubbedInput == "") {
            scrubbedInput = wildcardChar
        } else {
            scrubbedInput = StringUtils.replaceEach(scrubbedInput, fromstring, tostring)
            scrubbedInput = wildcardChar + scrubbedInput + wildcardChar
        }
        return scrubbedInput;
    }


    public static getCommunicationUserRoleMap() {
        def map = [:]
        def isUser = false
        def isAuthor = false
        def isAdmin = false
        def canExecuteQuery = false

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
            if (authorities.any { it.objectName == "CMQUERYEXECUTE" }) {
                canExecuteQuery = true
            }

            map.put("isUser", isUser)
            map.put("isAuthor", isAuthor)
            map.put("isAdmin", isAdmin)
            map.put("canExecuteQuery", canExecuteQuery)

//get the oracle userid associated with the login banner id.  If there is a gobeacc record, that oracle user will be
//returned. If no gobeacc record then the username associated with the bannerSsbDataSource will be returned
            map.put("userId", (SecurityContextHolder?.context?.authentication?.principal?.getOracleUserName() ?: Holders.config?.bannerSsbDataSource?.username)?.toUpperCase())

            return map
        } catch (Exception it) {
            log.error("principal lacks authorities - may be unauthenticated or session expired. Principal: ${SecurityContextHolder?.context?.authentication?.principal}")
            log.error(it)
            throw it
        }
    }


    public static userCanCreate() {
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


    public static userCanUpdateDelete(String createdBy) {
        try {
            def usermap = getCommunicationUserRoleMap()
            if (usermap.isAdmin)
                return true;
            else if (usermap.isAuthor && (usermap.userId).equals(createdBy))
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

}