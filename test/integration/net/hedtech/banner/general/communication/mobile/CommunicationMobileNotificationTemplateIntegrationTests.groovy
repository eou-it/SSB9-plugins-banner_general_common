/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.mobile

import groovy.time.DatumDependentDuration
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.template.CommunicationDurationUnit
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
        CommunicationMobileNotificationTemplate template = new CommunicationMobileNotificationTemplate(
            name: "create test",
            createdBy: 'MIKE',
            createDate: new Date(),
            validFrom: new Date(),
            validTo: null,
            folder: defaultFolder
        )

        template.save( failOnError: true, flush: true )
        template = CommunicationMobileNotificationTemplate.get( template.id )
        assertEquals( "create test", template.name )
        assertEquals( "MIKE", template.createdBy )
        assertFalse template.systemIndicator
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
        assertFalse template.systemIndicator
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

    @Test
    void testExpires() {
        CommunicationMobileNotificationTemplate template = new CommunicationMobileNotificationTemplate(
                name: "testExpires",
                createdBy: 'MIKE',
                createDate: new Date(),
                validFrom: new Date(),
                validTo: null,
                folder: defaultFolder,
                expirationPolicy: CommunicationMobileNotificationExpirationPolicy.NO_EXPIRATION
        )
        template.save( failOnError: true, flush: true )

        assertEquals( CommunicationMobileNotificationExpirationPolicy.NO_EXPIRATION, template.expirationPolicy )


        template.expirationPolicy = CommunicationMobileNotificationExpirationPolicy.DURATION
        template.duration = 7
        template.durationUnit = CommunicationDurationUnit.DAY
        template.save( failOnError: true, flush: true )

        assertEquals( CommunicationMobileNotificationExpirationPolicy.DURATION, template.expirationPolicy )
        assertEquals( 7, template.duration )
        assertEquals( CommunicationDurationUnit.DAY, template.durationUnit )

        Date today = new Date()
        DatumDependentDuration period = new DatumDependentDuration(0, 0, 2, 0, 0, 0, 0)
        Date expirationDate = period + today

        template.expirationPolicy = CommunicationMobileNotificationExpirationPolicy.DATE_TIME
        template.setExpirationDateTime( expirationDate )
        template.save( failOnError: true, flush: true )

        assertEquals( CommunicationMobileNotificationExpirationPolicy.DATE_TIME, template.expirationPolicy )
        assertEquals( expirationDate, template.expirationDateTime )
    }

}
