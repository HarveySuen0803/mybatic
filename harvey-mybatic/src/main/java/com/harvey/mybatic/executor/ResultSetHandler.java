package com.harvey.mybatic.executor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @author harvey
 */
public interface ResultSetHandler {
    <T> List<T> handleResultSet(ResultSet resultSet, String resultType) throws SQLException;
}
