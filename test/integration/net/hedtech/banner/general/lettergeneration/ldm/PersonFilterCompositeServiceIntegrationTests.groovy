/** *****************************************************************************
 Copyright 2015-2016 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.lettergeneration.ldm

import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.lettergeneration.PopulationSelectionExtract
import net.hedtech.banner.general.lettergeneration.ldm.v2.PersonFilter
import net.hedtech.banner.general.overall.ldm.GlobalUniqueIdentifier
import net.hedtech.banner.general.overall.ldm.LdmService
import net.hedtech.banner.restfulapi.RestfulApiValidationException
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
import org.junit.After
import org.junit.Before
import org.junit.Test


class PersonFilterCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    def personFilterCompositeService
    public static final String LDM_NAME = 'person-filters'
    String application
    String user
    String selection
    String selection1
    String guid
    String key
    Integer pidm_key


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        initiializeDataReferences()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    private void initiializeDataReferences() {
        application = "GENERAL"
        user = "GRAILS_USER"
        selection = "SAT_TEST"
        selection1 = "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
        key = "1"
    }


    @Test
    void testBasicList() {
        List personFiltersList = personFilterCompositeService.list([:])
        assertNotNull personFiltersList
        assertTrue personFiltersList.size() > 0
        assertTrue personFiltersList[0] instanceof PersonFilter
    }


    @Test
    void testBasicCount() {
        def personFilterCount = personFilterCompositeService.count([:])
        assertTrue personFilterCount > 0
    }


    @Test
    void testListWithPagination() {
        List personFiltersList = personFilterCompositeService.list([max: '4', offset: '0'])
        assertNotNull personFiltersList
        assertTrue personFiltersList.size() > 0
        assertEquals 4, personFiltersList.size()
        assertTrue personFiltersList[0] instanceof PersonFilter
    }


    @Test
    void testListWithSort() {
        createPopulationSelectionExtract(application, selection1, user, user, key)
        List personFiltersList = personFilterCompositeService.list([sort: 'code', order: 'desc', 'filter[0][field]': 'code', 'filter[0][operator]': 'contains', 'filter[0][value]': selection1])
        assertNotNull personFiltersList
        assertTrue personFiltersList.size() > 0
        assertTrue personFiltersList[0] instanceof PersonFilter
        assertEquals selection1, personFiltersList[0].abbreviation
    }

    @Test
    void testListWithSortHeader2() {
        //we will forcefully set the accept header so that the tests go through all possible code flows
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Accept", "application/vnd.hedtech.integration.v2+json")
        createPopulationSelectionExtract(application, selection1, user, user, key)
        List personFiltersList = personFilterCompositeService.list([sort: 'abbreviation', order: 'desc', 'filter[0][field]': 'abbreviation', 'filter[0][operator]': 'contains', 'filter[0][value]': selection1])
        assertNotNull personFiltersList
        assertTrue personFiltersList.size() > 0
        assertTrue personFiltersList[0] instanceof PersonFilter
        assertEquals selection1, personFiltersList[0].abbreviation
    }


    @Test
    void testListWithInvalidSortFiled() {
        shouldFail(RestfulApiValidationException) {
            personFilterCompositeService.list([sort: 'title', order: 'desc'])
        }
    }


    @Test
    void testListWithInvalidSortOrder() {
        shouldFail(RestfulApiValidationException) {
            personFilterCompositeService.list([sort: 'abbreviation', order: 'des'])
        }
    }


    @Test
    void testSchemaPropertiesForList() {
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def pidm_key_result = sql.rows("select spriden_pidm from spriden where spriden_id = ? and spriden_change_ind is null", ["HOS00001"])[0]
        assertNotNull pidm_key_result?.spriden_pidm
        pidm_key = pidm_key_result.spriden_pidm.toInteger()

        PopulationSelectionExtract populationSelectionExtract = createPopulationSelectionExtract(application, selection, user, user, pidm_key.toString())
        String domain_key = "${populationSelectionExtract.application}-^${populationSelectionExtract.selection}-^${populationSelectionExtract.creatorId}-^${populationSelectionExtract.lastModifiedBy}"
        GlobalUniqueIdentifier globalUniqueIdentifier = GlobalUniqueIdentifier.findByLdmNameAndDomainKey(LDM_NAME, domain_key)
        assertNotNull globalUniqueIdentifier
        assertNotNull globalUniqueIdentifier.guid
        List<PersonFilter> personFiltersList = personFilterCompositeService.list(['filter[0][field]': 'abbreviation', 'filter[0][operator]': 'contains', 'filter[0][value]': selection])
        assertNotNull personFiltersList
        assertTrue personFiltersList.size() > 0
        assertTrue personFiltersList[0] instanceof PersonFilter

        PersonFilter personFilter = personFiltersList.find {
            it.abbreviation == selection
        }

        assertEquals selection.toUpperCase(), personFilter.abbreviation
        assertEquals "Banner", personFilter.metadata.dataOrigin
        assertEquals globalUniqueIdentifier.guid, personFilter.guid
        assertEquals globalUniqueIdentifier.domainKey, personFilter.title
    }


    @Test
    void testGetInvalidGuid() {
        try {
            personFilterCompositeService.get('Invalid-guid')
        } catch (ApplicationException ae) {
            assertApplicationException ae, "NotFoundException"
        }
    }


    @Test
    void testGetNullGuid() {
        try {
            personFilterCompositeService.get(null)
        } catch (ApplicationException ae) {
            assertApplicationException ae, "NotFoundException"
        }
    }


    @Test
    void testGet() {
        PopulationSelectionExtract populationSelectionExtract = createPopulationSelectionExtract(application, selection, user, user, key)
        String domain_key = "${populationSelectionExtract.application}-^${populationSelectionExtract.selection}-^${populationSelectionExtract.creatorId}-^${populationSelectionExtract.lastModifiedBy}"
        GlobalUniqueIdentifier globalUniqueIdentifier = GlobalUniqueIdentifier.findByLdmNameAndDomainKey(LDM_NAME, domain_key)
        assertNotNull globalUniqueIdentifier
        assertNotNull globalUniqueIdentifier.guid

        def personFilter = personFilterCompositeService.get(globalUniqueIdentifier.guid)
        assertNotNull personFilter.toString()
        assertNotNull personFilter.title
        assertEquals selection, personFilter.abbreviation
        assertEquals globalUniqueIdentifier.guid, personFilter.guid
        assertEquals globalUniqueIdentifier.domainKey, personFilter.title
    }

    @Test
    void testGetHeader2() {
        //we will forcefully set the accept header so that the tests go through all possible code flows
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Accept", "application/vnd.hedtech.integration.v2+json")
        PopulationSelectionExtract populationSelectionExtract = createPopulationSelectionExtract(application, selection, user, user, key)
        String domain_key = "${populationSelectionExtract.application}-^${populationSelectionExtract.selection}-^${populationSelectionExtract.creatorId}-^${populationSelectionExtract.lastModifiedBy}"
        GlobalUniqueIdentifier globalUniqueIdentifier = GlobalUniqueIdentifier.findByLdmNameAndDomainKey(LDM_NAME, domain_key)
        assertNotNull globalUniqueIdentifier
        assertNotNull globalUniqueIdentifier.guid

        def personFilter = personFilterCompositeService.get(globalUniqueIdentifier.guid)
        assertNotNull personFilter.toString()
        assertNotNull personFilter.title
        assertEquals selection, personFilter.abbreviation
        assertEquals "Banner", personFilter.metadata.dataOrigin
        assertEquals globalUniqueIdentifier.guid, personFilter.guid
        assertEquals globalUniqueIdentifier.domainKey, personFilter.title
    }


    private
    def createPopulationSelectionExtract(String application, String selection, String creator, String user, String key) {
        def populationSelectionExtract = new PopulationSelectionExtract(
                application: application,
                selection: selection ,
                creatorId: creator,
                key: key,
                systemIndicator: "S",
                lastModified: new Date(),
                lastModifiedBy: user,
                dataOrigin: "Banner"
        )
        populationSelectionExtract.save(failOnError: true, flush: true)
        assertNotNull populationSelectionExtract.id
        return populationSelectionExtract
    }


}
