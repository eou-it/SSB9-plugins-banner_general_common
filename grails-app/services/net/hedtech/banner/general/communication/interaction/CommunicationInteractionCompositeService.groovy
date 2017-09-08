/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.interaction

import grails.util.Holders
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.general.communication.exceptions.CommunicationExceptionFactory
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import net.hedtech.banner.general.person.PersonIdentificationName
import net.hedtech.banner.service.ServiceBase
import org.apache.log4j.Logger
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.transaction.annotation.Transactional

@Transactional
class CommunicationInteractionCompositeService {

    private static final log = Logger.getLogger(CommunicationInteractionCompositeService.class)

    public static PersonIdentificationName getPersonOrNonPerson(String bannerId) {
        def person = fetchPersonOrNonPersonByAlternativeBannerId(bannerId)
        if (person) {
            return PersonIdentificationName.fetchPersonOrNonPersonByPidm( person?.pidm )
        } else
            return null
    }

    public static Map fetchPersonOrNonPersonByNameOrBannerId(filter, pagingAndSortParams) {
        String baseQuery = """FROM PersonIdentificationName a
	  	                WHERE (a.bannerId like :filter
	  	                or a.searchFirstName like :filter
	  	                or a.searchMiddleName like :filter
	  	                or a.searchLastName like :filter) """

        String queryString = baseQuery + " order by lastName, firstName, middleName, bannerId "
        String countQueryString = "select count(a.bannerId) " + baseQuery

        if (!filter)
            return []
        def queryCriteria = '%' + filter.toUpperCase().replaceAll("\\s","") + "%"  //remove spaces from search string

        def persons = PersonIdentificationName.withSession { session ->
            org.hibernate.Query query = session.createQuery( queryString ).setString('filter', queryCriteria)
            query.setMaxResults(pagingAndSortParams.max)
            query.setFirstResult(pagingAndSortParams.offset)
            query.list()
        }

        def count = PersonIdentificationName.withSession { session ->
            org.hibernate.Query query = session.createQuery( countQueryString ).setString('filter', queryCriteria)
            query.list().get(0)
        }

        return [list: persons, totalCount: count]
    }

    public static Map fetchPersonOrNonPersonByNameOrBannerIdEqual(filter, pagingAndSortParams) {
        String baseQuery = """FROM PersonIdentificationName a
	  	                WHERE (a.bannerId = :filter
	  	                or a.searchFirstName = :filter
	  	                or a.searchMiddleName = :filter
	  	                or a.searchLastName = :filter) """

        String queryString = baseQuery + " order by lastName, firstName, middleName, bannerId "
        String countQueryString = "select count(a.bannerId) " + baseQuery

        if (!filter) return []
        def queryCriteria = filter.toUpperCase().replaceAll("\\s","")

        def persons = PersonIdentificationName.withSession { session ->
            org.hibernate.Query query = session.createQuery( queryString ).setString('filter', queryCriteria)
            query.setMaxResults(pagingAndSortParams.max)
            query.setFirstResult(pagingAndSortParams.offset)
            query.list()
        }

        def count = PersonIdentificationName.withSession { session ->
            org.hibernate.Query query = session.createQuery( countQueryString ).setString('filter', queryCriteria)
            query.list().get(0)
        }

        return [list: persons, totalCount: count]
    }

    public static PersonIdentificationName fetchPersonOrNonPersonByAlternativeBannerId(String bannerId) {
        String queryString = """FROM PersonIdentificationName a
                        WHERE a.bannerId = :filter
                        """

        PersonIdentificationName object = PersonIdentificationName.withSession { session ->
            def list = session.createQuery( queryString ).setString('filter', bannerId).list()[0]
        }
        return object
    }
}
