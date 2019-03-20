/*********************************************************************************
 Copyright 2014-2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.job

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Tests public methods provided by communication job service.
 */
class CommunicationJobServiceIntegrationTests extends BaseIntegrationTestCase {
    def communicationJobService
    def selfServiceBannerAuthenticationProvider

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        def auth = selfServiceBannerAuthenticationProvider.authenticate( new UsernamePasswordAuthenticationToken( 'BCMADMIN', '111111' ) )
        SecurityContextHolder.getContext().setAuthentication( auth )
    }


    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }


    @Test
    void testCreate() {
        // test that a deletion to an unreferenced id will not cause any harm
        def referenceId = UUID.randomUUID().toString()
        assertEquals( 0, CommunicationJob.findAll().size() )

        CommunicationJob job = new CommunicationJob(
            referenceId: referenceId,
            creationDateTime: new Date()
        )
        communicationJobService.create( job )

        assertEquals( 1, CommunicationJob.findAll().size() )
    }


}
