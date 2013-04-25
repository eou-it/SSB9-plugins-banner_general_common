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
        def sourceBackgroundInstitutionBase = newValidForCreateSourceBackgroundInstitutionBase()
        sourceBackgroundInstitutionBase.save(failOnError: true, flush: true)

        def sourceAndBackgroundInstitution = sourceBackgroundInstitutionBase.sourceAndBackgroundInstitution
        sourceBackgroundInstitutionBaseCompositeService.createOrUpdate([sourceBackgroundInstitutionBases: sourceBackgroundInstitutionBase])

        sourceBackgroundInstitutionBase = SourceBackgroundInstitutionBase.findWhere(sourceAndBackgroundInstitution: sourceAndBackgroundInstitution)
        assertEquals 0L, sourceBackgroundInstitutionBase.version
    }


    void testSourceBackgroundInstitutionBaseCompositeDeleteAll() {
        // create new base and detail records
        def sourceBackgroundInstitutionBase = newValidForCreateSourceBackgroundInstitutionBase()
        sourceBackgroundInstitutionBase.save(failOnError: true, flush: true)

        def sourceBackgroundInstitutionComment = newValidForCreateSourceBackgroundInstitutionComment()
        sourceBackgroundInstitutionComment.save(failOnError: true, flush: true)

        def sourceBackgroundInstitutionContactPerson = newValidForCreateSourceBackgroundInstitutionContactPerson()
        sourceBackgroundInstitutionContactPerson.save(failOnError: true, flush: true)

        def sourceAndBackgroundInstitution = sourceBackgroundInstitutionBase.sourceAndBackgroundInstitution

        // comfirm these records exists
        sourceBackgroundInstitutionBase = SourceBackgroundInstitutionBase.findWhere(sourceAndBackgroundInstitution: sourceAndBackgroundInstitution)
        assertNotNull sourceBackgroundInstitutionBase.version

        sourceBackgroundInstitutionComment = SourceBackgroundInstitutionComment.findWhere(sourceAndBackgroundInstitution: sourceAndBackgroundInstitution)
        assertNotNull sourceBackgroundInstitutionComment.version

        sourceBackgroundInstitutionContactPerson = SourceBackgroundInstitutionContactPerson.findWhere(sourceAndBackgroundInstitution: sourceAndBackgroundInstitution)
        assertNotNull sourceBackgroundInstitutionContactPerson.version

        // delete base record
        sourceBackgroundInstitutionBaseCompositeService.createOrUpdate([deleteSourceBackgroundInstitutionBases: sourceBackgroundInstitutionBase])

        // comfirm these records were deleted
        sourceBackgroundInstitutionBase = SourceBackgroundInstitutionBase.findWhere(sourceAndBackgroundInstitution: sourceAndBackgroundInstitution)
        assertNull sourceBackgroundInstitutionBase

        sourceBackgroundInstitutionComment = SourceBackgroundInstitutionComment.findWhere(sourceAndBackgroundInstitution: sourceAndBackgroundInstitution)
        assertNull sourceBackgroundInstitutionComment

        sourceBackgroundInstitutionContactPerson = SourceBackgroundInstitutionContactPerson.findWhere(sourceAndBackgroundInstitution: sourceAndBackgroundInstitution)
        assertNull sourceBackgroundInstitutionContactPerson

    }


    void testSourceBackgroundInstitutionBaseCompositeDelete() {
        // create new base and detail records
        def sourceBackgroundInstitutionBase = newValidForCreateSourceBackgroundInstitutionBase()
        sourceBackgroundInstitutionBase.save(failOnError: true, flush: true)

        def sourceBackgroundInstitutionComment = newValidForCreateSourceBackgroundInstitutionComment()
        sourceBackgroundInstitutionComment.save(failOnError: true, flush: true)

        def sourceBackgroundInstitutionContactPerson = newValidForCreateSourceBackgroundInstitutionContactPerson()
        sourceBackgroundInstitutionContactPerson.save(failOnError: true, flush: true)

        def sourceAndBackgroundInstitution = sourceBackgroundInstitutionBase.sourceAndBackgroundInstitution

        // comfirm these records exists
        sourceBackgroundInstitutionBase = SourceBackgroundInstitutionBase.findWhere(sourceAndBackgroundInstitution: sourceAndBackgroundInstitution)
        assertNotNull sourceBackgroundInstitutionBase

        sourceBackgroundInstitutionComment = SourceBackgroundInstitutionComment.findWhere(sourceAndBackgroundInstitution: sourceAndBackgroundInstitution)
        assertNotNull sourceBackgroundInstitutionComment

        sourceBackgroundInstitutionContactPerson = SourceBackgroundInstitutionContactPerson.findWhere(sourceAndBackgroundInstitution: sourceAndBackgroundInstitution)
        assertNotNull sourceBackgroundInstitutionContactPerson

        // delete ONLY the detail records
        sourceBackgroundInstitutionBaseCompositeService.createOrUpdate([
                deleteSourceBackgroundInstitutionComments: sourceBackgroundInstitutionComment,
                deleteSourceBackgroundInstitutionContactPersons: sourceBackgroundInstitutionContactPerson,
        ])

        // confirm the master record still exists
        sourceBackgroundInstitutionBase = SourceBackgroundInstitutionBase.findWhere(sourceAndBackgroundInstitution: sourceAndBackgroundInstitution)
        assertNotNull sourceBackgroundInstitutionBase

        // comfirm detail records were deleted
        sourceBackgroundInstitutionComment = SourceBackgroundInstitutionComment.findWhere(sourceAndBackgroundInstitution: sourceAndBackgroundInstitution)
        assertNull sourceBackgroundInstitutionComment

        sourceBackgroundInstitutionContactPerson = SourceBackgroundInstitutionContactPerson.findWhere(sourceAndBackgroundInstitution: sourceAndBackgroundInstitution)
        assertNull sourceBackgroundInstitutionContactPerson

    }


    private def newValidForCreateSourceBackgroundInstitutionBase() {
        def sourceBackgroundInstitutionBase = new SourceBackgroundInstitutionBase(
                streetLine1: "123456789012345678901234567890123456789012345678901234567890123456789012345",
                streetLine2: "123456789012345678901234567890123456789012345678901234567890123456789012345",
                streetLine3: "123456789012345678901234567890123456789012345678901234567890123456789012345",
                city: "12345678901234567890123456789012345678901234567890",
                zip: "123456789012345678901234567890",
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
}
