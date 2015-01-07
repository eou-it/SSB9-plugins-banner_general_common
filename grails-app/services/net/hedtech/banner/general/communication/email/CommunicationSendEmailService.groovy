/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.email

import net.hedtech.banner.general.communication.merge.CommunicationRecipientData
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/**
 * Email Service provides low level email send capability.
 */
class CommunicationSendEmailService {
    private Log log = LogFactory.getLog( this.getClass() )

    public void sendEmail( CommunicationOrganization organization, CommunicationEmailMessage emailMessage, CommunicationRecipientData recipientData, Long pidm  ) {

    }

}
