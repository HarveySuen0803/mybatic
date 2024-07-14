package com.harvey.mybatic.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author harvey
 */
public class PooledDataSourceContext {
    private final PooledDataSource dataSource;
    
    private final LinkedList<PooledConnection> idleConnectionList = new LinkedList<>();
    private final LinkedList<PooledConnection> activeConnectionList = new LinkedList<>();
    
    private long accumulatedConnectionRequestTimeMillis = 0;
    private long accumulatedConnectionCheckoutTimeMillis = 0;
    private long accumulatedConnectionWaitTimeMillis = 0;
    private long accumulatedOverdueConnectionCheckoutTimeMillis = 0;
    
    private long connectionRequestCount = 0;
    private long connectionWaitCount = 0;
    private long overdueConnectionCount = 0;
    private long badConnectionCount = 0;
    
    public PooledDataSourceContext(PooledDataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    // dataSource
    public PooledDataSource getDataSource() {
        return dataSource;
    }
    
    // idleConnectionList
    public LinkedList<PooledConnection> getIdleConnectionList() {
        return idleConnectionList;
    }
    
    public int getIdleConnectionSize() {
        return idleConnectionList.size();
    }
    
    public void offerIdleConnection(PooledConnection connection) {
        idleConnectionList.offer(connection);
    }
    
    public PooledConnection pollIdleConnection() {
        return idleConnectionList.pollFirst();
    }
    
    public boolean isIdleConnectionListEmpty() {
        return idleConnectionList.isEmpty();
    }
    
    public boolean isIdleConnectionListNotEmpty() {
        return !isIdleConnectionListEmpty();
    }
    
    public void closeAllIdleConnection() throws SQLException {
        closeAllConnection(idleConnectionList);
    }
    
    // activeConnectionList
    public LinkedList<PooledConnection> getActiveConnectionList() {
        return activeConnectionList;
    }
    
    public int getActiveConnectionSize() {
        return activeConnectionList.size();
    }
    
    public void removeActiveConnection(PooledConnection connection) {
        activeConnectionList.remove(connection);
    }
    
    public void offerActiveConnection(PooledConnection connection) {
        activeConnectionList.offer(connection);
    }
    
    public PooledConnection pollActiveConnection() {
        return activeConnectionList.pollFirst();
    }
    
    public void closeAllActiveConnection() throws SQLException {
        closeAllConnection(activeConnectionList);
    }
    
    public void closeAllConnection() throws SQLException {
        closeAllConnection(idleConnectionList);
        closeAllConnection(activeConnectionList);
    }
    
    private void closeAllConnection(List<PooledConnection> connectionList) throws SQLException {
        Iterator<PooledConnection> iter = connectionList.iterator();
        while (iter.hasNext()) {
            PooledConnection connection = iter.next();
            connection.invalidate();
            Connection realConnection = connection.getRealConnection();
            if (!realConnection.getAutoCommit()) {
                realConnection.rollback();
            }
            realConnection.close();
            iter.remove();
        }
    }
    
    // accumulatedRequestTimeMillis
    public long getAccumulatedConnectionRequestTimeMillis() {
        return accumulatedConnectionRequestTimeMillis;
    }
    
    public void incrAccumulatedRequestTimeMillis(long requestTimeMillis) {
        accumulatedConnectionRequestTimeMillis += requestTimeMillis;
    }
    
    public void decrAccumulatedRequestTimeMillis(long requestTimeMillis) {
        accumulatedConnectionRequestTimeMillis -= requestTimeMillis;
    }
    
    // accumulatedCheckoutTimeMillis
    public long getAccumulatedConnectionCheckoutTimeMillis() {
        return accumulatedConnectionCheckoutTimeMillis;
    }
    
    public void incrAccumulatedCheckoutTimeMillis(long checkoutTimeMillis) {
        this.accumulatedConnectionCheckoutTimeMillis += checkoutTimeMillis;
    }
    
    public void decrAccumulatedCheckoutTimeMillis(long checkoutTimeMillis) {
        this.accumulatedConnectionCheckoutTimeMillis -= checkoutTimeMillis;
    }
    
    // accumulatedCheckoutTimeMillisOfOverdueConnections
    public long getAccumulatedOverdueConnectionCheckoutTimeMillis() {
        return accumulatedOverdueConnectionCheckoutTimeMillis;
    }
    
    public void incrAccumulatedOverdueConnectionCheckoutTimeMillis(long checkoutTimeMillis) {
        this.accumulatedOverdueConnectionCheckoutTimeMillis += checkoutTimeMillis;
    }
    
    public void decrAccumulatedOverdueConnectionCheckoutTimeMillis(long checkoutTimeMillis) {
        this.accumulatedOverdueConnectionCheckoutTimeMillis += checkoutTimeMillis;
    }
    
    // accumulatedWaitTimeMillis
    public long getAccumulatedConnectionWaitTimeMillis() {
        return accumulatedConnectionWaitTimeMillis;
    }
    
    public void incrAccumulatedWaitTimeMillis(long waitCount) {
        accumulatedConnectionWaitTimeMillis += waitCount;
    }
    
    public void decrAccumulatedWaitTimeMillis(long waitCount) {
        accumulatedConnectionWaitTimeMillis -= waitCount;
    }
    
    // requestCount
    public long getConnectionRequestCount() {
        return connectionRequestCount;
    }
    
    public void incrConnectionRequestCount() {
        this.connectionRequestCount++;
    }
    
    public void decrConnectionRequestCount() {
        this.connectionRequestCount--;
    }
    
    // waitCount
    public long getConnectionWaitCount() {
        return connectionWaitCount;
    }
    
    public void incrConnectionWaitCount() {
        this.connectionWaitCount++;
    }
    
    public void decrConnectionWaitCount() {
        this.connectionWaitCount--;
    }
    
    // claimedOverdueConnectionCount
    public long getOverdueConnectionCount() {
        return overdueConnectionCount;
    }
    
    public void incrOverdueConnectionCount() {
        this.overdueConnectionCount++;
    }
    
    public void decrOverdueConnectionCount() {
        this.overdueConnectionCount--;
    }
    
    // badConnectionCount
    public long getBadConnectionCount() {
        return badConnectionCount;
    }
    
    public void incrBadConnectionCount() {
        this.badConnectionCount++;
    }
    
    public void decrBadConnectionCount() {
        this.badConnectionCount--;
    }
}
