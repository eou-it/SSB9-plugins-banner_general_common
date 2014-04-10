/*******************************************************************************
Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall

import grails.validation.ValidationException
import groovy.sql.Sql
import java.text.SimpleDateFormat
import net.hedtech.banner.general.system.EntriesForSqlProcesss
import net.hedtech.banner.general.system.SqlProcessParameter
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException

class SqlProcessParameterByProcessIntegrationTests extends BaseIntegrationTestCase {

	//Test data for creating new domain instance
	//Valid test data (For success tests)
	def i_success_entriesForSqlProcesss
	def i_success_sqlProcessParameter

	def i_success_systemRequiredIndicator = true
	//Invalid test data (For failure tests)
	def i_failure_entriesForSqlProcesss
	def i_failure_sqlProcessParameter

	def i_failure_systemRequiredIndicator

	//Test data for creating updating domain instance
	//Valid test data (For success tests)
	def u_success_entriesForSqlProcesss
	def u_success_sqlProcessParameter

	def u_success_systemRequiredIndicator = false
	//Valid test data (For failure tests)
	def u_failure_entriesForSqlProcesss
	def u_failure_sqlProcessParameter

	def u_failure_systemRequiredIndicator


	protected void setUp() {
		formContext = ['GUAGMNU']
		super.setUp()
		initializeTestDataForReferences()
	}

	//This method is used to initialize test data for references.
	//A method is required to execute database calls as it requires a active transaction
	void initializeTestDataForReferences() {
        //Valid test data (For success tests)
        def entriesForSqlProcesss = new EntriesForSqlProcesss(code: 'INTEGRATION_TEST', description: 'INTEGRATION_TEST', startDate: new Date(), endDate: new Date() + 1, systemRequiredIndicator: false)
        entriesForSqlProcesss.save(failOnError: true, flush: true)
		
        def sqlProcessParameter = new SqlProcessParameter(code: 'INTEGRATION_TEST_PARAM', description: 'INTEGRATION_TEST_PARAM', dataType: 'C', startDate: new Date(), endDate: new Date() + 1)
        sqlProcessParameter.save(failOnError: true, flush: true)

        i_success_entriesForSqlProcesss = entriesForSqlProcesss
        i_success_sqlProcessParameter = sqlProcessParameter

        //Invalid test data (For failure tests)
        i_failure_entriesForSqlProcesss = entriesForSqlProcesss
        i_failure_sqlProcessParameter = sqlProcessParameter
	}


	protected void tearDown() {
		super.tearDown()
	}


	void testCreateValidSqlProcessParameterByProcess() {
		def sqlProcessParameterByProcess = newValidForCreateSqlProcessParameterByProcess()
		sqlProcessParameterByProcess.save( failOnError: true, flush: true )
		//Test if the generated entity now has an id assigned
        assertNotNull sqlProcessParameterByProcess.id
	}


	void testCreateInvalidSqlProcessParameterByProcess() {
		def sqlProcessParameterByProcess = newInvalidForCreateSqlProcessParameterByProcess()
		shouldFail(ValidationException) {
            sqlProcessParameterByProcess.save( failOnError: true, flush: true )
		}
	}


	void testUpdateValidSqlProcessParameterByProcess() {
		def sqlProcessParameterByProcess = newValidForCreateSqlProcessParameterByProcess()
		sqlProcessParameterByProcess.save( failOnError: true, flush: true )
        assertNotNull sqlProcessParameterByProcess.id
        assertEquals 0L, sqlProcessParameterByProcess.version
        assertEquals i_success_systemRequiredIndicator, sqlProcessParameterByProcess.systemRequiredIndicator

		//Update the entity
		sqlProcessParameterByProcess.systemRequiredIndicator = u_success_systemRequiredIndicator

		sqlProcessParameterByProcess.save( failOnError: true, flush: true )
		//Assert for sucessful update
        sqlProcessParameterByProcess = SqlProcessParameterByProcess.get( sqlProcessParameterByProcess.id )
        assertEquals 1L, sqlProcessParameterByProcess?.version
        assertEquals u_success_systemRequiredIndicator, sqlProcessParameterByProcess.systemRequiredIndicator

	}


	void testUpdateInvalidSqlProcessParameterByProcess() {
		def sqlProcessParameterByProcess = newValidForCreateSqlProcessParameterByProcess()
		sqlProcessParameterByProcess.save( failOnError: true, flush: true )
        assertNotNull sqlProcessParameterByProcess.id
        assertEquals 0L, sqlProcessParameterByProcess.version
        assertEquals i_success_systemRequiredIndicator, sqlProcessParameterByProcess.systemRequiredIndicator

		//Update the entity with invalid values
		sqlProcessParameterByProcess.systemRequiredIndicator = u_failure_systemRequiredIndicator

		shouldFail(ValidationException) {
            sqlProcessParameterByProcess.save( failOnError: true, flush: true )
		}
	}


    void testDates() {
        def time = new SimpleDateFormat('HHmmss')
        def hour = new SimpleDateFormat('HH')
        def date = new SimpleDateFormat('yyyy-M-d')
        def today = new Date()

    	def sqlProcessParameterByProcess = newValidForCreateSqlProcessParameterByProcess()



    	sqlProcessParameterByProcess.save(flush: true, failOnError: true)
    	sqlProcessParameterByProcess.refresh()
    	assertNotNull "SqlProcessParameterByProcess should have been saved", sqlProcessParameterByProcess.id

    	// test date values -
    	assertEquals date.format(today), date.format(sqlProcessParameterByProcess.lastModified)
    	assertEquals hour.format(today), hour.format(sqlProcessParameterByProcess.lastModified)


    }


    void testOptimisticLock() {
		def sqlProcessParameterByProcess = newValidForCreateSqlProcessParameterByProcess()
		sqlProcessParameterByProcess.save( failOnError: true, flush: true )

        def sql
        try {
            sql = new Sql( sessionFactory.getCurrentSession().connection() )
            sql.executeUpdate( "update GORSQPA set GORSQPA_VERSION = 999 where GORSQPA_SURROGATE_ID = ?", [ sqlProcessParameterByProcess.id ] )
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
		//Try to update the entity
		//Update the entity
		sqlProcessParameterByProcess.systemRequiredIndicator = u_success_systemRequiredIndicator
        shouldFail( HibernateOptimisticLockingFailureException ) {
            sqlProcessParameterByProcess.save( failOnError: true, flush: true )
        }
    }


	void testDeleteSqlProcessParameterByProcess() {
		def sqlProcessParameterByProcess = newValidForCreateSqlProcessParameterByProcess()
		sqlProcessParameterByProcess.save( failOnError: true, flush: true )
		def id = sqlProcessParameterByProcess.id
		assertNotNull id
		sqlProcessParameterByProcess.delete()
		assertNull SqlProcessParameterByProcess.get( id )
	}


    void testValidation() {
       def sqlProcessParameterByProcess = newInvalidForCreateSqlProcessParameterByProcess()
       assertFalse "SqlProcessParameterByProcess could not be validated as expected due to ${sqlProcessParameterByProcess.errors}", sqlProcessParameterByProcess.validate()
    }


    void testNullValidationFailure() {
        def sqlProcessParameterByProcess = new SqlProcessParameterByProcess()
        assertFalse "SqlProcessParameterByProcess should have failed validation", sqlProcessParameterByProcess.validate()
        assertErrorsFor sqlProcessParameterByProcess, 'nullable',
                                               [
                                                 'systemRequiredIndicator',
                                                 'entriesForSqlProcesss',
                                                 'sqlProcessParameter'
                                               ]
    }


	private def newValidForCreateSqlProcessParameterByProcess() {
		def sqlProcessParameterByProcess = new SqlProcessParameterByProcess(
			systemRequiredIndicator: i_success_systemRequiredIndicator,
			entriesForSqlProcesss: i_success_entriesForSqlProcesss,
			sqlProcessParameter: i_success_sqlProcessParameter,
		)
		return sqlProcessParameterByProcess
	}


	private def newInvalidForCreateSqlProcessParameterByProcess() {
		def sqlProcessParameterByProcess = new SqlProcessParameterByProcess(
			systemRequiredIndicator: i_failure_systemRequiredIndicator,
			entriesForSqlProcesss: i_failure_entriesForSqlProcesss,
			sqlProcessParameter: i_failure_sqlProcessParameter,
		)
		return sqlProcessParameterByProcess
	}

}
