package net.hedtech.banner.general.communication.letter

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import net.hedtech.banner.general.communication.exceptions.CommunicationExceptionFactory
import org.apache.log4j.Logger

/**
 * Formats a query for storage or execution.
 */
class CommunicationLetterPageSettings {
    def log = Logger.getLogger(this.getClass())
    private Map queryMap = [ unitOfMeasure: "${CommunicationLetterUnitOfMeasure.INCH.toString()}", topMargin: "1.0", leftMargin: "1.0", bottomMargin: "1.0", rightMargin: "1.0", pageSize: "${CommunicationLetterPageSize.LETTER.toString()}" ]

    public String toJson() {
        return JsonOutput.toJson( queryMap )
    }

    public setStyle( String style ) {
        Map parsedMap = null

        if (style != null && style.trim().length() != 0) {
            JsonSlurper jsonSlurper = new JsonSlurper()
            try {
                parsedMap = jsonSlurper.parseText( style )
            } catch (groovy.json.JsonException e) {
                throw CommunicationExceptionFactory.createApplicationException( CommunicationLetterPageSettings.class, "badSyntax" )
            }
        }

        setUnitOfMeasure( parsedMap?.unitOfMeasure )
        setPageSize( parsedMap?.pageSize )
        setTopMargin( parsedMap?.topMargin )
        setLeftMargin( parsedMap?.leftMargin )
        setBottomMargin( parsedMap?.bottomMargin )
        setRightMargin( parsedMap?.rightMargin )
    }

    public Map toMap() {
        return [ unitOfMeasure: getUnitOfMeasure(), topMargin: getTopMargin(), leftMargin: getLeftMargin(), bottomMargin: getBottomMargin(), rightMargin: getRightMargin(), pageSize: getPageSize() ]
    }

    public String toString() {
        return toJson()
    }

    public String getUnitOfMeasure() {
        return queryMap.unitOfMeasure
    }

    public void setUnitOfMeasure( String s ) {
        queryMap.unitOfMeasure = (s == null) ? CommunicationLetterUnitOfMeasure.INCH.toString() : s
    }

    public String getPageSize() {
        return queryMap.pageSize
    }

    public void setPageSize( String s ) {
        queryMap.pageSize = (s == null) ? CommunicationLetterPageSize.LETTER.toString() : s
    }

    public String getTopMargin() {
        return queryMap.topMargin
    }

    public void setTopMargin( String s ) {
        queryMap.topMargin = (s == null) ? "1.0" : s
    }

    public String getLeftMargin() {
        return queryMap.leftMargin
    }

    public void setLeftMargin( String s ) {
        queryMap.leftMargin = (s == null) ? "1.0" : s
    }

    public String getBottomMargin() {
        return queryMap.bottomMargin
    }

    public void setBottomMargin( String s ) {
        queryMap.bottomMargin = (s == null) ? "1.0" : s
    }

    public String getRightMargin() {
        return queryMap.rightMargin
    }

    public void setRightMargin( String s ) {
        queryMap.rightMargin = (s == null) ? "1.0" : s
    }

    public void validate() {
        validateNotMissing( getUnitOfMeasure(), "missingUnitOfMeasure" )
        validateNotMissing( getTopMargin(), "missingTopMargin" )
        validateNotMissing( getLeftMargin(), "missingLeftMargin" )
        validateNotMissing( getBottomMargin(), "missingBottomMargin" )
        validateNotMissing( getRightMargin(), "missingRightMargin" )
        validateNotMissing( getPageSize(), "missingPageSize" )

        try {
            CommunicationLetterPageSize.valueOf( getPageSize() )
        } catch (Exception e) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationLetterPageSettings.class, "invalidPageSize" )
        }

        try {
            CommunicationLetterUnitOfMeasure.valueOf( getUnitOfMeasure() )
        } catch (Exception e) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationLetterPageSettings.class, "invalidUnitOfMeasure" )
        }
    }

    private void validateNotMissing( String keyValue, String missingResourceId ) {
        if (keyValue == null || keyValue.trim().length() == 0) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationLetterPageSettings.class, missingResourceId )
        }
    }
}
