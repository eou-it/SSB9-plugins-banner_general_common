/** *****************************************************************************
 © 2014 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package net.hedtech.banner.general.communication.template

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.field.CommunicationField
import net.hedtech.banner.general.communication.field.CommunicationFieldStatus
import net.hedtech.banner.general.communication.field.CommunicationRuleStatementType
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

class CommunicationTemplateMergeServiceTests extends BaseIntegrationTestCase {

    def communicationEmailTemplateService
    def communicationTemplateMergeService
    def communicationFieldService


    def i_valid_emailTemplate_active = true
    def i_valid_emailTemplate_bccList = """Valid Emailtemplate Bcclist"""
    def i_valid_emailTemplate_ccList = """Valid Emailtemplate Cclist"""
    def i_valid_emailTemplate_content = """Valid Emailtemplate Content"""
    def i_valid_emailTemplate_description = """Valid Template Description"""
    def i_valid_emailTemplate_fromList = """Valid Emailtemplate Fromlist"""
    def i_valid_emailTemplate_name = """Valid Name"""
    def i_valid_emailTemplate_oneOff = true
    def i_valid_emailTemplate_personal = true
    def i_valid_emailTemplate_published = true
    def i_valid_emailTemplate_subject = """Valid Emailtemplate Subject"""
    def i_valid_emailTemplate_toList = """Valid Emailtemplate Tolist"""
    def i_valid_emailTemplate_validFrom = new Date()
    def i_valid_emailTemplate_validTo = new Date()
    def i_valid_folder_description = "Valid older description"
    def i_valid_folder_internal = true
    def i_valid_folder_name1 = "Valid Folder1 Name"
    def i_valid_folder_name2 = "Valid Folder2 Name"

    def CommunicationFolder folder1
    def CommunicationFolder folder2


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        folder1 = newValidForCreateFolder( i_valid_folder_name1 )
        folder1.save( failOnError: true, flush: true )
        //Test if the generated entity now has an id assigned
        assertNotNull folder1.id
        folder2 = newValidForCreateFolder( i_valid_folder_name2 )
        folder2.save( failOnError: true, flush: true )
        //Test if the generated entity now has an id assigned
        assertNotNull folder2.id

    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testInvalidDataFields() {
        def String templateString = "some template data with \$variable1\$ and \$variable2\$ should both fail"
        shouldFail( ApplicationException ) {
            communicationTemplateMergeService.processTemplate( templateString )
        }
    }


    @Test
    void testValidMerge() {
        CommunicationEmailTemplate emailTemplate = newValidForCreateEmailTemplate()
        emailTemplate.content = """Hi \$firstname\$ \$lastname\$"""
        emailTemplate.save()
        shouldFail( ApplicationException ) {
            communicationTemplateMergeService.processTemplate( emailTemplate.content )
        }
    }


    @Test
    void testRenderPreviewTemplate() {
        CommunicationEmailTemplate emailTemplate = newValidForCreateEmailTemplate()
        def CommunicationField testCommunicationField
        emailTemplate.content = """\$Salutation\$
            \$invitation\$
            \$signed\$"""
        emailTemplate.subject = "REQUEST FOR URGENT BUSINESS RELATIONSHIP "
        emailTemplate.save()

        // Now set up the fields

        testCommunicationField = newCommunicationField( "Salutation", "Greetings \$fn\$ \$ln\$,", "Greetings George Washington,", null )
        communicationFieldService.create( [domainModel: testCommunicationField] )

        testCommunicationField = newCommunicationField( "invitation", "\$IntroParagraph\$", """First, I must solicit your strictest confidence in this transaction. this is by
        virtue of its nature as being utterly confidential and 'top secret'. I am sure
        and have confidence of your ability and reliability to prosecute a transaction
        of this great magnitude involving a pending transaction requiring maxiimum
        confidence.""", null )
        communicationFieldService.create( [domainModel: testCommunicationField] )


        testCommunicationField = newCommunicationField( "signed", "", """Yours Faithfully,
            DR CLEMENT OKON """, null )
        communicationFieldService.create( [domainModel: testCommunicationField] )

        //(String name, String formatString, String previewValue, String ruleContent )
        String result = communicationTemplateMergeService.renderPreviewTemplate( emailTemplate.content )
        println result
        assertEquals( """Greetings George Washington,
                    First, I must solicit your strictest confidence in this transaction. this is by
                            virtue of its nature as being utterly confidential and 'top secret'. I am sure
                            and have confidence of your ability and reliability to prosecute a transaction
                            of this great magnitude involving a pending transaction requiring maxiimum
                            confidence.
                    Yours Faithfully,
                                DR CLEMENT OKON """,result )
    }


    @Test
    void testProcessGroovyTemplate() {
        def text = 'Dear "$firstname $lastname",\nSo nice to meet you in <% print city %>.\nSee you in ${month},\n${signed}'
        def bindings = ["firstname": "Sam", "lastname": "Pullara", "city": "San Francisco", "month": "December", "signed": "Groovy-Dev"]
        def renderedTemplate = communicationTemplateMergeService.processGroovyTemplate( text, bindings )
        def result = 'Dear "Sam Pullara",\nSo nice to meet you in San Francisco.\nSee you in December,\nGroovy-Dev'
        assert result == renderedTemplate.toString()
    }


    private def newValidForCreateFolder( String folderName ) {
        def folder = new CommunicationFolder(
                description: i_valid_folder_description,
                internal: i_valid_folder_internal,
                name: folderName
        )
        return folder
    }


    private def newValidForCreateEmailTemplate( CommunicationFolder folder ) {
        def communicationTemplate = new CommunicationEmailTemplate(
                description: i_valid_emailTemplate_description,
                personal: i_valid_emailTemplate_personal,
                name: i_valid_emailTemplate_name,
                active: i_valid_emailTemplate_active,
                oneOff: i_valid_emailTemplate_oneOff,
                published: i_valid_emailTemplate_published,
                validFrom: i_valid_emailTemplate_validFrom,
                validTo: i_valid_emailTemplate_validTo,
                folder: folder,
                bccList: i_valid_emailTemplate_bccList,
                ccList: i_valid_emailTemplate_ccList,
                content: i_valid_emailTemplate_content,
                fromList: i_valid_emailTemplate_fromList,
                subject: i_valid_emailTemplate_subject,
                toList: i_valid_emailTemplate_toList,

                )

        return communicationTemplate
    }


    private def newCommunicationField( String name, String formatString, String previewValue, String ruleContent ) {
        def communicationField = new CommunicationField(
                // Required fields
                folder: folder1,
                //immutableId: UUID.randomUUID().toString(),
                name: name,
                returnsArrayArguments: false,

                // Nullable fields
                description: name + " test",
                formatString: formatString,
                groovyFormatter: "TTTTTTTT",
                previewValue: previewValue,
                renderAsHtml: true,
                ruleUri: null,
                status: CommunicationFieldStatus.DEVELOPMENT,
                statmentType: CommunicationRuleStatementType.SQL_PREPARED_STATEMENT,
                ruleContent: ruleContent
        )

        return communicationField
    }


}
