/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.junit.Before
import org.junit.Test
import org.junit.After

import grails.validation.ValidationException
import groovy.sql.Sql
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.general.system.Document
import net.hedtech.banner.general.system.VisaSource
import net.hedtech.banner.general.system.VisaType
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.springframework.orm.hibernate5.HibernateOptimisticLockingFailureException
import static groovy.test.GroovyAssert.*
import java.text.SimpleDateFormat

@Integration
@Rollback
class DocumentInformationIntegrationTests extends BaseIntegrationTestCase {

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
    void testCreateValidDocumentInformation() {
        def documentInformation = newValidForCreateDocumentInformation()
        documentInformation.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull documentInformation.id
    }


    @Test
    void testCreateInvalidDocumentInformation() {
        def documentInformation = newInvalidForCreateDocumentInformation()
        shouldFail(ValidationException) {
            documentInformation.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testUpdateValidDocumentInformation() {
        def documentInformation = newValidForCreateDocumentInformation()
        documentInformation.save(failOnError: true, flush: true)
        assertNotNull documentInformation.id
        assertEquals 0L, documentInformation.version
        assertEquals PersonUtility.getPerson("HOR000001").pidm, documentInformation.pidm
        assertEquals 1, documentInformation.sequenceNumber
        assertEquals "123456789012345678", documentInformation.visaNumber
        assertEquals "P", documentInformation.disposition
        assertEquals "VISA", documentInformation.document.code
        assertEquals "IRS", documentInformation.visaSource.code
        assertEquals "F1", documentInformation.visaType.code

        //Update the entity
        documentInformation.visaNumber = "123456789012UPDATE"
        documentInformation.disposition = "A"
        documentInformation.visaSource = VisaSource.findByCode("INS")
        documentInformation.save(failOnError: true, flush: true)

        //Assert for sucessful update
        documentInformation = DocumentInformation.get(documentInformation.id)
        assertEquals 1L, documentInformation?.version
        assertEquals "123456789012UPDATE", documentInformation.visaNumber
        assertEquals "A", documentInformation.disposition
        assertEquals "INS", documentInformation.visaSource.code
    }


    @Test
    void testUpdateInvalidDocumentInformation() {
        def documentInformation = newValidForCreateDocumentInformation()
        documentInformation.save(failOnError: true, flush: true)
        assertNotNull documentInformation.id
        assertEquals 0L, documentInformation.version
        assertEquals PersonUtility.getPerson("HOR000001").pidm, documentInformation.pidm
        assertEquals 1, documentInformation.sequenceNumber
        assertEquals "123456789012345678", documentInformation.visaNumber
        assertEquals "P", documentInformation.disposition
        assertEquals "VISA", documentInformation.document.code
        assertEquals "IRS", documentInformation.visaSource.code
        assertEquals "F1", documentInformation.visaType.code

        //Update the entity with invalid values
        documentInformation.visaNumber = "123456789012345678Z"
        shouldFail(ValidationException) {
            documentInformation.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testDates() {
        def time = new SimpleDateFormat('HHmmss')
        def hour = new SimpleDateFormat('HH')
        def date = new SimpleDateFormat('yyyy-M-d')
        def today = new Date()

        def documentInformation = newValidForCreateDocumentInformation()

        documentInformation.requestDate = new Date()
        documentInformation.receivedDate = new Date()

        documentInformation.save(flush: true, failOnError: true)
        documentInformation.refresh()
        assertNotNull "DocumentInformation should have been saved", documentInformation.id

        // test date values -
        assertEquals date.format(today), date.format(documentInformation.lastModified)
        assertEquals hour.format(today), hour.format(documentInformation.lastModified)

        assertEquals time.format(documentInformation.requestDate), "000000"
        assertEquals time.format(documentInformation.receivedDate), "000000"
    }


    @Test
    void testOptimisticLock() {
        def documentInformation = newValidForCreateDocumentInformation()
        documentInformation.save(failOnError: true, flush: true)

        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("update GORDOCM set GORDOCM_VERSION = 999 where GORDOCM_SURROGATE_ID = ?", [documentInformation.id])
        } finally {
//            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
        //Try to update the entity
        //Update the entity
        documentInformation.visaNumber = "123456789012UPDATE"
        shouldFail(HibernateOptimisticLockingFailureException) {
            documentInformation.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testDeleteDocumentInformation() {
        def documentInformation = newValidForCreateDocumentInformation()
        documentInformation.save(failOnError: true, flush: true)
        def id = documentInformation.id
        assertNotNull id
        documentInformation.delete()
        assertNull DocumentInformation.get(id)
    }


    @Test
    void testValidation() {
        def documentInformation = newInvalidForCreateDocumentInformation()
        assertFalse "DocumentInformation could not be validated as expected due to ${documentInformation.errors}", documentInformation.validate()
    }


    @Test
    void testNullValidationFailure() {
        def documentInformation = new DocumentInformation()
        assertFalse "DocumentInformation should have failed validation", documentInformation.validate()
        assertErrorsFor documentInformation, 'nullable',
                [
                        'pidm',
                        'sequenceNumber',
                        'disposition',
                        'document',
                        'visaType'
                ]
        assertNoErrorsFor documentInformation,
                [
                        'visaNumber',
                        'requestDate',
                        'receivedDate',
                        'visaSource'
                ]
    }


    @Test
    void testMaxSizeValidationFailures() {
        def documentInformation = new DocumentInformation(
                visaNumber: 'XXXXXXXXXXXXXXXXXXXX')
        assertFalse "DocumentInformation should have failed validation", documentInformation.validate()
        assertErrorsFor documentInformation, 'maxSize', ['visaNumber']
    }


    private def newValidForCreateDocumentInformation() {
        def documentInformation = new DocumentInformation(
                pidm: PersonUtility.getPerson("HOR000001").pidm,
                sequenceNumber: 1,
                visaNumber: "123456789012345678",
                disposition: "P",
                requestDate: new Date(),
                receivedDate: new Date(),
                document: Document.findByCode("VISA"),
                visaSource: VisaSource.findByCode("IRS"),
                visaType: VisaType.findByCode("F1"),
        )
        return documentInformation
    }


    private def newInvalidForCreateDocumentInformation() {
        def documentInformation = new DocumentInformation()
        return documentInformation
    }
}
