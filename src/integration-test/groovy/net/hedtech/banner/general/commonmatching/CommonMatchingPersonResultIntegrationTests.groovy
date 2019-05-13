/*******************************************************************************
 Copyright 2015-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.commonmatching

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import groovy.sql.Sql
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.general.system.CommonMatchingSource
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.dao.InvalidDataAccessResourceUsageException
import static groovy.test.GroovyAssert.*

@Integration
@Rollback
class CommonMatchingPersonResultIntegrationTests extends BaseIntegrationTestCase {

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testCreateValidCommonMatchingPersonResult() {
        def CommonMatchingPersonResult = newValidCommonMatchingPersonResult()
        shouldFail(InvalidDataAccessResourceUsageException) {
            CommonMatchingPersonResult.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testUpdateResult() {
        newGeneratedResult()
        def personList = CommonMatchingPersonResult.findAll()
        assertEquals 1, personList.size()
        personList[0].message = "test"
        shouldFail(InvalidDataAccessResourceUsageException) {
            personList[0].save(failOnError: true, flush: true)
        }
    }


    @Test
    void testDeleteResult() {
        newGeneratedResult()
        def personList = CommonMatchingPersonResult.findAll()
        assertEquals 1, personList.size()
        assertNotNull personList[0].id
        shouldFail(InvalidDataAccessResourceUsageException) {
            personList[0].delete(failOnError: true, flush: true)
        }
    }


    @Test
    void testFetchMatchedResults() {
        newGeneratedResult()
        def personList = CommonMatchingPersonResult.findAll()
        assertEquals 1, personList.size()
        assertEquals "M", personList[0].resultIndicator

        def personMatchedList = CommonMatchingPersonResult.fetchAllMatchResults()
        assertEquals 1, personMatchedList.size()

        assertEquals "HOS00001", personMatchedList[0].bannerId
        assertEquals "M", personMatchedList[0].resultIndicator
    }


    @Test
    void testFetchResults() {
        newGeneratedAllResult()
        def personList = CommonMatchingPersonResult.findAll()
        assertTrue personList.size() > 0

        def personMatchedList = CommonMatchingPersonResult.fetchAllResults()
        assertTrue personMatchedList.size() > 0

    }


    @Test
    void testFetchMatchedResultsWithPagination() {
        newGeneratedLotsOfResults()
        def personList = CommonMatchingPersonResult.findAllByResultIndicator("M")
        assertTrue personList.size() > 10
        assertEquals "M", personList[0].resultIndicator

        def personMatchedList = CommonMatchingPersonResult.fetchAllMatchResults([max: 10, offset: 0])
        assertEquals 10, personMatchedList.size()

        def personMatchedList2 = CommonMatchingPersonResult.fetchAllMatchResults([max: 10, offset: 10])
        assertEquals 10, personMatchedList2.size()
    }


    @Test
    void testFetchResultsWithPagination() {
        newGeneratedAllResult()
        def personList = CommonMatchingPersonResult.findAll()
        assertTrue personList.size() > 1
        def max = personList.size()
        if ( personList.size() > 2 ) max = 2

        def personMatchedList = CommonMatchingPersonResult.fetchAllMatchResults([max: 2, offset: 0])
        assertEquals max, personMatchedList.size()

        def personMatchedList2 = CommonMatchingPersonResult.fetchAllMatchResults([max: 2, offset: 2])
        if ( personList.size() > 4) max = 2
        else max = personList.size() - 2
        assertEquals max, personMatchedList2.size()
    }


    private def newValidCommonMatchingPersonResult() {
        def result = new CommonMatchingPersonResult(
                id: "1",
                pidm: 11111,
                resultType: "M",
                message: "Testing",
                name: "Emily Jamison",
                bannerId: "HOS000001"
        )
        return result
    }


    private def newGeneratedResult() {
        def sourceCode = CommonMatchingSource.findByCode("HEDM_PERSON_MATCH")
        assertNotNull sourceCode
        def sources = CommonMatchingSourceRule.findAllByCommonMatchingSource(sourceCode)
        assertTrue sources.size() > 0

        def person = PersonUtility.getPerson("HOS00001")
        assertNotNull person
        assertEquals "Emily", person.firstName
        assertEquals "Jamison", person.lastName

        def result
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        try {
            sql.call("""
              declare
                cmsc  varchar2(100) := 'HEDM_PERSON_MATCH' ;
                lv_last_name varchar2(100) := ? ;
                lv_first_name varchar2(100) := ?;
                lv_mi varchar2(100);
                lv_email varchar2(100);
                lv_ssn  varchar2(100);
                lv_birth date;
                lv_banner_id varchar2(100);
                lv_gender varchar2(100);

                lv_day varchar2(100);
                lv_month varchar2(100);
                lv_year varchar2(100);

                resultind varchar2(100);
                new_pidm number;
                begin

                delete gotcmme;

                gokcmpk.p_insert_gotcmme (p_last_name     => lv_last_name,
                                              p_entity_cde    => 'P' ,
                                              p_first_name    => lv_first_name,
                                              p_mi            => lv_mi ,
                                              p_id            => lv_banner_id ,
                                              p_street_line1  => '',
                                              p_street_line2  => '',
                                              p_street_line3  => '',
                                              p_city          => '',
                                              p_stat_code    => '',
                                              p_zip          => '',
                                              p_natn_code    => '',
                                              p_cnty_code    => '',
                                              p_phone_area    => '',
                                              p_phone_number  => '',
                                              p_phone_ext     => '',
                                              p_ssn          => '',
                                              p_birth_day    => lv_day,
                                              p_birth_mon   =>  lv_month,
                                              p_birth_year  =>  lv_year,
                                              p_sex          => lv_gender,
                                              p_email_address => lv_email,
                                              p_atyp_code      => '',
                                              p_tele_code     => '',
                                              p_emal_code      => '',
                                              p_asrc_code      => '',
                                              p_addid_code   => '',
                                              p_addid         => '');

                gokcmpk.p_common_matching(p_cmsc_code => cmsc,
                      p_match_status_out  => resultind,
                      p_match_pidm_out   => new_pidm);

                end;
             """, [person.lastName, person.firstName]) { output_info ->
                result = output_info
            }
        }
        finally {
//            sql?.close()
        }

    }

    private def newGeneratedAllResult() {
        def sourceCode = CommonMatchingSource.findByCode("HEDM_PERSON_MATCH")
        assertNotNull sourceCode
        def sources = CommonMatchingSourceRule.findAllByCommonMatchingSource(sourceCode)
        assertTrue sources.size() > 0

        def result
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        try {
            sql.call("""
              declare
                cmsc  varchar2(100) := 'HEDM_PERSON_MATCH' ;
                lv_last_name varchar2(100) := ?;
                lv_first_name varchar2(100) := ?;

                lv_mi varchar2(100);
                lv_email varchar2(100);
                lv_ssn  varchar2(100);
                lv_birth date;
                lv_banner_id varchar2(100);
                lv_gender varchar2(100);

                lv_day varchar2(100);
                lv_month varchar2(100);
                lv_year varchar2(100);

                resultind varchar2(100);
                new_pidm number;
                begin

                delete gotcmme;

                gokcmpk.p_insert_gotcmme ( p_entity_cde    => 'P' ,
                                              p_first_name    => lv_first_name,
                                              p_last_name   => lv_last_name,
                                              p_mi            => lv_mi ,
                                              p_sex          => lv_gender );

                gokcmpk.p_common_matching(p_cmsc_code => cmsc,
                      p_match_status_out  => resultind,
                      p_match_pidm_out   => new_pidm);

                end;
             """, ["Lincoln", "Abraham"]) { output_info ->
                result = output_info
            }
        }
        finally {
//            sql?.close()
        }

    }

    private def newGeneratedLotsOfResults() {
        def sourceCode = CommonMatchingSource.findByCode("HEDM_LASTNAME_MATCH")
        assertNotNull sourceCode
        def sources = CommonMatchingSourceRule.findAllByCommonMatchingSource(sourceCode)
        assertTrue sources.size() > 0
        // find list of spriden last names with lots of records we can use for pagination
        def sql = new Sql(sessionFactory.currentSession.connection())
        def lists = sql.rows("select count(*) cnt, spriden_last_name from spriden group by spriden_last_name having count(*) > 1 order by count(*) desc")
        def list1 = lists[0]
        assertNotNull list1
        assertTrue list1.cnt > 20
        assertNotNull list1.spriden_last_name
        def result
        try {
            sql.call("""
              declare
                cmsc  varchar2(100) := 'HEDM_LASTNAME_MATCH' ;
                lv_last_name varchar2(100) := ? ;
                lv_first_name varchar2(100) ;
                lv_mi varchar2(100);
                lv_email varchar2(100);
                lv_ssn  varchar2(100);
                lv_birth date;
                lv_banner_id varchar2(100);
                lv_gender varchar2(100);

                lv_day varchar2(100);
                lv_month varchar2(100);
                lv_year varchar2(100);

                resultind varchar2(100);
                new_pidm number;
                begin

                delete gotcmme;

                gokcmpk.p_insert_gotcmme (p_last_name     => lv_last_name,
                                              p_entity_cde    => 'P' ,
                                              p_first_name    => lv_first_name,
                                              p_mi            => lv_mi ,
                                              p_id            => lv_banner_id ,
                                              p_street_line1  => '',
                                              p_street_line2  => '',
                                              p_street_line3  => '',
                                              p_city          => '',
                                              p_stat_code    => '',
                                              p_zip          => '',
                                              p_natn_code    => '',
                                              p_cnty_code    => '',
                                              p_phone_area    => '',
                                              p_phone_number  => '',
                                              p_phone_ext     => '',
                                              p_ssn          => '',
                                              p_birth_day    => lv_day,
                                              p_birth_mon   =>  lv_month,
                                              p_birth_year  =>  lv_year,
                                              p_sex          => lv_gender,
                                              p_email_address => lv_email,
                                              p_atyp_code      => '',
                                              p_tele_code     => '',
                                              p_emal_code      => '',
                                              p_asrc_code      => '',
                                              p_addid_code   => '',
                                              p_addid         => '');

                gokcmpk.p_common_matching(p_cmsc_code => cmsc,
                      p_match_status_out  => resultind,
                      p_match_pidm_out   => new_pidm);

                end;
             """, [list1.spriden_last_name]) { output_info ->
                result = output_info
            }
        }
        finally {
//            sql?.close()
        }
    }
}
