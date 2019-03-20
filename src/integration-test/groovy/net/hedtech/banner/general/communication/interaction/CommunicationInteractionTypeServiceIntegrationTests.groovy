/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.communication.interaction

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

class CommunicationInteractionTypeServiceIntegrationTests extends BaseIntegrationTestCase {

    def communicationInteractionTypeService
    def CommunicationFolder validFolder
    def selfServiceBannerAuthenticationProvider


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('BCMADMIN', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)

        validFolder = newValidForCreateFolder()
        validFolder.save( failOnError: true, flush: true )
        //Test if the generated entity now has an id assigned
        assertNotNull validFolder.id
    }


    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }


    @Test
    void testCreateCommunicationInteractionType() {
        def newInteractionType = newCommunicationInteractionType()
        def interactionType = communicationInteractionTypeService.create( [domainModel: newInteractionType] )
        // Assert domain values
        assertNotNull interactionType?.id
        assertEquals "Face to Face", interactionType.name
        assertEquals "This is an interaction that happens face to face", interactionType.description
        assertEquals validFolder, interactionType.folder
    }

    @Test
    void testCreateEmptyNameCommunicationInteractionType() {
        def newInteractionType = newCommunicationInteractionType()
        newInteractionType.name = "   "
        def message = shouldFail( ApplicationException ) {
            communicationInteractionTypeService.create( [domainModel: newInteractionType] )
        }
        assertEquals "Incorrect failure message returned", "@@r1:nameCannotBeNull@@", message
    }

    @Test
    void testUpdateCommunicationInteractionType() {
        def newInteractionType = newCommunicationInteractionType()
        newInteractionType = communicationInteractionTypeService.create( [domainModel: newInteractionType] )
        assertNotNull newInteractionType
        def id = newInteractionType.id
        // Find the domain
        def savedInteractionType = communicationInteractionTypeService.get( id )
        assertNotNull savedInteractionType?.id
        // Update domain values
        savedInteractionType.description = "###"
        def updatedInteractionType = communicationInteractionTypeService.update( [domainModel: savedInteractionType] )
        // Find the updated domain
        def interactionType = communicationInteractionTypeService.get( updatedInteractionType.id )
        // Assert updated domain values
        assertNotNull interactionType
        assertEquals "###", interactionType.description

        def newInteractionType2 = newCommunicationInteractionType()
        newInteractionType2.name = "Duplicate Interaction Type"
        newInteractionType2 = communicationInteractionTypeService.create( [domainModel: newInteractionType2] )
        assertNotNull newInteractionType2
        def id2 = newInteractionType2.id
        // Find the domain
        def savedInteractionType2 = communicationInteractionTypeService.get( id2 )
        assertNotNull savedInteractionType2?.id
        // Update domain values
        savedInteractionType2.name = newInteractionType.name
        try {
            communicationInteractionTypeService.update( [domainModel: savedInteractionType2] )
            Assert.fail "Expected sameNameFolder to fail because of name unique constraint."
        } catch (ApplicationException e) {
            assertEquals("@@r1:not.unique.message@@", e.message)
        }
    }

    @Test
    void testDeleteCommunicationInteractionType() {
        def newInteractionType = newCommunicationInteractionType()
        def interactionType = communicationInteractionTypeService.create( [domainModel: newInteractionType] )
        assertNotNull interactionType
        def id = interactionType.id
        // Find the domain
        def savedInteractionType = CommunicationInteractionType.get( id )
        assertNotNull savedInteractionType
        // Delete the domain
        def deletedInteractionType = communicationInteractionTypeService.delete( [domainModel: savedInteractionType] )
        // Attempt to find the deleted domain
        deletedInteractionType = CommunicationInteractionType.get( id )
        assertNull deletedInteractionType
    }

    @Test
    void testUniqueNameConstraintCommunicationInteractionType() {
        def newInteractionType = newCommunicationInteractionType()
        newInteractionType = communicationInteractionTypeService.create( [domainModel: newInteractionType] )
        assertNotNull newInteractionType
        def id = newInteractionType.id
        // Find the domain
        def savedInteractionType = communicationInteractionTypeService.get( id )
        assertNotNull savedInteractionType?.id
        // Create new domain with same name
        def newInteractionType1 = newCommunicationInteractionType()
        def message = shouldFail( ApplicationException ) {
            communicationInteractionTypeService.create( [domainModel: newInteractionType1] )
        }
        assertEquals "Incorrect failure message returned", "@@r1:not.unique.message@@", message
    }

    private def newCommunicationInteractionType() {
        def interactionType = new CommunicationInteractionType(
                // Required fields
                folder: validFolder,
                name: "Face to Face",
                // Nullable fields
                description: "This is an interaction that happens face to face",
        )

        return interactionType
    }

    private def newValidForCreateFolder() {
        def folder = new CommunicationFolder(
                description: "Test Folder Description",
                internal: false,
                name: "Test Folder Name"
        )
        return folder
    }
}
