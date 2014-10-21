package net.hedtech.banner.general.communication.template.merge

import org.apache.velocity.VelocityContext
import org.junit.Test

/**
 * Tests that the Velocity Engine spring bean is loaded.
 */
class VelocityEngineIntegrationTests extends GroovyTestCase {
    boolean transactional = false
    def velocityEngine

    @Test
    void testSimpleVariableSubstitution() {
        String templateString = """
            <html>
                <body>
                    Hello \${username}!
                </body>
            </html>
        """

        String templateName = "testSimpleVariableSubstitution";

        VelocityContext context = new VelocityContext();
        context.put("username", "bcmuser");

        StringWriter stringWriter = new StringWriter();
        velocityEngine.evaluate( context, stringWriter, templateName, templateString );
        System.out.println(" string : " + stringWriter );
    }


    @Test
    void testPropertySubstitution() {

        String templateString = """
            <html>
                <body>
                    Hello \${person.firstName} \${person.lastName}!
                </body>
            </html>
        """

        String templateName = "testPropertySubstitution";

        TestPerson aPerson = new TestPerson( firstName:'Michael', lastName:'Brzycki' )
        VelocityContext context = new VelocityContext();
        context.put( "person", aPerson );

        StringWriter stringWriter = new StringWriter();
        velocityEngine.evaluate( context, stringWriter, templateName, templateString );
        System.out.println(" string : " + stringWriter );
    }


    @Test
    void testPropertyMapSubstitution() {

        String templateString = """
            <html>
                <body>
                    Hello \${person.firstName} \${person.lastName}!
                </body>
            </html>
        """

        String templateName = "testPropertyMapSubstitution";

        def aPerson = [:]
        aPerson.put( 'firstName', 'Michael' )
        aPerson.lastName = 'Brzycki'
        VelocityContext context = new VelocityContext();
        context.put( "person", aPerson );

        StringWriter stringWriter = new StringWriter();
        velocityEngine.evaluate( context, stringWriter, templateName, templateString );
        System.out.println(" string : " + stringWriter );
    }


    @Test
    void testStaticMethodSubstitution() {

        String templateString = """
            <html>
                <body>
                    Today is \${System.exit( -1 )}!
                </body>
            </html>
        """

        String templateName = "testStaticMethodSubstitution";

        StringWriter stringWriter = new StringWriter();
        velocityEngine.evaluate( new VelocityContext(), stringWriter, templateName, templateString );
        System.out.println(" string : " + stringWriter );
    }

    @Test
    void testSimpleGuidSubstitution() {
        String templateString = """
            <html>
                <body>
                    Hello \${personName}!
                </body>
            </html>
        """

        String templateName = "testSimpleGuidSubstitution";

        TestDataField testDataField = new TestDataField(name:'personName', guid:'555-1212')

        VelocityContext context = new VelocityContext();
        context.put( testDataField.name, "\${${testDataField.guid}}" );

        StringWriter stringWriter = new StringWriter();
        velocityEngine.evaluate( context, stringWriter, templateName, templateString );
        System.out.println(" string : " + stringWriter );
    }


//    @Test
//    void testComplexGuidSubstitution() {
//        String templateString = """
//            <html>
//                <body>
//                    Hello \${person.firstName} \${person.lastName}!
//                </body>
//            </html>
//        """
//
//        String templateName = "testComplexGuidSubstitution";
//
//        TestPerson aPerson = new TestPerson( firstName:'Michael', lastName:'Brzycki' )
//        VelocityContext context = new VelocityContext();
//        context.put( "person", aPerson );
//
//        StringWriter stringWriter = new StringWriter();
//        velocityEngine.evaluate( context, stringWriter, templateName, templateString );
//        System.out.println(" string : " + stringWriter );
//
//    }

}

class TestPerson {
    String lastName
    String firstName
}

class TestDataField {
    String name
    String guid

    String toCanonicalString() {
        return "\${${this.guid}}"
    }
}
//
//class CustomContext extends VelocityContext {
//    Object get(String key) {
//        System.out.println( key )
//        return super.getKey( key )
//    }
//}

