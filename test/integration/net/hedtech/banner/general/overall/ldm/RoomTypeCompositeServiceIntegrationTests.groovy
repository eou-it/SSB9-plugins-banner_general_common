/*******************************************************************************
 Copyright 2015-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.overall.ldm

import net.hedtech.banner.general.overall.ldm.v4.RoomType
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.Before
import org.junit.Test

class RoomTypeCompositeServiceIntegrationTests extends BaseIntegrationTestCase{

    def roomTypeCompositeService

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }

    @Test
    public void testRoomTypesList(){
        List<RoomType> roomTypes = roomTypeCompositeService.list(params)
        roomTypes.each {
            roomType->
                assertNotNull roomType
                assertEquals roomType?.title,"classroom"
                assertEquals roomType?.type,"classroom"
        }
    }

    @Test
    public void testRoomType(){
        String guid = roomTypeCompositeService.list(params)?.get(0)?.id
        RoomType type = roomTypeCompositeService.get(guid)
        assertNotNull type.toString()
        assertNotNull type
        assertEquals guid,type?.id
        assertEquals type?.title,"classroom"
        assertEquals type?.type,"classroom"
    }

    @Test
    public void testCount(){
        List<RoomType> roomTypes = roomTypeCompositeService.list(params)
        assertEquals roomTypes.size(),roomTypeCompositeService.count()
    }
}
