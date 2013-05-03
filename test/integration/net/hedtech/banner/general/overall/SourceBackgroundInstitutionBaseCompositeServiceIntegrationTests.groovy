/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.exceptions.ApplicationException
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
        map.sourceBackgroundInstitutionAcademic.accreditationType = "UPDT"
        map.sourceBackgroundInstitutionComment.commentData = "UPDATE"
        map.sourceBackgroundInstitutionContactPerson.name = "UPDATE"
        map.sourceBackgroundInstitutionDemographic.enrollment = 101
        map.sourceBackgroundInstitutionEthnicMakeUp.ethnicPercent = 51
        map.sourceBackgroundInstitutionTestScore.meanTestScore = "B"

        /*
         NOTE: these domains have no fields that are updateable
           SourceBackgroundInstitutionCharacteristic
           SourceBackgroundInstitutionDegreesOffered
           SourceBackgroundInstitutionDiplomasOffered
        */

        // do the update
        sourceBackgroundInstitutionBaseCompositeService.createOrUpdate(
                [
                        // single
                        sourceBackgroundInstitutionBase: map.sourceBackgroundInstitutionBase,
                        sourceBackgroundInstitutionAcademic: map.sourceBackgroundInstitutionAcademic,

                        // repeating
                        sourceBackgroundInstitutionComments: [map.sourceBackgroundInstitutionComment],
                        sourceBackgroundInstitutionContactPersons: [map.sourceBackgroundInstitutionContactPerson],
                        sourceBackgroundInstitutionDemographics: [map.sourceBackgroundInstitutionDemographic],
                        sourceBackgroundInstitutionEthnicMakeUps: [map.sourceBackgroundInstitutionEthnicMakeUp],
                        sourceBackgroundInstitutionTestScores: [map.sourceBackgroundInstitutionTestScore],
                ])

        // test that the update worked
        def sourceBackgroundInstitutionBase = SourceBackgroundInstitutionBase.findWhere(sourceAndBackgroundInstitution: map.sourceAndBackgroundInstitution)
        assertEquals 1L, sourceBackgroundInstitutionBase.version
        assertEquals "UPDATE", sourceBackgroundInstitutionBase.streetLine1

        def sourceBackgroundInstitutionAcademic = SourceBackgroundInstitutionAcademic.findWhere(sourceAndBackgroundInstitution: map.sourceAndBackgroundInstitution)
        assertEquals 1L, sourceBackgroundInstitutionAcademic.version
        assertEquals "UPDT", sourceBackgroundInstitutionAcademic.accreditationType

        def sourceBackgroundInstitutionComment = SourceBackgroundInstitutionComment.findWhere(sourceAndBackgroundInstitution: map.sourceAndBackgroundInstitution)
        assertEquals 1L, sourceBackgroundInstitutionComment.version
        assertEquals "UPDATE", sourceBackgroundInstitutionComment.commentData

        def sourceBackgroundInstitutionContactPerson = SourceBackgroundInstitutionContactPerson.findWhere(sourceAndBackgroundInstitution: map.sourceAndBackgroundInstitution)
        assertEquals 1L, sourceBackgroundInstitutionContactPerson.version
        assertEquals "UPDATE", sourceBackgroundInstitutionContactPerson.name

        def sourceBackgroundInstitutionDemographic = SourceBackgroundInstitutionDemographic.findWhere(sourceAndBackgroundInstitution: map.sourceAndBackgroundInstitution)
        assertEquals 1L, sourceBackgroundInstitutionDemographic.version
        assertEquals 101, sourceBackgroundInstitutionDemographic.enrollment

        def sourceBackgroundInstitutionEthnicMakeUp = SourceBackgroundInstitutionEthnicMakeUp.findWhere(sourceAndBackgroundInstitution: map.sourceAndBackgroundInstitution)
        assertEquals 1L, sourceBackgroundInstitutionEthnicMakeUp.version
        assertEquals 51, sourceBackgroundInstitutionEthnicMakeUp.ethnicPercent

        def sourceBackgroundInstitutionTestScore = SourceBackgroundInstitutionTestScore.findWhere(sourceAndBackgroundInstitution: map.sourceAndBackgroundInstitution)
        assertEquals 1L, sourceBackgroundInstitutionTestScore.version
        assertEquals "B", sourceBackgroundInstitutionTestScore.meanTestScore
    }


    void testSourceBackgroundInstitutionBaseCompositeDeleteAll() {
        // create and test new base and detail records
        def map = createAll()

        // delete master record
        sourceBackgroundInstitutionBaseCompositeService.createOrUpdate([deleteSourceBackgroundInstitutionBase: map.sourceBackgroundInstitutionBase])

        // comfirm both master and detail records were deleted
        def sourceBackgroundInstitutionBase = SourceBackgroundInstitutionBase.findWhere(sourceAndBackgroundInstitution: map.sourceAndBackgroundInstitution)
        assertNull sourceBackgroundInstitutionBase

        def sourceBackgroundInstitutionAcademic = SourceBackgroundInstitutionAcademic.findWhere(sourceAndBackgroundInstitution: map.sourceAndBackgroundInstitution)
        assertNull sourceBackgroundInstitutionAcademic

        def sourceBackgroundInstitutionCharacteristic = SourceBackgroundInstitutionCharacteristic.findWhere(sourceAndBackgroundInstitution: map.sourceAndBackgroundInstitution)
        assertNull sourceBackgroundInstitutionCharacteristic

        def sourceBackgroundInstitutionComment = SourceBackgroundInstitutionComment.findWhere(sourceAndBackgroundInstitution: map.sourceAndBackgroundInstitution)
        assertNull sourceBackgroundInstitutionComment

        def sourceBackgroundInstitutionContactPerson = SourceBackgroundInstitutionContactPerson.findWhere(sourceAndBackgroundInstitution: map.sourceAndBackgroundInstitution)
        assertNull sourceBackgroundInstitutionContactPerson

        def sourceBackgroundInstitutionDegreesOffered = SourceBackgroundInstitutionDegreesOffered.findWhere(sourceAndBackgroundInstitution: map.sourceAndBackgroundInstitution)
        assertNull sourceBackgroundInstitutionDegreesOffered

        def sourceBackgroundInstitutionDemographic = SourceBackgroundInstitutionDemographic.findWhere(sourceAndBackgroundInstitution: map.sourceAndBackgroundInstitution)
        assertNull sourceBackgroundInstitutionDemographic

        def sourceBackgroundInstitutionDiplomasOffered = SourceBackgroundInstitutionDiplomasOffered.findWhere(sourceAndBackgroundInstitution: map.sourceAndBackgroundInstitution)
        assertNull sourceBackgroundInstitutionDiplomasOffered

        def sourceBackgroundInstitutionEthnicMakeUp = SourceBackgroundInstitutionEthnicMakeUp.findWhere(sourceAndBackgroundInstitution: map.sourceAndBackgroundInstitution)
        assertNull sourceBackgroundInstitutionEthnicMakeUp

        def sourceBackgroundInstitutionTestScore = SourceBackgroundInstitutionTestScore.findWhere(sourceAndBackgroundInstitution: map.sourceAndBackgroundInstitution)
        assertNull sourceBackgroundInstitutionTestScore
    }


    void testSourceBackgroundInstitutionBaseCompositeDelete() {
        // create and test new base and detail records
        def map = createAll()

        // delete ONLY the detail records
        sourceBackgroundInstitutionBaseCompositeService.createOrUpdate([
                // single
                deleteSourceBackgroundInstitutionAcademic: map.sourceBackgroundInstitutionAcademic,
                deleteSourceBackgroundInstitutionCharacteristic: map.sourceBackgroundInstitutionCharacteristic,

                // repeating
                deleteSourceBackgroundInstitutionComments: [map.sourceBackgroundInstitutionComment],
                deleteSourceBackgroundInstitutionContactPersons: [map.sourceBackgroundInstitutionContactPerson],
                deleteSourceBackgroundInstitutionDegreesOffereds: [map.sourceBackgroundInstitutionDegreesOffered],
                deleteSourceBackgroundInstitutionDemographics: [map.sourceBackgroundInstitutionDemographic],
                deleteSourceBackgroundInstitutionDiplomasOffereds: [map.sourceBackgroundInstitutionDiplomasOffered],
                deleteSourceBackgroundInstitutionEthnicMakeUps: [map.sourceBackgroundInstitutionEthnicMakeUp],
                deleteSourceBackgroundInstitutionTestScores: [map.sourceBackgroundInstitutionTestScore],
        ])

        // confirm the master record still exists
        def sourceBackgroundInstitutionBase = SourceBackgroundInstitutionBase.findWhere(sourceAndBackgroundInstitution: map.sourceAndBackgroundInstitution)
        assertNotNull sourceBackgroundInstitutionBase

        // comfirm detail records were deleted
        def sourceBackgroundInstitutionAcademic = SourceBackgroundInstitutionAcademic.findWhere(sourceAndBackgroundInstitution: map.sourceAndBackgroundInstitution)
        assertNull sourceBackgroundInstitutionAcademic

        def sourceBackgroundInstitutionCharacteristic = SourceBackgroundInstitutionCharacteristic.findWhere(sourceAndBackgroundInstitution: map.sourceAndBackgroundInstitution)
        assertNull sourceBackgroundInstitutionCharacteristic

        def sourceBackgroundInstitutionComment = SourceBackgroundInstitutionComment.findWhere(sourceAndBackgroundInstitution: map.sourceAndBackgroundInstitution)
        assertNull sourceBackgroundInstitutionComment

        def sourceBackgroundInstitutionContactPerson = SourceBackgroundInstitutionContactPerson.findWhere(sourceAndBackgroundInstitution: map.sourceAndBackgroundInstitution)
        assertNull sourceBackgroundInstitutionContactPerson

        def sourceBackgroundInstitutionDegreesOffered = SourceBackgroundInstitutionDegreesOffered.findWhere(sourceAndBackgroundInstitution: map.sourceAndBackgroundInstitution)
        assertNull sourceBackgroundInstitutionDegreesOffered

        def sourceBackgroundInstitutionDemographic = SourceBackgroundInstitutionDemographic.findWhere(sourceAndBackgroundInstitution: map.sourceAndBackgroundInstitution)
        assertNull sourceBackgroundInstitutionDemographic

        def sourceBackgroundInstitutionDiplomasOffered = SourceBackgroundInstitutionDiplomasOffered.findWhere(sourceAndBackgroundInstitution: map.sourceAndBackgroundInstitution)
        assertNull sourceBackgroundInstitutionDiplomasOffered

        def sourceBackgroundInstitutionEthnicMakeUp = SourceBackgroundInstitutionEthnicMakeUp.findWhere(sourceAndBackgroundInstitution: map.sourceAndBackgroundInstitution)
        assertNull sourceBackgroundInstitutionEthnicMakeUp

        def sourceBackgroundInstitutionTestScore = SourceBackgroundInstitutionTestScore.findWhere(sourceAndBackgroundInstitution: map.sourceAndBackgroundInstitution)
        assertNull sourceBackgroundInstitutionTestScore
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Setup
    // -----------------------------------------------------------------------------------------------------------------

    private def createAll() {
        // create new base and detail records
        def sourceBackgroundInstitutionBase = newValidForCreateSourceBackgroundInstitutionBase()
        def sourceBackgroundInstitutionAcademic = newValidForCreateSourceBackgroundInstitutionAcademic()
        def sourceBackgroundInstitutionCharacteristic = newValidForCreateSourceBackgroundInstitutionCharacteristic()
        def sourceBackgroundInstitutionComment = newValidForCreateSourceBackgroundInstitutionComment()
        def sourceBackgroundInstitutionContactPerson = newValidForCreateSourceBackgroundInstitutionContactPerson()
        def sourceBackgroundInstitutionDegreesOffered = newValidForCreateSourceBackgroundInstitutionDegreesOffered()
        def sourceBackgroundInstitutionDemographic = newValidForCreateSourceBackgroundInstitutionDemographic()
        def sourceBackgroundInstitutionDiplomasOffered = newValidForCreateSourceBackgroundInstitutionDiplomasOffered()
        def sourceBackgroundInstitutionEthnicMakeUp = newValidForCreateSourceBackgroundInstitutionEthnicMakeUp()
        def sourceBackgroundInstitutionTestScore = newValidForCreateSourceBackgroundInstitutionTestScore()

        def sourceAndBackgroundInstitution = sourceBackgroundInstitutionBase.sourceAndBackgroundInstitution

        sourceBackgroundInstitutionBaseCompositeService.createOrUpdate(
                [
                        // single
                        sourceBackgroundInstitutionBase: sourceBackgroundInstitutionBase,
                        sourceBackgroundInstitutionAcademic: sourceBackgroundInstitutionAcademic,
                        sourceBackgroundInstitutionCharacteristic: sourceBackgroundInstitutionCharacteristic,

                        // repeating
                        sourceBackgroundInstitutionComments: [sourceBackgroundInstitutionComment],
                        sourceBackgroundInstitutionContactPersons: [sourceBackgroundInstitutionContactPerson],
                        sourceBackgroundInstitutionDegreesOffereds: [sourceBackgroundInstitutionDegreesOffered],
                        sourceBackgroundInstitutionDemographics: [sourceBackgroundInstitutionDemographic],
                        sourceBackgroundInstitutionDiplomasOffereds: [sourceBackgroundInstitutionDiplomasOffered],
                        sourceBackgroundInstitutionEthnicMakeUps: [sourceBackgroundInstitutionEthnicMakeUp],
                        sourceBackgroundInstitutionTestScores: [sourceBackgroundInstitutionTestScore],
                ])

        // comfirm these records exists
        sourceBackgroundInstitutionBase = SourceBackgroundInstitutionBase.findWhere(sourceAndBackgroundInstitution: sourceAndBackgroundInstitution)
        assertNotNull sourceBackgroundInstitutionBase.version

        sourceBackgroundInstitutionAcademic = SourceBackgroundInstitutionAcademic.findWhere(sourceAndBackgroundInstitution: sourceAndBackgroundInstitution)
        assertNotNull sourceBackgroundInstitutionAcademic.version

        sourceBackgroundInstitutionCharacteristic = SourceBackgroundInstitutionCharacteristic.findWhere(sourceAndBackgroundInstitution: sourceAndBackgroundInstitution)
        assertNotNull sourceBackgroundInstitutionCharacteristic.version

        sourceBackgroundInstitutionComment = SourceBackgroundInstitutionComment.findWhere(sourceAndBackgroundInstitution: sourceAndBackgroundInstitution)
        assertNotNull sourceBackgroundInstitutionComment.version

        sourceBackgroundInstitutionContactPerson = SourceBackgroundInstitutionContactPerson.findWhere(sourceAndBackgroundInstitution: sourceAndBackgroundInstitution)
        assertNotNull sourceBackgroundInstitutionContactPerson.version

        sourceBackgroundInstitutionDegreesOffered = SourceBackgroundInstitutionDegreesOffered.findWhere(sourceAndBackgroundInstitution: sourceAndBackgroundInstitution)
        assertNotNull sourceBackgroundInstitutionDegreesOffered.version

        sourceBackgroundInstitutionDemographic = SourceBackgroundInstitutionDemographic.findWhere(sourceAndBackgroundInstitution: sourceAndBackgroundInstitution)
        assertNotNull sourceBackgroundInstitutionDemographic.version

        sourceBackgroundInstitutionDiplomasOffered = SourceBackgroundInstitutionDiplomasOffered.findWhere(sourceAndBackgroundInstitution: sourceAndBackgroundInstitution)
        assertNotNull sourceBackgroundInstitutionDiplomasOffered.version

        sourceBackgroundInstitutionEthnicMakeUp = SourceBackgroundInstitutionEthnicMakeUp.findWhere(sourceAndBackgroundInstitution: sourceAndBackgroundInstitution)
        assertNotNull sourceBackgroundInstitutionEthnicMakeUp.version

        sourceBackgroundInstitutionTestScore = SourceBackgroundInstitutionTestScore.findWhere(sourceAndBackgroundInstitution: sourceAndBackgroundInstitution)
        assertNotNull sourceBackgroundInstitutionTestScore.version

        def map =
            [
                    sourceAndBackgroundInstitution: sourceAndBackgroundInstitution,
                    sourceBackgroundInstitutionBase: sourceBackgroundInstitutionBase,
                    sourceBackgroundInstitutionAcademic: sourceBackgroundInstitutionAcademic,
                    sourceBackgroundInstitutionCharacteristic: sourceBackgroundInstitutionCharacteristic,
                    sourceBackgroundInstitutionComment: sourceBackgroundInstitutionComment,
                    sourceBackgroundInstitutionContactPerson: sourceBackgroundInstitutionContactPerson,
                    sourceBackgroundInstitutionDegreesOffered: sourceBackgroundInstitutionDegreesOffered,
                    sourceBackgroundInstitutionDemographic: sourceBackgroundInstitutionDemographic,
                    sourceBackgroundInstitutionDiplomasOffered: sourceBackgroundInstitutionDiplomasOffered,
                    sourceBackgroundInstitutionEthnicMakeUp: sourceBackgroundInstitutionEthnicMakeUp,
                    sourceBackgroundInstitutionTestScore: sourceBackgroundInstitutionTestScore,
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
        def sourceAndBackgroundInstitution = SourceAndBackgroundInstitution.findWhere(code: "999999")
        def sourceBackgroundInstitutionComment = new SourceBackgroundInstitutionComment(
                sequenceNumber: SourceBackgroundInstitutionComment.fetchNextSequenceNumber(sourceAndBackgroundInstitution),
                commentData: "1234567890..1234567890",
                sourceAndBackgroundInstitution: sourceAndBackgroundInstitution,
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


    private def newValidForCreateSourceBackgroundInstitutionAcademic() {
        def sourceBackgroundInstitutionAcademic = new SourceBackgroundInstitutionAcademic(
                demographicYear: 2014,
                stateApprovIndicator: "Y",
                calendarType: "STANDARD",
                accreditationType: "TTTT",
                creditTransactionValue: 5.5,
                sourceAndBackgroundInstitution: SourceAndBackgroundInstitution.findWhere(code: "999999"),
        )
        return sourceBackgroundInstitutionAcademic
    }


    private def newValidForCreateSourceBackgroundInstitutionCharacteristic() {
        def backgroundInstitutionCharacteristic = newBackgroundInstitutionCharacteristic()
        backgroundInstitutionCharacteristic.save(failOnError: true, flush: true)

        def sourceBackgroundInstitutionCharacteristic = new SourceBackgroundInstitutionCharacteristic(
                demographicYear: 2014,
                sourceAndBackgroundInstitution: SourceAndBackgroundInstitution.findWhere(code: "999999"),
                backgroundInstitutionCharacteristic: backgroundInstitutionCharacteristic,
        )
        return sourceBackgroundInstitutionCharacteristic
    }


    private def newBackgroundInstitutionCharacteristic() {
        def backgroundInstitutionCharacteristic = new BackgroundInstitutionCharacteristic(
                code: "T",
                description: "TTTT",
        )
        return backgroundInstitutionCharacteristic
    }


    private def newValidForCreateSourceBackgroundInstitutionDegreesOffered() {
        def sourceBackgroundInstitutionDegreesOffered = new SourceBackgroundInstitutionDegreesOffered(
                demographicYear: 2014,
                sourceAndBackgroundInstitution: SourceAndBackgroundInstitution.findWhere(code: "999999"),
                degree: Degree.findWhere(code: "PHD"),
        )
        return sourceBackgroundInstitutionDegreesOffered
    }


    private def newValidForCreateSourceBackgroundInstitutionDemographic() {
        def sourceBackgroundInstitutionDemographic = new SourceBackgroundInstitutionDemographic(
                demographicYear: 2014,
                enrollment: 100,
                numberOfSeniors: 50,
                meanFamilyIncome: 60,
                percentCollegeBound: 80,
                sourceAndBackgroundInstitution: SourceAndBackgroundInstitution.findWhere(code: "999999"),
        )
        return sourceBackgroundInstitutionDemographic
    }


    private def newValidForCreateSourceBackgroundInstitutionDiplomasOffered() {
        def diplomaType = newDiplomaType()
        diplomaType.save(failOnError: true, flush: true)
        def sourceBackgroundInstitutionDiplomasOffered = new SourceBackgroundInstitutionDiplomasOffered(
                demographicYear: 2014,
                sourceAndBackgroundInstitution: SourceAndBackgroundInstitution.findWhere(code: "999999"),
                diplomaType: diplomaType,
        )
        return sourceBackgroundInstitutionDiplomasOffered
    }


    private def newDiplomaType() {
        def diplomaType = new DiplomaType(
                code: "TT",
                description: "TTTT"
        )
        return diplomaType
    }


    private def newValidForCreateSourceBackgroundInstitutionEthnicMakeUp() {
        def sourceBackgroundInstitutionEthnicMakeUp = new SourceBackgroundInstitutionEthnicMakeUp(
                demographicYear: 2014,
                ethnicPercent: 50,
                sourceAndBackgroundInstitution: SourceAndBackgroundInstitution.findWhere(code: "999999"),
                ethnicity: Ethnicity.findWhere(code: "1"),
        )
        return sourceBackgroundInstitutionEthnicMakeUp
    }


    private def newValidForCreateSourceBackgroundInstitutionTestScore() {
        def sourceBackgroundInstitutionTestScore = new SourceBackgroundInstitutionTestScore(
                demographicYear: 2014,
                meanTestScore: 100,
                sourceAndBackgroundInstitution: SourceAndBackgroundInstitution.findWhere(code: "999999"),
                testScore: TestScore.findWhere(code: "JL"),
        )
        return sourceBackgroundInstitutionTestScore
    }
}
