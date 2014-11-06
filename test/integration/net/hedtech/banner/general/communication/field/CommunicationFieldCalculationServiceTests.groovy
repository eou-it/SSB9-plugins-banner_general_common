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
        def newCommunicationField = newCommunicationField()
        def communicationField = communicationFieldService.create( [domainModel: newCommunicationField] )
        assertNotNull communicationField.immutableId
        def params = ['pidm': 37815]
        def resultSet = communicationFieldCalculationService.calculateField( communicationField.immutableId, params )

        assertNotNull resultSet

        def result = [:]
        result.put(communicationField.name, resultSet[0])
         JSONObject json = new JSONObject()
        json.putAll(result)
        println json


        char delimeter = '$'
        String formatter = """hi \$PersonInfo.FIRSTNAME\$!,
        your last name is \$lastname\$!
         and I see your last name a second time is \$lastname\$
         Today is \$today\$ and you owe me \$amount\$""";
        ST st = new org.stringtemplate.v4.ST( formatter, delimeter, delimeter );
        //st.addAggr("PersonInfo.{firstname, lastname, today, amount}", result.getKey(), result.getValue())
/*        def String key
        result.each { k, v ->
            println "setting key : $k to $v"
            key = k.toLowerCase()
            st.add( key, v )
        }*/
        String mergedResults = st.render()
        println mergedResults

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
                formatString: "TTTTTTTT",
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
                        AND spriden_pidm = :pidm"""
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
