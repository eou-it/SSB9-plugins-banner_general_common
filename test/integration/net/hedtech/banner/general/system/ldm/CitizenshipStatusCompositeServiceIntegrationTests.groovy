/** *******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.system.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.common.GeneralValidationCommonConstants
import net.hedtech.banner.general.overall.ldm.GlobalUniqueIdentifier
import net.hedtech.banner.general.overall.ldm.LdmService
import net.hedtech.banner.general.system.CitizenType
import net.hedtech.banner.general.system.ldm.v4.CitizenshipStatus
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
import org.junit.Before
import org.junit.Test


class CitizenshipStatusCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    def citizenshipStatusCompositeService

    CitizenType citizenType


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        initializeDataReferences()
    }


    private void initializeDataReferences() {
        citizenType = CitizenType.findByCode('Y')
    }


    @Test
    void testList_CitizenshipStatuses_v4() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Accept", "application/vnd.hedtech.integration.v4+json")
        List<CitizenshipStatus> citizenshipStatuses = citizenshipStatusCompositeService.list([max: '500', offset: '0'])
        assertTrue citizenshipStatuses.size() > 0
        assertTrue citizenshipStatuses.code.contains(citizenType.code)
    }


    @Test
    void testCount_CitizenshipStatuses_v4() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Accept", "application/vnd.hedtech.integration.v4+json")
        assertTrue citizenshipStatusCompositeService.count() > 0
    }


    @Test
    void testGet_CitizenshipStatus_v4() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Accept", "application/vnd.hedtech.integration.v4+json")
        String guid = GlobalUniqueIdentifier.fetchByDomainKeyAndLdmName(citizenType.code, GeneralValidationCommonConstants.CITIZENSHIP_STATUSES_LDM_NAME)?.guid
        assertNotNull guid
        CitizenshipStatus citizenshipStatus = citizenshipStatusCompositeService.get(guid)
        assertNotNull citizenshipStatus
        assertEquals guid, citizenshipStatus.guid
        assertEquals citizenType.code, citizenshipStatus.code
        assertEquals citizenType.description, citizenshipStatus.description
        assertEquals citizenshipStatusCompositeService.getCitizenshipStatusCategory(citizenshipStatus.citizenIndicator), citizenshipStatus.category
    }


    @Test
    void testGet_InvalidCitizenshipStatus_v4() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Accept", "application/vnd.hedtech.integration.v4+json")
        String guid = 'xxxxx'
        try {
            citizenshipStatusCompositeService.get(guid)
            fail('Invalid guid')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'NotFoundException'
        }
    }

}
