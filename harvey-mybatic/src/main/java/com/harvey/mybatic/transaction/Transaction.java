package com.harvey.mybatic.transaction;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author harvey
 */
public interface Transaction {
    Connection getConnection() throws SQLException;
    
    void commit() throws SQLException;
    
    void rollback() throws SQLException;
    
    void close() throws SQLException;
}
