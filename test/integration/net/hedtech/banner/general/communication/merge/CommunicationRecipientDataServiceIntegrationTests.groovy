/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.merge

import net.hedtech.banner.general.communication.field.CommunicationField
import net.hedtech.banner.general.communication.field.CommunicationFieldStatus
import net.hedtech.banner.general.communication.field.CommunicationFieldView
import net.hedtech.banner.general.communication.field.CommunicationRuleStatementType
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import net.hedtech.banner.general.communication.population.CommunicationPopulationProfileView
import net.hedtech.banner.general.communication.population.CommunicationPopulationQuery
import net.hedtech.banner.general.communication.template.CommunicationEmailTemplate
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

class CommunicationRecipientDataServiceIntegrationTests extends BaseIntegrationTestCase {

    def communicationRecipientDataService
    def communicationEmailTemplateService
    def communicationTemplateMergeService
    def communicationPopulationQueryService
    def communicationPopulationExecutionService
    def communicationPopulationSelectionListService
    def communicationFieldCalculationService
    def communicationFieldService
    def selfServiceBannerAuthenticationProvider
    def communicationOrganizationService


    def CommunicationFolder validFolder
    def CommunicationEmailTemplate emailTemplate
    def CommunicationField field1
    def CommunicationRecipientData recipientData

    def String validImmutableId
    def i_valid_emailTemplate_active = true

    def i_valid_emailTemplate_bccList = """Valid Emailtemplate Bcclist"""
    def i_invalid_emailTemplate_bccList = "foo@bar.com".padLeft( 1021 )

    def i_valid_emailTemplate_ccList = """Valid Emailtemplate Cclist"""
    def i_invalid_emailTemplate_ccList = "foo@bar.com".padLeft( 1021 )

    def i_valid_emailTemplate_content = """Valid Emailtemplate Content  \$PersonName\$"""

    def i_valid_emailTemplate_createDate = new Date()

    def i_valid_emailTemplate_createdBy = """Valid EmailTemplate createdBy"""
    def i_invalid_emailTemplate_createdBy = """Valid EmailTemplate createdBy""".padLeft( 31 )

    def i_valid_emailTemplate_dataOrigin = """Valid Emailtemplate Dataorigin"""
    def i_invalid_emailTemplate_dataOrigin = "XE Communication Manager".padLeft( 31 )

    def i_valid_emailTemplate_description = """Valid Template Description"""
    def i_invalid_emailTemplate_description = """Valid Template Description""".padLeft( 4001 )

    def i_valid_emailTemplate_fromList = """Valid Emailtemplate Fromlist"""
    def i_invalid_emailTemplate_fromList = "foo@bar.com".padLeft( 1021 )

    def i_valid_emailTemplate_lastModified = new Date()

    def i_valid_emailTemplate_lastModifiedBy = """Valid Emailtemplate Lastmodifiedby"""
    def i_invalid_emailTemplate_lastModifiedBy = "BCMUSER".padLeft( 31 )

    def i_valid_emailTemplate_name = """Valid Name"""
    def i_invalid_emailTemplate_name = """Valid Name""".padLeft( 2049 )

    def i_valid_emailTemplate_oneOff = true
    def i_valid_emailTemplate_personal = true
    def i_valid_emailTemplate_published = true

    def i_valid_emailTemplate_subject = """Valid Emailtemplate Subject"""
    def i_invalid_emailTemplate_subject = """You're a winner!""".padLeft( 1021 )

    def i_valid_emailTemplate_toList = """Valid Emailtemplate Tolist"""
    def i_invalid_emailTemplate_toList = "foo@bar.com".padLeft( 1021 )

    def i_valid_emailTemplate_validFrom = new Date()-200
    def i_valid_emailTemplate_validTo = new Date()+200

    def i_valid_folder_description = "Valid older description"
    def i_valid_folder_internal = true
    def i_valid_folder_name1 = "Valid Folder1 Name"
    def i_valid_folder_name2 = "Valid Folder2 Name"

    def CommunicationPopulationQuery validQuery
    def CommunicationField validField1
    def CommunicationEmailTemplate validTemplate

    private CommunicationOrganization organization


    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']
        def auth = selfServiceBannerAuthenticationProvider.authenticate( new UsernamePasswordAuthenticationToken( 'BCMADMIN', '111111' ) )
        SecurityContextHolder.getContext().setAuthentication( auth )
        super.setUp()

        /* Create folder to hold things */
        validFolder = newValidForCreateFolder( "Test Folder" )
        validFolder.save( failOnError: true, flush: true )
        assertNotNull validFolder.id

        /* create a population query */
        validQuery = communicationPopulationQueryService.create( newPopulationQuery( validFolder, "TestQuery" ) )
        assertNotNull( validQuery.id )

        /* Create communication Field */
        validField1 = communicationFieldService.create( newCommunicationField( validFolder, "PersonName",
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
                                        AND (spriden_pidm = :pidm or spriden_id = :bannerId)""" ) )
        assertNotNull validField1.id

        /* create a template */
        validTemplate = communicationEmailTemplateService.create( [domainModel: newValidForCreateEmailTemplate( validFolder )] )
        assertNotNull( validTemplate.id )

        /* Create an organization */
        organization = new CommunicationOrganization( name: "Test Org", isRoot: true )
        def orgList = communicationOrganizationService.list()
        if (orgList.size() > 0) {
            organization = orgList[0]
        } else {
            organization = communicationOrganizationService.create(organization) as CommunicationOrganization
        }

    }


    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }


    @Test
    void testCreateCommunicationRecipientDataFromTemplate() {

        //execute the query to get a population
        def populationSelectionListId = communicationPopulationExecutionService.execute( validQuery.id )
        validQuery.refresh()

        def calculatedPopulationQuery = CommunicationPopulationQuery.fetchById( validQuery.id )
        calculatedPopulationQuery.refresh()

        def populationQuerySelectionList = communicationPopulationSelectionListService.fetchByNameAndId( calculatedPopulationQuery.id, getUser() )
        assertNotNull populationQuerySelectionList
        assertEquals( populationSelectionListId, populationQuerySelectionList.id )

        //get all the people in the population
        def personlist = CommunicationPopulationProfileView.findAllByPopulationId( populationQuerySelectionList.id )
        assertNotNull( personlist )
        assertTrue personlist.size() >= 1

        // Now set up the data fields
        CommunicationField tempField
        def testCommunicationField = newCommunicationField( validFolder, "firstname", "\$firstname\$",
                "George", "Select spriden_first_name firstname from spriden where spriden_pidm = :pidm and spriden_change_ind is null" )
        tempField = communicationFieldService.create( [domainModel: testCommunicationField] )
        assertNotNull tempField.immutableId

        testCommunicationField = newCommunicationField( validFolder, "lastname", "\$lastname\$",
                "George", "Select spriden_last_name lastname from spriden where spriden_pidm = :pidm and spriden_change_ind is null" )
        tempField = communicationFieldService.create( [domainModel: testCommunicationField] )
        assertNotNull tempField.immutableId

        // get all the fields from the template
        def fieldList = communicationTemplateMergeService.extractTemplateVariables( validTemplate.content.toString() )

        def resultSet

        //for each person in the population, calculate all the fields in the template and create the recipient data
        //the field is stored in the template by name, so get the immutable id from the name. Pass this immutable id along with the
        //pidm from popualation to the field calculation service
        personlist.each {
            person ->
                params = [:]
                params << ['pidm': person.pidm]
                def fieldListByPidm = [:]
                fieldList.each {
                    fieldName ->
                        CommunicationField communicationField = CommunicationField.fetchByName( fieldName )
                        if (communicationField) {
                            resultSet = communicationFieldCalculationService.calculateFieldByMap(
                                (String) communicationField.ruleContent,
                                (Boolean) communicationField.returnsArrayArguments,
                                (String) communicationField.formatString,
                                (Map) params
                            )
                            fieldListByPidm.put( communicationField.name, newFieldValue( resultSet ) )
                        }

                        CommunicationRecipientData recipient = new CommunicationRecipientData(
                                pidm: person.pidm,
                                templateId: validTemplate.id,
                                referenceId:  UUID.randomUUID().toString(),
                                ownerId: getUser(),
                                fieldValues: fieldListByPidm,
                                organization: this.organization
                        )
                        communicationRecipientDataService.create( recipient )
                }

                def recipientDataList = CommunicationRecipientData.fetchByTemplateId( validTemplate.id )
                assertNotNull( recipientDataList )
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
                organization: this.organization
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
                valid: false,

                // Nullable fields
                description: "TTTTTTTTTT",
                sqlString: "select spriden_pidm from spriden where spriden_change_ind is null  AND spriden_id LIKE 'A0000091%'"
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
                active: i_valid_emailTemplate_active,
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
                statmentType: CommunicationRuleStatementType.SQL_PREPARED_STATEMENT,
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
        communicationOrganizationService.create( organization )
    }

}
