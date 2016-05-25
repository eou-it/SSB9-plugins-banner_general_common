package net.hedtech.banner.general.communication.population.query

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import net.hedtech.banner.general.communication.exceptions.CommunicationExceptionFactory
import org.apache.commons.collections.functors.ExceptionFactory
import org.apache.log4j.Logger


/**
 * Formats a query for storage or execution.
 */
class CommunicationPopulationQueryExtractStatement {
    def log = Logger.getLogger(this.getClass())
    private Map queryMap = [ application: "", selection: "", creatorId: "", userId: "" ]

    public getQueryString() {
        return JsonOutput.toJson( queryMap )
    }

    public setQueryString( String queryString ) {
        Map parsedMap = null

        if (queryString != null && queryString.trim().length() != 0) {
            JsonSlurper jsonSlurper = new JsonSlurper()
            parsedMap = jsonSlurper.parseText( queryString )
        }

        setApplication( parsedMap?.application )
        setSelection( parsedMap?.selection )
        setCreatorId( parsedMap?.creatorId )
        setUserId( parsedMap?.userId )
    }

    public Map toMap() {
        return [ application: getApplication(), selection: getSelection(), creatorId: getCreatorId(), userId: getUserId() ]
    }

    public String toString() {
        return getQueryString()
    }

    public String getApplication() {
        return queryMap.application
    }

    public void setApplication( String s ) {
        queryMap.application = (s == null) ? "" : s
    }

    public String getSelection() {
        return queryMap.selection
    }

    public void setSelection( String s ) {
        queryMap.selection = (s == null) ? "" : s
    }

    public String getCreatorId() {
        return queryMap.creatorId
    }

    public void setCreatorId( String s ) {
        queryMap.creatorId = (s == null) ? "" : s
    }

    public String getUserId() {
        return queryMap.userId
    }

    public void setUserId( String s ) {
        queryMap.userId = (s == null) ? "" : s
    }

    public void validate() {
        validateKeyValue( getApplication(), "emptyApplication", "applicationTooLong" )
        validateKeyValue( getSelection(), "emptySelection", "selectionTooLong" )
        validateKeyValue( getCreatorId(), "emptyCreatorId", "creatorIdTooLong" )
        validateKeyValue( getUserId(), "emptyUserId", "userIdTooLong" )
    }

    private void validateKeyValue( String keyValue, String emptyResourceId, String tooLongResourceId) {
        if (keyValue == null || keyValue.trim().length() == 0) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationPopulationQueryExtractStatement.class, emptyResourceId )
        }
        if (keyValue.length() > 30) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationPopulationQueryExtractStatement.class, tooLongResourceId )
        }
    }
}
