package com.harvey.mybatic.datasource;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author harvey
 */
public class PooledConnection {
    private static final String CLOSE_METHOD = "close";
    
    private static final Class<?>[] IFACES = new Class<?>[]{Connection.class};
    
    private final PooledDataSource pooledDataSource;
    
    private final Connection realConnection;
    
    private final Connection proxyConnection;
    
    private final int hashCode;
    
    private long startConnectionCheckoutTimeMillis;
    
    private long createdTimeMillis;
    
    private long lastUsedTimeMillis;
    
    private int connectionTypeCode;
    
    private boolean isValid;
    
    public PooledConnection(Connection realConnection, PooledDataSource pooledDataSource) {
        this.hashCode = realConnection.hashCode();
        this.pooledDataSource = pooledDataSource;
        this.createdTimeMillis = System.currentTimeMillis();
        this.lastUsedTimeMillis = System.currentTimeMillis();
        this.realConnection = realConnection;
        this.isValid = true;
        this.proxyConnection = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), IFACES, new PooledConnectionProxyInvocationHandler());
    }
    
    private class PooledConnectionProxyInvocationHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // If the CLOSE_METHOD is called, then reclaim the connection.
            String methodName = method.getName();
            if (CLOSE_METHOD.equals(methodName) && CLOSE_METHOD.hashCode() == methodName.hashCode()) {
                pooledDataSource.pushConnection(PooledConnection.this);
                return null;
            }
            
            // Except for Object's toString(), all other methods need to check if the connection is normal.
            if (!Object.class.equals(method.getDeclaringClass())) {
                checkConnection();
            }
            
            return method.invoke(realConnection, args);
        }
    }
    
    public boolean isValid() {
        return isValid && realConnection != null && pooledDataSource.pingConnection(PooledConnection.this);
    }
    
    public boolean isInvalid() {
        return !isValid();
    }
    
    public void invalidate() {
        this.isValid = false;
    }
    
    private void checkConnection() throws SQLException {
        if (isInvalid()) {
            throw new SQLException("Error accessing PooledConnection. Connection is invalid.");
        }
    }
    
    public Connection getRealConnection() {
        return realConnection;
    }
    
    public Connection getProxyConnection() {
        return proxyConnection;
    }
    
    public int getRealConnectionHashCode() {
        return realConnection != null ? realConnection.hashCode() : 0;
    }
    
    public int getConnectionTypeCode() {
        return connectionTypeCode;
    }
    
    public void setConnectionTypeCode(int connectionTypeCode) {
        this.connectionTypeCode = connectionTypeCode;
    }
    
    public long getConnectionCheckoutTimeMillis() {
        return System.currentTimeMillis() - startConnectionCheckoutTimeMillis;
    }
    
    public long getStartConnectionCheckoutTimeMillis() {
        return startConnectionCheckoutTimeMillis;
    }
    
    public void setStartConnectionCheckoutTimeMillis(long startConnectionCheckoutTimeMillis) {
        this.startConnectionCheckoutTimeMillis = startConnectionCheckoutTimeMillis;
    }
    
    public long getCreatedTimeMillis() {
        return createdTimeMillis;
    }
    
    public void setCreatedTimeMillis(long createdTimeMillis) {
        this.createdTimeMillis = createdTimeMillis;
    }
    
    public long getLastUsedTimeMillis() {
        return lastUsedTimeMillis;
    }
    
    public void setLastUsedTimeMillis(long lastUsedTimeMillis) {
        this.lastUsedTimeMillis = lastUsedTimeMillis;
    }
    
    public long getElapsedTimeSinceLastUse() {
        return System.currentTimeMillis() - lastUsedTimeMillis;
    }
    
    @Override
    public int hashCode() {
        return hashCode;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PooledConnection) {
            return realConnection.hashCode() == ((PooledConnection) obj).realConnection.hashCode();
        } else if (obj instanceof Connection) {
            return hashCode == obj.hashCode();
        } else {
            return false;
        }
    }
}
