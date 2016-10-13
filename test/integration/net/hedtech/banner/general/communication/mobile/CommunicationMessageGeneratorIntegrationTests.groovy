/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.mobile

import groovy.sql.Sql
import net.hedtech.banner.general.communication.job.CommunicationMessageGenerator
import net.hedtech.banner.general.overall.ThirdPartyAccess
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

class CommunicationMessageGeneratorIntegrationTests extends BaseIntegrationTestCase {

    def selfServiceBannerAuthenticationProvider


    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']
        def authentication = selfServiceBannerAuthenticationProvider.authenticate( new UsernamePasswordAuthenticationToken( 'BCMADMIN', '111111' ) )
        SecurityContextHolder.getContext().setAuthentication( authentication )
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }

    /**
     * This test exercises the lookup used by service for translating a population result pidm
     * to a target loginId on the mobile server.
     */
    @Test
    void testFetchExternalLoginIdByPidm() {
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def row = sql.rows("select GOBTPAC_EXTERNAL_USER, GOBTPAC_PIDM from GV_GOBTPAC where GOBTPAC_EXTERNAL_USER is not null and rownum = 1" )[0]
        Long testPidm = row.GOBTPAC_PIDM
        String testExternalUser = row.GOBTPAC_EXTERNAL_USER

        // tests a valid pidm set up with an external user
        String externalLoginId = CommunicationMessageGenerator.fetchExternalLoginIdByPidm( testPidm )
        assertNotNull( externalLoginId )
        assertEquals( testExternalUser, externalLoginId )

        // tests a valid pidm with no external user set up
        row = sql.rows("select GOBTPAC_EXTERNAL_USER, GOBTPAC_PIDM from GV_GOBTPAC where GOBTPAC_EXTERNAL_USER is null and rownum = 1" )[0]
        testPidm = row.GOBTPAC_PIDM
        testExternalUser = row.GOBTPAC_EXTERNAL_USER

        String externalLoginId2 = CommunicationMessageGenerator.fetchExternalLoginIdByPidm( testPidm )
        assertNull( externalLoginId2 )

        // tests if pidm does not exist
        String externalLoginId3 = CommunicationMessageGenerator.fetchExternalLoginIdByPidm( -1 )
        assertNull( externalLoginId3 )
    }

}
