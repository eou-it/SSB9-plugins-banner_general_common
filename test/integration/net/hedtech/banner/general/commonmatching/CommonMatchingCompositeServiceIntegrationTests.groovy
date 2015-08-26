/** *****************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */

package net.hedtech.banner.general.commonmatching

import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.person.*
import net.hedtech.banner.general.system.CommonMatchingSource
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

class CommonMatchingCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    def commonMatchingCompositeService


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
    void testCleanUp() {
        def sql = new Sql(sessionFactory.getCurrentSession().connection())

        def cnt = sql.executeUpdate("""insert into gotcmme ( gotcmme_entity_cde, gotcmme_last_name)
                          values ('P', 'Jamison')""")
        assertEquals 1, cnt
        def mcnt = sql.executeUpdate("""
        insert into gotcmrt (
                GOTCMRT_CMSC_CODE         ,
                GOTCMRT_CMSR_PRIORITY_NO   ,
                GOTCMRT_PIDM               ,
                GOTCMRT_RESULT_TYPE        ,
                GOTCMRT_MATCH_COUNT        ,
                GOTCMRT_MISSING_COUNT      ,
                GOTCMRT_UNMATCH_COUNT      ,
                GOTCMRT_MESSAGE            ,
                GOTCMRT_RESULT_IND)
        values ( 'HEDM_PERSON_MATCH', 1, 4515, 'M', 1,0,0,'Test', 'M')""")
        assertEquals 1, mcnt

        def fcnt = CommonMatchingResult.findAll()?.size()
        assertEquals 1, fcnt
        def fcnt2 = sql.rows("select count(*) cnt from gotcmme")[0].cnt
        assertEquals 1, fcnt2.toInteger()

        commonMatchingCompositeService.commonMatchingCleanup()

        sql = new Sql(sessionFactory.getCurrentSession().connection())
        fcnt = CommonMatchingResult.findAll()?.size()
        assertEquals 0, fcnt
        fcnt2 = sql.rows("select count(*) cnt from gotcmme")[0].cnt
        assertEquals 0, fcnt2.toInteger()

    }


    @Test
    void testLastNameSearch() {

        commonMatchingCompositeService.commonMatchingCleanup()
        def sourceCode = CommonMatchingSource.findByCode("HEDM_LASTNAME_MATCH")
        assertNotNull sourceCode
        def sources = CommonMatchingSourceRule.findAllByCommonMatchingSource(sourceCode)
        assertTrue sources.size() > 0
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def rules = sql.rows("select gorcmsr_column_name from gorcmsr where  GORCMSr_CMSC_CODE = ?", [sourceCode.code])
        assertTrue rules.size() > 0
        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPRIDEN_LAST_NAME" }

        def names = PersonIdentificationNameCurrent.findAllByLastName("Jamison")
        assertTrue names.size() > 0


        def person = PersonUtility.getPerson("HOS00001")
        assertNotNull person
        assertEquals "Emily", person.firstName
        assertEquals "Jamison", person.lastName
        /*
        [lastName : map.lastname, firstName: map.firstname, mi: params.mi,
                banner_id: map.bannerId, street1: map.street1,
                street2  : map.street2, street3: map.street3,
                city     : map.city, stat: map.state, zip: map.zip, natn: map.nation,
                cnty     : map.county, phone: map.phone, phone_number: map.phoneNumber,
                phone_ext: map.phoneExt,
                ssn      : map.ssn, day: map.birthDay, month: map.birthMonth,
                year     : map.birthYear, gender: map.sex, email: map.email, atype: map.addrType,
                tele     : map.teleType, email_type: map.emailType,
                asrc     : map.addressSource, addid_code: map.additionalIdCode, addid: map.additionalId,
                cmsc     : map.source]
         */
        def map = [source: "HEDM_LASTNAME_MATCH", lastName: "Jamison", firstName: "Emily"]

        def results = commonMatchingCompositeService.commonMatching(map)
        assertEquals results.personList?.size(), names.size()
        assertNotNull results.personList?.find { it.pidm == person.pidm }

    }


    @Test
    void testLastFirstEmailGenderSsnBdateSearch() {

        commonMatchingCompositeService.commonMatchingCleanup()
        def sourceCode = CommonMatchingSource.findByCode("HEDM_PERSON_MATCH")
        assertNotNull sourceCode
        def sources = CommonMatchingSourceRule.findAllByCommonMatchingSource(sourceCode)
        assertTrue sources.size() > 0
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def rules = sql.rows("select gorcmsr_column_name from gorcmsr where  GORCMSr_CMSC_CODE = ?", [sourceCode.code])
        assertTrue rules.size() > 0

        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPRIDEN_SEARCH_LAST_NAME" }
        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPRIDEN_SEARCH_MI" }
        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPRIDEN_SEARCH_FIRST_NAME" }
        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPBPERS_BIRTH_DATE" }
        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPBPERS_SEX" }
        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPBPERS_SSN" }
        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "GOREMAL_EMAIL_ADDRESS" }

        def person = PersonUtility.getPerson("HOS00001")
        assertNotNull person
        assertEquals "Emily", person.firstName
        assertEquals "Jamison", person.lastName
        def bio = PersonBasicPersonBase.fetchByPidm(person.pidm)
        assertNotNull bio
        bio.ssn = "000333377"
        assertNotNull bio.birthDate
        assertNotNull bio.sex
        bio.save(flush: true, failOnError: true)
        def email = PersonEmail.findByPidm(person.pidm)
        assertNotNull email?.emailAddress

        def map = [source: sourceCode.code, lastName: "Jamison", firstName: "Emily", email: email.emailAddress,
                   ssn   : bio.ssn, birthDay: bio.birthDate.format("dd"), birthMonth: bio.birthDate.format("MM"),
                   birthYear  : bio.birthDate.format("yyyy"),
                   sex   : bio.sex ]

        def results = commonMatchingCompositeService.commonMatching(map)
        assertEquals 1, results.personList.size()
        assertNotNull results.personList.find { it.pidm == person.pidm }

    }


    @Test
    void testLastFirstEmailGenderSsnBdateFailedSearchBecauseGenderDifferent() {

        commonMatchingCompositeService.commonMatchingCleanup()
        def sourceCode = CommonMatchingSource.findByCode("HEDM_PERSON_MATCH")
        assertNotNull sourceCode
        def sources = CommonMatchingSourceRule.findAllByCommonMatchingSource(sourceCode)
        assertTrue sources.size() > 0
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def rules = sql.rows("select gorcmsr_column_name from gorcmsr where  GORCMSr_CMSC_CODE = ?", [sourceCode.code])
        assertTrue rules.size() > 0

        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPRIDEN_SEARCH_LAST_NAME" }
        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPRIDEN_SEARCH_MI" }
        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPRIDEN_SEARCH_FIRST_NAME" }
        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPBPERS_BIRTH_DATE" }
        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPBPERS_SEX" }
        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPBPERS_SSN" }
        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "GOREMAL_EMAIL_ADDRESS" }

        def person = PersonUtility.getPerson("HOS00001")
        assertNotNull person
        assertEquals "Emily", person.firstName
        assertEquals "Jamison", person.lastName
        def bio = PersonBasicPersonBase.fetchByPidm(person.pidm)
        assertNotNull bio
        bio.ssn = "000333377"
        assertNotNull bio.birthDate
        assertNotNull bio.sex
        assertEquals "F", bio.sex
        bio.save(flush: true, failOnError: true)
        def email = PersonEmail.findByPidm(person.pidm)
        assertNotNull email?.emailAddress

        def map = [source: sourceCode.code, lastName: "Jamison", firstName: "Emily", email: email.emailAddress,
                   ssn   : bio.ssn, birthDay: bio.birthDate.format("dd"), birthMonth: bio.birthDate.format("MM"),
                   birthYear  : bio.birthDate.format("yyyy"),
                   sex   : "M" ]

        def results = commonMatchingCompositeService.commonMatching(map)
        assertEquals 0, results.personList.size()

    }


    @Test
    void testAllData() {

        commonMatchingCompositeService.commonMatchingCleanup()
        def sourceCode = CommonMatchingSource.findByCode("HEDM_MATCH_ALL")
        assertNotNull sourceCode
        def sources = CommonMatchingSourceRule.findAllByCommonMatchingSource(sourceCode)
        assertTrue sources.size() > 0
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def rules = sql.rows("select gorcmsr_column_name from gorcmsr where  GORCMSr_CMSC_CODE = ?", [sourceCode.code])
        assertTrue rules.size() > 0

        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPBPERS_BIRTH_DATE" }
        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPBPERS_SEX" }
        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPBPERS_SSN" }
        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "GOREMAL_EMAIL_ADDRESS" }
        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPBPERS_BIRTH_DATE" }
        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPBPERS_BIRTH_DAY" }
        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPBPERS_BIRTH_MON" }
        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPBPERS_BIRTH_YEAR" }
        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPBPERS_SEX" }
        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPBPERS_SSN" }
        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPRADDR_CITY" }
        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPRADDR_CNTY_CODE" }
        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPRADDR_NATN_CODE" }
        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPRADDR_STAT_CODE" }
        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPRADDR_STREET_LINE1" }
        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPRADDR_ZIP" }
        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPRIDEN_ID" }
        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPRIDEN_SEARCH_FIRST_NAME" }
        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPRIDEN_SEARCH_LAST_NAME" }
        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPRIDEN_SEARCH_MI" }
        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPRTELE_PHONE_AREA" }
        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPRTELE_PHONE_NUMBER" }
        assertNotNull rules.find { it.GORCMSR_COLUMN_NAME == "SPRTELE_PRIMARY_IND" }

        def person = PersonUtility.getPerson("HOSFE2020")
        assertNotNull person
        def bio = PersonBasicPersonBase.fetchByPidm(person.pidm)
        assertNotNull bio
        assertNotNull bio.ssn
        assertNotNull bio.birthDate
        assertNotNull bio.sex
        bio.save(flush: true, failOnError: true)
        def email = PersonEmail.findAllByPidm(person.pidm)
        assertTrue  email.size() > 0
        def address = PersonAddress.findByPidm(person.pidm)
        assertNotNull address
        def phone = PersonTelephone.findByPidm(person.pidm)
        assertNotNull phone


        def map = [source  : sourceCode.code, lastName: person.lastName, firstName: person.firstName, bannerId: person.bannerId,
                   mi: person.middleName,
                   ssn     : bio?.ssn, birthDay: bio?.birthDate?.format("dd"), birthMonth: bio?.birthDate?.format("MM"),
                   birthYear    : bio?.birthDate?.format("yyyy"),
                   sex     : bio?.sex, street1: address?.streetLine1, street2: address?.streetLine2,
                   street3 : address?.streetLine3, city: address?.city, state: address?.state?.code,
                   zip     : address?.zip, county: address?.county?.code, nation: address?.nation?.code,
                   phone   : phone?.phoneArea, phoneNumber: phone?.phoneNumber, phoneExtension: phone?.phoneExtension,
                   email   : email[0]?.emailAddress ]
        def results = commonMatchingCompositeService.commonMatching(map)
        // assertEquals 1, results.size()
        def matchResults = CommonMatchingResult.findAll()
        assertTrue matchResults.size() > 0
        assertEquals person.pidm, matchResults[0].pidm
    }


    @Test
    void testCMInvalidSource() {

        commonMatchingCompositeService.commonMatchingCleanup()
        def sourceCode = CommonMatchingSource.findByCode("HEDM_LASTMATCH")
        assertNull sourceCode
        def sources = CommonMatchingSourceRule.findAllByCommonMatchingSource(sourceCode)
        assertEquals 0, sources.size()


        def person = PersonUtility.getPerson("HOS00001")
        assertNotNull person
        assertEquals "Emily", person.firstName
        assertEquals "Jamison", person.lastName

        def map = [source: "HEDM_LASTMATCH", lastName: "Jamison", firstName: "Emily"]

        try {
            def results = commonMatchingCompositeService.commonMatching(map)
        }
        catch (ApplicationException ae) {
            assertApplicationException(ae, "invalid_source")
        }

    }


    @Test
    void testCMNoSource() {

        commonMatchingCompositeService.commonMatchingCleanup()
        def sourceCode = CommonMatchingSource.findByCode("HEDM_LASTMATCH")
        assertNull sourceCode
        def sources = CommonMatchingSourceRule.findAllByCommonMatchingSource(sourceCode)
        assertEquals 0, sources.size()


        def person = PersonUtility.getPerson("HOS00001")
        assertNotNull person
        assertEquals "Emily", person.firstName
        assertEquals "Jamison", person.lastName

        def map = [lastName: "Jamison", firstName: "Emily"]

        try {
            def results = commonMatchingCompositeService.commonMatching(map)
        }
        catch (ApplicationException ae) {
            assertApplicationException(ae, "invalid_source")
        }

    }


}
