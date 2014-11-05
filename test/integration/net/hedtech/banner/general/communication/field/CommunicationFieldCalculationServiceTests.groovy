/** *****************************************************************************
 © 2014 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package net.hedtech.banner.general.communication.field

import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

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
        ruleContent: "Select sfbetrm_term_code termcode from sfbetrm where sfbetrm_pidm = :pidm"
        def communicationField = communicationFieldService.create( [domainModel: newCommunicationField] )
        assertNotNull communicationField.immutableId
        def  params= ['pidm': 37815]
        def resultSet = communicationFieldCalculationService.calculateField( communicationField.immutableId, params )

        assertNotNull resultSet
        println resultSet

        def dataMap = ['result':resultSet[0]]

   /*
         String formatter = "hi \$result.firstname\$! \$result.lastname\$! ";

         char delimeter = '$'
         ST st = new org.stringtemplate.v4.ST( formatter, delimeter, delimeter );
        String mergedResults = st.render()
        *//*
<table border=1>
$users:{ u |
  <tr>
    <td>$u.name$</td><td>$u.age$</td>
  </tr>
}$
</table>

 */

    }

    private def newCommunicationField() {
         def communicationField = new CommunicationField(
                 // Required fields
                 folder: validFolder,
                 //immutableId: validImmutableId,
                 name: "TTTTTTTTTT",
                 returnsArrayArguments: false,

                 // Nullable fields
                 description: "TTTTTTTTTT",
                 formatString: "TTTTTTTT",
                 groovyFormatter: "TTTTTTTT",
                 previewValue: "TTTTTTTTTT",
                 renderAsHtml: true,
                 ruleUri: "TTTTTTTTTT",
                 status: CommunicationFieldStatus.DEVELOPMENT,
                 statementType: CommunicationRuleStatementType.SQL_PREPARED_STATEMENT,
                 ruleContent: """SELECT spriden_id
                       ,spriden_last_name
                       ,spriden_first_name
                       ,spriden_mi
                       ,spbpers_legal_name
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
