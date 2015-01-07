package net.hedtech.banner.general.communication.email

import net.hedtech.banner.exceptions.ApplicationException

import javax.mail.MessagingException

/**
 * Taken largely as is from BRM equivalent.
 *
 * MessagingRuntimeException is a bridge between the existing communication log services that expect
 * to get a non localized system exception from the email server to client called services which
 * expect i18n capable application exceptions.
 *
 * Please note that this is an extension of SystemException (and thus RuntimeException) and the
 * compiler will not enforce that this is caught.
 *
 * @author Michael Brzycki
 */
class CommunicationMessagingSystemException extends ApplicationException {
    private static final long serialVersionUID = 1;
    private MessagingException messagingException;
    private String fromList;
    private String recipientList;
    private String replyToList;

    public CommunicationMessagingSystemException(MessagingException e, String fromList, String recipientList, String replyToList) {
        super("EmailServer.SendEmailMethod.execute", e);
        messagingException = e;
        this.fromList = fromList;
        this.recipientList = recipientList;
        this.replyToList = replyToList;
    }

    public MessagingException getMessagingException() {
        return messagingException;
    }
}
