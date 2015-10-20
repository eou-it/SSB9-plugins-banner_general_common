
/*******************************************************************************
Copyright 2015 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.general.lettergeneration


import net.hedtech.banner.testing.BaseIntegrationTestCase
import net.hedtech.banner.exceptions.ApplicationException
import org.junit.After
import org.junit.Before
import org.junit.Test


class PopulationSelectionBaseServiceIntegrationTests extends BaseIntegrationTestCase {

  def populationSelectionBaseService

	//Test data for creating new domain instance
	//Valid test data (For success tests)
	def i_success_application = "TTTTT"
	def i_success_selection = "TTTTT"
	def i_success_creatorId = "TTTTT"
	def i_success_description = "TTTTT"
	def i_success_lockIndicator =  true
	def i_success_typeIndicator = "M"


	//Invalid test data (For failure tests)
	def i_failure_application = "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT"
	def i_failure_selection = "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT"
	def i_failure_creatorId = "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT"
	def i_failure_description = "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT"
	def i_failure_lockIndicator =  true
	def i_failure_typeIndicator = "M"


	//Test data for creating updating domain instance
	//Valid test data (For success tests)
	def u_success_application = "TTTAAATT"
	def u_success_selection = "TTAAATTT"
	def u_success_creatorId = "TTTAAAATT"
	def u_success_description = "TTTTT"
	def u_success_lockIndicator =  true
	def u_success_typeIndicator = "M"


	//Valid test data (For failure tests)
	def u_failure_description = "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT"
	def u_failure_lockIndicator =  true
	def u_failure_typeIndicator = "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT"


    @Before
	public void setUp() {
		formContext = ['GUAGMNU']
		super.setUp()
	}


    @After
	public void tearDown() {
		super.tearDown()
	}


    @Test
	void testLetterGenerationPopulationSelectionBaseValidCreate() {
		def letterGenerationPopulationSelectionBase = newValidForCreateLetterGenerationPopulationSelectionBase()
		def map = [domainModel: letterGenerationPopulationSelectionBase]
        letterGenerationPopulationSelectionBase = populationSelectionBaseService.create(map)
		assertNotNull "LetterGenerationPopulationSelectionBase ID is null in LetterGenerationPopulationSelectionBase Service Tests Create", letterGenerationPopulationSelectionBase.id
	    assertNotNull letterGenerationPopulationSelectionBase.version
	    assertNotNull letterGenerationPopulationSelectionBase.dataOrigin
		assertNotNull letterGenerationPopulationSelectionBase.lastModifiedBy
	    assertNotNull letterGenerationPopulationSelectionBase.lastModified
    }


    @Test
	void testLetterGenerationPopulationSelectionBaseInvalidCreate() {
		def letterGenerationPopulationSelectionBase = newInvalidForCreateLetterGenerationPopulationSelectionBase()
		def map = [domainModel: letterGenerationPopulationSelectionBase]
		shouldFail(ApplicationException) {
            letterGenerationPopulationSelectionBase = populationSelectionBaseService.create(map)
		}
    }


    @Test
	void testLetterGenerationPopulationSelectionBaseValidUpdate() {
		def letterGenerationPopulationSelectionBase = newValidForCreateLetterGenerationPopulationSelectionBase()
		def map = [domainModel: letterGenerationPopulationSelectionBase]
        letterGenerationPopulationSelectionBase = populationSelectionBaseService.create(map)
		assertNotNull "LetterGenerationPopulationSelectionBase ID is null in LetterGenerationPopulationSelectionBase Service Tests Create", letterGenerationPopulationSelectionBase.id
	    assertNotNull letterGenerationPopulationSelectionBase.version
	    assertNotNull letterGenerationPopulationSelectionBase.dataOrigin
		assertNotNull letterGenerationPopulationSelectionBase.lastModifiedBy
	    assertNotNull letterGenerationPopulationSelectionBase.lastModified
		//Update the entity with new values
		letterGenerationPopulationSelectionBase.description = u_success_description
		letterGenerationPopulationSelectionBase.lockIndicator = u_success_lockIndicator
		letterGenerationPopulationSelectionBase.typeIndicator = u_success_typeIndicator

		map.domainModel = letterGenerationPopulationSelectionBase
		letterGenerationPopulationSelectionBase = populationSelectionBaseService.update(letterGenerationPopulationSelectionBase)
		// test the values
		assertEquals u_success_description, letterGenerationPopulationSelectionBase.description
		assertEquals u_success_lockIndicator, letterGenerationPopulationSelectionBase.lockIndicator
		assertEquals u_success_typeIndicator, letterGenerationPopulationSelectionBase.typeIndicator
	}


    @Test
	void testLetterGenerationPopulationSelectionBaseInvalidUpdate() {
		def letterGenerationPopulationSelectionBase = newValidForCreateLetterGenerationPopulationSelectionBase()
		def map = [domainModel: letterGenerationPopulationSelectionBase]
        letterGenerationPopulationSelectionBase = populationSelectionBaseService.create(map)
		assertNotNull "LetterGenerationPopulationSelectionBase ID is null in LetterGenerationPopulationSelectionBase Service Tests Create", letterGenerationPopulationSelectionBase.id
	    assertNotNull letterGenerationPopulationSelectionBase.version
	    assertNotNull letterGenerationPopulationSelectionBase.dataOrigin
		assertNotNull letterGenerationPopulationSelectionBase.lastModifiedBy
	    assertNotNull letterGenerationPopulationSelectionBase.lastModified
		//Update the entity with new invalid values
		letterGenerationPopulationSelectionBase.description = u_failure_description
		letterGenerationPopulationSelectionBase.lockIndicator = u_failure_lockIndicator
		letterGenerationPopulationSelectionBase.typeIndicator = u_failure_typeIndicator

		map.domainModel = letterGenerationPopulationSelectionBase
		shouldFail(ApplicationException) {
			letterGenerationPopulationSelectionBase = populationSelectionBaseService.update(letterGenerationPopulationSelectionBase)
		}
	}


    @Test
	void testLetterGenerationPopulationSelectionBaseDelete() {
		PopulationSelectionBase populationSelectionBase = newValidForCreateLetterGenerationPopulationSelectionBase()
        populationSelectionBase = populationSelectionBaseService.create(populationSelectionBase)

		def id = populationSelectionBase.id
        assertNotNull id

        populationSelectionBaseService.delete( [domainModel: populationSelectionBase] )
		assertNull "LetterGenerationPopulationSelectionBase should have been deleted", PopulationSelectionBase.get(id)
  	}


    @Test
	void testReadOnly() {
		PopulationSelectionBase letterGenerationPopulationSelectionBase = newValidForCreateLetterGenerationPopulationSelectionBase()
		def map = [domainModel: letterGenerationPopulationSelectionBase]
        letterGenerationPopulationSelectionBase = populationSelectionBaseService.create(map)
		assertNotNull "LetterGenerationPopulationSelectionBase ID is null in LetterGenerationPopulationSelectionBase Service Tests Create", letterGenerationPopulationSelectionBase.id
        letterGenerationPopulationSelectionBase.application = u_success_application
        letterGenerationPopulationSelectionBase.selection = u_success_selection
        letterGenerationPopulationSelectionBase.creatorId = u_success_creatorId
        shouldFail(ApplicationException) {
      			letterGenerationPopulationSelectionBase = populationSelectionBaseService.update(letterGenerationPopulationSelectionBase)
      	}
	}


	private def newValidForCreateLetterGenerationPopulationSelectionBase() {
		def letterGenerationPopulationSelectionBase = new PopulationSelectionBase(
			application: i_success_application,
			selection: i_success_selection,
			creatorId: i_success_creatorId,
			description: i_success_description,
			lockIndicator: i_success_lockIndicator,
			typeIndicator: i_success_typeIndicator,

	    )
		return letterGenerationPopulationSelectionBase
	}


	private def newInvalidForCreateLetterGenerationPopulationSelectionBase() {
		def letterGenerationPopulationSelectionBase = new PopulationSelectionBase(
			application: i_failure_application,
			selection: i_failure_selection,
			creatorId: i_failure_creatorId,
			description: i_failure_description,
			lockIndicator: i_failure_lockIndicator,
			typeIndicator: i_failure_typeIndicator,

		)
		return letterGenerationPopulationSelectionBase
	}
}
