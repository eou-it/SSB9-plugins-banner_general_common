/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.letter

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.folder.CommunicationFolder
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
class CommunicationLetterTemplateServiceIntegrationTests extends BaseIntegrationTestCase {

    def communicationTemplateService
    def communicationLetterTemplateService
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
        CommunicationLetterTemplate template = new CommunicationLetterTemplate(
            name: "testCreate",
            validFrom: today,
            folder: defaultFolder
        )

        template = communicationLetterTemplateService.create([domainModel: template])
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
        CommunicationLetterTemplate template = new CommunicationLetterTemplate(
            name: "testUpdateTemplate",
            validFrom: new Date(),
            validTo: null,
            folder: defaultFolder
        )
        template = communicationLetterTemplateService.create([domainModel: template])
        assertNotNull template.id

        template.description = "Updated description"
        template = communicationLetterTemplateService.update([domainModel: template])
        assertEquals( "Updated description", template.description )
    }

//    @Test
//    void testNoToAddressPublish() {
//        CommunicationLetterTemplate template = new CommunicationLetterTemplate(
//                name: "testUpdateTemplate",
//                validFrom: new Date(),
//                validTo: null,
//                folder: defaultFolder,
//                content: "testToAddressCannotBeNull"
//        )
//
//        template = communicationLetterTemplateService.create([domainModel: template])
//
//        try {
//            template = (CommunicationLetterTemplate) communicationLetterTemplateService.create([domainModel: template])
//            fail( "Expected application exception from validate exception on creation." )
//        } catch (ApplicationException e ) {
//            assertEquals( "", e.message )
//        }
//
//        template.toAddress = "Los Angeles"
//        template = (CommunicationLetterTemplate) communicationLetterTemplateService.create([domainModel: template])
//        assertNotNull template.id
//
//        template.toAddress = ""
//        try {
//            template = (CommunicationLetterTemplate) communicationLetterTemplateService.update([domainModel: template])
//            fail( "Expected application exception from validate exception on update." )
//        } catch (ApplicationException ae) {
//            assertEquals( "ValidationException", ae.wrappedException.getClass().simpleName )
//        }
//
//        template.duration = 2
//        communicationLetterTemplateService.update([domainModel: template])
//    }
//
//    @Test
//    void testNoExpirationPublish() {
//        CommunicationLetterTemplate template = new CommunicationLetterTemplate(
//            name: "testNoExpirationPublish",
//            validFrom: new Date(),
//            validTo: null,
//            folder: defaultFolder
//        )
//        template = communicationLetterTemplateService.create( [domainModel: template] )
//        assertNotNull template.id
//        assertFalse template.published
//
//        assertCannotPublish( template, "letterHeadlineFieldRequiredToPublish" )
//        template.letterHeadline = "a letter headline"
//        template = communicationLetterTemplateService.publish( [domainModel: template] )
//        assertTrue template.published
//    }
//
//
//    @Test
//    void testDateTimeExpirationPublish() {
//        CommunicationLetterTemplate template = new CommunicationLetterTemplate(
//            name: "testDateTimeExpirationPublish",
//            validFrom: new Date(),
//            validTo: null,
//            folder: defaultFolder,
//            expirationPolicy: CommunicationLetterExpirationPolicy.DATE_TIME
//        )
//        template = communicationLetterTemplateService.create( [domainModel: template] )
//        assertNotNull template.id
//        assertFalse template.published
//
//        assertCannotPublish( template, "letterHeadlineFieldRequiredToPublish" )
//        template.letterHeadline = "a letter headline"
//        assertCannotPublish( template, "expirationDateRequiredToPublish" )
//
//        Date today = new Date()
//        DatumDependentDuration period = new DatumDependentDuration(0, 0, 2, 0, 0, 0, 0)
//        Date expirationDate = period + today
//        template.expirationDateTime = expirationDate
//        template = communicationLetterTemplateService.publish( [domainModel: template] )
//        assertTrue template.published
//    }
//
//    @Test
//    void testDurationExpirationPublish() {
//        CommunicationLetterTemplate template = new CommunicationLetterTemplate(
//                name: "testDurationExpirationPublish",
//                validFrom: new Date(),
//                validTo: null,
//                folder: defaultFolder,
//                expirationPolicy: CommunicationLetterExpirationPolicy.DURATION
//        )
//        template = communicationLetterTemplateService.create( [domainModel: template] )
//        assertNotNull template.id
//        assertFalse template.published
//
//        assertCannotPublish( template, "letterHeadlineFieldRequiredToPublish" )
//        template.letterHeadline = "a letter headline"
//        assertCannotPublish( template, "durationRequiredToPublish" )
//
//        template.duration = 5
//        template = communicationLetterTemplateService.publish( [domainModel: template] )
//        assertTrue template.published
//    }


    private void assertCannotPublish( CommunicationTemplate template, String reason ) {
        Boolean originalPublished = template.published
        try {
            communicationLetterTemplateService.publish( template )
            fail( reason )
        } catch (ApplicationException ae ) {
            assertApplicationException ae, reason
        } finally {
            template.published = originalPublished
        }
    }


}
