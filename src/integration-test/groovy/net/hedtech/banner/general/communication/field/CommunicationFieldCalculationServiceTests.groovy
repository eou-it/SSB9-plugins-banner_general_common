/********************************************************************************
  Copyright 2014-2019 Ellucian Company L.P. and its affiliates.
********************************************************************************/
package net.hedtech.banner.general.communication.field

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.groupsend.CommunicationParameterValue
import net.hedtech.banner.general.communication.parameter.CommunicationParameterType
import net.hedtech.banner.general.person.PersonIdentificationNameCurrent
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import static groovy.test.GroovyAssert.*
import grails.util.GrailsWebMockUtil
import org.grails.plugins.testing.GrailsMockHttpServletRequest
import org.grails.plugins.testing.GrailsMockHttpServletResponse
import org.grails.web.servlet.mvc.GrailsWebRequest
import grails.web.servlet.context.GrailsWebApplicationContext

/**
 * Tests crud methods provided by field calculation service.
 */
@Integration
@Rollback
class CommunicationFieldCalculationServiceTests extends BaseIntegrationTestCase {
    def CommunicationFolder validFolder
    CommunicationFieldService communicationFieldService
    CommunicationFieldCalculationService communicationFieldCalculationService
    def selfServiceBannerAuthenticationProvider


    @Before
    public void setUp() {
        formContext = ['GUAGMNU','SELFSERVICE']
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
        validFolder = newValidForCreateFolder()
        validFolder.save( failOnError: true, flush: true )
        //Test if the generated entity now has an id assigned
        assertNotNull validFolder
    }

    @Test
    void testExecuteCommunicationField() {

        def newCommunicationField = newCommunicationField()
        def communicationField = communicationFieldService.create( [domainModel: newCommunicationField] )
        assertNotNull communicationField.immutableId

        def params = [:]
        Long pidm = 2
        params << ['bannerId': new CommunicationParameterValue( "710000001", CommunicationParameterType.TEXT )]


        def resultSet = communicationFieldCalculationService.calculateFieldByPidm(
                (String) communicationField.ruleContent,
                (Boolean) communicationField.returnsArrayArguments,
                (String) communicationField.formatString,
                (Map) params,
                pidm
        )
        assertNotNull resultSet

    }


    @Test
    void testExecuteCommunicationFieldWithNullStatement() {
        def newCommunicationField = newCommunicationField()
        newCommunicationField.ruleContent = null
        def communicationField = communicationFieldService.create( [domainModel: newCommunicationField] )
        assertNotNull communicationField.immutableId
        def params = [:]
        Long pidm = 2
        params << ['bannerId': new CommunicationParameterValue( "710000001" )]
        def resultSet = communicationFieldCalculationService.calculateFieldByPidm(
                (String) communicationField.ruleContent,
                (Boolean) communicationField.returnsArrayArguments,
                (String) communicationField.formatString,
                (Map) params,
                pidm )
        assertEquals( "Hello  ", resultSet )
    }


    @Test
    void testMergeWithEmptyValues() {
        String templateString = "Hello \$firstname\$ \$lastname\$"
        Map params = [:]
        params << ['firstname': '']
        params << ['lastname': '' ]
        String result = communicationFieldCalculationService.merge( templateString, params )
        assertEquals( "Hello  ", result )
    }


    @Test
    void testMergeWithUndefinedValues() {
        String templateString = "Hello \$firstname\$ \$lastname\$"
        Map params = [:]
        String result = communicationFieldCalculationService.merge( templateString, params )
        assertEquals( "Hello  ", result )
    }


    @Test
    void testMergeWithValues() {
        String templateString = "Hello \$firstname\$ \$lastname\$"
        Map params = [:]
        params.put( "firstname", "Michael" )
        params.put( "lastname", "Brzycki" )
        String result = communicationFieldCalculationService.merge( templateString, params )
        assertEquals( "Hello Michael Brzycki", result )
    }


    @Test
    void testExtractParameters() {
        String template = """hi \$firstname\$!,
                your last name is \$lastname\$!
                 and I see your last name fa second time is \$lastname\$
                 Today is \$today\$ and you owe me \$amount\$
                 But I would settle for \$someotheramount\$"""
        def variables = communicationFieldCalculationService.extractVariables( template )
        assertEquals( 5, variables.size() )
        assertEquals( ['firstname', 'lastname', 'today', 'amount', 'someotheramount'].sort().toArray(), variables.sort().toArray() )

    }

    @Test
      void testExtractParametersWithInvalidTemplate() {
          String template = """hi \$firstname\$!,
                  your last name is \$last %nbsp; name\$!
                   and I see your last name fa second time is \$lastname\$
                   Today is \$today\$ and you owe me \$amount\$
                   But I would settle for \$someotheramount\$"""
          shouldFail (){
              communicationFieldCalculationService.extractVariables( template )
          }
      }

    @Test
    void testCalculateFieldByPidm() {
        setUpData()
        CommunicationField communicationField = new CommunicationField(
            folder: validFolder,
            name: "testCalculateFieldByPidm",
            returnsArrayArguments: false,
            formatString: "Hello \$firstname\$ \$lastname\$",

            ruleContent: """SELECT spriden_id
                   ,spriden_last_name lastname
                   ,spriden_first_name firstname
                   ,spriden_mi mi
                   ,spbpers_legal_name legalname
                   ,sysdate today
                   ,trim(' ') empty_string
                   ,null null_string
                   ,50.56 amount
               FROM spriden, spbpers
              WHERE     spriden_pidm = spbpers_pidm(+)
                    AND spriden_change_ind IS NULL
                    AND (spriden_pidm = :pidm or spriden_id = :bannerId)"""
        )
        communicationField = communicationFieldService.create( [domainModel: communicationField] )
        assertNotNull communicationField.immutableId

        final Long pidm = (PersonIdentificationNameCurrent.fetchByBannerId('STUAFR001')).pidm
        String result = communicationFieldCalculationService.calculateFieldByPidm(
            (String) communicationField.ruleContent,
            (Boolean) communicationField.returnsArrayArguments,
            (String) communicationField.formatString,
            [:],
            (Long) pidm
        )
        assertEquals( "Hello Cliff Starr", result )

        communicationField.formatString = "\$firstname\$"
        communicationField = communicationFieldService.update( communicationField )
        result = communicationFieldCalculationService.calculateFieldByPidm(
            (String) communicationField.ruleContent,
            (Boolean) communicationField.returnsArrayArguments,
            (String) communicationField.formatString,
            [:],
            (Long) pidm
        )
        assertEquals( "Cliff", result )

        communicationField.formatString = "\$empty_string\$"
        communicationField = communicationFieldService.update( communicationField )
        result = communicationFieldCalculationService.calculateFieldByPidm(
            (String) communicationField.ruleContent,
            (Boolean) communicationField.returnsArrayArguments,
            (String) communicationField.formatString,
                [:],
                (Long) pidm
        )
        assertEquals( "", result )

        communicationField.formatString = "\$null_string\$"
        communicationField = communicationFieldService.update( communicationField )
        result = communicationFieldCalculationService.calculateFieldByPidm(
            (String) communicationField.ruleContent,
            (Boolean) communicationField.returnsArrayArguments,
            (String) communicationField.formatString,
                [:],
                (Long) pidm
        )
        assertEquals( "", result )

        // try one that doesn't exist
        communicationField.formatString = "\$whose_mama\$"
        communicationField = communicationFieldService.update( communicationField )
        result = communicationFieldCalculationService.calculateFieldByPidm(
            (String) communicationField.ruleContent,
            (Boolean) communicationField.returnsArrayArguments,
            (String) communicationField.formatString,
            [:],
            (Long) pidm
        )
        assertEquals( "", result )

        // try one that breaks parsing
        communicationField.formatString = "\$whose mama\$"
        communicationField = communicationFieldService.update( communicationField )
        try {
            communicationFieldCalculationService.calculateFieldByPidm(
                (String) communicationField.ruleContent,
                (Boolean) communicationField.returnsArrayArguments,
                (String) communicationField.formatString,
                [:],
                (Long) pidm
            )
            fail( "compileErrorDuringParsing" )
        } catch (ApplicationException ae ) {
            assertApplicationException ae, "compileErrorDuringParsing"
        }
    }

    private def newCommunicationField() {
        setUpData()
        def communicationField = new CommunicationField(
                // Required           fields
                folder: validFolder,
                //immutableId: validImmutableId,
                name: "PersonInfo",
                returnsArrayArguments: false,

                // Nullable fields
                description: "TTTTTTTTTT",
                formatString: "Hello \$firstname\$ \$lastname\$",
                groovyFormatter: "TTTTTTTT",
                previewValue: "TTTTTTTTTT",
                renderAsHtml: true,
                ruleUri: "TTTTTTTTTT",
                status:
                        CommunicationFieldStatus.DEVELOPMENT,
                statementType:
                        CommunicationRuleStatementType.SQL_PREPARED_STATEMENT,
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


    private def newValidForCreateFolder() {
        def folder = new CommunicationFolder(
                description: "Test Folder",
                internal: false,
                name: "Folder Name"
        )
        return folder
    }
}
