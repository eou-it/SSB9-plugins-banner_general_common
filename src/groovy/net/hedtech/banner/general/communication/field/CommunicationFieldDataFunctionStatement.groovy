/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.field

import javax.persistence.*

/**
 * An entity representing a RuleStatement.
 *
 * @author Charlie Hardt
 * @author Brian Bell
 */
@SuppressWarnings("serial")
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


    @Override
    public String toString() {
        return "RuleStatement{" +
                "type=" + type +
                ", arrayOutputFlag='" + arrayOutputFlag + '\'' +
                ", value='" + value + '\'' +
                '}';
    }


}
