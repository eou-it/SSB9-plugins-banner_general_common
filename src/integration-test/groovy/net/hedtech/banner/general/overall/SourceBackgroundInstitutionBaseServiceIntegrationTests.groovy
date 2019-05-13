/*********************************************************************************
  Copyright 2010-2013 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.junit.Before
import org.junit.Test
import org.junit.After
import static groovy.test.GroovyAssert.*
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.system.*
import net.hedtech.banner.testing.BaseIntegrationTestCase

@Integration
@Rollback
class SourceBackgroundInstitutionBaseServiceIntegrationTests extends BaseIntegrationTestCase {

    def sourceBackgroundInstitutionBaseService


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


    @Test
    void testSourceBackgroundInstitutionBaseInvalidCreate() {
        def sourceBackgroundInstitutionBase = newInvalidForCreateSourceBackgroundInstitutionBase()
        shouldFail(ApplicationException) {
            sourceBackgroundInstitutionBaseService.create([domainModel: sourceBackgroundInstitutionBase])
        }
    }


    @Test
    void testSourceBackgroundInstitutionBaseMissingAddressInfoCreate() {
        def zip = newValidForCreateZip()
        def sourceBackgroundInstitutionBase

        // Test no entry of zip, state, or nation
        sourceBackgroundInstitutionBase = new SourceBackgroundInstitutionBase(
                city: "TTTTTTTTTT",
        )

        try {
            sourceBackgroundInstitutionBaseService.create([domainModel: sourceBackgroundInstitutionBase])
            fail "Should have failed with @@r1:missingStateAndZipAndNation@@"
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "missingStateAndZipAndNation"
        }

        // Test entry of zip with no state and nation
        sourceBackgroundInstitutionBase = new SourceBackgroundInstitutionBase(
                zip: zip.code,
                city: "TTTTTTTTTT",
        )
        try {
            sourceBackgroundInstitutionBaseService.create([domainModel: sourceBackgroundInstitutionBase])
            fail "Should have failed with @@r1:missingStateAndNation@@"
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "missingStateAndNation"
        }


        // Test entry of state with no zip
        sourceBackgroundInstitutionBase = new SourceBackgroundInstitutionBase(
                state: State.findByCode("PA"),
                city: "TTTTTTTTTT",
        )
        try {
            sourceBackgroundInstitutionBaseService.create([domainModel: sourceBackgroundInstitutionBase])
            fail "Should have failed with @@r1:missingZip@@"
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "missingZip"
        }
    }


    @Test
    void testSourceBackgroundInstitutionBaseMissingAddressInfoUpdate() {
        SourceBackgroundInstitutionBase sourceBackgroundInstitutionBase = newValidForCreateSourceBackgroundInstitutionBase()
        sourceBackgroundInstitutionBase = sourceBackgroundInstitutionBaseService.create([domainModel: sourceBackgroundInstitutionBase])

        sourceBackgroundInstitutionBase.zip = null
        sourceBackgroundInstitutionBase.state = null
        sourceBackgroundInstitutionBase.nation = null

        try {
            sourceBackgroundInstitutionBaseService.create([domainModel: sourceBackgroundInstitutionBase])
            fail "Should have failed with @@r1:missingStateAndZipAndNation@@"
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "missingStateAndZipAndNation"
        }
    }


    private def doUpdate(sourceBackgroundInstitutionBase) {
        //Update the entity
        sourceBackgroundInstitutionBase.streetLine1 = "1234567890UPDATE1"
        sourceBackgroundInstitutionBase.streetLine2 = "1234567890UPDATE2"
        sourceBackgroundInstitutionBase.streetLine3 = "1234567890UPDATE3"
        sourceBackgroundInstitutionBase.city = "CITYNEW"
        sourceBackgroundInstitutionBase.zip = "19355"
        sourceBackgroundInstitutionBase.houseNumber = "NUMNEW"
        sourceBackgroundInstitutionBase.streetLine4 = "1234567890UPDATE4"
        sourceBackgroundInstitutionBase.state = State.findByCode("NJ")
        sourceBackgroundInstitutionBase.county = County.findByCode("002")
        sourceBackgroundInstitutionBase.nation = Nation.findByCode("2")
    }


    @Test
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
        assertEquals "19355", sourceBackgroundInstitutionBase.zip
        assertEquals "NUMNEW", sourceBackgroundInstitutionBase.houseNumber
        assertEquals "1234567890UPDATE4", sourceBackgroundInstitutionBase.streetLine4
        assertEquals "NJ", sourceBackgroundInstitutionBase.state.code
        assertEquals "002", sourceBackgroundInstitutionBase.county.code
        assertEquals "2", sourceBackgroundInstitutionBase.nation.code
    }


    @Test
    void testSourceBackgroundInstitutionBaseInvalidUpdate() {
        SourceBackgroundInstitutionBase sourceBackgroundInstitutionBase = newValidForCreateSourceBackgroundInstitutionBase()
        sourceBackgroundInstitutionBase = sourceBackgroundInstitutionBaseService.create([domainModel: sourceBackgroundInstitutionBase])

        //Update the entity with new invalid values
        sourceBackgroundInstitutionBase.city = null
        shouldFail(ApplicationException) {
            sourceBackgroundInstitutionBase = sourceBackgroundInstitutionBaseService.update([domainModel: sourceBackgroundInstitutionBase])
        }
    }


    @Test
    void testSourceBackgroundInstitutionBaseDelete() {
        SourceBackgroundInstitutionBase sourceBackgroundInstitutionBase = newValidForCreateSourceBackgroundInstitutionBase()
        sourceBackgroundInstitutionBase = sourceBackgroundInstitutionBaseService.create([domainModel: sourceBackgroundInstitutionBase])
        assertNotNull "SourceBackgroundInstitutionBase ID is null in SourceBackgroundInstitutionBase Service Tests Create", sourceBackgroundInstitutionBase.id
        def id = sourceBackgroundInstitutionBase.id
        sourceBackgroundInstitutionBaseService.delete([domainModel: sourceBackgroundInstitutionBase])
        assertNull "SourceBackgroundInstitutionBase should have been deleted", sourceBackgroundInstitutionBase.get(id)
    }


    @Test
    void testReadOnly() {
        def sourceBackgroundInstitutionBase = newValidForCreateSourceBackgroundInstitutionBase()
        def map = [domainModel: sourceBackgroundInstitutionBase]
        sourceBackgroundInstitutionBase = sourceBackgroundInstitutionBaseService.create(map)
        assertNotNull "SourceBackgroundInstitutionBase ID is null in SourceBackgroundInstitutionBase Service Tests Create", sourceBackgroundInstitutionBase.id

        sourceBackgroundInstitutionBase.sourceAndBackgroundInstitution = SourceAndBackgroundInstitution.findWhere(code: "5815")
        try {
            sourceBackgroundInstitutionBaseService.update([domainModel: sourceBackgroundInstitutionBase])
            fail("This should have failed with @@r1:readonlyFieldsCannotBeModified")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "readonlyFieldsCannotBeModified"
        }
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
                state: State.findByCode("PA"),
                county: County.findByCode("001"),
                nation: Nation.findByCode("1"),
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
                state: State.findByCode("PA"),
                county: County.findByCode("001"),
                nation: Nation.findByCode("1"),
        )
        zip.save(flush: true, failOnError: true)
        return zip
    }
}
