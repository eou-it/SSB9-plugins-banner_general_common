/** *****************************************************************************

 © 2011 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */

package com.sungardhe.banner.general.overall

import com.sungardhe.banner.testing.BaseIntegrationTestCase

import com.sungardhe.banner.general.system.Building
import com.sungardhe.banner.general.system.Campus
import com.sungardhe.banner.general.system.RoomRate
import com.sungardhe.banner.general.system.PhoneRate
import com.sungardhe.banner.general.system.Site
import com.sungardhe.banner.general.system.State
import com.sungardhe.banner.general.system.County
import com.sungardhe.banner.general.system.College
import com.sungardhe.banner.general.system.Department
import com.sungardhe.banner.general.system.Partition
import com.sungardhe.banner.exceptions.ApplicationException



class HousingLocationBuildingDescriptionServiceIntegrationTests extends BaseIntegrationTestCase {

    def housingLocationBuildingDescriptionService


    protected void setUp() {
        formContext = ["SSASECT"]
        super.setUp()
    }


    protected void tearDown() {
        super.tearDown()
    }


    void testHousingLocationBuildingDescriptionCreate() {
        def housingLocationBuildingDescription = newHousingLocationBuildingDescription()

        // 'success test'
        housingLocationBuildingDescription = housingLocationBuildingDescriptionService.create([domainModel: housingLocationBuildingDescription])
        assertNotNull "HousingLocationBuildingDescription ID is null in HousingLocationBuildingDescription Service Tests Create", housingLocationBuildingDescription.id
        assertNotNull housingLocationBuildingDescription.dataOrigin
        assertNotNull housingLocationBuildingDescription.lastModifiedBy
        assertNotNull housingLocationBuildingDescription.lastModified
        assertNotNull "HousingLocationBuildingDescription building is null in HousingLocationBuildingDescription Service Tests", housingLocationBuildingDescription.building
        assertNotNull "HousingLocationBuildingDescription campus is null in HousingLocationBuildingDescription Service Tests", housingLocationBuildingDescription.campus
        assertNotNull "HousingLocationBuildingDescription roomRate is null in HousingLocationBuildingDescription Service Tests", housingLocationBuildingDescription.roomRate
        assertNotNull "HousingLocationBuildingDescription phoneRate is null in HousingLocationBuildingDescription Service Tests", housingLocationBuildingDescription.phoneRate
        assertNotNull "HousingLocationBuildingDescription site is null in HousingLocationBuildingDescription Service Tests", housingLocationBuildingDescription.site
        assertNotNull "HousingLocationBuildingDescription state is null in HousingLocationBuildingDescription Service Tests", housingLocationBuildingDescription.state
        assertNotNull "HousingLocationBuildingDescription county is null in HousingLocationBuildingDescription Service Tests", housingLocationBuildingDescription.county
        assertNotNull "HousingLocationBuildingDescription college is null in HousingLocationBuildingDescription Service Tests", housingLocationBuildingDescription.college
        assertNotNull "HousingLocationBuildingDescription department is null in HousingLocationBuildingDescription Service Tests", housingLocationBuildingDescription.department
        assertNull "HousingLocationBuildingDescription partition is not null in HousingLocationBuildingDescription Service Tests", housingLocationBuildingDescription.partition
    }


    void testUpdate() {
        def housingLocationBuildingDescription = newHousingLocationBuildingDescription()
        def keyBlockMap = [code: "GRANT"]
        def map = [keyBlock: keyBlockMap,
                domainModel: housingLocationBuildingDescription]
        // create one to work with
        def housingLocationBuildingDescriptionUpdate = housingLocationBuildingDescriptionService.create(map)

        def icapacity = 9
        def imaximumCapacity = 9
        def istreetLine1 = "XXXXX"
        def istreetLine2 = "XXXXX"
        def istreetLine3 = "XXXXX"
        def icity = "XXXXX"
        def izip = "XXXXX"
        def iphoneArea = "XXXXX"
        def iphoneNumber = "XXXXX"
        def iphoneExtension = "XXXXX"
        def isex = "M"
        def ikeyNumber = "XXXXX"
        def icountryPhone = "XXXX"
        def ihouseNumber = "XXXXX"
        def istreetLine4 = "XXXXX"
        /**
         * Please use the appropriate finder methods to load the references here
         * This area is being protected to preserve the customization on regeneration
         */
        /*PROTECTED REGION ID(housinglocationbuildingdescription_service_integration_tests_update_test_data_fetch_for_references) ENABLED START*/
        def ibuilding = Building.findByCode("GRANT")
        def icampus = Campus.findByCode("M")
        def iroomRate = RoomRate.findByCode("SGD")
        def iphoneRate = PhoneRate.findByCode("PTYD")
        def isite = Site.findByCode("ONS")
        def istate = State.findByCode("NY")
        def icounty = County.findByCode("180")
        def icollege = College.findByCode("AS")
        def idepartment = Department.findByCode("ENGL")
        def ipartition = Partition.findByCode("AM")
        /*PROTECTED REGION END*/
        // change the values
        housingLocationBuildingDescriptionUpdate.capacity = icapacity
        housingLocationBuildingDescriptionUpdate.maximumCapacity = imaximumCapacity
        housingLocationBuildingDescriptionUpdate.streetLine1 = istreetLine1
        housingLocationBuildingDescriptionUpdate.streetLine2 = istreetLine2
        housingLocationBuildingDescriptionUpdate.streetLine3 = istreetLine3
        housingLocationBuildingDescriptionUpdate.city = icity
        housingLocationBuildingDescriptionUpdate.zip = izip
        housingLocationBuildingDescriptionUpdate.phoneArea = iphoneArea
        housingLocationBuildingDescriptionUpdate.phoneNumber = iphoneNumber
        housingLocationBuildingDescriptionUpdate.phoneExtension = iphoneExtension
        housingLocationBuildingDescriptionUpdate.sex = isex
        housingLocationBuildingDescriptionUpdate.keyNumber = ikeyNumber
        housingLocationBuildingDescriptionUpdate.countryPhone = icountryPhone
        housingLocationBuildingDescriptionUpdate.houseNumber = ihouseNumber
        housingLocationBuildingDescriptionUpdate.streetLine4 = istreetLine4
        housingLocationBuildingDescriptionUpdate.building = ibuilding
        housingLocationBuildingDescriptionUpdate.campus = icampus
        housingLocationBuildingDescriptionUpdate.roomRate = iroomRate
        housingLocationBuildingDescriptionUpdate.phoneRate = iphoneRate
        housingLocationBuildingDescriptionUpdate.site = isite
        housingLocationBuildingDescriptionUpdate.state = istate
        housingLocationBuildingDescriptionUpdate.county = icounty
        housingLocationBuildingDescriptionUpdate.college = icollege
        housingLocationBuildingDescriptionUpdate.department = idepartment
        housingLocationBuildingDescriptionUpdate.partition = ipartition
        housingLocationBuildingDescriptionUpdate = housingLocationBuildingDescriptionService.update([domainModel: housingLocationBuildingDescriptionUpdate])
        // test the values
        assertEquals icapacity, housingLocationBuildingDescriptionUpdate.capacity
        assertEquals imaximumCapacity, housingLocationBuildingDescriptionUpdate.maximumCapacity
        assertEquals istreetLine1, housingLocationBuildingDescriptionUpdate.streetLine1
        assertEquals istreetLine2, housingLocationBuildingDescriptionUpdate.streetLine2
        assertEquals istreetLine3, housingLocationBuildingDescriptionUpdate.streetLine3
        assertEquals icity, housingLocationBuildingDescriptionUpdate.city
        assertEquals izip, housingLocationBuildingDescriptionUpdate.zip
        assertEquals iphoneArea, housingLocationBuildingDescriptionUpdate.phoneArea
        assertEquals iphoneNumber, housingLocationBuildingDescriptionUpdate.phoneNumber
        assertEquals iphoneExtension, housingLocationBuildingDescriptionUpdate.phoneExtension
        assertEquals isex, housingLocationBuildingDescriptionUpdate.sex
        assertEquals ikeyNumber, housingLocationBuildingDescriptionUpdate.keyNumber
        assertEquals icountryPhone, housingLocationBuildingDescriptionUpdate.countryPhone
        assertEquals ihouseNumber, housingLocationBuildingDescriptionUpdate.houseNumber
        assertEquals istreetLine4, housingLocationBuildingDescriptionUpdate.streetLine4
        assertEquals ibuilding, housingLocationBuildingDescriptionUpdate.building
        assertEquals icampus, housingLocationBuildingDescriptionUpdate.campus
        assertEquals iroomRate, housingLocationBuildingDescriptionUpdate.roomRate
        assertEquals iphoneRate, housingLocationBuildingDescriptionUpdate.phoneRate
        assertEquals isite, housingLocationBuildingDescriptionUpdate.site
        assertEquals istate, housingLocationBuildingDescriptionUpdate.state
        assertEquals icounty, housingLocationBuildingDescriptionUpdate.county
        assertEquals icollege, housingLocationBuildingDescriptionUpdate.college
        assertEquals idepartment, housingLocationBuildingDescriptionUpdate.department
        assertEquals ipartition, housingLocationBuildingDescriptionUpdate.partition
    }


    void testHousingLocationBuildingDescriptionDelete() {
        def housingLocationBuildingDescription = newHousingLocationBuildingDescription()
        housingLocationBuildingDescription = housingLocationBuildingDescriptionService.create([domainModel: housingLocationBuildingDescription])

        def id = housingLocationBuildingDescription.id
        def keyBlockMap = [code: "GRANT"]
        housingLocationBuildingDescriptionService.delete(domainModel: housingLocationBuildingDescription, keyBlock: keyBlockMap)

        assertNull "HousingLocationBuildingDescription should have been deleted", housingLocationBuildingDescription.get(id)
    }


    void testReadOnly() {
        def housingLocationBuildingDescription = newHousingLocationBuildingDescription()
        housingLocationBuildingDescription = housingLocationBuildingDescriptionService.create([domainModel: housingLocationBuildingDescription])
        // create new values for the fields
        /**
         * Please use the appropriate finder methods to load the references here
         * This area is being protected to preserve the customization on regeneration
         */
        /*PROTECTED REGION ID(housinglocationbuildingdescription_service_integration_tests_readonly_test_data_fetch_for_references) ENABLED START*/
         def ibuilding = Building.findByCode("AHO")
        /*PROTECTED REGION END*/
        // change the values
        housingLocationBuildingDescription.building = ibuilding
        try {
            housingLocationBuildingDescriptionService.update([domainModel: housingLocationBuildingDescription])
            fail "Should have failed with @@r1:readonlyFieldsCannotBeModified"
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "readonlyFieldsCannotBeModified"
        }
    }


    private def newHousingLocationBuildingDescription() {
        /**
         * Please use the appropriate finder methods to load the references here
         * This area is being protected to preserve the customization on regeneration
         */
        /*PROTECTED REGION ID(housinglocationbuildingdescription_service_integration_tests_data_fetch_for_references) ENABLED START*/
        def ibuilding = Building.findByCode("GRANT")
        def icampus = Campus.findByCode("M")
        def iroomRate = RoomRate.findByCode("STND")
        def iphoneRate = PhoneRate.findByCode("PRVD")
        def isite = Site.findByCode("001")
        def istate = State.findByCode("TX")
        def icounty = County.findByCode("180")
        def icollege = College.findByCode("BU")
        def idepartment = Department.findByCode("HIST")
        def ipartition = null
        /*PROTECTED REGION END*/
        def housingLocationBuildingDescription = new HousingLocationBuildingDescription(
                capacity: 1,
                maximumCapacity: 1,
                streetLine1: "TTTTT",
                streetLine2: "TTTTT",
                streetLine3: "TTTTT",
                city: "TTTTT",
                zip: "TTTTT",
                phoneArea: "TTTTT",
                phoneNumber: "TTTTT",
                phoneExtension: "TTTTT",
                sex: "F",
                keyNumber: "TTTTT",
                countryPhone: "TTTT",
                houseNumber: "TTTTT",
                streetLine4: "TTTTT",

                building: ibuilding,
                campus: icampus,
                roomRate: iroomRate,
                phoneRate: iphoneRate,
                site: isite,
                state: istate,
                county: icounty,
                college: icollege,
                department: idepartment,
                partition: ipartition)
        return housingLocationBuildingDescription
    }

    /**
     * Please put all the custom service tests in this protected section to protect the code
     * from being overwritten on re-generation
     */
    /*PROTECTED REGION ID(housinglocationbuildingdescription_custom_service_integration_test_methods) ENABLED START*/

    /*PROTECTED REGION END*/
}  
