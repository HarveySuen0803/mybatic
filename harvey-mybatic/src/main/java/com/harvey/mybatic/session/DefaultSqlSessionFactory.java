package com.harvey.mybatic.session;

import com.harvey.mybatic.executor.Executor;
import com.harvey.mybatic.executor.ExecutorFactory;
import com.harvey.mybatic.mapping.Environment;
import com.harvey.mybatic.mapping.MybaticRuntime;
import com.harvey.mybatic.transaction.Transaction;
import com.harvey.mybatic.transaction.TransactionFactory;
import com.harvey.mybatic.transaction.TransactionIsolationLevelEnum;

import javax.sql.DataSource;

/**
 * @author harvey
 */
public class DefaultSqlSessionFactory implements SqlSessionFactory {
    private final MybaticRuntime mybaticRuntime;
    
    public DefaultSqlSessionFactory(MybaticRuntime mybaticRuntime) {
        this.mybaticRuntime = mybaticRuntime;
    }
    
    @Override
    public SqlSession getSqlSession() {
        Environment environment = mybaticRuntime.getEnvironment();
        DataSource dataSource = environment.getDataSource();
        TransactionFactory transactionFactory = environment.getTransactionFactory();
        Transaction transaction = transactionFactory.newTransaction(dataSource, TransactionIsolationLevelEnum.READ_COMMITTED, false);
        Executor executor = ExecutorFactory.newExecutor(transaction);
        return new DefaultSqlSession(mybaticRuntime, executor);
    }
}
