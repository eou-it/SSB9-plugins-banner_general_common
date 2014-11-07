/*********************************************************************************
 Copyright 2010-2015 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general
/**
 * This is a utilty class for Communication Management
 *
 */
class CommunicationCommonUtility {

    /**
     * Checks if a user input sqlstring has disallowed reserved words
     * @param sqlstring
     * @return true if disallowed words are found false otherwise
     */
    public static Boolean validateSqlStatementForInjection(String sqlstring) {

        if (sqlstring == null || sqlstring == "")
            return false;
//TODO enhance this to use regex to capture more reserved words like ALTER, DATABASE etc

        def disallowedQueryTypes = /(^insert|^update|^delete)/
        def sqlToMatch = sqlstring.toLowerCase()

        if (sqlToMatch =~ disallowedQueryTypes) {
            return true
        } else
            return false

    }


}