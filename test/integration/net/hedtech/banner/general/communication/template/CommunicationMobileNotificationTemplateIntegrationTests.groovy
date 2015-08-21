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
        defaultFolder = newValidForCreateFolder()
        defaultFolder.save( failOnError: true, flush: true )
        //Test if the generated entity now has an id assigned
        assertNotNull folder.id
    }

    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }

//    private void assignValidToWithFutureDate( CommunicationTemplate template ) {
//        Calendar c = Calendar.getInstance()
//        c.setTime( new Date() )
//        c.add( Calendar.DATE, 150 )
//        template = c.getTime()
//    }
//
//    private void assignValidToWithPastDate( CommunicationTemplate template ) {
//        Calendar c = Calendar.getInstance()
//        c.setTime( new Date() )
//        c.add( Calendar.DATE, -300 )
//        template = c.getTime()
//    }


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

    }

    @Test
    void testDelete() {
        def emailTemplate = newValidForCreateEmailTemplate( folder )
        emailTemplate.save( failOnError: true, flush: true )
        //Test if the generated entity now has an id assigned
        assertNotNull emailTemplate.id
        def id = emailTemplate.id
        emailTemplate.delete()
        assertNull CommunicationEmailTemplate.get( id )
    }


//    @Test
//    void testCreateEmailTemplate() {
//
//        def originalList = CommunicationEmailTemplate.findAll()
//
//        def emailTemplate = newValidForCreateEmailTemplate( folder )
//        emailTemplate.save( failOnError: true, flush: true )
//        //Test if the generated entity now has an id assigned
//        assertNotNull emailTemplate.id
//        assertEquals i_valid_emailTemplate_name, emailTemplate.name
//        assertEquals i_valid_emailTemplate_description, emailTemplate.description
//        assertEquals i_valid_emailTemplate_personal, emailTemplate.personal
//        assertEquals i_valid_emailTemplate_bccList, emailTemplate.bccList
//        assertEquals i_valid_emailTemplate_ccList, emailTemplate.ccList
//        assertEquals i_valid_emailTemplate_content, emailTemplate.content
//        assertEquals i_valid_emailTemplate_fromList, emailTemplate.fromList
//        assertEquals i_valid_emailTemplate_subject, emailTemplate.subject
//        assertEquals i_valid_emailTemplate_toList, emailTemplate.toList
//        assertEquals "grails_user".toUpperCase(), emailTemplate.lastModifiedBy.toUpperCase()
//        /* you can't predict what lastModified will get set to, so just check not null */
//        assertNotNull emailTemplate.lastModified
//        /* gets set to Banner by the framework */
//        assertEquals "Banner", emailTemplate.dataOrigin
//        assertEquals i_valid_emailTemplate_oneOff, emailTemplate.oneOff
//        assertEquals i_valid_emailTemplate_published, emailTemplate.published
//        assertEquals i_valid_emailTemplate_validFrom, emailTemplate.validFrom
//        assertEquals i_valid_emailTemplate_validTo, emailTemplate.validTo
//        assertEquals i_valid_emailTemplate_createdBy, emailTemplate.createdBy
//        assertEquals i_valid_emailTemplate_createDate, emailTemplate.createDate
//
//        // Now test findall
//        def foundEmailTemplates = CommunicationEmailTemplate.findAll()
//        assertEquals( originalList.size() + 1, foundEmailTemplates.size() )
//    }


//    @Test
//    void testUpdate() {
//        def emailTemplate = newValidForCreateEmailTemplate( folder )
//        emailTemplate.save( failOnError: true, flush: true )
//        //Test if the generated entity now has an id assigned
//        assertNotNull emailTemplate.id
//        emailTemplate.bccList = i_valid_emailTemplate_bccList
//        emailTemplate.ccList = i_valid_emailTemplate_ccList
//        emailTemplate.content = i_valid_emailTemplate_content
//        emailTemplate.fromList = i_valid_emailTemplate_fromList
//        emailTemplate.subject = i_valid_emailTemplate_subject
//        emailTemplate.toList = i_valid_emailTemplate_toList
//
//        emailTemplate.save()
//        def id = emailTemplate.id
//        def updatedEmailTemplate = CommunicationEmailTemplate.get( id )
//        assertEquals( "updated bccList                     ", i_valid_emailTemplate_bccList, updatedEmailTemplate.bccList )
//        assertEquals( "updated ccList                      ", i_valid_emailTemplate_ccList, updatedEmailTemplate.ccList )
//        assertEquals( "updated content                     ", i_valid_emailTemplate_content, updatedEmailTemplate.content )
//        assertEquals( "updated fromList                    ", i_valid_emailTemplate_fromList, updatedEmailTemplate.fromList )
//        assertEquals( "updated subject                     ", i_valid_emailTemplate_subject, updatedEmailTemplate.subject )
//        assertEquals( "updated toList                      ", i_valid_emailTemplate_toList, updatedEmailTemplate.toList )
//
//    }
//
//
//    @Test
//    void testCreateInValidEmailTemplate() {
//        def emailTemplate = newValidForCreateEmailTemplate( folder )
//
//        emailTemplate.bccList = i_invalid_emailTemplate_bccList
//        shouldFail { emailTemplate.save( failOnError: true, flush: true ) }
//
//        emailTemplate = newValidForCreateEmailTemplate( folder )
//        emailTemplate.ccList = i_invalid_emailTemplate_ccList
//        shouldFail { emailTemplate.save( failOnError: true, flush: true ) }
//
//
//        emailTemplate = newValidForCreateEmailTemplate( folder )
//        emailTemplate.fromList = i_invalid_emailTemplate_fromList
//        shouldFail { emailTemplate.save( failOnError: true, flush: true ) }
//
//
//        emailTemplate = newValidForCreateEmailTemplate( folder )
//        emailTemplate.subject = i_invalid_emailTemplate_subject
//        shouldFail { emailTemplate.save( failOnError: true, flush: true ) }
//
//
//        emailTemplate = newValidForCreateEmailTemplate( folder )
//        emailTemplate.toList = i_invalid_emailTemplate_toList
//        shouldFail { emailTemplate.save( failOnError: true, flush: true ) }
//
//
//        emailTemplate = newValidForCreateEmailTemplate( folder )
//        emailTemplate.lastModifiedBy = i_invalid_emailTemplate_lastModifiedBy
//        shouldFail { emailTemplate.save( failOnError: true, flush: true ) }
//
//        emailTemplate = newValidForCreateEmailTemplate( folder )
//        emailTemplate.dataOrigin = i_invalid_emailTemplate_dataOrigin
//        shouldFail { emailTemplate.save( failOnError: true, flush: true ) }
//
//
//    }
//
//
//    @Test
//    void testFetchPublishedActivePublicByFolderId() {
//
//        def emailTemplate = newValidForCreateEmailTemplate( folder )
//        emailTemplate.save( failOnError: true, flush: true )
//        println "From: " + emailTemplate.validFrom
//        println "To:" + emailTemplate.validTo
//        assertNotNull( emailTemplate.folder.name )
//        def emailTemplates = CommunicationTemplate.fetchPublishedActivePublicByFolderId( folder.id )
//        assertEquals( 1, emailTemplates.size() )
//    }
//
//
//    private def newValidForCreateFolder() {
//        def folder = new CommunicationFolder(
//                description: i_valid_folder_description,
//                internal: i_valid_folder_internal,
//                name: i_valid_folder_name
//        )
//        return folder
//    }


//    private def newValidForCreateEmailTemplate( CommunicationFolder folder ) {
//
//        def communicationTemplate = new CommunicationEmailTemplate(
//                description: i_valid_emailTemplate_description,
//                personal: i_valid_emailTemplate_personal,
//                name: i_valid_emailTemplate_name,
//                oneOff: i_valid_emailTemplate_oneOff,
//                published: i_valid_emailTemplate_published,
//                createdBy: i_valid_emailTemplate_createdBy,
//                createDate: i_valid_emailTemplate_createDate,
//                validFrom: i_valid_emailTemplate_validFrom,
//                validTo: i_valid_emailTemplate_validTo,
//                folder: folder,
//                bccList: i_valid_emailTemplate_bccList,
//                ccList: i_valid_emailTemplate_ccList,
//                content: i_valid_emailTemplate_content,
//                fromList: i_valid_emailTemplate_fromList,
//                subject: i_valid_emailTemplate_subject,
//                toList: i_valid_emailTemplate_toList, )
//        return communicationTemplate
//    }


}
