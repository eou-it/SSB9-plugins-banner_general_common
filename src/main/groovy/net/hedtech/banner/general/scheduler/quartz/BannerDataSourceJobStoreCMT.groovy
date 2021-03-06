/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.scheduler.quartz

import grails.util.Holders
import groovy.util.logging.Slf4j
import net.hedtech.banner.general.asynchronous.AsynchronousBannerAuthenticationSpoofer
import org.quartz.JobPersistenceException

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.quartz.SchedulerConfigException;
import org.quartz.impl.jdbcjobstore.JobStoreCMT;
import org.quartz.impl.jdbcjobstore.SimpleSemaphore;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.SchedulerSignaler;
import org.quartz.utils.ConnectionProvider;
import org.quartz.utils.DBConnectionManager;

import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;

/**
 * Extended from quartz 2 grails plugin LocalDataSourceJobStore class.
 *
 * Subclass of Quartz's JobStoreCMT class that delegates to a Spring-managed
 * DataSource instead of using a Quartz-managed connection pool. This JobStore
 * will be used if SchedulerFactoryBean's "dataSource" property is set.
 *
 * <p>Supports both transactional and non-transactional DataSource access.
 * With a non-XA DataSource and local Spring transactions, a single DataSource
 * argument is sufficient. In case of an XA DataSource and global JTA transactions,
 * SchedulerFactoryBean's "nonTransactionalDataSource" property should be set,
 * passing in a non-XA DataSource that will not participate in global transactions.
 *
 * <p>Operations performed by this JobStore will properly participate in any
 * kind of Spring-managed transaction, as it uses Spring's DataSourceUtils
 * connection handling methods that are aware of a current transaction.
 *
 * <p>Note that all Quartz Scheduler operations that affect the persistent
 * job store should usually be performed within active transactions,
 * as they assume to get proper locks etc.
 *
 */
@Slf4j
public class BannerDataSourceJobStoreCMT extends JobStoreCMT {

//    private final Log log = LogFactory.getLog( BannerDataSourceJobStoreCMT.class );


    /**
     * Name used for the transactional ConnectionProvider for Quartz.
     * This provider will delegate to the local Spring-managed DataSource.
     * @see org.quartz.utils.DBConnectionManager#addConnectionProvider
     */
    public static final String TX_DATA_SOURCE_PREFIX = "springTxDataSource.";

    /**
     * Name used for the non-transactional ConnectionProvider for Quartz.
     * This provider will delegate to the local Spring-managed DataSource.
     * @see org.quartz.utils.DBConnectionManager#addConnectionProvider
     */
    public static final String NON_TX_DATA_SOURCE_PREFIX = "springNonTxDataSource.";


    private DataSource dataSource;
    private AsynchronousBannerAuthenticationSpoofer asynchronousBannerAuthenticationSpoofer;


    @Override
    public void initialize(ClassLoadHelper loadHelper, SchedulerSignaler signaler)
            throws SchedulerConfigException {

        // Absolutely needs thread-bound DataSource to initialize.
        this.dataSource = Holders.grailsApplication.mainContext.getBean('dataSource')
        this.asynchronousBannerAuthenticationSpoofer = Holders.grailsApplication.mainContext.getBean('asynchronousBannerAuthenticationSpoofer')

        //QuartzFactoryBean.getConfigTimeDataSource();
        if (this.dataSource == null) {
            throw new SchedulerConfigException(
                    "No local DataSource found for configuration - " +
                            "'dataSource' property must be set on SchedulerFactoryBean");
        }

        // Configure transactional connection settings for Quartz.
        setDataSource(TX_DATA_SOURCE_PREFIX + getInstanceName());
        setDontSetAutoCommitFalse(true);

        // Register transactional ConnectionProvider for Quartz.
        DBConnectionManager.getInstance().addConnectionProvider(
                TX_DATA_SOURCE_PREFIX + getInstanceName(),
                new ConnectionProvider() {
                    @Override
                    void initialize() throws SQLException {
                        // wasn't here in quartz 2 implementation - mb
                    }

                    public Connection getConnection() throws SQLException {
                        // Return a transactional Connection, if any.
                        asynchronousBannerAuthenticationSpoofer.authenticateAndSetFormContextForExecute()
                        return DataSourceUtils.doGetConnection(Holders.grailsApplication.mainContext.getBean('dataSource'));
                    }
                    public void shutdown() {
                        // Do nothing - a Spring-managed DataSource has its own lifecycle.
                    }
                }
        );

        // Configure non-transactional connection settings for Quartz.
        setNonManagedTXDataSource(NON_TX_DATA_SOURCE_PREFIX + getInstanceName());
        final DataSource nonTxDataSourceToUse = this.dataSource;
        // Register non-transactional ConnectionProvider for Quartz.
        DBConnectionManager.getInstance().addConnectionProvider(
                NON_TX_DATA_SOURCE_PREFIX + getInstanceName(),
                new ConnectionProvider() {
                    @Override
                    void initialize() throws SQLException {
                        // wasn't here in quartz 2 implementation - mb
                    }

                    public Connection getConnection() throws SQLException {
                        // Always return a non-transactional Connection.
                        asynchronousBannerAuthenticationSpoofer.authenticateAndSetFormContextForExecute()
                        return nonTxDataSourceToUse.getConnection();
                    }
                    public void shutdown() {
                        // Do nothing - a Spring-managed DataSource has its own lifecycle.
                    }
                }
        );

        // No, if HSQL is the platform, we really don't want to use locks
        try {
            String productName = JdbcUtils.extractDatabaseMetaData(dataSource,
                    "getDatabaseProductName").toString();
            productName = JdbcUtils.commonDatabaseName(productName);
            if (productName != null
                    && productName.toLowerCase().contains("hsql")) {
                setUseDBLocks(false);
                setLockHandler(new SimpleSemaphore());
            }
        } catch (MetaDataAccessException e) {
            logWarnIfNonZero(1, "Could not detect database type.  Assuming locks can be taken.");
        }

        super.initialize(loadHelper, signaler);

    }

    @Override
    protected void closeConnection(Connection con) {
        // Will work for transactional and non-transactional connections.
        DataSourceUtils.releaseConnection(con, this.dataSource);
    }


    @Override
    protected Connection getConnection() throws JobPersistenceException {
        if (log.isTraceEnabled()) {
            log.trace "BannerDataSourceJobStoreCMT.getConnection"
        }
        asynchronousBannerAuthenticationSpoofer.authenticateAndSetFormContextForExecute()
        Connection connection = super.getConnection()
        return connection
    }

}
