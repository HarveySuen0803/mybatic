package com.harvey.mybatic.executor;

import com.harvey.mybatic.mapping.MappedStatement;

/**
 * @author harvey
 */
public class StatementHandlerFactory {
    public static StatementHandler newStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameter) {
        return new PreparedStatementHandler(executor, mappedStatement, parameter);
    }
}
