/********************************************************************************
  Copyright 2016 Ellucian Company L.P. and its affiliates.
********************************************************************************/
package net.hedtech.banner.general.lettergeneration
import net.hedtech.banner.testing.BaseIntegrationTestCase
import net.hedtech.banner.exceptions.ApplicationException
import org.junit.After
import org.junit.Before
import org.junit.Test


class PopulationSelectionExtractServiceIntegrationTests extends BaseIntegrationTestCase {

    def populationSelectionExtractService

    //Test data for creating new domain instance
    //Valid test data (For success tests)
    def i_success_application = "GENERAL"
    def i_success_selection = "ALL"
    def i_success_creatorId = "BANNER"
    def i_success_key = "TTTTT"
    def i_success_systemIndicator = "M"
    def i_success_selectIndicator = "Y"


    //Invalid test data (For failure tests)
    def i_failure_application = "TTTTT"
    def i_failure_selection = "TTTTT"
    def i_failure_creatorId = "TTTTT"
    def i_failure_key = "TTTTT"
    def i_failure_systemIndicator = "MT"
    def i_failure_selectIndicator = "Y"


    //Test data for creating updating domain instance
    //Valid test data (For success tests)
    def u_success_application = "TTTTT"
    def u_success_selection = "TTTTT"
    def u_success_creatorId = "TTTTT"
    def u_success_key = "TTTTT"
    def u_success_systemIndicator = "S"
    def u_success_selectIndicator = "Y"
    def u_success_lastModifiedBy = "TTTTT"


    //Valid test data (For failure tests)
    def u_failure_application = "TTTTT"
    def u_failure_selection = "TTTTT"
    def u_failure_creatorId = "TTTTT"
    def u_failure_key = "TTTTT"
    def u_failure_systemIndicator = "MT"
    def u_failure_selectIndicator = "Y"

    //TODO: Create keyblock map for insert (For success tests)
    def i_success_keyBlockMap = [:]

    //TODO: Create keyblock map for insert (For failure tests)
    def i_failure_keyBlockMap = [:]

    //TODO: Create keyblock map for update (If success required)
    def u_success_keyBlockMap = [:]

    //TODO: Create keyblock map for update (If failure required)
    def u_failure_keyBlockMap = [:]

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }

    //This method is used to initialize test data for references.
    //A method is required to execute database calls as it requires a active transaction
    void initializeTestDataForReferences() {
        //Valid test data (For success tests)

        //Invalid test data (For failure tests)

        //Valid test data (For success tests)

        //Valid test data (For failure tests)

        //Test data for references for custom tests

    }

    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    void testLetterGenerationPopulationSelectionExtractValidCreate() {
        def letterGenerationPopulationSelectionExtract = newValidForCreateLetterGenerationPopulationSelectionExtract()
        def map = [keyBlock: i_success_keyBlockMap,
                   domainModel: letterGenerationPopulationSelectionExtract]
        letterGenerationPopulationSelectionExtract=populationSelectionExtractService.create(map)
        assertNotNull "LetterGenerationPopulationSelectionExtract ID is null in LetterGenerationPopulationSelectionExtract Service Tests Create", letterGenerationPopulationSelectionExtract.id
        assertNotNull letterGenerationPopulationSelectionExtract.version
        assertNotNull letterGenerationPopulationSelectionExtract.dataOrigin
        assertNotNull letterGenerationPopulationSelectionExtract.lastModifiedBy
        assertNotNull letterGenerationPopulationSelectionExtract.lastModified
    }

    @Test
    void testLetterGenerationPopulationSelectionExtractInvalidCreate() {
        def letterGenerationPopulationSelectionExtract = newInvalidForCreateLetterGenerationPopulationSelectionExtract()
        def map = [keyBlock: i_failure_keyBlockMap,
                   domainModel: letterGenerationPopulationSelectionExtract]
        shouldFail(ApplicationException) {
            populationSelectionExtractService.create(map)
        }
    }

    @Test
    void testLetterGenerationPopulationSelectionExtractValidUpdate() {
        def letterGenerationPopulationSelectionExtract = newValidForCreateLetterGenerationPopulationSelectionExtract()
        def map = [keyBlock: i_success_keyBlockMap,
                   domainModel: letterGenerationPopulationSelectionExtract]
        letterGenerationPopulationSelectionExtract=populationSelectionExtractService.create(map)
        assertNotNull "LetterGenerationPopulationSelectionExtract ID is null in LetterGenerationPopulationSelectionExtract Service Tests Create", letterGenerationPopulationSelectionExtract.id
        assertNotNull letterGenerationPopulationSelectionExtract.version
        assertNotNull letterGenerationPopulationSelectionExtract.dataOrigin
        assertNotNull letterGenerationPopulationSelectionExtract.lastModifiedBy
        assertNotNull letterGenerationPopulationSelectionExtract.lastModified
        //Update the entity with new values
        letterGenerationPopulationSelectionExtract.systemIndicator = u_success_systemIndicator
        letterGenerationPopulationSelectionExtract.selectIndicator = u_success_selectIndicator

        map.keyBlock = u_success_keyBlockMap
        map.domainModel = letterGenerationPopulationSelectionExtract
        letterGenerationPopulationSelectionExtract = populationSelectionExtractService.update(map)
        // test the values
        assertEquals u_success_systemIndicator, letterGenerationPopulationSelectionExtract.systemIndicator
        assertEquals u_success_selectIndicator, letterGenerationPopulationSelectionExtract.selectIndicator
    }


    @Test
    void testLetterGenerationPopulationSelectionExtractInvalidUpdate() {
        def letterGenerationPopulationSelectionExtract = newValidForCreateLetterGenerationPopulationSelectionExtract()
        def map = [keyBlock: i_success_keyBlockMap,
                   domainModel: letterGenerationPopulationSelectionExtract]
        letterGenerationPopulationSelectionExtract=populationSelectionExtractService.create(map)
        assertNotNull "LetterGenerationPopulationSelectionExtract ID is null in LetterGenerationPopulationSelectionExtract Service Tests Create", letterGenerationPopulationSelectionExtract.id
        assertNotNull letterGenerationPopulationSelectionExtract.version
        assertNotNull letterGenerationPopulationSelectionExtract.dataOrigin
        assertNotNull letterGenerationPopulationSelectionExtract.lastModifiedBy
        assertNotNull letterGenerationPopulationSelectionExtract.lastModified
        //Update the entity with new invalid values
        letterGenerationPopulationSelectionExtract.systemIndicator = u_failure_systemIndicator
        letterGenerationPopulationSelectionExtract.selectIndicator = u_failure_selectIndicator

        map.keyBlock = u_failure_keyBlockMap
        map.domainModel = letterGenerationPopulationSelectionExtract
        shouldFail(ApplicationException) {
            letterGenerationPopulationSelectionExtract = populationSelectionExtractService.update(map)
        }
    }

    @Test
    void testLetterGenerationPopulationSelectionExtractDelete() {
        def letterGenerationPopulationSelectionExtract = newValidForCreateLetterGenerationPopulationSelectionExtract()
        def map = [keyBlock: i_success_keyBlockMap,
                   domainModel: letterGenerationPopulationSelectionExtract]
        letterGenerationPopulationSelectionExtract=populationSelectionExtractService.create(map)
        assertNotNull "LetterGenerationPopulationSelectionExtract ID is null in LetterGenerationPopulationSelectionExtract Service Tests Create", letterGenerationPopulationSelectionExtract.id
        def id = letterGenerationPopulationSelectionExtract.id
        map = [keyBlock: i_success_keyBlockMap,
                   domainModel: letterGenerationPopulationSelectionExtract]
        populationSelectionExtractService.delete( map )
        assertNull "LetterGenerationPopulationSelectionExtract should have been deleted", letterGenerationPopulationSelectionExtract.get(id)
    }


    @Test
    void testReadOnly() {
        def letterGenerationPopulationSelectionExtract = newValidForCreateLetterGenerationPopulationSelectionExtract()
        def map = [keyBlock: i_success_keyBlockMap,
                   domainModel: letterGenerationPopulationSelectionExtract]
        letterGenerationPopulationSelectionExtract=populationSelectionExtractService.create(map)
        assertNotNull "LetterGenerationPopulationSelectionExtract ID is null in LetterGenerationPopulationSelectionExtract Service Tests Create", letterGenerationPopulationSelectionExtract.id
        map = [keyBlock: i_success_keyBlockMap,
                   domainModel: letterGenerationPopulationSelectionExtract]
        map.domainModel.application = u_success_application
        map.domainModel.selection = u_success_selection
        map.domainModel.creatorId = u_success_creatorId
        map.domainModel.key = u_success_key
        map.domainModel.lastModifiedBy = u_success_lastModifiedBy

        try {
            //letterGenerationPopulationSelectionExtract=populationSelectionExtractService.update([domainModel: letterGenerationPopulationSelectionExtract])
            letterGenerationPopulationSelectionExtract=populationSelectionExtractService.update(map)
            fail("This should have failed with @@r1:readonlyFieldsCannotBeModified")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "readonlyFieldsCannotBeModified"
        }

    }

    private def newValidForCreateLetterGenerationPopulationSelectionExtract() {
        def letterGenerationPopulationSelectionExtract = new PopulationSelectionExtract(
                application: i_success_application,
                selection: i_success_selection,
                creatorId: i_success_creatorId,
                key: i_success_key,
                systemIndicator: i_success_systemIndicator,
                selectIndicator: i_success_selectIndicator,

        )
        return letterGenerationPopulationSelectionExtract
    }

    private def newInvalidForCreateLetterGenerationPopulationSelectionExtract() {
        def letterGenerationPopulationSelectionExtract = new PopulationSelectionExtract(
                application: i_failure_application,
                selection: i_failure_selection,
                creatorId: i_failure_creatorId,
                key: i_failure_key,
                systemIndicator: i_failure_systemIndicator,
                selectIndicator: i_failure_selectIndicator,

        )
        return letterGenerationPopulationSelectionExtract
    }
}
