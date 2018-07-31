/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.aip

import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

import static net.hedtech.banner.general.aip.AipNotificationUtil.YES
import static net.hedtech.banner.general.aip.AipNotificationUtil.NO
import static net.hedtech.banner.general.aip.AipNotificationUtil.ENABLED
import static net.hedtech.banner.general.aip.AipNotificationUtil.DISABLED
import static net.hedtech.banner.general.aip.AipNotificationUtil.SQPR_CODE_GENERAL_SSB
import static net.hedtech.banner.general.aip.AipNotificationUtil.ICSN_CODE_ENABLE_ACTION_ITEMS
import static org.junit.Assert.*


class AipNotificationServiceIntegrationTest extends BaseIntegrationTestCase {

    def aipNotificationService
    def existingValue


    @Before
    void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()

    }


    @After
    void tearDown() {
        super.tearDown()
    }

    @Test
    void testHasActiveActionItemsForUserWithActionItems() {

        Integer pidm = getPidmBySpridenId("CSRSTU001");
        assertNotNull pidm

        Boolean hasActiveItme = aipNotificationService.hasActiveActionItems(pidm);
        assertTrue hasActiveItme

    }

    @Test
    void testHasActiveActionItemsForUserWithOutActionItems() {
        //AIPADM001 is admin user who does not have action items
        Integer pidm = getPidmBySpridenId("AIPADM001");
        assertNotNull pidm

        Boolean hasActiveItme = aipNotificationService.hasActiveActionItems(pidm);
        assertFalse hasActiveItme

    }

    @Test
    void testGetGoriccrFlagForDisabled() {
        //Checking for goriccr value before updating it
        def oldValue = getGoriicrValue(SQPR_CODE_GENERAL_SSB, ICSN_CODE_ENABLE_ACTION_ITEMS,);
        assertNotNull oldValue

        assertTrue updateGoriccrRule(SQPR_CODE_GENERAL_SSB, ICSN_CODE_ENABLE_ACTION_ITEMS, NO)
        String flag = aipNotificationService.getGoriicrFlag()
        assertNotNull flag
        assertEquals( DISABLED, flag)

        //Restoring old value so other test will not be impacted
        assertTrue updateGoriccrRule( SQPR_CODE_GENERAL_SSB,  ICSN_CODE_ENABLE_ACTION_ITEMS, oldValue)
        def valueAfterUpdate = getGoriicrValue( SQPR_CODE_GENERAL_SSB,  ICSN_CODE_ENABLE_ACTION_ITEMS);
        assertEquals oldValue,valueAfterUpdate
    }

    @Test
    void testGetGoriccrFlagForEnable() {
        //Checking for goriccr value before updating it
        def oldValue = getGoriicrValue( SQPR_CODE_GENERAL_SSB,  ICSN_CODE_ENABLE_ACTION_ITEMS);
        assertNotNull oldValue

        assertTrue updateGoriccrRule( SQPR_CODE_GENERAL_SSB,  ICSN_CODE_ENABLE_ACTION_ITEMS,  YES)
        String flag = aipNotificationService.getGoriicrFlag()
        assertNotNull flag
        assertEquals( ENABLED, flag)

        //restoring value so that other
        assertTrue updateGoriccrRule( SQPR_CODE_GENERAL_SSB,  ICSN_CODE_ENABLE_ACTION_ITEMS, oldValue)
        def valueAfterUpdate = getGoriicrValue( SQPR_CODE_GENERAL_SSB,  ICSN_CODE_ENABLE_ACTION_ITEMS);
        assertEquals oldValue,valueAfterUpdate

    }


    private Integer getPidmBySpridenId(def spridenId) {
        Sql sqlObj
        Integer pidmValue
        try {
            sqlObj = new Sql(sessionFactory.getCurrentSession().connection())
            def query = "SELECT SPRIDEN_PIDM pidm FROM SPRIDEN WHERE SPRIDEN_ID=:spridenId"
            pidmValue = sqlObj?.firstRow(query,spridenId:spridenId)?.pidm
        } catch (Exception e) {
            return null;
        }
        finally{
            sqlObj.close()
        }
        return pidmValue
    }

    private def getGoriicrValue(def sqpr_code, def icsn_code) {
        Sql sqlObj
        def gorriccrValue
        try {
            sqlObj = new Sql(sessionFactory.getCurrentSession().connection())
            def sqlQuery = """
                            select g.goriccr_value
                              from GORICCR g
                             where g.goriccr_sqpr_code = :sqpr_code
                               and g.goriccr_icsn_code = :icsn_code
                           """
            gorriccrValue = sqlObj?.firstRow(sqlQuery,sqpr_code:sqpr_code,icsn_code:icsn_code).goriccr_value

        } catch (Exception Ex) {
           return null
        }
        finally{
            sqlObj.close()
        }
        gorriccrValue
    }
    /*
    * This method will set the GORRICCR Rule
    * */

    private def updateGoriccrRule(def sqpr_code, def icsn_code, def value) {
        Sql sqlObj
        Boolean retValue=false
        try {
            sqlObj = new Sql(sessionFactory.getCurrentSession().connection())
            def sqlQuery = """update GORICCR
                                 set goriccr_value = :value
                               where goriccr_sqpr_code = :sqpr
                                 and goriccr_icsn_code = :icsn
                            """
            sqlObj.executeUpdate(sqlQuery,sqpr:sqpr_code,icsn:icsn_code,value:value)
            retValue=true;
        } catch (Exception ex) {
            return retValue
        }
        finally{
            sqlObj.close()
        }
        retValue
    }
}