/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.communication.interaction

import groovy.sql.Sql
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException

/**
 * Integration tests for CommunicationManualInteraction entity
 */
class CommunicationManualInteractionIntegrationTests extends BaseIntegrationTestCase {

    def CommunicationOrganization organization
    def CommunicationInteractionType interactionType
    def CommunicationFolder folder

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        organization = newValidForCreateOrganization()
        organization.save( failOnError: true, flush: true )
        //Test if the generated entity now has an id assigned
        assertNotNull organization.id
        folder = newValidForCreateFolder()
        folder.save( failOnError: true, flush: true )
        //Test if the generated entity now has an id assigned
        assertNotNull folder.id
        interactionType = newValidForCreateCommunicationInteractionType()
        interactionType.save( failOnError: true, flush: true )
        //Test if the generated entity now has an id assigned
        assertNotNull interactionType.id
    }


    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    void testCreateCommunicationManualInteraction() {
        def manualInteraction = newCommunicationManualInteraction()
        manualInteraction.save( failOnError: true, flush: true )

        // Assert domain values
        assertNotNull manualInteraction?.id
        assertEquals(1353L, manualInteraction.constituentPidm)
        assertEquals "Class Schedule", manualInteraction.subject
        assertEquals "Tom stopped by today to have a discussion on his electives class schedule", manualInteraction.description
        assertEquals organization.name, manualInteraction.organization.name
        assertEquals interactionType.name, manualInteraction.interactionType.name
        assertEquals(30078L, manualInteraction.interactorPidm)
        assertNotNull(manualInteraction.interactionDate)
        assertNotNull(manualInteraction.createDate)
        assertNotNull(manualInteraction.createdBy)
    }

    @Test
    void testNullValidationFailure() {
        // Instantiate an empty domain
        def manualInteraction = new CommunicationManualInteraction()

        // Assert for domain validation
        assertFalse "CommunicationManualInteraction should have failed null value validation", manualInteraction.validate()

        // Test that these fields raise an error if left null
        assertErrorsFor manualInteraction, 'nullable',
                [
                        'constituentPidm',
                        'subject',
                        'organization',
                        'interactionType',
                        'interactorPidm',
                        'interactionDate'
                ]
        // Test that these fields do NOT raise an error if left null
        assertNoErrorsFor interactionType,
                [
                        'description'
                ]
    }


    @Test
    void testOptimisticLock() {
        def manualInteraction = newCommunicationManualInteraction()
        manualInteraction.save( failOnError: true, flush: true )
        assertNotNull manualInteraction?.id

        def sql
        try {
            sql = new Sql( sessionFactory.getCurrentSession().connection() )
            sql.executeUpdate( "UPDATE gcrmint SET gcrmint_version = 999 WHERE gcrmint_surrogate_id = ?", [manualInteraction.id] )
        } finally {
            sql?.close()
        }

        // Update the entity
        manualInteraction.dataOrigin = "OPT_TEST"
        shouldFail( HibernateOptimisticLockingFailureException ) {
            manualInteraction.save( failOnError: true, flush: true )
        }
    }

    private def newCommunicationManualInteraction() {
        def manualInteraction = new CommunicationManualInteraction(
                // Required fields
                constituentPidm: 1353L,
                organization: organization,
                interactionType: interactionType,
                aSubject: "Class Schedule",
                // Nullable fields
                description: "Tom stopped by today to have a discussion on his electives class schedule",
                interactorPidm: 30078L,
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

    private def newValidForCreateFolder() {
        def folder = new CommunicationFolder(
                description: "Test Folder Description",
                internal: false,
                name: "Test Folder Name"
        )
        return folder
    }

    private def newValidForCreateCommunicationInteractionType() {
        def interactionType = new CommunicationInteractionType(
                // Required fields
                folder: folder,
                name: "Face to Face",
                // Nullable fields
                description: "This is an interaction that happens face to face",
        )
        return interactionType
    }
}
