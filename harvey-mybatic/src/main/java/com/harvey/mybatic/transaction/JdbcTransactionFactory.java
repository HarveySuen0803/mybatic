package com.harvey.mybatic.transaction;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * @author harvey
 */
public class JdbcTransactionFactory implements TransactionFactory {
    @Override
    public Transaction newTransaction(Connection connection) {
        return new JdbcTransaction(connection);
    }
    
    @Override
    public Transaction newTransaction(DataSource dataSource, TransactionIsolationLevelEnum level, boolean autoCommit) {
        return new JdbcTransaction(dataSource, level, autoCommit);
    }
}
