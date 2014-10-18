/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.folder

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 * Tests crud methods provided by folder service.
 */
class CommunicationFolderServiceIntegrationTests extends BaseIntegrationTestCase {

    def communicationFolderService


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
    void testList() {
        long originalListCount = communicationFolderService.list().size()

        CommunicationFolder folder = new CommunicationFolder()
        folder.name = "test"
        folder.description = "description"
        CommunicationFolder createdFolder = communicationFolderService.create(folder)
        assertNotNull(createdFolder)

        long addedListCount = communicationFolderService.list().size()
        assertEquals(originalListCount + 1, addedListCount)
    }


    @Test
    void testCreate() {
        CommunicationFolder folder = new CommunicationFolder()
        folder.name = "test"
        folder.description = "description"
        CommunicationFolder createdFolder = communicationFolderService.create(folder)
        assertNotNull(createdFolder)
        assertEquals("test", createdFolder.name)
        assertEquals("description", createdFolder.description)
        assertEquals(false, createdFolder.internal)

        CommunicationFolder foundFolder = CommunicationFolder.findByName("test")
        assertEquals(createdFolder, foundFolder)

        CommunicationFolder sameNameFolder = new CommunicationFolder()
        sameNameFolder.name = "test"
        sameNameFolder.description = "another folder with same name"
        try {
            communicationFolderService.create(sameNameFolder)
            Assert.fail "Expected sameNameFolder to fail because of name unique constraint."
        } catch (ApplicationException e) {
            assertTrue(e.getMessage().toString().contains("Validation Error(s) occurred during save()") ||
                    e.getSqlException().toString().contains("ORA-00001: unique constraint (GENERAL.UK2_GCFOLDR) violated"))
        }

    }


    @Test
    void testUpdate() {
        CommunicationFolder folder1 = new CommunicationFolder()
        folder1.name = "folder1"
        folder1 = communicationFolderService.create(folder1)

        folder1 = CommunicationFolder.get(folder1.getId())
        folder1.setName("folder1 changed")
        folder1.setDescription("description changed")
        folder1 = communicationFolderService.update(folder1)

        assertEquals("folder1 changed", folder1.getName())
        assertEquals("description changed", folder1.getDescription())

        CommunicationFolder folder2 = new CommunicationFolder()
        folder2.name = "folder2"
        folder2 = communicationFolderService.create(folder2)

        folder1.name = folder2.name
        try {
            communicationFolderService.update(folder1)
            Assert.fail "Expected sameNameFolder to fail because of name unique constraint."
        } catch (ApplicationException e) {
            assertTrue(e.getMessage().toString().contains("Validation Error(s) occurred during save()") ||
                    e.getSqlException().toString().contains("ORA-00001: unique constraint (GENERAL.UK2_GCFOLDR) violated")
            )
        }
    }


    @Test
    void testDelete() {
        CommunicationFolder folder = new CommunicationFolder();
        folder.name = "test"
        folder.description = "description"
        CommunicationFolder createdFolder = communicationFolderService.create(folder)
        assertNotNull(createdFolder)
        Long id = createdFolder.getId()

        long count = communicationFolderService.list().size()

        communicationFolderService.delete(createdFolder)

        assertEquals(count - 1, communicationFolderService.list().size())

        try {
            assertNull(communicationFolderService.get(id))
            Assert.fail "Expected get by id to fail because does not exist."
        } catch (ApplicationException e) {
            assertEquals("NotFoundException", e.getType())
        }
    }
}