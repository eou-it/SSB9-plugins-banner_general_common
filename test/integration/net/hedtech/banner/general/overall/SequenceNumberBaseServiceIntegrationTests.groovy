/** *******************************************************************************
 Copyright 2009-2012 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of
 SunGard Higher Education and its subsidiaries. Any use of this software is limited
 solely to SunGard Higher Education licensees, and is further subject to the terms
 and conditions of one or more written license agreements between SunGard Higher
 Education and the licensee in question. SunGard is either a registered trademark or
 trademark of SunGard Data Systems in the U.S.A. and/or other regions and/or countries.
 Banner and Luminis are either registered trademarks or trademarks of SunGard Higher
 Education in the U.S.A. and/or other regions and/or countries.
 ********************************************************************************* */

/**
 Banner Automator Version: 1.24
 Generated: Tue Aug 09 14:09:56 IST 2011
 */
package net.hedtech.banner.general.overall

import net.hedtech.banner.testing.BaseIntegrationTestCase
import net.hedtech.banner.exceptions.ApplicationException
import org.junit.Test



class SequenceNumberBaseServiceIntegrationTests extends BaseIntegrationTestCase {

    def sequenceNumberBaseService

    /*PROTECTED REGION ID(sequencenumberbase_service_integration_test_data) ENABLED START*/
    //Test data for creating new domain instance
    //Valid test data (For success tests)
    def i_success_function = "TTTTT"
    def i_success_sequenceNumberPrefix = "#"
    def i_success_maximumSequenceNumber = 1

    //Invalid test data (For failure tests)
    def i_failure_function = "TTTTT"
    def i_failure_sequenceNumberPrefix = "##"
    def i_failure_maximumSequenceNumber = 1

    //Test data for creating updating domain instance
    //Valid test data (For success tests)
    def u_success_function = "UUUUU"
    def u_success_sequenceNumberPrefix = "A"
    def u_success_maximumSequenceNumber = 2

    //Valid test data (For failure tests)
    def u_failure_function = "TTTTT"
    def u_failure_sequenceNumberPrefix = "##"
    def u_failure_maximumSequenceNumber = 1

    //keyblock map may be null for these tests
    def i_success_keyBlockMap = [:]
    def i_failure_keyBlockMap = [:]
    def u_success_keyBlockMap = [:]
    def u_failure_keyBlockMap = [:]

    /*PROTECTED REGION END*/


    protected void setUp() {
        formContext = ['SLQEVNT']
        super.setUp()
    }

    //This method is used to initialize test data for references.
    //A method is required to execute database calls as it requires a active transaction
    void initializeTestDataForReferences() {
        /*PROTECTED REGION ID(sequencenumberbase_domain_service_integration_test_data_initialization) ENABLED START*/
        //Valid test data (For success tests)

        //Invalid test data (For failure tests)

        //Valid test data (For success tests)

        //Valid test data (For failure tests)

        //Test data for references for custom tests
        /*PROTECTED REGION END*/
    }


    protected void tearDown() {
        super.tearDown()
    }


    void testSequenceNumberBaseValidCreate() {
        def sequenceNumberBase = newValidForCreateSequenceNumberBase()
        def map = [keyBlock: i_success_keyBlockMap,
                domainModel: sequenceNumberBase]
        sequenceNumberBase = sequenceNumberBaseService.create(map)
        assertNotNull "SequenceNumberBase ID is null in SequenceNumberBase Service Tests Create", sequenceNumberBase.id
        assertNotNull sequenceNumberBase.version
        assertNotNull sequenceNumberBase.dataOrigin
        assertNotNull sequenceNumberBase.lastModifiedBy
        assertNotNull sequenceNumberBase.lastModified
    }


    void testSequenceNumberBaseInvalidCreate() {
        def sequenceNumberBase = newInvalidForCreateSequenceNumberBase()
        def map = [keyBlock: i_failure_keyBlockMap,
                domainModel: sequenceNumberBase]
        shouldFail(ApplicationException) {
            sequenceNumberBaseService.create(map)
        }
    }


    void testSequenceNumberBaseValidUpdate() {
        def sequenceNumberBase = newValidForCreateSequenceNumberBase()
        def map = [keyBlock: i_success_keyBlockMap,
                domainModel: sequenceNumberBase]
        sequenceNumberBase = sequenceNumberBaseService.create(map)
        assertNotNull "SequenceNumberBase ID is null in SequenceNumberBase Service Tests Create", sequenceNumberBase.id
        assertNotNull sequenceNumberBase.version
        assertNotNull sequenceNumberBase.dataOrigin
        assertNotNull sequenceNumberBase.lastModifiedBy
        assertNotNull sequenceNumberBase.lastModified
        //Update the entity with new values
        sequenceNumberBase.sequenceNumberPrefix = u_success_sequenceNumberPrefix
        sequenceNumberBase.maximumSequenceNumber = u_success_maximumSequenceNumber

        map.keyBlock = u_success_keyBlockMap
        map.domainModel = sequenceNumberBase
        sequenceNumberBase = sequenceNumberBaseService.update(map)
        // test the values
        assertEquals u_success_sequenceNumberPrefix, sequenceNumberBase.sequenceNumberPrefix
        assertEquals u_success_maximumSequenceNumber, sequenceNumberBase.maximumSequenceNumber
    }


    void testSequenceNumberBaseInvalidUpdate() {
        def sequenceNumberBase = newValidForCreateSequenceNumberBase()
        def map = [keyBlock: i_success_keyBlockMap,
                domainModel: sequenceNumberBase]
        sequenceNumberBase = sequenceNumberBaseService.create(map)
        assertNotNull "SequenceNumberBase ID is null in SequenceNumberBase Service Tests Create", sequenceNumberBase.id
        assertNotNull sequenceNumberBase.version
        assertNotNull sequenceNumberBase.dataOrigin
        assertNotNull sequenceNumberBase.lastModifiedBy
        assertNotNull sequenceNumberBase.lastModified
        //Update the entity with new invalid values
        sequenceNumberBase.sequenceNumberPrefix = u_failure_sequenceNumberPrefix
        sequenceNumberBase.maximumSequenceNumber = u_failure_maximumSequenceNumber

        map.keyBlock = u_failure_keyBlockMap
        map.domainModel = sequenceNumberBase
        shouldFail(ApplicationException) {
            sequenceNumberBase = sequenceNumberBaseService.update(map)
        }
    }


    void testSequenceNumberBaseDelete() {
        def sequenceNumberBase = newValidForCreateSequenceNumberBase()
        def map = [keyBlock: i_success_keyBlockMap,
                domainModel: sequenceNumberBase]
        sequenceNumberBase = sequenceNumberBaseService.create(map)
        assertNotNull "SequenceNumberBase ID is null in SequenceNumberBase Service Tests Create", sequenceNumberBase.id
        def id = sequenceNumberBase.id
        map = [keyBlock: i_success_keyBlockMap,
                domainModel: sequenceNumberBase]
        sequenceNumberBaseService.delete(map)
        assertNull "SequenceNumberBase should have been deleted", sequenceNumberBase.get(id)
    }


    void testReadOnly() {
        def sequenceNumberBase = newValidForCreateSequenceNumberBase()
        def map = [keyBlock: i_success_keyBlockMap,
                domainModel: sequenceNumberBase]
        sequenceNumberBase = sequenceNumberBaseService.create(map)
        assertNotNull "SequenceNumberBase ID is null in SequenceNumberBase Service Tests Create", sequenceNumberBase.id
        map = [keyBlock: i_success_keyBlockMap,
                domainModel: sequenceNumberBase]
        map.domainModel.function = u_success_function
        try {
            sequenceNumberBaseService.update([domainModel: sequenceNumberBase])
            fail("This should have failed with @@r1:readonlyFieldsCannotBeModified")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "readonlyFieldsCannotBeModified"
        }
    }


    private def newValidForCreateSequenceNumberBase() {
        def sequenceNumberBase = new SequenceNumberBase(
                function: i_success_function,
                sequenceNumberPrefix: i_success_sequenceNumberPrefix,
                maximumSequenceNumber: i_success_maximumSequenceNumber
        )
        return sequenceNumberBase
    }


    private def newInvalidForCreateSequenceNumberBase() {
        def sequenceNumberBase = new SequenceNumberBase(
                function: i_failure_function,
                sequenceNumberPrefix: i_failure_sequenceNumberPrefix,
                maximumSequenceNumber: i_failure_maximumSequenceNumber
        )
        return sequenceNumberBase
    }

    /**
     * Please put all the custom service tests in this protected section to protect the code
     * from being overwritten on re-generation
     */
    /*PROTECTED REGION ID(sequencenumberbase_custom_service_integration_test_methods) ENABLED START*/


    @Test
    void testGetNextSequenceNumberBase() {
        String function = "TEST"
        char sequenceNumberPrefix = 'A'
        Integer maximumSequence = 9

        def sequenceNumberBase = new SequenceNumberBase(function: function, sequenceNumberPrefix: sequenceNumberPrefix, maximumSequenceNumber: 0)
        sequenceNumberBase = sequenceNumberBaseService.create([domainModel: sequenceNumberBase])

        //case: maximum digits allowed is one, 1 -> 9
        //case a new sequence is starting from zero for prefix A
        def previousNumber = 0
        String expectedSequence = "${sequenceNumberPrefix}" + (++previousNumber) //A1
        assertEquals expectedSequence, sequenceNumberBaseService.getNextSequenceNumberBase(function, maximumSequence)
        expectedSequence = "${sequenceNumberPrefix}" + (++previousNumber)   //A2
        assertEquals expectedSequence, sequenceNumberBaseService.getNextSequenceNumberBase(function, maximumSequence)

        //the last sequence number is reached, the next call should begin with new prefix and sequence of 1
        sequenceNumberBase = SequenceNumberBase.findByFunction(function)
        sequenceNumberBase.maximumSequenceNumber = previousNumber = maximumSequence
        sequenceNumberBase = sequenceNumberBaseService.update([domainModel: sequenceNumberBase])

        previousNumber = 0
        sequenceNumberPrefix = (char)(sequenceNumberPrefix + 1) //B
        assertEquals "B", sequenceNumberPrefix
        expectedSequence = "${sequenceNumberPrefix}" + (++previousNumber) //B1
        assertEquals expectedSequence, sequenceNumberBaseService.getNextSequenceNumberBase(function, maximumSequence)
        expectedSequence = "${sequenceNumberPrefix}" + (++previousNumber) //B2
        assertEquals expectedSequence, sequenceNumberBaseService.getNextSequenceNumberBase(function, maximumSequence)

        //case: maximum digits allowed is two, 01 -> 99
        maximumSequence = 99

        expectedSequence = "${sequenceNumberPrefix}0" + (++previousNumber) //B03
        assertEquals expectedSequence, sequenceNumberBaseService.getNextSequenceNumberBase(function, maximumSequence)
        expectedSequence = "${sequenceNumberPrefix}0" + (++previousNumber) //B04
        assertEquals expectedSequence, sequenceNumberBaseService.getNextSequenceNumberBase(function, maximumSequence)

        //the last sequence number is reached, the next call should begin with new prefix and sequence of 01
        sequenceNumberBase = SequenceNumberBase.findByFunction(function)
        sequenceNumberBase.maximumSequenceNumber = previousNumber = maximumSequence
        sequenceNumberBase = sequenceNumberBaseService.update([domainModel: sequenceNumberBase])

        previousNumber = 0
        sequenceNumberPrefix = (char)(sequenceNumberPrefix + 1) //C
        assertEquals "C", sequenceNumberPrefix
        expectedSequence = "${sequenceNumberPrefix}0" + (++previousNumber)   //C01
        assertEquals expectedSequence, sequenceNumberBaseService.getNextSequenceNumberBase(function, maximumSequence)
        expectedSequence = "${sequenceNumberPrefix}0" + (++previousNumber)   //C02
        assertEquals expectedSequence, sequenceNumberBaseService.getNextSequenceNumberBase(function, maximumSequence)

        //case: maximum digits allowed is three, 001 -> 999
        maximumSequence = 999
        sequenceNumberBase = SequenceNumberBase.findByFunction(function)
        sequenceNumberBase.maximumSequenceNumber = previousNumber = 0
        sequenceNumberBase = sequenceNumberBaseService.update([domainModel: sequenceNumberBase])

        //case a new sequence is starting from zero for prefix C
        previousNumber = 0
        expectedSequence = "${sequenceNumberPrefix}00" + (++previousNumber)   //C001
        assertEquals expectedSequence, sequenceNumberBaseService.getNextSequenceNumberBase(function, maximumSequence)
        expectedSequence = "${sequenceNumberPrefix}00" + (++previousNumber)   //C002
        assertEquals expectedSequence, sequenceNumberBaseService.getNextSequenceNumberBase(function, maximumSequence)

        sequenceNumberBase = SequenceNumberBase.findByFunction(function)
        sequenceNumberBase.maximumSequenceNumber = previousNumber = 9
        sequenceNumberBase = sequenceNumberBaseService.update([domainModel: sequenceNumberBase])
        expectedSequence = "${sequenceNumberPrefix}0" + (++previousNumber)   //C010
        assertEquals expectedSequence, sequenceNumberBaseService.getNextSequenceNumberBase(function, maximumSequence)
        expectedSequence = "${sequenceNumberPrefix}0" + (++previousNumber)   //C011
        assertEquals expectedSequence, sequenceNumberBaseService.getNextSequenceNumberBase(function, maximumSequence)

        sequenceNumberBase = SequenceNumberBase.findByFunction(function)
        sequenceNumberBase.maximumSequenceNumber = previousNumber = 99
        sequenceNumberBase = sequenceNumberBaseService.update([domainModel: sequenceNumberBase])
        expectedSequence = "${sequenceNumberPrefix}" + (++previousNumber)   //C100
        assertEquals expectedSequence, sequenceNumberBaseService.getNextSequenceNumberBase(function, maximumSequence)
        expectedSequence = "${sequenceNumberPrefix}" + (++previousNumber)   //C101
        assertEquals expectedSequence, sequenceNumberBaseService.getNextSequenceNumberBase(function, maximumSequence)

        //the last sequence number is reached, the next call should begin with new prefix and sequence of 01
        sequenceNumberBase = SequenceNumberBase.findByFunction(function)
        sequenceNumberBase.maximumSequenceNumber = previousNumber = maximumSequence
        sequenceNumberBase = sequenceNumberBaseService.update([domainModel: sequenceNumberBase])

        previousNumber = 0
        sequenceNumberPrefix = (char)(sequenceNumberPrefix + 1)     //D
        assertEquals "D", sequenceNumberPrefix
        expectedSequence = "${sequenceNumberPrefix}00" + (++previousNumber)   //D001
        assertEquals expectedSequence, sequenceNumberBaseService.getNextSequenceNumberBase(function, maximumSequence)
        expectedSequence = "${sequenceNumberPrefix}00" + (++previousNumber)   //D002
        assertEquals expectedSequence, sequenceNumberBaseService.getNextSequenceNumberBase(function, maximumSequence)

        //case: maximum digits allowed is four, 0001 -> 9999
        maximumSequence = 9999

        //case a new sequence is starting from zero for prefix C
        expectedSequence = "${sequenceNumberPrefix}000" + (++previousNumber)   //D0003
        assertEquals expectedSequence, sequenceNumberBaseService.getNextSequenceNumberBase(function, maximumSequence)
        expectedSequence = "${sequenceNumberPrefix}000" + (++previousNumber)   //D0004
        assertEquals expectedSequence, sequenceNumberBaseService.getNextSequenceNumberBase(function, maximumSequence)

        //the last sequence number is reached, the next call should begin with new prefix and sequence of 0001
        sequenceNumberBase = SequenceNumberBase.findByFunction(function)
        sequenceNumberBase.maximumSequenceNumber = previousNumber = maximumSequence
        sequenceNumberBase = sequenceNumberBaseService.update([domainModel: sequenceNumberBase])

        previousNumber = 0
        sequenceNumberPrefix = (char)(sequenceNumberPrefix + 1) //E
        assertEquals "E", sequenceNumberPrefix
        expectedSequence = "${sequenceNumberPrefix}000" + (++previousNumber)   //E0001
        assertEquals expectedSequence, sequenceNumberBaseService.getNextSequenceNumberBase(function, maximumSequence)
        expectedSequence = "${sequenceNumberPrefix}000" + (++previousNumber)   //E0002
        assertEquals expectedSequence, sequenceNumberBaseService.getNextSequenceNumberBase(function, maximumSequence)

        //case: maximum digits allowed is five, 00001 -> 99999
        maximumSequence = 99999

        //case a new sequence is starting from zero for prefix E
        expectedSequence = "${sequenceNumberPrefix}0000" + (++previousNumber)   //E00003
        assertEquals expectedSequence, sequenceNumberBaseService.getNextSequenceNumberBase(function, maximumSequence)
        expectedSequence = "${sequenceNumberPrefix}0000" + (++previousNumber)   //E00004
        assertEquals expectedSequence, sequenceNumberBaseService.getNextSequenceNumberBase(function, maximumSequence)

        //the last sequence number is reached, the next call should begin with new prefix and sequence of 0001
        sequenceNumberBase = SequenceNumberBase.findByFunction(function)
        sequenceNumberBase.maximumSequenceNumber = previousNumber = maximumSequence
        sequenceNumberBase = sequenceNumberBaseService.update([domainModel: sequenceNumberBase])

        previousNumber = 0
        sequenceNumberPrefix = (char)(sequenceNumberPrefix + 1) //F
        assertEquals "F", sequenceNumberPrefix
        expectedSequence = "${sequenceNumberPrefix}0000" + (++previousNumber)   //F00001
        assertEquals expectedSequence, sequenceNumberBaseService.getNextSequenceNumberBase(function, maximumSequence)
        expectedSequence = "${sequenceNumberPrefix}0000" + (++previousNumber)   //F00002
        assertEquals expectedSequence, sequenceNumberBaseService.getNextSequenceNumberBase(function, maximumSequence)
    }

    /*PROTECTED REGION END*/
}
