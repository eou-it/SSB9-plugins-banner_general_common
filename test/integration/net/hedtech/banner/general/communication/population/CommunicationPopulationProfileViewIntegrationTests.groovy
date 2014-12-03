/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import groovy.sql.Sql
import net.hedtech.banner.general.communication.CommunicationManagementTestingSupport
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Integration tests for PopulationSelectionListEntry entity
 */
class CommunicationPopulationProfileViewIntegrationTests extends BaseIntegrationTestCase {
    def CommunicationPopulationQuery globalTestPopulationQuery
    def CommunicationPopulationSelectionList globalTestPopulationSelectionList
    def Long globalPidm
    def selfServiceBannerAuthenticationProvider


    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']
        super.setUp()
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('BCMADMIN', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)

        newPerson()
        globalTestPopulationQuery = newPopulationQuery().save(failOnError: true, flush: true)
        globalTestPopulationSelectionList = newPopulationSelectionList()
        globalTestPopulationSelectionList.populationQueryId = globalTestPopulationQuery.id
        globalTestPopulationSelectionList = globalTestPopulationSelectionList.save(failOnError: true, flush: true)
        assertNotNull(globalTestPopulationSelectionList.id)
    }


    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }


    @Test
    void testCreatePopulationSelectionListEntry() {

        def populationSelectionListEntry = newPopulationSelectionListEntry()
        populationSelectionListEntry.populationSelectionList = globalTestPopulationSelectionList
        populationSelectionListEntry = populationSelectionListEntry.save(failOnError: true, flush: true)
        // Assert domain values
        assertNotNull populationSelectionListEntry?.id
        assertEquals globalPidm, populationSelectionListEntry?.pidm
        assertEquals globalTestPopulationSelectionList, populationSelectionListEntry.populationSelectionList

        def listView = CommunicationPopulationProfileView.findAllByPopulationId(globalTestPopulationSelectionList.id)
        assertNotNull(listView)
        assertTrue listView.size() >= 1

        def querycount = CommunicationPopulationProfileView.countByFilterParams([params: [populationId: globalTestPopulationSelectionList.id]])
        assertTrue querycount > 0

        def listView1 = CommunicationPopulationProfileView.findByFilterPagingParams([params: [populationId: new Long(globalTestPopulationSelectionList.id)]],
                [sortColumn: "lastName", sortDirection: "asc", max: 20, offset: 0])
        assertNotNull listView1
        assertTrue listView1.size() == 1

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


    private def newPopulationQuery() {
        def populationQuery = new CommunicationPopulationQuery(
                // Required fields
                folder: CommunicationManagementTestingSupport.newValidForCreateFolderWithSave(),
                createDate: new Date(),
                createdBy: "TTTTTTTTTT",
                name: "TTTTTTTTTT",
                valid: true,

                // Nullable fields
                description: "TTTTTTTTTT",
                sqlString: "",
        )

        return populationQuery
    }


    private def newPopulationSelectionList() {
        def populationSelectionList = new CommunicationPopulationSelectionList(
                // Required fields
                // Nullable fields
                lastCalculatedBy: "TTTTTTTTTT",
                lastCalculatedTime: new Date(),
                status: CommunicationPopulationQueryExecutionStatus.PENDING_EXECUTION,
        )

        return populationSelectionList
    }

}
