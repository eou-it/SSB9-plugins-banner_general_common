/*********************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.utility

import grails.gorm.transactions.Transactional
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.service.ServiceBase

/**
 * Service for providing basic crud services on
 * Mail (GURMAIL) objects.
 */
@Transactional
class MailService extends ServiceBase {

    def preCreate( domainModelOrMap ) {

    }

    def preUpdate( domainModelOrMap ) {

    }

    def preDelete(domainModelOrMap) {

    }

}