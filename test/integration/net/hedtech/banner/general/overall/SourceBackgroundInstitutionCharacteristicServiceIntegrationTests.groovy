/*********************************************************************************
  Copyright 2010-2013 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.system.BackgroundInstitutionCharacteristic
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.testing.BaseIntegrationTestCase

class SourceBackgroundInstitutionCharacteristicServiceIntegrationTests extends BaseIntegrationTestCase {

    def sourceBackgroundInstitutionCharacteristicService


    void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }



    void tearDown() {
        super.tearDown()
    }


    void testSourceBackgroundInstitutionCharacteristicValidCreate() {
        def sourceBackgroundInstitutionCharacteristic = newValidForCreateSourceBackgroundInstitutionCharacteristic()
        def map = [domainModel: sourceBackgroundInstitutionCharacteristic]
        sourceBackgroundInstitutionCharacteristic = sourceBackgroundInstitutionCharacteristicService.create(map)
        assertNotNull "SourceBackgroundInstitutionCharacteristic ID is null in SourceBackgroundInstitutionCharacteristic Service Tests Create", sourceBackgroundInstitutionCharacteristic.id
        assertNotNull "SourceBackgroundInstitutionCharacteristic sourceAndBackgroundInstitution is null in SourceBackgroundInstitutionCharacteristic Service Tests", sourceBackgroundInstitutionCharacteristic.sourceAndBackgroundInstitution
        assertNotNull "SourceBackgroundInstitutionCharacteristic backgroundInstitutionCharacteristic is null in SourceBackgroundInstitutionCharacteristic Service Tests", sourceBackgroundInstitutionCharacteristic.backgroundInstitutionCharacteristic
        assertNotNull sourceBackgroundInstitutionCharacteristic.version
        assertNotNull sourceBackgroundInstitutionCharacteristic.dataOrigin
        assertNotNull sourceBackgroundInstitutionCharacteristic.lastModifiedBy
        assertNotNull sourceBackgroundInstitutionCharacteristic.lastModified
    }


    void testSourceBackgroundInstitutionCharacteristicInvalidCreate() {
        def sourceBackgroundInstitutionCharacteristic = newInvalidForCreateSourceBackgroundInstitutionCharacteristic()
        def map = [domainModel: sourceBackgroundInstitutionCharacteristic]
        shouldFail(ApplicationException) {
            sourceBackgroundInstitutionCharacteristicService.create(map)
        }
    }

    // NOTE: No Updates are allowed

    void testSourceBackgroundInstitutionCharacteristicDelete() {
        def sourceBackgroundInstitutionCharacteristic = newValidForCreateSourceBackgroundInstitutionCharacteristic()
        def map = [domainModel: sourceBackgroundInstitutionCharacteristic]
        sourceBackgroundInstitutionCharacteristic = sourceBackgroundInstitutionCharacteristicService.create(map)
        assertNotNull "SourceBackgroundInstitutionCharacteristic ID is null in SourceBackgroundInstitutionCharacteristic Service Tests Create", sourceBackgroundInstitutionCharacteristic.id
        def id = sourceBackgroundInstitutionCharacteristic.id
        sourceBackgroundInstitutionCharacteristicService.delete([domainModel: sourceBackgroundInstitutionCharacteristic])
        assertNull "SourceBackgroundInstitutionCharacteristic should have been deleted", sourceBackgroundInstitutionCharacteristic.get(id)
    }


    void testReadOnly() {
        def sourceBackgroundInstitutionCharacteristic = newValidForCreateSourceBackgroundInstitutionCharacteristic()
        def map = [domainModel: sourceBackgroundInstitutionCharacteristic]
        sourceBackgroundInstitutionCharacteristic = sourceBackgroundInstitutionCharacteristicService.create(map)
        assertNotNull "SourceBackgroundInstitutionCharacteristic ID is null in SourceBackgroundInstitutionCharacteristic Service Tests Create", sourceBackgroundInstitutionCharacteristic.id

        sourceBackgroundInstitutionCharacteristic.sourceAndBackgroundInstitution = SourceAndBackgroundInstitution.findWhere(code: "5815")
        sourceBackgroundInstitutionCharacteristic.backgroundInstitutionCharacteristic = new BackgroundInstitutionCharacteristic(
                code: "Z",
                description: "ZZZZ",
        )
        map.domainModel.demographicYear = 2013
        try {
            sourceBackgroundInstitutionCharacteristicService.update([domainModel: sourceBackgroundInstitutionCharacteristic])
            fail("This should have failed with @@r1:readonlyFieldsCannotBeModified")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "readonlyFieldsCannotBeModified"
        }
    }


    private def newValidForCreateSourceBackgroundInstitutionCharacteristic() {
        def backgroundInstitutionCharacteristic = newBackgroundInstitutionCharacteristic()
        backgroundInstitutionCharacteristic.save(failOnError: true, flush: true)

        def sourceBackgroundInstitutionCharacteristic = new SourceBackgroundInstitutionCharacteristic(
                demographicYear: 2014,
                sourceAndBackgroundInstitution: SourceAndBackgroundInstitution.findWhere(code: "443361"),
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
