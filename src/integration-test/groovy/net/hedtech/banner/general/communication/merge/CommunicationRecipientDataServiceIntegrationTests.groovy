/*********************************************************************************
 Copyright 2014-2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.merge

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.util.GrailsWebMockUtil
import grails.web.servlet.context.GrailsWebApplicationContext
import net.hedtech.banner.general.communication.field.CommunicationField
import net.hedtech.banner.general.communication.field.CommunicationFieldStatus
import net.hedtech.banner.general.communication.field.CommunicationFieldView
import net.hedtech.banner.general.communication.field.CommunicationRuleStatementType
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.item.CommunicationChannel
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import net.hedtech.banner.general.communication.parameter.CommunicationParameter
import net.hedtech.banner.general.communication.population.CommunicationPopulation
import net.hedtech.banner.general.communication.population.CommunicationPopulationCalculation
import net.hedtech.banner.general.communication.population.CommunicationPopulationProfileView
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQuery
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQueryExecutionResult
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQueryVersion
import net.hedtech.banner.general.communication.email.CommunicationEmailTemplate
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
class CommunicationRecipientDataServiceIntegrationTests extends BaseIntegrationTestCase {

    def communicationRecipientDataService
    def communicationTemplateMergeService
    def communicationPopulationQueryCompositeService
    def communicationPopulationQueryExecutionService
    def communicationFieldCalculationService
    def communicationFieldService
    def selfServiceBannerAuthenticationProvider
    def communicationOrganizationCompositeService
    def communicationEmailTemplateService
    def communicationPopulationCompositeService
    def communicationParameterService

    def CommunicationFolder validFolder
    def CommunicationEmailTemplate emailTemplate

    def i_valid_emailTemplate_bccList = """Valid Emailtemplate Bcclist"""

    def i_valid_emailTemplate_ccList = """Valid Emailtemplate Cclist"""

    def i_valid_emailTemplate_content = """Valid Emailtemplate Content  \$PersonName\$"""

    def i_valid_emailTemplate_createDate = new Date()

    def i_valid_emailTemplate_createdBy = """Valid EmailTemplate createdBy"""

    def i_valid_emailTemplate_dataOrigin = """Valid Emailtemplate Dataorigin"""

    def i_valid_emailTemplate_description = """Valid Template Description"""

    def i_valid_emailTemplate_fromList = """Valid Emailtemplate Fromlist"""

    def i_valid_emailTemplate_name = """Valid Name"""

    def i_valid_emailTemplate_oneOff = true
    def i_valid_emailTemplate_personal = true
    def i_valid_emailTemplate_published = true

    def i_valid_emailTemplate_subject = """Valid Emailtemplate Subject"""

    def i_valid_emailTemplate_toList = """Valid Emailtemplate Tolist"""

    def i_valid_emailTemplate_validFrom = new Date()-200
    def i_valid_emailTemplate_validTo = new Date()+200

    def CommunicationPopulationQuery validQuery
    def CommunicationField validField1
    def CommunicationEmailTemplate validTemplate

    private CommunicationOrganization organization


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        webAppCtx = new GrailsWebApplicationContext()
        mockRequest()
        def auth = selfServiceBannerAuthenticationProvider.authenticate( new UsernamePasswordAuthenticationToken( 'BCMADMIN', '111111' ) )
        SecurityContextHolder.getContext().setAuthentication( auth )
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
        /* Create folder to hold things */
        validFolder = newValidForCreateFolder( "Test Folder" )
        validFolder.save( failOnError: true, flush: true )
        assertNotNull validFolder.id

        /* create a population query */
        validQuery = communicationPopulationQueryCompositeService.createPopulationQuery( newPopulationQuery( validFolder, "TestQuery" ) )
        CommunicationPopulationQueryVersion validQueryVersion = communicationPopulationQueryCompositeService.publishPopulationQuery( validQuery )
        validQuery = validQueryVersion.query
        assertNotNull( validQuery.id )

        // create the parameters
        def banneridparm = new CommunicationParameter()
        banneridparm.name = 'bannerId'
        banneridparm.type = 'TEXT'
        banneridparm.title = 'Banner ID'
        CommunicationParameter cp = communicationParameterService.create(banneridparm)
        assertNotNull cp.id

        /* Create communication Field */
        def cf = newCommunicationField( validFolder, "PersonName",
                "Hello \$firstname\$ \$lastname\$", "Hello Peter Tosh", """SELECT spriden_id
                                       ,spriden_last_name lastname
                                       ,spriden_first_name firstname
                                       ,spriden_mi mi
                                       ,spbpers_legal_name legalname
                                       ,sysdate today
                                       ,50.56 amount
                                   FROM spriden, spbpers
                                  WHERE     spriden_pidm = spbpers_pidm(+)
                                        AND spriden_change_ind IS NULL
                                        AND (spriden_pidm = :pidm or spriden_id = :bannerId)""" )
        cf.status = 'DEVELOPMENT'
        validField1 = communicationFieldService.create( cf )
        assertNotNull validField1.id

        validField1 = communicationFieldService.publishDataField(cf)
        assertEquals CommunicationFieldStatus.PRODUCTION,validField1.status

        /* create a template */
        validTemplate = communicationEmailTemplateService.create( newValidForCreateEmailTemplate( validFolder ) )
        assertNotNull( validTemplate.id )

        /* Create an organization */
        organization = new CommunicationOrganization( name: "Test Org" )
        def orgList = communicationOrganizationCompositeService.listOrganizations()
        if (orgList.size() > 0) {
            organization = orgList[0]
        } else {
            organization = communicationOrganizationCompositeService.createOrganization(organization)
        }
    }

    @Test
    void testCreateCommunicationRecipientDataFromTemplate() {
        setUpData()
        //execute the query to get a population
        CommunicationPopulation population = communicationPopulationCompositeService.createPopulationFromQuery( validQuery.id, "TEST NAME", "TEST DESC", true )
        CommunicationPopulationCalculation populationCalculation = CommunicationPopulationCalculation.findLatestByPopulationIdAndCalculatedBy(population.id, getUser())
        assertNotNull( populationCalculation )
        List publishedQueryVersionList = CommunicationPopulationQueryVersion.findByQueryId( validQuery.id )
        assertEquals( 1, publishedQueryVersionList.size() )
        CommunicationPopulationQueryVersion publishedQueryVersion = publishedQueryVersionList.get( 0 )

        CommunicationPopulationQueryExecutionResult queryExecutionResult = communicationPopulationQueryExecutionService.execute( publishedQueryVersion.id )
        validQuery.refresh()

        def calculatedPopulationQuery = CommunicationPopulationQuery.fetchById(validQuery.id)
        calculatedPopulationQuery.refresh()

        //get all the people in the population
        def selectionList = CommunicationPopulationProfileView.findAllBySelectionListId( queryExecutionResult.selectionListId )
        assertNotNull(selectionList)
        assertTrue selectionList.size() >= 1

        // Now set up the data fields
        CommunicationField tempField
        def testCommunicationField = newCommunicationField(validFolder, "firstname", "\$firstname\$",
                "George", "Select spriden_first_name firstname from spriden where spriden_pidm = :pidm and spriden_change_ind is null")
        tempField = communicationFieldService.create(testCommunicationField)
        assertNotNull tempField.immutableId

        testCommunicationField = newCommunicationField(validFolder, "lastname", "\$lastname\$",
                "George", "Select spriden_last_name lastname from spriden where spriden_pidm = :pidm and spriden_change_ind is null")
        tempField = communicationFieldService.create(testCommunicationField)
        assertNotNull tempField.immutableId

        // get all the fields from the template
        def fieldList = communicationTemplateMergeService.extractTemplateVariables(validTemplate.content.toString())

        def resultSet

        //for each person in the population, calculate all the fields in the template and create the recipient data
        //the field is stored in the template by name, so get the immutable id from the name. Pass this immutable id along with the
        //pidm from popualation to the field calculation service
        selectionList.each {
            person ->
                Map parameterNameValueMap = [:]
                def fieldListByPidm = [:]
                fieldList.each {
                    fieldName ->
                        CommunicationField communicationField = CommunicationField.fetchByName(fieldName)
                        if (communicationField) {
                            resultSet = communicationFieldCalculationService.calculateFieldByPidm(
                                    (String) communicationField.ruleContent,
                                    (Boolean) communicationField.returnsArrayArguments,
                                    (String) communicationField.formatString,
                                    parameterNameValueMap,
                                    person.pidm
                            )
                            fieldListByPidm.put(communicationField.name, newFieldValue(resultSet))
                        }

                        CommunicationRecipientData recipient = new CommunicationRecipientData(
                                pidm: person.pidm,
                                templateId: validTemplate.id,
                                referenceId: UUID.randomUUID().toString(),
                                ownerId: getUser(),
                                fieldValues: fieldListByPidm,
                                organizationId: this.organization.id,
                                communicationChannel: validTemplate.communicationChannel
                        )
                        communicationRecipientDataService.create(recipient)
                }

                def recipientDataList = CommunicationRecipientData.fetchByTemplateId(validTemplate.id)
                assertNotNull(recipientDataList)
        }

    }

   /* This just tests that you can save accurately in differnt folders */
    @Test
    void testGroupFolder() {
        def originalList = CommunicationFieldView.findAll()
        def folder1 = newValidForCreateFolder( "testfolder1" )
        folder1 = folder1.save( failOnError: true, flush: true )
        def folder2 = newValidForCreateFolder( "testfolder2" )
        folder2 = folder2.save( failOnError: true, flush: true )
        def field1 = newCommunicationField( folder1, "Fieldname1", "FormatString",
                 "Preview Value", "select 1 from dual" )
        field1 = field1.save( failOnError: true, flush: true )
        field1 = communicationFieldService.publishDataField([id:field1.id])
        def field2 = newCommunicationField( folder1, "Fieldname2", "FormatString",
                "Preview Value", "select 1 from dual" )
        field2 = field2.save( failOnError: true, flush: true )
        field2 = communicationFieldService.publishDataField([id:field2.id])
        def field3 = newCommunicationField( folder2, "Fieldname3", "FormatString",
                "Preview Value", "select 1 from dual" )
        field3 = field3.save( failOnError: true, flush: true )
        field3 = communicationFieldService.publishDataField([id:field3.id])
        def field4 = newCommunicationField( folder2, "Fieldname4", "FormatString",
                "Preview Value", "select 1 from dual" )
        field4 = field4.save( failOnError: true, flush: true )
        field4 = communicationFieldService.publishDataField([id:field4.id])
        def finalFieldList = CommunicationFieldView.findAll()
        assertEquals( originalList.size + 4, finalFieldList.size() )
    }


    private def newCommunicationRecipientData( pidmvalue, templateid, fieldValue ) {
        def communicationRecipientData = new CommunicationRecipientData(
                // Required fields
                pidm: pidmvalue,
                templateId: templateid,
                referenceId: UUID.randomUUID().toString(),
                ownerId: getUser(),
                fieldValues: ["name": fieldValue],
                organizationId: this.organization.id,
                communicationChannel: CommunicationChannel.EMAIL
        )
        return communicationRecipientData
    }


    private String getUser() {
        return 'BCMADMIN'
    }


    private def newPopulationQuery( CommunicationFolder testFolder, String queryName ) {
        def populationQuery = new CommunicationPopulationQuery(
                // Required fields
                folder: testFolder,
                name: queryName,
                changesPending: false,

                // Nullable fields
                description: "TTTTTTTTTT",
                queryString: "select spriden_pidm from spriden where spriden_change_ind is null  AND spriden_id LIKE 'HOSWEB003%'"
        )

        return populationQuery
    }


    private def newValidForCreateFolder( String foldername ) {
        def folder = new CommunicationFolder(
                description: foldername,
                internal: false,
                name: foldername
        )
        return folder
    }


    private def newFieldValue( String insertvalue ) {
        def CommunicationFieldValue = new CommunicationFieldValue(
                value: insertvalue,
                renderAsHtml: false
        )

        return CommunicationFieldValue
    }


    private def newValidForCreateEmailTemplate( CommunicationFolder folder ) {
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
                toList: i_valid_emailTemplate_toList,
                dataOrigin: i_valid_emailTemplate_dataOrigin,
        )

        return communicationTemplate
    }


    private def newCommunicationField( CommunicationFolder folder, String name, String formatString, String previewValue, String ruleContent ) {
        def communicationField = new CommunicationField(
                // Required fields
                folder: folder,
                name: name,
                immutableId: UUID.randomUUID().toString(),
                returnsArrayArguments: false,
                // Nullable fields
                description: name + " test",
                formatString: formatString,
                groovyFormatter: formatString,
                previewValue: previewValue,
                renderAsHtml: true,
                ruleUri: null,
                status: CommunicationFieldStatus.PRODUCTION,
                ruleContent: ruleContent
        )

        return communicationField
    }


    private def newCommunicationFieldcc( CommunicationFolder folder, String immId, String fieldname ) {
        def communicationField = new CommunicationField(
                // Required fields
                folder: folder,
                immutableId: immId,
                name: fieldname,
                returnsArrayArguments: false,

                // Nullable fields
                description: "TTTTTTTTTT",
                formatString: "Hello \$firstname\$ \$lastname\$",
                groovyFormatter: "TTTTTTTT",
                previewValue: "TTTTTTTTTT",
                renderAsHtml: false,
                ruleUri: "TTTTTTTTTT",
                status: CommunicationFieldStatus.DEVELOPMENT,
                statementType: CommunicationRuleStatementType.SQL_PREPARED_STATEMENT,
                ruleContent: """SELECT spriden_id
                       ,spriden_last_name lastname
                       ,spriden_first_name firstname
                       ,spriden_mi mi
                       ,spbpers_legal_name legalname
                       ,sysdate today
                       ,50.56 amount
                   FROM spriden, spbpers
                  WHERE     spriden_pidm = spbpers_pidm(+)
                        AND spriden_change_ind IS NULL
                        AND (spriden_pidm = :pidm or spriden_id = :bannerId)"""
        )

        return communicationField
    }


    CommunicationOrganization createNewCommunicationOrganization() {
        CommunicationOrganization organization = new CommunicationOrganization()
        organization.name = "test"
        organization.description = "description"
        communicationOrganizationCompositeService.createOrganization( organization )
    }

}
