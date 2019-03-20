/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall
import org.junit.Before
import org.junit.Test
import org.junit.After

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.general.system.*
import net.hedtech.banner.testing.BaseIntegrationTestCase

class InternationalInformationCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    def internationalInformationCompositeService

    // stored mapping of the primary keys for the child records
    def mapPK


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
    void testCompositeServiceCreate() {
        // create and test new records
        createAll()
    }


    @Test
    void testCompositeServiceDelete() {
        // create and test new records
        def deleteMap = createAll()

        // delete them
        internationalInformationCompositeService.createOrUpdate([deleteDocumentInformations: [deleteMap.documentInformation]])
        internationalInformationCompositeService.createOrUpdate([deleteVisaInformations: [deleteMap.visaInformation]])

        internationalInformationCompositeService.createOrUpdate([deleteVisaInternationalInformation: deleteMap.visaInternationalInformation])

        // test the deleted records does not exists
        def recMap = fetchAll()
        assertNull recMap.visaInformation
        assertNull recMap.documentInformation
        assertNull recMap.visaInternationalInformation
    }


    @Test
    void testCompositeServiceUpdate() {
        // create and test new records
        def map = createAll()

        // update the records
        map.visaInformation.nationIssue = "2"
        internationalInformationCompositeService.createOrUpdate([visaInformations: [map.visaInformation]])

        map.documentInformation.disposition = "A"
        internationalInformationCompositeService.createOrUpdate([documentInformations: [map.documentInformation]])

        map.visaInternationalInformation.spouseIndicator = "N"
        internationalInformationCompositeService.createOrUpdate([visaInternationalInformation: map.visaInternationalInformation])

        // test the updates worked
        def recMap = fetchAll()

        assertEquals 1L, recMap.visaInformation.version
        assertEquals "2", recMap.visaInformation.nationIssue

        assertEquals 1L, recMap.documentInformation.version
        assertEquals "A", recMap.documentInformation.disposition

        assertEquals 1L, recMap.visaInternationalInformation.version
        assertEquals "N", recMap.visaInternationalInformation.spouseIndicator
    }


    @Test
    void testCompositeServiceDuplicateDocument() {
        // create and test new records
        createAll()

        // create a duplicate document
        def documentInformation = newValidForCreateDocumentInformation()

        try {
            internationalInformationCompositeService.createOrUpdate([documentInformations: [documentInformation]])
            fail "Should have failed with @@r1:duplicateDocument"
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "duplicateDocument"
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Setup
    // -----------------------------------------------------------------------------------------------------------------

    // create test records with concentration/major/minor value
    private def createAll() {
        mapPK =
            [
                    pidm: PersonUtility.getPerson("HOR000008").pidm,
                    sequenceNumber: null,
                    visaNumber: "123456789012345678",
            ]


        def visaInformation = newValidForCreateVisaInformation()
        internationalInformationCompositeService.createOrUpdate([visaInformations: [visaInformation]])

        mapPK.sequenceNumber = visaInformation.sequenceNumber

        def documentInformation = newValidForCreateDocumentInformation()
        internationalInformationCompositeService.createOrUpdate([documentInformations: [documentInformation]])

        // Single
        def visaInternationalInformation = newValidForCreateVisaInternationalInformation()
        internationalInformationCompositeService.createOrUpdate([visaInternationalInformation: visaInternationalInformation])

        // confirm these records exists
        def map = fetchAll()
        assertNotNull map.visaInformation.version
        assertNotNull map.documentInformation.version
        assertNotNull map.visaInternationalInformation.version

        return map
    }

    // fetch test records with concentration/major/minor value
    private def fetchAll() {
        def visaInformation = VisaInformation.findByPidmAndSequenceNumber(mapPK.pidm, mapPK.sequenceNumber)
        def documentInformation = DocumentInformation.findByPidmAndSequenceNumber(mapPK.pidm, mapPK.sequenceNumber)
        def visaInternationalInformation = VisaInternationalInformation.findByPidm(mapPK.pidm)

        def map =
            [
                    visaInformation: visaInformation,
                    documentInformation: documentInformation,
                    visaInternationalInformation: visaInternationalInformation,
            ]

        return map
    }


    private def newValidForCreateVisaInformation() {
        def visaInformation = new VisaInformation(
                pidm: mapPK.pidm,
                sequenceNumber: mapPK.sequenceNumber,
                visaNumber: mapPK.visaNumber,
                nationIssue: "1",
                visaStartDate: new Date(),
                visaExpireDate: new Date(),
                entryIndicator: true,
                visaRequiredDate: new Date(),
                visaIssueDate: new Date(),
                numberEntries: "M",
                visaType: VisaType.findByCode("F1"),
                visaIssuingAuthority: VisaIssuingAuthority.findByCode("PARIS"),
                portOfEntry: PortOfEntry.findByCode("NYC"),
        )
        return visaInformation
    }


    private def newValidForCreateDocumentInformation() {
        def documentInformation = new DocumentInformation(
                pidm: mapPK.pidm,
                sequenceNumber: mapPK.sequenceNumber,
                visaNumber: mapPK.visaNumber,
                disposition: "P",
                requestDate: new Date(),
                receivedDate: new Date(),
                document: Document.findByCode("VISA"),
                visaSource: VisaSource.findByCode("IRS"),
                visaType: VisaType.findByCode("F1"),
        )
        return documentInformation
    }


    private def newValidForCreateVisaInternationalInformation() {
        def visaInternationalInformation = new VisaInternationalInformation(
                pidm: mapPK.pidm,
                spouseIndicator: "Y",
                signatureIndicator: "N",
                passportId: "1234567890123456789012345",
                nationIssue: "1",
                passportExpenditureDate: new Date(),
                i94Status: "123",
                i94Date: new Date(),
                registrationNumber: "12345678901",
                duration: "Y",
                certificateNumber: "1234567890X",
                certificateDateIssue: new Date(),
                certificateDateReceipt: new Date(),
                nationBirth: "2",
                nationLegal: "3",
                foreignSsn: "123456789012345",
                childNumber: 99,
                certificationOfEligibility: CertificationOfEligibility.findByCode("I-20"),
                admissionRequest: AdmissionRequest.findByCode("VISA"),
                language: Language.findByCode("SPN"),
                internationalSponsor: InternationalSponsor.findByCode("SUN"),
                employmentType: EmploymentType.findByCode("STU"),
        )
        return visaInternationalInformation
    }
}
