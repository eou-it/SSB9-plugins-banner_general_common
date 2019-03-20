/*********************************************************************************
 Copyright 2009-2013 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.overall
import org.junit.Before
import org.junit.Test
import org.junit.After

import grails.validation.ValidationException
import groovy.sql.Sql
import net.hedtech.banner.general.system.AddressType
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException

/**
 * Tests for AddressRolePrivileges
 */
class AddressRolePrivilegesIntegrationTests  extends BaseIntegrationTestCase{

    def i_success_role = "successrole"
    def i_success_privilege_indicator = "U"
    def i_success_modified_by = "modifiedbyme"
    def i_success_data_origin = "origin"

    def i_failure_role = "failurerole12345678912345678901"
    def i_failure_privilege_indicator = "TT"
    def i_failure_modified_by = "modifiedbyme1234567812345678901"
    def i_failure_data_origin = "origin7890123456789012345678901"

    @Before
    public void setUp() {
        formContext = ['GUAGMNU'] // Since we are not testing a controller, we need to explicitly set this (removing GEAPART because of GUOBOBS_UI_VERSION = B)
        super.setUp()
    }

    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    void testCreateValidObject() {
        AddressRolePrivileges myPriv = newObject()
        save myPriv
        assertNotNull( myPriv.id )

        def myId1 = myPriv.id

        myPriv = newObject()
        myPriv.role = i_success_role + "2"
        save myPriv
        assertNotNull( myPriv.id )
        def myId2 = myPriv.id

        myPriv = AddressRolePrivileges.get(myId1)
        assertEquals(i_success_role, myPriv.role)

        myPriv = AddressRolePrivileges.get(myId2)
        assertEquals(i_success_role + "2", myPriv.role)

    }


    @Test
    void testCreateInvalidObject() {
        AddressRolePrivileges myPriv = newObject()
        myPriv.addressType = null // not nullable
        shouldFail(ValidationException) {
            myPriv.save( failOnError: true, flush: true )
        }
    }


    @Test
    void testUpdateValidObject() {
        AddressRolePrivileges myPriv = newObject()
        save myPriv
        assertNotNull( myPriv.id )
        assertEquals(0l, myPriv.version)

        myPriv.role = i_success_role + "u"
        assertNotNull( myPriv.id )

        def updatedPriv = AddressRolePrivileges.get(myPriv.id)

        assertEquals(i_success_role + "u", updatedPriv.role)
    }


    @Test
    void testOptimisticLock() {
        AddressRolePrivileges myPriv = newObject()
        myPriv.save(failOnError: true, flush: true)

        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("update GORADRL set GORADRL_VERSION = 999 where GORADRL_SURROGATE_ID = ?", [myPriv.id])
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }

        myPriv.role = i_success_role + "l"
        shouldFail(HibernateOptimisticLockingFailureException) {
            myPriv.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testDelete() {
        AddressRolePrivileges myPriv = newObject()
        save myPriv
        assertNotNull( myPriv.id )

        def myId1 = myPriv.id

        myPriv.delete()
        assertNull AddressRolePrivileges.get( myId1 )

    }


    @Test
    void testNullValidationFailure() {
        AddressRolePrivileges myPriv = new AddressRolePrivileges()
        assertFalse "AddressRolePrivileges should have failed validation", myPriv.validate()
        assertErrorsFor myPriv, 'nullable',
                [
                        'role',
                        'privilegeIndicator',
                        'lastModified',
                        'addressType'
                ]
    }


    @Test
    void testLengthValidation() {
        AddressRolePrivileges myPriv = newObject()
        myPriv.privilegeIndicator = i_failure_privilege_indicator
        assertFalse "AddressRolePrivileges should have failed validation", myPriv.validate()
        assertEquals("maxSize.exceeded", myPriv.errors.getFieldError( 'privilegeIndicator' ).getCode())

        myPriv = newObject()
        myPriv.role = i_failure_role
        assertFalse "AddressRolePrivileges should have failed validation", myPriv.validate()
        assertEquals("maxSize.exceeded", myPriv.errors.getFieldError( 'role' ).getCode())

        myPriv = newObject()
        myPriv.lastModifiedBy = i_failure_modified_by
        assertFalse "AddressRolePrivileges should have failed validation", myPriv.validate()
        assertEquals("maxSize.exceeded", myPriv.errors.getFieldError( 'lastModifiedBy' ).getCode())

        myPriv = newObject()
        myPriv.dataOrigin = i_failure_data_origin
        assertFalse "AddressRolePrivileges should have failed validation", myPriv.validate()
        assertEquals("maxSize.exceeded", myPriv.errors.getFieldError( 'dataOrigin' ).getCode())
    }


    @Test
    void testEquivalency() {
        AddressRolePrivileges myPriv = newObject()
        AddressRolePrivileges myPriv2 = newObject()
        Date thisDate = new Date()
        Date thatDate = thisDate.minus(1)

        assertTrue myPriv == myPriv

        myPriv.lastModified = thisDate
        myPriv2.lastModified = thisDate
        assertTrue myPriv == myPriv2

        assertFalse myPriv == "A String"

        myPriv2.privilegeIndicator = "D"
        assertFalse myPriv == myPriv2

        myPriv2 = newObject()
        myPriv2.lastModified = thisDate
        myPriv2.addressType = AddressType.findByCode("PO")
        assertFalse myPriv == myPriv2

        myPriv2 = newObject()
        myPriv2.lastModified = thisDate
        myPriv2.dataOrigin = "different"
        assertFalse myPriv == myPriv2

        myPriv2 = newObject()
        myPriv2.lastModified = thisDate
        myPriv2.lastModifiedBy = "different"
        assertFalse myPriv == myPriv2

        myPriv2 = newObject()
        myPriv2.lastModified = thisDate
        myPriv2.role = "different"
        assertFalse myPriv == myPriv2

        myPriv2 = newObject()
        myPriv2.lastModified = thisDate
        myPriv2.lastModified = thatDate
        assertFalse myPriv == myPriv2
    }


    @Test
    void testFetchPrivilegedByRole() {
        AddressRolePrivileges myPriv = newObject()
        myPriv.role = "testrole1"
        myPriv.dataOrigin = "testorigin1"
        save myPriv

        myPriv = newObject()
        myPriv.role = "testrole1"
        myPriv.addressType =  AddressType.findByCode("PO")
        myPriv.dataOrigin = "testorigin2"
        save myPriv

        myPriv = newObject()
        myPriv.role = "testrole2"
        myPriv.addressType = AddressType.findByCode("PO")
        myPriv.dataOrigin = "testorigin1"
        save myPriv

        def myPrivs = AddressRolePrivileges.fetchPrivilegedByRole("testrole1")
        assertEquals 2, myPrivs.size()

        myPrivs = AddressRolePrivileges.fetchPrivilegedByRole("testrole2")
        assertEquals 1, myPrivs.size()
      }


    @Test
    void testFetchPrivilegedByRoleMixedPrivileges() {
        AddressRolePrivileges myPriv = newObject()
        myPriv.role = "testrole1"
        myPriv.dataOrigin = "testorigin1"
        save myPriv

        myPriv = newObject()
        myPriv.role = "testrole1"
        myPriv.addressType =  AddressType.findByCode("PO")
        myPriv.dataOrigin = "testorigin2"
        myPriv.privilegeIndicator = "N" // deny privilege to this record
        save myPriv

        def myPrivs = AddressRolePrivileges.fetchPrivilegedByRole("testrole1")
        assertEquals 1, myPrivs.size()
    }

    @Test
    void testFetchPrivilegedByRoleList() {
        AddressRolePrivileges myPriv = newObject()
        myPriv.role = "testrole1"
        myPriv.dataOrigin = "testorigin1"
        save myPriv

        myPriv = newObject()
        myPriv.role = "testrole1"
        myPriv.addressType =  AddressType.findByCode("PO")
        myPriv.dataOrigin = "testorigin2"
        save myPriv

        myPriv = newObject()
        myPriv.role = "testrole2"
        myPriv.addressType = AddressType.findByCode("PO")
        myPriv.dataOrigin = "testorigin1"
        save myPriv

        def roleList = ['testrole1', 'testrole2']
        def myPrivs = AddressRolePrivileges.fetchUpdatePrivsByRoleList(roleList)
        assertEquals 3, myPrivs.size()
    }

    @Test
    void testFetchUpdatePrivByCodeAndRoles() {
        AddressRolePrivileges myPriv = newObject()
        myPriv.role = "testrole1"
        myPriv.dataOrigin = "testorigin1"
        save myPriv

        myPriv = newObject()
        myPriv.role = "testrole1"
        myPriv.addressType =  AddressType.findByCode("PO")
        myPriv.dataOrigin = "testorigin2"
        save myPriv

        myPriv = newObject()
        myPriv.role = "testrole2"
        myPriv.addressType = AddressType.findByCode("PO")
        myPriv.dataOrigin = "testorigin1"
        save myPriv

        def roleList = ['testrole1', 'testrole2']
        def code = 'PO'
        def myPrivs = AddressRolePrivileges.fetchUpdatePrivByCodeAndRoles(roleList, code)
        assertEquals 'Purchase Order Address', myPrivs.addressType.description
    }


    def newObject() {
        def addressType = AddressType.findByCode("MA")
        new AddressRolePrivileges(role: i_success_role,
                privilegeIndicator: i_success_privilege_indicator,
                lastModified: new Date(),
                lastModifiedBy: i_success_modified_by,
                dataOrigin: i_success_data_origin,
                addressType: addressType
            )
    }
}
