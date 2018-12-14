/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.textmessage

import groovy.time.DatumDependentDuration
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.mobile.CommunicationMobileNotificationExpirationPolicy
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
class CommunicationTextMessageTemplateServiceIntegrationTests extends BaseIntegrationTestCase {

    def communicationTemplateService
    def communicationTextMessageTemplateService
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
        CommunicationTextMessageTemplate template = new CommunicationTextMessageTemplate(
                name: "testCreate",
                validFrom: today,
                folder: defaultFolder
        )

        template = communicationTextMessageTemplateService.create([domainModel: template])
        assertNotNull template.createDate
        assertNotNull template.createdBy
        assertNotNull template.lastModified
        assertNotNull template.lastModifiedBy
        assertEquals today, template.validFrom
        assertNull template.validTo
        assertFalse template.systemIndicator

        def templateList = communicationTemplateService.findAll()
        assertEquals( 1, templateList.size() )
    }


    @Test
    void testUpdateTemplate() {
        CommunicationTextMessageTemplate template = new CommunicationTextMessageTemplate(
                name: "testUpdateTemplate",
                validFrom: new Date(),
                validTo: null,
                folder: defaultFolder,
                message: "test message"
        )
        template = communicationTextMessageTemplateService.create([domainModel: template])
        assertNotNull template.id

        template.description = "Updated description"
        template = communicationTextMessageTemplateService.update([domainModel: template])
        assertEquals( "Updated description", template.description )
    }

}
