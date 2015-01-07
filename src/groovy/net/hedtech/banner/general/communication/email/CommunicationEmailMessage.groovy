/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.email;

import groovy.transform.EqualsAndHashCode;
import groovy.transform.ToString;
import org.hibernate.annotations.Type;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents an Email Message entity with placeholders for attributes of a typical email message.
 */
@SuppressWarnings("serial")
@EqualsAndHashCode
@ToString
public class CommunicationEmailMessage implements Cloneable, Serializable {

    /**
     * Unique identifier for the email message.
     */
    private String guid;

    /**
     * place holder for RFC 822 "From" header field of an email message.
     */
    private Set<CommunicationEmailAddress> senders;

    /**
     * place holder for RFC 822 "Reply-To" header field attribute of an email message addresses to which replies should
     * be directed.
     */
    private Set<CommunicationEmailAddress> replyTo;

    /**
     * place holder for "to" attribute of an email message.
     */
    private Set<CommunicationEmailAddress> toList;

    /**
     * place holder for "cc" attribute of an email message.
     */
    private Set<CommunicationEmailAddress> ccList;

    /**
     * place holder for "bcc" attribute of an email message.
     */
    private Set<CommunicationEmailAddress> bccList;

    /**
     * place holder for the sent date of this email message.
     */
//    @Type(type = "com.sungardhe.utils.TimestampInDatabaseDateHibernateType")
    private Date dateSent;

    /**
     * place holder for the subject of this email message.
     */
    private String subjectLine;

    /**
     * place holder for the content of the email message.
     */
    private String messageBody;

    /**
     * content type of the message body, html or plain text.
     */
    private String messageBodyContentType;

    /**
     * messageId present in the header of the Message.
     */
    private String messageId;

    /**
     * parentMessageId is the in-Reply-To in the header of the message.
     */
    private String parentMessageId;

    /**
     * status of the EmailMessage.
     */
    private String status;



    /**
     * Returns the bcc attribute value for the email message as a set of email addresses.
     * @return Set<EmailAddress> email address in the bcc list
     */
    public Set<CommunicationEmailAddress> getBccList() {
        return bccList;
    }


    /**
     * Sets the bcc attribute value as a set of email addresses.
     * @param bccList the bcc list to set
     */
    public void setBccList( Set<CommunicationEmailAddress> bccList ) {
        this.bccList = bccList;
    }


    /**
     * Returns the cc attribute value for the email message as a set of email addresses.
     * @return Set<CommunicationEmailMessage> email addresses in the cc list
     */
    public Set<CommunicationEmailAddress> getCcList() {
        return ccList;
    }


    /**
     * Sets the cc attribute value as a set of email addresses.
     * @param ccList the cc list to set
     */
    public void setCcList( Set<CommunicationEmailAddress> ccList ) {
        this.ccList = ccList;
    }


    /**
     * Returns the date sent attribute's value.
     * @return Date the sent date value
     */
    public Date getDateSent() {
        return dateSent;
    }


    /**
     * Set the sent date attribute.
     * @param dateSent the value to set
     */
    public void setDateSent( Date dateSent ) {
        this.dateSent = dateSent;
    }


    /**
     * Returns the message body or content of the email message.
     * @return the content of the emai message
     */
    public String getMessageBody() {
        return messageBody;
    }


    /**
     * Sets the email message body/content.
     * @param messageBody the value to set
     */
    public void setMessageBody( String messageBody ) {
        this.messageBody = messageBody;
    }


    /**
     * Returns the mime type of the message body.
     * @return mime type of the message body
     */
    public String getMessageBodyContentType() {
        return messageBodyContentType;
    }


    /**
     * Sets the mime type for the message content.
     * @param messageBodyContentType mime type to be set
     */
    public void setMessageBodyContentType( String messageBodyContentType ) {
        this.messageBodyContentType = messageBodyContentType;
    }


    /**
     * Returns the subject of the email message.
     * @return the subject of the email message
     */
    public String getSubjectLine() {
        return subjectLine;
    }


    /**
     * Sets the subject of the email message.
     * @param subjectLine the value to set
     */
    public void setSubjectLine( String subjectLine ) {
        this.subjectLine = subjectLine;
    }


    /**
     * Returns the "to" attribute values as a set of email addresses.
     * @return Set<CommunicationEmailAddress> contents of the "to" attribute
     */
    public Set<CommunicationEmailAddress> getToList() {
        return toList;
    }


    /**
     * Sets the "to" attribute of the email message.
     * @param toList the values to set
     */
    public void setToList( Set<CommunicationEmailAddress> toList ) {
        this.toList = toList;
    }


    /**
     * Returns the "reply-to" attribute values as a set of email addresses.
     * @return Set<CommunicationEmailMessage> contents of the "reply-to" attribute
     */
    public Set<CommunicationEmailAddress> getReplyTo() {
        return replyTo;
    }


    /**
     * Sets the values for "reply-to" attribute as a set of email addresses.
     * @param replyTo the values to set
     */
    public void setReplyTo( Set<CommunicationEmailAddress> replyTo ) {
        this.replyTo = replyTo;
    }


    /**
     * Returns the values for "from" attribute.
     * @return Set<CommunicationEmailMessage> values for "from" attribute
     */
    public Set<CommunicationEmailAddress> getSenders() {
        return senders;
    }


    /**
     * Sets the "from" attribute of the email message.
     * @param senders the values to set
     */
    public void setSenders( Set<CommunicationEmailAddress> senders ) {
        this.senders = senders;
    }


    /**
     * Returns the unique identifier for the email message.
     * @return the guid the unique identifier for the email message
     */
    public String getGuid() {
        return guid;
    }


    /**
     * Sets the unique identifier for the email message.
     * @param guid the guid to set
     */
    public void setGuid( String guid ) {
        this.guid = guid;
    }


    /**
     * @return Returns the messageId.
     */
    public String getMessageId() {
        return messageId;
    }


    /**
     * @param messageId The messageId to set.
     */
    public void setMessageId( String messageId ) {
        this.messageId = messageId;
    }


    /**
     * @return Returns the parentMessageId.
     */
    public String getParentMessageId() {
        return parentMessageId;
    }


    /**
     * @param parentMessageId The parentMessageId to set.
     */
    public void setParentMessageId( String parentMessageId ) {
        this.parentMessageId = parentMessageId;
    }


    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }


    /**
     * @param status the status to set
     */
    public void setStatus( String status ) {
        this.status = status;
    }


    /*
     * (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        CommunicationEmailMessage newMessage = new CommunicationEmailMessage();
        if (null != senders) {
            newMessage.setSenders( new HashSet<CommunicationEmailAddress>( senders ) );
        }

        if (null != replyTo) {
            newMessage.setReplyTo( new HashSet<CommunicationEmailAddress>( replyTo ) );
        }

        if (null != toList) {
            newMessage.setToList( new HashSet<CommunicationEmailAddress>( toList ) );
        }

        if (null != ccList) {
            newMessage.setCcList( new HashSet<CommunicationEmailAddress>( ccList ) );
        }

        if (null != bccList) {
            newMessage.setBccList( new HashSet<CommunicationEmailAddress>( bccList ) );
        }

        newMessage.setDateSent( dateSent );
        newMessage.setSubjectLine( subjectLine );
        newMessage.setMessageBody( messageBody );
        newMessage.setMessageBodyContentType( messageBodyContentType );
        if (null != messageId) {
            newMessage.setMessageId( messageId );
        }
        if (null != parentMessageId) {
            newMessage.setParentMessageId( parentMessageId );
        }
        newMessage.setStatus( status );
        return newMessage;
    }



}
