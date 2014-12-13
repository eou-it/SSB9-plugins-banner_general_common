/** *****************************************************************************
 © 2014 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package net.hedtech.banner.general.communication.template

import net.hedtech.banner.general.communication.field.CommunicationField
import net.hedtech.banner.general.communication.field.CommunicationFieldStatus
import net.hedtech.banner.general.communication.field.CommunicationRuleStatementType
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.merge.CommunicationFieldValue
import net.hedtech.banner.general.communication.merge.CommunicationRecipientData
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

class CommunicationTemplateMergeServiceTests extends BaseIntegrationTestCase {

    def communicationTemplateMergeService
    def communicationFieldService
    def communicationFieldCalculationService
    def communicationRecipientDataService
    def communicationOrganizationService

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
    def i_valid_pidm = 1299
    def CommunicationFolder folder1
    def CommunicationFolder folder2
    def CommunicationOrganization i_valid_Organization

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
        i_valid_Organization = createNewCommunicationOrganization()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    /* The sunny day test */
    void testMergeTemplate() {
        /* create the template */
        CommunicationEmailTemplate emailTemplate = newValidForCreateEmailTemplate()
        /* it doesn't matter what we set them to, as long as they contain personalization fields, we can test that they get rendered */
        emailTemplate.content = """Hi \$firstname\$ \$lastname\$"""
        emailTemplate.toList = """Hi \$firstname\$ \$lastname\$"""
        emailTemplate.subject = """Hi \$firstname\$ \$lastname\$"""
        emailTemplate.save( failOnError: true, flush: true )

        // Now set up the data fields
        CommunicationField tempfield
        def testCommunicationField = newCommunicationField( "firstname", "\$firstname\$", "George", "Select spriden_first_name firstname from spriden where spriden_pidm = :pidm and spriden_change_ind is null" )
        tempfield = communicationFieldService.create( [domainModel: testCommunicationField] )
        assertNotNull tempfield.immutableId

        testCommunicationField = newCommunicationField( "lastname", "\$lastname\$", "George", "Select spriden_last_name lastname from spriden where spriden_pidm = :pidm and spriden_change_ind is null" )
        tempfield = communicationFieldService.create( [domainModel: testCommunicationField] )
        assertNotNull tempfield.immutableId

        // Now extract the template variables and calculate their values and save as RecipientData
        def templateVariables = communicationTemplateMergeService.extractTemplateVariables( emailTemplate )

        String fieldCalculationResult

        CommunicationRecipientData communicationRecipientData
        def fieldListByPidm = [:]
        params = [:]
        params << ['pidm': i_valid_pidm]

        println templateVariables
        templateVariables.each {
            variableName ->
                println variableName
                tempfield = CommunicationField.fetchByName( variableName )
                assertNotNull( tempfield.immutableId )
                fieldCalculationResult = communicationFieldCalculationService.calculateField( tempfield.immutableId, params )
                fieldListByPidm.put( tempfield.name, new CommunicationFieldValue(
                        value: fieldCalculationResult,
                        renderAsHtml: false ) )
        }
        communicationRecipientData = new CommunicationRecipientData(
                pidm: i_valid_pidm,
                templateId: emailTemplate.id,
                organization: i_valid_Organization,
                referenceId: 1,
                ownerId: getUser(),
                fieldValues: fieldListByPidm
        )

        communicationRecipientDataService.create( communicationRecipientData )

        /* Now merge the recipient data into the template */
        CommunicationMergedEmailTemplate communicationMergedEmailTemplate = communicationTemplateMergeService.mergeEmailTemplate( emailTemplate, communicationRecipientData )
        assertNotNull( communicationMergedEmailTemplate )
        assertEquals( "Hi Chubby Checker", communicationMergedEmailTemplate.toList )
        assertEquals( "Hi Chubby Checker", communicationMergedEmailTemplate.subject )
        assertEquals( "Hi Chubby Checker", communicationMergedEmailTemplate.content )


    }


    @Test
    void testInvalidDataFields() {
        CommunicationEmailTemplate emailTemplate = newValidForCreateEmailTemplate()
        emailTemplate.content = """Hi \$firstname\$ \$lastname\$"""
        emailTemplate.save( failOnError: true, flush: true )

        // do not set up any fields, you should not get any exception
        CommunicationMergedEmailTemplate communicationMergedEmailTemplate = communicationTemplateMergeService.calculateTemplateByBannerId( emailTemplate.id, "17" )
        //CommunicationMergedEmailTemplate communicationMergedEmailTemplate = communicationTemplateMergeService.mergeEmailTemplate( emailTemplate, communicationRecipientData )
        assertNotNull( communicationMergedEmailTemplate )
        /* No errors expect, just nulls returned */
        assertEquals( "Hi  ", communicationMergedEmailTemplate.content )
    }


    @Test
    void testRenderPreviewTemplate() {
        CommunicationEmailTemplate emailTemplate = newValidForCreateEmailTemplate()
        def CommunicationField testCommunicationField
        emailTemplate.content = """
\$code, reason:{ c,r |
\t<li>\$c\$, \$r\$</li>
}\$
\$Salutation\$
        \$invitation\$
        \$signed\$"""
        emailTemplate.subject = "REQUEST FOR URGENT BUSINESS RELATIONSHIP "
        emailTemplate.save( failOnError: true, flush: true )

        // Now set up the fields

        testCommunicationField = newCommunicationField( "Salutation", "Greetings \$fn\$ \$ln\$,", "Greetings George Washington,", null )
        communicationFieldService.create( [domainModel: testCommunicationField] )

        testCommunicationField = newCommunicationField( "invitation", "\$IntroParagraph\$",
                                                        """First, I must solicit your strictest confidence in this transaction. this is by virtue of its nature as being utterly confidential and 'top secret'.""", null )
        communicationFieldService.create( [domainModel: testCommunicationField] )

        testCommunicationField = newCommunicationField( "signed", "", """Yours Faithfully, DR CLEMENT OKON """, null )
        communicationFieldService.create( [domainModel: testCommunicationField] )


        CommunicationMergedEmailTemplate result = communicationTemplateMergeService.renderPreviewTemplate( emailTemplate )
        CommunicationMergedEmailTemplate communicationMergedEmailTemplate = new CommunicationMergedEmailTemplate()
        communicationMergedEmailTemplate.content = """Greetings George Washington,\r
        First, I must solicit your strictest confidence in this transaction. this is by virtue of its nature as being utterly confidential and 'top secret'.\r
        Yours Faithfully, DR CLEMENT OKON """
        /* Since toList and subject contents are not communicationFields, they should render null */
        println "debug: result content is: **"+result.content+"**"
        println "debug: template content is: **"+communicationMergedEmailTemplate.content
        assertNull( communicationMergedEmailTemplate.toList )
        assertNull( communicationMergedEmailTemplate.subject )
        assertEquals( communicationMergedEmailTemplate.content, result.content )

    }


    @Test
    void testProcessGroovyTemplate() {
        def text = """
        Dear \$firstname \$lastname,
        So nice to meet you in <% print city %>.
        See you in \${month},
        \${signed}"""
        def bindings = ["firstname": "Sam", "lastname": "Pullara", "city": "San Francisco", "month": "December", "signed": "Groovy-Dev"]
        def renderedTemplate = communicationTemplateMergeService.processGroovyTemplate( text, bindings )
        def result = """
        Dear Sam Pullara,
        So nice to meet you in San Francisco.
        See you in December,
        Groovy-Dev"""
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
                folder: folder1,
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


    CommunicationOrganization createNewCommunicationOrganization() {
        CommunicationOrganization organization = new CommunicationOrganization()
        organization.name = "test"
        organization.description = "description"
        communicationOrganizationService.create( organization )
    }


    private String getUser() {
        return 'GRAILS_USER'
    }
}
