/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall
import org.junit.Before
import org.junit.Test
import org.junit.After

import net.hedtech.banner.testing.BaseIntegrationTestCase
import grails.validation.ValidationException
import groovy.sql.Sql
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException
import net.hedtech.banner.general.system.DirectoryOption
import net.hedtech.banner.general.system.AddressType
import net.hedtech.banner.general.system.TelephoneType


class DirectoryAddressIntegrationTests extends BaseIntegrationTestCase {

    //Test data for creating new domain instance
    //Valid test data (For success tests)
    def i_success_directoryOption
    def i_success_addressType
    def i_success_telephoneType

    def i_success_priorityNumber = 1
    //Invalid test data (For failure tests)
    def i_failure_directoryOption
    def i_failure_addressType
    def i_failure_telephoneType

    def i_failure_priorityNumber = 1

    //Test data for creating updating domain instance
    //Valid test data (For success tests)
    def u_success_directoryOption
    def u_success_addressType
    def u_success_telephoneType

    def u_success_priorityNumber = 1
    //Valid test data (For failure tests)
    def u_failure_directoryOption
    def u_failure_addressType
    def u_failure_telephoneType

    def u_failure_priorityNumber = 1


	@Before
	public void setUp() {
        formContext = ['GUAGMNU'] // Since we are not testing a controller, we need to explicitly set this
        super.setUp()
        initializeTestDataForReferences()
    }


    //This method is used to initialize test data for references. 
    //A method is required to execute database calls as it requires a active transaction
    void initializeTestDataForReferences() {

        //Valid test data (For success tests)	
        i_success_directoryOption = DirectoryOption.findByCode("ADDR_HO")
        i_success_addressType = AddressType.findByCode("MA")
        i_success_telephoneType = TelephoneType.findByCode("")

        //Invalid test data (For failure tests)
        i_failure_directoryOption = DirectoryOption.findByCode("")
        i_failure_addressType = AddressType.findByCode("")
        i_failure_telephoneType = TelephoneType.findByCode("")

        //Valid test data (For success tests)
        u_success_directoryOption = DirectoryOption.findByCode("ADRR_HO")
        u_success_addressType = AddressType.findByCode("BU")
        u_success_telephoneType = TelephoneType.findByCode("")

        //Valid test data (For failure tests)
        u_failure_directoryOption = DirectoryOption.findByCode("")
        u_failure_addressType = AddressType.findByCode("")
        u_failure_telephoneType = TelephoneType.findByCode("")

        //Test data for references for custom tests
    }


	@After
	public void tearDown() {
        super.tearDown()
    }


	@Test
    void testCreateValidDirectoryAddress() {
        def directoryAddress = newValidForCreateDirectoryAddress()
        directoryAddress.priorityNumber = 1
        directoryAddress.save( failOnError: true, flush: true )
        //Test if the generated entity now has an id assigned		
        assertNotNull directoryAddress.id
    }


	@Test
    void testCreateInvalidDirectoryAddress() {
        def directoryAddress = newInvalidForCreateDirectoryAddress()
        directoryAddress.priorityNumber = null
        shouldFail(ValidationException) {
            directoryAddress.save( failOnError: true, flush: true )
        }
    }


	@Test
    void testUpdateValidDirectoryAddress() {
        def directoryAddress = newValidForCreateDirectoryAddress()
        directoryAddress.priorityNumber = 1
        directoryAddress.save( failOnError: true, flush: true )
        assertNotNull directoryAddress.id
        assertEquals 0L, directoryAddress.version
        assertEquals i_success_priorityNumber, directoryAddress.priorityNumber

        //Update the entity
        directoryAddress.addressType = u_success_addressType
        directoryAddress.save( failOnError: true, flush: true )

        //Assert for sucessful update
        directoryAddress = DirectoryAddress.get( directoryAddress.id )
        assertEquals 1L, directoryAddress?.version
        directoryAddress.addressType = u_success_addressType
        directoryAddress.telephoneType = u_success_telephoneType
    }


	@Test
    void testUpdateInvalidDirectoryAddress() {
        def directoryAddress = newValidForCreateDirectoryAddress()
        directoryAddress.priorityNumber = 1
        directoryAddress.save( failOnError: true, flush: true )
        assertNotNull directoryAddress.id
        assertEquals 0L, directoryAddress.version
        assertEquals i_success_priorityNumber, directoryAddress.priorityNumber

        //Update the entity with invalid values
        directoryAddress.directoryOption = null
        directoryAddress.priorityNumber = null

        shouldFail(ValidationException) {
            directoryAddress.save( failOnError: true, flush: true )
        }
    }


	@Test
    void testOptimisticLock() {
        def directoryAddress = newValidForCreateDirectoryAddress()
        directoryAddress.save( failOnError: true, flush: true )

        def sql
        try {
            sql = new Sql( sessionFactory.getCurrentSession().connection() )
            sql.executeUpdate( "update GORDADD set GORDADD_VERSION = 999 where GORDADD_SURROGATE_ID = ?", [ directoryAddress.id ] )
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
        //Try to update the entity
        //Update the entity
        directoryAddress.priorityNumber = 2
        shouldFail( HibernateOptimisticLockingFailureException ) {
            directoryAddress.save( failOnError: true, flush: true )
        }
    }


	@Test
    void testDeleteDirectoryAddress() {
        def directoryAddress = newValidForCreateDirectoryAddress()
        directoryAddress.save( failOnError: true, flush: true )
        def id = directoryAddress.id
        assertNotNull id
        directoryAddress.delete()
        assertNull DirectoryAddress.get( id )
    }


	@Test
    void testValidation() {
        def directoryAddress = newInvalidForCreateDirectoryAddress()
        directoryAddress.priorityNumber = 1234567890123
        assertFalse "DirectoryAddress could not be validated as expected due to ${directoryAddress.errors}", directoryAddress.validate()
    }


	@Test
    void testNullValidationFailure() {
        def directoryAddress = new DirectoryAddress()
        assertFalse "DirectoryAddress should have failed validation", directoryAddress.validate()
        assertErrorsFor directoryAddress, 'nullable',
                [
                        'priorityNumber',
                        'directoryOption'
                ]
        assertNoErrorsFor directoryAddress,
                [
                        'addressType',
                        'telephoneType'
                ]
    }


	@Test
    void testfetchByDirectoryOptionOrderByPriority(){
        def directoryAddress = newValidForCreateDirectoryAddress()
        directoryAddress.priorityNumber = 1
        directoryAddress.save( failOnError: true, flush: true )
        assertNotNull directoryAddress.id
        assertEquals i_success_priorityNumber, directoryAddress.priorityNumber

        def directoryAddress1 = newValidForCreateDirectoryAddress()
        directoryAddress1.priorityNumber = 2
        directoryAddress1.addressType = AddressType.findByCode("MA")
        directoryAddress1.save( failOnError: true, flush: true )
        assertNotNull directoryAddress1.id
        assertEquals 2, directoryAddress1.priorityNumber

        def address = DirectoryAddress.fetchByDirectoryOptionOrderByPriority("ADDR_OF")
        assertNotNull(address)
        assertEquals(address[0].priorityNumber,directoryAddress.priorityNumber)
    }


    private def newValidForCreateDirectoryAddress() {
        def directoryAddress = new DirectoryAddress(
                priorityNumber: i_success_priorityNumber,
                directoryOption: i_success_directoryOption,
                addressType: i_success_addressType,
                telephoneType: i_success_telephoneType,
        )
        return directoryAddress
    }


    private def newInvalidForCreateDirectoryAddress() {
        def directoryAddress = new DirectoryAddress(
                priorityNumber: i_failure_priorityNumber,
                directoryOption: i_failure_directoryOption,
                addressType: i_failure_addressType,
                telephoneType: i_failure_telephoneType,
        )
        return directoryAddress
    }

}
