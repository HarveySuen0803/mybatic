package com.harvey.mybatic.executor;

import com.harvey.mybatic.mapping.BoundSql;
import com.harvey.mybatic.mapping.MappedStatement;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @author harvey
 */
public class SimpleStatementHandler extends BaseStatementHandler {
    public SimpleStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameter) {
        super(executor, mappedStatement, parameter);
    }
    
    @Override
    public Statement newStatement(Connection connection, BoundSql boundSql) throws SQLException {
        Statement statement = connection.createStatement();
        statement.setQueryTimeout(350);
        statement.setFetchSize(10000);
        return statement;
    }
    
    @Override
    public void parameterize(Statement statement, Object parameter) {
    }
    
    @Override
    protected <T> List<T> execute(Statement statement, BoundSql boundSql, ResultSetHandler resultSetHandler) throws SQLException {
        String sql = boundSql.getSql();
        statement.execute(sql);
        ResultSet resultSet = statement.getResultSet();
        String resultType = boundSql.getResultType();
        return resultSetHandler.handleResultSet(resultSet, resultType);
    }
}
