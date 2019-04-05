/*********************************************************************************
  Copyright 2010-2013 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
/** *****************************************************************************
 Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.junit.Before
import org.junit.Test
import org.junit.After

import net.hedtech.banner.testing.BaseIntegrationTestCase
import groovy.sql.Sql
import org.springframework.orm.hibernate5.HibernateOptimisticLockingFailureException
import net.hedtech.banner.general.system.Building
import net.hedtech.banner.general.system.BuildingAndRoomAttribute
import org.junit.Ignore
import static groovy.test.GroovyAssert.*


@Integration
@Rollback
class HousingRoomAttributesDefinitionIntegrationTests extends BaseIntegrationTestCase {

    //Test data for creating new domain instance
    //Valid test data (For success tests)
    def i_success_building
    def i_success_buildingAndRoomAttribute

    def i_success_roomNumber = "101"
    def i_success_termEffective = "200410"
    def i_success_mustMatch = "Y"
    //Invalid test data (For failure tests)
    def i_failure_building
    def i_failure_buildingAndRoomAttribute

    def i_failure_roomNumber = "TTTTT"
    def i_failure_termEffective = "TTTTT"
    def i_failure_mustMatch = "N"

    //Test data for creating updating domain instance
    //Valid test data (For success tests)
    def u_success_building
    def u_success_buildingAndRoomAttribute

    def u_success_roomNumber = "201"
    def u_success_termEffective = "YYYYY"
    def u_success_mustMatch = "N"
    //Valid test data (For failure tests)
    def u_failure_building
    def u_failure_buildingAndRoomAttribute

    def u_failure_roomNumber = "TTTTT"
    def u_failure_termEffective = "TTTTT"
    def u_failure_mustMatch = null


    @Before
    public void setUp() {
        formContext = ['GUAGMNU','SSASECT'] // Since we are not testing a controller, we need to explicitly set this
        super.setUp()
        initializeTestDataForReferences()
    }

    //This method is used to initialize test data for references.
    //A method is required to execute database calls as it requires a active transaction
    void initializeTestDataForReferences() {
        //Valid test data (For success tests)
        i_success_building = Building.findWhere(code: "HUM")
        i_success_buildingAndRoomAttribute = BuildingAndRoomAttribute.findWhere(code: "AUD")

        //Invalid test data (For failure tests)
        i_failure_building = Building.findWhere(code: "SOUTH")
        i_failure_buildingAndRoomAttribute = BuildingAndRoomAttribute.findWhere(code: "LAB")

        //Valid test data (For success tests)
        u_success_building = Building.findWhere(code: "BIOL")
        u_success_buildingAndRoomAttribute = BuildingAndRoomAttribute.findWhere(code: "GYM")

        //Valid test data (For failure tests)
        u_failure_building = Building.findWhere(code: "MENDAL")
        u_failure_buildingAndRoomAttribute = BuildingAndRoomAttribute.findWhere(code: "GCL")
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testCreateValidHousingRoomAttributesDefinition() {
        def housingRoomAttributesDefinition = newValidForCreateHousingRoomAttributesDefinition()
        housingRoomAttributesDefinition.save(flush: true, failOnError: true)
        //Test if the generated entity now has an id assigned
        assertNotNull housingRoomAttributesDefinition.id
        assertNotNull housingRoomAttributesDefinition.lastModified
        assertNotNull housingRoomAttributesDefinition.lastModifiedBy
        assertNotNull housingRoomAttributesDefinition.dataOrigin
    }

    @Ignore
    @Test
    void testUpdateValidHousingRoomAttributesDefinition() {
        def housingRoomAttributesDefinition = newValidForCreateHousingRoomAttributesDefinition()
        housingRoomAttributesDefinition.save(flush: true, failOnError: true)

        assertNotNull housingRoomAttributesDefinition.id
        assertEquals 0L, housingRoomAttributesDefinition.version
        assertEquals i_success_roomNumber, housingRoomAttributesDefinition.roomNumber
        assertEquals i_success_termEffective, housingRoomAttributesDefinition.termEffective
        assertEquals i_success_mustMatch, housingRoomAttributesDefinition.mustMatch

        //TODO - KMH there is an issue with the dbeu_ext_stu_bgc script which is setting this table up as if it has an API
        //Update the entity
        housingRoomAttributesDefinition.mustMatch = u_success_mustMatch
        housingRoomAttributesDefinition.save(flush: true, failOnError: true)

        //Assert for successful update
        housingRoomAttributesDefinition = HousingRoomAttributesDefinition.get(housingRoomAttributesDefinition.id)
        assertEquals 1L, housingRoomAttributesDefinition?.version
        assertEquals u_success_mustMatch, housingRoomAttributesDefinition.mustMatch
    }


    @Test
    void testOptimisticLock() {
        def housingRoomAttributesDefinition = newValidForCreateHousingRoomAttributesDefinition()
        housingRoomAttributesDefinition.save(flush: true, failOnError: true)

        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("update SLRRDEF set SLRRDEF_VERSION = 999 where SLRRDEF_SURROGATE_ID = ?", [housingRoomAttributesDefinition.id])
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
        //Try to update the entity
        //Update the entity
        housingRoomAttributesDefinition.mustMatch = u_success_mustMatch
        shouldFail(HibernateOptimisticLockingFailureException) {
            housingRoomAttributesDefinition.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testDeleteHousingRoomAttributesDefinition() {
        def housingRoomAttributesDefinition = newValidForCreateHousingRoomAttributesDefinition()
        housingRoomAttributesDefinition.save(flush: true, failOnError: true)
        def id = housingRoomAttributesDefinition.id
        assertNotNull id
        housingRoomAttributesDefinition.delete()
        assertNull HousingRoomAttributesDefinition.get(id)
    }


    @Test
    void testNullValidationFailure() {
        def housingRoomAttributesDefinition = new HousingRoomAttributesDefinition()
        assertFalse "HousingRoomAttributesDefinition should have failed validation", housingRoomAttributesDefinition.validate()
        assertErrorsFor housingRoomAttributesDefinition, 'nullable',
                        [
                        'roomNumber',
                        'termEffective',
                        'building',
                        'buildingAndRoomAttribute'
                        ]
        assertNoErrorsFor housingRoomAttributesDefinition,
                          [
                          'mustMatch'
                          ]
    }


    @Test
    void testMaxSizeValidationFailures() {
        def housingRoomAttributesDefinition = new HousingRoomAttributesDefinition(
                mustMatch: 'XXX',
                termEffective: '1234567',
                roomNumber: '01234567891')
        assertFalse "HousingRoomAttributesDefinition should have failed validation", housingRoomAttributesDefinition.validate()
        assertErrorsFor housingRoomAttributesDefinition, 'maxSize', ['mustMatch', 'termEffective', 'roomNumber']
    }


    @Test
    void testFetchByBuildingRoomNumberAndTermEffective() {
        def housingRoomAttributesDefinition = newValidForCreateHousingRoomAttributesDefinition()
        housingRoomAttributesDefinition.save(flush: true, failOnError: true)
        def lst = HousingRoomAttributesDefinition.fetchByBuildingRoomNumberAndTermEffective(i_success_building.code, i_success_roomNumber, i_success_termEffective)
        assertNotNull lst
        assertTrue "List is not empty", !lst.isEmpty()
        def resultAttributes = []
        for (HousingRoomAttributesDefinition attr: lst) {
            resultAttributes.add(attr.buildingAndRoomAttribute)
        }
        BuildingAndRoomAttribute audBuilding = BuildingAndRoomAttribute.findByCode("AUD")
        assertTrue resultAttributes.contains(audBuilding)
    }


    private def newValidForCreateHousingRoomAttributesDefinition() {
        def housingRoomAttributesDefinition = new HousingRoomAttributesDefinition(
                roomNumber: i_success_roomNumber,
                termEffective: i_success_termEffective,
                mustMatch: i_success_mustMatch,
                building: i_success_building,
                buildingAndRoomAttribute: i_success_buildingAndRoomAttribute
        )
        return housingRoomAttributesDefinition
    }


}
