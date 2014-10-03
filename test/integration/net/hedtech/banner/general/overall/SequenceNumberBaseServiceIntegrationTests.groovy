/*********************************************************************************
 Copyright 2010-2013 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall
import org.junit.Before
import org.junit.Test
import org.junit.After

import net.hedtech.banner.testing.BaseIntegrationTestCase
import net.hedtech.banner.exceptions.ApplicationException
import org.junit.Test



class SequenceNumberBaseServiceIntegrationTests extends BaseIntegrationTestCase {

    def sequenceNumberBaseService

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


	@Before
	public void setUp() {
        formContext = ['SCACRSE']
        super.setUp()
    }


	@After
	public void tearDown() {
        super.tearDown()
    }


	@Test
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


	@Test
    void testSequenceNumberBaseInvalidCreate() {
        def sequenceNumberBase = newInvalidForCreateSequenceNumberBase()
        def map = [keyBlock: i_failure_keyBlockMap,
                domainModel: sequenceNumberBase]
        shouldFail(ApplicationException) {
            sequenceNumberBaseService.create(map)
        }
    }


	@Test
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


	@Test
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


	@Test
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


	@Test
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
        sequenceNumberPrefix = (char) (sequenceNumberPrefix + 1) //B
        assertEquals "B", sequenceNumberPrefix.toString()
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
        sequenceNumberPrefix = (char) (sequenceNumberPrefix + 1) //C
        assertEquals "C", sequenceNumberPrefix.toString()
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
        sequenceNumberPrefix = (char) (sequenceNumberPrefix + 1)     //D
        assertEquals "D", sequenceNumberPrefix.toString()
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
        sequenceNumberPrefix = (char) (sequenceNumberPrefix + 1) //E
        assertEquals "E", sequenceNumberPrefix.toString()
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
        sequenceNumberPrefix = (char) (sequenceNumberPrefix + 1) //F
        assertEquals "F", sequenceNumberPrefix.toString()
        expectedSequence = "${sequenceNumberPrefix}0000" + (++previousNumber)   //F00001
        assertEquals expectedSequence, sequenceNumberBaseService.getNextSequenceNumberBase(function, maximumSequence)
        expectedSequence = "${sequenceNumberPrefix}0000" + (++previousNumber)   //F00002
        assertEquals expectedSequence, sequenceNumberBaseService.getNextSequenceNumberBase(function, maximumSequence)
    }
}
