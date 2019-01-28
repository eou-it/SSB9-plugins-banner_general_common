/*********************************************************************************
 Copyright 2014-2017 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.organization

import grails.util.Holders
import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.service.ServiceBase


class CommunicationMailboxAccountService extends ServiceBase {

    def sessionFactory

    def preCreate(domainModelOrMap) {
        CommunicationMailboxAccount mailboxAccount = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationMailboxAccount
        if (mailboxAccount.clearTextPassword) {
            mailboxAccount.encryptedPassword = encryptPassword( mailboxAccount.clearTextPassword)
        }

    }

    def decryptPassword(String encryptedPassword) {
        def decryptedPassword
        println "In the decrypt password method of mailbox account service"

        try {
            println "The holders value is "+Holders.config
            println "communication: "+Holders.config.communication
            println "The different values are: "+Holders.config.communication.security.password
            println "The comunication is : " + Holders.config.communication.security.password.encKey
        } catch (Exception e){
            println "I GOT AN exception: "+e.getMessage()
        }
        String encryptionKey = Holders.config.communication?.security?.password?.encKey
        println "The encryption key used is:***"+encryptionKey+"***"
        if (encryptionKey == null)
            throw new ApplicationException(CommunicationOrganization, "@@r1:security.keyMissing@@")
        println "The encryption key is not null as it has passed the exception"
        println "The password is "+encryptedPassword
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.call("{$Sql.VARCHAR = call gckencr.decrypt_string (${encryptionKey},${encryptedPassword})}") {
            result -> decryptedPassword = result
        }
        println "Executed the method successfully: "+decryptedPassword
        decryptedPassword
    }

    def encryptPassword(String clearTextPassword) {
        def encryptedPassword
        String encryptionKey = Holders.config.communication?.security?.password?.encKey
        if (encryptionKey == null)
            throw new ApplicationException(CommunicationOrganization, "@@r1:security.keyMissing@@")
        try {
            def sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.call("{$Sql.VARCHAR = call gckencr.encrypt_string (${encryptionKey},${clearTextPassword})}") {
                result -> encryptedPassword = result
            }
        } catch (Exception e) {
            throw new net.hedtech.banner.exceptions.ApplicationException(CommunicationOrganization, "@@r1:password.encrypt.error@@")
        }
        encryptedPassword
    }
}
