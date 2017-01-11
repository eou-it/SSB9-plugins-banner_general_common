/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.field

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

class CommunicationFieldServiceIntegrationTests extends BaseIntegrationTestCase {

    def communicationFieldService
    def CommunicationFolder validFolder
    def String validImmutableId
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
        validImmutableId = UUID.randomUUID().toString()
    }


    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }


    @Test
    void testCreateCommunicationField() {
        def newCommunicationField = newCommunicationField()
        def communicationField = communicationFieldService.create( [domainModel: newCommunicationField] )
        // Assert domain values
        assertNotNull communicationField?.id
        assertEquals "TTTTTTTTTT", communicationField.description
        assertEquals validFolder, communicationField.folder
        assertEquals "TT" +
                             "TTTTTT", communicationField.formatString
        assertEquals "TTTTTTTT", communicationField.groovyFormatter
        assertEquals validImmutableId, communicationField.immutableId
        assertEquals "TTTTTTTTTT", communicationField.name
        assertEquals "TTTTTTTTTT", communicationField.previewValue
        assertEquals true, communicationField.renderAsHtml
        assertEquals "TTTTTTTTTT", communicationField.ruleUri
        assertEquals CommunicationFieldStatus.DEVELOPMENT, communicationField.status
    }

    @Test
    void testCommunicationFieldPublish() {
        def newCommunicationField = newCommunicationField()
        def communicationField = communicationFieldService.create( [domainModel: newCommunicationField] )
        // Assert domain values
        assertNotNull communicationField?.id
        assertEquals "TTTTTTTTTT", communicationField.description
        assertEquals validFolder, communicationField.folder
        assertEquals "TT" +
                "TTTTTT", communicationField.formatString
        assertEquals "TTTTTTTT", communicationField.groovyFormatter
        assertEquals validImmutableId, communicationField.immutableId
        assertEquals "TTTTTTTTTT", communicationField.name
        assertEquals "TTTTTTTTTT", communicationField.previewValue
        assertEquals true, communicationField.renderAsHtml
        assertEquals "TTTTTTTTTT", communicationField.ruleUri
        assertEquals CommunicationFieldStatus.DEVELOPMENT, communicationField.status
        def newPublishedField = communicationFieldService.publishDataField([id:communicationField.id])
        assertEquals(CommunicationFieldStatus.PRODUCTION, newPublishedField.status)
    }

    @Test
    void testCommunicationFieldPublishError() {
        def newCommunicationField = newCommunicationField()
        newCommunicationField.formatString = null
        def communicationField = communicationFieldService.create( [domainModel: newCommunicationField] )
        // Assert domain values
        assertNotNull communicationField?.id
        assertEquals "TTTTTTTTTT", communicationField.description
        assertEquals validFolder, communicationField.folder
        assertNull communicationField.formatString
        assertEquals "TTTTTTTT", communicationField.groovyFormatter
        assertEquals validImmutableId, communicationField.immutableId
        assertEquals "TTTTTTTTTT", communicationField.name
        assertEquals "TTTTTTTTTT", communicationField.previewValue
        assertEquals true, communicationField.renderAsHtml
        assertEquals "TTTTTTTTTT", communicationField.ruleUri
        assertEquals CommunicationFieldStatus.DEVELOPMENT, communicationField.status
        def newPublishedField
        shouldFail {
            newPublishedField = communicationFieldService.publishDataField([id: communicationField.id])
        }
    }

    /*
    Test that the service automatically assigns the immutableId
     */


    @Test
    void testImmutableId() {
        def newCommunicationField = newCommunicationField()
        newCommunicationField.immutableId = null
        def communicationField = communicationFieldService.create( [domainModel: newCommunicationField] )
        assertNotNull communicationField.immutableId
    }


    @Test
    void testUpdateCommunicationField() {
        def communicationField = newCommunicationField()
        communicationField = communicationFieldService.create( [domainModel: communicationField] )
        assertNotNull communicationField
        def id = communicationField.id
        // Find the domain
        def savedCommunicationField = communicationFieldService.get( id )
        assertNotNull savedCommunicationField?.id
        // Update domain values
        savedCommunicationField.formatString = "###"
        def updatedCommunicationField = communicationFieldService.update( [domainModel: savedCommunicationField] )
        // Find the updated domain
        communicationField = communicationFieldService.get( updatedCommunicationField.id )
        // Assert updated domain values
        assertNotNull communicationField
        assertEquals "###", communicationField.formatString

        //Test exists same name
        def field2 = newCommunicationField()
        field2.name = "field2"
        field2.immutableId = UUID.randomUUID().toString()
        field2 = communicationFieldService.create(field2)

        communicationField.name = field2.name
        try {
            communicationFieldService.update(communicationField)
            Assert.fail "Expected sameNameFolder to fail because of name unique constraint."
        } catch (ApplicationException e) {
            assertEquals("@@r1:fieldNameAlreadyExists@@", e.message)
        }

    }


    @Test
    void testImmutableIdCannotBeUpdated() {
        def newCommunicationField = newCommunicationField()
        def savedCommunicationField = communicationFieldService.create( [domainModel: newCommunicationField] )

        // Assert domain values
        assertNotNull savedCommunicationField?.id
        savedCommunicationField.immutableId = "foo"
        shouldFail {
            communicationFieldService.update( [domainModel: savedCommunicationField] )
        }
    }


    @Test
    void testRenderAsHtmlDefaultsToFalse() {
        def newCommunicationField = newCommunicationField()
        newCommunicationField.renderAsHtml=null
        def savedCommunicationField = communicationFieldService.create( [domainModel: newCommunicationField] )
        assertEquals(  false, savedCommunicationField?.renderAsHtml)
    }


    @Test
    void testRenderAsHtmlCannotBeUpdatedToNull() {
        def newCommunicationField = newCommunicationField()
        newCommunicationField.renderAsHtml = null
        def savedCommunicationField = communicationFieldService.create( [domainModel: newCommunicationField] )

        savedCommunicationField.renderAsHtml = null
        shouldFail {
            communicationFieldService.update( [domainModel: savedCommunicationField] )
        }
    }


    @Test
    void testUpdateSomeNulls() {
        def communicationField = newCommunicationField()
        def savedCommunicationField = communicationFieldService.create( [domainModel: communicationField] )

        // Assert domain values
        assertNotNull savedCommunicationField?.id
        def id = savedCommunicationField.id

        // Find the domain
        communicationField = communicationFieldService.get( id )
        assertNotNull communicationField

        // Update domain values to null

        communicationField.statementType = null
        communicationFieldService.update( [domainModel: communicationField] )

        // Find the updated domain
        communicationField = CommunicationField.get( id )
        /* Check that the service set it to a default value */
        assertNotNull communicationField.statementType
    }

    @Test
    void testDeleteCommunicationField() {
        def newCommunicationField = newCommunicationField()
        def communicationField = communicationFieldService.create( [domainModel: newCommunicationField] )
        assertNotNull communicationField
        def id = communicationField.id
        // Find the domain
        def savedCommunicationField = CommunicationField.get( id )
        assertNotNull savedCommunicationField
        // Delete the domain
        def deletedCommunicationField = communicationFieldService.delete( [domainModel: savedCommunicationField] )
        // Attempt to find the deleted domain
        deletedCommunicationField = savedCommunicationField.get( id )
        assertNull deletedCommunicationField
    }

    private def newCommunicationField() {
        def communicationField = new CommunicationField(
                // Required fields
                folder: validFolder,
                immutableId: validImmutableId,
                name: "TTTTTTTTTT",
                returnsArrayArguments: false,

                // Nullable fields
                description: "TTTTTTTTTT",
                formatString: "TTTTTTTT",
                groovyFormatter: "TTTTTTTT",
                previewValue: "TTTTTTTTTT",
                renderAsHtml: true,
                ruleUri: "TTTTTTTTTT",
                status: CommunicationFieldStatus.DEVELOPMENT,
                statementType: CommunicationRuleStatementType.SQL_PREPARED_STATEMENT,
                ruleContent: "Select max(stvterm_code) from stvterm where :pidm = :pidm"
        )

        return communicationField
    }


    private def newValidForCreateFolder() {
        def folder = new CommunicationFolder(
                description: "Test Folder",
                internal: false,
                name: "Folder Name"
        )
        return folder
    }
}


