package com.harvey.mybatic.datasource;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * @author harvey
 */
public class UnpooledDataSource implements DataSource {
    private final String url;
    
    private final String username;
    
    private final String password;
    
    private final String driverClassName;
    
    private ClassLoader driverClassLoader;
    
    private Properties driverProperties;
    
    private Boolean isAutoCommit;
    
    private Integer transactionIsolationLevel;
    
    private static final Map<String, Driver> DRIVER_MAP = new ConcurrentHashMap<>();
    
    static {
        Enumeration<Driver> driverEnumeration = DriverManager.getDrivers();
        while (driverEnumeration.hasMoreElements()) {
            Driver driver = driverEnumeration.nextElement();
            String driverClassName = driver.getClass().getName();
            DRIVER_MAP.put(driverClassName, driver);
        }
    }
    
    public UnpooledDataSource(String url, String username, String password, String driverClassName) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.driverClassName = driverClassName;
    }
    
    public Connection doGetConnection(String username, String password) throws SQLException {
        Properties properties = new Properties();
        if (driverProperties != null) {
            properties.putAll(driverProperties);
        }
        if (username != null) {
            // This property must be "user", not "password"
            properties.put("user", username);
        }
        if (password != null) {
            properties.put("password", password);
        }
        return doGetConnection(properties);
    }
    
    public Connection doGetConnection(Properties properties) throws SQLException {
        initDriver();
        
        Connection connection;
        try {
            connection = DriverManager.getConnection(url, properties);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        
        if (isAutoCommit != null && isAutoCommit != connection.getAutoCommit()) {
            connection.setAutoCommit(isAutoCommit);
        }
        if (transactionIsolationLevel != null && transactionIsolationLevel != connection.getTransactionIsolation()) {
            connection.setTransactionIsolation(transactionIsolationLevel);
        }
        
        return connection;
    }
    
    private synchronized void initDriver() throws SQLException {
        if (DRIVER_MAP.containsKey(driverClassName)) {
            return;
        }
        
        try {
            Class<?> driverClass = Class.forName(driverClassName, true, driverClassLoader);
            Driver driver = (Driver) driverClass.getDeclaredConstructor().newInstance();
            DriverManager.registerDriver(new DriverProxy(driver));
            DRIVER_MAP.put(driverClassName, driver);
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e ) {
            throw new SQLException("Error setting driver on UnpooledDataSource. Cause: " + e);
        }
    }
    
    private static class DriverProxy implements Driver {
        private Driver driver;
        
        DriverProxy(Driver driver) {
            this.driver = driver;
        }
        
        @Override
        public Connection connect(String url, Properties properties) throws SQLException {
            return this.driver.connect(url, properties);
        }
        
        @Override
        public boolean acceptsURL(String url) throws SQLException {
            return this.driver.acceptsURL(url);
        }
        
        @Override
        public DriverPropertyInfo[] getPropertyInfo(String url, Properties properties) throws SQLException {
            return this.driver.getPropertyInfo(url, properties);
        }
        
        @Override
        public int getMajorVersion() {
            return this.driver.getMajorVersion();
        }
        
        @Override
        public int getMinorVersion() {
            return this.driver.getMinorVersion();
        }
        
        @Override
        public boolean jdbcCompliant() {
            return this.driver.jdbcCompliant();
        }
        
        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        }
        
        public Driver getDriver() {
            return this.driver;
        }
        
        public void setDriver(Driver driver) {
            this.driver = driver;
        }
    }
    
    public String getUrl() {
        return url;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public String getDriverClassName() {
        return driverClassName;
    }
    
    public ClassLoader getDriverClassLoader() {
        return driverClassLoader;
    }
    
    public void setDriverClassLoader(ClassLoader driverClassLoader) {
        this.driverClassLoader = driverClassLoader;
    }
    
    public Properties getDriverProperties() {
        return driverProperties;
    }
    
    public void setDriverProperties(Properties driverProperties) {
        this.driverProperties = driverProperties;
    }
    
    public Boolean getAutoCommit() {
        return isAutoCommit;
    }
    
    public void setAutoCommit(Boolean autoCommit) {
        isAutoCommit = autoCommit;
    }
    
    public Integer getTransactionIsolationLevel() {
        return transactionIsolationLevel;
    }
    
    public void setTransactionIsolationLevel(Integer transactionIsolationLevel) {
        this.transactionIsolationLevel = transactionIsolationLevel;
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        return doGetConnection(this.username, this.password);
    }
    
    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return doGetConnection(username, password);
    }
    
    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return DriverManager.getLogWriter();
    }
    
    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        DriverManager.setLogWriter(out);
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
    
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException(getClass().getName() + " is not a wrapper.");
    }
    
    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }
}
