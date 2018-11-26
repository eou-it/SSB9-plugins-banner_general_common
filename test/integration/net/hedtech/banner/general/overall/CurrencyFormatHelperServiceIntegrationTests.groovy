/********************************************************************************
  Copyright 2018 Ellucian Company L.P. and its affiliates.
********************************************************************************/
package net.hedtech.banner.general.overall

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.context.i18n.LocaleContextHolder

class CurrencyFormatHelperServiceIntegrationTests extends BaseIntegrationTestCase {

    def currencyFormatHelperService
    
    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()

    }


    @After
    public void tearDown() {
        super.tearDown()
        super.logout()
    }
    

    @Test
    void testFormatCurrency() {
        BigDecimal value = new BigDecimal(1.8)
        def result = currencyFormatHelperService.formatCurrency(value)

        assertEquals '$1.80', result
    }

    @Test
    void testFormatCurrencyInt() {
        Integer value = new Integer(21)
        def result = currencyFormatHelperService.formatCurrency(value)

        assertEquals '$21.00', result
    }

    @Test
    void testFormatCurrencyDouble() {
        Double value = new Double(92.01)
        def result = currencyFormatHelperService.formatCurrency(value)

        assertEquals '$92.01', result
    }

    @Test
    void testFormatCurrencyString() {
        def result = currencyFormatHelperService.formatCurrency('$1,000.00')

        assertNull result
    }

    @Test
    void testFormatCurrencyNull() {
        def result = currencyFormatHelperService.formatCurrency(null)

        assertNull result
    }
}
