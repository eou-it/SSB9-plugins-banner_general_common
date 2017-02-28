package net.hedtech.banner.general.communication.groupsend

import groovy.sql.Sql
import net.hedtech.banner.general.communication.CommunicationBaseConcurrentTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

class SqlExecuteConcurrentTests extends CommunicationBaseConcurrentTestCase {
    def selfServiceBannerAuthenticationProvider

    @Before
    public void setUp() {
        formContext = ['GUAGMNU','SELFSERVICE']
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('BCMADMIN', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)
        super.setUp()
    }

    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }

//    @Test
//    public void testSqlExecuteUpdateBenchmark() {
//        long t0 = System.currentTimeMillis()
//        long attempts = 10000
//        for (int i=0; i<attempts; i++) {
//            Sql sql
//            try {
//                sessionFactory.currentSession.with { session ->
//                    def tx = session.beginTransaction()
//                    sql = new Sql(session.connection())
//                    String name = "AIPgeneral"+i
//                    sql.execute( "update GCRFLDR set GCRFLDR_NAME = ${name} where GCRFLDR_SURROGATE_ID=10140" )
//                    assertEquals( 1, sql.getUpdateCount() )
//                    tx.commit()
//                }
//            } finally {
//                sql?.close()
//            }
//        }
//        long t1 = System.currentTimeMillis()
//        println "Total time to query using sql.execute ${attempts} is ${(t1-t0)/1000} seconds."
//    }


    @Test
    public void testSqlExecuteQueryBenchmark() {
        long t0 = System.currentTimeMillis()
        long attempts = 10000
        for (int i=0; i<attempts; i++) {
            Sql sql
            try {
                sessionFactory.currentSession.with { session ->
                    def tx = session.beginTransaction()
                    sql = new Sql(session.connection())
                    boolean result = sql.execute( "select 1 from dual" )
                    assertTrue( result )
                    tx.commit()
                }
            } finally {
                sql?.close()
            }
        }
        long t1 = System.currentTimeMillis()
        println "Total time to query using sql.execute ${attempts} is ${(t1-t0)/1000} seconds."
    }

//    @Test
//    public void testNullSql() {
//        Sql sql
//        try {
//            sessionFactory.currentSession.with { session ->
//                def tx = session.beginTransaction()
//                sql = new Sql(session.connection())
//                sql.execute( null )
//                tx.commit()
//            }
//        } finally {
//            sql?.close()
//        }
//    }
//
//    @Test
//    public void testEmptySql() {
//        Sql sql
//        try {
//            sessionFactory.currentSession.with { session ->
//                def tx = session.beginTransaction()
//                sql = new Sql(session.connection())
//                sql.execute( "" )
//                tx.commit()
//            }
//        } finally {
//            sql?.close()
//        }
//    }
//
//    @Test
//    public void testBadSql() {
//        Sql sql
//        try {
//            sessionFactory.currentSession.with { session ->
//                def tx = session.beginTransaction()
//                sql = new Sql(session.connection())
//                sql.execute( "select mike from nothing" )
//                tx.commit()
//            }
//        } finally {
//            sql?.close()
//        }
//    }
//
//    @Test
//    public void testBadSqlWithIdentifiedBy() {
//        Sql sql
//        try {
//            sessionFactory.currentSession.with { session ->
//                def tx = session.beginTransaction()
//                Map bannerAuth = [roleName: "programmer", bannerPassword: "u_pick_it"]
//                sql = new Sql(session.connection())
//                String stmt = "set role \"${bannerAuth.roleName}\" identified by \"${bannerAuth.bannerPassword}\"" as String
//                sql.execute(stmt)
//                tx.commit()
//            }
//        } finally {
//            sql?.close()
//        }
//    }


//    @Test
//    public void testSqlExecuteLoadBenchmark() {
//        long t0 = System.currentTimeMillis()
//        long attempts = 10000
//        for (int i=0; i<attempts; i++) {
//            Sql sql
//            try {
//                sessionFactory.currentSession.with { session ->
//                    def tx = session.beginTransaction()
//                    sql = new Sql(session.connection())
//                    boolean result = sql.execute( "select 1 from dual" )
//                    assertTrue( result )
//                    tx.commit()
//                }
//            } finally {
//                sql?.close()
//            }
//        }
//        long t1 = System.currentTimeMillis()
//        println "Total time to query using sql.execute ${attempts} is ${(t1-t0)/1000} seconds."
//
//
////        String stmt = "set role \"${bannerAuth.roleName}\" identified by \"${bannerAuth.bannerPassword}\"" as String
////        log.trace "BannerDS.unlockRole will set role '${bannerAuth.roleName}' for connection $conn"
////
////        Sql db = new Sql(conn)
////        db.execute(stmt) // Note: we don't close the Sql as this closes the connection, and we're preparing the connection for subsequent use
//
//    }


//    public void testSqlExecuteWithDynamicFixLoadBenchmark() {
//
//    }
//
//
//    public void testSqlExecuteWithNoceboLoadBenchmark() {
//
//    }
}
