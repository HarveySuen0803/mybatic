package com.harvey.mybatic.transaction;

import javax.sql.DataSource;
import java.sql.Connection;

public interface TransactionFactory {
    Transaction newTransaction(Connection connection);

    Transaction newTransaction(DataSource dataSource, TransactionIsolationLevelEnum level, boolean autoCommit);
}