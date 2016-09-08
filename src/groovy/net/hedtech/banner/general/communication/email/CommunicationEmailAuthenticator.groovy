/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.email

import net.hedtech.banner.general.communication.organization.CommunicationMailboxAccount
import javax.mail.Authenticator
import javax.mail.PasswordAuthentication

/**
 * A class that is used to authenticate an email account.
 */
class CommunicationEmailAuthenticator extends Authenticator {

    /**
     * Username of the account to be used for connecting to the Email Server.
     */
    private String username;

    /**
     * Password of the account to be used for connecting to the Email Server.
     */
    private String password;


    /**
     * Default Constructor for an email authenticator.
     */
    public CommunicationEmailAuthenticator() {
    }

    public CommunicationEmailAuthenticator( String accountUserName, String accountPassword ) {
        this.username = accountUserName
        this.password = accountPassword
    }


    /**
     * Returns a data holder that is used by Authenticator.
     *
     * @return PasswordAuthentication the password authentication
     */
    @Override
    public PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication( username, password );
    }
}
