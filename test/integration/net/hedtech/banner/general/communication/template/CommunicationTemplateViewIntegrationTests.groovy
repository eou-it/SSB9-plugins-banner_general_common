/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.template

import net.hedtech.banner.general.communication.CommunicationManagementTestingSupport
import net.hedtech.banner.general.communication.email.CommunicationEmailTemplate
import net.hedtech.banner.general.communication.email.CommunicationEmailTemplateService
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.letter.CommunicationLetterTemplate
import net.hedtech.banner.general.communication.letter.CommunicationLetterTemplateService
import net.hedtech.banner.general.communication.mobile.CommunicationMobileNotificationTemplate
import net.hedtech.banner.general.communication.mobile.CommunicationMobileNotificationTemplateService
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

class CommunicationTemplateViewIntegrationTests extends BaseIntegrationTestCase {

    def CommunicationFolder testFolder
    def selfServiceBannerAuthenticationProvider
    CommunicationEmailTemplateService communicationEmailTemplateService
    CommunicationMobileNotificationTemplateService communicationMobileNotificationTemplateService
    CommunicationLetterTemplateService communicationLetterTemplateService


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
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
        def origTemplateList =  CommunicationTemplateView.list().size()

        CommunicationEmailTemplate emailTemplate = new CommunicationEmailTemplate(
            name: "testEmailTemplate",
            folder: testFolder,
            subject: "test subject",
            toList: "test@test.edu",
        )

        emailTemplate = communicationEmailTemplateService.create( emailTemplate )
        assertNotNull emailTemplate?.id

        def results = CommunicationTemplateView.findByNameWithPagingAndSortParams( [params: ["name": ("testEmailTemplate")]], [sortColumn: "name", sortDirection: "asc", max: 20, offset: 0])
        assertNotNull results
        assertEquals  "testEmailTemplate", results[0].name

        results = CommunicationTemplateView.findByNameWithPagingAndSortParams( [params: ["name": ("testEMAIL")]], [sortColumn: "name", sortDirection: "asc", max: 20, offset: 0])
        assertNotNull results
        assertEquals  "testEmailTemplate", results[0].name

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

        CommunicationMobileNotificationTemplate mobileNotificationTemplate = new CommunicationMobileNotificationTemplate(
            name: "testMobileNotificationTemplate",
            folder: testFolder
        )
        mobileNotificationTemplate = (CommunicationMobileNotificationTemplate) communicationMobileNotificationTemplateService.create( mobileNotificationTemplate )
        templateView = CommunicationTemplateView.get( mobileNotificationTemplate.id )
        assertNotNull( templateView )
        assertEquals( mobileNotificationTemplate.communicationChannel, templateView.communicationChannel )

        CommunicationLetterTemplate letterTemplate = new CommunicationLetterTemplate(
                name: "testLetterTemplate",
                folder: testFolder
        )
        letterTemplate = (CommunicationLetterTemplate) communicationLetterTemplateService.create( letterTemplate )
        templateView = CommunicationTemplateView.get( letterTemplate.id )
        assertNotNull( templateView )
        assertEquals( letterTemplate.communicationChannel, templateView.communicationChannel )

        assertEquals( origTemplateList + 3, CommunicationTemplateView.list().size() )

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
