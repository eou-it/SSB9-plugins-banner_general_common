/*********************************************************************************
 Copyright 2016-2018 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.general.communication.organization

import grails.gorm.transactions.Transactional
import net.hedtech.banner.exceptions.ApplicationException
/**
 * Service for providing basic crud services on
 * Communication Organization domain objects while also
 * handling dependent mailbox accounts and email server properties.
 */
@Transactional
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
        removeDependentMailboxAccountsAndEmailServerProperties( oldOrganization, newOrganization )

        // Default the mobile application key to its previous value if mobile settings present during an
        // update but no clear password passed in.
        if ((newOrganization.mobileApplicationName || newOrganization.mobileEndPointUrl) &&
            !newOrganization?.clearMobileApplicationKey &&
            oldOrganization?.encryptedMobileApplicationKey) {

            newOrganization.encryptedMobileApplicationKey  = oldOrganization.encryptedMobileApplicationKey
        } else if (newOrganization.parent == null && newOrganization.mobileApplicationName != null && newOrganization.mobileEndPointUrl != null && oldOrganization.encryptedMobileApplicationKey == null &&
                   newOrganization.clearMobileApplicationKey == null) {
            throw new ApplicationException(CommunicationOrganization, "@@r1:notExists.mobileApplicationKey@@")
        }

        newOrganization = communicationOrganizationService.update( newOrganization )

        return newOrganization;
    }

    void deleteOrganization( Map organizationAsMap ) {
        CommunicationOrganization organization = this.getOrganization( organizationAsMap.id )
        communicationOrganizationService.delete( organizationAsMap )
        removeDependentMailboxAccountsAndEmailServerProperties( organization )
    }

    Boolean emailDetailExists(id) {
        CommunicationOrganization org =  communicationOrganizationService.get( id )
        CommunicationOrganization rootOrg = CommunicationOrganization.fetchRoot()
        if ((org?.senderMailboxAccount && org?.replyToMailboxAccount) &&
                (org?.sendEmailServerProperties || rootOrg?.sendEmailServerProperties))
            return true;
        else
            return false;
    }

    Boolean mobileDetailExists() {
        CommunicationOrganization rootorg = CommunicationOrganization.fetchRoot()
        if (rootorg?.mobileApplicationName && rootorg?.mobileEndPointUrl)
            return true;
        else
            return false;
    }

    def fetchRootReadOnly() {
        CommunicationOrganization rootorg = CommunicationOrganization.fetchRoot()
        return ['id':rootorg?.id, 'name':rootorg?.name, 'isRoot':true, 'version':rootorg?.version]

    }


    private void createDependentMailboxAccountAndEmailServerProperties( CommunicationOrganization newOrganization, CommunicationOrganization oldOrganization = null) {
        CommunicationOrganization rootorg = CommunicationOrganization.fetchRoot()
        Map smtpProperties
        if (!(rootorg && rootorg.id && rootorg.id == newOrganization?.id) && newOrganization.sendEmailServerProperties == null) {
              smtpProperties = rootorg?.sendEmailServerProperties?.getSmtpPropertiesAsMap()
        } else {
            smtpProperties = newOrganization.sendEmailServerProperties?.getSmtpPropertiesAsMap()
        }
        if (newOrganization.senderMailboxAccount) {
            newOrganization.senderMailboxAccount.type = CommunicationMailboxAccountType.Sender
            if (!newOrganization.senderMailboxAccount.clearTextPassword && oldOrganization?.senderMailboxAccount) {
                newOrganization.senderMailboxAccount.encryptedPassword = oldOrganization.senderMailboxAccount.encryptedPassword
            }

            if ( (smtpProperties == null || smtpProperties?.auth == null || smtpProperties?.auth) && (newOrganization.senderMailboxAccount && !(newOrganization.senderMailboxAccount.emailAddress != null && newOrganization.senderMailboxAccount.userName != null)
                    && !(newOrganization.senderMailboxAccount.emailAddress == null && newOrganization.senderMailboxAccount.userName == null)
                    && ((oldOrganization?.sendEmailServerProperties?.getSmtpPropertiesAsMap()?.auth) || (!newOrganization.parent))) ){
//                Do nothing here and throw a warning message from the controller
//                throw new ApplicationException(CommunicationOrganization, "@@r1:mailbox.nameAndAddress.required@@")
            } else if (newOrganization.senderMailboxAccount && (newOrganization.senderMailboxAccount.emailAddress == null && newOrganization.senderMailboxAccount.userName == null
                    && newOrganization.senderMailboxAccount.clearTextPassword == null && newOrganization.senderMailboxAccount.emailDisplayName == null)) {
                //setting the object to null if it was pre-existing and all attributes were nulled out during the update of organization, so it does not try to create it
                newOrganization.senderMailboxAccount = null;
            }
            if (newOrganization?.senderMailboxAccount?.id != null) {
                if(newOrganization.senderMailboxAccount.emailAddress == null) {
                    throw new ApplicationException(CommunicationOrganization, "@@r1:mailbox.emailAddress.required@@")
                }
                newOrganization.senderMailboxAccount = communicationMailboxAccountService.update(newOrganization.senderMailboxAccount)
            } else  {
                if(newOrganization.senderMailboxAccount.emailAddress == null) {
                    throw new ApplicationException(CommunicationOrganization, "@@r1:mailbox.emailAddress.required@@")
                }
                newOrganization.senderMailboxAccount = communicationMailboxAccountService.create(newOrganization.senderMailboxAccount)
            }
        }

        if (newOrganization.replyToMailboxAccount) {
            newOrganization.replyToMailboxAccount.type = CommunicationMailboxAccountType.ReplyTo
            if (!newOrganization.replyToMailboxAccount.clearTextPassword && oldOrganization?.replyToMailboxAccount) {
                newOrganization.replyToMailboxAccount.encryptedPassword = oldOrganization.replyToMailboxAccount.encryptedPassword
            }

            if ((smtpProperties == null || smtpProperties?.auth == null || smtpProperties?.auth) && (newOrganization.replyToMailboxAccount && !(newOrganization.replyToMailboxAccount.emailAddress != null && newOrganization.replyToMailboxAccount.userName != null)
                    && !(newOrganization.replyToMailboxAccount.emailAddress == null && newOrganization.replyToMailboxAccount.userName == null))
                    && ((oldOrganization?.sendEmailServerProperties?.getSmtpPropertiesAsMap()?.auth) || (!newOrganization.parent))){
                throw new ApplicationException(CommunicationOrganization, "@@r1:mailbox.nameAndAddress.required@@")
            } else if (newOrganization.replyToMailboxAccount && (newOrganization.replyToMailboxAccount.emailAddress == null && newOrganization.replyToMailboxAccount.userName == null
                    && newOrganization.replyToMailboxAccount.clearTextPassword == null && newOrganization.replyToMailboxAccount.emailDisplayName == null)) {
                //setting the object to null if it was pre-existing and all attributes were nulled out during the update of organization, so it does not try to create it
                newOrganization.replyToMailboxAccount = null;
            }
            if (newOrganization.replyToMailboxAccount?.id != null) {
                if(newOrganization.replyToMailboxAccount.emailAddress == null) {
                    throw new ApplicationException(CommunicationOrganization, "@@r1:mailbox.emailAddress.required@@")
                }
                newOrganization.replyToMailboxAccount = communicationMailboxAccountService.update(newOrganization.replyToMailboxAccount)
            } else {
                if(newOrganization.replyToMailboxAccount.emailAddress == null) {
                    throw new ApplicationException(CommunicationOrganization, "@@r1:mailbox.emailAddress.required@@")
                }
                newOrganization.replyToMailboxAccount = communicationMailboxAccountService.create(newOrganization.replyToMailboxAccount)
            }
        }

        if (newOrganization.sendEmailServerProperties && (newOrganization.sendEmailServerProperties?.id == null)) {
            newOrganization.sendEmailServerProperties.type = CommunicationEmailServerPropertiesType.Send
            newOrganization.sendEmailServerProperties = communicationEmailServerPropertiesService.create(newOrganization.sendEmailServerProperties)
        }

        if (newOrganization.receiveEmailServerProperties && (newOrganization.receiveEmailServerProperties.id == null)) {
            newOrganization.receiveEmailServerProperties.type = CommunicationEmailServerPropertiesType.Receive
            newOrganization.receiveEmailServerProperties = communicationEmailServerPropertiesService.create(newOrganization.receiveEmailServerProperties)
        }
    }

    private void removeDependentMailboxAccountsAndEmailServerProperties(CommunicationOrganization fetched, CommunicationOrganization neworg=null) {

        if (fetched.senderMailboxAccount && !(neworg && neworg.senderMailboxAccount && neworg.senderMailboxAccount.id && neworg.senderMailboxAccount.id == fetched.senderMailboxAccount.id) ) {
            communicationMailboxAccountService.delete(fetched.senderMailboxAccount)
        }

        if (fetched.replyToMailboxAccount && !(neworg && neworg.replyToMailboxAccount && neworg.replyToMailboxAccount.id && neworg.replyToMailboxAccount.id == fetched.replyToMailboxAccount.id)) {
            communicationMailboxAccountService.delete(fetched.replyToMailboxAccount)
        }

        if (fetched.sendEmailServerProperties && !(neworg && neworg.sendEmailServerProperties && neworg.sendEmailServerProperties.id && neworg.sendEmailServerProperties.id == fetched.sendEmailServerProperties.id)) {
            communicationEmailServerPropertiesService.delete(fetched.sendEmailServerProperties)
        }

        if (fetched.receiveEmailServerProperties && !(neworg && neworg.receiveEmailServerProperties && neworg.receiveEmailServerProperties.id && neworg.receiveEmailServerProperties.id == fetched.receiveEmailServerProperties.id)) {
            communicationEmailServerPropertiesService.delete(fetched.receiveEmailServerProperties)
        }
    }

}
