/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.general.system.PortOfEntry
import net.hedtech.banner.general.system.VisaIssuingAuthority
import net.hedtech.banner.general.system.VisaType
import net.hedtech.banner.testing.BaseIntegrationTestCase

class VisaInformationServiceIntegrationTests extends BaseIntegrationTestCase {

    def visaInformationService


    void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    void tearDown() {
        super.tearDown()
    }


    void testVisaInformationValidCreate() {
        def visaInformation = newValidForCreateVisaInformation()
        def map = [domainModel: visaInformation]
        visaInformation = visaInformationService.create(map)
        assertNotNull "VisaInformation ID is null in VisaInformation Service Tests Create", visaInformation.id
        assertNotNull "VisaInformation visaType is null in VisaInformation Service Tests", visaInformation.visaType
        assertNotNull "VisaInformation visaIssuingAuthority is null in VisaInformation Service Tests", visaInformation.visaIssuingAuthority
        assertNotNull "VisaInformation portOfEntry is null in VisaInformation Service Tests", visaInformation.portOfEntry
        assertNotNull visaInformation.version
        assertNotNull visaInformation.dataOrigin
        assertNotNull visaInformation.lastModifiedBy
        assertNotNull visaInformation.lastModified
    }


    void testVisaInformationInvalidCreate() {
        def visaInformation = newInvalidForCreateVisaInformation()
        def map = [domainModel: visaInformation]
        shouldFail(ApplicationException) {
            visaInformationService.create(map)
        }
    }


    void testVisaInformationValidUpdate() {
        def visaInformation = newValidForCreateVisaInformation()
        def map = [domainModel: visaInformation]
        visaInformation = visaInformationService.create(map)
        assertNotNull "VisaInformation ID is null in VisaInformation Service Tests Create", visaInformation.id
        assertNotNull "VisaInformation visaType is null in VisaInformation Service Tests", visaInformation.visaType
        assertNotNull "VisaInformation visaIssuingAuthority is null in VisaInformation Service Tests", visaInformation.visaIssuingAuthority
        assertNotNull "VisaInformation portOfEntry is null in VisaInformation Service Tests", visaInformation.portOfEntry
        assertNotNull visaInformation.version
        assertNotNull visaInformation.dataOrigin
        assertNotNull visaInformation.lastModifiedBy
        assertNotNull visaInformation.lastModified

        //Update the entity with new values
        visaInformation.visaNumber = "123456789012UPDATE"
        visaInformation.nationIssue = "2"
        visaInformation.entryIndicator = false
        visaInformation.numberEntries = ""
        visaInformation.visaIssuingAuthority = VisaIssuingAuthority.findByCode("CHINAE")
        visaInformation.portOfEntry = PortOfEntry.findByCode("CHI")

        map.domainModel = visaInformation
        visaInformation = visaInformationService.update(map)
        // test the values
        assertEquals "123456789012UPDATE", visaInformation.visaNumber
        assertEquals "2", visaInformation.nationIssue
        assertEquals false, visaInformation.entryIndicator
        assertNull visaInformation.numberEntries
        assertEquals "CHINAE", visaInformation.visaIssuingAuthority.code
        assertEquals "CHI", visaInformation.portOfEntry.code
    }


    void testVisaInformationInvalidUpdate() {
        def visaInformation = newValidForCreateVisaInformation()
        def map = [domainModel: visaInformation]
        visaInformation = visaInformationService.create(map)
        assertNotNull "VisaInformation ID is null in VisaInformation Service Tests Create", visaInformation.id
        assertNotNull "VisaInformation visaType is null in VisaInformation Service Tests", visaInformation.visaType
        assertNotNull "VisaInformation visaIssuingAuthority is null in VisaInformation Service Tests", visaInformation.visaIssuingAuthority
        assertNotNull "VisaInformation portOfEntry is null in VisaInformation Service Tests", visaInformation.portOfEntry
        assertNotNull visaInformation.version
        assertNotNull visaInformation.dataOrigin
        assertNotNull visaInformation.lastModifiedBy
        assertNotNull visaInformation.lastModified

        //Update the entity with new invalid values
        visaInformation.visaNumber = "123456789012345678Z"
        visaInformation.nationIssue = "12345Z"

        map.domainModel = visaInformation
        shouldFail(ApplicationException) {
            visaInformation = visaInformationService.update(map)
        }
    }


    void testVisaInformationDelete() {
        def visaInformation = newValidForCreateVisaInformation()
        def map = [domainModel: visaInformation]
        visaInformation = visaInformationService.create(map)
        assertNotNull "VisaInformation ID is null in VisaInformation Service Tests Create", visaInformation.id

        def id = visaInformation.id
        visaInformationService.delete([domainModel: visaInformation])
        assertNull "VisaInformation should have been deleted", visaInformation.get(id)
    }


    void testReadOnly() {
        def visaInformation = newValidForCreateVisaInformation()
        def map = [domainModel: visaInformation]
        visaInformation = visaInformationService.create(map)
        assertNotNull "VisaInformation ID is null in VisaInformation Service Tests Create", visaInformation.id

        visaInformation.pidm = PersonUtility.getPerson("HOR000007").pidm
        try {
            visaInformationService.update([domainModel: visaInformation])
            fail("This should have failed with @@r1:readonlyFieldsCannotBeModified")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "readonlyFieldsCannotBeModified"
        }
    }


    private def newValidForCreateVisaInformation() {
        def visaInformation = new VisaInformation(
                pidm: PersonUtility.getPerson("HOR000008").pidm,
                sequenceNumber: null,
                visaNumber: "123456789012345678",
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


    private def newInvalidForCreateVisaInformation() {
        def visaInformation = new VisaInformation()
        return visaInformation
    }
}
