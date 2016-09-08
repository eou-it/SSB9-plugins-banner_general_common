/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/

package net.hedtech.banner.general.communication.letter

import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQueryExtractStatement
import net.hedtech.banner.security.BannerUser
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

class CommunicationLetterPageSettingsIntegrationTests extends BaseIntegrationTestCase {
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
        CommunicationLetterPageSettings extractStatement = new CommunicationLetterPageSettings()

        String json = extractStatement.toJson()
        assertNotNull( json )

        extractStatement.setStyle( "{ \"unitOfMeasure\":\"INCH\", \"topMargin\": \"1\", \"leftMargin\":\"1\", \"bottomMargin\": \"0.5\", \"rightMargin\": \"2\", \"pageSize\":\"LETTER\" }" )
        extractStatement.validate()
    }


}
