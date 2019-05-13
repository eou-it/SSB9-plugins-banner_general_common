/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.field

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * RuleResult.
 */
@EqualsAndHashCode
@ToString
class CommunicationFieldDataFunctionResult implements Serializable {

    String name;
    Serializable value;

    static constraints = {
        name(nullable: false)
        value(nullable: false)
    }
}
