/*******************************************************************************
 Copyright 2016-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.letter

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import net.hedtech.banner.general.communication.exceptions.CommunicationExceptionFactory
import org.apache.log4j.Logger

/**
 * Formats a query for storage or execution.
 */
class CommunicationLetterPageSettings {

    private static final log = Logger.getLogger(CommunicationLetterPageSettings.class)
    private Map queryMap = createDefaultQueryMap()
    private final float minimumMarginInches = 0
    private final float maximumMarginInches = 22
    private final float minimumMarginMillimeters = 0
    private final float maximumMarginMillimeters = 600

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
        } else {
            parsedMap = createDefaultQueryMap()
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
        queryMap.topMargin = s
    }

    public String getLeftMargin() {
        return queryMap.leftMargin
    }

    public void setLeftMargin( String s ) {
        queryMap.leftMargin = s
    }

    public String getBottomMargin() {
        return queryMap.bottomMargin
    }

    public void setBottomMargin( String s ) {
        queryMap.bottomMargin = s
    }

    public String getRightMargin() {
        return queryMap.rightMargin
    }

    public void setRightMargin( String s ) {
        queryMap.rightMargin = s
    }

    public void validate() {
        validateNotMissing( getUnitOfMeasure(), "missingUnitOfMeasure" )

        CommunicationLetterUnitOfMeasure unitOfMeasure = null
        try {
            unitOfMeasure = CommunicationLetterUnitOfMeasure.valueOf( getUnitOfMeasure() )
        } catch (Throwable t) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationLetterPageSettings.class, "invalidUnitOfMeasure" )
        }
        if (unitOfMeasure == null) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationLetterPageSettings.class, "invalidUnitOfMeasure" )
        }

        validateNotMissing( getPageSize(), "missingPageSize" )

        try {
            CommunicationLetterPageSize.valueOf( getPageSize() )
        } catch (Exception e) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationLetterPageSettings.class, "invalidPageSize" )
        }

        validateNotMissing( getTopMargin(), "missingTopMargin" )
        validateMargin( unitOfMeasure, getTopMargin(), "invalidTopMargin" )

        validateNotMissing( getLeftMargin(), "missingLeftMargin" )
        validateMargin( unitOfMeasure, getLeftMargin(), "invalidLeftMargin" )

        validateNotMissing( getBottomMargin(), "missingBottomMargin" )
        validateMargin( unitOfMeasure, getBottomMargin(), "invalidBottomMargin" )

        validateNotMissing( getRightMargin(), "missingRightMargin" )
        validateMargin( unitOfMeasure, getRightMargin(), "invalidRightMargin" )
    }

    private void validateNotMissing( String keyValue, String missingResourceId ) {
        if (keyValue == null || keyValue.trim().length() == 0) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationLetterPageSettings.class, missingResourceId )
        }
    }

    private void validateMargin( CommunicationLetterUnitOfMeasure unitOfMeasure, String value, String invalidResourceId ) {
        try {
            float margin = Float.parseFloat( value )

            if (unitOfMeasure == CommunicationLetterUnitOfMeasure.INCH) {
                if ((margin < this.minimumMarginInches) || (margin > this.maximumMarginInches)) {
                    throw CommunicationExceptionFactory.createApplicationException( CommunicationLetterPageSettings.class, invalidResourceId )
                }
            } else if (unitOfMeasure == CommunicationLetterUnitOfMeasure.MILLIMETER) {
                if ((margin < this.minimumMarginMillimeters) || (margin > this.maximumMarginMillimeters)) {
                    throw CommunicationExceptionFactory.createApplicationException( CommunicationLetterPageSettings.class, invalidResourceId )
                }
            } else {
                throw CommunicationExceptionFactory.createApplicationException( CommunicationLetterPageSettings.class, "invalidUnitOfMeasure" )
            }
        } catch (NumberFormatException e ) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationLetterPageSettings.class, invalidResourceId )
        }
    }

    private static LinkedHashMap<String, String> createDefaultQueryMap() {
        return [unitOfMeasure: "${CommunicationLetterUnitOfMeasure.INCH.toString()}", topMargin: "1.0", leftMargin: "1.0", bottomMargin: "1.0", rightMargin: "1.0", pageSize: "${CommunicationLetterPageSize.LETTER.toString()}"]
    }
}
