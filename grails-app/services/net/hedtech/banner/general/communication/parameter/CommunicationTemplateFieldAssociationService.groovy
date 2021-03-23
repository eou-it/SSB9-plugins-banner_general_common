/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.parameter

import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.general.communication.field.CommunicationField
import net.hedtech.banner.service.ServiceBase

@Slf4j
@Transactional
class CommunicationTemplateFieldAssociationService extends ServiceBase {

    //private static final log = Logger.getLogger(CommunicationTemplateFieldAssociationService.class)

    def preCreate(domainModelOrMap) {
        if (!CommunicationCommonUtility.userCanAuthorContent()) {
            throw new ApplicationException(CommunicationTemplateFieldAssociation, "@@r1:operation.not.authorized@@")
        }
    }

    def preUpdate(domainModelOrMap) {
        throw new UnsupportedOperationException()
    }

    public List findAllByField(CommunicationField field) {
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())

        List templateList
        try {
            templateList = sql.rows("""select * from
                                            (select gcbtmpl_surrogate_id as templateId, gcbtmpl_name as templateName, gcbtmpl_comm_channel as communicationChannel, gcbtmpl_description as description, 
                                            gcbtmpl_folder_id as folderId, gcrfldr_name as folderName
                                            from gcbtmpl left join gcrfldr
                                            on gcbtmpl_folder_id = gcrfldr_surrogate_id)
                                            where templateId in ( select gcrtpfl_template_id from gcrtpfl where gcrtpfl_field_id = ? )""", [field.id]);
        } catch (Exception le) {
            log.debug("Could not retrieve the associated templates for the data field: ${le.getMessage()}")
        } finally {
        }
        return templateList
    }
}
