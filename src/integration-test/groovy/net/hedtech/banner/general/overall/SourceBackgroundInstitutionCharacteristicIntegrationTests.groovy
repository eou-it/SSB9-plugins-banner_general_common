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
import grails.validation.ValidationException
import net.hedtech.banner.general.system.BackgroundInstitutionCharacteristic
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.testing.BaseIntegrationTestCase

import java.text.SimpleDateFormat

@Integration
@Rollback
class SourceBackgroundInstitutionCharacteristicIntegrationTests extends BaseIntegrationTestCase {

    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testCreateValidSourceBackgroundInstitutionCharacteristic() {
        def sourceBackgroundInstitutionCharacteristic = newValidForCreateSourceBackgroundInstitutionCharacteristic()
        sourceBackgroundInstitutionCharacteristic.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull sourceBackgroundInstitutionCharacteristic.id
    }


    @Test
    void testCreateInvalidSourceBackgroundInstitutionCharacteristic() {
        def sourceBackgroundInstitutionCharacteristic = newInvalidForCreateSourceBackgroundInstitutionCharacteristic()
        shouldFail(ValidationException) {
            sourceBackgroundInstitutionCharacteristic.save(failOnError: true, flush: true)
        }
    }

    // NOTE: No Updates are allowed

    @Test
    void testDates() {
        def hour = new SimpleDateFormat('HH')
        def date = new SimpleDateFormat('yyyy-M-d')
        def today = new Date()

        def sourceBackgroundInstitutionCharacteristic = newValidForCreateSourceBackgroundInstitutionCharacteristic()

        sourceBackgroundInstitutionCharacteristic.save(flush: true, failOnError: true)
        sourceBackgroundInstitutionCharacteristic.refresh()
        assertNotNull "SourceBackgroundInstitutionCharacteristic should have been saved", sourceBackgroundInstitutionCharacteristic.id

        // test date values -
        assertEquals date.format(today), date.format(sourceBackgroundInstitutionCharacteristic.lastModified)
        assertEquals hour.format(today), hour.format(sourceBackgroundInstitutionCharacteristic.lastModified)
    }


    @Test
    void testDeleteSourceBackgroundInstitutionCharacteristic() {
        def sourceBackgroundInstitutionCharacteristic = newValidForCreateSourceBackgroundInstitutionCharacteristic()
        sourceBackgroundInstitutionCharacteristic.save(failOnError: true, flush: true)
        def id = sourceBackgroundInstitutionCharacteristic.id
        assertNotNull id
        sourceBackgroundInstitutionCharacteristic.delete()
        assertNull SourceBackgroundInstitutionCharacteristic.get(id)
    }


    @Test
    void testValidation() {
        def sourceBackgroundInstitutionCharacteristic = newInvalidForCreateSourceBackgroundInstitutionCharacteristic()
        assertFalse "SourceBackgroundInstitutionCharacteristic could not be validated as expected due to ${sourceBackgroundInstitutionCharacteristic.errors}", sourceBackgroundInstitutionCharacteristic.validate()
    }


    @Test
    void testNullValidationFailure() {
        def sourceBackgroundInstitutionCharacteristic = new SourceBackgroundInstitutionCharacteristic()
        assertFalse "SourceBackgroundInstitutionCharacteristic should have failed validation", sourceBackgroundInstitutionCharacteristic.validate()
        assertErrorsFor sourceBackgroundInstitutionCharacteristic, 'nullable',
                [
                        'demographicYear',
                        'sourceAndBackgroundInstitution',
                        'backgroundInstitutionCharacteristic'
                ]
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


    private def newInvalidForCreateSourceBackgroundInstitutionCharacteristic() {
        def sourceBackgroundInstitutionCharacteristic = new SourceBackgroundInstitutionCharacteristic(
                demographicYear: null,
                sourceAndBackgroundInstitution: null,
                backgroundInstitutionCharacteristic: null,
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
}
