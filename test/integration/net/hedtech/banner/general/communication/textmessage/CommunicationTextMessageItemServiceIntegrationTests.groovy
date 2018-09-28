/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.textmessage

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.folder.CommunicationFolderService
import net.hedtech.banner.general.communication.item.CommunicationChannel
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Tests crud methods provided by communication text message item service.
 */
class CommunicationTextMessageItemServiceIntegrationTests extends BaseIntegrationTestCase {

    CommunicationTextMessageItemService communicationTextMessageItemService
    CommunicationTextMessageTemplateService communicationTextMessageTemplateService
    def selfServiceBannerAuthenticationProvider
    CommunicationFolderService communicationFolderService

    CommunicationFolder folder
    CommunicationTextMessageTemplate textMessageTemplate

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()

        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('BCMADMIN', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)

        folder = new CommunicationFolder()
        folder.name = "test folder"
        folder = (CommunicationFolder) communicationFolderService.create( folder )

        textMessageTemplate = new CommunicationTextMessageTemplate()
        textMessageTemplate.name = "mobile notification template"
        textMessageTemplate.folder = folder
        textMessageTemplate.message = "test message"
        textMessageTemplate.footer = "footer"
        textMessageTemplate = (CommunicationTextMessageTemplate) communicationTextMessageTemplateService.create( textMessageTemplate )
        textMessageTemplate = (CommunicationTextMessageTemplate) communicationTextMessageTemplateService.publish( textMessageTemplate )
    }

    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }

    @Test
    void testCreate() {
        Long recipientPidm = 49152
        String createdBy = 'MBRZYCKI'
        String referenceId = UUID.randomUUID().toString()

        CommunicationTextMessageItem textMessageItem = new CommunicationTextMessageItem()
        textMessageItem.serverResponse = "test Server Response"
        textMessageItem.templateId = textMessageTemplate.id
        textMessageItem.referenceId = referenceId
        textMessageItem.createdBy = createdBy
        textMessageItem.recipientPidm = recipientPidm
        textMessageItem = (CommunicationTextMessageItem) communicationTextMessageItemService.create( textMessageItem )

        assertNotNull( textMessageItem.id )
        assertEquals( CommunicationChannel.TEXT_MESSAGE, textMessageItem.communicationChannel )
        assertEquals( "test Server Response", textMessageItem.serverResponse )
        assertEquals( textMessageTemplate.id, textMessageItem.templateId )
        assertEquals( referenceId, textMessageItem.referenceId )
        assertEquals( createdBy, textMessageItem.createdBy )
        assertEquals( recipientPidm, textMessageItem.recipientPidm )

        assertNotNull( communicationTextMessageItemService.get( textMessageItem.id ) )
    }

    @Test
    void testDelete() {
        CommunicationTextMessageItem textMessageItem = new CommunicationTextMessageItem()
        textMessageItem.serverResponse = "test Server Response"
        textMessageItem.templateId = textMessageTemplate.id
        textMessageItem.referenceId = UUID.randomUUID().toString()
        textMessageItem.createdBy = 'MBRZYCKI'
        textMessageItem.recipientPidm = 49152
        textMessageItem = (CommunicationTextMessageItem) communicationTextMessageItemService.create( textMessageItem )

        assertNotNull( communicationTextMessageItemService.get( textMessageItem.id ) )

        communicationTextMessageItemService.delete( textMessageItem )

        try {
            assertNull( communicationTextMessageItemService.get( textMessageItem.id ) )
            fail "Expected NotFoundException"
        } catch (ApplicationException e) {
            assertEquals( new NotFoundException([id:textMessageItem.id, entityClassName:textMessageItem.getClass().getSimpleName()]).message, e.message )
        }
    }

}