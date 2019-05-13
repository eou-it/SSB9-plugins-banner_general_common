/********************************************************************************
  Copyright 2014-2019 Ellucian Company L.P. and its affiliates.
********************************************************************************/

package net.hedtech.banner.general.communication.template

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.util.GrailsWebMockUtil
import grails.web.servlet.context.GrailsWebApplicationContext
import groovy.sql.Sql
import net.hedtech.banner.general.communication.email.CommunicationEmailTemplate
import net.hedtech.banner.general.communication.email.CommunicationMergedEmailTemplate
import net.hedtech.banner.general.communication.field.CommunicationField
import net.hedtech.banner.general.communication.field.CommunicationFieldStatus
import net.hedtech.banner.general.communication.field.CommunicationRuleStatementType
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.merge.CommunicationFieldValue
import net.hedtech.banner.general.communication.merge.CommunicationRecipientData
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import net.hedtech.banner.general.person.PersonIdentificationNameCurrent
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.grails.plugins.testing.GrailsMockHttpServletRequest
import org.grails.plugins.testing.GrailsMockHttpServletResponse
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

@Integration
@Rollback
class CommunicationTemplateMergeServiceTests extends BaseIntegrationTestCase {

    def communicationTemplateMergeService
    def communicationFieldService
    def communicationFieldCalculationService
    def communicationRecipientDataService
    def communicationOrganizationCompositeService
    def communicationTemplateService
    def communicationEmailTemplateService
    def selfServiceBannerAuthenticationProvider

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
    def i_valid_pidm
    def i_valid_bannerId = 'HOSWEB003'
    def CommunicationFolder folder1
    def CommunicationFolder folder2
    def CommunicationOrganization i_valid_Organization


    public void cleanUp() {
        def sql
        try {
            sessionFactory.currentSession.with { session ->
                sql = new Sql( session.connection() )
                sql.executeUpdate( "Delete from GCRORAN" )
            }
        } finally {
            //sql?.close()
        }
    }


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        webAppCtx = new GrailsWebApplicationContext()
        mockRequest()
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('BCMADMIN', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)
    }


    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }

    public GrailsWebRequest mockRequest() {
        GrailsMockHttpServletRequest mockRequest = new GrailsMockHttpServletRequest();
        GrailsMockHttpServletResponse mockResponse = new GrailsMockHttpServletResponse();
        GrailsWebMockUtil.bindMockWebRequest(webAppCtx, mockRequest, mockResponse)
    }

    void setUpData() {
        folder1 = newValidForCreateFolder( i_valid_folder_name1 )
        folder1.save( failOnError: true, flush: true )
        //Test if the generated entity now has an id assigned
        assertNotNull folder1.id
        folder2 = newValidForCreateFolder( i_valid_folder_name2 )
        folder2.save( failOnError: true, flush: true )
        //Test if the generated entity now has an id assigned
        assertNotNull folder2.id
//        cleanUp()
        i_valid_Organization = createNewCommunicationOrganization()
        i_valid_pidm = (PersonIdentificationNameCurrent.fetchByBannerId('STUAFR001')).pidm
    }

    @Test
    /* The sunny day test */
    void testMergeTemplate() {
        setUpData()
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
        Map parameterNameValueMap = [:]

        templateVariables.each {
            variableName ->
                tempfield = CommunicationField.fetchByName( variableName )
                assertNotNull( tempfield.immutableId )
                fieldCalculationResult = communicationFieldCalculationService.calculateFieldByPidm (
                    (String) tempfield.ruleContent,
                    (Boolean) tempfield.returnsArrayArguments,
                    (String) tempfield.formatString,
                    parameterNameValueMap,
                    i_valid_pidm
                )
                fieldListByPidm.put( tempfield.name, new CommunicationFieldValue(
                        value: fieldCalculationResult,
                        renderAsHtml: false ) )
        }
        communicationRecipientData = new CommunicationRecipientData(
                pidm: i_valid_pidm,
                templateId: emailTemplate.id,
                organizationId: i_valid_Organization.id,
                referenceId: 1,
                ownerId: getUser(),
                fieldValues: fieldListByPidm,
                communicationChannel: emailTemplate.communicationChannel
        )

        communicationRecipientDataService.create( communicationRecipientData )

        /* Now merge the recipient data into the template */
        CommunicationMergedEmailTemplate communicationMergedEmailTemplate = communicationTemplateMergeService.mergeEmailTemplate( emailTemplate, communicationRecipientData )
        assertNotNull( communicationMergedEmailTemplate )
        assertEquals( "Hi Cliff Starr", communicationMergedEmailTemplate.toList )
        assertEquals( "Hi Cliff Starr", communicationMergedEmailTemplate.subject )
        assertEquals( "Hi Cliff Starr", communicationMergedEmailTemplate.content )


    }


    @Test
    void testInvalidDataFields() {
        setUpData()
        CommunicationEmailTemplate emailTemplate = newValidForCreateEmailTemplate()
        emailTemplate.content = """Hi \$firstname\$ \$lastname\$"""
        emailTemplate.save( failOnError: true, flush: true )

        // do not set up any fields, you should not get any exception
        CommunicationMergedEmailTemplate communicationMergedEmailTemplate = communicationTemplateMergeService.calculateTemplateByBannerId( emailTemplate.id, i_valid_bannerId )
        //CommunicationMergedEmailTemplate communicationMergedEmailTemplate = communicationTemplateMergeService.mergeEmailTemplate( emailTemplate, communicationRecipientData )
        assertNotNull( communicationMergedEmailTemplate )
        /* No errors expect, just nulls returned */
        assertEquals( "Hi  ", communicationMergedEmailTemplate.content )
    }


    @Test
    void testRenderPreviewTemplate() {
        setUpData()
        CommunicationEmailTemplate emailTemplate = newValidForCreateEmailTemplate()
        def CommunicationField testCommunicationField
        def expectedContent = """
           \$UndefinedDataField\$
           \$DefinedButWithoutPreviewValue\$
           Yours Faithfully, DR CLEMENT OKON """

        emailTemplate.content = """
           \$UndefinedDataField\$
           \$DefinedButWithoutPreviewValue\$
           \$signedTest\$"""
        emailTemplate.subject = "REQUEST FOR URGENT BUSINESS RELATIONSHIP "
        emailTemplate.save( failOnError: true, flush: true )

        // Now set up the fields

        testCommunicationField = newCommunicationField( "DefinedButWithoutPreviewValue", "\$IntroParagraph\$", null, null )
        communicationFieldService.create( [domainModel: testCommunicationField] )

        testCommunicationField = newCommunicationField( "signedTest", "", """Yours Faithfully, DR CLEMENT OKON """, null )
        communicationFieldService.create( [domainModel: testCommunicationField] )

        // First make sure you can publish it
        communicationEmailTemplateService.publishTemplate( id: emailTemplate.id )

        CommunicationMergedEmailTemplate result = communicationTemplateMergeService.renderMergedEmailTemplate( emailTemplate )


        CommunicationMergedEmailTemplate communicationMergedEmailTemplate = new CommunicationMergedEmailTemplate()
        communicationMergedEmailTemplate.content = """\r
                Yours Faithfully, DR CLEMENT OKON """
        /* Since toList and subject contents are not communicationFields, they should render null */
        assertNull( communicationMergedEmailTemplate.toList )
        assertNull( communicationMergedEmailTemplate.subject )

        // we don't care about whitespace, ST messes with things like line endings etc
        assertEquals( expectedContent.trim().replaceAll( "\\s+", "" ), result.content.trim().replaceAll( "\\s+", "" ) )

    }

    @Test
    void testRenderPreviewTemplateWithEscapeButNoFields() {
        setUpData()
        /* create the template */
        CommunicationEmailTemplate emailTemplate = newValidForCreateEmailTemplate()
        /* it doesn't matter what we set them to, as long as they contain personalization fields, we can test that they get rendered */
        emailTemplate.content = """I want my \\\$2.00!"""
        emailTemplate.toList = """I want my \\\$2.00!"""
        emailTemplate.subject = """I want my \\\$2.00!"""
        emailTemplate.save( failOnError: true, flush: true )

        // Now extract the template variables and calculate their values and save as RecipientData
        def templateVariables = communicationTemplateMergeService.extractTemplateVariables( emailTemplate )

        String fieldCalculationResult

        CommunicationRecipientData communicationRecipientData
        def fieldListByPidm = [:]
        Map parameterNameValueMap = [:]

        templateVariables.each {
            variableName ->
                tempfield = CommunicationField.fetchByName( variableName )
                assertNotNull( tempfield.immutableId )
                fieldCalculationResult = communicationFieldCalculationService.calculateFieldByPidm (
                        (String) tempfield.ruleContent,
                        (Boolean) tempfield.returnsArrayArguments,
                        (String) tempfield.formatString,
                        parameterNameValueMap,
                        i_valid_pidm
                )
                fieldListByPidm.put( tempfield.name, new CommunicationFieldValue(
                        value: fieldCalculationResult,
                        renderAsHtml: false ) )
        }
        communicationRecipientData = new CommunicationRecipientData(
                pidm: i_valid_pidm,
                templateId: emailTemplate.id,
                organizationId: i_valid_Organization.id,
                referenceId: 1,
                ownerId: getUser(),
                fieldValues: fieldListByPidm,
                communicationChannel: emailTemplate.communicationChannel
        )

        communicationRecipientDataService.create( communicationRecipientData )

        /* Now merge the recipient data into the template */
        CommunicationMergedEmailTemplate communicationMergedEmailTemplate = communicationTemplateMergeService.mergeEmailTemplate( emailTemplate, communicationRecipientData )
        assertNotNull( communicationMergedEmailTemplate )
        assertEquals( "I want my \$2.00!", communicationMergedEmailTemplate.toList )
        assertEquals( "I want my \$2.00!", communicationMergedEmailTemplate.subject )
        assertEquals( "I want my \$2.00!", communicationMergedEmailTemplate.content )
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
        communicationOrganizationCompositeService.createOrganization( organization )
    }


    private String getUser() {
        return 'GRAILS_USER'
    }
}
