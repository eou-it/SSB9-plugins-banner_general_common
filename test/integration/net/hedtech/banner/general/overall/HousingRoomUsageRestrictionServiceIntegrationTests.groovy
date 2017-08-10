
/*********************************************************************************
  Copyright 2010-2017 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall
import org.junit.Before
import org.junit.Test
import org.junit.After
import net.hedtech.banner.testing.BaseIntegrationTestCase
import net.hedtech.banner.general.system.Building



class HousingRoomUsageRestrictionServiceIntegrationTests extends BaseIntegrationTestCase {

  def housingRoomUsageRestrictionService

	//Test data for creating new domain instance
	//Valid test data (For success tests)
    def i_success_building
	def i_success_roomNumber = "TTTTT"
	def i_success_startDate =  new Date()
	def i_success_endDate =  new Date()
	def i_success_beginTime = "TTTT"
	def i_success_endTime = "TTTT"
	def i_success_sunday = "#"
	def i_success_monday = "#"
	def i_success_tuesday = "#"
	def i_success_wednesday = "#"
	def i_success_thursday = "#"
	def i_success_friday = "#"
	def i_success_saturday = "#"


	//Invalid test data (For failure tests)
    def i_failure_building
	def i_failure_roomNumber = null
	def i_failure_startDate =  new Date()
	def i_failure_endDate =  new Date()
	def i_failure_beginTime = "TTTT"
	def i_failure_endTime = "TTTT"
	def i_failure_sunday = "#"
	def i_failure_monday = "#"
	def i_failure_tuesday = "#"
	def i_failure_wednesday = "#"
	def i_failure_thursday = "#"
	def i_failure_friday = "#"
	def i_failure_saturday = "#"


	//Test data for creating updating domain instance
	//Valid test data (For success tests)
    def u_success_building
	def u_success_roomNumber = "YYYYY"
	def u_success_startDate =  new Date()
	def u_success_endDate =  new Date()
	def u_success_beginTime = "YYYY"
	def u_success_endTime = "YYYY"
	def u_success_sunday = "#"
	def u_success_monday = "#"
	def u_success_tuesday = "#"
	def u_success_wednesday = "#"
	def u_success_thursday = "#"
	def u_success_friday = "#"
	def u_success_saturday = "#"


	//Valid test data (For failure tests)
    def u_failure_building
	def u_failure_roomNumber = null
	def u_failure_startDate =  new Date()
	def u_failure_endDate =  new Date()
	def u_failure_beginTime = "TTTT"
	def u_failure_endTime = "TTTT"
	def u_failure_sunday = "#"
	def u_failure_monday = "#"
	def u_failure_tuesday = "#"
	def u_failure_wednesday = "#"
	def u_failure_thursday = "#"
	def u_failure_friday = "#"
	def u_failure_saturday = "#"
    //keyblock maps may be null for these tests
	def i_success_keyBlockMap = [:]
    def i_failure_keyBlockMap = [:]
	def u_success_keyBlockMap = [:]
	def u_failure_keyBlockMap = [:]


    @Before
    public void setUp() {
		formContext = ['SSASECT']
		super.setUp()
        initializeTestDataForReferences()
	}

	//This method is used to initialize test data for references.
	//A method is required to execute database calls as it requires a active transaction
	void initializeTestDataForReferences() {
		//Valid test data (For success tests)
    	i_success_building = Building.findWhere(code:"NORTH")

		//Invalid test data (For failure tests)
	    i_failure_building = Building.findWhere(code:"SOUTH")

		//Valid test data (For success tests)
	    u_success_building = Building.findWhere(code:"EAST")

		//Valid test data (For failure tests)
    	u_failure_building = Building.findWhere(code:"MENDAL")

		//Test data for references for custom tests
	}


    @After
    public void tearDown() {
		super.tearDown()
	}


    @Test
	void testHousingRoomUsageRestrictionValidCreate() {
		def housingRoomUsageRestriction = newValidForCreateHousingRoomUsageRestriction()
		def map = [keyBlock: i_success_keyBlockMap,
			domainModel: housingRoomUsageRestriction]
		housingRoomUsageRestriction = housingRoomUsageRestrictionService.create(map)
		assertNotNull "HousingRoomUsageRestriction ID is null in HousingRoomUsageRestriction Service Tests Create", housingRoomUsageRestriction.id
		assertNotNull "HousingRoomUsageRestriction building is null in HousingRoomUsageRestriction Service Tests", housingRoomUsageRestriction.building
	    assertNotNull housingRoomUsageRestriction.version
	    assertNotNull housingRoomUsageRestriction.dataOrigin
		assertNotNull housingRoomUsageRestriction.lastModifiedBy
	    assertNotNull housingRoomUsageRestriction.lastModified
    }


    @Test
	void testHousingRoomUsageRestrictionValidUpdate() {
		def housingRoomUsageRestriction = newValidForCreateHousingRoomUsageRestriction()
		def map = [keyBlock: i_success_keyBlockMap,
			domainModel: housingRoomUsageRestriction]
		housingRoomUsageRestriction = housingRoomUsageRestrictionService.create(map)
		assertNotNull "HousingRoomUsageRestriction ID is null in HousingRoomUsageRestriction Service Tests Create", housingRoomUsageRestriction.id
		assertNotNull "HousingRoomUsageRestriction building is null in HousingRoomUsageRestriction Service Tests", housingRoomUsageRestriction.building
	    assertNotNull housingRoomUsageRestriction.version
	    assertNotNull housingRoomUsageRestriction.dataOrigin
		assertNotNull housingRoomUsageRestriction.lastModifiedBy
	    assertNotNull housingRoomUsageRestriction.lastModified
		//Update the entity with new values
		housingRoomUsageRestriction.building = u_success_building
		housingRoomUsageRestriction.roomNumber = u_success_roomNumber
		housingRoomUsageRestriction.startDate = u_success_startDate
		housingRoomUsageRestriction.endDate = u_success_endDate
		housingRoomUsageRestriction.beginTime = u_success_beginTime
		housingRoomUsageRestriction.endTime = u_success_endTime
		housingRoomUsageRestriction.sunday = u_success_sunday
		housingRoomUsageRestriction.monday = u_success_monday
		housingRoomUsageRestriction.tuesday = u_success_tuesday
		housingRoomUsageRestriction.wednesday = u_success_wednesday
		housingRoomUsageRestriction.thursday = u_success_thursday
		housingRoomUsageRestriction.friday = u_success_friday
		housingRoomUsageRestriction.saturday = u_success_saturday

		map.keyBlock = u_success_keyBlockMap
		map.domainModel = housingRoomUsageRestriction
		housingRoomUsageRestriction = housingRoomUsageRestrictionService.update(map)
		// test the values
		assertEquals u_success_roomNumber, housingRoomUsageRestriction.roomNumber
		assertEquals u_success_startDate, housingRoomUsageRestriction.startDate
		assertEquals u_success_endDate, housingRoomUsageRestriction.endDate
		assertEquals u_success_beginTime, housingRoomUsageRestriction.beginTime
		assertEquals u_success_endTime, housingRoomUsageRestriction.endTime
		assertEquals u_success_sunday, housingRoomUsageRestriction.sunday
		assertEquals u_success_monday, housingRoomUsageRestriction.monday
		assertEquals u_success_tuesday, housingRoomUsageRestriction.tuesday
		assertEquals u_success_wednesday, housingRoomUsageRestriction.wednesday
		assertEquals u_success_thursday, housingRoomUsageRestriction.thursday
		assertEquals u_success_friday, housingRoomUsageRestriction.friday
		assertEquals u_success_saturday, housingRoomUsageRestriction.saturday
		assertEquals u_success_building, housingRoomUsageRestriction.building
	}


    @Test
	void testHousingRoomUsageRestrictionDelete() {
		def housingRoomUsageRestriction = newValidForCreateHousingRoomUsageRestriction()
		def map = [keyBlock: i_success_keyBlockMap,
			domainModel: housingRoomUsageRestriction]
		housingRoomUsageRestriction = housingRoomUsageRestrictionService.create(map)
		assertNotNull "HousingRoomUsageRestriction ID is null in HousingRoomUsageRestriction Service Tests Create", housingRoomUsageRestriction.id
		def id = housingRoomUsageRestriction.id
        map = [keyBlock: i_success_keyBlockMap,
			domainModel: housingRoomUsageRestriction]
		housingRoomUsageRestrictionService.delete( map )
		assertNull "HousingRoomUsageRestriction should have been deleted", housingRoomUsageRestriction.get(id)
  	}


	private def newValidForCreateHousingRoomUsageRestriction() {
		def housingRoomUsageRestriction = new HousingRoomUsageRestriction(
			roomNumber: i_success_roomNumber,
			startDate: i_success_startDate,
			endDate: i_success_endDate,
			beginTime: i_success_beginTime,
			endTime: i_success_endTime,
			sunday: i_success_sunday,
			monday: i_success_monday,
			tuesday: i_success_tuesday,
			wednesday: i_success_wednesday,
			thursday: i_success_thursday,
			friday: i_success_friday,
			saturday: i_success_saturday,
			building: i_success_building
	    )
		return housingRoomUsageRestriction
	}

}
