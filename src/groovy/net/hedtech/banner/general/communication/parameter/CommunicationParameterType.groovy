/*********************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.general.communication.parameter

import net.hedtech.banner.general.communication.CommunicationEnum

/**
 * An enumeration of types supported for communication parameters.
 * These values can never change.
 */
enum CommunicationParameterType implements CommunicationEnum {

    TEXT,
    NUMBER,
    DATE;

}
