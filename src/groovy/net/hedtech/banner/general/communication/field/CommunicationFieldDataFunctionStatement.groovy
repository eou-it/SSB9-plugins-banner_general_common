/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.field

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import javax.persistence.*

@SuppressWarnings("serial")
@EqualsAndHashCode
@ToString
@Embeddable
public class CommunicationFieldDataFunctionStatement implements Serializable {

    /**
     * The type of the rule statement.
     */
    @Enumerated(EnumType.STRING)
    CommunicationRuleStatementType type;

    /**
     * Indicates whether the rule statement returns array output arguments
     * (i.e., returns multiple rows of recurring data). Defaults to false.
     */
    // NOTE: we have to use a String instead of a boolean here because this is
    // an optional embedded object
    String arrayOutputFlag = Boolean.FALSE.toString();

    /**
     * The value of the rule statement.
     */
    @Lob
    @Column(name = "CONTENT")
    String value;

}
