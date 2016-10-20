/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall.ldm;

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.dao.InvalidDataAccessResourceUsageException

public class PersonVisaIntegrationTests extends BaseIntegrationTestCase {
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
        PersonVisa personVisa = createNew()
        shouldFail(InvalidDataAccessResourceUsageException) {
            personVisa.save(flush: true, failOnError: true)
        }
    }

    @Test
    public void testUpdate() {
        PersonVisa personVisa = PersonVisa.findAll()[0]
        personVisa.personGuid = 'X'
        shouldFail(InvalidDataAccessResourceUsageException) {
            personVisa.save(flush: true, failOnError: true)
        }
    }

    @Test
    public void testRead() {
        PersonVisa personVisa = PersonVisa.findAll()[0]
        assertNotNull(personVisa.id)
        assertNotNull(PersonVisa.get(personVisa.id))
    }

    @Test
    public void testDelete() {
        PersonVisa personVisa = PersonVisa.findAll()[0]
        assertNotNull(personVisa.id)
        shouldFail(InvalidDataAccessResourceUsageException) {
            personVisa.delete(flush: true, failOnError: true)
        }
    }


    private PersonVisa createNew() {
        PersonVisa personVisa = new PersonVisa()
        personVisa.id = 'A' * 36
        personVisa.personGuid = 'A' * 36
        personVisa.nonResInd = 'A' * 1
        personVisa.visaTypeGuid = 'A' * 36
        personVisa.visaIssueDate = new Date()
        personVisa.visaNumber = 'A' * 18
        personVisa.visaRequestDate = new Date()
        personVisa.visaExpireDate = new Date()
        return personVisa
    }
}