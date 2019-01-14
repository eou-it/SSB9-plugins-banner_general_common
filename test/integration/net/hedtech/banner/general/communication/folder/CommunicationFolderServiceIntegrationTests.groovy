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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Tests crud methods provided by folder service.
 */
class CommunicationFolderServiceIntegrationTests extends BaseIntegrationTestCase {

    def communicationFolderService
    def selfServiceBannerAuthenticationProvider


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('BCMADMIN', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)
    }


    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }


    @Test
    void testList() {
        long originalListCount = communicationFolderService.list().size()

        CommunicationFolder folder = new CommunicationFolder()
        folder.name = "test-integration"
        folder.description = "description"
        CommunicationFolder createdFolder = communicationFolderService.create(folder)
        assertNotNull(createdFolder)
        assertFalse(createdFolder.systemIndicator)

        long addedListCount = communicationFolderService.list().size()
        assertEquals(originalListCount + 1, addedListCount)
    }


    @Test
    void testCreate() {
        CommunicationFolder folder = new CommunicationFolder()
        folder.name = "test-integration"
        folder.description = "description"
        CommunicationFolder createdFolder = communicationFolderService.create(folder)
        assertNotNull(createdFolder)
        assertEquals("test-integration", createdFolder.name)
        assertEquals("description", createdFolder.description)
        assertEquals(false, createdFolder.internal)
        assertFalse createdFolder.systemIndicator

        CommunicationFolder foundFolder = CommunicationFolder.findByName("test-integration")
        assertEquals(createdFolder, foundFolder)

        CommunicationFolder sameNameFolder = new CommunicationFolder()
        sameNameFolder.name = "test-integration"
        sameNameFolder.description = "another folder with same name"
        try {
            communicationFolderService.create(sameNameFolder)
            Assert.fail "Expected sameNameFolder to fail because of name unique constraint."
        } catch (ApplicationException e) {
            assertTrue(e.getMessage().toString().contains("folderExists"))

        }

        CommunicationFolder differentCaseFolder = new CommunicationFolder()
        differentCaseFolder.name = "TEST-integration"
        differentCaseFolder.description = "another folder with same name different case"
        try {
            communicationFolderService.create(differentCaseFolder)
            Assert.fail "Expected different case folder to fail because of name unique constraint."
        } catch (ApplicationException e) {
            assertTrue(e.getMessage().toString().contains("folderExists"))

        }

        CommunicationFolder differentNameFolder = new CommunicationFolder()
        differentNameFolder.name = "TEST-integration12"
        differentNameFolder.description = "another folder with different name different case"
        try {
            differentNameFolder = communicationFolderService.create(differentNameFolder)
            assertNotNull(differentNameFolder)
            assertEquals("TEST-integration12", differentNameFolder.name)
            assertEquals("another folder with different name different case", differentNameFolder.description)
            assertEquals(false, createdFolder.internal)
            assertFalse createdFolder.systemIndicator

        } catch (ApplicationException e) {
            Assert.fail "Expected different case folder to pass."
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
            assertEquals("@@r1:folderExists:" + folder2.name  + "@@", e.message)
        }

        folder1.name = folder2.name.toUpperCase()
        try {
            communicationFolderService.update(folder1)
            Assert.fail "Expected different case folder to fail because of name unique constraint."
        } catch (ApplicationException e) {
            assertEquals("@@r1:folderExists:" + folder2.name.toUpperCase()  + "@@", e.message)
        }
    }

    @Test
    void testDelete() {
        CommunicationFolder folder = new CommunicationFolder();
        folder.name = "test-integration"
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
