/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.overall.ldm

import groovy.sql.Sql
import net.hedtech.banner.general.overall.IntegrationConfiguration
import net.hedtech.banner.general.overall.IntegrationConfigurationService
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

class AddressCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    AddressCompositeService addressCompositeService

    String PROCESS_CODE = IntegrationConfigurationService.PROCESS_CODE
    String NATION_ISO = IntegrationConfigurationService.NATION_ISO
    String COUNTRY_DEFAULT_ISO = IntegrationConfigurationService.COUNTRY_DEFAULT_ISO

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }

    @Test
    @Ignore
    void testValidList(){
        updateGoriccr(PROCESS_CODE, NATION_ISO, 2)
        updateGoriccr(PROCESS_CODE, COUNTRY_DEFAULT_ISO, "US")
        setAcceptHeader("application/vnd.hedtech.integration.v6+json")

        def paginationParams = [:]
        def addresses = addressCompositeService.list(paginationParams)
        assertNotNull addresses
    }

    @Test
    @Ignore
    void testGet(){
        updateGoriccr(PROCESS_CODE, NATION_ISO, 2)
        updateGoriccr(PROCESS_CODE, COUNTRY_DEFAULT_ISO, "US")
        setAcceptHeader("application/vnd.hedtech.integration.v6+json")

        def paginationParams = [:]
        def addresses = addressCompositeService.list(paginationParams)
        assertNotNull addresses
        String guid = addresses.get(0).guid
        def address = addressCompositeService.get(guid)
        assertNotNull address
        assertEquals address.guid, guid
    }


    private void setAcceptHeader(String mediaType) {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Accept", mediaType)
    }


    private void setContentTypeHeader(String mediaType) {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", mediaType)
    }

    // use sql to get around the validation process
    private updateGoriccr(def process, def type, def value) {
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def sqlUpdate = "update goriccr set goriccr_value = ? where goriccr_sqpr_code = ? and goriccr_icsn_code = ?"
        sql.executeUpdate(sqlUpdate, [value, process, type])
        sql.close()
        def intConf = IntegrationConfiguration.findByProcessCodeAndSettingName(process, type)
        intConf.refresh()
    }
}
