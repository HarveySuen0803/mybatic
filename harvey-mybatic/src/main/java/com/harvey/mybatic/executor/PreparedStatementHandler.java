package com.harvey.mybatic.executor;

import com.harvey.mybatic.mapping.BoundSql;
import com.harvey.mybatic.mapping.MappedStatement;

import java.sql.*;
import java.util.List;

/**
 * @author harvey
 */
public class PreparedStatementHandler extends BaseStatementHandler {
    public PreparedStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameter) {
        super(executor, mappedStatement, parameter);
    }
    
    @Override
    public Statement newStatement(Connection connection, BoundSql boundSql) throws SQLException {
        String sql = boundSql.getSql();
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setQueryTimeout(350);
        preparedStatement.setFetchSize(10000);
        return preparedStatement;
    }
    
    @Override
    protected void parameterize(Statement statement, Object parameter) throws SQLException {
        PreparedStatement preparedStatement = (PreparedStatement) statement;
        preparedStatement.setLong(1, Long.parseLong(((Object[]) parameter)[0].toString()));
    }
    
    @Override
    protected <T> List<T> execute(Statement statement, BoundSql boundSql, ResultSetHandler resultSetHandler) throws SQLException {
        PreparedStatement preparedStatement = (PreparedStatement) statement;
        preparedStatement.execute();
        ResultSet resultSet = preparedStatement.getResultSet();
        String resultType = boundSql.getResultType();
        return resultSetHandler.handleResultSet(resultSet, resultType);
    }
}
