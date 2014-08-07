/*********************************************************************************
  Copyright 2010-2013 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase

class SqlProcessCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    def sqlProcessCompositeService


    protected void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    protected void tearDown() {
        super.tearDown()
    }


    void testSqlProcessCompositeServiceTestGetSqlProcessResultsNoBinds() {
        def params = [sqlCode: 'FACULTY', sqlProcessCode: 'INTCOMP']
        Sql db
        try {
            db = new Sql(new Sql(sessionFactory.getCurrentSession().connection()))
            def results = sqlProcessCompositeService.getSqlProcessResults(params)
            //assertEquals results, "testing"
            assertEquals results.size(),
                    db.rows("SELECT DISTINCT a.sibinst_pidm" +
                            "  FROM sibinst a," +
                            "       stvfcst" +
                            " WHERE a.sibinst_term_code_eff IN" +
                            "       (SELECT MAX(b.sibinst_term_code_eff)" +
                            "          FROM sibinst b," +
                            "               sobterm c" +
                            "         WHERE b.sibinst_pidm = a.sibinst_pidm" +
                            "           AND b.sibinst_term_code_eff" +
                            "                 <= c.sobterm_term_code" +
                            "           AND icgokcom.f_calc_valid_ldi_term(c.sobterm_term_code) = 'Y'" +
                            "         GROUP BY c.sobterm_term_code)" +
                            "   AND a.sibinst_fcst_code = stvfcst_code" +
                            "   AND stvfcst_active_ind = 'A'").size()
        }
        finally {
            db?.close()
        }
    }


    void testSqlProcessCompositeServiceTestGetSqlProcessResultsBinds() {
        def params = [sqlCode: 'IAM_GOBEACC_RULE', sqlProcessCode: 'IAM', pidm: 32473, garbage: "randomGarbage"]
        Sql db
        try {
            db = new Sql(new Sql(sessionFactory.getCurrentSession().connection()))
            def results = sqlProcessCompositeService.getSqlProcessResults(params)
            //assertEquals results, "testing"
            assertEquals results.size(),
                    db.rows("SELECT GOBTPAC_EXTERNAL_USER PRINCIPAL, GOBTPAC_PIN CREDENTIAL, GOBTPAC_PIDM PIDM FROM GOBTPAC WHERE GOBTPAC.GOBTPAC_PIDM = :pidm", [pidm: 32473]).size()
        }
        finally {
            db?.close()
        }
    }
    // TODO: Test bad cases.
}
