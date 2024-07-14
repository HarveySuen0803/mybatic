package com.harvey.mybatic.executor;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @author harvey
 */
public interface StatementHandler {
    <T> List<T> query(Connection connection) throws SQLException;
}
