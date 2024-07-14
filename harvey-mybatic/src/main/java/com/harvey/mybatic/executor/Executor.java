package com.harvey.mybatic.executor;

import com.harvey.mybatic.mapping.BoundSql;
import com.harvey.mybatic.mapping.MappedStatement;
import com.harvey.mybatic.transaction.Transaction;

import java.sql.SQLException;
import java.util.List;

/**
 * @author harvey
 */
public interface Executor {
    <E> List<E> query(MappedStatement mappedStatement, Object parameter);
    
    Transaction getTransaction();
    
    void commit(boolean isRequire) throws SQLException;
    
    void rollback(boolean isRequire) throws SQLException;
    
    void close(boolean isRollBack) throws SQLException;
}
