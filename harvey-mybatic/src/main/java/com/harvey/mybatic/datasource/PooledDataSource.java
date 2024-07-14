package com.harvey.mybatic.datasource;

import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * @author harvey
 */
public class PooledDataSource implements DataSource {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(PooledDataSource.class);
    
    private final UnpooledDataSource dataSource;
    
    private final PooledDataSourceContext context = new PooledDataSourceContext(this);
    
    private final ReentrantLock contextLock = new ReentrantLock();
    
    private final Condition contextCondition = contextLock.newCondition();
    
    private int expectedConnectionTypeCode;
    
    private int maximumActiveConnectionSize = 10;
    
    private int maximumIdleConnectionSize = 5;
    
    private int maximumConnectionCheckoutTimeMillis = 20000;
    
    private int maximumBadConnectionCount = 8;
    
    private int maximumConnectionWaitTimeMillis = 20000;
    
    private int pingTimeout = 100;
    
    private boolean isPingConnectionEnabled = true;
    
    public PooledDataSource(String url, String username, String password, String driverClassName) {
        this.dataSource = new UnpooledDataSource(url, username, password, driverClassName);
        this.expectedConnectionTypeCode = buildConnectionTypeCode(dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword());
        closeAllConnection();
    }
    
    public static Connection unwrapConnection(Connection connection) {
        if (Proxy.isProxyClass(connection.getClass())) {
            InvocationHandler invocationHandler = Proxy.getInvocationHandler(connection);
            if (invocationHandler instanceof PooledConnection) {
                return ((PooledConnection) invocationHandler).getRealConnection();
            }
        }
        return connection;
    }
    
    public boolean pingConnection(PooledConnection connection) {
        if (!isPingConnectionEnabled || connection.getElapsedTimeSinceLastUse() < pingTimeout) {
            return true;
        }
        
        try {
            Connection realConnection = connection.getRealConnection();
            if (realConnection.isClosed()) {
                return false;
            }
            
            if (!realConnection.getAutoCommit()) {
                realConnection.rollback();
            }
            
            Statement statement = realConnection.createStatement();
            ResultSet resultSet = statement.executeQuery("select 1");
            resultSet.close();
            
            logger.info("Connection {} is GOOD!", connection.getRealConnectionHashCode());
            
            return true;
        } catch (Exception e) {
            logger.info("Connection {} is BAD: {}", connection.getRealConnectionHashCode(), e.getMessage());
            
            return false;
        }
    }
    
    public void pushConnection(PooledConnection connection) throws SQLException {
        contextLock.lock();
        try {
            context.removeActiveConnection(connection);
            
            if (connection.isInvalid()) {
                logger.info("A bad connection ({}) attempted to return to the pool, discarding connection.", connection.getRealConnectionHashCode());
                return;
            }
            
            long checkoutTimeMillis = connection.getConnectionCheckoutTimeMillis();
            context.incrAccumulatedCheckoutTimeMillis(checkoutTimeMillis);
            
            // If the auto-commit mode is not enabled, perform a rollback operation,
            // you need to manually commit or roll back the transaction.
            Connection realConnection = connection.getRealConnection();
            if (!realConnection.getAutoCommit()) {
                realConnection.rollback();
                return;
            }
            
            if (context.getIdleConnectionSize() >= maximumIdleConnectionSize || expectedConnectionTypeCode != connection.getConnectionTypeCode()) {
                realConnection.close();
                connection.invalidate();
                return;
            }
            
            // Instantiate a new DB connection and add it to the idle list.
            PooledConnection idleConnection = new PooledConnection(realConnection, this);
            idleConnection.setCreatedTimeMillis(connection.getCreatedTimeMillis());
            idleConnection.setLastUsedTimeMillis(connection.getLastUsedTimeMillis());
            context.offerIdleConnection(idleConnection);
            
            connection.invalidate();
            logger.info("Returned connection {} to pool.", connection.getRealConnectionHashCode());
            
            // Notify all waiting threads that they can obtain a connection.
            contextCondition.signalAll();
        } finally {
            contextLock.unlock();
        }
    }
    
    private PooledConnection popConnection(String username, String password) throws SQLException {
        PooledConnection connection = null;
        boolean isConnectionWaitCounted = false;
        int localBadConnectionCount = 0;
        long startRequestTimeMillis = System.currentTimeMillis();
        
        // Loop to ensure obtaining a connection.
        while (connection == null || !connection.isValid()) {
            contextLock.lock();
            try {
                // If there are idle connections, then get a connection from idle connection list.
                if (context.isIdleConnectionListNotEmpty()) {
                    connection = context.pollIdleConnection();
                    logger.info("Checked out connection {} from pool.", connection.getRealConnectionHashCode());
                }
                // If there are no idle connections, then remove the oldest active connection.
                else {
                    // If the number of active connections has not yet reached the maximum value, directly create an active connection.
                    if (context.getActiveConnectionSize() < maximumActiveConnectionSize) {
                        connection = new PooledConnection(dataSource.getConnection(), this);
                        logger.info("Created connection {}.", connection.getRealConnectionHashCode());
                    }
                    // If the number of active connections has reached the maximum value.
                    else {
                        PooledConnection oldestActiveConnection = context.pollActiveConnection();
                        long connectionCheckoutTimeMillis = oldestActiveConnection.getConnectionCheckoutTimeMillis();
                        
                        // If the checkout time is too long, this link will be marked as invalid.
                        if (connectionCheckoutTimeMillis > maximumConnectionCheckoutTimeMillis) {
                            context.incrOverdueConnectionCount();
                            context.incrAccumulatedCheckoutTimeMillis(connectionCheckoutTimeMillis);
                            context.incrAccumulatedOverdueConnectionCheckoutTimeMillis(connectionCheckoutTimeMillis);
                            context.removeActiveConnection(oldestActiveConnection);
                            oldestActiveConnection.invalidate();
                            connection = new PooledConnection(oldestActiveConnection.getRealConnection(), this);
                            logger.info("Claimed overdue connection {}.", connection.getRealConnectionHashCode());
                        }
                        // If the checkout time is not too long, then waiting.
                        else {
                            // Increase the count of waiting thread.
                            if (!isConnectionWaitCounted) {
                                context.incrConnectionWaitCount();
                                isConnectionWaitCounted = true;
                            }
                            
                            // Waiting for a connection.
                            try {
                                logger.info("Waiting as long as {} milliseconds for connection.", maximumConnectionWaitTimeMillis);
                                long startConnectionWaitTimeMillis = System.currentTimeMillis();
                                boolean isSignaled = contextCondition.await(maximumConnectionWaitTimeMillis, TimeUnit.MILLISECONDS);
                                if (isSignaled) {
                                    logger.info("Be signaled to get a connection");
                                } else {
                                    logger.info("Already waited for {} milliseconds, still not be signaled", maximumConnectionWaitTimeMillis);
                                }
                                long endConnectionWaitTimeMillis = System.currentTimeMillis();
                                long connectionWaitTimeMillis = endConnectionWaitTimeMillis - startConnectionWaitTimeMillis;
                                context.incrAccumulatedWaitTimeMillis(connectionWaitTimeMillis);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
                
                if (connection != null && connection.isInvalid()) {
                    logger.info("A bad connection ({}) was returned from the pool, getting another connection.", connection.getRealConnectionHashCode());
                    context.incrBadConnectionCount();
                    localBadConnectionCount++;
                    if (localBadConnectionCount > maximumBadConnectionCount) {
                        logger.debug("PooledDataSource: Could not get a good connection to the database.");
                        throw new SQLException("PooledDataSource: Could not get a good connection to the database.");
                    }
                }
            } finally {
                contextLock.unlock();
            }
        }
        
        long endRequestTimeMillis = System.currentTimeMillis();
        long requestTimeMillis = endRequestTimeMillis - startRequestTimeMillis;
        context.incrAccumulatedRequestTimeMillis(requestTimeMillis);
        context.incrConnectionRequestCount();
        context.offerActiveConnection(connection);
        
        connection.setConnectionTypeCode(buildConnectionTypeCode(dataSource.getUrl(), username, password));
        connection.setStartConnectionCheckoutTimeMillis(System.currentTimeMillis());
        connection.setLastUsedTimeMillis(System.currentTimeMillis());
        
        return connection;
    }
    
    public void closeAllConnection() {
        contextLock.lock();
        try {
            context.closeAllConnection();
            logger.info("PooledDataSource forcefully closed/removed all connections.");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            contextLock.unlock();
        }
    }
    
    private int buildConnectionTypeCode(String url, String username, String password) {
        return (url + username + password).hashCode();
    }
    
    public int getMaximumActiveConnectionSize() {
        return maximumActiveConnectionSize;
    }
    
    public void setMaximumActiveConnectionSize(int maximumActiveConnectionSize) {
        this.maximumActiveConnectionSize = maximumActiveConnectionSize;
    }
    
    public int getMaximumIdleConnectionSize() {
        return maximumIdleConnectionSize;
    }
    
    public void setMaximumIdleConnectionSize(int maximumIdleConnectionSize) {
        this.maximumIdleConnectionSize = maximumIdleConnectionSize;
    }
    
    public int getMaximumConnectionCheckoutTimeMillis() {
        return maximumConnectionCheckoutTimeMillis;
    }
    
    public void setMaximumConnectionCheckoutTimeMillis(int maximumConnectionCheckoutTimeMillis) {
        this.maximumConnectionCheckoutTimeMillis = maximumConnectionCheckoutTimeMillis;
    }
    
    public int getMaximumBadConnectionCount() {
        return maximumBadConnectionCount;
    }
    
    public void setMaximumBadConnectionCount(int maximumBadConnectionCount) {
        this.maximumBadConnectionCount = maximumBadConnectionCount;
    }
    
    public int getMaximumConnectionWaitTimeMillis() {
        return maximumConnectionWaitTimeMillis;
    }
    
    public void setMaximumConnectionWaitTimeMillis(int maximumConnectionWaitTimeMillis) {
        this.maximumConnectionWaitTimeMillis = maximumConnectionWaitTimeMillis;
    }
    
    public int getPingTimeout() {
        return pingTimeout;
    }
    
    public void setPingTimeout(int pingTimeout) {
        this.pingTimeout = pingTimeout;
    }
    
    public boolean isPingConnectionEnabled() {
        return isPingConnectionEnabled;
    }
    
    public void setPingConnectionEnabled(boolean pingConnectionEnabled) {
        isPingConnectionEnabled = pingConnectionEnabled;
    }
    
    public int getExpectedConnectionTypeCode() {
        return expectedConnectionTypeCode;
    }
    
    public void setExpectedConnectionTypeCode(int expectedConnectionTypeCode) {
        this.expectedConnectionTypeCode = expectedConnectionTypeCode;
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        return popConnection(dataSource.getUsername(), dataSource.getPassword()).getProxyConnection();
    }
    
    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return popConnection(username, password).getProxyConnection();
    }
    
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException(getClass().getName() + " is not a wrapper.");
    }
    
    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }
    
    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return DriverManager.getLogWriter();
    }
    
    @Override
    public void setLogWriter(PrintWriter logWriter) throws SQLException {
        DriverManager.setLogWriter(logWriter);
    }
    
    @Override
    public void setLoginTimeout(int loginTimeout) throws SQLException {
        DriverManager.setLoginTimeout(loginTimeout);
    }
    
    @Override
    public int getLoginTimeout() throws SQLException {
        return DriverManager.getLoginTimeout();
    }
    
    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    }
}
