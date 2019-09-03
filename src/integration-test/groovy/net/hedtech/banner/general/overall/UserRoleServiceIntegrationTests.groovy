/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

@Integration
@Rollback
class UserRoleServiceIntegrationTests extends BaseIntegrationTestCase {

    def userRoleService

    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
        super.logout()
    }


    @Test
    void testHasUserRoleWithEmployeeUser() {
        loginSSB 'HOP510001', '111111'

        assertTrue userRoleService.hasUserRole('EMPLOYEE')
    }

    @Test
    void testHasUserRoleWithFacultyUser() {
        loginSSB 'HOF00714', '111111'

        assertFalse userRoleService.hasUserRole('STUDENT')
        assertFalse userRoleService.hasUserRole('EMPLOYEE')
        assertTrue userRoleService.hasUserRole('FACULTY')

    }

    @Test
    void testHasUserRoleWithWebTailorRoleOnlyUser() {
        loginSSB 'BCMADMIN', '111111'

        assertFalse userRoleService.hasUserRole('STUDENT')
        assertFalse userRoleService.hasUserRole('EMPLOYEE')
        assertFalse userRoleService.hasUserRole('FACULTY')
        assertTrue  userRoleService.hasUserRole('COMMUNICATIONADMIN')
    }

    @Test
    void testGetRoles() {
        loginSSB 'HOP510001', '111111'

        def roles = userRoleService.getRoles()

        assertNotNull roles.isStudent
        assertNotNull roles.isEmployee
        assertNotNull roles.isAipAdmin
        assertFalse  roles.isStudent
        assertTrue roles.isEmployee
        assertFalse roles.isAipAdmin
    }
}
