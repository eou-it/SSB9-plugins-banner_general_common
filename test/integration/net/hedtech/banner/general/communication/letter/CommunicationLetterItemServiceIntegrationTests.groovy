/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.general.communication.letter

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
 * Tests crud methods provided by communication letter item service.
 */
class CommunicationLetterItemServiceIntegrationTests extends BaseIntegrationTestCase {

    CommunicationLetterItemService communicationLetterItemService
    CommunicationLetterTemplateService communicationLetterTemplateService
    def selfServiceBannerAuthenticationProvider
    CommunicationFolderService communicationFolderService

    CommunicationFolder folder
    CommunicationLetterTemplate letterTemplate

    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']
        super.setUp()

        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('BCMADMIN', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)

        folder = new CommunicationFolder()
        folder.name = "test folder"
        folder = (CommunicationFolder) communicationFolderService.create( folder )

        letterTemplate = new CommunicationLetterTemplate()
        letterTemplate.name = "letterTemplate"
        letterTemplate.folder = folder
        letterTemplate.toAddress = "test to address"
        letterTemplate.content = "test content"
        letterTemplate = (CommunicationLetterTemplate) communicationLetterTemplateService.create( letterTemplate )
        letterTemplate = (CommunicationLetterTemplate) communicationLetterTemplateService.publish( letterTemplate )
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

        CommunicationLetterItem letterItem = new CommunicationLetterItem()
        letterItem.toAddress = "result to address"
        letterItem.content = "result content"
        letterItem.templateId = letterTemplate.id
        letterItem.referenceId = referenceId
        letterItem.createdBy = createdBy
        letterItem.recipientPidm = recipientPidm
        letterItem = (CommunicationLetterItem) communicationLetterItemService.create( letterItem )

        assertNotNull( letterItem.id )
        assertEquals( CommunicationChannel.LETTER, letterItem.communicationChannel )
        assertEquals( "result to address", letterItem.toAddress )
        assertEquals( "result content", letterItem.content )
        assertEquals( letterTemplate.id, letterItem.templateId )
        assertEquals( referenceId, letterItem.referenceId )
        assertEquals( createdBy, letterItem.createdBy )
        assertEquals( recipientPidm, letterItem.recipientPidm )

        assertNotNull( communicationLetterItemService.get( letterItem.id ) )
    }

    @Test
    void testDelete() {
        CommunicationLetterItem letterItem = new CommunicationLetterItem()
        letterItem.toAddress = "result to address"
        letterItem.content = "result content"
        letterItem.templateId = letterTemplate.id
        letterItem.referenceId = UUID.randomUUID().toString()
        letterItem.createdBy = 'MBRZYCKI'
        letterItem.recipientPidm = 49152
        letterItem = (CommunicationLetterItem) communicationLetterItemService.create( letterItem )

        assertNotNull( communicationLetterItemService.get( letterItem.id ) )

        communicationLetterItemService.delete( letterItem )

        try {
            assertNull( communicationLetterItemService.get( letterItem.id ) )
            fail "Expected NotFoundException"
        } catch (ApplicationException e) {
            assertEquals( new NotFoundException([id:letterItem.id, entityClassName:letterItem.getClass().getSimpleName()]).message, e.message )
        }
    }

}
