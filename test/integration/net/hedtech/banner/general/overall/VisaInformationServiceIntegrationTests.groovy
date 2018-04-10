/*******************************************************************************
 Copyright 2013-2018 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.general.common.GeneralValidationCommonConstants
import net.hedtech.banner.general.overall.ldm.GlobalUniqueIdentifier
import org.junit.Before
import org.junit.Test
import org.junit.After

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.general.system.PortOfEntry
import net.hedtech.banner.general.system.VisaIssuingAuthority
import net.hedtech.banner.general.system.VisaType
import net.hedtech.banner.testing.BaseIntegrationTestCase

class VisaInformationServiceIntegrationTests extends BaseIntegrationTestCase {

    def visaInformationService


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


    @Test
    void testVisaInformationInvalidCreate() {
        def visaInformation = newInvalidForCreateVisaInformation()
        def map = [domainModel: visaInformation]
        shouldFail(ApplicationException) {
            visaInformationService.create(map)
        }
    }


    @Test
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
        visaInformation.numberEntries = null
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


    @Test
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


    @Test
    void testVisaInformationDelete() {
        def visaInformation = newValidForCreateVisaInformation()
        def map = [domainModel: visaInformation]
        visaInformation = visaInformationService.create(map)
        assertNotNull "VisaInformation ID is null in VisaInformation Service Tests Create", visaInformation.id

        def id = visaInformation.id
        visaInformationService.delete([domainModel: visaInformation])
        assertNull "VisaInformation should have been deleted", visaInformation.get(id)
    }


    @Test
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

    @Test
    void testFetchAllWithMaxSeqNumByIssuingNationCodeAndPidmInListNullList() {
        List<VisaInformation> visaInformationList = visaInformationService.fetchAllWithMaxSeqNumByIssuingNationCodeAndPidmInList(null,null)
        assertNotNull visaInformationList
        assertEquals(0, visaInformationList.size())

    }

    @Test
    void testFetchAllWithMaxSeqNumByIssuingNationCodeAndPidmInListEmptyList() {
        List<VisaInformation> visaInformationList = visaInformationService.fetchAllWithMaxSeqNumByIssuingNationCodeAndPidmInList([],null)
        assertNotNull visaInformationList
        assertEquals(0, visaInformationList.size())

    }

    @Test
    void testFetchAllWithMaxSeqNumByIssuingNationCodeAndPidmInListNoVisaInformation() {
        Integer pidm = PersonUtility.getPerson("HOR000008").pidm
        List pidms =[]
        pidms.add(pidm)
        List<VisaInformation> visaInformationList = visaInformationService.fetchAllWithMaxSeqNumByIssuingNationCodeAndPidmInList(pidms,"1")
        assertEquals(0, visaInformationList.size())
    }

    @Test
    void testFetchAllWithMaxSeqNumByIssuingNationCodeAndPidmInListVisaInformationButNotIssuingNation() {
        def visaInformation = newValidForCreateVisaInformationForParameterPassed("1",(new Date()-1), (new Date()+1), "F1")
        def map = [domainModel: visaInformation]
        visaInformation = visaInformationService.create(map)
        assertNotNull visaInformation.id
        Integer pidm = PersonUtility.getPerson("HOR000008").pidm
        List pidms =[]
        pidms.add(pidm)
        List<VisaInformation> visaInformationList = visaInformationService.fetchAllWithMaxSeqNumByIssuingNationCodeAndPidmInList(pidms,"2")
        assertEquals(0, visaInformationList.size())
    }

    //modifying the visa type temporarily from B1 to F1 to see if it passes.
    //this test is constantly failing because there is no gorguid record for B1
    @Test
    void testFetchAllWithMaxSeqNumByIssuingNationCodeAndPidmInList() {
        def visaInformation = newValidForCreateVisaInformationForParameterPassed("1",(new Date()-1), (new Date()+1), "F1")
        println "The visa information is "+visaInformation
        def map = [domainModel: visaInformation]
        visaInformation = visaInformationService.create(map)
        assertNotNull visaInformation.id
        Integer pidm = PersonUtility.getPerson("HOR000008").pidm
        List pidms =[]
        pidms.add(pidm)
        List<VisaInformation> visaInformationList = visaInformationService.fetchAllWithMaxSeqNumByIssuingNationCodeAndPidmInList(pidms,"1")
        println "The visainformationlist is: "+visaInformationList
        assertEquals(1, visaInformationList.size())
        List<GlobalUniqueIdentifier> globalUniqueIdentifier = GlobalUniqueIdentifier.fetchByLdmNameAndDomainKey(GeneralValidationCommonConstants.VISA_TYPES_LDM_NAME, visaInformationList[0].visaType.code)
        assertEquals(1, globalUniqueIdentifier.size())
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

    private def newValidForCreateVisaInformationForParameterPassed(String nationIssue,Date visaIssueDate, Date visaExpireDate, String visaType) {
        def visaInformation = new VisaInformation(
                pidm: PersonUtility.getPerson("HOR000008").pidm,
                sequenceNumber: null,
                visaNumber: "123456789012345678",
                nationIssue: nationIssue,
                visaStartDate: new Date(),
                visaExpireDate: visaExpireDate,
                entryIndicator: true,
                visaRequiredDate: visaIssueDate,
                visaIssueDate: visaIssueDate,
                numberEntries: "M",
                visaType: VisaType.findByCode(visaType),
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
