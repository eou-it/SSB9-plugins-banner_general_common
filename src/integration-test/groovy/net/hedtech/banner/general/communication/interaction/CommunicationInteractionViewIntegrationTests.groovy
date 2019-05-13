/*********************************************************************************
 Copyright 2016-2018 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.communication.interaction

import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
/**
 * Integration tests for CommunicationInteractionView entity
 */
@Integration
@Rollback
class CommunicationInteractionViewIntegrationTests extends BaseIntegrationTestCase {

    def CommunicationOrganization organization
    def CommunicationInteractionType interactionType
    def CommunicationFolder folder
    def CommunicationManualInteraction interaction

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    void setUpOrganizationData() {
        organization = newValidForCreateOrganization()
        organization.save( failOnError: true, flush: true )
    }

    void setUpFolderData() {
        folder = newValidForCreateFolder()
        folder.save( failOnError: true, flush: true )
        //Test if the generated entity now has an id assigned
        assertNotNull folder.id
    }

    void setUpInteractionTypeData() {
        interactionType = newValidForCreateCommunicationInteractionType()
        interactionType.save( failOnError: true, flush: true )
        //Test if the generated entity now has an id assigned
        assertNotNull interactionType.id
    }

    @Test
    void testCommunicationInteractionView() {
        interaction = newValidForCreateCommunicationManualInteraction()
        interaction.save( failOnError: true, flush: true )
        //Test if the generated entity now has an id assigned
        assertNotNull interaction.id

        List interactions = CommunicationInteractionView.findByConstituentNameOrBannerId('%');
        // Assert domain values
        assertNotNull interactions
        CommunicationInteractionView interaction = interactions.get(0)
        assertNotNull interaction?.surrogateId
        assertEquals(1L, interaction.interacteePidm)
        assertEquals "Class Schedule", interaction.subject
        assertEquals organization.name, interaction.organizationName
        assertEquals interactionType.folder.name, interaction.folderName
        assertEquals interactionType.name, interaction.templateName
        assertNotNull(interaction.interactionDate)
    }

    private def newValidForCreateCommunicationManualInteraction() {
        setUpOrganizationData()
        setUpInteractionTypeData()
        def manualInteraction = new CommunicationManualInteraction(
                // Required fields
                constituentPidm: 1L,
                organization: organization,
                interactionType: interactionType,
                aSubject: "Class Schedule",
                // Nullable fields
                description: "Tom stopped by today to have a discussion on his electives class schedule",
                interactorPidm: 11L,
                interactionDate: new Date(),
                createDate: new Date(),
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
        setUpFolderData()
        def interactionType = new CommunicationInteractionType(
                // Required fields
                folder: folder,
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
