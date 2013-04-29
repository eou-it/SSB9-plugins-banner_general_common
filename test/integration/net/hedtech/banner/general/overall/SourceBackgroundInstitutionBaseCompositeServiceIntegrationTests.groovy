/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.general.system.*
import net.hedtech.banner.testing.BaseIntegrationTestCase

class SourceBackgroundInstitutionBaseCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    def sourceBackgroundInstitutionBaseCompositeService


    protected void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    protected void tearDown() {
        super.tearDown()
    }


    void testSourceBackgroundInstitutionBaseCompositeCreate() {
        // create and test new base and detail records
        createAll()
    }


    void testSourceBackgroundInstitutionBaseCompositeUpdate() {
        // create and test new base and detail records
        def map = createAll()

        // update some property of each record
        map.sourceBackgroundInstitutionBase.streetLine1 = "UPDATE"
        map.sourceBackgroundInstitutionComment.commentData = "UPDATE"
        map.sourceBackgroundInstitutionContactPerson.name = "UPDATE"

        // do the update
        sourceBackgroundInstitutionBaseCompositeService.createOrUpdate(
                [
                        sourceBackgroundInstitutionBases: [map.sourceBackgroundInstitutionBase],
                        sourceBackgroundInstitutionComments: [map.sourceBackgroundInstitutionComment],
                        sourceBackgroundInstitutionContactPersons: [map.sourceBackgroundInstitutionContactPerson],
                ])

        // test that the update worked
        def sourceBackgroundInstitutionBase = SourceBackgroundInstitutionBase.findWhere(sourceAndBackgroundInstitution: map.sourceAndBackgroundInstitution)
        assertEquals 1L, sourceBackgroundInstitutionBase.version
        assertEquals "UPDATE", sourceBackgroundInstitutionBase.streetLine1

        def sourceBackgroundInstitutionComment = SourceBackgroundInstitutionComment.findWhere(sourceAndBackgroundInstitution: map.sourceAndBackgroundInstitution)
        assertEquals 1L, sourceBackgroundInstitutionComment.version
        assertEquals "UPDATE", sourceBackgroundInstitutionComment.commentData

        def sourceBackgroundInstitutionContactPerson = SourceBackgroundInstitutionContactPerson.findWhere(sourceAndBackgroundInstitution: map.sourceAndBackgroundInstitution)
        assertEquals 1L, sourceBackgroundInstitutionContactPerson.version
        assertEquals "UPDATE", sourceBackgroundInstitutionContactPerson.name
    }


    void testSourceBackgroundInstitutionBaseCompositeDeleteAll() {
        // create and test new base and detail records
        def map = createAll()

        // delete master record
        sourceBackgroundInstitutionBaseCompositeService.createOrUpdate([deleteSourceBackgroundInstitutionBases: [map.sourceBackgroundInstitutionBase]])

        // comfirm both master and detail records were deleted
        def sourceBackgroundInstitutionBase = SourceBackgroundInstitutionBase.findWhere(sourceAndBackgroundInstitution: map.sourceAndBackgroundInstitution)
        assertNull sourceBackgroundInstitutionBase

        def sourceBackgroundInstitutionComment = SourceBackgroundInstitutionComment.findWhere(sourceAndBackgroundInstitution: map.sourceAndBackgroundInstitution)
        assertNull sourceBackgroundInstitutionComment

        def sourceBackgroundInstitutionContactPerson = SourceBackgroundInstitutionContactPerson.findWhere(sourceAndBackgroundInstitution: map.sourceAndBackgroundInstitution)
        assertNull sourceBackgroundInstitutionContactPerson
    }


    void testSourceBackgroundInstitutionBaseCompositeDelete() {
        // create and test new base and detail records
        def map = createAll()

        // delete ONLY the detail records
        sourceBackgroundInstitutionBaseCompositeService.createOrUpdate([
                deleteSourceBackgroundInstitutionComments: [map.sourceBackgroundInstitutionComment],
                deleteSourceBackgroundInstitutionContactPersons: [map.sourceBackgroundInstitutionContactPerson],
        ])

        // confirm the master record still exists
        def sourceBackgroundInstitutionBase = SourceBackgroundInstitutionBase.findWhere(sourceAndBackgroundInstitution: map.sourceAndBackgroundInstitution)
        assertNotNull sourceBackgroundInstitutionBase

        // comfirm detail records were deleted
        def sourceBackgroundInstitutionComment = SourceBackgroundInstitutionComment.findWhere(sourceAndBackgroundInstitution: map.sourceAndBackgroundInstitution)
        assertNull sourceBackgroundInstitutionComment

        def sourceBackgroundInstitutionContactPerson = SourceBackgroundInstitutionContactPerson.findWhere(sourceAndBackgroundInstitution: map.sourceAndBackgroundInstitution)
        assertNull sourceBackgroundInstitutionContactPerson
    }


    private def createAll() {
        // create new base and detail records
        def sourceBackgroundInstitutionBase = newValidForCreateSourceBackgroundInstitutionBase()
        def sourceBackgroundInstitutionComment = newValidForCreateSourceBackgroundInstitutionComment()
        def sourceBackgroundInstitutionContactPerson = newValidForCreateSourceBackgroundInstitutionContactPerson()
        def sourceAndBackgroundInstitution = sourceBackgroundInstitutionBase.sourceAndBackgroundInstitution

        sourceBackgroundInstitutionBaseCompositeService.createOrUpdate(
                [
                        sourceBackgroundInstitutionBases: [sourceBackgroundInstitutionBase],
                        sourceBackgroundInstitutionComments: [sourceBackgroundInstitutionComment],
                        sourceBackgroundInstitutionContactPersons: [sourceBackgroundInstitutionContactPerson],
                ])

        // comfirm these records exists
        sourceBackgroundInstitutionBase = SourceBackgroundInstitutionBase.findWhere(sourceAndBackgroundInstitution: sourceAndBackgroundInstitution)
        assertNotNull sourceBackgroundInstitutionBase.version

        sourceBackgroundInstitutionComment = SourceBackgroundInstitutionComment.findWhere(sourceAndBackgroundInstitution: sourceAndBackgroundInstitution)
        assertNotNull sourceBackgroundInstitutionComment.version

        sourceBackgroundInstitutionContactPerson = SourceBackgroundInstitutionContactPerson.findWhere(sourceAndBackgroundInstitution: sourceAndBackgroundInstitution)
        assertNotNull sourceBackgroundInstitutionContactPerson.version

        def map =
            [
                    sourceAndBackgroundInstitution: sourceAndBackgroundInstitution,
                    sourceBackgroundInstitutionBase: sourceBackgroundInstitutionBase,
                    sourceBackgroundInstitutionComment: sourceBackgroundInstitutionComment,
                    sourceBackgroundInstitutionContactPerson: sourceBackgroundInstitutionContactPerson,
            ]

        return map
    }


    private def newValidForCreateSourceBackgroundInstitutionBase() {
        def zip = newValidForCreateZip()
        zip.save(failOnError: true, flush: true)

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


    private def newValidForCreateSourceBackgroundInstitutionComment() {
        def sourceBackgroundInstitutionComment = new SourceBackgroundInstitutionComment(
                sequenceNumber: SourceBackgroundInstitutionComment.fetchNextSequenceNumber(),
                commentData: "1234567890..1234567890",
                sourceAndBackgroundInstitution: SourceAndBackgroundInstitution.findWhere(code: "999999"),
        )
        return sourceBackgroundInstitutionComment
    }


    private def newValidForCreateSourceBackgroundInstitutionContactPerson() {
        def personType = newValidPersonType("TTTT", "TTTT")
        personType.save(failOnError: true, flush: true)

        def sourceBackgroundInstitutionContactPerson = new SourceBackgroundInstitutionContactPerson(
                name: "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890",
                phoneArea: "123456",
                phoneNumber: "123456789012",
                phoneExtension: "1234567890",
                countryPhone: "1234",
                sourceAndBackgroundInstitution: SourceAndBackgroundInstitution.findWhere(code: "999999"),
                personType: personType,
        )
        return sourceBackgroundInstitutionContactPerson
    }


    private def newValidPersonType(code, description) {
        def personType = new PersonType(code: code, description: description)
        return personType
    }


    private def newValidForCreateZip() {
        def zip = new Zip(
                code: "123456789012345678901234567890",
                city: "12345678901234567890123456789012345678901234567890",
                state: State.findWhere(code: "PA"),
                county: County.findWhere(code: "001"),
                nation: Nation.findWhere(code: "1"),
        )
        return zip
    }
}
