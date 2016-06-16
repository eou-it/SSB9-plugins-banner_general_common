/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.letter

import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.letter.CommunicationLetterTemplate
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Tests basic CRUD operations on an CommunicationLetterTemplate entity object
 * and any field level validation.
 */
class CommunicationLetterTemplateIntegrationTests extends BaseIntegrationTestCase {

    def CommunicationFolder defaultFolder


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        defaultFolder = new CommunicationFolder(
            name: "default folder"
        )
        defaultFolder.save( failOnError: true, flush: true )
        //Test if the generated entity now has an id assigned
        assertNotNull defaultFolder.id
    }

    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }

    @Test
    void testCreate() {
        CommunicationLetterTemplate template = new CommunicationLetterTemplate(
            name: "create test",
            createdBy: 'MIKE',
            createDate: new Date(),
            validFrom: new Date(),
            validTo: null,
            folder: defaultFolder
        )

        template.save( failOnError: true, flush: true )
    }

    @Test
    void testUpdate() {
        CommunicationLetterTemplate template = new CommunicationLetterTemplate(
                name: "update test",
                description: "description",
                createdBy: 'MIKE',
                createDate: new Date(),
                validFrom: new Date(),
                validTo: null,
                folder: defaultFolder
        )

        template.save( failOnError: true, flush: true )

        template.toAddress = "Planet Mars"
        template.content = "ET phone home!"
        template.save( failOnError: true, flush: true )

        template = CommunicationLetterTemplate.get( template.id )
        assertEquals( "Planet Mars", template.toAddress )
        assertEquals( "ET phone home!", template.content )
    }

    @Test
    void testDelete() {
        int templateCount = CommunicationLetterTemplate.findAll().size()

        CommunicationLetterTemplate template = new CommunicationLetterTemplate(
                name: "delete test",
                createdBy: 'MIKE',
                createDate: new Date(),
                validFrom: new Date(),
                validTo: null,
                folder: defaultFolder
        )

        template.save( failOnError: true, flush: true )

        assertEquals( templateCount + 1, CommunicationLetterTemplate.findAll().size() )

        template.delete()

        assertEquals( templateCount, CommunicationLetterTemplate.findAll().size() )
    }

}
