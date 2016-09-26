/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population.selectionlist

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.service.ServiceBase

// NOTE:
// This service is injected with create, update, and delete methods that may throw runtime exceptions (listed below).
// These exceptions must be caught and handled by the controller using this service.
//
// update and delete may throw net.hedtech.banner.exceptions.NotFoundException if the entity cannot be found in the database
// update and delete may throw org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException a runtime exception if an optimistic lock failure occurs
// create, update, and delete may throw grails.validation.ValidationException a runtime exception when there is a validation failure

class CommunicationPopulationSelectionListService extends ServiceBase {

    boolean transactional = true


    def preCreate(map) {
        //throw new ApplicationException(PopulationSelectionList, "@@r1:unsupported.operation@@")
    }


    def preDelete(domainModelOrMap) {
        if ((domainModelOrMap.id == null) && (domainModelOrMap?.domainModel.id == null))
            throw new ApplicationException(CommunicationPopulationSelectionList, "@@r1:queryDoesNotExist@@")

        def oldpop = CommunicationPopulationSelectionList.get(domainModelOrMap.id ?: domainModelOrMap?.domainModel.id)

        if (oldpop.id == null)
            throw new ApplicationException(CommunicationPopulationSelectionList, "@@r1:queryDoesNotExist@@")
    }


    def preUpdate(domainModelOrMap) {
//        throw new ApplicationException(PopulationSelectionList, "@@r1:unsupported.operation@@")

    }
}
