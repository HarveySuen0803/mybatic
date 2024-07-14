package com.harvey.mybatic.executor;

import com.harvey.mybatic.mapping.BoundSql;
import com.harvey.mybatic.mapping.MappedStatement;
import com.harvey.mybatic.mapping.MybaticRuntime;
import com.harvey.mybatic.mapping.MybaticRuntimeHolder;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @author harvey
 */
public abstract class BaseStatementHandler implements StatementHandler{
    private final Executor executor;
    
    private final MappedStatement mappedStatement;
    
    private final MybaticRuntime mybaticRuntime;
    
    private final Object parameter;
    
    private final ResultSetHandler resultSetHandler;
    
    public BaseStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameter) {
        this.executor = executor;
        this.mappedStatement = mappedStatement;
        this.mybaticRuntime = MybaticRuntimeHolder.getInstance();
        this.parameter = parameter;
        this.resultSetHandler = ResultSetHandlerFactory.newResultSetHandler();
    }
    
    @Override
    public <T> List<T> query(Connection connection) throws SQLException {
        Statement statement = this.newStatement(connection, this.mappedStatement.getBoundSql());
        
        this.parameterize(statement, parameter);
        
        return this.execute(statement, this.mappedStatement.getBoundSql(), this.resultSetHandler);
    }
    
    protected abstract Statement newStatement(Connection connection, BoundSql boundSql) throws SQLException;
    
    protected abstract void parameterize(Statement statement, Object parameter) throws SQLException;
    
    protected abstract <T> List<T> execute(Statement statement, BoundSql boundSql, ResultSetHandler resultSetHandler) throws SQLException;
}
