/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.letter

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.folder.CommunicationFolder
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
        formContext = ['GUAGMNU']
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

    @Test
    void testDelete() {
        Date today = new Date()
        CommunicationLetterTemplate template = new CommunicationLetterTemplate(
                name: "testDelete",
                validFrom: today,
                folder: defaultFolder
        )

        template = communicationLetterTemplateService.create( template )
        assertNotNull template.id

        assertNotNull( CommunicationLetterTemplate.get( template.id ) )

        communicationTemplateService.delete( template )

        assertNull( CommunicationLetterTemplate.get( template.id ) )
    }

    @Test
    void testNoToAddressPublish() {
        CommunicationLetterTemplate template = new CommunicationLetterTemplate(
                name: "testUpdateTemplate",
                validFrom: new Date(),
                validTo: null,
                folder: defaultFolder,
                content: "testToAddressCannotBeNull"
        )

        template = (CommunicationLetterTemplate) communicationLetterTemplateService.create([domainModel: template])

        try {
            template = (CommunicationLetterTemplate) communicationLetterTemplateService.publish( template )
            fail "Expected template to fail publish because of no toAddress"
        } catch (ApplicationException e ) {
            assertEquals( "@@r1:toAddressRequiredToPublish@@", e.message )
        }

        template.toAddress = "Los Angeles"
        template = (CommunicationLetterTemplate) communicationLetterTemplateService.publish(template)
        assertTrue template.published
    }

    @Test
    void testNoContentPublish() {
        CommunicationLetterTemplate template = new CommunicationLetterTemplate(
                name: "testUpdateTemplate",
                validFrom: new Date(),
                validTo: null,
                folder: defaultFolder,
                toAddress: "attn: santa claus, 1 north pole way, north pole"
        )

        template = (CommunicationLetterTemplate) communicationLetterTemplateService.create([domainModel: template])

        try {
            template = (CommunicationLetterTemplate) communicationLetterTemplateService.publish(template)
            fail "Expected template to fail publish because of no content"
        } catch (ApplicationException e) {
            assertEquals("@@r1:contentRequiredToPublish@@", e.message)
        }

        template.content = "A deployment of BCM"
        template = (CommunicationLetterTemplate) communicationLetterTemplateService.publish(template)
        assertTrue template.published
    }

}
