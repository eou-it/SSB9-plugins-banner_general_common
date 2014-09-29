/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall


import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.Test


class SqlProcessParameterIntegrationTests extends BaseIntegrationTestCase {

    @Override
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }

    @Override
    public void tearDown() {
        super.tearDown()
    }

    @Test
    void testValidation() {
        def sqlProcessParameter = new SqlProcessParameter()
        assertFalse "SqlProcessParameter could not be validated as expected due to ${sqlProcessParameter.errors}", sqlProcessParameter.validate()
    }

    @Test
    void testNullValidationFailure() {
        def sqlProcessParameter = new SqlProcessParameter()
        assertFalse "SqlProcessParameter should have failed validation", sqlProcessParameter.validate()
        assertErrorsFor sqlProcessParameter, 'nullable',
        [
            'systemRequiredIndicator',
            'entriesForSqlProcess',
            'parameterForSqlProcess'
        ]
    }

    @Test
    void testCreateAndDeleteAndCommonDomainMethods() {
        SqlProcessParameter sqlProcessParameter = createSqlProcessParameter()
        sqlProcessParameter.save(failOnError: true, flush: true)
        SqlProcessParameter that = SqlProcessParameter.get(sqlProcessParameter.id)
        assertTrue sqlProcessParameter.equals(that)
        assertTrue sqlProcessParameter.toString() instanceof String
        assertTrue sqlProcessParameter.hashCode() instanceof Integer
        assertNotNull that
        that.delete(failOnError: true, flush: true)
        assertNull SqlProcessParameter.get(that?.id)
    }

    SqlProcessParameter createSqlProcessParameter() {
        new SqlProcessParameter(systemRequiredIndicator:false,
        lastModified:new Date(),
        lastModifiedBy:"GRAILS_USER",
        dataOrigin:"Banner",
        entriesForSqlProcess:"LDM",
        parameterForSqlProcess:"PIDM")
    }
}
