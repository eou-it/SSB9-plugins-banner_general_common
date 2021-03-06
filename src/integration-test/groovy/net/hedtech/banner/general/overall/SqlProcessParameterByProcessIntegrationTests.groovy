/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test


@Integration
@Rollback
class SqlProcessParameterByProcessIntegrationTests extends BaseIntegrationTestCase {


    static final PROCESS_CODE = 'HEDM'


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
    void testValidation() {
        def sqlProcessParameter = new SqlProcessParameterByProcess()
        assertFalse "SqlProcessParameter could not be validated as expected due to ${sqlProcessParameter.errors}", sqlProcessParameter.validate()
    }

    @Test
    void testNullValidationFailure() {
        def sqlProcessParameter = new SqlProcessParameterByProcess()
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
        SqlProcessParameterByProcess sqlProcessParameter = createSqlProcessParameter()
        sqlProcessParameter.save(failOnError: true, flush: true)
        SqlProcessParameterByProcess that = SqlProcessParameterByProcess.get(sqlProcessParameter.id)
        assertTrue sqlProcessParameter.equals(that)
        assertTrue sqlProcessParameter.toString() instanceof String
        assertTrue sqlProcessParameter.hashCode() instanceof Integer
        assertNotNull that
        that.delete(failOnError: true, flush: true)
        assertNull SqlProcessParameterByProcess.get(that?.id)
    }

    SqlProcessParameterByProcess createSqlProcessParameter() {
        new SqlProcessParameterByProcess(systemRequiredIndicator:false,
        lastModified:new Date(),
        lastModifiedBy:"GRAILS_USER",
        dataOrigin:"Banner",
        entriesForSqlProcess:PROCESS_CODE,
        parameterForSqlProcess:"PIDM")
    }
}
