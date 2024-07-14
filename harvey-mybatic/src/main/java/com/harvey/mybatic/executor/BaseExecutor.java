package com.harvey.mybatic.executor;

import com.harvey.mybatic.mapping.BoundSql;
import com.harvey.mybatic.mapping.MappedStatement;
import com.harvey.mybatic.mapping.MybaticRuntime;
import com.harvey.mybatic.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

/**
 * @author harvey
 */
public abstract class BaseExecutor implements Executor {
    private static final Logger logger = LoggerFactory.getLogger(BaseExecutor.class);
    
    private MybaticRuntime mybaticRuntime;
    
    private Transaction transaction;
    
    private boolean isClosed;
    
    public BaseExecutor(MybaticRuntime mybaticRuntime, Transaction transaction) {
        this.mybaticRuntime = mybaticRuntime;
        this.transaction = transaction;
    }
    
    @Override
    public <E> List<E> query(MappedStatement mappedStatement, Object parameter) {
        return doQuery(mappedStatement, transaction, parameter);
    }
    
    public abstract <E> List<E> doQuery(MappedStatement mappedStatement, Transaction transaction, Object parameter);
    
    @Override
    public Transaction getTransaction() {
        if (isClosed) {
            throw new RuntimeException("Executor was closed.");
        }
        
        return transaction;
    }
    
    @Override
    public void commit(boolean isRequire) throws SQLException {
        if (isClosed) {
            throw new RuntimeException("Cannot commit, transaction is already closed");
        }
        
        if (!isRequire) {
            return;
        }
        
        this.transaction.commit();
    }
    
    @Override
    public void rollback(boolean isRequire) throws SQLException {
        if (isClosed) {
            throw new RuntimeException("Cannot rollback, transaction is already closed");
        }
        
        if (!isRequire) {
            return;
        }
        
        transaction.rollback();
    }
    
    @Override
    public void close(boolean isRollBack) throws SQLException {
        try {
            rollback(true);
            transaction.close();
        } catch (SQLException e) {
            logger.warn("Unexpected exception on closing transaction.  Cause: {}", String.valueOf(e));
        } finally {
            transaction = null;
            isClosed = true;
        }
    }
}
