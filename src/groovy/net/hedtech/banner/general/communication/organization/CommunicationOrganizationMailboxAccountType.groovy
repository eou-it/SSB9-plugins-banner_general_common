/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.organization

/**
 * Defines the type of MailboxAccount.
 * MailboxAccount instances must be of a specific type; the type tells the monitoring system
 * how to treat email received by the account.  (For example, parse as bounce-backs, or parse as normal replies.)
 *
 * Note that the string representations of this enum are persisted in the database.  Do not change the types unless
 * you also handle migration of existing data.
 */
public enum CommunicationOrganizationMailboxAccountType {
    Sender,
    ReplyTo;
}
