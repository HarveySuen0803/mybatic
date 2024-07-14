package com.harvey.mybatic.executor;

import com.harvey.mybatic.mapping.BoundSql;
import com.harvey.mybatic.mapping.MappedStatement;
import com.harvey.mybatic.mapping.ModelMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author harvey
 */
public class DefaultResultSetHandler implements ResultSetHandler {
    @Override
    public <T> List<T> handleResultSet(ResultSet resultSet, String resultType) throws SQLException {
        Class<?> resultClass;
        try {
            resultClass = Class.forName(resultType);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return ModelMapper.toBeanList(resultSet, resultClass);
    }
}
