/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.junit.Before
import org.junit.Test
import org.junit.After

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.general.system.Document
import net.hedtech.banner.general.system.VisaSource
import net.hedtech.banner.general.system.VisaType
import net.hedtech.banner.testing.BaseIntegrationTestCase

@Integration
@Rollback
class DocumentInformationServiceIntegrationTests extends BaseIntegrationTestCase {

    def documentInformationService


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
    void testDocumentInformationValidCreate() {
        def documentInformation = newValidForCreateDocumentInformation()
        def map = [domainModel: documentInformation]
        documentInformation = documentInformationService.create(map)
        assertNotNull "DocumentInformation ID is null in DocumentInformation Service Tests Create", documentInformation.id
        assertNotNull "DocumentInformation document is null in DocumentInformation Service Tests", documentInformation.document
        assertNotNull "DocumentInformation visaSource is null in DocumentInformation Service Tests", documentInformation.visaSource
        assertNotNull "DocumentInformation visaType is null in DocumentInformation Service Tests", documentInformation.visaType
        assertNotNull documentInformation.version
        assertNotNull documentInformation.dataOrigin
        assertNotNull documentInformation.lastModifiedBy
        assertNotNull documentInformation.lastModified
    }


    @Test
    void testDocumentInformationInvalidCreate() {
        def documentInformation = newInvalidForCreateDocumentInformation()
        def map = [domainModel: documentInformation]
        shouldFail(ApplicationException) {
            documentInformationService.create(map)
        }
    }

    @Test
    void testDocumentInformationValidUpdate() {
        def documentInformation = newValidForCreateDocumentInformation()
        def map = [domainModel: documentInformation]
        documentInformation = documentInformationService.create(map)
        assertNotNull "DocumentInformation ID is null in DocumentInformation Service Tests Create", documentInformation.id
        assertNotNull "DocumentInformation document is null in DocumentInformation Service Tests", documentInformation.document
        assertNotNull "DocumentInformation visaSource is null in DocumentInformation Service Tests", documentInformation.visaSource
        assertNotNull "DocumentInformation visaType is null in DocumentInformation Service Tests", documentInformation.visaType
        assertNotNull documentInformation.version
        assertNotNull documentInformation.dataOrigin
        assertNotNull documentInformation.lastModifiedBy
        assertNotNull documentInformation.lastModified

        //Update the entity with new values
        documentInformation.visaNumber = "123456789012UPDATE"
        documentInformation.disposition = "A"
        documentInformation.visaSource = VisaSource.findByCode("INS")

        map.domainModel = documentInformation
        documentInformation = documentInformationService.update(map)

        // test the values
        assertEquals "123456789012UPDATE", documentInformation.visaNumber
        assertEquals "A", documentInformation.disposition
        assertEquals "INS", documentInformation.visaSource.code
    }


    @Test
    void testDocumentInformationInvalidUpdate() {
        def documentInformation = newValidForCreateDocumentInformation()
        def map = [domainModel: documentInformation]
        documentInformation = documentInformationService.create(map)
        assertNotNull "DocumentInformation ID is null in DocumentInformation Service Tests Create", documentInformation.id
        assertNotNull "DocumentInformation document is null in DocumentInformation Service Tests", documentInformation.document
        assertNotNull "DocumentInformation visaSource is null in DocumentInformation Service Tests", documentInformation.visaSource
        assertNotNull "DocumentInformation visaType is null in DocumentInformation Service Tests", documentInformation.visaType
        assertNotNull documentInformation.version
        assertNotNull documentInformation.dataOrigin
        assertNotNull documentInformation.lastModifiedBy
        assertNotNull documentInformation.lastModified

        //Update the entity with new invalid values
        documentInformation.visaNumber = "123456789012345678Z"

        map.domainModel = documentInformation
        shouldFail(ApplicationException) {
            documentInformation = documentInformationService.update(map)
        }
    }


    @Test
    void testDocumentInformationDelete() {
        def documentInformation = newValidForCreateDocumentInformation()
        def map = [domainModel: documentInformation]
        documentInformation = documentInformationService.create(map)
        assertNotNull "DocumentInformation ID is null in DocumentInformation Service Tests Create", documentInformation.id

        def id = documentInformation.id
        documentInformationService.delete([domainModel: documentInformation])
        assertNull "DocumentInformation should have been deleted", documentInformation.get(id)
    }


    @Test
    void testReadOnly() {
        def documentInformation = newValidForCreateDocumentInformation()
        def map = [domainModel: documentInformation]
        documentInformation = documentInformationService.create(map)
        assertNotNull "DocumentInformation ID is null in DocumentInformation Service Tests Create", documentInformation.id

        documentInformation.pidm = PersonUtility.getPerson("HOR000002").pidm
        try {
            documentInformationService.update([domainModel: documentInformation])
            fail("This should have failed with @@r1:readonlyFieldsCannotBeModified")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "readonlyFieldsCannotBeModified"
        }
    }


    @Test
    void testMissingRequestDate() {
        def documentInformation = new DocumentInformation(
                pidm: PersonUtility.getPerson("HOR000001").pidm,
                sequenceNumber: 1,
                disposition: "P",
                requestDate: null,
                document: Document.findByCode("VISA"),
                visaType: VisaType.findByCode("F1"),
        )
        try {
            documentInformationService.create([domainModel: documentInformation])
            fail("This should have failed with @@r1:missingRequestDate")
        }
        catch (ApplicationException ae) { assertApplicationException ae, "missingRequestDate" }
    }


    @Test
    void testInvalidDocumentDate() {
        def documentInformation = new DocumentInformation(
                pidm: PersonUtility.getPerson("HOR000001").pidm,
                sequenceNumber: 1,
                disposition: "P",
                requestDate: new Date(),
                receivedDate: new Date() - 1,
                document: Document.findByCode("VISA"),
                visaType: VisaType.findByCode("F1"),
        )
        try {
            documentInformationService.create([domainModel: documentInformation])
            fail("This should have failed with @@r1:invalidDocumentDate")
        }
        catch (ApplicationException ae) { assertApplicationException ae, "invalidDocumentDate" }
    }


    private def newValidForCreateDocumentInformation() {
        def documentInformation = new DocumentInformation(
                pidm: PersonUtility.getPerson("HOR000001").pidm,
                sequenceNumber: 1,
                visaNumber: "123456789012345678",
                disposition: "P",
                requestDate: new Date(),
                receivedDate: new Date(),
                document: Document.findByCode("VISA"),
                visaSource: VisaSource.findByCode("IRS"),
                visaType: VisaType.findByCode("F1"),
        )
        return documentInformation
    }


    private def newInvalidForCreateDocumentInformation() {
        def documentInformation = new DocumentInformation()
        return documentInformation
    }
}
