/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.overall.ldm.v6.PersonVisaDecorator
import net.hedtech.banner.restfulapi.RestfulApiValidationUtility
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

public class PersonVisaCompositeServiceIntegrationTests extends BaseIntegrationTestCase {
    PersonVisaCompositeService personVisaCompositeService

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
    void testListNoParams() {
        List<PersonVisaDecorator> personVisaDecoratorList = personVisaCompositeService.list([:])
        assertNotNull(personVisaDecoratorList)
        int max = RestfulApiValidationUtility.MAX_DEFAULT < PersonVisa.findAll().size() ?: PersonVisa.findAll().size()
        assertEquals(max, personVisaDecoratorList.size())
    }

    @Test
    void testListMax() {
        List<PersonVisaDecorator> personVisaDecoratorList = personVisaCompositeService.list([max: "1"])
        assertNotNull(personVisaDecoratorList)
        assertEquals(1, personVisaDecoratorList.size())
    }

    @Test
    void testListOffset() {
        List<PersonVisaDecorator> personVisaDecoratorList = personVisaCompositeService.list([max: "2"])
        assertNotNull(personVisaDecoratorList)
        int max = 2 < PersonVisa.findAll().size() ?: PersonVisa.findAll().size()
        assertEquals(max, personVisaDecoratorList.size())
        List<PersonVisaDecorator> personVisaDecoratorList1 = personVisaCompositeService.list([max: "1", offset: "1"])
        assertNotNull(personVisaDecoratorList1)
        if (PersonVisa.findAll().size() == 1) {
            assertNull(personVisaDecoratorList1[0])
        } else {
            assertEquals(personVisaDecoratorList[1].id, personVisaDecoratorList1[0].id)
        }
    }

    @Test
    void testInvalidSearchField() {
        List<PersonVisaDecorator> personVisaDecoratorList = personVisaCompositeService.list([:])
        List<PersonVisaDecorator> personVisaDecoratorList1 = personVisaCompositeService.list([invalid: 'some-junk'])
        assertEquals(personVisaDecoratorList.size(), personVisaDecoratorList1.size())
        assertEquals(personVisaDecoratorList.id, personVisaDecoratorList1.id)
    }

    @Test
    void testPersonFilter() {
        String personGuid = PersonVisa.findAll()[0].personGuid
        List<PersonVisaDecorator> personVisaDecoratorList = personVisaCompositeService.list([person: personGuid])
        assertNotNull(personVisaDecoratorList)
        personVisaDecoratorList.each {
            assertEquals(personGuid, it.person.id)
        }
    }

    @Test
    void testCount() {
        assertEquals(PersonVisa.count(), personVisaCompositeService.count([:]))
    }

    @Test
    void testInvalidIdGet() {
        shouldFail(ApplicationException) {
            personVisaCompositeService.get('invalid-random-id')
        }
    }

    @Test
    void testValidIdGet() {
        PersonVisa personVisa = PersonVisa.findAll()[0]
        assertEquals(personVisa.id, personVisaCompositeService.get(personVisa.id).id)
    }
}