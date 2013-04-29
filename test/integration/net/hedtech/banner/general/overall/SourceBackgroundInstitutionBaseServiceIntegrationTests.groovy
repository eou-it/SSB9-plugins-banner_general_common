/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.system.County
import net.hedtech.banner.general.system.Nation
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.general.system.State
import net.hedtech.banner.general.system.Zip
import net.hedtech.banner.testing.BaseIntegrationTestCase

class SourceBackgroundInstitutionBaseServiceIntegrationTests extends BaseIntegrationTestCase {

    def sourceBackgroundInstitutionBaseService


    protected void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    protected void tearDown() {
        super.tearDown()
    }


    void testSourceBackgroundInstitutionBaseValidCreate() {
        SourceBackgroundInstitutionBase sourceBackgroundInstitutionBase = newValidForCreateSourceBackgroundInstitutionBase()
        sourceBackgroundInstitutionBase = sourceBackgroundInstitutionBaseService.create([domainModel: sourceBackgroundInstitutionBase])
        assertNotNull "SourceBackgroundInstitutionBase ID is null in SourceBackgroundInstitutionBase Service Tests Create", sourceBackgroundInstitutionBase.id
        assertNotNull "SourceBackgroundInstitutionBase sourceAndBackgroundInstitution is null in SourceBackgroundInstitutionBase Service Tests", sourceBackgroundInstitutionBase.sourceAndBackgroundInstitution
        assertNotNull "SourceBackgroundInstitutionBase state is null in SourceBackgroundInstitutionBase Service Tests", sourceBackgroundInstitutionBase.state
        assertNotNull "SourceBackgroundInstitutionBase county is null in SourceBackgroundInstitutionBase Service Tests", sourceBackgroundInstitutionBase.county
        assertNotNull "SourceBackgroundInstitutionBase nation is null in SourceBackgroundInstitutionBase Service Tests", sourceBackgroundInstitutionBase.nation
        assertNotNull sourceBackgroundInstitutionBase.version
        assertNotNull sourceBackgroundInstitutionBase.dataOrigin
        assertNotNull sourceBackgroundInstitutionBase.lastModifiedBy
        assertNotNull sourceBackgroundInstitutionBase.lastModified
    }


    void testSourceBackgroundInstitutionBaseInvalidCreate() {
        def sourceBackgroundInstitutionBase = newInvalidForCreateSourceBackgroundInstitutionBase()
        shouldFail(ApplicationException) {
            sourceBackgroundInstitutionBaseService.create([domainModel: sourceBackgroundInstitutionBase])
        }
    }


    private def doUpdate(sourceBackgroundInstitutionBase) {
        //Update the entity
        sourceBackgroundInstitutionBase.streetLine1 = "1234567890UPDATE1"
        sourceBackgroundInstitutionBase.streetLine2 = "1234567890UPDATE2"
        sourceBackgroundInstitutionBase.streetLine3 = "1234567890UPDATE3"
        sourceBackgroundInstitutionBase.city = "CITYNEW"
        sourceBackgroundInstitutionBase.zip = "ZIPNEW"
        sourceBackgroundInstitutionBase.houseNumber = "NUMNEW"
        sourceBackgroundInstitutionBase.streetLine4 = "1234567890UPDATE4"
        sourceBackgroundInstitutionBase.state = State.findWhere(code: "NJ")
        sourceBackgroundInstitutionBase.county = County.findWhere(code: "002")
        sourceBackgroundInstitutionBase.nation = Nation.findWhere(code: "2")
    }


    void testSourceBackgroundInstitutionBaseValidUpdate() {
        SourceBackgroundInstitutionBase sourceBackgroundInstitutionBase = newValidForCreateSourceBackgroundInstitutionBase()
        sourceBackgroundInstitutionBase = sourceBackgroundInstitutionBaseService.create([domainModel: sourceBackgroundInstitutionBase])
        assertNotNull "SourceBackgroundInstitutionBase ID is null in SourceBackgroundInstitutionBase Service Tests Create", sourceBackgroundInstitutionBase.id
        assertNotNull "SourceBackgroundInstitutionBase sourceAndBackgroundInstitution is null in SourceBackgroundInstitutionBase Service Tests", sourceBackgroundInstitutionBase.sourceAndBackgroundInstitution
        assertNotNull "SourceBackgroundInstitutionBase state is null in SourceBackgroundInstitutionBase Service Tests", sourceBackgroundInstitutionBase.state
        assertNotNull "SourceBackgroundInstitutionBase county is null in SourceBackgroundInstitutionBase Service Tests", sourceBackgroundInstitutionBase.county
        assertNotNull "SourceBackgroundInstitutionBase nation is null in SourceBackgroundInstitutionBase Service Tests", sourceBackgroundInstitutionBase.nation
        assertNotNull sourceBackgroundInstitutionBase.version
        assertNotNull sourceBackgroundInstitutionBase.dataOrigin
        assertNotNull sourceBackgroundInstitutionBase.lastModifiedBy
        assertNotNull sourceBackgroundInstitutionBase.lastModified

        //Update the entity with new values
        doUpdate(sourceBackgroundInstitutionBase)
        sourceBackgroundInstitutionBase = sourceBackgroundInstitutionBaseService.update([domainModel: sourceBackgroundInstitutionBase])
        assertEquals "1234567890UPDATE1", sourceBackgroundInstitutionBase.streetLine1
        assertEquals "1234567890UPDATE2", sourceBackgroundInstitutionBase.streetLine2
        assertEquals "1234567890UPDATE3", sourceBackgroundInstitutionBase.streetLine3
        assertEquals "CITYNEW", sourceBackgroundInstitutionBase.city
        assertEquals "ZIPNEW", sourceBackgroundInstitutionBase.zip
        assertEquals "NUMNEW", sourceBackgroundInstitutionBase.houseNumber
        assertEquals "1234567890UPDATE4", sourceBackgroundInstitutionBase.streetLine4
        assertEquals "NJ", sourceBackgroundInstitutionBase.state.code
        assertEquals "002", sourceBackgroundInstitutionBase.county.code
        assertEquals "2", sourceBackgroundInstitutionBase.nation.code
    }


    void testSourceBackgroundInstitutionBaseInvalidUpdate() {
        SourceBackgroundInstitutionBase sourceBackgroundInstitutionBase = newValidForCreateSourceBackgroundInstitutionBase()
        sourceBackgroundInstitutionBase = sourceBackgroundInstitutionBaseService.create([domainModel: sourceBackgroundInstitutionBase])

        //Update the entity with new invalid values
        sourceBackgroundInstitutionBase.city = null
        shouldFail(ApplicationException) {
            sourceBackgroundInstitutionBase = sourceBackgroundInstitutionBaseService.update([domainModel: sourceBackgroundInstitutionBase])
        }
    }


    void testSourceBackgroundInstitutionBaseDelete() {
        SourceBackgroundInstitutionBase sourceBackgroundInstitutionBase = newValidForCreateSourceBackgroundInstitutionBase()
        sourceBackgroundInstitutionBase = sourceBackgroundInstitutionBaseService.create([domainModel: sourceBackgroundInstitutionBase])
        assertNotNull "SourceBackgroundInstitutionBase ID is null in SourceBackgroundInstitutionBase Service Tests Create", sourceBackgroundInstitutionBase.id
        def id = sourceBackgroundInstitutionBase.id
        sourceBackgroundInstitutionBaseService.delete([domainModel: sourceBackgroundInstitutionBase])
        assertNull "SourceBackgroundInstitutionBase should have been deleted", sourceBackgroundInstitutionBase.get(id)
    }


    private def newValidForCreateSourceBackgroundInstitutionBase() {
        def zip = newValidForCreateZip()

        def sourceBackgroundInstitutionBase = new SourceBackgroundInstitutionBase(
                streetLine1: "123456789012345678901234567890123456789012345678901234567890123456789012345",
                streetLine2: "123456789012345678901234567890123456789012345678901234567890123456789012345",
                streetLine3: "123456789012345678901234567890123456789012345678901234567890123456789012345",
                city: "12345678901234567890123456789012345678901234567890",
                zip: zip.code,
                houseNumber: "1234567890",
                streetLine4: "123456789012345678901234567890123456789012345678901234567890123456789012345",
                sourceAndBackgroundInstitution: SourceAndBackgroundInstitution.findWhere(code: "999999"),
                state: State.findWhere(code: "PA"),
                county: County.findWhere(code: "001"),
                nation: Nation.findWhere(code: "1"),
        )
        return sourceBackgroundInstitutionBase
    }


    private def newInvalidForCreateSourceBackgroundInstitutionBase() {
        def sourceBackgroundInstitutionBase = new SourceBackgroundInstitutionBase(
                city: "12345678901234567890123456789012345678901234567890FAIL",
                sourceAndBackgroundInstitution: SourceAndBackgroundInstitution.findWhere(code: "999999"),
        )
        return sourceBackgroundInstitutionBase
    }


    private def newValidForCreateZip() {
        def zip = new Zip(
                code: "123456789012345678901234567890",
                city: "12345678901234567890123456789012345678901234567890",
                state: State.findWhere(code: "PA"),
                county: County.findWhere(code: "001"),
                nation: Nation.findWhere(code: "1"),
        )
        zip.save(flush: true, failOnError: true)
        return zip
    }
}
