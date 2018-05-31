/*********************************************************************************
 Copyright 2016-2018 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.communication.interaction

import net.hedtech.banner.general.person.PersonIdentificationName
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

class CommunicationInteractionCompositeServiceTests extends BaseIntegrationTestCase {
    CommunicationInteractionCompositeService communicationInteractionCompositeService

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('BCMADMIN', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)
    }


    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }


    @Test
    void testFetchPersonOrNonPersonByNameOrBannerId() {
        Map pagingAndSortParams = [sortColumn:"lastName", sortDirection:"ASC", max:20, offset:0]
        Map found = communicationInteractionCompositeService.fetchPersonOrNonPersonByNameOrBannerId( "EVT00029", pagingAndSortParams )
        assertNotNull( found )
        assertEquals( 1, found.totalCount )
        assertNotNull( found.list )

        PersonIdentificationName identity = found.list.get( 0 )
        assertEquals( "EVT00029", identity.bannerId )
        assertEquals( "C", identity.entityIndicator )
//        assertEquals( "I", identity.changeIndicator )

//        found = communicationInteractionCompositeService.fetchPersonOrNonPersonByNameOrBannerId( "610009613", pagingAndSortParams )
//        assertNotNull( found )
//        assertEquals( 2, found.totalCount )
//        assertEquals( "P", found.list.get( 0 ).entityIndicator )
//        assertEquals( "P", found.list.get( 1 ).entityIndicator )
//        assertNotNull( found.list )
    }

//    @Test
//    void testFetchPersonOrNonPersonByAlternativeBannerId() {
//        PersonIdentificationName found = communicationInteractionCompositeService.fetchPersonOrNonPersonByAlternativeBannerId( "500000045" )
//        assertNotNull( found )
//        assertEquals( "500000045", found.bannerId )
//        assertEquals( "C", found.entityIndicator )
//        assertEquals( "I", found.changeIndicator )
//    }

    @Test
    void testGetPersonOrNonPerson() {
        PersonIdentificationName found = communicationInteractionCompositeService.getPersonOrNonPerson( "EVT00029" )
        assertNotNull( found )
        assertEquals( "EVT00029", found.bannerId )
        assertEquals( "C", found.entityIndicator )
        assertNull( found.changeIndicator )

        found = communicationInteractionCompositeService.getPersonOrNonPerson( "HOSWEB003" )
        assertNotNull( found )
        assertEquals( "P", found.entityIndicator )
        assertNull( found.changeIndicator )
    }

}
