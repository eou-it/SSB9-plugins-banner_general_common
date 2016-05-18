/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.general.communication.organization

/**
 * Service for providing basic crud services on
 * Communication Organization domain objects while also
 * handling dependent mailbox accounts and email server properties.
 */
class CommunicationOrganizationCompositeService {
    CommunicationOrganizationService communicationOrganizationService
    CommunicationEmailServerPropertiesService communicationEmailServerPropertiesService
    CommunicationMailboxAccountService communicationMailboxAccountService

    def listOrganizations( args ) {
        return communicationOrganizationService.list( args )
    }

    def getOrganization( id ) {
        return communicationOrganizationService.get( id )
    }

    CommunicationOrganization createOrganization( CommunicationOrganization organization ) {
        createDependentMailboxAccountAndEmailServerProperties( organization )
        return communicationOrganizationService.create( organization )
    }

    CommunicationOrganization updateOrganization( CommunicationOrganization newOrganization ) {
        assert newOrganization != null
        assert newOrganization.id != null
        assert newOrganization.version != null

        CommunicationOrganization oldOrganization = CommunicationOrganization.fetchById( newOrganization.id )
        createDependentMailboxAccountAndEmailServerProperties( newOrganization, oldOrganization )
        removeDependentMailboxAccountsAndEmailServerProperties( oldOrganization )

        // Default the mobile application key to its previous value if mobile settings present during an
        // update but no clear password passed in.
        if ((newOrganization.mobileApplicationName || newOrganization.mobileEndPointUrl) &&
            !newOrganization?.clearMobileApplicationKey &&
            oldOrganization?.encryptedMobileApplicationKey) {

            newOrganization.encryptedMobileApplicationKey  = oldOrganization.encryptedMobileApplicationKey
        }

        newOrganization = communicationOrganizationService.update( newOrganization )

        return newOrganization;
    }

    void deleteOrganization( Map organizationAsMap ) {
        CommunicationOrganization organization = this.getOrganization( organizationAsMap.id )
        communicationOrganizationService.delete( organizationAsMap )
        removeDependentMailboxAccountsAndEmailServerProperties( organization )
    }

    private void createDependentMailboxAccountAndEmailServerProperties( CommunicationOrganization newOrganization, CommunicationOrganization oldOrganization = null) {
        if (newOrganization.senderMailboxAccount) {
            newOrganization.senderMailboxAccount.type = CommunicationMailboxAccountType.Sender
            if (!newOrganization.senderMailboxAccount.clearTextPassword && oldOrganization?.senderMailboxAccount) {
                newOrganization.senderMailboxAccount.encryptedPassword = oldOrganization.senderMailboxAccount.encryptedPassword
            }
            newOrganization.senderMailboxAccount = communicationMailboxAccountService.create(newOrganization.senderMailboxAccount)
        }

        if (newOrganization.replyToMailboxAccount) {
            newOrganization.replyToMailboxAccount.type = CommunicationMailboxAccountType.ReplyTo
            if (!newOrganization.replyToMailboxAccount.clearTextPassword && oldOrganization?.replyToMailboxAccount) {
                newOrganization.replyToMailboxAccount.encryptedPassword = oldOrganization.replyToMailboxAccount.encryptedPassword
            }
            newOrganization.replyToMailboxAccount = communicationMailboxAccountService.create(newOrganization.replyToMailboxAccount)
        }

        if (newOrganization.sendEmailServerProperties) {
            newOrganization.sendEmailServerProperties.type = CommunicationEmailServerPropertiesType.Send
            newOrganization.sendEmailServerProperties = communicationEmailServerPropertiesService.create(newOrganization.sendEmailServerProperties)
        }

        if (newOrganization.receiveEmailServerProperties) {
            newOrganization.receiveEmailServerProperties.type = CommunicationEmailServerPropertiesType.Receive
            newOrganization.receiveEmailServerProperties = communicationEmailServerPropertiesService.create(newOrganization.receiveEmailServerProperties)
        }
    }

    private void removeDependentMailboxAccountsAndEmailServerProperties(CommunicationOrganization fetched) {
        if (fetched.senderMailboxAccount) {
            communicationMailboxAccountService.delete(fetched.senderMailboxAccount)
        }

        if (fetched.replyToMailboxAccount) {
            communicationMailboxAccountService.delete(fetched.replyToMailboxAccount)
        }

        if (fetched.sendEmailServerProperties) {
            communicationEmailServerPropertiesService.delete(fetched.sendEmailServerProperties)
        }

        if (fetched.receiveEmailServerProperties) {
            communicationEmailServerPropertiesService.delete(fetched.receiveEmailServerProperties)
        }
    }

}
