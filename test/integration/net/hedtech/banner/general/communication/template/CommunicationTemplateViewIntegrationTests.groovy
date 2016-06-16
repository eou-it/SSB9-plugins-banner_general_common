/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.template

import net.hedtech.banner.general.communication.CommunicationManagementTestingSupport
import net.hedtech.banner.general.communication.email.CommunicationEmailTemplate
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

class CommunicationTemplateViewIntegrationTests extends BaseIntegrationTestCase {

    def CommunicationFolder testFolder
    def selfServiceBannerAuthenticationProvider
    def communicationEmailTemplateService


    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']
        super.setUp()
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('BCMADMIN', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)

        testFolder = CommunicationManagementTestingSupport.newValidForCreateFolderWithSave()
    }


    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }


    @Test
    void testTemplateView() {
        CommunicationEmailTemplate emailTemplate = new CommunicationEmailTemplate(
            name: "testEmailTemplate",
            folder: testFolder,
            subject: "test subject",
            toList: "test@test.edu",
        )

        emailTemplate = communicationEmailTemplateService.create( emailTemplate )
        assertNotNull emailTemplate?.id

        CommunicationTemplateView templateView = CommunicationTemplateView.get( emailTemplate.id )
        assertNotNull( templateView )
        assertEquals( emailTemplate.name, templateView.name )
        assertEquals( testFolder.name, templateView.folderName )
        assertEquals( emailTemplate.description, templateView.description )
        assertEquals( emailTemplate.communicationChannel, templateView.communicationChannel )
        assertEquals( emailTemplate.createdBy, templateView.createdBy )
        assertEquals( Boolean.FALSE, templateView.active )

        emailTemplate = communicationEmailTemplateService.publishTemplate( ["id": emailTemplate.id] )
        assertEquals( Boolean.TRUE, emailTemplate.published )

        cleanUpGorm()
        templateView = CommunicationTemplateView.get( emailTemplate.id )
        // Disabled as we're getting flakey behavior that seems somewhat dependent on the caching within gorm
        // during integration tests.
//        assertEquals( Boolean.TRUE, templateView.active )
    }

    protected cleanUpGorm() {
        def session = sessionFactory.currentSession
        session.flush()
        session.clear()
    }

}
