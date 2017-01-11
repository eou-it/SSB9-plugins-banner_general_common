/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

public class PersonVisaServiceIntegrationTests extends BaseIntegrationTestCase {
    PersonVisaService personVisaService

    @Before
    public void setUp() {
        formContext = ['GUAGMNU'] // Since we are not testing a controller, we need to explicitly set this
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    public void testCreate() {
        PersonVisa personvisa = createNew()
        shouldFail(ApplicationException) {
            personVisaService.create(personvisa)
        }
    }

    @Test
    public void testUpdate() {
        PersonVisa personvisa = PersonVisa.findAll()[0]
        assertNotNull(personvisa.id)
        personvisa.personGuid = 'X'
        shouldFail(ApplicationException) {
            personVisaService.update(personvisa)
        }
    }

    @Test
    public void testRead() {
        PersonVisa personvisa = PersonVisa.findAll()[0]
        assertNotNull(personvisa.id)
        assertNotNull(personVisaService.read(personvisa.id))
    }

    @Test
    public void testDelete() {
        PersonVisa personvisa = PersonVisa.findAll()[0]
        assertNotNull(personvisa.id)
        shouldFail(ApplicationException) {
            personVisaService.delete(personvisa)
        }
    }

    @Test
    public void testFetchByCriteria() {
        List<PersonVisa> personVisaList = personVisaService.fetchByCriteria([:], [], [max: 1])
        assertEquals(1, personVisaList.size())
    }

    @Test
    public void testCountByCriteria() {
        int count = personVisaService.countByCriteria([:], [])
        assertEquals(PersonVisa.count(), count)
    }

    private PersonVisa createNew() {
        PersonVisa personvisa = new PersonVisa()
        personvisa.id = 'A' * 36
        personvisa.personGuid = 'A' * 36
        personvisa.nonResInd = 'A' * 1
        personvisa.visaTypeGuid = 'A' * 36
        personvisa.visaIssueDate = new Date()
        personvisa.visaNumber = 'A' * 18
        personvisa.visaRequestDate = new Date()
        personvisa.visaExpireDate = new Date()
        return personvisa
    }
}