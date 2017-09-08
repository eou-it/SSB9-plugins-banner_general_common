/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
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
        String encryptionKey = Holders.config.communication?.security?.password?.encKey
        if (encryptionKey == null)
            throw new ApplicationException(CommunicationOrganization, "@@r1:security.keyMissing@@")
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.call("{$Sql.VARCHAR = call gckencr.decrypt_string (${encryptionKey},${encryptedPassword})}") {
            result -> decryptedPassword = result
        }
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
