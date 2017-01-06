/*********************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.parameter

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Tests crud methods provided by parameter service.
 */
class CommunicationParameterServiceIntegrationTests extends BaseIntegrationTestCase {
    
    def communicationParameterService
    def selfServiceBannerAuthenticationProvider


    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']
        super.setUp()
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('BCMADMIN', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)
    }


    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }


    @Test
    void testList() {
        long originalListCount = communicationParameterService.list().size()

        CommunicationParameter parameter = new CommunicationParameter()
        parameter.name = "firstName"
        parameter.title = "First Name"
        parameter.type = CommunicationParameterType.TEXT
        CommunicationParameter createdParameter = communicationParameterService.create(parameter)
        assertNotNull(createdParameter)

        long addedListCount = communicationParameterService.list().size()
        assertEquals(originalListCount + 1, addedListCount)
    }


    @Test
    void testCreate() {
        CommunicationParameter parameter = new CommunicationParameter()
        parameter.name = "firstName"
        parameter.title = "First Name"
        parameter.type = CommunicationParameterType.TEXT
        CommunicationParameter createdParameter = communicationParameterService.create(parameter)
        assertNotNull(createdParameter)
        assertEquals("firstName", createdParameter.name)
        assertEquals("First Name", createdParameter.title)
        assertEquals(CommunicationParameterType.TEXT, createdParameter.type)

        CommunicationParameter foundParameter = CommunicationParameter.fetchByName("firstName")
        assertEquals(createdParameter, foundParameter)

        CommunicationParameter sameNameParameter = new CommunicationParameter()
        sameNameParameter.name = "firstName"
        sameNameParameter.title = "another parameter with same name"
        try {
            communicationParameterService.create(sameNameParameter)
            Assert.fail "Expected sameNameParameter to fail because of name unique constraint."
        } catch (ApplicationException e) {
            assertEquals("@@r1:parameterNameAlreadyExists@@", e.getMessage())
        }
    }


    @Test
    void testUpdate() {
        CommunicationParameter parameter1 = new CommunicationParameter()
        parameter1.name = "firstName"
        parameter1.title = "First Name"
        parameter1.type = CommunicationParameterType.TEXT
        parameter1 = communicationParameterService.create(parameter1)

        parameter1 = CommunicationParameter.get(parameter1.getId())
        parameter1.setName("lastName")
        parameter1.setTitle("Last Name")
        parameter1 = communicationParameterService.update(parameter1)

        assertEquals("lastName", parameter1.getName())
        assertEquals("Last Name", parameter1.getTitle())

        CommunicationParameter parameter2 = new CommunicationParameter()
        parameter2.name = "firstName"
        parameter2.title = "First Name"
        parameter2.type = CommunicationParameterType.TEXT
        parameter2 = communicationParameterService.create(parameter2)

        parameter1.name = parameter2.name
        try {
            communicationParameterService.update(parameter1)
            Assert.fail "Expected sameNameParameter to fail because of name unique constraint."
        } catch (ApplicationException e) {
            assertEquals("@@r1:parameterNameAlreadyExists@@", e.message)
        }
    }

    @Test
    void testDelete() {
        CommunicationParameter parameter = new CommunicationParameter();
        parameter.name = "firstName"
        parameter.title = "First Name"
        parameter.type = CommunicationParameterType.TEXT
        CommunicationParameter createdParameter = communicationParameterService.create(parameter)
        assertNotNull(createdParameter)
        Long id = createdParameter.getId()

        long count = communicationParameterService.list().size()

        communicationParameterService.delete(createdParameter)

        assertEquals(count - 1, communicationParameterService.list().size())

        try {
            assertNull(communicationParameterService.get(id))
            Assert.fail "Expected get by id to fail because does not exist."
        } catch (ApplicationException e) {
            assertEquals("NotFoundException", e.getType())
        }
    }

    @Test
    void testCreateEmptyName() {
        CommunicationParameter parameter = new CommunicationParameter()
        parameter.name = ""
        parameter.title = "First Name"
        parameter.type = CommunicationParameterType.TEXT
        try {
            communicationParameterService.create(parameter)
        } catch (ApplicationException e) {
            assertEquals("@@r1:nameCannotBeNull@@", e.getMessage())
        }
    }

    @Test
    void testCreateNameWithSpaces() {
        CommunicationParameter parameter = new CommunicationParameter()
        parameter.name = "first Name"
        parameter.title = "First Name"
        parameter.type = CommunicationParameterType.TEXT
        try {
            communicationParameterService.create(parameter)
        } catch (ApplicationException e) {
            assertEquals("@@r1:space.not.allowed@@", e.getMessage())
        }
    }

    @Test
    void testCreateNameWithDollar() {
        CommunicationParameter parameter = new CommunicationParameter()
        parameter.name = "\$firstName"
        parameter.title = "First Name"
        parameter.type = CommunicationParameterType.TEXT
        try {
            communicationParameterService.create(parameter)
        } catch (ApplicationException e) {
            assertEquals("@@r1:dollarCharacter.not.allowed@@", e.getMessage())
        }
    }

    @Test
    void testCreateNameWithColon() {
        CommunicationParameter parameter = new CommunicationParameter()
        parameter.name = ":firstName"
        parameter.title = "First Name"
        parameter.type = CommunicationParameterType.TEXT
        try {
            communicationParameterService.create(parameter)
        } catch (ApplicationException e) {
            assertEquals("@@r1:colonNotAllowedInParameterName@@", e.getMessage())
        }
    }
}
