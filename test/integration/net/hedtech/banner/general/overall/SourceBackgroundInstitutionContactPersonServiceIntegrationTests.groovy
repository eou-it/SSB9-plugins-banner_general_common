/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.system.PersonType
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.testing.BaseIntegrationTestCase

class SourceBackgroundInstitutionContactPersonServiceIntegrationTests extends BaseIntegrationTestCase {

    def sourceBackgroundInstitutionContactPersonService


    protected void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    protected void tearDown() {
        super.tearDown()
    }


    void testSourceBackgroundInstitutionContactPersonValidCreate() {
        SourceBackgroundInstitutionContactPerson sourceBackgroundInstitutionContactPerson = newValidForCreateSourceBackgroundInstitutionContactPerson()
        sourceBackgroundInstitutionContactPerson = sourceBackgroundInstitutionContactPersonService.create([domainModel: sourceBackgroundInstitutionContactPerson])
        assertNotNull "SourceBackgroundInstitutionContactPerson ID is null in SourceBackgroundInstitutionContactPerson Service Tests Create", sourceBackgroundInstitutionContactPerson.id
        assertNotNull "SourceBackgroundInstitutionContactPerson sourceAndBackgroundInstitution is null in SourceBackgroundInstitutionContactPerson Service Tests", sourceBackgroundInstitutionContactPerson.sourceAndBackgroundInstitution
        assertNotNull "SourceBackgroundInstitutionContactPerson personType is null in SourceBackgroundInstitutionContactPerson Service Tests", sourceBackgroundInstitutionContactPerson.personType
        assertNotNull sourceBackgroundInstitutionContactPerson.version
        assertNotNull sourceBackgroundInstitutionContactPerson.dataOrigin
        assertNotNull sourceBackgroundInstitutionContactPerson.lastModifiedBy
        assertNotNull sourceBackgroundInstitutionContactPerson.lastModified
    }


    void testSourceBackgroundInstitutionContactPersonInvalidCreate() {
        SourceBackgroundInstitutionContactPerson sourceBackgroundInstitutionContactPerson = newInvalidForCreateSourceBackgroundInstitutionContactPerson()
        shouldFail(ApplicationException) {
            sourceBackgroundInstitutionContactPersonService.create([domainModel: sourceBackgroundInstitutionContactPerson])
        }
    }


    void testSourceBackgroundInstitutionContactPersonValidUpdate() {
        SourceBackgroundInstitutionContactPerson sourceBackgroundInstitutionContactPerson = newValidForCreateSourceBackgroundInstitutionContactPerson()
        sourceBackgroundInstitutionContactPerson = sourceBackgroundInstitutionContactPersonService.create([domainModel: sourceBackgroundInstitutionContactPerson])
        assertNotNull "SourceBackgroundInstitutionContactPerson ID is null in SourceBackgroundInstitutionContactPerson Service Tests Create", sourceBackgroundInstitutionContactPerson.id
        assertNotNull "SourceBackgroundInstitutionContactPerson sourceAndBackgroundInstitution is null in SourceBackgroundInstitutionContactPerson Service Tests", sourceBackgroundInstitutionContactPerson.sourceAndBackgroundInstitution
        assertNotNull "SourceBackgroundInstitutionContactPerson personType is null in SourceBackgroundInstitutionContactPerson Service Tests", sourceBackgroundInstitutionContactPerson.personType
        assertNotNull sourceBackgroundInstitutionContactPerson.version
        assertNotNull sourceBackgroundInstitutionContactPerson.dataOrigin
        assertNotNull sourceBackgroundInstitutionContactPerson.lastModifiedBy
        assertNotNull sourceBackgroundInstitutionContactPerson.lastModified

        //Update the entity with new values
        def personType = newValidPersonType("UPDT", "UPDTDESC")
        personType.save(failOnError: true, flush: true)

        sourceBackgroundInstitutionContactPerson.phoneArea = "UPDATE"
        sourceBackgroundInstitutionContactPerson.phoneNumber = "UPDATE789012"
        sourceBackgroundInstitutionContactPerson.phoneExtension = "UPDATE7890"
        sourceBackgroundInstitutionContactPerson.countryPhone = "UPDT"
        sourceBackgroundInstitutionContactPerson.personType = newValidPersonType("UPDT", "UPDTDESC")
        sourceBackgroundInstitutionContactPerson.save(failOnError: true, flush: true)
        sourceBackgroundInstitutionContactPerson = sourceBackgroundInstitutionContactPersonService.update([domainModel: sourceBackgroundInstitutionContactPerson])

        // test the values
        assertEquals "UPDATE", sourceBackgroundInstitutionContactPerson.phoneArea
        assertEquals "UPDATE789012", sourceBackgroundInstitutionContactPerson.phoneNumber
        assertEquals "UPDATE7890", sourceBackgroundInstitutionContactPerson.phoneExtension
        assertEquals "UPDT", sourceBackgroundInstitutionContactPerson.countryPhone
        assertEquals "UPDT", sourceBackgroundInstitutionContactPerson.personType.code
    }


    void testSourceBackgroundInstitutionContactPersonInvalidUpdate() {
        SourceBackgroundInstitutionContactPerson sourceBackgroundInstitutionContactPerson = newValidForCreateSourceBackgroundInstitutionContactPerson()
        sourceBackgroundInstitutionContactPerson = sourceBackgroundInstitutionContactPersonService.create([domainModel: sourceBackgroundInstitutionContactPerson])
        assertNotNull "SourceBackgroundInstitutionContactPerson ID is null in SourceBackgroundInstitutionContactPerson Service Tests Create", sourceBackgroundInstitutionContactPerson.id
        assertNotNull "SourceBackgroundInstitutionContactPerson sourceAndBackgroundInstitution is null in SourceBackgroundInstitutionContactPerson Service Tests", sourceBackgroundInstitutionContactPerson.sourceAndBackgroundInstitution
        assertNotNull "SourceBackgroundInstitutionContactPerson personType is null in SourceBackgroundInstitutionContactPerson Service Tests", sourceBackgroundInstitutionContactPerson.personType
        assertNotNull sourceBackgroundInstitutionContactPerson.version
        assertNotNull sourceBackgroundInstitutionContactPerson.dataOrigin
        assertNotNull sourceBackgroundInstitutionContactPerson.lastModifiedBy
        assertNotNull sourceBackgroundInstitutionContactPerson.lastModified

        //Update the entity with new invalid values
        sourceBackgroundInstitutionContactPerson.personName = null
        shouldFail(ApplicationException) {
            sourceBackgroundInstitutionContactPerson = sourceBackgroundInstitutionContactPersonService.update([domainModel: sourceBackgroundInstitutionContactPerson])
        }
    }


    void testSourceBackgroundInstitutionContactPersonDelete() {
        SourceBackgroundInstitutionContactPerson sourceBackgroundInstitutionContactPerson = newValidForCreateSourceBackgroundInstitutionContactPerson()
        sourceBackgroundInstitutionContactPerson = sourceBackgroundInstitutionContactPersonService.create([domainModel: sourceBackgroundInstitutionContactPerson])
        assertNotNull "SourceBackgroundInstitutionContactPerson ID is null in SourceBackgroundInstitutionContactPerson Service Tests Create", sourceBackgroundInstitutionContactPerson.id
        def id = sourceBackgroundInstitutionContactPerson.id
        sourceBackgroundInstitutionContactPersonService.delete([domainModel: sourceBackgroundInstitutionContactPerson])
        assertNull "SourceBackgroundInstitutionContactPerson should have been deleted", sourceBackgroundInstitutionContactPerson.get(id)
    }


    void testReadOnly() {
        def sourceBackgroundInstitutionContactPerson = newValidForCreateSourceBackgroundInstitutionContactPerson()
        def map = [domainModel: sourceBackgroundInstitutionContactPerson]
        sourceBackgroundInstitutionContactPerson = sourceBackgroundInstitutionContactPersonService.create(map)
        assertNotNull "SourceBackgroundInstitutionContactPerson ID is null in SourceBackgroundInstitutionContactPerson Service Tests Create", sourceBackgroundInstitutionContactPerson.id

        sourceBackgroundInstitutionContactPerson.sourceAndBackgroundInstitution = SourceAndBackgroundInstitution.findWhere(code: "5815")
        sourceBackgroundInstitutionContactPerson.personName = "UPDATENAME"
        try {
            sourceBackgroundInstitutionContactPersonService.update([domainModel: sourceBackgroundInstitutionContactPerson])
            fail("This should have failed with @@r1:readonlyFieldsCannotBeModified")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "readonlyFieldsCannotBeModified"
        }
    }


    private def newValidForCreateSourceBackgroundInstitutionContactPerson() {
        def personType = newValidPersonType("TTTT", "TTTT")
        personType.save(failOnError: true, flush: true)

        def sourceBackgroundInstitutionContactPerson = new SourceBackgroundInstitutionContactPerson(
                personName: "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890",
                phoneArea: "123456",
                phoneNumber: "123456789012",
                phoneExtension: "1234567890",
                countryPhone: "1234",
                sourceAndBackgroundInstitution: SourceAndBackgroundInstitution.findWhere(code: "443361"),
                personType: personType,
        )
        return sourceBackgroundInstitutionContactPerson
    }


    private def newInvalidForCreateSourceBackgroundInstitutionContactPerson() {
        def sourceBackgroundInstitutionContactPerson = new SourceBackgroundInstitutionContactPerson(
                phoneArea: "123456FAIL",
                sourceAndBackgroundInstitution: null,
                personType: null,
        )
        return sourceBackgroundInstitutionContactPerson
    }


    private def newValidPersonType(code, description) {
        def personType = new PersonType(code: code, description: description)
        return personType
    }
}
