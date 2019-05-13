/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import net.hedtech.banner.general.communication.CommunicationManagementTestingSupport
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQuery
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQueryExecutionStatus
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
class CommunicationPopulationListViewIntegrationTests extends BaseIntegrationTestCase {
    def CommunicationPopulationSelectionList globalTestPopulationSelectionList
    def CommunicationFolder folder
    def i_valid_foldername = "TestFolderName"
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

    void setUpFolderData() {
        folder = CommunicationManagementTestingSupport.newValidForCreateFolder()
        folder.save()
    }

    void setUpPopulationData() {
        globalTestPopulationSelectionList = new CommunicationPopulationSelectionList()
        globalTestPopulationSelectionList.save(failOnError: true, flush: true)
    }

    @Test
    void testCreatePopulationSelectionListEntry() {

        setUpPopulationData()
        def populationSelectionListEntry = newPopulationSelectionListEntry()
        populationSelectionListEntry.populationSelectionList = globalTestPopulationSelectionList
        populationSelectionListEntry.save(failOnError: true, flush: true)
        // Assert domain values
        assertNotNull populationSelectionListEntry?.id
        assertEquals 9199999999999999999, populationSelectionListEntry.pidm
        assertEquals globalTestPopulationSelectionList, populationSelectionListEntry.populationSelectionList

        def listView = CommunicationPopulationSelectionList.fetchById(populationSelectionListEntry.populationSelectionList.id)
        assertNotNull(listView)

    }

    private def newPopulationSelectionListEntry() {
        def populationSelectionListEntry = new CommunicationPopulationSelectionListEntry(
                // Required fields
                pidm: 9199999999999999999,
        )
        return populationSelectionListEntry
    }
}
