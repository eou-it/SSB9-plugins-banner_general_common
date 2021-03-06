/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import groovy.sql.Sql
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQuery
import net.hedtech.banner.general.communication.population.selectionlist.CommunicationPopulationSelectionList
import net.hedtech.banner.general.communication.population.selectionlist.CommunicationPopulationSelectionListEntry
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
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
class CommunicationPopulationProfileViewIntegrationTests extends BaseIntegrationTestCase {
    def CommunicationPopulationQuery globalTestPopulationQuery
    def CommunicationPopulationSelectionList globalTestPopulationSelectionList
    def Long globalPidm
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
        newPerson()
        globalTestPopulationSelectionList = new CommunicationPopulationSelectionList()
        globalTestPopulationSelectionList = globalTestPopulationSelectionList.save(failOnError: true, flush: true)
    }

    @Test
    void testCreatePopulationSelectionListEntry() {
        setUpData()
        def populationSelectionListEntry = newPopulationSelectionListEntry()
        populationSelectionListEntry.populationSelectionList = globalTestPopulationSelectionList
        populationSelectionListEntry = populationSelectionListEntry.save(failOnError: true, flush: true)
        // Assert domain values
        assertNotNull populationSelectionListEntry?.id
        assertEquals globalPidm, populationSelectionListEntry?.pidm
        assertEquals globalTestPopulationSelectionList, populationSelectionListEntry.populationSelectionList

        def listView = CommunicationPopulationProfileView.findAllBySelectionListId(globalTestPopulationSelectionList.id)
        assertNotNull(listView)
        assertTrue listView.size() >= 1

        def results = CommunicationPopulationProfileView.findByNameWithPagingAndSortParams([params: [selectionListId: globalTestPopulationSelectionList.id, "name":"%"]],[sortColumn: "lastName", sortDirection: "asc", max: 20, offset: 0])
        def querycount = results.getTotalCount()
        assertTrue querycount > 0

        assertNotNull results
        assertTrue results.size() == 1

    }

    private def newPerson() {
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        String idSql = """select gb_common.f_generate_id bannerId, gb_common.f_generate_pidm pidm from dual """
        def bannerValues = sql.firstRow(idSql)
        def ibannerId = bannerValues.bannerId
        def ipidm = bannerValues.pidm
        globalPidm = bannerValues.pidm

        def personSql =
                """insert into spriden (SPRIDEN_PIDM, SPRIDEN_ID, SPRIDEN_LAST_NAME, SPRIDEN_FIRST_NAME, SPRIDEN_MI, SPRIDEN_CHANGE_IND, SPRIDEN_ENTITY_IND,
           SPRIDEN_ACTIVITY_DATE, SPRIDEN_USER, SPRIDEN_ORIGIN, SPRIDEN_SEARCH_LAST_NAME, SPRIDEN_SEARCH_FIRST_NAME, SPRIDEN_SEARCH_MI,
           SPRIDEN_SOUNDEX_LAST_NAME, SPRIDEN_SOUNDEX_FIRST_NAME, SPRIDEN_NTYP_CODE, SPRIDEN_CREATE_USER, SPRIDEN_CREATE_DATE)
           values (""" + ipidm + """,'""" + ibannerId + """', 'TTTTT', 'TTTTT', '', '', 'P', SYSDATE, USER, '', 'TTTTT', 'TTTTT', '', 'J162', 'T520', '', USER, SYSDATE)
        """

        def ps = sql.executeInsert(personSql)

    }

    private def newPopulationSelectionListEntry() {
        def populationSelectionListEntry = new CommunicationPopulationSelectionListEntry(
                // Required fields
                pidm: globalPidm,

        )
        return populationSelectionListEntry
    }

}
