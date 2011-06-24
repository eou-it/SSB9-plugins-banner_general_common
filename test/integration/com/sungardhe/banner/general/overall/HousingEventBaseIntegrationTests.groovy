/** *****************************************************************************
 © 2011 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.general.overall

import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException as OptimisticLock

import com.sungardhe.banner.testing.BaseIntegrationTestCase
import groovy.sql.Sql

class HousingEventBaseIntegrationTests extends BaseIntegrationTestCase {

    def housingEventBaseService


    protected void setUp() {
        formContext = ['SLQEVNT', 'SLAEVNT'] // Since we are not testing a controller, we need to explicitly set this
        super.setUp()
    }


    protected void tearDown() {
        super.tearDown()
    }


    void testCreateHousingEventBase() {
        def housingEventBase = newHousingEventBase()
        save housingEventBase
        //Test if the generated entity now has an id assigned
        assertNotNull housingEventBase.id
    }


    void testUpdateHousingEventBase() {
        def housingEventBase = newHousingEventBase()
        save housingEventBase

        assertNotNull housingEventBase.id
        assertEquals 0L, housingEventBase.version
        assertEquals "TTTTT", housingEventBase.courseReferenceNumber
        assertEquals "TTTT", housingEventBase.event
        assertEquals "TTTTT", housingEventBase.description
        assertEquals 1, housingEventBase.agencyPidm
        assertEquals "TTTTT", housingEventBase.contactName
        assertEquals "TTT", housingEventBase.campus
        assertEquals "TTT", housingEventBase.site
        assertEquals "TT", housingEventBase.college
        assertEquals "TTTT", housingEventBase.department
        assertEquals "TTTTT", housingEventBase.phoneArea
        assertEquals "TTTTT", housingEventBase.phone
        assertEquals "TTTTT", housingEventBase.phoneExt
        assertEquals 1, housingEventBase.contactPidm
        assertEquals "TT", housingEventBase.addressType
        assertEquals "TT", housingEventBase.system
        assertTrue housingEventBase.committeeIndicator
        assertEquals "TTT", housingEventBase.district
        assertEquals "TTTT", housingEventBase.countryPhone

        //Update the entity
        def testDate = new Date()
        housingEventBase.courseReferenceNumber = "UUUUU"
        housingEventBase.event = "UUUU"
        housingEventBase.description = "UUUUU"
        housingEventBase.agencyPidm = 0
        housingEventBase.contactName = "UUUUU"
        housingEventBase.campus = "UUU"
        housingEventBase.site = "UUU"
        housingEventBase.college = "UU"
        housingEventBase.department = "UUUU"
        housingEventBase.phoneArea = "UUUUU"
        housingEventBase.phone = "UUUUU"
        housingEventBase.phoneExt = "UUUUU"
        housingEventBase.contactPidm = 0
        housingEventBase.addressType = "UU"
        housingEventBase.system = "UU"
        housingEventBase.committeeIndicator = false
        housingEventBase.district = "UUU"
        housingEventBase.countryPhone = "UUUU"
        housingEventBase.lastModified = testDate
        housingEventBase.lastModifiedBy = "test"
        housingEventBase.dataOrigin = "Banner"
        save housingEventBase

        housingEventBase = HousingEventBase.get(housingEventBase.id)
        assertEquals 1L, housingEventBase?.version
        assertEquals "UUUUU", housingEventBase.courseReferenceNumber
        assertEquals "UUUU", housingEventBase.event
        assertEquals "UUUUU", housingEventBase.description
        assertEquals 0, housingEventBase.agencyPidm
        assertEquals "UUUUU", housingEventBase.contactName
        assertEquals "UUU", housingEventBase.campus
        assertEquals "UUU", housingEventBase.site
        assertEquals "UU", housingEventBase.college
        assertEquals "UUUU", housingEventBase.department
        assertEquals "UUUUU", housingEventBase.phoneArea
        assertEquals "UUUUU", housingEventBase.phone
        assertEquals "UUUUU", housingEventBase.phoneExt
        assertEquals 0, housingEventBase.contactPidm
        assertEquals "UU", housingEventBase.addressType
        assertEquals "UU", housingEventBase.system
        assertEquals false, housingEventBase.committeeIndicator
        assertEquals "UUU", housingEventBase.district
        assertEquals "UUUU", housingEventBase.countryPhone
    }


    void testOptimisticLock() {
        def housingEventBase = newHousingEventBase()
        save housingEventBase

        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("update SLBEVNT set SLBEVNT_VERSION = 999 where SLBEVNT_SURROGATE_ID = ?", [housingEventBase.id])
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
        //Try to update the entity
        housingEventBase.courseReferenceNumber = "UUUUU"
        housingEventBase.event = "UUUU"
        housingEventBase.description = "UUUUU"
        housingEventBase.agencyPidm = 0
        housingEventBase.contactName = "UUUUU"
        housingEventBase.campus = "UUU"
        housingEventBase.site = "UUU"
        housingEventBase.college = "UU"
        housingEventBase.department = "UUUU"
        housingEventBase.phoneArea = "UUUUU"
        housingEventBase.phone = "UUUUU"
        housingEventBase.phoneExt = "UUUUU"
        housingEventBase.contactPidm = 0
        housingEventBase.addressType = "UU"
        housingEventBase.system = "UU"
        housingEventBase.committeeIndicator = false
        housingEventBase.district = "UUU"
        housingEventBase.countryPhone = "UUUU"
        housingEventBase.lastModified = new Date()
        housingEventBase.lastModifiedBy = "test"
        housingEventBase.dataOrigin = "Banner"
        shouldFail(OptimisticLock) {
            housingEventBase.save(flush: true)
        }
    }


    void testDeleteHousingEventBase() {
        def housingEventBase = newHousingEventBase()
        save housingEventBase
        def id = housingEventBase.id
        assertNotNull id
        housingEventBase.delete()
        assertNull HousingEventBase.get(id)
    }


    void testValidation() {
        def housingEventBase = newHousingEventBase()
        assertTrue "HousingEventBase could not be validated as expected due to ${housingEventBase.errors}", housingEventBase.validate()
    }


    void testNullValidationFailure() {
        def housingEventBase = new HousingEventBase()
        assertFalse "HousingEventBase should have failed validation", housingEventBase.validate()
        assertErrorsFor housingEventBase, 'nullable', ['courseReferenceNumber', 'event', 'committeeIndicator']
        assertNoErrorsFor housingEventBase, ['description', 'agencyPidm', 'contactName', 'campus', 'site', 'college', 'department', 'phoneArea', 'phone', 'phoneExt', 'contactPidm', 'addressType', 'system', 'district', 'countryPhone']
    }


    void testMaxSizeValidationFailures() {
        def housingEventBase = new HousingEventBase(
                description: 'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
                contactName: 'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
                campus: 'XXXXX',
                site: 'XXXXX',
                college: 'XXXX',
                department: 'XXXXXX',
                phoneArea: 'XXXXXXXX',
                phone: 'XXXXXXXXX',
                phoneExt: 'XXXXXXXXXXXX',
                addressType: 'XXXX',
                system: 'XXXX',
                district: 'XXXXX',
                countryPhone: 'XXXXXX')
        assertFalse "HousingEventBase should have failed validation", housingEventBase.validate()
        assertErrorsFor housingEventBase, 'maxSize', ['description', 'contactName', 'campus', 'site', 'college', 'department', 'phoneArea', 'phone', 'phoneExt', 'addressType', 'system', 'district', 'countryPhone']
    }


    void testValidationMessages() {
        def housingEventBase = newHousingEventBase()

        housingEventBase.courseReferenceNumber = null
        assertFalse housingEventBase.validate()
        assertLocalizedError housingEventBase, 'nullable', /.*Field.*courseReferenceNumber.*of class.*HousingEventBase.*cannot be null.*/, 'courseReferenceNumber'

        housingEventBase.event = null
        assertFalse housingEventBase.validate()
        assertLocalizedError housingEventBase, 'nullable', /.*Field.*event.*of class.*HousingEventBase.*cannot be null.*/, 'event'

        housingEventBase.committeeIndicator = null
        assertFalse housingEventBase.validate()
        assertLocalizedError housingEventBase, 'nullable', /.*Field.*committeeIndicator.*of class.*HousingEventBase.*cannot be null.*/, 'committeeIndicator'

    }


    private def newHousingEventBase() {
        new HousingEventBase(courseReferenceNumber: "TTTTT",
                event: "TTTT",
                description: "TTTTT",
                agencyPidm: 1,
                contactName: "TTTTT",
                campus: "TTT",
                site: "TTT",
                college: "TT",
                department: "TTTT",
                phoneArea: "TTTTT",
                phone: "TTTTT",
                phoneExt: "TTTTT",
                contactPidm: 1,
                addressType: "TT",
                system: "TT",
                committeeIndicator: true,
                district: "TTT",
                countryPhone: "TTTT",
                lastModified: new Date(),
                lastModifiedBy: "test",
                dataOrigin: "Banner")
    }

    /**
     * Please put all the custom tests in this protected section to protect the
     * from being overwritten on re-generation
     */
    /*PROTECTED REGION ID(housingeventbase_custom_integration_test_methods) ENABLED START*/
    /*PROTECTED REGION END*/
}
