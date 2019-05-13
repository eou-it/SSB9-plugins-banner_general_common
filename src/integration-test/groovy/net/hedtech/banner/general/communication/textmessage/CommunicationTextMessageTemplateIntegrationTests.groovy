/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.textmessage

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import net.hedtech.banner.general.communication.CommunicationManagementTestingSupport
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Tests basic CRUD operations on an CommunicationTextMessageTemplate entity object
 * and any field level validation.
 */
@Integration
@Rollback
class CommunicationTextMessageTemplateIntegrationTests extends BaseIntegrationTestCase {

    def CommunicationFolder defaultFolder

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }

    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }

    void setUpData() {
        defaultFolder = CommunicationManagementTestingSupport.newValidForCreateFolderWithSave("default folder")
    }

    @Test
    void testCreate() {
        setUpData()
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
        setUpData()
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
        setUpData()
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