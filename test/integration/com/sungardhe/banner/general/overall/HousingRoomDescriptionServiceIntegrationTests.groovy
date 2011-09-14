/*********************************************************************************
 Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of 
 SunGard Higher Education and its subsidiaries. Any use of this software is limited 
 solely to SunGard Higher Education licensees, and is further subject to the terms 
 and conditions of one or more written license agreements between SunGard Higher 
 Education and the licensee in question. SunGard is either a registered trademark or
 trademark of SunGard Data Systems in the U.S.A. and/or other regions and/or countries.
 Banner and Luminis are either registered trademarks or trademarks of SunGard Higher 
 Education in the U.S.A. and/or other regions and/or countries.
 **********************************************************************************/

package com.sungardhe.banner.general.overall

import com.sungardhe.banner.testing.BaseIntegrationTestCase

import com.sungardhe.banner.general.system.Department
import com.sungardhe.banner.general.system.Partition
import com.sungardhe.banner.general.system.Building
import com.sungardhe.banner.general.system.RoomStatus
import com.sungardhe.banner.general.system.RoomRate
import com.sungardhe.banner.general.system.PhoneRate
import com.sungardhe.banner.general.system.College
import com.sungardhe.banner.exceptions.ApplicationException



class HousingRoomDescriptionServiceIntegrationTests extends BaseIntegrationTestCase {

    def housingRoomDescriptionService


    protected void setUp() {
        formContext = ["SSASECT"]
        super.setUp()
    }


    protected void tearDown() {
        super.tearDown()
    }


    void testHousingRoomDescriptionCreate() {
        def housingRoomDescription = newHousingRoomDescription()

        def keyBlockMap = [termEffective: "201410", building: "LAW", room: "102"]
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
        /**
         * Please use the appropriate finder methods to load the references here
         * This area is being protected to preserve the customization on regeneration
         */
        /*PROTECTED REGION ID(housingroomdescription_service_integration_tests_update_test_data_fetch_for_references) ENABLED START*/
        def idepartment = Department.findByCode("LAW")
        def ipartition = null
        def ibuilding = Building.findByCode("LAW")
        def iroomStatus = RoomStatus.findByCode("AC")
        def iroomRate = null
        def iphoneRate = null
        def icollege = College.findByCode("LW")
        /*PROTECTED REGION END*/
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
        assertEquals imaximumCapacity, housingRoomDescription.maximumCapacity
        assertEquals iutilityRate, housingRoomDescription.utilityRate
        assertEquals iutilityRatePeriod, housingRoomDescription.utilityRatePeriod
        assertEquals iphoneArea, housingRoomDescription.phoneArea
        assertEquals iphoneNumber, housingRoomDescription.phoneNumber
        assertEquals iphoneExtension, housingRoomDescription.phoneExtension
        assertEquals ibenefitCategory, housingRoomDescription.benefitCategory
        assertEquals isex, housingRoomDescription.sex
        assertEquals iroomType, housingRoomDescription.roomType
        assertEquals ipriority, housingRoomDescription.priority
        assertEquals ikeyNumber, housingRoomDescription.keyNumber
        assertEquals iwidth, housingRoomDescription.width
        assertEquals ilength, housingRoomDescription.length
        assertEquals iwidth * ilength, housingRoomDescription.area // calculated by GB_ROOMDEFINITION.p_update procedure
        assertEquals icountryPhone, housingRoomDescription.countryPhone
        assertEquals idepartment, housingRoomDescription.department
        assertEquals ipartition, housingRoomDescription.partition
        assertEquals ibuilding, housingRoomDescription.building
        assertEquals iroomStatus, housingRoomDescription.roomStatus
        assertEquals iroomRate, housingRoomDescription.roomRate
        assertEquals iphoneRate, housingRoomDescription.phoneRate
        assertEquals icollege, housingRoomDescription.college
    }


    void testHousingRoomDescriptionDelete() {
        def housingRoomDescription = newHousingRoomDescription()
        housingRoomDescription = housingRoomDescriptionService.create([domainModel: housingRoomDescription])

        def id = housingRoomDescription.id
        def keyBlockMap = [termEffective: "201410", building: "LAW", room: "102"]
        housingRoomDescriptionService.delete(domainModel: housingRoomDescription, keyBlock: keyBlockMap)

        assertNull "HousingRoomDescription should have been deleted", housingRoomDescription.get(id)
    }


    void testReadOnly() {
        def housingRoomDescription = newHousingRoomDescription()
        housingRoomDescription = housingRoomDescriptionService.create([domainModel: housingRoomDescription])
        // create new values for the fields
        def iroomNumber = "104"
        def itermEffective = "201420"
        /**
         * Please use the appropriate finder methods to load the references here
         * This area is being protected to preserve the customization on regeneration
         */
        /*PROTECTED REGION ID(housingroomdescription_service_integration_tests_readonly_test_data_fetch_for_references) ENABLED START*/
        def ibuilding = Building.findByCode("HUM")
        /*PROTECTED REGION END*/
        // change the values
        housingRoomDescription.roomNumber = iroomNumber
        housingRoomDescription.termEffective = itermEffective
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
        /**
         * Please use the appropriate finder methods to load the references here
         * This area is being protected to preserve the customization on regeneration
         */
        /*PROTECTED REGION ID(housingroomdescription_service_integration_tests_data_fetch_for_references) ENABLED START*/
        def idepartment = Department.findByCode("LAW")
        def ipartition = null
        def ibuilding = Building.findByCode("LAW")
        def iroomStatus = RoomStatus.findByCode("AC")
        def iroomRate = null
        def iphoneRate = null
        def icollege = College.findByCode("LW")
        /*PROTECTED REGION END*/
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

    /**
     * Please put all the custom service tests in this protected section to protect the code
     * from being overwritten on re-generation
     */
    /*PROTECTED REGION ID(housingroomdescription_custom_service_integration_test_methods) ENABLED START*/

    /*PROTECTED REGION END*/
}  
