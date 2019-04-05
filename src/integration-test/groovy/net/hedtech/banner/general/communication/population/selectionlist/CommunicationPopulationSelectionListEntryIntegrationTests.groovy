/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population.selectionlist

import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.orm.hibernate5.HibernateOptimisticLockingFailureException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import static groovy.test.GroovyAssert.*
import grails.util.GrailsWebMockUtil
import org.grails.plugins.testing.GrailsMockHttpServletRequest
import org.grails.plugins.testing.GrailsMockHttpServletResponse
import org.grails.web.servlet.mvc.GrailsWebRequest
import grails.web.servlet.context.GrailsWebApplicationContext

/**
 * Integration tests for PopulationSelectionListEntry entity
 */
@Integration
@Rollback
class CommunicationPopulationSelectionListEntryIntegrationTests extends BaseIntegrationTestCase {
    def CommunicationPopulationSelectionList globalTestPopulationSelectionList
    def selfServiceBannerAuthenticationProvider


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        webAppCtx = new GrailsWebApplicationContext()
        mockRequest()
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('BCMADMIN', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)
    }


    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }

    public GrailsWebRequest mockRequest() {
        GrailsMockHttpServletRequest mockRequest = new GrailsMockHttpServletRequest();
        GrailsMockHttpServletResponse mockResponse = new GrailsMockHttpServletResponse();
        GrailsWebMockUtil.bindMockWebRequest(webAppCtx, mockRequest, mockResponse)
    }

    void setUpData() {
        globalTestPopulationSelectionList = new CommunicationPopulationSelectionList()
        globalTestPopulationSelectionList.save(failOnError: true, flush: true)
        assertNotNull(globalTestPopulationSelectionList.id)
    }

    @Test
    void testCreatePopulationSelectionListEntry() {
        setUpData()
        def populationSelectionListEntry = newPopulationSelectionListEntry()
        populationSelectionListEntry.populationSelectionList = globalTestPopulationSelectionList
        populationSelectionListEntry.save()
        // Assert domain values
        assertNotNull populationSelectionListEntry?.id
        assertEquals 9199999999999999999, populationSelectionListEntry.pidm
        assertEquals globalTestPopulationSelectionList, populationSelectionListEntry.populationSelectionList
    }


    @Test
    void testDeletePopulationSelectionListEntry() {
        setUpData()
        def populationSelectionListEntry = newPopulationSelectionListEntry()
        populationSelectionListEntry.populationSelectionList = globalTestPopulationSelectionList
        populationSelectionListEntry.save()

        // Assert domain values
        assertNotNull populationSelectionListEntry?.id
        def id = populationSelectionListEntry.id

        // Find the domain
        populationSelectionListEntry = CommunicationPopulationSelectionListEntry.get(id)
        assertNotNull populationSelectionListEntry

        // Delete the domain
        populationSelectionListEntry.delete()

        // Attempt to find the deleted domain
        populationSelectionListEntry = CommunicationPopulationSelectionListEntry.get(id)
        assertNull populationSelectionListEntry
    }

    /*
    Test the named query that returns entires for a selection list
     */


    @Test
    void testFetchBySelectionListId() {
        setUpData()
        def populationSelectionListEntry1 = newPopulationSelectionListEntry()
        populationSelectionListEntry1.populationSelectionList = globalTestPopulationSelectionList
        populationSelectionListEntry1.save(failOnError: true, flush: true)

        def populationSelectionListEntry2 = newPopulationSelectionListEntry()
        populationSelectionListEntry2.pidm = 14
        populationSelectionListEntry2.populationSelectionList = globalTestPopulationSelectionList
        populationSelectionListEntry2.save(failOnError: true, flush: true)

        List entries = CommunicationPopulationSelectionListEntry.fetchBySelectionListId(globalTestPopulationSelectionList.id)
        assertEquals(2, entries.size())
    }


    @Test
    void testNullValidationFailure() {
        // Instantiate an empty domain
        def populationSelectionListEntry = new CommunicationPopulationSelectionListEntry()

        // Assert for domain validation
        assertFalse "PopulationSelectionListEntry should have failed null value validation", populationSelectionListEntry.validate()

        // Assert for specific field validation
        assertErrorsFor populationSelectionListEntry, 'nullable',
                [
                        'pidm',
                        'populationSelectionList',
                ]
    }


    @Test
    void testOptimisticLock() {
        setUpData()
        def populationSelectionListEntry = newPopulationSelectionListEntry()
        populationSelectionListEntry.populationSelectionList = globalTestPopulationSelectionList
        populationSelectionListEntry.save(failOnError: true, flush: true)
        assertNotNull populationSelectionListEntry?.id

        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("UPDATE gcrlent SET gcrlent_version = 999 WHERE gcrlent_surrogate_id = ?", [populationSelectionListEntry.id])
        } finally {
            //sql?.close()
        }

        // Update the entity
        populationSelectionListEntry.dataOrigin = "OPT_TEST"
        shouldFail(HibernateOptimisticLockingFailureException) {
            populationSelectionListEntry.save(failOnError: true, flush: true)
        }
    }

    private def newPopulationSelectionListEntry() {
        def populationSelectionListEntry = new CommunicationPopulationSelectionListEntry(
                // Required fields
                pidm: 9199999999999999999,

        )

        return populationSelectionListEntry
    }

}
