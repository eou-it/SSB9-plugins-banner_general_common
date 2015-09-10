/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.template

import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Tests basic CRUD operations on an CommunicationMobileNotificationTemplate entity object
 * and any field level validation.
 */
class CommunicationMobileNotificationTemplateIntegrationTests extends BaseIntegrationTestCase {

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
        int templateCount = CommunicationMobileNotificationTemplate.findAll().size()

        CommunicationMobileNotificationTemplate template = new CommunicationMobileNotificationTemplate(
            name: "create test",
            createdBy: 'MIKE',
            createDate: new Date(),
            validFrom: new Date(),
            validTo: null,
            folder: defaultFolder
        )

        template.save( failOnError: true, flush: true )
    }

    @Test
    void testUpdate() {
        CommunicationMobileNotificationTemplate template = new CommunicationMobileNotificationTemplate(
                name: "update test",
                description: "description",
                createdBy: 'MIKE',
                createDate: new Date(),
                validFrom: new Date(),
                validTo: null,
                folder: defaultFolder
        )

        template.save( failOnError: true, flush: true )

        template.mobileHeadline = "test mobile headline"
        template.headline = "headline"
        template.messageDescription = "message description"
        template.destinationLabel = "destination label"
        template.destinationLink = "http:////127.0.0.1//index.html"

        template.save( failOnError: true, flush: true )

        template = CommunicationMobileNotificationTemplate.get( template.id )
        assertEquals( "test mobile headline", template.mobileHeadline )
        assertEquals( "headline", template.headline )
        assertEquals( "message description", template.messageDescription )
        assertEquals( "destination label", template.destinationLabel )
        assertEquals( "description", template.description )
    }

    @Test
    void testDelete() {
        int templateCount = CommunicationMobileNotificationTemplate.findAll().size()

        CommunicationMobileNotificationTemplate template = new CommunicationMobileNotificationTemplate(
                name: "delete test",
                createdBy: 'MIKE',
                createDate: new Date(),
                validFrom: new Date(),
                validTo: null,
                folder: defaultFolder
        )

        template.save( failOnError: true, flush: true )

        assertEquals( templateCount + 1, CommunicationMobileNotificationTemplate.findAll().size() )

        template.delete()

        assertEquals( templateCount, CommunicationMobileNotificationTemplate.findAll().size() )
    }

}
