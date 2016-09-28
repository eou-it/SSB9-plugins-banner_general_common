/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.overall.ldm

import groovy.sql.Sql
import net.hedtech.banner.general.overall.AddressView
import net.hedtech.banner.general.overall.IntegrationConfiguration
import net.hedtech.banner.general.overall.IntegrationConfigurationService
import net.hedtech.banner.general.overall.SourceBackgroundInstitutionBase
import net.hedtech.banner.general.overall.ldm.v6.AddressV6
import net.hedtech.banner.general.person.PersonAddress
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.general.system.AddressType
import net.hedtech.banner.general.system.County
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

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
    void testValidList(){
        updateGoriccr(PROCESS_CODE, NATION_ISO, 2)
        updateGoriccr(PROCESS_CODE, COUNTRY_DEFAULT_ISO, "US")
        updateSpraddr()
        updateSobsbgi()

        setAcceptHeader("application/vnd.hedtech.integration.v6+json")

        def paginationParams = [:]
        def addresses = addressCompositeService.list(paginationParams)
        assertNotNull addresses
    }

    @Test
    void testGet(){
        updateGoriccr(PROCESS_CODE, NATION_ISO, 2)
        updateGoriccr(PROCESS_CODE, COUNTRY_DEFAULT_ISO, "US")
        updateSpraddr()
        updateSobsbgi()
        setAcceptHeader("application/vnd.hedtech.integration.v6+json")

        def paginationParams = [:]
        def addresses = addressCompositeService.list(paginationParams)
        assertNotNull addresses
        String guid = addresses.get(0).guid
        def address = addressCompositeService.get(guid)
        assertNotNull address
        assertEquals address.guid, guid
    }

    @Test
    void testForCntyTitleWhenBannerCntyCodeAvailableForPersonAddress(){
        String bannerId = "HOSFE2000"
        String seqNo = "1"
        String atypCode = "MA"
        String cntyCode = "044"
        updateGoriccr(PROCESS_CODE, NATION_ISO, 2)
        updateGoriccr(PROCESS_CODE, COUNTRY_DEFAULT_ISO, "US")
        def pidm = PersonUtility.getPerson(bannerId).pidm
        String cntyDesc = County.findByCode(cntyCode).description
        updateSpraddrCntyTitle(pidm, seqNo,atypCode, cntyCode, "test")
        def addresses = AddressView.findAllByAddressTypeCodeAndSequenceNumber(atypCode,seqNo)
        def addr = addresses.find{it.pidmOrCode==pidm.toString()}
        assertNotNull addr
        def addrGuid=addr.id

        setAcceptHeader("application/vnd.hedtech.integration.v6+json")

        def address = addressCompositeService.get(addrGuid)
        assertNotNull address
        assertEquals cntyDesc,address.place.country.subRegion.title

    }

    @Test
    void testForCntyTitleWhenBannerCntyCodeNotAvailableForPersonAddress(){
        String bannerId = "HOSFE2000"
        String seqNo = "1"
        String atypCode = "MA"
        String cntyCode = ""
        updateGoriccr(PROCESS_CODE, NATION_ISO, 2)
        updateGoriccr(PROCESS_CODE, COUNTRY_DEFAULT_ISO, "US")
        def pidm = PersonUtility.getPerson(bannerId).pidm
        updateSpraddrCntyTitle(pidm, seqNo,atypCode, cntyCode, "test")
        def addresses = AddressView.findAllByAddressTypeCodeAndSequenceNumber(atypCode,seqNo)
        def addr = addresses.find{it.pidmOrCode==pidm.toString()}
        assertNotNull addr
        def addrGuid=addr.id

        setAcceptHeader("application/vnd.hedtech.integration.v6+json")

        def address = addressCompositeService.get(addrGuid)
        assertNotNull address
        assertEquals "test",address.place.country.subRegion.title

    }


    @Test
    void testForCntyTitleWhenBannerCntyCodeAvailableForInstitutionAddress(){
        String sbgiCode = "ASPHS1"
        String cntyCode = "044"
        updateGoriccr(PROCESS_CODE, NATION_ISO, 2)
        updateGoriccr(PROCESS_CODE, COUNTRY_DEFAULT_ISO, "US")
        String cntyDesc = County.findByCode(cntyCode).description
        updateSobsbgi()
        updateSobsgbiCntyTitle(sbgiCode, cntyCode, "test")
        def addrGuid =getSobsgbiguid(sbgiCode)

        setAcceptHeader("application/vnd.hedtech.integration.v6+json")

        def address = addressCompositeService.get(addrGuid)
        assertNotNull address
        assertEquals cntyDesc,address.place.country.subRegion.title

    }

    @Test
    void testForCntyTitleWhenBannerCntyCodeNotAvailableForInstitutionAddress(){
        String sbgiCode = "ASPHS1"
        String cntyCode = ""
        updateGoriccr(PROCESS_CODE, NATION_ISO, 2)
        updateGoriccr(PROCESS_CODE, COUNTRY_DEFAULT_ISO, "US")
        updateSobsbgi()
        updateSobsgbiCntyTitle(sbgiCode, cntyCode, "test")
        def addrGuid =getSobsgbiguid(sbgiCode)

        setAcceptHeader("application/vnd.hedtech.integration.v6+json")

        def address = addressCompositeService.get(addrGuid)
        assertNotNull address
        assertEquals "test",address.place.country.subRegion.title

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

    // update all SPRADDR record to check only list, as we need to see only no. of records
    private updateSpraddr() {
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def sqlUpdate = "update spraddr set spraddr_natn_code=null, spraddr_stat_code=null, spraddr_cnty_code=null"
        sql.executeUpdate(sqlUpdate)
        sql.close()
    }

    // update all SOBSBGI record to check only list, as we need to see only no. of records
    private updateSobsbgi() {
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def sqlUpdate = "update sobsbgi set sobsbgi_natn_code=null, sobsbgi_stat_code=null, sobsbgi_cnty_code=null"
        sql.executeUpdate(sqlUpdate)
        sql.close()
    }

    // use sql to update spraddr cnty details
    private updateSpraddrCntyTitle(def pidm, def seqNo,def atypCode, def cntyCode, def cntyTitle) {
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def sqlUpdate = "update spraddr set spraddr_cnty_code = ?,spraddr_cnty_title=? where spraddr_pidm = ? and spraddr_atyp_code = ? and spraddr_seqno=?"
        sql.executeUpdate(sqlUpdate, [cntyCode, cntyTitle, pidm, atypCode,seqNo])
        sql.close()
    }

    // use sql to update sobsbgi cnty details
    private updateSobsgbiCntyTitle(def sbgiCode, def cntyCode, def cntyTitle) {
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def sqlUpdate = "update sobsbgi set sobsbgi_cnty_code = ?,sobsbgi_cnty_title=? where sobsbgi_sbgi_code = ?"
        sql.executeUpdate(sqlUpdate, [cntyCode, cntyTitle, sbgiCode])
        sql.close()
    }

    // use sql to get institution address guid
    private String getSobsgbiguid(def sbgiCode) {
        String guid
        Connection sql
        try {

            sql = sessionFactory.getCurrentSession().connection()
            def sqlQuery = "select sobsbgi_guid from sobsbgi where sobsbgi_sbgi_code = ?"
            PreparedStatement preparedStatement = sql.prepareStatement(sqlQuery)
            preparedStatement.setString(1,sbgiCode)
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                guid = rs.getString("sobsbgi_guid")
            }

        }
        catch(SQLException e){
            throw e
        }
        finally {
            sql?.close()
        }
        return guid
    }


}
