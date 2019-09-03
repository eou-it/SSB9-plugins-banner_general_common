/*********************************************************************************
 Copyright 2010-2016 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.utility

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.After

import net.hedtech.banner.testing.BaseIntegrationTestCase
import groovy.sql.Sql
import org.springframework.orm.hibernate5.HibernateOptimisticLockingFailureException
import net.hedtech.banner.general.system.Term
import net.hedtech.banner.general.system.LetterProcessLetter
import net.hedtech.banner.general.system.Initials
import java.text.SimpleDateFormat
import grails.validation.ValidationException
import static groovy.test.GroovyAssert.*

@Integration
@Rollback
class MailIntegrationTests extends BaseIntegrationTestCase {

    //Valid test data (For success tests)
    def i_success_term
    def i_success_letterProcessLetter
    def i_success_initials
    def i_success_communicationPlan

    def i_success_pidm = 1
    def i_success_systemIndicator = "TT"
    def i_success_module = "#"
    def i_success_adminIdentifier = 1
    def i_success_materialMod = "RITK"
    def i_success_dateInitial = new Date()
    def i_success_datePrinted = new Date()
    def i_success_userData = "TTTTT"
    def i_success_waitDays = 1
    def i_success_publishedGenerated = "#"
    def i_success_originalIndicator = "U"
    def i_success_aidYear = "TTTT"
    def i_success_quantity = 1
    def i_success_miscellaneousVc2 = "TTTTT"
    def i_success_miscellaneousDate = new Date()
    def i_success_miscellaneousNumber = 1
    //Invalid test data (For failure tests)
    def i_failure_term
    def i_failure_letterProcessLetter
    def i_failure_initials
    def i_failure_communicationPlan

    def i_failure_pidm = 1
    def i_failure_systemIndicator = "TT"
    def i_failure_module = "#"
    def i_failure_adminIdentifier = 1
    def i_failure_materialMod = "TTTT"
    def i_failure_dateInitial = new Date()
    def i_failure_datePrinted = new Date()
    def i_failure_userData = "TTTTT"
    def i_failure_waitDays = 1
    def i_failure_publishedGenerated = "#"
    def i_failure_originalIndicator = "U"
    def i_failure_aidYear = "TTTT"
    def i_failure_quantity = 1
    def i_failure_miscellaneousVc2 = "TTTTT"
    def i_failure_miscellaneousDate = new Date()
    def i_failure_miscellaneousNumber = 1

    //Test data for creating updating domain instance
    //Valid test data (For success tests)
    def u_success_term
    def u_success_letterProcessLetter
    def u_success_initials
    def u_success_communicationPlan

    def u_success_pidm = 1
    def u_success_systemIndicator = "WW"
    def u_success_module = "#"
    def u_success_adminIdentifier = 1
    def u_success_materialMod = "ADPK"
    def u_success_dateInitial = new Date()
    def u_success_datePrinted = new Date()
    def u_success_userData = "TTTTT"
    def u_success_waitDays = 2
    def u_success_publishedGenerated = "#"
    def u_success_originalIndicator = "U"
    def u_success_aidYear = "TTTT"
    def u_success_quantity = 2
    def u_success_miscellaneousVc2 = "TTTTT"
    def u_success_miscellaneousDate = new Date()
    def u_success_miscellaneousNumber = 1
    //Valid test data (For failure tests)
    def u_failure_term
    def u_failure_letterProcessLetter
    def u_failure_initials
    def u_failure_communicationPlan

    def u_failure_pidm = 1
    def u_failure_systemIndicator = "TT"
    def u_failure_module = "#"
    def u_failure_adminIdentifier = 1
    def u_failure_materialMod = "TTTT"
    def u_failure_dateInitial = new Date()
    def u_failure_datePrinted = new Date()
    def u_failure_userData = "TTTTT"
    def u_failure_waitDays = 1
    def u_failure_publishedGenerated = "#"
    def u_failure_originalIndicator = "U"
    def u_failure_aidYear = "TTTT"
    def u_failure_quantity = 1
    def u_failure_miscellaneousVc2 = "TTTTT"
    def u_failure_miscellaneousDate = new Date()
    def u_failure_miscellaneousNumber = 1


    @Before
    public void setUp() {
        formContext = ['SELFSERVICE'] // Since we are not testing a controller, we need to explicitly set this
        super.setUp()
        initializeTestDataForReferences()
    }

    //This method is used to initialize test data for references.
    //A method is required to execute database calls as it requires a active transaction
    void initializeTestDataForReferences() {
        //Valid test data (For success tests)
        i_success_term = Term.findWhere(code: "200320")
        i_success_letterProcessLetter = LetterProcessLetter.findWhere(code: "E_GIFT_ACK")
        i_success_initials = Initials.findWhere(code: "RCS0")
        i_success_communicationPlan = "WWWW"

        //Invalid test data (For failure tests)
        i_failure_term = Term.findWhere(code: "200320")
        i_failure_letterProcessLetter = LetterProcessLetter.findWhere(code: "E_GIFT_ACK")
        i_failure_initials = Initials.findWhere(code: "RCS0")
        i_failure_communicationPlan = "WW"

        //Valid test data (For success tests)
        u_success_term = Term.findWhere(code: "200320")
        u_success_letterProcessLetter = LetterProcessLetter.findWhere(code: "E_GIFT_ACK")
        u_success_initials = Initials.findWhere(code: "RCS0")
        u_success_communicationPlan = "TT"

        //Valid test data (For failure tests)
        u_failure_term = Term.findWhere(code: "200320")
        u_failure_letterProcessLetter = LetterProcessLetter.findWhere(code: "E_GIFT_ACK")
        u_failure_initials = Initials.findWhere(code: "RCS0")
        u_failure_communicationPlan = "WW"
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testCreateValidMail() {
        def mail = newValidForCreateMail()
        mail.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull mail.id
        assertNotNull mail.dataOrigin
        assertNotNull mail.lastModified
        assertNotNull mail.lastModifiedBy
    }


    @Test
    void testCreateInvalidMail() {
        def mail = newInvalidForCreateMail()
        mail.pidm = null
        shouldFail(ValidationException) {
            mail.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testUpdateValidMail() {
        def mail = newValidForCreateMail()
        mail.save(failOnError: true, flush: true)
        assertNotNull mail.id
        assertEquals 0L, mail.version
        assertEquals i_success_pidm, mail.pidm
        assertEquals i_success_systemIndicator, mail.systemIndicator
        assertEquals i_success_module, mail.module
        assertEquals i_success_adminIdentifier, mail.adminIdentifier
        assertEquals i_success_materialMod, mail.materialMod
        assertEquals i_success_dateInitial, mail.dateInitial
        assertEquals i_success_datePrinted, mail.datePrinted
        assertEquals i_success_userData, mail.userData
        assertEquals i_success_waitDays, mail.waitDays
        assertEquals i_success_publishedGenerated, mail.publishedGenerated
        assertEquals i_success_originalIndicator, mail.originalIndicator
        assertEquals i_success_aidYear, mail.aidYear
        assertEquals i_success_quantity, mail.quantity
        assertEquals i_success_miscellaneousVc2, mail.miscellaneousVc2
        assertEquals i_success_miscellaneousDate, mail.miscellaneousDate
        assertEquals i_success_miscellaneousNumber, mail.miscellaneousNumber

        //Update the entity
        mail.pidm = u_success_pidm
        mail.systemIndicator = u_success_systemIndicator
        mail.module = u_success_module
        mail.adminIdentifier = u_success_adminIdentifier
        mail.materialMod = u_success_materialMod
        mail.dateInitial = u_success_dateInitial
        mail.datePrinted = u_success_datePrinted
        mail.userData = u_success_userData
        mail.waitDays = u_success_waitDays
        mail.publishedGenerated = u_success_publishedGenerated
        mail.originalIndicator = u_success_originalIndicator
        mail.aidYear = u_success_aidYear
        mail.quantity = u_success_quantity
        mail.miscellaneousVc2 = u_success_miscellaneousVc2
        mail.miscellaneousDate = u_success_miscellaneousDate
        mail.miscellaneousNumber = u_success_miscellaneousNumber


        mail.term = u_success_term

        mail.letterProcessLetter = u_success_letterProcessLetter

        mail.initials = u_success_initials

        mail.communicationPlan = u_success_communicationPlan
        mail.save(failOnError: true, flush: true)
        //Assert for sucessful update
        mail = Mail.get(mail.id)
        assertEquals 1L, mail?.version
        assertEquals u_success_pidm, mail.pidm
        assertEquals u_success_systemIndicator, mail.systemIndicator
        assertEquals u_success_module, mail.module
        assertEquals u_success_adminIdentifier, mail.adminIdentifier
        assertEquals u_success_materialMod, mail.materialMod
        assertEquals u_success_dateInitial, mail.dateInitial
        assertEquals u_success_datePrinted, mail.datePrinted
        assertEquals u_success_userData, mail.userData
        assertEquals u_success_waitDays, mail.waitDays
        assertEquals u_success_publishedGenerated, mail.publishedGenerated
        assertEquals u_success_originalIndicator, mail.originalIndicator
        assertEquals u_success_aidYear, mail.aidYear
        assertEquals u_success_quantity, mail.quantity
        assertEquals u_success_miscellaneousVc2, mail.miscellaneousVc2
        assertEquals u_success_miscellaneousDate, mail.miscellaneousDate
        assertEquals u_success_miscellaneousNumber, mail.miscellaneousNumber


        mail.term = u_success_term

        mail.letterProcessLetter = u_success_letterProcessLetter

        mail.initials = u_success_initials

        mail.communicationPlan = u_success_communicationPlan
    }


    @Test
    void testUpdateInvalidMail() {
        def mail = newValidForCreateMail()
        mail.save(failOnError: true, flush: true)
        assertNotNull mail.id
        assertEquals 0L, mail.version
        assertEquals i_success_pidm, mail.pidm
        assertEquals i_success_systemIndicator, mail.systemIndicator
        assertEquals i_success_module, mail.module
        assertEquals i_success_adminIdentifier, mail.adminIdentifier
        assertEquals i_success_materialMod, mail.materialMod
        assertEquals i_success_dateInitial, mail.dateInitial
        assertEquals i_success_datePrinted, mail.datePrinted
        assertEquals i_success_userData, mail.userData
        assertEquals i_success_waitDays, mail.waitDays
        assertEquals i_success_publishedGenerated, mail.publishedGenerated
        assertEquals i_success_originalIndicator, mail.originalIndicator
        assertEquals i_success_aidYear, mail.aidYear
        assertEquals i_success_quantity, mail.quantity
        assertEquals i_success_miscellaneousVc2, mail.miscellaneousVc2
        assertEquals i_success_miscellaneousDate, mail.miscellaneousDate
        assertEquals i_success_miscellaneousNumber, mail.miscellaneousNumber

        //Update the entity with invalid values
        mail.pidm = u_failure_pidm
        mail.systemIndicator = u_failure_systemIndicator
        mail.module = u_failure_module
        mail.adminIdentifier = u_failure_adminIdentifier
        mail.materialMod = u_failure_materialMod
        mail.dateInitial = u_failure_dateInitial
        mail.datePrinted = u_failure_datePrinted
        mail.userData = u_failure_userData
        mail.waitDays = u_failure_waitDays
        mail.publishedGenerated = u_failure_publishedGenerated
        mail.originalIndicator = u_failure_originalIndicator
        mail.aidYear = u_failure_aidYear
        mail.quantity = u_failure_quantity
        mail.miscellaneousVc2 = u_failure_miscellaneousVc2
        mail.miscellaneousDate = u_failure_miscellaneousDate
        mail.miscellaneousNumber = u_failure_miscellaneousNumber


        mail.term = u_failure_term

        mail.letterProcessLetter = u_failure_letterProcessLetter

        mail.initials = u_failure_initials

        mail.communicationPlan = u_failure_communicationPlan
        mail.pidm = null
        shouldFail(ValidationException) {
            mail.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testDates() {
        def time = new SimpleDateFormat('HHmmss')
        def hour = new SimpleDateFormat('HH')
        def date = new SimpleDateFormat('yyyy-M-d')
        def today = new Date()

        def mail = newValidForCreateMail()

        // TODO review the arbitrary use of "Date()" as a date value in the test below and choose better values

        mail.dateInitial = new Date()
        mail.datePrinted = new Date()
        mail.miscellaneousDate = new Date()

        mail.save(flush: true, failOnError: true)
        mail.refresh()
        assertNotNull "Mail should have been saved", mail.id

        // test date values -
        assertEquals date.format(today), date.format(mail.lastModified)
        assertEquals hour.format(today), hour.format(mail.lastModified)

        assertEquals time.format(mail.dateInitial), "000000"
        assertEquals time.format(mail.datePrinted), "000000"
        assertEquals time.format(mail.miscellaneousDate), "000000"

    }


    @Test
    void testOptimisticLock() {
        def mail = newValidForCreateMail()
        mail.save(failOnError: true, flush: true)

        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("update GURMAIL set GURMAIL_VERSION = 999 where GURMAIL_SURROGATE_ID = ?", [mail.id])
        } finally {
          //TODO grails3  sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
        //Try to update the entity
        //Update the entity
        mail.pidm = u_success_pidm
        mail.systemIndicator = u_success_systemIndicator
        mail.module = u_success_module
        mail.adminIdentifier = u_success_adminIdentifier
        mail.materialMod = u_success_materialMod
        mail.dateInitial = u_success_dateInitial
        mail.datePrinted = u_success_datePrinted
        mail.userData = u_success_userData
        mail.waitDays = u_success_waitDays
        mail.publishedGenerated = u_success_publishedGenerated
        mail.originalIndicator = u_success_originalIndicator
        mail.aidYear = u_success_aidYear
        mail.quantity = u_success_quantity
        mail.miscellaneousVc2 = u_success_miscellaneousVc2
        mail.miscellaneousDate = u_success_miscellaneousDate
        mail.miscellaneousNumber = u_success_miscellaneousNumber
        shouldFail(HibernateOptimisticLockingFailureException) {
            mail.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testDeleteMail() {
        def mail = newValidForCreateMail()
        mail.save(failOnError: true, flush: true)
        def id = mail.id
        assertNotNull id
        mail.delete()
        assertNull Mail.get(id)
    }


    @Test
    void testValidation() {
        def mail = newInvalidForCreateMail()
        mail.pidm = null
        assertFalse "Mail could not be validated as expected due to ${mail.errors}", mail.validate()
    }


    @Test
    void testNullValidationFailure() {
        def mail = new Mail()
        assertFalse "Mail should have failed validation", mail.validate()
        assertErrorsFor mail, 'nullable',
                [
                        'pidm',
                        'systemIndicator'
                ]
        assertNoErrorsFor mail,
                [
                        'module',
                        'adminIdentifier',
                        'materialMod',
                        'dateInitial',
                        'datePrinted',
                        'userData',
                        'waitDays',
                        'publishedGenerated',
                        'originalIndicator',
                        'aidYear',
                        'quantity',
                        'miscellaneousVc2',
                        'miscellaneousDate',
                        'miscellaneousNumber',
                        'term',
                        'letterProcessLetter',
                        'initials',
                        'communicationPlan'
                ]
    }


    @Test
    void testMaxSizeValidationFailures() {
        def mail = new Mail(
                systemIndicator: 'XXX',
                module: 'XXX',
                materialMod: 'XXXXXX',
                userData: 'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
                publishedGenerated: 'XXX',
                originalIndicator: 'XXX',
                aidYear: 'XXXXXX',
                miscellaneousVc2: 'XXXXXXXXXXXXXXXXX')
        assertFalse "Mail should have failed validation", mail.validate()
        assertErrorsFor mail, 'maxSize', ['systemIndicator', 'module', 'materialMod', 'userData', 'publishedGenerated', 'originalIndicator', 'aidYear', 'miscellaneousVc2']
    }


    @Test
    void testMaxValidationFailures() {
        def mail = new Mail(
                pidm: 999999991,
                adminIdentifier: 100,
                waitDays: 1000,
                quantity: 1000)
        assertFalse "Mail should have failed validation", mail.validate()
        assertErrorsFor mail, 'max', ['pidm', 'adminIdentifier', 'waitDays', 'quantity']
    }


    @Test
    void testMinValidationFailures() {
        def mail = new Mail(
                pidm: -999999991,
                adminIdentifier: -100,
                waitDays: -1000,
                quantity: -1000)
        assertFalse "Mail should have failed validation", mail.validate()
        assertErrorsFor mail, 'min', ['pidm', 'adminIdentifier', 'waitDays', 'quantity']
    }

    //TODO Platform 9.12 causes failure here; investigate
	@Ignore
    @Test
    void testValidationMessages() {
        def mail = newInvalidForCreateMail()
        mail.pidm = null
        assertFalse mail.validate()
        assertLocalizedError mail, 'nullable', /.*Field.*pidm.*of class.*Mail.*cannot be null.*/, 'pidm'
        mail.systemIndicator = null
        assertFalse mail.validate()
        assertLocalizedError mail, 'nullable', /.*Field.*systemIndicator.*of class.*Mail.*cannot be null.*/, 'systemIndicator'
    }


    private def newValidForCreateMail() {
        def mail = new Mail(
                pidm: i_success_pidm,
                systemIndicator: i_success_systemIndicator,
                module: i_success_module,
                adminIdentifier: i_success_adminIdentifier,
                materialMod: i_success_materialMod,
                dateInitial: i_success_dateInitial,
                datePrinted: i_success_datePrinted,
                userData: i_success_userData,
                waitDays: i_success_waitDays,
                publishedGenerated: i_success_publishedGenerated,
                originalIndicator: i_success_originalIndicator,
                aidYear: i_success_aidYear,
                quantity: i_success_quantity,
                miscellaneousVc2: i_success_miscellaneousVc2,
                miscellaneousDate: i_success_miscellaneousDate,
                miscellaneousNumber: i_success_miscellaneousNumber,
                term: i_success_term,
                letterProcessLetter: i_success_letterProcessLetter,
                initials: i_success_initials,
                communicationPlan: i_success_communicationPlan,
        )
        return mail
    }


    private def newInvalidForCreateMail() {
        def mail = new Mail(
                pidm: i_failure_pidm,
                systemIndicator: i_failure_systemIndicator,
                module: i_failure_module,
                adminIdentifier: i_failure_adminIdentifier,
                materialMod: i_failure_materialMod,
                dateInitial: i_failure_dateInitial,
                datePrinted: i_failure_datePrinted,
                userData: i_failure_userData,
                waitDays: i_failure_waitDays,
                publishedGenerated: i_failure_publishedGenerated,
                originalIndicator: i_failure_originalIndicator,
                aidYear: i_failure_aidYear,
                quantity: i_failure_quantity,
                miscellaneousVc2: i_failure_miscellaneousVc2,
                miscellaneousDate: i_failure_miscellaneousDate,
                miscellaneousNumber: i_failure_miscellaneousNumber,
                term: i_failure_term,
                letterProcessLetter: i_failure_letterProcessLetter,
                initials: i_failure_initials,
                communicationPlan: i_failure_communicationPlan,
        )
        return mail
    }
}
