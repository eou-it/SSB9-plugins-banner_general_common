/*********************************************************************************
 Copyright 2015-2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.email

import net.hedtech.banner.general.communication.CommunicationManagementTestingSupport
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import static groovy.test.GroovyAssert.*

@Integration
@Rollback
class CommunicationEmailTemplateIntegrationTests extends BaseIntegrationTestCase {

    // template
    def i_valid_emailTemplate_name = """Valid Name"""
    def i_valid_emailTemplate_description = """Valid Template Description"""
    def i_valid_emailTemplate_personal = false
    def i_valid_emailTemplate_bccList = """Valid Emailtemplate Bcclist"""
    def i_valid_emailTemplate_ccList = """Valid Emailtemplate Cclist"""
    def i_valid_emailTemplate_content = """Valid Emailtemplate Content"""
    def i_valid_emailTemplate_fromList = """Valid Emailtemplate Fromlist"""
    def i_valid_emailTemplate_subject = """Valid Emailtemplate Subject"""
    def i_valid_emailTemplate_toList = """Valid Emailtemplate Tolist"""
    def i_valid_emailTemplate_oneOff = true
    def i_valid_emailTemplate_published = true
    def i_valid_emailTemplate_validFrom = new Date()
    def i_valid_emailTemplate_validTo = new Date()
    def i_valid_emailTemplate_createdBy = """Valid EmailTemplate createdBy"""
    def i_valid_emailTemplate_createDate = new Date()

    // invalid data
    def i_invalid_emailTemplate_bccList = "foo@bar.com".padLeft( 1021 )
    def i_invalid_emailTemplate_ccList = "foo@bar.com".padLeft( 1021 )
    def i_invalid_emailTemplate_fromList = "foo@bar.com".padLeft( 1021 )
    def i_invalid_emailTemplate_subject = """You're a winner!""".padLeft( 1021 )
    def i_invalid_emailTemplate_toList = "foo@bar.com".padLeft( 1021 )
    def i_invalid_emailTemplate_lastModifiedBy = "BCMUSER".padLeft( 31 )
    def i_invalid_emailTemplate_dataOrigin = "XE Communication Manager".padLeft( 31 )


    def CommunicationFolder folder


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }


    void setUpData() {
        folder = CommunicationManagementTestingSupport.newValidForCreateFolderWithSave()
        //Test if the generated entity now has an id assigned
        assertNotNull folder.id
        // Force the validTo into the future
        Calendar c = Calendar.getInstance()
        c.setTime( new Date() )
        c.add( Calendar.DATE, 150 )
        i_valid_emailTemplate_validTo = c.getTime()
        c.setTime( new Date() )
        c.add( Calendar.DATE, -300 )
        i_valid_emailTemplate_validFrom = c.getTime()
    }

    @Test
    void testCreateEmailTemplate() {

        def originalList = CommunicationEmailTemplate.findAll()

        def emailTemplate = newValidForCreateEmailTemplate(  )
        emailTemplate.save( failOnError: true, flush: true )
        //Test if the generated entity now has an id assigned
        assertNotNull emailTemplate.id
        assertEquals i_valid_emailTemplate_name, emailTemplate.name
        assertEquals i_valid_emailTemplate_description, emailTemplate.description
        assertEquals i_valid_emailTemplate_personal, emailTemplate.personal
        assertEquals i_valid_emailTemplate_bccList, emailTemplate.bccList
        assertEquals i_valid_emailTemplate_ccList, emailTemplate.ccList
        assertEquals i_valid_emailTemplate_content, emailTemplate.content
        assertEquals i_valid_emailTemplate_fromList, emailTemplate.fromList
        assertEquals i_valid_emailTemplate_subject, emailTemplate.subject
        assertEquals i_valid_emailTemplate_toList, emailTemplate.toList
        assertEquals "grails_user".toUpperCase(), emailTemplate.lastModifiedBy.toUpperCase()
        /* you can't predict what lastModified will get set to, so just check not null */
        assertNotNull emailTemplate.lastModified
        /* gets set to Banner by the framework */
        assertEquals "Banner", emailTemplate.dataOrigin
        assertEquals i_valid_emailTemplate_oneOff, emailTemplate.oneOff
        assertEquals i_valid_emailTemplate_published, emailTemplate.published
        assertEquals i_valid_emailTemplate_validFrom, emailTemplate.validFrom
        assertEquals i_valid_emailTemplate_validTo, emailTemplate.validTo
        assertEquals i_valid_emailTemplate_createdBy, emailTemplate.createdBy
        assertEquals i_valid_emailTemplate_createDate, emailTemplate.createDate
        assertFalse emailTemplate.systemIndicator

        // Now test findall
        def foundEmailTemplates = CommunicationEmailTemplate.findAll()
        assertEquals( originalList.size() + 1, foundEmailTemplates.size() )
    }


    @Test
    void testUpdate() {
        def emailTemplate = newValidForCreateEmailTemplate(  )
        emailTemplate.save( failOnError: true, flush: true )
        //Test if the generated entity now has an id assigned
        assertNotNull emailTemplate.id
        emailTemplate.bccList = i_valid_emailTemplate_bccList
        emailTemplate.ccList = i_valid_emailTemplate_ccList
        emailTemplate.content = i_valid_emailTemplate_content
        emailTemplate.fromList = i_valid_emailTemplate_fromList
        emailTemplate.subject = i_valid_emailTemplate_subject
        emailTemplate.toList = i_valid_emailTemplate_toList

        emailTemplate.save()
        def id = emailTemplate.id
        def updatedEmailTemplate = CommunicationEmailTemplate.get( id )
        assertEquals( "updated bccList                     ", i_valid_emailTemplate_bccList, updatedEmailTemplate.bccList )
        assertEquals( "updated ccList                      ", i_valid_emailTemplate_ccList, updatedEmailTemplate.ccList )
        assertEquals( "updated content                     ", i_valid_emailTemplate_content, updatedEmailTemplate.content )
        assertEquals( "updated fromList                    ", i_valid_emailTemplate_fromList, updatedEmailTemplate.fromList )
        assertEquals( "updated subject                     ", i_valid_emailTemplate_subject, updatedEmailTemplate.subject )
        assertEquals( "updated toList                      ", i_valid_emailTemplate_toList, updatedEmailTemplate.toList )
        assertFalse updatedEmailTemplate.systemIndicator
    }


    @Test
    void testCreateInValidEmailTemplate() {
        def emailTemplate = newValidForCreateEmailTemplate(  )

        emailTemplate.bccList = i_invalid_emailTemplate_bccList
        shouldFail { emailTemplate.save( failOnError: true, flush: true ) }

        emailTemplate = newValidForCreateEmailTemplate(  )
        emailTemplate.ccList = i_invalid_emailTemplate_ccList
        shouldFail { emailTemplate.save( failOnError: true, flush: true ) }


        emailTemplate = newValidForCreateEmailTemplate(  )
        emailTemplate.fromList = i_invalid_emailTemplate_fromList
        shouldFail { emailTemplate.save( failOnError: true, flush: true ) }


        emailTemplate = newValidForCreateEmailTemplate(  )
        emailTemplate.subject = i_invalid_emailTemplate_subject
        shouldFail { emailTemplate.save( failOnError: true, flush: true ) }


        emailTemplate = newValidForCreateEmailTemplate(  )
        emailTemplate.toList = i_invalid_emailTemplate_toList
        shouldFail { emailTemplate.save( failOnError: true, flush: true ) }


        emailTemplate = newValidForCreateEmailTemplate(  )
        emailTemplate.lastModifiedBy = i_invalid_emailTemplate_lastModifiedBy
        shouldFail { emailTemplate.save( failOnError: true, flush: true ) }

        emailTemplate = newValidForCreateEmailTemplate(  )
        emailTemplate.dataOrigin = i_invalid_emailTemplate_dataOrigin
        shouldFail { emailTemplate.save( failOnError: true, flush: true ) }


    }

    @Test
    void testExistsAnotherNameFolder() {

        def originalList = CommunicationEmailTemplate.findAll()

        def emailTemplate = newValidForCreateEmailTemplate(  )
        emailTemplate.save( failOnError: true, flush: true )
        //Test if the generated entity now has an id assigned
        assertNotNull emailTemplate.id
        assertEquals i_valid_emailTemplate_name, emailTemplate.name
        assertEquals i_valid_emailTemplate_description, emailTemplate.description
        assertEquals i_valid_emailTemplate_personal, emailTemplate.personal
        assertEquals i_valid_emailTemplate_bccList, emailTemplate.bccList
        assertEquals i_valid_emailTemplate_ccList, emailTemplate.ccList
        assertEquals i_valid_emailTemplate_content, emailTemplate.content
        assertEquals i_valid_emailTemplate_fromList, emailTemplate.fromList
        assertEquals i_valid_emailTemplate_subject, emailTemplate.subject
        assertEquals i_valid_emailTemplate_toList, emailTemplate.toList
        assertEquals "grails_user".toUpperCase(), emailTemplate.lastModifiedBy.toUpperCase()
        /* you can't predict what lastModified will get set to, so just check not null */
        assertNotNull emailTemplate.lastModified
        /* gets set to Banner by the framework */
        assertEquals "Banner", emailTemplate.dataOrigin
        assertEquals i_valid_emailTemplate_oneOff, emailTemplate.oneOff
        assertEquals i_valid_emailTemplate_published, emailTemplate.published
        assertEquals i_valid_emailTemplate_validFrom, emailTemplate.validFrom
        assertEquals i_valid_emailTemplate_validTo, emailTemplate.validTo
        assertEquals i_valid_emailTemplate_createdBy, emailTemplate.createdBy
        assertEquals i_valid_emailTemplate_createDate, emailTemplate.createDate
        assertFalse emailTemplate.systemIndicator

        // Now test findall
        def foundEmailTemplates = CommunicationEmailTemplate.findAll()
        assertEquals( originalList.size() + 1, foundEmailTemplates.size() )

        Boolean exists = emailTemplate.existsAnotherNameFolder(emailTemplate.id, emailTemplate.name, emailTemplate.folder.name)
        assertFalse exists

    }

    @Test
    void testFetchByTemplateNameAndFolderName() {

        def originalList = CommunicationEmailTemplate.findAll()

        def emailTemplate = newValidForCreateEmailTemplate(  )
        emailTemplate.save( failOnError: true, flush: true )
        //Test if the generated entity now has an id assigned
        assertNotNull emailTemplate.id
        assertEquals i_valid_emailTemplate_name, emailTemplate.name
        assertEquals i_valid_emailTemplate_description, emailTemplate.description
        assertEquals i_valid_emailTemplate_personal, emailTemplate.personal
        assertEquals i_valid_emailTemplate_bccList, emailTemplate.bccList
        assertEquals i_valid_emailTemplate_ccList, emailTemplate.ccList
        assertEquals i_valid_emailTemplate_content, emailTemplate.content
        assertEquals i_valid_emailTemplate_fromList, emailTemplate.fromList
        assertEquals i_valid_emailTemplate_subject, emailTemplate.subject
        assertEquals i_valid_emailTemplate_toList, emailTemplate.toList
        assertEquals "grails_user".toUpperCase(), emailTemplate.lastModifiedBy.toUpperCase()
        /* you can't predict what lastModified will get set to, so just check not null */
        assertNotNull emailTemplate.lastModified
        /* gets set to Banner by the framework */
        assertEquals "Banner", emailTemplate.dataOrigin
        assertEquals i_valid_emailTemplate_oneOff, emailTemplate.oneOff
        assertEquals i_valid_emailTemplate_published, emailTemplate.published
        assertEquals i_valid_emailTemplate_validFrom, emailTemplate.validFrom
        assertEquals i_valid_emailTemplate_validTo, emailTemplate.validTo
        assertEquals i_valid_emailTemplate_createdBy, emailTemplate.createdBy
        assertEquals i_valid_emailTemplate_createDate, emailTemplate.createDate
        assertFalse emailTemplate.systemIndicator

        // Now test findall
        def foundEmailTemplates = CommunicationEmailTemplate.findAll()
        assertEquals( originalList.size() + 1, foundEmailTemplates.size() )

        Boolean exists = emailTemplate.fetchByTemplateNameAndFolderName(emailTemplate.name, emailTemplate.folder.name)
        assertTrue exists

    }

    private def newValidForCreateEmailTemplate( ) {
        setUpData()
        def communicationTemplate = new CommunicationEmailTemplate(
                description: i_valid_emailTemplate_description,
                personal: i_valid_emailTemplate_personal,
                name: i_valid_emailTemplate_name,
                oneOff: i_valid_emailTemplate_oneOff,
                published: i_valid_emailTemplate_published,
                createdBy: i_valid_emailTemplate_createdBy,
                createDate: i_valid_emailTemplate_createDate,
                validFrom: i_valid_emailTemplate_validFrom,
                validTo: i_valid_emailTemplate_validTo,
                folder: folder,
                bccList: i_valid_emailTemplate_bccList,
                ccList: i_valid_emailTemplate_ccList,
                content: i_valid_emailTemplate_content,
                fromList: i_valid_emailTemplate_fromList,
                subject: i_valid_emailTemplate_subject,
                toList: i_valid_emailTemplate_toList, )
        return communicationTemplate
    }


}
