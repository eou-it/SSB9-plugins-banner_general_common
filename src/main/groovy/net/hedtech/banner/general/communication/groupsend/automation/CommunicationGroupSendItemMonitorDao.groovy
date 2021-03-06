/********************************************************************************
  Copyright 2014-2018 Ellucian Company L.P. and its affiliates.
********************************************************************************/
package net.hedtech.banner.general.communication.groupsend.automation

import groovy.util.logging.Slf4j
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.asynchronous.task.AsynchronousTaskMonitorRecord


import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

/**
 * A database access object for Group Send Item Monitor records.
 * Copy and pasted with slight modification from CommunicationJobMonitorDao.java.
 *
 * @author Michael Brzycki
 */
@Slf4j
public class CommunicationGroupSendItemMonitorDao  {

    public static final String AGENT_ID = "AGENT_ID";
    public static final String JOB_ID = "GROUP_SEND_ITEM_KEY";
    public static final String UPDATE_TIME = "UPDATE_TIME";
    public static final String MONITOR_TABLE = "GCBGSIM"; //"REL_GROUP_SEND_ITEM_MONITOR";

    def sessionFactory

    /**
     * Persists a communication job monitor record into the persistent store.
     */
    public void create( AsynchronousTaskMonitorRecord monitorRecord ) {
        SqlPreparedStatement ps = null;
        try {
            long updateTime = System.currentTimeMillis();
            ps = new SqlPreparedStatement( getConnection(), "insert into " + MONITOR_TABLE + " (" + AGENT_ID + ", " + JOB_ID + ", " + UPDATE_TIME + ") values (?,?,?)" );
            ps.setString( 1, monitorRecord.getAgentID() );
            ps.setLong( 2, monitorRecord.getJobID() );
            ps.setLong( 3, updateTime );
            ps.execute();
        } catch (SQLException e) {
            throw new ApplicationException( this.getClass(), e );
        } finally {
            close( ps );
        }
    }


    /**
     * Updates the 'updateTime' for an existing communication job monitor record.
     */
    public AsynchronousTaskMonitorRecord touch( AsynchronousTaskMonitorRecord monitorRecord ) {
        SqlPreparedStatement ps = null;
        monitorRecord.setLastUpdate( System.currentTimeMillis() );
        try {
            ps = new SqlPreparedStatement( getConnection(), "update " + MONITOR_TABLE + " set " + UPDATE_TIME
                                           + " = " + monitorRecord.getLastUpdate() + " where AGENT_ID = " + monitorRecord.getAgentID() + " and JOB_ID = " + monitorRecord.getJobID() );
            ps.setLong( 1, monitorRecord.getLastUpdate() );
            ps.setString( 2, monitorRecord.getAgentID() );
            ps.setLong( 3, monitorRecord.getJobID() );
            int rows = ps.executeUpdate();
            if (rows != 1) {
                throw new RuntimeException( "update " + MONITOR_TABLE + " set " + UPDATE_TIME + " = "
                                           + monitorRecord.getLastUpdate() + " where AGENT_ID = " + monitorRecord.getAgentID() + " and JOB_ID = "
                                           + monitorRecord.getJobID() + " did not update a (single) row!" );
            }
        } catch (SQLException e) {
            throw new ApplicationException( this.getClass(), "CommunicationJobMonitorDao.touch update " + MONITOR_TABLE + " set "
                                            + UPDATE_TIME + " = " + monitorRecord.getLastUpdate() + " where AGENT_ID = " + monitorRecord.getAgentID()
                                            + " and JOB_ID = " + monitorRecord.getJobID() + " caught exception" + e.getMessage(), e );
        } finally {
            close( ps );
        }
        return monitorRecord;
    }


    public List getOverdueMonitors( long overdueMillis ) {
        SqlPreparedStatement ps = null;
        ResultSet rs = null;
        List<AsynchronousTaskMonitorRecord> monitors = new ArrayList<AsynchronousTaskMonitorRecord>();
        try {
            ps = new SqlPreparedStatement( getConnection(), "SELECT AGENT_ID, JOB_ID, UPDATE_TIME FROM " + MONITOR_TABLE + " where " + UPDATE_TIME + " <= ?" );
            ps.setLong( 1, System.currentTimeMillis() - overdueMillis );
            if (log.isDebugEnabled()) {
                log.debug( "getSoftwareAgentMonitors:" + ps.toString() );
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                monitors.add( new AsynchronousTaskMonitorRecord( rs.getString( "AGENT_ID" ), rs.getLong( "JOB_ID" ),
                                                    new Date( rs.getLong( "UPDATE_TIME" ) ) ) );
            }
        } catch (SQLException e) {
            throw new ApplicationException( this.getClass(), "getOverdueMonitors caught " + e.getMessage(), e );
        } finally {
            close( ps );
        }
        return monitors;
    }


    public void delete( AsynchronousTaskMonitorRecord monitorRecord ) {
        SqlPreparedStatement ps = null;
        try {
            ps = new SqlPreparedStatement( getConnection(), "DELETE FROM " + MONITOR_TABLE + " WHERE JOB_ID=?" );
            ps.setLong( 1, monitorRecord.getJobID() );
            ps.executeUpdate();
        } catch (SQLException e ) {
            throw new ApplicationException( this.getClass(), "CommunicationJobMonitorDao.delete where job_id = " + monitorRecord.getJobID(), e );
        } finally {
            close( ps );
        }
    }


    /**
     * Returns the connection from the JpaTemplate. An exception will be thrown
     * if this method is called when not in a method annotated as Transactional.
     * @return Connection the active JDBC connection
     * @throws java.sql.SQLException if reported by the database or driver
     */
    private Connection getConnection() throws SQLException {
        def conn = sessionFactory.getCurrentSession().connection()
        return conn;
    }


    private void close( PreparedStatement ps ) {
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException e) {
            }
        }
    }

}
