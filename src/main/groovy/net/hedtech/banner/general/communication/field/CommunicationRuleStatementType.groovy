/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.field
/**
 * An enumeration of rule statement types.
 *
 * @author Ed Delaney
 */
public enum CommunicationRuleStatementType {

    SQL_PREPARED_STATEMENT,
    SQL_CALLABLE_STATEMENT,
    GROOVY_STATEMENT;


    public Set<CommunicationRuleStatementType> set() {
        return EnumSet.range(CommunicationRuleStatementType.SQL_PREPARED_STATEMENT, CommunicationRuleStatementType.GROOVY_STATEMENT);
    }

}
