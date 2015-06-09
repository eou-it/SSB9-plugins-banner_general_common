/*********************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/

package net.hedtech.banner.general.communication.field

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.security.FormContext
import net.hedtech.banner.service.ServiceBase
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder

class CommunicationFieldService extends ServiceBase {
    boolean transactional = true
    def communicationPopulationQueryStatementParseService
    def communicationFieldCalculationService
    def asynchronousBannerAuthenticationSpoofer

    def preCreate( domainModelOrMap ) {

        if (!CommunicationCommonUtility.userCanCreate()) {
            throw new ApplicationException(CommunicationField, "@@r1:operation.not.authorized@@")
        }

        CommunicationField communicationField = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationField
        communicationField.folder = (communicationField.folder ?: domainModelOrMap.folder)
        /* The default for renderAsHtml is false */
        if (communicationField.renderAsHtml == null)
            communicationField.renderAsHtml = false

        /* Supply default values */
        if (communicationField.ruleContent == null) {
            communicationField.statementType = null
            communicationField.returnsArrayArguments = null
        }

        if (!(communicationField.ruleContent == null)) {
            if (communicationField.statementType == null) {
                /* Since this is all we currently support */
                communicationField.statementType = CommunicationRuleStatementType.SQL_PREPARED_STATEMENT
            }
            if (communicationField.returnsArrayArguments == null) {
                communicationField.returnsArrayArguments = false
            }
            //check for sql injection and if it returns true then throw invalid exception
            communicationPopulationQueryStatementParseService.parse( communicationField.ruleContent )
        }

        validateFormatter( communicationField )

        if (communicationField.name == null || communicationField.name == "")
            throw new ApplicationException( CommunicationField, "@@r1:nameCannotBeNull@@" )

        if (communicationField.folder == null)
            throw new ApplicationException( CommunicationField, "@@r1:folderCannotBeNull@@" )
        else
            validateFolder( communicationField.getFolder().getId() )

        if (communicationField.name.contains( " " ))
            throw new ApplicationException( CommunicationField, "@@r1:space.not.allowed@@" )

        if (CommunicationField.fetchByName( communicationField.name ))
            throw new ApplicationException( CommunicationField, "@@r1:fieldNameAlreadyExists@@" )

        if (communicationField.immutableId == null)
            communicationField.immutableId = UUID.randomUUID().toString()

        if (communicationField.status == null)
            communicationField.status = CommunicationFieldStatus.DEVELOPMENT

        /* Make sure the status is one of the enumerated ones */
        if (CommunicationFieldStatus.set().every { it != communicationField.status }) {
            throw new ApplicationException( CommunicationField, "@@r1:invalidStatus@@" )
        }
    }


    def preUpdate( domainModelOrMap ) {
        CommunicationField communicationField = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationField

        if (communicationField.id == null)
            throw new ApplicationException(CommunicationField, "@@r1:fieldDoesNotExist@@")

        def oldfield = CommunicationField.get(communicationField.id)

        if (oldfield.id == null)
            throw new ApplicationException(CommunicationField, "@@r1:fieldDoesNotExist@@")

        //check if user is authorized. user should be admin or author
        if (!CommunicationCommonUtility.userCanUpdateDelete(oldfield.lastModifiedBy)) {
            throw new ApplicationException(CommunicationField, "@@r1:operation.not.authorized@@")
        }

        /* Supply default values */
        if (communicationField.ruleContent == null) {
            communicationField.statementType = null
            communicationField.returnsArrayArguments = null
        }

        if (!(communicationField.ruleContent == null)) {
            if (communicationField.statementType == null) {
                /* Since this is all we currently support */
                communicationField.statementType = CommunicationRuleStatementType.SQL_PREPARED_STATEMENT
            }
            if (communicationField.returnsArrayArguments == null) {
                communicationField.returnsArrayArguments = false
            }
            //check for sql injection and if it returns true then throw invalid exception
            communicationPopulationQueryStatementParseService.parse( communicationField.ruleContent )
        }

        if (communicationField.name == null || communicationField.name == "")
            throw new ApplicationException( CommunicationField, "@@r1:nameCannotBeNull@@" )

        if (communicationField.name.contains( " " ))
            throw new ApplicationException( CommunicationField, "@@r1:space.not.allowed@@" )

        if (CommunicationField.existsAnotherName( communicationField.id, communicationField.name ))
            throw new ApplicationException( CommunicationField, "@@r1:fieldNameAlreadyExists@@" )


        validateFormatter( communicationField )

        if (communicationField.renderAsHtml == null)
            throw new ApplicationException( CommunicationField, "@@r1:renderAsHtmlCannotBeNull@@" )

        if (communicationField.status == null)
            throw new ApplicationException( CommunicationField, "@@r1:statusCannotBeNull@@" )

        if (communicationField.status == CommunicationFieldStatus.PRODUCTION && !(communicationField.name != null && communicationField.folder != null && communicationField.formatString != null)) {
            throw new ApplicationException( CommunicationFieldService, "@@r1:datafield.cannotUpdatePublished@@" )
        }
        }


    void validateFormatter( CommunicationField communicationField ) {
        /* Either all three of these are null, or all three are not null */
        if (!((communicationField.returnsArrayArguments == null && communicationField.ruleContent == null && communicationField.statementType == null) ||
                (communicationField.returnsArrayArguments != null && communicationField.ruleContent != null && communicationField.statementType != null)))
            throw new ApplicationException( CommunicationField, "@@r1:allOrNone@@" )

    }


    void validateFolder( Long folderId ) {
        def fetchedFolder = CommunicationFolder.fetchById( folderId )
        if (fetchedFolder == null || fetchedFolder.id == null) {
            throw new NotFoundException( id: folderId, entityClassName: domainClass.simpleName )
        }
    }


    def publishDataField( map ) {

        if (map.id) {
            def communicationField = CommunicationField.get( map.id )
            /*
            Attempt to extract the variables from the format string, which functions as a StringTemplate iteself.
            If this fails, the template is not parsable, it should throw an exception
            */
            communicationFieldCalculationService.extractVariables( communicationField.formatString )

            if (communicationField.status == CommunicationFieldStatus.PRODUCTION)
                return
            if (communicationField.name != null && communicationField.folder != null && communicationField.formatString != null) {
                if (communicationField.ruleContent != null) {
                    //check for sql injection and if it returns true then throw invalid exception
                    def parseResult = communicationPopulationQueryStatementParseService.parse( communicationField.ruleContent )
                    if (parseResult?.status != "Y") {
                        throw new ApplicationException( CommunicationField, "@@r1:cannotPublishSqlStatementInvalid@@" )
                    }
                }
                communicationField.status = CommunicationFieldStatus.PRODUCTION
                update( communicationField )
            } else
                throw new ApplicationException( CommunicationField, "@@r1:datafield.cannotBePublished@@" )
        } else
            throw new ApplicationException( CommunicationField, "@@r1:idNotValid@@" )
    }

    def preDelete(domainModelOrMap) {

        if ((domainModelOrMap.id == null) && (domainModelOrMap?.domainModel.id == null))
            throw new ApplicationException(CommunicationField, "@@r1:fieldDoesNotExist@@")

        def oldfield = CommunicationField.get(domainModelOrMap.id ?: domainModelOrMap?.domainModel.id)

        if (oldfield.id == null)
            throw new ApplicationException(CommunicationField, "@@r1:fieldDoesNotExist@@")

        if (!CommunicationCommonUtility.userCanUpdateDelete(oldfield.lastModifiedBy)) {
            throw new ApplicationException(CommunicationField, "@@r1:operation.not.authorized@@")
        }
    }
}
