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
import net.hedtech.banner.general.system.*
import net.hedtech.banner.testing.BaseIntegrationTestCase

@Integration
@Rollback
class VisaInternationalInformationServiceIntegrationTests extends BaseIntegrationTestCase {

    def visaInternationalInformationService


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
    void testVisaInternationalInformationValidCreate() {
        def visaInternationalInformation = newValidForCreateVisaInternationalInformation()
        def map = [domainModel: visaInternationalInformation]
        visaInternationalInformation = visaInternationalInformationService.create(map)
        assertNotNull "VisaInternationalInformation ID is null in VisaInternationalInformation Service Tests Create", visaInternationalInformation.id
        assertNotNull "VisaInternationalInformation certificationOfEligibility is null in VisaInternationalInformation Service Tests", visaInternationalInformation.certificationOfEligibility
        assertNotNull "VisaInternationalInformation admissionRequest is null in VisaInternationalInformation Service Tests", visaInternationalInformation.admissionRequest
        assertNotNull "VisaInternationalInformation language is null in VisaInternationalInformation Service Tests", visaInternationalInformation.language
        assertNotNull "VisaInternationalInformation internationalSponsor is null in VisaInternationalInformation Service Tests", visaInternationalInformation.internationalSponsor
        assertNotNull "VisaInternationalInformation employmentType is null in VisaInternationalInformation Service Tests", visaInternationalInformation.employmentType
        assertNotNull visaInternationalInformation.version
        assertNotNull visaInternationalInformation.dataOrigin
        assertNotNull visaInternationalInformation.lastModifiedBy
        assertNotNull visaInternationalInformation.lastModified
    }


    @Test
    void testVisaInternationalInformationInvalidCreate() {
        def visaInternationalInformation = newInvalidForCreateVisaInternationalInformation()
        def map = [domainModel: visaInternationalInformation]
        shouldFail(ApplicationException) {
            visaInternationalInformationService.create(map)
        }
    }


    @Test
    void testVisaInternationalInformationValidUpdate() {
        def visaInternationalInformation = newValidForCreateVisaInternationalInformation()
        def map = [domainModel: visaInternationalInformation]
        visaInternationalInformation = visaInternationalInformationService.create(map)
        assertNotNull "VisaInternationalInformation ID is null in VisaInternationalInformation Service Tests Create", visaInternationalInformation.id
        assertNotNull "VisaInternationalInformation certificationOfEligibility is null in VisaInternationalInformation Service Tests", visaInternationalInformation.certificationOfEligibility
        assertNotNull "VisaInternationalInformation admissionRequest is null in VisaInternationalInformation Service Tests", visaInternationalInformation.admissionRequest
        assertNotNull "VisaInternationalInformation language is null in VisaInternationalInformation Service Tests", visaInternationalInformation.language
        assertNotNull "VisaInternationalInformation internationalSponsor is null in VisaInternationalInformation Service Tests", visaInternationalInformation.internationalSponsor
        assertNotNull "VisaInternationalInformation employmentType is null in VisaInternationalInformation Service Tests", visaInternationalInformation.employmentType
        assertNotNull visaInternationalInformation.version
        assertNotNull visaInternationalInformation.dataOrigin
        assertNotNull visaInternationalInformation.lastModifiedBy
        assertNotNull visaInternationalInformation.lastModified

        //Update the entity with new values
        visaInternationalInformation.spouseIndicator = "N"
        visaInternationalInformation.signatureIndicator = "T"
        visaInternationalInformation.passportId = "1234567890123456789UPDATE"
        visaInternationalInformation.nationIssue = "5"
        visaInternationalInformation.i94Status = "UPD"
        visaInternationalInformation.registrationNumber = "12345678UPD"
        visaInternationalInformation.duration = "N"
        visaInternationalInformation.certificateNumber = "12345UPDATE"
        visaInternationalInformation.nationBirth = "6"
        visaInternationalInformation.nationLegal = "7"
        visaInternationalInformation.foreignSsn = "123456789012UPD"
        visaInternationalInformation.childNumber = 88
        visaInternationalInformation.certificationOfEligibility = CertificationOfEligibility.findByCode("I-94")
        visaInternationalInformation.admissionRequest = AdmissionRequest.findByCode("TUTD")
        visaInternationalInformation.language = Language.findByCode("JPN")
        visaInternationalInformation.internationalSponsor = InternationalSponsor.findByCode("SOI")
        visaInternationalInformation.employmentType = EmploymentType.findByCode("EMP")

        map.domainModel = visaInternationalInformation
        visaInternationalInformation = visaInternationalInformationService.update(map)

        // test the values
        assertEquals "N", visaInternationalInformation.spouseIndicator
        assertEquals "T", visaInternationalInformation.signatureIndicator
        assertEquals "1234567890123456789UPDATE", visaInternationalInformation.passportId
        assertEquals "5", visaInternationalInformation.nationIssue
        assertEquals "UPD", visaInternationalInformation.i94Status
        assertEquals "12345678UPD", visaInternationalInformation.registrationNumber
        assertEquals "N", visaInternationalInformation.duration
        assertEquals "12345UPDATE", visaInternationalInformation.certificateNumber
        assertEquals "6", visaInternationalInformation.nationBirth
        assertEquals "7", visaInternationalInformation.nationLegal
        assertEquals "123456789012UPD", visaInternationalInformation.foreignSsn
        assertEquals 88, visaInternationalInformation.childNumber
        assertEquals "I-94", visaInternationalInformation.certificationOfEligibility.code
        assertEquals "TUTD", visaInternationalInformation.admissionRequest.code
        assertEquals "JPN", visaInternationalInformation.language.code
        assertEquals "SOI", visaInternationalInformation.internationalSponsor.code
        assertEquals "EMP", visaInternationalInformation.employmentType.code
    }


    @Test
    void testVisaInternationalInformationInvalidUpdate() {
        def visaInternationalInformation = newValidForCreateVisaInternationalInformation()
        def map = [domainModel: visaInternationalInformation]
        visaInternationalInformation = visaInternationalInformationService.create(map)
        assertNotNull "VisaInternationalInformation ID is null in VisaInternationalInformation Service Tests Create", visaInternationalInformation.id
        assertNotNull "VisaInternationalInformation certificationOfEligibility is null in VisaInternationalInformation Service Tests", visaInternationalInformation.certificationOfEligibility
        assertNotNull "VisaInternationalInformation admissionRequest is null in VisaInternationalInformation Service Tests", visaInternationalInformation.admissionRequest
        assertNotNull "VisaInternationalInformation language is null in VisaInternationalInformation Service Tests", visaInternationalInformation.language
        assertNotNull "VisaInternationalInformation internationalSponsor is null in VisaInternationalInformation Service Tests", visaInternationalInformation.internationalSponsor
        assertNotNull "VisaInternationalInformation employmentType is null in VisaInternationalInformation Service Tests", visaInternationalInformation.employmentType
        assertNotNull visaInternationalInformation.version
        assertNotNull visaInternationalInformation.dataOrigin
        assertNotNull visaInternationalInformation.lastModifiedBy
        assertNotNull visaInternationalInformation.lastModified

        //Update the entity with new invalid values
        visaInternationalInformation.spouseIndicator = "Z"
        map.domainModel = visaInternationalInformation
        shouldFail(ApplicationException) {
            visaInternationalInformation = visaInternationalInformationService.update(map)
        }
    }


    @Test
    void testVisaInternationalInformationDelete() {
        def visaInternationalInformation = newValidForCreateVisaInternationalInformation()
        def map = [domainModel: visaInternationalInformation]
        visaInternationalInformation = visaInternationalInformationService.create(map)
        assertNotNull "VisaInternationalInformation ID is null in VisaInternationalInformation Service Tests Create", visaInternationalInformation.id

        def id = visaInternationalInformation.id
        visaInternationalInformationService.delete([domainModel: visaInternationalInformation])
        assertNull "VisaInternationalInformation should have been deleted", visaInternationalInformation.get(id)
    }


    @Test
    void testReadOnly() {
        def visaInternationalInformation = newValidForCreateVisaInternationalInformation()
        def map = [domainModel: visaInternationalInformation]
        visaInternationalInformation = visaInternationalInformationService.create(map)
        assertNotNull "VisaInternationalInformation ID is null in VisaInternationalInformation Service Tests Create", visaInternationalInformation.id

        visaInternationalInformation.pidm = PersonUtility.getPerson("HOR000002").pidm
        try {
            visaInternationalInformationService.update([domainModel: visaInternationalInformation])
            fail("This should have failed with @@r1:readonlyFieldsCannotBeModified")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "readonlyFieldsCannotBeModified"
        }
    }


    @Test
    void testMissingNationOfIssue() {
        def visaInternationalInformation = new VisaInternationalInformation(
                pidm: PersonUtility.getPerson("HOR000008").pidm,
                spouseIndicator: "Y",
                signatureIndicator: "N",
                passportId: "1234567890123456789012345",
                nationIssue: null,
        )
        try {
            visaInternationalInformationService.create([domainModel: visaInternationalInformation])
            fail("This should have failed with @@r1:missingNationOfIssue")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "missingNationOfIssue"
        }
    }


    @Test
    void testInvalidCertificationDate() {
        def visaInternationalInformation = new VisaInternationalInformation(
                pidm: PersonUtility.getPerson("HOR000008").pidm,
                spouseIndicator: "Y",
                signatureIndicator: "N",
                certificateDateIssue: new Date(),
                certificateDateReceipt: new Date() - 1,
        )
        try {
            visaInternationalInformationService.create([domainModel: visaInternationalInformation])
            fail("This should have failed with @@r1:invalidCertificationDate")
        }
        catch (ApplicationException ae) { assertApplicationException ae, "invalidCertificationDate" }
    }


    @Test
    void testDefaultRequired() {
        def visaInternationalInformation = new VisaInternationalInformation(
                pidm: PersonUtility.getPerson("HOR000008").pidm,
        )
        visaInternationalInformation = visaInternationalInformationService.create([domainModel: visaInternationalInformation])
        assertEquals "T", visaInternationalInformation.spouseIndicator
        assertEquals "T", visaInternationalInformation.signatureIndicator
    }


    private def newValidForCreateVisaInternationalInformation() {
        def visaInternationalInformation = new VisaInternationalInformation(
                pidm: PersonUtility.getPerson("HOR000008").pidm,
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


    private def newInvalidForCreateVisaInternationalInformation() {
        def visaInternationalInformation = new VisaInternationalInformation()
        return visaInternationalInformation
    }
}
