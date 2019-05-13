/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.field

/**
 * Valid states of a Communication Field.
 * The string representations of this enumeration are used as persistent values in the database, and may not
 * be changed without providing appropriate migrations.
 *
 */
public enum CommunicationFieldStatus {
    DEVELOPMENT,
    PRODUCTION,
    DEPRECATED;

    public static Set<CommunicationFieldStatus> set() {
        return EnumSet.range( CommunicationFieldStatus.DEVELOPMENT, CommunicationFieldStatus.DEPRECATED );
    }

}

