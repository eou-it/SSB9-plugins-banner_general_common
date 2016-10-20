/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall.ldm

import grails.transaction.Transactional
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.overall.ldm.v6.PersonVisaCategory
import net.hedtech.banner.general.overall.ldm.v6.PersonVisaDecorator
import net.hedtech.banner.general.overall.ldm.v6.PersonVisaStatus
import net.hedtech.banner.query.operators.Operators
import net.hedtech.banner.restfulapi.RestfulApiValidationUtility

public class PersonVisaCompositeService extends LdmService {

    PersonVisaService personVisaService
    private final List<String> allowedSearchFields = ['person']
    private final Map<String, String> ldmToDomainMap = ['person': 'personGuid']

    @Transactional(readOnly = true)
    List<PersonVisaDecorator> list(Map params) {
        RestfulApiValidationUtility.correctMaxAndOffset(params, RestfulApiValidationUtility.MAX_DEFAULT, RestfulApiValidationUtility.MAX_UPPER_LIMIT)
        Map queryParams = [:]
        List criteria = []
        prepareSearchCriteria(queryParams, criteria, params)
        List<PersonVisa> personVisaList = personVisaService.fetchByCriteria(queryParams, criteria, params)
        return decorate(personVisaList)
    }

    @Transactional(readOnly = true)
    int count(Map params) {
        Map queryParams = [:]
        List criteria = []
        prepareSearchCriteria(queryParams, criteria, params)
        return personVisaService.countByCriteria(queryParams, criteria)
    }

    private void prepareSearchCriteria(Map queryParams, List criteria, Map params) {
        allowedSearchFields.each {
            if (params.containsKey(it)) {
                queryParams.put(it, params[it])
                criteria.add([key: it, binding: ldmToDomainMap[it], operator: Operators.EQUALS])
            }
        }
    }

    @Transactional(readOnly = true)
    PersonVisaDecorator get(String id) {
        PersonVisa personVisa = personVisaService.read(id)
        if (personVisa) {
            return decorate([personVisa])[0]
        } else {
            throw new ApplicationException("personvisa", new NotFoundException())
        }
    }

    private List<PersonVisaDecorator> decorate(List<PersonVisa> personVisaList) {
        List<PersonVisaDecorator> personVisaDecoratorList = []
        personVisaList.each {
            PersonVisaDecorator personVisaDecorator = new PersonVisaDecorator()
            //id
            personVisaDecorator.id = it.id

            //person
            PersonVisaDecorator.Person person = new PersonVisaDecorator.Person()
            person.id = it.personGuid
            personVisaDecorator.person = person

            //visa type
            PersonVisaCategory category = it.nonResInd?.toUpperCase() == 'Y' ? PersonVisaCategory.IMMIGRANT : PersonVisaCategory.NONIMMIGRANT
            PersonVisaDecorator.VisaType.Detail visaTypeDetail = new PersonVisaDecorator.VisaType.Detail()
            visaTypeDetail.id = it.visaTypeGuid
            PersonVisaDecorator.VisaType visaType = new PersonVisaDecorator.VisaType()
            visaType.category = category.value
            visaType.detail = visaTypeDetail
            personVisaDecorator.visaType = visaType

            //visa id
            personVisaDecorator.visaId = it.visaNumber

            //status
            if (it.visaExpireDate) {
                personVisaDecorator.status = new Date().after(it.visaExpireDate) ? PersonVisaStatus.EXPIRED : PersonVisaStatus.CURRENT
            }

            //requested on
            personVisaDecorator.requestedOn = it.visaRequestDate

            //issued on
            personVisaDecorator.issuedOn = it.visaIssueDate

            //expires on
            personVisaDecorator.expiresOn = it.visaExpireDate

            //entries
            //NA for now as GORVISA only stores the number of entries but the response expects dates

            personVisaDecoratorList << personVisaDecorator
        }
        return personVisaDecoratorList
    }
}