/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.aip

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.jdbc.UncategorizedSQLException

import static org.junit.Assert.*
import static groovy.test.GroovyAssert.*

@Integration
@Rollback
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

        def listOfActionItems = UserActiveActionItem.findAllByPidm(actionItemPidm);
        assertNotNull listOfActionItems
        assertNotEquals 0,listOfActionItems.size()

        Boolean hasActiveActionItem = UserActiveActionItem.checkIfActionItemPresent(actionItemPidm)
        assert hasActiveActionItem
    }


    @Test
    void testFetchUserActionItemForExistingPidmNoActionItems() {
        //This banner user should have active action items
        def actionItemPidm = getPidmBySpridenId("AIPADM001")
        assertNotNull actionItemPidm

        def listOfActionItems = UserActiveActionItem.findAllByPidm(actionItemPidm);
        assertNotNull listOfActionItems
        assertEquals 0,listOfActionItems.size()

        Boolean hasActiveActionItem = UserActiveActionItem.checkIfActionItemPresent(actionItemPidm)
        assertFalse("AIPADM001 Shuld not have any action items",hasActiveActionItem)
    }


    @Test
    public void testSave()
    {
        UserActiveActionItem userActiveActionItem = new UserActiveActionItem();
        userActiveActionItem.id=10
        userActiveActionItem.pidm=10001
        userActiveActionItem.displayStartDate= new Date()
        userActiveActionItem.displayEndDate=new Date()+20

        shouldFail(UncategorizedSQLException) {
        userActiveActionItem.save(flush :true , ailOnError: true)}
    }


    @Test
    public void testDelete()
    {
        def actionItemPidm = getPidmBySpridenId("CSRSTU001")
        assertNotNull actionItemPidm

        def listOfActionItems = UserActiveActionItem.findAllByPidm(actionItemPidm);
        assertNotNull listOfActionItems
        assertNotEquals 0,listOfActionItems.size()

        def userActiveActionItem=listOfActionItems[0]

        shouldFail(UncategorizedSQLException) {
            userActiveActionItem.delete(flush :true , failOnError: true)}
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
            //sqlObj.close()
        }

        return pidmValue
    }

}
