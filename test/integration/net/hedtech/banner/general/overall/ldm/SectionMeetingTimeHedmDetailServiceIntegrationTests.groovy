/*******************************************************************************
 Copyright 2009-2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.overall.SectionMeetingTime
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

import java.text.SimpleDateFormat

class SectionMeetingTimeHedmDetailServiceIntegrationTests extends BaseIntegrationTestCase {
    def sectionMeetingTimeHedmDetailService
    def i_success_term_code = '201410'
    def i_success_courseReferenceNumber = '20001'
    def i_success_category = '01'
    def i_success_instructionalMethodCode = 'L'
    def i_success_instructionalMethodDesc = 'Lecture'
    def i_success_startDate = '2010-01-10'
    def i_success_endDate = '2010-05-20'
    def i_success_beginTime = '0900'
    def i_success_endTime = '1000'
    def i_success_monday = 'M'
    def i_success_wednesday = 'W'
    def i_success_friday = 'F'
    def ldm_name = 'instructional-events'

    @Before
    public void setUp() {
        formContext = ['GUAGMNU','GEIFUNC', 'GEAFUNC', 'SLAEVNT', 'SSAMATX', 'SFQSECT', 'SSASECT']
        // Since we are not testing a controller, we need to explicitly set this
        super.setUp()
    }

    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    void testCreateFailure() {
        SectionMeetingTimeHedmDetail sectionMeetingTimeHedmDetail = new SectionMeetingTimeHedmDetail()
        sectionMeetingTimeHedmDetail.guid = "dummy-guid"
        shouldFail(ApplicationException) {
            sectionMeetingTimeHedmDetailService.create(sectionMeetingTimeHedmDetail)
        }
    }

    @Test
    void testRead() {
        SectionMeetingTime sectionMeetingTime = SectionMeetingTime.fetchByTermCRNAndCategory(i_success_term_code, i_success_courseReferenceNumber, i_success_category)[0]
        assertNotNull sectionMeetingTime
        GlobalUniqueIdentifier globalUniqueIdentifier = GlobalUniqueIdentifier.fetchByLdmNameAndDomainId(ldm_name, sectionMeetingTime.id)
        assertNotNull globalUniqueIdentifier
        SectionMeetingTimeHedmDetail sectionMeetingTimeHedmDetail = sectionMeetingTimeHedmDetailService.read(globalUniqueIdentifier.guid)
        assertNotNull sectionMeetingTimeHedmDetail
        assertEquals(i_success_instructionalMethodCode, sectionMeetingTimeHedmDetail.instructionalMethodCode)
        assertEquals(i_success_instructionalMethodDesc, sectionMeetingTimeHedmDetail.instructionalMethodDescription)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd")
        assertEquals(i_success_startDate, sdf.format(sectionMeetingTimeHedmDetail.startDate))
        assertEquals(i_success_endDate, sdf.format(sectionMeetingTimeHedmDetail.endDate))
        assertEquals(i_success_beginTime, sectionMeetingTimeHedmDetail.beginTime)
        assertEquals(i_success_endTime, sectionMeetingTimeHedmDetail.endTime)
        assertNull sectionMeetingTimeHedmDetail.sunday
        assertEquals(i_success_monday, sectionMeetingTimeHedmDetail.monday)
        assertNull sectionMeetingTimeHedmDetail.tuesday
        assertEquals(i_success_wednesday, sectionMeetingTimeHedmDetail.wednesday)
        assertNull sectionMeetingTimeHedmDetail.thursday
        assertEquals(i_success_friday, sectionMeetingTimeHedmDetail.friday)
        assertNull sectionMeetingTimeHedmDetail.saturday
        assertNull sectionMeetingTimeHedmDetail.roomGuid
        assertNull sectionMeetingTimeHedmDetail.roomDescription
        assertNull sectionMeetingTimeHedmDetail.roomNumber
        assertNull sectionMeetingTimeHedmDetail.buildingGuid
        assertNull sectionMeetingTimeHedmDetail.buildingDescription
        assertNull sectionMeetingTimeHedmDetail.houseNumber

    }

    @Test
    void testUpdateFailure() {
        SectionMeetingTime sectionMeetingTime = SectionMeetingTime.fetchByTermCRNAndCategory(i_success_term_code, i_success_courseReferenceNumber, i_success_category)[0]
        assertNotNull sectionMeetingTime
        GlobalUniqueIdentifier globalUniqueIdentifier = GlobalUniqueIdentifier.fetchByLdmNameAndDomainId(ldm_name, sectionMeetingTime.id)
        assertNotNull globalUniqueIdentifier
        SectionMeetingTimeHedmDetail sectionMeetingTimeHedmDetail = SectionMeetingTimeHedmDetail.findByGuid(globalUniqueIdentifier.guid)
        sectionMeetingTimeHedmDetail.houseNumber = 10
        shouldFail(ApplicationException) {
            sectionMeetingTimeHedmDetailService.update(sectionMeetingTimeHedmDetail)
        }
    }

    @Test
    void testDeleteFailure() {
        SectionMeetingTime sectionMeetingTime = SectionMeetingTime.fetchByTermCRNAndCategory(i_success_term_code, i_success_courseReferenceNumber, i_success_category)[0]
        assertNotNull sectionMeetingTime
        GlobalUniqueIdentifier globalUniqueIdentifier = GlobalUniqueIdentifier.fetchByLdmNameAndDomainId(ldm_name, sectionMeetingTime.id)
        assertNotNull globalUniqueIdentifier
        SectionMeetingTimeHedmDetail sectionMeetingTimeHedmDetail = SectionMeetingTimeHedmDetail.findByGuid(globalUniqueIdentifier.guid)
        shouldFail(ApplicationException) {
            sectionMeetingTimeHedmDetailService.delete(sectionMeetingTimeHedmDetail)
        }
    }

    @Test
    void testFetchAllByGuidInListAllInvalidGuids() {
        List guids = ['invalid-guid-1', 'invalid-guid-2', 'invalid-guid-3']
        List<SectionMeetingTimeHedmDetail> sectionMeetingTimeHedmDetails = sectionMeetingTimeHedmDetailService.fetchAllByGuidInList(guids)
        assertEquals(0, sectionMeetingTimeHedmDetails.size())
    }

    @Test
    void testFetchAllByGuidInListSomeInvalidAndSomeValidGuids() {
        SectionMeetingTime sectionMeetingTime = SectionMeetingTime.fetchByTermCRNAndCategory(i_success_term_code, i_success_courseReferenceNumber, i_success_category)[0]
        assertNotNull sectionMeetingTime
        GlobalUniqueIdentifier globalUniqueIdentifier = GlobalUniqueIdentifier.fetchByLdmNameAndDomainId(ldm_name, sectionMeetingTime.id)
        assertNotNull globalUniqueIdentifier
        List guids = ['invalid-guid-1', globalUniqueIdentifier.guid, 'invalid-guid-3']
        List<SectionMeetingTimeHedmDetail> sectionMeetingTimeHedmDetails = sectionMeetingTimeHedmDetailService.fetchAllByGuidInList(guids)
        assertEquals(1, sectionMeetingTimeHedmDetails.size())
        assertEquals(globalUniqueIdentifier.guid, sectionMeetingTimeHedmDetails[0].guid)
    }

    @Test
    void testFetchAllByGuidInListAllValidGuids() {
        List<SectionMeetingTime> sectionMeetingTimes = SectionMeetingTime.findAllByTerm('201110')
        assertNotNull sectionMeetingTimes
        assertTrue sectionMeetingTimes.size() > 3
        SectionMeetingTime sectionMeetingTime1 = sectionMeetingTimes[0]
        SectionMeetingTime sectionMeetingTime2 = sectionMeetingTimes[1]
        SectionMeetingTime sectionMeetingTime3 = sectionMeetingTimes[2]

        GlobalUniqueIdentifier globalUniqueIdentifier1 = GlobalUniqueIdentifier.fetchByLdmNameAndDomainId(ldm_name, sectionMeetingTime1.id)
        assertNotNull globalUniqueIdentifier1
        GlobalUniqueIdentifier globalUniqueIdentifier2 = GlobalUniqueIdentifier.fetchByLdmNameAndDomainId(ldm_name, sectionMeetingTime2.id)
        assertNotNull globalUniqueIdentifier2
        GlobalUniqueIdentifier globalUniqueIdentifier3 = GlobalUniqueIdentifier.fetchByLdmNameAndDomainId(ldm_name, sectionMeetingTime3.id)
        assertNotNull globalUniqueIdentifier3

        List guids = [globalUniqueIdentifier1.guid, globalUniqueIdentifier2.guid, globalUniqueIdentifier3.guid]
        List<SectionMeetingTimeHedmDetail> sectionMeetingTimeHedmDetails = sectionMeetingTimeHedmDetailService.fetchAllByGuidInList(guids)
        assertEquals(3, sectionMeetingTimeHedmDetails.size())
        assertTrue sectionMeetingTimeHedmDetails.guid.contains(globalUniqueIdentifier1.guid)
        assertTrue sectionMeetingTimeHedmDetails.guid.contains(globalUniqueIdentifier2.guid)
        assertTrue sectionMeetingTimeHedmDetails.guid.contains(globalUniqueIdentifier3.guid)
    }
}
