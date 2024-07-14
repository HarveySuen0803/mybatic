package com.harvey.mybatic.executor;

import com.harvey.mybatic.mapping.BoundSql;
import com.harvey.mybatic.mapping.MappedStatement;
import com.harvey.mybatic.mapping.MybaticRuntime;
import com.harvey.mybatic.mapping.MybaticRuntimeHolder;
import com.harvey.mybatic.transaction.Transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * @author harvey
 */
public class SimpleExecutor extends BaseExecutor {
    public SimpleExecutor(MybaticRuntime mybaticRuntime, Transaction transaction) {
        super(mybaticRuntime, transaction);
    }
    
    @Override
    public <E> List<E> doQuery(MappedStatement mappedStatement, Transaction transaction, Object parameter) {
        try {
            MybaticRuntime mybaticRuntime = MybaticRuntimeHolder.getInstance();
            Connection connection = transaction.getConnection();
            Executor executor = ExecutorFactory.newExecutor(transaction);
            StatementHandler statementHandler = StatementHandlerFactory.newStatementHandler(executor, mappedStatement, parameter);
            return statementHandler.query(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
