/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.folder

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * FolderTest.
 */
class CommunicationFolderIntegrationTests extends BaseIntegrationTestCase {

    def i_valid_name = "My Folder"
    def i_valid_description = "My Folder"
    def i_valid_internal = true

    def u_valid_name = "My Folder1"
    def u_valid_description = "My Folder1"
    def u_valid_internal = false

    def i_invalid_name = "My Folder".padLeft(1021)
    def i_invalid_description = "My Folder".padLeft(4001)
    def i_invalid_internal = null


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
    void testCreateValidFolder() {
        def folder = newValidForCreateFolder()
        folder.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull folder.id
        assertFalse folder.systemIndicator
    }


    @Test
    void testDelete() {
        def folder = newValidForCreateFolder()
        folder.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull folder.id
        folder.delete()
        def id = folder.id
        assertNull folder.get(id)
    }


    @Test
    void testUpdate() {
        def folder = newValidForCreateFolder()
        folder.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull folder.id
        folder.description = u_valid_description
        folder.internal = u_valid_internal
        folder.name = u_valid_name

        folder.save()
        def id = folder.id
        def updatedFolder = folder.get(id)
        assertEquals("Updated description", u_valid_description, folder.description)
        assertEquals("Updated internal", u_valid_internal, folder.internal)
        assertEquals("Updated name", u_valid_name, folder.name)
        assertFalse folder.systemIndicator
    }


    @Test
    void testCreateInValidFolder() {
        def folder = newValidForCreateFolder()

        folder = newValidForCreateFolder()
        folder.description = i_invalid_description
        shouldFail { folder.save(failOnError: true, flush: true) }

        folder = newValidForCreateFolder()
        folder.internal = i_invalid_internal
        shouldFail { folder.save(failOnError: true, flush: true) }

        folder = newValidForCreateFolder()
        folder.name = i_invalid_name
        shouldFail { folder.save(failOnError: true, flush: true) }
    }

    @Test
    void testExistsAnotherNameFolder() {
        def folder = newValidForCreateFolder()
        folder.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull folder.id

        Boolean falseResult = CommunicationFolder.existsAnotherSameNameFolder(folder.id, folder.name)
        assertFalse(falseResult)

        def folder2 = newValidForCreateFolder()
        folder2.name = "Duplicate Folder"
        folder2.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull folder2.id

        Boolean trueResult = CommunicationFolder.existsAnotherSameNameFolder(folder.id, folder2.name)
        assertTrue(trueResult)
    }

    private def newValidForCreateFolder() {
        def folder = new CommunicationFolder(
                description: i_valid_description,
                internal: i_valid_internal,
                name: i_valid_name
        )
        return folder
    }
}
