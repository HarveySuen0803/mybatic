package com.harvey.mybatic.executor;

import com.harvey.mybatic.mapping.MybaticRuntimeHolder;
import com.harvey.mybatic.transaction.Transaction;

/**
 * @author harvey
 */
public class ExecutorFactory {
    public static Executor newExecutor(Transaction transaction) {
        return new SimpleExecutor(MybaticRuntimeHolder.getInstance(), transaction);
    }
}
