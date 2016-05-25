/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/

package net.hedtech.banner.general.communication.population.query

import net.hedtech.banner.general.communication.CommunicationManagementTestingSupport
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQuery
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQueryView
import net.hedtech.banner.security.BannerUser
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.hibernate.FlushMode
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

class CommunicationPopulationQueryExtractStatementIntegrationTests extends BaseIntegrationTestCase {
    def bannerUser
    def selfServiceBannerAuthenticationProvider

    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('BCMADMIN', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)
        super.setUp()
        bannerUser = SecurityContextHolder?.context?.authentication?.principal as BannerUser;
    }

    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }

    @Test
    void testConstruction() {
        CommunicationPopulationQueryExtractStatement extractStatement = new CommunicationPopulationQueryExtractStatement()
    }


}
