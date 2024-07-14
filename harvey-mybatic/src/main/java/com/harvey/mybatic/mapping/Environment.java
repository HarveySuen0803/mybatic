package com.harvey.mybatic.mapping;

import com.harvey.mybatic.transaction.TransactionFactory;

import javax.sql.DataSource;

/**
 * @author harvey
 */
public class Environment {
    private final String id;
    
    private final TransactionFactory transactionFactory;
    
    private final DataSource dataSource;
    
    public Environment(String id, TransactionFactory transactionFactory, DataSource dataSource) {
        this.id = id;
        this.transactionFactory = transactionFactory;
        this.dataSource = dataSource;
    }
    
    public static class Builder {
        private String id;
        
        private TransactionFactory transactionFactory;
        
        private DataSource dataSource;
        
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        
        public Builder transactionFactory(TransactionFactory transactionFactory) {
            this.transactionFactory = transactionFactory;
            return this;
        }
        
        public Builder dataSource(DataSource dataSource) {
            this.dataSource = dataSource;
            return this;
        }
        
        public Environment build() {
            return new Environment(id, transactionFactory, dataSource);
        }
    }
    
    public String getId() {
        return id;
    }
    
    public TransactionFactory getTransactionFactory() {
        return transactionFactory;
    }
    
    public DataSource getDataSource() {
        return dataSource;
    }
}
