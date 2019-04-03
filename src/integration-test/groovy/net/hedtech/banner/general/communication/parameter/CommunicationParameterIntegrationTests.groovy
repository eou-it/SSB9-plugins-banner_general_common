package net.hedtech.banner.general.communication.parameter

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback

@Integration
@Rollback
class CommunicationParameterIntegrationTests extends BaseIntegrationTestCase {

    def i_valid_name = "firstName"
    def i_valid_title = "First Name"
    def i_valid_type = CommunicationParameterType.TEXT.name()

    def u_valid_name = "lastName"
    def u_valid_title = "Last Name"
    def u_valid_type = CommunicationParameterType.TEXT.name()

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
    void testCreateValidParameter() {
        def parameter = newValidForCreateParameter()
        parameter.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull parameter.id
    }


    @Test
    void testDelete() {
        def parameter = newValidForCreateParameter()
        parameter.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull parameter.id
        parameter.delete()
        def id = parameter.id
        assertNull parameter.get(id)
    }


    @Test
    void testUpdate() {
        def parameter = newValidForCreateParameter()
        parameter.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull parameter.id
        parameter.title = u_valid_title
        parameter.type = u_valid_type
        parameter.name = u_valid_name

        parameter.save()
        def id = parameter.id
        def updatedParameter = parameter.get(id)
        assertEquals("lastName", u_valid_title, parameter.title)
        assertEquals(CommunicationParameterType.TEXT.name(), u_valid_type, parameter.type.name())
        assertEquals("Last Name", u_valid_name, parameter.name)
    }


    @Test
    void testExistsAnotherNameParameter() {
        def parameter = newValidForCreateParameter()
        parameter.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull parameter.id

        Boolean falseResult = CommunicationParameter.existsAnotherName(parameter.id, parameter.name)
        assertFalse(falseResult)

        def parameter2 = newValidForCreateParameter()
        parameter2.name = "Duplicate Parameter"
        parameter2.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull parameter2.id

        Boolean trueResult = CommunicationParameter.existsAnotherName(parameter.id, parameter2.name)
        assertTrue(trueResult)
    }

    private def newValidForCreateParameter() {
        def parameter = new CommunicationParameter(
                title: i_valid_title,
                type: i_valid_type,
                name: i_valid_name
        )
        return parameter
    }
}
