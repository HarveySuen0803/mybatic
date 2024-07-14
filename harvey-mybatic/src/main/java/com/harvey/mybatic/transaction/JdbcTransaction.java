package com.harvey.mybatic.transaction;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author harvey
 */
public class JdbcTransaction implements Transaction {
    
    protected Connection connection;
    
    protected DataSource dataSource;
    
    protected TransactionIsolationLevelEnum isolationLevel = TransactionIsolationLevelEnum.NONE;
    
    protected boolean autoCommit;
    
    public JdbcTransaction(Connection connection) {
        this.connection = connection;
    }
    
    public JdbcTransaction(DataSource dataSource, TransactionIsolationLevelEnum isolationLevel, boolean autoCommit) {
        this.dataSource = dataSource;
        this.isolationLevel = isolationLevel;
        this.autoCommit = autoCommit;
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        connection = dataSource.getConnection();
        connection.setTransactionIsolation(isolationLevel.getLevel());
        connection.setAutoCommit(autoCommit);
        return connection;
    }
    
    @Override
    public void commit() throws SQLException {
        if (connection == null) {
            return;
        }
        
        if (connection.getAutoCommit()) {
            return;
        }
        
        connection.commit();
    }
    
    @Override
    public void rollback() throws SQLException {
    
    }
    
    @Override
    public void close() throws SQLException {
    
    }
}
