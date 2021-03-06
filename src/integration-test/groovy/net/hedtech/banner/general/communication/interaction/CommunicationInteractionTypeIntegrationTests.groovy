/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.communication.interaction

import groovy.sql.Sql
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.orm.hibernate5.HibernateOptimisticLockingFailureException
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import static groovy.test.GroovyAssert.*

/**
 * Integration tests for CommunicationInteractionType entity
 */
@Integration
@Rollback
class CommunicationInteractionTypeIntegrationTests extends BaseIntegrationTestCase {

    def CommunicationFolder folder

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }

    void setUpData() {
        folder = newValidForCreateFolder()
        folder.save( failOnError: true, flush: true )
        //Test if the generated entity now has an id assigned
        assertNotNull folder.id
    }

    @Test
    void testCreateCommunicationInteractionType() {
        def interactionType = newCommunicationInteractionType()
        interactionType.save( failOnError: true, flush: true )

        // Assert domain values
        assertNotNull interactionType?.id
        assertEquals "Face to Face", interactionType.name
        assertEquals "This is an interaction that happens face to face", interactionType.description
        assertEquals folder.name, interactionType.folder.name
    }

    @Test
    void testUpdateCommunicationInteractionType() {
        def interactionType = newCommunicationInteractionType()
        interactionType = interactionType.save( failOnError: true, flush: true )

        // Assert domain values
        assertNotNull interactionType?.id
        def id = interactionType.id

        // Find the domain
        interactionType = CommunicationInteractionType.get( id )
        assertNotNull interactionType

        // Update domain values
        interactionType.description = "###"
        interactionType.save( failOnError: true, flush: true )

        // Find the updated domain
        interactionType = CommunicationInteractionType.get( id )

        // Assert updated domain values
        assertNotNull interactionType?.id
        assertEquals( "###", interactionType.description )
    }

    @Test
    void testDeleteCommunicationInteractionType() {
        def interactionType = newCommunicationInteractionType()
        interactionType = interactionType.save( failOnError: true, flush: true )

        // Assert domain values
        assertNotNull interactionType?.id
        def id = interactionType.id

        // Find the domain
        interactionType = interactionType.get( id )
        assertNotNull interactionType

        // Delete the domain
        interactionType.delete()

        // Attempt to find the deleted domain
        interactionType = CommunicationInteractionType.get( id )
        assertNull interactionType
    }

    @Test
    void testNullValidationFailure() {
        // Instantiate an empty domain
        def interactionType = new CommunicationInteractionType()

        // Assert for domain validation
        assertFalse "CommunicationInteractionType should have failed null value validation", interactionType.validate()

        // Test that these fields raise an error if left null
        assertErrorsFor interactionType, 'nullable',
                [
                        'folder',
                        'name',
                ]
        // Test that these fields do NOT raise an error if left null
        assertNoErrorsFor interactionType,
                [
                        'description',
                        'isAvailable'
                ]
    }


    @Test
    void testMaxSizeValidationFailure() {
        def interactionType = newCommunicationInteractionType()

        // Set domain values to exceed maximum allowed length
        interactionType.description = "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT"
        interactionType.name = "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT"

        // Assert for domain
        assertFalse "CommunicationField should have failed max size validation", interactionType.validate()

        // Assert for specific fields
        assertErrorsFor interactionType, 'maxSize',
                [
                        'description',
                        'name',
                ]
    }


    @Test
    void testOptimisticLock() {
        def interactionType = newCommunicationInteractionType()
        interactionType.save( failOnError: true, flush: true )
        assertNotNull interactionType?.id

        def sql
        try {
            sql = new Sql( sessionFactory.getCurrentSession().connection() )
            sql.executeUpdate( "UPDATE gcritpe SET gcritpe_version = 999 WHERE gcritpe_surrogate_id = ?", [interactionType.id] )
        } finally {
            //sql?.close()
        }

        // Update the entity
        interactionType.dataOrigin = "OPT_TEST"
        shouldFail( HibernateOptimisticLockingFailureException ) {
            interactionType.save( failOnError: true, flush: true )
        }
    }

    @Test
    void testExistsAnotherNameFolder() {
        setUpData()
        def interactionType = new CommunicationInteractionType(
                // Required fields
                folder: folder,
                name: "Face to Face",
                // Nullable fields
                description: "This is an interaction that happens face to face",
        )
        interactionType.save( failOnError: true, flush: true )
        assertNotNull interactionType?.id

        Boolean falseResult = CommunicationInteractionType.existsAnotherNameFolder(interactionType.id, interactionType.name, interactionType.folder.name)
        assertFalse(falseResult)

        def interactionType2 = new CommunicationInteractionType(
                // Required fields
                folder: folder,
                name: "Duplicate Interaction Type",
                // Nullable fields
                description: "This is an interaction that happens face to face",
        )
        interactionType2.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull interactionType2.id

        Boolean trueResult = CommunicationInteractionType.existsAnotherNameFolder(interactionType.id, interactionType2.name, interactionType2.folder.name)
        assertTrue(trueResult)
    }

    private def newCommunicationInteractionType() {
        setUpData()
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
