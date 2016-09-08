/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.mobile

import groovy.time.DatumDependentDuration
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.mobile.CommunicationMobileNotificationExpirationPolicy
import net.hedtech.banner.general.communication.mobile.CommunicationMobileNotificationTemplate
import net.hedtech.banner.general.communication.template.CommunicationDurationUnit
import net.hedtech.banner.general.communication.template.CommunicationTemplate
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Tests crud methods provided by communication template service.
 */
class CommunicationMobileNotificationTemplateServiceIntegrationTests extends BaseIntegrationTestCase {

    def communicationTemplateService
    def communicationMobileNotificationTemplateService
    def selfServiceBannerAuthenticationProvider

    def CommunicationFolder defaultFolder


    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']
        super.setUp()
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('BCMADMIN', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)

        defaultFolder = new CommunicationFolder(
            name: "testFolder"
        )
        defaultFolder.save(failOnError: true, flush: true)
        assertNotNull defaultFolder.id
    }


    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }


    @Test
    void testCreate() {
        Date today = new Date()
        CommunicationMobileNotificationTemplate template = new CommunicationMobileNotificationTemplate(
            name: "testCreate",
            validFrom: today,
            folder: defaultFolder
        )

        template = communicationMobileNotificationTemplateService.create([domainModel: template])
        assertNotNull template.createDate
        assertNotNull template.createdBy
        assertNotNull template.lastModified
        assertNotNull template.lastModifiedBy
        assertEquals today, template.validFrom
        assertNull template.validTo

        def templateList = communicationTemplateService.findAll()
        assertEquals( 1, templateList.size() )
    }


    @Test
    void testUpdateTemplate() {
        CommunicationMobileNotificationTemplate template = new CommunicationMobileNotificationTemplate(
            name: "testUpdateTemplate",
            validFrom: new Date(),
            validTo: null,
            folder: defaultFolder
        )
        template = communicationMobileNotificationTemplateService.create([domainModel: template])
        assertNotNull template.id

        template.description = "Updated description"
        template = communicationMobileNotificationTemplateService.update([domainModel: template])
        assertEquals( "Updated description", template.description )
    }

    @Test
    void testDurationCannotBeNull() {
        CommunicationMobileNotificationTemplate template = new CommunicationMobileNotificationTemplate(
                name: "testUpdateTemplate",
                validFrom: new Date(),
                validTo: null,
                folder: defaultFolder,
                expirationPolicy: CommunicationMobileNotificationExpirationPolicy.DURATION,
                durationUnit: CommunicationDurationUnit.MINUTE
        )

        template.duration = -1
        try {
            template = (CommunicationMobileNotificationTemplate) communicationMobileNotificationTemplateService.create([domainModel: template])
            fail( "Expected application exception from validate exception on creation." )
        } catch (ApplicationException ae ) {
            assertEquals( "ValidationException", ae.wrappedException.getClass().simpleName )
        }

        template.duration = 1
        template = (CommunicationMobileNotificationTemplate) communicationMobileNotificationTemplateService.create([domainModel: template])
        assertNotNull template.id

        template.duration = -1
        try {
            template = (CommunicationMobileNotificationTemplate) communicationMobileNotificationTemplateService.update([domainModel: template])
            fail( "Expected application exception from validate exception on update." )
        } catch (ApplicationException ae) {
            assertEquals( "ValidationException", ae.wrappedException.getClass().simpleName )
        }

        template.duration = 2
        communicationMobileNotificationTemplateService.update([domainModel: template])
    }

    @Test
    void testNoExpirationPublish() {
        CommunicationMobileNotificationTemplate template = new CommunicationMobileNotificationTemplate(
            name: "testNoExpirationPublish",
            validFrom: new Date(),
            validTo: null,
            folder: defaultFolder
        )
        template = communicationMobileNotificationTemplateService.create( [domainModel: template] )
        assertNotNull template.id
        assertFalse template.published

        assertCannotPublish( template, "mobileHeadlineFieldRequiredToPublish" )
        template.mobileHeadline = "a mobile headline"
        template = communicationMobileNotificationTemplateService.publish( [domainModel: template] )
        assertTrue template.published
    }


    @Test
    void testDateTimeExpirationPublish() {
        CommunicationMobileNotificationTemplate template = new CommunicationMobileNotificationTemplate(
            name: "testDateTimeExpirationPublish",
            validFrom: new Date(),
            validTo: null,
            folder: defaultFolder,
            expirationPolicy: CommunicationMobileNotificationExpirationPolicy.DATE_TIME
        )
        template = communicationMobileNotificationTemplateService.create( [domainModel: template] )
        assertNotNull template.id
        assertFalse template.published

        assertCannotPublish( template, "mobileHeadlineFieldRequiredToPublish" )
        template.mobileHeadline = "a mobile headline"
        assertCannotPublish( template, "expirationDateRequiredToPublish" )

        Date today = new Date()
        DatumDependentDuration period = new DatumDependentDuration(0, 0, 2, 0, 0, 0, 0)
        Date expirationDate = period + today
        template.expirationDateTime = expirationDate
        template = communicationMobileNotificationTemplateService.publish( [domainModel: template] )
        assertTrue template.published
    }

    @Test
    void testDurationExpirationPublish() {
        CommunicationMobileNotificationTemplate template = new CommunicationMobileNotificationTemplate(
                name: "testDurationExpirationPublish",
                validFrom: new Date(),
                validTo: null,
                folder: defaultFolder,
                expirationPolicy: CommunicationMobileNotificationExpirationPolicy.DURATION
        )
        template = communicationMobileNotificationTemplateService.create( [domainModel: template] )
        assertNotNull template.id
        assertFalse template.published

        assertCannotPublish( template, "mobileHeadlineFieldRequiredToPublish" )
        template.mobileHeadline = "a mobile headline"
        assertCannotPublish( template, "durationRequiredToPublish" )

        template.duration = 5
        template = communicationMobileNotificationTemplateService.publish( [domainModel: template] )
        assertTrue template.published
    }


    @Test
    void testStickyPublish() {
        CommunicationMobileNotificationTemplate template = new CommunicationMobileNotificationTemplate(
                name: "testNoExpirationPublish",
                validFrom: new Date(),
                validTo: null,
                folder: defaultFolder
        )
        template = communicationMobileNotificationTemplateService.create( [domainModel: template] )
        assertNotNull template.id
        assertFalse template.published

        assertCannotPublish( template, "mobileHeadlineFieldRequiredToPublish" )
        template.mobileHeadline = "a mobile headline"
        template.sticky = true
        assertCannotPublish( template, "expirationRequiredForSticky" )

        template.expirationPolicy = CommunicationMobileNotificationExpirationPolicy.DURATION
        template.duration = 1
        template = communicationMobileNotificationTemplateService.publish( [domainModel: template] )
        assertTrue template.published
    }


    private void assertCannotPublish( CommunicationTemplate template, String reason ) {
        Boolean originalPublished = template.published
        try {
            communicationMobileNotificationTemplateService.publish( template )
            fail( reason )
        } catch (ApplicationException ae ) {
            assertApplicationException ae, reason
        } finally {
            template.published = originalPublished
        }
    }


}
