/** *****************************************************************************
 © 2014 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package net.hedtech.banner.general.communication.field

import grails.converters.*
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.codehaus.groovy.grails.web.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.stringtemplate.v4.ST

class CommunicationFieldCalculationServiceTests extends BaseIntegrationTestCase {
    def CommunicationFolder validFolder
    def communicationFieldService
    def communicationFieldCalculationService


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
        /* This is setup */
        def newCommunicationField = newCommunicationField()
        def communicationField = communicationFieldService.create( [domainModel: newCommunicationField] )
        assertNotNull communicationField.immutableId

        /* This is the 'model', just building a result set from a query */
        def params = [:]
        params << ['pidm': 37815]
        params << ['bannerId' : "AA0037815"]
        def resultSet = communicationFieldCalculationService.calculateField( communicationField.immutableId, params )
        assertNotNull resultSet
        println resultSet
    }

    @Test
    void testExtractParameters() {
        String template = """hi \$person.firstname\$!,
                your last name is \$person.lastname\$!
                 and I see your last name a second time is \$person.lastname\$
                 Today is \$session.today\$ and you owe me \$fundage.amount\$
                 But I would settle for \$globalamount\$"""
        def parms = communicationFieldCalculationService.extractTemplateVariables( template )
        println parms
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
