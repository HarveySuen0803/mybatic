package com.harvey.mybatic.executor;

/**
 * @author harvey
 */
public class ResultSetHandlerFactory {
    public static ResultSetHandler newResultSetHandler() {
        return new DefaultResultSetHandler();
    }
}
