/*********************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/

package net.hedtech.banner.general.communication.field

import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.general.communication.exceptions.CommunicationExceptionFactory
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.parameter.CommunicationParameter
import net.hedtech.banner.general.communication.parameter.CommunicationParameterFieldAssociation
import net.hedtech.banner.service.ServiceBase

import java.sql.Connection
import java.sql.SQLException
import java.util.regex.Pattern

class CommunicationFieldService extends ServiceBase {
    boolean transactional = true
    def communicationPopulationQueryStatementParseService
    def communicationFieldCalculationService
    def asynchronousBannerAuthenticationSpoofer
    def communicationParameterFieldAssociationService

    def preCreate(domainModelOrMap) {

        if (!CommunicationCommonUtility.userCanAuthorContent()) {
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

        if (communicationField.name.contains( "\$" ))
            throw new ApplicationException( CommunicationField, "@@r1:dollarCharacter.not.allowed@@" )

        if (CommunicationField.fetchByNameForFGAC( communicationField.name ))
            throw new ApplicationException( CommunicationField, "@@r1:fieldNameAlreadyExists@@" )

        if (communicationField.immutableId == null)
            communicationField.immutableId = UUID.randomUUID().toString()

        if (communicationField.status == null)
            communicationField.status = CommunicationFieldStatus.DEVELOPMENT

        /* Make sure the status is one of the enumerated ones */
        if (CommunicationFieldStatus.set().every { it != communicationField.status }) {
            throw new ApplicationException( CommunicationField, "@@r1:invalidStatus@@" )
        }

        if (communicationField.isPublished()) {
            validatePublished( communicationField )
            updateFieldParameterAssociation(communicationField)
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
        if (!CommunicationCommonUtility.userCanUpdateDeleteContent(oldfield.lastModifiedBy)) {
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

        if (communicationField.name.contains( "\$" ))
            throw new ApplicationException( CommunicationField, "@@r1:dollarCharacter.not.allowed@@" )

        if (CommunicationField.existsAnotherName( communicationField.id, communicationField.name ))
            throw new ApplicationException( CommunicationField, "@@r1:fieldNameAlreadyExists@@" )


        validateFormatter( communicationField )

        if (communicationField.renderAsHtml == null)
            throw new ApplicationException( CommunicationField, "@@r1:renderAsHtmlCannotBeNull@@" )

        if (communicationField.status == null)
            throw new ApplicationException( CommunicationField, "@@r1:statusCannotBeNull@@" )

        if (communicationField.isPublished()) {
            validatePublished( communicationField )
            updateFieldParameterAssociation(communicationField)
        }
    }

    List<String> fieldParameterNameList(CommunicationField field) {
        def regex = Pattern.compile(":(\\w+)", Pattern.DOTALL); //get all words between a colon and a space including new lines
        return (field?.ruleContent =~ regex) as List
    }

    void updateFieldParameterAssociation(CommunicationField communicationField) {
        assert communicationField != null

        if (communicationField.id != null) {
            deleteExistingParameterFieldAssociations( communicationField )
        }

        // for each extracted parameter create a parmaeter field association
        fieldParameterNameList(communicationField)?.each { m ->
            if (m[1] != 'pidm') { //dont create association for the pidm

                def param = CommunicationParameter.fetchByName(m[1])
                if (param == null || param?.id == null) {
                    throw new ApplicationException( CommunicationField, "@@r1:parameter.does.not.exist@@" )
                }
                if (!CommunicationParameterFieldAssociation.fetchByFieldAndParameter(communicationField, param)) {
                    def cfa = new CommunicationParameterFieldAssociation()
                    cfa.field = communicationField
                    cfa.parameter = param
                    communicationParameterFieldAssociationService.create(cfa)
                }
            }
        }
    }

    void validateFormatter( CommunicationField communicationField ) {
        /* Either all three of these are null, or all three are not null */
        if (!((communicationField.returnsArrayArguments == null && communicationField.ruleContent == null && communicationField.statementType == null) ||
                (communicationField.returnsArrayArguments != null && communicationField.ruleContent != null && communicationField.statementType != null)))
            throw new ApplicationException( CommunicationField, "@@r1:allOrNone@@" )

    }


    void validateFolder( Long folderId ) {
        def fetchedFolder = CommunicationFolder.get( folderId )
        if (fetchedFolder == null || fetchedFolder.id == null) {
            throw new NotFoundException( id: folderId, entityClassName: domainClass.simpleName )
        }
    }


    def publishDataField( map ) {
        if (!map || !map.id) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationField.class, "idNotValid" )
        }

        def theField = CommunicationField.get( map.id )
        theField.status = CommunicationFieldStatus.PRODUCTION
        this.update( theField )
    }


    void validatePublished( CommunicationField field ) {
        assert( field )

        if (isEmpty( field.name )) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationField.class, "nameRequiredForPublished" )
        }

        if (!field.folder) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationField.class, "folderRequiredForPublished" )
        }

        if (isEmpty( field.formatString )) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationField.class, "formatRequiredForPublished" )
        }

        if (!isEmpty( field.ruleContent )) {
            int whereIndex = field.ruleContent.toLowerCase().indexOf( "where" )

            if (whereIndex < 0) {
                throw CommunicationExceptionFactory.createApplicationException( CommunicationField.class, "sqlStatementWhereRequiredForPublished" )
            }

            if (field.ruleContent.indexOf( ":pidm", whereIndex + "where".length() ) < 0) {
                throw CommunicationExceptionFactory.createApplicationException( CommunicationField.class, "sqlStatementMustReferPidmForPublished" )
            }

            def parseResult = communicationPopulationQueryStatementParseService.parse( field.ruleContent )
            if (!parseResult) {
                throw CommunicationExceptionFactory.createApplicationException( CommunicationField.class, "cannotPublishSqlStatementInvalid" )
            }

            if (parseResult.status != "Y") {
                throw CommunicationExceptionFactory.createApplicationException( CommunicationField.class, "cannotPublishSqlStatementInvalid" )
            }
        }

        /*
        Attempt to extract the variables from the format string, which functions as a StringTemplate iteself.
        If this fails, the template is not parsable, it should throw an exception
        */
        communicationFieldCalculationService.extractVariables( field.formatString )
    }


    def preDelete(domainModelOrMap) {

        if ((domainModelOrMap.id == null) && (domainModelOrMap?.domainModel.id == null))
            throw new ApplicationException(CommunicationField, "@@r1:fieldDoesNotExist@@")

        def communicationField = CommunicationField.get(domainModelOrMap.id ?: domainModelOrMap?.domainModel.id)

        if (communicationField.id == null)
            throw new ApplicationException(CommunicationField, "@@r1:fieldDoesNotExist@@")

        if (!CommunicationCommonUtility.userCanUpdateDeleteContent(communicationField.lastModifiedBy)) {
            throw new ApplicationException(CommunicationField, "@@r1:operation.not.authorized@@")
        }

        if (communicationField.id != null) {
            deleteExistingParameterFieldAssociations( communicationField )
        }
    }


    private boolean isEmpty( String s ) {
        return (!s || s.trim().size() == 0)
    }

    private void deleteExistingParameterFieldAssociations(CommunicationField communicationField) {
        def Sql sql
        try {
            Connection connection = (Connection) sessionFactory.getCurrentSession().connection()
            sql = new Sql((Connection) sessionFactory.getCurrentSession().connection())
            int rowsDeleted = sql.executeUpdate("delete from gcrflpm where gcrflpm_field_id = ?", [communicationField.id])
        } catch (SQLException e) {
            throw CommunicationExceptionFactory.createApplicationException(CommunicationFieldService, e)
        } catch (Exception e) {
            throw CommunicationExceptionFactory.createApplicationException(CommunicationFieldService, e)
        } finally {
            sql?.close()
        }
    }
}
