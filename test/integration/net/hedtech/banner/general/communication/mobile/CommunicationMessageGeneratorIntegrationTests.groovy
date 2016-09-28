/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.mobile

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
        formContext = ['GUAGMNU']
        super.setUp()
        def authentication = selfServiceBannerAuthenticationProvider.authenticate( new UsernamePasswordAuthenticationToken( 'BCMADMIN', '111111' ) )
        SecurityContextHolder.getContext().setAuthentication( authentication )

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
        ThirdPartyAccess thirdPartyAccess = ThirdPartyAccess.findByExternalUser( "bbery" )
        assertNotNull( thirdPartyAccess )
        assertNotNull( thirdPartyAccess.pidm )
        assertNotNull( thirdPartyAccess.externalUser )

        // tests a valid pidm set up with an external user
        String externalLoginId = CommunicationMessageGenerator.fetchExternalLoginIdByPidm( thirdPartyAccess.pidm )
        assertNotNull( externalLoginId )
        assertEquals( thirdPartyAccess.externalUser, externalLoginId )

        // tests a valid pidm with no external user set up
        String externalLoginId2 = CommunicationMessageGenerator.fetchExternalLoginIdByPidm( 1252 )
        assertNull( externalLoginId2 )

        // tests if pidm does not exist
        String externalLoginId3 = CommunicationMessageGenerator.fetchExternalLoginIdByPidm( -1 )
        assertNull( externalLoginId3 )
    }

}
