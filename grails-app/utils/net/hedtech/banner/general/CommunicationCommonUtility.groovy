/*********************************************************************************
 Copyright 2010-2015 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general

import org.apache.commons.lang.StringUtils
import org.apache.log4j.Logger
import java.util.regex.Pattern

import org.apache.commons.lang.StringUtils.*

/**
 * This is a utilty class for Communication Management
 *
 */
class CommunicationCommonUtility {

    static def log = Logger.getLogger('net.hedtech.banner.general.communication.CommunicationCommonUtility')

    //TODO enhance these regex to make it more comprehensive

    static def Pattern sqlpattern = Pattern.compile("(;)|(\\b(ALTER|CREATE|DATABASE|DELETE|DROP|EXEC(UTE){0,1}|INSERT( +INTO){0,1}|MERGE|UPDATE)\\b)")
    static def Pattern scrubPattern = Pattern.compile("[,.;!()/:={}<>\\p{Z}\\|\\\"\\\'\\`\\-\\+\\~\\[\\]\\@\\#\\&\\^\\\$\\\\]")
    static def wildcardChar = "%"

    /**
     * Checks if a user input sqlstring has disallowed reserved words
     * @param sqlstring
     * @return true if disallowed words are found false otherwise
     */
    public static Boolean validateSqlStatementForInjection(String sqlstring) {

        //replace disallowed words with this for testing
        def stringToReplace = "**ZZZZ**"

        //if the input string is null then return false
        if (sqlstring == null || sqlstring == "")
            return false;

        log.info("The sql string before pattern matching - "+sqlstring)

        //Apply the compiled pattern to the input string
        def sqlToMatch = sqlpattern.matcher(sqlstring.toUpperCase()).replaceAll(stringToReplace)

        log.info("The sql after pattern matching - " + sqlToMatch)

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
            return wildcardChar
        }
        def String[] fromstring = [ "*","?" ]
        def String[] tostring = ["%","_"]

        def scrubbedInput = scrubPattern.matcher(userinput).replaceAll("").toLowerCase()
        if (scrubbedInput == null || scrubbedInput == "") {
            scrubbedInput = wildcardChar
        } else {
            scrubbedInput = StringUtils.replaceEach(scrubbedInput, fromstring, tostring)
            scrubbedInput = wildcardChar + scrubbedInput + wildcardChar
        }
        return scrubbedInput;
    }
}