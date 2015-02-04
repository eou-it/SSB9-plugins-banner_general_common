/*********************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.general.communication.template

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.testing.BaseIntegrationTestCase

import static org.junit.Assert.*
import org.junit.*

class CommunicationStringTemplateErrorListenerIntegrationTests extends BaseIntegrationTestCase {

    def communicationTemplateMergeService

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    void testSimpleSubstitution() {
        String template = "simple parameter swap \$pi\$"
        Map<String, String> nameValueMap = new HashMap<String,String>()
        nameValueMap.put( "pi", "3.14" )

        String result = communicationTemplateMergeService.merge( template, nameValueMap )
        assertEquals( "simple parameter swap 3.14", result )
    }

    @Test
    void testMergeCompileTimeError() {
        String template = "simple parameter swap \$p i\$"
        Map<String, String> nameValueMap = new HashMap<String,String>()
        nameValueMap.put( "pi", "3.14" )

        try {
            communicationTemplateMergeService.merge( template, nameValueMap )
            fail( "compileErrorDuringParsing" )
        } catch (ApplicationException ae ) {
            assertApplicationException ae, "compileErrorDuringParsing"
        }
    }

    @Test
    void testMissingFieldReferenceGetsIgnored() {
        String template = "simple parameter swap \$pi\$"
        Map<String, String> nameValueMap = new HashMap<String,String>()
        nameValueMap.put( "e", "2.7" )

        String result = communicationTemplateMergeService.merge( template, nameValueMap )
        assertEquals( "simple parameter swap ", result )
    }

    @Test
    void testMissingPropertyCapture() {
        String SPACE = "\$\\ \$"
        String template = "simple parameter swap \$pi\$ and \$mi\$.\$names,phones:{ n,p |" + SPACE + "\$n\$" + SPACE + ":" + SPACE + "\$p\$}\$"

        List<String> fieldReferences = communicationTemplateMergeService.extractTemplateVariables( template )

        assertEquals( 4, fieldReferences.size() )
        assertTrue( fieldReferences.contains( "pi" ) )
        assertTrue( fieldReferences.contains( "mi" ) )
        assertTrue( fieldReferences.contains( "names" ) )
        assertTrue( fieldReferences.contains( "phones" ) )
        assertFalse( fieldReferences.contains( "n" ) )
        assertFalse( fieldReferences.contains( "p" ) )

        HashMap<String,String> nameValueMap = new HashMap<String,String>()
        nameValueMap.put( "pi", "3.14" )
        nameValueMap.put( "mi", "7" )
        nameValueMap.put( "names", ['mike', 'ed', 'lakshmi', 'carol'] )
        nameValueMap.put( "phones", ['1', '2', '3', '4'] )
        String result = communicationTemplateMergeService.merge( template, nameValueMap )
        assertEquals( "simple parameter swap 3.14 and 7. mike : 1 ed : 2 lakshmi : 3 carol : 4", result )
    }
}