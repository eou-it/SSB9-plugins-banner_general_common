/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.organization

import net.hedtech.banner.general.communication.template.util.CommunicationExcludeFromValidation

import javax.persistence.Embeddable
import javax.persistence.ManyToOne


@Embeddable
public class CommunicationOrganizationEmailContact implements Serializable {


    private String defaultFromAddress;
    private String defaultDisplayName;

    @CommunicationExcludeFromValidation
    //we don't want to validate the fields of the MailboxAccount, as this is just a reference
    @ManyToOne
    private CommunicationMailboxAccount sender;
    @CommunicationExcludeFromValidation
    //we don't want to validate the fields of the MailboxAccount, as this is just a reference
    @ManyToOne
    private CommunicationMailboxAccount replyTo;

    /**
     * Returns the default from email address.
     * @return
     */
    public String getDefaultFromAddress() {
        return defaultFromAddress;
    }

    /**
     * Sets the default from email address.
     * @param defaultFromAddress
     */
    public void setDefaultFromAddress(String defaultFromAddress) {
        this.defaultFromAddress = defaultFromAddress;
    }

    /**
     * Returns the defaultDisplayName attribute value.
     * @return the defaultDisplayName
     */
    public String getDefaultDisplayName() {
        return defaultDisplayName;
    }

    /**
     * Sets the defaultDisplayName attribute.
     * @param defaultDisplayName the defaultDisplayName to set
     */
    public void setDefaultDisplayName(String defaultDisplayName) {
        this.defaultDisplayName = defaultDisplayName;
    }

    /**
     * Returns the MailboxAccount used for sending email
     * @return
     */
    public CommunicationMailboxAccount getSender() {
        return sender;
    }

    /**
     * Sets the MailboxAccount used for sending email
     * @param sender
     */
    public void setSender(CommunicationMailboxAccount sender) {
        this.sender = sender;
    }

    /**
     * Returns the MailboxAccount used for monitoring replies.
     * @return
     */
    public CommunicationMailboxAccount getReplyTo() {
        return replyTo;
    }

    /**
     * Sets the MailboxAccount used for monitoring replies.
     * @param replyTo
     */
    public void setReplyTo(CommunicationMailboxAccount replyTo) {
        this.replyTo = replyTo;
    }

}
