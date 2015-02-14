/** *****************************************************************************
 © 2014 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package net.hedtech.banner.general.communication.field

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

class CommunicationFieldCalculationServiceTests extends BaseIntegrationTestCase {
    def CommunicationFolder validFolder
    CommunicationFieldService communicationFieldService
    CommunicationFieldCalculationService communicationFieldCalculationService


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        validFolder = newValidForCreateFolder()
        validFolder.save( failOnError: true, flush: true )
        //Test if the generated entity now has an id assigned
        assertNotNull validFolder

    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testExecuteCommunicationField() {

        def newCommunicationField = newCommunicationField()
        def communicationField = communicationFieldService.create( [domainModel: newCommunicationField] )
        assertNotNull communicationField.immutableId


        def params = [:]
        params << ['pidm': 37815]
        params << ['bannerId': "AA0037815"]

        def resultSet = communicationFieldCalculationService.calculateFieldByMap(
                (String) communicationField.ruleContent,
                (Boolean) communicationField.returnsArrayArguments,
                (String) communicationField.formatString,
                (Map) params )
        assertNotNull resultSet

    }


    @Test
    void testExecuteCommunicationFieldWithNullStatement() {
        def newCommunicationField = newCommunicationField()
        newCommunicationField.ruleContent = null
        def communicationField = communicationFieldService.create( [domainModel: newCommunicationField] )
        assertNotNull communicationField.immutableId
        def params = [:]
        params << ['pidm': 37815]
        params << ['bannerId': "AA0037815"]
        def resultSet = communicationFieldCalculationService.calculateFieldByMap(
                (String) communicationField.ruleContent,
                (Boolean) communicationField.returnsArrayArguments,
                (String) communicationField.formatString,
                (Map) params )
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
        communicationField = communicationFieldService.create( communicationField )
        assertNotNull communicationField.immutableId

        final Long pidm = 37815L
        String result = communicationFieldCalculationService.calculateFieldByPidm(
            (String) communicationField.ruleContent,
            (Boolean) communicationField.returnsArrayArguments,
            (String) communicationField.formatString,
            (Long) pidm
        )
        assertEquals( "Hello test carter roll", result )

        communicationField.formatString = "\$firstname\$"
        communicationField = communicationFieldService.update( communicationField )
        result = communicationFieldCalculationService.calculateFieldByPidm(
            (String) communicationField.ruleContent,
            (Boolean) communicationField.returnsArrayArguments,
            (String) communicationField.formatString,
            (Long) pidm
        )
        assertEquals( "test", result )

        communicationField.formatString = "\$empty_string\$"
        communicationField = communicationFieldService.update( communicationField )
        result = communicationFieldCalculationService.calculateFieldByPidm(
            (String) communicationField.ruleContent,
            (Boolean) communicationField.returnsArrayArguments,
            (String) communicationField.formatString,
            (Long) pidm
        )
        assertEquals( "", result )

        communicationField.formatString = "\$null_string\$"
        communicationField = communicationFieldService.update( communicationField )
        result = communicationFieldCalculationService.calculateFieldByPidm(
            (String) communicationField.ruleContent,
            (Boolean) communicationField.returnsArrayArguments,
            (String) communicationField.formatString,
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
                (Long) pidm
            )
            fail( "compileErrorDuringParsing" )
        } catch (ApplicationException ae ) {
            assertApplicationException ae, "compileErrorDuringParsing"
        }
    }

    private def newCommunicationField() {
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
