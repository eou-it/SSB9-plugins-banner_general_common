/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.communication.interaction

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

class CommunicationManualInteractionServiceIntegrationTests extends BaseIntegrationTestCase {
    def communicationManualInteractionService
    def CommunicationOrganization validOrganization
    def CommunicationFolder validFolder
    def CommunicationInteractionType validInteractionType
    def selfServiceBannerAuthenticationProvider

    def validInteractionDate = Calendar.getInstance().getTime()
    def validCreatedDate = Calendar.getInstance().getTime()

    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']
        super.setUp()
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('BCMADMIN', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)

        validOrganization = newValidForCreateOrganization()
        validOrganization.save( failOnError: true, flush: true )
        //Test if the generated entity now has an id assigned
        assertNotNull validOrganization.id
        validFolder = newValidForCreateFolder()
        validFolder.save( failOnError: true, flush: true )
        //Test if the generated entity now has an id assigned
        assertNotNull validFolder.id
        validInteractionType = newValidForCreateCommunicationInteractionType()
        validInteractionType.save( failOnError: true, flush: true )
        //Test if the generated entity now has an id assigned
        assertNotNull validInteractionType.id
    }


    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }


    @Test
    void testCreateCommunicationManualInteraction() {
        def newManualInteraction = newCommunicationManualInteraction()
        def manualInteraction = communicationManualInteractionService.create( [domainModel: newManualInteraction] )
        // Assert domain values
        assertNotNull manualInteraction?.id
        assertEquals(1353L, manualInteraction.constituentPidm)
        assertEquals "Class Schedule", manualInteraction.aSubject
        assertEquals "Tom stopped by today to have a discussion on his electives class schedule", manualInteraction.description
        assertEquals validOrganization.name, manualInteraction.organization.name
        assertEquals validInteractionType.name, manualInteraction.interactionType.name
        assertEquals(30078L, manualInteraction.interactorPidm)
        assertNotNull(manualInteraction.interactionDate)
        assertNotNull(manualInteraction.createDate)
        assertNotNull(manualInteraction.createdBy)
    }

    @Test
    void testCreateEmptySubjectCommunicationManualInteraction() {
        def newManualInteraction = newCommunicationManualInteraction()
        newManualInteraction.aSubject = "   "
        def message = shouldFail( ApplicationException ) {
            communicationManualInteractionService.create( [domainModel: newManualInteraction] )
        }
        assertEquals "Incorrect failure message returned", "@@r1:subjectCannotBeNull@@", message
    }

    @Test
    void testUpdateCommunicationManualInteraction() {
        def newManualInteraction = newCommunicationManualInteraction()
        newManualInteraction = communicationManualInteractionService.create( [domainModel: newManualInteraction] )
        assertNotNull newManualInteraction
        def id = newManualInteraction.id
        // Find the domain
        def savedInteractionType = communicationManualInteractionService.get( id )
        assertNotNull savedInteractionType?.id
        // Update domain values
        savedInteractionType.aSubject = "Updated subject"
        def updatedInteractionType = communicationManualInteractionService.update( [domainModel: savedInteractionType] )
        // Find the updated domain
        def manualInteraction = communicationManualInteractionService.get( updatedInteractionType.id )
        // Assert updated domain values
        assertNotNull manualInteraction
        assertEquals "Updated subject", manualInteraction.aSubject

    }

    @Test
    void testDeleteCommunicationManualInteraction() {
        def newManualInteraction = newCommunicationManualInteraction()
        def manualInteraction = communicationManualInteractionService.create( [domainModel: newManualInteraction] )
        assertNotNull manualInteraction
        def id = manualInteraction.id
        // Find the domain
        def savedInteractionType = CommunicationManualInteraction.get( id )
        assertNotNull savedInteractionType
        // Delete the domain // Not supported exception thrown
        shouldFail( ApplicationException ) {
            communicationManualInteractionService.delete([domainModel: savedInteractionType])
        }
    }

    private def newCommunicationManualInteraction() {
        def manualInteraction = new CommunicationManualInteraction(
                // Required fields
                constituentPidm: 1353L,
                organization: validOrganization,
                interactionType: validInteractionType,
                aSubject: "Class Schedule",
                // Nullable fields
                description: "Tom stopped by today to have a discussion on his electives class schedule",
                interactorPidm: 30078L,
                interactionDate: validInteractionDate,
                createDate: validCreatedDate,
                createdBy: "BCMADMIN"
        )
        return manualInteraction
    }
    
    private def newValidForCreateOrganization() {
        def organization = new CommunicationOrganization(
                description: "Test Description for Organization",
                name: "Root Organization",
                isAvailable : true
        )
        return organization
    }
    
    private def newValidForCreateCommunicationInteractionType() {
        def manualInteraction = new CommunicationInteractionType(
                // Required fields
                folder: validFolder,
                name: "Face to Face",
                // Nullable fields
                description: "This is an interaction that happens face to face",
        )

        return manualInteraction
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
