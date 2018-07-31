/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.aip

import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import static org.junit.Assert.*


class UserActiveActionItemIntegrationTest extends BaseIntegrationTestCase {

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
    void testFetchUserActionItemForExistingPidm() {
        //This banner user should have active action items
        def actionItemPidm = getPidmBySpridenId("CSRSTU001")
        assertNotNull actionItemPidm

        Boolean hasActiveActionItem = UserActiveActionItem.checkIfActionItemPresent(actionItemPidm)
        assert hasActiveActionItem
    }

    @Test
    void testFetchUserActionItemForExistingPidmNoActionItems() {
        //This banner user should have active action items
        def actionItemPidm = getPidmBySpridenId("AIPADM001")
        assertNotNull actionItemPidm

        Boolean hasActiveActionItem = UserActiveActionItem.checkIfActionItemPresent(actionItemPidm)
        assertFalse("AIPADM001 Shuld not have any action items",hasActiveActionItem)
    }



    private Integer getPidmBySpridenId(def spridenId) {
        Sql sqlObj
        Integer pidmValue
        try{
            sqlObj = new Sql(sessionFactory.getCurrentSession().connection())
            def query = "SELECT SPRIDEN_PIDM pidm FROM SPRIDEN WHERE SPRIDEN_ID=:spridenId"
            pidmValue = sqlObj?.firstRow(query,spridenId:spridenId)?.pidm
        } catch(Exception e){
            return null;//Null will fail the assertion after this me
        }
        finally{
            sqlObj.close()
        }

        return pidmValue
    }

}