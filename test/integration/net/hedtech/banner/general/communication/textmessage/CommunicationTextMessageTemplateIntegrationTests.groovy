/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.textmessage

import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Tests basic CRUD operations on an CommunicationTextMessageTemplate entity object
 * and any field level validation.
 */
class CommunicationTextMessageTemplateIntegrationTests extends BaseIntegrationTestCase {

    def CommunicationFolder defaultFolder

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        defaultFolder = new CommunicationFolder(
                name: "default folder"
        )
        defaultFolder.save( failOnError: true, flush: true )
        //Test if the generated entity now has an id assigned
        assertNotNull defaultFolder.id
    }

    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }

    @Test
    void testCreate() {
        CommunicationTextMessageTemplate template = new CommunicationTextMessageTemplate(
                name: "create test",
                createdBy: 'MIKE',
                createDate: new Date(),
                validFrom: new Date(),
                validTo: null,
                folder: defaultFolder
        )

        template.save( failOnError: true, flush: true )
        template = CommunicationTextMessageTemplate.get( template.id )
        assertEquals( "create test", template.name )
        assertEquals( "MIKE", template.createdBy )
        assertFalse template.systemIndicator
    }

    @Test
    void testUpdate() {
        CommunicationTextMessageTemplate template = new CommunicationTextMessageTemplate(
                name: "update test",
                description: "description",
                createdBy: 'MIKE',
                createDate: new Date(),
                validFrom: new Date(),
                validTo: null,
                folder: defaultFolder
        )

        template.save( failOnError: true, flush: true )

        template.message = "test message"
        template.footer = "footer"
        template.destinationLabel = "destination label"
        template.destinationLink = "http:////127.0.0.1//index.html"

        template.save( failOnError: true, flush: true )

        template = CommunicationTextMessageTemplate.get( template.id )
        assertEquals( "test message", template.message )
        assertEquals( "footer", template.footer )
        assertEquals( "destination label", template.destinationLabel )
        assertEquals( "description", template.description )
        assertFalse template.systemIndicator
    }

    @Test
    void testDelete() {
        int templateCount = CommunicationTextMessageTemplate.findAll().size()

        CommunicationTextMessageTemplate template = new CommunicationTextMessageTemplate(
                name: "delete test",
                createdBy: 'MIKE',
                createDate: new Date(),
                validFrom: new Date(),
                validTo: null,
                folder: defaultFolder
        )

        template.save( failOnError: true, flush: true )

        assertEquals( templateCount + 1, CommunicationTextMessageTemplate.findAll().size() )

        template.delete()

        assertEquals( templateCount, CommunicationTextMessageTemplate.findAll().size() )
    }

}