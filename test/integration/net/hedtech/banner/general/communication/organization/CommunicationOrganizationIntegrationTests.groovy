/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.organization

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 * OrganizationTest.
 */
class CommunicationOrganizationIntegrationTests extends BaseIntegrationTestCase {

    def i_valid_name = "My Organization"
    def i_valid_description = "My Organization"


    def u_valid_name = "My Organization1"
    def u_valid_description = "My Organization1"


    def i_invalid_name = "My Organization".padLeft(1021)
    def i_invalid_description = "My Organization".padLeft(4001)


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
    void testCreateValidOrganization() {
        def organization = newValidForCreateOrganization()
        organization.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull organization.id
    }


    @Test
    void testDelete() {
        def organization = newValidForCreateOrganization()
        organization.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull organization.id
        organization.delete()
        def id = organization.id
        assertNull organization.get(id)
    }


    @Test
    void testUpdate() {
        def organization = newValidForCreateOrganization()
        organization.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull organization.id
        organization.description = u_valid_description

        organization.name = u_valid_name

        organization.save()
        def id = organization.id
        def updatedOrganization = organization.get(id)
        assertEquals("Updated description", u_valid_description, organization.description)

        assertEquals("Updated name", u_valid_name, organization.name)
    }


    @Test
    void testCreateInValidOrganization() {
        def organization = newValidForCreateOrganization()

        organization = newValidForCreateOrganization()
        organization.description = i_invalid_description
        shouldFail { organization.save(failOnError: true, flush: true) }


        organization = newValidForCreateOrganization()
        organization.name = i_invalid_name
        shouldFail { organization.save(failOnError: true, flush: true) }
    }


    private def newValidForCreateOrganization() {
        def organization = new CommunicationOrganization(
                description: i_valid_description,
                name: i_valid_name
        )
        return organization
    }
}
