/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.email;

import groovy.transform.EqualsAndHashCode;
import groovy.transform.ToString;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Represents an Email Address entity.
 */
@SuppressWarnings("serial")
@EqualsAndHashCode
@ToString
public class CommunicationEmailAddress implements Serializable {

    /**
     * The actual email address
     */
    private String mailAddress;

    /**
     * the display name for the email address
     */
    private String displayName;


    /**
     * Returns the display name for the email address
     * @return the displayName for the email address
     */
    public String getDisplayName() {
        return displayName;
    }


    /**
     * Sets the display name for the address
     * @param displayName the displayName to set
     */
    public void setDisplayName( String displayName ) {
        this.displayName = displayName;
    }


    /**
     * Returns the actual email address
     * @return the mailAddress
     */
    public String getMailAddress() {
        return mailAddress;
    }


    /**
     * Sets the mailAddress to set.
     * @param mailAddress the mailAddress to set
     * @throws AddressException if the address passed does not confirm to RFC822
     */
    public void setMailAddress( String mailAddress ) throws AddressException {
        if (null == mailAddress) {
            this.mailAddress = null;
            return;
        }
        if (isValidEmailAddress( mailAddress ) == true) {
            this.mailAddress = mailAddress;
        } else {
            throw new AddressException( "RFC 822 address format violation.", mailAddress );
        }
    }


    /**
     * Checks for the well-formedness of an email address.
     * @param emailAddress the address to validate
     * @return true if valid and false otherwise
     */
    public static boolean isValidEmailAddress( String emailAddress ) {
        if (null == emailAddress || 0 == emailAddress.trim().length()) {
            return false;
        }

        boolean isValid = true;
        try {
            new InternetAddress( emailAddress, true );
        } catch (AddressException e) {
            isValid = false;
        }
        return isValid;
    }


    /**
     * Checks for the well-formedness of an array of email addresses.
     * @param emailAddresses the email addresses to validate
     * @return the email addresses which are not well-formed
     */
    public static String[] isValidEmailAddresses( String[] emailAddresses ) {
        ArrayList<String> invalidEmailAddresses = new ArrayList<String>();
        for (String emailAddress : emailAddresses) {
            if (false == isValidEmailAddress( emailAddress )) {
                invalidEmailAddresses.add( emailAddress );
            }
        }

        String[] invalidEmailAddressesArray = null;
        if (invalidEmailAddresses.size() > 0) {
            invalidEmailAddressesArray = invalidEmailAddresses.toArray( new String[0] );
        }

        return invalidEmailAddressesArray;
    }


}
