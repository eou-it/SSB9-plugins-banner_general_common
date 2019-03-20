/*********************************************************************************
  Copyright 2010-2016 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/

package net.hedtech.banner.general.overall
import org.junit.Before
import org.junit.Test
import org.junit.After

import net.hedtech.banner.testing.BaseIntegrationTestCase

import net.hedtech.banner.general.system.Department
import net.hedtech.banner.general.system.Building
import net.hedtech.banner.general.system.RoomStatus
import net.hedtech.banner.general.system.College
import net.hedtech.banner.exceptions.ApplicationException



class HousingRoomDescriptionServiceIntegrationTests extends BaseIntegrationTestCase {

    def housingRoomDescriptionService


    @Before
    public void setUp() {
        formContext = ["GUAGMNU","SSASECT"]
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testHousingRoomDescriptionCreate() {
        def housingRoomDescription = newHousingRoomDescription()

        housingRoomDescription = housingRoomDescriptionService.create([domainModel: housingRoomDescription])
        assertNotNull "HousingRoomDescription ID is null in HousingRoomDescription Service Tests Create", housingRoomDescription.id
        assertNotNull housingRoomDescription.lastModified
        assertNotNull "HousingRoomDescription department is null in HousingRoomDescription Service Tests", housingRoomDescription.department
        assertNull "HousingRoomDescription partition is not null in HousingRoomDescription Service Tests", housingRoomDescription.partition
        assertNotNull "HousingRoomDescription building is null in HousingRoomDescription Service Tests", housingRoomDescription.building
        assertNotNull "HousingRoomDescription roomStatus is null in HousingRoomDescription Service Tests", housingRoomDescription.roomStatus
        assertNull "HousingRoomDescription roomRate is not null in HousingRoomDescription Service Tests", housingRoomDescription.roomRate
        assertNull "HousingRoomDescription phoneRate is not null in HousingRoomDescription Service Tests", housingRoomDescription.phoneRate
        assertNotNull "HousingRoomDescription college is null in HousingRoomDescription Service Tests", housingRoomDescription.college
    }


    @Test
    void testUpdate() {
        def housingRoomDescription = newHousingRoomDescription()
        def keyBlockMap = [termEffective: 201410, building: "LAW", room: "102"]
        def map = [keyBlock: keyBlockMap,
                domainModel: housingRoomDescription]
        housingRoomDescription = housingRoomDescriptionService.create(map)

        def iroomNumber = "102"
        def itermEffective = "201410"
        def idescription = "Law Updated"
        def icapacity = 10
        def imaximumCapacity = 100
        def iutilityRate = 9
        def iutilityRatePeriod = "XX"
        def iphoneArea = "XXXXX"
        def iphoneNumber = "XXXXX"
        def iphoneExtension = "XXXXX"
        def ibenefitCategory = null
        def isex = null
        def iroomType = "C"
        def ipriority = "XXXXX"
        def ikeyNumber = "XXXXX"
        def iwidth = 9
        def ilength = 9
        def iarea = 9
        def icountryPhone = "XXXX"
        def idepartment = Department.findByCode("LAW")
        def ipartition = null
        def ibuilding = Building.findByCode("LAW")
        def iroomStatus = RoomStatus.findByCode("AC")
        def iroomRate = null
        def iphoneRate = null
        def icollege = College.findByCode("LW")
        // change the values
        housingRoomDescription.roomNumber = iroomNumber
        housingRoomDescription.termEffective = itermEffective
        housingRoomDescription.description = idescription
        housingRoomDescription.capacity = icapacity
        housingRoomDescription.maximumCapacity = imaximumCapacity
        housingRoomDescription.utilityRate = iutilityRate
        housingRoomDescription.utilityRatePeriod = iutilityRatePeriod
        housingRoomDescription.phoneArea = iphoneArea
        housingRoomDescription.phoneNumber = iphoneNumber
        housingRoomDescription.phoneExtension = iphoneExtension
        housingRoomDescription.benefitCategory = ibenefitCategory
        housingRoomDescription.sex = isex
        housingRoomDescription.roomType = iroomType
        housingRoomDescription.priority = ipriority
        housingRoomDescription.keyNumber = ikeyNumber
        housingRoomDescription.width = iwidth
        housingRoomDescription.length = ilength
        housingRoomDescription.area = iarea
        housingRoomDescription.countryPhone = icountryPhone
        housingRoomDescription.department = idepartment
        housingRoomDescription.partition = ipartition
        housingRoomDescription.building = ibuilding
        housingRoomDescription.roomStatus = iroomStatus
        housingRoomDescription.roomRate = iroomRate
        housingRoomDescription.phoneRate = iphoneRate
        housingRoomDescription.college = icollege
        housingRoomDescription = housingRoomDescriptionService.update([domainModel: housingRoomDescription])
        // test the values
        assertEquals iroomNumber, housingRoomDescription.roomNumber
        assertEquals itermEffective, housingRoomDescription.termEffective
        assertEquals idescription, housingRoomDescription.description
        assertEquals icapacity, housingRoomDescription.capacity
        assertEquals imaximumCapacity, housingRoomDescription.maximumCapacity, 0.001
        assertEquals iutilityRate, housingRoomDescription.utilityRate, 0.001
        assertEquals iutilityRatePeriod, housingRoomDescription.utilityRatePeriod
        assertEquals iphoneArea, housingRoomDescription.phoneArea
        assertEquals iphoneNumber, housingRoomDescription.phoneNumber
        assertEquals iphoneExtension, housingRoomDescription.phoneExtension
        assertEquals ibenefitCategory, housingRoomDescription.benefitCategory
        assertEquals isex, housingRoomDescription.sex
        assertEquals iroomType, housingRoomDescription.roomType
        assertEquals ipriority, housingRoomDescription.priority
        assertEquals ikeyNumber, housingRoomDescription.keyNumber
        assertEquals iwidth, housingRoomDescription.width, 0.001
        assertEquals ilength, housingRoomDescription.length, 0.001
        assertEquals iwidth * ilength, housingRoomDescription.area, 0.001 // calculated by GB_ROOMDEFINITION.p_update procedure
        assertEquals icountryPhone, housingRoomDescription.countryPhone
        assertEquals idepartment, housingRoomDescription.department
        assertEquals ipartition, housingRoomDescription.partition
        assertEquals ibuilding, housingRoomDescription.building
        assertEquals iroomStatus, housingRoomDescription.roomStatus
        assertEquals iroomRate, housingRoomDescription.roomRate
        assertEquals iphoneRate, housingRoomDescription.phoneRate
        assertEquals icollege, housingRoomDescription.college
    }


    @Test
    void testHousingRoomDescriptionDelete() {
        def housingRoomDescription = newHousingRoomDescription()
        housingRoomDescription = housingRoomDescriptionService.create([domainModel: housingRoomDescription])

        def id = housingRoomDescription.id
        def keyBlockMap = [termEffective: "201410", building: "LAW", room: "102"]
        housingRoomDescriptionService.delete(domainModel: housingRoomDescription, keyBlock: keyBlockMap)

        assertNull "HousingRoomDescription should have been deleted", housingRoomDescription.get(id)
    }


    @Test
    void testReadOnlyRoomNumber() {
        def housingRoomDescription = newHousingRoomDescription()
        housingRoomDescription = housingRoomDescriptionService.create([domainModel: housingRoomDescription])
        def iroomNumber = "104"
        // change the values
        housingRoomDescription.roomNumber = iroomNumber
        try {
            housingRoomDescriptionService.update([domainModel: housingRoomDescription])
            fail "Should have failed with @@r1:readonlyFieldsCannotBeModified"
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "readonlyFieldsCannotBeModified"
        }
    }


    @Test
    void testReadOnlyTermEffective() {
        def housingRoomDescription = newHousingRoomDescription()
        housingRoomDescription = housingRoomDescriptionService.create([domainModel: housingRoomDescription])
        def itermEffective = "201420"
        // change the values
        housingRoomDescription.termEffective = itermEffective
        try {
            housingRoomDescriptionService.update([domainModel: housingRoomDescription])
            fail "Should have failed with @@r1:readonlyFieldsCannotBeModified"
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "readonlyFieldsCannotBeModified"
        }
    }


    @Test
    void testReadOnlyBuilding() {
        def housingRoomDescription = newHousingRoomDescription()
        housingRoomDescription = housingRoomDescriptionService.create([domainModel: housingRoomDescription])
        def ibuilding = Building.findByCode("HUM")
        // change the values
        housingRoomDescription.building = ibuilding
        try {
            housingRoomDescriptionService.update([domainModel: housingRoomDescription])
            fail "Should have failed with @@r1:readonlyFieldsCannotBeModified"
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "readonlyFieldsCannotBeModified"
        }
    }


    private def newHousingRoomDescription() {
        def idepartment = Department.findByCode("LAW")
        def ipartition = null
        def ibuilding = Building.findByCode("LAW")
        def iroomStatus = RoomStatus.findByCode("AC")
        def iroomRate = null
        def iphoneRate = null
        def icollege = College.findByCode("LW")
        def housingRoomDescription = new HousingRoomDescription(
                roomNumber: "102",
                termEffective: "201410",
                description: "TTTTT",
                capacity: 1,
                maximumCapacity: 1,
                utilityRate: 1,
                utilityRatePeriod: "TT",
                phoneArea: "TTTTT",
                phoneNumber: "TTTTT",
                phoneExtension: "TTTTT",
                benefitCategory: null,
                sex: null,
                roomType: "C",
                priority: "TTTTT",
                keyNumber: "TTTTT",
                width: 1, length: 1, area: 1, countryPhone: "TTTT",

                department: idepartment,
                partition: ipartition,
                building: ibuilding,
                roomStatus: iroomStatus,
                roomRate: iroomRate,
                phoneRate: iphoneRate,
                college: icollege)
        return housingRoomDescription
    }
}
