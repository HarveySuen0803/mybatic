package com.harvey.mybatic.transaction;

import java.sql.Connection;

/**
 * @author harvey
 */
public enum TransactionIsolationLevelEnum {
    NONE(Connection.TRANSACTION_NONE),
    READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),
    READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
    REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),
    SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE);
    
    private final int level;
    
    TransactionIsolationLevelEnum(int level) {
        this.level = level;
    }
    
    public int getLevel() {
        return level;
    }
}
