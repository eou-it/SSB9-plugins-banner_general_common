/*********************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.parameter

import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.service.ServiceBase

/**
 * Service for providing basic crud services on
 * Parameter domain objects.
 */
@Transactional
class CommunicationParameterService extends ServiceBase {

    def preCreate( domainModelOrMap ) {

        if (!CommunicationCommonUtility.userCanAuthorContent()) {
            throw new ApplicationException(CommunicationParameter, "@@r1:operation.not.authorized@@")
        }

        CommunicationParameter communicationParameter = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationParameter
        if (communicationParameter.name == null || communicationParameter.name == "")
            throw new ApplicationException( CommunicationParameter, "@@r1:nameCannotBeNull@@" )

        if (communicationParameter.name.contains( " " ))
            throw new ApplicationException( CommunicationParameter, "@@r1:space.not.allowed@@" )

        if (communicationParameter.name.contains( "\$" ))
            throw new ApplicationException( CommunicationParameter, "@@r1:dollarCharacter.not.allowed@@" )

        if (communicationParameter.name.contains( ":" ))
            throw new ApplicationException( CommunicationParameter, "@@r1:colonNotAllowedInParameterName@@" )

        if (communicationParameter.name.equalsIgnoreCase( "pidm" ))
            throw new ApplicationException( CommunicationParameter, "@@r1:pidmNotAllowedInParameterName@@" )

        if (CommunicationParameter.fetchByName( communicationParameter.name ))
            throw new ApplicationException( CommunicationParameter, "@@r1:parameterNameAlreadyExists:"+communicationParameter.name + "@@" )
    }


    def preUpdate( domainModelOrMap ) {
        CommunicationParameter communicationParameter = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationParameter

        if (communicationParameter.id == null)
            throw new ApplicationException(CommunicationParameter, "@@r1:parameterDoesNotExist@@")

        def oldfield = CommunicationParameter.get(communicationParameter.id)

        if (oldfield.id == null)
            throw new ApplicationException(CommunicationParameter, "@@r1:parameterDoesNotExist@@")

        //check if user is authorized. user should be admin or author
        if (!CommunicationCommonUtility.userCanUpdateDeleteContent(oldfield.lastModifiedBy)) {
            throw new ApplicationException(CommunicationParameter, "@@r1:operation.not.authorized@@")
        }

        if (communicationParameter.name == null || communicationParameter.name == "")
            throw new ApplicationException( CommunicationParameter, "@@r1:nameCannotBeNull@@" )

        if (communicationParameter.name.contains( " " ))
            throw new ApplicationException( CommunicationParameter, "@@r1:space.not.allowed@@" )

        if (communicationParameter.name.contains( "\$" ))
            throw new ApplicationException( CommunicationParameter, "@@r1:dollarCharacter.not.allowed@@" )

        if (communicationParameter.name.contains( ":" ))
            throw new ApplicationException( CommunicationParameter, "@@r1:colonNotAllowedInParameterName@@" )

        if (communicationParameter.name.equalsIgnoreCase( "pidm" ))
            throw new ApplicationException( CommunicationParameter, "@@r1:pidmNotAllowedInParameterName@@" )

        if (CommunicationParameter.existsAnotherName( communicationParameter.id, communicationParameter.name ))
            throw new ApplicationException( CommunicationParameter, "@@r1:parameterNameAlreadyExists:"+communicationParameter.name + "@@" )
    }


    def preDelete(domainModelOrMap) {

        if ((domainModelOrMap.id == null) && (domainModelOrMap?.domainModel.id == null))
            throw new ApplicationException(CommunicationParameter, "@@r1:parameterDoesNotExist@@")

        def oldfield = CommunicationParameter.get(domainModelOrMap.id ?: domainModelOrMap?.domainModel.id)

        if (oldfield.id == null)
            throw new ApplicationException(CommunicationParameter, "@@r1:parameterDoesNotExist@@")

        if (!CommunicationCommonUtility.userCanUpdateDeleteContent(oldfield.lastModifiedBy)) {
            throw new ApplicationException(CommunicationParameter, "@@r1:operation.not.authorized@@")
        }
    }

    def boolean checkParameterUsedInField (parameterId) {
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        def count
        try {
            sql.eachRow("""select count(*) as totalCount from gcrflpm 
                         where GCRFLPM_PARAMETER_ID = ?""", [parameterId]) { row ->
                count = row.totalCount
            }
        } catch (Exception le) {
            log.debug("Could not retrieve feild parameter association for parameter id: ${le.getMessage()}")
        }

        return count > 0 ? true : false
    }
}
